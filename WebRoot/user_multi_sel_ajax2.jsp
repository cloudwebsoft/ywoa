<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = ParamUtil.get(request, "CPages");
String op = ParamUtil.get(request, "op");	  
int pagesize = StrUtil.toInt(ParamUtil.get(request, "pagesize"));	
String name = ParamUtil.get(request, "name");
String depts = ParamUtil.get(request, "depts");
// System.out.println("name:"+name);
if(op.equals("getResult")) {	     
	String temp = ParamUtil.get(request, "temp");
	String deptCode = ParamUtil.get(request, "deptCode");
	if (strcurpage.equals(""))
		  strcurpage = "1";
	if (!StrUtil.isNumeric(strcurpage)) {
		  out.print(StrUtil.makeErrMsg("标识非法！"));
		  return;
	}
	int curpage = Integer.parseInt(strcurpage);
	String sql = "";
	/*
	String unitCode = privilege.getUserUnitCode(request);
	if(!name.equals("")){
		if (unitCode.equals(DeptDb.ROOTCODE)) {
	   		sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.realName like '%"+name+"%' order by du.DEPT_CODE asc, orders asc";
		}
		else {
	   		sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " and u.realName like '%"+name+"%' order by du.DEPT_CODE asc, orders asc";
		}
	}else{
		if (unitCode.equals(DeptDb.ROOTCODE)) {
	   		sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 order by du.DEPT_CODE asc, orders asc";
		}
		else {
	   		sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by du.DEPT_CODE asc, orders asc";
		}
	}
	*/
	// System.out.println(getClass() + " " + sql);
	StringBuilder sb = new StringBuilder();
	if (depts != null && !depts.equals("") && !depts.equals(DeptDb.ROOTCODE)) {
		sb.append("(");
		String[] deptAry = depts.split(",", -1);
		for (int i = 0; i < deptAry.length; i++) {
			DeptDb dd = new DeptDb();
			dd = dd.getDeptDb(deptAry[i]);

			Vector v = new Vector();
			v = dd.getAllChild(v, dd);
			sb.append(StrUtil.sqlstr(deptAry[i])).append(i == deptAry.length - 1 ? "" : ",");
			if (v != null && v.size() != 0) {
				Iterator it = v.iterator();
				while (it.hasNext()) {
					DeptDb ddb = (DeptDb)it.next();
					sb.append(StrUtil.sqlstr(ddb.getCode())).append(it.hasNext() ? "" : ",");
				}
			}
		}
		sb.append(")");
	}
	if(!name.equals("")) {
		if (depts != null && !depts.equals("") && !depts.equals(DeptDb.ROOTCODE)) {
			sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.realName like " + StrUtil.sqlstr("%" + name + "%") + " and dept_code in " + sb.toString() + " order by du.DEPT_CODE asc, orders asc";
		} else {
			sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.realName like " + StrUtil.sqlstr("%" + name + "%") + " order by du.DEPT_CODE asc, orders asc";
		}
	} else if(temp.equals("ajaxList")){
		if (deptCode != null && !deptCode.equals("") && !deptCode.equals(DeptDb.ROOTCODE)) {
			sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE=" + StrUtil.sqlstr(deptCode) + " order by orders asc";
		} else {
			sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 order by du.DEPT_CODE asc, orders asc";
		}
	} else {
		if (depts != null && !depts.equals("") && !depts.equals(DeptDb.ROOTCODE)) {
			sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and dept_code in " + sb.toString() + " order by du.DEPT_CODE asc, orders asc";
		} else {
			sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 order by du.DEPT_CODE asc, orders asc";
		}
	}
	// System.out.println(getClass() + " " + sql);
	DeptUserDb du = new DeptUserDb();
	ListResult lr = du.listResult(sql, curpage, pagesize);
	int total = lr.getTotal();
	Paginator paginator = new Paginator(request, total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		  curpage = 1;
		  totalpages = 1;
	}
	Vector v = lr.getResult();
	Iterator ir = null;
	if (v!=null)
	  ir = v.iterator();  
				
	String result = "";
	String begin = "";
	String end = "";
	begin = "<table width='100%' style='margin:0px; padding:0px; border:0px' border='0' cellpadding='0' cellspacing='0' >";
	end = "</table>";
	
	String allUserOfDept = "";
	String allUserRealNameOfDept = "";
	UserDb ud = new UserDb();
	while (ir.hasNext()) {
	  DeptUserDb pu = (DeptUserDb)ir.next();
	  if (!pu.getUserName().equals(""))
		  ud = ud.getUserDb(pu.getUserName());
		  if(temp.equals("harddriver")){
		  	result += "<tr class='userList'><td width='98' style='width:30%' align='center'>"+pu.getDeptName()+"</td><td width='91' style='width:30%' align='center'>"+ud.getRealName()+"</td><td width='74' style='width:30%' align='center'><a href='clouddisk_network_neighborhood_info.jsp?op=showDirShare&shareUser="+StrUtil.UrlEncode(ud.getName())+"'><img src='"+request.getContextPath()+"/netdisk/images/clouddisk/network_2.gif' width='30px' height='22px' title='共享目录'></a></td></tr>";
		  }
		  else if(temp.equals("ajaxList")){
		  	result += "<tr class='userList'><td width='98' style='width:30%' align='center'>"+pu.getDeptName()+"</td><td width='91' style='width:30%' align='center'>"+ud.getRealName()+"</td><td width='74' style='width:30%' align='center'><a href='clouddisk_network_neighborhood_info.jsp?op=showDirShare&shareUser="+StrUtil.UrlEncode(ud.getName())+"'><img src='"+request.getContextPath()+"/netdisk/images/clouddisk/network_2.gif' width='30px' height='22px' title='共享目录'></a></td></tr>";
		  }
		  else{
	  		result += "<tr><td width='98' align='center'>"+pu.getDeptName()+"</td><td width='91' align='center'><a href='javascript:;' onClick=\"selPerson('"+pu.getDeptCode()+"', '"+pu.getDeptName()+"', '"+ud.getName()+"','"+ud.getRealName()+"')\">"+ud.getRealName()+"</a></td><td width='74' align='center'><a href='#' onClick=\"cancelSelPerson('"+pu.getDeptCode()+"', '"+pu.getDeptName()+"', '"+ud.getName()+"','"+ud.getRealName()+"')\">取消选择</a></td></tr>";	
	  	  }
	  if (allUserOfDept.equals(""))
	  	allUserOfDept = ud.getName();
	  else
		allUserOfDept += "," + ud.getName();
	  if (allUserRealNameOfDept.equals(""))
	  	allUserRealNameOfDept = ud.getRealName();
	  else
		  allUserRealNameOfDept += "、"  + ud.getRealName();
	}
	
	// System.out.println(getClass() + " result=" + result);
	
	if (name.equals("admin") || name.equals("管理员")) {
	  result += "<tr><td width='98' align='center'></td><td width='91' align='center'>管理员</td><td width='74' align='center'><a href='#' onClick=\"cancelSelPerson('', '', 'admin','管理员')\">取消选择</a></td></tr>";	
	  allUserOfDept = "admin";
	  allUserRealNameOfDept = "管理员";
	}
	
	JSONObject json = new JSONObject();
	json.put("ret", "1");
	json.put("result", begin+result+end);
	json.put("allUserOfDept", allUserOfDept);
	json.put("allUserRealNameOfDept", allUserRealNameOfDept);
	json.put("totalpages", totalpages);
	out.print(json);
	
	// System.out.println(getClass() + " json=" + json);
	return;
}
%>