package com.redmoon.oa.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeDeptDb;
import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.person.UserDb;

public class NoticeAddAction {
	private String skey = "";
	private String result = "";
	private String title = "";
	private boolean isBold = false;
	private Date beginDate;
	private Date endDate;
	private String content = "";
	private String strusers = "";
	private int isDeptNotice;
	private int isShow;
	private int kind;
	private String depts = "";
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

	public String getDepts() {
		return depts;
	}

	public void setDepts(String depts) {
		this.depts = depts;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getIsShow() {
		return isShow;
	}

	public void setIsShow(int isShow) {
		this.isShow = isShow;
	}

	public int getIsDeptNotice() {
		return isDeptNotice;
	}

	public void setIsDeptNotice(int isDeptNotice) {
		this.isDeptNotice = isDeptNotice;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isBold() {
		return isBold;
	}

	public void setBold(boolean isBold) {
		this.isBold = isBold;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStrusers() {
		return strusers;
	}

	public void setStrusers(String strusers) {
		this.strusers = strusers;
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
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			HttpServletRequest request = ServletActionContext.getRequest();
			String userName = privilege.getUserName(getSkey());
			String unitCode = privilege.getUserUnitCode(getSkey());
			privilege.doLogin(request, getSkey());
			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
			boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
			boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
			if (!isNoticeAll && !isNoticeMgr) {
				json.put("res", "-1");
				json.put("msg", "权限非法");
				setResult(json.toString());
				return "SUCCESS";
			}
			
			UserDb user = new UserDb(userName);
			NoticeDb nd = new NoticeDb();
			NoticeMgr nm = new NoticeMgr();
			long id = (long) SequenceManager.nextID(SequenceManager.OA_NOTICE);
			nd.setUnitCode(unitCode);
			nd.setId(id);
			nd.setTitle(title);
			nd.setContent(content);
			nd.setUserName(userName);
			nd.setCreateDate(new java.util.Date());
			boolean isDn = !pvg.isUserPrivValid(request, "notice") && pvg.isUserPrivValid(request, "notice.dept");
			nd.setIsDeptNotice(isDn?1:0);
			nd.setIsShow(isShow);
			nd.setBeginDate(beginDate);
			nd.setEndDate(endDate);
			nd.setBold(isBold);
			nd.setIsall(2);
			String sql = "insert into oa_notice (id,title,content,user_name,create_date,is_dept_notice,is_show,begin_date,end_date,color,is_bold,unit_code,is_all) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			JdbcTemplate jd = new JdbcTemplate();
			int y = jd.executeUpdate(sql,
					new Object[] { id, nd.getTitle(), nd.getContent(),
							userName, new java.util.Date(),
							nd.getIsDeptNotice(), nd.getIsShow(),
							nd.getBeginDate(), nd.getEndDate(), "",
							isBold ? 1 : 0, user.getUnitCode(), nd.getIsall() });
			if (y > 0) {
				
				FileOutputStream out;
				Calendar cal = Calendar.getInstance();
				String year = "" + (cal.get(Calendar.YEAR));
				String month = "" + (cal.get(Calendar.MONTH) + 1);
				String vpath = "upfile/notice/" + year + "/" + month + "/";
				String filepath = Global.getRealPath() + vpath;
				setSavePath(filepath);
				File file_path = new File(getSavePath());
				if (!file_path.exists()) { // 创建文件夹
					file_path.mkdirs();
				}
				String real_path = file_path.getPath();
				NoticeAttachmentDb nad = new NoticeAttachmentDb();

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
						nad.setVisualPath(vpath);
						nad.setNoticeId(id);
						nad.setName(getUploadFileName()[i]);
						nad.setDiskName(diskName);
						nad.setOrders(k);
						nad.setSize(size);
						nad.create();
						k++;
					}
				}
				boolean isToMobile = com.redmoon.oa.sms.SMSFactory.isUseSMS();
				boolean flag = nm.createNoticeReply(nd, isToMobile);
				if (flag) {
					json.put("res", "0");
					json.put("msg", "添加成功");
				} else {
					json.put("res", "-1");
					json.put("msg", "添加失败");
				}

			} else {
				json.put("res", "-1");
				json.put("msg", "添加失败");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				json.put("res", "-1");
				json.put("msg", e.getMessage());
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e1) {
				Logger.getLogger(NoticeAddAction.class).error(e1.getMessage());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(NoticeAddAction.class).error(e.getMessage());
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Logger.getLogger(NoticeAddAction.class).error(e1.getMessage());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Logger.getLogger(NoticeAddAction.class).error(e1.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
