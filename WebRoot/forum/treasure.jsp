<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
// seo
com.redmoon.forum.util.SeoConfig scfg = new com.redmoon.forum.util.SeoConfig();
String seoTitle = scfg.getProperty("seotitle");
String seoKeywords = scfg.getProperty("seokeywords");
String seoDescription = scfg.getProperty("seodescription");
String seoHead = scfg.getProperty("seohead");
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.treasure" key="treasure"/> - <%=Global.AppName%> <%=seoTitle%></title>
<%=seoHead%>
<META name="keywords" content="<%=seoKeywords%>">
<META name="description" content="<%=seoDescription%>">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
%>
<br>
<div class="tableTitle"><lt:Label res="res.label.forum.treasure" key="treasure"/></div>
<table width="98%" class="per100" border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr> 
    <td valign="top">
      <table width="92%"  border="0" align="center" cellpadding="0" cellspacing="0">
        <%
	TreasureMgr em = new TreasureMgr();
	Vector v = em.getAllTreasure();
	Iterator ir = v.iterator();
	TreasureUnit tu;
	int i = 0;
	while (ir.hasNext()) {
	%>
        <tr align="center">
          <td height="24">
            <%
			for (i=0; i<3; i++) {
				if (ir.hasNext()) {
					tu = (TreasureUnit)ir.next();
			%>
			<table class="tableCommon30" width="32%"  border="1" align="left" cellpadding="1" cellspacing="0">
			<thead>
              <tr>
                <td height="24" colspan="2" align="center"><%=tu.getName()%></td>
              </tr>
			</thead>
              <tr align="center">
                <td height="22" colspan="2"><%=tu.getDesc()%></td>
              </tr>
              <tr>
                <td width="48%" height="130" rowspan="3" align="center">
				<img src="<%=tu.getImage()%>">
				</td>
              <td width="52%" height="55" valign="top">
			  <lt:Label res="res.label.forum.treasure" key="buy_point"/><br>
			  <%
			  Vector pricev = tu.getPrice();
			  Iterator pir = pricev.iterator();
			  ScoreMgr sm = new ScoreMgr();
			  while (pir.hasNext()) {
			  	TreasurePrice tp = (TreasurePrice)pir.next();
			  	ScoreUnit su = sm.getScoreUnit(tp.getScoreCode());
				out.print(su.getName() + "：" + tp.getValue() + "<BR>");
			  } 
			  %>
              <br>
              <lt:Label res="res.label.forum.treasure" key="day_count"/><%=tu.getDay()%>
				</td>
              </tr>
              <tr>
                <td height="22"><lt:Label res="res.label.forum.treasure" key="store_count"/><%=tu.getCount()%></td>
              </tr>
              <tr>
                <td height="22">
					<a href="treasure_buy.jsp?code=<%=StrUtil.UrlEncode(tu.getCode())%>"><lt:Label res="res.label.forum.treasure" key="buy"/></a>
				<%if (tu.getCount()>0) {%>
				<%}%>
				</td>
              </tr>
            </table>
			<table width="1%"  border="0" align="left" cellpadding="1" cellspacing="1">
              <tr>
                <td>&nbsp;</td>
              </tr>
            </table>
			<%	}
				else
					break;
			}%>
		  </td>
        </tr>
		<tr><td height=5></td></tr>
    <%}%>
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
  