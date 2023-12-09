package supportFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class readASC extends Thread {

    // 读取 DEM 类的属性
    public int ncols;
    public int nrows;
    public int cellsize;
    public int NODATA_VALUE;
    public double xllcorner;
    public double yllcorner;
    public ArrayList<ArrayList<Double>> InterpolationValue;
    public String z;

    public readASC(String s) {
    }

    public void run() {
        // 线程运行方法，可以执行后续操作。
        // System.out.println("readDEM线程:" + getName());
    }

    public void readASC(String path) throws Exception {
        // 读取 ASC 文件的方法，接受文件路径作为参数

        try {
            File ascFile = new File(path);
            Scanner inASC = new Scanner(ascFile);

            // 读取 DEM 文件的基本信息
            z = inASC.next();
            ncols = inASC.nextInt();

            z = inASC.next();
            nrows = inASC.nextInt();

            z = inASC.next();
            xllcorner = inASC.nextDouble();

            z = inASC.next();
            yllcorner = inASC.nextDouble();

            z = inASC.next();
            cellsize = (int) inASC.nextDouble();

            z = inASC.next();
            NODATA_VALUE = inASC.nextInt();

            InterpolationValue = new ArrayList<ArrayList<Double>>();
            for (int i = 0; i < nrows; i++) {
                ArrayList<Double> tempInterpolationValue = new ArrayList<Double>();
                for (int j = 0; j < ncols; j++) {
                    tempInterpolationValue.add(inASC.nextDouble());
                }
                InterpolationValue.add(tempInterpolationValue);
            }

            inASC.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

    public double[][] getInterpolationValues() {
        int numRows = InterpolationValue.size();
        if (numRows == 0) {
            return new double[0][0]; // 返回空数组，如果没有数据
        }

        int numCols = InterpolationValue.get(0).size();
        double[][] result = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            ArrayList<Double> row = InterpolationValue.get(i);
            for (int j = 0; j < numCols; j++) {
                result[i][j] = row.get(j);
            }
        }

        return result;
    }



}

