<%@ page contentType="text/html;charset=utf-8" %><%@page import="com.redmoon.oa.post.*"%><%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%><%@page import="net.sf.json.*"%><%@page import="cn.js.fan.util.*"%><%@page import="com.redmoon.oa.pvg.Privilege"%><%@page import="com.redmoon.oa.person.UserDb"%><%@page import="com.redmoon.oa.dept.*"%><%@page import="java.util.*"%><%
String op = ParamUtil.get(request, "op");
PostDb pdb = new PostDb();
PostUserDb pudb = new PostUserDb();
PostFlowDb pfdb = new PostFlowDb();
boolean re = false;
JSONObject json = new JSONObject();

if (!new Privilege().isUserPrivValid(request, "admin.user")) {
	json.put("ret", 0);
	json.put("msg", cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	out.print(json.toString());
	return;
}

if (op.equals("addPost")) {
	String name = ParamUtil.get(request, "name");
	String unitCode = ParamUtil.get(request, "unitCode");
	String description = ParamUtil.get(request, "description");
	int orders = ParamUtil.getInt(request, "orders", 0);
	try {
		PostMgr pm = new PostMgr();
		pm.setName(name);
		pm.setUnitCode(unitCode);
		if (pm.isExist()) {
			json.put("ret", 0);
			json.put("msg", "操作失败，该岗位已存在！");
			out.print(json.toString());
			return;
		}
		re = pdb.create(new JdbcTemplate(), new Object[]{name, unitCode, description, orders});
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("editPost")) {
	int id = ParamUtil.getInt(request, "id", 0);
	String name = ParamUtil.get(request, "name");
	String unitCode = ParamUtil.get(request, "unitCode");
	String description = ParamUtil.get(request, "description");
	int orders = ParamUtil.getInt(request, "orders", 0);
	pdb = pdb.getPostDb(id);
	if (pdb == null || !pdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，岗位不存在！");
		out.print(json.toString());
		return;
	}
	try {
		re = pdb.save(new JdbcTemplate(), new Object[]{name, unitCode, description, orders, id});
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("delPost")) {
	int id = ParamUtil.getInt(request, "id", 0);
	pdb = pdb.getPostDb(id);
	if (pdb == null || !pdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，岗位不存在！");
		out.print(json.toString());
		return;
	}
	try {
		re = pdb.del();
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("addPostUser")) {
	int postId = ParamUtil.getInt(request, "post_id", 0);
	pdb = pdb.getPostDb(postId);
	if (pdb == null || !pdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，岗位不存在！");
		out.print(json.toString());
		return;
	}
	String users = ParamUtil.get(request, "users");
	if (users == null || users.equals("")) {
		json.put("ret", 0);
		json.put("msg", "请选择人员！");
		out.print(json.toString());
		return;
	}
	PostUserMgr puMgr = new PostUserMgr();
	puMgr.setPostId(postId);
	try {
		String[] userNames = StrUtil.split(users, ",");
		users = "";
		for (String userName : userNames) {
			puMgr.setUserName(userName);
			if (!puMgr.isExist()) {
				users += (users.equals("") ? userName : "," + userName);
			}
		}
		if (!users.equals("")) {
			re = puMgr.create(users);
			if (re) {
				JSONArray jsonAry = new JSONArray();
				JSONObject subjson = new JSONObject();
				userNames = StrUtil.split(users, ",");
				for (String userName : userNames) {
					puMgr.setUserName(userName);
					if (puMgr.isExist()) {
						pudb = pudb.getPostUserDb(puMgr.getId());
						UserDb ud = new UserDb(pudb.getString("user_name"));
						subjson.put("id", puMgr.getId());
						subjson.put("orders", pudb.getInt("orders"));
						subjson.put("userName", ud.getName());
						subjson.put("realName", ud.getRealName());
						subjson.put("gender", ud.getGender() == UserDb.GENDER_MAN ? "男" : "女");
						
						DeptUserDb du = new DeptUserDb();
						Iterator ir = du.getDeptsOfUser(userName).iterator();
						String deptName = "";
						while (ir.hasNext()) {
							DeptDb dd = (DeptDb) ir.next();
							if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
								deptName += new DeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
							} else {
								deptName += dd.getName() + "&nbsp;&nbsp;";
							}
							if (ir.hasNext()) {
								deptName += "，";
							}
						}
						subjson.put("dept", deptName);
						jsonAry.add(subjson);
					}
				}
				if (jsonAry.isEmpty()) {
					json.put("ret", 0);
					json.put("msg", "被选择的人员已属于其他岗位！");
				} else {
					json.put("ret", 1);
					json.put("data", jsonAry);
				}
			} else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");
			}
		} else {
			json.put("ret", 0);
			json.put("msg", "被选择的人员已属于其他岗位！");
		}
	} catch (Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}

	out.print(json.toString());
	return;
} else if (op.equals("delPostUser")) {
	int id = ParamUtil.getInt(request, "id", 0);
	pudb = pudb.getPostUserDb(id);
	if (pudb == null || !pudb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，该人员不存在！");
		out.print(json.toString());
		return;
	}
	JdbcTemplate jt = new JdbcTemplate();
	try {
		String userName = pudb.getString("user_name");
		re = pudb.del();
		
		// 更新人员基本信息表的岗位
		String sql = "update form_table_personbasic set job_level='' where user_name=?";
		jt.executeUpdate(sql, new Object[] { userName });
		
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	} finally {
		jt.close();
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("delPostUserBatch")) {
	String ids = ParamUtil.get(request, "ids");
	PostUserMgr puMgr = new PostUserMgr();
	try {
		re = puMgr.delBatch(ids);
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("addPostFlow")) {
	try {
		PostFlowMgr pfm = new PostFlowMgr();
		re = pfm.addPostFlow(request);
		if (re) {
			pfm.isExist();
			json.put("ret", 1);
			json.put("id", pfm.getId());
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	}
	catch (ErrMsgException e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	out.print(json.toString());
	return;
} else if (op.equals("editPostFlow")) {
	int id = ParamUtil.getInt(request, "id", 0);
	int postId = ParamUtil.getInt(request, "post_id", 0);
	String flowCode = ParamUtil.get(request, "flow_code");
	pfdb = pfdb.getPostFlowDb(id);
	if (pfdb == null || !pfdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，该流程不存在！");
		out.print(json.toString());
		return;
	}
	try {
		int isRelated = pfdb.getInt("is_related");
		re = pfdb.save(new JdbcTemplate(), new Object[]{postId, flowCode, id, isRelated});
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("delPostFlow")) {
	int id = ParamUtil.getInt(request, "id", 0);
	pfdb = pfdb.getPostFlowDb(id);
	if (pfdb == null || !pfdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，该流程不存在！");
		out.print(json.toString());
		return;
	}
	try {
		re = pfdb.del();
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("delPostFlowBatch")) {
	String ids = ParamUtil.get(request, "ids");
	PostFlowMgr pfMgr = new PostFlowMgr();
	try {
		re = pfMgr.delBatch(ids);
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch(Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("relateScore")) {
	int id = ParamUtil.getInt(request, "id", 0);
	pfdb = pfdb.getPostFlowDb(id);
	if (pfdb == null || !pfdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，该流程不存在！");
		out.print(json.toString());
		return;
	}
	try {
		pfdb.set("is_related", 1);
		re = pfdb.save();
		if (re) {
			id = ParamUtil.getInt(request, "maxId", 0);
			if (id > 0) {
				pfdb = pfdb.getPostFlowDb(id);
				if (pfdb == null || !pfdb.isLoaded()) {
					json.put("ret", 0);
					json.put("msg", "操作失败，该流程不存在！");
					out.print(json.toString());
					return;
				}
				try {
					pfdb.set("is_related", 0);
					re = pfdb.save();
					if (re) {
						json.put("ret", 1);
					} else {
						json.put("ret", 0);
						json.put("msg", "操作失败！");
					}
				} catch (Exception e) {
					json.put("ret", 0);
					json.put("msg", e.getMessage());
				}
			} else {
				json.put("ret", 1);
			}
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch (Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
} else if (op.equals("cancelScore")) {
	int id = ParamUtil.getInt(request, "id", 0);
	pfdb = pfdb.getPostFlowDb(id);
	if (pfdb == null || !pfdb.isLoaded()) {
		json.put("ret", 0);
		json.put("msg", "操作失败，该流程不存在！");
		out.print(json.toString());
		return;
	}
	try {
		pfdb.set("is_related", 0);
		re = pfdb.save();
		if (re) {
			json.put("ret", 1);
		} else {
			json.put("ret", 0);
			json.put("msg", "操作失败！");
		}
	} catch (Exception e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	
	out.print(json.toString());
	return;
}
%>
