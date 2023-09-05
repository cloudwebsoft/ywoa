package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

public class PaperNoPrefixDb extends QObjectDb {

    public PaperNoPrefixDb() {
        super();
    }

    public PaperNoPrefixDb getPaperNoPrefixDb(int id) {
        return (PaperNoPrefixDb) getQObjectDb(id);
    }

    /**
     * 根据名称获取
     *
     * @param name
     * @return
     */
    public PaperNoPrefixDb getPaperNoPrefixDbByName(String name) {
        String sql = "select id from " + getTable().getName() + " where name=" + StrUtil.sqlstr(name);
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri;
        try {
            ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return getPaperNoPrefixDb(rr.getInt("id"));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return null;
    }

    public Vector<PaperNoPrefixDb> getPaperNoFrefixs(String userName) {
        UserDb user = new UserDb();
        user = user.getUserDb(userName);

        DeptUserDb dud = new DeptUserDb();
        String depts = "";
        Vector<PaperNoPrefixDb> v = new Vector<>();
        for (DeptDb dd : dud.getDeptsOfUser(userName)) {
            if ("".equals(depts)) {
                depts = StrUtil.sqlstr(dd.getCode());
            } else {
                depts += "," + StrUtil.sqlstr(dd.getCode());
            }
        }

        String sql;
        if ("".equals(depts)) {
            // admin 登录操作
            sql = "select id from " + getTable().getName() + " order by orders desc";
        } else {
            // 取出公共文号及本单位的文号，两者都需在指定的部门范围内
            sql = "select p.id from " + getTable().getName() + " p, flow_paper_no_prefix_dept d where p.id=d.prefix_id and d.dept_code in (" + depts + ") and (p.unit_code=" + StrUtil.sqlstr(user.getUnitCode()) + " or p.unit_code=" + StrUtil.sqlstr(Leaf.UNIT_CODE_PUBLIC) + ") order by p.orders desc";
        }
        for (Object o : list(sql)) {
            PaperNoPrefixDb pdpd = (PaperNoPrefixDb) o;
            v.addElement(pdpd);
        }
        return v;
    }
}
