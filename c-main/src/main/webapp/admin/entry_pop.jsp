<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="com.redmoon.oa.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="bsh.StringUtil"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String op = ParamUtil.get(request, "op");
	String user_name = ParamUtil.get(request, "userName");
	if ("edit".equals(op)) {
		try {
			FavoriteMgr fm = new FavoriteMgr();
			fm.update(request);
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		if ("system".equals(user_name))
		{
			out.print(StrUtil.Alert_Redirect("操作成功！", "entry_pop.jsp?userName="+user_name));
		}
		else
		{
			out.print(StrUtil.Alert_Redirect("操作成功！", "entry_pop.jsp"));
		}
		return;
	}
	//若是当前用户无快捷菜单，则复制admin快捷菜单到该用户下
	else if ("refresh".equals(op)) //刷新所有用户下该项配置，若不同配置则修改为管理员配置
	{
		try {
			String order = ParamUtil.get(request, "order");
			FavoriteDb fdb = new FavoriteDb();
			//获取所有用户
			String refreshSql = "select id from " + fdb.getTable().getName()
					+ " where style='1' order by orders asc";
			Vector userV = fdb.list(refreshSql);
			Iterator userVIt = userV.iterator();
			List<String> userList = new ArrayList<String>();
			FavoriteDb userFdb = new FavoriteDb();
			while (userVIt.hasNext()) {
				userFdb = (FavoriteDb) userVIt.next();
				String name = StrUtil.getNullStr(userFdb.getString("user_name"));
				if (!userList.contains(name) && !"system".equals(name))
				{
					userList.add(name);
				}
			}
			
			//获取管理员配置
			FavoriteDb sysDb = new FavoriteDb();
			FavoriteDb otherDb = new FavoriteDb();
			refreshSql = "select id from " + fdb.getTable().getName()
					+ " where user_name='system' and orders=" + order + " and style='1' order by orders asc";
			Vector v = fdb.list(refreshSql);
			Iterator irfav = v.iterator();
			while (irfav.hasNext()) {
				sysDb = (FavoriteDb) irfav.next();
				//遍历用户，更新数据
				for (String userName : userList)
				{
					
					String otherSql = "select id from " + fdb.getTable().getName()
					+ " where user_name= '" + userName + "' and orders= '" + order + "' and style='1' order by orders asc";
					Vector otherV = fdb.list(otherSql);
					Iterator otherIt = otherV.iterator();
					while (otherIt.hasNext())
					{
						otherDb = (FavoriteDb) otherIt.next();
						otherDb.set("item",StrUtil.getNullStr(sysDb.getString("item")));
						otherDb.set("title",StrUtil.getNullStr(sysDb.getString("title")));
						otherDb.set("icon",StrUtil.getNullStr(sysDb.getString("icon")));
						otherDb.save();
					}
				}
			}
		}
		catch (Exception ex)
		{
			out.print(StrUtil.Alert_Back(ex.getMessage()));
		}
		if ("system".equals(user_name))
		{
			out.print(StrUtil.Alert_Redirect("操作成功！", "entry_pop.jsp?userName="+user_name));
		}
		else
		{
			out.print(StrUtil.Alert_Redirect("操作成功！", "entry_pop.jsp"));
		}
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>快速入口</title>

		<script src="../inc/common.js"></script>
		<script src="../js/jquery-1.9.1.min.js"></script>
		<script src="../js/jquery-migrate-1.2.1.min.js"></script>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
		<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

		<link href="<%=SkinMgr.getSkinPath(request)%>/entry.css" rel="stylesheet" type="text/css" />
	</head>
	<link type="text/css" rel="stylesheet"
		href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

	<script type="text/javascript">
var curForm,curIconDiv;
function selIcon(icon) {
	curForm.icon.value = icon;
	curIconDiv.innerHTML = "<img src='<%=SkinMgr.getSkinPath(request)%>/icons/" + icon + "' width='17px' height='17px' id='getIcon'>";
}
function selBigIcon(icon) {
	o("bigIcon").value = icon;
	o("bigIconDiv").innerHTML = "<img src='<%=request.getContextPath()%>/images/bigicons/" + icon + "' width='17px' height='17px'>";
}

function SwitchEntry(name, url)
{ 
	
 	addTab(name,  url);
 	top.mainFrame.hide();
 	
   
}
function onChangeItem(frm, selObj) {
var t=selObj.options[selObj.selectedIndex].text;
var p = t.indexOf("├");
if (p!=-1){
	t = t.substring(p+1);
}
frm.title.value=t;
}

  function ChangeCss(obj,idx)
  {
     if(idx==1)
     {
        if(obj ==1)
        {
      	  $("#sel1").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel1").addClass("toggleStyle");
        }
     }
     
     if(idx==2)
     {
        if(obj ==1)
        {
      	  $("#sel2").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel2").addClass("toggleStyle");
        }
     }
     
     if(idx==3)
     {
        if(obj ==1)
        {
      	  $("#sel3").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel3").addClass("toggleStyle");
        }
     }
     
     if(idx==4)
     {
        if(obj ==1)
        {
      	  $("#sel4").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel4").addClass("toggleStyle");
        }
     }
     
     if(idx==5)
     {
        if(obj ==1)
        {
      	  $("#sel5").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel5").addClass("toggleStyle");
        }
     }
     
     if(idx==6)
     {
        if(obj ==1)
        {
      	  $("#sel6").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel6").addClass("toggleStyle");
        }
     }
     
     
      if(idx==7)
     {
        if(obj ==1)
        {
      	  $("#sel7").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel7").addClass("toggleStyle");
        }
     }
     
     if(idx==8)
     {
        if(obj ==1)
        {
      	  $("#sel8").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel8").addClass("toggleStyle");
        }
     }
     
     if(idx==9)
     {
        if(obj ==1)
        {
      	  $("#sel9").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel9").addClass("toggleStyle");
        }
     }
     
     if(idx==10)
     {
        if(obj ==1)
        {
      	  $("#sel10").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sel10").addClass("toggleStyle");
        }
     }
     
     
  }
  function ChangeCss2(obj,idx)
  {
     if(idx==1)
     {
        if(obj ==1)
        {
      	  $("#sell").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell").addClass("toggleStyle");
        }
     }
     
     if(idx==2)
     {
        if(obj ==1)
        {
      	  $("#sell2").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell2").addClass("toggleStyle");
        }
     }
     
     if(idx==3)
     {
        if(obj ==1)
        {
      	  $("#sell3").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell3").addClass("toggleStyle");
        }
     }
     
     if(idx==4)
     {
        if(obj ==1)
        {
      	  $("#sell4").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell4").addClass("toggleStyle");
        }
     }
     
     if(idx==5)
     {
        if(obj ==1)
        {
      	  $("#sell5").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell5").addClass("toggleStyle");
        }
     }
     
     if(idx==6)
     {
        if(obj ==1)
        {
      	  $("#sell6").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell6").addClass("toggleStyle");
        }
     }
     
     
      if(idx==7)
     {
        if(obj ==1)
        {
      	  $("#sell7").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell7").addClass("toggleStyle");
        }
     }
     
     if(idx==8)
     {
        if(obj ==1)
        {
      	  $("#sell8").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell8").addClass("toggleStyle");
        }
     }
     
     if(idx==9)
     {
        if(obj ==1)
        {
      	  $("#sell9").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell9").addClass("toggleStyle");
        }
     }
     
     if(idx==10)
     {
        if(obj ==1)
        {
      	  $("#sell10").removeClass("toggleStyle");
        }
        if (obj==2){
     	  $("#sell10").addClass("toggleStyle");
        }
     }
     
     
  }
function edit(){
		$("#edit").css("display","");
		$("#prototype").css("display","none");
		$("#toggle_bg").css("background-color","#eddbcf");
		// entry_arrow_op
		$("#img").attr("src","images/entry_arrow_op.png");
		if ('system' != '<%=user_name%>')
		{
			top.mainFrame.removeHide();
		}
		$("#cancelEdit").show();
		$("#editIcon").hide();

}
function editContent(title,id,item,icon){
	
	$("#title").val(title);
	$("#id").val(id);
	if (item != "")
	{
		$("#item").val(item);
	}
	else
	{
		$("#item").val('');
	}
	if (icon != "")
	{
		$("#bigIcon").val(icon);
		$("#imgIcon").attr('src','<%=request.getContextPath()%>/images/bigicons/' + icon);
		$("#getIcon").attr('src','<%=request.getContextPath()%>/images/bigicons/' + icon);
		$("#imgIcon").show();
		$("#getIcon").show();
	}
	else
	{
		$("#bigIcon").val('');
		$("#imgIcon").attr('src','');
		$("#imgIcon").hide();
		$("#getIcon").hide();
	}
	$("#dialog").dialog("open");
	
}
function cancelEdit()
{
	$("#edit").css("display","none");
	$("#prototype").css("display","");
	$("#toggle_bg").css("background-color","");
	$("#img").attr("src","images/entry_arrow.png");
	if ('system' != '<%=user_name%>')
	{
		top.mainFrame.addHide();
	}
	$("#cancelEdit").hide();
	$("#editIcon").show();
}
function closeQuickMenu()
{
	top.mainFrame.hide();
}
jQuery(document).ready(function(){
	$("#dialog").dialog(
	 	{
	 	 bgiframe: true,
	     resizable: false,
	     modal: true,
	     width: 350,
	     title: "配置", 
	     position: "center",  
	     autoOpen:false,
	     overlay: {
	        backgroundColor: '#000',
	        opacity: 0.5
	    },
		 buttons: {
		 "取消": function() {
            $(this).dialog('close');
        },
        '提交': function() {
            $("#frm").submit();
            top.mainFrame.addHide();
        }
       
    }
		
        });
        //操作员从系统管理中进入
      if ("<%=user_name %>" == "system")
      {
      	edit();
      }
      
       
})
</script>


	<body>
		<!--快速入口弹框开始-->
		<div class="entry_bgbox" 
		<% 
			if (!"system".equals(user_name))
			{
		%>
			style="opacity:0.8"
		<%
			}
		%>
		
		>
		<% 
				
				if (!"system".equals(user_name))
				{
			%>
			<div class="arr">
				<img id="img" src="images/entry_arrow.png" width="19" height="10" />
			</div>
			<%} %>
			<div class="bg bgcol" id="toggle_bg">
			
				<div class="op">
					<!--编辑按钮-->
					<a href="#" id="editIcon" title="编辑"> <span onclick="edit();"><img src="images/entry_editor.png"
								width="15" height="22"  />编辑</span> </a>
					<!--取消编辑按钮-->
					<a href="#" id="cancelEdit" style="display:none" title="取消编辑"> <span onclick="cancelEdit();" ><img src="images/entry_shut.png"
								width="14" height="14" />&nbsp;取消</span> </a>
					<!-- 关闭 -->
					<% 
							if (!"system".equals(user_name))
							{
					%>
					<a href="#" id="closeIcon" title="关闭"> <span><img src="images/entry_shut2.png"
								width="22" height="22" onclick="closeQuickMenu();" /> </span> </a>
					<%
							}
					%>
				</div>
			
				<div id="prototype" >
					<%
						FavoriteDb ufd = new FavoriteDb();
						String sql = "";
						if ("system".equals(user_name) && privilege.isUserPrivValid(request, "admin"))
						{
							sql= "select id from " + ufd.getTable().getName()
							+ " where user_name='system' and style='1' order by orders asc";
						}
						else
						{
							sql = "select id from " + ufd.getTable().getName()
							+ " where user_name='" + privilege.getUser(request) + "' and style='1' order by orders asc";
							
						}
						Vector v = ufd.list(sql);
						Iterator irfav = v.iterator();
						int m = 0;
						while (irfav.hasNext()) {
							ufd = (FavoriteDb) irfav.next();
							String icon = StrUtil.getNullStr(ufd.getString("icon"));
							String title = StrUtil.getNullStr(ufd.getString("title"));
							String item = StrUtil.getNullStr(ufd.getString("item"));
							m++;
					%>
					
					<div id="sel<%=m%>" 
						onmousemove="ChangeCss(2,<%=m%>)" class="block_<%=m%>"
						onmouseout="ChangeCss(1,<%=m%>)">
						
						<div onclick="SwitchEntry('<%=title%>', '<%=item%>')">
						<div class="icobox">

							<%
								if (!icon.equals("")) {
							%>
							<%
								if (icon.toLowerCase().startsWith("http:")) {
							%>
							<img src="<%=icon%>" />
							<%
								} else {
							%>
							<img src="<%=request.getContextPath()%>/images/bigicons/<%=icon%>" />
							<%
								}
							%>
							<%
								}
								else 
								{
							%>
								<img src="<%=SkinMgr.getSkinPath(request)%>/images/entry_ico<%=m %>.png" />	
							<%		
								}
							%>

						</div>
						<div class="text"><%=title%></div>
						</div>
					</div>

					<%
						}
					%>

				</div>
				<!-- edit -->
				<div  id="edit" style="display: none">
					<%
						Iterator editIt = v.iterator();
						int y = 0;
						while (editIt.hasNext()) {
							ufd = (FavoriteDb) editIt.next();
							String icon = StrUtil.getNullStr(ufd.getString("icon"));
							String title = StrUtil.getNullStr(ufd.getString("title"));
							y++;
					%>
					<div id="sel<%=y%>" 
						onmousemove="ChangeCss(2,<%=y%>)" class="block_<%=y%>"
						onmouseout="ChangeCss(1,<%=y%>)" >
						<!-- 判断是否有权限刷新 -->
						<% 
							if (privilege.isUserPrivValid(request, "admin") && "system".equals(user_name))
							{
						%>
						<form action="?op=refresh&order=<%=y-1 %>&userName=<%=user_name %>" method="post">
							<input class="btn" name="submit" type="submit" value="刷新" style="cursor:pointer" title="刷新到每个用户的快捷入口"/>
						</form>
						<%} %>
						<div onclick="editContent('<%=title %>','<%=ufd.getInt("id") %>','<%=StrUtil.getNullStr(ufd.getString("item")) %>','<%=icon %>')" style="cursor:pointer">
						<div class="icobox">

							<%
								if (!icon.equals("")) {
							%>
							<%
								if (icon.toLowerCase().startsWith("http:")) {
							%>
							<img src="<%=icon%>" />
							<%
								} else {
							%>
							<img src="<%=request.getContextPath()%>/images/bigicons/<%=icon%>" />
							<%
								}
							%>
							<%
								}
								else 
								{
							%>
								<img src="<%=SkinMgr.getSkinPath(request)%>/images/entry_ico<%=y %>.png" />	
							<%		
								}
							%>

						</div>
						<div class="text"><%=title%></div>
						</div>
					</div>

					<%
						}
					%>
					
				</div>
				<!-- edit -->
			</div>
		</div>
		<!--快速入口弹框结束-->
		<div id="dialog" style="display: none;">
			<form name="frm" action="?op=edit&userName=<%=user_name %>" method="post" id="frm">
			<table width="100%" align="center" class="tabStyle_1 percent98">
			
				<tr>
					<td>
						<span>名称</span>
					</td>
					<td>
						<input name="title" id="title"
							value="" />
						<input name="id" id="id" type="hidden"/>
					</td>
				</tr>
				<tr>
					<td>
						<span>菜单项</span>
					</td>
					<td>
						
						<select name="item" onchange="onChangeItem(frm, this)" id="item">
							<option value="">
								请选择菜单项
							</option>
							<%
								LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(
												Leaf.CODE_ROOT );
										Iterator ir = lccm.getChildren().iterator();
										int k = 2;
										int x = 0;
										while (ir.hasNext()) {
											Leaf lf = (Leaf) ir.next();
											if (!lf.canUserSee(request))
												continue;
											LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(
													lf.getCode());
											Vector v2 = lccm2.getChildren();
							%>
							<option value="<%=lf.getLink(request)%>">
								╋<%=lf.getName()%></option>
							<%
								k++;
											Iterator ir2 = v2.iterator();
											while (ir2.hasNext()) {
												Leaf lf2 = (Leaf) ir2.next();
												if (!lf2.canUserSee(request))
													continue;
												LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(
														lf2.getCode());
												Vector v3 = lccm3.getChildren();
												if (v3.size() == 0) {
							%>
							<option value="<%=lf2.getLink(request)%>">
								├<%=lf2.getName()%></option>
							<%
								} else {
							%>
							<option value="">
								├<%=lf2.getName()%></option>
							<%
								k++;
													Iterator ir3 = v3.iterator();
													while (ir3.hasNext()) {
														Leaf lf3 = (Leaf) ir3.next();
														if (!lf3.canUserSee(request))
															continue;
							%>
							<option value="<%=lf3.getLink(request)%>">
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├<%=lf3.getName()%></option>
							<%
								}
							%>
							<%
								}
							%>
							<%
								}
							%>
							<%
								}
							%>
						</select>
					</td>
				</tr>
				<tr>
					<td>
						<span>图标</span>
					</td>
					<td>
						<input name="icon" value="" size="5" id="bigIcon" style="width:40%"/>
						<input class="btn" name="button" type="button"
							onclick="openWin('<%=request.getContextPath()%>/admin/menu_big_icon_sel.jsp', 800, 600)"
							value="选择" />
						<span id="bigIconDiv"> 
						 	  <img src="" width="17px" height="17px" style="display:none" id="imgIcon"/> 
						 </span>
					</td>
				</tr>
				
			

			</table>
			</form>
		</div>

	</body>
</html>
