package supportFunction;

import supportFunction.Statistics;

import java.util.Arrays;
import java.io.FileWriter;
import java.io.IOException;

public class StatisticsCalculator {
    public static void calculateArrayStatistics(double[][] matrix, double[][] matrix2, double NODATA_VALUE) {
        double[] array1 = Statistics.RemoveNodata(matrix, NODATA_VALUE);
        double[] array2 = Statistics.RemoveNodata(matrix2, NODATA_VALUE);

        try (FileWriter writer = new FileWriter("output/output.txt")) {
            // System.out.println("array1: " + Arrays.toString(array1));
            // writer.write("array1: " + Arrays.toString(array1) + "\n");

            double ArrayAverage = Statistics.calculateArrayAverage(array1);
            writer.write("平均值: " + ArrayAverage + "\n");
            System.out.println("平均值: " + ArrayAverage);


            double ArrayMedian = Statistics.findMedian(array1);
            writer.write("中位数: " + ArrayMedian + "\n");
            System.out.println("中位数: " + ArrayMedian);

            double ArrayVariance = Statistics.calculateVariance(array1);
            writer.write("总体方差: " + ArrayVariance + "\n");
            System.out.println("总体方差: " + ArrayVariance);

            double meanSquaredError = Statistics.calculateMeanSquaredError(array1, array2);
            writer.write("均方误差: " + meanSquaredError + "\n");
            System.out.println("均方误差: " + meanSquaredError);

            double ArrayHeightDifference = Statistics.calculateArrayHeightDifference(array1);
            writer.write("最大值和最小值之差: " + ArrayHeightDifference + "\n");
            System.out.println("最大值和最小值之差: " + ArrayHeightDifference);

            double skewness = Statistics.calculateArraySkewness(array1);
            writer.write("偏度: " + skewness + "\n");
            System.out.println("偏度: " + skewness);

            double ArrayKurtosis = Statistics.calculateKurtosis(array1);
            writer.write("密度: " + ArrayKurtosis + "\n");
            System.out.println("密度: " + ArrayKurtosis);

            double correlation = Statistics.arrayCorrelation(array1, array2);
            writer.write("相关系数: " + correlation + "\n");
            System.out.println("相关系数: " + correlation);

            double cosineSimilarity = Statistics.ArrayCosineSimilarity(array1, array2);
            writer.write("矩阵1和矩阵2的余弦相似度: " + cosineSimilarity + "\n");
            System.out.println("矩阵1和矩阵2的余弦相似度: " + cosineSimilarity);

            double[] meanCenteredArray = Statistics.meanCentering(array1);
            writer.write("均平:" + Arrays.toString(meanCenteredArray) + "\n");
            System.out.println("均平:"+Arrays.toString(meanCenteredArray));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        double[][] matrixIDW = Statistics.readASCAndGetMatrix("output/rainfall_IDWinterpolation.asc");

        double[][] matrixKrig = Statistics.readASCAndGetMatrix("output/rainfall_Kriginginterpolation.asc");
        /*double[][] matrixNNI = Statistics.readASCAndGetMatrix("output/rainfall_NaturalNeighborinterpolation.asc");
        double[][] matrixSpline = Statistics.readASCAndGetMatrix("output/rainfall_RBFinterpolation.asc");
        double[][] matrixRBF = Statistics.readASCAndGetMatrix("output/rainfall_Splineinterpolation.asc");
        double[][] matrixTrend = Statistics.readASCAndGetMatrix("output/rainfall_TrendSurfaceinterpolation.asc");

         */
        double NODATA_VALUE =  -9999.0;
        calculateArrayStatistics(matrixIDW, matrixKrig, NODATA_VALUE);
        /*
        calculateArrayStatistics(matrixKrig, matrixNNI, NODATA_VALUE);
        calculateArrayStatistics(matrixNNI, matrixSpline, NODATA_VALUE);
        calculateArrayStatistics(matrixSpline,matrixRBF, NODATA_VALUE);
        calculateArrayStatistics(matrixRBF, matrixTrend, NODATA_VALUE);
        calculateArrayStatistics(matrixTrend, matrixIDW, NODATA_VALUE);

         */


        //读入数据
         /* double[] timeSeries={0,4,13,40,51,18,8,3,10,13,22, 5,
            2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,
            0,30,13,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1};
         double[] timeSeries={1.0, 0.0, 4.0, 17.0, 48.0, 21.0, 0.0, 3.0, 15.0, 9.0, 8.0, 7.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.0, 10.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};*/
        double[] timeSeries={0.0, 0.0, 2.0, 26.0, 84.0, 35.0, 8.0, 2.0, 10.0, 12.0, 9.0, 11.0, 8.0, 2.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 24.0, 18.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};


        // 计算时间序列的自相关系数
        int lag = 1;
        double autocorrelation = Statistics.calculateAutocorrelation(timeSeries, lag);
        System.out.println("时间序列的自相关系数 (滞后 " + lag + "): " + autocorrelation);

        // 计算时间序列数组的均值
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        double mean = Statistics.calculateArrayAverage(data);
        System.out.println("时间序列数组的均值: " + mean);

    }
}

