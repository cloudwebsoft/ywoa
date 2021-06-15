package com.cloudwebsoft.framework.security;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class AntiXSS {
    public static String antiXSS( String html ) {
        return antiXSS(html, true);
    }

    public static String antiXSS( String html, boolean isGet) {
    	// 应对攻击方式：'A"+alert(1295)+"，过滤后变为：'A&quot;+alert(1295)+&quot;
    	// 会导致过滤器通不过象这样的脚本<p>1111</p>
    	// html = StringEscapeUtils.escapeHtml4(html);

    	return cn.js.fan.security.AntiXSS.antiXSS(html, isGet);

    	// Jsoup Whitelist.none()只能过滤标签
    	// return Jsoup.clean(html, Whitelist.none());  
    }
    
    public static String clean(String html) {
    	return Jsoup.clean(html, Whitelist.none());  
    }
}
