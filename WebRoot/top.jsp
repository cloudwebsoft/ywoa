<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.net.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.util.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "java.io.*"%>
<%
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	int leftWidth = skin.getLeftWidth();
	License license = License.getInstance();
	String enterpriseNum = license.getEnterpriseNum();
	String enterpriseName = StrUtil.UrlEncode(license.getName());
	String id = license.getId();
	String type = license.getType();
	String roleCode= "";
	JdbcTemplate jTTemplate = new JdbcTemplate();
	String roleSQL = "select code from user_role order by orders desc";
	try {
		ResultIterator ri = jTTemplate.executeQuery(roleSQL);
		if (ri!=null&&ri.hasNext()){
			ResultRecord rd = (ResultRecord) ri.next();
			roleCode = rd.getString(1);
		}
	} catch (Exception e){
		LogUtil.getLog(getClass()).error("获取最大角色出现异常："+StrUtil.trace(e));
	} finally{
		if (jTTemplate!=null)
			jTTemplate.close();
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8" />
<title>top</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<link href="<%=skinPath%>/main.css" rel="stylesheet" type="text/css" />
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script src="js/jquery-ui/jquery-ui.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
var now=new Date();
var yy=now.getYear();
if (!isIE() || isIE9 || isIE10 || isIE11)
	yy = 1900 + yy;
var MM=now.getMonth()+1;
var dd=now.getDate();
var DD=now.getDay();
var x = new Array("星期日","星期一","星期二","星期三","星期四","星期五","星期六");
var date = yy+"年"+MM+"月"+dd+"日"+"  "+x[DD]+"  ";//+"<img src=\"<%=skinPath%>/images/OAimg04.gif\" />";
function refreshCalendarClock(){
	var now=new Date();
	var hh=now.getHours();
	var mm=now.getMinutes();
	var ss=now.getTime()%60000;
	ss=(ss-(ss%1000))/1000;
	if(hh<10)hh="0"+hh;
	if(mm<10)mm="0"+mm;
	if(ss<10)ss="0"+ss;
	$("#spanDate").html(date);
	$("#spanTime").html(hh+":"+mm+":"+ss);
}

function init() {
	window.setInterval('refreshCalendarClock()',1000);
	//获取光标
	//document.body.focus();  
	//禁止退格键 作用于Firefox、Opera   
    document.onkeypress = banBackSpace;  
    //禁止退格键 作用于IE、Chrome  
    document.onkeydown = banBackSpace; 
}

function enterTop(){
	try {
		//删除所有一级菜单选中的样式,并隐藏子级菜单层
		top.mainFrame.removeAllMenusClass();
	}
	catch (e) {}
}

//阻止事件冒泡
function stopBubble(e) {
   if (e && e.stopPropagation)
       e.stopPropagation()
   else
       window.event.cancelBubble=true
}
function onClickDoc(e) {
	if (isIE() && e.shiftKey)
	 	return;
	var obj = e.target;
	if (!obj || obj.id=="")
		return;
	var target =  document.getElementById(obj.id).getAttribute("target");
	var link = document.getElementById(obj.id).getAttribute("link");
	var canRepeat = document.getElementById(obj.id).getAttribute("canRepeat");
	if (obj.tagName=="IMG" || obj.tagName=="SPAN") {
	    if (target=="_blank") {
                if (link == null) {
                    return false;
                }
                window.open(link);
                stopBubble(e);
                return false;   
            }
            else if (target=="_top") {
                window.top.location.href = link;
                return false;
            }
		if (target=="mainFrame") {
			// 检查是否已有同样的title的选项卡
			var isAdded = false;
			var tabs = top.mainFrame.tabpanel.tabs;
			var tabsId;
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].title.text()==obj.title) {
					isAdded = true;
					tabsId = tabs[i].id;
					break;
				}
			}
			if (!isAdded) {
				top.mainFrame.addTab(obj.title, link);
			}
			else {
				var isActive = false;
				if (canRepeat) {
					if (canRepeat=="false")
						isActive = true;
				}
			
				if (isActive) {
					// 如果不允许重复，则激活
					top.mainFrame.addTab(obj.title, link, tabsId);
					
					var position = top.mainFrame.tabpanel.getTabPosision(tabsId);
					var iframes = top.mainFrame.tabpanel.tabs[position].content.find('iframe');
					iframes[0].src = iframes[0].src;	//刷新，注意不能用iframes[0].location.reload()，这样会失去tab
				}
				else {
					top.mainFrame.addTab(obj.title, link);				
				}
			}
			e.preventDefault();
			return false;
		}
	}
}

