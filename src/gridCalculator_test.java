import gridCalculator.gridCalculator;
import gridDatabase.gridDatabase;
import RainfallStation.RainfallStation;
import supportFunction.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.sql.*;
import java.util.concurrent.*;

public class gridCalculator_test {
    public static void main(String[] args) throws Exception {
        // 使用 ExecutorService 管理线程
        ExecutorService executor = Executors.newCachedThreadPool();

//        // 创建并启动读取降雨站和DEM数据的线程
//        Callable<readRainFallStation> readRainFallStationCallable = () -> new readRainFallStation("src/gauges.txt", "src/rainfall.txt");
//        Callable<readDEM> readDEMCallable = () -> new readDEM("src/domin.asc");
//
//        Future<readRainFallStation> rRFSFuture = executor.submit(readRainFallStationCallable);
//        Future<readDEM> rDEMFuture = executor.submit(readDEMCallable);
        // 创建并启动读取降雨站和DEM数据的线程
        Future<readRainFallStation> rRFSFuture = executor.submit(() -> {
            System.out.println("readRainFallStation线程开始运行...");
            return new readRainFallStation("data/gauges.txt", "data/rainfall.txt");
        });
        Future<readDEM> rDEMFuture = executor.submit(() -> {
            System.out.println("readDEM线程开始运行...");
            return new readDEM("data/domin.asc");
        });

        // 等待线程完成并获取结果
        readRainFallStation rRFS = rRFSFuture.get();
        readDEM rDEM = rDEMFuture.get();

        // ... (其他与 rRFS 和 rDEM 相关的操作)
        final int NODATA_VALUE = rDEM.NODATA_VALUE;
        final int nrows = rDEM.nrows;
        final int ncolumns = rDEM.ncols;
        final double cellsize = rDEM.cellsize;
        final int stationsNumber = rRFS.stationsNumber;
        final ArrayList<Double> cellcorner = new ArrayList<Double>();
        final double xllcorner = rDEM.xllcorner;
        final double yllcorner = rDEM.yllcorner;
        cellcorner.add(rDEM.xllcorner);
        cellcorner.add(rDEM.yllcorner);
        final ArrayList<ArrayList<Double>> DEM = rDEM.DEM;
        final ArrayList<Double> stationsX = rRFS.stationsX;
        final ArrayList<Double> stationsY = rRFS.stationsY;
        final ArrayList<Double> stationsRainfall = rRFS.stationsRainfall;
        final ArrayList<ArrayList<Double>> stationsTimeSeriesRainfall = rRFS.stationsTimeSeriesRainfall;
        //首先创建数据库
        String URL = "jdbc:mysql://localhost:3306/";
        String Database = "grid";
        String user = "root";
        String password = "chovy/11/ZYC";
//        gridDatabase.createDatabase(URL, Database, user, password);
        //数据库准备工作
//        gridDatabase.createTable(URL, Database, user, password, ncolumns);

        // 进行降雨量插值
            gridCalculator gC = new gridCalculator(NODATA_VALUE, nrows, ncolumns, cellsize,
                    cellcorner, DEM, stationsNumber, stationsX, stationsY, stationsRainfall,stationsTimeSeriesRainfall);
//            // 进行降雨量插值的任务
//            Callable<Void> interpolationTask = () -> {
//                // 插值计算和数据库操作的代码
//                // ... (原有的插值计算和数据库操作代码)
//
//                // 确保所有操作完成后关闭数据库连接等资源
//                // ...
//
//                return null;
//            };
            // 对每个插值方法创建一个Callable任务
        // 对每个插值方法创建一个Callable任务
//        List<Callable<Void>> interpolationTasks = new ArrayList<>();

        int startYear = 2020;
        int endYear = 2021;
        int startIndex = (startYear - 1960) * 12; // 从0开始索引
        int endIndex = (endYear - 1960 + 1) * 12 - 1; // 包含结束年的最后一个月

//        List<Future<Void>> futures = new ArrayList<>();

//        for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
//            // 为每个时间点生成一个线程任务
//            int finalTimeIndex = timeIndex; // 因为要在lambda表达式中使用，需要是effectively final
//            Callable<Void> idwInterpolationTask = () -> {
//                System.out.println("IDW插值线程开始运行，时间索引: " + finalTimeIndex);
//
//                // 生成降雨量数据的特定时间点的ArrayList
//                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(finalTimeIndex));
//                }
//
//                // 使用getter方法获取所需数据
//                ArrayList<RainfallStation> stations = gC.getStations();
//                ArrayList<ArrayList<Double>> mask = gC.getMask();
//                ArrayList<ArrayList<Double>> rainfall_IDW = gC.VoronoiDWithDistance_ForRainfall(rainfallAtSpecificTime);
//
//                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
//                String outputFilename = String.format("photo/rainfall_IDWinterpolation_%d.png", finalTimeIndex);
//                gC.saveRainfallPlot(outputFilename, stations, rainfall_IDW, mask, finalTimeIndex);
//
//                return null; // Callable需要返回值，这里返回null表示没有返回值
//            };
//
//            Callable<Void> krigingInterpolationTask  = () -> {
//                System.out.println("Kriging插值线程开始运行，时间索引: " + finalTimeIndex);
//                // Kriging插值方法的代码
//                // ...
//                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(finalTimeIndex));
//                }
//
//                // 使用getter方法获取所需数据
//                ArrayList<RainfallStation> stations = gC.getStations();
//                ArrayList<ArrayList<Double>> mask = gC.getMask();
//                ArrayList<ArrayList<Double>> rainfall_Kriging = gC.Kriging_ForRainfall(28788,0.137,2.91,rainfallAtSpecificTime);
////                writeASC write_rfi_Kriging = new writeASC("output2/rainfall_Kriginginterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
////                        cellsize, NODATA_VALUE, rainfall_Kriging, 0);
//
//                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
//                String outputFilename = String.format("photo/rainfall_Kriginginterpolation_%d.png", finalTimeIndex);
//                gC.saveRainfallPlot(outputFilename, stations, rainfall_Kriging, mask, finalTimeIndex);
//
//                return null;
//            };
//
//            Callable<Void> splineInterpolationTask = () -> {
//                System.out.println("Spline插值线程开始运行，时间索引: " + finalTimeIndex);
//                // Spline插值方法的代码
//                // ...
//                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(finalTimeIndex));
//                }
//
//                // 使用getter方法获取所需数据
//                ArrayList<RainfallStation> stations = gC.getStations();
//                ArrayList<ArrayList<Double>> mask = gC.getMask();
//                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, finalTimeIndex);
//                ArrayList<ArrayList<Double>> rainfall_Spline = gC.spline_ForRainfall(stationsAtSpecificTime);
////                writeASC write_rfi_Spline = new writeASC("output2/rainfall_Splineinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
////                        cellsize, NODATA_VALUE, rainfall_Spline, 0);
//                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
//                String outputFilename = String.format("photo/rainfall_Splineinterpolation_%d.png", finalTimeIndex);
//                gC.saveRainfallPlot(outputFilename, stations, rainfall_Spline, mask, finalTimeIndex);
//
//                return null;
//            };
//
//            Callable<Void> trendsurfaceInterpolationTask = () -> {
//                System.out.println("TrendSurface插值线程开始运行，时间索引: " + finalTimeIndex);
//                // TrendSurface插值方法的代码
//                // ...
//                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(finalTimeIndex));
//                }
//
//                // 使用getter方法获取所需数据
//                ArrayList<RainfallStation> stations = gC.getStations();
//                ArrayList<ArrayList<Double>> mask = gC.getMask();
//                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, finalTimeIndex);
//                ArrayList<ArrayList<Double>> rainfall_TrendSurface = gC.TrendSurface_ForRainfall(stationsAtSpecificTime);
////                writeASC write_rfi_Trendsurface = new writeASC("output2/rainfall_TrendSurfaceinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
////                        cellsize, NODATA_VALUE, rainfall_TrendSurface, 0);
//                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
//                String outputFilename = String.format("photo/rainfall_TrendSurfaceinterpolation_%d.png", finalTimeIndex);
//                gC.saveRainfallPlot(outputFilename, stations, rainfall_TrendSurface, mask, finalTimeIndex);
//
//                return null;
//            };
//
//            Callable<Void> rbfInterpolationTask = () -> {
//                System.out.println("RBF插值线程开始运行，时间索引: " + finalTimeIndex);
//                // RBF插值方法的代码
//                // ...
//                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(finalTimeIndex));
//                }
//
//                // 使用getter方法获取所需数据
//                ArrayList<RainfallStation> stations = gC.getStations();
//                ArrayList<ArrayList<Double>> mask = gC.getMask();
//                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, finalTimeIndex);
//                ArrayList<ArrayList<Double>> rainfall_RBF = gC.RBF_ForRainfall(stationsAtSpecificTime);
////                writeASC write_rfi_RBF = new writeASC("output2/rainfall_RBFinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
////                        cellsize, NODATA_VALUE, rainfall_RBF, 0);
//                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
//                String outputFilename = String.format("photo/rainfall_RBF_%d.png", finalTimeIndex);
//                gC.saveRainfallPlot(outputFilename, stations, rainfall_RBF, mask, finalTimeIndex);
//
//                return null;
//            };
//
//            // 提交任务到线程池
//            executor.submit(idwInterpolationTask);
//            executor.submit(krigingInterpolationTask);
//            executor.submit(splineInterpolationTask);
//            executor.submit(trendsurfaceInterpolationTask);
//            executor.submit(rbfInterpolationTask);
////            // 提交任务到线程池
////            Future<Void> idwFuture = executor.submit(idwInterpolationTask);
////            Future<Void> krigingFuture = executor.submit(krigingInterpolationTask);
////            Future<Void> splineFuture = executor.submit(splineInterpolationTask);
////            Future<Void> trendsurfaceFuture = executor.submit(trendsurfaceInterpolationTask);
////            Future<Void> rbfFuture = executor.submit(rbfInterpolationTask);
//
////            // 等待所有插值任务完成
////            idwFuture.get();
////            krigingFuture.get();
////            splineFuture.get();
////            trendsurfaceFuture.get();
////            rbfFuture.get();
//
////            // 关闭线程池
////            executor.shutdown();
////            // 等待所有线程结束
////            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
////                executor.shutdownNow();
////            }
//
//            System.out.println("-------------------------------------------------");
//            // 例如使用Executors.newFixedThreadPool来创建线程池
//            // ExecutorService executor = Executors.newFixedThreadPool(10);
//            // Future<Void> future = executor.submit(idwInterpolationTask);
//        }
//
//
//        // 关闭线程池，不再接受新任务
//        executor.shutdown();
//        try {
//            // 等待现有任务完成
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                executor.shutdownNow(); // 取消当前执行的任务
//            }
//        } catch (InterruptedException ie) {
//            executor.shutdownNow();
//            Thread.currentThread().interrupt(); // 保留中断状态
//        }

//        System.out.println("所有插值任务已完成。");

        Callable<Void> idwInterpolationTask = () -> {
            for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
                System.out.println("IDW插值线程开始运行，时间索引: " + timeIndex);

                // 生成降雨量数据的特定时间点的ArrayList
                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
                }

                // 使用getter方法获取所需数据
                ArrayList<RainfallStation> stations = gC.getStations();
                ArrayList<ArrayList<Double>> mask = gC.getMask();
                ArrayList<ArrayList<Double>> rainfall_IDW = gC.VoronoiDWithDistance_ForRainfall(rainfallAtSpecificTime);

                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
                String outputFilename = String.format("photo/rainfall_IDWinterpolation_%d.png", timeIndex);
                gC.saveRainfallPlot(outputFilename, stations, rainfall_IDW, mask, timeIndex);
            }
            return null; // Callable需要返回值，这里返回null表示没有返回值
        };
        // 提交任务到线程池
        Future<Void> idwFuture = executor.submit(idwInterpolationTask);

        Callable<Void> krigingInterpolationTask  = () -> {
            for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
                try {
                    System.out.println("Kriging插值线程开始运行，时间索引: " + timeIndex);

                    // 生成降雨量数据的特定时间点的ArrayList
                    ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
                    for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
                        rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
                    }

                    // 使用getter方法获取所需数据
                    ArrayList<RainfallStation> stations = gC.getStations();
                    ArrayList<ArrayList<Double>> mask = gC.getMask();
                    ArrayList<ArrayList<Double>> rainfall_Kriging = gC.Kriging_ForRainfall(28788,0.137,2.91,rainfallAtSpecificTime);

                    // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
                    String outputFilename = String.format("photo/rainfall_Kriginginterpolation_%d.png", timeIndex);
                    gC.saveRainfallPlot(outputFilename, stations, rainfall_Kriging, mask, timeIndex);

                } catch (RuntimeException e) {
                    System.err.println("在时间索引 " + timeIndex + " 处克里金插值失败: " + e.getMessage());
                    // 可以在这里进行额外的错误处理，例如记录错误信息
                    // 继续下一个时间点的插值
                }
            }
            return null;
        };
        Future<Void> krigingFuture = executor.submit(krigingInterpolationTask);

        Callable<Void> splineInterpolationTask = () -> {
            for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
                System.out.println("Spline插值线程开始运行，时间索引: " + timeIndex);
                // Spline插值方法的代码
                // ...
                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
                }

                // 使用getter方法获取所需数据
                ArrayList<RainfallStation> stations = gC.getStations();
                ArrayList<ArrayList<Double>> mask = gC.getMask();
                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
                ArrayList<ArrayList<Double>> rainfall_Spline = gC.spline_ForRainfall(stationsAtSpecificTime);
                //                writeASC write_rfi_Spline = new writeASC("output2/rainfall_Splineinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
                //                        cellsize, NODATA_VALUE, rainfall_Spline, 0);
                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
                String outputFilename = String.format("photo/rainfall_Splineinterpolation_%d.png", timeIndex);
                gC.saveRainfallPlot(outputFilename, stations, rainfall_Spline, mask, timeIndex);
            }
            return null;
        };
        Future<Void> splineFuture = executor.submit(splineInterpolationTask);

        Callable<Void> trendsurfaceInterpolationTask = () -> {
            for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
                System.out.println("TrendSurface插值线程开始运行，时间索引: " + timeIndex);
                // TrendSurface插值方法的代码
                // ...
                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
                }

                // 使用getter方法获取所需数据
                ArrayList<RainfallStation> stations = gC.getStations();
                ArrayList<ArrayList<Double>> mask = gC.getMask();
                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
                ArrayList<ArrayList<Double>> rainfall_TrendSurface = gC.TrendSurface_ForRainfall(stationsAtSpecificTime);
                //                writeASC write_rfi_Trendsurface = new writeASC("output2/rainfall_TrendSurfaceinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
                //                        cellsize, NODATA_VALUE, rainfall_TrendSurface, 0);
                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
                String outputFilename = String.format("photo/rainfall_TrendSurfaceinterpolation_%d.png", timeIndex);
                gC.saveRainfallPlot(outputFilename, stations, rainfall_TrendSurface, mask, timeIndex);
            }
            return null;
        };
        Future<Void> trendsurfaceFuture = executor.submit(trendsurfaceInterpolationTask);

        Callable<Void> rbfInterpolationTask = () -> {
            for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
                System.out.println("RBF插值线程开始运行，时间索引: " + timeIndex);
                // RBF插值方法的代码
                // ...
                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
                }

                // 使用getter方法获取所需数据
                ArrayList<RainfallStation> stations = gC.getStations();
                ArrayList<ArrayList<Double>> mask = gC.getMask();
                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
                ArrayList<ArrayList<Double>> rainfall_RBF = gC.RBF_ForRainfall(stationsAtSpecificTime);
                //                writeASC write_rfi_RBF = new writeASC("output2/rainfall_RBFinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
                //                        cellsize, NODATA_VALUE, rainfall_RBF, 0);
                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
                String outputFilename = String.format("photo/rainfall_RBF_%d.png", timeIndex);
                gC.saveRainfallPlot(outputFilename, stations, rainfall_RBF, mask, timeIndex);
            }
            return null;
        };
        Future<Void> rbfFuture = executor.submit(rbfInterpolationTask);

        Callable<Void> ThiessenInterpolationTask = () -> {
            for (int timeIndex = startIndex; timeIndex <= endIndex; timeIndex++) {
                System.out.println("Thiessen插值线程开始运行，时间索引: " + timeIndex);
                // RBF插值方法的代码
                // ...
                ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
                for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
                }

                // 使用getter方法获取所需数据
                ArrayList<RainfallStation> stations = gC.getStations();
                ArrayList<ArrayList<Double>> mask = gC.getMask();
                ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
                ArrayList<ArrayList<Double>> rainfall_RBF = gC.Thiessen_ForRainfall(stationsAtSpecificTime);
                //                writeASC write_rfi_RBF = new writeASC("output2/rainfall_RBFinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
                //                        cellsize, NODATA_VALUE, rainfall_RBF, 0);
                // 插值结果转换为图片并保存，文件名中包含时间索引作为ID
                String outputFilename = String.format("photo/rainfall_Thiessen_%d.png", timeIndex);
                gC.saveRainfallPlot(outputFilename, stations, rainfall_RBF, mask, timeIndex);
            }
            return null;
        };
        Future<Void> thiessenFuture = executor.submit(ThiessenInterpolationTask);

        // 等待所有插值任务完成
        idwFuture.get();
        krigingFuture.get();
        splineFuture.get();
        trendsurfaceFuture.get();
        rbfFuture.get();
        thiessenFuture.get();
