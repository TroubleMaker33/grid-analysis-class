package supportFunction;

import RainfallStation.RainfallStation;

import Jama.Matrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.special.BesselJ;


public class supportFunction {
    public static double indexWithCellcornerForDistance(double cellSize, int i, int j, int di, int dj) {
        //求解两点的欧氏距离
        double X = cellSize * (i - di);
        double Y = cellSize * (j - dj);
        return Math.sqrt(X * X + Y * Y);
    }

    public static double inverseDistanceWeightingForRainfall(ArrayList<ArrayList<Double>> Rainfall, ArrayList<Double> standardX
            , ArrayList<Double> standardY, int i, int j, double cellSize, int standardNumber, ArrayList<Double> cellcorner) {
        //反距离权重法
        double[] standardDistanceInverse = new double[standardNumber];
        for (int n = 0; n < standardNumber; n++) {
            double d = indexWithCellcornerForDistance(cellSize, i, j, (int) ((standardY.get(n) - cellcorner.get(1)) / cellSize),
                    (int) ((standardX.get(n) - cellcorner.get(0)) / cellSize));
            standardDistanceInverse[n] = 1 / (d * d);
        }
        double totalDistanceInverse = 0;
        for (int n = 0; n < standardNumber; n++) {
            totalDistanceInverse = totalDistanceInverse + standardDistanceInverse[n];
        }
        double[] weightForRainfall = new double[standardNumber];
        for (int n = 0; n < standardNumber; n++) {
            weightForRainfall[n] = standardDistanceInverse[n] / totalDistanceInverse;
        }
        double resultRainfall = 0;
        for (int n = 0; n < standardNumber; n++) {
            resultRainfall = resultRainfall + weightForRainfall[n] *
                    Rainfall.get((int) ((standardY.get(n) - cellcorner.get(1)) / cellSize))
                            .get((int) ((standardX.get(n) - cellcorner.get(0)) / cellSize));
        }
        return resultRainfall;
    }

    public static double XYtoRainfall(ArrayList<ArrayList<Double>> Rainfall, int cellSize, int gridNumber, int x, int y) {
        //坐标与数组索引的转换算法
        double i = Math.floor((double) (y) / (double) cellSize);
        double j = Math.floor((double) (x) / (double) cellSize);
        return Rainfall.get((int) i).get((int) j);
    }//本程序用不到

    public static Matrix semi_station(int NumStation, int[][] StationLocation, double cellSize, double[][] Inipre) {
        double[][] Semi_variogram = new double[NumStation + 1][NumStation + 1];
        for (int i = 0; i < NumStation + 1; i++) {
            for (int j = 0; j < NumStation + 1; j++) {
                if (j != NumStation && i != NumStation)
                    Semi_variogram[i][j] = (Inipre[StationLocation[i][0]][StationLocation[i][1]] - Inipre[StationLocation[j][0]][StationLocation[j][1]]) * (Inipre[StationLocation[i][0]][StationLocation[i][1]] - Inipre[StationLocation[j][0]][StationLocation[j][1]]) / 2;
                else if (j == NumStation && i == NumStation)
                    Semi_variogram[i][j] = 0;
                else
                    Semi_variogram[i][j] = 1;
            }
        }
        Matrix Semi_variogram_m = new Matrix(Semi_variogram);
        Semi_variogram_m = Semi_variogram_m.inverse();
        return Semi_variogram_m;
    }

    //变差函数,球状模型,要改***
    public static double r_cir(double dis, double a, double c0, double c1) {
        if (dis <= a) {
            return c0 + c1 * (1.5 * dis / a - Math.pow((dis / a), 3) / 2);
        } else {
            return c0 + c1;
        }
    }

