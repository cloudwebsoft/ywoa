<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    int docId = ParamUtil.getInt(request, "docId");

    Document doc = new Document();
    doc = doc.getDocument(docId);
// 检查目录查看权限
    LeafPriv lp = new LeafPriv(doc.getDirCode());
    if (!lp.canUserSee(privilege.getUser(request))) {
        out.print(SkinUtil.makeErrMsg(request, Privilege.MSG_INVALID));
        return;
    }

    DocPriv dp = new DocPriv();
    if (!dp.canUserOfficeSee(request, docId)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    int attachId = ParamUtil.getInt(request, "attachId");
    int pageNum = ParamUtil.getInt(request, "pageNum", 1);
    Attachment att = doc.getAttachment(pageNum, attachId);
    att.setDownloadCount(att.getDownloadCount() + 1);
    att.save();

    Leaf lf = new Leaf(doc.getDirCode());
    DocLogDb dld = new DocLogDb();
    DocAttachmentLogDb dad = new DocAttachmentLogDb();
    if (lf.isLog()) {
        String uName = privilege.getUser(request);
        String ip = com.cloudwebsoft.framework.util.IPUtil.getRemoteAddr(request);
        dad.setUserName(uName);
        dad.setAtt_id(attachId);
        dad.setDoc_id(docId);
        dad.setIp(ip);
        dad.setLogDate(new java.util.Date());
        dad.create();

        dld.setUserName(uName);
        dld.setDoc_id(docId);
        dld.setIp(ip);
        dld.setLogDate(new java.util.Date());
        dld.create();
    }

    UserDb user = new UserDb();
    user = user.getUserDb(privilege.getUser(request));
    String userRealName = user.getRealName();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Office - 在线查看</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script>
        var TANGER_OCX;

        function init() {
            // 获取文档控件对象
            TANGER_OCX = document.getElementById('TANGER_OCX');
            TANGER_OCX.IsUseUTF8Data = true;
            // 创建新文档
            // TANGER_OCX.CreateNew("Word.Document");

            var url = "<%=Global.getFullRootPath(request)%>/fileark/getFile.do?docId=<%=docId%>&attachId=<%=attachId%>";

            if (<%=att.getExt().equals("xls") || att.getExt().equals("xlsx")%>) {
                TANGER_OCX.OpenFromURL(url, false, "excel.sheet");
            } else if (<%=att.getExt().equals("doc") || att.getExt().equals("docx")%>) {
                TANGER_OCX.OpenFromURL(url, false, "Word.Document");
            } else if (<%=att.getExt().equals("wps")%>) {
                TANGER_OCX.OpenFromURL(url, false, "WPS.Document");
            } else {
                TANGER_OCX.OpenFromURL(url);
            }

            TANGER_OCX.height = document.body.clientHeight;

            TANGER_OCX.ActiveDocument.Saved = true;
            // 禁用右键菜单
            TANGER_OCX.ActiveDocument.CommandBars("Text").Enabled = false;
            TANGER_OCX.Menubar = true;
            TANGER_OCX.IsNoCopy = true;
            TANGER_OCX.SetReadOnly(true);

            TANGER_OCX.ActiveDocument.ActiveWindow.DocumentMap = false;//隐藏导航窗格
            // TANGER_OCX.ActiveDocument.ActiveWindow.View.Type=6;  // web视图
            TANGER_OCX.ActiveDocument.ActiveWindow.ActivePane.DisplayRulers = false;
            // 隐藏页面空白
            // TANGER_OCX.ActiveDocument.ActiveWindow.View.DisplayPageBoundaries=false;
        }

        function AddMyMenuItems() {
            try {
                //在自定义主菜单中增加菜单项目
                // TANGER_OCX.AddCustomMenuItem('打印文件',false,false,1);
                // TANGER_OCX.AddCustomMenuItem('');
            } catch (err) {
                alert("不能创建新对象：" + err.number + ":" + err.description);
            }
        }
    </script>
</HEAD>
<BODY onLoad="init();AddMyMenuItems();">
<object id="TANGER_OCX" classid="clsid:C9BC4DFF-4248-4a3c-8A49-63A7D317F404" codebase="../activex/OfficeControl.cab#version=5,0,2,1" width="100%" height="100%">
    <param name="CustomMenuCaption" value="操作">
    <param name="Caption" value="Office - 查看">
    <param name="MakerCaption" value="cloudweb">
    <param name="MakerKey" value="0727BEFE0CCD576DFA15807DA058F1AC691E1904">
    <%
        if (com.redmoon.oa.kernel.License.getInstance().isOem()) {%>
    <param name="ProductCaption" value="<%=License.getInstance().getCompany()%>">
    <param name="ProductKey" value="<%=License.getInstance().getOfficeControlKey()%>">
    <%} else { %>
    <param name="ProductCaption" value="YIMIOA">
    <param name="ProductKey" value="D026585BDAFC28B18C8E01C0FC4C0AA29B6226B5">
    <%} %>
    <param name="ToolBars" value="0">
    <param name="Menubar" value="0">
    <param name="FileNew" value="0">
    <param name="FileOpen" value="0">
    <param name="FileSave" value="0">
    <param name="FileSaveAs" value="0">
    <param name="FilePageSetup" value="0">
    <%if (!dp.canUserOfficePrint(request, docId)) {%>
    <param name="FilePrint" value="0">
    <param name="FilePrintPreview" value="0">
    <%}%>
    <SPAN STYLE="color:red">该网页需要控件浏览.浏览器无法装载所需要的文档控件.请检查浏览器选项中的安全设置.</SPAN>
</object>
<form id="myForm" METHOD="post" NAME="myForm">
</FORM>
</BODY>
<script language="JScript" for="TANGER_OCX" event="OnCustomMenuCmd(menuIndex,menuCaption,menuID)">
    switch (menuID) {
        case 1:
            TANGER_OCX.ActiveDocument.PrintOut()
            break;
    }
</script>
</html>
