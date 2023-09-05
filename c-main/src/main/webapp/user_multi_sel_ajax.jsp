<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="com.redmoon.oa.db.SequenceManager" %>
<%@ page import="com.cloudweb.oa.service.IUserService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.service.IGroupService" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudweb.oa.service.IUserOfGroupService" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String path = request.getContextPath();
	String unitCode = privilege.getUserUnitCode(request);
	String op = ParamUtil.get(request, "op");
	String temp = "";

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";

	if(op.equals("getDeptUsers")){
		JSONObject json = new JSONObject();
		String sql = "";
		StringBuffer result = new StringBuffer();
		String deptCode = ParamUtil.get(request, "deptCode");
		String limitDepts = ParamUtil.get(request, "limitDepts");

		// 仅发通知时可以选择多个部门中的人员
		boolean isIncludeChildren = ParamUtil.getBoolean(request, "isIncludeChildren", false);
		if (isIncludeChildren) {
			String[] ary = StrUtil.split(deptCode, ",");
			if (ary!=null && ary.length>=1) {
				limitDepts = deptCode;
			}
		}

		boolean includeRootDept = false;
		String[] limitDeptArr = null;
		StringBuilder deptForSql = new StringBuilder();
		if(!limitDepts.equals("") ){
			limitDeptArr = StrUtil.split(limitDepts, ",");
			for(String dept : limitDeptArr){
				if (!deptForSql.toString().equals("")) {
					deptForSql.append(",");
				}
				deptForSql.append(StrUtil.sqlstr(dept));
				if(dept.equals(DeptDb.ROOTCODE)){
					includeRootDept = true;
				}
			}
		}

		if(limitDepts.equals("") || includeRootDept) {
			if (includeRootDept) {//如果是根部门的所有人员，要根据人员在查询出其所属的所有部门，以逗号隔开，拼成字符串(因为每个人所属的部门可能不止一个)
				if ("".equals(deptCode) || DeptDb.ROOTCODE.equals(deptCode)) {
					if (unitCode.equals(DeptDb.ROOTCODE)) {
						sql = "select u.name,u.realName,u.gender from users u where u.isValid=1 order by u.orders desc,u.realName asc";
					} else {
						sql = "select u.name,u.realName,u.gender from users u where u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
					}
				}
				else {
					if (unitCode.equals(DeptDb.ROOTCODE)) {
						sql = "select u.name,u.realName,u.gender from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code in (" + StrUtil.sqlstr(deptCode) + ") order by " + orderField + " desc,u.realName asc";
					} else {
						sql = "select u.name,u.realName,u.gender from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + "  and du.dept_code in (" + StrUtil.sqlstr(deptCode) + ") order by " + orderField + " desc,u.realName asc";
					}
				}
			}else{
				sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code=" + StrUtil.sqlstr(deptCode) + " order by " + orderField + " desc,du.dept_code asc, du.orders asc";
			}
		}
		else if (!limitDepts.equals("") && (isIncludeChildren || deptCode.equals(DeptDb.ROOTCODE))) {
			if (unitCode.equals(DeptDb.ROOTCODE)) {
				sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code in (" + deptForSql + ") order by " + orderField + " desc,du.dept_code asc, du.orders asc";
			}else{
				sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code in (" + deptForSql + ") and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,du.dept_code asc, du.orders asc";
			}
		}
		else if (!"".equals(deptCode)) { // limitDepts不为空时
			sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code=" + StrUtil.sqlstr(deptCode) + " order by " + orderField + " desc,du.dept_code asc, du.orders asc";
		}
		else {
			result.append("<select name='webmenu' id='webmenu' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'></select>");
			json.put("ret", "1");
			json.put("result", result);
			out.print(json);
			return;
		}

		long total = 0;
		int maxCount = 200;
		JdbcTemplate rmconn = new JdbcTemplate();
		try {
			rmconn.setAutoClose(false);
			ResultIterator ri = null;
			// 优化速度，如果查询出来的用户>200人，在flow_action_modify.jsp中显示为msDropDown时，速度会很慢
			// 而>100人时，从中再手工选人也很困难
			ri = rmconn.executeQuery(sql, 1, maxCount);
			total = ri.getTotal();

			ResultRecord rr = null;
			String userName = null;
			String userRealName = null;
			String deptName = "";
			int gender =0;//0代表男性，1代表女性
			boolean isFirst = true;
			result.append("<select name='webmenu' id='webmenu' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'>");
			String spaceSize = "";
			while (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				userName = rr.getString(1);
				userRealName = rr.getString(2);
				temp = userRealName;
				gender = rr.getInt(3);
				if (includeRootDept) {
					sql = "select d.name from dept_user du,department d where du.dept_code=d.code and du.user_name="+StrUtil.sqlstr(userName);
					ResultIterator ri1 = rmconn.executeQuery(sql);
					ResultRecord rr1 = null;
					while (ri1.hasNext()) {
						rr1 = (ResultRecord)ri1.next();
						deptName += rr1.getString(1)+(ri1.hasNext() ? "," : "");
					}
				}else{
					deptName = rr.getString(4);
				}

				if (userRealName.length()==2) {
					spaceSize = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				}
				else {
					spaceSize = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				}

				userRealName = userRealName+spaceSize+deptName;
				if(isFirst){
					if( gender == 0){
						result.append("<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>");
					}else{
						result.append("<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>");
					}
				}else{
					if( gender == 0){
						result.append("<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"'>"+userRealName+"</option>");
					}else{
						result.append("<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"'>"+userRealName+"</option>");
					}
				}
				isFirst = false;
				spaceSize = "";
				deptName = "";
			}
		}
		finally  {
			rmconn.close();
		}

		result.append("</select>");
		String tip = "";
		if (total > maxCount) {
			tip = "人数超出" + maxCount + "，未全部显示";
		}
		json.put("ret", "1");
		json.put("tip", tip);
		json.put("result", result);
		out.print(json);
		return;
	} else if(op.equals("getRoleUsers")) {
		String sql = "";
		String result = "";
		String roleCode = URLDecoder.decode(ParamUtil.get(request, "roleCode"),"utf-8");

		try {
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "roleCode", roleCode, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "roleCode", roleCode, getClass().getName());
		}
		catch (ErrMsgException e) {
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			json.put("result", e.getMessage());
			out.print(json);
			return;
		}

		if(roleCode.equals("member")){
			if (unitCode.equals(DeptDb.ROOTCODE)) {
				sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 order by u.orders desc,u.realName asc";
			}else{
				sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
			}
		}else{
			if (unitCode.equals(DeptDb.ROOTCODE)) {
				sql = "select u.name,u.realName,u.gender from users u,user_of_role a where u.isValid=1 and u.name = a.userName and a.roleCode = "+StrUtil.sqlstr(roleCode)+" order by u.orders desc,u.realName asc";
			}else{
				sql = "select u.name,u.realName,u.gender from users u,user_of_role a where u.isValid=1 and u.name = a.userName and a.roleCode = "+StrUtil.sqlstr(roleCode)+" and unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
			}
		}
		JdbcTemplate rmconn = new JdbcTemplate();
		ResultIterator ri = rmconn.executeQuery(sql);
		ResultRecord rr = null;
		String userName = null;
		String userRealName = null;
		String deptName = "";
		int gender =0;//0代表男性，1代表女性
		boolean isFirst = true;
		String spaceSize = "";
		result = "<select name='webmenu3' id='webmenu3' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'>";
		while (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			userName = rr.getString(1);
			userRealName = rr.getString(2);
			temp = userRealName;
			gender = rr.getInt(3);
			sql = "select d.name from dept_user du,department d where du.dept_code=d.code and du.user_name="+StrUtil.sqlstr(userName);
			ResultIterator ri1 = rmconn.executeQuery(sql);
			ResultRecord rr1 = null;
			while (ri1.hasNext()) {
				rr1 = (ResultRecord)ri1.next();
				deptName += rr1.getString(1)+(ri1.hasNext() ? "," : "");
			}

			if (userRealName.length()==2) {
				spaceSize = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
			}
			else {
				spaceSize = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
			}

			userRealName = userRealName+spaceSize+deptName;
			if(isFirst){
				if( gender == 0){
					result += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";
				}else{
					result += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";
				}
			}else{
				if( gender == 0){
					result += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"'>"+userRealName+"</option>";
				}else{
					result += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"'>"+userRealName+"</option>";
				}
			}
			isFirst = false;
			spaceSize = "";
			deptName = "";
		}
		result += "</select>";
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("result", result);
		out.print(json);
		return;
	}else if(op.equals("getGroupUsers")){
		String sql = "";
		String result = "";
		String groupCode = URLDecoder.decode(ParamUtil.get(request, "groupCode"),"utf-8");
		if(groupCode.equals("Everyone")){
			if (unitCode.equals(DeptDb.ROOTCODE)) {
				sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 order by u.orders desc,u.realName asc";
			}else{
				sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
			}
		}else{
			UserGroupDb ugd = new UserGroupDb(groupCode);
			if (ugd != null && ugd.isLoaded()) {
				if (ugd.isDept()) {
					if (ugd.isIncludeSubDept()) {
						DeptDb dd = new DeptDb(ugd.getDeptCode());
						Vector v = new Vector();
						v = dd.getAllChild(v, dd);
						v.add(dd);
						Iterator it = v.iterator();
						String allDepts = "";
						while (it.hasNext()) {
							DeptDb deptDb = (DeptDb) it.next();
							allDepts += (allDepts.equals("") ? "" : ",") + StrUtil.sqlstr(deptDb.getCode());
						}
						if (unitCode.equals(DeptDb.ROOTCODE)) {
							sql = "select u.name,u.realName,u.gender from users u,user_group a,dept_user du where u.isValid=1 and du.dept_code in (" + allDepts + ") and du.user_name=u.name and a.code = "+StrUtil.sqlstr(groupCode)+" order by " + orderField + " desc,u.realName";
						}else{
							sql = "select u.name,u.realName,u.gender from users u,user_group a,dept_user du where u.isValid=1 and du.dept_code in (" + allDepts + ") and du.user_name=u.name and a.code = "+StrUtil.sqlstr(groupCode)+" and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,u.realName asc";
						}
					} else {
						if (unitCode.equals(DeptDb.ROOTCODE)) {
							sql = "select u.name,u.realName,u.gender from users u,user_group a,dept_user du where u.isValid=1 and a.dept_code=du.dept_code and du.user_name=u.name and a.code = "+StrUtil.sqlstr(groupCode)+" order by " + orderField + " desc,u.realName asc";
						}else{
							sql = "select u.name,u.realName,u.gender from users u,user_of_group a,dept_user du where u.isValid=1 and a.dept_code=du.dept_code and du.user_name=u.name and a.code = "+StrUtil.sqlstr(groupCode)+" and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,u.realName asc";
						}
					}
				} else {
					if (unitCode.equals(DeptDb.ROOTCODE)) {
						sql = "select u.name,u.realName,u.gender from users u,user_of_group a where u.isValid=1 and u.name = a.user_name and a.group_code = "+StrUtil.sqlstr(groupCode)+" order by u.orders desc,u.realName asc";
					}else{
						sql = "select u.name,u.realName,u.gender from users u,user_of_group a where u.isValid=1 and u.name = a.user_name and a.group_code = "+StrUtil.sqlstr(groupCode)+" and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
					}
				}
			}
		}
		JdbcTemplate rmconn = new JdbcTemplate();
		try {
			rmconn.setAutoClose(false);
			ResultIterator ri = rmconn.executeQuery(sql);
			ResultRecord rr = null;
			String userName = null;
			String userRealName = null;
			String deptName = "";
			int gender =0;//0代表男性，1代表女性
			boolean isFirst = true;
			String spaceSize = "";
			result = "<select name='webmenu4' id='webmenu4' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'>";
			while (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				userName = rr.getString(1);
				userRealName = rr.getString(2);
				temp = userRealName;
				gender = rr.getInt(3);
				sql = "select d.name from dept_user du,department d where du.dept_code=d.code and du.user_name="+StrUtil.sqlstr(userName);
				ResultIterator ri1 = rmconn.executeQuery(sql);
				ResultRecord rr1 = null;
				while (ri1.hasNext()) {
					rr1 = (ResultRecord)ri1.next();
					deptName += rr1.getString(1)+(ri1.hasNext() ? "," : "");
				}
				if (userRealName.length()==2) {
					spaceSize = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				}
				else {
					spaceSize = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				}
				userRealName = userRealName+spaceSize+deptName;
				if(isFirst){
					if( gender == 0){
						result += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";
					}else{
						result += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";
					}
				}else{
					if( gender == 0){
						result += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"'>"+userRealName+"</option>";
					}else{
						result += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"'>"+userRealName+"</option>";
					}
				}
				isFirst = false;
				spaceSize = "";
				deptName = "";
			}
		}
		finally {
			rmconn.close();
		}
		result += "</select>";

		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("result", result);
		out.print(json);
		return;
	}else if(op.equals("searchUsers")){
		String sql = "";
		String result = "";
		// String valueName = URLDecoder.decode(ParamUtil.get(request, "userName"),"utf-8");
		String valueName = ParamUtil.get(request, "userName");

		String limitDepts = ParamUtil.get(request, "limitDepts");
		String sqlLimitDepts = "";
		boolean includeRootDept = false;
		String[] limitDeptArr = null;
		if(!limitDepts.equals("") ){
			limitDeptArr = StrUtil.split(limitDepts, ",");
			int num = limitDeptArr.length;
			int index = 0;
			for(String dept : limitDeptArr){
				sqlLimitDepts += StrUtil.sqlstr(dept);
				if((++index) < num){
					sqlLimitDepts += ",";
				}
				if(dept.equals(DeptDb.ROOTCODE)){
					includeRootDept = true;
				}

			}
		}

		if(limitDepts.equals("") || includeRootDept){
			if (true || unitCode.equals(DeptDb.ROOTCODE)) {
				sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.realName like "+ StrUtil.sqlstr("%" + valueName + "%")+" order by u.orders desc,u.realName asc";
			}else{
				sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.realName like "+ StrUtil.sqlstr("%" + valueName + "%")+" and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
			}
		}else{
			if (true || unitCode.equals(DeptDb.ROOTCODE)) {
				sql = "select u.name,u.realName,u.gender from users u,dept_user du where u.name <> 'system' and u.isValid=1 and u.realName like "+ StrUtil.sqlstr("%" + valueName + "%")+" and du.user_name=u.name and du.dept_code in ("+sqlLimitDepts+") order by " + orderField + " desc,u.realName asc";
			}else{
				sql = "select u.name,u.realName,u.gender from users u,dept_user du where u.name <> 'system' and u.isValid=1 and u.realName like "+ StrUtil.sqlstr("%" + valueName + "%")+" and du.user_name=u.name and du.dept_code in ("+sqlLimitDepts+") and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,u.realName asc";
			}
		}

		JdbcTemplate rmconn = new JdbcTemplate();
		ResultIterator ri = rmconn.executeQuery(sql);
		ResultRecord rr = null;
		String userName = null;
		String userRealName = null;
		String deptName = "";
		int gender =0;//0代表男性，1代表女性
		boolean isFirst = true;
		String spaceSize = "";
		String newName = null;
		result = "<select name='webmenu5' id='webmenu5' style='width:300px;' size='24' multiple='true' ondblclick='notSelected(this);'>";
		while (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			userName = rr.getString(1);
			userRealName = rr.getString(2);
			temp = userRealName;
			gender = rr.getInt(3);
			sql = "select d.name from dept_user du,department d where du.dept_code=d.code and du.user_name="+StrUtil.sqlstr(userName);
			ResultIterator ri1 = rmconn.executeQuery(sql);
			ResultRecord rr1 = null;
			while (ri1.hasNext()) {
				rr1 = (ResultRecord)ri1.next();
				deptName += rr1.getString(1)+(ri1.hasNext() ? "," : "");
			}
			newName = new String(userRealName.getBytes("gb2312"),"iso-8859-1");
			if(newName.length() >= 24){
				userRealName = userRealName.substring(0,10)+"...";
				newName = new String(userRealName.getBytes("gb2312"),"iso-8859-1");
			}
			for(int i=0;i<24-newName.length();i++){
				spaceSize += "&nbsp;";
			}
			userRealName = userRealName+spaceSize+deptName;
			if(isFirst){
				if( gender == 0){
					result += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";
				}else{
					result += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";
				}
			}else{
				if( gender == 0){
					result += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"'>"+userRealName+"</option>";
				}else{
					result += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"'>"+userRealName+"</option>";
				}
			}
			isFirst = false;
			spaceSize = "";
			deptName = "";
		}
		result += "</select>";

		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("result", result);
		out.print(json);
		return;
	}else if(op.equals("addUserGroup")){
		JSONObject json = new JSONObject();
		String selectedUsers = request.getParameter("selectedUsers");
		String[] users = StrUtil.split(selectedUsers, ",");
		String groupDescription = URLDecoder.decode(ParamUtil.get(request, "desc"),"utf-8");
		String groupCode = String.valueOf(SequenceManager.nextID(SequenceManager.OA_USER_GROUP));
		if(new String(groupDescription.getBytes("utf-8"),"iso-8859-1").length() > 50){
			json.put("ret", "2");
			json.put("result", "添加失败！用户组描述长度过长！");
			out.print(json);
			return;
		}
		if(Pattern.compile("'").matcher(groupDescription).find()){
			json.put("ret", "2");
			json.put("result", "添加失败！用户组描述不能包含‘字符");
			out.print(json);
			return;
		}

		IGroupService groupService = SpringUtil.getBean(IGroupService.class);
		if (groupService.create(groupCode, groupDescription, 0, 0, ConstUtil.DEPT_ROOT, "")) {
			IUserOfGroupService userOfGroupService = SpringUtil.getBean(IUserOfGroupService.class);
			userOfGroupService.create(groupCode, users);
			json.put("ret", "1");
			json.put("result", "添加成功！");
		}
		else {
			json.put("ret", "2");
			json.put("result", "添加失败！请检查编码是否有重复！");
		}
		out.print(json);
		return;
	}else if(op.equals("refreshUserGroup")){
		String sql = "";
		String result = "";
		sql = "select code,description from user_group order by isSystem desc, code asc";
		JdbcTemplate rmconn = new JdbcTemplate();
		ResultIterator ri = rmconn.executeQuery(sql);
		ResultRecord rr = null;
		String groupCode = null;
		String groupDescription = null;
		boolean isFirst = true;
		result = "<ul>";
		while (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			groupCode = rr.getString(1);
			groupDescription = rr.getString(2);
			result += "<li onMouseOver='addLiClass(this);' onMouseOut='removeLiClass(this);' onclick='getLiGroup(this);' id='"+groupCode+"' name='"+groupCode+"'>&nbsp;&nbsp;&nbsp;&nbsp;"+groupDescription+"</li>";
		}
		result += "</ul>";

		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("result", result);
		out.print(json);
		return;
	}else if(op.equals("initUsers")){
		//获取登陆用户的用户名
		String sql = "";
		String result = "<select name='websites' id='websites' style='width:300px;' size='24' multiple='true' ondblclick='hasSelected();'>";
		String userNames = URLDecoder.decode(ParamUtil.get(request, "userNames"),"utf-8");
		String userRealNames = URLDecoder.decode(ParamUtil.get(request, "userRealNames"),"utf-8");
		String[] userNameArr = StrUtil.split(userNames, ",");
		String[] userRealNameArr = StrUtil.split(userRealNames, ",");
		//根据用户名，从数据库中查询得到用户名和该用户的性别和部门，再放入map中，就能按顺序取出每个用户了
		sql = "select name,gender from users where name in (";
		int num = userNameArr.length;
		int index = 0;
		for(String user : userNameArr){
			sql += StrUtil.sqlstr(user);
			if((++index) < num){
				sql += ",";
			}
		}
		sql += ") ";
		//从库中查询，将用户名和性别放入map中
		JdbcTemplate rmconn = new JdbcTemplate();
		ResultIterator ri = rmconn.executeQuery(sql);
		ResultRecord rr = null;
		String userName = null;
		int gender =0;//0代表男性，1代表女性
		Map<String,Integer> userMap = new HashMap<String,Integer>();
		while (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			userName = rr.getString(1);
			gender = rr.getInt(2);
			userMap.put(userName,gender);
		}
		//取出用户信息，拼select
		for(int i=0;i<num;i++){
			if (!userMap.containsKey(userNameArr[i])) {
				continue;
			}
			gender = userMap.get(userNameArr[i]);
			if(i == 0){
				if( gender == 0){
					result += "<option value='"+userNameArr[i]+"' title='"+path+"/images/man.png' selected='selected'>"+userRealNameArr[i]+"</option>";
				}else{
					result += "<option value='"+userNameArr[i]+"' title='"+path+"/images/woman.png' selected='selected'>"+userRealNameArr[i]+"</option>";
				}
			}else{
				if( gender == 0){
					result += "<option value='"+userNameArr[i]+"' title='"+path+"/images/man.png' >"+userRealNameArr[i]+"</option>";
				}else{
					result += "<option value='"+userNameArr[i]+"' title='"+path+"/images/woman.png' >"+userRealNameArr[i]+"</option>";
				}
			}
		}
		result += "</select>";
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("result", result);
		out.print(json);
		return;
	}else if(op.equals("updateTable")){
		//获取登陆用户的用户名
		String logonUserName = privilege.getUser(request);
		String sql = "";
		String result = "";
		String userNames = URLDecoder.decode(ParamUtil.get(request, "userNames"),"utf-8");
		String[] users = StrUtil.split(userNames, ",");
		//从表user_recently_selected中查询出此登陆用户已经选择的所有用户
		sql = "select userName,times from user_recently_selected where name="+StrUtil.sqlstr(logonUserName)+" order by times desc";
		JdbcTemplate rmconn = new JdbcTemplate();
		ResultIterator ri = rmconn.executeQuery(sql);
		ResultRecord rr = null;
		String user = null;
		int times = 0;
		//将结果集ResultIterator中的数据存放到list集合中
		List<String> userList = new ArrayList<String>();
		while (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			user = rr.getString(1);
			times = rr.getInt(2);
			userList.add(user);
		}
		JdbcTemplate rmconn2 = new JdbcTemplate();
		try {
			rmconn2.beginTrans();
			boolean equal = false;
			for(int i=0;i<users.length;i++){
				for(int j=0;j<userList.size();j++){
					if(users[i].equals(userList.get(j))){//相等就修改次数
						sql = "update user_recently_selected set times = times+1 where name="+StrUtil.sqlstr(logonUserName)+" and userName = "+StrUtil.sqlstr(users[i]);
						equal = true;
						break;
					}
				}
				//判断是否相等
				if(equal){
					equal = false;
				}else{//不相等就插入
					//System.out.println(users[i] + "not same");
					sql = "insert into user_recently_selected values("+StrUtil.sqlstr(logonUserName)+","+StrUtil.sqlstr(users[i])+",1)";
				}
				rmconn2.executeUpdate(sql);
			}
			rmconn2.commit();
		} catch (SQLException e) {
			rmconn2.rollback();
		} finally {
			rmconn2.close();
		}
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		out.print(json);
		return;
	}
%>