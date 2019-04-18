<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.Config"%>
<%@ page import="com.redmoon.oa.SpConfig"%>
<%@page import="com.redmoon.oa.kernel.License"%>
<%
Config oaCfg = new Config();
SpConfig spCfg = new SpConfig();
String version = StrUtil.getNullStr(oaCfg.get("version"));
String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>手动更新系统</title>
		<script src="../js/jquery.js" type="text/javascript"></script>
		<script src="../js/ajaxfileupload.js" type="text/javascript"></script>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
		<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
        <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
        <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css/admin/manuallyUpdate/manuallyUpdate.css" />
		<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
		<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	</head>

	<body>
		<div class="bgCss">
			<form name="importFrm" id="importFrm"
				action=""
				method="post" enctype="multipart/form-data">
				<div>
					<img class="imgCss"
						src="<%=SkinMgr.getSkinPath(request)%>/images/admin/manuallyUpdate/update_icon1.png" />
					<label>
						文件：
					</label>
					<input type='text' id='textfield' class='txt' name="filePath" />
					<input type='button' class='btn' value='浏览...' 
						id="browse" />
						<%
							String type = License.getInstance().getType();
							if(!type.equals(License.TYPE_OEM)){
						%>
						<a href="javascript:void(0)" onclick="downUpgradeZip()" class="remarkCss">下载更新包</a>
						<%} %>

					<input type="file" name="upload" class="file" id="upload"
						onchange="document.getElementById('textfield').value=this.value" />
				</div>
				<div class ="versionDivCss">
				    <label >
				        注：当前版本：<%=version%>；补丁版本：<%=spVersion%>
				    </label>
				</div>
				<div class="buttonsDiv">
					<input name="updateBtn" type="button" id="updateBtn" class="submitCss"
						value="" onclick="updateAll()"/>
					<input name="reset" type="reset" class="resetCss" id="reset" value="" />
				</div>


			</form>
		</div>
	</body>
	<script type="text/javascript">
 $(function(){  
              
          $("#browse").mouseover(function(e){  
               $("#upload").show();  
               //debugger;
                var local_button_left = $(this).offset().left;  
                var local_button_top = $(this).offset().top;
                var local_button_innerWidth = $(this).innerWidth(); 
                var x_ = e.pageX;  
                var file_left = x_;  
                var file_innerWidth = $("#upload").innerWidth(); 
                 
                //alert(local_button_left+"-"+local_button_innerWidth+"-"+x_+"-"+$("#uploadFile").innerWidth());  
                if((x_+file_innerWidth)>(local_button_left+local_button_innerWidth)){  
                   $("#upload").css("left",e.pageX-file_innerWidth);  
               }else{  
                   $("#upload").css("left",local_button_left-5);  
                }  
                $("#upload").css("top",local_button_top);    
            });  
        });  
function updateAll()
{
    
    if($("#textfield").val() == ""){
       jAlert("请选择更新包！", "提示");
       return false;
    }
	$("#importFrm").showLoading();
    $.ajaxFileUpload({
                        url:'<%=request.getContextPath()%>/update/updateSytem',  //用于文件上传的服务器端请求地址
                        secureuri:false,//一般设置为false
                        fileElementId:'upload',//文件上传空间的id属性  <input type="file" id="file" name="file" />
                        dataType: 'json',//返回值类型 一般设置为json
                        success:function(data,status){
                            jAlert(data.message,"提示");
                            if(data.message == "更新成功")
                            {
                                $("#reset").click();
                            }
                            else
                            {
                                $("#reset").click();
                            }
                        },
					complete: function(XMLHttpRequest, status){
						$("#importFrm").hideLoading();				
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						jAlert(XMLHttpRequest.responseText,"提示");
					}
                     })
}
function downUpgradeZip()
{
    window.open("http://www.yimihome.com/list_patch.jsp?dirCode=bdxz");
}
</script>
</html>
