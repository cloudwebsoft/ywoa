package com.redmoon.oa.android.general;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;
import org.json.*;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;

/**
 * @author lichao
 * 部门获取接口
 */
public class GetDepartmentAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //获取成功
	private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
	
	private String skey = "";
	private String deptCode = "";
	private String result = "";
	private String deptCodeReturn = "";
	private String deptName = "";
	private String parentCode = "";
	private boolean hasChild = false;
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}
	
	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		JSONArray jArray = new JSONArray();
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
		
		HttpServletRequest request = ServletActionContext.getRequest();
		privilege.doLogin(request, getSkey());
		
		String sql = "select * from department where is_show=1 and parentCode=? order by orders";
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;

		try {
			ri = jt.executeQuery(sql,new Object[]{ deptCode });

			if(!ri.hasNext()){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			}				
			
			while (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				
				deptCodeReturn = rd.getString("code");
				deptName = rd.getString("name");
				parentCode = rd.getString("parentCode");
				hasChild = (rd.getInt("childCount")==0) ? false : true;
					
				JSONObject jObject = new JSONObject();
				jObject.put("deptcode", deptCodeReturn);
				jObject.put("deptName", deptName);
				jObject.put("parentCode", parentCode);
				jObject.put("hasChild", hasChild);
				jArray.put(jObject);
			}
			
			jReturn.put("res", RES_SUCCESS);
			jResult.put("returnCode", RETURNCODE_SUCCESS);	
			jResult.put("datas", jArray);
			jReturn.put("result", jResult);
		} catch (Exception e) {
			try {
				jReturn.put("res", RES_FAIL);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			jt.close();
		}
		
		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
