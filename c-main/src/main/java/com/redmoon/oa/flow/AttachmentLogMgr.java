package com.redmoon.oa.flow;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

public class AttachmentLogMgr {
	
	String errMsg = "";
	
	public String getErrMsg(HttpServletRequest request) {
		return errMsg;
	}
	
	/**
	 * 判断能否下载
	 * @param userName
	 * @param flowId
	 * @param attId
	 * @return
	 */
	public boolean canDownload(String userName, long flowId, long attId) {
		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb((int)flowId);
		
		WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
		
		int dc = wpd.getDownloadCount();
		if (dc!=-1) {
			// 取得已下载次数
			int count = AttachmentLogDb.getDownloadCount(userName, attId);
			if ( dc <= count) {
				errMsg = "下载受限，最多只允许下载" + dc + "次";
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 记录对于文件的操作日志
	 * @param userName
	 * @param flowId
	 * @param attId
	 * @param logType
	 * @return
	 */
	public static boolean log(String userName, long flowId, long attId, int logType) {
		AttachmentLogDb ald = new AttachmentLogDb();
		return ald.log(userName, flowId, attId, logType);
	}

}
