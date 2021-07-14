package cn.js.fan.db;
import javax.servlet.jsp.tagext.*;
import org.apache.log4j.Logger;

public class RRTag extends TagSupport{
    ResultIterator ri;
    String field;
    Logger logger = Logger.getLogger(RITag.class.getName());

    /**
     * put your documentation comment here
     */
    public RRTag () {
        ri = null;
    }

    /**
     * put your documentation comment here
     * @param para
     */
    public void setField (String field) {
        this.field = field;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doStartTag () {
        RITag rit = (RITag)this.findAncestorWithClass(this,RITag.class);
        if (rit != null) {
            ri = rit.ri;
        }
        else {
            logger.error("get resultsets failed!");
        }
        return  SKIP_BODY;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag () {
        try {
            if (field!=null && ri!=null) {
               ResultRecord rr = (ResultRecord) ri.next();
               String str = rr.get(field).toString();
               pageContext.getOut().print(str);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return  EVAL_PAGE;
    }

}
