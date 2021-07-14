<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
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

String sql = "select id from netdisk_public_attach where public_dir=" + StrUtil.sqlstr(dirCode);

String what = ParamUtil.get(request, "what");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (op.equals("search")) {
	sql += " and name like " + StrUtil.sqlstr("%" + what + "%");
}

sql += " order by create_date desc";

// out.print(sql);

PublicAttachment att = new PublicAttachment();
ListResult lr = att.listResult(sql, curpage, pagesize);

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
	else {
		// window.setTimeout("checkResult()",200);
		doAfter(false);
	}
}

function doAfter(isSucceed) {
	if (isSucceed) {
		window.location.href = "netdisk_public_attach_list.jsp?dir_code=<%=StrUtil.UrlEncode(dirCode)%>";
	}
	else {
		alert(webedit.ReturnMessage);
		window.location.href = "netdisk_public_attach_list.jsp?dir_code=<%=StrUtil.UrlEncode(dirCode)%>";
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
	<%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	if (cfg.get("isUseNTKO").equals("true")) {%>
	openWin("netdisk_public_office_ntko_edit.jsp?id=" + id, 1100, 800);	
	<%}else{%>
	rmofficeTable.style.display = "";
	redmoonoffice.AddField("id", id);
	redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/netdisk/netdisk_public_office_get.jsp?id=" + id);
	<%}%>
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">全局共享&nbsp;-&nbsp;<%=publf.getName()%>
	  <%if (lp.canUserManage(privilege.getUser(request))) {%>
	  |&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=publf.getName()%>', '<%=request.getContextPath()%>/admin/netdisk_public_dir_frame.jsp?root_code=<%=publf.getCode()%>')">管理目录</a>
	  <%}%>
	  </td>
    </tr>
  </tbody>
</table>
<table width="98%" border="0" class="percent98" align="center">
  <tr>
    <td width="56%" align="left"><form name="formSearch" action="netdisk_public_attach_list.jsp" method="get">
    <input name="op" type="hidden" value="search" />
    <input name="dir_code" value="<%=dirCode%>" type="hidden" />
    <input name="what" value="<%=what%>" />&nbsp;<input class="btn" type="submit" value="搜索" /></form></td>
    <td width="44%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="767" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" width="2%"><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td class="tabStyle_1_title" width="6%">&nbsp;</td>
      <td width="31%" class="tabStyle_1_title">文件名</td>
      <td width="9%" class="tabStyle_1_title">大小</td>
      <td width="13%" class="tabStyle_1_title">发布者</td>
      <td width="13%" class="tabStyle_1_title">时间</td>
      <td width="26%" class="tabStyle_1_title">操作</td>
    </tr>
	<%if (!publf.getParentCode().equals(Leaf.PARENT_CODE_NONE)) {%>
	  <tr>
		<td align="center">&nbsp;</td>
		<td height="20" align="center"><img src="images/parent.gif" align="absmiddle"></td>
		<td colspan="5" align="left">
		<a href="netdisk_public_attach_list.jsp?dir_code=<%=publf.getParentCode()%>" onMouseUp="curAttachId=''">上级目录</a>					</td>
	  </tr>
	<%}%>
	<%
	Iterator irch = publf.getChildren().iterator();
	while (irch.hasNext()) {
		PublicLeaf clf = (PublicLeaf)irch.next();
	%>
	  <tr>
		<td align="center">&nbsp;</td>
		<td height="20" align="center"><img src="images/folder_01.gif" align="absmiddle"></td>
		<td colspan="5" align="left"><a href="netdisk_public_attach_list.jsp?dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>" onMouseUp="curAttachId=''"><%=clf.getName()%></a></td>
	  </tr>
	<%}%>	
    <%
long fileLength = -1;
UserMgr um = new UserMgr();
Directory dir = new Directory();
DocumentMgr dm = new DocumentMgr();
while (ir.hasNext()) {
 	PublicAttachment am = (PublicAttachment)ir.next();
	lp.setDirCode(am.getPublicDir());
	if (!lp.canUserSeeByAncestor(privilege.getUser(request)))
		continue;	
	fileLength = (long)am.getSize()/1024; 
	if(fileLength == 0 && (long)am.getSize() > 0)
		fileLength = 1;  
	%>
    <tr class="highlight">
      <td><input type="checkbox" id="ids" name="ids" value="<%=am.getId()%>" /></td>
      <td align="center"><a href="netdisk_public_getfile.jsp?id=<%=am.getId()%>" target="_blank"><img src="../netdisk/images/<%=am.getIcon()%>" border="0"></a></td>
      <td><a href="netdisk_public_getfile.jsp?id=<%=am.getId()%>" target="_blank"><%=am.getName()%></a></td>
      <td align="center"><%=fileLength%>&nbsp;KB</td>
      <td align="center"><%
	  UserDb ud = um.getUserDb(am.getUserName());
	  %>
	  <a href="../user_info.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank"><%=ud.getRealName()%></a>      </td>
      <td align="center"><%=DateUtil.format(am.getCreateDate(), "yy-MM-dd HH:mm")%></td>
      <td align="center"><a href="netdisk_public_getfile.jsp?id=<%=am.getId()%>&attId=<%=am.getAttId()%>" target="_blank">打开</a>&nbsp;&nbsp;&nbsp;&nbsp;
      <a target="_blank" href="netdisk_public_downloadfile.jsp?id=<%=am.getId()%>">下载</a>
    <%if (lp.canUserManage(privilege.getUser(request))) {%>
		&nbsp;&nbsp;<a href="javascript:;" onClick="if (confirm('您确定要删除吗？')) window.location.href='netdisk_public_attach_list.jsp?op=del&dir_code=<%=StrUtil.UrlEncode(dirCode)%>&id=<%=am.getId()%>'">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="netdisk_public_dir_change.jsp?attachId=<%=am.getId()%>&privurl=<%=StrUtil.getUrl(request)%>">重命名</a>
	<%}%>
<%
	if (am.getAttId()==0 && lp.canUserModify(privilege.getUser(request))) {
		if (am.getExt()!=null) {
			if (am.getExt().equals("doc") || am.getExt().equals("xls") || am.getExt().equals("ppt")) {%>
			&nbsp;<a href="javascript:editdoc('<%=am.getId()%>')">编辑</a>
			<%}
		}
	}%>
	</td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="33%" align="left">
    
    </td>
    <td width="67%" align="right"><%
	String querystr = "dir_code=" + StrUtil.UrlEncode(dirCode) + "&op="+op + "&what=" + StrUtil.UrlEncode(what);
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0" name="ctlTable" id="ctlTable" style="border-top:1px dashed #cccccc; display:">
  <tr>
    <td><table  border="0" align="center" cellpadding="0" cellspacing="1">
      <tr>
        <td><div style="width:400;margin-top:10px;border:1px solid #cccccc">
          <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,2,0,1" width="400" style="height:75px" align="middle" id="webedit">
            <param name="Encode" value="utf-8" />
            <param name="MaxSize" value="<%=Global.MaxSize%>" />
            <!--上传字节-->
            <param name="ForeColor" value="(0,0,0)" />
            <param name="BgColor" value="(255,255,255)" />
            <param name="ForeColorBar" value="(255,255,255)" />
            <param name="BgColorBar" value="(104,181,200)" />
            <param name="ForeColorBarPre" value="(0,0,0)" />
            <param name="BgColorBarPre" value="(230,230,230)" />
            <param name="FilePath" value="<%=filePath%>" />
            <param name="Relative" value="1" />
            <!--上传后的文件需放在服务器上的路径-->
            <param name="Server" value="<%=request.getServerName()%>" />
            <param name="Port" value="<%=request.getServerPort()%>" />
            <param name="VirtualPath" value="<%=Global.virtualPath%>" />
            <param name="PostScript" value="<%=Global.virtualPath%>/netdisk/netdisk_public_attach_do.jsp" />
            <param name="PostScriptDdxc" value="" />
            <param name="SegmentLen" value="204800" />
            <param name="info" value="文件拖放区" />
			<%
            License license = License.getInstance();	  
            %>
            <param name="Organization" value="<%=license.getCompany()%>">
            <param name="Key" value="<%=license.getKey()%>">            
          </object>
        </div>
        </td>
      </tr>
    </table></td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="40" align="center"><input class="btn" name="button24" type="button" onclick="moveBatch()" value="移动" />
      &nbsp;<input class="btn" style="margin-left:3px" name="button2" type="button" onclick="delBatch()" value="删除" />
      &nbsp;
        <!--共 <b><%=paginator.getTotal() %></b> 个　每页显示 <b><%=paginator.getPageSize() %></b> 个　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b>-->
        <%
	// String querystr = "op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&dir_code=" + StrUtil.UrlEncode(dir_code);
    // out.print(paginator.getCurPageBlock("?"+querystr));
%>
        <input class="btn" name="button" type="button" onclick="webedit.OpenFileDlg();if (webedit.GetFiles()=='') return false; else SubmitWithFileThread()" value="上传文件" />
      &nbsp;
        <input class="btn" name="button" type="button" onclick="webedit.OpenFolderDlg();if (webedit.GetFiles()=='') return false; else SubmitWithFileThread()" value="上传目录" />
      &nbsp;
        <input class="btn" name="button" type="button" onclick="webedit.StopUpload()" value="停止上传" />
  <div id="attFiles"></div></td></tr>
</table>
		  <table id="rmofficeTable" name="rmofficeTable" style="display:none;margin-top:10px" width="29%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#cccccc">
            <tr>
              <td height="22" align="center" bgcolor="#eeeeee"><strong>&nbsp;编辑Office文件</strong></td>
            </tr>
            <tr>
              <td align="center"><div style="width:400px;height:43"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="../activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                  <param name="Encode" value="utf-8" />
                  <param name="BackColor" value="0000ff00" />
                  <param name="Server" value="<%=Global.server%>" />
                  <param name="Port" value="<%=Global.port%>" />
                  <!--设置是否自动上传-->
                  <param name="isAutoUpload" value="1" />
                  <!--设置文件大小不超过1M-->
                  <param name="MaxSize" value="<%=Global.FileSize%>" />
                  <!--设置自动上传前出现提示对话框-->
                  <param name="isConfirmUpload" value="1" />
                  <!--设置IE状态栏是否显示信息-->
                  <param name="isShowStatus" value="0" />
                  <param name="PostScript" value="<%=Global.virtualPath%>/netdisk/netdisk_public_office_upload.jsp" />
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />                  
                </object></div>
                <!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />--></td>
            </tr>
          </table>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="ids" type="hidden" />
<input name="dir_code" value="<%=dirCode%>" type="hidden" />
</form>
</body>
</html>