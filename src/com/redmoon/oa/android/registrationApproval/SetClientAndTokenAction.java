package com.redmoon.oa.android.registrationApproval;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;

/**
 * @author lichao
 * 登录成功之后，将手机client和token写入user_setup表接口,若数据改变则更新
 */
public class SetClientAndTokenAction {
	private static int RES_SUCCESS = 0;                      	//成功
	private static int RES_FAIL = -1;                           //失败
	
	private static int RETURNCODE_ADD_SUCCESS = 1;              //新增成功
	private static int RETURNCODE_MODIFY_SUCCESS= 2;            //更新成功
	
	private String name = "";
	private int client;
	private String token = "";
	private String result = "";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getClient() {
		return client;
	}

	public void setClient(int client) {
		this.client = client;
	}

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

		UserDb ud = new UserDb(name);
		
		UserSetupDb ub = new UserSetupDb();
		ub = ub.getUserSetupDb(ud.getName());
		
		try {
			String sql = "select user_name from user_setup where token=? ";
			String tokenIsExist="";
			
			UserSetupDb usb = new UserSetupDb();
			
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = null;
			ResultRecord rd = null;
			
			ri = jt.executeQuery(sql,new Object[]{token});
			while (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				String name = rd.getString("user_name");
				usb = usb.getUserSetupDb(name);
				tokenIsExist = usb.getToken();
				
				if (token.equals(tokenIsExist)) {
					usb.setToken("");
					usb.save();
				}
			}
					
			String tokenDB = StrUtil.getNullString(ub.getToken());
			if ("".equals(tokenDB)) {
				ub.setClient(client);
				ub.setToken(token);
				ub.save();

				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_ADD_SUCCESS);
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			}
			
			if (!token.equals(tokenDB)) {
				ub.setClient(client);
				ub.setToken(token);
				ub.save();

				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_MODIFY_SUCCESS);
				jReturn.put("result", jResult);
			}else{
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_MODIFY_SUCCESS);
				jReturn.put("result", jResult);
			}
		}  catch (SQLException e) {
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
