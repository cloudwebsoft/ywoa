package com.redmoon.oa.fileark;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import cn.js.fan.util.net.TrackBack;
import org.apache.log4j.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GatherUtil {
    Logger logger = Logger.getLogger(GatherUtil.class.getName());

    public GatherUtil() {
    }

    public String gather(String link) {
        String str = "";
        try {
            URL url = new URL(link);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            // logger.error("url=" + url + " encode:"+huc.getContentEncoding()+" type:"+huc.getContentType());
            String type = huc.getContentType();
            String charset = "utf-8";
            int index = type.indexOf("charset=");
            if (index!=-1) {
                index = index + 8;
                charset = type.substring(index).trim();
            }
            else
                charset = "gb2312";

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(huc.getInputStream(), charset));
            String line = reader.readLine();
            while (line != null) {
                str += line;
                line = reader.readLine();
            }
            reader.close();
            huc.disconnect();
        }
        catch (Exception e) {
            logger.error("gather: " + e.getMessage());
        }
        return str;
    }

    public String parseWeather(String source, String content) {
        if (source.equals("163"))
            return parseWeather163(content);
        if (source.equals("china.com.cn"))
            return parseWeatherChina(content);
        return "";
    }

    public String parseWeather163(String content) {
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        String str = "";

        // patternStr = "(\\[URL\\])(.*)(\\[\\/URL\\])";
        patternStr = "<div class=\\\"wetMain\\\">(.*)</div>";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        matcher = pattern.matcher(content);
        // 提取今日的天气
        if (matcher.find()) {
            str = matcher.group(1);
            // logger.info("str=" + str);
            str = str.replaceAll("\\.\\./images", "http://mimg.163.com/tianqi/images");
            // 替换onClick="detail();"
            str = str.replaceAll("detail\\(\\);", "");
            str = str.replaceAll("tomorrow\\(\\);", "");
        }

        return str;
    }

    public String parseWeatherChina(String content) {
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        String str = "";

        // patternStr = "(\\[URL\\])(.*)(\\[\\/URL\\])";
        patternStr = "<table width=\\\"308\\\".*</table>";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        matcher = pattern.matcher(content);
        // 提取今日的天气
        if (matcher.find()) {
            str = matcher.group(0);
            logger.info("str=" + str);
            str = str.replaceAll("/weathericons", "http://weather.china.com.cn/weathericons");
            str = str.replaceAll("/city", "http://weather.china.com.cn/city");
            // 替换表格宽度
            str = str.replaceFirst("<table width=\\\"308\\\"", "<table width=180");
            // 替换列宽
            str = str.replaceFirst("未来天气：", "");
            str = str.replaceFirst("<td width=\\\"68\\\"", "<td width=0");
            str = str.replaceFirst("<td width=\\\"67\\\"", "<td ");
            str = str.replaceFirst("<td width=\\\"73\\\"", "<td ");
        }

        return str;
    }

}
