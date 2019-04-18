<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int id = 0;
String dirCode = ParamUtil.get(request, "dir_code");
boolean isDirArticle = false;
Leaf lf = new Leaf();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			id = lf.getDocID();
			isDirArticle = true;
		}
	}
}

if (id==0) {
	try {
		id = ParamUtil.getInt(request, "id");
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.makeErrMsg(e.getMessage()));
		return;
	}
}
Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
doc = docmgr.getDocument(id);
if (doc!=null) {
	// 使点击量增1
	doc.increaseHit();
}
if (!isDirArticle)
	lf = lf.getLeaf(doc.getDirCode());

String op = ParamUtil.get(request, "op");
String view = ParamUtil.get(request, "view");
CommentMgr cm = new CommentMgr();
if (op.equals("addcomment")) {
	try {
		cm.insert(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

if (op.equals("vote")) {
	try {
		docmgr.vote(request,id);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>查看模板 - <%=doc.getTitle()%></title>
<link href="../common.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
.style1 {
	font-size: 14px;
	font-weight: bold;
}
-->
</style>
</head>
<body>
<table width="769" border="0" cellpadding="0" cellspacing="0" >
  <tr>
    <td width="1" bgcolor="#CCCCCC"></td>
    <td width="593" valign="top"><TABLE BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
      <TR valign="top" bgcolor="#FFFFFF">
        <TD width="662" height="260"><table width="581"  border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
              <td width="581">&nbsp;</td>
            </tr>
            <tr>
              <td height="40" align="center">
			  
			  <%if (doc.isLoaded()) {%>
			  <b><font size="3"> <%=doc.getTitle()%></font></b>&nbsp;[<%=doc.getModifiedDate()%>]
			  <%}else{%>
			  未找到该文章！
			  <%}%>
			  </td>
            </tr>
            <tr>
              <td height="35"><%=doc.getContent(1)%><br>
                <br>
                </td>
            </tr>
            <tr>
              <td valign="top"><%if (doc.getType()==1 && (op.equals("") || !op.equals("vote"))) {
					String[] voptions = doc.getVoteOption().split("\\|");
					int len = voptions.length; %>
                  <table width="100%" >
                    <form action="../?op=vote" name=formvote method="post">
                      <input type=hidden name=op value="vote">
                      <input type=hidden name=id value="<%=doc.getID()%>">
                      <%for (int k=0; k<len; k++) { %>
                      <tr>
                        <td width="5%"><%=k+1%>、 </td>
                        <td width="73%"><input class="n" type=radio name=votesel value="<%=k%>">
                            <%=voptions[k]%> </td>
                        <td>&nbsp;</td>
                      </tr>
                      <% } %>
                      <tr>
                        <td colspan="2" align="center"><input name="Submit" type="submit" class="btn" value=" 投  票 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <input name="btn" type="button" class="btn" value="查看结果" onClick="window.location.href='doc_show.jsp?id=<%=id%>?view=result'"></td>
                        <td width="22%">&nbsp;</td>
                      </tr>
                    </form>
                  </table>
                  <%}%>
                  <br>
                  <%if (view.equals("result") || op.equals("vote")) {
					String[] result = doc.getVoteResult().split("\\|");
					int len = result.length;
					int[] re = new int[len];
					int[] bfb = new int[len];
					int total = 0;
					for (int k=0; k<len; k++) {
						re[k] = Integer.parseInt(result[k]);
						total += re[k];
					}
					if (total!=0) {
						for (int k=0; k<len; k++) {
							bfb[k] = (int)Math.round((double)re[k]/total*100);
						}
					}
		%>
                  <table class=p9 width="98%" border="0" cellpadding="0" cellspacing="1" height="100">
                    <% for (int k=0; k<len; k++) { %>
                    <tr bgcolor="#FEF2E9">
                      <td width="5%"><%=k+1%>、</td>
                      <td width="59%"><img src=../images/bar.gif width=<%=bfb[k]*2%> height=10></td>
                      <td width="17%" align="right"><%=re[k]%>人</td>
                      <td width="19%" align="right"><%=bfb[k]%>%</td>
                    </tr>
                    <%}%>
                    <tr bgcolor="#FEF2E9">
                      <td colspan="4" align="center">共有<%=total%>人参加调查</td>
                    </tr>
                  </table>
                  <%}%>
              </td>
            </tr>
            <tr>
              <td height="34" valign="top">&nbsp;</td>
            </tr>
        </table></TD>
      </TR>
    </TABLE>
      <br>
      
    </td>
  </tr>
</table>
</body>
</html>
