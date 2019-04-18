package com.redmoon.oa.android.i;

import java.util.Date;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import org.json.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;


 /**
 * @Description: 获取个人信息接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class GetPersonInforAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //获取成功
	private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
	
	private String skey = "";
	private String name = "";
	private String realName = "";
	private String mobile = "";
	private String birthday = "";
	private String qq = "";
	private String address = "";
	private String deptName = "";
	private String photo = "";
	private String gender = "";
	private String role = "";
	private String result = "";
	
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
		boolean flag = true;
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

		try {
			String userName = privilege.getUserName(skey);
			String sql = "select * from users where name=? ";
			
			UserDb ud = new UserDb(userName);
			
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = null;
			ResultRecord rd = null;
			
			ri = jt.executeQuery(sql, new Object[] { ud.getName()});

			if (!ri.hasNext()) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}

			if (ri.hasNext()) {
				rd = (ResultRecord) ri.next();

				name = rd.getString("name");
				realName = rd.getString("realName");
				mobile = rd.getString("mobile");
				
				java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
				Date de  = rd.getDate("birthday");
				if(de == null){
					birthday = "";
				}else{
					birthday = f.format(rd.getDate("birthday"));
				}

				qq = rd.getString("qq");
				address = rd.getString("address");

				DeptUserDb dub = new DeptUserDb();
				DeptDb db = new DeptDb();

				Vector vr = dub.getDeptsOfUser(name);
				Iterator ir = vr.iterator();
				
				String temp = "";
				boolean fg = true;
				while (ir.hasNext()) {
					db = (DeptDb) ir.next();
					temp = StrUtil.getNullString(db.getName());
					
					if("".equals(temp)){
						continue;
					}
					
					if(fg){
						deptName = temp;
						fg = false;
					}else{
						deptName = deptName + "," + temp;
					}
				}

				photo = rd.getString("photo");
				gender = rd.getString("gender");
				
				
				String sql2 = "select a.description from user_role a , user_of_role b  where a.code = b.roleCode and b.userName=?";
				
				JdbcTemplate jt2 = new JdbcTemplate();
				ResultIterator ri2 = null;
				ResultRecord rd2 = null;
				
				ri2 = jt2.executeQuery(sql2, new Object[] { ud.getName()});
				
				temp = "";
				fg = true;
				while (ri2.hasNext()) {
					rd2 = (ResultRecord) ri2.next();
					temp = StrUtil.getNullString(rd2.getString("description"));
					
					if("".equals(temp)){
						continue;
					}
					
					if(fg){
						role = temp;
						fg = false;
					}else{
						role = role + "," + temp;
					}
				}
				
				JSONObject jObject = new JSONObject();
				jObject.put("userName", name);
				jObject.put("realName", realName);
				jObject.put("mobile", mobile);
				jObject.put("birthday", birthday);
				jObject.put("qq", qq);
				jObject.put("address", address);
				jObject.put("deptName", deptName);
				jObject.put("headUrl", photo);
				jObject.put("gender", gender);
				jObject.put("role", role);
				jArray.put(jObject);

				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS);
				jResult.put("datas", jArray);
				jReturn.put("result", jResult);
			}
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
		} finally{
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
