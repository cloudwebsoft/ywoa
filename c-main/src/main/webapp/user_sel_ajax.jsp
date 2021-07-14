<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = ParamUtil.get(request, "CPages");
String op = ParamUtil.get(request, "op");	  
int pagesize = StrUtil.toInt(ParamUtil.get(request, "pagesize"));	
String name = ParamUtil.get(request, "name");	
	
if (!cn.js.fan.db.SQLFilter.isValidSqlParam(name)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ user_sel_ajax.jsp name=" + name);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

// System.out.println("name:"+name);
if(op.equals("getResult")) {	     
	if (strcurpage.equals(""))
		  strcurpage = "1";
	if (!StrUtil.isNumeric(strcurpage)) {
		  out.print(StrUtil.makeErrMsg("标识非法！"));
		  return;
	}

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";

	int curpage = Integer.parseInt(strcurpage);
	String sql = "";
	if(!name.equals("")){
	   sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " and u.realName like '%"+name+"%' order by " + orderField + " desc,du.DEPT_CODE asc, orders asc";
	}else{
	   sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by " + orderField + " desc,du.DEPT_CODE asc, orders asc";
	}
	// System.out.println(getClass() + " " + sql);
	DeptUserDb du = new DeptUserDb();
	ListResult lr = du.listResult(sql, curpage, pagesize);
	long total = lr.getTotal();
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
	begin = "<table width='100%' style='margin:0px; padding:0px' border='0' cellpadding='0' cellspacing='0' >";
	end = "</table>";
	
	String allUserOfDept = "";
	String allUserRealNameOfDept = "";
	UserDb ud = new UserDb();
	while (ir.hasNext()) {
	  DeptUserDb pu = (DeptUserDb)ir.next();
	  if (!pu.getUserName().equals(""))
		  ud = ud.getUserDb(pu.getUserName());
	  result += "<tr><td width='98' align='center'>"+pu.getDeptName()+"</td><td width='98' align='center'><a href='javascript:;' onClick=\"setPerson('"+pu.getDeptCode()+"', '"+pu.getDeptName()+"', '"+ud.getName()+"','"+ud.getRealName()+"')\">"+ud.getRealName()+"</a></td></tr>";	
	  
	  if (allUserOfDept.equals(""))
	  	allUserOfDept = ud.getName();
	  else
		allUserOfDept += "," + ud.getName();
	  if (allUserRealNameOfDept.equals(""))
	  	allUserRealNameOfDept = ud.getRealName();
	  else
		  allUserRealNameOfDept += "、"  + ud.getRealName();
	}	    
	JSONObject json = new JSONObject();
	json.put("ret", "1");
	json.put("result", begin+result+end);
	json.put("allUserOfDept", allUserOfDept);
	json.put("allUserRealNameOfDept", allUserRealNameOfDept);
	out.print(json);
	
	return;
}
%>