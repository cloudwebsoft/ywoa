package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Vector;

import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.PlanDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;

public class PaperDistributeDb extends QObjectDb {
	/**
	 * 分发给单位
	 */
	public static final int KIND_UNIT = 0;
	/**
	 * 分发给用户
	 */
	public static final int KIND_USER = 1;
	
	public PaperDistributeDb getPaperDistributeDb(long id) {
		return (PaperDistributeDb)getQObjectDb(id);
	}

	public int getCountOfWorkflow(int flowId) {
		String sql = "select count(*) from " + getTable().getName() + " where flow=" + flowId + " order by id asc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getInt(1);
			}			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return 0;
	}
	
    @Override
	public boolean del() throws ResKeyException {
		int kind = getInt("kind");
		MessageDb md = new MessageDb();
		PlanDb pd = new PlanDb();

		if (kind==KIND_UNIT) {
			PaperConfig pc = PaperConfig.getInstance();	
			// 从配置文件中得到收文角色
			String swRoles = pc.getProperty("swRoles");
			String[] aryRole = StrUtil.split(swRoles, ",");
			int aryRoleLen = 0;
			if (aryRole!=null) {
				aryRoleLen = aryRole.length;
			}
			RoleDb[] aryR = new RoleDb[aryRoleLen];
			RoleDb rd = new RoleDb();
			// 取出收文角色
			for (int i=0; i<aryRoleLen; i++) {
				aryR[i] = rd.getRoleDb(aryRole[i]);
			}
			String toUnit = getString("to_unit");
			for (int j=0; j<aryRoleLen; j++) {
				// 取出角色中的全部用户
				for (UserDb user : aryR[j].getAllUserOfRole()) {
					// 如果用户属于收文单位
					if (user.getUnitCode().equals(toUnit)) {
						// 删除相关消息、待办事项
						md.del(user.getName(), MessageDb.ACTION_PAPER_DISTRIBUTE, String.valueOf(getLong("id")));

						// 删除日程安排
						try {
							pd.del(user.getName(), PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE, String.valueOf(getLong("id")));
						} catch (ErrMsgException e) {
							LogUtil.getLog(getClass()).error(e);
						}
					}
				}
			}
		}
		else {
			// 删除相关消息、待办事项
			md.del(getString("to_unit"), MessageDb.ACTION_PAPER_DISTRIBUTE, String.valueOf(getLong("id")));
			
			// 删除日程安排
			try {
				pd.del(getString("to_unit"), PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE, String.valueOf(getLong("id")));
			} catch (ErrMsgException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}

        return super.del();
    }
}
