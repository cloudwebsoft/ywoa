<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="net.sf.json.JSONObject"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="java.io.File"%>
<%@ page import="java.util.*"%>
<%@ page import="java.awt.image.BufferedImage"%>
<%@ page import="javax.imageio.ImageIO"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print("对不起，请先登录！");
	 return;
}
String op = ParamUtil.get(request, "op");
JSONObject json = new JSONObject();
if(op.equals("showImg")){
	int attId = ParamUtil.getInt(request,"attId",0);
	int docId = ParamUtil.getInt(request,"docId",0);
	try{
		Attachment att = new Attachment(attId);
		String realPath = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
		File input = new File(realPath);
		BufferedImage image = ImageIO.read(input);
		if (image == null) {
			json.put("ret", 0);
			json.put("msg", "图片不存在！");
		} else {
			int w = image.getWidth();
			int h = image.getHeight();
			json.put("ret", 1);
			json.put("width", w);
			json.put("height", h);
			json.put("downloadUrl","fileark/getfile.jsp?docId="+docId+"&attachId="+attId);
		}
		out.print(json);
	}catch(Exception e){
		out.print(StrUtil.Alert(e.getMessage()));
		return;
	}
	return;
}else if(op.equals("showNextImg")){  //图片上下翻页
	boolean re = false;
	int attId = ParamUtil.getInt(request,"attId",0);
	int docId = ParamUtil.getInt(request,"docId",0);
	String arrow = ParamUtil.get(request,"arrow");//判断显示上一张还是下一张图片
	int isImgSearch = ParamUtil.getInt(request,"isImgSearch",0);
	int newId = 0;
	if(attId == 0){
		return;
	}
	try{
		Attachment att = new Attachment();
		newId = att.showNextImg(attId,docId,arrow,isImgSearch);
		Attachment attNew = new Attachment(newId);
		String realPath = Global.getRealPath() + attNew.getVisualPath() + "/" + attNew.getDiskName();
		File input = new File(realPath);
		BufferedImage image = ImageIO.read(input);
		if (image == null) {
			json.put("ret", 0);
			json.put("msg", "图片不存在！");
		} else {
			int w = image.getWidth();
			int h = image.getHeight();
			json.put("ret", 1);
			json.put("newId", newId);
			json.put("width", w);
			json.put("height", h);
			json.put("downloadUrl","fileark/getfile.jsp?docId="+docId+"&attachId="+newId);
		}
		out.print(json);
	}catch(Exception e){
		out.print(StrUtil.Alert(e.getMessage()));
		return;
	}
	return;
}
%>
