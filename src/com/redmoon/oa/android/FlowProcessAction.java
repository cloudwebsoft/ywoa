package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.DirectoryView;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.LeafChildrenCacheMgr;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.PaperConfig;
import com.redmoon.oa.flow.PaperDistributeDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

public class FlowProcessAction {

	private String result;
	private String skey;
	private String title;
	private int pageNum;
	private int pageSize;
	private String typeCode;
	private String is_readed;

	public String getIs_readed() {
		return is_readed;
	}

	public void setIs_readed(String isReaded) {
		is_readed = isReaded;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
		JSONObject result = new JSONObject();
		Privilege privilege = new Privilege();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());
			String userName = privilege.getUserName(getSkey());
			String myUnitCode = privilege.getUserUnitCode(getSkey()); // 获得当前登陆人的username
			DeptUserDb deptUsers = new DeptUserDb();
			WorkflowDb wf = new WorkflowDb();
			JSONArray pdds = new JSONArray();
			String[] ary = deptUsers.getUnitsOfUser(myUnitCode);
			PaperConfig pc = PaperConfig.getInstance();
			String swRole = pc.getProperty("swRoles");
			String[] swRoles = StrUtil.split(swRole, ",");
			UserDb user = new UserDb();
			user = user.getUserDb(userName);
			// 检查用户是否为收文角色
			int swRoleLen = 0;
			if (swRoles != null) {
				swRoleLen = swRoles.length;
			}
			// 如果没有在配置文件中限定收文角色，则允许有“收文处理”权限的用户，收取分发给单位的文件
			boolean isWithUnit = false;
			if (swRoleLen == 0) {
				isWithUnit = true;
			} else {
				for (int i = 0; i < swRoleLen; i++) {
					if (user.isUserOfRole(swRoles[i])) {
						isWithUnit = true;
						break;
					}
				}
			}
			String sql = "";
			// 判断是否加模糊查询条件
			boolean isSearch = false;
			if (getTitle() != null && !getTitle().trim().equals("")) {
				isSearch = true;
			}
			PaperDistributeDb pdd = new PaperDistributeDb();
			if (ary.length == 1) {
				if (isWithUnit) {
					if (isSearch) {
						sql = "SELECT id FROM " + pdd.getTable().getName()
								+ " WHERE to_unit IN("
								+ StrUtil.sqlstr(myUnitCode) + ","
								+ StrUtil.sqlstr(userName) + ")";
						sql +=" AND is_readed="+getIs_readed();
						sql += " AND title like "
								+ StrUtil.sqlstr("%" + title + "%");
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";
					} else {
						sql = "SELECT id FROM " + pdd.getTable().getName()
								+ " WHERE to_unit IN("
								+ StrUtil.sqlstr(myUnitCode) + ","
								+ StrUtil.sqlstr(userName) + ")";
						sql +="  AND is_readed="+getIs_readed();
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";
					}
				} else {
					if (isSearch) {
						sql = "SELECT id FROM " + pdd.getTable().getName()
								+ " WHERE to_unit=" + StrUtil.sqlstr(userName);
						sql += " AND title like "
								+ StrUtil.sqlstr("%" + title + "%");
						sql += " AND is_readed="+getIs_readed();
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";

					} else {
						sql = "SELECT id FROM " + pdd.getTable().getName()
								+ " WHERE to_unit=" + StrUtil.sqlstr(userName);
						sql += " AND is_readed="+getIs_readed();
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";
					}
				}
			} else {
				if (isWithUnit) {
					String units = StrUtil.sqlstr(userName);
					for (String aryInfo : ary) {
						if (units.equals("")) {
							units = StrUtil.sqlstr(aryInfo);
						} else {
							units += "," + StrUtil.sqlstr(aryInfo);
						}
					}
					units = "(" + units + ")";
					if (isSearch) {
						sql = "SELECT ID FROM " + pdd.getTable().getName()
						+ " WHERE to_unit IN " + units;
						sql += " AND title like "
							+ StrUtil.sqlstr("%" + title + "%");
						sql += " AND is_readed="+getIs_readed();
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";
						
					}else{
						sql = "SELECT ID FROM " + pdd.getTable().getName()
						+ " WHERE to_unit IN " + units;
						sql += " AND is_readed="+getIs_readed();
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";
					}
					
				} else {
					if(isSearch){
						sql = "SELECT id FROM " + pdd.getTable().getName()
						+ " WHERE to_unit=" + StrUtil.sqlstr(userName);
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " AND title like "
							+ StrUtil.sqlstr("%" + title + "%");
						sql += " AND is_readed="+getIs_readed();
						sql += " ORDER BY id DESC";
					}else{
						sql = "SELECT id FROM " + pdd.getTable().getName()
						+ " WHERE to_unit=" + StrUtil.sqlstr(userName);
						sql += " AND is_readed="+getIs_readed();
						sql += " AND flow IN(SELECT id FROM flow WHERE type_code=?)";
						sql += " ORDER BY id DESC";
					}
					
				}
			}

