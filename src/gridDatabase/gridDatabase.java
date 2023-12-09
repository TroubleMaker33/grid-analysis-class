package gridDatabase;

import java.sql.*;
import java.util.ArrayList;

public class gridDatabase {
    // 数据库创建
    public static void createDatabase(String URL, String Database, String user, String password) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("mysql database load successfully!");

        Connection conn_createdatabase = DriverManager.getConnection(URL, user, password);
        System.out.println("mysql database connect successfully!");
        Statement stat_createdatabase = conn_createdatabase.createStatement();
        System.out.println("statement successfully!");
        stat_createdatabase.executeUpdate("create database if not exists " + Database + " default character set=\"utf8\"");
        stat_createdatabase.close();
        conn_createdatabase.close();
    }

    // 表创建
    public static void createTable(String URL, String Database, String user, String password, int ncolumns) throws Exception{
        Connection conn_createtable = DriverManager.getConnection(URL+Database, user, password);
        Statement stat_createtable = conn_createtable.createStatement();

        stat_createtable.executeUpdate("drop table if exists Krigingrainfall_interpolation");
        stat_createtable.executeUpdate("drop table if exists IDWrainfall_interpolation");
//        stat_createtable.executeUpdate("drop table if exists filling_dem");
//        stat_createtable.executeUpdate("drop table if exists slope");
//        stat_createtable.executeUpdate("drop table if exists aspect");
//        stat_createtable.executeUpdate("drop table if exists flow");
//        stat_createtable.executeUpdate("drop table if exists acc_flow");
//        stat_createtable.executeUpdate("drop table if exists water_zone");
//        stat_createtable.executeUpdate("drop table if exists ridge_line");

        stat_createtable.executeUpdate("create table if not exists Krigingrainfall_interpolation(kri_row int not null)");
        stat_createtable.executeUpdate("create table if not exists IDWrainfall_interpolation(iri_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists filling_dem(fdem_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists slope(slo_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists aspect(asp_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists flow(flow_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists acc_flow(accflow_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists water_zone(wz_row int not null)");
//        stat_createtable.executeUpdate("create table if not exists ridge_line(rl_row int not null)");

        // 每个表插入对应的列
        for(int i=1;i<=ncolumns;i++){
            String sql_iri="alter table IDWrainfall_interpolation ADD column"+String.valueOf(i)+" double";
            String sql_kri="alter table Krigingrainfall_interpolation ADD column"+String.valueOf(i)+" double";
//            String sql_f="alter table flow ADD column"+String.valueOf(i)+" double";
//            String sql_af="alter table acc_flow ADD column"+String.valueOf(i)+" double";
//            String sql_fdem="alter table filling_dem ADD column"+String.valueOf(i)+" double";
//            String sql_s="alter table slope ADD column"+String.valueOf(i)+" double";
//            String sql_a="alter table aspect ADD column"+String.valueOf(i)+" double";
//            String sql_w="alter table water_zone ADD column"+String.valueOf(i)+" double";
//            String sql_r="alter table ridge_line ADD column"+String.valueOf(i)+" double";

            stat_createtable.executeUpdate(sql_iri);
            stat_createtable.executeUpdate(sql_kri);
//            stat_createtable.executeUpdate(sql_f);
//            stat_createtable.executeUpdate(sql_af);
//            stat_createtable.executeUpdate(sql_fdem);
//            stat_createtable.executeUpdate(sql_s);
//            stat_createtable.executeUpdate(sql_a);
//            stat_createtable.executeUpdate(sql_w);
//            stat_createtable.executeUpdate(sql_r);

        }
        stat_createtable.close();
        conn_createtable.close();
    }

    // 数据导入（插入）
