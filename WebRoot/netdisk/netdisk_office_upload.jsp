<%@ page contentType="text/html;charset=utf-8" import="cn.js.fan.util.*" import="com.redmoon.oa.netdisk.*"%><%

response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
DocumentMgr dm = new DocumentMgr();
int type = ParamUtil.getInt(request,"type",1);
boolean flag = false;
try {
	if(type == 1){
		if (dm.uploadDocument(application, request))
			out.print("上传成功！");
	}else{
		flag = dm.commonUpload(application,request);
		out.print(flag);
	}

}catch (ErrMsgException e) {out.print(e.getMessage());}
%>