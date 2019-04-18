<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.treasure.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.treasure" key="treasure_use"/> - <%=Global.AppName%></title>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
	return;
}

String userName = privilege.getUser(request);

long id = 0;
try {
	id = ParamUtil.getLong(request, "id");
}
catch (ErrMsgException e) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}

MsgDb md = new MsgDb();
md = md.getMsgDb(id);

String op = ParamUtil.get(request, "op");
if (op.equals("use")) {
	String code = ParamUtil.get(request, "code");
	try {
		TreasureMgr tmg = new TreasureMgr();
		if (tmg.use(request, userName, code, id))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "showtopic.jsp?rootid="+id));
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	}
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">
	</td>
  </tr>
</table>
<br>
<%
int pagesize = 10;
Paginator paginator = new Paginator(request);

TreasureUserDb tu = new TreasureUserDb();
String sql = "select userName,treasureCode from " + tu.getTableName() + " where userName=" + StrUtil.sqlstr(userName);
int total = tu.getObjectCount(sql);
paginator.init(total, pagesize);
int curpage = paginator.getCurPage();
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<div class="tableTitle"><lt:Label res="res.label.forum.treasure" key="treasure_use"/>
&nbsp;-&nbsp;<a href="showtopic_tree.jsp?showid=<%=md.getId()%>"><%=StrUtil.toHtml(md.getTitle())%></a></div>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td valign="top">
      <table class="tableCommon80" width="86%" border="1" align="center" cellpadding="1" cellspacing="0">
	  <thead>
        <tr align="center">
          <td width="17%" height="22"><lt:Label res="res.label.forum.treasure" key="treasure_name"/></td>
          <td width="22%" height="22"><lt:Label res="res.label.forum.treasure" key="buy_date"/></td>
          <td width="21%"><lt:Label res="res.label.forum.treasure" key="count"/></td>
          <td width="26%"><lt:Label key="op"/></td>
        </tr>
	  </thead>
<%
Vector v = tu.list(sql, (curpage-1)*pagesize, curpage*pagesize-1);
TreasureMgr tmg = new TreasureMgr();
Iterator ir = v.iterator();
int i = 0;
while (ir.hasNext()) {
	tu = (TreasureUserDb)ir.next();
	i++;
	String treasureCode = tu.getTreasureCode();
	TreasureUnit tun = tmg.getTreasureUnit(treasureCode);
	if (tun==null) {
		continue;
	}
%>
        <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method="post">
          <tr align="center">
            <td height="22" bgcolor="#FFFFFF">
			<%

			out.print(tun.getName());
			%>
			</td>
            <td height="22" bgcolor="#FFFFFF">
			<%=DateUtil.format(tu.getBuyDate(), "yy-MM-dd")%>
            </td>
            <td bgcolor="#FFFFFF"><%=tu.getAmount()%></td>
            <td height="22" bgcolor="#FFFFFF"><a href="treasure_use.jsp?op=use&code=<%=StrUtil.UrlEncode(tu.getTreasureCode())%>&id=<%=id%>"><lt:Label res="res.label.forum.treasure" key="use"/></a></td>
          </tr>
        </form>
        <%}%>
      </table>
          <table width="86%" border="0" cellspacing="1" cellpadding="3" align="center" class="per80">
            <tr>
              <td height="23" align="right">
<%
	String querystr = "";
    out.print(paginator.getCurPageBlock(request, "?id=" + id + querystr));
%>
              </td>
            </tr>
          </table></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
</div>
<%@ include file="inc/footer.jsp"%>
</div>                        
</body>                                        
</html>                            
  