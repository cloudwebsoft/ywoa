package com.redmoon.oa.android;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.db.DataSource;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.mobileskins.MobileSkinsDb;

public class MobileSkinDetailInfoAction {
	private String result = "";
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		JSONObject json = new JSONObject();		
		MobileSkinsDb skinDb = new MobileSkinsDb();
	    String skinsDetail = "select code,version,name,is_used,visual_path,disk_name,modify_date,user_name from mobile_skins where is_used=1";
		ResultRecord record = skinDb.getIsUsedInfoDetail(skinsDetail);
		JSONObject skins = new JSONObject();
		try {
			License license = License.getInstance();
			String type = license.getType();
			if(type!=null && !type.trim().equals("")){
				if(type.equals(License.TYPE_BIZ) || type.equals(License.TYPE_SRC) || type.equals(License.TYPE_OEM)){
					if( skinDb.getCountInfo()){
						skins.put("code","default");
						json.put("skins", skins);
					}else{ 
						skins.put("code",record.getString("code"));
						skins.put("version",record.getInt("version"));
						skins.put("name",record.getString("name"));
						skins.put("is_used",record.getInt("is_used"));
						skins.put("visual_path",record.getString("visual_path"));
						skins.put("disk_name",record.getString("disk_name"));
						json.put("skins", skins);
					}
				}else{
					skins.put("code","default");
					json.put("skins", skins);
				}
			}else{
				skins.put("code","default");
				json.put("skins", skins);
			}
			json.put("res", "0");
			json.put("msg", "操作成功");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}

}