    //计算每个点的半方差函数值
    public static Matrix semi_dest(int NumStation, int row, int colum, double CellSize, int[][] StationLocation, double a, double c0, double c1) {
        double[][] distance = new double[NumStation + 1][row * colum];
        for (int i = 0; i < row; i++)
            for (int j = 0; j < colum; j++) {
                for (int m = 0; m < NumStation + 1; m++) {
                    if (m == NumStation)
                        distance[m][i * colum + j] = 1;
                    else {
                        distance[m][i * colum + j] = CellSize * Math.abs(Math.sqrt((i - StationLocation[m][0]) * (i - StationLocation[m][0]) + (j - StationLocation[m][1]) * (j - StationLocation[m][1])));
                        distance[m][i * colum + j] = r_cir(distance[m][i * colum + j], a, c0, c1);
                    }
                }
            }
        //定义插值点的半方差矩阵
        Matrix r_des = new Matrix(distance);
        return r_des;
    }

    public static void filling_Iterate(int line, int column, int NODATA_value, double[][] H, double[][][] grid) {
        for (int i = 1; i < line - 1; i++) {
            for (int j = 1; j < column - 1; j++) {
                //寻找非NODATA_value网格8邻域中，除NODATA_value值外的最小值
                if (H[i][j] != grid[i][j][0] && H[i][j] != NODATA_value) {
                    double[] neighbor = {H[i - 1][j + 1], H[i][j + 1], H[i + 1][j + 1],
                            H[i - 1][j], H[i + 1][j],
                            H[i - 1][j - 1], H[i][j - 1], H[i + 1][j - 1]};
                    Arrays.sort(neighbor);
                    double neighbor_min = 0;
                    for (int k = 0; k < neighbor.length; k++) {
                        if (neighbor[k] != NODATA_value) {
                            neighbor_min = neighbor[k];
                            break;
                        }
                    }
                    // 将网格 C 的当前值与邻域网格值作比较，若 C 的当前值大于邻域网格值加上极小变量的值，则将网格 C 重新赋值为邻域网格值加上极小变量
                    // 遍历8个领域的结果 == 寻找最小的领域网格加上极小变量的值，并将它与 C当前值 对比
                    if (H[i][j] > neighbor_min + 0.001) {
                        H[i][j] = neighbor_min + 0.001;
                    }
                    // 判断网格原始高程与邻域网格值加上极小变量的大小关系，若原始高程更大，则将网格新值重新赋值为原始高程
                    // 遍历8个邻域的结果 == 寻找最小的领域网格加上极小变量的值，并将它与 原始高程 对比
                    if (grid[i][j][0] > neighbor_min + 0.001) {
                        H[i][j] = grid[i][j][0];
                    }
                }
            }
        }
    }
    //泰森相关函数
    public static double ThiessenInterpolation(double rx, double cy,ArrayList<RainfallStation> stations) {
        double interpolationRainfall=0;
        double distance = Double.MAX_VALUE;
        for(RainfallStation station:stations){
            double distance_temp = Math.sqrt((station.getX()-rx)*(station.getX()-rx)+(station.getY()-cy)*(station.getY()-cy));
            if(distance_temp <= distance){
                distance = distance_temp;
                interpolationRainfall = station.getRainfall();
            }
        }
        return interpolationRainfall;
    }


    //样条函数法相关函数
    public static double calculateDistance(RainfallStation station1, RainfallStation station2) {
        double dx = station1.getX() - station2.getX();
        double dy = station1.getY() - station2.getY();
//        System.out.println(Math.sqrt(dx * dx + dy * dy));
        return Math.sqrt(dx * dx + dy * dy) / 1000;
    }

    public static double R_r(double r, double c, double tauSquared) {
        if (r == 0) return 0;  // 当距离为0时直接返回0

        double term1 = (r * r) / 4.0 * (Math.log(r / (2.0 * Math.sqrt(tauSquared))) + c - 1);
        double term2 = tauSquared * (BesselJ.value(0, r / Math.sqrt(tauSquared)) + c + Math.log(r / (2.0 * Math.PI)));
//        System.out.println("R_r:" + (1.0 / (2.0 * Math.PI)) * (term1 + term2));
        return (1.0 / (2.0 * Math.PI)) * (term1 + term2);
    }

