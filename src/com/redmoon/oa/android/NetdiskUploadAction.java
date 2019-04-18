package com.redmoon.oa.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import cn.js.fan.util.DateUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.netdisk.Attachment;
import com.redmoon.oa.netdisk.Document;
import com.redmoon.oa.netdisk.Leaf;
import com.redmoon.oa.person.UserDb;


public class NetdiskUploadAction {
	private String result = "";
	private String skey = "";
	private String dircode = "";
	
	private String savePath; // 保存路径
	private File[] upload; // 封装文件属性
    private String[] uploadContentType;// 文件类型
    private String[] uploadFileName;// 文件名称
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public String getDircode() {
		return dircode;
	}
	public void setDircode(String dircode) {
		this.dircode = dircode;
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
	
	public String execute() {
		JSONObject json = new JSONObject();

		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if (re) {
			try {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
				
		String userName = privilege.getUserName(getSkey());

		UserDb user = new UserDb();
		user = user.getUserDb(userName);
		String dirCode = "";
        if(getDircode().equals("")){
        	dirCode = userName;
        }else{
        	dirCode = getDircode();
        }
		
		Leaf lf = new Leaf();
		lf = lf.getLeaf(dirCode);
        
		if(lf==null){
			try {
				json.put("res", "-1");
				json.put("msg", "目录不存在");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}       	
        }
		
		Document doc = new Document();
		doc = doc.getDocumentByDirCode(dirCode);
		
			FileOutputStream out;
			try {
				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
				String filepath = cfg.get("file_netdisk")+"/"+lf.getFilePath();
				String path  = Global.getRealPath()+filepath;
				
				File file_path = new File(path);
				if(!file_path.exists()){ //创建文件夹
				    file_path.mkdirs();
				}
				String real_path = file_path.getPath();							
				Attachment att = new Attachment();				
				
				if(this.getUpload()!=null){
					File[] files = getUpload();			
					for (int i = 0 ; i < files.length ; i++){						
						out = new FileOutputStream(real_path + "/" + getUploadFileName()[i]);	
						
						FileInputStream in = new FileInputStream(files[i]);
						int size  = in.available(); //文件大小
						
						byte buffer[] = new byte[1024 * 10];
						int length = 0;
						while ((length = in.read(buffer)) > 0) {
							out.write(buffer, 0, length);
						}
						out.close();
						String fileName = getUploadFileName()[i];
						String ext = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
						att.setExt(ext);
						att.setDocId(doc.getId());
						att.setFullPath(real_path + "/"+ fileName);
						att.setVisualPath(lf.getFilePath());
						att.setName(getUploadFileName()[i]);
						att.setDiskName(getUploadFileName()[i]);
						att.setSize(size);
						att.setPageNum(1);
						att.setUploadDate(new Date());
						att.create();					
					  }			
					json.put("res", "0");
					json.put("msg", "操作成功");		
					json.put("categoryid", String.valueOf(doc.getId()));
					json.put("dircode", dirCode);
					json.put("createdate", DateUtil.format(att.getUploadDate(), "yyyy-MM-dd HH:mm:ss"));
				}	
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		setResult(json.toString());
		return "SUCCESS";
	}
}
