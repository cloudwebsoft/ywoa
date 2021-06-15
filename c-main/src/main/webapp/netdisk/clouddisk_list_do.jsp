<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.util.*"%><%@ page import="com.redmoon.oa.netdisk.*"%><%@ page import="com.redmoon.oa.dept.*"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.db.*"%><%@ page import = "org.json.*"%><%@page import="bsh.StringUtil"%><%@page import="net.sf.json.JSONObject"%><%@page import="java.sql.SQLException"%><%@page import="java.net.URLDecoder"%><%@page import="com.redmoon.clouddisk.db.CooperateDb"%><%@page import="com.redmoon.clouddisk.bean.CooperateBean"%><%@page import="cn.js.fan.web.Global"%><%@page import="com.redmoon.oa.util.TwoDimensionCode"%><%@page import="java.io.File"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/><%
if (!privilege.isUserLogin(request)) {
	out.print("对不起，请先登录！");
	 return;
}
String op = ParamUtil.get(request, "op");
String edit = ParamUtil.get(request, "edit");
JSONObject json = new JSONObject();
boolean re = false;

if(op.equals("publicShare")){
	
	PublicDirectory pd = new PublicDirectory();
	JSONObject resJson = null; 
	try {
		String dirCode = ParamUtil.get(request,"dirCode");
		String userName = ParamUtil.get(request,"userName");
		if(!dirCode.trim().equals("")){
			resJson = pd.publicShareDirectoy(dirCode,userName);
		}else{
			resJson = new JSONObject();
			resJson.put("result",-1);
		}
		
	} catch (ErrMsgException e1) {
		resJson = new JSONObject();
		resJson.put("result",-2);
		
	}finally{
		out.print(resJson);
	}
	
	
}else if (op.equals("AddChild")) {   // 新建文件夹
	Directory dir = new Directory();
	//Leaf leaf = new Leaf(dir.getCode());
	try {
		re = dir.AddChild(request);
		if (re) {
			CooperateMgr cm = new CooperateMgr();
			cm.isParentFolderRefused(dir.getCode());
			json.put("ret", "1");
			json.put("msg", "操作成功！");
			json.put("code", dir.getCode());
			json.put("docId", "0");
			json.put("name", dir.getName());
			out.print(json);
		} else {
			json.put("ret", "0");
			json.put("msg", "操作失败！");
			out.print(json);
		}
	} catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
	}
}
else if(op.equals("changeName")) {	// 文件夹重命名
	Directory dir = new Directory();
	String newName = ParamUtil.get(request, "name").replace(" ","");
	String dirCode = ParamUtil.get(request, "code");
	Leaf lf = dir.getLeaf(dirCode);
	Leaf plf = dir.getLeaf(lf.getParentCode());
	//先判断是否重名，然后再重命名
	if (plf!=null) {
		java.util.Iterator ir = plf.getChildren().iterator();
		while (ir.hasNext()) {
			Leaf lf2 = (Leaf)ir.next();
			if (lf2.getName().equals(newName)) {
				re = true;
				break;
			}
		}
				
		if (re) {
			json.put("ret", "0");
			json.put("msg", "指定的文件夹与现有文件夹重名！");
			out.print(json);
			return;
		}
	}
	try {
		re = lf.rename(newName);
		if (re) {
			json.put("ret",1);
			json.put("msg","修改成功！");
			json.put("name", newName);
			json.put("code", dirCode);
			out.print(json);
		}
		else{
			json.put("ret",0);
			json.put("msg","修改失败！");
			out.print(json);
		}
	} catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
	}
} 
else if (op.equals("changeFileName")){	// 文件重命名
	String newName = ParamUtil.get(request, "att_name");
	int attId = ParamUtil.getInt(request, "att_id", 0);
	int attDocId = ParamUtil.getInt(request, "att_docId", 0);
	int attOldId = ParamUtil.getInt(request, "att_oldId", 0);
	if (attId == 0) {
		json.put("ret","0");
		json.put("msg", "文件重名，修改失败！");
		out.print(json);
	}
	
	Attachment am = new Attachment();
	am = am.getAttachment(attId);
	String theName = am.getName();
	String ext=newName.substring(newName.lastIndexOf(".")+1);
	if (!(newName.equals(theName))) {
		try {
			am = am.getAttachment(attId);
			re = am.changeName(newName,attDocId);
			if (re) {
				attId = am.theNewId(newName);
				json.put("ret","1");
				json.put("msg","修改成功！");
				json.put("name",newName);
				json.put("attId",attId);
				json.put("attDocId",attDocId);
				json.put("attOldId",attOldId);
				json.put("ext",ext);
				json.put("url",Attachment.getIcon(ext));
				json.put("config",UtilTools.getConfigType(ext));
				out.print(json);
			}else{
				json.put("ret", "0");
				json.put("attId",attId);
				json.put("attDocId",attDocId);
				json.put("msg","指定的文件可能与现有文件重名或者文件不存在！");
				out.print(json);
			}
		} catch (Exception e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage() );
			out.print(json);
		}
	} else {
		json.put("ret", "0");
		json.put("msg", "指定的文件可能与现有文件重名！" );
		out.print(json);
		return;
	}
}
else if (op.equals("moveFile")){  //文件移动 或文件夹移动
	String type = ParamUtil.get(request,"type");
	String dirCode = ParamUtil.get(request, "dirCode"); //移动至的目标文件夹 
	String dirName = ParamUtil.get(request, "dirName");
	Directory dir = new Directory();
	Leaf lf = dir.getLeaf(dirCode);
	if (dirName == null || dirName.equals("")) {
		dirName = lf.getName();
		dirCode = lf.getCode();
	}
	if (dirCode == null || dirCode.equals("")) {
		dirCode = privilege.getUser(request);
	}
	//判断是文件还是文件夹移动
	if(type.equals("number")){
		int attId = ParamUtil.getInt(request, "att_id", 0);
			if (attId == 0) {
			json.put("ret","0");
			json.put("msg", "文件不存在");
			out.print(json);
			return;
		}
	
		Attachment att = new Attachment(attId);
		String attName = att.getName();
		int doc_id = lf.getDocId(); 
		int doc_oldId = att.getDocId();
		Leaf plf = dir.getLeaf(lf.getParentCode());
		try{
			if(doc_id == doc_oldId){
				json.put("ret","2");
				json.put("msg","移动文件成功");
				out.print(json);
				return;
			}
			re = att.isExist(attName,doc_id);
			if(re){
				json.put("ret","0");
				json.put("msg","目标文件夹内有重名文件存在！请更改文件名称！");
				out.print(json);
				return;
			}
			re = lf.moveFile(attId, dirCode, dirName);
			if(re){
				json.put("ret","1");
				json.put("msg","移动文件成功");
				json.put("doc_id",doc_id);
				json.put("doc_oldId",doc_oldId);
				out.print(json);
				return;
			}
		}catch(Exception e){
			json.put("ret", 0);
			json.put("msg", "移动文件失败");
			out.print(json);
			return;
		}
	}else{
		String dir_code = ParamUtil.get(request, "att_id"); //操作移动的文件夹 
		if (dir_code == "") {
			json.put("ret","0");
			json.put("msg", "文件夹不存在");
			out.print(json);
			return;
		}
		Leaf leaf = new Leaf(dir_code);
		if(leaf.getDocId()== lf.getDocId()){
			json.put("ret","2");
			json.put("msg","移动文件夹成功");
			out.print(json);
			return;
		}
		try{
			re = leaf.isExist(leaf.getName(),lf.getCode());
			if(re){
					json.put("ret","0");
					json.put("msg","目标文件夹内有重名文件夹存在！请更改文件夹名称！");
					out.print(json);
					return;
				}
			re = lf.moveFolder(dir_code, dirCode);
			if(re){
				CooperateMgr cm = new CooperateMgr();
				cm.isParentFolderRefused(dir_code);
				json.put("ret","2");
				json.put("msg","移动文件夹成功");
				json.put("oldCode",dir_code);
				out.print(json);
				return;
			}
		}catch(Exception e){
			json.put("ret", 0);
			json.put("msg", "移动文件夹失败");
			out.print(json);
			return;
		}
	}
	
}
else if (op.equals("restoreCurrent")){   //历史版本恢复 
	int attId = ParamUtil.getInt(request,"attId",0);
	int currentId =ParamUtil.getInt(request,"porposeId",0);
	int newId = 0;
	String name = "";
	String vDate = "";
	if (attId == 0) {
		out.print(StrUtil.Alert_Back("文件不存在！"));
	} else{
		Attachment att = new Attachment();
		try{
		att = att.getAttachment(currentId);
		name = att.getName();
		re = att.restoreCurrent(attId);
		newId = att.getId();
		vDate = DateUtil.format(att.getVersionDate(), "yyyy-MM-dd HH:mm");
		att = att.getAttachment(currentId);
		re = att.renameCurrent();
		att = att.getAttachment(attId);
		re = att.getCurrentCopy();
		re = att.del();
		}catch(Exception e){
			out.print( e.getMessage());
		}
		if (re) {
			json.put("ret","1");
			json.put("msg",name+"已还原至版本"+vDate);
			json.put("newId",newId); //create以后的新ID
			json.put("oldId",currentId); // 原页面上的ID
			json.put("name",name);
			json.put("ext",att.getExt());
			json.put("docId",att.getDocId());
			json.put("url",att.getIcon(att.getExt()));
			out.print(json);
			return;
		}else{
			json.put("ret","2");
			json.put("msg","还原文件失败");
			out.print(json);
			return;
		}	
	}
}
else if(op.equals("delFile")){   //文件夹逻辑删除
	String dirCode = ParamUtil.get(request , "dirCode");
	Attachment att = new Attachment();
	Leaf dir = new Leaf();
	Leaf lf = dir.getLeaf(dirCode);
	if (lf!=null) { // 防止反复刷新
		try {	
			re = dir.delFolders(lf);
		}
		catch (Exception e) {
			out.print(StrUtil.Alert(e.getMessage()));
			return;
		}
		if(re){
			json.put("ret","1");
			json.put("msg","文件夹已删除");
			json.put("dirCode",dirCode);
			out.print(json);
		}
		else {
			json.put("ret","0");
			json.put("msg","文件夹不存在");
			out.print(json);
		}
	}
	else{
		json.put("ret","0");
		json.put("msg","文件夹不存在");
		out.print(json);
	}
	
}

