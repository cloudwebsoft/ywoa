/**
 * 手动更新系统action
 */
package com.redmoon.oa.manuallyUpdate;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.manuallyUpdate.service.ManuallyUpdateService;
import com.redmoon.oa.upgrade.service.SpringHelper;

public class ManuallyUpdateAction extends ActionSupport{
	Logger logger = Logger.getLogger(ManuallyUpdateAction.class.getName());
	private FileUpload fileUpload;
	private File upload;
	private String uploadFileName;       //上传文件名
	private String uploadContentType;
	private String message;			// json调用信息
	
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

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

	public FileUpload getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}

	/**
	 * 更新系统
	 * 
	 * @param application
	 * @param request
	 * @return
	 */
	public String  manuallyUpdate() {
		boolean flag = true;
		//String path = ServletActionContext.getServletContext().getRealPath(
		//		"FileUploadTmp");
		String path = Global.getRealPath() + "FileUploadTmp" ;
		String subName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
		String fileType = subName.substring(subName.lastIndexOf(".")+1);
		File targetFile = new File(path, uploadFileName);

		// 保存
		try {
			FileUtils.copyFile(upload, targetFile);
			
		} catch (Exception e) {
			logger.debug("保存文件错误：" + e.getMessage());
			this.message = "上传文件错误!";
			flag = false;
		}

		
		// 升级包格式必须为zip格式
		if (flag && fileType != null && !fileType.equals("zip")) {
			logger.debug("文件类型错误,只支持zip格式升级包!");
			this.message = "上传文件类型错误，仅支持上传(*.zip)格式!";
			flag = false;
			
		}
		// 升级包名称校验，升级包名称为sp_+版本号
		if (flag && subName != null && !subName.startsWith("sp_")) {
			logger.debug("升级包命名不符合规范（sp_+版本号）!");
			this.message = "升级包命名不符合规范（sp_+版本号）!";
			flag = false;
		}
		// 校验版本号 版本号：大版本号.小版本号.补丁号
		String[] tempStrs = subName.split("_");
		if (flag && tempStrs.length != 2
				&& !tempStrs[1].matches("[0-9]+\\.[0-9]+\\.[0-9]+")) {
			logger.debug("升级包命名不符合规范（sp_+版本号）,版本号规定错误，版本号：大版本号.小版本号.补丁号!");
			this.message = "升级包命名不符合规范（sp_+版本号）,版本号规定错误，版本号：大版本号.小版本号.补丁号!";
			flag = false;
		}
		//校验通过，执行更新
		if (flag)
		{
			try
			{
				boolean updateFlag = SpringHelper.getBean(ManuallyUpdateService.class).manuallyUpdate(targetFile);
				if (updateFlag)
				{
					this.message = "更新成功,请等待Tomcat自动重启,或者手工重启Tomcat！";
				}
				else
				{
					this.message = "更新失败！";
					flag = false;
				}
				
			}
			catch(ErrMsgException eer){
				this.message = eer.getMessage() ;
				LogUtil.getLog(getClass()).error("更新出错：" + StrUtil.trace(eer));
				flag = false;
			}
			catch(Exception ex)
			{
				this.message = "更新失败！" ;
				LogUtil.getLog(getClass()).error("更新出错：" + StrUtil.trace(ex));
				flag = false;
			}
			finally
			{
				try {
					FileUtils.cleanDirectory(new File(path));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.debug("删除文件失败!");
				}
			}
		}
		
		
		
        return SUCCESS;
	}
	
	

}
