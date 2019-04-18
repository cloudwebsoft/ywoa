<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>付费用户管理管理</title>
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
	var ret = showModalDialog('VIPCard_dir_sel_multi.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	form1.kindNames.value = "";
	form1.kind.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.kindNames.value=="") {
			form1.kind.value += ret[i][0];
			form1.kindNames.value += ret[i][1];
		}
		else {
			form1.kind.value += "," + ret[i][0];
			form1.kindNames.value += "," + ret[i][1];
		}
	}
}

function getLeaves() {
	return form1.kind.value;
}

function setFee(fee) {
	form1.fee.value = fee;
}

function getFee() {
	if (form1.fee.value=="")
		return 0.0;
	else
		return form1.fee.value;
}
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理付费用户</td>
  </tr>
</table>
<br>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"> 添加付费用户 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="entranceVIPTeach.jsp">入口管理</a></td>
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

VIPCardDb vtu = new VIPCardDb();

if (op.equals("add")) {
	String userName = ParamUtil.get(request, "userName").trim();
	String errMsg = "";
	if (userName.equals("")) {
		// errMsg += "用户名不能为空！\\r\\n";
	}
	else {
		// 检查用户在论坛中是否存在
		UserDb ud = new UserDb();
		ud = ud.getUser(userName);
		if (!ud.isLoaded())
			errMsg += "用户不存在！\\r\\n";
		else if (!ud.isValid())
			errMsg += "该用户帐号已被禁止！\\r\\n";
	}
	String id = ParamUtil.get(request, "id");
	if (id.equals("") || !StrUtil.isNumeric(id))
		errMsg += "卡号不能为空且必须为数字！\\r\\n";
	String pwd = ParamUtil.get(request, "pwd").trim();
	if (pwd.equals(""))
		errMsg += "密码不能为空！\\r\\n";
	double fee = 0;
	String kind="",bDate,eDate;
	Date beginDate=null,endDate=null;
	boolean isValid = false;
	try {
		fee = ParamUtil.getDouble(request, "fee");
		String[] kindary = ParamUtil.getParameters(request, "kind");
		if (kindary==null)
			errMsg += "对不起，您必须选择至少一项类别！\\r\\n";
		else {
			int klen = kindary.length;
			for (int k=0; k<klen; k++) {
				if (kind.equals(""))
					kind = kindary[k];
				else
					kind += "|" + kindary[k];
			}
		}
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
		errMsg += "开始和结束日期不能为空！";
	if (!errMsg.equals("")) {
		out.print(StrUtil.Alert(errMsg));
	}
	else {
		vtu.setUserName(userName);
		vtu.setFee(fee);
		vtu.setBeginDate(beginDate);
		vtu.setEndDate(endDate);
		vtu.setKind(kind);
		vtu.setId(id);
		vtu.setPwd(pwd);
		vtu.setValid(isValid);
		if (vtu.create())
			out.print(StrUtil.p_center("<BR>添加成功！您现在可以继续添加学习卡！"));
		else
			out.print(StrUtil.Alert("添加失败！"));
	}
}

%>	
	<br>
      <br>
      <table width="38%"  border="0" align="center" cellpadding="5" class="tableframe_gray">
        <form name="form1" method="post" action="?op=add">
        <tr>
          <td align="center">卡&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号</td>
          <td align="left"><input name="id" type="hidden" id="id" value="<%=vtu.getNextID()%>"><%=vtu.getNextID()%></td>
        </tr>
        <tr>
          <td align="center">密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码</td>
          <td align="left"><input name="pwd" type="text" id="pwd" value="<%=RandomSecquenceCreator.getId(8)%>"></td>
        </tr>
        <tr>
          <td align="center">用&nbsp;&nbsp;户&nbsp;&nbsp;名</td>
          <td align="left"><label>
            <input name="userName" type="text" id="userName">
          </label></td>
        </tr>
        <tr>
          <td width="29%" align="center">开始时间</td>
          <td width="71%" align="left"><input name="beginDate" size=10 readonly value="">
            &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('beginDate','yyyy-mm-dd')"></td>
        </tr>
        <tr>
          <td align="center">到期时间</td>
          <td align="left"><input name="endDate" size=10 readonly>
            &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('endDate','yyyy-mm-dd')"></td>
        </tr>
        
        <tr>
          <td align="center">费用共计</td>
          <td align="left"><input name="fee" type="text" id="fee" size="10">
            <label>
            &nbsp;元&nbsp;&nbsp;&nbsp;</label></td>
        </tr>
        <tr>
          <td align="center">目&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;录</td>
          <td align="left"><label>
          <input type="hidden" name="kind">
          <textarea name="kindNames" cols="45" rows="3" readOnly wrap="yes" id="kindNames"></textarea>			
            <input class="SmallButton" title="添加部门" onClick="openWinKinds()" type="button" value="选择目录" name="button">
            </label></td>
        </tr>
        <tr>
          <td align="center">是否有效</td>
          <td align="left"><input type=checkbox name="isValid" value="true"></td>
        </tr>
        <tr>
          <td colspan="2" align="center"><input type="submit" name="Submit" value="确定">
&nbsp;&nbsp;&nbsp;&nbsp;            &nbsp;
<input type="reset" name="Submit2" value="重写"></td>
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
  