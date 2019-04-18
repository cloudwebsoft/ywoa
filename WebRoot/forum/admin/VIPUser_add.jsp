<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.entrance" key="vip_user_manage"/></title>
<script>
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

var GetDate=""; 
function SelectDate(ObjName,FormatDate) {
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("../../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
} 

function openWinKinds() {
	var ret = showModalDialog('board_sel_multi.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	form1.boardNames.value = "";
	form1.boards.value = "";
	
	for (var i=0; i<ret.length; i++) {
		if (form1.boardNames.value=="") {
			form1.boards.value += ret[i][0];
			form1.boardNames.value += ret[i][1];
		}
		else {
			form1.boards.value += "," + ret[i][0];
			form1.boardNames.value += "," + ret[i][1];
		}
	}
}

function getBoards() {
	return form1.boards.value;
}

function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
function setPerson(userName, userNick){
	form1.userName.value = userNick;
}
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.entrance" key="vip_user_manage"/></td>
  </tr>
</table>
<br>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"> <lt:Label res="res.label.forum.admin.entrance" key="add_vip_user"/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="entranceVIPUser.jsp"><lt:Label res="res.label.forum.admin.entrance" key="entrance_manage"/></a></td>
  </tr>
  <tr> 
    <td valign="top">
<%
if (!privilege.isUserPrivValid(request, "forum.plugin"))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

VIPUserDb vtu = new VIPUserDb();

if (op.equals("add")) {
	UserDb ud = new UserDb();

	String userName = ParamUtil.get(request, "userName").trim();
	String errMsg = "";
	if (userName.equals("")) {
		// errMsg += "用户名不能为空！\\r\\n";
	}
	else {
		// 检查用户在论坛中是否存在
		ud = ud.getUserDbByNick(userName);
		if (ud==null)
			errMsg += SkinUtil.LoadString(request, "res.label.forum.admin.entrance", "vip_user_not_exist") + "\\r\\n";
		else if (!ud.isValid())
			errMsg += SkinUtil.LoadString(request, "res.label.forum.admin.entrance", "user_invalid") + "\\r\\n";
	}
	String boards="",bDate,eDate;
	Date beginDate=null,endDate=null;
	boolean isValid = false;
	boards = ParamUtil.get(request, "boards");
	try {
		bDate = ParamUtil.get(request, "beginDate");
		eDate = ParamUtil.get(request, "endDate");
		beginDate = DateUtil.parse(bDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(eDate, "yyyy-MM-dd");
		isValid = ParamUtil.getBoolean(request, "isValid", false);
	}
	catch (Exception e) {
		errMsg += e.getMessage() + "\\r\\n";
	}
	if (beginDate==null || endDate==null)
		errMsg += SkinUtil.LoadString(request, "res.label.forum.admin.entrance", "date_empty");
	if (!errMsg.equals("")) {
		out.print(StrUtil.Alert(errMsg));
	}
	else {
		vtu.setUserName(ud.getName());
		vtu.setBeginDate(beginDate);
		vtu.setEndDate(endDate);
		vtu.setValid(isValid);
		vtu.setBoards(boards);
		if (vtu.create())
			out.print(StrUtil.p_center(SkinUtil.LoadString(request, "info_op_success")));
		else
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	}
}
%>	
	<br>
      <br>
      <table width="38%"  border="0" align="center" cellpadding="5" class="tableframe_gray">
        <form name="form1" method="post" action="?op=add">
        <tr>
          <td align="center"><lt:Label res="res.label.forum.admin.entrance" key="user_name"/></td>
          <td align="left"><label>
            <input name="userName" type="text" id="userName"><a href="#" onClick="openWin('forum_user_sel.jsp', 480, 420)">
                <lt:Label res="res.label.forum.admin.manager_list" key="select"/>
                </a>
          </label></td>
        </tr>
        
        <tr>
          <td width="29%" align="center"><lt:Label res="res.label.forum.admin.entrance" key="begin_date"/></td>
          <td width="71%" align="left"><input name="beginDate" size=10 readonly value="">
            &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('beginDate','yyyy-mm-dd')"></td>
        </tr>
        <tr>
          <td align="center"><lt:Label res="res.label.forum.admin.entrance" key="end_date"/></td>
          <td align="left"><input name="endDate" size=10 readonly>
            &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('endDate','yyyy-mm-dd')"></td>
        </tr>
        
        <tr>
          <td align="center"><lt:Label res="res.label.forum.admin.entrance" key="board"/></td>
          <td align="left"><label>
		  <input name="boards" value="" type="hidden">
          <textarea name="boardNames" cols="45" rows="3" readOnly wrap="yes" id="boardNames"></textarea>			
            <input class="SmallButton" title="选择版块" onClick="openWinKinds()" type="button" value="<lt:Label res="res.label.forum.admin.entrance" key="sel_board"/>" name="button">
            </label></td>
        </tr>
        <tr>
          <td align="center"><lt:Label res="res.label.forum.admin.entrance" key="is_valid"/></td>
          <td align="left"><input type=checkbox name="isValid" value="true"></td>
        </tr>
        <tr>
          <td colspan="2" align="center"><input type="submit" name="Submit" value="<lt:Label key="ok"/>">
&nbsp;&nbsp;&nbsp;&nbsp;            &nbsp;
<input type="reset" name="Submit2" value="<lt:Label key="reset"/>"></td>
        </tr>
      </form>
      </table>
      <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  