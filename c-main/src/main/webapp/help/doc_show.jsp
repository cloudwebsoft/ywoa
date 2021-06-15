<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.help.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="java.io.*"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

int id = 0;
String dirCode = ParamUtil.get(request, "dir_code");
boolean isDirArticle = false;
Leaf lf = new Leaf();

Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
UserMgr um = new UserMgr();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			// id = lf.getDocID();
			doc = docmgr.getDocumentByCode(request, dirCode, privilege);
			id = doc.getID();
			isDirArticle = true;
		}
	}
}

if (id==0) {
	try {
		id = ParamUtil.getInt(request, "id");
		doc = docmgr.getDocument(id);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

if (!doc.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该文章不存在！"));
	return;
}
if (!isDirArticle)
	lf = lf.getLeaf(doc.getDirCode());

String CPages = ParamUtil.get(request, "CPages");
int pageNum = 1;
if (StrUtil.isNumeric(CPages))
	pageNum = Integer.parseInt(CPages);

String op = ParamUtil.get(request, "op");
String view = ParamUtil.get(request, "view");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=Global.AppName%> - <%=doc.getTitle()%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.docImg {max-width:500px;_width:500px;}
</style>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
<script src="inc/livevalidation_standalone.js"></script>
<script>
var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "关闭菜单";		
	}
}
</script>
</head>
<body>
<%
	if (!privilege.isUserPrivValid(request, "read")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

    LeafPriv lp = new LeafPriv();
	lp.setDirCode(doc.getDirCode());
    if (!lp.canUserSee(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	else {
		if (doc!=null && pageNum==1) {
			// 使点击量增1
			doc.increaseHit();
		}
%>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td width="74%" class="tdStyle_1">
	<%
	String pageUrl = "document_list_m.jsp?";
	%>
	<a href="<%=pageUrl%>&dir_code=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a>
      <script>
		if (typeof(window.parent.leftFileFrame)=="object"){
			var btnN = "关闭菜单";
			if (window.parent.getCols()!="200,*"){
				btnN = "打开菜单";
				isLeftMenuShow=false;
			}
			document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">");
			document.write(btnN);
			document.write("</span></a>");
		}
		</script></td>
    <td width="26%" align="right" class="tdStyle_1"><%
	if (lp.canUserModify(privilege.getUser(request))) {
		UserSetupDb usd = new UserSetupDb();
		usd = usd.getUserSetupDb(privilege.getUser(request));
		pageUrl = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";				
	%>
      <a href="<%=pageUrl%>?op=edit&id=<%=doc.getID()%>&dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>&dir_name=<%=StrUtil.UrlEncode(lf.getName())%>">编辑</a>&nbsp;&nbsp;
	<%
	}
	%>
	<%if (lf.isLog()) {%>
        &nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=doc.getTitle()%>', '<%=request.getContextPath()%>/fileark/doc_log.jsp?id=<%=doc.getId()%>&title=<%=StrUtil.UrlEncode(doc.getTitle())%>')">日志</a>
    <%}%>
    &nbsp;&nbsp;
    </td>
  </tr>
</table>
<table cellSpacing="0" class="percent98" cellPadding="5" width="100%" align="center" border="0">
  <tbody>
    <tr>
      <td height="39" align="center"><%if (doc.isLoaded()) {%>
          <b><font size="3"> <%=doc.getTitle()%></font></b>&nbsp; </td>
    </tr>
  </tbody>
</table>
	<table width="100%" align="center" class="percent98">
    <tr>
      <td height="22" align="right" bgcolor="#e4e4e4">
	  <%
	  if (!doc.getKind().equals("")) {
		SelectOptionDb sod = new SelectOptionDb();
		%>
		类别：<%=sod.getOptionName("fileark_kind", doc.getKind())%>&nbsp;&nbsp;
		<%
	  }
	  %>
	  <%if (!doc.getAuthor().equals("")){%>
        作者：<%=doc.getAuthor()%>&nbsp;
        <%}%>
        &nbsp;&nbsp;日期：<%=DateUtil.format(doc.getModifiedDate(), "yyyy-MM-dd HH:mm")%>&nbsp;&nbsp;访问次数：<%=doc.getHit()%>
        <%}else{%>
        未找到该文章！
        <%}%>     
      &nbsp;&nbsp;&nbsp;&nbsp;
      </td>
    </tr>
</table>
<%
                java.util.Vector attachments = doc.getAttachments(pageNum);
                java.util.Iterator ir = attachments.iterator();
                String str = "";
				int m=0;
                while (ir.hasNext()) {
                    Attachment am = (Attachment)
                                    ir.next();
                    // 根据其diskName取出ext
                    String ext = StrUtil.getFileExt(am.getDiskName());
                    String link = am.getVisualPath() + "/" + am.getDiskName();
                    if (ext.equals("mp3") || ext.equals("wma")) {
                        // 使用realplay会导致IE崩溃
                        // str += "<div><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=80><PARAM NAME=SRC VALUE='" + link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=true></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" + link + "'><PARAM NAME=AUTOSTART VALUE=-1><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></div>";
                        if (m==0) {
                            str += "<table align=center width=500><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=70><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></td></tr></table><BR>";
                        } else {
                            str += "<table align=center width=500><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=70><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><param name='AutoStart' value=0><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></td></tr></table><BR>";
                        }
                    }else if (ext.equals("wmv") || ext.equals("mpg") || ext.equals("avi")) {
                        // 使用realplay会导致IE崩溃
                        // str += "<div><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=80><PARAM NAME=SRC VALUE='" + link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=true></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" + link + "'><PARAM NAME=AUTOSTART VALUE=-1><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></div>";
                        if (m==0) {
                            str += "<table align=center width=500><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=400><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></td></tr></table><BR>";
                        } else {
                            str += "<table align=center width=500><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=400><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><param name='AutoStart' value=0><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></td></tr></table><BR>";
                        }
                    } else if (ext.equals("rm") || ext.equals("rmvb")) {
                        if (m==0)
                            str += "<table align=center width=500><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=380><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=true></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=AUTOSTART VALUE=-1><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></td></tr></table><BR>";
                        else
                            str += "<table align=center width=500><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=380><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=false></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=AUTOSTART VALUE=0><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></td></tr></table><BR>";
                    }
					m++;
                }
				out.print(str);
			  %>
<%if (doc.isLoaded()) {%>
<table width="98%" align="center" class="percent98">
<tr><td style="line-height:150%">
<%=doc.getContent(pageNum)%>
<%
if (lf.isOfficeNTKOShow()) {
  if (doc!=null) {
	  ir = attachments.iterator();
	  while (ir.hasNext()) {
		Attachment am = (Attachment) ir.next();
		// System.out.println(getClass() + " am.getExt()=" + am.getExt());
		if (am.getExt().equals("doc") || am.getExt().equals("docx") || am.getExt().equals("xls") || am.getExt().equals("xlsx")) {
%>
            <object id="TANGER_OCX" classid="clsid:C9BC4DFF-4248-4a3c-8A49-63A7D317F404" codebase="../activex/OfficeControl.cab#version=5,0,2,1" width="100%" height="100%" >
            <param name="CustomMenuCaption" value="操作">
            <param name="Caption" value="Office - 编辑">
            <param name="MakerCaption" value="cloudweb">
            <param name="MakerKey" value="0727BEFE0CCD576DFA15807DA058F1AC691E1904">
			<%
            if (com.redmoon.oa.kernel.License.getInstance().isOem()) {%>
            <param name="ProductCaption" value="<%=License.getInstance().getCompany()%>">
            <param name="ProductKey" value="<%=License.getInstance().getOfficeControlKey()%>">
            <%}else{ %>
            <param name="ProductCaption" value="YIMIOA">
            <param name="ProductKey" value="D026585BDAFC28B18C8E01C0FC4C0AA29B6226B5">
            <%} %>
            <SPAN STYLE="color:red">该网页需要控件浏览.浏览器无法装载所需要的文档控件.请检查浏览器选项中的安全设置.</SPAN>
            </object>
            <script>
			$(document).ready(function () {
				// 获取文档控件对象
				TANGER_OCX = document.getElementById('TANGER_OCX');
				TANGER_OCX.IsUseUTF8Data = true;
				
				TANGER_OCX.OpenFromURL("fileark/getfile.jsp?docId=<%=doc.getId()%>&attachId=<%=am.getId()%>", true);
				
				TANGER_OCX.height = document.body.clientHeight;
			});
			</script>
<%		
			// 只显示第一个Office附件
			break;
		}
		else if (am.getExt().equals("pdf")) {
		%>
            <DIV id="IfNoAcrobat" style="text-align:right">  
            如果不能正常浏览文件，请先下载Adobe Reader.  
            </DIV>  
			<object classid="clsid:CA8A9780-280D-11CF-A24D-444553540000" width="100%" height="768" border="0">  
			<param name="_Version" value="65539"> 
			<param name="_ExtentX" value="20108"> 
			<param name="_ExtentY" value="10866"> 
			<param name="_StockProps" value="0"> 
			<param name="SRC" value="<%=request.getContextPath()%>/<%=am.getVisualPath() + "/" + am.getDiskName()%>">
			</object> 		
            
		<%
			// 只显示第一个pdf附件
			break;
		}
	  }
  }
}
%>
</td></tr></table>
<%}%>
<br>
<br>
<%
			  if (doc!=null) {
				  ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next();
					
					// Document mmd = new Document();
					// mmd = mmd.getDocument(doc.getID());
					// Attachment att = mmd.getAttachment(pageNum, am.getId());
					Attachment att = doc.getAttachment(pageNum, am.getId());
					String s = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
					String htmlfile=s.substring(0,s.lastIndexOf("."))+".html";
					File fileExist=new File(htmlfile);
					boolean resultValue=false;
					if(fileExist.exists()){
						resultValue=true;
				  }
%>
<table width="98%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td align="left">
    <%
	boolean isOffice = am.getExt().equals("doc") || am.getExt().equals("docx") || am.getExt().equals("xls") || am.getExt().equals("xlsx");
	if (!am.isEmbedded() && StrUtil.isImage(StrUtil.getFileExt(am.getDiskName()))) {
		%>
		<div style="margin:10px"><img class="docImg" src="<%=request.getContextPath() + "/" + att.getVisualPath() + "/" + att.getDiskName()%>" /></div>
		<%
	}
	%>
	&nbsp;&nbsp;<img src="../images/attach.gif">&nbsp;&nbsp;&nbsp;&nbsp;<a target=_blank href="doc_getfile.jsp?pageNum=<%=pageNum%>&id=<%=doc.getID()%>&attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;&nbsp;&nbsp;下载次数&nbsp;&nbsp;<%=am.getDownloadCount()%>
	&nbsp;&nbsp;<a target=_blank id="previewId" style="display: <%=resultValue==true?"inline":"none"%>;" href="doc_show_preview.jsp?pageNum=<%=pageNum%>&id=<%=doc.getID()%>&attachId=<%=am.getId()%>">预览</a>
    </td>
  </tr>
</table>
<%}
}%>
<br>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="24" align="center">文章共<%=doc.getPageCount()%>页&nbsp;&nbsp;页码
      <%
	  int pagesize = 1;
	  int total = DocContent.getContentCount(doc.getID());
	  int curpage,totalpages;
	  Paginator paginator = new Paginator(request, total, pagesize);
	  // 设置当前页数和总页数
	  totalpages = paginator.getTotalPages();
	  curpage	= paginator.getCurrentPage();
	  if (totalpages==0)
	  {
		  curpage = 1;
		  totalpages = 1;
	  }
	  
	  String querystr = "op=edit&id=" + id;
	  out.print(paginator.getCurPageBlock("doc_show.jsp?"+querystr));
	  %></td>
  </tr>
</table>
<%}%>
</body>
</html>
