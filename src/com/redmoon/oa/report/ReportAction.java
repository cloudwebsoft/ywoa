package com.redmoon.oa.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.db.SequenceManager;


public class ReportAction extends ActionSupport {
		private File upload;
		private String uploadFileName;       //上传文件名
		private String uploadContentType;
		private String description;		// 报表功能描述
		private String message;			// json调用信息
		private String priv_code;		// 权限编码
		private String priv_desc;   	// 权限描述
		private String username;   		//发布人用户名
		private String downloadFileName;     //下载文件名
		private int id;					// 修改时传入的id
		
		protected static Logger logger = Logger.getLogger(ReportAction.class);
		public File getUpload() {
			return upload;
		}
		public void setUpload(File upload) {
			this.upload = upload;
		}
		public String getUploadFileName() {
			return uploadFileName;
		}
		public void setUploadFileName(String uploadFileName) {
			this.uploadFileName = uploadFileName;
		}
		public String getUploadContentType() {
			return uploadContentType;
		}
		public void setUploadContentType(String uploadContentType) {
			this.uploadContentType = uploadContentType;
		}
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		
		/**
		 * 上传文件错误执行动作
		 */
		@Override
		public void addActionError(String anErrorMessage) {
			// TODO Auto-generated method stub
			if (anErrorMessage.startsWith("the request was rejected because its size")){
				ServletActionContext.getRequest().setAttribute("info", "您上传的文件超出最大值（2G）！");
				super.addActionError(anErrorMessage);
			} else {
			super.addActionError(anErrorMessage);
			}
		}
		/**
		 * 增加文件
		 * @return
		 */
		public String create(){
			if (upload!=null){
				
				ReportManageDb rmDb = new ReportManageDb();
				int key =new Long(SequenceManager.nextID(SequenceManager.OA_REPORT_MANAGE)).intValue();    
				String subName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
				String fileType = subName.substring(subName.indexOf(".")+1);
				if (!"raq".equals(fileType)){
					this.message = "上传文件类型错误，仅支持上传(*.raq)格式!";
					return "create";
				}
				String savePath = "/reportFiles";      //上传文件存储路径
				//String realPath = ServletActionContext.getServletContext().getRealPath(savePath);
				String realPath = Global.getRealPath() + savePath;
				//String report_date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				//String alter_date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				Date date = new Date();
				String sql = "select id from report_manage where name="+ StrUtil.sqlstr(subName);
				Vector v = rmDb.list(sql);
				if (v!=null&&v.size()!=0){
					this.message = "文件名重复，请重命名。";
					return "create";
				} else {
					try {
						if (rmDb.create(new JdbcTemplate(), new Object[]{key,subName,savePath,description,date,priv_code,priv_desc,username,date})){
							File saveFile = new File(realPath,subName);
							FileUtils.copyFile(upload, saveFile);
							this.message = "上传成功！";
							};
						} catch (Exception e) {
							// TODO: handle exception
							this.message="上传失败！";
							logger.info("报表上传失败！");
					}
					
				}
			} else {
				this.message = "请选择上传文件！";
			}
			return "create";
			
		}
		/**
		 * 修改
		 * @return
		 */
		public String edit(){
			ReportManageDb rmDb = new ReportManageDb();
			rmDb = (ReportManageDb)rmDb.getQObjectDb(id);
			//String alter_date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			Date date = new Date();
			if(upload==null){
				rmDb.set("priv_code",priv_code);
				rmDb.set("priv_desc",priv_desc);
				rmDb.set("description",description);
				rmDb.set("alter_date",date);
				try {
					rmDb.save();
					this.message = "修改成功！";
				} catch (Exception e) {
					// TODO: handle exception
					this.message = "修改失败！";
					logger.info("文件修改出现异常，保存失败");
				}
				
			} else {
				String oldName = rmDb.getString("name");
				String savePath = "/reportFiles";      //上传文件存储路径
				String realPath = ServletActionContext.getServletContext().getRealPath(savePath);
				File saveFile = new File(realPath,oldName);     //旧文件路径
				if (saveFile.exists())
					saveFile.delete();
				String subName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
				String fileType = subName.substring(subName.indexOf(".")+1);
				if (!"raq".equals(fileType)){
					this.message = "上传文件类型错误，仅支持上传(*.raq)格式!";
					return "edit";
				}
				String sql = "select id from report_manage where name="+ StrUtil.sqlstr(subName)+" and id != "+id;
				Vector v = rmDb.list(sql);
				if (v!=null&&v.size()!=0){
					this.message = "文件名重复，请重命名。";
					return "edit";
				} else {
					rmDb.set("name", subName);
					rmDb.set("priv_code", priv_code);
					rmDb.set("priv_desc", priv_desc);
					rmDb.set("description", description);
					rmDb.set("alter_date",date);
					try {
						rmDb.save();
						File sFile = new File(realPath,subName);    //新上传文件路径
						FileUtils.copyFile(upload, sFile);
						this.message = "修改成功！";
					} catch (Exception e) {
						// TODO: handle exception
						this.message = "修改失败！";
						logger.info("文件修改出现异常，保存失败");
					}
				}
					
			}
			return "edit";
		}
		/**
		 * 得到下载文件输入流
		 * @return
		 */
		public InputStream getInputStream(){
			InputStream is = null;
			try {
				String str = new String(downloadFileName.getBytes("iso-8859-1"),"utf-8");
				String downPath = ServletActionContext.getServletContext().getRealPath("/reportFiles");
				File downFile = new File(downPath,str);
				is  = new FileInputStream(downFile);
				this.downloadFileName = URLEncoder.encode(str,"utf-8");
			} catch (Exception e) {
				// TODO: handle exception
				logger.info("获取文件输入流异常！");
			}
			
			return is;
			
		}
		/**
		 * 文件下载执行动作
		 * @return
		 */
		public String download(){
			return "download";
		}
		public String getPriv_code() {
			return priv_code;
		}
		public void setPriv_code(String priv_code) {
			this.priv_code = priv_code;
		}
		public String getPriv_desc() {
			return priv_desc;
		}
		public void setPriv_desc(String priv_desc) {
			this.priv_desc = priv_desc;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getDownloadFileName() {
			return downloadFileName;
		}
		public void setDownloadFileName(String downloadFileName) {
			this.downloadFileName = downloadFileName;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		
		
		
}
