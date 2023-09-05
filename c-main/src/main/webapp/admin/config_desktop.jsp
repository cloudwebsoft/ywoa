<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.ui.*,
				 com.redmoon.oa.ui.menu.*,
				 com.redmoon.oa.person.*,
				 org.jdom.*,
				 com.cloudwebsoft.framework.db.*,
                 java.util.*"
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%!
public void set(String code, String property, String textValue,Element root) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    List list1 = child.getChildren();
                    if (list1 != null) {
                        Iterator ir1 = list1.listIterator();
                        while (ir1.hasNext()) {
                            Element childContent = (Element) ir1.next();
                            System.out.println(getClass() + " name=" + childContent.getName() + " " + property);
                            if (childContent.getName().equals(property)) {
                                childContent.setText(textValue);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
%>
<%!
public void delPluginUnit(String code,Element root) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    root.removeContent(child);
                    break;
                }
            }
        }
    }
%>
<%
int k = 0;
DesktopMgr sm = new DesktopMgr();
sm.init();

Element root = sm.root;
String code="", name = "";

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>桌面管理</title>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function openWinUsers() {
	selUserNames = form_users.users.value;
	selUserRealNames = form_users.userRealNames.value;
	openWin('../user_multi_sel.jsp',600,480);
}

function setUsers(users, userRealNames) {
	form_users.users.value = users;
	form_users.userRealNames.value = userRealNames;
}
</script>
</head>
<body>
<%
if (op.equals("modify")) {
	code = ParamUtil.get(request, "code");
	String defaultCol = ParamUtil.get(request, "defaultCol");
	String isDefault = ParamUtil.get(request, "isDefault");
	String className = ParamUtil.get(request, "className");
	String pageShow = ParamUtil.get(request, "pageShow");
	String defaultOrder = ParamUtil.get(request, "defaultOrder");
	String username = ParamUtil.get(request, "name");
	String pageList = ParamUtil.get(request, "pageList");
	String type = ParamUtil.get(request, "type");
	
	set(code, "defaultCol", defaultCol,root);	
	set(code, "isDefault", isDefault,root);	
    set(code, "className", className,root);
	set(code, "pageShow", pageShow,root);
	set(code, "defaultOrder", defaultOrder,root);
	set(code, "name", username,root);
	set(code, "pageList", pageList,root);
	set(code, "type", type,root);
	
	sm.writemodify();
	sm.reload();
	out.println(fchar.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "config_desktop.jsp"));
	return;
}
else if (op.equals("del")) {
	code = ParamUtil.get(request, "code");
	delPluginUnit(code,root);
    sm.writemodify();
	sm.reload();
	out.println(fchar.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "config_desktop.jsp"));
	return;
}else if (op.equals("init")) {
	PortalDb pd = new PortalDb();
	SlideMenuGroupDb smgd = new SlideMenuGroupDb();
	UserDb user = new UserDb();
	Iterator ir = user.list().iterator();
	String type = ParamUtil.get(request, "type");
	String userName = "";
	FavoriteMgr fm = new FavoriteMgr();
	while(ir.hasNext()) {
	  user = (UserDb)ir.next();
	  userName = user.getName();
	  if (userName.equals(UserDb.SYSTEM)) {
	  	continue;
	  }
	  if (type.equals("portal")) {
	  	pd.init(userName);
	  }
	  else if (type.equals("favoriate")) {
		fm.initQuickMenu4User(userName);	  	
	  }
	  else
	  	smgd.init(userName);
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "config_desktop.jsp"));
	return;
}else if(op.equals("initUsers")){
	String users = ParamUtil.get(request, "users");
	String userNames[] = StrUtil.split(users, ",");
	if (userNames==null) {
		out.print(StrUtil.jAlert_Back("请选择用户！","提示"));
		return;
	}
	String type = ParamUtil.get(request, "type");
	if (type.equals("portal")) {
		PortalDb pd = new PortalDb();
		for (String userName:userNames){
		   pd.init(userName);
		}
	}
	else if (type.equals("favoriate")) {
		FavoriteMgr fm = new FavoriteMgr();
		for (String userName:userNames){
			fm.initQuickMenu4User(userName);
		}
	}
	else {
		SlideMenuGroupDb smgd = new SlideMenuGroupDb();
		for (String userName:userNames){
		   smgd.init(userName);
		}
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "config_desktop.jsp"));	
	return;
}
if (!privilege.isUserPrivValid(request, "admin")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<div class="spacerH"></div>
<form id="form_users" name="form_users" action="config_desktop.jsp?op=initUsers" method="post">
<table class="tabStyle_1 percent80" width="98%" border="0" align="center">
  <tr>
    <td align="center" class="tabStyle_1_title">初始化用户界面</td>
  </tr>
  <tr>
    <td align="center" style="line-height:1.5"><input name="users" id="users" type="hidden" value="">	
        <input name="userRealNames" readonly wrap="yes" id="userRealNames" />
        <input class="btn" title="选择用户" onClick="openWinUsers()" type="button" value="选择">
         <!--
         &nbsp;
         <input class="btn" title="清空用户" onClick="form_users.users.value='';form_users.userRealNames.value=''" type="button" value="清空" name="button">
         -->
         &nbsp;
         <input name="type" value="portal" type="hidden" />
<%--
         <input class="btn" type="submit" value="初始化门户" />
        &nbsp;&nbsp;
--%>
         <!-- &nbsp;&nbsp;
        <input class="btn" type="button" value="初始化滑动菜单" onclick="o('type').value='slideMenu'; o('form_users').submit()" />
         &nbsp;&nbsp;
        <input class="btn" type="button" value="初始化快速入口" onclick="o('type').value='favoriate'; o('form_users').submit()" />
         -->
         <%
         String displayModular = "display:none";
			com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();    
			if(license.isPlatformSrc()) {
				displayModular = "";
			}
         %>
<%--
        <input class="btn" type="button" onclick="jConfirm('您确定要初始化全部用户的门户么？','提示',function(r){if(!r){return;}else{window.location.href='config_desktop.jsp?op=init&type=portal'}}) " value="初始化全部门户" />
        &nbsp;&nbsp;
--%>
        <input style="display:" class="btn" type="button" onclick="jConfirm('您确定要初始化全部用户的滑动菜单么？','提示',function(r){if(!r){return;}else{window.location.href='config_desktop.jsp?op=init&type=slideMenu'}}) " value="初始化全部滑动菜单" />
        &nbsp;&nbsp;
        <input style="display:none" class="btn" type="button" onclick="jConfirm('您确定要初始化全部用户的快速入口么？','提示',function(r){if(!r){return;}else{window.location.href='config_desktop.jsp?op=init&type=favoriate'}}) " value="初始化全部快速入口" />
	</td>
  </tr>
</table>
</form>
<%
Vector v = sm.getAllDeskTopUnit();
if (v != null) {
Iterator ir = v.iterator();
while (ir.hasNext()) {
   	 DesktopUnit pu = (DesktopUnit) ir.next();
   	 if (!pu.isDisplay()) {
   		 continue;
   	 }
%>
  <table width="98%" border="0" align="center" cellpadding="3" cellspacing="1" class="tabStyle_1 percent80">
    <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_desktop.jsp?op=modify'>
      <thead>
        <tr>
          <td class="tabStyle_1_title" colspan="4"><%=pu.getName()%><input type="hidden" name="code" value="<%=pu.getCode()%>"/></td>
          </tr>
        </thead>
      <tr >
        <td width="15%">名称</td>
        <td width="35%" align="left"><input type="input" name="name" value="<%=pu.getName()%>" /></td>
        <td width="15%">显示</td>
        <td width="35%" align="left">
          <select name="isDefault">
            <option value="true">是</option>
            <option value="false">否</option>
          </select>
          <script>
	form<%=k%>.isDefault.value = "<%=pu.isDef()%>";
	</script>
            <span style="display:none">
          位置<select name="defaultCol">
            <option value="0">左</option>
            <option value="1">中</option>
            <option value="2">右</option>
            </select>
          <script>
	form<%=k%>.defaultCol.value = "<%=pu.getDefaultCol()%>";
	</script>
    		</span>
    </td>
      </tr>  
      <tr style="display:none">
        <td>显示页面</td>
        <td align="left"><input type="input" name="pageShow" value="<%=pu.getPageShow()%>"></td>
        <td>类名</td>
        <td align="left"><input type="input" name="className" value="<%=pu.getClassName()%>" />

	        </td>
        </tr>
      <tr style="display:none">
        <td>列表页</td>
        <td   align="left"><input type="input" name="pageList" value="<%=pu.getPageList()%>" /></td>
        <td>显示类型</td>
        <td   align="left"><select name="type">
          <option value="document">文章</option>
          <option value="list">列表</option>
        </select>
        <script>
	form<%=k%>.type.value = "<%=pu.getType()%>";
	      </script>
        <span style="display:none">列中顺序号<input type="input" name="defaultOrder" value="<%=pu.getDefaultOrder()%>" /></span>          
          </td>
      </tr>
      <tr>
        <td colspan="4"><div align="center">
          <INPUT class="btn" TYPE=submit name='edit' value='修改'>
          <!--
          &nbsp;
          <INPUT class="btn" TYPE="button" value='删除' onClick="if (confirm('您确定要删除吗？')) window.location.href='config_desktop.jsp?op=del&code=<%=pu.getCode()%>'">
          -->
          </div></td>
        </tr>
  </form>  
  </table>
  <%	
	k++;	 
   }   
 } // end if
%>
</body>
</html>