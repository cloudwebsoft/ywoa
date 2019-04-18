<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<HTML><HEAD><TITLE>流程动作设定</TITLE>
<link href="common.css" rel="stylesheet" type="text/css">
<%
String op = ParamUtil.get(request, "op");
int flowId = ParamUtil.getInt(request, "flowId");
WorkflowDb wfd = new WorkflowDb();
wfd = wfd.getWorkflowDb(flowId);
Leaf lf = new Leaf();
lf = lf.getLeaf(wfd.getTypeCode());
FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());
Vector v = fd.getFields();
Iterator ir = v.iterator();
String options = "";
String fieldWrite = ParamUtil.get(request, "fieldWrite");
String[] fds = fieldWrite.split(",");
int len = fds.length;
if (fieldWrite.equals(""))
	len = 0; // 当为空时，split所得的数组长度为1
String[] fdsText = new String[len];
while (ir.hasNext()) {
	FormField ff = (FormField) ir.next();
	boolean isFinded = false;
	for (int i=0; i<len; i++) {
		if (ff.getName().equals(fds[i])) {
			isFinded = true;
			fdsText[i] = ff.getTitle();
		}
	}
	if (!isFinded)
		options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
}
String selOptions = "";
for (int i=0; i<len; i++) {
	selOptions += "<option value='" + fds[i] + "'>" + fdsText[i] + "</option>";
}
%>
<script language="JavaScript">
function setFieldWrite() {
	var str = "";
	var strText = "";
	var opts = fieldsSelected.options;
	var len = opts.length;
	for (var i=0; i<len; i++) {
		if (str=="") {
			str = opts[i].value;
			strText = opts[i].text;
		}
		else {
			str += "," + opts[i].value;
			strText += "," + opts[i].text;
		}
	}
	window.opener.setFieldWriteValue(str);
	window.opener.setFieldWriteText(strText);
	window.close();
}

function sel() {
	var opts = fieldsNotSelected.options;
	var len = opts.length;
	var ary = new Array(len);
	for (var i=0; i<len; i++) {
		ary[i] = "0";
		if (opts(i).selected) {
			fieldsSelected.options.add(new Option(opts[i].text, opts[i].value));
			ary[i] = opts[i].value;
		}
	}
	for (var i=0; i<len; i++) {
		for (var j=0; j<len; j++) {
			if (ary[i]!="0") {
				try {
				    // 删除项目后，options会变短，因此用异常捕获来防止出错
					if (opts[j].value==ary[i])
						opts.remove(j);
				}
				catch(e) {
				}
			}
		}
	}
}

function notsel() {
	var opts = fieldsSelected.options;
	var len = opts.length;
	var ary = new Array(len);
	for (var i=0; i<len; i++) {
		ary[i] = "0";
		if (opts(i).selected) {
			fieldsNotSelected.options.add(new Option(opts[i].text, opts[i].value));
			ary[i] = opts[i].value;
		}
	}
	
	for (var i=0; i<len; i++) {
		for (var j=0; j<len; j++) {
			if (ary[i]!="0") {
				try {
				    // 删除项目后，options会变短，因此用异常捕获来防止出错
					if (opts[j].value==ary[i])
						opts.remove(j);
				}
				catch(e) {
				}
			}
		}
	}
	
}
</script>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</HEAD>
<BODY bgColor=#FBFAF0 leftMargin=4 topMargin=8 rightMargin=0 class=menubar onLoad="window_onload()">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.println(fchar.makeErrMsg("警告非法用户，你无访问此页的权限！"));
	// return;
}
%>
<table width="501" height="293"  border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
  <tr>
    <td height="23" colspan="3" class="right-title">&nbsp;&nbsp;<span>选择表单域 (表单名称：<%=fd.getName()%>)</span></td>
  </tr>
  <tr>
    <td width="231" height="22" align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;以下为已选的域</td>
    <td width="37">&nbsp;</td>
    <td width="231" height="22">以下为备选的域</td>
  </tr>
  <tr>
    <td align="right"><select name="fieldsSelected" size=15 multiple style="width:200px">
	<%=selOptions%>
    </select>    </td>
    <td align="center" valign="middle"><input type="button" name="sel" value=" &lt; " onClick="sel()">
      <br>
      <br>
    <input type="button" name="notsel" value=" &gt; " onClick="notsel()"></td>
    <td>
	<select name="fieldsNotSelected" size=15 multiple style="width:200px">
	<%=options%>
    </select>
	</td>
  </tr>
  <tr>
    <td height="22" align="left">&nbsp;</td>
    <td>&nbsp;</td>
    <td height="22">&nbsp;</td>
  </tr>
  <tr align="center">
    <td height="28" colspan="3"><input type="button" name="okbtn" value="确定" onClick="setFieldWrite()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="button" name="cancelbtn" value="取消" onClick="window.close()"></td>
  </tr>
</table>
</BODY></HTML>
