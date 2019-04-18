<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>
<%
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.ERR_NOT_LOGIN));
	return;
}
String ids = ParamUtil.get(request, "ids");
String dir_code = ParamUtil.get(request, "dir_code"); // 网盘目录
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "ids", ids, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("share")) {
	String publicShareDir = ParamUtil.get(request, "dirCode");
	
	String depts = ParamUtil.get(request, "depts");
	if (ids.equals("")) {
		out.print(StrUtil.Alert_Back("请选择文件！"));
		return;
	}
	
	String[] ary = StrUtil.split(ids, ",");
	boolean re = false;
	for (int i=0; i<ary.length; i++) {
		Attachment att = new Attachment(StrUtil.toInt(ary[i]));
		
		LeafPriv lp = new LeafPriv(att.getDirCode());
		if (privilege.isUserPrivValid(request, "admin")) {
		}
		else {
			boolean canManage = false;
			if (lp.canUserModify(privilege.getUser(request)))
				canManage = true;
			else {
				PublicLeafPriv plp = new PublicLeafPriv(publicShareDir);
				if (plp.canUserManage(privilege.getUser(request)))
					canManage = true;
			}
			// 用户对该目录具有共享的权限
			if (!canManage) {
				out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
				return;
			}
		}		
		
		att.setPublicShareDepts(depts);
		att.setPublicShareDir(publicShareDir);
		re = att.save();
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "dir_list.jsp?op=editarticle&dir_code=" + StrUtil.UrlEncode(dir_code)));
	}
	else
		out.print(StrUtil.Alert("操作失败！"));
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>网络硬盘批量发布</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
var oldDirCode = "";
function selDirCode() {
	var code = form1.dirCode.options(form1.dirCode.selectedIndex).value;
	if (code=="<%=PublicLeaf.ROOTCODE%>") {
		alert("请选择相应的目录！");
		form1.dirCode.value = oldDirCode;
		return false;
	}
	oldDirCode = code;
}

function form1_onsubmit() {
	if (form1.dirCode.options(form1.dirCode.selectedIndex).value=="<%=PublicLeaf.ROOTCODE%>") {
		alert("请选择相应的目录！");
		return false;
	}
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

function getDepts() {
	return form1.depts.value;
}
</script>
</head>
<body>
<table cellspacing="0" cellpadding="3" width="100%" align="center">
  <tbody>
    <tr>
      <td class="tdStyle_1">发布</td>
    </tr>
  </tbody>
</table>
<form name=form1 action="?op=share" method=post onsubmit="return form1_onsubmit()">
<table class="tabStyle_1 percent80">

  <tr>
    <td class="tabStyle_1_title" colspan="2">批量发布至全局共享目录</td>
    </tr>
  
  <tr>
    <td width="16%" align="center">目录名称</td>
    <td width="84%"><select name=dirCode onChange="selDirCode()">
        <%
	PublicLeaf rootLeaf = new PublicLeaf();
	rootLeaf = rootLeaf.getLeaf(rootLeaf.ROOTCODE);
	PublicDirectoryView pdv = new PublicDirectoryView(rootLeaf);
	pdv.ShowDirectoryAsOptionsWithCode(request, out, rootLeaf, rootLeaf.getLayer());
	%>
      </select>
	</td>
  </tr>
  <tr>
    <td align="center">共享部门</td>
    <td><textarea name="deptNames" cols="45" rows="3" readOnly wrap="yes" id="deptName"></textarea>
          <span class="TableData">
          <input type="hidden" name="depts" value="">
          <input type="hidden" name="ids" value="<%=ids%>">
          <input type="hidden" name="dir_code" value="<%=dir_code%>">
          &nbsp;<br />
          <input class="btn" title="添加部门" onClick="openWinDepts()" type="button" value="添 加" name="button">
&nbsp;
<input class="btn" title="清空部门" onClick="form1.deptNames.value='';form1.depts.value=''" type="button" value="清 空" name="button">
<br>
(&nbsp;共享部门为空表示所有部门都可以共享&nbsp;)</span></td>
  </tr>
  <tr>
    <td colspan="2" align="center"><input class="btn" type="submit" name="Submit" value=" 确  定 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input class="btn" name="button2" type="button" onClick="form1.dirCode.value='';form1.submit()" value="取消发布"></td>
  </tr>
</table>
</form>
</body>
</html>