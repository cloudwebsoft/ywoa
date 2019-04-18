package cn.js.fan.module.cms;

import org.apache.log4j.Logger;
import java.util.Iterator;
import javax.servlet.jsp.tagext.*;
import java.util.Vector;

public class DirListTag extends TagSupport {
    int[] ids = null;
    Logger logger = Logger.getLogger(DirListTag.class.getName());
    String parentCode;
    Iterator ir;
    static String cachePrix = "dirlist";

    public DirListTag() {
    }

    public void setParentCode(String cCode) {
        this.parentCode = cCode;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doStartTag() {
        getList();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doAfterBody() {
        try {
            if (ir == null || !ir.hasNext()) {
                return SKIP_BODY; // 输出完毕则继续往下执行
            } else {
                return EVAL_BODY_AGAIN; //还有未输出，重新对其body content计值
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        return SKIP_BODY;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }

    public void getList() {
        LeafChildrenCacheMgr dlc = new LeafChildrenCacheMgr(parentCode);
        Vector v = dlc.getList();
        if (v!=null)
            ir = v.iterator();
    }
}
