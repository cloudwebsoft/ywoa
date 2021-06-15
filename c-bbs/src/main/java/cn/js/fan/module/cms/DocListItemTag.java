package cn.js.fan.module.cms;

import javax.servlet.jsp.tagext.*;
import org.apache.log4j.Logger;
import java.util.Iterator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.DateUtil;

public class DocListItemTag extends BodyTagSupport{
    Iterator ir;
    String field;
    Logger logger = Logger.getLogger(DocListItemTag.class.getName());
    String mode = "simple"; // 显示模式
    int length = -1;
    String action = "";
    /**
     * put your documentation comment here
     */
    public DocListItemTag () {
        ir = null;
    }

    /**
     * put your documentation comment here
     * @param para
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
            action = dlt.action;
        }
        else {
            logger.error("get iterator failed!");
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
               // logger.info("field=" + field + " ir=" + ir);
               Document doc = (Document) ir.next();
               if (doc==null) {
                   logger.info("doEndTag doc=null");
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
                   //logger.info(bc.getString());
                   body = bc.getString();
                   String t = doc.getTitle();
                   if (length!=-1)
                       t = StrUtil.getLeft(t, length);

                   boolean isDateValid = DateUtil.compare(new java.util.Date(), doc.getExpireDate())==2;
                   if (isDateValid) {
                       if (doc.isBold())
                           t = "<B>" + t + "</B>";
                       if (!doc.getColor().equals("")) {
                           t = "<font color=" + doc.getColor() + ">" + t +
                               "</font>";
                       }
                   }

                   Leaf lf = new Leaf();
                   lf = lf.getLeaf(doc.getDirCode());
                   String dirName = "";
                   if (lf!=null)
                       dirName = lf.getName();
                   body = body.replaceAll("\\#title", doc.getTitle()); // 用于a中的title
                   body = body.replaceAll("\\$title", t);
                   body = body.replaceAll("\\$summary", doc.getSummary());
                   body = body.replaceAll("\\$id", "" + doc.getID());
                   body = body.replaceAll("\\$hit", "" + doc.getHit());
                   body = body.replaceAll("\\$modifiedDate_s", cn.js.fan.util.DateUtil.format(doc.getModifiedDate(), "yy-MM-dd"));
                   body = body.replaceAll("\\$modifiedDate", cn.js.fan.util.DateUtil.format(doc.getModifiedDate(), "yy-MM-dd HH:mm"));
                   body = body.replaceAll("\\$createDate_s", cn.js.fan.util.DateUtil.format(doc.getCreateDate(), "yy-MM-dd"));
                   body = body.replaceAll("\\$createDate", cn.js.fan.util.DateUtil.format(doc.getCreateDate(), "yy-MM-dd HH:mm"));
                   body = body.replaceAll("\\$dirCode", doc.getDirCode());
                   body = body.replaceFirst("\\$dirName", dirName);
                   body = body.replaceFirst("\\$source", doc.getSource());
                   body = body.replaceAll("\\@dirCode", StrUtil.UrlEncode(doc.getDirCode()));
                   body = body.replaceAll("\\$htmlName", doc.getDocHtmlName(1));

                   if (isDateValid && doc.getIsNew() == 1) {
                       body = body.replaceAll("\\$isNew",
                                              "<img border=0 src='images/i_new.gif' width='18' height='7'>");
                   } else
                       body = body.replaceAll("\\$isNew",
                                              "");
               }
               pageContext.getOut().print(body);
            }
        } catch (Exception e) {
            logger.error("doEndTag: " + e.getMessage());
            e.printStackTrace();
        }
        return  EVAL_PAGE;
    }

}
