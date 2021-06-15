<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsDb"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>皮肤列表</title>
<%@ include file="../inc/nocache.jsp"%>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />



</head>
<body>
<% 
    int pagesize = ParamUtil.getInt(request,"pageSize",5);
    int curpage = ParamUtil.getInt(request,"CPages",1);
	MobileSkinsMgr mobileSkinMgr = new MobileSkinsMgr();
	ListResult lr = mobileSkinMgr.queryAllSkinsMobilByList(curpage,pagesize);
	Iterator iterator = lr.getResult().iterator();
	long total = lr.getTotal();
	Paginator paginator = new Paginator(request, total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
	String op = ParamUtil.get(request,"op");
	MobileSkinsMgr mobileSkinsMgr = new MobileSkinsMgr();
	if(op!=null && !op.trim().equals("")){
		if(op.equals("del")){
			String codes = ParamUtil.get(request,"codes");
			boolean flag = mobileSkinMgr.deleteMobileSkinsByBatch(codes);
			if(flag){
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "mobile_skins_list.jsp?CPages=" + curpage));
			}else{
				out.print(StrUtil.jAlert("删除失败!","提示"));
			}
		}
	}
%>
	<!--<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr>
			<td width="48%" height="30" align="left">
			<form action="mobile_skins_list.jsp" method="get">
			<input id="op" name="op" value="search" type="hidden" />
			<select id="cond" name="cond">
			<option value="title">标题</option>
			<option value="content">内容</option>
			</select>
			<input class="tSearch" value="搜索" type="submit" />
			</form>
		  </td>
		</tr>
	</table>-->
      <table width="100%" border="0" cellpadding="0" cellspacing="0" id="grid">
      <thead>
        <tr>
		  <th width="300" name="name">名称</th> 
          <th width="50" name="is_used">启用</th>
          <th width="100" name="user_name">用户</th>
          <th width="150" name="modify_date">时间</th>
        </tr>
      </thead>
      <tbody>
      
		<%
		UserDb user = new UserDb();
		while(iterator.hasNext()) {
			MobileSkinsDb mobileSkin = (MobileSkinsDb)iterator.next();
			user = user.getUserDb(mobileSkin.getString("user_name"));
		%>
	        <tr id="<%=mobileSkin.getString("code")%>">
	          <td><%=mobileSkin.getString("name")%></td> 
	          <td align="center"><%=mobileSkin.getInt("is_used")== 1?"是":"否"%></td>
	          <td align="center"><%=user.getRealName()%></td>
	          <td align="center"><%=DateUtil.format(mobileSkin.getDate("modify_date"), "yyyy-MM-dd HH:mm:SS")%></td>
	        </tr>
		<%
		}
		%>
	</tbody>
</table>  
<%
	String querystr = "";
	// out.print(paginator.getPageBlock(request,"notice_list.jsp?"+querystr));
%>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "mobile_skins_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}
function changePage(newp) {
	if (newp)
		window.location.href = "mobile_skins_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}
function rpChange(pageSize) {
	window.location.href = "mobile_skins_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}
function onReload() {
	window.location.reload();
}
$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '添加', bclass: 'add', onpress : action},
			{name: '修改', bclass: 'edit', onpress : action},
			{name: '删除', bclass: 'delete', onpress : action}
			],
		url: false,
		usepager: true,
		checkbox: true,
		page: <%=curpage%>,
		total:<%=total%>,
		useRp: true,
		rp:<%=pagesize%>,
		singleSelect: true,
		resizable: false,
		showTableToggleBtn: true,
		showToggleBtn: true,
		nomsg: '没有数据',//无结果的提示信息
		onChangeSort: changeSort,
		onChangePage: changePage,
		onRpChange: rpChange,
		onReload: onReload,
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
	
});
	function action(com, grid) {
		if (com=='添加')	{
			window.location.href = "mobile_skin_upload.jsp";
		}else if(com == '删除'){
			var selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
			if(selectedCount == 0){
				jAlert("请选择相应删除的记录","提示");
				return;
			}
			jConfirm('您确定要取消删除该记录吗？', '提示', function(r) {
					if(!r){
						return;
					}else{
						var codes = "";
						$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
							if (codes=="")
								codes = $(this).val();
							else
								codes += "," + $(this).val();
						});
						window.location.href='mobile_skins_list.jsp?op=del&CPages=<%=curpage%>&codes='+ codes;	
					}
			});
		}else if(com == '修改'){
			selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
			if (selectedCount == 0) {
				jAlert('请选择一条记录!','提示');
				return;
			}
			if (selectedCount > 1) {
				jAlert('只能选择一条记录!','提示');
				return;
			}
			var id = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
			window.location.href='mobile_skin_upload.jsp?code='+ id;	
			
		}
	}
	//ajax 删除
	function del (codes){
		 $.ajax({
				type: "post",
				url: "mobile_skin_do.jsp",
				data :{"op":"del","codes":codes},
				success: function(data, status){
					if(data){
					   $('#grid').flexReload();  
					}else{
						jAlert("删除失败！","提示");
					}
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					jAlert(XMLHttpRequest.responseText,'提示');
				}
			});

	}

	

</script>
</body>
</html>