<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.OnlineInfo"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<LINK href="../common.css" type=text/css rel=stylesheet>
<LINK href="admin/default.css" type=text/css rel=stylesheet>
<link href="<%=skinPath%>/skin.css" rel="stylesheet" type="text/css">
<script src="../inc/calendar.js"></script>
<script src="../inc/common.js"></script>
<script language="javascript">
function advanceoptionShow(obj){
	if(advanceoption.style.display == "none"){
		obj.value = "1";
		advanceoption.style.display = ""
	}else{
		obj.value = "0";
		advanceoption.style.display = "none"
	}
}

function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2 =eval("document.all.followImg" + t_id);
	var targetTR2 =eval("document.all.follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="images/minus.gif";
			if (targetImg2.loaded=="no"){
				document.frames["hiddenframe"].location.replace("listtree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="images/plus.gif";
		}
	}
}
</script>
</head>

<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
		if (!privilege.isMasterLogin(request))
		{
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		
		String operation = ParamUtil.get(request, "operation");
		if(!operation.trim().equals("")){
			MsgMgr mm = new MsgMgr();
			
			if (operation.equals("moveBoard")) {
			    String strIds = ParamUtil.get(request, "ids");
				String selToBoard = ParamUtil.get(request, "selToBoard");
				String[] idsary = StrUtil.split(strIds, ",");
				if (idsary!=null) {
					int len = idsary.length;
					for (int i=0; i<len; i++) {
						try {
							mm.ChangeBoard(request, Long.parseLong(idsary[i]), selToBoard);
						}
						catch (ErrMsgException e) {
							out.print(StrUtil.Alert(e.getMessage()));
							return;
						}
					}
				}
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "batch_topic_m.jsp"));
			}
			
			if (operation.equals("moveThreadType")) {
			    String strIds = ParamUtil.get(request, "ids");
				String toTxtBoardCode = ParamUtil.get(request, "toBoardCode");
				int toThreadType = ParamUtil.getInt(request, "toThreadType");
				String[] idsary = StrUtil.split(strIds, ",");
				String name = privilege.getUser(request);
				MsgDb md = new MsgDb();
				
				if (idsary!=null) {
					int len = idsary.length;
					for (int i=0; i<len; i++) {
						try {
							md.ChangeBoardThreadType(request, Long.parseLong(idsary[i]), toTxtBoardCode, toThreadType);
						}
						catch (ErrMsgException e) {
							out.print(StrUtil.Alert(e.getMessage()));
							return;
						}
					}
				}
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "batch_topic_m.jsp"));
			}
			
			if (operation.equals("delTopic")) {
				String strIds = ParamUtil.get(request, "ids");
				String[] idsary = StrUtil.split(strIds, ",");
				if (idsary!=null) {
					int len = idsary.length;
					for (int i=0; i<len; i++) {
						mm.delTopicAbsolutely(application, request, Long.parseLong(idsary[i]));
					}
				}
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "batch_topic_m.jsp"));
			}	
			
			if (operation.equals("msgLevel")) {
				String strIds = ParamUtil.get(request, "ids");
				String[] idsary = StrUtil.split(strIds, ",");
				String msgLevel = ParamUtil.get(request, "msgLevel");
				int value = -1;
				if(msgLevel.equals("levelTopBoard")){
					value = MsgDb.LEVEL_TOP_BOARD;
				}else{
					value = MsgDb.LEVEL_TOP_FORUM;
				}
				if (idsary!=null) {
					int len = idsary.length;
					for (int i=0; i<len; i++) {
						try {			    
							mm.setOnTop(request, Long.parseLong(idsary[i]), value);
						}
						catch (ErrMsgException e) {
							out.print(StrUtil.Alert(e.getMessage()));
							return;
						}
					}
				}
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "batch_topic_m.jsp"));
			}	
			
			if (operation.equals("elite")) {
				String strIds = ParamUtil.get(request, "ids");
				String[] idsary = StrUtil.split(strIds, ",");
				if (idsary!=null) {
					int len = idsary.length;
					for (int i=0; i<len; i++) {
						try {
							mm.setElite(request, Long.parseLong(idsary[i]), 1);
						}
						catch (ErrMsgException e) {
							out.print(StrUtil.Alert(e.getMessage()));
							return;
						}
					}
				}
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "batch_topic_m.jsp"));
			}	
			
			if (operation.equals("delAttach")) {
				String strIds = ParamUtil.get(request, "ids");
				String[] idsary = StrUtil.split(strIds, ",");
				if (idsary!=null) {
					int len = idsary.length;
					for (int i=0; i<len; i++) {
						MsgDb md = new MsgDb(Integer.parseInt(idsary[i]));
						Attachment am = new Attachment();
						Vector vt_attach = md.getAttachments();
						Iterator ir_attach = vt_attach.iterator();
						while (ir_attach.hasNext()) {
							am = (Attachment)ir_attach.next();
							am.del();
						}
					}
				}
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "batch_topic_m.jsp"));
			}	
			
		}
			
		String selboard = ParamUtil.get(request, "selboard");
		if (selboard.equals(""))
			selboard = "allboard";
			
		String title = "", nick = "", sBeginDate = "", sEndDate = "";
		String threadType = "", hitLess = "", hitMore = "", recountLess = "", recountMore = "", sReDate = "", msgLevel = "", isElite = "", attach = "", selBoardCode = "";    	
			
		//搜索主要选项
		title = ParamUtil.get(request, "title");
		nick = ParamUtil.get(request, "nick");
		sBeginDate = ParamUtil.get(request, "beginDate");
		sEndDate = ParamUtil.get(request, "endDate");
		
		//搜索更多选项
		threadType = ParamUtil.get(request, "threadType"); 
		hitLess = ParamUtil.get(request, "hitLess");   
		hitMore = ParamUtil.get(request, "hitMore");  
		recountLess = ParamUtil.get(request, "recountLess");  
		recountMore = ParamUtil.get(request, "recountMore");     
		sReDate = ParamUtil.get(request, "reDate");  
		msgLevel = ParamUtil.get(request, "msgLevel");
		isElite = ParamUtil.get(request, "isElite");
		attach = ParamUtil.get(request, "attach");
		selBoardCode = ParamUtil.get(request, "hBoardCode");
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">批量主体管理</td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
 <form name="form1" method="post" action="?op=search">
  <tr> 
    <td height=20 align="left" class="thead">搜索符合条件主题</td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999" class="tableframe_gray">  
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;所在论坛:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;
          <select name="selboard">
			<option value="allboard" selected>
				<lt:Label res="res.label.forum.search" key="all_board"/>
			</option>
			<%
				LeafChildrenCacheMgr dlcm = new LeafChildrenCacheMgr("root");
				java.util.Vector vt = dlcm.getChildren();
				Iterator ir = vt.iterator();
				while (ir.hasNext()) {
					Leaf leaf = (Leaf) ir.next();
					String parentCode = leaf.getCode();
			%>
            <optgroup style="BACKGROUND-COLOR: #f8f8f8" label="╋ <%=leaf.getName()%>">
            <%
					LeafChildrenCacheMgr dl = new LeafChildrenCacheMgr(parentCode);
					java.util.Vector v = dl.getChildren();
					Iterator ir1 = v.iterator();
					while (ir1.hasNext()) {
						Leaf lf = (Leaf) ir1.next();
			%>
             <option value="<%=lf.getCode()%>">　├『<%=lf.getName()%>』</option>
			<%
					}
			%>
			</optgroup>
			<%
				}
			%>
        </select>
        <script language=javascript>
		<!--
		var v = "<%=selboard%>";
		if (v!="")
			form1.selboard.value = v;
		//-->
		</script>		</td>
	  </tr>  
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;主题作者(多用户名中间请用半角逗号 &quot;,&quot; 分割):</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="nick" value="<%=nick%>" /></td>
	  </tr> 
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;标题关键字:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="title" value="<%=title%>"/></td>
	  </tr>   
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;发表时间范围(格式 yyyy-mm-dd，不限制为空):</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;从<input name="beginDate" value="<%=sBeginDate%>" onclick="showcalendar(event, this)" readonly>到
            <input name="endDate" value="<%=sEndDate%>" onclick="showcalendar(event, this)" readonly></td>
	  </tr>  
	  <tr>
	    <td height="26" bgcolor="#FFFBFF">&nbsp;</td>
	    <td height="26" bgcolor="#FFFBFF" align="right">
	      <input type="checkbox" name="checkAdvanceOption" value="0" onclick="advanceoptionShow(this)"/>
	      更多选项&nbsp;&nbsp;</td>
	    </tr>
	  <tbody id="advanceoption" style="display:none">
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;所在分类:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;
          <select name="threadType" onChange="threadTypeChange()">
		  	<option value=""></option>
			<%
			    ThreadTypeDb ttdb = new ThreadTypeDb();
				String boardCode = "", boardName = "";
				vt = ttdb.list();
				ir = vt.iterator();
				int i = 0;
				int len = vt.size();
				String threadTypeArr[][] = new String[len][3]; 
				while (ir.hasNext()) {
					ttdb = (ThreadTypeDb) ir.next();
					boardCode = ttdb.getBoardCode();
					Leaf lf = new Leaf(boardCode);
					threadTypeArr[i][0] = Integer.toString(ttdb.getId());
                    threadTypeArr[i][1] = lf.getName();
					threadTypeArr[i][2] = boardCode;
					
			%>
			<option value="<%=ttdb.getId()%>"><%=ttdb.getName()%></option>
			<%
			        i++;
				}
				out.println("<script language='javascript'>");
				out.println("var threadTypeArr = new Array("+len+");");
				for(int j = 0; j < len; j++){
					out.println("var threadType = new Array(3);");
					out.println("threadType[0] = '"+ threadTypeArr[j][0] + "';");
					out.println("threadType[1] = '"+ threadTypeArr[j][1]  + "';");
					out.println("threadType[2] = '"+ threadTypeArr[j][2]  + "';");
					out.println("threadTypeArr[" + j + "] = threadType;");
				}
				out.println("</script>");
			%>
          </select>
          所在论坛:
		  <input type="text" name="txtBoardName" style="border:0px; color:#000000; background:transparent; font:bold 12px; height:15px" readonly/>
		  <input type="hidden" name="hBoardCode"/>
		  <script>
		        form1.threadType.value = "<%=threadType%>"
				if(form1.threadType.value == ""){
					form1.txtBoardName.value = "";
					form1.hBoardCode.value = "";
				}else{
					for(var i = 0; i < threadTypeArr.length; i++){
						if(form1.threadType.value == threadTypeArr[i][0]){
						   form1.txtBoardName.value = threadTypeArr[i][1];   
						   form1.hBoardCode.value = threadTypeArr[i][2];  
						   break;
						}   
					}
				}
		  </script>
		  </td>
	  </tr>  
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;被浏览次数小于:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="hitLess" value="<%=hitLess%>"/></td>
	  </tr>  
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;被浏览次数大于:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="hitMore" value="<%=hitMore%>"/></td>
	  </tr>  
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;被回复次数小于:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="recountLess" value="<%=recountLess%>"/></td>
	  </tr>
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;被回复次数大于:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="recountMore" value="<%=recountMore%>"/></td>
	  </tr>    
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;多少天内无新回复:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="text" name="reDate" value="<%=sReDate%>"/></td>
	  </tr>   
	  <tr>
        <td width="45%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;是否包含置顶帖:</td>
        <td width="55%" height="26" bgcolor="#FFFBFF">
          <input type="radio" name="msgLevel" value="0" checked/>
          无限制 
          <input type="radio" name="msgLevel" value="1" />
          包含且仅包含
          <input type="radio" name="msgLevel" value="2" />
          不包含        </td>
	  </tr> 
	  <tr>
        <td width="45%" height="22" bgcolor="#FFFBFF">&nbsp;&nbsp;是否包含精华帖:</td>
        <td width="55%" height="22" bgcolor="#FFFBFF"><input type="radio" name="isElite" value="0" checked/>
