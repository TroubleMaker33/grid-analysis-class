package supportFunction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class writeASC {
    public String path;
    public int nrows;
    public int ncolumns;
    public double xllcorner;
    public double yllcorner;
    public double cellsize;
    public int NODATA_VALUE;
    public ArrayList<ArrayList<Double>> matrix_double;
    public ArrayList<ArrayList<Integer>> matrix_integer;

    public writeASC(String p, int r, int c, double x, double y, double cs, int NV,
                    ArrayList<ArrayList<Double>> m, double type){
        path=p;
        nrows=r;
        ncolumns=c;
        xllcorner=x;
        yllcorner=y;
        cellsize=cs;
        NODATA_VALUE=NV;
        matrix_double=m;

        File outputFile=new File(path);
        FileOutputStream out;
        try{
            out=new FileOutputStream(outputFile);
            out.write("ncols\t".getBytes());
            out.write((Integer.toString(ncolumns)+"\n").getBytes());
            out.write("nrows\t".getBytes());
            out.write((Integer.toString(nrows)+"\n").getBytes());
            out.write("xllcorner\t".getBytes());
            out.write((Double.toString(xllcorner)+"\n").getBytes());
            out.write("yllcorner\t".getBytes());
            out.write((Double.toString(yllcorner)+"\n").getBytes());
            out.write("cellsize\t".getBytes());
            out.write((Double.toString(cellsize)+"\n").getBytes());
            out.write("NODATA_VALUE\t".getBytes());
            out.write((Integer.toString(NODATA_VALUE)+"\n").getBytes());
            for(int i=0;i<nrows;i++){
                for(int j=0;j<ncolumns;j++){
                    out.write((Double.toString(matrix_double.get(i).get(j))+" ").getBytes());
                }
            }
            System.out.println("write "+path+" success!");
        }catch(IOException IOE){
            System.out.println("write "+path+" fail!");
        }
    }

    public writeASC(String p, int r, int c, double x, double y, double cs, int NV,
                    ArrayList<ArrayList<Integer>> m, int type){
        path=p;
        nrows=r;
        ncolumns=c;
        xllcorner=x;
        yllcorner=y;
        cellsize=cs;
        NODATA_VALUE=NV;
        matrix_integer=m;

        File outputFile=new File(path);
        FileOutputStream out;
        try{
            out=new FileOutputStream(outputFile);
            out.write("ncols\t".getBytes());
            out.write((Integer.toString(ncolumns)+"\n").getBytes());
            out.write("nrows\t".getBytes());
            out.write((Integer.toString(nrows)+"\n").getBytes());
            out.write("xllcorner\t".getBytes());
            out.write((Double.toString(xllcorner)+"\n").getBytes());
            out.write("yllcorner\t".getBytes());
            out.write((Double.toString(yllcorner)+"\n").getBytes());
            out.write("cellsize\t".getBytes());
            out.write((Double.toString(cellsize)+"\n").getBytes());
            out.write("NODATA_VALUE\t".getBytes());
            out.write((Integer.toString(NODATA_VALUE)+"\n").getBytes());
            for(int i=0;i<nrows;i++){
                for(int j=0;j<ncolumns;j++){
                    out.write((Double.toString(matrix_integer.get(i).get(j))+" ").getBytes());
                }
            }
            System.out.println("write "+path+" success!");
        }catch(IOException IOE){
            System.out.println("write "+path+" fail!");
        }
    }
}
