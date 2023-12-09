import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;


class Mean_Kendall{

    //显著性检验
    static private double nor=1.96;

    //斜率 衡量趋势大小
    private double SlopeEstimate;

    //数据数量
    public int Num=55;

    //读入数据
   /* double[] StationData={0,4,13,40,51,18,8,3,10,13,22, 5,
            2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,
            0,30,13,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1};
    double[] StationData={1.0, 0.0, 4.0, 17.0, 48.0, 21.0, 0.0, 3.0, 15.0, 9.0, 8.0, 7.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.0, 10.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};*/
//    double[] StationData={0.0, 0.0, 2.0, 26.0, 84.0, 35.0, 8.0, 2.0, 10.0, 12.0, 9.0, 11.0, 8.0, 2.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 24.0, 18.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
    double[] StationData;


    //记录数据
    List<Double> RecordValue = new ArrayList<Double>(Num);


    //改成读到站点的降雨数据
    void ReadData()
    {
        for (int i = 0; i < StationData.length; i++) {
            RecordValue.add(StationData[i]);
        }

        System.out.print("研究时段：2005-06-23 03：00 至 2005-06-25 09：00");
        System.out.println("\t共计"+Num+"小时");
        System.out.println("雨量站降雨量变化检测结果：");
    }


    //MK趋势检验 分为无明显趋势（稳定）、趋势上升、趋势下降
    void MKTrendDetection() {


        int sgn = 0;    //检验函数
        double Z = 0;   //Z统计量

        //差值函数
        int S = 0;
        //计算统计量S
        for (int i = 1; i < Num - 1; i++) {
            for (int j = i + 1; j < Num; j++) {
                double MinusData = RecordValue.get(j) - RecordValue.get(i);
                if (MinusData < 0) {
                    sgn = -1;
                } else if (MinusData == 0) {
                    sgn = 0;
                } else {
                    sgn = 1;
                }
                S = S + sgn;
            }
        }


        // 计算方差var
        double var=0;
        //数据中是否有重复数值
        Map<Double, Integer> countMap = new HashMap<>();// 创建一个HashMap，用于存储元素及其出现次数
        for (int i = 0; i < Num; i++) {
            countMap.put(RecordValue.get(i), countMap.getOrDefault(RecordValue.get(i), 0) + 1);
        }
        if (countMap.size() == Num)//没有重复值
             {
                 var=Num*(Num-1)*(2*Num+5)/18;
             }
         else  //有重复值
            {
                //System.out.print( countMap.size()+"   ");
                for (Map.Entry<Double,Integer>entry:countMap.entrySet())
                {
                    int FQ=entry.getValue();
                    var=var+FQ*(FQ-1)*(2*FQ+5);
                }
                var=(Num*(Num-1)*(2*Num+5)-var)/18;
                System.out.println("方差Var："+var);
            }


        //双边检验 H0:没有单调趋势
        if(S>0){
            Z=(S-1)/Math.sqrt(var);
        }
        else if(S==0){
            Z=0;
        }
        else{
            Z=(S+1)/Math.sqrt(var);
        }

        System.out.println("标准正态分布统计量Z："+Z);

        if(Math.abs(Z)>nor)
        {
            System.out.print("趋势分析：通过95%显著性检验，");
            /*计算倾斜度β 衡量趋势
            List<Double> Slope = new ArrayList<Double>(Num*(Num-1)/2);
            for (int i = 1; i < Num - 1; i++) {
                for (int j = i + 1; j < Num; j++) {
                    double SlopeData = (RecordValue.get(j) - RecordValue.get(i)) / (j - i);
                    Slope.add(SlopeData);
                }
            }
            //求中位数
            Collections.sort(Slope);
            int size=Slope.size();
            if(size%2==0){
                SlopeEstimate=(Slope.get(size / 2 - 1) + Slope.get(size / 2)) / 2.0;
            }
            else{
                SlopeEstimate=Slope.get(size/2);
            }
*/

            //判断趋势
            if(Z>0)
            {
                System.out.println("降雨量在该时段内有上升趋势");
            }
            else if(Z<0){
                System.out.println("降雨量在该时段内有下降趋势");
            }
            else {
                System.out.println("降雨量在该时段内无明显趋势");
            }

        }
        else {
            System.out.println("趋势分析：未通过95%显著性检验,降雨量在该时段内没有明显变化趋势");
        }

    }


    //MK突变检验
    void MKMutationPDetection()
    {

        //顺序时间序列
        List<Integer> skOrder = new ArrayList<Integer>(Num);
        List<Double>UFk=new ArrayList<>(Num);
        skOrder.add(0);
        UFk.add(0.0);
        int r=0;
        for (int i = 1; i < Num ; i++) {
            for (int j = 0; j < i; j++) {
                if(RecordValue.get(i) > RecordValue.get(j))
                {
                    r=r+1;
                }
            }
            skOrder.add(r);
            double Var_sk=i*(i+1)*(2*(i+1)+5)/72;
            double E_sk=(i+1)*(i+2)/4;
            UFk.add((r-E_sk)/Math.sqrt(Var_sk));
        }


        //逆序时间序列
        Collections.reverse(RecordValue);
        List<Integer> skInverse = new ArrayList<Integer>(Num);
        List<Double>UBk=new ArrayList<>(Num);
        skInverse.add(0);
        UBk.add(0.0);
        int s=0;
        for (int k = 1; k < Num ; k++) {
            for (int j = 0; j < k; j++) {
                if(RecordValue.get(k) > RecordValue.get(j))
                {
                    s=s+1;
                }
                else {
                    s=s+0;
                }
            }
            skInverse.add(r);
            double Var_sk=k*(k+1)*(2*(k+1)+5)/72;
            double E_sk=(k+2)*(k+1)/4;
            UBk.add((r-E_sk)/Math.sqrt(Var_sk));
        }

        Collections.reverse(UBk);


        //找突变点
        int index=0;
        for(int i=1;i<Num;i++)
        {
             double difference=(UBk.get(i-1)-UFk.get(i-1))* (UBk.get(i)-UFk.get(i));

            if (difference<0 && Math.abs(UBk.get(i))<2.58)//0.01显著水平
             {
                 System.out.print(i-1+"个点是突变点,");
                 index=1;

                 // 2005-06-23-3：00 基础上加i个小时
                 int time=i+3;
                 if(i+3<24)
                 {
                     System.out.print("即2005-06-"+time+"时降水量出现突变");
                 }
                 else
                 {
                     int hour=(i+3)%24;
                     int day=23+time/24;
                     System.out.print("即2005-06-"+day+"-"+hour+"时降水量出现突变");
                 }
             }

        }

        if(index==0) {
            System.out.println("突变点检测：未检测到突变点");
        }

    }


    //test
    public static void main(String[] args) {
        Mean_Kendall test=new Mean_Kendall();
        test.ReadData();
        test.MKTrendDetection();
        test.MKMutationPDetection();
    }
}

