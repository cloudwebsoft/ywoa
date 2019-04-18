<%@ page contentType="text/html;charset=utf-8"
import="cn.js.fan.util.*"
import="com.redmoon.oa.fileark.*"%><%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
DocumentMgr dm = new DocumentMgr();
int type = ParamUtil.getInt(request,"type",0);//1代表极速上传 0代表普通上传
try {
	//if (dm.swfUploadDocument(application, request))
		//out.print(cn.js.fan.web.SkinUtil.LoadString(request, "info_op_success"));
	if(type == 0){
		if(dm.swfUploadDocument(application, request)){
			response.sendRedirect("document_list_m.jsp");
		}
	}else{
		if(dm.upload(application, request)){
			out.print("上传成功!");
			//response.sendRedirect("document_list_m.jsp");
		}else{
			try{
				out.print("操作失败!");
			}catch(Exception e){
				out.print(StrUtil.Alert(e.getMessage()));
			}
			
		}
	}
	
}
catch (ErrMsgException e) {
	out.print(e.getMessage());
}
%>
