//import java.util.ArrayList;
//import java.util.List;
//
//class RainfallStation
//{
//    double x;              //单元格x坐标
//    double y;              //单元格y坐标
//    double Rainfall;       //降雨量
//
//    public RainfallStation(double x, double y, double Rainfall) {
//        this.x = x;
//        this.y = y;
//        this.Rainfall = Rainfall;
//    }
//}
//
///*
//   算出每个单元格前权重系数λ,作为一个函数在插值函数里通过循环对每一个单元格进行计算,
//   得到插值结果,放入H2矩阵输出.插值结果=λzi累计求和,zi为已知值,即雨量站记录数据.
//   采用球状模型拟合半方差函数
// */
//
//
//public class KrigingInterpolation {
//
//    // 网格参数
//    int gridSizeX = 5;
//    int gridSizeY = 5;
//    // 单元大小
//    private static double cellSizeX = 1000.0;
//    private static double cellSizeY = 1000.0;
//    // 左下角坐标
//    private static double CELL_CORNER_X = 500;
//    private static double CELL_CORNER_Y = 500;
//    // 雨量站数量
//    private int StationN=3;
//    // 定义输出数组
//    private double[][] Result=new double[5][5];
//    // 雨量站列表
//    ArrayList<RainfallStation> Station = new ArrayList<>();
//
//
//
//    //------------------------------Kriging----------------------------------------
//
//
//    //降雨量观测值
//    List<Double> Rain = new ArrayList<Double>(StationN);
//
//
//
//    //初始化成员变量
//    public  void SetKrigingInterpolator(int a, int b, int c, int d, int e, int f, double g, double h, double i) {
//        Station.add(new RainfallStation(a, b, g));
//        Station.add(new RainfallStation(c, d, h));
//        Station.add(new RainfallStation(e, f, i));
//        Rain.add(g);
//        Rain.add(h);
//        Rain.add(i);
//    }
//
//    //定义球面模型参数
//
//    private double range=100;   //变程
//
//    private double nugget=2;    //拱高
//
//    private double sill=0.1 ;   //基台
//
//    //计算两点间距离
//    private static double calculateDistance(double x1, double y1, double x2, double y2) {
//        double Distance_x = x1 - x2;
//        double Distance_y = y1 - y2;
//        double Distance = Math.sqrt((Distance_y * Distance_y) + (Distance_x + Distance_x));
//        return Distance;
//    }
//
//
//    //计算两个雨量站之间半方差
//    private static double calculateSemiVariance(RainfallStation p1, RainfallStation p2) {
//        return Math.pow(p2.Rainfall - p1.Rainfall, 2) / 2.0;
//    }
//
//
//    //选择模型计算每两个点之间的半方差
//    double semivariance(RainfallStation p1, RainfallStation p2,String model ) {
//
//        double distance = calculateDistance(p1.x, p1.y, p2.x, p2.y);
//        double semivariance=0;
//        switch(model){
//
//        case("sperical"):  //球形模型
//            if (distance < this.range && distance > 0)
//            {
//                semivariance=this.nugget + this.sill * (3 * distance / 2 / this.range - 1 / 2 * Math.pow(distance / this.range, 3));
//            }
//            else
//                semivariance=this.nugget + this.sill;
//        break;
//
//        case("gaussian"):  //高斯模型
//            semivariance= this.nugget + this.sill * (1.0 - Math.exp(-1.0 * Math.pow(distance / this.range, 2)));
//        break;
//
//        case("exponential"):  //指数模型
//            semivariance=this.nugget + this.sill * (1.0 - Math.exp(-1.0 * (distance / this.range)));
//        break;
//    }
//        return semivariance;
//    }
//
//
//    //构造克里金方程系数矩阵
//    double[][] buildMatrix(List<RainfallStation> points)
//    {
//        double[][] A = new double[StationN+1][StationN+1];
//        for (int i = 0; i < StationN; i++)
//        {
//            for (int j = 0; j < StationN; j++)
//            {
//                A[i][j] = semivariance(points.get(i), points.get(j),"sperical");
//            }
//            A[i][StationN] = 1.0;
//            A[StationN][i] = 1.0;
//        }
//        A[StationN][StationN]=1.0;
//        return A;
//    }
//
//    //构造克里金方程右侧向量
//    double[] buildVector(List<RainfallStation> points, RainfallStation origin)
//    {
//        double[] b = new double[StationN+1];
//
//        for (int i = 0; i < StationN; i++)
//        {
//            b[i] = semivariance(points.get(i), origin,"sperical");
//        }
//        b[StationN]=1.0;
//        return b;
//    }
//
//    //高斯消元法解方程
//    double[] solve(double[][] A, double[] B)
//    {
//        double[] X = new double[StationN];
//
//        for (int k = 0; k < StationN - 1; k++)
//        {
//            for (int i = k + 1; i < StationN; i++)
//            {
//                double factor = A[i][k] / A[k][k];
//                B[i] -= factor * B[k];
//                for (int j = k; j < StationN; j++)
//                {
//                    A[i][j] -= factor * A[k][j];
//                }
//            }
//        }
//
//        X[StationN - 1] = B[StationN - 1] / A[StationN - 1][StationN - 1];
//        for (int i = StationN - 2; i >= 0; i--)
//        {
//            double sum = B[i];
//            for (int j = i + 1; j < StationN; j++)
//            {
//                sum -= A[i][j] * X[j];
//            }
//            X[i] = sum / A[i][i];
//        }
//        return X;
//    }
//
//
//    //插值输出结果
//    public void  KrigingInterpolation()
//    {
//        for (int i = 0; i < gridSizeX; i++)
//        {
//            for (int j = 0; j < gridSizeY; j++)
//            {
//                RainfallStation p1 = new RainfallStation(i, j, 0.0);
//                double[] B = buildVector(this.Station, p1);
//                double[][] A = buildMatrix(this.Station);
//                double[] weights = solve(A,B);
//
//                //计算权重得到插值结果
//                double estimate = 0;
//                for (int p = 0; p < StationN; p++)
//                {
//                    estimate += weights[p] * Rain.get(p);
//                }
//
//                this.Result[i][j] = estimate;
//            }
//        }
//
//        //打印输出
//        for(int k=0;k<gridSizeX;k++)
//        {
//            for(int p=0;p<gridSizeY;p++)
//            {
//                System.out.printf("%.2f\t",Result[k][p]);
//            }
//            System.out.println('\n');
//        }
//    }
//
//
//    //测试
//    public static void main(String[] args)
//    {
//        KrigingInterpolation test=new KrigingInterpolation();
//
//        test.SetKrigingInterpolator(1, 1, 3,2, 2, 4, 22.3 ,34.2, 19.5); ;
//
//        test.KrigingInterpolation();
//
//    }
//
//}
