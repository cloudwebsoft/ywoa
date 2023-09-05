package com.redmoon.oa.fileark;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class DocumentTag extends BodyTagSupport {
    int id = -1;
    String dirCode = "";

    public DocumentTag() {
    }

    public void setId(String strid) {
        this.id = Integer.parseInt(strid);
    }

    public void setDirCode(String d) {
        this.dirCode = d;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        try {
            Document doc = null;

            if (id != -1) {
                doc = getDoc(id);
            } else if (!dirCode.equals("")) {
                Leaf leaf = new Leaf();
                leaf = leaf.getLeaf(dirCode);
                //logger.info("dirCode=" + dirCode);
                if (leaf != null && leaf.getType() == 1)
                    doc = getDoc(leaf.getDocID());
            }
            if (doc != null) {
                BodyContent bc = getBodyContent();
                String body = bc.getString();
                //logger.info("content=" + doc.getContent());
                body = body.replaceAll("\\$title", doc.getTitle());
                body = body.replaceAll("\\$id", "" + doc.getID());
                body = body.replaceAll("\\$modifiedDate", DateUtil.format(doc.getModifiedDate(), "yyyy-MM-dd HH:mm:ss"));
                body = body.replaceAll("\\$summary", doc.getSummary());
                body = body.replaceAll("\\$content", doc.getContent(1));
                body = body.replaceAll("\\$dirCode", doc.getDirCode());
                   body = body.replaceAll("\\@dirCode", StrUtil.UrlEncode(doc.getDirCode()));
                pageContext.getOut().print(body);
            } else
                pageContext.getOut().print("文件不存在！");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return EVAL_PAGE;
    }

    public Document getDoc(int id) {
        DocumentMgr docmgr = new DocumentMgr();
        Document doc = docmgr.getDocument(id);
        return doc;
    }

}
