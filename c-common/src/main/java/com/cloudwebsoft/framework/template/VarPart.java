package com.cloudwebsoft.framework.template;

import java.util.HashMap;

import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ParamUtil;
import java.net.URLEncoder;
import cn.js.fan.util.DateUtil;
import java.util.Date;
import com.cloudwebsoft.framework.util.BeanUtil;
import java.util.List;

/**
 * <p>Title: </p>
 * 变量型标签
 * <p>Description:</p>
 * {$article.title} title为field {$doc.id(1).title(len=3)} id为keyName，即主键名称
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VarPart implements ITemplate {
    public String name;
    public String parentName;
    public HashMap props = new HashMap();

    public VarPart() {

    }

    public String toString(HttpServletRequest request, List param) {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getParentName() {
        return parentName;
    }

    public String getKeyName() {
        return keyName;
    }

    /**
     * 从request中获取主键的值
     * @param request HttpServletRequest
     * @return String
     */
    public String parseKeyValueFromRequest(HttpServletRequest request) {
        if (keyValue!=null && keyValue.startsWith("request")) {
            int p = keyValue.indexOf(".");
            if (p!=-1) {
                String param = keyValue.substring(p + 1);
                // LogUtil.getLog(getClass()).info("parseKeyValueFromRequest: param=" + param + " request=" + request);
                String v = ParamUtil.get(request, param);
                if (v.equals("")) {
                    v = (String)request.getAttribute(param);
                    if (v==null)
                        v = "";
                }
                return v;
            }
        }
        return "";
    }

    public String getKeyValue() {
        return keyValue;
    }

    public String getField() {
        return field;
    }

    /**
     * 解析属性(len=10, format=yyyy-MM-dd HH:mm:ss)
     * @param propsStr String
     */
    public void parseProps(String propsStr) {
        // LogUtil.getLog(getClass()).info("parseProps=" + propsStr);
        String[] propPairs = StrUtil.split(propsStr, ",");
        if (propPairs==null)
            return;
        int len = propPairs.length;
        for (int i=0; i<len; i++) {
            String str = propPairs[i];
            String[] pair = StrUtil.split(str, "=");
            if (pair!=null) {
                if (pair.length<2) {
                    LogUtil.getLog(getClass()).error("parseProps: propsStr=" + propsStr + " len=" + pair.length + " format error.");
                }
                else
                    props.put(pair[0].trim().toLowerCase(), pair[1].trim());
            }
        }
    }

    public String getProperty(Object obj) {
        BeanUtil bu = new BeanUtil();
        return format(bu.getProperty(obj, field), props);
    }

    /**
     * 根据是否显示时间，进行格式化
     * @param date Date
     * @param props HashMap
     * @return String
     */
    public static String formatDate(Date date, HashMap props) {
        String d = StrUtil.getNullStr((String) props.get("date"));
        boolean isDate = d.equals("true") || d.equals("yes");
        if (!isDate)
            return "";
        String dateFormat = (String) props.get("dateFormat");
        if (dateFormat == null) {
            dateFormat = "yy-MM-dd";
        }

        String dateAlign = (String)props.get("dateAlign");

        String str = "";
        boolean isRight = dateAlign.equals("right");

        if (isRight)
            str += "<span style='float:right'>";
        str += DateUtil.format(date, dateFormat);
        if (isRight)
            str += "</span>";

        return str;
    }

    /**
     * 套用props中的格式
     * @param value Object
     * @return String
     */
    public static String format(Object value, HashMap props) {
        if (value==null)
            return null;
        if (props==null || props.size()==0)
            return value.toString();
        if (value instanceof String) {
            String v = (String)value;
            // 处理长度
            String strLen = (String)props.get("len");
            // LogUtil.getLog(VarPart.class).info("value=" + value + " strLen=" + strLen);

            if (strLen!=null) {
                if (!StrUtil.isNumeric(strLen))
                    throw new IllegalArgumentException(strLen + " is not a number");
                int len = Integer.parseInt(strLen);
                v = StrUtil.getLeft(v, len);
            }
            String urlencode = (String)props.get("urlencode");
            if (urlencode!=null) {
                try {
                    v = URLEncoder.encode(v, urlencode);
                }
                catch (java.io.UnsupportedEncodingException e) {
                    LogUtil.getLog(VarPart.class).error(e.getMessage());
                }
            }

            String htmlencode = (String)props.get("htmlencode");
            if (htmlencode!=null) {
                if (htmlencode.equals("y"))
                    v = StrUtil.toHtml(v);
            }
            return v;
        }
        else if (value instanceof Date) {
            String v = "";
            String format = (String)props.get("format");
            if (format!=null) {
                v = DateUtil.format((Date)value, format);
            }
            else
                v = DateUtil.format((Date)value, "yyyy-MM-dd HH:mm:ss");
            return v;
        }
        else
            return value.toString();
    }

    public String keyName;
    public String keyValue;
    public String field;
}