    //样条参数矩阵
    public static class Coefficients {
        public double[] ai; // 存储 a1, a2, a3
        public double[] lambda; // 存储 λj
    }

    public static Coefficients get_coefficients(List<RainfallStation> stations) {
        int n = stations.size();
        RealMatrix matrixA = new Array2DRowRealMatrix(n + 3, n + 3);
        RealVector vectorB = new ArrayRealVector(n + 3);

        // 填充矩阵A和向量B的前n行
        for (int i = 0; i < n; i++) {
            double x = stations.get(i).getX();
            double y = stations.get(i).getY();
            double rainfall = stations.get(i).getRainfall();

            matrixA.setEntry(i, 0, 1.0);
            matrixA.setEntry(i, 1, x);
            matrixA.setEntry(i, 2, y);
            for (int j = 0; j < n; j++) {
                double distance = calculateDistance(stations.get(i), stations.get(j));
                matrixA.setEntry(i, j + 3, R_r(distance, 0.577215, 0.01));
            }
            vectorB.setEntry(i, rainfall);
        }

        // 填充矩阵A的最后三行
        for (int i = 0; i < 3; i++) {
            matrixA.setEntry(n + i, i, 1);
        }

//        System.out.println(matrixA);

        // 使用LU分解求解线性方程组
        DecompositionSolver solver = new LUDecomposition(matrixA).getSolver();
        RealVector solution = solver.solve(vectorB);

        Coefficients coeffs = new Coefficients();
        coeffs.ai = new double[3];
        coeffs.lambda = new double[n];

        // 提取 a1, a2, a3
        for (int i = 0; i < 3; i++) {
            coeffs.ai[i] = solution.getEntry(i);
        }

        // 提取 λj
        for (int i = 0; i < n; i++) {
            coeffs.lambda[i] = solution.getEntry(i + 3);
        }

        return coeffs;
    }

    public static double rainfallInterpolation(double centerX, double centerY, List<RainfallStation> stations, Coefficients coeffs) {
        int N = stations.size();
        double interpolation = coeffs.ai[0] + coeffs.ai[1] * centerX + coeffs.ai[2] * centerY;
//        System.out.println(coeffs.ai[0] + " " + coeffs.ai[1] + " " + coeffs.ai[2]);

        for (int j = 0; j < N; j++) {
            double distance = calculateDistance(new RainfallStation(centerX, centerY, 0), stations.get(j));
            interpolation += coeffs.lambda[j] * R_r(distance, 0.577215, 0.01);
        }

        return interpolation;
    }

