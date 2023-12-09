//import org.apache.commons.math3.linear.*;
//import org.apache.commons.math3.stat.StatUtils;
//import supportFunction.ARIMA;
//import supportFunction.DataBase;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class TimeSeries {
//    private ArrayList<Double> Data;
//    private static int gaugeid = 59317;
//    private static String starttime = "1962-06-01";
//    private static String endtime = "1963-06-01";
//
//    public static void main(String[] args) {
//        //连接到数据库
//        String myURL="jDataBasec:mysql://localhost:3306";
//        String myUser="root";
//        String myPassword="chovy/11/ZYC";
//        DataBase DataBase=new DataBase(myURL,myUser,myPassword);
//
//        ArrayList<Double> testData=DataBase.getPrecip(gaugeid,starttime,endtime);
//
//        TimeSeries test = new TimeSeries(testData);
//        test.ChangeDetection();
//        test.predictRainfall();
//        // test.getACF();
//        // test.getPACF();
//        // TimeSeries NNResult = new TimeSeries(test.NN(3));
//        // NNResult.print();
//
//    }
//
//    public TimeSeries(ArrayList<Double> Data){
//        this.Data = Data;
//    }
//
//    public void ChangeDetection() {
//        // 设置显著性水平为0.05
//        double alpha = 0.05;
//
//        // 进行降雨量变化趋势判断
//        String[] trend = judgeTrend(Data, alpha);
//        System.out.println( "站点" + gaugeid + "的降雨量" + "在" + starttime + "至" + endtime + "内" + trend[0]);
//    }
//
//    public void predictRainfall() {
//        // 进行降雨量预测
//        ARIMA arima = new ARIMA(listToDoubleArray(Data));
//        int[] model = arima.getARIMAmodel();
//        int prediction = arima.aftDeal(arima.predictValue(model[0], model[1]));
//        System.out.println("站点" + gaugeid + "的降雨量预测值为: " + prediction);
//    }
//
//    //  ------------------------------------------- MK 所需函数 ---------------------------------------------------
//    public static double[] listToDoubleArray(List<Double> doubleList) {
//        double[] doubleArray = new double[doubleList.size()];
//        for (int i = 0; i < doubleList.size(); i++) {
//            doubleArray[i] = doubleList.get(i);
//        }
//        return doubleArray;
//    }
//    // 计算序列的累积量S
//    public static int calculateS(double[] x) {
//        int n = x.length; // 序列的长度
//        int s = 0; // 累积量的初始值
//        for (int i = 0; i < n - 1; i++) { // 遍历序列中的每个元素
//            for (int j = i + 1; j < n; j++) { // 与后面的元素进行比较
//                s += Math.signum(x[j] - x[i]); // 根据差值的符号累加
//            }
//        }
////        System.out.println("s值: " + s);
//        return s; // 返回累积量的值
//    }
//
//    // 计算序列的方差Var(S)
//    public static double calculateVarS(double[] data) {
//        int n = data.length; // 序列的长度
//        double[] uniquedata = Arrays.stream(data).distinct().toArray(); // 去除序列中的重复值
//        int g = uniquedata.length; // 去重后序列的长度
//        if (n == g) { // 如果序列中没有重复值
//            return n * (n - 1) * (2 * n + 5) / 18.0; // 直接计算方差公式
//        } else { // 如果序列中有重复值
//            int[] tp = new int[g]; // 创建一个数组，用于存储每个去重后元素在原序列中出现的次数
//            for (int i = 0; i < g; i++) { // 遍历去重后序列中的每个元素
//                for (int j = 0; j < n; j++) { // 在原序列中查找该元素出现的次数
//                    if (uniquedata[i] == data[j]) { // 如果找到了该元素
//                        tp[i]++; // 将对应位置的计数器加一
//                    }
//                }
//            }
//            double sum = 0; // 创建一个变量，用于存储方差公式中求和部分的结果
//            for (int t : tp) { // 遍历每个去重后元素出现的次数
//                sum += t * (t - 1) * (2 * t + 5); // 根据公式累加求和
//            }
//            double vars = (n * (n - 1) * (2 * n + 5) - sum) / 18.0;
//            return vars; // 返回方差的值
//        }
//    }
//
//    // 计算序列的标准化统计量Z
//    public static double calculateZ(double[] data) {
//        // int n = data.length; // 序列的长度
//        int s = calculateS(data); // 计算累积量S
//        double varS = calculateVarS(data); // 计算方差Var(S)
//        if (s > 0) { // 如果S大于0
//            return (s - 1) / Math.sqrt(varS); // 计算标准化统计量Z
//        } else if (s < 0) { // 如果S小于0
//            return (s + 1) / Math.sqrt(varS); // 计算标准化统计量Z
//        } else { // 如果S等于0
//            return 0; // 返回0作为标准化统计量Z
//        }
//    }
//
//    // 判断序列是否存在显著变化趋势，并返回趋势类型和显著性水平
//    public static String[] judgeTrend(List<Double> X, double alpha) {
//        double[] x = listToDoubleArray(X);
//        double z = calculateZ(x); // 计算标准化统计量Z
////        NormalDistribution nd = new NormalDistribution(); // 创建一个正态分布对象
////        double zCritical = nd.inverseCumulativeProbability(1 - alpha / 2); // 计算给定显著性水平下的临界值
//        double zCritical = 1.96;
//        String[] result = new String[1]; // 创建一个字符串数组，用于存储判断结果
//        if (Math.abs(z) < zCritical) { // 如果Z的绝对值小于临界值
//            result[0] = "没有变化趋势"; // 表示序列没有显著变化趋势
//        } else { // 如果Z的绝对值大于或等于临界值
//            if (z > 0) { // 如果Z大于0
//                result[0] = "存在上升趋势"; // 表示序列存在显著上升趋势
//            } else { // 如果Z小于0
//                result[0] = "存在下降趋势"; // 表示序列存在显著下降趋势
//            }
//        }
//        return result; // 返回判断结果
//    }
//
//    // 定义一个方法，用于判断序列是否存在突变点，并返回突变点位置和类型
//    public static String[] judgeBreakPoint(double[] data, double alpha) {
//        int n = data.length; // 序列的长度
//        int[] Sk = new int[n];   // 定义累计量序列 Sk
//        double[] UFk = new double[n]; // 定义统计量 UFk
//        int s = 0;
//
//        // 正序列
//        for (int i = 1; i < n; i++) {
//            for (int j = 0; j < i; j++) {
//                if (data[i] > data[j]) {
//                    s++;
//                }
//            }
//            Sk[i] = s;
//            double E = i * (i + 1) / 4.0; // Sk[i]的均值
//            double Var = i * (i + 1) * (2 * i + 5) / 72.0; // Sk[i]的方差
//            UFk[i] = (Sk[i] - E) / Math.sqrt(Var);
//        }
//
//        int[] Sk2 = new int[n];   // 定义逆序累计量序列 Sk2
//        double[] UBk = new double[n]; // 定义统计量 UBk
//        int s2 = 0;
//
//        double [] Data2 = new double[n];
//        for (int i = 0; i < n; i++) {
//            Data2[i] = data[n - 1 - i]; // 按时间序列逆转样本 Data
//        }
//        // 逆序列-
//        for (int i = 1; i < n; i++) {
//            for (int j = 0; j < i; j++) {
//                if (Data2[i] > Data2[j]) {
//                    s2++;
//                }
//            }
//            Sk2[i] = s2;
//            double E = i * (i + 1) / 4.0; // Sk2[i]的均值
//            double Var = i * (i + 1) * (2 * i + 5) / 72.0; // Sk2[i]的方差
//            UBk[i] = 0 - (Sk2[i] - E) / Math.sqrt(Var);
//        }
//
//        List<Integer> p = new ArrayList<>();
//        double[] u = new double[UFk.length];
//        for (int i = 0; i < UFk.length; i++) {
//            u[i] = UFk[i] - UBk[i];
//        }
//        for (int i = 1; i < u.length; i++) {
//            if (u[i - 1] * u[i] < 0) {
//                p.add(i);
//            }
//        }
//        String[] result = new String[2];
//        if (!p.isEmpty()) {
//            result[0]  = "检测到突变点";
//            StringBuilder pString = new StringBuilder();
//            for (int i : p) {
//                pString.append(i).append(" ");
//            }
//            result[1] = pString.toString();
//        } else {
//            result[0]  = "未检测到突变点";
//        }
//        return result; // 返回判断结果
//    }
//
//    public void getACF(){
//        ArrayList<Double> data = new ArrayList<Double>(this.Data);
//        double sum_data = 0.0;
//        for (int i=0; i<data.size(); i++){
//            sum_data = sum_data + data.get(i);
//        }
//        for (int i=0; i<data.size(); i++){
//            data.set(i, data.get(i)-sum_data/data.size());
//        }
//        ArrayList<Double> ACF = new ArrayList<Double>();
//        for (int i=1; i<data.size()-1; i++){
//            double test[] = new double[data.size()-i];
//            double sum_test = 0.0;
//            for (int j=0; j<data.size()-i; j++){
//                test[j] = data.get(j)*data.get(j+i);
//                sum_test = sum_test + test[j];
//            }
//            ACF.add(sum_test/data.size());
//        }
//        System.out.printf("lag\t");
//        System.out.printf("ACF\n");
//        for (int i=0; i<ACF.size();i++){
//            System.out.printf("%d\t",i+1);
//            System.out.printf("%.2f\n",ACF.get(i));
//        }
//    }
//
//    public double getCorr(ArrayList<Double> data, int order){
//        double sum_data = 0.0;
//        for (int i=0; i<data.size(); i++){
//            sum_data = sum_data + data.get(i);
//        }
//        for (int i=0; i<data.size(); i++){
//            data.set(i, data.get(i)-sum_data/data.size());
//        }
//        double test[] = new double[data.size()-order];
//        double sum_test = 0.0;
//        for (int j=0; j<data.size()-order; j++){
//            test[j] = data.get(j)*data.get(j+order);
//            sum_test = sum_test + test[j];
//        }
//        return sum_test/data.size();
//    }
//
//    public void getPACF(){
//        ArrayList<Double> data = new ArrayList<Double>(this.Data);
//        ArrayList<Double> PACF = new ArrayList<Double>();
//
//        for (int i=1; i<data.size()-1; i++){
//            double[][] Pk = new double[i][i];
//            for (int r=0;r<i;r++){
//                for (int c=0;c<i;c++){
//                    if (c==r){
//                        Pk[r][c] = getCorr(data,0);
//                        // System.out.printf("%.2f",Pk[r][c]);System.out.print("\t");
//                    }
//                    else if (c<r){
//                        Pk[r][c] = getCorr(data,r-c);
//                        // System.out.printf("%.2f",Pk[r][c]);System.out.print("\t");
//                    }
//                    else if (c>r){
//                        Pk[r][c] = getCorr(data,c-r);
//                        // System.out.printf("%.2f",Pk[r][c]);System.out.print("\t");
//                    }
//                }
//                // System.out.println("");
//            }
//            RealMatrix A_apache = new Array2DRowRealMatrix(Pk, true);
//            RealVector B_apache = new ArrayRealVector(new double[i]);
//            for (int r=0;r<i;r++){
//                B_apache.setEntry(r,getCorr(data,r+1));
//            }
//            DecompositionSolver solver = new LUDecomposition(A_apache).getSolver();
//            RealVector coef = solver.solve(B_apache);
//            PACF.add(coef.getEntry(i-1));
//        }
//        System.out.printf("lag\t");
//        System.out.printf("PACF\n");
//        for (int i=0; i<PACF.size();i++){
//            System.out.printf("%d\t",i+1);
//            System.out.printf("%.2f\n",PACF.get(i));
//        }
//    }
//
//    public ArrayList<Double> mapminmax(ArrayList<Double> data) {
//        double[] data_array = new double[data.size()];
//        for (int i=0;i<data.size();i++){
//            data_array[i] = data.get(i);
//        }
//        double dataMax = StatUtils.max(data_array);
//        double dataMin = StatUtils.min(data_array);
//        ArrayList<Double> mapminmaxed = new ArrayList<Double>();
//        for (int i=0;i<data.size();i++){
//            mapminmaxed.add(i,(data_array[i]-dataMin)/(dataMax-dataMin));
//        }
//        return mapminmaxed;
//
//    }
//
//    public ArrayList<Double> demapminmax(List<Double> data, ArrayList<Double> origninalData) {
//        double[] origninalData_array = new double[origninalData.size()];
//        for (int i=0;i<origninalData.size();i++){
//            origninalData_array[i] = origninalData.get(i);
//        }
//        double origninalDataMax = StatUtils.max(origninalData_array);
//        double origninalDataMin = StatUtils.min(origninalData_array);
//        ArrayList<Double> demapminmaxed = new ArrayList<Double>();
//        for (int i=0;i<data.size();i++){
//            demapminmaxed.add(i,data.get(i)*(origninalDataMax-origninalDataMin)+origninalDataMin);
//        }
//        return demapminmaxed;
//    }
//
//    public ArrayList<Double> NN(int window) {
//        ArrayList<Double> P = new ArrayList<Double>(this.Data);
//        P = mapminmax(P);
//
//        double[][] X = new double[(P.size()-1)/window-1][window];
//        double[][] Y = new double[(P.size()-1)/window-1][window];
//
//        int r=0;
//        for (int i=P.size()-window-1;i>=P.size()%window;i=i-3){
//            for (int c=0; c<window;c++){
//                X[r][window-c-1] = P.get(i-c);
//                Y[r][window-c-1] = P.get(i-c+window);
//            }
//            r++;
//        }
//
//        NeuralNetwork nn = new NeuralNetwork(window,2,window);
//
//        List<Double>output;
//
//        nn.fit(X, Y, 500000);
//
//        double[][] input = new double[1][window];
//        int c = window-1;
//        for (int i = P.size()-1; i>=P.size()-window;i--){
//            input[0][c] = P.get(i);
//            c--;
//        }
//        ArrayList<Double> result = new ArrayList<Double>();
//        for(double d[]:input)
//        {
//            output = nn.predict(d);
//            result = demapminmax(output, this.Data);
//        }
//        return result;
//    }
//
//    public void print(){
//        ArrayList<Double> data = this.Data;
//        System.out.printf("Estimated Precipitation\n");
//        for (int i=0;i<data.size();i++){
//            System.out.printf("%d\t%.2f\n",i,data.get(i));
//        }
//    }
//
//    public ArrayList<Double> getData() {
//        return Data;
//    }
//}
