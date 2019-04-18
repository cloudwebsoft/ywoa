package com.redmoon.oa.android.i;

import java.io.*;
import java.sql.SQLException;
import org.json.*;
import java.util.*;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;


 /**
 * @Description: 上传头像接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class UploadHeadImageAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //上传成功
	
	private String skey = "";
	private	File image ;
	private	String imageType = ".jpg" ;
	private String result = "";
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		boolean flag = true;
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(skey);
		
		if(re){
			try {
				jReturn.put("res",RES_EXPIRED);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			String imageSavePath = Global.getAppPath() + "public/users/";
			//System.out.println(imageSavePath);

			File file = new File(imageSavePath);
			if (!file.exists()) {
				file.mkdir();
			}
			
			String now = System.currentTimeMillis() + "" ;
			String imageName = now.substring(4) + new Random().nextInt(10);
			
			String imageFullPath = imageSavePath + imageName + imageType;
			String imageUrl = "public/users/" +  imageName + imageType;

			fos = new FileOutputStream(imageFullPath);
			fis = new FileInputStream(image);
			
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			
			fos.close();
			fis.close();
			
			String sql = "update users set photo=? where name=?";
		
			JdbcTemplate jt = new JdbcTemplate();
			int n = jt.executeUpdate(sql, new Object[] { imageUrl, privilege.getUserName(skey)});
			
			if (n == 1) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS);
				jResult.put("imageUrl", imageUrl);
				jReturn.put("result", jResult);
			}
		} catch (FileNotFoundException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}catch (Exception e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					System.out.println("FileInputStream关闭失败");
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
					fis = null;
				} catch (IOException e) {
					System.out.println("FileOutputStream关闭失败");
					e.printStackTrace();
				}
			}
			if(!flag){
				try {
					jReturn.put("res", RES_FAIL);
					jResult.put("returnCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
		}

		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