$(document).bind("click", function(e){
	onClickDoc(e);
	if (e.button==0){ //hide all flex menus (and their sub ULs) when left mouse button is clicked
		window.top.mainFrame.hideMenu();
	}
})

function checkLeave() {
	// event.returnValue="您还没有保存文件，确定要关闭么？";
}
function showConfig(name,url)
{
	addTab(name, url);
}
function refreshTab() 
{
		if (window.top.mainFrame.tabpanel) {
			var ifrm = window.top.mainFrame.tabpanel.getActiveTab().content.find('iframe');
			ifrm.attr("src", ifrm.attr("src"));
		}
		return;	
}
function exitOa()
{
	if (window.top.mainFrame.tabpanel) {
	   window.top.mainFrame.exitOa();
	}
	
}
function changeClass(obj)
{
	jQuery(obj).toggleClass("quickIcon");
}
//隐藏菜单及搜索功能
function hide4Fashion()
{
	jQuery("#tab").hide();
	jQuery("#img").hide();
}

function changeSkin(){
	alert('换肤');
}
</script>
</head>
<body onload="init()"  onbeforeunload="checkLeave()" onmouseover="enterTop();">
<%
com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(pvg.getUser(request));
String desktopUrl;
int uiMode = ParamUtil.getInt(request, "uiMode", -1);
if (uiMode==UserSetupDb.UI_MODE_FASHION) {
	desktopUrl = "desktop_frame.jsp";
}
else {
	if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION)
		desktopUrl = "desktop.jsp";
	else
		desktopUrl = "desktop_frame.jsp";
}
boolean isMaxRole = false;
UserDb user = new UserDb();
user = user.getUserDb(pvg.getUser(request));
String username = user.getName();
RoleDb[] roledbArr = user.getRoles();
if (roledbArr!=null&&roledbArr.length>0){
	for(int i=0;i<roledbArr.length;i++){
		RoleDb roledb = roledbArr[i];
		String code = roledb.getCode();
		if (code.equals(roleCode)){
			isMaxRole = true;
			break;
		}
	}
}
%>
<div id="top" class="oatop">
	<div class="upbox">
	<div class="topleft"><img src="<%=skinPath%>/images/logo.png"/></div>
	<div class="topright">
		<!--用户名/时间-->
		<span id="spanUserName" title="<%=user.getRealName()%>" link="user/user_edit.jsp" canRepeat="false" class="spanUserName" target="mainFrame">
		<%if (user.getGender()==0) {%>
		<img src="images/man.png" width="24" height="24" />
		<%}else{%>
		<img src="images/woman.png" width="24" height="24" />
		<%}%>
			&nbsp;<%=user.getRealName() %>
			<!--&nbsp;&nbsp;&nbsp;&nbsp;<span id="spanDate"></span><img src="<%=skinPath%>/images/OAimg04.gif" /><span id="spanTime"></span>-->
		</span>
            <span><img src="<%=skinPath%>/images/toplie.png" width="2" height="24" /></span>
            <%
			LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.CODE_BOTTOM);
			Iterator ir = lccm.getChildren().iterator();
			while (ir.hasNext()) {
				Leaf lf = (Leaf)ir.next();
				if (!lf.canUserSee(request))
					continue;
			%>
				<span><img src="<%=skinPath%>/icons/<%=lf.getIcon()%>" width="24" height="24" style="cursor:pointer" id="<%=lf.getName()%>" title="<%=lf.getName()%>" canRepeat="<%=lf.isCanRepeat()%>" hidefocus="true" link="<%=lf.getLink(request)%>" target="<%=lf.getTarget()%>" class="quickIcon" onmouseover="changeClass(this)" onmouseout="changeClass(this)"/></span>
			<%}%>	
			<!--刷新图标-->
			<span><img src="<%=skinPath%>/images/refresh.png" width="24" height="24" title="刷新" onclick="refreshTab();" style="cursor:pointer" class="quickIcon" onmouseover="changeClass(this)" onmouseout="changeClass(this)"/></span>
			<!--退出图标-->
			<span><img src="<%=skinPath%>/images/signout.png" width="24" height="24" title="退出" onclick="exitOa()" style="cursor:pointer" class="quickIcon" onmouseover="changeClass(this)" onmouseout="changeClass(this)"/></span>
 
  </div>
  </div>
