<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>My JSP 'mobile_skin_upload.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
		if (!privilege.isUserPrivValid(request, "admin.user")) {
	   		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		String code = ParamUtil.get(request,"code");
		String name = "";
		int is_used = 0;
		MobileSkinsMgr mobileSkinMgr = new MobileSkinsMgr();
		MobileSkinsDb mobileSkinsDb = null;
		if(code!=null && !code.trim().equals("")){
	    	 mobileSkinsDb = mobileSkinMgr.getMobileSkinsDb(code);
	    	 name = mobileSkinsDb.getString("name");
	    	 is_used = mobileSkinsDb.getInt("is_used");
		}
		String op = ParamUtil.get(request, "op");
		if (op != null && !op.equals("")&&op.equals("upload")) {
			boolean re = false;
			try {
				re = mobileSkinMgr.create(application,request);
			}
			catch (ErrMsgException e) {
				out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
				return;
			}
			if(re){
				out.print(StrUtil.jAlert_Redirect("上传成功！","提示","mobile_skins_list.jsp"));
			}else{
				out.print(StrUtil.jAlert("上传成功！","提示"));
			}
		}else if(op!=null && !op.equals("")&&op.equals("save")){
			int isNewFile = ParamUtil.getInt(request,"isNewfile",0);
			int is_used1 = ParamUtil.getInt(request,"is_used",0);
			String code1 = ParamUtil.get(request,"code");
			String name1 = ParamUtil.get(request,"name");
			boolean re = mobileSkinMgr.save(application,request,code1,is_used1,isNewFile,name1);
			if(re){
				out.print(StrUtil.jAlert_Redirect("修改成功！","提示","mobile_skins_list.jsp"));
			}else{
				out.print(StrUtil.jAlert("修改成功","提示"));
			}
			
			
		}
	
	%>
	<script type="text/javascript">
		$(function(){
			var name = '<%=name%>';
			var is_used = <%=is_used%>;
			if(name!=""){
				$(".name").val(name);
				$(".is_used").val(is_used);
				if(is_used == 1){
					$(".is_used").attr("checked","checked");
				}else{
					$(".is_used").removeAttr("checked");
				}
			}

			})
			
		function skinsMobile_onsubmit()
		{
			var code = '<%=code%>';
			var url = "";
			if(name == ""){
				jAlert("请输入名称！","提示");
				return false;
			}
			if(code!=""){//更新
				var file = $(".file").val();
				var isNewFile = 0;
				if(file!=""){
					isNewFile = 1; 
				}
				var is_used = 0;
				if($(".is_used").is(":checked")){
					is_used = 1;
				}
				var name = $(".name").val();
				url ="?isNewfile=" +isNewFile + "&code=" + code+ "&op=save&is_used="+is_used+"&name="+name;
			}else{//新增
				url ="?op=upload";
			}
			skinsMobileform.action += url;
			return true;
		}
	</script>
  </head>
  
  <body>
  <div class="spacerH"></div>
	<FORM action="mobile_skin_upload.jsp" enctype="multipart/form-data" method="post" id="skinsMobileform" name="skinsMobileform" onsubmit="return skinsMobile_onsubmit()">
		<TABLE align="center" class="tabStyle_1 percent80">
		    <TBODY>
		      <TR>
		        <TD colspan="2" align="left" class="tabStyle_1_title">手机客户端换肤</TD>
		      </TR>
		      <TR>
		        <TD height="26" align="right">名称：</TD>
		        <TD> <input type="text" name="name" class="name"/>
		        <span class="STYLE5"> *</span> </TD>
		      </TR>
		       <TR>
		        <TD height="26" align="right">是否启用：</TD>
		        <TD><input type="checkbox" name="is_used" class="is_used" value="1"/></TD>
		      </TR>
		      <TR>
		        <TD height="26" align="right">文件：</TD>
		        <TD>
		         <input type="file" name="file" class="file"/><br/>
	    		 <input type="hidden" name="user_name" value="<%=privilege.getUser(request) %>"/>
		        </TD>
		      </TR>
		      <TR>
		        <TD height="30" colspan="2" align="center"><input name="button" type="submit" class="btn"  value="确定 ">
		          &nbsp;&nbsp;&nbsp;&nbsp;
		        <input name="button2" type="button" class="btn"  value="返回" onClick="window.location.href='mobile_skins_list.jsp'"></TD>
		      </TR>
		    </TBODY>
		</TABLE>
	</FORM>
  </body>
</html>
