<%@ page contentType="text/html;charset=utf-8"%><%@ page import = "java.net.URLEncoder"%><%@ page import = "java.util.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "com.redmoon.oa.flow.*"%><%@ page import = "com.redmoon.oa.person.*"%><%@ page import = "com.redmoon.oa.pvg.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("getRoleUserName")) {
	String roles = ParamUtil.get(request, "roles");
	String[] ary = StrUtil.split(roles, ",");
	int aryrolelen = 0;
	if (ary != null)
		aryrolelen = ary.length;
	UserDb user;
		
	response.setContentType("text/xml;charset=UTF-8");
	String str = "";
	str += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	str += "<ret>\n";
	
	RoleMgr rm = new RoleMgr();
	for (int k = 0; k < aryrolelen; k++) {
		RoleDb rd = rm.getRoleDb(ary[k]);
		Vector v_user = rd.getAllUserOfRole();
		Iterator ir = v_user.iterator();
		while (ir.hasNext()) {
			user = (UserDb)ir.next();
			str += "<item>\n";
			str += "<userName>" + user.getName() + "</userName>\n";
			str += "<userRealName>" + user.getRealName() + "</userRealName>\n";
			str += "</item>\n";
		}
	}

	str += "</ret>";
	out.print(str);	
}
else if (op.equals("matchActionUsers")) {	
	String internalName1 = ParamUtil.get(request, "internalName1");
	String jobCode1 = ParamUtil.get(request, "jobCode1");
	int proxyJobCode1 = ParamUtil.getInt(request, "proxyJobCode1", WorkflowActionDb.DIRECTION_UP);
	String proxyJobName1 = ParamUtil.get(request, "proxyJobName1");
	String dept1 = ParamUtil.get(request, "dept1");
	int deptMode1 = ParamUtil.getInt(request, "deptMode1");
	String proxyUserRealName1 = ParamUtil.get(request, "proxyUserRealName1");
	
	String curUserName = ParamUtil.get(request, "curUserName");
	String curUserRealName = ParamUtil.get(request, "curUserRealName");

	String internalName2 = ParamUtil.get(request, "internalName2");
	String jobCode2 = ParamUtil.get(request, "jobCode2");
	String jobName2 = ParamUtil.get(request, "jobName2");
	int proxyJobCode2 = ParamUtil.getInt(request, "proxyJobCode2", WorkflowActionDb.DIRECTION_UP);
	String proxyJobName2 = ParamUtil.get(request, "proxyJobName2");
	String dept2 = ParamUtil.get(request, "dept2");
	int deptMode2 = ParamUtil.getInt(request, "deptMode2");
	String proxyUserRealName2 = ParamUtil.get(request, "proxyUserRealName2");
	
	WorkflowActionDb wa1 = new WorkflowActionDb();
	wa1.setInternalName(internalName1);
	wa1.setJobCode(jobCode1);
	wa1.setRelateRoleToOrganization(proxyUserRealName1.equals("1"));
	wa1.setRankCode(proxyJobName1);
	wa1.setDept(dept1);
	wa1.setNodeMode(deptMode1);
	wa1.setDirection(proxyJobCode1);
	
	wa1.setUserName(curUserName);
	wa1.setUserRealName(curUserRealName);

	WorkflowActionDb wa2 = new WorkflowActionDb();
	wa2.setInternalName(internalName2);
	wa2.setJobCode(jobCode2);
	wa2.setRelateRoleToOrganization(proxyUserRealName2.equals("1"));
	wa2.setRankCode(proxyJobName2);
	wa2.setDept(dept2);
	wa2.setNodeMode(deptMode2);	
	wa2.setDirection(proxyJobCode2);
	wa2.setStrategy("");
	
	Iterator ir;
	try {
		WorkflowRouter workflowRouter = new WorkflowRouter();
		ir = workflowRouter.matchActionUser(null, wa2, wa1, true, null).iterator();
	}
	catch (ErrMsgException e) {
		out.print("-" + e.getMessage());
		return;
	}
	
	UserDb user;	
	
	response.setContentType("text/xml;charset=UTF-8");
	String str = "";
	str += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	str += "<ret>\n";
	str += "<jobName>" + jobName2 + "</jobName>";
	
	while (ir.hasNext()) {
		user = (UserDb)ir.next();
		str += "<item>\n";
		str += "<userName>" + user.getName() + "</userName>\n";
		str += "<userRealName>" + user.getRealName() + "</userRealName>\n";
		str += "</item>\n";
	}

	str += "</ret>";
	out.print(str);		
}
%>