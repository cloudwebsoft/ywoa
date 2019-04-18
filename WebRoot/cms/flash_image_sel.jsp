<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>图片轮播选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<%@ include file="../inc/nocache.jsp"%>
<style type="text/css">
/*Tooltips*/
.tooltips{
position:relative; /*这个是关键*/
z-index:2;
}
.tooltips:hover{
z-index:3;
background:none; /*没有这个在IE中不可用*/
}
.tooltips span{
display: none;
}
.tooltips:hover span img {
max-width:300px;
width:expression(this.width>300?"300px":this.width);
}
.tooltips:hover span{ /*span 标签仅在 :hover 状态时显示*/
display:block;
position:absolute;
top:21px;
left:9px;
width:5px;
border:0px solid black;
background-color: #FFFFFF;
padding: 3px;
color:black;
}
</style>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "portal")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
	<td valign="middle" class="tdStyle_1">选择图片(仅支持jpg格式的图片)
    </td>
  </tr>
</table>
<br />
<%
			Paginator paginator = new Paginator(request);
			int curpage = paginator.getCurPage();
						
			// out.print("op=" + op + " sql=" + sql);
			
			//String sql = "select id from document_attach where ext='gif' or ext='jpg' or ext='png' order by id";
			String sql = "select id from document_attach where (ext='jpg' or ext='jpeg' or ext='png' or ext='gif') order by id";
			
			String title = ParamUtil.get(request, "title");
			String op = ParamUtil.get(request, "op");
			if (op.equals("search")) {
				//sql = "select a.id from document_attach a, document doc where a.doc_id=doc.id and doc.title like " + StrUtil.sqlstr("%" + title + "%") + " and (a.ext='gif' or a.ext='jpg' or a.ext='png') order by id";
				sql = "select a.id from document_attach a, document doc where a.doc_id=doc.id and doc.title like " + StrUtil.sqlstr("%" + title + "%") + " and (ext='jpg' or ext='jpeg' or ext='png' or ext='gif') order by id";
			}

			Document doc = new Document();
			int pagesize = 10;
			Attachment att = new Attachment();
			ListResult lr = att.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			paginator.init(total, pagesize);
			// 设置当前页数和总页数
			int totalpages = paginator.getTotalPages();
			if (totalpages==0)
			{
				curpage = 1;
				totalpages = 1;
			}
	%>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center">
                <form action="flash_image_sel.jsp" method="get">
                文件标题&nbsp;<input id="title" name="title" value="<%=title%>" />
                <input name="op" value="search" type="hidden" />
                <input type="submit" value="搜索" />
                </form>
                </td>
              </tr>
            </table>
            <br />
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></td>
              </tr>
            </table>
            <table class="tabStyle_1" width="100%" border="0" align="center" cellpadding="2" cellspacing="0">
              <tr align="center">
                <td class="tabStyle_1_title" width="32%">文件</td>
                <td class="tabStyle_1_title" width="26%">附件名</td>
                <td class="tabStyle_1_title" width="24%">类型</td>
                <td class="tabStyle_1_title" width="18%">操作</td>
              </tr>
            <%	
		while (ir!=null && ir.hasNext()) {
			att = (Attachment)ir.next();
			doc = doc.getDocument(att.getDocId());
			if (doc==null) {
				doc = new Document();
				continue;
			}			
		%>
              <tr align="center">
                <td align="left">
                <a class="tooltips" href="#">
                <%=doc.getTitle()%>
				<span><img src="../<%=att.getVisualPath()%>/<%=att.getDiskName()%>"></span></a>
                </td>
                <td><a href="../doc_getfile.jsp?attachId=<%=att.getId()%>" target="_blank"><%=att.getName()%></a></td>
                <td><%=att.getExt()%></td>
                <td>
                <a href="../doc_show.jsp?id=<%=doc.getId()%>" target="_blank">查看</a>&nbsp;&nbsp;
                <a href="javascript:;" onclick="window.opener.setImgUrl('<%=att.getVisualPath()%>/<%=att.getDiskName()%>', '<%=att.getDocId()%>', '<%=doc.getTitle()%>'); window.close()">选择</a></td>
              </tr>
            <%}%>
            </table>
            <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
              <tr>
                <td height="23" align="right" valign="baseline"><%
					String querystr = "op=" + op + "&title=" + StrUtil.UrlEncode(title);
					out.print(paginator.getCurPageBlock("flash_image_sel.jsp?"+querystr));
					%></td>
              </tr>
            </table>
</body>
</html>