<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="cn.js.fan.web.*"%><%@page import="com.redmoon.oa.*"%><%@page import="com.redmoon.oa.dept.*"%><%@page import="com.redmoon.oa.netdisk.*"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv = request.getParameter("priv");
if (priv==null)
	priv = "read";
/*
if (!privilege.isUserPrivValid(request, priv)) {
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

int id = ParamUtil.getInt(request, "id");
int attId = ParamUtil.getInt(request, "attachId");

Document mmd = new Document();
mmd = mmd.getDocument(id);
if (mmd==null || !mmd.isLoaded()) {
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println("文件不存在！");
	return;
}
Attachment att = mmd.getAttachment(1, attId);

LeafPriv lp = new LeafPriv(att.getDirCode());
if (!lp.canUserSee(privilege.getUser(request))) {
	// 如果没有共享的权限，则检查文件是否为已发布
	String[] depts = StrUtil.split(att.getPublicShareDepts(), ",");
	int len = 0;
	if (depts!=null)
		len = depts.length;
	boolean isValid = false;
	if (len==0)
		isValid = true;
	else {	
		DeptUserDb du = new DeptUserDb();
		for (int i=0; i<len; i++) {
			if (du.isUserOfDept(privilege.getUser(request), depts[i])) {
				isValid = true;
				break;
			}
		}
	}
	if (!isValid) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");

String ext = att.getExt();
if (ext==null)
	ext = "";
if (ext.equals("rm") || ext.equals("rmvb")) {
%><table width="100%" align="center"><tr><td align="center">
<object ID="video1" CLASSID="clsid:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA" HEIGHT="300" WIDTH="400">
<param name="_ExtentX" value="22304">
<param name="_ExtentY" value="14288">
<param name="AUTOSTART" value="-1">
<param name="SHUFFLE" value="0">
<param name="PREFETCH" value="0">
<param name="NOLABELS" value="0">
<param name="SRC" value="<%=Global.getRootPath() + "/" + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName()%>">
<param name="CONTROLS" value="ImageWindow">
<param name="CONSOLE" value="Clip1">
<param name="LOOP" value="0">
<param name="NUMLOOP" value="0">
<param name="CENTER" value="0">
<param name="MAINTAINASPECT" value="0">
<param name="BACKGROUNDCOLOR" value="#000000">
</object>
<object ID="video1" CLASSID="clsid:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA" HEIGHT="60" WIDTH="400">
<param name="controls" value="ControlPanel,StatusBar">
<param name="console" value="Clip1">
<embed type="audio/x-pn-realaudio-plugin" CONSOLE="Clip1" CONTROLS="ControlPanel,StatusBar" HEIGHT="60" WIDTH="400" AUTOSTART="true"/>
</object>
</td></tr></table><%	
} else if (ext.equals("wmv") || ext.equals("asf") || ext.equals("mp3") || ext.equals("mpeg") || ext.equals("wma") || ext.equals("mpg")) {
%><table width="100%" align="center"><tr><td align="center">
<object classid="clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95" id="MediaPlayer1" width="428" height="330">
        <param name="AudioStream" value="-1">
        <param name="AutoSize" value="0">
        <param name="AutoStart" value="-1">
        <param name="AnimationAtStart" value="-1">
        <param name="AllowScan" value="-1">
        <param name="AllowChangeDisplaySize" value="-1">
        <param name="AutoRewind" value="0">
        <param name="Balance" value="0">
        <param name="BaseURL" value>
        <param name="BufferingTime" value="5">
        <param name="CaptioningID" value>
        <param name="ClickToPlay" value="-1">
        <param name="CursorType" value="0">
        <param name="CurrentPosition" value="-1">
        <param name="CurrentMarker" value="0">
        <param name="DefaultFrame" value>
        <param name="DisplayBackColor" value="0">
        <param name="DisplayForeColor" value="16777215">
        <param name="DisplayMode" value="0">
        <param name="DisplaySize" value="2">
        <param name="Enabled" value="-1">
        <param name="EnableContextMenu" value="-1">
        <param name="EnablePositionControls" value="-1">
        <param name="EnableFullScreenControls" value="0">
        <param name="EnableTracker" value="-1">
        <param name="Filename" value="<%=Global.getRootPath() + "/" + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName()%>">
        <param name="InvokeURLs" value="-1">
        <param name="Language" value="-1">
        <param name="Mute" value="0">
        <param name="PlayCount" value="1">
        <param name="PreviewMode" value="0">
        <param name="Rate" value="1">
        <param name="SAMILang" value>
        <param name="SAMIStyle" value>
        <param name="SAMIFileName" value>
        <param name="SelectionStart" value="-1">
        <param name="SelectionEnd" value="-1">
        <param name="SendOpenStateChangeEvents" value="-1">
        <param name="SendWarningEvents" value="-1">
        <param name="SendErrorEvents" value="-1">
        <param name="SendKeyboardEvents" value="0">
        <param name="SendMouseClickEvents" value="0">
        <param name="SendMouseMoveEvents" value="0">
        <param name="SendPlayStateChangeEvents" value="-1">
        <param name="ShowCaptioning" value="0">
        <param name="ShowControls" value="-1">
        <param name="ShowAudioControls" value="-1">
        <param name="ShowDisplay" value="0">
        <param name="ShowGotoBar" value="0">
        <param name="ShowPositionControls" value="-1">
        <param name="ShowStatusBar" value="-1">
        <param name="ShowTracker" value="-1">
        <param name="TransparentAtStart" value="0">
        <param name="VideoBorderWidth" value="0">
        <param name="VideoBorderColor" value="0">
        <param name="VideoBorder3D" value="0">
        <param name="Volume" value="-40">
        <param name="WindowlessVideo" value="0">
      </object>
</td></tr></table><%
} else {	
	// response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
	response.setContentType(MIMEMap.get(att.getExt()));
	response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(att.getName()));
	
	// 以询问下载的方式打开，会覆盖父窗口
	// response.setContentType("application/octet-stream");
	// response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(att.getName()));
	
	BufferedInputStream bis = null;
	BufferedOutputStream bos = null;
	
	try {
		bis = new BufferedInputStream(new FileInputStream(Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName()));
		bos = new BufferedOutputStream(response.getOutputStream());
		
		byte[] buff = new byte[2048];
		int bytesRead;
		
		while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
		}	
	} catch(final IOException e) {
		System.out.println( "IOException: " + e );
	} finally {
		if (bis != null)
			bis.close();
		if (bos != null)
			bos.close();
	}
}
%>