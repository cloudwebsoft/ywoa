<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request,priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String username = privilege.getUser(request);
	UserMgr um = new UserMgr();
	UserDb user = um.getUserDb(username);
	if (user==null || !user.isLoaded()) {
		out.print(StrUtil.Alert_Back("该用户已不存在！"));
		return;
	}
	
	String op = ParamUtil.get(request, "op");
	if (op.equals("add")) {
		try {
			FavoriteMgr fm = new FavoriteMgr();
			fm.create(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		out.print(StrUtil.Alert_Redirect("操作成功！", "control_panel_user_favorite.jsp"));
		return;
	}
	else if (op.equals("edit")) {
		try {
			FavoriteMgr fm = new FavoriteMgr();
			fm.save(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		out.print(StrUtil.Alert_Redirect("操作成功！", "control_panel_user_favorite.jsp"));
		return;
	}
	else if (op.equals("del")) {
		try {
			FavoriteDb fd = new FavoriteDb();
			int id = ParamUtil.getInt(request, "id");
			fd = (FavoriteDb)fd.getQObjectDb(new Integer(id));
			fd.del();
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		out.print(StrUtil.Alert_Redirect("操作成功！", "control_panel_user_favorite.jsp"));
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>我的收藏夹</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script>
var curForm,curIconDiv;
function selIcon(icon) {
	curForm.icon.value = icon;
	curIconDiv.innerHTML = "<img src='<%=SkinMgr.getSkinPath(request)%>/icons/" + icon + "'>";
}
</script>
</head>
<body>
<%@ include file="user_inc_menu_top.jsp" %>
<script>
$("menu6").className="current";
</script>
<div class="spacerH"></div>
<table width="101%" align="center" class="tabStyle_1 percent98">
		<tr>
			<td width="19%" class="tabStyle_1_title">名称</td>
		    <td width="10%" class="tabStyle_1_title">打开位置</td>
		    <td width="7%" class="tabStyle_1_title">序号</td>
		    <td width="23%" class="tabStyle_1_title">菜单项/链接</td>
		    <td width="22%" class="tabStyle_1_title">图标</td>
		    <td width="19%" class="tabStyle_1_title">操作</td>
	    </tr>
<%
	FavoriteDb ufd = new FavoriteDb();
	String sql = "select id from " + ufd.getTable().getName() + " where user_name=" + StrUtil.sqlstr(username) + " and style is NULL  order by orders asc";
	Vector v = ufd.list(sql);	
	Iterator irfav = v.iterator(); 
	int n = 0;
	while (irfav.hasNext()) {
		ufd = (FavoriteDb)irfav.next();
		int type = ufd.getInt("f_type");
		String icon = StrUtil.getNullStr(ufd.getString("icon"));
		n++;
%>
<form name="frm<%=n%>" action="?op=edit" method="post">
		<tr class="highlight">
			<td>
		    <input name="title" value="<%=StrUtil.toHtml(ufd.getString("title"))%>" /></td>
            <td><select name="target">
              <option value="mainFrame">界面右侧</option>
              <option value="_top">顶层窗口</option>
              <option value="_blank">新窗口</option>
			  <!--
              <option value="_self">本窗口</option>
              <option value="_parent">父窗口</option>
			  -->
            </select>
			<script>
            frm<%=n%>.target.value = "<%=ufd.getString("target")%>";
			</script>			</td>
            <td><input name="orders" size="3" value="<%=ufd.getInt("orders")%>" />
            <input name="f_type" type="hidden" value="<%=FavoriteDb.TYPE_MENU%>" />
            <input name="id" type="hidden" value="<%=ufd.getInt("id")%>" />			</td>
            <td>
<%if (ufd.getInt("f_type")==FavoriteDb.TYPE_MENU) {%>			
			<select name="item" onchange="onChangeItem(frm<%=n%>, this)">
              <option value="">请选择菜单项</option>
              <%
LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Iterator ir = lccm.getChildren().iterator();
int k=2;
int x=0;
while (ir.hasNext()) {
	Leaf lf = (Leaf)ir.next();
	if (!lf.canUserSee(request))
		continue;
	LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(lf.getCode());
	Vector v2 = lccm2.getChildren();
%>
              <option value="">╋<%=lf.getName()%></option>
              <%
	k++;
	Iterator ir2 = v2.iterator();
	while (ir2.hasNext()) {
		Leaf lf2 = (Leaf)ir2.next();
		if (!lf2.canUserSee(request))
			continue;
		LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(lf2.getCode());
		Vector v3 = lccm3.getChildren();
		if (v3.size()==0) {
	%>
              <option value="<%=lf2.getCode()%>">　├<%=lf2.getName()%></option>
              <%}else{%>
              <option value="">　├<%=lf2.getName()%></option>
              <%
			k++;
			Iterator ir3 = v3.iterator();
			while (ir3.hasNext()) {
				Leaf lf3 = (Leaf)ir3.next();
				if (!lf3.canUserSee(request))
					continue;
			%>
              <option value="<%=lf3.getCode()%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├<%=lf3.getName()%></option>
              <%}%>
              <%}%>
              <%}%>
              <%}%>
            </select>
<%}else{%>
<input name="item" value="<%=StrUtil.toHtml(ufd.getString("item"))%>">
<%}%>			
			<script>
			frm<%=n%>.item.value = "<%=ufd.getString("item")%>";
			</script>			</td>
            <td><input name="icon" value="<%=icon%>" size="5" />
              <input class="btn" name="button" type="button" onclick="curForm=frm<%=n%>;curIconDiv=iconDiv<%=n%>;openWin('../admin/menu_icon_sel.jsp', 800, 600)" value="选择" />
			<span id="iconDiv<%=n%>">
          <%if (!icon.equals("")) {%>
		  	<%if (icon.toLowerCase().startsWith("http:")) {%>
	          <img src="<%=icon%>" />
			<%}else{%>
	          <img src="<%=SkinMgr.getSkinPath(request)%>/icons/<%=icon%>" />
			<%}%>
          <%}%>			
			</span>
			  </td>
            <td><input class="btn" name="submit" type="submit" value="修改">
            &nbsp;&nbsp;
            <input class="btn" type="button" onClick="if (confirm('您确定要删除么？')) window.location.href='control_panel_user_favorite.jsp?op=del&id=<%=ufd.getInt("id")%>'" value="删除"></td>
        </tr>
</form>		
<%
	}
%>
<form name="form1" method="post" action="control_panel_user_favorite.jsp?op=add">
		<tr>
		  <td align="left"><input name="title"></td>
	      <td align="left"><select name="target">
            <option value="mainFrame">界面右侧</option>
            <option value="_top">顶层窗口</option>
            <option value="_blank">新窗口</option>
            <!--
              <option value="_self">本窗口</option>
              <option value="_parent">父窗口</option>
			  -->
          </select></td>
	      <td align="left"><input name="orders" value="0" size=3 />
          <input name="f_type" type="hidden" value="<%=FavoriteDb.TYPE_MENU%>" /></td>
	      <td align="left"><select name="item" onchange="onChangeItem(form1, this)">
            <option value="">请选择菜单项</option>
            <%
LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Iterator ir = lccm.getChildren().iterator();
int k=2;
int x=0;
while (ir.hasNext()) {
	Leaf lf = (Leaf)ir.next();
	if (!lf.canUserSee(request))
		continue;
	LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(lf.getCode());
	Vector v2 = lccm2.getChildren();
%>
            <option value="">╋<%=lf.getName()%></option>
            <%
	k++;
	Iterator ir2 = v2.iterator();
	while (ir2.hasNext()) {
		Leaf lf2 = (Leaf)ir2.next();
		if (!lf2.canUserSee(request))
			continue;
		LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(lf2.getCode());
		Vector v3 = lccm3.getChildren();
		if (v3.size()==0) {
	%>
            <option value="<%=lf2.getCode()%>">　├<%=lf2.getName()%></option>
            <%}else{%>
            <option value="">　├<%=lf2.getName()%></option>
            <%
			k++;
			Iterator ir3 = v3.iterator();
			while (ir3.hasNext()) {
				Leaf lf3 = (Leaf)ir3.next();
				if (!lf3.canUserSee(request))
					continue;
			%>
            <option value="<%=lf3.getCode()%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├<%=lf3.getName()%></option>
            <%}%>
            <%}%>
            <%}%>
            <%}%>
          </select></td>
	      <td align="left">&nbsp;</td>
	      <td><input class="btn" name="submit2" type="submit" value="添加菜单项" /></td>
	  </tr>
</form>
<form name="form2" method="post" action="control_panel_user_favorite.jsp?op=add">
		<tr>
          <td align="left"><input name="title" /></td>
		  <td align="left"><select name="target">
            <option value="mainFrame">界面右侧</option>
            <option value="_top">顶层窗口</option>
            <option value="_blank">新窗口</option>
            <!--
              <option value="_self">本窗口</option>
              <option value="_parent">父窗口</option>
			  -->
          </select></td>
		  <td align="left"><input name="orders" value="0" size="3" />
              <input name="f_type" type="hidden" value="<%=FavoriteDb.TYPE_LINK%>" /></td>
		  <td align="left"><input name="item"></td>
		  <td align="left"><input name="icon" size="5" />
            <input class="btn" name="button2" type="button" onclick="curForm=form2;curIconDiv=iconDivLink;openWin('../admin/menu_icon_sel.jsp', 800, 600)" value="选择" />
            <span id="iconDivLink">
            </span> </td>
		  <td><input class="btn" name="submit2" type="submit" value="添加链接项" /></td>
    </tr>
</form>
</table>
</body>
<script>
function onChangeItem(frm, selObj) {
var t=selObj.options[selObj.selectedIndex].text;
var p = t.indexOf("├");
if (p!=-1){
	t = t.substring(p+1);
}
frm.title.value=t;
}
</script>
</html>