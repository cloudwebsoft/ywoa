package com.redmoon.oa.android;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

public class AddressOrganizeListAction {
	private String skey = "";
	private String result = "";
	private int type;
	private String op = "";
	private String cond = ""; // 查询列表值
	private String what = "";
	private String spinner;
	private String searchCond = "";
	private String searchWhat = "";
	private String searchOp = "";
	
	

	public String getSearchCond() {
		return searchCond;
	}

	public void setSearchCond(String searchCond) {
		this.searchCond = searchCond;
	}

	public String getSearchWhat() {
		return searchWhat;
	}

	public void setSearchWhat(String searchWhat) {
		this.searchWhat = searchWhat;
	}

	public String getSearchOp() {
		return searchOp;
	}

	public void setSearchOp(String searchOp) {
		this.searchOp = searchOp;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getCond() {
		return cond;
	}

	public void setCond(String cond) {
		this.cond = cond;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private String groupType;
	private int pagenum;
	private int pagesize;

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

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public int getPagenum() {
		return pagenum;
	}

	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}

	public String execute() {
		JSONObject json = new JSONObject();		
		if (getSkey()==null || "".equals(getSkey())) {
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
		
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(groupType);
		String userName = privilege.getUserName(getSkey());
		String unitCode = privilege.getUserUnitCode(getSkey());
		if(searchOp!=null && !searchOp.equals("")&&searchOp.equals("searchOne")){
			try {
				json.put("res", "0");
				json.put("msg", "操作成功");
				String spinner = "";
				if ("name".equals(searchCond)) {
					spinner = "realName";
				} else if ("mobile".equals(searchCond)) {
					spinner = "mobile";
				}
				else if ("shortMobile".equals(searchCond)) {
					spinner = "msn";
				}
				JSONObject result = new JSONObject();
				result.put("count", String.valueOf(pagesize));
				json.put("total", String.valueOf("0"));
				String sql = "SELECT name,realName,mobile,msn,phone,email,duty FROM  users WHERE "+spinner+" like '%"+searchWhat+"%'";
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = null;
			    ri = jt.executeQuery(sql);
			    JSONArray addressers = new JSONArray();
			    while(ri.hasNext()){
			    	 ResultRecord rr = (ResultRecord) ri.next();
			    	 JSONObject addresser = new JSONObject();
			    	 addresser.put("id", rr.getString("name"));
					 addresser.put("name", rr.getString("realName"));
					 addresser.put("mobile", rr.getString("mobile"));
					 addresser.put("shortMobile",rr.getString("msn"));
					 addresser.put("operationPhone",rr.getString("phone"));
					 addresser.put("email",rr.getString("email"));
					 addresser.put("job",rr.getString("duty")); // 职务
					 addressers.put(addresser);
			    }
			    result.put("addressers", addressers);
			    json.put("result", result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			if(cond.equals("")){
				try {
					json.put("res", "0");
					json.put("msg", "操作成功");
					DeptUserDb dud =new DeptUserDb();
					Vector v = dud.list(groupType);
					Iterator ir = null;
					if (v != null)
						ir = v.iterator();
					JSONObject result = new JSONObject();
					result.put("count", String.valueOf(pagesize));
					json.put("total", String.valueOf("0"));
					JSONArray addressers = new JSONArray();
					while (ir != null && ir.hasNext()) {
						DeptUserDb dud2 = (DeptUserDb) ir.next();
						UserDb userDb = new UserDb();
						userDb = userDb.getUserDb(dud2.getUserName());
						JSONObject addresser = new JSONObject();
						addresser.put("id", String.valueOf(userDb.getName()));
						addresser.put("name", userDb.getRealName());
						addresser.put("mobile", userDb.getMobile());
						addresser.put("shortMobile",userDb.getMSN());
						addresser.put("operationPhone", userDb.getPhone());
						addresser.put("email", userDb.getEmail());
						addresser.put("job", userDb.getDuty()); // 职务
						addressers.put(addresser);
					}
					result.put("addressers", addressers);
					json.put("result", result);
					JSONArray groupTypes = new JSONArray();
						Iterator irr = dd.getChildren().iterator();
						while (irr.hasNext()) {
						    DeptDb dept = (DeptDb)irr.next();
						  	JSONObject groupType = new JSONObject();
							groupType.put("grouptype",dept.getCode());
							groupType.put("name", dept.getName());
							groupType.put("mode", "show");
							groupType.put("type","-1"); //-1代表组织机构
							groupTypes.put(groupType);
						}
						result.put("groupTypes", groupTypes);
				

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				try {
					json.put("res", "0");
					json.put("msg", "操作成功");
					String spinner = "";
					if ("name".equals(cond)) {
						spinner = "realName";
					} else if ("mobile".equals(cond)) {
						spinner = "mobile";
					}
					else if ("shortMobile".equals(cond)) {
						spinner = "msn";
					}
					JSONObject result = new JSONObject();
					result.put("count", String.valueOf(pagesize));
					json.put("total", String.valueOf("0"));
					String sql = "SELECT name,realName,mobile,msn,phone,email,duty FROM  users WHERE "+spinner+" like '%"+what+"%'";
					JdbcTemplate jt = new JdbcTemplate();
					ResultIterator ri = null;
				    ri = jt.executeQuery(sql);
				    JSONArray addressers = new JSONArray();
				    while(ri.hasNext()){
				    	 ResultRecord rr = (ResultRecord) ri.next();
				    	 JSONObject addresser = new JSONObject();
				    	 addresser.put("id", rr.getString("name"));
						 addresser.put("name", rr.getString("realName"));
						 addresser.put("mobile", rr.getString("mobile"));
						 addresser.put("shortMobile",rr.getString("msn"));
						 addresser.put("operationPhone",rr.getString("phone"));
						 addresser.put("email",rr.getString("email"));
						 addresser.put("job",rr.getString("duty")); // 职务
						 addressers.put(addresser);
				    }
				    result.put("addressers", addressers);
				    json.put("result", result);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		}
		setResult(json.toString());
		return "SUCCESS";
	}
	

	
}
