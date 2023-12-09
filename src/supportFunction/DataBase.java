package supportFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

public class DataBase {
    private String url;
    private String user;
    private String password;
    public Connection conn;

    public static void main(String[] args) {

        //---------------------------DataBase连接--------------------------------
        String myURL="jdbc:mysql://localhost:3306";
        String myUser="root";
        String myPassword="chovy/11/ZYC";
        DataBase DataBase=new DataBase(myURL,myUser,myPassword);

        //---------------------------DataBase创建--------------------------------
        // DataBase.createPIDataBase();

        //---------------------------DataBase插入--------------------------------
        String Path="./data";
        String GaugeFile=Path+File.separator+"gauges.txt";
        DataBase.insertGauge(GaugeFile);
        String gaugeP=Path+File.separator+"rainfall.txt";
        DataBase.insertPrecipitation(gaugeP);

        //---------------------------DataBase查询--------------------------------
        DataBase.selectGauge(1);
        DataBase.selectPrecip(59317, "1962-06-01");
        DataBase.selectGrid(15, 20, 20);

        //---------------------------DataBase统计--------------------------------
        DataBase.StatisticOneGrid(12);
        DataBase.StatisticOneGridPoint(50, 50, "RBF");

    }

    //---------------------------------constructor---------------------------------
    public DataBase(String newUrl, String newUser, String newPassword){
        this.url=newUrl;
        this.user=newUser;
        this.password=newPassword;
        //连接数据库
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn=DriverManager.getConnection(url,user,password);
            System.out.println("DataBase connected");
        }catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }
    //---------------------------------create precipitation interpolation data base---------------------------------
    public void createPIDataBase(){
        try{
            Statement stat=conn.createStatement();
//            stat.executeUpdate("DROP DATABASE preciinterp");
            stat.executeUpdate("CREATE DATABASE IF NOT EXISTS preciinterp");
            stat.executeUpdate("USE preciinterp");
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS `Grid` (" +
                    "`GridID` INT PRIMARY KEY," +
                    "`CreateTime` DATETIME," +
                    "`Time` INT," +
                    "`Method` CHAR(20)," +
                    "`Row` INT," +
                    "`Column` INT," +
                    "`CellSize` INT," +
                    "`XLLCorner` DOUBLE," +
                    "`YLLCorner` DOUBLE," +
                    "`NODATA_VALUE` INT," +
                    "`Data` LONGTEXT);");
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS `Gauge` ("+
                    "`GaugeID` INT PRIMARY KEY,"+
                    "`SID` INT,"+
                    "`GaugeName` CHAR(20),"+
                    "`EnName` CHAR(20),"+
                    "`X` DOUBLE,"+
                    "`Y` DOUBLE);");
            stat.executeUpdate("ALTER TABLE gauge ADD INDEX gauge_id_index (SID)");
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS `Precipitation` (" +
                    "`PrecipitationID` INT PRIMARY KEY," +
                    "`GaugeID` INT," +
                    "`Rainfall` DOUBLE," +
                    "`Time` DATE,"+
                    "FOREIGN KEY (`GaugeID`) REFERENCES `Gauge`(`SID`));");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    //---------------------------------read files and insert data---------------------------------

    public double[][] insertGauge(String gaugesFile) {
        //读取雨量站个数
        try (BufferedReader reader1 = new BufferedReader(new FileReader(gaugesFile))) {
            // 第一行获取雨量站个数
            String[] columns1 = reader1.readLine().split("\\s+");
            int N = Integer.parseInt(columns1[0]);
            double[][] gaugeInfo = new double[N][2];

            Statement stat = conn.createStatement();
            stat.executeUpdate("USE preciinterp");
            String insertQuery = "INSERT INTO Gauge (GaugeID, SID, GaugeName, EnName, X, Y) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = conn.prepareStatement(insertQuery);

            List<Integer> sidList = new ArrayList<>();
            List<String> gaugeName = new ArrayList<>();
            List<String> enName = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                try (BufferedReader reader = new BufferedReader(new FileReader(gaugesFile))) {
                    // 跳过前两行
                    reader.readLine();
                    reader.readLine();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] columns = line.split("\\s+"); // 假设列之间使用空格分隔
                        if (columns.length >= 3) {
                            sidList.add(Integer.parseInt(columns[2]));
                            gaugeName.add(columns[1]);
                            enName.add(columns[5]);
                            gaugeInfo[i][0] = Double.parseDouble(columns[3]);
                            gaugeInfo[i][1] = Double.parseDouble(columns[4]);
                            insertStatement.setInt(1, i + 1);
                            insertStatement.setInt(2, sidList.get(i));
                            insertStatement.setString(3, gaugeName.get(i));  // 设置GaugeName
                            insertStatement.setString(4, enName.get(i));  // 设置EnName
                            insertStatement.setDouble(5, gaugeInfo[i][0]);  // 设置X
                            insertStatement.setDouble(6, gaugeInfo[i][1]);  // 设置Y
                        }
                    }
                }
                insertStatement.executeUpdate();
            }
            System.out.println("成功将雨量站数据存储到站点表中");
            return gaugeInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public double[][] insertPrecipitation(String rainFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(rainFile))) {
            String line;

            // 读取文件的第一行，获取行数和列数
            line = reader.readLine();
            String[] headers = line.split("\\s+");

            int rowCount = Integer.parseInt(headers[2]);
            int columnCount = Integer.parseInt(headers[3]);

            // 读取第二行
            String line2 = reader.readLine();
            String[] data = line2.split("\\s+");
            // 初始化 gaugeIDs 数组，用于存储每一列的 GaugeID
            int[] gaugeIDs = new int[columnCount];
            gaugeIDs[0] = Integer.parseInt(data[1]);
            for (int i = 0; i < columnCount; i++) {
                gaugeIDs[i] = Integer.parseInt(data[i + 1]);
            }

            // 用于保存降水量数据的二维数组
            double[][] rainfallData = new double[rowCount][columnCount];

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            // 日期数组，根据降水量的行数生成日期
            String[] dates = new String[rowCount];
            // 解析第一行的日期
            Date firstDate = dateFormat.parse(headers[0]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(firstDate);

            // 生成日期数组
            for (int i = 0; i < rowCount; i++) {
                dates[i] = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.MONTH, 1); // 每行增加一个月
            }

            // 读取文件的剩余行，并解析降水量数据
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split("\\s+");

                // 解析当前行的降水量数据
                for (int i = 2; i < columnCount; i++) {
                    rainfallData[rowIndex][i - 2] = Double.parseDouble(rowData[i]); // 从第三个元素开始
                }
                rowIndex++;
            }

            // 使用参数化查询将数据插入到表中
            Statement stat = conn.createStatement();
            stat.executeUpdate("USE preciinterp");
            stat.executeUpdate("ALTER TABLE Precipitation MODIFY COLUMN PrecipitationID INT AUTO_INCREMENT");
            String insertQuery = "INSERT INTO Precipitation (PrecipitationID, GaugeID, Rainfall, TIME) VALUES (DEFAULT, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(insertQuery)) {
                // 遍历二维数组，插入数据
                for (int row = 0; row < rowCount; row++) {
                    for (int column = 0; column < columnCount; column++) {
                        double rainfall = rainfallData[row][column];

                        // 设置参数并执行插入操作
                        statement.setInt(1, gaugeIDs[column]);
                        statement.setDouble(2, rainfall);
                        statement.setString(3, dates[row]);
                        statement.executeUpdate();
                    }
                }
            }

            System.out.println("成功将降雨数据存储到降雨量表中");
            return rainfallData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertGrid(String Path,String method,Grid result) {
        String outputFile = Path;
        result.writefiles(outputFile);

        int ROW = result.getROW();
        int COLUMN = result.getCOLUMN();
        double CELL_CORNER_X = result.getCELL_CORNER_X();
        double CELL_CORNER_Y = result.getCELL_CORNER_Y();
        int CELL_SIZE = result.getCELL_SIZE();
        try {
            // 获取当前时间
            Date currentDate = new Date();
            Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

            Statement stat = conn.createStatement();
            stat.executeUpdate("USE preciinterp");
            stat.executeUpdate("ALTER TABLE Grid MODIFY COLUMN GridID INT AUTO_INCREMENT");
            stat.executeUpdate("INSERT INTO Grid (GridID, CreateTime, Method, `Row`, `Column`, CellSize, " +
                    "XLLCorner, YLLCorner, NODATA_VALUE, Data) " + "VALUES (DEFAULT, '" + currentTimestamp + "', '" + method + "', '" + ROW + "', '" + COLUMN + "', " +
                    "'" + CELL_SIZE + "', '" + CELL_CORNER_X + "', '" + CELL_CORNER_Y + "', -9999, '" + outputFile + "')");
            System.out.println("成功将插值结果数据插入到Grid表中");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //---------------------------------select---------------------------------
    public void selectGrid(int newGridID,int readR,int readC) {
        //连接并查询
        try {
            //建立preparedstatement对象
            String sqlGrid = "select GridID,CreateTime,Time,Method,CellSize,`Row`,`Column`,XLLCorner,YLLCorner,NODATA_VALUE,Data from Grid where GridID=?";
            PreparedStatement statGrid = conn.prepareStatement(sqlGrid);  //PreparedStatement接口
            //System.out.println("查询Grid的preparestatement对象建立成功");
            statGrid.executeUpdate("use preciinterp");
            statGrid.setInt(1,newGridID);  //用1代表GridID第1个占位，GridID

            //查询Grid
            ResultSet rs1=statGrid.executeQuery();  //查询
            if(rs1.next()){
                int GridID=rs1.getInt("GridID");
                Date CreateTime=rs1.getDate("CreateTime");
                int Time=rs1.getInt("Time");
                String Method=rs1.getString("Method");
                int Row=rs1.getInt("Row");
                int Column=rs1.getInt("Column");
                int CellSize=rs1.getInt("CellSize");
                double XLLCorner=rs1.getDouble("XLLCorner");
                double YLLCorner=rs1.getDouble("YLLCorner");
                int NODATA_VALUE=rs1.getInt("NODATA_VALUE");
                String Data=rs1.getString("Data");
                //System.out.println("编号为"+GridID+"的Grid存放的地址为"+Data);
                double result=readCSVFile(Data,readR,readC);
                System.out.println("\n-------Grid表查询结果-------");
                System.out.println("创建时间："+CreateTime+"\n降雨时间："+Time+"\n插值方法："+Method);
                System.out.println("行数："+Row+"\n列数："+Column+"\n栅格大小："+CellSize+"\n左下角坐标：（"+XLLCorner+","+YLLCorner+")");
                System.out.println("编号为"+GridID+"的Grid "+readR+"行"+readC+"列 对应的降雨量为"+result);
                System.out.println("-----------------------------------");}
            else{
                System.out.println("\n-------Grid表查询结果-------");
                System.out.println("未查找到对应数据。");
                System.out.println("-----------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void selectPrecip(int newGaugeID,String newTime) {
        //连接并查询
        try {
            String sqlPrecip = "select PrecipitationID,GaugeID,Time,Rainfall from precipitation where GaugeID=? and Time=?";
            PreparedStatement statPrecip = conn.prepareStatement(sqlPrecip);  //PreparedStatement接口
            //System.out.println("查询Precip的preparestatement对象建立成功");
            statPrecip.executeUpdate("use preciinterp");
            statPrecip.setInt(1,newGaugeID);  //GaugeID
            statPrecip.setString(2, newTime);
    
            //查询Precip
            ResultSet rs2=statPrecip.executeQuery();  //查询
            if(rs2.next()) {
    
                int PrecipitationID = rs2.getInt("PrecipitationID");
                int GaugeID = rs2.getInt("GaugeID");
                Date Time2 = rs2.getDate("Time");
                double Rainfall = rs2.getDouble("Rainfall");
                System.out.println("\n-------Precipitation表查询结果-------");
                System.out.println("站点：" + GaugeID + "\n时间：" + Time2 + "\n降雨量：" + Rainfall);
                System.out.println("-----------------------------------");
            }
            else{
                System.out.println("\n-------Precipitation表查询结果-------");
                System.out.println("未查找到对应数据。");
                System.out.println("-----------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void selectGauge(int newGaugeID) {
        //连接并查询
        try {
            String sqlGauge = "select GaugeID,SID,GaugeName,EnName,X,Y from gauge where GaugeID=? ";
            PreparedStatement statGauge = conn.prepareStatement(sqlGauge);  //PreparedStatement接口
            //System.out.println("查询Gauge的preparestatement对象建立成功");
            statGauge.executeUpdate("use preciinterp");
            statGauge.setInt(1,newGaugeID);  //GaugeID=1

            //查询gauge
            ResultSet rs3=statGauge.executeQuery();  //查询
            if(rs3.next()) {

                int GaugeID1 = rs3.getInt("GaugeID");
                int SID = rs3.getInt("SID");
                String GaugeName = rs3.getString("GaugeName");
                String EnName = rs3.getString("EnName");
                double X = rs3.getDouble("X");
                double Y = rs3.getDouble("Y");
                System.out.println("\n-------Gauge表查询结果-------");
                System.out.println("编号：" + GaugeID1 + "\nSID：" + SID + "\n名称：" + GaugeName + "\n坐标：（" + X + "," + Y + ")");
                System.out.println("-----------------------------------");
            }
            else{
                System.out.println("\n-------Gauge表查询结果-------");
                System.out.println("未查找到对应数据。");
                System.out.println("-----------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Double> getPrecip(int newGaugeID, String newTime1, String newTime2) {
        //连接并查询
        try {
            String sqlPrecip = "select PrecipitationID,GaugeID,Time,Rainfall from precipitation where (GaugeID=?) and (Time between ? and ?)";
            PreparedStatement statPrecip = conn.prepareStatement(sqlPrecip);  //PreparedStatement接口
            //System.out.println("查询Precip的preparestatement对象建立成功");
            statPrecip.executeUpdate("use preciinterp");
            statPrecip.setInt(1,newGaugeID);  //GaugeID
            statPrecip.setString(2, newTime1);
            statPrecip.setString(3, newTime2);
    
            //查询Precip
            ResultSet rs2=statPrecip.executeQuery();  //查询
            rs2.next();
            ArrayList<Double> result = new ArrayList<Double>();
            do{
                double Rainfall=rs2.getDouble("Rainfall");
                result.add(Rainfall);
            }while(rs2.next());
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 读取CSV文件为二维数组
    private static List<List<Double>> readCSVFile (String filePath){
        List<List<Double>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<Double> rowData = new ArrayList<>();
                for (String value : values) {
                    rowData.add(Double.parseDouble(value));
                }
                data.add(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

    // 读取CSV中特定行列
    private static double readCSVFile (String filePath,int i,int j){
        List<List<Double>> data = readCSVFile (filePath);
        if(data!=null){
            return data.get(i).get(j);
        }
        else return 0;
    }

    //---------------------------------统计---------------------------------
    // 统计一个降水插值文件
    public void StatisticOneGrid(int newGridID) {
        //连接并查询
        try {
            String sql1 = "SELECT Data FROM Grid WHERE GridID = ?";
            PreparedStatement sta_rainfall = conn.prepareStatement(sql1);
            sta_rainfall.executeUpdate("use preciinterp");
            sta_rainfall.setInt(1, newGridID);
            ResultSet resultSet = sta_rainfall.executeQuery();
            if (resultSet.next()) {
                String data = resultSet.getString("Data");
                List<List<Double>> Data_list = readCSVFile(data);
                if (data == null) {
                    System.out.println("无法读取数据文件或数据文件为空。");
                    return;
                }
                // 计算平均值
                double mean = calculateMean(Data_list);
                // 计算最大值
                double max = calculateMax(Data_list);
                // 最小值
                double min = calculateMin(Data_list);
                // 计算均方差
                double standardDeviation = calculateVariance(Data_list);
                // 中位数
                double median = calculateMedian(Data_list);
                System.out.println("\n-------Grid表统计结果-------");
                System.out.println("对GridID= "+newGridID+" "+"插值文件统计如下：");
                System.out.println("平均值: " + mean);
                System.out.println("最大值: " + max);
                System.out.println("最小值: " + min);
                System.out.println("均方差: " + standardDeviation);
                System.out.println("中位数: " + median);
                System.out.println("-----------------------------------");
            } else {
                System.out.println("\n-------Grid表统计结果-------");
                System.out.println("未查找到对应数据。");
                System.out.println("-----------------------------------");
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    // 对某个点降水值进行统计
    public void StatisticOneGridPoint(int ROW,int COL,String method) {
        try {
            //String sql2 = "SELECT Data FROM Grid WHERE Time>20 AND Method = 'method'"  ;
            // 对一个方法所得所有时间降水值进行统计
            String sql = "SELECT Data FROM Grid WHERE Method = method" ;
            // 对特定时间所有方法得到的降水量统计
            //String sql = "SELECT Data FROM Grid WHERE Time=10"  ;
            PreparedStatement stas = conn.prepareStatement(sql);
            stas.executeUpdate("use preciinterp");
            ResultSet resultSet1 = stas.executeQuery(sql);
            double sum=0.0,max=-9999.0,min=9999.0;
            int count=0;
            double sumSquared=0.0;
            // 统计
            while (resultSet1.next()) {
                String path = resultSet1.getString("Data");
                double temp = readCSVFile(path, ROW, COL);
                sum += temp;
                sumSquared += temp * temp;
                if (temp > max) {
                    max = temp;
                }
                if (temp < min) {
                    min = temp;
                }
                count++;
            }
            if (count > 0) {
                double variance = (sumSquared / count) - (sum / count) * (sum / count);
                double standardDeviation = Math.sqrt(variance);
                System.out.println();
                System.out.println("行：" +ROW +" "+"列："+COL+" "+"统计结果如下：");
                System.out.println("平均值: " + (sum / count));
                System.out.println("最大值: " + max);
                System.out.println("最小值: " + min);
                System.out.println("均方差: " + standardDeviation);
            } else {
                System.out.println("未找到匹配的数据。");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    //----------------------------------------------------
    private static double calculateMean(List<List<Double>> data) {
        double sum = 0;
        int count = 0;
        for (List<Double> row : data) {
            for (Double value : row) {
                sum += value;
                count++;
            }
        }
        return sum / count;
    }
    // 计算最大值
    private static double calculateMax(List<List<Double>> data) {
        double max = Double.MIN_VALUE;
        for (List<Double> row : data) {
            for (Double value : row) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }
    private static double calculateMin(List<List<Double>> data) {
        double min = Double.MAX_VALUE;
        for (List<Double> row : data) {
            for (Double value : row) {
                if (value < min) {
                    min = value;
                }
            }
        }
        return min;
    }
    // 计算均方差
    private static double calculateVariance(List<List<Double>> data) {
        double sum = 0;
        double sumSquared = 0;
        int count = 0;
        for (List<Double> row : data) {
            for (Double value : row) {
                sum += value;
                sumSquared += value * value;
                count++;
            }
        }
        double mean = sum / count;
        double variance = (sumSquared / count) - (mean * mean);
        return Math.sqrt(variance);
    }
    private static double calculateMedian(List<List<Double>> data) {
        List<Double> values = new ArrayList<>();
        for (List<Double> row : data) {
            values.addAll(row);
        }
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 1) {
            // 奇数个值，中位数就是中间那个值
            return values.get(size / 2);
        } else {
            // 偶数个值，中位数是中间两个值的平均值
            double middle1 = values.get(size / 2 - 1);
            double middle2 = values.get(size / 2);
            return (middle1 + middle2) / 2.0;
        }
    }

}
