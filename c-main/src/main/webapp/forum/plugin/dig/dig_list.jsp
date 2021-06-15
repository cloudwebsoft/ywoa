<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.plugin.dig.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="pragma" content="no-cache">
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>挖掘信息列表 - <%=Global.AppName%></title>
<body>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<%@ include file="../../inc/position.jsp"%>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class=table_list>
  <tr>
    <td align="center">
    <strong><font color="#6666DF">挖掘记录</font></strong></td>
  </tr>
</table>
<br>
<%
long msgId = ParamUtil.getLong(request, "msgId");
MsgDb md = new MsgDb();
md = md.getMsgDb(msgId);

DigConfig dc = DigConfig.getInstance();

               int digRecordSecretLevel = dc.getIntProperty("digRecordSecretLevel");
                // 0 public 1 owner 2 manager
                boolean canShowRecord = false;
                if (digRecordSecretLevel==0)
                    canShowRecord = true;
                else if (digRecordSecretLevel==1) {
                    Privilege pvg = new Privilege();
                    if (pvg.isUserLogin(request)) {
                        if (pvg.getUser(request).equals(md.getName())) {
                            canShowRecord = true;
                        }
                    }
                }
                else if (digRecordSecretLevel==2) {
                    Privilege pvg = new Privilege();
                    if (pvg.isManager(request, md.getboardcode()))
                        canShowRecord = true;
                }
                
				if (!canShowRecord) {
					response.sendRedirect("../../../info.jsp?info=" + StrUtil.UrlEncode("您没有查看挖掘记录的权限!"));
					return;
				}

int pagesize = 10;
Paginator paginator = new Paginator(request);

DigDb digDb = new DigDb();

String sql = "select id from " + digDb.getTable().getName() + " where msg_id=" + msgId + " order by dig_date desc";

long total = digDb.getQObjectCount(sql, "" + msgId);
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
<table width="98%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="98%"  border="1" align="center" cellpadding="0" cellspacing="0" class="tableCommon">
  <thead>
    <td width="15%">用户</td>
    <td width="21%">积分类别</td>
    <td width="22%" height="22">积分消耗</td>
    <td width="22%" height="22">得分</td>
    <td width="20%">日期</td>
  </thead>
  <%
String querystr = "msgId=" + msgId;

QObjectBlockIterator qi = digDb.getQObjects(sql, ""+msgId, (curpage-1)*pagesize, curpage*pagesize);

int i = 0;
ScoreMgr sm = new ScoreMgr();
UserMgr um = new UserMgr();
while (qi.hasNext()) {
	digDb = (DigDb)qi.next();
	ScoreUnit su = sm.getScoreUnit(digDb.getString("score_code"));
	UserDb user = um.getUser(digDb.getString("user_name"));
	i++;
%>
    <tr>
      <td><a href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(digDb.getString("user_name"))%>"><%=user.getNick()%></a></td>
      <td><%=su.getName(request)%></td>
      <td height="28"><%=digDb.getInt("pay")%></td>
      <td height="28"><%=NumberUtil.round(digDb.getDouble("reward"), 3)%></td>
      <td><%=ForumSkin.formatDateTime(request, digDb.getDate("dig_date"))%></td>
    </tr>
<%}%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr>
    <td height="23" align="right">
<%
    out.print(paginator.getCurPageBlock(request, "dig_list.jsp?"+querystr));
%>
    </td>
  </tr>
</table>
<br>

</td>
</tr>
</table>
</td>
</tr>
</table>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</body>                                        
</html>                            
  