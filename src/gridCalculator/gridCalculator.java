package gridCalculator;

import java.util.ArrayList;


import RainfallStation.RainfallStation;
import supportFunction.supportFunction;

import Jama.Matrix;

import java.io.*;

import java.awt.Color;

import java.awt.image.BufferedImage;

import java.awt.Font;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;



public class gridCalculator {
    // 网格计算主类
    private int NODATA_VALUE;
    private int nrows;//行
    private int ncolumns;//列
    private double cellsize;//网格大小
    private int stationsNumber;//降雨站数量
    private ArrayList<Double> cellcorner;//左下角起始坐标
    private ArrayList<ArrayList<Double>> DEM;//输入高程
    private ArrayList<Double> stationsX;//降雨站X坐标
    private ArrayList<Double> stationsY;//降雨站Y坐标
    final ArrayList<Double> stationsRainfall;//降雨站数据
    final ArrayList<ArrayList<Double>> stationsTimeSeriesRainfall;//降雨站时间序列
    private ArrayList<RainfallStation> stations;
    private ArrayList<ArrayList<Double>> Mask; // 掩膜
    private ArrayList<ArrayList<Double>> gridRainfall_IDW;//IDW插值降雨量矩阵
    private ArrayList<ArrayList<Double>> gridRainfall_Kriging;//克里金插值降雨量矩阵
    private ArrayList<ArrayList<Double>> gridRainfall_Spline;//样条函数插值降雨量矩阵
    private ArrayList<ArrayList<Double>> gridRainfall_Trendsurface;
    private ArrayList<ArrayList<Double>> gridRainfall_RBF;  // 径向基
    private ArrayList<ArrayList<Double>> gridRainfall_NaturalNeighbor;  // 自然邻域
    private ArrayList<ArrayList<Double>> gridRainfall_Thiessen;  // 泰森


    public gridCalculator(int n, int r, int c, double gS, ArrayList<Double> cC, ArrayList<ArrayList<Double>> h, int sN, ArrayList<Double> sX,
                          ArrayList<Double> sY, ArrayList<Double> sR, ArrayList<ArrayList<Double>> sTR) {
        //构造函数
        NODATA_VALUE = n;
        nrows = r;
        ncolumns = c;
        cellsize = gS;
        cellcorner = cC;
        DEM = h;
        stationsNumber = sN;
        stationsX = sX;
        stationsY = sY;
        stationsRainfall = sR;
        stationsTimeSeriesRainfall = sTR;
        Mask = generateMask();


//        //合并降雨量站信息
//        stations = new ArrayList<>();
//        for (int i = 0; i < stationsX.size(); i++) {
//            double x = stationsX.get(i);
//            double y = stationsY.get(i);
//            double rainfall = stationsRainfall.get(i);
//            RainfallStation station = new RainfallStation(x, y, rainfall);
//            stations.add(station);
//        }
        int timeIndex; // 设置您想要提取降雨数据的时间索引
        gridRainfall_IDW = new ArrayList<ArrayList<Double>>();//IDW插值降雨量矩阵初始化
        gridRainfall_Kriging = new ArrayList<ArrayList<Double>>();//插值降雨量矩阵初始化
        gridRainfall_Spline = new ArrayList<ArrayList<Double>>();//Spline插值降雨量矩阵初始化
        gridRainfall_Trendsurface = new ArrayList<ArrayList<Double>>();//Spline插值降雨量矩阵初始化
        gridRainfall_RBF = new ArrayList<ArrayList<Double>>();//RBF插值降雨量矩阵初始化
        gridRainfall_NaturalNeighbor = new ArrayList<ArrayList<Double>>();//自然邻域插值降雨量矩阵初始化

        for (int i = 0; i < r; i++) {//IDW插值降雨量矩阵置0
            ArrayList<Double> rainfalltemp = new ArrayList<Double>();
            for (int j = 0; j < c; j++) {
                rainfalltemp.add((double) 0);
            }
            gridRainfall_IDW.add(rainfalltemp);
        }
    }

