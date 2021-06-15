package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

public class PaperNoPrefixDb extends QObjectDb {
   
//	public static final int IS_DELETED = 0;
//    public static final int IS_NOT_DELETED = 1;
    public PaperNoPrefixDb() {
         super();
    }
    
    public PaperNoPrefixDb getPaperNoPrefixDb(int id) {
        return (PaperNoPrefixDb) getQObjectDb(new Integer(id));
    }
    
    /**
     * 根据名称获取
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
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		return getPaperNoPrefixDb(rr.getInt("id"));
	    	}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public Vector getPaperNoFrefixs(String userName) {
    	UserDb user = new UserDb();
    	user = user.getUserDb(userName);
    	
    	Vector v = new Vector();
    	DeptUserDb dud = new DeptUserDb();
    	String depts = "";
    	Iterator ir = dud.getDeptsOfUser(userName).iterator();
    	while (ir.hasNext()) {
    		DeptDb dd = (DeptDb)ir.next();
    		if (depts.equals("")) {
    			depts = StrUtil.sqlstr(dd.getCode());
    		}
    		else {
    			depts += "," + StrUtil.sqlstr(dd.getCode());
    		}
    	}
    	if (depts.equals("")) {
    		return v;
    	}
    	
    	// 取出公共文号及本单位的文号，两者都需在指定的部门范围内
    	String sql = "select p.id from " + getTable().getName() + " p, flow_paper_no_prefix_dept d where p.id=d.prefix_id and d.dept_code in (" + depts + ") and (p.unit_code=" + StrUtil.sqlstr(user.getUnitCode()) + " or p.unit_code=" + StrUtil.sqlstr(Leaf.UNIT_CODE_PUBLIC) + ") order by p.orders desc";
    	ir = list(sql).iterator();
    	while (ir.hasNext()) {
    		PaperNoPrefixDb pdpd = (PaperNoPrefixDb)ir.next();
    		v.addElement(pdpd);
    	}
    	return v;
    }        
}
