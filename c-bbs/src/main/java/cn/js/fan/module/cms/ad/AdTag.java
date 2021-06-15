package cn.js.fan.module.cms.ad;

import javax.servlet.http.*;
import javax.servlet.jsp.tagext.*;

import org.apache.log4j.*;

public class AdTag extends BodyTagSupport {
    int type = AdDb.TYPE_DOC;
    String dirCode;

    Logger logger = Logger.getLogger(AdTag.class.getName());

    public AdTag() {
    }

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        try {
            // BodyContent bc = getBodyContent();
            // String body = bc.getString();
            String str = "";
            if (type == AdDb.TYPE_DOC) {
                AdRender ar = new AdRender(dirCode, "doc");
                str = ar.renderDoc((HttpServletRequest) pageContext.
                                     getRequest());
            }
            else if (type == AdDb.TYPE_FLOAT) {
                AdRender ar = new AdRender(dirCode, "float");
                str = ar.renderFloat((HttpServletRequest) pageContext.
                                     getRequest());
            } else if (type == AdDb.TYPE_COUPLE_LEFT ||
                     type == AdDb.TYPE_COUPLE_RIGHT) {
                AdRender ar = new AdRender(dirCode, "couple");
                str = ar.renderCouple((HttpServletRequest) pageContext.
                                      getRequest());
            }
            else if (type == AdDb.TYPE_HEADER) {
                AdRender ar = new AdRender(dirCode, "header");
                str = ar.renderHeader((HttpServletRequest) pageContext.
                             getRequest());
            }
            else if (type == AdDb.TYPE_FOOTER) {
                AdRender ar = new AdRender(dirCode, "footer");
                str = ar.renderFooter((HttpServletRequest) pageContext.
                                      getRequest());
            }
            pageContext.getOut().print(str);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return EVAL_PAGE;
    }

}