    // 生成掩膜的方法
    private ArrayList<ArrayList<Double>> generateMask() {
        ArrayList<ArrayList<Double>> mask = new ArrayList<>();

        // 遍历 DEM 数据生成 mask
        for (ArrayList<Double> row : this.DEM) {
            ArrayList<Double> maskRow = new ArrayList<>();
            for (Double value : row) {
                if (value.equals((double)NODATA_VALUE)) {
                    maskRow.add(0.0); // 错误或缺失数据标记为 0
                } else {
                    maskRow.add(1.0); // 正常数据标记为 1
                }
            }
            mask.add(maskRow);
        }

        return mask;
    }

    public ArrayList<RainfallStation> mergeRainfallStationInfo(
            ArrayList<Double> stationsX,
            ArrayList<Double> stationsY,
            ArrayList<ArrayList<Double>> stationsTimeSeriesRainfall,
            int timeIndex) {

        ArrayList<Double> rainfallAtTime = new ArrayList<>();
        for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
            if (stationTimeSeries.size() > timeIndex) {
                rainfallAtTime.add(stationTimeSeries.get(timeIndex));
            } else {
                // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
                rainfallAtTime.add(Double.NaN); // 或者您可以选择添加一个默认值
            }
        }

        // 检查提供的时间索引是否有效
        if (timeIndex < 0 || timeIndex >= stationsTimeSeriesRainfall.get(0).size()) {
            throw new IllegalArgumentException("无效的时间索引: " + timeIndex);
        }

//        // 从时间序列中获取特定时间的降雨数据
//        ArrayList<Double> rainfallAtTime = stationsTimeSeriesRainfall.get(timeIndex);

        // 检查所有列表的大小是否一致
        if (stationsX.size() != stationsY.size() || stationsX.size() != rainfallAtTime.size()) {
            throw new IllegalArgumentException("列表大小不一致");
        }

