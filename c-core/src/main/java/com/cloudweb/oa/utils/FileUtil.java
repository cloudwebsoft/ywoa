package com.cloudweb.oa.utils;

import cn.js.fan.util.NumberUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    public static List<String> read(InputStream inputStream) {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            LogUtil.getLog(FileUtil.class).error(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("InputStream关闭异常", e);
            }
        }
        return list;
    }

    public static List<String> read(String fileName) {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            File f = new File(fileName);
            if (f.isFile() && f.exists()) {
                fis = new FileInputStream(f);
                reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!"".equals(line)) {
                        list.add(line);
                    }
                }
            }
        } catch (Exception e) {
            log.error("readFile", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("InputStream关闭异常", e);
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                log.error("FileInputStream关闭异常", e);
            }
        }
        return list;
    }

    /**
     * 判断是否为office文件
     * @param fileName
     * @return
     */
    public static boolean isOfficeFile(String fileName) {
        String ext = cn.js.fan.util.file.FileUtil.getFileExt(fileName).toLowerCase();
        return "doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext) || "wps".equals(ext);
    }

    public static boolean isPdfFile(String fileName) {
        return "pdf".equals(cn.js.fan.util.file.FileUtil.getFileExt(fileName).toLowerCase());
    }
}
