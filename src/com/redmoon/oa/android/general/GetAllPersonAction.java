package com.redmoon.oa.android.general;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;
import org.json.*;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;

/**
 * @author lichao
 * 全部同事获取接口
 */
public class GetAllPersonAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //获取成功
	private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
	
	private String skey = "";
	private String result = "";
	private String userName = "";
	private String realName = "";
	private String headUrl = "";
	private String deptName = "";
	private String mobile = "";
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
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
		
		//取得所有人员
		String sql = "select name,realName,photo,mobile from users where isValid=1 and isPass=1 order by id asc ";
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;

		try {
			ri = jt.executeQuery(sql);

			if(!ri.hasNext()){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			}				
			
			DeptUserDb dub = new DeptUserDb();
			DeptDb db = new DeptDb();
			while (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				
				userName = rd.getString("name");
				realName = rd.getString("realName");
				headUrl = rd.getString("photo");
				mobile = rd.getString("mobile");

				Vector vr = dub.getDeptsOfUser(userName);
				Iterator ir = vr.iterator();
				if(ir.hasNext()){
					db = (DeptDb)ir.next();
					deptName = StrUtil.getNullString(db.getName());
				}

				JSONObject jObject = new JSONObject();
				jObject.put("userName", userName);
				jObject.put("realName", realName);
				jObject.put("headUrl", headUrl);
				jObject.put("mobile", mobile);
				jObject.put("deptName", deptName);
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
