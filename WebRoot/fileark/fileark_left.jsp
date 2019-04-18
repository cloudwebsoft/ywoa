<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "com.redmoon.oa.fileark.*" %>
<%@ page import = "java.util.*" %>
<%@ page import = "cn.js.fan.util.*" %>
<%@ page import = "cn.js.fan.web.*" %>
<%@ page import = "org.json.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	JSONObject json = new JSONObject();

	String delcode = ParamUtil.get(request, "delcode");
	try {
		Directory dir = new Directory();
		dir.del(request, delcode);
	}
	catch (ErrMsgException e) {
		json.put("ret", 3);	
		json.put("msg", e.getMessage());	
		out.print(json);
		return;
	}
	json.put("ret", 1);	
	out.print(json.toString());
	return;
}
else if (op.equals("move")) {
	JSONObject json = new JSONObject();
	String code = ParamUtil.get(request, "code");
	String parent_code = ParamUtil.get(request, "parent_code");
	int position = Integer.parseInt(ParamUtil.get(request, "position"));
	if("root".equals(code)) {
		json.put("ret", "0");
		json.put("msg", "根节点不能移动！");
		out.print(json.toString());	
		return;
	}
	if("#".equals(parent_code)){
		json.put("ret", "0");
		json.put("msg", "不能与根节点平级！");
		out.print(json.toString());	
		return;
	}
        	
	Directory dir = new Directory();
	Leaf moveleaf = dir.getLeaf(code);
	int old_position = moveleaf.getOrders();//得到被移动节点原来的位置
	String old_parent_code = moveleaf.getParentCode();
	
	Privilege privilege = new Privilege();
    LeafPriv lp = new LeafPriv();
    lp.setDirCode(code);
    if (!lp.canUserExamine(privilege.getUser(request))) {
        throw new ErrMsgException(SkinUtil.LoadString(request,
                "pvg_invalid"));
    }
    
    if (!parent_code.equals(old_parent_code)) {
	    lp.setDirCode(parent_code);
	    if (!lp.canUserExamine(privilege.getUser(request))) {
	        throw new ErrMsgException(SkinUtil.LoadString(request,
	                "pvg_invalid"));
	    }	
    }
	
	Leaf newParentLeaf = dir.getLeaf(parent_code);
	
	int p = position + 1;
	moveleaf.setOrders(p);	
    if (!parent_code.equals(old_parent_code)) {
		moveleaf.update(parent_code);
	}
	else {
		moveleaf.update();
	}
	
	// 重新梳理orders
	Iterator ir = newParentLeaf.getChildren().iterator();
	while (ir.hasNext()) {
		Leaf lf = (Leaf)ir.next();
		// 跳过自己
		if (lf.getCode().equals(code)) {
			continue;
		}
		if(p<old_position){//上移
			if (lf.getOrders()>=p) {
				lf.setOrders(lf.getOrders() + 1);
				lf.update();
			}
		}else{//下移
			if(lf.getOrders()<=p && lf.getOrders()>old_position){
				lf.setOrders(lf.getOrders() - 1);
				lf.update();
			}
		}
	}
	
	// 原节点下的孩子节点通过修复repairTree处理
	Leaf rootLeaf = dir.getLeaf(Leaf.ROOTCODE);
	Directory dm = new Directory();
	dm.repairTree(rootLeaf);	
	
	json.put("ret", "1");
	json.put("msg", "操作成功！");
	out.print(json.toString());	
	return;	
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>文件柜-菜单</title>
<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1' />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/frame.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.my.js"></script>
<script src="../js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css" />

<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>

<style>
#directoryTree{
	margin-top:10px;
}
</style>
<script>
function findObj(theObj, theDoc) {
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function ShowChild(imgobj, name) {
	var tableobj = findObj("childof"+name);
	if (tableobj==null) {
		o("ifrmGetChildren").contentWindow.location.href = "dir_ajax_getchildren.jsp?op=simple&parentCode=" + name;
		// document.frames.ifrmGetChildren.location.href = "dir_ajax_getchildren.jsp?op=simple&parentCode=" + name;
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "images/i_minus.gif";
		}
		else
			imgobj.src = "images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}	
}

function insertAdjacentHTML(objId,code,isStart){ 
	var obj = document.getElementById(objId);
	if(isIE()) 
		obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd",code); 
	else{ 
		var range=obj.ownerDocument.createRange(); 
		range.setStartBefore(obj); 
		var fragment = range.createContextualFragment(code); 
		if(isStart) 
			obj.insertBefore(fragment,obj.firstChild); 
		else 
			obj.appendChild(fragment); 
	}
}
</script>
</head>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<div id="directoryTree"></div>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
<%
String dirCode = ParamUtil.get(request, "dir_code");
if (dirCode.equals("")){
	dirCode = Leaf.ROOTCODE;  
}
Leaf leaf = dir.getLeaf(dirCode);
DirView tv = new DirView(request, leaf);

String jsonData = tv.getJsonStringByUser(leaf, new Privilege().getUser(request));
%>
</body>
<script>
function bindClick() {
	$("a").bind("click", function() {
			$("a").css("color", "");
			$(this).css("color", "red");
		});
}

var myjsTree;
$(document).ready(function(){
	myjsTree = $('#directoryTree').jstree({
				    	"core" : {
				            "data" :  <%=jsonData%>,
				            "themes" : {
							   "theme" : "default" ,
							   "dots" : true,  
							   "icons" : true  
							},
							"check_callback" : true,	
				 		},
						"plugins" : ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu" ,"types", "crrm", "state"],
						"contextmenu": {	//绑定右击事件
							"items": {
								"create": {  
									"label": "增加",
									"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_add.png",
									"action": function (data) { 
										inst = $.jstree.reference(data.reference);
										node = inst.get_node(data.reference);
										selectNodeId = node.id;
										selectNodeName = node.text;
										parent.mainFileFrame.location.href = "dir_right.jsp?parent_code=" + selectNodeId + "&op=AddChild";
									}
								},  
								"rename": {  
									"label": "修改",  
									"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
									"action": function (data) { 
										inst = $.jstree.reference(data.reference);
										node = inst.get_node(data.reference);
										selectNodeId = node.id;
										selectNodeName = node.text;
										window.parent.mainFileFrame.location.href = "dir_right.jsp?op=modify&code=" + selectNodeId;
									}  
								},   
								"remove": {  
									"label": "删除",
									"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
									"action": function (data) { 
										inst = $.jstree.reference(data.reference);
										node = inst.get_node(data.reference);
										selectNodeId = node.id;
										selectNodeName = node.text;
										deleteLeaf(selectNodeId,inst,node);
									} 
								}
							}
						}						
					}).bind('click.jstree', function (e, data) {//绑定选中事件
					     //alert(data.node.id);
						var eventNodeName = e.target.nodeName;               
			            if (eventNodeName == 'INS') {                   
			                return;               
			            } else if (eventNodeName == 'A') {           
			               var $subject = $(e.target).parent();                   
		                   var code = $(e.target).parents('li').attr('id');
		                   var url = "fileark_main.jsp?dir_code=" + code;
		                   if (code.indexOf("cws_prj_") == 0) {
		                	   	var project = code.substring(8);
		                		var p = project.indexOf("_");
		                		if (p != -1) {
		                			project = project.substring(0, p);
		                		}
		                		url += "&projectId=" + project + "&parentId=" + project + "&formCode=project";
		                   }
		                   window.open(url,"mainFileFrame");
			            }
   					})
					.bind('move_node.jstree', function (e, data) {//绑定移动节点事件
					    //data.node.id移动节点的id
					    //data.parent移动后父节点的id
					    //data.position移动后所在父节点的位置，第一个位置为0
					    node = data.node;
					    $.ajax({
							type: "post",
							url: "<%=request.getContextPath() %>/fileark/fileark_left.jsp",
							dataType: "json",
							data: {
								op: "move",
								code: data.node.id+"",
								parent_code: data.parent+"",
								position : data.position+"" 
							},
							success: function(data, status) {
								if(data.ret == 0){
									alert(data.msg);
									window.location.reload(true);   
								}
							},
							complete: function(XMLHttpRequest, status){
							},
							error: function(XMLHttpRequest, textStatus){
								alert(XMLHttpRequest.responseText);
								window.location.reload(true); 
							}
						})
					}).bind('ready.jstree',function(){
	   						myjsTree.jstree("deselect_all");
	   					    myjsTree.jstree("select_node", "<%=Leaf.ROOTCODE%>");
   						});
   	bindClick();
});

function deleteLeaf(code){
	if("root" == code){
		setToaster("根节点不能被删除");
		return;
	 }
	 window.parent.mainFileFrame.jConfirm("您确定要删除吗?","提示",function(r){
		if(!r){return;}
		else{
		 $.ajax({
			type: "post",
			url: "<%=request.getContextPath() %>/fileark/fileark_left.jsp",
			dataType: "json",
			data: {
				op: "del",
				root_code: "root",
				delcode:code+""
			},
			beforeSend: function(XMLHttpRequest){
				// parent.parent.showLoading();
			},
			success: function(data, status){
				if (data.ret==1){
					window.parent.mainFileFrame.jAlert("删除成功", "提示");
					var node = myjsTree.jstree("get_node", code);
					myjsTree.jstree('delete_node', node);
					window.parent.mainFileFrame.location.reload();
				} else {
					parent.parent.jAlert(data.msg,"提示");
				}
				positionNode(data.selectCode);
			},
			complete: function(XMLHttpRequest, status){
				// parent.parent.hiddenLoading();
				// shrink();
			},
			error: function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		});	
		}
	 })
}

//定位节点 myId=code
function positionNode(myId) {
	myjsTree.jstree("deselect_all");
       myjsTree.jstree("select_node", myId);
}

function setToaster(mess) {
	$.toaster({priority : 'info', message : mess });   
}
</script>
</html>
