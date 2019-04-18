package com.redmoon.oa.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.redmoon.oa.fileark.Attachment;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.person.UserDb;

public class FilecaseUploadAction {
	private String title = "";
	private String result = "";
	private String skey = "";
	private String content = "";
	private String dircode = "";

	public String getDircode() {
		return dircode;
	}

	public void setDircode(String dircode) {
		this.dircode = dircode;
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

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	
	private String savePath; // 保存路径
	private File[] upload; // 封装文件属性
    private String[] uploadContentType;// 文件类型
    private String[] uploadFileName;// 文件名称
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
        	dirCode = Leaf.ROOTCODE;
        }else{
        	dirCode = getDircode();
        }
		
		Leaf lf = new Leaf();
		lf = lf.getLeaf(dirCode);

		if(lf==null){
			if (dirCode.equals("camera")) {
		        lf = new Leaf();
		        lf.setName("现场拍照");
		        lf.setCode(dirCode);
		        lf.setParentCode(Leaf.ROOTCODE);
		        lf.setDescription("现场拍照");
		        lf.setType(Leaf.TYPE_LIST);
		        lf.setPluginCode("");
		        lf.setSystem(true);
		        lf.setIsHome(true);
		        String target = "";
		        lf.setTarget(target);
		        lf.setShow(true);
		        lf.setOfficeNTKOShow(false);

		        Leaf rootleaf = lf.getLeaf(Leaf.ROOTCODE);
		        try {
					rootleaf.AddChild(lf);
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					try {
						json.put("res", "-1");
						json.put("msg", "添加目录失败！");
						setResult(json.toString());
						return "SUCCESS";
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			else {
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
        }
			
		LeafPriv lp = new LeafPriv(lf.getCode());
		if (!lp.canUserAppend(userName)) {
			try {
				json.put("res", "-1");
				json.put("msg", "权限非法！");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		Document doc = new Document();
		// doc.setUnitCode(privilege.getUserUnitCode(getSkey()));
		doc.setKeywords(lf.getName());
		doc.setExamine(2);
		re = doc.create(dirCode, getTitle(), getContent(), 0, "", "", userName,
				-1, user.getRealName());		
				
		if (re) {			
			FileOutputStream out;
			try {
				Calendar cal = Calendar.getInstance();
				String year = "" + (cal.get(cal.YEAR));
				String month = "" + (cal.get(cal.MONTH) + 1);
				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
				String filepath = cfg.get("file_folder") + "/" + year + "/" + month;
				String path  = Global.getRealPath()+ filepath;
				setSavePath(path);
				
				File file_path = new File(getSavePath());
				if(!file_path.exists()){ //创建文件夹
				    file_path.mkdirs();
				}
				String real_path = file_path.getPath();				
				Attachment att = new Attachment();

				if(this.getUpload()!=null){
					File[] files = getUpload();			
					for (int i = 0 ; i < files.length ; i++){						
						out = new FileOutputStream(real_path + File.separator + getUploadFileName()[i]);	
						
						FileInputStream in = new FileInputStream(files[i]);
						int size  = in.available(); //文件大小
						byte buffer[] = new byte[1024 * 10];
						int length = 0;
						while ((length = in.read(buffer)) > 0) {
							out.write(buffer, 0, length);
						}
						out.close();
						
						att.setDocId(doc.getId());
						att.setFullPath(getSavePath() + File.separator + getUploadFileName()[i]);
						att.setVisualPath(filepath);
						att.setSize(size);
						att.setName(getUploadFileName()[i]);
						att.setDiskName(getUploadFileName()[i]);
						att.setPageNum(1);
						att.setUploadDate(new Date());
						att.create();
					  }
				}	
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				json.put("res", "0");
				json.put("msg", "操作成功");
				json.put("id", doc.getId());
				json.put("title", getTitle());
				json.put("content", getContent());
				json.put("createdate", doc.getCreateDate());

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setResult(json.toString());
		return "SUCCESS";
	}

}
