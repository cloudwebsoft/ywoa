package com.redmoon.oa.fileark;

import javax.servlet.jsp.tagext.*;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class DocListItemTag extends BodyTagSupport{
    DocBlockIterator ir;
    String field;
    String mode = "simple";// 显示模式
    int length = -1;
    /**
     * put your documentation comment here
     */
    public DocListItemTag () {
        ir = null;
    }

    /**
     * put your documentation comment here
     */
    public void setField (String field) {
        this.field = field;
    }

    public void setMode(String m) {
        this.mode = m;
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
        DocListTag dlt = (DocListTag)this.findAncestorWithClass(this,DocListTag.class);
        if (dlt != null) {
            ir = dlt.ir;
        }
        else {
            LogUtil.getLog(getClass()).error("get iterator failed!");
        }
        return  EVAL_BODY_BUFFERED;//SKIP_BODY;跳过body在mode=detail时会出错
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag () {
        try {
            if (field!=null && ir!=null && ir.hasNext()) {
               // LogUtil.getLog(getClass()).info("field=" + field + " ir=" + ir);
               Document doc = (Document) ir.next();
               if (doc==null) {
                   LogUtil.getLog(getClass()).info("doc=null");
                   return EVAL_PAGE;
               }
               String body = "";
               if (mode==null || !mode.equals("detail")) {
                   if (length!=-1)
                       body = "<a href='doc_show.jsp?id=" + doc.getID() + "'>" +
                              StrUtil.getLeft(doc.get(field), length) + "</a>";
                   else
                       body = "<a href='doc_show.jsp?id=" + doc.getID() + "'>" +
                              doc.get(field) + "</a>";
               }
               else {
                   BodyContent bc = getBodyContent();
                   //LogUtil.getLog(getClass()).info(bc.getString());
                   body = bc.getString();
                   String t = doc.getTitle();
                   if (length!=-1)
                       t = StrUtil.getLeft(t, length);

                   if (DateUtil.compare(new java.util.Date(), doc.getExpireDate())==2) {
                       if (doc.isBold())
                           t = "<B>" + t + "</B>";
                       if (!doc.getColor().equals("")) {
                           t = "<font color=" + doc.getColor() + ">" + t +
                               "</font>";
                       }
                   }

                   body = body.replaceAll("\\#title", doc.getTitle()); // 用于a中的title
                   body = body.replaceAll("\\$title", t);
                   body = body.replaceAll("\\$id", "" + doc.getID());
                   body = body.replaceAll("\\$hit", "" + doc.getHit());
                   body = body.replaceAll("\\$modifiedDate", DateUtil.format(doc.getModifiedDate(), "yyyy-MM-dd HH:mm:ss"));
                   body = body.replaceAll("\\$dirCode", doc.getDirCode());
                   body = body.replaceAll("\\@dirCode", StrUtil.UrlEncode(doc.getDirCode()));
                   if (doc.getIsNew() == 1) {
                       body = body.replaceAll("\\$isNew",
                                              "<img src='images/i_new.gif' width='18' height='7'>");
                   } else
                       body = body.replaceAll("\\$isNew",
                                              "");
               }
               pageContext.getOut().print(body);
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return  EVAL_PAGE;
    }

}
