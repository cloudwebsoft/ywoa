package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

public class UsersListAction {
	private String skey = "";
	private String result = "";
	private String deptcode = "";
	private int pagenum;
	private int pagesize;
	private String op;
	private String what;

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
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

	public String getDeptcode() {
		return deptcode;
	}

	public void setDeptcode(String deptcode) {
		this.deptcode = deptcode;
	}

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
		
		try {
			if(op!=null && !op.equals("")){
				if(op.equals("search")){
					JSONArray usersArr = new JSONArray();
					if(what!=null && !what.trim().equals("")){
						String QUERY_LIST = "SELECT name FROM users  WHERE isValid=1 and realname like "+StrUtil.sqlstr("%"+what+"%");
						UserDb userDb = new UserDb();
						Vector v = userDb.list(QUERY_LIST);
						if(v!=null && v.size()>0){
							Iterator it = v.iterator();
							while(it.hasNext()){
								UserDb user = (UserDb)it.next();
								String userName = user.getName();
								JSONObject userObj = new JSONObject();
								userObj.put("id", String.valueOf(user.getId()));
								userObj.put("name", userName);
								userObj.put("realName",user.getRealName());
								DeptUserDb deptUser = new DeptUserDb(userName);
								JSONObject deptObj = new JSONObject();
								deptObj.put("dCode",deptUser.getDeptCode());
								deptObj.put("dName", deptUser.getDeptName());
								userObj.put("dept", deptObj);
								usersArr.put(userObj);
							}
							json.put("res", "0");
							json.put("msg", "操作成功");
							JSONObject result = new JSONObject();
							result.put("users", usersArr);
							json.put("result", result);
							setResult(json.toString());
							return "SUCCESS";
						}
						
					}
					
				}
				
			}else{
				String unitCode = "";
				if (getDeptcode().equals("")) {
					String userName = privilege.getUserName(getSkey());
					UserDb ud = new UserDb();
					ud = ud.getUserDb(userName);
					unitCode = ud.getUnitCode();
				} else {
					unitCode = getDeptcode();
				}

				DeptDb dd = new DeptDb();
				dd = dd.getDeptDb(unitCode);
				if (dd == null) {
					json.put("res", "-1");
					json.put("msg", "部门不存在");
					setResult(json.toString());
					return "SUCCESS";
				}

				json.put("res", "0");
				json.put("msg", "操作成功");

				JSONObject result = new JSONObject();

				DeptUserDb jd = new DeptUserDb();

				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
				boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
				String orderField = showByDeptSort ? "du.orders" : "u.orders";

			  	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.dept_code="+StrUtil.sqlstr(unitCode)+" order by du.DEPT_CODE asc, " + orderField + " asc";

			  	int curpage = getPagenum();
				int pagesize = getPagesize();
				ListResult lr = jd.listResult(sql, curpage, pagesize);
				int total = lr.getTotal();
				Vector v = lr.getResult();
				json.put("total", String.valueOf(total));
				result.put("count", String.valueOf(pagesize));
				Iterator ir = v.iterator();
				JSONArray users = new JSONArray();
				while (ir.hasNext()) {
					DeptUserDb pu = (DeptUserDb) ir.next();
					UserDb userDb = new UserDb();
					JSONObject user = new JSONObject();
					if (!pu.getUserName().equals(""))
						userDb = userDb.getUserDb(pu.getUserName());
					user.put("id", String.valueOf(userDb.getId()));
					user.put("name", userDb.getName());
					user.put("realName", userDb.getRealName());
					JSONObject deptObj = new JSONObject();
					deptObj.put("dCode",pu.getDeptCode());
					deptObj.put("dName", pu.getDeptName());
					user.put("dept", deptObj);
					users.put(user);
				}
				result.put("users", users);

				Vector v_c = dd.getChildren();
				ir = v_c.iterator();
				JSONArray childrens = new JSONArray();
				while (ir.hasNext()) {
					DeptDb dd_c = (DeptDb) ir.next();
					JSONObject children = new JSONObject();
					children.put("deptName", dd_c.getName());
					children.put("deptCode", dd_c.getCode());
					childrens.put(children);
				}
				result.put("childrens", childrens);

				json.put("result", result);
				
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
