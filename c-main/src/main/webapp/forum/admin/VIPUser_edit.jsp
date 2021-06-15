<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
function SelectDate(ObjName,FormatDate){
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
	var ret = showModalDialog('board_sel_multi.jsp',window.self,'dialogWidth:500px;dialogHeight:320px;status:no;help:no;')
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
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.entrance" key="vip_user_manage"/></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"> <lt:Label res="res.label.forum.admin.entrance" key="modify_user_vip"/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="entranceVIPUser.jsp"><lt:Label res="res.label.forum.admin.entrance" key="entrance_manage"/></a></td>
  </tr>
  <tr> 
    <td valign="top">
<%
String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}

VIPUserDb vpc = new VIPUserDb();
vpc = vpc.getVIPUserDb(userName);
if (vpc==null || !vpc.isLoaded()) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.entrance", "vip_user_not_exist"))); // "该VIP用户不存在！"));
	return;
}

UserDb ud = new UserDb();
ud = ud.getUser(userName);

String op = ParamUtil.get(request, "op");
if (op.equals("edit")) {
	String errMsg = "";
	String boards="",bDate,eDate,fingerPrint="";
	Date beginDate=null,endDate=null;
	boolean isUseFingerPrint = false;
	boolean isValid = false;
	try {
		boards = ParamUtil.get(request, "boards");
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
		vpc.setBeginDate(beginDate);
		vpc.setEndDate(endDate);
		vpc.setBoards(boards);
		vpc.setValid(isValid);
		if (vpc.save())
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
		else
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	}
}
%>	
	<br>
	<table width="66%"  border="0" align="center" cellpadding="5" class="tableframe_gray">
        <form id="form1" name="form1" method="post" action="?op=edit">
        <tr>
          <td align="left"><lt:Label res="res.label.forum.admin.entrance" key="user_name"/></td>
          <td align="left"><%=ud.getNick()%><input name="userName" type="hidden" id="userName" value="<%=userName%>"></td>
        </tr>
        
        
        <tr>
          <td width="27%" align="left"><lt:Label res="res.label.forum.admin.entrance" key="begin_date"/></td>
          <td width="73%" align="left"><input name="beginDate" size=10 readonly value="<%=DateUtil.format(vpc.getBeginDate(), "yyyy-MM-dd")%>">
            &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('beginDate','yyyy-mm-dd')"></td>
        </tr>
        <tr>
          <td align="left"><lt:Label res="res.label.forum.admin.entrance" key="end_date"/></td>
          <td align="left"><input name="endDate" size=10 readonly value="<%=DateUtil.format(vpc.getEndDate(), "yyyy-MM-dd")%>">
            &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('endDate','yyyy-mm-dd')"></td>
        </tr>
        
        <tr>
          <td align="left"><lt:Label res="res.label.forum.admin.entrance" key="board"/></td>
          <td align="left"><label>
          <input type="hidden" name="boards" value="<%=vpc.getBoards()%>">
		  <%
		  String boards = vpc.getBoards();
		  String boardNames = "";
		  if (boards!=null && !boards.equals("")) {
		  	String[] boardary = boards.split(",");
			int len = boardary.length;
			Directory dir = new Directory();
			Leaf lf = null;
			for (int i=0; i<len; i++) {
				lf = dir.getLeaf(boardary[i]);
				// System.out.println("VIPCard_edit.jsp " + kindAry[i]);
				if (lf!=null) {
					if (boardNames.equals(""))
						boardNames = lf.getName();
					else
						boardNames += "," + lf.getName();
				}
			}
		  }
		  %>
          <textarea name="boardNames" cols="45" rows="3" readOnly wrap="yes" id="boardNames"><%=boardNames%></textarea>
          <input onClick="openWinKinds()" type="button" value="<lt:Label res="res.label.forum.admin.entrance" key="sel_board"/>" name="button">
</label></td>
        </tr>
        
        <tr>
          <td align="left"><lt:Label res="res.label.forum.admin.entrance" key="is_valid"/></td>
          <td align="left">
		  <input type=checkbox name="isValid" value="true" <%=vpc.isValid()?"checked":""%>></td>
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
  