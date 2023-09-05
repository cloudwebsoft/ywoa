package cn.js.fan.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import cn.js.fan.base.ISkin;
import cn.js.fan.util.ResBundle;
import cn.js.fan.cache.jcs.RMCache;
import java.util.HashMap;
import cn.js.fan.util.XMLConfig;
import cn.js.fan.util.file.image.Thumbnail;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Element;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.util.StrUtil;

public class SkinUtil implements ISkin {
    static final String resName = "res.common";

    public static final String ERR_DB = "err_db";
    public static final String PVG_INVALID = "pvg_invalid";
    public static final String ERR_SQL = "err_sql";
    public static final String ERR_NOT_LOGIN = "err_not_login";
    public static final String ERR_ID = "err_id";

    public static final String SESSION_LOCALE = "locale";

    // transient RMCache rmCache = RMCache.getInstance();

    public SkinUtil() {
    }

    public static Locale getLocale(HttpServletRequest request) {
        if (Global.localeSpecified)
            return Global.getLocale();
        if (request==null) {
            // 如当通过博客模板生成首页的时候，会出现request为null的情况
             return Global.locale;
        }
        HttpSession session = request.getSession(true);
        Locale locale = (Locale) session.getAttribute(SESSION_LOCALE);
        if (locale != null) {
            return locale;
        }
        // IE中为zh-cn，firefox中为zh-cn,zh;q=0.5 遨游好象不发送这个Accept-Language
        String str = request.getHeader("Accept-Language"); // zh-cn
        // logger.info("getLocale:str=" + str);

        String lang = null;
        String country = null;
        if (str!=null) {
            try {
                String[] ary = str.split("-");
                lang = ary[0];
                if (ary.length>1) {
                	country = ary[1];
                }
                else {
                	country = "";
                }
                // 如果是firefox浏览器还需进一步处理country，如果是IE，则country即为cn
                int d = country.indexOf(",");
                if (d != -1)
                    country = country.substring(0, d);
                // logger.info("getLocale:lang=" + lang + " country=" + country);
            }
            catch (Exception e) { // 防止数组越界等
                LogUtil.getLog(SkinUtil.class).error("getLocale: " + StrUtil.trace(e));
            }
        }
        if (lang == null || country == null) {
            locale = Global.locale; // Locale.getDefault();
        }
        else {
            if (country.equalsIgnoreCase("sg"))
                country = "tw";
            else if (country.equalsIgnoreCase("hk"))
                country = "tw";
            else if (country.equalsIgnoreCase("mo"))
                country = "tw";
            HashMap hm = getSupportLangCountry();
            // 如果不支持则返回CWBBS默认的locale
            if (!hm.containsKey(lang.toLowerCase() + "_" + country.toLowerCase())) {
                locale = Global.locale;
            } else
                locale = new Locale(lang, country);
        }
        // logger.info("getLocale:" + locale);
        session.setAttribute(SESSION_LOCALE, locale);
        return locale;
    }

    public String LoadStr(HttpServletRequest request, String key) {
        return LoadString(request, resName, key);
    }

    public static String LoadString(HttpServletRequest request, String key) {
        return LoadString(request, resName, key);
    }

    public static String LoadString(HttpServletRequest request, String resource, String key) {
        String str = "";
        try {
            Locale locale = null;
            if (request==null)
                locale = Global.locale;
            else
                locale = getLocale(request);
            ResBundle rb = new ResBundle(resource, locale);
            str = rb.get(key);
            // logger.info("LoadString: key=" + key + " str=" + str + " " + getLocale(request));
        }
        catch (Exception e) {
            LogUtil.getLog(SkinUtil.class).error("LoadString: resource=" + resource + " key=" + key + " " + e.getMessage());
            LogUtil.getLog(SkinUtil.class).error(e);
        }
        return str;
    }

    public static String LoadStr(HttpServletRequest request, String resource, String key) {
        return LoadString(request, resource, key);
    }

    public static String makeErrMsg(HttpServletRequest request, String errMsg) {
        return makeErrMsg(request, errMsg, false);
    }

    public static String makeErrMsg(HttpServletRequest request, String errMsg, boolean isShowBack) {
        return makeErrMsg(request, errMsg, isShowBack, true);
    }

