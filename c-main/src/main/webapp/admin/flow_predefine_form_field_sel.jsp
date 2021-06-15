<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE>流程表单域设定</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%
String op = ParamUtil.get(request, "op");
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());
if (!fd.isLoaded()) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单" + lf.getFormCode() + "不存在！"));
	return;
}
Vector v = fd.getFields();
Iterator ir = v.iterator();
String options = "";
String fields = ParamUtil.get(request, "fields");

String[] fds = fields.split(",");
int len = fds.length;
if (fields.equals(""))
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
	window.opener.setFieldValue(str);
	window.opener.setFieldText(strText);
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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</HEAD>
<BODY>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="501" height="293"  border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
  <tr>
    <td height="23" colspan="3" class="tabStyle_1_title">选择表单域 (表单名称：<%=fd.getName()%>)</td>
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
    <td align="center" valign="middle"><input type="button" name="sel" style="font-family:'宋体'" value=" &lt; " onClick="sel()">
      <br>
      <br>
    <input type="button" name="notsel" style="font-family:'宋体'" value=" &gt; " onClick="notsel()"></td>
    <td>
	<select name="fieldsNotSelected" size=15 multiple style="width:200px">
	<%=options%>
    </select>	</td>
  </tr>
  <tr align="center">
    <td height="28" colspan="3"><input class="btn" type="button" name="okbtn" value="确定" onClick="setFieldWrite()">
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <input class="btn" type="button" name="cancelbtn" value="取消" onClick="window.close()">
    </td>
  </tr>
</table>
</BODY></HTML>
