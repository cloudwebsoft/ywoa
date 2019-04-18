<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.flow.macroctl.*"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.flow.FormField"%>
<%@page import="com.redmoon.oa.visual.*"%>
<%@page import="com.redmoon.oa.android.*"%>
<%
		String skey = ParamUtil.get(request, "skey");
		int id = ParamUtil.getInt(request, "id", 0);
		String moduleCode = ParamUtil.get(request,"moduleCode");
		boolean isOnlyCamera = false;
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(moduleCode);
		if (msd==null) {
			out.print("模块不存在！");
			return;
		}
		FormDb fd = new FormDb();
		fd = fd.getFormDb(msd.getString("form_code"));
		if (fd==null) {
			out.print("表单不存在！");
			return;
		}

		boolean isLocation = false;
		MacroCtlUnit mu;
		MacroCtlMgr mm = new MacroCtlMgr();
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.getType().equals("macro")) {
				mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu.getCode().equals("macro_location_ctl")) {
					isLocation = true;
					break;
				}
			}
		}
		
		isOnlyCamera = fd.isOnlyCamera();
		String formCodeRelated = ParamUtil.get(request,"formCodeRelated");
		int parentId = ParamUtil.getInt(request,"parentId",0);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title><%=msd.getString("name")%></title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<meta content="telephone=no" name="format-detection" />
		<link rel="stylesheet" href="../css/mui.css">
		<link rel="stylesheet" href="../css/iconfont.css" />
		<link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css" />
		<link href="../css/mui.indexedlist.css" rel="stylesheet" />
		<link rel="stylesheet" href="../css/my_dialog.css" />
	</head>
	<style>
.mui-input-row .input-icon {
	width: 50%;
	float: left;
}

.mui-input-row a {
	margin-right: 10px;
	float: right;
	text-align: left;
	line-height: 1.5;
}

.div_opinion {
	text-align: left;
}

.opinionContent {
	margin: 10px;
	width: 65%;
	float: right;
	font-weight: normal;
}

.opinionContent div {
	text-align: right;
}

.opinionContent div span {
	padding: 10px;
}

.opinionContent .content_h5 {
	color: #000;
	font-size: 17px;
}
#captureFile {
	display: none;
}


</style>

	<body>
		<div class="mui-content">
			<form class="mui-input-group" id="module_form" >
			
			</form>
			<input type="file" id="captureFile" name="upload" accept="image/*"   />
		</div>
		<%
		if (isLocation) {
		%>
		<script type="text/javascript" src="http://api.map.baidu.com/api?v=1.5&ak=3dd31b657f333528cc8b581937fd066a"></script>
		<%
		}
		%>
        <link rel="stylesheet" href="../css/photoswipe.css">
        <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">	
        <script type="text/javascript" src="../js/photoswipe.js"></script>
        <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
        <script type="text/javascript" src="../js/photoswipe-init-manual.js"></script>	         
        
		<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
		<script src="../js/jq_mydialog.js"></script>
		<script type="text/javascript" src="../js/newPopup.js"></script>
		<script src="../js/macro/macro.js"></script>
		<script src="../js/mui.min.js"></script>
		<script src="../js/mui.picker.min.js"></script>
		<script type="text/javascript" src="../js/config.js"></script>
		<script type="text/javascript" src="../js/base/mui.form.js"></script>
	
		<script type="text/javascript" src="../js/visual/mui_module.js"></script>
                
		<script type="text/javascript" charset="utf-8">
	    function callJS(){
		  	return {"type":"module", "isOnlyCamera":"<%=isOnlyCamera%>"};
	   	}
	   	
		$(function() {
			<%
			CloudConfig cloudConfig = CloudConfig.getInstance();
			int photoMaxSize = cloudConfig.getIntProperty("photoMaxSize");
			int intPhotoQuality = cloudConfig.getIntProperty("photoQuality");
			%>
			maxSize = {
				width: <%=photoMaxSize%>,
				height: <%=photoMaxSize%>,
				level: <%=intPhotoQuality%>
			};	   	
				
			var content = document.querySelector('.mui-content');
			var skey = '<%=skey%>';
			var moduleCode = '<%=moduleCode%>';
			var id = <%=id%>;
			var formCodeRelated = '<%=formCodeRelated%>';
			var parentId = <%=parentId%>;
			var options = {
						"skey" : skey,
						"moduleCode":moduleCode,
						"id":id,
						"formCodeRelated":formCodeRelated,
						"parentId":parentId
					};
			window.ModuleForm = new mui.ModuleForm(content, options);
			window.ModuleForm.moduleInit();
		});
		</script>

		<jsp:include page="../inc/navbar.jsp">
			<jsp:param name="skey" value="<%=skey%>" />
			<jsp:param name="isBarBottomShow" value="false"/>
		</jsp:include>
	</body>
</html>
