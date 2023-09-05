package com.redmoon.oa.flow;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

import javax.servlet.http.*;

public class PaperNoPrefixMgr {

	public PaperNoPrefixMgr() {
	}

	public PaperNoPrefixDb getPaperNoPrefixDb(int id) {
		PaperNoPrefixDb addr = new PaperNoPrefixDb();
		return addr.getPaperNoPrefixDb(id);
	}

	public boolean create(HttpServletRequest request) throws ErrMsgException,
			ResKeyException {
		boolean re = true;
		PaperNoPrefixDb pnp = new PaperNoPrefixDb();
        String name = ParamUtil.get(request, "name");
        if (checkExist(name)) {
			throw new ErrMsgException("名称不能重复！");
		}
        int id = (int) SequenceManager.nextID(SequenceManager.FLOW_PAPER_NO_PREFIX);
        int orders = ParamUtil.getInt(request, "sort", 1);
        String depts = ParamUtil.get(request, "depts");
        String unitCode = ParamUtil.get(request, "unitCode");
		re = pnp.create(new JdbcTemplate(), new Object[] {
			new Integer(id),
			name,
 	        new Integer(orders),
 	        unitCode
			});
		
		if (re){
			PaperNoPrefixDeptDb pnpd = new PaperNoPrefixDeptDb();
			String[] ary = StrUtil.split(depts, ",");
			int len = 0;
			if (ary!=null) {
				len = ary.length;
				for (int i=0; i<len; i++) {
					re = pnpd.create(new JdbcTemplate(), new Object[] {
						id,
						ary[i]
					});
				}
			}
		}
		return re;
	}

	public boolean save(HttpServletRequest request) throws ErrMsgException,
			ResKeyException, SQLException {
		boolean re = true;
		PaperNoPrefixDb pnp = new PaperNoPrefixDb();
		
        int id = ParamUtil.getInt(request, "id");
        String name = ParamUtil.get(request, "name");
        int orders = ParamUtil.getInt(request, "sort", 1);
        String depts = ParamUtil.get(request, "depts");
                
		pnp = pnp.getPaperNoPrefixDb(id);
		pnp.set("name", name);
		pnp.set("orders", new Integer(orders));
		re = pnp.save();
        
		if (re) {
	        PaperNoPrefixDeptDb pnpd = new PaperNoPrefixDeptDb();
	        pnpd.delOfPrefix(id);
			String[] ary = StrUtil.split(depts, ",");
			int len = 0;
			if (ary!=null) {
				len = ary.length;
				for (int i=0; i<len; i++) {
					re = pnpd.create(new JdbcTemplate(), new Object[] {
						id,
						ary[i]
					});
				}
			}
		}
		return re;
	}

    public boolean del(HttpServletRequest request) throws ErrMsgException {
		PaperNoPrefixDb pnp = new PaperNoPrefixDb();
        int id = ParamUtil.getInt(request,"id");
        pnp = pnp.getPaperNoPrefixDb(id);
        
        boolean re = false;
		try {
			re = pnp.del();
	        if (re) {
	        	// 删除部门
	            PaperNoPrefixDeptDb pnpd = new PaperNoPrefixDeptDb();
	            pnpd.delOfPrefix(id);
	            
	            // 删除文号值
	        	PaperNoDb pnd = new PaperNoDb();
	        	pnd.delOfPrefix(id);
	        }			
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(e);
		}

        return re;
    }
    
    public boolean checkExist(String name) {
        boolean re = false;
		String sql = "select * FROM flow_paper_no_prefix where name=" + StrUtil.sqlstr(name);
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator it = null;
		try {
			it = jt.executeQuery(sql);
			if(it.hasNext()){
				re = true;
			}			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return re;
    }
}
