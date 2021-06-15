<%@ page contentType="text/html; charset=utf-8" %><%@ page import="com.redmoon.oa.netdisk.*"%><%@ page import="cn.js.fan.util.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>
<%
if (!privilege.isUserLogin(request)) {
	// out.print("对不起，请先登录！");
	// return;
}
String op = ParamUtil.get(request, "op");
if (op.equals("changeattachname")) {
	boolean re = false;
	try {
		re = docmanager.updateAttachmentName(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Back("修改成功！"));
		%>
		<script>
		// window.parent.location.reload(true);
		</script>
		<%
	}
	
	return;
}

if (op.equals("delAttach")) {
	boolean re = false;
	try {
		re = docmanager.delAttachment(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		%>
		<script>
		// window.alert("删除成功！");
		window.parent.location.reload(true);
		</script>
		<%
	}
	
	return;
}
else if (op.equals("remove")) {
	boolean re = false;
	try {
		re = docmanager.remove(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		%>
		<script>
		// window.alert("删除成功！");
		//window.location.reload(true);
		//window.location.href="clouddisk_recycler.jsp";
		</script>
		<%
		out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_recycler.jsp"));
	}
	
	return;
}
else if (op.equals("removeBatch")) {
	boolean re = false;
	try {
		re = docmanager.removeBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		%>
		<script>
		// window.alert("删除成功！");
		window.location.href="clouddisk_recycler.jsp";
		</script>
		<%
	}
	
	return;
}
else if (op.equals("delAttachBatch")||op.equals("delAttachBatch_tiled")) {
	boolean re = false;
	String ids_folder = ParamUtil.get(request , "ids_folder");
	int page_num = ParamUtil.getInt(request, "page_num");
	//DocContent dc = doc.getDocContent(page_num);
	String[] ary = StrUtil.split(ids_folder, ",");
	Attachment att = new Attachment();
	Leaf dir = new Leaf();
	//alert(ids_folder);
	//return;
	try {
		if(op.equals("delAttachBatch")){
			re = docmanager.delAttachmentBatch(request);
			out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_list.jsp?op=editarticle"));
		}else{
			if(ary != null){
				for (int i = 0; i < ary.length; i++) {
					re = att.isExitFile(ary[i]);
					if(re){
						//re = false;
						out.print(StrUtil.Alert_Redirect("文件夹内有文件，不能删除！","clouddisk_tiled.jsp?op=editarticle"));
						return ;
					}
					if(!re){
						Leaf lf = dir.getLeaf(ary[i]);
						if (lf!=null) { // 防止反复刷新
							try {
								dir.del(lf);
							}
							catch (Exception e) {
								out.print(StrUtil.Alert(e.getMessage()));
							}
						}
					}	
				}
			}
			if(!re){
				re = docmanager.delAttachmentBatch(request);
				out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_tiled.jsp?op=editarticle"));
			}
			
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		String dir_code = ParamUtil.get(request, "dir_code");
		//String uploadMode = ParamUtil.get(request, "uploadMode");
		String pageView = "clouddisk_list.jsp";
		//if (uploadMode.equals("new")) {
		if(op.equals("delAttachBatch_tiled")){
				pageView = "clouddisk_tiled.jsp";
		}
		//}
		%>
		<script>
		// window.alert("删除成功！");
		// window.parent.location.reload(true);
		window.location.href = "<%=pageView%>?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle";
		</script>
		<%
	}
	return;
}
else if (op.equals("restore")) {
	boolean re = false;
	try {
		re = docmanager.restore(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		%>
		<script>
		window.alert("已经还原至根目录文件夹下！");
		window.parent.location.reload(true);
		</script>
		<%
	}
	
	return ;
}
else if (op.equals("upload")) {
	boolean re = false;
	try {
		re = docmanager.uploadFile(application, request);
	}
	catch (ErrMsgException e) {
		System.out.println(getClass() + " " + e.getMessage());
		out.print(StrUtil.Alert(e.getMessage()));
	}
	return;
}


else if (op.equals("AddChild")) {
	out.print("1");
	return;
	/**String parent_code = ParamUtil.get(request, "parent_code");
	boolean re = false;
	Directory dir = new Directory();
	try {
		//re = dir.AddChild(request);
		out.print(true);	
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (!re) {
		out.print(StrUtil.Alert_Back("添加节点失败，请检查编码是否重复！"));
		return;
	}*/
}


boolean re = false;
//String isuploadfile = StrUtil.getNullString(request.getParameter("isuploadfile"));
try {
	//if (isuploadfile.equals("false"))
		//re = docmanager.UpdateWithoutFile(request);
	//else
		re = docmanager.Operate(application, request, privilege);
}
catch(ErrMsgException e) {
	//if (isuploadfile.equals("false")) {
	//	out.print(StrUtil.Alert(e.getMessage()));
	//}
	//else
		out.print(e.getMessage());
}
if (re) {
	out.print("操作成功！");
}
else {
	out.print("操作失败！");
}
%>