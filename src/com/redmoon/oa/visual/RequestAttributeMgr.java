package com.redmoon.oa.visual;

import java.util.*;

import javax.servlet.http.*;

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
public class RequestAttributeMgr {

    public RequestAttributeMgr() {
    }

    public Map getAttributes(HttpServletRequest request) {
        Map attributes = (Map)request.getAttribute("visual_req_attr");
        if (attributes==null) {
            attributes = new HashMap();
            request.setAttribute("visual_req_attr", attributes);
        }
        return attributes;
    }

    public void addAttribute(HttpServletRequest request, RequestAttributeElement rae) {
        Map attributes = getAttributes(request);
        attributes.put(rae.getName(), rae);
    }

    public RequestAttributeElement getAttribute(HttpServletRequest request, String attrName) {
        Map attributes = getAttributes(request);
        return (RequestAttributeElement)attributes.get(attrName);
    }

    /**
     * 显示于visual_add.jsp的表单中
     * @param request HttpServletRequest
     * @return String
     */
    public String render(HttpServletRequest request) {
        Map attributes = getAttributes(request);
        Iterator ir = attributes.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        while (ir.hasNext()) {
            String key = (String)ir.next();
            RequestAttributeElement rae = (RequestAttributeElement)attributes.get(key);
            sb.append(rae.toHtmlCtl(request));
        }
        return sb.toString();
    }

    /**
     * 显示于visual_add.jsp的form的action中
     * @param request HttpServletRequest
     * @return String
     */
    public String renderURL(HttpServletRequest request) {
        Map attributes = getAttributes(request);
        Iterator ir = attributes.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        int k = 0;
        while (ir.hasNext()) {
            String key = (String)ir.next();
            RequestAttributeElement rae = (RequestAttributeElement)attributes.get(key);
            if (!rae.getType().equals(RequestAttributeElement.TYPE_BUTTON)) {
                if (k==0) {
                    sb.append(rae.toURL(request));
                    k++;
                }
                else
                    sb.append("&" + rae.toURL(request));
            }
        }
        return sb.toString();
    }
}
