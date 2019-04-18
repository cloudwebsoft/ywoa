<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<style>
.skin0 {
padding-top:2px;
cursor:default;
font:menutext;
position:absolute;
text-align:left;
font-family: "宋体";
font-size: 9pt;
width:80px;              /*宽度，可以根据实际的菜单项目名称的长度进行适当地调整*/
background-color:menu;    /*菜单的背景颜色方案，这里选择了系统默认的菜单颜色*/
border:1 solid buttonface;
visibility:hidden;        /*初始时，设置为不可见*/
border:2 outset buttonhighlight;
}

/*定义菜单条的显示样式*/
.menuitems {
padding:2px 1px 2px 10px;
}
.STYLE1 {color: #FFFFFF}

td {
font-family:宋体, Arial, Tahoma;
}
input {
font-size:12px;
}
</style>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.netdisk.Directory"/>
<%
String dir_code = ParamUtil.get(request, "dir_code");
String dir_name = "";
 
int id = 0;

Privilege privilege = new Privilege();
String userName = privilege.getUser(request);

String correct_result = "操作成功！";

Document doc = new Document();

Document template = null;

Leaf leaf = dir.getLeaf(dir_code);
if (leaf==null || !leaf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该目录已不存在！"));
	return;
}

dir_name = leaf.getName();

LeafPriv lp = new LeafPriv(dir_code);
if (!lp.canUserSee(privilege.getUser(request))) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 取得上级目录
String filePath = leaf.getFilePath();
String op = ParamUtil.get(request, "op");
String mode = ParamUtil.get(request, "mode"); // select
String work = ParamUtil.get(request, "work"); // init modify

if (op.equals("editarticle")) {
	op = "edit";
	try {
		doc = docmanager.getDocumentByCode(request, dir_code, privilege);
		dir_code = doc.getDirCode();
		
	} catch (ErrMsgException e) {
		out.print(strutil.makeErrMsg(e.getMessage(), "red", "green"));
		return;
	}
}

String action = ParamUtil.get(request, "action");
if (action.equals("changeName")) {
	boolean re = false;
	try {
		re = docmanager.updateAttachmentName(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

if (doc!=null) {
	id = doc.getID();
	Leaf lfn = new Leaf();
	lfn = lfn.getLeaf(doc.getDirCode());
	dir_name = lfn.getName();
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");
%>
<title><%=doc!=null?doc.getTitle():""%></title>
<script src="../inc/common.js"></script>
<script src="../inc/map.js"></script>
<script src="../js/jquery.js"></script>
<script src="../inc/upload_ajax.js"></script>
<script language=JavaScript src='formpost.js'></script>
<script language="JavaScript">
<!--
function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
	// alert(index + "-" + fileName + "-" + filePath + "-" + fileSize + "-" + modifyDate);
	// o("attFiles").innerHTML += "<div id='attTmp' name='attTmp' attId='" + index + "' >" + index + "-" + fileName + "<a id='attA' name='attA' href='javascript:;' attId='" + index + "' onclick='removeFile(this.attId);'>删除</a></div>";
}

function removeFile(index) {
	addform.webedit.RemoveFile(index);
	var attTmps = document.getElementsByName("attTmp");
	var attAs = document.getElementsByName("attA");
	var obj = null;
	for (var i=0; i<attTmps.length; i++) {
		if (attTmps[i].attId==index) {
			obj = attTmps[i];
			continue;
		}
		if (attTmps[i].attId>index) {
			attTmps[i].attId -= 1;
			attAs[i].attId -= 1;
		}
	}
	if (obj!=null) {
		obj.outerHTML = "";
	}	
}

function OfficeOperate() {
	alert(redmoonoffice.ReturnMessage.substring(0, 4)); // 防止后面跟乱码
}

// 编辑文件
function editdoc(id, attachId)
{
	<%if (cfg.get("isUseNTKO").equals("true")) {%>
	openWin("netdisk_office_ntko_edit.jsp?id=" + id + "&attachId="+attachId, 1100, 800);	
	<%}else{%>

	if (!checkOfficeEditInstalled())
		return;
	// 下句在关闭窗口时，IE会崩溃
	// openWin("weboffice.jsp?id=" + id + "&attachId=" + attachId);
	/*
	window.open("weboffice.jsp?id=" + id + "&attachId=" + attachId);
	return;
	*/
	rmofficeTable.style.display = "";
	redmoonoffice.AddField("id", id);
	redmoonoffice.AddField("attachId", attachId);
	redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/netdisk/netdisk_office_get.jsp?id=" + id + "&attachId=" + attachId);
	<%}%>
}

<%
if (doc!=null) {
	out.println("var id=" + doc.getID() + ";");
}
%>
var id = "<%=id%>"; // 用于右键菜单
var curAttachId = "";
var curAttachName = "";

var op = "<%=op%>";
var work = "<%=work%>";

function checkOfficeEditInstalled() {
	var bCtlLoaded = false;
	try	{
		if (typeof(redmoonoffice.AddField)=="undefined")
			bCtlLoaded = false;
		if (typeof(redmoonoffice.AddField)=="unknown") {
			bCtlLoaded = true;
		}
	}
	catch (ex) {
	}
	if (!bCtlLoaded) {
		if (confirm("您还没有安装Office在线编辑控件！请点击确定按钮下载安装！")) {
			window.open("<%=request.getContextPath()%>/activex/oa_client.EXE");
		}
	}
	return bCtlLoaded;
}

function window_onload() {
	// checkOfficeEditInstalled();
}

function displayCtlTable(btnObj) {
	if (ctlTable.style.display=="none") {
		ctlTable.style.display = "";
		addform.webedit.height = "75px";
		btnObj.value = "隐 藏";
	}
	else {
		ctlTable.style.display = "none";
	
		//addform.webedit.height = "0px";
			//alert("test")
		btnObj.value = "上 传";
	}
}

function findObj(theObj, theDoc)
{
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
//-->
</script>
</head>
<body onLoad="window_onload()">
<%
if (!privilege.isUserLogin(request))
{
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "uploadDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";	

UserDb ud = new UserDb();
ud = ud.getUserDb(userName);
String strDiskAllow = NumberUtil.round((double)ud.getDiskQuota()/1024000, 1);
String strDiskHas = NumberUtil.round((double)(ud.getDiskQuota()-ud.getDiskSpaceUsed())/1024000, 1);
%>
<table width="100%"  border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td colspan="3" align="center">
				  <table width="100%" cellpadding="0" cellspacing="0">
                    <tr>
                      <td width="25" height="26" align="left" class="tabStyle_1_title"><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
                      <td width="348" height="18" class="tabStyle_1_title" style="cursor:hand" onclick="doSort('name')">&nbsp;<%=dir_name%>&nbsp;&nbsp;
                        <%if (orderBy.equals("name")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
						}%></td>
                    </tr>
                  </table></td>
                  <td width="12%"><table width="100%" cellpadding="0" cellspacing="0" bordercolordark="#ffffff">
                      <tr>
                        <td height="20" class="tabStyle_1_title" style="cursor:hand" onClick="doSort('file_size')">&nbsp;大小&nbsp;&nbsp;<span style="cursor:hand">
                          <%if (orderBy.equals("file_size")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
						}%>
                        </span></td>
                      </tr>
                  </table></td>
                  <td width="18%"><table width="100%" cellpadding="0" cellspacing="0" borderColorDark="#ffffff">
                      <tr>
                        <td class="tabStyle_1_title" height="20" style="cursor:hand" onClick="doSort('uploadDate')">&nbsp;上传时间                    &nbsp;<span style="cursor:hand">
                          <%if (orderBy.equals("uploadDate")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
						}%>
                        </span></td>
                      </tr>
                  </table></td>
                  <td width="25%"><table width="100%" cellpadding="0" cellspacing="0" borderColorDark="#ffffff">
                      <tr>
                        <td height="20" class="tabStyle_1_title">&nbsp;操作</td>
                      </tr>
                  </table></td>
                </tr>
			    <%if (!leaf.getParentCode().equals(Leaf.PARENT_CODE_NONE)) {%>
                  <tr>
                    <td width="3%" align="center">&nbsp;</td>
                    <td width="4%" height="20" align="center"><img src="images/parent.gif" align="absmiddle"></td>
                    <td width="38%" align="left">
					<a href="dir_list.jsp?op=editarticle&dir_code=<%=leaf.getParentCode()%>&mode=<%=mode%>" onMouseUp="curAttachId=''">上级目录</a>					</td>
                    <td width="12%">&nbsp;</td>
                    <td width="18%">&nbsp;</td>
                    <td width="25%">&nbsp;</td>
                  </tr>
                <%}%>
<%
				Iterator irch = leaf.getChildren().iterator();
				while (irch.hasNext()) {
					Leaf clf = (Leaf)irch.next();
				%>
                  <tr>
                    <td width="3%" align="center">&nbsp;</td>
                    <td width="4%" height="20" align="center"><img src="images/folder_01.gif" align="absmiddle"></td>
                    <td width="38%" align="left"><a href="dir_list.jsp?op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>" onMouseUp="curAttachId=''"><%=clf.getName()%></a></td>
                    <td width="12%">&nbsp;</td>
                    <td width="18%">&nbsp;</td>
                    <td width="25%">&nbsp;</td>
                  </tr>
        <%}%>
	  </table>
      <table id="mainTable" width="100%" border="0" cellpadding="0" cellspacing="0">
      <%
			String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
			if (strcurpage.equals(""))
				strcurpage = "1";
			if (!StrUtil.isNumeric(strcurpage)) {
				out.print(StrUtil.makeErrMsg("标识非法！"));
				return;
			}
			
			Attachment am = new Attachment();
			long fileLength = -1;
			int pagesize = 50;
			
			String sql = "SELECT id FROM netdisk_document_attach WHERE doc_id=" + doc.getID() + " and page_num=1 and is_current=1 and is_deleted=0 order by ";
			sql += orderBy + " " + sort;
			
			// out.print(sql);
			
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(SQLFilter.getCountSql(sql));
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				pagesize = rr.getInt(1);
			}
			int curpage = Integer.parseInt(strcurpage);
			ListResult lr = am.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			Paginator paginator = new Paginator(request, total, pagesize);
			// 设置当前页数和总页数
			int totalpages = paginator.getTotalPages();
			if (totalpages==0)
			{
				curpage = 1;
				totalpages = 1;
			}
			  if (doc!=null) {
				  // Vector attachments = doc.getAttachments(1);
				  Vector attachments = lr.getResult();
				  Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	am = (Attachment) ir.next(); 
					fileLength = (long)am.getSize()/1024; 
					if(fileLength == 0 && (long)am.getSize() > 0)
						fileLength = 1;  
					%>
                      <tr>
                        <td width="3%" align="left"><input type="checkbox" id="ids" name="ids" value="<%=am.getId()%>" /></td>
                        <td width="4%" align="center"><a title="打开文件" target=_blank href="netdisk_getfile.jsp?id=<%=doc.getID()%>&attachId=<%=am.getId()%>"><img width="20px" height="20px" src="images/<%=am.getIcon()%>" border="0"></a></td>
                        <td width="38%" align="left">
                        <div style="width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; padding:0px; margin:0px"><span id="span<%=am.getId()%>" name="span<%=am.getId()%>"><a target=_blank title="<%=am.getName()%>" class="mainA" href="netdisk_getfile.jsp?id=<%=doc.getID()%>&attachId=<%=am.getId()%>" onmouseup='onMouseUp("<%=am.getId()%>", "<%=am.getName()%>")'><%=am.getName()%></a></span></div></td>
                        <td width="12%" align="left"><%=fileLength%>KB</td>
                        <td width="18%"><%=DateUtil.format(am.getUploadDate(), "yyyy-MM-dd HH:mm")%></td>
                        <td width="25%" align="left">&nbsp;<a href="dir_change.jsp?attachId=<%=am.getId()%>"><img src="images/rename.gif" alt="重命名或移动文件" width="16" height="16" border="0" align="absmiddle"></a>&nbsp;<a href="javascript:delAttach('<%=am.getId()%>', '<%=doc.getID()%>')"><img src="images/del.gif" alt="删除" width="16" height="16" border="0" align="absmiddle"></a>&nbsp;<a target="_blank" href="netdisk_downloadfile.jsp?id=<%=am.getDocId()%>&attachId=<%=am.getId()%>"><img src="images/download.gif" alt="下载" width="16" height="16" border="0" align="absmiddle"></a>
                          <%if (am.isLinkShared()) {%>
                          <a href="netdisk_public_share.jsp?attachId=<%=am.getId()%>"><img src="images/share_public_yes.gif" alt="已发布" width="16" height="16" border="0" align="absmiddle"></a>
                          <%}else{%>
                          <a href="netdisk_public_share.jsp?attachId=<%=am.getId()%>"><img src="images/share_public.gif" alt="发布" width="16" height="16" border="0" align="absmiddle"></a>
                        <%}%>
                        <%
						if (am.getExt()!=null) {
						if (am.getExt().equals("doc") || am.getExt().equals("xls") || am.getExt().equals("ppt") || am.getExt().equals("docx") || am.getExt().equals("xlsx")) {%>
                        <a href="javascript:editdoc('<%=id%>', '<%=am.getId()%>')" title="编辑Office文件"><img src="images/btn_edit_office.gif" width="16" height="16" border="0" align="absmiddle"></a>
                        <%}
						}%></td>
                      </tr>
	  <%}
			  }
			  %></table>
			    <table width="100%" border="0" cellspacing="0" cellpadding="0" name="ctlTable" id="ctlTable" style="border-top:1px dashed #cccccc; display:">
                  <tr>
                    <td><table  border="0" align="center" cellpadding="0" cellspacing="1">
                      <tr>
                        <td><script>//initUpload()</script>
					    </td>
                      </tr>
                    </table></td>
                  </tr>
                  <tr>
                    <td align="left" height="30">
                <div style="float:left; margin-top:5px">
                &nbsp;&nbsp;空间：<%=strDiskAllow%>M,&nbsp;剩余：<%=strDiskHas%>M
					<%
					Leaf dlf = new Leaf();
					if (doc!=null) {
						dlf = dlf.getLeaf(doc.getDirCode());
					}
					%>
                    &nbsp;&nbsp;<a href="dir_list.jsp?dir_code=<%=dir_code%>&op=editarticle">高级方式</a>
                    &nbsp;&nbsp;<a href="dir_priv_m.jsp?dirCode=<%=dir_code%>">共享管理</a>
                </div>
				<style>
                .OnPic{float:left;overflow:hidden;position:relative;margin-right:9px;*margin-right:3px;cursor:pointer; margin-top:5px}
                .OnPic input{-moz-opacity:0;filter:alpha(opacity=0);opacity:0;position:absolute;left:-2px;top:0;background:none transparent;border:0; width:55px; height:22px;cursor:pointer;}
                
                .progressBar {
                font-size:0px;
                float:left;
                margin:0px;
                margin-top:10px;
                padding:0px;
                background-color:#eeeeee;
                width:180px;
                height:3px;
                display:none;
                }
                .progressBar div {
                margin:0px;
                padding:0px;
                font-size:0px;
                background-color:#00CC66;
                height:3px;
                width:0px;
                }
                .progressStatus {
                margin-top:5px;
                float:left;
                height:20px;
                padding:0px;
                margin-left:5px;
                }
                </style>                
                <div style="float:left; margin-left:15px">
                    <div class="OnPic" style="float:left">
                    <span>
                    <img src="images/pp1.gif" class="small_icon picnormal" />&nbsp;<a href="#">上传文件</a>
                    <%
                      String uploadSerialNo = RandomSecquenceCreator.getId(20);
                    %>
                      <form target="uploadFrm" action="dir_list_do.jsp?op=upload&uploadSerialNo=<%=uploadSerialNo%>" id="formUpload" enctype="multipart/form-data" method="post">
                        <input type="file" name="file" id="file" onChange="submitFile('<%=uploadSerialNo%>')" />
                        <input type="hidden" name="docId" value="<%=doc.getId()%>" />
                        <input type="hidden" name="filepath" value="<%=filePath%>" />
                      </form>
                      <iframe id="uploadFrm" name="uploadFrm" src="" width="0" height="0" frameborder="0" scrolling="no"></iframe>
                      
                    </span>
                    </div>
    
					<div id="progressBar" class="progressBar" style="float:left">
						<div id="uploadStatusProgress_<%=uploadSerialNo%>"></div>
					</div>
      				<div class="progressStatus" style="float:left" id="uploadStatus_<%=uploadSerialNo%>">&nbsp;</div>
                </div>

                </td>
              </tr>
          </table>
   
		  <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td height="30" align="center">
			  <%if (mode.equals("select")) {%>
			  <input class="btn" name="button22" type="button" onclick="sel()" value="选择" />&nbsp;
			  <%}%>
			  <input class="btn" name="button23" type="button" onclick="send()" value="转发" />&nbsp;
			  <input class="btn" name="button24" type="button" onclick="moveBatch()" value="移动" />&nbsp;
			  <input class="btn" name="button25" type="button" onclick="shareBatch()" value="发布" />&nbsp;
              <input class="btn" style="margin-left:3px" name="button2" type="button" onclick="delBatch()" value="删除" />
			  <!--共 <b><%=paginator.getTotal() %></b> 个　每页显示 <b><%=paginator.getPageSize() %></b> 个　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b>-->
                <%
	// String querystr = "op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&dir_code=" + StrUtil.UrlEncode(dir_code);
    // out.print(paginator.getCurPageBlock("?"+querystr));
%>&nbsp;&nbsp;
                <div id="attFiles"></div>
				</td>
            </tr>
          </table>
		  <table id="rmofficeTable" name="rmofficeTable" style="display:none;margin-top:10px" width="29%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#CCCCCC">
            <tr>
              <td height="22" align="center" bgcolor="#eeeeee"><strong>&nbsp;编辑Office文件</strong></td>
            </tr>
            <tr>
              <td align="center"><div style="width:400px;height:43"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="../activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                  <param name="Encode" value="utf-8" />
                  <param name="BackColor" value="0000ff00" />
                  <param name="Server" value="<%=Global.server%>" />
                  <param name="Port" value="<%=Global.port%>" />
                  <!--设置是否自动上传-->
                  <param name="isAutoUpload" value="1" />
                  <!--设置文件大小不超过1M-->
                  <param name="MaxSize" value="<%=Global.FileSize%>" />
                  <!--设置自动上传前出现提示对话框-->
                  <param name="isConfirmUpload" value="1" />
                  <!--设置IE状态栏是否显示信息-->
                  <param name="isShowStatus" value="0" />
                  <param name="PostScript" value="<%=Global.virtualPath%>/netdisk/netdisk_office_upload.jsp" />
				  <%
                  com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
                  %>
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />                  
                </object></div>
                <!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />--></td>
            </tr>
          </table>
		  <table width="100%" border="0">
          <tr>
            <td align="center">
			<form name="form3" action="dir_list_new.jsp" method="post"><input name="newname" type="hidden"></form>
			</td>
          </tr>
      </table>

<div id="ie5menu" class="skin0" onMouseover="highlightie5(event)" onMouseout="lowlightie5(event)" onClick="jumptoie5(event)" style="display:none">
<div class="menuitems" url="javascript:openFile()">打开</div>
<div class="menuitems" url="javascript:changeName()">重命名</div>
<div class="menuitems" url="javascript:move()">移动文件</div>
<div class="menuitems" url="javascript:del()">删除</div>
<div class="menuitems" url="javascript:download()">下载</div>
<div class="menuitems" url="javascript:publicShare()">发布</div>
<hr width=100%>
<div class="menuitems" url="dir_list.jsp?op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>">刷新</div>
</div>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
<tr><td>
<form name=form10 action="?">
<input name="op" type="hidden" value="editarticle">
<input name="action" type="hidden">
<input name="dir_code" type="hidden" value="<%=dir_code%>">
<input name="newname" type="hidden">
<input name="attach_id" type="hidden">
<input name="doc_id" type="hidden" value="<%=id%>">
<input name="page_num" type="hidden" value="1">
<input name="CPages" type="hidden" value="<%=curpage%>">
</form>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="page_num" value="1" type="hidden" />
<input name="ids" type="hidden" />
<input name="doc_id" value="<%=doc.getID()%>" type="hidden" />
<input name="dir_code" value="<%=dir_code%>" type="hidden" />
<input name="docId" type="hidden" value="<%=doc.getID()%>"  />
<input name="uploadMode" type="hidden" value="new" />
<input name="netdiskFiles" type="hidden" />
</form>
</td></tr>
</table>
<iframe id="hideframe" name="hideframe" src="" width=0 height=0 style="display:none"></iframe>
</body>
<script>
function submitFile(serialNo) {
	formUpload.submit();
	formUpload.file.disabled = true;
	o("progressBar").style.display = "block";
	showProgress(serialNo);
}

function changeAttachName(attach_id, doc_id, nm) {
	var obj = findObj(nm);
	// document.frames.hideframe.location.href = "fwebedit_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id + "&newname=" + obj.value
	form3.action = "dir_list_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id;
	form3.newname.value = obj.value;
	form3.submit();
}

function delAttach(attach_id, doc_id) {
	if (!window.confirm("您确定要删除吗？")) {
		return;
	}
	document.frames.hideframe.location.href = "dir_list_do.jsp?op=delAttach&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id
}

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	window.location.href = "?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
<script language="JavaScript1.2">

//set this variable to 1 if you wish the URLs of the highlighted menu to be displayed in the status bar
var display_url=0

var ie5=document.all&&document.getElementById
var ns6=document.getElementById&&!document.all
if (ie5||ns6)
var menuobj=document.getElementById("ie5menu")

function showmenuie5(e){
if (curAttachId=="")
	return;
//Find out how close the mouse is to the corner of the window
var rightedge=ie5? document.body.clientWidth-event.clientX : window.innerWidth-e.clientX
var bottomedge=ie5? document.body.clientHeight-event.clientY : window.innerHeight-e.clientY

//if the horizontal distance isn't enough to accomodate the width of the context menu
if (rightedge<menuobj.offsetWidth)
//move the horizontal position of the menu to the left by it's width
menuobj.style.left=ie5? document.body.scrollLeft+event.clientX-menuobj.offsetWidth : window.pageXOffset+e.clientX-menuobj.offsetWidth
else
//position the horizontal position of the menu where the mouse was clicked
menuobj.style.left=ie5? document.body.scrollLeft+event.clientX : window.pageXOffset+e.clientX

//same concept with the vertical position
if (bottomedge<menuobj.offsetHeight)
menuobj.style.top=ie5? document.body.scrollTop+event.clientY-menuobj.offsetHeight : window.pageYOffset+e.clientY-menuobj.offsetHeight
else
menuobj.style.top=ie5? document.body.scrollTop+event.clientY : window.pageYOffset+e.clientY

menuobj.style.visibility="visible"
return false
}

function hidemenuie5(e){
menuobj.style.visibility="hidden"
}

function highlightie5(e){
var firingobj=ie5? event.srcElement : e.target
if (firingobj.className=="menuitems"||ns6&&firingobj.parentNode.className=="menuitems"){
if (ns6&&firingobj.parentNode.className=="menuitems") firingobj=firingobj.parentNode //up one node
firingobj.style.backgroundColor="highlight"
firingobj.style.color="white"
if (display_url==1)
window.status=event.srcElement.url
}
}

function lowlightie5(e){
var firingobj=ie5? event.srcElement : e.target
if (firingobj.className=="menuitems"||ns6&&firingobj.parentNode.className=="menuitems"){
if (ns6&&firingobj.parentNode.className=="menuitems") firingobj=firingobj.parentNode //up one node
firingobj.style.backgroundColor=""
firingobj.style.color="black"
window.status=''
}
}

function jumptoie5(e){
var firingobj=ie5? event.srcElement : e.target
if (firingobj.className=="menuitems"||ns6&&firingobj.parentNode.className=="menuitems"){
if (ns6&&firingobj.parentNode.className=="menuitems") firingobj=firingobj.parentNode
if (firingobj.getAttribute("target"))
window.open(firingobj.getAttribute("url"),firingobj.getAttribute("target"))
else
window.location=firingobj.getAttribute("url")
}
}

if (ie5||ns6){
menuobj.style.display=''
document.oncontextmenu=showmenuie5
document.onclick=hidemenuie5
}

function onMouseUp(attachId, attachName) {
	// 左键
	curAttachId = attachId;
	curAttachName = attachName;
	if (event.button==1) {
		// alert(id + "_" + attachId);
	}
	// 右键
	if (event.button==2) {
		// alert(id + "_" + attachId);
	}
}

var spanInnerHTML = "";
function changeName() {
	if (curAttachId!="") {
		spanObj = findObj("span" + curAttachId);
		spanInnerHTML = spanObj.innerHTML;
		spanObj.innerHTML = "<input name='newName' class=singleboarder size=22 value='" + curAttachName + "' onblur=\"doChange('" + curAttachId + "',this.value,'" + curAttachName + "'," + spanObj.name + ")\" onKeyDown=\"if (event.keyCode==13) this.blur()\">";
		addform.newName.focus();
		addform.newName.select();
	}
}

function doChange(attachId, newName, oldName, spanObj) {
	if (newName!=oldName) {
		form10.action.value = "changeName";
		form10.attach_id.value = attachId;
		form10.newname.value = newName;
		form10.submit();
	}
	else {
		spanObj.innerHTML = spanInnerHTML;
	}
	curAttachId = "";
}

function move() {
	window.location.href = "dir_change.jsp?attachId=" + curAttachId;
}

function del() {
	delAttach(curAttachId, id);
}

function openFile() {
	window.open("netdisk_getfile.jsp?id=" + id + "&attachId=" + curAttachId);
}

function download() {
	window.open("netdisk_downloadfile.jsp?id=" + id + "&attachId=" + curAttachId);
}

function publicShare() {
	window.location.href = "netdisk_public_share.jsp?attachId=" + curAttachId;
}

function selAllCheckBox(checkboxname){
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = true;
  }
  for (i=0; i<checkboxboxs.length; i++)
  {
	  checkboxboxs[i].checked = true;
  }
  }
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function delBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	if (!confirm("您确定要删除么？"))
		return;
	// document.frames.hideframe.location.href = "dir_list_do.jsp?op=delAttachBatch&page_num=1&doc_id=<%=doc.getID()%>&ids=" + ids	
	hidForm.op.value = "delAttachBatch";
	hidForm.action = "dir_list_do.jsp";
	hidForm.page_num.value = "1";
	hidForm.ids.value = ids;
	hidForm.submit();
}

function moveBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	// window.location.href = "dir_change_batch.jsp?page_num=1&docId=<%=doc.getID()%>&ids=" + ids	
	hidForm.action = "dir_change_batch.jsp";
	hidForm.page_num.value = "1";
	hidForm.ids.value = ids;
	hidForm.submit();
}

function shareBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	// window.location.href = "netdisk_public_share_batch.jsp?page_num=1&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids	
	hidForm.action = "netdisk_public_share_batch.jsp";
	hidForm.page_num.value = "1";
	hidForm.ids.value = ids;
	hidForm.submit();
	
}

function sel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	window.top.opener.setNetdiskFiles(ids);
	window.top.close();
}
function send() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	// window.location.href = "../message_oa/message_ext/send.jsp?netdiskFiles=" + ids;
	hidForm.action = "../message_oa/message_ext/send.jsp";
	hidForm.netdiskFiles.value = ids;
	hidForm.submit();
}

function doUploadClear(response) {
	// alert(response.responseText);
	window.location.reload();
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>