无限制 
  <input type="radio" name="isElite" value="1" />
包含且仅包含
<input type="radio" name="isElite" value="2" />
不包含 </td>
	  </tr>
	  <tr>
        <td width="45%" height="22" bgcolor="#FFFBFF">&nbsp;&nbsp;是否包含附件:</td>
        <td width="55%" height="22" bgcolor="#FFFBFF"><input type="radio" name="attach" value="0" checked/>
无限制 
  <input type="radio" name="attach" value="1" />
包含且仅包含
<input type="radio" name="attach" value="2" />
不包含 </td>
	  </tr>  
	  </tbody>      
    </table>
      <br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td align="center"><input type=submit value="提交"></td>
        </tr>
      </table>
      <br></td>
  </tr>
  </form>
</table>
<br>
<br>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">符合条件的主题数</td>
  </tr>
  <tr> 
    <td valign="top"><br><table width="86%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999" class="tableframe_gray">
      <tr>
        <td height="24" colspan="2" bgcolor="#EFEBDE">&nbsp;&nbsp;符合条件的主题数</td>
      </tr>  
      <tr>
        <td width="36%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="radio" name="operation" value="moveBoard" />批量移动到论坛</td>
        <td width="64%" height="26" bgcolor="#FFFBFF">&nbsp;
			<select name="selToBoard">
			<%
				vt = dlcm.getChildren();
				ir = vt.iterator();
				while (ir.hasNext()) {
					Leaf leaf = (Leaf) ir.next();
					String parentCode = leaf.getCode();
			%>
            <optgroup style="BACKGROUND-COLOR: #f8f8f8" label="╋ <%=leaf.getName()%>">
            <%
					LeafChildrenCacheMgr dl = new LeafChildrenCacheMgr(parentCode);
					java.util.Vector v = dl.getChildren();
					Iterator ir1 = v.iterator();
					while (ir1.hasNext()) {
						Leaf lf = (Leaf) ir1.next();
			%>
             <option value="<%=lf.getCode()%>">　├『<%=lf.getName()%>』</option>
			<%
					}
			%>
            </optgroup>
            <%
				}
			%>
			</select>
		</td>
      </tr>  
	  <tr>
        <td width="36%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="radio" name="operation" value="moveThreadType" />
          批量移动到分类</td>
        <td width="64%" height="26" bgcolor="#FFFBFF">&nbsp;
		<select name="selToThreadType" onChange="toThreadTypeChange()">
			<%
			    ThreadTypeDb tottdb = new ThreadTypeDb();
				String toBoardCode = "", toBoardName = "";
				vt = tottdb.list();
				ir = vt.iterator();
				i = 0;
				len = vt.size();
				String toThreadTypeArr[][] = new String[len][3]; 
				while (ir.hasNext()) {
					tottdb = (ThreadTypeDb) ir.next();
					toBoardCode = tottdb.getBoardCode();
					Leaf lf = new Leaf(toBoardCode);
					toThreadTypeArr[i][0] = Integer.toString(tottdb.getId());
                    toThreadTypeArr[i][1] = lf.getName();
					toThreadTypeArr[i][2] = toBoardCode;
					
			%>
			<option value="<%=tottdb.getId()%>"><%=tottdb.getName()%></option>
			<%
			        i++;
				}
				out.println("<script language='javascript'>");
				out.println("var toThreadTypeArr = new Array("+len+");");
				for(int j = 0; j < len; j++){
					out.println("var toThreadType = new Array(3);");
					out.println("toThreadType[0] = '"+ toThreadTypeArr[j][0] + "';");
					out.println("toThreadType[1] = '"+ toThreadTypeArr[j][1]  + "';");
					out.println("toThreadType[2] = '"+ toThreadTypeArr[j][2]  + "';");
					out.println("toThreadTypeArr[" + j + "] = toThreadType;");
				}
				out.println("</script>");
			%>
          </select>
          所在论坛:
		  <input type="text" name="txtToBoardName" style="border:0px; color:#000000; background:transparent; font:bold 12px; height:15px" readonly/>
		  <input type="hidden" name="hToBoardCode" />
		  <script>
				if(selToThreadType.value == ""){
					txtToBoardName.value = "";
					hToBoardCode.value ="";
				}else{
					for(var i = 0; i < toThreadTypeArr.length; i++){
						if(selToThreadType.value == toThreadTypeArr[i][0]){
						   txtToBoardName.value = toThreadTypeArr[i][1];  
						   hToBoardCode.value = toThreadTypeArr[i][2];
						   break;
						}   
					}
				}
		  </script>
		  </td>
	  </tr>  
	  <tr>
        <td width="36%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="radio" name="operation" value="delTopic" />
          批量删除</td>
        <td width="64%" height="26" bgcolor="#FFFBFF">
          <input type="checkbox" name="checkbox3" value="checkbox" />
          删帖不减用户发帖数和积分        </td>
	  </tr> 
	  <tr>
        <td width="36%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="radio" name="operation" value="msgLevel" />
          批量置顶</td>
        <td width="64%" height="26" bgcolor="#FFFBFF">
          <input type="radio" name="setMsgLevel" value="levelTopBoard" />
          版块置顶
          <input type="radio" name="setMsgLevel" value="levelTopForum" />
          论坛置顶</td>
	  </tr>   
	  <tr>
        <td width="36%" height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="radio" name="operation" value="elite" />
          批量设置精华</td>
        <td width="64%" height="26" bgcolor="#FFFBFF">&nbsp;</td>
	  </tr>  
	  <tr>
	    <td height="26" bgcolor="#FFFBFF">&nbsp;&nbsp;<input type="radio" name="operation" value="delAttach" />
	      删除主题中的附件</td>
	    <td height="26" bgcolor="#FFFBFF" align="right">&nbsp;</td>
	    </tr>        
    </table>
    <br>
   </td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray" id="list">
  <tr> 
    <td height=20 align="left" class="thead">符合条件的主题列表:</td>
  </tr>
  <tr> 
    <td valign="top"><br>
	<%
		String op = ParamUtil.get(request, "op");
		String sql = "", condition = "",querystr = "";	   
		if(op.equals("search")){	
			sql = "select id from sq_thread";
			
			if(!selboard.trim().equals("allboard")){
				condition += "boardcode="  + StrUtil.sqlstr(selboard);
			}
			
			if(!nick.trim().equals("")){
			    if(!condition.equals(""))
					condition += " and ";
				UserDb ud = new UserDb();
				String[] nickAry = StrUtil.split(nick, ",");
				String strNick = "";
				if (nickAry!=null) {
					int length = nickAry.length;
					for (int j=0; j<length; j++) {
						strNick += ud.getNicksLike(nickAry[j]);
						if(j < length - 1)
							strNick += ",";
					}
				}
				
			    condition += "name in (" + strNick + ")";
			}
			
			if(!title.trim().equals("")){
				if(!condition.equals(""))
					condition += " and ";
				condition += "replyid=-1 and title like " + StrUtil.sqlstr("%"+title+"%");
				sql = "select id from sq_message";
			}	 
			
			if(!sBeginDate.trim().equals("") && !sEndDate.trim().equals("")){
			    if(!condition.equals(""))
					condition += " and ";
				java.util.Date beginDate = DateUtil.parse(sBeginDate, "yyyy-MM-dd");
				java.util.Date endDate = DateUtil.parse(sEndDate, "yyyy-MM-dd");
				long lBeginDate = beginDate.getTime();
				long lEndDate = endDate.getTime() + 24*60*60*1000;
				condition += "lydate>=" + lBeginDate + " and lydate<" + lEndDate;
			}else{
				if(!sBeginDate.trim().equals("")){
					if(!condition.equals(""))
						condition += " and ";
					java.util.Date beginDate = DateUtil.parse(sBeginDate, "yyyy-MM-dd");
					long lBeginDate = beginDate.getTime();	
					condition += "lydate>=" + lBeginDate;
				}else{
					if(!sEndDate.trim().equals("")){
						if(!condition.equals(""))
							condition += " and ";
						java.util.Date endDate = DateUtil.parse(sEndDate, "yyyy-MM-dd");
						long lEndDate = endDate.getTime() + 24*60*60*1000;
						condition += "lydate<" + lEndDate;
					}			
				}
			}

			
			if(!threadType.trim().equals("")){ 
				if(!condition.equals(""))
					condition += " and ";
				condition += "boardcode=" + StrUtil.sqlstr(selBoardCode) + " and thread_type in (" + threadType + ")";
			}
			
			if(StrUtil.isNumeric(hitLess)){
				if(!condition.equals(""))
					condition += " and ";
				condition += "hit<" + hitLess;
			}
			
			if(StrUtil.isNumeric(hitMore)){
				if(!condition.equals(""))
					condition += " and ";
				condition += "hit>" + hitMore;
			}
			
			if(StrUtil.isNumeric(recountLess)){
				if(!condition.equals(""))
					condition += " and ";
				condition += "id in (select id from sq_message where replyid = -1 and recount<" + recountLess + ")";
			}

			if(StrUtil.isNumeric(recountMore)){
				if(!condition.equals(""))
					condition += " and ";
				condition += "id in (select id from sq_message where replyid = -1 and recount>" + recountMore + ")";
			}
			
			if(StrUtil.isNumeric(sReDate)){
				if(!condition.equals(""))
					condition += " and ";
			    java.util.Date today = DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
			    long lReDate = today.getTime() - Long.parseLong(sReDate)*24*60*60*1000;
				out.print(today.getTime());
				
				condition += "redate<" + lReDate;	
			}
			
			if(msgLevel.equals("1")){
				if(!condition.equals(""))
					condition += " and ";
				condition += "msg_level <> 0 and iselite = 0 and id not in (select msgId from sq_message_attach)";
			}else{
			    if(msgLevel.equals("2")){
					if(!condition.equals(""))
						condition += " and ";
					condition += "msg_level = 0";
				}
			}
			
			if(isElite.equals("1")){
				if(!condition.equals(""))
					condition += " and ";
				condition += "msg_level = 0 and iselite = 1 and id not in (select msgId from sq_message_attach)";
			}else{
			    if(isElite.equals("2")){
					if(!condition.equals(""))
						condition += " and ";
					condition += "iselite = 0";
				}
			}
			
			if(attach.equals("1")){
				if(!condition.equals(""))
					condition += " and ";
				condition += "msg_level = 0 and iselite = 0 and id in (select msgId from sq_message_attach)";
			}else{
			    if(attach.equals("2")){
					if(!condition.equals(""))
						condition += " and ";
					condition += "id in (select msgId from sq_message_attach)";
				}
			}
			
			if(!condition.equals("")){
				condition = " where " + condition;
				sql = sql + condition;
			}

			String orderby = "";
			if (selboard.equals("allboard"))
				orderby = " ORDER BY lydate desc";
			else
				orderby = " ORDER BY msg_level desc,lydate desc";
			sql = sql + orderby;

			int pagesize = 10;
			Paginator paginator = new Paginator(request);
			int curpage = paginator.getCurPage();
			PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
			ResultIterator ri = pageconn.getResultIterator(sql);
			paginator.init(pageconn.getTotal(), pagesize);
			
			ResultRecord rr = null;
			
			//设置当前页数和总页数
			int totalpages = paginator.getTotalPages();
			if (totalpages==0)
			{
				curpage = 1;
				totalpages = 1;
			}
%>
		<TABLE cellSpacing=0 cellPadding=1 width="98%" align=center>
		  <TBODY>
		  <TR height=25 class="td_title">
			<TD height="26" colSpan=3 align=middle noWrap bgcolor="#EFEBDE"><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
			<TD width=91 height="26" align=middle noWrap  bgcolor="#EFEBDE"><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
			<TD width=55 height="26" align=middle noWrap bgcolor="#EFEBDE"><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
			<TD width=55 height="26" align=middle noWrap bgcolor="#EFEBDE"><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
			<TD width=80 height="26" align=middle noWrap bgcolor="#EFEBDE"><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
			<TD width=91 height="26" align=middle noWrap bgcolor="#EFEBDE"><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
		  </TR>
		  </TBODY>
		</TABLE>
<%		
			String topic = "",name="",lydate="",expression="", myboardname = "", myboardcode = "";
			int id = -1;
			int k = 0,recount=0,hit=0,type=0;
			MsgDb md = new MsgDb();
			Leaf myleaf = new Leaf();
			Directory dir = new Directory();
			com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
			while (ri.hasNext()) {
				rr = (ResultRecord)ri.next(); 
				k++;
				id = rr.getInt("id");
				md = md.getMsgDb(id);
				topic = md.getTitle();
				name = md.getName();
				lydate = com.redmoon.forum.ForumSkin.formatDate(request, md.getAddDate());
				recount = md.getRecount();
				hit = md.getHit();
				expression = "" + md.getExpression();
				type = md.getType();
				myboardcode = md.getboardcode();
				myleaf = dir.getLeaf(myboardcode);
				myboardname = "";
				if (myleaf!=null)
					myboardname = myleaf.getName();
	%>
	  <table cellspacing=0 cellpadding=1 width="98%" align=center>
		<tbody> 
		 <tr>
		  <td width=30 height="22" align=middle noWrap bgcolor=#f8f8f8><input name="ids" value="<%=id%>" type="checkbox"></td>
		  <td noWrap align=left width=50 bgcolor=#f8f8f8><%=md.getId()%></td> 
		  <td noWrap align=middle width=30 bgcolor=#f8f8f8> 
		    <%if (recount>20){ %>
			  <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="<%=skinPath%>/images/f_hot.gif"> 
			<%}else if (recount>0) {%>
			  <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="<%=skinPath%>/images/f_new.gif"> 
			<%}else {%>
			  <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="<%=skinPath%>/images/f_norm.gif"> 
			<%}%>	   
		  </td>
		  <td align=middle width=17 bgcolor=#ffffff> 
			  <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
			   <a href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>" target=_blank> 
			  <% if (type==1) { %>
			  <IMG height=15 alt="" src="images/f_poll.gif" width=17 border=0>
			  <%}else { %>
			  <img src="images/brow/<%=expression%>.gif" border=0>
			  <%}%>
			  </a>
		   </td>
		   <td onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8> 
			<%
			if (recount==0) {
			%>
			  <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="<%=skinPath%>/images/minus.gif" loaded="no"> 
			  <% }else { %>
			  <img id=followImg<%=id%> title=<lt:Label res="res.label.forum.listtopic" key="extend_reply"/> style="CURSOR: hand" onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>&hit=<%=hit+1%>&boardname=<%=urlboardname%>')" src="<%=skinPath%>/images/plus.gif" loaded="no"> 
			  <% } %>
			  <a target="_blank" href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>"><%=StrUtil.toHtml(topic)%></a>
			  <%
			// 计算共有多少页回贴
			int allpages = (int)Math.ceil((double)recount/pagesize);
			if (allpages>1)
			{
				out.print("[");
				for (int m=1; m<=allpages; m++)
				{ %>
			  <a target="_blank" href="showtopic.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&boardname=<%=urlboardname%>&rootid=<%=id%>&CPages=<%=m%>"><%=m%></a> 
			  <% }
				out.print("]");
			 }%>        
		  </td>
		  <td align=middle width=91 bgcolor=#ffffff> 
		  <%if (privilege.getUser(request).equals(name)) { %>
			  <IMG height=14 src="<%=skinPath%>/images/my.gif" width=14>
		  <%}%>
		  <a href="../userinfo.jsp?username=<%=name%>"><%=um.getUser(name).getNick()%></a>      </td>
		  <td align=middle width=55 bgcolor=#f8f8f8><font color=red>[<%=recount%>]</font></td>
		  <td align=middle width=55 bgcolor=#ffffff><%=hit%></td>
		  <td align=left width=80 bgcolor=#f8f8f8> 
			<table cellspacing=0 cellpadding=2 width="100%" align=center border=0>
			  <tbody> 
			  <tr> 
				<td width="10%">&nbsp;</td>
				<td><%=lydate%></td>
			  </tr>
			  </tbody> 
			</table>      
		  </td>
		  <td align=middle width=91 bgcolor=#ffffff>&nbsp;&nbsp;
			<%if (!myboardcode.equals(Leaf.CODE_BLOG)) {%>
			<a target=_blank href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(myboardcode)%>"><%=myboardname%></a>&nbsp;
			<%}else{%>
			<a target=_blank href="blog/myblog.jsp?userName=<%=StrUtil.UrlEncode(md.getName())%>"><%=myboardname%></a>
			<%}%></td>
		</tr>
		<tr id=follow<%=id%> style="DISPLAY: none">
		  <td noWrap align=middle width=30 bgcolor=#f8f8f8>&nbsp;</td>
		  <td noWrap align=middle width=30 bgcolor=#f8f8f8>&nbsp;</td> 
		  <td noWrap align=middle width=30 bgcolor=#f8f8f8>&nbsp;</td>
		  <td align=middle width=17 bgcolor=#ffffff>&nbsp;</td>
		  <td onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8 colspan="6">
		  <div id=followDIV<%=id%> style="WIDTH: 100%;BACKGROUND-COLOR: lightyellow" onclick='loadThreadFollow(<%=id%>,<%=id%>,"&hit=<%=hit+1%>&boardname=<%=urlboardname%>")'><span style="WIDTH: 100%;">
		   <lt:Label res="res.label.forum.listtopic" key="wait"/>
		 </span></div></td>
		</tr>
		<tr> 
		  <td style="PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; PADDING-TOP: 0px" colspan=7>      </td>
		</tr>
		</tbody> 
	  </table>
<%
				}
%>
		<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
			<tr> 
			  <td width="51%" height="23" align="left">
			  <input value="<lt:Label res="res.label.forum.topic_m" key="sel_all"/>" type="button" onClick="selAllCheckBox('ids')">&nbsp;&nbsp;
			  <input value="<lt:Label res="res.label.forum.topic_m" key="clear_all"/>" type="button" onClick="clearAllCheckBox('ids')">&nbsp;&nbsp; 
			  </td>
			  <td width="49%" align="right"><%   	
			  querystr = "op="+op+"&selboard="+selboard+"&title="+title+"&nick="+nick+"&sBeginDate="+sBeginDate+"&sEndDate="+sEndDate;
			  querystr += "&threadType="+threadType+"&hitLess="+hitLess+"&hitMore="+hitMore+"&recountLess="+recountLess+"&recountMore="+recountMore;
			  querystr += "&sReDate="+sReDate+"&msgLevel="+msgLevel+"&isElite="+isElite+"&attach="+attach;
			  out.print(paginator.getCurPageBlock(request, "?"+querystr));
			  %></td>
			</tr>
		</table> 		
<%		
		}
