package com.redmoon.oa.android.work;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.worklog.WorkLogAttachmentDb;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.WorkLogForModuleMgr;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-15下午04:41:52
 */
public class AddWorkAction extends BaseAction{
	private String skey;//用户名
	private String content;
	private int type;
	private String savePath; // 保存路径
	private File[] upload; // 封装文件属性
	private String[] uploadContentType;// 文件类型
	private String[] uploadFileName;// 文件名称
	
	public String getSavePath() {
		return savePath;
	}


	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}


	public File[] getUpload() {
		return upload;
	}


	public void setUpload(File[] upload) {
		this.upload = upload;
	}


	public String[] getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String[] uploadContentType) {
		this.uploadContentType = uploadContentType;
	}


	public String[] getUploadFileName() {
		return uploadFileName;
	}


	public void setUploadFileName(String[] uploadFileName) {
		this.uploadFileName = uploadFileName;
	}


	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}


	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}


	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}


	/**
	 * @return the skey
	 */
	public String getSkey() {
		return skey;
	}


	/**
	 * @param skey the skey to set
	 */
	public void setSkey(String skey) {
		this.skey = skey;
	}

	

	@Override
	public void executeAction() {
		// TODO Auto-generated method stub
		super.executeAction();
		com.redmoon.oa.android.Privilege mPriv = new com.redmoon.oa.android.Privilege();
		boolean re = mPriv.Auth(getSkey());
		HttpServletRequest request = ServletActionContext.getRequest();
		
		try {
			jReturn.put(RES, String.valueOf(RESULT_SUCCESS));
			if(re){
				jResult.put(RETURNCODE, RESULT_TIME_OUT);
				jReturn.put(RESULT, jResult);
				return;
			}
			mPriv.doLogin(request, skey);
			JSONObject jRes = insertWorkLog(request);
			jReturn.put(RESULT, jRes);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		}
		
	} 
	
	public JSONObject insertWorkLog(HttpServletRequest request) {
		JSONObject jRes = new JSONObject();
		try {
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			String myDate = DateUtil.format(new Date(), "yyyy-MM-dd")+" 00:00:00";
			Privilege mPriv = new Privilege();
			String userName = mPriv.getUserName(skey);
			int id = (int) SequenceManager.nextID(SequenceManager.OA_WORK_LOG);
			int log_item = 0;
			int log_year = 0;
			WorkLog workLog = new WorkLog();
			workLog.setUserName(userName);
			if(type == WorkLogDb.TYPE_NORMAL){
				
			}else if(type == WorkLogDb.TYPE_WEEK){
				log_item = DateUtil.getWeekOfYear(new Date());
				log_year = DateUtil.getYear(new Date());
			}else{
			
				log_item = DateUtil.getMonth(new Date())+1;
				log_year = DateUtil.getYear(new Date());
			}
			workLog.setType(String.valueOf(type));
			workLog.setLogItem(log_item);
			workLog.setLogYear(log_year);
			workLog.setContent(content);
			workLog.setMyDate(myDate);
			workLog.setId(id);
			boolean isDateExit = isExit(workLog);
			if(isDateExit){
				jRes.put(RETURNCODE, RESULT_DATE_ISEXISTS); //日报已经存在
				return jRes;
			}
			boolean isSuccess = mwms.createMyWorkLog(workLog);				
			if (isSuccess) {
				FileOutputStream out;
				Calendar cal = Calendar.getInstance();
				String year = "" + (cal.get(Calendar.YEAR));
				String month = "" + (cal.get(Calendar.MONTH) + 1);
				String vpath = "upfile/workLog/" + year + "/" + month + "/";
				String filepath = Global.getRealPath() + vpath;
				setSavePath(filepath);
				File file_path = new File(getSavePath());
				if (!file_path.exists()) { // 创建文件夹
					file_path.mkdirs();
				}
				String real_path = file_path.getPath();
				WorkLogAttachmentDb wad = new WorkLogAttachmentDb();

				if (this.getUpload() != null) {
					File[] files = getUpload();
					int k = 0;
					for (int i = 0; i < files.length; i++) {
						String diskName = FileUpload.getRandName() + "."
								+ StrUtil.getFileExt(getUploadFileName()[i]);
						out = new FileOutputStream(real_path + File.separator
								+ diskName);
						FileInputStream in = new FileInputStream(files[i]);
						int size = in.available(); // 文件大小
						byte buffer[] = new byte[1024 * 10];
						int length = 0;
						while ((length = in.read(buffer)) > 0) {
							out.write(buffer, 0, length);
						}
						out.close();
						wad.setVisualPath(vpath);
						wad.setWorkLogId(id);
						wad.setName(getUploadFileName()[i]);
						wad.setDiskName(diskName);
						wad.setOrders(k);
						wad.setSize(size);
						wad.create();
						k++;
					}
				}
				jRes.put(RETURNCODE, RETURNCODE_SUCCESS);
			}else{
				jRes.put(RETURNCODE, RESULT_INSERT_FAIL);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName() + "==="
					+ e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName() + "==="
					+ e.getMessage());
		}
		return jRes;
	}
	
	/**
	 * 判断是否存在
	 * @Description: 
	 * @param workLog
	 * @return
	 */
	public static boolean isExit(WorkLog workLog){
		boolean flag = false;
		int type = Integer.parseInt(workLog.getType());
		StringBuilder sb = new StringBuilder();
		sb.append("select count(id) from work_log where userName =").append(StrUtil.sqlstr(workLog.getUserName())).append(" and log_type= ").append(type);
		if(type == WorkLogDb.TYPE_NORMAL){
			sb.append(" and myDate =").append(SQLFilter.getDateStr(workLog.getMyDate(), "yyyy-MM-dd HH:mm:ss"));
		}else if(type == WorkLogDb.TYPE_MONTH){
			sb.append(" and log_item = ").append(workLog.getLogItem()).append(" and log_year = ").append(workLog.getLogYear());
			
		}else{
			sb.append(" and log_item = ").append(workLog.getLogItem()).append(" and log_year = ").append(workLog.getLogYear());
		}
		JdbcTemplate jt = null;
		jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sb.toString());
			while(ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				int result = rr.getInt(1);
				if(result>0){
					flag = true;
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(BaseAction.class.getName()).error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		}
		return flag;
		
	}

	
}
