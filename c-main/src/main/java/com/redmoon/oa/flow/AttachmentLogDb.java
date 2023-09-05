package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class AttachmentLogDb extends QObjectDb {
	public static final int TYPE_DOWNLOAD = 0;
	public static final int TYPE_EDIT = 1;
	
	public static String getTypeDesc(int type) {
		if (type==TYPE_DOWNLOAD) {
			return "下载";
		}
		else {
			return "修改";
		}
	}
	
	public static int getDownloadCount(String userName, long attId) {
		String sql = "select count(*) from flow_document_attach_log where log_type=" + TYPE_DOWNLOAD + " and att_id=? and user_name=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{attId, userName});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getInt(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(AttachmentLogDb.class).error(e);
		}

		return 0;
	}
	
	public boolean log(String userName, long flowId, long attId, int logType) {
		boolean re = false;
		try {
			re = create(new JdbcTemplate(), new Object[]{userName,flowId,attId,logType,new Date()});
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(e);
		} 
		return re;
	}
	
	/**
	 * 查询
	 * @param request
	 * @param flowId
	 * @param attId
	 * @return
	 */
	public String getQuery(HttpServletRequest request, long flowId, long attId) {
		String orderBy = ParamUtil.get(request, "orderBy");
		String sort = ParamUtil.get(request, "sort");

		if (orderBy.equals("")) {
			orderBy = "id";
		}

		if (sort.equals(""))
			sort = "desc";			
		
		if ("logTime".equals(orderBy)) {
			orderBy = "log_time";
		}
		else if ("realName".equals(orderBy)) {
			orderBy = "user_name";
		}
		else if ("logType".equals(orderBy)) {
			orderBy = "log_type";
		}
		else if ("attName".equals(orderBy)) {
			orderBy = "att_id";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append( "select id from flow_document_attach_log where att_id=" + attId );
		
		String op = ParamUtil.get(request, "op");
		if ("search".equals(op)) {
			String userName = ParamUtil.get(request, "userName");
			String logTimeBegin = ParamUtil.get(request, "logTimeBegin");
			String logTimeEnd = ParamUtil.get(request, "logTimeEnd");
			if (!"".equals(userName)) {
				sb = new StringBuffer();
				sb.append("select l.id from flow_document_attach_log l, users u where u.name=l.user_name and att_id=" + attId);
				sb.append(" and u.realname like " + StrUtil.sqlstr("%" + userName + "%"));
			}
			if (!"".equals(logTimeBegin)) {
				sb.append(" and log_time>=" + SQLFilter.getDateStr(logTimeBegin, "yyyy-MM-dd"));
			}
			if (!"".equals(logTimeEnd)) {
				Date d = DateUtil.parse(logTimeEnd, "yyyy-MM-dd");
				if (d!=null) {
					// 结束日期需加1，以符合日常习惯
					d = DateUtil.addDate(d, 1);
					String strDate = DateUtil.format(d, "yyyy-MM-dd");
					sb.append(" and log_time<" + SQLFilter.getDateStr(strDate, "yyyy-MM-dd"));
				}
			}			
		}
		
		sb.append( " order by " + orderBy + " " + sort );
		return sb.toString();
	}	
}
