package com.redmoon.oa.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;

import java.util.Iterator;
import java.util.Vector;

/**
 * @Description: 将用户表duty字段作为用户角色序号的同步字段,便于排序
 * @author: 古月圣
 * @Date: 2015-12-3上午09:32:46
 */
public class SetUserDutyJob implements Job {
	public SetUserDutyJob() {
	}

	/**
	 * @Description:
	 * @param jobExecutionContext
	 * @throws JobExecutionException
	 */
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		executeJob();
	}

	public void executeJob() {
		UserDb ud = new UserDb();
		Vector v = ud.list();
		Iterator it = v.iterator();
		while (it.hasNext()) {
			UserDb user = (UserDb) it.next();
			RoleDb[] roles = user.getRoles();
			int order = 0;
			for (RoleDb role : roles) {
				if (role.getOrders() > order) {
					order = role.getOrders();
				}
			}
			user.setDuty(order + "");
			user.save();
		}
	}
}
