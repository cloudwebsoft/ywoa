<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.util.*"%><%@ page import="com.redmoon.oa.netdisk.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import = "org.json.*"%>
<%@page import="java.io.IOException"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print("对不起，请先登录！");
	 return;
}
String op = ParamUtil.get(request, "op");
PublicLeafPrivMgr lpMgr = new PublicLeafPrivMgr();

// 新建文件夹
if (op.equals("modifyPriv")) {
	JSONObject result = new JSONObject();
	try {
		int see = ParamUtil.getInt(request,"see",0);
		int append = ParamUtil.getInt(request,"append",0);
		int del = ParamUtil.getInt(request,"del",0);
		int modify = ParamUtil.getInt(request,"modify",0);
		int examine = ParamUtil.getInt(request,"examine",0);
		int id = ParamUtil.getInt(request,"id",0);
		String dirCode = ParamUtil.get(request,"dirCode");
		PublicLeafPriv lp = new PublicLeafPriv();
		if (examine==1) {
			see = 1;
			append = 1;
			modify = 1;
			del = 1;
		}
		else if (modify==1 || del==1) {
			if(see == 1 && append == 1 && modify == 1 && del == 1){
				examine = 1;
			}else{
				see = 1;
			}
			
		}
		lp.setExamine(examine);
		lp.setSee(see);
		lp.setId(id);
		lp.setDirCode(dirCode);
		lp.setModify(modify);
		lp.setDel(del);
		lp.setAppend(append);
		result = lpMgr.modifyPriv(lp);
	} catch (JSONException e) {
		result.put("result",false);
		
	}finally{
		out.print(result);
	}
}else if(op.equals("delPriv")){
	JSONObject result = new JSONObject();
	try {
		int id = ParamUtil.getInt(request,"id",0);
		PublicLeafPriv lp = new PublicLeafPriv();
		lp = lp.getPublicLeafPriv(id);
		boolean flag = lp.del();
		result.put("result",flag);
	} catch (JSONException e) {
		result.put("result",false);
		
	}finally{
		out.print(result);
	}
}else if(op.equals("delPublicDir")){
	JSONObject result = new JSONObject();
	try {
		String dirCode = ParamUtil.get(request,"dirCode");
		PublicLeaf pl = new PublicLeaf();
		pl = pl.getLeaf(dirCode);
		pl.del(pl); //删除文件夹 以及文件夹下的子文件
		result.put("result",true);
	} catch (Exception e) {
		result.put("result",false);
		
	}finally{
		out.print(result);
	}
	
}else if(op.equals("changeAttName")){//附件重命名
	JSONObject result = new JSONObject();
	try {
		int attId = ParamUtil.getInt(request,"attId",0);
		String attName = ParamUtil.get(request,"attName").trim();
		if(attId != 0 && !attName.equals("")){
			PublicAttachmentMgr pam = new PublicAttachmentMgr();
			result = pam.changePublicAttName(attId,attName);
		}
	} catch (JSONException e) {
		result.put("result",-1);
	}finally{
		out.print(result);
	}
}else if(op.equals("delAtt")){//删除附件
	JSONObject result = new JSONObject();
	try {
		int attId = ParamUtil.getInt(request,"attId",0);
		if(attId != 0){
			PublicAttachment pa = new PublicAttachment(attId);
			boolean flag = pa.del();
			result.put("result",flag);
		}
	} catch (JSONException e) {
		result.put("result",false);
	}finally{
		out.print(result);
	}
}else if(op.equals("showPublicImg")){ //公用共享图片显示
	JSONObject result = new JSONObject();
	try {
		int attId = ParamUtil.getInt(request,"attId",0);
		if(attId != 0){
			PublicAttachmentMgr  pam = new PublicAttachmentMgr();
			PublicAttachment pa = new PublicAttachment(attId);
			result = pam.getImgSrc(pa);//获得图片路径
		}
	} catch (JSONException e) {
		result.put("ret",0);
	}catch (IOException e) {
		result.put("ret",0);
	}finally{
		out.print(result);
	}
}else if(op.equals("showNextImg")){ //公用共享图片显示
	JSONObject result = new JSONObject();
	int newId = 0;
	try {
		int attId = ParamUtil.getInt(request,"att_id",0);
		String arrow = ParamUtil.get(request,"arrow");
		
		if(attId != 0){
			PublicAttachmentMgr  pam = new PublicAttachmentMgr();
			newId = pam.showNextImg(attId,arrow);
			PublicAttachment pa = new PublicAttachment(newId);
			result = pam.getImgSrc(pa);//获得图片路径
			if(result.getInt("ret") == 1){
				result.put("newId",newId);
			}
		}
	} catch (JSONException e) {
		result.put("ret",0);
	}catch (IOException e) {
		result.put("ret",1);
		if(newId !=0){
			result.put("newId",newId);
		}
	}finally{
		out.print(result);
	}
}else if(op.equals("showPublicTxt")){ //预览txt文档
	JSONObject result = new JSONObject();
	try {
		int attId = ParamUtil.getInt(request,"attId",0);
		if(attId != 0){
			PublicAttachmentMgr  pam = new PublicAttachmentMgr();
			PublicAttachment pa = new PublicAttachment(attId);
			String txtContent = pam.getTxtInfo(pa);//获得txt
			result.put("result",true);
			result.put("txtContent",txtContent);
		}
	} catch (JSONException e) {
		result.put("result",false);
	}finally{
		out.print(result);
	}
}else if(op.equals("showPublicTree")){ //公用共享图片显示
	JSONObject result = new JSONObject();
	try {
		PublicDirectory publicDir = new PublicDirectory();
		PublicLeaf pl = new PublicLeaf(PublicLeaf.ROOTCODE);
		PublicDirectoryView pdv = new PublicDirectoryView(pl);
		String treeShow = "";
		StringBuilder sb = new StringBuilder();
		treeShow = pdv.ListSimple(sb, "mainFileFrame", "", "showTable", "treeMouseOver");
		result.put("result",true);
		result.put("treeContent",treeShow);
	} catch (JSONException e) {
		result.put("result",false);
	}finally{
		out.print(result);
	}
}else if(op.equals("movePublicAtt")){//移动公共共享中附件
	JSONObject result = new JSONObject();
	try {
		int attId = ParamUtil.getInt(request,"attId",0);
		String publicDirCode = ParamUtil.get(request,"publicDirCode").trim();
		if(attId != 0 && !publicDirCode.equals("")){
			PublicAttachmentMgr pam = new PublicAttachmentMgr();
			result = pam.movePublicAtt(attId,publicDirCode);
		    
		}
	} catch (JSONException e) {
		result.put("result",-1);
	}finally{
		out.print(result);
	}
}else if(op.equals("delBatchAtt")){//批量上传
	JSONObject result = new JSONObject();
	boolean flag =true;
	try {
		String ids = ParamUtil.get(request, "att_ids");
		if(!ids.trim().equals("") && ids!= null){
			String[] idsArr = ids.split(",");
			if(idsArr!=null && idsArr.length>0){
				for(String id : idsArr){
					PublicAttachment pa = new PublicAttachment(StrUtil.toInt(id));
					flag &= pa.del();
				}
			}
		}
		result.put("result",flag);
	} catch (JSONException e) {
		result.put("result",false);
	}finally{
		out.print(result);
	}
	
}
%>