else if (op.equals("isExitFile")){
	String dirCode = ParamUtil.get(request , "dirCode");
	Attachment att = new Attachment();
	re = att.isExitFile(dirCode);
	if(!re){
		Leaf dir = new Leaf();
		Leaf lf = dir.getLeaf(dirCode);
		if (lf!=null) { // 防止反复刷新
			try {
				dir.delFolders(lf);
				re = true;
			}
			catch (Exception e) {
				out.print(StrUtil.Alert(e.getMessage()));
				return;
			}
			if(re){
				json.put("ret","1");
				json.put("msg","文件夹已删除");
				json.put("dirCode",dirCode);
				out.print(json);
			}
			else {
				json.put("ret","0");
				json.put("msg","文件夹不存在");
				out.print(json);
			}
		}
		else{
			json.put("ret","0");
			json.put("msg","文件夹不存在");
			out.print(json);
		}
	}
	else{
		json.put("ret","0");
		json.put("msg","文件夹内有文件,是否确定删除？");
		out.print(json);
	}
}
else if (op.equals("delAttach")){   //附件逻辑删除
	int attId = ParamUtil.getInt(request, "att_id", 0);
	int docId = ParamUtil.getInt(request, "att_docId", 0);
	if (attId == 0) {
		json.put("ret","0");
		json.put("msg", "文件不存在");
		out.print(json);
	}
	try {
		Attachment att = new Attachment(attId);
		re = att.delLogical();
		//re = docmanager.delAttachment(request);
		if(re){
			json.put("ret","1");
			json.put("msg","删除文件成功");
			json.put("att_id",attId);
			out.print(json);
			return;
		}
	}
	catch (Exception e) {
			json.put("ret","0");
			json.put("msg","删除文件失败");
			out.print(json);
			return;
	}
}
else if(op.equals("actionCooperate")){   //发起协作
	String userName = ParamUtil.get(request, "userName");
	userName = java.net.URLDecoder.decode(userName, "utf-8");
	String dirCode = ParamUtil.get(request, "dirCode");
	String names = ParamUtil.get(request, "names");
	CooperateMgr cm = new CooperateMgr();
	if(names.equals("")){
		re = false;
		json.put("msg","请填写协作对象！");
	}else{
		Leaf lf = new Leaf();
		CooperateBean cb = new CooperateBean();
		if(names.indexOf(",") >=0 ? true : false){
			re = cm.isRefused(userName,userName,lf.getFullPath(dirCode));
			if(!re){
				cb.setDirCode(dirCode);// 发起人数据添加
				cb.setUserName(userName);
				cb.setShareUser(userName);
				cb.setVisualPath(lf.getFullPath(dirCode));
				CooperateDb cd = new CooperateDb(cb);
				cd.create();
			}
			String[] ary_names = StrUtil.split(names, ","); //协作人数据添加
			for(int i = 0; i<ary_names.length ; i++){
				re = cm.isRefused(ary_names[i],userName,lf.getFullPath(dirCode));
				if(re){
					continue;
				}
				cb.setDirCode(dirCode);
				cb.setUserName(ary_names[i]);
				cb.setShareUser(userName);
				cb.setVisualPath(lf.getFullPath(dirCode));
				CooperateDb cd = new CooperateDb(cb);
				cd.create();
			}
		}
		else{
			re = cm.isRefused(userName,userName,lf.getFullPath(dirCode));
			if(!re){
				cb.setDirCode(dirCode);// 发起人数据添加
				cb.setUserName(userName);
				cb.setShareUser(userName);
				cb.setVisualPath(lf.getFullPath(dirCode));
				CooperateDb cd = new CooperateDb(cb);
				cd.create();
			}
			re = cm.isRefused(names,userName,lf.getFullPath(dirCode));
			if(!re){
				String ary_names = names; //协作人数据添加
				cb.setDirCode(dirCode);
				cb.setUserName(ary_names);
				cb.setShareUser(userName);
				cb.setVisualPath(lf.getFullPath(dirCode));
				CooperateDb cd = new CooperateDb(cb);
				cd.create();
			}
		}
		
		lf = new Leaf(dirCode);//将分享的文件夹shared变为1
		lf.setShared(true);
		re = lf.update(); 
		
		if(!re){
			json.put("msg","发起协作失败！");
		}
	}
	if(re){
		json.put("ret","1");
		json.put("msg","发起协作成功！");
	}else{
		json.put("ret","0");
	}
	out.print(json);
}
else if (op.equals("isSharedDir")){    //判断是否存在没有同意的已分享文件
	String userName = ParamUtil.get(request,"userName");
	userName = java.net.URLDecoder.decode(userName, "utf-8");
	CooperateMgr cmg = new CooperateMgr();
	String info = cmg.isSharedDir(userName);
	if(info.equals("")){
		json.put("ret","0");
	}else{
		json.put("ret","1");
		json.put("list",info);
	}
	out.print(json);
}
else if (op.equals("agreedShared")){    //同意接受别人发起的协作文件
	String code = ParamUtil.get(request,"dirCode");
	String userName = ParamUtil.get(request,"userName");
	userName = java.net.URLDecoder.decode(userName, "utf-8");
	CooperateMgr cm = new CooperateMgr();
	re = cm.agreeShared(userName, code);
	if(re){
		%>
		<script>
		window.parent.location.reload(true);
		</script>
		<%
	}
}
else if (op.equals("refusedShared")){    //拒绝接受别人发起的协作文件
	String code = ParamUtil.get(request,"dirCode");
	String userName = ParamUtil.get(request,"userName");
	userName = java.net.URLDecoder.decode(userName, "utf-8");
	CooperateMgr cm = new CooperateMgr();
	re = cm.refusedShared(userName, code);
	if(re){
		%>
		<script>
		window.parent.location.reload(true);
		</script>
		<%
	}
}
else if (op.equals("deCooperateFolder")){  //取消文件夹的分享
	String code = ParamUtil.get(request,"dirCode");
	CooperateMgr cm = new CooperateMgr();
	re = cm.noRefused(code);
	if(re){
		json.put("ret","1");
		out.print(json);
	}else{
		json.put("ret","0");
		out.print(json);
	}
}
else if (op.equals("delBatch") || op.equals("delBatch_tiled")){   //批量逻辑删除（包括tiled页面）
	String ids = ParamUtil.get(request, "att_ids");
	String ids_folder = ParamUtil.get(request , "dir_ids");
	String dir_ids = "";
	String[] ary_tiled = StrUtil.split(ids_folder, ",");
	if(op.equals("delBatch_tiled")){
		if (ary_tiled != null){
			for (int i = 0; i < ary_tiled.length; i++) {
				String dirCods = ary_tiled[i];
				try {
					Leaf lf_tiled = new Leaf(dirCods);
					Leaf leaf_tiled = new Leaf();
					leaf_tiled.delFolders(lf_tiled);
					dir_ids += dirCods+",";
					re = true;
				}
				catch (Exception e) {
					re = false;
				}
			}
		}
	}
	String[] ary = StrUtil.split(ids, ",");
	String att_ids = "";
	if (ary != null){
		for (int i = 0; i < ary.length; i++) {
			int attId = StrUtil.toInt(ary[i]);
			try {
				Attachment att = new Attachment(attId);
				re = att.delLogical();
				att_ids += attId+",";
			}
			catch (Exception e) {
				re = false;
			}
		}
	}
	if(re){
		json.put("ret","1");
		json.put("msg","删除文件成功");
		json.put("att_ids",att_ids);
		json.put("dir_ids",dir_ids);
		out.print(json);
	}
	else{
		json.put("ret","0");
		json.put("msg","删除文件失败");
		out.print(json);
	}
}
else if (op.equals("removeAttach")){   //文件彻底删除
	int attId = ParamUtil.getInt(request, "att_id", 0);
	//int docId = ParamUtil.getInt(request, "att_docId", 0);
	if (attId == 0) {
		json.put("ret","0");
		json.put("msg", "文件不存在");
		out.print(json);
	}
	try {
		Attachment att = new Attachment(attId);
		re = att.del();
		//re = docmanager.delAttachment(request);
		if(re){
			json.put("ret","1");
			json.put("msg","删除文件成功");
			json.put("att_id",attId);
			out.print(json);
			return;
		}
	}
	catch (Exception e) {
			json.put("ret","0");
			json.put("msg","删除文件失败");
			out.print(json);
			return;
	}
}
else if (op.equals("removeFolder")){   //文件夹彻底删除
	String docCode = ParamUtil.get(request, "dir_code");
	if (docCode.equals("")) {
		json.put("ret","0");
		json.put("msg", "文件不存在");
		out.print(json);
	}

	Leaf lf = new Leaf(docCode);
	re = lf.remove(docCode);
	//re = docmanager.delAttachment(request);
	if(re){
		json.put("ret","1");
		json.put("msg","文件夹已删除");
		json.put("dirCode",docCode);
		out.print(json);
	}else{
		json.put("ret","0");
		json.put("msg","删除文件夹失败");
		out.print(json);
	}
	
}
else if (op.equals("removeBatch")){    //批量彻底删除
	String ids = ParamUtil.get(request, "att_ids");
	String ids_folder = ParamUtil.get(request , "dir_ids");
	String[] ary = StrUtil.split(ids, ",");
	String[] ary_dir = StrUtil.split(ids_folder, ",");
	String att_ids = "";
	String dir_ids = "";
	if(ary_dir != null){
		for (int i = 0; i < ary_dir.length; i++) {
			String dirCods = ary_dir[i];
			try {
				Leaf lf_tiled = new Leaf(dirCods);
				Leaf leaf_tiled = new Leaf();
				re = leaf_tiled.remove(lf_tiled.getCode());
				dir_ids += dirCods+",";
			}
			catch (Exception e) {
				re = false;
			}
		}
	}
	if(ary != null){
		for (int i = 0; i < ary.length; i++) {
			int attId = StrUtil.toInt(ary[i]);
			try {
				Attachment att = new Attachment(attId);
				re = att.del();
				att_ids += attId+",";
				re = true;
			}
			catch (Exception e) {
				re = false;
			}
		}
	}
	if(re){
		json.put("ret","1");
		json.put("msg","删除文件成功");
		json.put("att_ids",att_ids);
		json.put("dir_ids",dir_ids);
		out.print(json);
	}
	else{
		json.put("ret","0");
		json.put("msg","删除文件失败");
		out.print(json);
	}
	
}
else if(op.equals("restoreAttach")){    //回收站还原
	int attId = ParamUtil.getInt(request, "att_id", 0);
	//int docId = ParamUtil.getInt(request, "att_docId", 0);
	if (attId == 0) {
		json.put("ret","0");
		json.put("msg", "文件不存在");
		out.print(json);
	}
	try {
		Attachment att = new Attachment(attId);
		re = att.restore();
		//re = docmanager.delAttachment(request);
		if(re){
			json.put("ret","1");
			json.put("msg","还原文件成功");
			json.put("att_id",attId);
			out.print(json);
			return;
		}
	}
	catch (Exception e) {
			json.put("ret","0");
			json.put("msg","还原文件失败");
			out.print(json);
			return;
	}
}
else if(op.equals("restoreFolder")){    //回收站还原文件夹
	String docCode = ParamUtil.get(request, "dir_code");
	if (docCode.equals("")) {
		json.put("ret","0");
		json.put("msg", "文件不存在");
		out.print(json);
	}
	try {
		Leaf lf = new Leaf(docCode);
		re = lf.isSameName();
		if(re){
			json.put("ret","0");
			json.put("msg","根目录下有与恢复文件夹重名的文件夹，请修改后再恢复");
			out.print(json);
		}
		else{
			re = lf.restore(docCode);
			//re = docmanager.delAttachment(request);
			if(re){
				json.put("ret","1");
				json.put("msg","还原文件夹成功");
				json.put("dirCode",docCode);
				out.print(json);
			}else {
				json.put("ret","0");
				json.put("msg","还原文件夹失败");
				out.print(json);
				
			}
		}
	}
	catch (Exception e) {
			json.put("ret","0");
			json.put("msg","还原文件夹失败");
			out.print(json);
	}
}
else if (op.equals("restoreBatch")){    //批量回收站还原
	String ids = ParamUtil.get(request, "att_ids");
	String ids_folder = ParamUtil.get(request , "dir_ids");
	String[] ary = StrUtil.split(ids, ",");
	String[] ary_dir = StrUtil.split(ids_folder, ",");
	String att_ids = "";
	String dir_ids = "";
	if(ary_dir != null){
		for (int i = 0; i < ary_dir.length; i++) {
			String dirCods = ary_dir[i];
			try {
				Leaf lf_tiled = new Leaf(dirCods);
				Leaf leaf_tiled = new Leaf();
				re = leaf_tiled.restore(lf_tiled.getCode());
				dir_ids += dirCods+",";
			}
			catch (Exception e) {
				re = false;
			}
		}
	}
	if(ary != null){
		for (int i = 0; i < ary.length; i++) {
			int attId = StrUtil.toInt(ary[i]);
			try {
				Attachment att = new Attachment(attId);
				re = att.restore();
				att_ids += attId+",";
				re = true;
			}
			catch (Exception e) {
				re = false;
			}
		}
	}
	if(re){
		json.put("ret","1");
		json.put("msg","还原文件成功");
		json.put("att_ids",att_ids);
		json.put("dir_ids",dir_ids);
		out.print(json);
	}
	else{
		json.put("ret","0");
		json.put("msg","还原文件失败");
		out.print(json);
	}
}
else if(op.equals("showTree")){   //显示树结构
	String userName = ParamUtil.get(request,"user_name");
	String mode = ParamUtil.get(request, "mode");
	String dirCode = ParamUtil.get(request,"dir_code");
	Directory dir = new Directory();
	Leaf leaf = dir.getLeaf(userName);
	DirectoryView tv = new DirectoryView(leaf);
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));
	String pageUrl = usd.isWebedit()?"dir_list.jsp":"dir_list_new.jsp";
	
	StringBuilder sb = new StringBuilder();
	String divTree = tv.ListSimple(sb, "mainFileFrame", pageUrl, "op=editarticle&mode=" + mode, "showTable", "treeMouseOver",dirCode); 
	String theTree ="";
	theTree = divTree;
	
	json.put("ret","1");
	json.put("msg",theTree);
	out.print(json);
	return;
}
else if(op.equals("showTxt")){  //显示txt内容
	int attId = ParamUtil.getInt(request,"attId",0);
	Attachment att = new Attachment(attId);
	String ext = att.getExt();
	String txtInfo = att.getTxtInfo(att);
	json.put("ret","1");
	json.put("msg",txtInfo);
	out.print(json);
	return;
}

