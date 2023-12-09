package supportFunction;

import java.io.*;
import java.util.ArrayList;
import java.sql.*;

public class readRainFallStation extends Thread {
    //读取降雨站以及雨量数据类
    public int stationsNumber;
    public ArrayList<Integer> stationID;
    public ArrayList<String> NAME;
    public ArrayList<String> NAME_station;
    public ArrayList<String> SID;
    public ArrayList<Double> stationsX;
    public ArrayList<Double> stationsY;
    public ArrayList<String> enname;
    public ArrayList<Double> stationsRainfall;
    public ArrayList<ArrayList<Double>> stationsTimeSeriesRainfall;


    public void run() {
        System.out.println("readRainFallStation线程:" + getName());
    }

    public readRainFallStation(String fileName_StationProperty, String fileName_rain) throws Exception {
        // 主要内容是逐行读取
        // String fileName_StationProperty ="src/StationProperty.txt";
        // String fileName_rain ="src/rain.txt";
        FileReader fileReader_StationProperty = new FileReader(fileName_StationProperty);
        BufferedReader bufferedReader_StationProperty = new BufferedReader(fileReader_StationProperty);
        String line_StationProperty = bufferedReader_StationProperty.readLine();

        ArrayList<ArrayList<String>> file = new ArrayList<ArrayList<String>>();
        int rows_StationProperty = 0;
        stationID = new ArrayList<Integer>();
        NAME = new ArrayList<String>();
        SID = new ArrayList<String>();
        stationsX = new ArrayList<Double>();
        stationsY = new ArrayList<Double>();
        enname = new ArrayList<String>();

        // 数据库建表
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/grid", "root", "chovy/11/ZYC");
        Statement stat=conn.createStatement();
        stat.executeUpdate("drop table if exists stationProperty");
        stat.executeUpdate("create table if not exists stationProperty(ID int(3) not null," +
                "NAME varchar(10),SID varchar(10),X double,Y double,enname varchar(20))");

        stat.executeUpdate("drop table if exists rain");
        stat.executeUpdate("create table if not exists rain(NAME varchar(10) not null,rainfall double)");
        stat.close();

        String sql_station="insert into stationProperty (ID,NAME,SID,X,Y,enname) values(?,?,?,?,?,?)";
        String sql_rain="insert into rain (NAME,rainfall) values(?,?)";

        PreparedStatement prestat_station=conn.prepareStatement(sql_station);
        PreparedStatement prestat_rain=conn.prepareStatement(sql_rain);

        while (line_StationProperty != null) {
            ArrayList<String> tempfile = new ArrayList<String>();
            String[] s = line_StationProperty.split("\\t");
            for (int i = 0; i < s.length; i++) {
                tempfile.add(s[i]);
            }
            file.add(tempfile);
            line_StationProperty = bufferedReader_StationProperty.readLine();
            if (rows_StationProperty == 0) {
                stationsNumber = Integer.parseInt(file.get(rows_StationProperty).get(0));
            } else if (rows_StationProperty > 1) {
                stationID.add(Integer.parseInt(file.get(rows_StationProperty).get(0)));
                NAME.add(file.get(rows_StationProperty).get(1));
                SID.add(file.get(rows_StationProperty).get(2));
                stationsX.add(Double.parseDouble(file.get(rows_StationProperty).get(3)));
                stationsY.add(Double.parseDouble(file.get(rows_StationProperty).get(4)));
                enname.add(file.get(rows_StationProperty).get(5));

                prestat_station.setInt(1,Integer.parseInt(file.get(rows_StationProperty).get(0)));
                prestat_station.setString(2,file.get(rows_StationProperty).get(1));
                prestat_station.setString(3,file.get(rows_StationProperty).get(2));
                prestat_station.setDouble(4,Double.parseDouble(file.get(rows_StationProperty).get(3)));
                prestat_station.setDouble(5,Double.parseDouble(file.get(rows_StationProperty).get(4)));
                prestat_station.setString(6,file.get(rows_StationProperty).get(5));
                prestat_station.executeUpdate();
            }
            ++rows_StationProperty;
        }

        bufferedReader_StationProperty.close();
        fileReader_StationProperty.close();

        FileReader fileReader_rain = new FileReader(fileName_rain);
        BufferedReader bufferedReader_rain = new BufferedReader(fileReader_rain);
        String line_rain = bufferedReader_rain.readLine();

        ArrayList<ArrayList<String>> file_rain = new ArrayList<ArrayList<String>>();
        int rows_rain = 0;
        NAME_station = new ArrayList<String>();

        // 下面的读取那么复杂是因为想要处理降雨站不按顺序或没有该降雨站的雨量的异常情况
        while (line_rain != null) {
            ArrayList<String> tempfile = new ArrayList<String>();
            String[] s = line_rain.split("\\t");
            for (int i = 0; i < s.length; i++) {
                tempfile.add(s[i]);
            }
            file_rain.add(tempfile);
            line_rain = bufferedReader_rain.readLine();
            if (rows_rain == 1) {
                for (int i = 2; i < s.length; i++) {
                    NAME_station.add(file_rain.get(rows_rain).get(i));
                }
            }
            ++rows_rain;
        }

        ArrayList<Integer> stationsIndex = new ArrayList<Integer>(stationsNumber);
        for (int i = 0; i < stationsNumber; i++) {
            for (int j = 0; j < NAME_station.size(); j++) {
                if (NAME.get(i).equals(NAME_station.get(j))) {
                    stationsIndex.add(j + 2);
                    break;
                }
                if (j == NAME_station.size() - 1) {
                    stationsIndex.add(-1);
                }
            }
        }

        stationsRainfall = new ArrayList<Double>();
        stationsTimeSeriesRainfall = new ArrayList<ArrayList<Double>>(); // 初始化新的变量
        for (int i = 0; i < stationsNumber; i++) {
            stationsRainfall.add((double) 0);
            stationsTimeSeriesRainfall.add(new ArrayList<Double>()); // 为每个站点初始化一个空的降雨量列表
        }

        for (int i = 0; i < stationsNumber; i++) {
            if (stationsIndex.get(i) != -1) {
                for (int j = 2; j < rows_rain; j++) {
                    stationsRainfall.set(i, stationsRainfall.get(i) + Double.parseDouble(file_rain.get(j).get(stationsIndex.get(i))));
                    stationsTimeSeriesRainfall.get(i).add(Double.parseDouble(file_rain.get(j).get(stationsIndex.get(i)))); // 填充时序降雨量数据
                }
            }else {
                for (int j = 2; j < rows_rain; j++) {
                    stationsTimeSeriesRainfall.get(i).add(0.0); // 如果没有数据，添加一个默认值，例如0.0
                }
            }
            prestat_rain.setString(1,NAME.get(i));
            prestat_rain.setDouble(2,stationsRainfall.get(i));
            prestat_rain.executeUpdate();
        }
        bufferedReader_StationProperty.close();
        fileReader_StationProperty.close();
    }
}
