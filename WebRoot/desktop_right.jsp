<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	String userName = privilege.getUser(request);
	
	// 取得默认门户
	PortalDb pd = new PortalDb();
	pd = pd.getDefaultPortalOfUser(userName);
	if (pd==null) {
		// out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "门户尚未初始化！", true));
		return;
	}

	UserDesktopSetupDb udid = new UserDesktopSetupDb();
	String sql = "select id from " + udid.getTableName() + " where user_name=" + StrUtil.sqlstr(userName) + " and portal_id=" + pd.getLong("id") + " order by td,order_in_td";
	Vector items = udid.list(sql);
	Iterator iItems = items.iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>桌面</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<link href="<%=skinPath%>/main.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="inc/common.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
<style>
.btnIcon {
	cursor:pointer; margin:3px -3px 0px 0px;
}

.btnSpan {
	float:right;
}
</style>
<script>
$(document).ready(function () {
	top.topFrame.hide4Fashion();
});
</script>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0" id="columns">
<tr>
		<td style="width:200px;" valign="top" id="tdSideBar">
        <div id="col_0" class="col_div">
			<%
				DesktopMgr dm = new DesktopMgr();
				iItems = items.iterator();
				while(iItems.hasNext()) {
					udid = (UserDesktopSetupDb)iItems.next();
					if(udid.getTd() == UserDesktopSetupDb.TD_SIDEBAR) {
						DesktopUnit du = dm.getDesktopUnit(udid.getModuleCode());
						if (du==null) {
							out.print("模块3:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
							continue;
						}						
						IDesktopUnit idu = du.getIDesktopUnit();
						if (idu==null) {
							out.print("模块4:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
							continue;
						}
						if (idu instanceof com.redmoon.oa.ui.desktop.IncludeDesktopUnit)
							out.print(idu.display(request, udid));
						else {					
			%>
							<div id="drag_<%=udid.getId()%>" class="portlet drag_div bor" >
								<div id="drag_<%=udid.getId()%>_h" class="box">			
								 <span class="titletxt"><img src="<%=skinPath%>/images/titletype.png" width="8" height="12" /><a href="javascript:;" onclick="addTab('<%=udid.getTitle()%>', '<%=du.getIDesktopUnit().getPageList(request, udid)%>')"><%=udid.getTitle()%></a></span>
								</div>
								<div class="article">			
								<%=du.getIDesktopUnit().display(request, udid)%>
								</div>
							</div>
			<%			}
					}
				}
			%>
        </div>
        </td>
	</tr>
</table>
</body>
<script>
window.onload = function(){
	document.onclick=onClickDoc;
}

function onClickDoc(e) {
	var obj=isIE()? event.srcElement : e.target
	if (isIE() && event.shiftKey) {
		if (obj.tagName=="A") {
			window.open(obj.getAttribute("href"));
			return false;
		}
	}
	if (obj.parentNode) {
		// alert(obj.parentNode.getAttribute("href"));
		if (obj.parentNode.tagName=="A") {
			var href = obj.parentNode.getAttribute("href");
			if (href!=null && href.indexOf("javascript")!=0) {
				addTab(obj.parentNode.innerText, href);
				return false;
			}
		}
		if (obj.parentNode.parentNode) {
			if (obj.parentNode.parentNode.tagName=="A") {
				addTab(obj.parentNode.parentNode.innerText, obj.parentNode.parentNode.getAttribute("href"));
				return false;
			}
		}
		
	}
	if (obj.tagName=="A") {
		var href = obj.getAttribute("href");
		if (href!=null && href.indexOf("javascript")!=0) {
			addTab(obj.innerText, href);
			return false;
		}
	}
}

if(!isIE()){ // firefox innerText define
   HTMLElement.prototype.__defineGetter__("innerText", 
    function(){
     var anyString = "";
     var childS = this.childNodes;
     for(var i=0; i<childS.length; i++) {
      if(childS[i].nodeType==1)
       anyString += childS[i].tagName=="BR" ? '\n' : childS[i].textContent;
      else if(childS[i].nodeType==3)
       anyString += childS[i].nodeValue;
     }
     return anyString;
    } 
   ); 
   HTMLElement.prototype.__defineSetter__("innerText", 
    function(sText){ 
     this.textContent=sText; 
    } 
   ); 
}
</script>
</html>