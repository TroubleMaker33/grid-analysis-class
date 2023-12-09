import supportFunction.ARIMA;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.ArrayList;

public class test1 {

	private final Logger logger = Logger.getLogger(test1.class.getName());

	public static void main(String[] args) {
		new test1().run();
	}

	public void run() {
		
		//定义接受时间序的降雨量文件变量名
		Scanner rainfallFile = null;

		//读取文件
		try {
			//读取文件中的降雨量信息
			ArrayList<Double> rainfallDataList = new ArrayList<Double>();
			rainfallFile = new Scanner(new File("src/rainfall(1).txt"));

			while (rainfallFile.hasNext()) {
				//转成double
				rainfallDataList.add(rainfallFile.nextDouble());
			}

			//存进数组
			double[] rainfallDataList1 = new double[rainfallDataList.size() - 1];
			for (int i = 0; i < rainfallDataList1.length; i++) {
				rainfallDataList1[i] = rainfallDataList.get(i);
			}

			ARIMA arima = new ARIMA(rainfallDataList1);

			int[] modelPara = arima.getARIMAmodel();
			System.out.println("Best parameter is [p,q]=" + Arrays.toString(modelPara));

			int[] predictions = arima.predictValue(modelPara[0], modelPara[1]);
			int[] aftDealtPredictions = new int[predictions.length];


			for (int i = 0; i < aftDealtPredictions.length; i++) {
				aftDealtPredictions[i] = arima.aftDeal(predictions[i]);
			}

			System.out.println("Predict values after dealing: " + Arrays.toString(aftDealtPredictions));

		}
		catch (FileNotFoundException e) {
			logger.severe("An error occurred: " + e.getMessage());
		}
		finally {
			if (rainfallFile != null) {
				rainfallFile.close();
			}
		}
	}
}