//        naturalneighborFuture.get();
        // 关闭线程池
        executor.shutdown();
        // 等待所有线程结束
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        System.out.println("所有插值任务已完成。");

        System.out.println("-------------------------------------------------");

//        for(ArrayList<Double> i:rainfall_IDW){
//            System.out.println(i);
//        }
        System.out.println();
//        Callable<Void> idwInterpolationTask = () -> {
//            System.out.println("IDW插值线程开始运行...");
//            // IDW插值方法的代码
//            // ...
//            // 根据降雨站降雨量的IDW插值结果
//            int timeIndex = 2; // 第三个时间点，因为索引是从0开始的
//            ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//            for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                if (stationTimeSeries.size() > timeIndex) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
//                } else {
//                    // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
//                    rainfallAtSpecificTime.add(Double.NaN); // 或者您可以选择添加一个默认值
//                }
//            }
//            // 在gridCalculator_test类中使用getter方法
//            ArrayList<RainfallStation> stations = gC.getStations();
//            ArrayList<ArrayList<Double>> mask = gC.getMask();
//            ArrayList<ArrayList<Double>> rainfall_IDW = gC.VoronoiDWithDistance_ForRainfall(rainfallAtSpecificTime);
//            writeASC write_rfi_IDW = new writeASC("output2/rainfall_IDWinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
//                    cellsize, NODATA_VALUE, rainfall_IDW, 0);
//            // 插值结果转换为图片并保存
//            gC.saveRainfallPlot("output2/rainfall_IDWinterpolation.png", stations, rainfall_IDW, mask, timeIndex);
//            return null; // Callable需要返回值，这里返回null表示没有返回值
//        };
//
//        Callable<Void> krigingInterpolationTask  = () -> {
//            System.out.println("Kriging插值线程开始运行...");
//            // Kriging插值方法的代码
//            // ...
//            int timeIndex = 2; // 第三个时间点，因为索引是从0开始的
//            ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//            for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                if (stationTimeSeries.size() > timeIndex) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
//                } else {
//                    // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
//                    rainfallAtSpecificTime.add(Double.NaN); // 或者您可以选择添加一个默认值
//                }
//            }
//            ArrayList<RainfallStation> stations = gC.getStations();
//            ArrayList<ArrayList<Double>> mask = gC.getMask();
//            ArrayList<ArrayList<Double>> rainfall_Kriging = gC.Kriging_ForRainfall(28788,0.137,2.91,rainfallAtSpecificTime);
//            writeASC write_rfi_Kriging = new writeASC("output2/rainfall_Kriginginterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
//                    cellsize, NODATA_VALUE, rainfall_Kriging, 0);
//            // 插值结果转换为图片并保存
//            gC.saveRainfallPlot("output2/rainfall_Kriginginterpolation.png", stations, rainfall_Kriging, mask, timeIndex);
//            return null;
//        };
//
//        Callable<Void> splineInterpolationTask = () -> {
//            System.out.println("Spline插值线程开始运行...");
//            // Spline插值方法的代码
//            // ...
//            int timeIndex = 2;
//            ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//            for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                if (stationTimeSeries.size() > timeIndex) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
//                } else {
//                    // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
//                    rainfallAtSpecificTime.add(Double.NaN); // 或者您可以选择添加一个默认值
//                }
//            }
//            ArrayList<RainfallStation> stations = gC.getStations();
//            ArrayList<ArrayList<Double>> mask = gC.getMask();
//            ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
//            ArrayList<ArrayList<Double>> rainfall_Spline = gC.spline_ForRainfall(stationsAtSpecificTime);
//            writeASC write_rfi_Spline = new writeASC("output2/rainfall_Splineinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
//                    cellsize, NODATA_VALUE, rainfall_Spline, 0);
//            // 插值结果转换为图片并保存
//            gC.saveRainfallPlot("output2/rainfall_Splineinterpolation.png", stations, rainfall_Spline, mask, timeIndex);
//            return null;
//        };
//
//        Callable<Void> trendsurfaceInterpolationTask = () -> {
//            System.out.println("TrendSurface插值线程开始运行...");
//            // TrendSurface插值方法的代码
//            // ...
//            int timeIndex = 2;
//            ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//            for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                if (stationTimeSeries.size() > timeIndex) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
//                } else {
//                    // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
//                    rainfallAtSpecificTime.add(Double.NaN); // 或者您可以选择添加一个默认值
//                }
//            }
//            ArrayList<RainfallStation> stations = gC.getStations();
//            ArrayList<ArrayList<Double>> mask = gC.getMask();
//            ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
//            ArrayList<ArrayList<Double>> rainfall_TrendSurface = gC.TrendSurface_ForRainfall(stationsAtSpecificTime);
//            writeASC write_rfi_Trendsurface = new writeASC("output2/rainfall_TrendSurfaceinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
//                    cellsize, NODATA_VALUE, rainfall_TrendSurface, 0);
//            // 插值结果转换为图片并保存
//            gC.saveRainfallPlot("output2/rainfall_TrendSurfaceinterpolation.png", stations, rainfall_TrendSurface, mask, timeIndex);
//            return null;
//        };
//
//        Callable<Void> rbfInterpolationTask = () -> {
//            System.out.println("RBF插值线程开始运行...");
//            // RBF插值方法的代码
//            // ...
//            int timeIndex = 2;
//            ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
//            for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
//                if (stationTimeSeries.size() > timeIndex) {
//                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
//                } else {
//                    // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
//                    rainfallAtSpecificTime.add(Double.NaN); // 或者您可以选择添加一个默认值
//                }
//            }
//            ArrayList<RainfallStation> stations = gC.getStations();
//            ArrayList<ArrayList<Double>> mask = gC.getMask();
//            ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
//            ArrayList<ArrayList<Double>> rainfall_RBF = gC.RBF_ForRainfall(stationsAtSpecificTime);
//            writeASC write_rfi_RBF = new writeASC("output2/rainfall_RBFinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
//                    cellsize, NODATA_VALUE, rainfall_RBF, 0);
//            // 插值结果转换为图片并保存
//            gC.saveRainfallPlot("output2/rainfall_RBFinterpolation.png", stations, rainfall_RBF, mask, timeIndex);
//            return null;
//        };
//
////        Callable<Void> naturalneighborInterpolationTask = () -> {
////            System.out.println("NaturalNeighbor插值线程开始运行...");
////            // NaturalNeighbor插值方法的代码
////            // ...
////            int timeIndex = 2;
////            ArrayList<Double> rainfallAtSpecificTime = new ArrayList<>();
////            for (ArrayList<Double> stationTimeSeries : stationsTimeSeriesRainfall) {
////                if (stationTimeSeries.size() > timeIndex) {
////                    rainfallAtSpecificTime.add(stationTimeSeries.get(timeIndex));
////                } else {
////                    // 某些降雨站可能没有足够的数据，您可以决定如何处理这种情况
////                    rainfallAtSpecificTime.add(Double.NaN); // 或者您可以选择添加一个默认值
////                }
////            }
////            ArrayList<RainfallStation> stationsAtSpecificTime = gC.mergeRainfallStationInfo(stationsX, stationsY, stationsTimeSeriesRainfall, timeIndex);
////            ArrayList<ArrayList<Double>> rainfall_NaturalNeighbor = gC.NaturalNeighbor_ForRainfall(stationsAtSpecificTime);
////            writeASC write_rfi_NaturalNeighbor = new writeASC("output2/rainfall_NaturalNeighborinterpolation.asc", nrows, ncolumns, rDEM.xllcorner, rDEM.yllcorner,
////                    cellsize, NODATA_VALUE, rainfall_NaturalNeighbor, 0);
////            // 插值结果转换为图片并保存
////            supportFunction.saveImg_Double(rainfall_NaturalNeighbor, "output2/rainfall_NaturalNeighborinterpolation.jpg", NODATA_VALUE);
////            return null;
////        };
//
//        // 提交任务到线程池
//        Future<Void> idwFuture = executor.submit(idwInterpolationTask);
//        Future<Void> krigingFuture = executor.submit(krigingInterpolationTask);
//        Future<Void> splineFuture = executor.submit(splineInterpolationTask);
//        Future<Void> trendsurfaceFuture = executor.submit(trendsurfaceInterpolationTask);
//        Future<Void> rbfFuture = executor.submit(rbfInterpolationTask);
////        Future<Void> naturalneighborFuture = executor.submit(naturalneighborInterpolationTask);
//
//        // 等待所有插值任务完成
//        idwFuture.get();
//        krigingFuture.get();
//        splineFuture.get();
//        trendsurfaceFuture.get();
//        rbfFuture.get();
////        naturalneighborFuture.get();
//        // 关闭线程池
//        executor.shutdown();
//        // 等待所有线程结束
//        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//            executor.shutdownNow();
//        }
//
//        System.out.println("所有插值任务已完成。");
//
//        System.out.println("-------------------------------------------------");

        //统计
        double[][] matrixIDW = Statistics.readASCAndGetMatrix("output/rainfall_IDWinterpolation.asc");

        double[][] matrixKrig = Statistics.readASCAndGetMatrix("output/rainfall_Kriginginterpolation.asc");
        /*double[][] matrixNNI = Statistics.readASCAndGetMatrix("output/rainfall_NaturalNeighborinterpolation.asc");
        double[][] matrixSpline = Statistics.readASCAndGetMatrix("output/rainfall_RBFinterpolation.asc");
        double[][] matrixRBF = Statistics.readASCAndGetMatrix("output/rainfall_Splineinterpolation.asc");
        double[][] matrixTrend = Statistics.readASCAndGetMatrix("output/rainfall_TrendSurfaceinterpolation.asc");

         */
//        double NODATA_VALUE =  -9999.0;
        StatisticsCalculator.calculateArrayStatistics(matrixIDW, matrixKrig, NODATA_VALUE);
        /*
        calculateArrayStatistics(matrixKrig, matrixNNI, NODATA_VALUE);
        calculateArrayStatistics(matrixNNI, matrixSpline, NODATA_VALUE);
        calculateArrayStatistics(matrixSpline,matrixRBF, NODATA_VALUE);
        calculateArrayStatistics(matrixRBF, matrixTrend, NODATA_VALUE);
        calculateArrayStatistics(matrixTrend, matrixIDW, NODATA_VALUE);

         */


        //读入数据
         /* double[] timeSeries=rRFS.stationsTimeSeriesRainfall.get(0).stream().mapToDouble(Double::doubleValue).toArray();
         double[] timeSeries=rRFS.stationsTimeSeriesRainfall.get(1).stream().mapToDouble(Double::doubleValue).toArray();*/
        double[] timeSeries=rRFS.stationsTimeSeriesRainfall.get(2).stream().mapToDouble(Double::doubleValue).toArray();


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
