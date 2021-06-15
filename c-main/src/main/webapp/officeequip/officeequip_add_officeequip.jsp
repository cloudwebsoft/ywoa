<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%
String strTypeId = ParamUtil.get(request, "typeId");
String strEquipId =ParamUtil.get(request, "equipId");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用品领用</title>
<link href="../common.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
.style2 {font-size: 14px}
.STYLE3 {color: #313031}
-->
</style>
<script>
var oldTypeId = "<%=strTypeId%>";
function getOfficeEquipOfType(typeId) {
	if (typeId=="") {
		alert("请选择类别");
		form1.typeId.value = oldTypeId;
		return;
	}	
	window.location.href = "?typeId=" + typeId;
}

var oldEquipId = "<%=strEquipId%>";
function getEquipOfType(equipId) {
	if (equipId=="") {
		alert("请选择类别");
		form1.equipId.value = oldEquipId;
		return;
	}	
	window.location.href = "?equipId=" + equipId + "&typeId=" + oldTypeId;
}
</script>
<script language="JavaScript" type="text/JavaScript">
<!--
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
	GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}
//-->
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="officetype";
if (!privilege.isUserPrivValid(request, priv)) {
	//out.println(fchar.makeErrMsg("对不起，您不具有发起流程的权限！"));
	//return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("chageStorageCount")) {
	OfficeMgr om = new OfficeMgr();
	boolean re = false;
	try {
		  re = om.chStorageCount(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re )
		out.print(StrUtil.Alert_Redirect("操作成功！", "officeequip_add_officeequip.jsp"));
}
Date d = new Date();
String dt = DateUtil.format(d, "yyyy-MM-dd");
%>
<table width="663" border="0" align="center" cellpadding="3" cellspacing="0" class="tableframe">
<form action="?op=chageStorageCount" name="form1" method="post">
<tr>
  <td colspan="4" class="right-title" > &nbsp;&nbsp;用品入库登记</td>
</tr>
<tr>
  <td colspan="4" ><table width="657" border="0" align="center">
      <tr>
        <td width="111">用品类别：</td>
        <td width="189"><%
	  OfficeTypeDb otd = new OfficeTypeDb();
	  String opts = "";
	  Iterator ir = otd.list().iterator();
	  while (ir.hasNext()) {
	  	 otd = (OfficeTypeDb)ir.next();
	  	 opts += "<option value='" + otd.getId() + "'>" + otd.getName() + "</option>";
	  }
	  %>
          <select name="typeId" id="typeId" onChange="getOfficeEquipOfType(form1.typeId.value)">
          <option value="" selected>-----请选择-----</option>
          <%=opts%>
        </select>
		 <script>
	     form1.typeId.value = "<%=strTypeId%>";
	    </script>		</td>
        <td width="72">用品名称：</td>
        <td width="257"><%
	  String opts1 = "";
	  int total =0 ;
	  OfficeDb od = new OfficeDb();
	  if (!strTypeId.equals("")) {
		  String sql = "select id from office_equipment where typeId=" + strTypeId;
		  Iterator ir1 = od.list(sql).iterator();
		  while (ir1.hasNext()) {
			 od = (OfficeDb)ir1.next();
			 opts1 += "<option value='" + od.getId() + "'>" + od.getOfficeName() + "</option>";
		     total += od.getStorageCount();
		  }
	  }
	  %>
          <select name="equipId" id="equipId" onChange="getEquipOfType(form1.equipId.value)">
          <option selected>-----请选择-----</option>
          <%=opts1%>
        </select>
		<script>
	  form1.equipId.value = "<%=strEquipId%>";
	  </script>		</td>
      </tr>
      <tr>
        <td>库存量：</td>
        <td><%
	  if (!strEquipId.equals("")) {
	  	int equipId = Integer.parseInt(strEquipId);
	  	od = od.getOfficeDb(equipId);
		total = od.getStorageCount();
	  }
	  %>
          <%=total%> </td>
        <td>数量：</td>
        <td><input name="storageCount" type="text" id="storageCount" size="20"></td>
      </tr>
    </table>
    <p>&nbsp;</p></td>
  </tr>

 <tr> 
  <td colspan="4" align="center"><input name="submit" type="submit" class="btn"  value="确定" >    &nbsp;</td>
</tr>
</form>
</table>
</body>
</html>
