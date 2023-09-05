package com.redmoon.oa.fileark;

import java.util.*;

import javax.servlet.jsp.tagext.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;

public class DirListItemTag extends BodyTagSupport {
    Iterator ri;
    String field;
    int length = -1;

    /**
     * put your documentation comment here
     */
    public DirListItemTag () {
        ri = null;
    }

    /**
     * put your documentation comment here
     */
    public void setField (String field) {
        this.field = field;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setLength(String l) {
        if (StrUtil.isNumeric(l))
            length = Integer.parseInt(l);
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doStartTag () {
        DirListTag rit = (DirListTag)this.findAncestorWithClass(this,DirListTag.class);
        if (rit != null) {
            ri = rit.ir;
        }
        else {
            LogUtil.getLog(getClass()).error("get resultsets failed!");
        }
        return EVAL_BODY_BUFFERED;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag () {
        try {
            if (ri!=null && ri.hasNext()) {
               Leaf lf = (Leaf) ri.next();
               // LogUtil.getLog(getClass()).info("doEndTag lf=" + lf + " mode=" + mode + " field=" + field);
               String body = "";
               if (mode==null || !mode.equals("detail")) {
                   if (field!=null) {
                       if (lf.getType() == lf.TYPE_DOCUMENT)
                           body = "<a href='doc_show.jsp?id=" + lf.getDocID() +
                                  "'>" + lf.get(field) + "</a>";
                       else if (lf.getChildCount() == 0) // 无子目录
                           body = "<a href='doc_list.jsp?dir_code=" +
                                  StrUtil.UrlEncode(lf.getCode()) + "'>" +
                                  lf.get(field) + "</a>";
                       else if (lf.getChildCount() >= 1)
                           body = "<a href='doc_list_sub.jsp?dir_code=" +
                                  StrUtil.UrlEncode(lf.getCode()) + "'>" +
                                  lf.get(field) + "</a>";
                       else
                           body = lf.get(field);
                   }
              }
               else {
                   BodyContent bc = getBodyContent();
                   // LogUtil.getLog(getClass()).info(bc.getString());
                   body = bc.getString();
                   String t = lf.getName();
                   if (length!=-1)
                       t = StrUtil.getLeft(t, length);
                   body = body.replaceAll("\\#name", lf.getName()); // 用于a中的title
                   body = body.replaceAll("\\$name", t);
                   body = body.replaceAll("\\$code", "" + lf.getCode());
                   body = body.replaceAll("\\@code", "" + StrUtil.UrlEncode(lf.getCode()));
                   body = body.replaceAll("\\$docId", "" + lf.getDocID());
               }
               pageContext.getOut().print(body);
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("doEndTag:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        return  EVAL_PAGE;
    }

    private String mode;

}
