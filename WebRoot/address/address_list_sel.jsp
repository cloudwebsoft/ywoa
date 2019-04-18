<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<style type="text/css">
.menutitle{
cursor:pointer;
margin-bottom: 5px;
background-color:#ECECFF;
color:#000000;
width:140px;
padding:2px;
text-align:center;
font-weight:bold;
border:1px solid #000000;
}

.submenu{
margin-bottom: 0.1em;
}
.STYLE1 {
	color: #FFFFFF;
	font-weight: bold;
}
</style>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>通讯录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0" style="overflow:auto">
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String strtype = ParamUtil.get(request, "type");
int type = AddressDb.TYPE_USER;
if (!strtype.equals(""))
	type = Integer.parseInt(strtype);
String mode = ParamUtil.get(request, "mode");
String groupType = ParamUtil.get(request, "dir_code");
if (!mode.equals("show")) {	
	if (type==AddressDb.TYPE_PUBLIC) {
		if (!privilege.isUserPrivValid(request, "admin.address.public")) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
}

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", groupType, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}


			String op = ParamUtil.get(request, "op");

			String sql = "select id from address where type=" + type;
			String myname = privilege.getUser(request);
			String group = ParamUtil.get(request, "dir_code");
			String searchStr = "";
			
				if (op.equals("search")){
				   String person = ParamUtil.get(request, "person");
				   String nickname = ParamUtil.get(request, "nickname");
				   String company = ParamUtil.get(request, "company");
				   String address = ParamUtil.get(request, "address");
				   String street = ParamUtil.get(request, "street");
				   String QQ = ParamUtil.get(request, "QQ");
				   String MSN = ParamUtil.get(request, "MSN");
				   String typeId = ParamUtil.get(request, "typeId");	
				   String mobile = ParamUtil.get(request, "mobile");
				   
				   searchStr += "&person=" + StrUtil.UrlEncode(person);
				   searchStr += "&nickname=" + StrUtil.UrlEncode(nickname);
				   searchStr += "&company=" + StrUtil.UrlEncode(company);
				   searchStr += "&address=" + StrUtil.UrlEncode(address);
				   searchStr += "&street=" + StrUtil.UrlEncode(street);
				   searchStr += "&QQ=" + StrUtil.UrlEncode(QQ);
				   searchStr += "&MSN=" + StrUtil.UrlEncode(MSN);
				   searchStr += "&typeId=" + typeId;				   
				   searchStr += "&mobile=" + mobile;  

				   if (type==AddressDb.TYPE_USER)
					   sql = "select id from address where userName=" + StrUtil.sqlstr(privilege.getUser(request)) + " and type=" + AddressDb.TYPE_USER;
				   else {
						sql = "select id from address where type=" + type;
				   }
				   if (!person.equals("")){
						sql += " and person like " + StrUtil.sqlstr("%" + person + "%");
				   }
				   if (!nickname.equals("")){
						sql += " and nickname like " + StrUtil.sqlstr("%" + nickname + "%");
				   }
				   if (!company.equals("")){
						sql += " and company like " + StrUtil.sqlstr("%" + company + "%");
				   }
				   if (!address.equals("")){
						sql += " and address like " + StrUtil.sqlstr("%" + address + "%");
				   }
				   if (!street.equals("")){
						sql += " and street like " + StrUtil.sqlstr("%" + street + "%");
				   }
				   if (!MSN.equals("")){
						sql += " and MSN like " + StrUtil.sqlstr("%" + MSN + "%");
				   }
				   if (!QQ.equals("")){
						sql += " and QQ like " + StrUtil.sqlstr("%" + QQ + "%");
				   }
				   if (!typeId.equals("")){
                       sql += " and typeId = " + StrUtil.sqlstr(typeId);				   
				   }
				   if (!mobile.equals("")) {
					   sql += " and mobile like " + StrUtil.sqlstr("%" + mobile + "%");
				   }				   
				}
				else {
					if (!group.equals(""))
						sql += " and typeId = " + StrUtil.sqlstr(group);
					if (type!=AddressDb.TYPE_PUBLIC)	 
						sql += " and userName=" + 	StrUtil.sqlstr(privilege.getUser(request)); 
				}
				
			if (type==AddressDb.TYPE_PUBLIC) {
				sql += " and unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request));
			}				


			int pagesize = 30;
			Paginator paginator = new Paginator(request);
			int curpage = paginator.getCurPage();
			AddressDb addr = new AddressDb();
			ListResult lr = addr.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			Vector v = lr.getResult();
			Iterator ir = null;
			if (v!=null)
				ir = v.iterator();
			paginator.init(total, pagesize);
			// 设置当前页数和总页数
			int totalpages = paginator.getTotalPages();
			if (totalpages==0)
			{
				curpage = 1;
				totalpages = 1;
			}
	%>
          <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
              <td width="23%" height="30" align="left"><input class="btn" type="button" onclick="window.location.href='address_search.jsp?type=<%=type%>&flag=sel'" value="查询" />
              </td>
              <td width="77%" align="right"><span class="title1">共 <b><%=paginator.getTotal() %></b> 条　每页 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></span></td>
            </tr>
</table>
          <form name="form1" action="../message_oa/sms_send.jsp" method="post">
            <table width="100%" class="tabStyle_1" border="0" align="center" cellpadding="2" cellspacing="0">
              <thead>
              <tr align="center">
                <td width="4%"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('mobiles'); else cancelAllCheckBox('mobiles');" /></td>
                <td width="18%">姓名</td>
                <td width="23%">部门</td>
                <td width="19%">科室&nbsp;</td>
                <td width="23%">手机</td>
                <td width="13%">操作</td>
                </tr>
              </thead>
              <%	
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			addr = (AddressDb)ir.next();
			i++;
			int id = addr.getId();
			String person = addr.getPerson();
			String mobile = addr.getMobile();
			String email = addr.getEmail();
			String qq = addr.getQQ();
			String job = addr.getJob();
			String adddate = DateUtil.format(addr.getAddDate(), "yyyy-MM-dd");
		%>
              <tr align="center">
                <td align="center"><input type="checkbox" name="mobiles" value="<%=person%>|<%=mobile%>" /></td>
                <td align="left"><a href=address_show.jsp?id=<%=id%>&mode=show><%=person%></a></td>
                <td><%=addr.getDepartment()%></td>
                <td><%=addr.getCompany()%></td>
                <td><%=mobile%>&nbsp;</td>
                <td><a href="javascript:sel('<%=person%>','<%=mobile%>')">选择</a></td>
              </tr>
              <%}%>
            </table>
            <table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
              <tr>
                <td width="26%" align="left">
				<input class="btn" type="button" onClick="selBatch()" value="选择">                
                &nbsp;&nbsp;
                <input id="btnSelAll" type="button" class="btn" title="选择通讯录中全部人员" value="全部人员" onclick="ajaxSelAll('<%=type%>', '<%=groupType%>')" />
                <span id="spanLoad"></span>
                </td>
                <td width="74%" align="right"><%
						String querystr = "type=" + type + "&mode=" + mode + "&dir_code=" + StrUtil.UrlEncode(groupType);
						if (op.equals("search")) {
							querystr += "&op=search" + searchStr;
						}						
						out.print(paginator.getCurPageBlock("?"+querystr));
					%></td>
              </tr>
            </table>
