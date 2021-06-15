<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<html><head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common.css" rel="stylesheet" type="text/css">
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.netdisk.Directory"/>
<%
int id = 0;

Privilege privilege = new Privilege();
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
String fileName = ParamUtil.get(request, "fileName");
%>
<title>搜索</title>
<script language="JavaScript">
<!--

//-->
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000" style="overflow:auto">
<TABLE width="100%" BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
  <TR valign="top" bgcolor="#FFFFFF">
    <TD width="" height="430" colspan="2" style="background-attachment: fixed; background-image: url(images/bg_bottom.jpg); background-repeat: no-repeat">
          <TABLE cellSpacing=0 cellPadding=0 width="100%">
            <TBODY>
              <TR>
                <TD class=head>
					&nbsp;
				    <%
				UserDb ud = new UserDb();
				ud = ud.getUserDb(userName);
				String strDiskAllow = NumberUtil.round((double)ud.getDiskQuota()/1024000, 3);
				String strDiskHas = NumberUtil.round((double)(ud.getDiskQuota()-ud.getDiskSpaceUsed())/1024000, 3);
				%>
				&nbsp;磁盘份额：<%=strDiskAllow%>M &nbsp;剩余空间：<%=strDiskHas%>M&nbsp;&nbsp;</TD>
              </TR>
            </TBODY>
          </TABLE>
          <table border="0" cellspacing="0" width="100%" cellpadding="0" align="center">
            <tr align="center">
              <td width="90%" align="left" valign="top" bgcolor="#F2F2F2" class="unnamed2">              </td>
            </tr>
            
            <tr>
              <td height="25" align="center" bgcolor="#FFFFFF"><table width="100%"  border="0" cellpadding="0" cellspacing="0" bgcolor="D6D3CE">
                <tr>
                  <td width="40%" align="center"><table width="100%" border="1" cellpadding="0" cellspacing="0" bordercolorlight="#aaaaaa" borderColorDark="#ffffff" bgcolor="D6D3CE">
                      <tr>
                        <td>&nbsp;文件名</td>
                      </tr>
                  </table></td>
                  <td width="13%" bgcolor="#EAE9E6"><table width="100%" border="1" cellpadding="0" cellspacing="0" bordercolorlight="#aaaaaa" borderColorDark="#ffffff" bgcolor="D6D3CE">
                      <tr>
                        <td>&nbsp;大小</td>
                      </tr>
                    </table></td>
                  <td width="20%" bgcolor="#EAE9E6"><table width="100%" border="1" cellpadding="0" cellspacing="0" bordercolorlight="#aaaaaa" borderColorDark="#ffffff" bgcolor="D6D3CE">
                      <tr>
                        <td>&nbsp;上传时间                    </td>
                      </tr>
                  </table></td>
                  <td width="27%" bgcolor="#EAE9E6"><table width="100%" border="1" cellpadding="0" cellspacing="0" bordercolorlight="#aaaaaa" borderColorDark="#ffffff" bgcolor="D6D3CE">
                      <tr>
                        <td>&nbsp;操作</td>
                      </tr>
                  </table></td>
                </tr>
              </table>
			    <%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
Attachment am = new Attachment();
long fileLength = -1;
int pagesize = 20;
int curpage = Integer.parseInt(strcurpage);
String dirCode = ParamUtil.get(request, "dirCode");
String sql = "SELECT id FROM netdisk_document_attach WHERE name like " + StrUtil.sqlstr("%" + fileName + "%") + " and page_num=1";
if (dirCode.equals("")) {
	sql += " and USER_NAME=" + StrUtil.sqlstr(privilege.getUser(request));
}
else {
	Leaf lf = new Leaf();
	lf = lf.getLeaf(dirCode);
	int docId = lf.getDocId();
	sql +=  " and doc_id=" + docId;
}

sql += " order by name";
// out.print(sql);
ListResult lr = am.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
				  // Vector attachments = doc.getAttachments(1);
				  Vector attachments = lr.getResult();
				  Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	am = (Attachment) ir.next(); 
					fileLength = (long)am.getSize()/1024; 
					if(fileLength == 0 && (long)am.getSize() > 0)
						fileLength = 1;  
%>
			    <table width="100%"  border="0" cellpadding="0" cellspacing="0" onMouseOver="this.style.backgroundColor='#FFD6DE'" onMouseOut="this.style.backgroundColor='#ffffff'">
                  <tr>
                    <td width="4%" align="center"><a title="打开文件" target="_blank" href="netdisk_getfile.jsp?id=<%=am.getDocId()%>&attachId=<%=am.getId()%>"><img src="images/<%=am.getIcon()%>" border="0" width="20" height="20"></a></td>
                    <td width="37%">&nbsp; <a title="打开文件" class="mainA" href="netdisk_getfile.jsp?id=<%=am.getDocId()%>&attachId=<%=am.getId()%>" target="_blank"><%=am.getName()%></a></td>
                    <td width="13%"><%=fileLength%>KB</td>
                    <td width="19%"><%=DateUtil.format(am.getUploadDate(), "yyyy-MM-dd HH:mm")%></td>
                    <td width="27%">&nbsp;<a href="dir_change.jsp?attachId=<%=am.getId()%>"><img src="images/rename.gif" alt="重命名及移动文件" width="16" height="16" border="0" align="absmiddle"></a> &nbsp;<a href="javascript:delAttach('<%=am.getId()%>', '<%=am.getDocId()%>')"><img src="images/del.gif" alt="删除" width="16" height="16" border="0" align="absmiddle"></a>&nbsp;&nbsp;<a target="_blank" href="netdisk_downloadfile.jsp?id=<%=am.getDocId()%>&attachId=<%=am.getId()%>"><img src="images/download.gif" alt="下载" width="16" height="16" border="0" align="absmiddle"></a>
                        <%if (!StrUtil.getNullStr(am.getPublicShareDir()).equals("")) {%>
                        <a href="netdisk_public_share.jsp?attachId=<%=am.getId()%>"><img src="images/share_public_yes.gif" alt="已发布" width="16" height="16" border="0" align="absmiddle"></a>
                        <%}else{%>
                        <a href="netdisk_public_share.jsp?attachId=<%=am.getId()%>"><img src="images/share_public.gif" alt="发布" width="16" height="16" border="0" align="absmiddle"></a>
                        <%}%></td>
                  </tr>
                </table>
			  <%}
			  %>			  <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td height="30" align="right">共 <b><%=paginator.getTotal() %></b> 个　每页显示 <b><%=paginator.getPageSize() %></b> 个　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b>
                    <%
	String querystr = "";
    out.print(paginator.getCurPageBlock("?"+querystr));
%>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                </tr>
              </table></td>
            </tr>
      </table>
	</TD>
  </TR>
</TABLE>

<iframe id="hideframe" name="hideframe" src="fwebedit_do.jsp" width=0 height=0></iframe>
</body>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function changeAttachName(attach_id, doc_id, nm) {
	var obj = findObj(nm);
	// document.frames.hideframe.location.href = "fwebedit_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id + "&newname=" + obj.value
	form3.action = "dir_list_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id;
	form3.newname.value = obj.value;
	form3.submit();
}

function delAttach(attach_id, doc_id) {
	if (!window.confirm("您确定要删除吗？")) {
		return;
	}
	document.frames.hideframe.location.href = "dir_list_do.jsp?op=delAttach&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id
}
</script>
</html>