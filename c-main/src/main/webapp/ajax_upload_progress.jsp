<%@ page contentType="text/html; charset=utf-8" %><%@ page import="com.redmoon.kit.util.*"%><%@ page import="cn.js.fan.util.*"%><%
	response.setContentType("text/xml;charset=UTF-8");
	String serialNo = ParamUtil.get(request, "serialNo");
	
	FileUploadStatus fus = (FileUploadStatus)session.getAttribute(FileUploadExt.UPLOADSTATUS);
	
	String op = ParamUtil.get(request, "op");
	if (op.equals("clear")) {
		// 从session中清空对应的上传项
		// System.out.println(getClass() + " clear serialNo=" + serialNo);
		fus.del(serialNo);
		out.print(serialNo + " is cleared");
		return;
	}

	int bytesRead = -1;
	boolean isFinish = false;
	int contentLength = 0;

	if (fus!=null) {
		FileUploadStatusInfo fui = fus.get(serialNo);
		if (fui!=null) {
			bytesRead = fui.getBytesRead();
			isFinish = fui.isFinish();
			contentLength = fui.getRequestContentLength();
			
			if (op.equals("cancel")) {
				fui.setCancel(true);
				out.print(serialNo + " is canceled");
				return;
			}
		}
	}

	String str = "";
	str += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	str += "<ret>\n";
	str += "<item>\n";
	str += "<serialNo>" + serialNo + "</serialNo>\n";
	str += "<bytesRead>" + bytesRead + "</bytesRead>\n";
	str += "<contentLength>" + contentLength + "</contentLength>\n";	
	str += "<isFinish>" + isFinish + "</isFinish>\n";
	str += "</item>\n";
	str += "</ret>";
	
	// System.out.println(getClass() + " str=" + str);
	
	out.print(str);
%>