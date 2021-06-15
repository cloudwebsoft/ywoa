<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.treasure.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
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
<title><lt:Label res="res.label.forum.treasure" key="treasure_show"/> - <%=Global.AppName%></title>
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
String code = ParamUtil.get(request, "code");
if (code.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "res.label.forum.treasure", "need_code")));
	return;
}
TreasureMgr tmr = new TreasureMgr();
TreasureUnit tu = tmr.getTreasureUnit(code);

String userName = privilege.getUser(request);

%>
<br>
<div class="tableTitle"><lt:Label res="res.label.forum.treasure" key="treasure_show"/></div>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr> 
    <td valign="top">
      <br>
      <table class="tableCommon60" width="43%" border="0" align="center" cellpadding="5" cellspacing="1">
	  <thead>
        <tr>
          <td height="24" colspan="2" align="center"><%=tu.getName()%></td>
        </tr>
	  </thead>
        <tr align="center">
          <td height="22" colspan="2"><%=tu.getDesc()%></td>
        </tr>
        <tr>
          <td width="48%" height="130" rowspan="2" align="center"><img src="<%=tu.getImage()%>"> </td>
          <td width="52%" height="55" valign="top"><lt:Label res="res.label.forum.treasure" key="buy_point"/>
            <br>
              <%
			  Vector pricev = tu.getPrice();
			  Iterator pir = pricev.iterator();
			  ScoreMgr sm = new ScoreMgr();
			  while (pir.hasNext()) {
			  	TreasurePrice tp = (TreasurePrice)pir.next();
			  	ScoreUnit su = sm.getScoreUnit(tp.getScoreCode());
			  %>
              <%=su.getName()%>&nbsp;<%=tp.getValue()%>&nbsp;
			  <%}%>			  
              <br>
          <lt:Label res="res.label.forum.treasure" key="day_count"/>
          <%=tu.getDay()%> </td>
        </tr>
        <tr>
          <td height="22"><lt:Label res="res.label.forum.treasure" key="store_count"/>
            <%=tu.getCount()%></td>
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
  