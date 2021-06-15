<%@page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="com.redmoon.oa.flow.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.stamp.StampPriv"%>
<%@page import="com.redmoon.oa.stamp.StampDb"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%
 /*
- 功能描述：移动手机端使用
- 访问规则：手机流程模块
- 过程描述：用于手机客户端流程附件的下载
- 注意事项：
- 创建者：fgf 
- 创建时间：
*/
JSONObject result = new JSONObject(); 
String op = ParamUtil.get(request,"op");
//下载全路径 ，下载标题
String url = "";
String title = "";
String skey = ParamUtil.get(request, "skey");
com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
String userName = pri.getUserName(skey);
if(userName.equals("")){
	try {
		result.put("res", "-1");
		result.put("msg", "skey不存在！");
		out.print(result.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}
if(op!=null && !op.trim().equals("")){
	if(op.equals("stamp")){
		String opinionName = ParamUtil.get(request,"opinionName");
		if(opinionName!=null && !opinionName.trim().equals("")){
			  StampPriv sp = new StampPriv();
		      StampDb sd = sp.getPersonalStamp(opinionName);
		      if(sd!=null){
		    	  title = sd.getImage();
		    	  url = cn.js.fan.web.Global.getRealPath() + "/upfile/stamp/" + title;
		    	 
		      }
		}
	}
}else{
	int flowId = ParamUtil.getInt(request, "flowId");
	int attId = ParamUtil.getInt(request, "attachId");
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	Document doc = new Document();
	doc = doc.getDocument(wf.getDocId());
	Attachment att = doc.getAttachment(1, attId);
	title = att.getName();
	url = cn.js.fan.web.Global.getRealPath() + "/" + att.getVisualPath() + "/" + att.getDiskName();
	
	// 判断是否超出下载次数限制
	AttachmentLogMgr alm = new AttachmentLogMgr();
	if (!alm.canDownload(userName, flowId, attId)) {
		out.print(alm.getErrMsg(request));
		return;
	}
	// 下载记录存至日志
	AttachmentLogMgr.log(userName, flowId, attId, AttachmentLogDb.TYPE_DOWNLOAD);
}

// response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
response.setContentType("application/octet-stream");
// chrome下载文件名中带有逗号“,”的文件，页面提示ERR_RESPONSE_HEADERS_MULTIPLE_CONTENT_DISPOSITION，需在filename上加双引号
response.setHeader("Content-disposition","attachment; filename=\""+StrUtil.GBToUnicode(title) + "\"");

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	if(url!=null && !url.equals("")){
		bis = new BufferedInputStream(new FileInputStream(url));
		bos = new BufferedOutputStream(response.getOutputStream());
		
		byte[] buff = new byte[2048];
		int bytesRead;
		
		while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
			bos.write(buff,0,bytesRead);
		}
	}
	
} catch(final IOException e) {
	System.out.println( "IOException." + e );
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

out.clear();
out = pageContext.pushBody();
%>