//    public static void insertData(String URL, String Database, String user, String password, int nrows, int ncolumns,
//                                  ArrayList<ArrayList<Double>> IDWrainfall,
//                                  ArrayList<ArrayList<Double>> Krigingrainfall,
//                                  ArrayList<ArrayList<Double>> filling_dem,
//                                  ArrayList<ArrayList<Double>> aspect,
//                                  ArrayList<ArrayList<Double>> slope,
//                                  ArrayList<ArrayList<Integer>> flow,
//                                  ArrayList<ArrayList<Integer>> acc_flow,
//                                  ArrayList<ArrayList<Integer>> water_zone,
//                                  ArrayList<ArrayList<Integer>> ridge_line) throws Exception{
    public static void insertData(String URL, String Database, String user, String password, int nrows, int ncolumns,
                              ArrayList<ArrayList<Double>> IDWrainfall,
                              ArrayList<ArrayList<Double>> Krigingrainfall) throws Exception{

        Connection conn_insertdata = DriverManager.getConnection(URL+Database, user, password);
        Statement stat_insertdata = conn_insertdata.createStatement();
//
        // SQL语句准备
        String sql_iri = "insert into IDWrainfall_interpolation (iri_row";
        for(int i=1;i<=ncolumns;i++) {
            sql_iri=sql_iri+",column"+String.valueOf(i);
        }
        sql_iri=sql_iri+") values (?";
        for(int i=1;i<=ncolumns;i++) {
            sql_iri=sql_iri+",?";
        }
        sql_iri=sql_iri+")";

        String sql_kri = "insert into Krigingrainfall_interpolation (kri_row";
        for(int i=1;i<=ncolumns;i++) {
            sql_kri=sql_kri+",column"+String.valueOf(i);
        }
        sql_kri=sql_kri+") values (?";
        for(int i=1;i<=ncolumns;i++) {
            sql_kri=sql_kri+",?";
        }
        sql_kri=sql_kri+")";
//
//        String sql_f = "insert into flow (flow_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_f=sql_f+",column"+String.valueOf(i);
//        }
//        sql_f=sql_f+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_f=sql_f+",?";
//        }
//        sql_f=sql_f+")";
//
//        String sql_af = "insert into acc_flow (accflow_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_af=sql_af+",column"+String.valueOf(i);
//        }
//        sql_af=sql_af+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_af=sql_af+",?";
//        }
//        sql_af=sql_af+")";
//
//
//        String sql_fdem = "insert into filling_dem (fdem_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_fdem=sql_fdem+",column"+String.valueOf(i);
//        }
//        sql_fdem=sql_fdem+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_fdem=sql_fdem+",?";
//        }
//        sql_fdem=sql_fdem+")";
//
//        String sql_s = "insert into slope (slo_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_s=sql_s+",column"+String.valueOf(i);
//        }
//        sql_s=sql_s+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_s=sql_s+",?";
//        }
//        sql_s=sql_s+")";
//
//        String sql_a = "insert into aspect (asp_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_a=sql_a+",column"+String.valueOf(i);
//        }
//        sql_a=sql_a+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_a=sql_a+",?";
//        }
//        sql_a=sql_a+")";
//
//        String sql_w = "insert into water_zone (wz_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_w=sql_w+",column"+String.valueOf(i);
//        }
//        sql_w=sql_w+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_w=sql_w+",?";
//        }
//        sql_w=sql_w+")";
//
//        String sql_r = "insert into ridge_line (rl_row";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_r=sql_r+",column"+String.valueOf(i);
//        }
//        sql_r=sql_r+") values (?";
//        for(int i=1;i<=ncolumns;i++) {
//            sql_r=sql_r+",?";
//        }
//        sql_r=sql_r+")";
//
        PreparedStatement prestat_iri= conn_insertdata.prepareStatement(sql_iri);
        PreparedStatement prestat_kri= conn_insertdata.prepareStatement(sql_kri);
//        PreparedStatement prestat_f= conn_insertdata.prepareStatement(sql_f);
//        PreparedStatement prestat_af= conn_insertdata.prepareStatement(sql_af);
//        PreparedStatement prestat_fdem= conn_insertdata.prepareStatement(sql_fdem);
//        PreparedStatement prestat_s= conn_insertdata.prepareStatement(sql_s);
//        PreparedStatement prestat_a= conn_insertdata.prepareStatement(sql_a);
//        PreparedStatement prestat_w= conn_insertdata.prepareStatement(sql_w);
//        PreparedStatement prestat_r= conn_insertdata.prepareStatement(sql_r);
//        // 上述都是在为插入数据库准备字段以及插入程序
//
        // 将IDW降雨插值计算结果插入在MySQL中准备好的表
        for(int i=1;i<=nrows;i++){
            prestat_iri.setInt(1,i);
            for(int j=1;j<=ncolumns;j++){
                prestat_iri.setDouble(j+1,IDWrainfall.get(i-1).get(j-1));
            }
            prestat_iri.executeUpdate();
        }
        System.out.println("IDWrainfall interpolation adding to database successfully!");
        System.out.println();
//
//        // 将流向计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_f.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_f.setDouble(j+1,flow.get(i-1).get(j-1));
//            }
//            prestat_f.executeUpdate();
//        }
//        System.out.println("flow adding to database successfully!");
//        System.out.println();
//
//        // 将累积流计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_af.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_af.setDouble(j+1,acc_flow.get(i-1).get(j-1));
//            }
//            prestat_af.executeUpdate();
//        }
//        System.out.println("acc_flow adding to database successfully!");
//
        // 将克里金降雨插值计算结果插入在MySQL中准备好的表
        for(int i=1;i<=nrows;i++){
            prestat_kri.setInt(1,i);
            for(int j=1;j<=ncolumns;j++){
                prestat_kri.setDouble(j+1,Krigingrainfall.get(i-1).get(j-1));
            }
            prestat_kri.executeUpdate();
        }
        System.out.println("Krigingrainfall interpolation adding to database successfully!");
        System.out.println();
//
//        // 将填洼计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_fdem.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_fdem.setDouble(j+1,filling_dem.get(i-1).get(j-1));
//            }
//            prestat_fdem.executeUpdate();
//        }
//        System.out.println("filling_dem adding to database successfully!");
//        System.out.println();
//
//        // 将坡度计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_s.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_s.setDouble(j+1,slope.get(i-1).get(j-1));
//            }
//            prestat_s.executeUpdate();
//        }
//        System.out.println("slope adding to database successfully!");
//        System.out.println();
//
//        // 将坡向计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_a.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_a.setDouble(j+1,aspect.get(i-1).get(j-1));
//            }
//            prestat_a.executeUpdate();
//        }
//        System.out.println("aspect adding to database successfully!");
//        System.out.println();
//
//        // 将河网计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_w.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_w.setDouble(j+1,water_zone.get(i-1).get(j-1));
//            }
//            prestat_w.executeUpdate();
//        }
//        System.out.println("water_zone adding to database successfully!");
//        System.out.println();
//
//        // 将山脊线计算结果插入在MySQL中准备好的表
//        for(int i=1;i<=nrows;i++){
//            prestat_r.setInt(1,i);
//            for(int j=1;j<=ncolumns;j++){
//                prestat_r.setDouble(j+1,ridge_line.get(i-1).get(j-1));
//            }
//            prestat_r.executeUpdate();
//        }
//        System.out.println("ridge_line adding to database successfully!");
//        System.out.println();
//
        stat_insertdata.close();
        conn_insertdata.close();
    }
}
