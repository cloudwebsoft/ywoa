package cn.js.fan.util;

import java.util.*;

import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

public class ResBundle {
    // 默认字符集为简体中文
    Locale locale;
    ResourceBundle bundle;
    String encode = "";

    public ResBundle(String resName, Locale locale) {
        if (locale == null) {
            try {
                locale = Global.locale;
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("ResBundle1:" + e.getMessage());
                locale = Locale.getDefault();
            }
        }
        this.locale = locale;

        if (locale.getLanguage().equals("zh")) {
            if (locale.getCountry().equals("TW"))
                this.encode = "BIG5";
            else if (locale.getCountry().equals("CN"))
                this.encode = "gb2312";
        }
        if(locale.getLanguage().equals("en")){
        	if(locale.getCountry().equals("US")){
        		this.encode = "utf-8";
        	}
        }
        this.resName = resName;
        try {
            bundle = ResourceBundle.getBundle(resName, locale);
        } catch (MissingResourceException e) {
            LogUtil.getLog(getClass()).error("ResBundle2:" + e.getMessage());
            // bundle = ResourceBundle.getBundle(resName, Locale.CHINA);
        }
    }

    public String get(String key) {
        String str = bundle.getString(key);
        if (str==null)
            return "";
        if (!encode.equals("")) {
            try {
                // gb2312的内容可以通过如下方式处理，但big5却不行
                // str = new String(str.getBytes("ISO8859-1"), encode);

                // 对于gb2312，资源文件不需要转成utf-8编码，转了反而会变成乱码，而big5的资源文件需转换为utf-8编码
                // 在本机区域改为台湾时，gb2312资源文件不能转换为utf8编码，而当本机区域改回中国即简体时，gb2312的资源文件需转成utf8编码
                str = new String(str.getBytes("ISO8859-1"), "utf-8");
            } catch (java.io.UnsupportedEncodingException ex) {
                LogUtil.getLog(getClass()).error("resName=" + resName + " key=" + key + " locale=" + locale);
                LogUtil.getLog(getClass()).error(ex);
            }
        }
        return str;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResName() {
        return resName;
    }

    private String resName;

}
