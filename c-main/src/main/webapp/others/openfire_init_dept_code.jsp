<%@ page contentType="text/html; charset=utf-8" language="java" %>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.sso.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<html>
<head><title>初始化部门编码</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<%!
	/**
	 * 将部门的编码转换为数字型，root不变
	 * root
	 *   0001
	 *   	00010001
	 *   	00010002
	 *   0002
	 * @param leaf
	 * @param parentCode
	 * @throws ErrMsgException
	 * @throws SQLException
	 */
	public void initDeptCodeToNumberStr(JdbcTemplate jt, DeptDb leaf, String newParentCode) throws ErrMsgException, SQLException {
		DeptMgr dm = new DeptMgr();
		Vector children = dm.getChildren(leaf.getCode());
		
		String code = leaf.getCode();
		String newCode = "";
		if (!newParentCode.equals("root")) {
			newCode = newParentCode + StrUtil.PadString("" + leaf.getOrders(), '0', 4, true);
		}
		else {
			newCode = StrUtil.PadString("" + leaf.getOrders(), '0', 4, true);
		}
		
		// 更新节点的code
		String sql = "";
		if (!code.equals("root")) {
			sql = "update department set code=" + StrUtil.sqlstr(newCode) + ", parentCode=" + StrUtil.sqlstr(newParentCode) + " where code=" + StrUtil.sqlstr(code);
			
			System.out.println(getClass() + " " + sql);
			
			jt.executeUpdate(sql);
			
			// 更新部门下的用户
			sql = "update dept_user set dept_code=" + StrUtil.sqlstr(newCode) + " where dept_code=" + StrUtil.sqlstr(code);
			jt.executeUpdate(sql);
			
			// 更新通知下面的部门编码
			sql = "update oa_notice_dept set dept_code=" + StrUtil.sqlstr(newCode) + " where dept_code=" + StrUtil.sqlstr(code);
			jt.executeUpdate(sql);
			
		}

		int size = children.size();
		if (size == 0)
			return;
		
		newParentCode = newCode;
		
		if (code.equals("root")) {
			newParentCode = "root";
		}
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			DeptDb childlf = (DeptDb) ri.next();
			initDeptCodeToNumberStr(jt, childlf, newParentCode);
		}
	}
%>
<%
	JdbcTemplate jt = new JdbcTemplate();
	jt.setAutoClose(false);
	jt.beginTrans();

	DeptDb dd = new DeptDb();
	dd = dd.getDeptDb(DeptDb.ROOTCODE);
	initDeptCodeToNumberStr(jt, dd, "-1");
	
	jt.commit();
	jt.close();
%>
操作成功!
</body>
</html> 
