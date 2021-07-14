package com.redmoon.t;

import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ResKeyException;

public class StatJob implements Job {
	public StatJob() {
	}

	/**
	 * execute
	 *
	 * @param jobExecutionContext JobExecutionContext
	 * @throws JobExecutionException
	 * @todo Implement this org.quartz.Job method
	 */
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
		
		// 计算一周内回复最多的贴子,计算前200个
		TMsgDb tmd = new TMsgDb();
		String sql = "select reply_id, count(*) as c from " + tmd.getTable().getName() + " where create_date>? and reply_id<>0 group by reply_id order by c desc";

		Config cfg = Config.getInstance();
		int statHotMsgInDays = cfg.getIntProperty("t.statHotMsgInDays"); // 多少天内
		java.util.Date beginDate = DateUtil.addDate(new java.util.Date(), -statHotMsgInDays);
		int statHotMsgCount = cfg.getIntProperty("t.statHotMsgCount");

		TStatDb tsd = new TStatDb();

		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[] { beginDate }, 1, statHotMsgCount);
			String content = "", content2 = "";
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				long reply_id = rr.getLong(1);
				if (content.equals("")) {
					content = "" + reply_id;
					content2 = "" + rr.getInt(2);
				}
				else {
					content += "," + reply_id;
					content2 += "," + rr.getInt(2);
				}
			}

			tsd = (TStatDb) tsd.getQObjectDb(TStatDb.CODE_HOT_MSG);
			tsd.set("content", content);
			tsd.set("content2", content2);
			tsd.save();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			e.printStackTrace();
		}

		// 计算一周内微博发贴及被回贴最多的微博,计算前200名
		int statHotTInDays = cfg.getIntProperty("t.statHotTInDays"); // 多少天内	        
		sql = "select t_id, count(*) as c from " + tmd.getTable().getName() + " where create_date>? group by t_id order by c desc";
		beginDate = DateUtil.addDate(new java.util.Date(), -statHotTInDays);
		int statHotTCount = cfg.getIntProperty("t.statHotTCount");
		try {
			ri = jt.executeQuery(sql, new Object[] { beginDate }, 1, statHotTCount);
			String content = "";
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				long t_id = rr.getLong("t_id");
				if (content.equals(""))
					content = "" + t_id;
				else
					content += "," + t_id;
			}
			tsd = (TStatDb) tsd.getQObjectDb(TStatDb.CODE_HOT_T);
			tsd.set("content", content);
			tsd.save();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ResKeyException e) {
			e.printStackTrace();
		}

	}
}
