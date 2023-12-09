import java.util.Arrays;

import static java.lang.Math.abs;

public class TysonPolygonInterpolator<b>
{
    //定义一个二维数组全部为零，用于接下来插值的赋值，h2为赋值后的数组
    private double[][] h1;
    private double[][] h2;

    //私有成员变量：坐标和降雨量（我自己写的时候设置了三个站点）
    private int x1,x2,x3,y1,y2,y3;
    private double r1,r2,r3;

    //初始化成员变量
    public void SetTysonPolygonInterpolator(int a,int b,int c,int d,int e,int f,double g,double h,double i){
        x1=a;
        x2=b;
        x3=c;
        y1=d;
        y2=e;
        y3=f;
        r1=g;
        r2=h;
        r3=i;
    };

    //定义数组大小
    public TysonPolygonInterpolator(int size) {
        h1 = new double[size][size];
        h2 = new double[size][size];
    }

    //给二维数组h1赋值为零，并给站点位置赋值
    public void initializeH1() {
        for (int k = 0; k < h1.length; k++) {
            for (int j = 0; j < h1[0].length; j++) {
                h1[k][j] = 0;
            }
        }
        h1[x1][y1] = r1;
        h1[x2][y2] = r2;
        h1[x3][y3] = r3;
    }

    //泰森
    /*public void interpolateH2() {
        for (int i = 0; i < h2.length; i++) {
            for (int j = 0; j < h2[0].length; j++) {
                double distance1 = (i - x1) * (i - x1) + (j - y1) * (j - y1);
                double distance2 = (i - x2) * (i - x2) + (j - y2) * (j - y2);
                double distance3 = (i - x3) * (i - x3) + (j - y3) * (j - y3);

                if (distance1 <= distance2 && distance1 <= distance3) {
                    h2[i][j] = h1[x1][y1];
                }
                if (distance2 <= distance1 && distance2 <= distance3) {
                    h2[i][j] = h1[x2][y2];
                }
                if (distance3 <= distance2 && distance3 <= distance1) {
                    h2[i][j] = h1[x3][y3];
                }
            }
        }
    }*/

    //趋势面
    public double[] solve() {
        double[][] A = {{1, x1, y1},
                {1, x2, y2},
                {1, x3, y3}};
        double[] B = {r1, r2, r3};
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

    /*public double [] TrendSurface(){
        int[][] A={{1,x1,y1},{1,x2,y2},{1,x3,y3}};
        double[] B={r1,r2,r3};
        int n = B.length;
        double[] X = new double[n];
        //创建增广矩阵
        double [][] AugmentMatrix = new double[n][n+1];
        for (int i = 0;i < n; i++)  {
            for (int j= 0;j < n; j++){
                AugmentMatrix[i][j] = A[i][j];
            }
            AugmentMatrix[i][n] = B[i];
            //找到行最大
            int index=i;
            for (int j = 0;j < n; j++){
                if(abs(AugmentMatrix[j][i])>abs(AugmentMatrix[index][i])){
                    index=j;
                }
            }
            //交换行，把最大行放到最上面
            double [] temp=AugmentMatrix[i];
            AugmentMatrix[i]=AugmentMatrix[index];
            AugmentMatrix[index]=temp;
            //归一化
            double factor =AugmentMatrix[i][i];
            for(int j = 0;j < n; j++){
                AugmentMatrix[i][j]/=factor;
            }
            //消元
            for(int j = 0; j < n; j++){
                if(j != i) {
                    double factor1=AugmentMatrix[j][i];
                    for (int k = 0; k <= n; k++) {
                        AugmentMatrix[j][k] -= factor1*AugmentMatrix[i][k];
                    }
                }
            }
        }
        // 提取解
        for (int i = 0; i < n; i++) {
            X[i] = AugmentMatrix[i][n];
        }

        return X;

    }*/
    public void TrendSurface(double []x) {
        for (int i = 0; i < h2.length; i++) {
            for (int j = 0; j < h2[0].length; j++) {
                h2[i][j]=x[0]+x[1]*i+x[2]*j;
            }
        }
    }

    public double[][] getH2() {
        return h2;
    }

    public static void main(String[] args) {
        //25*25
        TysonPolygonInterpolator interpolator = new TysonPolygonInterpolator(25);
        interpolator.SetTysonPolygonInterpolator(1,3,7,4,5,6,15,22,33);
        interpolator.initializeH1();
        //interpolator.interpolateH2();
        double[][] result = interpolator.getH2();

        double[] X = interpolator.solve();

        interpolator.TrendSurface(X);

        // 打印结果
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }

        }

    }