</form>
</body>
<script>
function openExcel() {
	var sql = "<%=sql%>";
	window.open("address_excel.jsp?sql=" + sql); 
}
</script>
<script>
function sel(person, mobile) {
	window.top.rightAddressFrame.addPerson(person, mobile);
}
function selBatch() {
	var mo = getCheckboxValue("mobiles");
	var ary = mo.split(",");
	for (var i=0; i<ary.length; i++){
		if (ary[i]=="")
			continue;
		var pair = ary[i].split("|");
		window.top.rightAddressFrame.addPerson(pair[0], pair[1]);
	}
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
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
function cancelAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
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

var errFunc = function(response) {
	window.status = 'Error ' + response.status + ' - ' + response.statusText;
	// alert(response.responseText);
}

function doFunc(response){
	var resText = response.responseText.trim();
	var json = $.parseJSON(resText);

	window.parent.rightAddressFrame.clearAll();
	o("spanLoad").innerHTML = "";
	
	$.each(json,function(index,obj){
		  sel(obj.person, obj.mobile);
		});
}

function ajaxSelAll() {
	var str = "<%=querystr%>";
	o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";
	var myAjax = new cwAjax.Request(
		"ajax_get_all.jsp",
		{ 
			method:"post",
			parameters:str,
			onComplete:doFunc,
			onError:errFunc
		}
	);
}
</script>
</html>