else if(op.equals("role_template")){
	String checkRoleCodes =  StrUtil.getNullStr(ParamUtil.get(request, "check_role_codes"));
	String unCheckRoleCodes =  StrUtil.getNullStr(ParamUtil.get(request, "uncheck_role_codes"));
	String dirCode =  StrUtil.getNullStr(ParamUtil.get(request, "dir_code"));
	RoleTemplateMgr rt = new RoleTemplateMgr();
	JSONObject resultJson = null; 
	try {
		resultJson = rt.uploadTemplate(checkRoleCodes,unCheckRoleCodes,dirCode);
		out.print(resultJson);
	} catch (ErrMsgException e1) {
		// TODO Auto-generated catch block
		resultJson = new JSONObject();
		resultJson.put("result",false);
		out.print(resultJson);
	}catch (SQLException e2){
		resultJson = new JSONObject();
		resultJson.put("result",false);
		out.print(resultJson);
	}
}
else if(op.equals("history")){   //历史版本弹窗
	int attId = ParamUtil.getInt(request,"attId",0);
	Attachment att = new Attachment();
	out.print(att.queryMyHistoryLogByAjax(attId));	
}
else if(op.equals("showImg")){
	int attId = ParamUtil.getInt(request,"attId",0);
	try{
		Attachment att = new Attachment();
		json = att.getImgSrc(attId);
		out.print(json);
	}catch(Exception e){
		out.print(StrUtil.Alert(e.getMessage()));
		return;
	}
}
else if(op.equals("showNextImg")){  //图片上下翻页
	int attId = ParamUtil.getInt(request,"att_id",0);
	String arrow = ParamUtil.get(request,"arrow");
	int isImgSearch = ParamUtil.getInt(request,"isImgSearch",0);
	int newId = 0;
	if(attId == 0){return;}
	try{
		Attachment att = new Attachment();
		newId = att.showNextImg(attId,arrow,isImgSearch);
		Attachment attNew = new Attachment(newId);
		json = attNew.getImgSrc(newId);
		if(newId != 0){
			re = true;
		}else{
			re = false;
		}
	}catch(Exception e){
		re = false;
	}
	if(re){
		json.put("ret","1");
		json.put("newId",newId);
		out.print(json);
	}
	else{
		json.put("ret","0");
		out.print(json);
	}
}
else if(op.equals("sidebar_setup")){   //更改sidebar显示主题
	String userName =ParamUtil.get(request,"user_name");
	int upDiv =ParamUtil.getInt(request,"upDiv",0);
	String notice_topic=ParamUtil.get(request,"notice_topic");
	int is_show = ParamUtil.getInt(request,"showNotice",1);
	SideBarMgr sbm = new SideBarMgr();
	int mod_flag = sbm.getFlag(userName,is_show,upDiv,"notice","","",1);
	re = sbm.updateUpDiv(userName,is_show,upDiv,"notice",SideBarDb.TYPE_NOTICE,mod_flag);
	if(re){
		re = sbm.updateNoticeTopic(userName,666,notice_topic,mod_flag);
	}
	if(re){
		json.put("ret","1");
		out.print(json);
	}else{
		json.put("ret","0");
		out.print(json);
	}
}
else if(op.equals("sidebar_setup_item")){  //更改sidebar图片属性
	String user_name =ParamUtil.get(request,"user_name");
	int is_show = ParamUtil.getInt(request,"is_show",1);
	String p = ParamUtil.get(request,"position");
	int i = p.indexOf("_");
	p = p.substring(i+1);
	int position = Integer.parseInt(p);
	String title = ParamUtil.get(request,"title");
	String picture = ParamUtil.get(request,"picture");
	String href = ParamUtil.get(request,"href");
	int custom = ParamUtil.getInt(request,"custom",0);
	SideBarMgr sbm = new SideBarMgr();
	int flag = sbm.getFlag(user_name,is_show,position,title,picture,href,custom);
	re = sbm.updateArray(user_name,is_show,position,title,picture,href,SideBarDb.TYPE_PICTURE,custom,flag);
	sbm.cleanDiyImg(user_name); //清除当前用户自定义冗余图片缓存
	if(re){
		json.put("ret","1");
		json.put("msg","设置完成。");
		out.print(json);
	}
	else{
		json.put("ret","0");
		json.put("msg","设置失败。");
		out.print(json);
	}
}else if(op.equals("getSideBar")){   //获取sidebar图片属性 
	String userName = ParamUtil.get(request,"user_name");
	NetDiskSideBar ndsb = new NetDiskSideBar();
	out.print(ndsb.querySideBarLogByAjax(userName));
}else if(op.equals("sidebar_recover")){  // 恢复sidebar默认图片属性
	String userName = ParamUtil.get(request,"user_name");
	SideBarMgr sbm = new SideBarMgr();
	re = sbm.recover(userName);
	if(re){
		//out.print(StrUtil.jAlert_Back("初始化成功！","提示"));
		json.put("ret","1");
		json.put("msg","初始化成功！");
		out.print(json);
	}else{
		//out.print(StrUtil.jAlert_Back("初始化失败！","提示"));
		json.put("ret","0");
		json.put("msg","初始化失败！");
		out.print(json);
	}
}else if(op.equals("sidebar_diyImg")){     //设置自定义图片
	String userName = ParamUtil.get(request,"userName");
	int fileId = ParamUtil.getInt(request,"fileId",0);
	SideBarMgr ndsb = new SideBarMgr();
	String defaultName = ndsb.diyImg(userName,fileId);
	defaultName = userName + "/" + defaultName;
	json.put("ret","1");
	json.put("defaultPath",defaultName);
	out.print(json);
	
	
}
/*else if (op.equals("template")){
	RoleTemplateMgr rt = new RoleTemplateMgr();
	try{
		rt.uploadRoleTemplate(request);
	}catch(ErrMsgException e){
		
	}
	out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_template.jsp"));
	/*String roleCodes = ParamUtil.get(request,"ids");
	String rootCode = ParamUtil.get(request,"root_code");
	Leaf leaf = new Leaf();
	Directory dir = new Directory();
	String theParentCode = dir.getCode();
	if(!dirCode.equals("")){
		String[] ary = StrUtil.split(roleCodes, ",");
		if (ary == null)
			return ;
			leaf = new Leaf(dirCode);
			leaf.setParentCode(theParentCode);
		for (int i = 0; i < ary.length; i++) {
			String roleCode = ary[i];
			try {
				RoleTemplateMgr rt = new RoleTemplateMgr();
				rt.setDirCode(dirCode);
				rt.setRoleCode(roleCode);
				re = rt.updateTemplate();
				out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_template.jsp"));
			}
			catch (Exception e) {
				out.print(StrUtil.Alert_Redirect("操作失败！","clouddisk_template.jsp"));
			}
		}
	}else{
		out.print(StrUtil.Alert_Redirect("操作失败！","clouddisk_template.jsp"));
	}*/