<div class="downbox" >

<div class="goldCode">

<% 
//金牌服务按钮
if ("admin".equals(username)||isMaxRole){
	String licenseType = License.getInstance().getType();
	if(!licenseType.equals(License.TYPE_OEM)){
%>
		<div id="gold_medal_service" style="margin-top: 10px; display:none" class="gold_medal_service2" onclick="linkToGoldMedalService()" title="金牌服务保障体系期待您的加入"></div>
<% 
	}
}
//二维码按钮
if(pvg.isUserPrivValid(request, "admin")){
%>
	<div id="QRCode_printer" style="margin-top: 10px; margin-right:5px;" class="QRCode_printer" onclick="linkToQRCode()" title="下载二维码"></div>
<% 
}
%>
<%if(!license.isPlatformSrc()) {%>
<div style="width: 100px;margin-top:10px;float: right;margin-right: 150px;height: 20px;font-size: 14px;"><a href="http://www.yimihome.com" title="云网OA" target="_blank" >云网OA标准版</a></div>
<%}%>
</div>
	<div class="userinf">
<!--收起箭头按钮-->
<%
if (pvg.isUserPrivValid(request, "admin") || !pvg.isUserPrivValid(request, "menu.main.forbid")) {
%>
<div class="putaway" onclick="hideLeft(this);" title="折叠左侧菜单">
<a href="javascript:;"><img src="<%=skinPath%>/images/putawayico.png" width="27" height="27" id="img"/></a>
</div>
<div class="showOnLine1" style="display:block;float:left;margin-top:15px;margin-left:10px" onclick="online();" title="在线人数"><a href="javascript:;"><img src="<%=skinPath%>/images/user_2.png" width="16" height="16" id="img"/></a></div>
<div class="showOnLine2" style="display:none;float:left;margin-top:15px;margin-left:10px" onclick="online();" title="在线人数"><a href="javascript:;"><img src="<%=skinPath%>/images/user_1.png" width="16" height="16" id="img"/></a></div>
<script>
$(function() {
	// 如果是经典传统菜单型界面，则不显示“在线人数”图标
	if (window.top.location.href.indexOf("oa_main")!=-1) {
		$('.showOnLine1').hide();
	}
});
</script>
<%
}
%>
 <div class="username">
 		<%
            PortalDb pd = new PortalDb();
            String sql = "select id from " + pd.getTable().getName() + " where (user_name='system' or user_name=?) order by is_fixed desc, orders asc";
    		if (pvg.isUserPrivValid(request, "desk.default.forbid")) {
            	sql = "select id from " + pd.getTable().getName() + " where (user_name='system' or user_name=?) and orders<>1 order by is_fixed desc, orders asc";    		
			}            
            Iterator pdir = pd.list(sql, new Object[]{pvg.getUser(request)}).iterator();
            while (pdir.hasNext()) {
                pd = (PortalDb)pdir.next();
                boolean canSee = true;
                if (pd.getString("user_name").equals("system")) {
                	canSee = pd.canUserSee(pvg.getUser(request));
                }
                if (canSee) {
        %>
            	<span  style="cursor:pointer;" id="<%=pd.getString("name")%>" title="<%=pd.getString("name")%>" canRepeat="false" hidefocus="true" link="desktop.jsp?portalId=<%=pd.getLong("id")%>" target="mainFrame">
                <img src="<%=skinPath%>/icons/<%=pd.getString("icon") %>" width="23" height="23"  class=""/> 
                <%=pd.getString("name")%>
           		</span>
        <%
        		}
        }
	    //LeafChildrenCacheMgr configLf = new LeafChildrenCacheMgr("1131829842");
	    if (pvg.isUserPrivValid(request, "admin")) {
	        Leaf leaf = new Leaf();
	        Leaf configLf = leaf.getLeaf("747765919");
	        if (configLf != null && configLf.isUse())
	        {
	    %>
			<span id="openConfig" class="openConfig">
			    <img src="<%=skinPath%>/images/configFunction.png" width="17px" height="20px"/>
			    <SPAN id="configSpan" class="configSpan" title="<%=configLf.getName()%>" canRepeat="<%=configLf.isCanRepeat()%>" hidefocus="true" link="<%=configLf.getLink(request)%>" target="<%=configLf.getTarget()%>">操作引导</SPAN>
			</span>
			<%}} %>
        </div>
