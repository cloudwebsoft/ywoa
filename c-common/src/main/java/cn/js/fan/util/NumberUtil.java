package cn.js.fan.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NumberUtil {
    public NumberUtil() {
    }

    /**
     * 四舍五入
     * @param v 双精度输入值
     * @param digit 小数点后的位数
     * @return 四舍五入后的字符串
     */
    public static String round(double v, int digit) {
        //提供能够精确到小数点后位数的四舍五入，而java.math.round不能做到
        ///String temp = "#,##0.";
        //String temp = "###.";//如果小数点前面为0，则不显示0
        StringBuilder temp = new StringBuilder("##0.");
        if (digit==0) {
            temp = new StringBuilder("##0");
        }
        for (int i = 0; i < digit; i++) {
            temp.append("0");
        }
        DecimalFormat d = new DecimalFormat(temp.toString());
        return d.format(v);
    }

    /**
     * 保留小数点后位数，不四舍五入，如果值为0，则显示为0，不会出现小数点后的位数
     * @param v
     * @param digit
     * @return
     */
    public static String noRound(double v, int digit) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(digit);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);

        // DecimalFormat formater = new DecimalFormat("#0.##");
        return formater.format(v);
    }

    /**
     * 四舍五入人民币
     * @param v 双精度值
     * @return 四舍五入后的人民币
     */
    public static String roundRMB(double v) {
        String temp = "##0.00";
        DecimalFormat d = new DecimalFormat(temp);
        return d.format(v);
    }

    /**
     * 四舍五入至人民币分
     * @param v 双精度输入值
     * @return 四舍五入后的字符串
     */
    public static String roundRMB2Feng(double v) {
        //提供能够精确到小数点后位数的四舍五入，而java.math.round不能做到
        String temp = "##0.";
        DecimalFormat d = new DecimalFormat(temp);
        d.setDecimalSeparatorAlwaysShown(false); //去掉小数点
        return d.format(v * 100);
    }

    /**
     * 取得a至b之间的随机整数
     * @param a int
     * @param b int
     * @return int a至b-1
     */
    public static int random(int a, int b) {
        return (int) ((b - a) * Math.random() + a);
    }

    public static long random(long a, long b) {
        return (long) ((b - a) * Math.random() + a);
    }

    public static String tran2CN(int n) {
        Stack<Integer> s = new Stack<>();
        int division = 0; //余数
        while (n >= 10) {
            division = n % 10;
            s.push(division);
            n = n / 10;
        }
        s.push(n); //将最高位压栈

        Map<Integer, String> hp1 = new HashMap<>(); //第一个映射表
        //根据所在位的数值与中文对应
        hp1.put(0, "零");
        hp1.put(1, "一");
        hp1.put(2, "二");
        hp1.put(3, "三");
        hp1.put(4, "四");
        hp1.put(5, "五");
        hp1.put(6, "六");
        hp1.put(7, "七");
        hp1.put(8, "八");
        hp1.put(9, "九");

        Map<Integer, String> hp2 = new HashMap<>(); //第二个映射表
        hp2.put(2, "十"); //根据所在位数，与中文对应
        hp2.put(3, "百");
        hp2.put(4, "千");
        hp2.put(5, "万");
        hp2.put(6, "十万");
        hp2.put(7, "百万");
        hp2.put(8, "千万");
        hp2.put(9, "亿");

        //System.out.println(s.size());
        String out = "";
        while (!s.isEmpty()) {
            int temp = s.pop();

            if (s.size() == 0) {
                if (temp != 0) {
                    out = out + (String)hp1.get(temp);
                }
            } else {
                if (temp == 0) {
                    out = out + (String)hp1.get(temp);
                } else {
                    out = out + (String)hp1.get(temp) + (String)hp2.get(s.size() + 1);
                }
            }
        }
        return out;
    }

    /*
    // 适用于jdk1.5
         public static String tran2CN(int n) {
             Stack<Integer> s = new Stack<Integer>();
             int division = 0; //余数
             while (n >= 10) {
                 division = n % 10;
                 s.push(division);
                 n = n / 10;
             }

             s.push(n); //将最高位压栈

             HashMap<Integer, String> hp1 = new HashMap<Integer, String>(); //第一个映射表
             hp1.put(0, "零"); //根据所在位的数值与中文对应
             hp1.put(1, "一");
             hp1.put(2, "二");
             hp1.put(3, "三");
             hp1.put(4, "四");
             hp1.put(5, "五");
             hp1.put(6, "六");
             hp1.put(7, "七");
             hp1.put(8, "八");
             hp1.put(9, "九");

             HashMap<Integer, String> hp2 = new HashMap<Integer, String>(); //第二个映射表
             hp2.put(2, "十"); //根据所在位数，与中文对应
             hp2.put(3, "百");
             hp2.put(4, "千");
             hp2.put(5, "万");
             hp2.put(6, "十万");
             hp2.put(7, "百万");
             hp2.put(8, "千万");
             hp2.put(9, "亿");

             //System.out.println(s.size());
             String out = "";
             while (!s.isEmpty()) {
                 int temp = s.pop();

                 if (s.size() == 0) {
                     if (temp != 0) {
                         out = out + hp1.get(temp);
                     }
                 } else {
                     if (temp == 0) {
                         out = out + hp1.get(temp);
                     } else {
                         out = out + hp1.get(temp) + hp2.get(s.size() + 1);
                     }
                 }
             }
             return out;
         }
      */
    
    /**
     * 判断是否为数字，区别于StrUtil.isNumeric可以含有负号及小数点
     */
	public static boolean isNumeric(String str) {
		//采用正则表达式的方式来判断一个字符串是否为数字，这种方式判断面比较全
		//可以判断正负、整数小数
		//?:0或1个, *:0或多个, +:1或多个
		return str.matches("[-+]{0,1}\\d+\\.?\\d*");
	}    

}
