package com.redmoon.oa.workplan;

import java.io.File;
import java.io.IOException;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.mpp.MPPReader;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cn.js.fan.web.Global;

import com.opensymphony.xwork2.ActionSupport;

public class ProjectImportAction extends ActionSupport {
	protected static Logger logger = Logger.getLogger(ProjectImportAction.class);
	 //上传文件名
	private File upload;
	 //上传文件
	private String uploadFileName; 
	//上传文件类型
	private String uploadContentType;
    //项目编号
    private int id;
    // json调用信息
    private String message;	
    
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * 项目导入
	 * @return
	 */
	public String importProject()
    {
    	if (upload!=null){
    		String subName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
			String fileType = subName.substring(subName.indexOf(".")+1);
			if (!"mpp".equals(fileType)){
				this.message = "导入文件类型错误，仅支持导入(*.mpp)格式!";
				return SUCCESS;
			}
			String savePath = "/upfile";      //上传文件存储路径
			//文件路径修改为公用路径
			//String realPath = ServletActionContext.getServletContext().getRealPath(savePath);
			String realPath = Global.getRealPath() + savePath;
			File saveFile = new File(realPath,subName);
			try {
				FileUtils.copyFile(upload, saveFile);
				MPPReader mppRead = new MPPReader();
			
				ProjectFile pf = mppRead.read(saveFile);
				
				// 创建根任务
				WorkPlanDb wpd = new WorkPlanDb();
				wpd = wpd.getWorkPlanDb(id);
				WorkPlanTaskDb wptd = new WorkPlanTaskDb();
				
				
	
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				this.message="导入失败！";
				logger.info("项目导入失败,文件上传失败！");
				
			}
			catch (MPXJException e) {
				// TODO Auto-generated catch block
				// TODO Auto-generated catch block
				this.message="导入失败！";
				logger.info("项目导入失败，文件读取失败！");
			}
			finally
			{
				//删除上传文件
				delFile(saveFile);
			}
			this.message = "导入成功！";
			
    	}
    	return SUCCESS;
    }
	/**
	 * 删除文件
	 * @param file
	 */
	private void delFile(File file)
	{
		if (file.exists())
		{
			file.delete();
		}
	}
    
    

}