        // 创建雨量站信息的合并列表
        ArrayList<RainfallStation> stations = new ArrayList<>();
        for (int i = 0; i < stationsX.size(); i++) {
            double x = stationsX.get(i);
            double y = stationsY.get(i);
            double rainfall = rainfallAtTime.get(i); // 使用特定时间的降雨数据
            RainfallStation station = new RainfallStation(x, y, rainfall);
            stations.add(station);
        }
        return stations;
    }

    public ArrayList<ArrayList<Double>> VoronoiDWithDistance_ForRainfall(ArrayList<Double> stationsRainfall_temp) {
        // 反距离权重插值法求降雨量
        for (int i = 0; i < stationsNumber; i++) {
            gridRainfall_IDW.get((int) ((stationsY.get(i) - cellcorner.get(1)) / cellsize))
                    .set((int) ((stationsX.get(i) - cellcorner.get(0)) / cellsize), stationsRainfall_temp.get(i));
        }// 获得降雨站坐标及其数据

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                int mark = 1;// 标记：不根据反距离权重法改变降雨站的降雨量
                for (int t = 0; t < stationsNumber; t++) {
                    if ((i == (int) ((stationsY.get(t) - cellcorner.get(1)) / cellsize)) &&
                            (j == (int) ((stationsX.get(t) - cellcorner.get(0)) / cellsize))) {
                        mark = 0;
                    }
                }
                if (mark == 1) {
                    gridRainfall_IDW.get(i).set(j, supportFunction.inverseDistanceWeightingForRainfall(gridRainfall_IDW, stationsX, stationsY, i, j, cellsize, stationsNumber, cellcorner));
                }// 插值
            }
        }
        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < nrows; i++) {
            true_rainfall.add(gridRainfall_IDW.get(nrows - i - 1));
        }

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    true_rainfall.get(i).set(j, (double) NODATA_VALUE);
                }
            }
        }

        gridRainfall_IDW = true_rainfall;
        return true_rainfall;// 返回插值后的降雨量矩阵
    }

    public ArrayList<ArrayList<Double>> Kriging_ForRainfall(double a, double c0, double c1, ArrayList<Double> stationsRainfall_temp) {
        // 克里金插值法求降雨量
        double[][] Inipre = new double[nrows][ncolumns];//降雨量数据
        for (int i = 0; i < stationsNumber; i++) {
            Inipre[(int) ((stationsY.get(i) - cellcorner.get(1)) / cellsize)][(int) ((stationsX.get(i) - cellcorner.get(0)) / cellsize)] = stationsRainfall_temp.get(i);
        }

        int[][] StationLocation = new int[stationsNumber][2];//雨量站坐标
        for (int i = 0; i < stationsNumber; i++) {
            StationLocation[i][0] = (int) ((stationsY.get(i) - cellcorner.get(1)) / cellsize);
            StationLocation[i][1] = (int) ((stationsX.get(i) - cellcorner.get(0)) / cellsize);
        }

        Matrix semi_sta = supportFunction.semi_station(stationsNumber, StationLocation, cellsize, Inipre);//计算雨量站的半方差矩阵
        Matrix semi_des = supportFunction.semi_dest(stationsNumber, nrows, ncolumns, cellsize, StationLocation, a, c0, c1);//计算每个站点的版方差函数
        Matrix weight = semi_sta.times(semi_des);//二者相乘得到权重
        double[][] z = new double[1][stationsNumber + 1];//将降雨量存入数组中
        double[] sortIni = new double[stationsNumber + 1];
        double maxIni;
        double minIni;
        double maxKIni;
        double minKIni;

        for (int i = 0; i < stationsNumber + 1; i++) {
            if (i == stationsNumber) {
                z[0][i] = 1;
                sortIni[i] = 0;
            } else {
                z[0][i] = Inipre[StationLocation[i][0]][StationLocation[i][1]];
                sortIni[i] = Inipre[StationLocation[i][0]][StationLocation[i][1]];
            }
        }

        maxIni = sortIni[0];
        minIni = sortIni[0];
        for (int i = 0; i < stationsNumber + 1; i++) {
            if (maxIni <= sortIni[i])
                maxIni = sortIni[i];
            if (minIni >= sortIni[i] && sortIni[i] != 0)
                minIni = sortIni[i];
        }

        Matrix ini = new Matrix(z);//将降雨量存入矩阵
        Matrix krging = ini.times(weight);//加权求和
        double[][] krging_d = krging.getArray();

        //标准化
        maxKIni = krging_d[0][0];
        minKIni = krging_d[0][0];
        for (int i = 0; i < nrows * ncolumns; i++) {
            if (maxKIni <= krging_d[0][i])
                maxKIni = krging_d[0][i];
            if (minKIni >= krging_d[0][i] && krging_d[0][i] != 0)
                minKIni = krging_d[0][i];
        }
        double disK = maxKIni - minKIni;
        double dis = maxIni - minIni;
        double[][] kringIni = new double[nrows][ncolumns];

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                kringIni[i][j] = krging_d[0][i * ncolumns + j];
                kringIni[i][j] = (kringIni[i][j] - minKIni) / disK;
                kringIni[i][j] = kringIni[i][j] * dis + minIni;
            }
        }

        //裁剪
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    kringIni[i][j] = NODATA_VALUE;
                }
            }
        }

        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();// 转为ArrayList
        for (int i = 0; i < nrows; i++) {
            ArrayList<Double> kriging_rainfall_temp = new ArrayList<Double>();
            for (int j = 0; j < ncolumns; j++) {
                kriging_rainfall_temp.add(kringIni[i][j]);
            }
            true_rainfall.add(kriging_rainfall_temp);
        }

        gridRainfall_Kriging = true_rainfall;
        return gridRainfall_Kriging;
    }

    //泰森
    public ArrayList<ArrayList<Double>> Thiessen_ForRainfall(ArrayList<RainfallStation> stations) {
        double[][] grid = new double[nrows][ncolumns];
//        supportFunction.Coefficients coeffs = supportFunction.get_coefficients(stations);

        // 遍历每个单元
        for (int i = 0; i < ncolumns; i++) {
            double cellCenterY = cellcorner.get(1) + (ncolumns - i - 1) * cellsize + cellsize / 2.0;
            for (int j = 0; j < nrows; j++) {
                double cellCenterX = cellcorner.get(0) + j * cellsize + cellsize / 2.0;

                // 检查当前单元是否为雨量站点
                boolean isStation = false;
                for (RainfallStation station : stations) {
                    double stationX = station.getX();
                    double stationY = station.getY();
                    if (Math.abs(cellCenterX - stationX) < 1e-6 && Math.abs(cellCenterY - stationY) < 1e-6) {
                        // 当前单元是雨量站点
                        isStation = true;
                        grid[j][i] = station.getRainfall();
                        break;
                    }
                }

                // 仅对非雨量站点进行降雨量插值
                if (!isStation) {
                    double interpolation = supportFunction.ThiessenInterpolation(cellCenterX, cellCenterY, stations);
                    grid[j][i] = interpolation;
                }
            }
        }
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    grid[i][j] = NODATA_VALUE;
                }
            }
        }

        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();// 转为ArrayList
        for (int i = 0; i < nrows; i++) {
            ArrayList<Double> spline_rainfall_temp = new ArrayList<Double>();
            for (int j = 0; j < ncolumns; j++) {
                spline_rainfall_temp.add(grid[i][j]);
            }
            true_rainfall.add(spline_rainfall_temp);
        }

        gridRainfall_Thiessen = true_rainfall;
        return gridRainfall_Thiessen;
    }
    //样条函数
    public ArrayList<ArrayList<Double>> spline_ForRainfall(ArrayList<RainfallStation> stations) {
        double[][] grid = new double[nrows][ncolumns];
        supportFunction.Coefficients coeffs = supportFunction.get_coefficients(stations);

        // 遍历每个单元
        for (int i = 0; i < ncolumns; i++) {
            double cellCenterY = cellcorner.get(1) + (ncolumns - i - 1) * cellsize + cellsize / 2.0;
            for (int j = 0; j < nrows; j++) {
                double cellCenterX = cellcorner.get(0) + j * cellsize + cellsize / 2.0;

                // 检查当前单元是否为雨量站点
                boolean isStation = false;
                for (RainfallStation station : stations) {
                    double stationX = station.getX();
                    double stationY = station.getY();
                    if (Math.abs(cellCenterX - stationX) < 1e-6 && Math.abs(cellCenterY - stationY) < 1e-6) {
                        // 当前单元是雨量站点
                        isStation = true;
                        grid[j][i] = station.getRainfall();
                        break;
                    }
                }

                // 仅对非雨量站点进行降雨量插值
                if (!isStation) {
                    double interpolation = supportFunction.rainfallInterpolation(cellCenterX, cellCenterY, stations, coeffs);
                    grid[j][i] = interpolation;
                }
            }
        }
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    grid[i][j] = NODATA_VALUE;
                }
            }
        }

        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();// 转为ArrayList
        for (int i = 0; i < nrows; i++) {
            ArrayList<Double> spline_rainfall_temp = new ArrayList<Double>();
            for (int j = 0; j < ncolumns; j++) {
                spline_rainfall_temp.add(grid[i][j]);
            }
            true_rainfall.add(spline_rainfall_temp);
        }

        gridRainfall_Spline = true_rainfall;
        return gridRainfall_Spline;
    }

    //趋势面
    public ArrayList<ArrayList<Double>> TrendSurface_ForRainfall(ArrayList<RainfallStation> stations) {
        double[][] grid = new double[nrows][ncolumns];
        double[] parameter = supportFunction.TrendSurfaceParameter(stations);

        // 遍历每个单元
        for (int i = 0; i < ncolumns; i++) {
            double cellCenterY = cellcorner.get(1) + (ncolumns - i - 1) * cellsize + cellsize / 2.0;
            for (int j = 0; j < nrows; j++) {
                double cellCenterX = cellcorner.get(0) + j * cellsize + cellsize / 2.0;

                // 检查当前单元是否为雨量站点
                boolean isStation = false;
                for (RainfallStation station : stations) {
                    double stationX = station.getX();
                    double stationY = station.getY();
                    if (Math.abs(cellCenterX - stationX) < 1e-6 && Math.abs(cellCenterY - stationY) < 1e-6) {
                        // 当前单元是雨量站点
                        isStation = true;
                        grid[j][i] = station.getRainfall();
                        break;
                    }
                }

                // 仅对非雨量站点进行降雨量插值
                if (!isStation) {
                    double interpolation = supportFunction.TrendSurfaceInterpolation(parameter, cellCenterX, cellCenterY);
                    grid[j][i] = interpolation;
                }
            }
        }

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    grid[i][j] = NODATA_VALUE;
                }
            }
        }

        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();// 转为ArrayList
        for (int i = 0; i < nrows; i++) {
            ArrayList<Double> trendsurface_rainfall_temp = new ArrayList<Double>();
            for (int j = 0; j < ncolumns; j++) {
                trendsurface_rainfall_temp.add(grid[i][j]);
            }
            true_rainfall.add(trendsurface_rainfall_temp);
        }

        gridRainfall_Trendsurface = true_rainfall;
        return gridRainfall_Trendsurface;
    }

    //RBF
    public ArrayList<ArrayList<Double>> RBF_ForRainfall(ArrayList<RainfallStation> stations) {
        double[][] grid = new double[nrows][ncolumns];

        // 遍历每个单元
        for (int i = 0; i < ncolumns; ++i) {
            double cellCenterY = cellcorner.get(1) + (ncolumns - i - 1) * cellsize + cellsize / 2.0;
            for (int j = 0; j < nrows; j++) {
                double cellCenterX = cellcorner.get(0) + j * cellsize + cellsize / 2.0;

                // 检查当前单元是否为雨量站点
                boolean isStation = false;
                for (RainfallStation station : stations) {
                    double stationX = station.getX();
                    double stationY = station.getY();
                    if (Math.abs(cellCenterX - stationX) < 1e-6 && Math.abs(cellCenterY - stationY) < 1e-6) {
                        // 当前单元是雨量站点
                        isStation = true;
                        grid[j][i] = station.getRainfall();
                        break;
                    }
                }

                // 仅对非雨量站点进行径向基函数插值
                if (!isStation) {
                    double interpolation = supportFunction.rbfInterpolation(cellCenterX, cellCenterY, stations);
                    grid[j][i] = interpolation;
                }
            }
        }

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    grid[i][j] = NODATA_VALUE;
                }
            }
        }

        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();// 转为ArrayList
        for (int i = 0; i < nrows; i++) {
            ArrayList<Double> RBF_rainfall_temp = new ArrayList<Double>();
            for (int j = 0; j < ncolumns; j++) {
                RBF_rainfall_temp.add(grid[i][j]);
            }
            true_rainfall.add(RBF_rainfall_temp);
        }

        gridRainfall_RBF = true_rainfall;
        return gridRainfall_RBF;
    }

    //自然邻域
    public ArrayList<ArrayList<Double>> NaturalNeighbor_ForRainfall(ArrayList<RainfallStation> stations) {
        double[][] grid = new double[nrows][ncolumns];

        // 遍历每个单元
        for (int i = 0; i < ncolumns; ++i) {
            double cellCenterY = cellcorner.get(1) + (ncolumns - i - 1) * cellsize + cellsize / 2.0;
            for (int j = 0; j < nrows; j++) {
                double cellCenterX = cellcorner.get(0) + j * cellsize + cellsize / 2.0;

                // 检查当前单元是否为雨量站点
                boolean isStation = false;
                for (RainfallStation station : stations) {
                    double stationX = station.getX();
                    double stationY = station.getY();
                    if (Math.abs(cellCenterX - stationX) < 1e-6 && Math.abs(cellCenterY - stationY) < 1e-6) {
                        // 当前单元是雨量站点
                        isStation = true;
                        grid[j][i] = station.getRainfall();
                        break;
                    }
                }

                // 仅对非雨量站点进行径向基函数插值
                if (!isStation) {
                    double interpolation = supportFunction.NaturalNeighborInterpolation(cellCenterX, cellCenterY, stations);
                    grid[j][i] = interpolation;
                }
            }
        }

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                if (DEM.get(i).get(j) == NODATA_VALUE) {
                    grid[i][j] = NODATA_VALUE;
                }
            }
        }

        ArrayList<ArrayList<Double>> true_rainfall = new ArrayList<ArrayList<Double>>();// 转为ArrayList
        for (int i = 0; i < nrows; i++) {
            ArrayList<Double> NaturalNeighbor_rainfall_temp = new ArrayList<Double>();
            for (int j = 0; j < ncolumns; j++) {
                NaturalNeighbor_rainfall_temp.add(grid[i][j]);
            }
            true_rainfall.add(NaturalNeighbor_rainfall_temp);
        }

        gridRainfall_NaturalNeighbor = true_rainfall;
        return gridRainfall_NaturalNeighbor;
    }

    // 新方法：将降雨量插值结果保存为带颜色条和雨量站点的图像
    public void saveRainfallPlot(String path, ArrayList<RainfallStation> stations, ArrayList<ArrayList<Double>> stationsTimeSeriesRainfall, ArrayList<ArrayList<Double>> mask, int timeIndex) throws IOException {
        int extraWidth = 100; // Width for the color bar and padding


//        g2d.scale(scaleFactor, scaleFactor);
        int width = stationsTimeSeriesRainfall.get(0).size() + extraWidth;
        int height = stationsTimeSeriesRainfall.size();

//        int width = stationsTimeSeriesRainfall.get(0).size() + extraWidth;
//        int height = stationsTimeSeriesRainfall.size();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

//        // 应用缩放
        double scaleFactor = 0.4;
        g2d.scale(scaleFactor, scaleFactor);
        // 应用缩放，绘制主图
        plotRainfallInterpolation(g2d, width, height, stations, stationsTimeSeriesRainfall, mask, timeIndex);

        // 在绘制颜色条之前重置缩放
        g2d.scale(1.0 / scaleFactor, 1.0 / scaleFactor);
//        // 恢复缩放，以便保存图像时所有内容都能正确显示
//        g2d.scale(1 / scaleFactor, 1 / scaleFactor);
        // 获取降雨量数据的最小和最大值
        double[] minMax = getMinMaxPrecipitation(stationsTimeSeriesRainfall);
        double min = minMax[0];
        double max = minMax[1];

        // 绘制颜色条
        drawColorBar(g2d, min, max, width, height);



        // 保存图像到文件
        File outputFile = new File(path);
        ImageIO.write(image, "png", outputFile);

        g2d.dispose();
    }

    private void plotRainfallInterpolation(Graphics2D g2d, int width, int height, ArrayList<RainfallStation> stations, ArrayList<ArrayList<Double>> stationsTimeSeriesRainfall, ArrayList<ArrayList<Double>> mask, int timeIndex) {
//        // 缩放因子和绘图逻辑
//        double scaleFactor = 0.4;  // 根据需要调整缩放因子
//        g2d.scale(scaleFactor, scaleFactor);

        // 获取降雨量数据的最小和最大值
        double[] minMax = getMinMaxPrecipitation(stationsTimeSeriesRainfall);
        double min = minMax[0];
        double max = minMax[1];

        int cellWidth = width / ncolumns + 1;
        int cellHeight = height / nrows + 1;

        // 绘制降水量图
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncolumns; j++) {
                double precipitationValue = stationsTimeSeriesRainfall.get(i).get(j);

                // 将降水量归一化到0-1之间
                double normalizedValue = (precipitationValue - min) / (max - min);

                // 获取mask值
                double maskValue = mask.get(i).get(j);

                // 判断是否在 mask 为 0 的位置，是则颜色设置为灰色
                if (maskValue == 0) {
                    g2d.setColor(Color.white);
                } else {
                    // 将降水量映射到颜色
                    Color cellColor = getColorForNormalizedValue(normalizedValue);
                    g2d.setColor(cellColor);
                }

//                g.fillRect(j * cellWidth + 100, i * cellHeight + 180, cellWidth, cellHeight);
//                g.fillRect(j * cellWidth+1000, i * cellHeight+900, cellWidth, cellHeight);
                g2d.fillRect(j * cellWidth + 500, i * cellHeight + 400, cellWidth, cellHeight);
            }
        }

        // 绘制已知站点
        for (int stationIndex = 0; stationIndex < stationsNumber; stationIndex++) {
            double knownX = stationsX.get(stationIndex);
            double knownY = stationsY.get(stationIndex);
            int knownColumn = (int) Math.floor((knownX - cellcorner.get(0)) / cellsize);
            int knownRow = (int) Math.floor((knownY - cellcorner.get(1)) / cellsize);
            knownRow = nrows - knownRow - 1;

            // 使用相同的坐标计算方法
            int knownXPixel = knownColumn * cellWidth + 500;
            int knownYPixel = knownRow * cellHeight + 400;

            g2d.setColor(Color.darkGray);  // 设置已知站点标记的颜色
            g2d.fillRect(knownXPixel, knownYPixel, 10, 10);

            // 绘制降水量文字
            double precipitation = stationsTimeSeriesRainfall.get(knownRow).get(knownColumn);
            String precipitationText = String.format("%.2f", precipitation);

            // 设置字体大小和样式
            Font textFont = new Font("Arial", Font.PLAIN, 30);
            g2d.setFont(textFont);

            // 绘制文字
            g2d.setColor(Color.darkGray);  // 设置文字颜色，这里假设使用黑色
            g2d.drawString(precipitationText, knownXPixel, knownYPixel + cellHeight + 60);

        }