    public static String makeErrMsg(HttpServletRequest request, String errMsg, boolean isShowBack, boolean isToHtml) {
        if (!isShowBack) {
            if (isToHtml) {
                String str = StrUtil.toHtml(errMsg);
                // 此处替换掉\\r，因为在ParamChecker中，当getMessage()时，其中的\\r，如果直接仅toHtml，则将显示为\r
                str = str.replaceAll("\\\\r", "<BR />");
                return LoadString(request, "err_display_begin") +
                        StrUtil.ubb(request, str, false) +
                        LoadString(request, "err_display_end");
            }
            else
                return LoadString(request, "err_display_begin") +
                        StrUtil.ubb(request, errMsg, false) +
                        LoadString(request, "err_display_end");
        }
        else {
            if (isToHtml) {
                String str = StrUtil.toHtml(errMsg);
                // 此处替换掉\\r，因为在ParamChecker中，当getMessage()时，其中的\\r，如果直接仅toHtml，则将显示为\r
                str = str.replaceAll("\\\\r", "<BR />");
                return LoadString(request, "err_display_begin") +
                        StrUtil.ubb(request, str, false) + "<a class=\"historyBack\" title=\"点击此处返回\" href=\"javascript:;\" onclick=\"history.back()\">返回</a>" +
                        LoadString(request, "err_display_end");
            }
            else
                return LoadString(request, "err_display_begin") +
                    StrUtil.ubb(request, errMsg, false) + "<a class=\"historyBack\" title=\"点击此处返回\" href=\"javascript:;\" onclick=\"history.back()\">返回</a>" +
                    LoadString(request, "err_display_end");
        }
    }

    public static String makeInfo(HttpServletRequest request, String errMsg) {
        return makeInfo(request, errMsg, false);
    }

    public static String makeInfo(HttpServletRequest request, String errMsg, boolean isShowBack) {
        if (!isShowBack)
            return LoadString(request, "info_display_begin") + StrUtil.toHtml(errMsg) + LoadString(request, "info_display_end");
        else
            return LoadString(request, "info_display_begin") + StrUtil.toHtml(errMsg) + "<a class=\"historyBack\" title=\"点击此处返回\" href=\"javascript:;\" onclick=\"history.back()\">返回</a>" + LoadString(request, "info_display_end");
    }

    public static String waitJump(HttpServletRequest request, String msg, int t, String url) {
        String str = "";
        String spanid = "id" + System.currentTimeMillis();
        str = "\n<ol><b><span id=" + spanid + "> 3 </span>";
        str += SkinUtil.LoadString(request, "js_waitJump") + "</b></ol>";
        str += "<ol>" + msg + "</ol>";
        str += "<script language=javascript>\n";
        str += "<!--\n";
        str += "function tickout(secs) {\n";
        str += spanid + ".innerText = secs;\n";
        str += "if (--secs > 0) {\n";
        str += "  setTimeout('tickout(' +secs + ')', 1000);\n";
        str += "}\n";
        str += "}\n";
        str += "tickout(" + t + ");\n";
        str += "-->\n";
        str += "</script>\n";
        str += "<meta http-equiv=refresh content=" + t + ";url=" + url + ">\n";
        return str;
    }

    /**
     * 获取支持的lang_country
     * @return HashMap
     */
    public static HashMap getSupportLangCountry() {
        String key = "cwbbs_locales";
        HashMap hm = null;
        try {
            hm = (HashMap) RMCache.getInstance().get(key);
        }
        catch (Exception e) {
            LogUtil.getLog(SkinUtil.class).error("getSupportLocales:" + e.getMessage());
        }
        if (hm==null) {
            hm = new HashMap();
            XMLConfig xc = new XMLConfig("config_i18n.xml", true, "utf-8");
            Element root = xc.getRootElement();
            Element child = root.getChild("support");
            List list = child.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element e = (Element) ir.next();
                    hm.put(e.getChildText("lang") + "_" + e.getChildText("country"), "");
                }
                try {
                    RMCache.getInstance().put(key, hm);
                }
                catch (Exception e) {
                    LogUtil.getLog(SkinUtil.class).error("getSupportLocales2:" + e.getMessage());
                }
            }
        }
        return hm;
    }


}
