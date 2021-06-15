package com.redmoon.oa.help;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.*;
import cn.js.fan.db.ResultRecord;
import org.apache.log4j.Logger;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

public class DocumentTag extends BodyTagSupport {
    int id = -1;
    String dirCode = "";

    Logger logger = Logger.getLogger(DocumentTag.class.getName());
	private String type;
	
	private int size = 0;

    public DocumentTag() {
    }

    public void setId(String strid) {
        this.id = Integer.parseInt(strid);
    }

    public void setDirCode(String d) {
        this.dirCode = d;
    }
    
    public String getType() {
    	return type;
    }
    
    public void setType(String type) {
    	this.type = type;
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
                if (leaf != null && leaf.getType() == 1) {
                	id = leaf.getDocID();
                    doc = getDoc(leaf.getDocID());
                }
            }
            if (doc != null) {
                BodyContent bc = getBodyContent();
                /*
                String type = "";
                if (bc!=null)
                	type = bc.getString();
                */
                
                if (type==null)
                	type = "";
            	HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
                
                String str = "<img id='tip" + id + "' src='" + request.getContextPath() + "/images/help.gif' align='absmiddle' />";
                
                if (request.getAttribute("isHelpTagJs") == null) {
                	str += "<link href=\"" + request.getContextPath() + "/js/qTip2/jquery.qtip.css\" rel=\"stylesheet\" />";
                	str += "<script src=\"" + request.getContextPath() + "/js/qTip2/jquery.qtip.js\"></script>";
    	            str += "<script src='" + request.getContextPath() + "/help/js.jsp?id=" + id + "&type=" + type + "&size=" + size + "'></script>";
    	            request.setAttribute("isHelpTagJs", "y");
                }
                
                pageContext.getOut().print(str);
            } else
                pageContext.getOut().print("文件不存在！");
        } catch (Exception e) {
            logger.error(StrUtil.trace(e));
        }
        return EVAL_PAGE;
    }

    public Document getDoc(int id) {
        DocumentMgr docmgr = new DocumentMgr();
        Document doc = docmgr.getDocument(id);
        return doc;
    }

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

}
