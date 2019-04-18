package com.redmoon.oa.android.registrationApproval;

import org.json.JSONException;
import org.json.JSONObject;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.person.UserDb;

/**
 * @author lichao
 * 注册后查询审核状态接口
 */
public class CheckRegistStatusAction {
	private static int RETURNCODE_NOT_EXAMINE = 0;       //没审核
	private static int RETURNCODE_PASS = 1;              //审核通过
	private static int RETURNCODE_NOT_PASS= 2;           //审核不通过（用户不存在）
	
	private String name = "";
	private String result = "";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		boolean re = false;
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();

		String sql = "select isPass from users where name=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		
		UserDb ud = new UserDb(name);
		
		int isPass = -1;
		try {
			ri = jt.executeQuery(sql, new Object[] { ud.getName() });
			if (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				isPass = rd.getInt("isPass");
				re = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jt.close();
		}

		try {
			if (re) {
				jReturn.put("res", 0);
				if (isPass == 0) {
					jReturn.put("msg", "未审核");
					jResult.put("returnCode", RETURNCODE_NOT_EXAMINE);
				} else if (isPass == 1) {
					jReturn.put("msg", "通过");
					jResult.put("returnCode", RETURNCODE_PASS);
				} 
				jReturn.put("result", jResult);
			} else {
				jReturn.put("res", 0);
				jReturn.put("msg", "不通过");
				jResult.put("returnCode", RETURNCODE_NOT_PASS);
				jReturn.put("result", jResult);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