else if(op.equals("templateRole")){ //查询模板角色
	RoleTemplateMgr rt = new RoleTemplateMgr();
	String unit_code = StrUtil.getNullStr(ParamUtil.get(request,"unit_code"));
	String dirCode =  StrUtil.getNullStr(ParamUtil.get(request,"dirCode"));
	JSONObject resultObj = null;	
	if(!unit_code.trim().equals("") && !dirCode.trim().equals("")) {
		try {
			resultObj = rt.showRolesByTemplete(unit_code,dirCode);
			out.print(resultObj);
		}catch (SQLException e2){
			resultObj = new JSONObject();
			resultObj.put("result",false);
			out.print(resultObj);
		}
	}else{
		JSONObject errorRes = new JSONObject();
		errorRes.put("result",false);
		out.print(errorRes);
	}
	return;
}
else if (op.equals("space")) {
	String userName = privilege.getUser(request);
	UserDb ud = new UserDb(userName);
	if (ud != null && ud.isLoaded()) {
		json.put("ret","1");
		String diskSpace = UtilTools.getFileSize(ud.getDiskSpaceAllowed());
		String diskUsed = UtilTools.getFileSize(ud.getDiskSpaceUsed());
		json.put("msg",diskUsed + "/" + diskSpace);
	} else {
		json.put("ret","0");
	}
	out.print(json);
	return;
}
else if(op.equals("tdcode")){
	int id = ParamUtil.getInt(request, "attId", 0);
	String userName = privilege.getUser(request);
	String curTime = DateUtil.format(new Date(), "yyyyMMddHHmmss");
	Attachment at = new Attachment(id);
	//String content = Global.getFullRootPath(request)+"/upfile/file_netdisk/"+at.getVisualPath()+"/"+at.getDiskName();
	String content = Global.getFullRootPath(request) + "/public/clouddisk_getfilebytdcode.jsp?id=" + id + "&authKey=" + ThreeDesUtil.encrypt2hex(com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY, userName+"|"+curTime);
	//String content =  "http://192.168.1.8:8080/yimioa/public/clouddisk_getfilebytdcode.jsp?id=" + id + "&authKey=" + ThreeDesUtil.encrypt2hex(com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY, userName+"|"+curTime);
	//String content = "http://192.168.1.8:8080/yimioa/upfile/file_netdisk/"+at.getVisualPath()+"/"+at.getDiskName();
	// 把content生成短链接
	
	
	String path = Global.getRealPath() + "upfile/tdcode";
	File file = new File(path);
	if (!file.exists()) {
		file.mkdirs();
	}
	path += "/tdcode.jpg";
	file = new File(path);
	if(file.exists()){
		file.delete();
	}
	TwoDimensionCode.generate2DCode(content, path, "jpg");
	String showPath = "../upfile/tdcode/tdcode.jpg";
	json.put("ret","1");
	json.put("showPath",showPath);
	out.print(json);
	return;
}

%>


