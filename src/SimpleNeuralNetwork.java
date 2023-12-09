import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SimpleNeuralNetwork {

    private double[] inputLayerWeights;
    private double[] hiddenLayerWeights;

    public SimpleNeuralNetwork(int inputSize, int hiddenSize) {
        inputLayerWeights = new double[inputSize];
        hiddenLayerWeights = new double[hiddenSize];
        // 初始化权重，可以根据需要进行调整
        for (int i = 0; i < inputSize; i++) {
            inputLayerWeights[i] = 1.0 / (i + 1); // 输入层权重递增
        }
        for (int i = 0; i < hiddenSize; i++) {
            hiddenLayerWeights[i] = 1.0 / (i + 1); // 隐藏层权重递增
        }
    }

    public double predict(double[] input) {
        if (input.length != inputLayerWeights.length) {
            throw new IllegalArgumentException("输入维度与输入层权重维度不匹配");
        }

        if (hiddenLayerWeights.length != 1) {
            throw new IllegalArgumentException("隐藏层权重维度必须为1");
        }

        double inputSum = 0;
        for (int i = 0; i < input.length; i++) {
            inputSum += input[i] * inputLayerWeights[i];
        }

        // 隐藏层
        double hiddenOutput = inputSum * hiddenLayerWeights[0];

        return hiddenOutput;
    }

    public static void main(String[] args) {
        try {
            // 从文件中读取输入序列
            String fileName = "src/rainfall1.txt";
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            if ((line = reader.readLine()) != null) {
                // 将中文逗号替换为英文逗号
                line = line.replace("，", ",");
                String[] values = line.split(",");
                double[] inputSequence = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    inputSequence[i] = Double.parseDouble(values[i]);
                }

                int hiddenLayerSize = 1; // 隐藏层中神经元的数量
                SimpleNeuralNetwork neuralNetwork = new SimpleNeuralNetwork(inputSequence.length, hiddenLayerSize);

                // 预测输出序列的前十个值
                double[] predictedSequence = new double[5];
                for (int i = 0; i < 5; i++) {
                    double[] input = new double[inputSequence.length];
                    for (int j = 0; j <= i; j++) {
                        input[j] = inputSequence[j];
                    }
                    predictedSequence[i] = neuralNetwork.predict(input);
                }

                // 打印前十个预测结果
                System.out.println("前5个预测结果：");
                for (int i = 0; i < 5; i++) {
                    System.out.println(predictedSequence[i]);
                }
            } else {
                System.err.println("无法读取输入数据。");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
