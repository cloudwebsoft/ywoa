<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
function form1_onsubmit() {
	form1.type.value = form1.seltype.value;
	form1.root_code.value = window.parent.dirmainFrame.getRootCode();
}

function selTemplate(id)
{
	if (form1.templateId.value!=id) {
		form1.templateId.value = id;
	}
}

function enableSelType() {
	if (confirm("如果该项中已经含有内容，则更改以后会造成问题，您要强制更改吗？")) {
		form1.seltype.disabled = false;
	}
}

function getDepts() {
	return form1.depts.value;
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	form1.deptNames.value = "";
	form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.deptNames.value=="") {
			form1.depts.value += ret[i][0];
			form1.deptNames.value += ret[i][1];
		}
		else {
			form1.depts.value += "," + ret[i][0];
			form1.deptNames.value += "," + ret[i][1];
		}
	}
	if (form1.depts.value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		form1.depts.value = "<%=DeptDb.ROOTCODE%>";
		form1.deptNames.value = "全部";
	}
}

</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = "root";
String parent_name = ParamUtil.get(request, "parent_name");
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");
String op = ParamUtil.get(request, "op");
boolean isHome = false;
int type = 0;
if (op.equals(""))
	op = "AddChild";

Leaf leaf = null;
if (op.equals("modify")) {
	Directory dir = new Directory();
	leaf = dir.getLeaf(code);
	name = leaf.getName();
	description = leaf.getDescription();
	type = leaf.getType();
	isHome = leaf.getIsHome();
}
%>
<table width="98%" align="center" class="tabStyle_1 percent80">
  <form name="form1" method="post" action="flow_dir_top.jsp?op=<%=op%>" target="dirmainFrame" onClick="return form1_onsubmit()">
    <tr>
      <td colspan="2" class="tabStyle_1_title">目录增加或修改</td>
    </tr>
    <tr>
      <td width="130" rowspan="7" align="left" valign="top"><br>
        当前结点：<br>
        <font color=blue><%=parent_name.equals("")?"根结点":parent_name%></font> </td>
      <td width="557" align="left"> 编码：
        <input name="code" value="<%=code%>" <%=op.equals("modify")?"readonly":""%>>      </td>
    </tr>
    <tr>
      <td align="left">名称：
        <input name="name" value="<%=name%>"></td>
    </tr>
    <tr>
      <td align="left">描述：
        <input name="description" value="<%=description%>">
          <input type=hidden name=parent_code value="<%=parent_code%>">      </td>
    </tr>
    <tr>
      <td align="left"><%
				String disabled = "";
				if (op.equals("modify") && leaf.getType()>=1)
					disabled = "true";
				%>
        类型：
        <select name="seltype">
            <option value="<%=Leaf.TYPE_NONE%>">分类</option>
            <option value="<%=Leaf.TYPE_LIST%>" <%=op.equals("AddChild")?"selected":""%>>预置流程</option>
            <option value="<%=Leaf.TYPE_FREE%>">自由流程</option>
          </select>
          <script>
				  <%if (op.equals("modify")) {%>
					  form1.seltype.value = "<%=type%>"
				  <%}%>
				  form1.seltype.disabled = "<%=disabled%>"
				  </script>
          <input type=hidden name=root_code value="">
          <input type=hidden name="type" value="<%=type%>">
          <input type=hidden name="templateId" class="btn" value="<%=op.equals("modify")?""+leaf.getTemplateId():"-1"%>" size=3 readonly>
        &nbsp; </td>
    </tr>
    <tr>
      <td align="left"><span class="unnamed2">
        <%if (op.equals("modify")) {%>
        <%if (leaf.getCode().equals(leaf.CODE_ROOT)) {%>
        <input name="parentCode" value="-1" type="hidden">
        <%}else{%>
        <script>
var bcode = "<%=leaf.getCode()%>";
</script>
        父结点：
        <select name="parentCode">
          <%
				Leaf rootlf = leaf.getLeaf("root");
				DirectoryView dv = new DirectoryView(rootlf);
				dv.ShowDirectoryAsOptionsWithCode(out, rootlf, rootlf.getLayer());
%>
        </select>
        <script>
form1.parentCode.value = "<%=leaf.getParentCode()%>";
</script>
        <%}
}%>
      </span></td>
    </tr>
    <tr>
      <td align="left"><%
			boolean canEditForm = true;
			if (op.equals("modify")) {
				WorkflowDb wfd = new WorkflowDb();
				int count = wfd.getWorkflowCountOfType(leaf.getCode());
				FormDb fd = new FormDb();
				fd = fd.getFormDb(StrUtil.getNullString(leaf.getFormCode()));
				if (leaf.getType()==Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_FREE) {
					if (count>0) {
						canEditForm = false;
					%>
        表单：<%=fd.getName()%>&nbsp;(流程数量现有<%=count%>个)
        <input name="formCode" value="<%=leaf.getFormCode()%>" type="hidden">
        <%}
					else
						canEditForm = true;
				}
				else
					canEditForm = false;
			}
			if (canEditForm) {%>
        表单：
        <select name="formCode">
          <%
					String flowTypeCode = "";
					if (op.equals("modify"))
						flowTypeCode = leaf.getParentCode();
					else
						flowTypeCode = parent_code;
					FormDb fd = new FormDb();
					String sql = "select code from form where flowTypeCode=" + StrUtil.sqlstr(flowTypeCode) + " and isFlow=1 order by orders asc";
					Iterator ir = fd.list(sql).iterator();
					while (ir.hasNext()) {
						fd = (FormDb)ir.next();
					%>
          <option value="<%=fd.getCode()%>"><%=fd.getName()%></option>
          <%
					}
				%>
        </select>
        <%if (op.equals("modify")) {%>
        <script>
					form1.formCode.value = "<%=leaf.getFormCode()%>";
					</script>
        <%}%>
        <%}%>
        <input type="hidden" name="isHome" value="true">
        <input type="hidden" name="pluginCode" value="default">      </td>
    </tr>
    <tr>
      <td align="left">能够发起此流程的部门：（空表示所有部门）<br>
          <input type="hidden" name="depts" value="<%=op.equals("modify")?leaf.getDept():""%>">
          <textarea name="deptNames" cols="45" rows="5" readOnly wrap="yes" id="deptNames"><%
		  if (op.equals("modify")) {
			  String[] arydepts = StrUtil.split(leaf.getDept(), ",");  	  
			  int len = 0;
			  String deptNames = "";
			  if (arydepts!=null) {
				len = arydepts.length;
				DeptDb dd = new DeptDb();
				for (int i=0; i<len; i++) {
					if (deptNames.equals("")) {
						dd = dd.getDeptDb(arydepts[i]);
						deptNames = dd.getName();
					}
					else {
						dd = dd.getDeptDb(arydepts[i]);
						deptNames += "," + dd.getName();
					}
				}
			  }		  
		  	  out.print(deptNames);
		  }
		  %>
		  </textarea>
          <span class="TableData">
          <input class="btn" title="添加部门" onClick="openWinDepts()" type="button" value="添 加" name="button">
          <input class="btn" title="清空部门" onClick="form1.deptNames.value='';form1.depts.value=''" type="button" value="清 空" name="button">
        </span></td>
    </tr>
    <tr>
      <td colspan="2" align="center" valign="top"><input class="btn" name="Submit" type="submit" value="提交">
&nbsp;&nbsp;&nbsp;
        <input class="btn" name="Submit" type="reset" value="重置">
&nbsp;&nbsp;&nbsp;
      <input class="btn" name="button" type="button" onClick="enableSelType()" value="强制类型修改"></td>
    </tr>
  </form>
</table>
</body>
</html>