    //趋势面相关函数
    public static double[] TrendSurfaceParameter(List<RainfallStation> stations) {
        double[][] A = {{1, stations.get(0).getX(), stations.get(0).getY()},
                {1, stations.get(1).getX(), stations.get(1).getY()},
                {1, stations.get(2).getX(), stations.get(2).getY()}};
        double[] B = {stations.get(0).getRainfall(), stations.get(1).getRainfall(), stations.get(2).getRainfall()};
        int n = B.length;

        // 创建增广矩阵
        double[][] augmentedMatrix = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j] = A[i][j];
            }
            augmentedMatrix[i][n] = B[i];
        }

        // 高斯消元
        for (int i = 0; i < n; i++) {
            // 找到主元（pivot）
            int pivotRow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(augmentedMatrix[j][i]) > Math.abs(augmentedMatrix[pivotRow][i])) {
                    pivotRow = j;
                }
            }

            // 交换当前行和主元所在行
            double[] temp = augmentedMatrix[i];
            augmentedMatrix[i] = augmentedMatrix[pivotRow];
            augmentedMatrix[pivotRow] = temp;

            // 将主元归一化
            double pivot = augmentedMatrix[i][i];
            for (int j = i; j <= n; j++) {
                augmentedMatrix[i][j] /= pivot;
            }

            // 消元操作
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    double factor = augmentedMatrix[j][i];
                    for (int k = i; k <= n; k++) {
                        augmentedMatrix[j][k] -= factor * augmentedMatrix[i][k];
                    }
                }
            }
        }

        // 提取解
        double[] X = new double[n];
        for (int i = 0; i < n; i++) {
            X[i] = augmentedMatrix[i][n];
        }

        return X;
    }

    public static double TrendSurfaceInterpolation(double[] x, double rx, double cy) {
        double interpolationRainfall;
        return interpolationRainfall = x[0] + x[1] * rx + x[2] * cy;
    }

    //径向基
    public static double rbfInterpolation(double centerX, double centerY, List<RainfallStation> stations) {
        // 选择径向基函数（例如，高斯函数）
        double rbfWeightedSum = 0.0;
        double sumWeights = 0.0;

        for (RainfallStation station : stations) {
            double distance = calculateDistance(station, new RainfallStation(centerX, centerY, 0));
            double weight = gaussianRBF(distance); // 这里使用高斯径向基函数
            rbfWeightedSum += weight * station.getRainfall();
            sumWeights += weight;
        }

        if (sumWeights > 0) {
            return rbfWeightedSum / sumWeights;
        } else {
            return 0.0; // 如果权重和为0，返回0或其他默认值
        }
    }

    // 高斯径向基函数
    public static double gaussianRBF(double distance) {
        double sigma = 2.0; // 调整高斯函数的标准差
        return Math.exp(-distance * distance / (2 * sigma * sigma));
    }

    //自然领域
    public static double NaturalNeighborInterpolation(double x, double y, List<RainfallStation> stations) {
        GeometryFactory geometryFactory = new GeometryFactory();
        List<Coordinate> coordinates = new ArrayList<>();
        for (RainfallStation station : stations) {
            coordinates.add(new Coordinate(station.getX(), station.getY()));
        }

        VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
        voronoiBuilder.setSites(coordinates);
        Geometry voronoiDiagram = voronoiBuilder.getDiagram(geometryFactory);

        Point interpolationPoint = geometryFactory.createPoint(new Coordinate(x, y));

        double totalArea = 0;
        double weightedRainfall = 0;

        for (int i = 0; i < stations.size(); i++) {
            RainfallStation station = stations.get(i);
            Geometry cell = voronoiDiagram.getGeometryN(i);
            Geometry intersection = cell.intersection(interpolationPoint.buffer(0.1));  // Small buffer to ensure intersection

            double area = intersection.getArea();
            totalArea += area;
            weightedRainfall += area * station.getRainfall();
        }

        return weightedRainfall / totalArea;
    }

    //克里金
    public static double semivariance(RainfallStation p1, RainfallStation p2, String model, double range, double nugget, double sill) {

        double distance = calculateDistance(p1, p2);
        double semivariance = 0;
        switch (model) {

            case ("sperical"):  //球形模型
                if (distance < range && distance > 0) {
                    semivariance = nugget + sill * (3 * distance / 2 / range - 1 / 2 * Math.pow(distance / range, 3));
                } else
                    semivariance = nugget + sill;
                break;

            case ("gaussian"):  //高斯模型3
                semivariance = nugget + sill * (1.0 - Math.exp(-1.0 * Math.pow(distance / range, 2)));
                break;

            case ("exponential"):  //指数模型
                semivariance = nugget + sill * (1.0 - Math.exp(-1.0 * (distance / range)));
                break;
        }
        return semivariance;
    }

    //构造克里金方程系数矩阵
    public static double[][] buildMatrix(List<RainfallStation> station, double range, double nugget, double sill) {
        double[][] A = new double[station.size() + 1][station.size() + 1];
        for (int i = 0; i < station.size(); i++) {
            for (int j = 0; j < station.size(); j++) {
                A[i][j] = semivariance(station.get(i), station.get(j), "sperical", range, nugget, sill);
            }
            A[i][station.size()] = 1.0;
            A[station.size()][i] = 1.0;
        }
        A[station.size()][station.size()] = 1.0;
        return A;
    }

    //构造克里金方程右侧向量
    double[] buildVector(List<RainfallStation> station, RainfallStation origin, double range, double nugget, double sill)
    {
        double[] b = new double[station.size()+1];

        for (int i = 0; i < station.size(); i++)
        {
            b[i] = semivariance(station.get(i), origin,"sperical", range, nugget, sill);
        }
        b[station.size()]=1.0;
        return b;
    }

    public static ArrayList<ArrayList<Integer>> Norm_img(ArrayList<ArrayList<Double>> ini, int NODATA_VALUE) {
        double max;
        double min;
        int row = ini.size();
        int colum = ini.get(0).size();
        ArrayList<Double> maxArr = new ArrayList<Double>();
        ArrayList<Double> minArr = new ArrayList<Double>();
        //获取列中的最大值
        for (int i = 0; i < row; i++) {
            maxArr.add(Collections.max(ini.get(i)));
        }
        //获取列中的最小值
        ArrayList<ArrayList<Double>> ini1 = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < row; i++) {
            ArrayList<Double> ini1_temp = new ArrayList<Double>();
            for (int j = 0; j < colum; j++) {
                if (ini.get(i).get(j) == NODATA_VALUE) {
                    ini1_temp.add((double) -NODATA_VALUE);
                } else {
                    ini1_temp.add(ini.get(i).get(j));
                }
            }
            ini1.add(ini1_temp);
        }

        for (int i = 0; i < row; i++) {
            minArr.add(Collections.min(ini1.get(i)));
        }

        max = Collections.max(maxArr);
        min = Collections.min(minArr);//获取最大最小值
        double dis = max - min;
        ArrayList<ArrayList<Double>> nor_img_D = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Integer>> nor_img_I = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < row; i++) {
            ArrayList<Double> imi_tem = new ArrayList<Double>();
            for (int j = 0; j < colum; j++) {
                if (ini.get(i).get(j) == NODATA_VALUE) {
                    imi_tem.add(0.0);
                } else {
                    double m = ini.get(i).get(j) - min;
                    double por = 255 * m / dis;//拉伸
                    imi_tem.add(por);

                }
            }
            nor_img_D.add(imi_tem);
        }
        for (int i = 0; i < row; i++) {
            ArrayList<Integer> nor_img_I_tmp = new ArrayList<Integer>();
            for (int j = 0; j < colum; j++) {
                nor_img_I_tmp.add((nor_img_D.get(i).get(j).intValue()));
            }
            nor_img_I.add(nor_img_I_tmp);
        }
        return nor_img_I;
    }

    //转成图片
    public static void saveImg_Double(ArrayList<ArrayList<Double>> data, String path, int NODATA_VALUE) throws IOException {
        ArrayList<ArrayList<Integer>> arr_im = Norm_img(data, NODATA_VALUE);//归一化,将数据转为0-255
        int width = data.get(0).size();
        int height = data.size();
        BufferedImage ims = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        byte[] in = new byte[width * height];
        int k = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                in[k] = arr_im.get(i).get(j).byteValue();
                k++;
            }
        }
        ims.getRaster().setDataElements(0, 0, width, height, in);
        File outputfile = new File(path);
        ImageIO.write(ims, "jpg", outputfile);
    }

    public static void saveImg_Integer(ArrayList<ArrayList<Integer>> data, String path, int NODATA_VALUE) throws IOException {
        ArrayList<ArrayList<Double>> data_double = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < data.size(); i++) {
            ArrayList<Double> temp = new ArrayList<Double>();
            for (int j = 0; j < data.get(0).size(); j++) {
                temp.add((double) data.get(i).get(j));
            }
            data_double.add(temp);
        }
        ArrayList<ArrayList<Integer>> arr_im = Norm_img(data_double, NODATA_VALUE);//归一化,将数据转为0-255
        int width = data.get(0).size();
        int height = data.size();
        BufferedImage ims = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        byte[] in = new byte[width * height];
        int k = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                in[k] = arr_im.get(i).get(j).byteValue();
                k++;
            }
        }
        ims.getRaster().setDataElements(0, 0, width, height, in);
        File outputfile = new File(path);
        ImageIO.write(ims, "jpg", outputfile);
    }



}
