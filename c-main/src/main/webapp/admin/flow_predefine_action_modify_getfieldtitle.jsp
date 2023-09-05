<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
if (flowTypeCode.equals(""))
	return;
Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
String formCode = lf.getFormCode();
String fieldWrite = ParamUtil.get(request, "fieldWrite");
String[] fieldAry = null;
String fieldText = "";
if (fieldWrite!=null && !fieldWrite.equals("")) {
 	fieldAry = fieldWrite.split(",");
  	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	int len = fieldAry.length;
	for (int i=0; i<len; i++) {
		if (fieldText.equals(""))
			fieldText = fd.getFieldTitle(fieldAry[i]);
		else
			fieldText += "," + fd.getFieldTitle(fieldAry[i]);
	}
%>	
<script>
window.parent.setFieldWriteText("<%=fieldText%>");
</script>
<%}%>
<%
String dept = ParamUtil.get(request, "dept");
String deptName = "";
if (!dept.equals("")) {
	String[] deptAry = StrUtil.split(dept, ",");
	int len = 0;
	if (deptAry!=null)
		len = deptAry.length;
	
	com.redmoon.oa.dept.DeptMgr dm = new com.redmoon.oa.dept.DeptMgr();
	for (int i=0; i<len; i++) {
		if (deptName.equals(""))
			deptName = dm.getDeptDb(deptAry[i]).getName();
		else
			deptName += "," + dm.getDeptDb(deptAry[i]).getName();
	}
}
else
	return;
%>
<script>
window.parent.setDeptName("<%=deptName%>");
</script>