<!--快速入口--><div class="entrance" onmouseover="showQuickMenu()" style="cursor:pointer;display:none;"></div>
    </div>
	
	</div>
</div>
</div>
</div>

</body>
<script>
function online(){
	var flag1 = $(".showOnLine1").css("display");
	var flag2 = $(".showOnLine2").css("display");
	if(flag1 == "block"){
		$(".showOnLine1").css({"display":"none"});
		$(".showOnLine2").css({"display":"block"});
		window.top.mainFrame.online();
	}else{
		$(".showOnLine2").css({"display":"none"});
		$(".showOnLine1").css({"display":"block"});
		window.top.mainFrame.closeOnline();
	}
}
function type2(){
	$(".showOnLine2").css({"display":"none"});
	$(".showOnLine1").css({"display":"block"});
}
function showQuickMenu()
{
	try	{
		top.mainFrame.show();
	}
	catch(e) {
	}
}

function hideLeft()
{
	if (jQuery("#tab"))
	{
		jQuery("#tab").toggle();
	}
	if (jQuery("#img").attr("src") == '<%=skinPath%>/images/putawayico.png')
	{
		var s = "top.middleFrame.cols = \"" + 0 + ",*\"";
		eval(s);
		jQuery("#img").attr("src","<%=skinPath%>/images/putawayico2.png");
		
		jQuery(".putaway").attr("title", "展开左侧菜单");
	}
	else
	{
		var mLeftWidth = 244;
		if (window.top.location.href.indexOf("oa_main.jsp")==-1) {
			mLeftWidth = <%=leftWidth%>;
		}
		var s = "top.middleFrame.cols = \"" + mLeftWidth + ",*\"";
		eval(s);
		jQuery("#img").attr("src","<%=skinPath%>/images/putawayico.png");
		jQuery(".putaway").attr("title", "折叠左侧菜单");
	}
}
jQuery(document).ready(function(){
       if(jQuery("#openConfig"))
       {
        var options = {};
        var selectedEffect = "pulsate";
        $("#openConfig").show(selectedEffect, options, 1000, callback);
       } 
       // 金牌服务交互  
       /*
       $.getJSON("http://service.yimihome.com/public/service_status.jsp?customerNo=<%=enterpriseNum%>&name=<%=enterpriseName%>&id=<%=id%>&type=<%=type%>&callback=?",function(json){
			if (json.ret==1){
				$("#gold_medal_service").attr("class","gold_medal_service1");
				$("#gold_medal_service").attr("title",json.msg);
			}
			if (json.ret==0){
				$("#gold_medal_service").attr("class","gold_medal_service2");
				$("#gold_medal_service").attr("title",json.msg);
			}
		});
		*/
});
function linkToGoldMedalService(){
	addTab("金牌服务","<%=request.getContextPath()%>/admin/service.jsp");
}
function linkToQRCode(){
	addTab("二维码","<%=request.getContextPath()%>/admin/printerQRCode.jsp");
}
function callback(){
    
}
function hideConfig()
{
    
    if ($("#openConfig"))
    {
        //此处再次调用定时器，解决chrome及360浏览器定时器5秒无法执行代码问题
        setTimeout(function(){$("#openConfig").hide()},25000);//animate({opacity:'hide'},25000)
   }
}
setTimeout(function(){hideConfig()}, 5000);
 
//解决页面按了退格键，回到登陆页bug
function banBackSpace(e){
    var ev = e || window.event;//获取event对象
    var obj = ev.target || ev.srcElement;//获取事件源
    var t = obj.type || obj.getAttribute('type');//获取事件源类型
    //获取作为判断条件的事件类型
    var vReadOnly = obj.readOnly;
    var vDisabled = obj.disabled;
    //处理undefined值情况
    vReadOnly = (vReadOnly == undefined) ? false: vReadOnly;
    vDisabled = (vDisabled == undefined) ? true: vDisabled;
    //当敲Backspace键时，事件源类型为密码或单行、多行文本的，
    //并且readOnly属性为true或disabled属性为true的，则退格键失效
    var flag1= ev.keyCode == 8 && (t=="password"|| t=="text"|| t=="textarea")&& (vReadOnly==true|| vDisabled==true);
    //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
    var flag2= ev.keyCode == 8 && t != "password"&& t != "text"&& t != "textarea";
    //判断
    if(flag2 || flag1) return false;
}
</script>
</html>