%>
	    <br>
	</td>
  </tr>
</table>
<iframe width=0 height=0 src="" id="hiddenframe"></iframe>
<br>
<br>
<table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
	<tr>
	  <td align="center"><input type="button" value="提交" onclick="doOperation()"></td>
	</tr>
</table>
</body>
<script>
function doOperation() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}
	var operation = getRadioValue("operation");
	if (operation == "") {
		alert("请选择操作项目！");
		return;
	}

	var msgLevel = setMsgLevel.value;
	if(operation == "moveBoard"){
		var toBoard = selToBoard.value;
		window.location.href = "batch_topic_m.jsp?operation=" + operation + "&selToBoard=" + toBoard + "&ids=" + ids + "&<%=querystr%>";
	}else{
		if(operation == "moveThreadType"){
			var toThreadType = selToThreadType.value;
			toBoardCode = hToBoardCode.value;
			window.location.href = "batch_topic_m.jsp?operation=" + operation + "&toThreadType=" + toThreadType + "&toBoardCode=" + toBoardCode + "&ids=" + ids + "&<%=querystr%>";
		}else{
			window.location.href = "batch_topic_m.jsp?operation=" + operation + "&ids=" + ids + "&<%=querystr%>";
		}
	}
}

function threadTypeChange(){
  for(var i = 0; i < threadTypeArr.length; i++){
    if(form1.threadType.value == threadTypeArr[i][0]){
	   form1.txtBoardName.value = threadTypeArr[i][1]; 
	   form1.hBoardCode.value = toThreadTypeArr[i][2];  
	   return;
	}   
  }
  form1.txtBoardName.value = "";
  form1.hBoardCode.value = "";
}

function toThreadTypeChange(){
  for(var i = 0; i < toThreadTypeArr.length; i++){
    if(selToThreadType.value == toThreadTypeArr[i][0]){
	   txtToBoardName.value = toThreadTypeArr[i][1];  
	   hToBoardCode.value = toThreadTypeArr[i][2]; 
	   return;
	}   
  }
  txtToBoardName.value = "";
  hToBoardCode.value = "";
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}
</script>
</html>
