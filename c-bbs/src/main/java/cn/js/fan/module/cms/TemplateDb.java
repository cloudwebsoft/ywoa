package cn.js.fan.module.cms;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import java.util.Vector;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ResultRecord;

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
public class TemplateDb extends QObjectDb {
    public static final int TYPE_CODE_DOC = 0; // 用于文章页
    public static final int TYPE_CODE_LIST = 1; // 用于列表页
    public static final int TYPE_CODE_SUBJECT_LIST = 2; // 用于专题列表页
    public static final int TYPE_CODE_COLUMN = 3;  // 用于栏目

    public static final int TYPE_CODE_DEFAULT = -1;

    public TemplateDb() {
    }

    public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
        boolean re = super.save(jt, params);
        refreshList();
        return re;
    }

    public TemplateDb getTemplateDb(int id) {
        return (TemplateDb)getQObjectDb(new Integer(id));
    }

    public TemplateDb getDefaultTemplate(Leaf lf) {
        if (lf.getType()==Leaf.TYPE_LIST) {
            return getDefaultTemplate(TYPE_CODE_LIST);
        }
        else if (lf.getType()==Leaf.TYPE_COLUMN || lf.getType()==Leaf.TYPE_SUB_SITE) {
            return getDefaultTemplate(TYPE_CODE_COLUMN);
        }
        else {
            return getDefaultTemplate(TYPE_CODE_DOC);
        }
    }

    public TemplateDb getDefaultTemplate(int type) {
        String sql = "select id from cms_template where type_code=" + type + " order by orders asc";
        QObjectBlockIterator oir = getQObjects(sql, 0, 1);
        if (oir.hasNext()) {
            return (TemplateDb)oir.next();
        }
        return null;
    }
}
