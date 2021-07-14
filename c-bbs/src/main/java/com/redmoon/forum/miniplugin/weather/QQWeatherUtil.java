package com.redmoon.forum.miniplugin.weather;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

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
public class QQWeatherUtil {
    Logger logger = Logger.getLogger(WeatherUtil.class.getName());

    public QQWeatherUtil() {
    }

    /**
     * 获取一个网页的内容
     * @param link String
     * @return String
     */
    public String gather(String link) {
        String str = "";
        try {
            URL url = new URL(link);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            // logger.error("url=" + url + " encode:"+huc.getContentEncoding()+" type:"+huc.getContentType());
            String type = huc.getContentType();
            String charset = "utf-8";
            int index = type.indexOf("charset=");
            if (index != -1) {
                index = index + 8;
                charset = type.substring(index).trim();
            } else
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
        } catch (Exception e) {
            logger.error("gather: " + e.getMessage());
        }
        return str;
    }

    public String getWeather() {
        String content = gather("http://weather.news.qq.com/inc/ss59.htm");
        return parseWeatherQQ(content);
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
            str = str.replaceAll("\\.\\./images",
                                 "http://mimg.163.com/tianqi/images");
            // 替换onClick="detail();"
            str = str.replaceAll("detail\\(\\);", "");
            str = str.replaceAll("tomorrow\\(\\);", "");
        }

        return str;
    }

    public String parseWeatherQQ(String content) {
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        String str = "";

        patternStr = "<table width=\\\"189\\\" border=\\\"0\\\" cellspacing=\\\"0\\\" cellpadding=\\\"0\\\" background.*?height=\\\"3\\\"></td></tr>";
        // patternStr = "<table cellSpacing=\\\"0\\\" cellPadding=\\\"0\\\" width=\\\"189\\\".*        </table>";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        matcher = pattern.matcher(content);
        // System.out.println(getClass() + " content=" + content);
        // 提取今日的天气
        if (matcher.find()) {
            str = matcher.group(0);
            str = str + "</table>";
            // logger.info("str=" + str);
            str = str.replaceAll("width=\\\"189\\\"",
                                 "width=100%");

            // 替换表格背景
            str = str.replaceAll("background=\"/images/r_tembg2.gif\"", "");

            // 替换第一行的标头背景（灰色），左侧
            str = str.replaceAll("background=\"/images/r_tembg4.gif\"", "");

            // 替换第一行的标头背景（灰色），右侧
            str = str.replaceAll("background=\"/images/r_tembg5.gif\"", "");

            // 去除第一列的列宽
            str = str.replaceAll("width=\"72\"", "width=50%");

            // 去除第二列的列宽
            str = str.replaceAll("width=\"117\"", "width=50%");

            // 去除顶部的图片
            str = str.replaceAll("<tr><td colspan=\"2\"><img src=\"/images/r_tembg1.gif\" width=100% height=\"6\"></td>", "");

            // 去除底部的图片
            str = str.replaceAll("<tr><td colspan=\"2\"><img src=\"/images/r_tembg3.gif\" width=100% height=\"3\"></td></tr>", "");

            // 替换列高
            // str = str.replaceAll("<td height=\"20\">", "<td height=0>");
            // 替换图片
            str = str.replaceAll("/images", "http://weather.news.qq.com/images");

        }
        return str;
    }

    public String parseWeather(String content) {
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        String str = "";

        // patternStr = "(\\[URL\\])(.*)(\\[\\/URL\\])";
        // patternStr = "<table width=\\\"308\\\".*</table>"; //
        patternStr = "<table width=\\\"200\\\".*      </table></td>";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        matcher = pattern.matcher(content);
        // 提取今日的天气
        if (matcher.find()) {
            str = matcher.group(0);
            // logger.info("str=" + str);
            str = str.replaceAll("/weathericons",
                                 "http://weather.china.com.cn/weathericons");
            str = str.replaceAll("/city/58248_full.html",
                                 Global.getRootPath() + "/forum/miniplugin/weather/58248_full.htm");
            // 替换表格宽度
            str = str.replaceAll("<table width=\\\"200\\\"", "<table width=172");
            // 替换列高
            str = str.replaceAll("<td height=\"20\">", "<td height=0>");
            // 替换列宽及图片
            // str = str.replaceFirst("<td width=\\\"29\\\" align=\\\"center\\\"><img src=\\\"/images/t_2.gif\\\" width=\\\"19\\\" height=\\\"13\\\">", "<td width=1>");
            str = str.replaceFirst("<td width=\"29\" align=\"center\"><img src=\"/images/t_2.gif\" width=\"19\" height=\"13\">",
                                   "<td width=1>");
            // 替换列宽
            str = str.replaceFirst("未来天气", "");
            str = str.replaceFirst("<td width=\\\"65\\\"", "<td width=15");
            str = str.replaceFirst("<td width=\\\"67\\\"", "<td ");
            str = str.replaceFirst("<td width=\\\"73\\\"", "<td ");
        }

        return str;
    }

    public void createIncFile(String content, String fullcontent) throws
            ErrMsgException {
        String str = "";
        str +=
                "<%@ page pageEncoding='utf-8' contentType='text/html;charset=utf-8' %>";
        str += content;

        cn.js.fan.util.file.FileUtil fu = new cn.js.fan.util.file.FileUtil();
        fu.WriteFileUTF8(Global.realPath + "forum/miniplugin/weather/58248.htm",
                         str);

        str =
                "<%@ page pageEncoding='utf-8' contentType='text/html;charset=utf-8' %>";
        str += "<style>TD {FONT-SIZE: 9pt;}</style>";
        str += "<table align=center><tr><td>" + fullcontent +
                "</td></tr></table>";
        fu.WriteFileUTF8(Global.realPath +
                         "forum/miniplugin/weather/58248_full.htm", str);
    }
}
