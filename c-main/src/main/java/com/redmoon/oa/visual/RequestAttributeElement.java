package com.redmoon.oa.visual;

import javax.servlet.http.*;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RequestAttributeElement {

    public static String NAME_FORWARD = "forward";

    public static String TYPE_HIDDEN = "hidden";
    public static String TYPE_BUTTON = "button";

    public RequestAttributeElement createHidden(String name, String value) {
        RequestAttributeElement r = new RequestAttributeElement();
        r.name = name;
        r.type = TYPE_HIDDEN;
        r.value = value;
        return r;
    }

    public RequestAttributeElement createButton(String name, String value, String script) {
        RequestAttributeElement r = new RequestAttributeElement();
        r.name = name;
        r.type = TYPE_BUTTON;
        r.value = value;
        r.script = script;
        return r;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getScript() {
        return script;
    }

    public String toHtmlCtl(HttpServletRequest request) {
        if (type.equals(TYPE_HIDDEN)) {
            return "<input id='" + name + "' name='" + name + "' value='" + value + "' type='hidden' />";
        }
        else if (type.equals(TYPE_BUTTON)) {
            return "<input id='" + name + "' name='" + name + "' value='" + value + "' type='button' onclick='" + script + "' />";
        }
        return "";
    }

    public String toURL(HttpServletRequest request) {
        if (type.equals(TYPE_HIDDEN)) {
            return name + "=" + StrUtil.UrlEncode(value);
        }
        return "";
    }

    private String name;
    private String type;
    private String value;
    private String script;


}
