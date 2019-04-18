package com.redmoon.oa.ui;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class LocalTag extends BodyTagSupport {

    Logger logger = Logger.getLogger(LocalTag.class.getName());

    public LocalTag() {
    }

    public void setRes(String res) {
        this.res = res;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        try {
            String str = "";
            if (res.equals(""))
                str = LocalUtil.LoadString((HttpServletRequest)pageContext.getRequest(), key);
            else
                str = LocalUtil.LoadString((HttpServletRequest)pageContext.getRequest(), res, key);
            pageContext.getOut().print(str);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return EVAL_PAGE;
    }

    String res = "";
    String key = "";
}
