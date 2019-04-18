package cn.js.fan.module.cms;

import org.apache.log4j.Logger;
import java.util.Iterator;
import javax.servlet.jsp.tagext.*;
import cn.js.fan.util.StrUtil;

public class DocListTag extends TagSupport {
    int[] ids = null;
    Logger logger = Logger.getLogger(DocListTag.class.getName());
    String dirCode, query = "", action;
    String subjectCode;
    int start = 0, end = 0;
    Iterator ir;

    public DocListTag() {
    }

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public void setStart(int s) {
        this.start = s;
    }

    public void setEnd(int e) {
        this.end = e;
    }

    public void setQuery(String q) {
        this.query = q;
    }

    public void setAction(String a) {
        this.action = a;
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
        // logger.info("query=" + query);
        // 注意如果在Tag标签中不设query，就会使得query在页面刷新以后，仍保留上次的值
        if (dirCode!=null) {
            if (action.equals("list")) {
                query = SQLBuilder.getDirDocListSql(dirCode);
                Document doc = new Document();
                String groupKey = dirCode;
                // logger.info("getList:" + query);
                ir = doc.getDocuments(query, groupKey, start, end);
            } else {
                if (action.equalsIgnoreCase("list_nocache")) {
                    if (query==null || query.equals(""))
                        query = SQLBuilder.getDirDocListSql(dirCode);
                    Document doc = new Document();
                    ir = doc.list(query, start, end).iterator();
                } else {
                    Document doc = new Document();
                    String groupKey = dirCode;
                    // logger.info("getList:" + query);
                    ir = doc.getDocuments(query, groupKey, start, end);
                }
            }
        }
        else if (subjectCode!=null) {
            query = SQLBuilder.getSubjectDocListSql(subjectCode);
            SubjectListDb sld = new SubjectListDb();
            String groupKey = subjectCode;
            // logger.info("getList:" + query);

            ir = sld.getDocuments(query, groupKey, start, end);
        }
    }
}
