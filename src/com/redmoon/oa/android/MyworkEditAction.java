package com.redmoon.oa.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.work.LeaderViewWorkAction;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.worklog.WorkLogAttachmentDb;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.WorkLogForModuleMgr;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

public class MyworkEditAction {
	public Logger logger = Logger.getLogger(MyworkEditAction.class.getName());
	private String skey = "";
	private String result = "";
	private int id;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String content = "";

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "服务器忙,请求失败！");
				setResult(json.toString());
				return "SUCCESS";
			}
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, skey);
			WorkLogDb wld = new WorkLogDb();
			wld = wld.getWorkLogDb(getId());
			int logType = wld.getLogType();
			boolean canEditPreviousWorklog = com.redmoon.oa.worklog.Config
					.getInstance().getBooleanProperty("canEditPreviousWorklog");
			int dayLimit = com.redmoon.oa.worklog.Config.getInstance()
					.getIntProperty("editPreviousWorklogLimit");
			int dataDiff = DateUtil.datediff(new Date(), wld.getMyDate());
			if (!canEditPreviousWorklog) {
				json.put("res", "-1");
				json.put("msg", "不能修改工作汇报！");
				setResult(json.toString());
				return "SUCCESS";
			}
			if (logType == WorkLogDb.TYPE_NORMAL) {
				if (dataDiff > dayLimit) {
					json.put("res", "-1");
					json.put("msg", "工作报告超过" + dayLimit + "天后无法修改！");
					setResult(json.toString());
					return "SUCCESS";

				}
			}

			wld.setContent(getContent());
			re = wld.save();
			

			if (re) {
				re = uploadAttach(request);
				if(re){
					json.put("res", "0");
					json.put("msg", "编辑成功");
				}else{
					json.put("res", "-1");
					json.put("msg", "编辑成功");
				}
			} else {
				json.put("res", "-1");
				json.put("msg", "编辑失败");
			}

			// jReturn.put(RESULT, jRes);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(MyworkEditAction.class.getName() + ":"
					+ e.getMessage());
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			logger.error(MyworkEditAction.class.getName() + ":"
					+ e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}

	public boolean uploadAttach(HttpServletRequest request) {
		boolean re = true;
		FileOutputStream out = null;
		FileInputStream in = null;
		try {
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
					in = new FileInputStream(files[i]);
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
					re &= wad.create();
					k++;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName() + "==="
					+ e.getMessage());
		} 
		return re;
	}

}
