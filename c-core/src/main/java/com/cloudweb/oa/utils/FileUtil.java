package com.cloudweb.oa.utils;

import cn.js.fan.util.NumberUtil;

public class FileUtil {

    /**
     * @Description: 将字节数转为适合的单位对应的大小
     * @param src
     * @return
     */
    public static String getSizeDesc(long src) {
        boolean isNegative = src < 0;
        long temp = Math.abs(src);
        double dst = 0.0f;
        int i = 0;
        char unit = ' ';

        for (i = 0; temp >= 1024; i++) {
            dst = (dst == 0.0f ? temp : dst) / 1024.0;
            temp = temp / 1024;
        }
        switch (i) {
            case 0:
                dst = (double) src;
                unit = ' ';
                break;
            case 1:
                unit = 'K';
                break;
            case 2:
                unit = 'M';
                break;
            case 3:
                unit = 'G';
                break;
            case 4:
                unit = 'T';
                break;
            default:
                unit = 'B';
                break;
        }

        return NumberUtil.round((isNegative ? -1 : 1) * dst + 0.05, 1) + " "
                + unit + 'B';
    }

}
