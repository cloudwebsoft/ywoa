package com.redmoon.oa.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.notice.NoticeReplyMgr;
import com.redmoon.oa.person.UserDb;

public class NoticeEditAction {
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
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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
			privilege.doLogin(request,skey);
			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
			
			String myUnitCode = pvg.getUserUnitCode(request);
			boolean isUserPrivValid = false;		
			NoticeMgr nMgr = new NoticeMgr();
			NoticeDb nd = new NoticeDb(id);
			if (nMgr.canEditNotice(id,userName) || (pvg.isUserPrivValid(request, "notice") && myUnitCode.equals(DeptDb.ROOTCODE))) {
				isUserPrivValid = true;
			} else {
				
				if ((nd.getIsDeptNotice() == 0 && pvg.isUserPrivValid(request, "notice")) 
						|| (nd.getIsDeptNotice() == 1 && pvg.isUserPrivValid(request, "notice.dept"))) {
					if (myUnitCode.equals(nd.getUnitCode())) {
						isUserPrivValid = true;
					}
				}
			}
			
			if (!isUserPrivValid) {
				json.put("res", "-1");
				json.put("msg", "权限非法");
				setResult(json.toString());
				return "SUCCESS";
			}
			UserDb user = new UserDb();
			user = user.getUserDb(userName);
			if (id == 0) {
				json.put("res", "-1");
				json.put("msg", "通知公告不存在！");
				setResult(json.toString());
				return "SUCCESS";
			}
			json.put("res", "-1");
			json.put("msg", "修改失败");
			
			java.util.Date rDate = new java.util.Date();
			NoticeReplyDb nrdb = new NoticeReplyDb();
			NoticeReplyMgr replyMgr = new NoticeReplyMgr();
			if(!replyMgr.readStatusByReply(id, userName)){
				nrdb.setIsReaded("1");
				nrdb.setReadTime(rDate);
				nrdb.setNoticeid((long) id);
				nrdb.setUsername(userName);
				nrdb.saveStatus();
			}
			// 更新回复表
			
			// 更新主表字段
			nd.setTitle(title);
			nd.setBold(isBold);
			String usersKnow = nd.getUsersKnow();
			if (usersKnow.equals("")) {
				usersKnow = userName;
			} else {
				// 检查用户是否已被记录
				String[] ary = usersKnow.split(",");
				boolean isFound = false;
				int len = ary.length;
				for (int i = 0; i < len; i++) {
					if (userName.equals(ary[i])) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					usersKnow += "," + userName;
				}
			}
			nd.setUsersKnow(usersKnow);
			nd.setContent(content);
			boolean flag = nd.save();
			if (flag) {
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
						nad.setNoticeId(getId());
						nad.setName(getUploadFileName()[i]);
						nad.setDiskName(diskName);
						nad.setOrders(k);
						nad.setSize(size);
						re = nad.create();
						k++;
					}

				}
				json.put("res", "0");
				json.put("msg", "修改成功");
			} 
		} catch (JSONException e) {
			Logger.getLogger(NoticeEditAction.class).error(e.getMessage());
		} catch (FileNotFoundException e1) {
			Logger.getLogger(NoticeEditAction.class).error(e1.getMessage());
		} catch (IOException e1) {
			Logger.getLogger(NoticeEditAction.class).error(e1.getMessage());
		}finally{
			setResult(json.toString());
		}
		return "SUCCESS";
	}
}
