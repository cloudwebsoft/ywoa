<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.module.cms.*"%>
<%@ page import = "cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import = "com.itextpdf.text.*"%>
<%@ page import = "com.itextpdf.text.pdf.*"%>
<%@ page import = "com.itextpdf.text.html.simpleparser.*"%>
<%@ page import = "java.io.*"%>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>

<object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" 
codebase="../../../../activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                <param name="Encode" value="utf-8" />
                <param name="BackColor" value="0000ff00" />
                <param name="Server" value="<%=request.getServerName()%>" />
                <param name="Port" value="<%=request.getServerPort()%>" />
                <!--设置是否自动上传-->
                <param name="isAutoUpload" value="1" />
                <!--设置文件大小不超过1M-->
                <param name="MaxSize" value="<%=Global.FileSize%>" />
                <!--设置自动上传前出现提示对话框-->
                <param name="isConfirmUpload" value="1" />
                <!--设置IE状态栏是否显示信息-->
                <param name="isShowStatus" value="0" />
                <param name="PostScript" value="<%=Global.virtualPath%>/flow_document_check.jsp" />
              </object>
              
<input value="另存" onclick="redmoonoffice.Visible=0; redmoonoffice.OpenWordDocNotVisable('c:/tmp1/a.doc'); redmoonoffice.SaveAs('c:/f9.doc'); redmoonoffice.isAutoUpload=0; redmoonoffice.close();" type=button />  

