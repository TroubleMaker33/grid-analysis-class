import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.*;

import supportFunction.*;

public class PrecipitationInterpolation {

    public static void main(String[] args) throws InterruptedException {
        String Path="." + File.separator + "data";
        //读入研究区数据
        String dominFile=Path+File.separator+"domin.asc";
        Grid result = new Grid(dominFile);
        //生成掩膜
        double[][] mask = result.generateMask();

        //连接到数据库
        String myURL="jdbc:mysql://localhost:3306";
        String myUser="root";
        String myPassword="chovy/11/ZYC";
        DataBase DataBase=new DataBase(myURL,myUser,myPassword);
        DataBase.createPIDataBase();

        //读入降雨量数据
        String gaugeP=Path+File.separator+"rainfall.txt";

        //读入雨量站数据
        String gaugesFile=Path+File.separator+"gauges.txt";
        double[][] gaugeInfo=result.getGaugeInfo(gaugesFile);

        //我们后面都用gaugeXY，但是函数返回两个值比较麻烦，所以这里先返回数组再赋值
        int N=gaugeInfo.length;  //数组长度表示雨量站个数
        ArrayList<Double> gaugeX = new ArrayList<Double>(N);
        ArrayList<Double> gaugeY = new ArrayList<Double>(N);

        for(int i=0;i<N;i++){
            gaugeX.add(gaugeInfo[i][0]);
            gaugeY.add(gaugeInfo[i][1]);
        }

        //读入降雨量数据
        //String gaugeP=Path+File.separator+"rainfall.txt";

        int batchSize=0;
        int startRow=0;
        int endRow=0;
        String method = "RBF";
        boolean usingThread = true; // 是否使用多线程
        if (usingThread){ // 如果使用多线程
            batchSize = 5; // 线程个数
            startRow = 1; // 开始计算的行
            endRow = 50; // 结束计算的行
        } else { // 如果不使用多线程
            batchSize = 1;
            startRow = 12; // 要计算的一行
            endRow = startRow;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(batchSize);
        for (int precipitationRow = startRow; precipitationRow<=endRow; precipitationRow++){
            CompletionService<Grid> completion = new ExecutorCompletionService<>(executorService);
            InterpolationThread thread_test = new InterpolationThread(method,gaugeX,gaugeY,gaugeP,N,result,precipitationRow,mask,usingThread,DataBase);
            if(method =="Lanczos"){
                thread_test.setA(1);// 平滑度
                thread_test.setB(200000); // 影响范围
            }
            if(method=="RBF"){
                thread_test.setRange(1000);// 高斯函数参数
            }
            if (method=="IDW"){
                thread_test.setPower(2);// 反距离的幂
            }
            if(method=="Kriging"){
                thread_test.setModel("sperical");// 克里金函数模型
            }
            if(method=="trendSurface"){
                thread_test.setDegree(3);  //三次趋势面
            }
            completion.submit(thread_test);
            if(!usingThread){
                try {
                    result = completion.take().get();
                } catch (InterruptedException | ExecutionException e) {
                    executorService.shutdownNow();
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }

            }
        }



//----------------------------------------统计函数 测试------------------------------------------------------
        // 先新建二维数组，再赋值返回的离差
        // double[][] devi =new double[result.getROW()][result.getCOLUMN()];
        // devi = result.calculateDeviation();
        // System.out.println("插值结果result[50][50]的离差: " + devi[50][50]);
        // System.out.println("降雨量平均值: " + result.calculateMean());
        // System.out.println("降雨量峰度: " + result.calculateKurtosis());
        // System.out.println("降雨量偏度: " + result.calculateSkewness());
        // System.out.println("降雨量中位数: " + result.calculateMedian());
        // System.out.println("降雨量自相关系数: " + result.calculateAutocorrelation());
        // System.out.println("降雨量均方差: " + result.calculateStd());
        // System.out.println("ID=9与ID=10两个降雨量相关系数: " + result.calculateCorrelation(result2));
        // System.out.println("ID=9与ID=10两个降雨量关联度: " + result.calculateAssociation(result2));

    }

}
