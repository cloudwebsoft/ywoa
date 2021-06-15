<%@ page contentType="text/html; charset=utf-8" language="java" %>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.sso.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<html>
<head><title>导入Openfire部门</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<%!
public void initDept(DeptDb leaf, String userName) {
	SyncUtil su = new SyncUtil();
	su.orgSync(leaf, SyncUtil.CREATE, userName);
}

public void initDepts(DeptDb leaf, String userName) throws ErrMsgException {
	if (!leaf.getCode().equals(DeptDb.ROOTCODE))
		initDept(leaf, userName);
	DeptMgr dm = new DeptMgr();
	Vector children = dm.getChildren(leaf.getCode());
	int size = children.size();
	if (size == 0)
		return;

	Iterator ri = children.iterator();
	while (ri.hasNext()) {
		DeptDb childlf = (DeptDb) ri.next();
		System.out.println("initDepts: " + childlf.getName());
		initDepts(childlf, userName);
	}
}
%>
<%
	Privilege pvg = new Privilege();
	String userName = "admin";
	DeptDb dd = new DeptDb();
	dd = dd.getDeptDb(DeptDb.ROOTCODE);
	initDepts(dd, userName);
	out.print("导入成功，注意不能重复导入！");
%>
</body>
</html> 