//        // 绘制颜色条
//        drawColorBar(g2d, min, max, width, height);
//        // 恢复缩放，以免影响其他绘制
//        g2d.scale(1 / scaleFactor, 1 / scaleFactor);

//        // 设置字体大小和样式
//        Font titleFont = new Font("Arial", Font.BOLD, 20); // 使用 Arial 字体，加粗，字体大小 16
//        g2d.setFont(titleFont);
//        g2d.setColor(Color.BLACK);
//        g2d.drawString("Precipitation Interpolation Plot", width / 2 - 140, 115);


    }

    private double[] getMinMaxPrecipitation(ArrayList<ArrayList<Double>> data) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (ArrayList<Double> row : data) {
            for (Double value : row) {
                if (value != NODATA_VALUE) {
                    if (value < min) min = value;
                    if (value > max) max = value;
                }
            }
        }
        return new double[]{min, max};
    }

    private Color getColorForNormalizedValue(double normalizedValue) {
//        // 使用最浅和最深颜色的RGB插值来实现颜色渐变
//        int red = (int) (231 + normalizedValue * (5 - 207));
//        int green = (int) (240 + normalizedValue * (109 - 229));
//        int blue = (int) (247 + normalizedValue * (180 - 243));
        // 使用最浅和最深颜色的RGB插值来实现颜色渐变
        int red = (int) (200 + normalizedValue * (0 - 200));
        int green = (int) (255 + normalizedValue * (100 - 255));
        int blue = (int) (200 + normalizedValue * (0 - 200));
        // 限制分量的范围在 [0, 255]
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));
        return new Color(red, green, blue);
    }

    private void drawColorBar(Graphics2D g2d, double min, double max, int width, int height) {
        int colorBarHeight = 200;
        int colorBarWidth = 50;
        int padding = 200; // 颜色条右边的边距
        // 计算颜色条的X坐标位置
        int colorBarX = width - colorBarWidth - 300;
        int colorBarY = height - colorBarHeight - 100;

        // 绘制颜色条
        for (int i = 0; i < colorBarHeight; i++) {
            double normalizedValue = (double) i / (double) colorBarHeight;
            Color color = getColorForNormalizedValue(1 - normalizedValue); // 1 - normalizedValue使颜色从上到下渐变
            g2d.setColor(color);
            g2d.fillRect(colorBarX, colorBarY + i, colorBarWidth, 1);
        }

        // 设置字体大小和样式
        Font labelFont = new Font("Arial", Font.PLAIN, 13);
        g2d.setFont(labelFont);
        // 绘制最小值和最大值标签
        g2d.setColor(Color.BLACK);
        g2d.drawString(String.format("%.2f", max), colorBarX, colorBarY + colorBarHeight - 220);
        g2d.drawString(String.format("%.2f", min), colorBarX, colorBarY + colorBarHeight + 20);

        // 绘制标题
        String title = "Rainfall (mm)";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        int titleX = colorBarX + (colorBarWidth - titleWidth) / 2;  // 居中显示
        int titleY = colorBarY - 50; // 标题位置在颜色条上方
        g2d.drawString(title, titleX, titleY);
    }

    // 在gridCalculator类中添加以下方法
    public ArrayList<RainfallStation> getStations() {
        return stations;
    }

    public ArrayList<ArrayList<Double>> getMask() {
        return Mask;
    }

    public int getNODATA_VALUE() {
        return NODATA_VALUE;
    }

    public int getnrows() {
        return nrows;
    }

    public int getncolumns() {
        return ncolumns;
    }

    public double getcellsize() {
        return cellsize;
    }

    public ArrayList<Double> getcellcorner() {
        return cellcorner;
    }

    public ArrayList<ArrayList<Double>> getDEM() {
        return DEM;
    }

    public ArrayList<ArrayList<Double>> getgridRainfall_IDW() {
        return gridRainfall_IDW;
    }

    public ArrayList<ArrayList<Double>> getgridRainfall_Kriging() {
        return gridRainfall_Kriging;
    }
}
