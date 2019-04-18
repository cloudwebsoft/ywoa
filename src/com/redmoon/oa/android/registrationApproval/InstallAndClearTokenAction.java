package com.redmoon.oa.android.registrationApproval;


import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserSetupDb;

/**
 * @author lichao
 * 安装app时，清空所传token在user_setup表中的对应的记录接口
 */
public class InstallAndClearTokenAction {
	private static int RES_SUCCESS = 0;                      	//成功
	private static int RES_FAIL = -1;                           //失败
	
	private static int RETURNCODE_CLEAR_SUCCESS = 1;            //清空token成功
	
	private String token = "";
	private String result = "";
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

		try {
			String sql = "select user_name from user_setup where token=? ";
			UserSetupDb usb = new UserSetupDb();
			
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = null;
			ResultRecord rd = null;
			
			ri = jt.executeQuery(sql, new Object[] { token });
			while (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				String name = rd.getString("user_name");
				usb = usb.getUserSetupDb(name);

				usb.setClient(0); // 1：android 2 ：ios 0 :没有使用过手机端app
				usb.setToken("");
				usb.save();
			}

			jReturn.put("res", RES_SUCCESS);
			jResult.put("returnCode", RETURNCODE_CLEAR_SUCCESS);
			jReturn.put("result", jResult);
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if(!flag){
				try {
					jReturn.put("res", RES_FAIL);
					jResult.put("returnCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
