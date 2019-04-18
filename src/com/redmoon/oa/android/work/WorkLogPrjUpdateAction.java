package com.redmoon.oa.android.work;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.NoticeAddAction;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.android.sales.SalesContractListAction;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.prj.PrjConfig;
import com.redmoon.oa.worklog.WorkLogAttachmentDb;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.WorkLogForModuleMgr;
import com.redmoon.oa.worklog.WorkLogMgr;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

public class WorkLogPrjUpdateAction extends BaseAction {
	private String skey;
	private int workLogType;
	private int prjTaskType;
	private int progress;
	private String content;
	private int ptId;
	private int workLogId;
	private String savePath; // 保存路径
	private File[] upload; // 封装文件属性
	private String[] uploadContentType;// 文件类型
	private String[] uploadFileName;// 文件名称
	
	
	

	/**
	 * @return the workLogId
	 */
	public int getWorkLogId() {
		return workLogId;
	}

	/**
	 * @param workLogId the workLogId to set
	 */
	public void setWorkLogId(int workLogId) {
		this.workLogId = workLogId;
	}

	/**
	 * @return the workLogType
	 */
	public int getWorkLogType() {
		return workLogType;
	}

	/**
	 * @param workLogType the workLogType to set
	 */
	public void setWorkLogType(int workLogType) {
		this.workLogType = workLogType;
	}

	/**
	 * @return the prjTaskType
	 */
	public int getPrjTaskType() {
		return prjTaskType;
	}

	/**
	 * @param prjTaskType the prjTaskType to set
	 */
	public void setPrjTaskType(int prjTaskType) {
		this.prjTaskType = prjTaskType;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getPtId() {
		return ptId;
	}

	public void setPtId(int ptId) {
		this.ptId = ptId;
	}

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

	@Override
	public void executeAction() {
		// TODO Auto-generated method stub
		super.executeAction();
		Privilege mPriv = new Privilege();
		boolean re = mPriv.Auth(getSkey());
		HttpServletRequest request = ServletActionContext.getRequest();

		try {
			jReturn.put(RES, RESULT_SUCCESS);
			if (re) {
				jResult.put(RETURNCODE, RESULT_TIME_OUT);
				jReturn.put(RESULT, jResult);
				return;
			} 
			mPriv.doLogin(request, skey);
			JSONObject jRes = updateWorkLog(request,workLogType,prjTaskType);
			jReturn.put(RESULT, jRes);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName() + "==="
					+ e.getMessage());
		}
	}

	public JSONObject updateWorkLog(HttpServletRequest request,int workType,int ptType) {
		JSONObject jRes = new JSONObject();
		try {
			
			
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			WorkLog workLog = mwms.getWorkLogInfoById(workLogId);
			String formCode = PrjConfig.CODE_PRJ;
    		if(ptType == WOKR_lOG_TASK_TYPE){
    			formCode = PrjConfig.CODE_TASK;
    		}
    		workLog.setFormCode(formCode);
    		workLog.setPrjId(ptId);
    		workLog.setProcess(progress);
    		workLog.setContent(content);	
			boolean isSuccess = mwms.saveMyWorkLog(workLog);	
			
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
						wad.setWorkLogId(workLogId);
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
	
	
}
