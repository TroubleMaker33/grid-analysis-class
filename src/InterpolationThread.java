import org.apache.commons.math3.linear.*;
import supportFunction.DataBase;
import supportFunction.Grid;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class InterpolationThread implements Callable<Grid> {
    private String method;
    private ArrayList<Double> gaugeX;
    private ArrayList<Double> gaugeY;
    private String gaugeP;
    private int N;
    private double range=50;
    private int a=1;
    private int b=10000;
    private int power=2;
    private int degree=3;
    private String model = "sperical";
    private Grid result;
    private int Index;
    private double[][] mask;
    private String Path = "./data";
    private boolean usingThread;
    private Grid output;
    private supportFunction.DataBase DataBase;

    public InterpolationThread(String method, ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, String gaugeP, int N, Grid input_Grid, int Index, double[][] mask, boolean usingThread, DataBase DataBase){
        this.method = method;
        this.gaugeX = gaugeX;
        this.gaugeY = gaugeY;
        this.gaugeP = gaugeP;
        this.N = N;
        this.result = input_Grid;
        this.Index = Index;
        this.mask = mask;
        this.usingThread = usingThread;
        this.DataBase = DataBase;
    }

    @Override
    public Grid call() {
        ArrayList<Double> P = new ArrayList<Double>(N);
        double[] PrecipInfo=result.getPrecipInfo(gaugeP,Index,N);
        for(int i=0;i<N;i++){
            P.add(PrecipInfo[i+2]);
        }

        switch(method){
            case("Thiessen"):  //泰森多边形
                System.out.println("泰森多边形："+Index);
                Thiessen(gaugeX, gaugeY, P, N, range, result,mask);
                break;
            case("IDW"):  // 反距离权重，IDWpower为计算权重的幂
                System.out.println("反距离权重："+Index);
                IDW(gaugeX,gaugeY, P, N, range, result,mask,power);
                break;
            case("Kriging"):  // 克里金插值，半变异模型model可选："sperical"、"gaussian"、"exponential"
                System.out.println("克里金："+Index);
                Kriging(gaugeX, gaugeY, P, N, model, result,range,mask);
                break;
            case("Spline"):  // 样条函数
                System.out.println("样条函数："+Index);
                Spline(gaugeX, gaugeY, P, N, range, result,mask);
                break;
            case("trendSurface"):  // 趋势面，参数为拟合的次方
                System.out.println("趋势面："+Index);
                trendSurfaceInterpolation(gaugeX,gaugeY, P, N, degree, result,range,mask);
                break;
            case("RBF"):  // 径向基函数，参数为高斯函数的方差
                System.out.println("径向基函数："+Index);
                RBF(gaugeX, gaugeY, P, N, range, result,mask);
                break;
            case("Lanczos"):  // Lanczos插值
                System.out.println("Lanczos插值："+Index);
                Lanczos(gaugeX, gaugeY, P, N, result,range,mask,a,b);
                break;
        }
        return this.output;
    }
    // --------------------------------------------泰森多边形部分--------------------------------------------
    void Thiessen(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N, double range, Grid input_Grid, double[][] mask) {
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];
            double[][] distance = new double[input_Grid.getROW()][input_Grid.getCOLUMN()];

            Grid output_Grid = new Grid(input_Grid);

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COLUMN; j++) {
                    double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                    double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                    for (int n = 0; n < N; n++) {
                        double distance_temp = Math.pow((centerX - gaugeX.get(n)), 2)
                                + Math.pow((centerY - gaugeY.get(n)), 2);
                        if (distance[i][j] != 0 && distance_temp <= distance[i][j]) {
                            distance[i][j] = distance_temp;
                            result[ROW - i - 1][j] = P.get(n);
                        } else if (distance[i][j] == 0) {
                            distance[i][j] = distance_temp;
                            result[ROW - i - 1][j] = P.get(n);
                        }
                    }
                }
            }
            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }

            DataBase.insertGrid(outputFile,method,output_Grid);
    }

    //-----------------------------------------样条函数插值部分-----------------------------------------
    void Spline(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N, double range, Grid input_Grid, double[][] mask) {
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];

            Grid output_Grid = new Grid(input_Grid);

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COLUMN; j++) {
                    double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                    double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                    result[ROW-i-1][j] = splineLocal(gaugeX, gaugeY, P, centerX, centerY, N);
                }
            }

            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }

        DataBase.insertGrid(outputFile,method,output_Grid);
    }

    double splineLocal(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, double centerX, double centerY, int N){
        double[][] A = new double[N+3][N+3];
        double B[] = new double[N+3];

        for (int i=0; i<N; i++){
            A[i][0] = 1.0;
            A[i][1] = gaugeX.get(i);
            A[i][2] = gaugeY.get(i);
        }
        for (int i=0; i<N; i++){
            for (var j=3; j<N+3; j++){
                if (i!=j-3){
                    A[i][j] = splineRFunc(gaugeX.get(i), gaugeY.get(i), gaugeX.get(j-3), gaugeY.get(j-3));
                }
                else{
                    A[i][j] = 0;
                }
            }
        }

        for (int j=3; j<N+3; j++){
            A[N][j] = 1;
            A[N+1][j] = gaugeX.get(j-3);
            A[N+2][j] = gaugeY.get(j-3);
        }

        for (int i=0; i<N; i++){
            B[i] = P.get(i);
        }

        RealMatrix A_apache = new Array2DRowRealMatrix(A, true);
        RealVector B_apache = new ArrayRealVector(B);
        DecompositionSolver solver = new LUDecomposition(A_apache).getSolver();
        RealVector coef = solver.solve(B_apache);
        // double[] coef = solve(A, B);
        double value = coef.getEntry(0) + coef.getEntry(1)*centerX + coef.getEntry(2)*centerY;
        for (int i=0;i<N;i++){
            value += coef.getEntry(i+3)*splineRFunc(centerX, centerY, gaugeX.get(i), gaugeY.get(i));
        }
        return value;
    }

    double splineRFunc(double x1, double y1, double x2, double y2){
        double d = distance(x1,y1,x2,y2)/1000;
        // double c = 0.577215;
        // double tau = 0.2;
        // BesselJ K = new BesselJ(0);
        double splineRDistance = Math.pow(d,2)*Math.log10(d);
        // double splineRDistance = (1.0/(2.0*Math.PI))*((Math.pow(d,2)/4.0)*(Math.log(d/(2*tau))+c-1)+(Math.pow(tau,2))*(K.value(d/tau)+c+Math.log(d/(2.0*Math.PI))));
        return splineRDistance;
    }

    // --------------------------------------------反距离权重部分--------------------------------------------

    // 每一个的插值
    private double interpolateIDW(double x, double y, ArrayList<Double> gaugeX, ArrayList<Double> gaugeY,
                                  ArrayList<Double> P, int N, int IDWpower) {

        // 计算权重
        double[] weights = new double[N];
        double sumWeights = 0.0;
        for (int i = 0; i < N; i++) {
            weights[i] = 1.0 / Math.pow(distance(x, y, gaugeX.get(i), gaugeY.get(i)), IDWpower);
            sumWeights = sumWeights + weights[i];
        }
        for (int i = 0; i < N; i++) {
            weights[i] = weights[i] / sumWeights;
        }

        // 计算估计值
        double estimate = 0;
        for (int i = 0; i < N; i++) {
            estimate += weights[i] * P.get(i);
        }

        return estimate;
    }

    void IDW(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N, double range, Grid input_Grid, double[][] mask, int IDWpower) {
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];

            Grid output_Grid = new Grid(input_Grid);

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COLUMN; j++) {
                    double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                    double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                    result[ROW - i - 1][j] = interpolateIDW(centerX, centerY, gaugeX, gaugeY, P, N, IDWpower);
                }
            }
            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }
        DataBase.insertGrid(outputFile,method,output_Grid);
    }

    // --------------------------------------------克里金插值部分--------------------------------------------
    // 定义半变异函数
    private double semivariogram(double h, String model) {
        // 半变异函数参数
        double range = 250000;
        double sill = 1;
        double nugget = 0.1;

        double semivariance = 0;
        switch (model) {
            case ("sperical"): // 球形模型
                if (h < range) {
                    semivariance = nugget + sill * (3 * h / 2 / range - 0.5 * Math.pow(h / range, 3));
                } else {
                    semivariance = nugget + sill;
                }
                break;
            case ("gaussian"): // 高斯模型
                semivariance = nugget + sill * (1.0 - Math.exp(-1.0 * Math.pow(h / range, 2)));
                break;
            case ("exponential"): // 指数模型
                semivariance = nugget + sill * (1.0 - Math.exp(-1.0 * (h / range)));
                break;
        }
        return semivariance;
    }

    // 高斯消元法解线性方程组
    public static double[] solve(double[][] A, double[] B) {
        int n = A.length;
        double[] X = new double[n];

        for (int k = 0; k < n - 1; k++) {
            for (int i = k + 1; i < n; i++) {
                double factor = A[i][k] / A[k][k];
                B[i] -= factor * B[k];
                for (int j = k; j < n; j++) {
                    A[i][j] -= factor * A[k][j];
                }
            }
        }

        // 回代求解
        X[n - 1] = B[n - 1] / A[n - 1][n - 1];
        for (int i = n - 2; i >= 0; i--) {
            double sum = B[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * X[j];
            }
            X[i] = sum / A[i][i];
        }

        return X;
    }

    // 计算一个点的插值结果
    private double interpolateKriging(double x, double y, ArrayList<Double> gaugeX, ArrayList<Double> gaugeY,
                                      ArrayList<Double> P, int N, String model) {
        double[][] matrix = new double[N + 1][N + 1];
        double[] vector = new double[N + 1];

        // 半变异方程的matrix和vector
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // distance函数在反距离权重部分
                double dist = distance(gaugeX.get(i), gaugeY.get(i), gaugeX.get(j), gaugeY.get(j));
                matrix[i][j] = semivariogram(dist, model);
            }
            matrix[i][N] = 1.0;
            matrix[N][i] = 1.0;

            double distToUnknown = distance(gaugeX.get(i), gaugeY.get(i), x, y);
            vector[i] = semivariogram(distToUnknown, model);
        }
        matrix[N][N] = 0.0;
        vector[N] = 1.0;

        // 解方程
        double[] weights = solve(matrix, vector); // This should be solved from the linear system: matrix * weights =
        // vector

        // 计算估计值
        double estimate = 0;
        for (int i = 0; i < N; i++) {
            estimate += weights[i] * P.get(i);
        }

        return estimate;
    }

    void Kriging(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N, String model, Grid input_Grid, double range, double[][] mask) {
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];

            Grid output_Grid = new Grid(input_Grid);

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COLUMN; j++) {
                    double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                    double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                    result[ROW - i - 1][j] = interpolateKriging(centerX, centerY, gaugeX, gaugeY, P, N, model);
                }
            }

            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }

        DataBase.insertGrid(outputFile,method,output_Grid);
    }

    // -----------------------------------------径向基函数插值部分-----------------------------------------
    void RBF(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N, double range, Grid input_Grid, double[][] mask) {
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];

            Grid output_Grid = new Grid(input_Grid);

            ArrayList<double[][]> pGaussianLayer = new ArrayList<double[][]>(N);
            double[][] pGaussianValue = new double[N][N];
            double PValue[] = new double[N];
            for (int n = 0; n < N; n++) {
                double[][] ipGaussianLayer = pGaussian(gaugeX.get(n), gaugeY.get(n), range, output_Grid);
                pGaussianLayer.add(ipGaussianLayer);
                for (int iPoint = 0; iPoint < N; iPoint++) {
                    // pGaussianValue[iPoint][n] = ipGaussianLayer[(int) (gaugeY.get(iPoint) - CELL_CORNER_Y) / CELL_SIZE
                    //         - 1][(int) (gaugeX.get(iPoint) - CELL_CORNER_X) / CELL_SIZE - 1];
                    double distance_temp = distance(gaugeX.get(iPoint), gaugeY.get(iPoint), gaugeX.get(n), gaugeY.get(n));
                    pGaussianValue[iPoint][n] = Math.exp(-1.0 * distance_temp / (2.0 * range * range));
                }
                PValue[n] = P.get(n);
            }
            RealMatrix A_apache = new Array2DRowRealMatrix(pGaussianValue, true);
            RealVector B_apache = new ArrayRealVector(PValue);
            DecompositionSolver solver = new LUDecomposition(A_apache).getSolver();
            RealVector coef = solver.solve(B_apache);

            for (int i = ROW - 1; i >= 0; i--) {
                for (int j = 0; j < COLUMN; j++) {
                    for (int iLayer = 0; iLayer < N; iLayer++) {
                        double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                        double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                        double distance_temp = distance(centerX, centerY, gaugeX.get(iLayer), gaugeY.get(iLayer));
                        result[ROW-i-1][j] += (Math.exp(-1.0 * distance_temp / (2.0 * range * range))) * coef.getEntry(iLayer);
                    }
                    // System.out.printf("%.2f\t", result[i][j]);
                }
                // System.out.println("");
            }
            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }
        DataBase.insertGrid(outputFile,method,output_Grid);
    }

    double[][] pGaussian(double igaugeX, double igaugeY, double range, Grid input_Grid) {
        int ROW = input_Grid.getROW();
        int COLUMN = input_Grid.getCOLUMN();
        double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
        double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
        int CELL_SIZE = input_Grid.getCELL_SIZE();
        double[][] pGaussianLayer = input_Grid.getData();
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                double distance = Math.pow(Math.pow((centerX - igaugeX), 2) + Math.pow((centerY - igaugeY), 2), 0.5);
                pGaussianLayer[i][j] = Math.exp(-1.0 * distance / (2.0 * range * range));
            }
        }
        return pGaussianLayer;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    // -----------------------------------------趋势面分析------------------------------------------------------------
    void trendSurfaceInterpolation(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N,
                                   int degree, Grid input_Grid, double range, double[][]mask) {

        // 最小二乘法求解
        double[] coefficients = leastSquaresFit(gaugeX, gaugeY, P, degree);

        // 输出趋势面插值结果
        System.out.println(degree + "次趋势面：");
        // 输出插值系数
        printInterpolationResults(coefficients);
            // 计算每个栅格的降水量并输出
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];

            Grid output_Grid = new Grid(input_Grid);
            result = calculateAndPrintPrecipitation(gaugeX, gaugeY, degree, coefficients, input_Grid);
            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }

        DataBase.insertGrid(outputFile,method,output_Grid);
    }

    //
    double[] leastSquaresFit(ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> P, int degree) {
        int n = x.size();
        int terms = (degree + 1) * (degree + 2) / 2;

        double[] coefficients = new double[terms];

        double[][] A = new double[terms][terms];
        double[] B = new double[terms];

        // 构建矩阵A和向量B
        double[][] basicTerms = generateBasicTerms(x, y, degree);

        for (int i = 0; i < terms; i++) {
            for (int j = 0; j < terms; j++) {
                A[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    A[i][j] += basicTerms[k][i] * basicTerms[k][j]; // 循环迭代所有的数据点,计算基本项之间的内积
                }
            }
            B[i] = 0;
            for (int k = 0; k < n; k++) {
                B[i] += P.get(k) * basicTerms[k][i];// 利用观测值和基本项的值
            }
        }

        // 解线性方程组，得到多项式系数
        solve(A, B, coefficients);

        return coefficients;
    }

    // 基本项的生成即:一次曲面(常数,x,y),二次曲面(常数,x,y,xy,x^2,y^2)
    double[][] generateBasicTerms(ArrayList<Double> x, ArrayList<Double> y, int degree) {// 基本项的生成即,一次曲面(常数,x,y),二次曲面(常数,x,y,xy,x^2,y^2)
        int n = x.size();
        int terms = (degree + 1) * (degree + 2) / 2; // (degree + 1) choose 2

        double[][] basicTerms = new double[n][terms];
        int idx = 0;

        for (int i = 0; i <= degree; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k < n; k++) {
                    basicTerms[k][idx] = Math.pow(x.get(k), i - j) * Math.pow(y.get(k), j);
                }
                idx++;
            }
        }

        return basicTerms;
    }

    // 高斯消元解线性方程
    void solve(double[][] A, double[] B, double[] X) {
        int n = A.length;

        for (int k = 0; k < n - 1; k++) {
            for (int i = k + 1; i < n; i++) {
                double factor = A[i][k] / A[k][k];
                B[i] -= factor * B[k];
                for (int j = k; j < n; j++) {
                    A[i][j] -= factor * A[k][j];
                }
            }
        }

        // 回代求解
        X[n - 1] = B[n - 1] / A[n - 1][n - 1];
        for (int i = n - 2; i >= 0; i--) {
            double sum = B[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * X[j];
            }
            X[i] = sum / A[i][i];
        }
    }

    // 输出插值系数(曲面的系数0
    void printInterpolationResults(double[] coefficients) {
        System.out.println("插值系数：");
        for (int i = 0; i < coefficients.length; i++) {
            System.out.println("Coefficient " + i + ": " + coefficients[i]);
        }
    }

    // 计算每个栅格的降水量并输出
    double[][] calculateAndPrintPrecipitation(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, int degree,
                                              double[] coefficients, Grid input_Grid) {

        int ROW = input_Grid.getROW();
        int COLUMN = input_Grid.getCOLUMN();
        double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
        double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
        int CELL_SIZE = input_Grid.getCELL_SIZE();
        double[][] result = new double[ROW][COLUMN];

        // 计算每个栅格的降水量
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                result[ROW - i - 1][j] = interpolateTrendSurface(centerX, centerY, coefficients, degree);
            }
        }

        return result;
    }

    // 根据曲面对剩余位置进行插值
    double interpolateTrendSurface(double x, double y, double[] coefficients, int degree) {
        double result = 0;
        // int terms = coefficients.length;
        int idx = 0;
        for (int i = 0; i <= degree; i++) {
            for (int j = 0; j <= i; j++) {
                result += coefficients[idx] * Math.pow(x, i - j) * Math.pow(y, j);
                idx++;
            }
        }
        return result;
    }

    // -----------------------------------------Lanczos插值------------------------------------------------------------
    // 核函数
    double lanczosKernel(double x, int a,int b) {
        x=x/b;
        if (x == 0) {
            return 1.0;
        }
        if (x > a || x<-a) {
            return 0.0;
        }
        return (a * Math.sin(Math.PI * x) * Math.sin(Math.PI * x / a)) / (Math.PI * Math.PI * x * x);

    }

    // 求插值
    double interpolate(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, double xi, double yi, int a, int b,Grid input_Grid) {
        double numerator = 0.0;
        double denominator = 0.0;
        for (int n = 0; n < gaugeX.size(); n++) {
            double distance = Math.sqrt(Math.pow(xi - gaugeX.get(n), 2) + Math.pow(yi - gaugeY.get(n), 2));
            double weight = lanczosKernel(distance, a,b);
            // 与权重相乘相加
            numerator += weight * P.get(n);
            denominator += weight;
        }
        if (denominator != 0) {
            return numerator / denominator;
            //return numerator;
        } else {
            return input_Grid.getNODATA_VALUE();
        }
    }

    // lanczos插值函数
    void Lanczos(ArrayList<Double> gaugeX, ArrayList<Double> gaugeY, ArrayList<Double> P, int N, Grid input_Grid, double range, double[][]mask,int a,int b) {
            int ROW = input_Grid.getROW();
            int COLUMN = input_Grid.getCOLUMN();
            double CELL_CORNER_X = input_Grid.getCELL_CORNER_X();
            double CELL_CORNER_Y = input_Grid.getCELL_CORNER_Y();
            int CELL_SIZE = input_Grid.getCELL_SIZE();
            double[][] result = new double[ROW][COLUMN];

            Grid output_Grid = new Grid(input_Grid);

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COLUMN; j++) {
                    double centerX = CELL_CORNER_X + 0.5 * CELL_SIZE + j * CELL_SIZE;
                    double centerY = CELL_CORNER_Y + 0.5 * CELL_SIZE + i * CELL_SIZE;
                    result[ROW - i - 1][j] = interpolate(gaugeX,gaugeY,P,centerX,centerY,a,b,input_Grid);
                }
            }

            output_Grid.setData(result);
            String outputFile = Path + "/" + method + "_" +Index+ ".csv";
            output_Grid.plot(gaugeX, gaugeY, P, CELL_CORNER_X, CELL_CORNER_Y, CELL_SIZE, mask);
            output_Grid.writefiles(outputFile);
            if (!usingThread){
                this.output = output_Grid;
            }
        DataBase.insertGrid(outputFile,method,output_Grid);
    }
    public void setA(int a) {
        this.a = a;}
    public void setB(int b) {
        this.b = b;}
    // -----------------------------------------------------------------------------

    public void setIndex(int index) {
        this.Index = index;
    }

    public void setGaugeX(ArrayList<Double> gaugeX) {
        this.gaugeX = gaugeX;
    }

    public void setGaugeY(ArrayList<Double> gaugeY) {
        this.gaugeY = gaugeY;
    }

    public void setN(int n) {
        N = n;
    }

    public void setGaugeP(String p) {
        gaugeP = p;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setPower(int power) {
        this.power = power;}
    public void setModel(String model){
        this.model=model;
    }
    public void setDegree(int degree) {
        this.degree = degree;}

    public void setResult(Grid result) {
        this.result = result;
    }

    public void setMask(double[][] mask) {
        this.mask = mask;
    }
}