			// 判断流程类型 是否加树遍历
			if (typeCode != null && !typeCode.trim().equals("")) {
				int curPage = getPageNum(); // 当前页
				int pageSize = getPageSize();// 每页显示多少条
				ListResult lr = pdd.listResult(sql, new Object[] { typeCode },
						curPage, pageSize);
				int total = lr.getTotal();
				json.put("total", String.valueOf(total));
				Vector v = lr.getResult();
				Iterator ir = null;
				if (v != null) {
					ir = v.iterator();
				}
				while (ir.hasNext()) {
					pdd = (PaperDistributeDb) ir.next();
					JSONObject pdd2 = new JSONObject();
					user = user.getUserDb(pdd.getString("user_name"));
					String title = pdd.getString("title");
					int isRead = pdd.getInt("is_readed");
					String disDate = DateUtil.format(pdd.getDate("dis_date"),
							"yyyy-MM-dd HH:mm");
					String realName = user.getRealName();
					Long id = pdd.getLong("id");
					Long flow = pdd.getLong("flow");
					int is_flow_display = pdd.getInt("is_flow_display");
					pdd2.put("real_name", realName);
					pdd2.put("id", id);
					pdd2.put("is_read", isRead);
					pdd2.put("title", title);
					pdd2.put("disDate", disDate);
					pdd2
							.put("is_flow_display", String
									.valueOf(is_flow_display));
					pdd2.put("flow_id", String.valueOf(flow));
					pdd2.put("canConvertToPDF", cfg.get("canConvertToPDF"));
					pdds.put(pdd2);
				}
				result.put("pdd", pdds);
				json.put("result", result);
				json.put("res", "0");
				json.put("msg", "操作成功");
			} else {
				Leaf lf = new Leaf();
				lf = lf.getLeaf(Leaf.CODE_ROOT);
				LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(
						Leaf.CODE_ROOT);
				DirectoryView dv = new DirectoryView(lf);
				Iterator irlf = lccm.getList().iterator();
				while (irlf.hasNext()) {
					lf = (Leaf) irlf.next();
					if (lf.isOpen()) {
						LeafChildrenCacheMgr childlccm = new LeafChildrenCacheMgr(
								lf.getCode());
						Iterator childir = childlccm.getList().iterator();
						while (childir.hasNext()) {
							lf = (Leaf) childir.next();
							if (lf.isOpen()) {
								// 如果用户只存在于一个单位 没有兼职单位
								ListResult lr = pdd.listResult(sql,
										new Object[] { lf.getCode() }, 1, 3);
								Vector v = lr.getResult();
								Iterator ir = null;
								if (v != null) {
									ir = v.iterator();
									if (v.size() > 0) {
										JSONObject pdd2 = new JSONObject();
										pdd2.put("type_code", lf.getCode());
										pdd2.put("flow_title", lf.getName());
										pdd2.put("count", lr.getTotal());
										pdds.put(pdd2);
									}
								}
								while (ir.hasNext()) {
									pdd = (PaperDistributeDb) ir.next();
									JSONObject pdd2 = new JSONObject();
									user = user.getUserDb(pdd
											.getString("user_name"));
									String title = pdd.getString("title");
									int isRead = pdd.getInt("is_readed");
									String disDate = DateUtil.format(pdd
											.getDate("dis_date"),
											"yyyy-MM-dd HH:mm");
									String realName = user.getRealName();
									Long id = pdd.getLong("id");
									Long flow = pdd.getLong("flow");
									pdd2.put("real_name", realName);
									pdd2.put("id", id);
									pdd2.put("is_read", isRead);
									pdd2.put("title", title);
									pdd2.put("disDate", disDate);
									int is_flow_display = pdd
											.getInt("is_flow_display");
									pdd2.put("is_flow_display", String
											.valueOf(is_flow_display));
									pdd2.put("flow_id", String.valueOf(flow));
									pdd2.put("canConvertToPDF", cfg
											.get("canConvertToPDF"));
									pdds.put(pdd2);
								}
							}
						}
					}
				}

				result.put("pdd", pdds);
				json.put("result", result);
				json.put("res", "0");
				json.put("msg", "操作成功");
				json.put("total", 0);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}

}
