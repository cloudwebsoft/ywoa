<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String dirCode = ParamUtil.get(request, "dir_code");
if (dirCode.equals(""))
	dirCode = PublicLeaf.ROOTCODE;
String correct_result = "操作成功！";	

PublicLeafPriv lp = new PublicLeafPriv(dirCode);
if (lp.canUserSeeByAncestor(privilege.getUser(request)))
	;
else {
	out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	if (lp.canUserManage(privilege.getUser(request))) {
		int delAttachId = ParamUtil.getInt(request, "id");
		PublicAttachment patt = new PublicAttachment();
		patt = patt.getPublicAttachment(delAttachId);
		if (patt.del()) {
			out.print(StrUtil.Alert_Redirect("操作成功！", "netdisk_public_attach_list.jsp?dir_code=" + StrUtil.UrlEncode(dirCode)));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
	}
	else {
		out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	}
	return;
}
else if (op.equals("delBatch")) {
	if (lp.canUserManage(privilege.getUser(request))) {
		String strIds = ParamUtil.get(request, "ids");
		if (strIds.equals("")) {
			out.print(StrUtil.Alert_Back("请选择！"));
			return;
		}
		String[] ary = StrUtil.split(strIds, ",");
		PublicAttachment patt = new PublicAttachment();		
		for (int i=0; i<ary.length; i++) {
			int delAttachId = StrUtil.toInt(ary[i]);
			patt = patt.getPublicAttachment(delAttachId);
			patt.del();
		}
		out.print(StrUtil.Alert_Redirect("操作成功！", "netdisk_public_attach_list.jsp?dir_code=" + StrUtil.UrlEncode(dirCode)));
	}
	else {
		out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	}
	return;
}

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 10;
int curpage = Integer.parseInt(strcurpage);

PublicLeaf publf = new PublicLeaf();
if (!dirCode.equals("")) {
	publf = publf.getLeaf(dirCode);
	if (publf==null || !publf.isLoaded()) {
		out.print(StrUtil.Alert("节点不存在！"));
		return;
	}
}

String filePath = publf.getFilePath();

if (dirCode.equals(""))
	dirCode = PublicLeaf.ROOTCODE;

String sql = "select id from net_disk where is_share=0 order by id desc";

PublicShare ps = new PublicShare();
ListResult lr = ps.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>网络硬盘-公共文件列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script language=JavaScript src='formpost.js'></script>
<script>
function selAllCheckBox(checkboxname){
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = true;
  }
  for (i=0; i<checkboxboxs.length; i++)
  {
	  checkboxboxs[i].checked = true;
  }
  }
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function delBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	if (!confirm("您确定要删除么？"))
		return;
	hidForm.op.value = "delBatch";
	hidForm.action = "netdisk_public_attach_list.jsp";
	hidForm.ids.value = ids;
	hidForm.submit();
}

function moveBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	hidForm.op.value = "";
	hidForm.action = "netdisk_public_dir_change_batch.jsp";
	hidForm.ids.value = ids;
	hidForm.submit();
}

function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
	// alert(index + "-" + fileName + "-" + filePath + "-" + fileSize + "-" + modifyDate);
	// o("attFiles").innerHTML += "<div id='attTmp' name='attTmp' attId='" + index + "' >" + index + "-" + fileName + "<a id='attA' name='attA' href='javascript:;' attId='" + index + "' onclick='removeFile(this.attId);'>删除</a></div>";
}

function SubmitWithFileThread() {
	webedit.AddField("op", "add")
	webedit.AddField("dirCode", "<%=dirCode%>")
	webedit.Upload();
	window.setTimeout("checkResult()",200);
}

function onDropFile(filePaths) {
	var ary = filePaths.split(",");
	var hasFile = false;
	for (var i=0; i<ary.length; i++) {
		var filePath = ary[i].trim();
		if (filePath!="") {
			hasFile = true;
			webedit.InsertFileToList(filePath);
		}
	}
	if (hasFile)
		SubmitWithFileThread();
}

function OfficeOperate() {
	alert(redmoonoffice.ReturnMessage.substring(0, 4)); // 防止后面跟乱码
}

function checkResult() {
	if (webedit.ReturnMessage.trim() == "<%=correct_result%>") {
		// window.status = addform.webedit.ReturnMessage;
		doAfter(true);
	}
	else
		window.setTimeout("checkResult()",200);
}

function doAfter(isSucceed) {
	if (isSucceed) {
		location.href = "netdisk_public_attach_list.jsp?dir_code=<%=StrUtil.UrlEncode(dirCode)%>";
	}
	else {
		alert(webedit.ReturnMessage);
	}
}

// 编辑文件
function editdoc(id)
{
	// 下句在关闭窗口时，IE会崩溃
	// openWin("weboffice.jsp?id=" + id + "&attachId=" + attachId);
	/*
	window.open("weboffice.jsp?id=" + id + "&attachId=" + attachId);
	return;
	*/
	rmofficeTable.style.display = "";
	redmoonoffice.AddField("id", id);
	redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/netdisk/netdisk_public_office_get.jsp?id=" + id);
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">网盘分享文件
	  </td>
    </tr>
  </tbody>
</table>
<table width="98%" border="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="767" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <!-- <td class="tabStyle_1_title" width="2%"><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td class="tabStyle_1_title" width="6%">&nbsp;</td> -->
      <td width="43%" class="tabStyle_1_title">文件名</td>
      <td width="16%" class="tabStyle_1_title">大小</td>
      <td width="16%" class="tabStyle_1_title">状态</td>
      <td width="25%" class="tabStyle_1_title">日期</td>
    </tr>
	
    <%

while (ir.hasNext()) {
 	PublicShare pse = (PublicShare)ir.next();
	%>
    <tr class="highlight">
      <td><%=pse.getName() %></td>
      <td align="center">1024KB</td>
      <td align="center"><%
	  if("1".equals(pse.getIsEdit())){
	  %>
	  	可编辑
	  <%}else{ %>
	  	只读
	  	<%} %>
	  </td>
      <td align="center"><%=DateUtil.format(pse.getCreateDate(), "yy-MM-dd HH:mm")%></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"><%
	String querystr = "dir_code=" + StrUtil.UrlEncode(dirCode) + "&op="+op;
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table>
</body>
</html>