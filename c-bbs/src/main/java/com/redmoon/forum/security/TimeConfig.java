package com.redmoon.forum.security;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import org.apache.log4j.Logger;
import java.net.URLDecoder;
import java.util.*;
import cn.js.fan.util.StrUtil;
import cn.js.fan.cache.jcs.RMCache;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.Privilege;

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
public class TimeConfig {
    private final String CONFIG_FILENAME = "config_forum_time.xml";
    private final String rootChild = "forum";
    Logger logger;
    private String cfgpath;
    Document doc = null;
    Element root = null;
    final String cacheGroup = "forum_reg_time";

    public TimeConfig() {
        logger = Logger.getLogger(TimeConfig.class.getName());
    }

    public void init() {
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("RegConfig:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("RegConfig:" + e.getMessage());
        }
    }

    public String getDescription(HttpServletRequest request, String name) {
        return SkinUtil.LoadString(request, "res.config.config_forum_time", name);
    }

    public Element getRootElement() {
        if (root == null) {
            init();
        }
        return root;
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(cacheGroup);
        } catch (Exception e) {
            logger.error("refresh:" + e.getMessage());
        }
    }

    public String getProperty(String name) {
        String v;
        if (root == null)
            init();
        Element element = root.getChild("forum").getChild(name);
        v = element.getValue();
        return v;
    }

    public int[][] getTimeSectArray(String name) {
        int[][] r = null;
        try {
            r = (int[][]) RMCache.getInstance().getFromGroup(name, cacheGroup);
        } catch (Exception e) {
            logger.error("getTimeSect:" + e.getMessage());
        }
        if (r == null) {
            String timeSectProp = getProperty(name);
            if (timeSectProp.equals(""))
                return null;
            r = parseTimeSect(timeSectProp);
            if (r!=null) {
                try {
                    RMCache.getInstance().putInGroup(name, cacheGroup, r);
                } catch (Exception e) {
                    logger.error("getTimeSect2:" + e.getMessage());
                }
            }
        }
        return r;
    }

    public int getIntProperty(String name) {
        String p = getProperty(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        } else
            return 0;
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return p.equals("true");
    }

    public String[] getStringArrProperty(String name) {
        String[] p = null;
        p = StrUtil.split(getProperty(name), "\n");
        return p;
    }

    public void set(String code, String property, String textValue) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    List list1 = child.getChildren();
                    if (list1 != null) {
                        Iterator ir1 = list1.listIterator();
                        while (ir1.hasNext()) {
                            Element childContent = (Element) ir1.next();
                            if (childContent.getName().equals(property)) {
                                childContent.setText(textValue);
                            }
                        }
                    }
                } // end if
            }
        }
    }

    public boolean put(String name, String value) {
        if (root == null)
            init();
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null)
            return false;
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
        refresh();
    }

    /**
     * 将格式为12:20:00-20:00:00或者12:20:00-这样的时间段解析为二维数组
     * @param timeSect String
     * @return int[] null表示timeSect为空或者格式不符，array[1][0]=-1表示时间段的结束时间为空
     */
    public int[][] parseTimeSect(String timeSect) {
        if (timeSect.equals("-"))
            return null;
        int[][] ary = new int[2][3];

        ary[1][0] = -1;

        // 时间格式为
        int p = timeSect.indexOf("-");
        String value1 = "";
        String value2 = "";
        if (p != -1) {
            value1 = timeSect.substring(0, p);
            value2 = timeSect.substring(p + 1);
        }
        else
            value1 = timeSect;
        String[] ary1 = StrUtil.split(value1, ":");

        // 时间段没有开始部分，如：-12:22:20
        if (ary1==null)
            return null;

        int len1 = ary1.length;
        String sh1 = ary1[0]; // 小时
        String sm1 = "0";     // 分钟
        String ss1 = "0";     // 秒
        if (len1>1)
            sm1 = ary1[1];
        if (len1>2)
            ss1 = ary1[2];

        // 格式中有错误
        if (StrUtil.isNumeric(sh1) && StrUtil.isNumeric(sm1) && StrUtil.isNumeric(ss1))
            ;
        else
            return null;

        int h1 = Integer.parseInt(sh1);
        int m1 = Integer.parseInt(sm1);
        int s1 = Integer.parseInt(ss1);

        ary[0][0] = h1;
        ary[0][1] = m1;
        ary[0][2] = s1;

        if (!value2.equals("")) {
            String[] ary2 = StrUtil.split(value2, ":");
            int len2 = ary2.length;
            String sh2 = ary2[0];
            String sm2 = "0";
            String ss2 = "0";
            if (len2>1)
                sm2 = ary2[1];
            if (len2>2)
                ss2 = ary2[2];

            if (StrUtil.isNumeric(sh2) && StrUtil.isNumeric(sm2) && StrUtil.isNumeric(ss2))
                ;
            else
                return null;

            int h2 = Integer.parseInt(sh2);
            int m2 = Integer.parseInt(sm2);
            int s2 = Integer.parseInt(ss2);
            ary[1][0] = h2;
            ary[1][1] = m2;
            ary[1][2] = s2;
        }
        return ary;
    }

    public boolean isBetweenTimeSect(String timeSect) {
        int[][] ary = getTimeSectArray(timeSect);
        if (ary==null)
            return false;

        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        int s = cal.get(Calendar.SECOND);

        boolean isAfterBegin = false;
        if (h>ary[0][0])
            isAfterBegin = true;
        else if (h==ary[0][0]) {
            if (m>ary[0][1])
                isAfterBegin = true;
            else if (m==ary[0][1]) {
                if (s>=ary[0][2])
                    isAfterBegin = true;
                else
                    isAfterBegin = false;
            }
            else
                isAfterBegin = false;
        }
        else
                isAfterBegin = false;

        if (!isAfterBegin)
            return false;

        // 如果时间段结束部分为空
        if (ary[1][0]==-1) {
            return isAfterBegin;
        }
        else {
           boolean isAfterEnd = false;
           if (h>ary[1][0])
               isAfterEnd = true;
           else if (h==ary[1][0]) {
               if (m>ary[1][1])
                   isAfterEnd = true;
               else if (m==ary[1][1]) {
                   if (s>=ary[1][2])
                       isAfterEnd = true;
                   else
                       isAfterEnd = false;
               }
               else
                   isAfterEnd = false;
           }
           else
                isAfterEnd = false;

          return !isAfterEnd;
        }
    }

    /**
     * 当前时间段是否允许访问论坛
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean isVisitForbidden(HttpServletRequest request) {
        if (Privilege.isMasterLogin(request))
            return false;
        if (isBetweenTimeSect("forbidVisitTime1") || isBetweenTimeSect("forbidVisitTime2")) {
            return true;
        }
        return false;
    }

    /**
     * 当前时间段是否允许发贴或回贴，用于com.redmoon.forum.Privilege的checkCanPost方法中
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean isPostForbidden(HttpServletRequest request) {
       /*在com.redmoon.forum.Privilege的checkCanPost方法中对总版及版主的权限有控制
       Privilege privilege = new Privilege();
       if (privilege.isMasterLogin(request))
           return false;
        */
       if (isBetweenTimeSect("forbidPostTime1") || isBetweenTimeSect("forbidPostTime2")) {
           return true;
       }
       return false;
   }

   public boolean isPostNeedCheck(HttpServletRequest request) {
      if (Privilege.isMasterLogin(request))
          return false;
      if (isBetweenTimeSect("topicCheckTime1") || isBetweenTimeSect("topicCheckTime2")) {
          return true;
      }
      return false;
   }

   public boolean isSearchForbidden(HttpServletRequest request) {
       Privilege privilege = new Privilege();
       if (privilege.isMasterLogin(request))
          return false;

      if (isBetweenTimeSect("forbidSearchTime")) {
          return true;
      }
      return false;
   }
}
