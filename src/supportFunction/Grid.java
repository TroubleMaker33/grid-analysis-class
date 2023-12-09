package supportFunction;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class Grid {
    private int ROW;
    private int COLUMN;
    private double CELL_CORNER_X;
    private double CELL_CORNER_Y;
    private int CELL_SIZE;
    private int NODATA_VALUE;
    private double[][] Data;
    // private double[][] Mask;

    //按行读入文件，将每一行作为一个字符串保存在ArrayList中
    public ArrayList<String> readfiles(String fileName){
        ArrayList<String> Info=new ArrayList<String>();  //这个ArrayList用于保存文件内容，一行为一个String
        File myFile=new File(fileName);
        if(!myFile.exists()){
            System.out.println("文件对象创建失败");
        }else{
            try {  //开始读入文件
                BufferedReader reader=new BufferedReader(new FileReader(myFile));
                String tempString=null;
                while((tempString=reader.readLine())!=null){
                    Info.add(tempString);  //按行读取，直至读完
                }
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Info;
    }
    // 生成mask，-9999的位置为0，非-9999的位置为1
    public double[][] generateMask() {
        double[][] mask = new double[this.ROW][this.COLUMN];  // Initialize the mask array

        // Generate the mask
        for (int i = 0; i < this.ROW; i++) {
            for (int j = 0; j < this.COLUMN; j++) {
                if (this.Data[i][j] == this.NODATA_VALUE) {
                    mask[i][j] = 0;
                } else {
                    mask[i][j] = 1;
                }
            }
        }

        return mask;
    }

    public Grid(String dominFile){
        ArrayList<String> dominArrayList=readfiles(dominFile);
        //读取行列等信息
        String[] tempInfoSplit=dominArrayList.get(0).split("\\s+");  //多个空格分隔字符串
        this.COLUMN = Integer.parseInt(tempInfoSplit[1]);
        tempInfoSplit=dominArrayList.get(1).split("\\s+");
        this.ROW = Integer.parseInt(tempInfoSplit[1]);
        tempInfoSplit=dominArrayList.get(2).split("\\s+");
        this.CELL_CORNER_X = Double.parseDouble(tempInfoSplit[1]);;
        tempInfoSplit=dominArrayList.get(3).split("\\s+");
        this.CELL_CORNER_Y = Double.parseDouble(tempInfoSplit[1]);;
        tempInfoSplit=dominArrayList.get(4).split("\\s+");
        this.CELL_SIZE = Integer.parseInt(tempInfoSplit[1]);
        tempInfoSplit=dominArrayList.get(5).split("\\s+");
        this.NODATA_VALUE = Integer.parseInt(tempInfoSplit[1]);

        this.Data = new double[ROW][COLUMN];
        for(int i=0;i<ROW;i++){
            tempInfoSplit=dominArrayList.get(i+6).split("\\s+");  //从第6行开始读
            for(int j=0;j<COLUMN;j++){
                this.Data[i][j]=Integer.parseInt(tempInfoSplit[j]);
            }
        }


    }
    
    public Grid(Grid exampleGrid) {
        this.CELL_CORNER_X = exampleGrid.getCELL_CORNER_X();
        this.CELL_CORNER_Y = exampleGrid.getCELL_CORNER_Y();
        this.CELL_SIZE = exampleGrid.getCELL_SIZE();
        this.COLUMN = exampleGrid.getCOLUMN();
        this.ROW = exampleGrid.getROW();
        this.NODATA_VALUE = exampleGrid.getNODATA_VALUE();
        this.Data = exampleGrid.getData();
    }

    //处理读入的gauge数据，只保留每一行的XY坐标
    public double[][] getGaugeInfo(String GaugeFile){
        ArrayList<String> gaugeArrayList=readfiles(GaugeFile);
        String[] tempInfoSplit=gaugeArrayList.get(0).split("\t");  //按\t分割字符串
        int n=Integer.parseInt(tempInfoSplit[0]);  //第0行表示雨量站数量，从String转为int
        double[][] gaugeInfo=new double[n][2];
        //遍历提取XY
        for(int i=0;i<n;i++){
            //第0表示数量，第1表示列名，从第2行开始读
            tempInfoSplit=gaugeArrayList.get(i+2).split("\t");  //按\t分割字符串
            gaugeInfo[i][0]=Double.parseDouble(tempInfoSplit[3]);
            gaugeInfo[i][1]=Double.parseDouble(tempInfoSplit[4]);
        }
        return gaugeInfo;
    }

    //处理读入的降雨量数据，返回某时间点的一行，其中2-4的索引表示3个雨量站数据。n表示雨量站个数，便于以后添加雨量站和数据
    public double[] getPrecipInfo(String PrecipFile,int timeID,int n){
        ArrayList<String> PrecipArrayList=readfiles(PrecipFile);
        double[] precipInfo=new double[2+n];

        String tempInfo=PrecipArrayList.get(timeID-1+2);  //timeID-1表示从0开始的索引，-2去除前两行
        String[] tempInfoSplit=tempInfo.split("\t");
        for(int i=0;i<2+n;i++){
            precipInfo[i]= Double.parseDouble(tempInfoSplit[i]);
        }
        return precipInfo;
    }

    //按行输出文件
    public void writefiles(String fileName){
        File file = new File(fileName);
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COLUMN; j++) {
                    bw.write(String.valueOf(this.Data[i][j]));
                    bw.write(",");
                }
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void print() {
        int ROW = this.ROW;
        int COLUMN = this.COLUMN;
        double[][] data = this.Data;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                System.out.printf("%.2f\t", data[i][j]);
            }
            System.out.println("");
        }
    }
    //----------------------------------统计------------------------------------
    // 平均值
    public double calculateMean() {
        double sum = 0.0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                sum += Data[i][j];
            }
        }
        return sum / (ROW * COLUMN);
    }
    // 中位数
    public double calculateMedian() {
        int totalElements = ROW * COLUMN;
        double[] flattenedData = new double[totalElements];
        int index = 0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                flattenedData[index++] = Data[i][j];
            }
        }
        Arrays.sort(flattenedData);
        if (totalElements % 2 == 0) {
            int mid1 = totalElements / 2 - 1;
            int mid2 = totalElements / 2;
            return (flattenedData[mid1] + flattenedData[mid2]) / 2;
        } else {
            int mid = totalElements / 2;
            return flattenedData[mid];
        }
    }
    // 均方差
    public double calculateStd() {
        double mean = calculateMean();
        double sumSquaredDiff = 0.0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double diff = Data[i][j] - mean;
                sumSquaredDiff += diff * diff;
            }
        }
        return Math.sqrt(sumSquaredDiff / (ROW * COLUMN));
    }
    // 偏度
    public double calculateSkewness() {
        double mean = calculateMean();
        double std = calculateStd();
        double variance=std*std;
        double sumCubedDiff = 0.0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double diff = Data[i][j] - mean;
                sumCubedDiff += diff * diff * diff;
            }
        }
        double skewness = sumCubedDiff / (ROW * COLUMN);
        skewness /= Math.pow(variance, 1.5);
        return skewness;
    }
    // 距平，与离差相同，返回二维数组
    // 求峰度
    public double calculateKurtosis() {
        double mean = calculateMean();
        double variance = Math.pow(calculateStd(),2);
        double sumFourthDiff = 0.0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double diff = Data[i][j] - mean;
                sumFourthDiff += diff * diff * diff * diff;
            }
        }
        double kurtosis = sumFourthDiff / (ROW * COLUMN);
        kurtosis /= (variance * variance);
        return kurtosis - 3; // 减去3以获得与正态分布的峰度比较
    }

    // 离差,返回二维数组
    public double[][] calculateDeviation() {
        double[][] deviations = new double[ROW][COLUMN];
        double mean = calculateMean();
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double deviation = Data[i][j] - mean;
                deviations[i][j] = deviation;
            }
        }
        return deviations;
    }
    // 丰度 可以多种方式表示，这里用降水量总和
    public double calculateAbundance() {
        double sum = 0.0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                sum=sum+Data[i][j];
            }
        }
        return sum;
    }
    // 自相关系数，-1到1，
    public double calculateAutocorrelation() {
        double[] flattenedData = flattenData();
        int n = flattenedData.length;
        double mean = calculateMean();
        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < n - 1; i++) {
            numerator += (flattenedData[i] - mean) * (flattenedData[i + 1] - mean);
            denominator += Math.pow(flattenedData[i] - mean, 2);
        }
        double autocorrelation = numerator / denominator;
        return autocorrelation;
    }
    // 相关系数  Pearson
    public double calculateCorrelation(Grid otherGrid) {
        double[] thisFlattenedData = flattenData();
        double[] otherFlattenedData = otherGrid.flattenData();
        if (thisFlattenedData.length != otherFlattenedData.length) {
            throw new IllegalArgumentException("The two Grids must have the same dimensions.");
        }
        double mean1 = calculateMean();
        double mean2 = otherGrid.calculateMean();
        double sumProduct = 0.0;
        double sumSqThis = 0.0;
        double sumSqOther = 0.0;
        for (int i = 0; i < thisFlattenedData.length; i++) {
            double thisValue = thisFlattenedData[i];
            double otherValue = otherFlattenedData[i];
            sumProduct += (thisValue-mean1) * (otherValue-mean2);
            sumSqThis += thisValue * thisValue;
            sumSqOther += otherValue * otherValue;
        }
        double correlation = sumProduct / (Math.sqrt(sumSqThis) * Math.sqrt(sumSqOther));
        return correlation;
    }
    // 关联度，灰色分析关联度
    public double calculateAssociation(Grid otherGrid) {
        double[][] t =new double[ROW][COLUMN];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {;
                t[i][j]=Math.abs(Data[i][j]-otherGrid.Data[i][j]);
            }
        }
        // double mmin = Double.MAX_VALUE;
        double mmin = 9999999.00;
        // double mmax = Double.MIN_VALUE;
        double mmax = -999999.00;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (t[i][j] < mmin) {
                    mmin = t[i][j];
                }
                if (t[i][j] > mmax) {
                    mmax = t[i][j];
                }
            }
        }
        // 分辨系数，一般为0.5，也可以改变
        double rho = 0.5;
        // 计算灰色关联系数
        double[][] xs = new double[ROW][COLUMN];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                xs[i][j] = (mmin + rho * mmax) / (t[i][j] + rho * mmax);
            }
        }
        // 计算关联度
        double gd = 0.0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                gd += xs[i][j];
            }
        }
        gd /= (ROW*COLUMN);
        return gd;
    }
    // 拉成一维矩阵
    private double[] flattenData() {
        int totalElements = ROW * COLUMN;
        double[] flattenedData = new double[totalElements];
        int index = 0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                flattenedData[index++] = Data[i][j];
            }
        }
        return flattenedData;
    }

    //----------------------------------画图------------------------------------
    public void plot(ArrayList<Double> gaugeX,ArrayList<Double> gaugeY,ArrayList<Double> P,double CELL_CORNER_X,double CELL_CORNER_Y,int CELL_SIZE,double[][] mask ) {
        JFrame frame = new JFrame("Precipitation Interpolation Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
//                double scaleFactor = 0.2;  // 根据需要调整缩放因子
                double scaleFactor = 0.4;  // 根据需要调整缩放因子

                Graphics2D g2d = (Graphics2D) g;
                g2d.scale(scaleFactor, scaleFactor);

                double[] minMax = getMinMaxPrecipitation();
                double min = minMax[0];
                double max = minMax[1];

                int cellWidth = getWidth() / COLUMN + 1  ;
                int cellHeight = getHeight() / ROW + 1  ;
//                System.out.println("Panel Width: " + cellWidth);
//                System.out.println("Panel Height: " + cellHeight);

                //绘制降水量图
                for (int i = 0; i < ROW; i++) {
                    for (int j = 0; j < COLUMN; j++) {
                        double precipitationValue = Data[i][j];

                        // 将降水量归一化到0-1之间
                        double normalizedValue = (precipitationValue - min) / (max - min);

                        // 获取mask值
                        double maskValue = mask[i][j];

                        // 判断是否在 mask 为 0 的位置，是则颜色设置为灰色
                        if (maskValue == 0) {
                            g.setColor(Color.lightGray);
                        } else {
                            // 将降水量映射到颜色
                            Color cellColor = getColorForNormalizedValue(normalizedValue);
                            g.setColor(cellColor);
                        }

//                        g.fillRect(j * cellWidth + 100, i * cellHeight + 180, cellWidth, cellHeight);
//                        g.fillRect(j * cellWidth+1000, i * cellHeight+900, cellWidth, cellHeight);
                        g.fillRect(j * cellWidth+500, i * cellHeight+400, cellWidth, cellHeight);
                    }
                }

                //绘制已知站点
                for (int k = 0; k < gaugeX.size(); k++) {
                    double knownX = gaugeX.get(k);
                    double knownY = gaugeY.get(k);
                    int knownColumn = (int) Math.floor((knownX - CELL_CORNER_X) / CELL_SIZE);
                    int knownRow = (int) Math.floor((knownY - CELL_CORNER_Y) / CELL_SIZE);
                    knownRow = ROW -knownRow;

                    // 使用相同的坐标计算方法
                    int knownXPixel = (int) (knownColumn * cellWidth +500);
                    int knownYPixel = (int)knownRow* cellHeight +400;

                    g.setColor(Color.darkGray);  // 设置已知站点标记的颜色
                    g.fillRect(knownXPixel, knownYPixel, 10, 10);

                    // 绘制降水量文字
                    double precipitation = P.get(k);
                    String precipitationText = String.format("%.2f", precipitation);

                    // 设置字体大小和样式
                    Font textFont = new Font("Arial", Font.PLAIN, 30);
                    g.setFont(textFont);

                    // 绘制文字
                    g.setColor(Color.darkGray);  // 设置文字颜色，这里假设使用黑色
                    g.drawString(precipitationText, knownXPixel, knownYPixel + cellHeight + 60);

                }
                // 恢复缩放，以免影响其他绘制
                g2d.scale(1 / scaleFactor, 1 / scaleFactor);
                // 设置字体大小和样式
                Font titleFont = new Font("Arial", Font.BOLD, 20); // 使用 Arial 字体，加粗，字体大小 16
                g.setFont(titleFont);

                // 绘制标题
                g.setColor(Color.BLACK);
                g.drawString("Precipitation Interpolation Plot", getWidth() / 2 - 140, 115);

                // 绘制颜色条
                drawColorBar(g, min, max);

            }

            private Color getColorForNormalizedValue(double normalizedValue) {
                // 使用最浅和最深颜色的RGB插值来实现颜色渐变
                int red = (int) (231 + normalizedValue * (5 - 207));
                int green = (int) (240 + normalizedValue * (109 - 229));
                int blue = (int) (247 + normalizedValue * (180 - 243));
                // 限制分量的范围在 [0, 255]
                red = Math.min(255, Math.max(0, red));
                green = Math.min(255, Math.max(0, green));
                blue = Math.min(255, Math.max(0, blue));
                return new Color(red, green, blue);
            }


            private void drawColorBar(Graphics g, double min, double max) {
                int colorBarHeight = getHeight()- 255;
                int colorBarWidth = getWidth()/20;
                int colorBarX = getWidth()  - colorBarWidth -180;
                int colorBarY = getHeight() - colorBarHeight -240 ;  // 居中显示;

                for (int i = 255; i >= 0; i--) {
                    double normalizedValue = i / 255.0;
                    Color color = getColorForNormalizedValue(normalizedValue);
                    g.setColor(color);
                    g.fillRect(colorBarX, colorBarY +colorBarHeight - i * (colorBarHeight / 255), colorBarWidth, colorBarHeight / 255);
                }

                // 设置字体大小和样式
                Font labelFont = new Font("Arial", Font.PLAIN, 15); // 使用 Arial 字体，普通样式，字体大小 12
                g.setFont(labelFont);
                // 绘制最小值和最大值标签
                g.setColor(Color.BLACK);
                g.drawString(String.format("%.2f", max), colorBarX , colorBarY + colorBarHeight -260);
                g.drawString(String.format("%.2f", min), colorBarX , colorBarY + colorBarHeight +20);
                // 绘制标题
                String title = "Rainfall (mm)";
                int titleWidth = g.getFontMetrics().stringWidth(title);
                int titleX = colorBarX + (colorBarWidth - titleWidth) / 2;  // 居中显示
                int titleY = colorBarY + colorBarHeight -300;  // 在最大值上方一些
                g.drawString(title, titleX, titleY);

                //绘制已知站点的标签
                // 绘制已知点
                g.setColor(Color.darkGray);
                g.fillOval(colorBarX-18, colorBarY + colorBarHeight +50, 8, 8);
                // 绘制标签
                Font labelFont1 = new Font("Arial", Font.PLAIN, 14);
                g.setFont(labelFont1);
                g.drawString("Knownpoint", colorBarX + 5, colorBarY + colorBarHeight +58);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private double[] getMinMaxPrecipitation() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                double precipitationValue = Data[i][j];
                // 排除 NODATA_VALUE 值
                if (precipitationValue != NODATA_VALUE) {
                    if (precipitationValue < min) {
                        min = precipitationValue;
                    }
                    if (precipitationValue > max) {
                        max = precipitationValue;
                    }
                }
            }
        }
        return new double[]{min, max};
    }
    public int getROW() {
        return ROW;
    }

    public int getCOLUMN() {
        return COLUMN;
    }

    public double getCELL_CORNER_X() {
        return CELL_CORNER_X;
    }

    public double getCELL_CORNER_Y() {
        return CELL_CORNER_Y;
    }

    public int getCELL_SIZE() {
        return CELL_SIZE;
    }

    public int getNODATA_VALUE() {
        return NODATA_VALUE;
    }

    public double[][] getData() {
        return Data;
    }

    public void setData(double[][] data) {
        Data = data;
    }

}