package supportFunction;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.ArrayList;

public class Statistics {
    // 去除NoData
    public static double[] RemoveNodata(double[][] matrix, double nodata) {
        ArrayList<Double> validData = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != nodata) {
                    validData.add(matrix[i][j]);
                }
            }
        }
        double[] dataArray = new double[validData.size()];
        for (int i = 0; i < dataArray.length; i++) {
            dataArray[i] = validData.get(i);
        }
        return dataArray;
    }
    // 算均值
    public static double calculateArrayAverage(double[]dataArray) {
        // 计算平均值
        int Count = dataArray.length;
        double total = 0;
        for (int i = 0; i < Count; i++) {
                total += dataArray[i];
            }
        return total / Count;
    }
    // 算总体方差
    public static double calculateVariance(double[] data) {
        double mean = calculateArrayAverage(data);
        double sumOfSquares = 0.0;
        for (double x : data) {
            sumOfSquares += Math.pow(x - mean, 2);
        }
        return sumOfSquares / data.length ;
    }

    // 算中位数
    public static double findMedian(double[] dataArray) {
        // 查找数组的中位数
        int length = dataArray.length;

        Arrays.sort(dataArray);

        if (length % 2 == 0) {
            double m = dataArray[length / 2 - 1];
            double n = dataArray[length / 2];
            return (m + n) / 2.0;
        } else {
            return dataArray[(length + 1) / 2 - 1]; // 中位数下标为 (length + 1) / 2 - 1
        }
    }
    // 距平
    public static double[] meanCentering(double[] Array) {
        // 对矩阵进行均值中心化
        int Count = Array.length;
        double[] newArray = new double[Count];
        for (int i = 0; i < Count; i++) {
            newArray[i] = Array[i] - calculateArrayAverage(Array);

        }

        return Array;
    }
    // 均方误差：衡量两组数据之间差异或误差的度量方法
    public static double calculateMeanSquaredError(double[] array1, double[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("数组长度必须相同");
        }
        // 计算一维数组的均方误差
        int length = array1.length;
        double sum = 0.0;

        for (int i = 0; i < length; i++) {
            double diff = array1[i] - array2[i];
            sum += diff * diff;
        }

        return sum / length;
    }
    // 算离差（range）
    public static double calculateArrayHeightDifference(double[] dataArray) {
        // 计算一维数组的最大值和最小值之差
        int length = dataArray.length;
        double max = dataArray[0];
        double min = dataArray[0];
        for (int i = 0; i < length; i++) {
            double value = dataArray[i];
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
        }

        return max - min;
    }

    // 算偏度
    public static double calculateArraySkewness(double[] dataArray) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double value : dataArray) {
            stats.addValue(value);
        }
        return stats.getSkewness();
    }

    // 算峰度
    public static double calculateKurtosis(double[] data) {
        int n = data.length;
        double mean = calculateArrayAverage(data);
        double variance = calculateVariance(data);
        double sumOfFourthPowers = 0.0;

        for (double x : data) {
            sumOfFourthPowers += Math.pow(x - mean, 4);
        }

        double kurtosis = (sumOfFourthPowers / (n * Math.pow(variance, 2))) - 3;
        return kurtosis;
    }

    // 相关系数（皮尔逊）:表示两个变量之间的关联程度
    public static double arrayCorrelation(double[] array1, double[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("数组长度必须相同");
        }

        double mean1 = calculateArrayAverage(array1);
        double mean2 = calculateArrayAverage(array2);

        double covariance = 0.0;
        double variance1 = 0.0;
        double variance2 = 0.0;

        for (int i = 0; i < array1.length; i++) {
            double diff1 = array1[i] - mean1;
            double diff2 = array2[i] - mean2;
            covariance += diff1 * diff2;
            variance1 += diff1 * diff1;
            variance2 += diff2 * diff2;
        }

        return (double) covariance / (Math.sqrt(variance1) * Math.sqrt(variance2));
    }
    // 自相关系数
    public static double calculateAutocorrelation(double[] timeSeries, int lag) {
        // 检查滞后期数lag是否在合理范围内
        if (lag < 0 || lag >= timeSeries.length) {
            throw new IllegalArgumentException("滞后期数不在合理范围内");
        }
        // 计算时间序列的自相关系数
        int n = timeSeries.length;
        double mean = calculateArrayAverage(timeSeries);
        double numerator = 0.0;
        double denominator = 0.0;

        for (int t = lag; t < n; t++) {
            numerator += (timeSeries[t] - mean) * (timeSeries[t - lag] - mean);
            denominator += Math.pow(timeSeries[t] - mean, 2);
        }
        // 处理分母为零的情况
        if (denominator == 0.0) {
            // 返回NaN或其他适当的值，这里返回0.0
            return 0.0;
        }

        return numerator / denominator;
    }
    // 余弦相似度
    public static double ArrayCosineSimilarity(double[] vector1, double[]vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("数组长度必须相同");
        }

        // 计算点积
        double dotProduct = IntStream.range(0, vector1.length)
                .mapToDouble(i -> vector1[i] * vector2[i])
                .sum();

        // 计算范数
        double norm1 = Math.sqrt(IntStream.range(0, vector1.length)
                .mapToDouble(i -> Math.pow(vector1[i], 2))
                .sum());

        double norm2 = Math.sqrt(IntStream.range(0, vector2.length)
                .mapToDouble(i -> Math.pow(vector2[i], 2))
                .sum());

        // 计算余弦相似度
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        } else {
            return dotProduct / (norm1 * norm2);
        }
    }
    public static double[][] readASCAndGetMatrix(String filePath) {
        try {
            readASC ascReader = new readASC(filePath);
            ascReader.readASC(filePath);
            double[][] interpolationValues = ascReader.getInterpolationValues();
            return interpolationValues;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

