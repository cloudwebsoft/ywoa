<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.dept.DeptUserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_customer";

int kind = ParamUtil.getInt(request, "kind", 0);

String preDate = ParamUtil.get(request, "preDate");
String customer = ParamUtil.get(request, "customer");
String person = ParamUtil.get(request, "person");
String web = ParamUtil.get(request, "web");
String tel = ParamUtil.get(request, "tel");
String email = ParamUtil.get(request, "email");
String customerType = ParamUtil.get(request, "customerType");
String customerRygm = ParamUtil.get(request, "customerRygm");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

String founder = ParamUtil.get(request, "founder");
String zczj = ParamUtil.get(request, "zczj");

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	if (preDate.equals("*")) {
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	}
	else {
		strBeginDate = "";
		strEndDate = "";
	}
}

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function selCustomer(id, customer) {
	window.opener.setIntpuObjValue(id, customer);
	window.close();
}

function selAllCheckBox(checkboxname) {
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

function clearAllCheckBox(checkboxname) {
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

function doDistribute() {
	jConfirm("您确定要分配吗？","提示",function(r){
		if(!r){return false;}
		else{
			var ids = getCheckboxValue("ids");
			if (ids=="") {
				jAlert("请选择客户！","提示");
				return;
			}
			openWin("customer_distribute_batch.jsp?ids=" + ids, 300, 50);
		}
	})
}
</script>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<%@ include file="customer_distribute_inc_menu_top.jsp"%>
<script>
<%if (kind==0) {%>
o("menu1").className="current"; 
<%} else if (kind==1){%>
o("menu2").className="current"; 
<%} else {%>
o("menu4").className="current"; 
<%}%>
</script>
<div class="spacerH"></div>
<%
String unitCode = privilege.getUserUnitCode(request);

String sql = "select id from " + fd.getTableNameByForm() + " where kind='" + kind + "' and unit_code=" + StrUtil.sqlstr(unitCode);

if (op.equals("search")) {
	if (!customer.equals(""))
		sql += " and customer like " + StrUtil.sqlstr("%" + customer + "%");
	if (!person.equals(""))
		sql += " and sales_person=" + StrUtil.sqlstr(person);
	if (!founder.equals("")) {
		sql += " and founder=" + StrUtil.sqlstr(founder);
	}
	
	if (!web.equals(""))
		sql += " and web like " + StrUtil.sqlstr("%" + web + "%");
	if (!tel.equals(""))
		sql += " and tel like " + StrUtil.sqlstr("%" + tel + "%");
	if (!email.equals(""))
		sql += " and email like " + StrUtil.sqlstr("%" + email + "%");
	if (!customerType.equals(""))
		sql += " and customer_type=" + customerType;
	if (!customerRygm.equals(""))
		sql += " and rygm=" + customerRygm;
	
	if (!zczj.equals(""))
		sql += " and zczj=" + zczj;

	if (beginDate!=null) {
		sql += " and find_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and find_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}


if(!privilege.isUserPrivValid(request, "admin")){
	String salesPerson = getSalesPerson(privilege.getUser(request));
	if(!salesPerson.equals("")){
		sql += " and sales_person in ("+salesPerson+")";
	}
}


sql += " order by id desc";

// out.print(sql);

int pagesize = ParamUtil.getInt(request, "pagesize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
FormDAO fdao = new FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
long total = lr.getTotal();
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

String querystr = "op=" + op + "&kind=" + kind + "&founder=" + StrUtil.UrlEncode(founder) + "&zczj=" + zczj + "&customer=" + StrUtil.UrlEncode(customer) + "&preDate=" + preDate + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&person=" + StrUtil.UrlEncode(person) + "&web=" + web + "&tel=" + tel + "&email=" + email + "&customerType=" + customerType + "&customerRygm=" + customerRygm;
%>
<form id="form2" name="form2" action="customer_distribute.jsp" method="get">
  <table class="tabStyle_1 percent98" width="100%" border="0" align="center" cellpadding="2" cellspacing="0">
    <tbody>
      <tr>
        <td width="11%">客户名称：</td>
        <td width="29%"><input type="text" name="customer" size="20" value="<%=customer%>" />
        <input name="op" type="hidden" value="search" />
        <input name="kind" type="hidden" value="<%=kind%>" /></td>
        <td width="11%">电话：</td>
        <td width="29%"><input type="text" name="tel" size="20" value="<%=tel%>" /></td>
      </tr>
      <tr>
        <td>客户类型：</td>
        <td>
        <select id="customerType" name="customerType">
        <option value="">不限</option>
        <%
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("sales_customer_type");
        Vector vsd = sd.getOptions();
        Iterator irsd = vsd.iterator();
        while (irsd.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb)irsd.next();
            %>
            <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
            <%	
        }
        %>
        </select>
        <script>
		o("customerType").value = "<%=customerType%>";
		</script>
        </td>
        <td>人员规模：</td>
        <td><select id="customerRygm" name="customerRygm">
          <option value="">不限</option>
			<%
            sd = sm.getSelect("customer_rygm");
            vsd = sd.getOptions();
            irsd = vsd.iterator();
            while (irsd.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb)irsd.next();
                %>
                      <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
                 <%	
            }
            %>
        </select>
        <script>
		o("customerRygm").value = "<%=customerRygm%>";
		</script>
        </td>
      </tr>
      <tr>
        <td width="11%">网址：</td>
        <td width="29%"><input type="text" name="web" size="20" value="<%=web%>" /></td>
        <td width="11%">电子邮件：</td>
        <td width="29%"><input type="text" name="email" size="20" value="<%=email%>" /></td>
      </tr>
      <tr>
        <td>发现者：</td>
        <td><select id="founder" name="founder">
          <option value="">不限</option>
          <%
		Iterator uir = privilege.getUsersHavePriv("sales.user", unitCode).iterator();
		while (uir.hasNext()) {
			UserDb user = (UserDb)uir.next();
			%>
          <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
          <%
		}
		%>
        </select>
        <script>
		o("founder").value = "<%=founder%>";
		</script>        
        </td>
        <td>注册资金：</td>
        <td><select id="zczj" name="zczj">
          <option value="">不限</option>
          <%
            sd = sm.getSelect("customer_zczj");
            vsd = sd.getOptions();
            irsd = vsd.iterator();
            while (irsd.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb)irsd.next();
                %>
          <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
          <%	
            }
            %>
        </select></td>
      </tr>
      <tr>
        <td>销售员：</td>
        <td>
        <select id="person" name="person">
        <option value="">不限</option>
        <%
		uir = privilege.getUsersHavePriv("sales.user", unitCode).iterator();
		while (uir.hasNext()) {
			UserDb user = (UserDb)uir.next();
			%>
			<option value="<%=user.getName()%>"><%=user.getRealName()%></option>
			<%
		}
		%>
        </select>
        <script>
		o("person").value = "<%=person%>";
		</script>        
        </td>
        <td>发现日期：</td>
        <td>
<select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
<option selected="selected" value="">不限</option>
<%
java.util.Date[] ary = DateUtil.getDateSectOfToday();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今天</option>
<%
ary = DateUtil.getDateSectOfYestoday();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">昨天</option>
<%
ary = DateUtil.getDateSectOfCurWeek();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本周</option>
<%
ary = DateUtil.getDateSectOfLastWeek();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上周</option>
<%
ary = DateUtil.getDateSectOfCurMonth();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本月</option>
<%
ary = DateUtil.getDateSectOfLastMonth();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上月</option>
<%
ary = DateUtil.getDateSectOfQuarter();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本季度</option>
<%
ary = DateUtil.getDateSectOfCurYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今年</option>
<%
ary = DateUtil.getDateSectOfLastYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">去年</option>
<%
ary = DateUtil.getDateSectOfLastLastYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">前年</option>
<option value="*">自定义</option>
</select>
<script>
o("preDate").value = "<%=preDate%>";
</script>
<span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>">
从
<input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
至
<input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
</span>
        </td>
      </tr>
      <tr>
        <td colspan="4" align="center"><input class="btn"  type="submit" value="查  询" name="submit" /></td>
      </tr>
    </tbody>
  </table>
</form>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr align="center">
    <th width="40"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" /></th>
    <th width="300">客户名称</th>
    <th width="100">销售员</th>
    <th width="100">类型</th>
    <th width="100">规模</th>
    <th width="100">注资</th>
    <th width="100">电话</th>
    <th width="100">网址</th>
    <th width="100">发现者</th>
    <th width="100">发现日期</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			String realName = "", founderName = "";
			if (fdao.getFieldValue("sales_person")!=null && !"".equals(fdao.getFieldValue("sales_person"))) {
				UserDb user = um.getUserDb(fdao.getFieldValue("sales_person"));
				realName = user.getRealName();
			}
			if (fdao.getFieldValue("founder")!=null && !"".equals(fdao.getFieldValue("founder"))) {
				UserDb user = um.getUserDb(fdao.getFieldValue("founder"));
				founderName = user.getRealName();
			}			
		%>
  <tr align="center">
    <td width="2%">
      <input type="checkbox" id="ids" name="ids" value="<%=id%>" />
    </td>
    <td width="19%" align="left"><a target="_blank" href="customer_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("customer")%></a></td>
    <td width="9%"><%=realName%></td>
    <td width="8%"><%=sod.getOptionName("sales_customer_type", fdao.getFieldValue("customer_type"))%></td>
    <td width="8%"><%=sod.getOptionName("customer_rygm", fdao.getFieldValue("rygm"))%></td>
    <td width="8%"><%=sod.getOptionName("customer_zczj", fdao.getFieldValue("zczj"))%></td>
    <td width="8%" align="left"><%=StrUtil.getNullStr(fdao.getFieldValue("tel"))%></td>
    <td width="20%" align="left"><a href="<%=fdao.getFieldValue("web")%>" target="_blank"><%=fdao.getFieldValue("web")%></a></td>
    <td width="7%">
    <%=founderName%>
    </td>
    <td width="11%"><%=fdao.getFieldValue("find_date")%></td>
  </tr>
  <%
		}
%>
</table>
</body>
<script>
	$(function(){
		$('#beginDate').datetimepicker({
        	lang:'ch',
        	timepicker:false,
        	format:'Y-m-d',
        	formatDate:'Y/m/d'
        });
     	$('#endDate').datetimepicker({
     		lang:'ch',
     		timepicker:false,
     		format:'Y-m-d',
     		formatDate:'Y/m/d'
     	});
		flex = $("#grid").flexigrid
		(
			{
				buttons : [
			{name: '分配', bclass: '', onpress : actions}],
			/*
			searchitems : [
				{display: 'ISO', name : 'iso'},
				{display: 'Name', name : 'name', isdefault: true}
				],
			*/
			url: false,
			usepager: true,
			checkbox : false,
			page: <%=curpage%>,
			total: <%=total%>,
			useRp: true,
			rp: <%=pagesize%>,
			
			// title: "通知",
			singleSelect: true,
			resizable: false,
			showTableToggleBtn: true,
			showToggleBtn: false,
			
			onChangeSort: changeSort,
			
			onChangePage: changePage,
			onRpChange: rpChange,
			onReload: onReload,
			/*
			onRowDblclick: rowDbClick,
			onColSwitch: colSwitch,
			onColResize: colResize,
			onToggleCol: toggleCol,
			*/
			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});

function changeSort(sortname, sortorder) {
	window.location.href = "customer_distribute.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "customer_distribute.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "customer_distribute.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
	if (com=='分配')  {
		doDistribute();
	}
}
</script>
</html>
<%!
	String getSalesPerson(String userName){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(StrUtil.sqlstr(userName));
		
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		String[] depts = ud.getAdminDepts();
		DeptUserDb dud = null;
		Vector v = null;
		Iterator ir = null;
		if (depts!=null) {
			for(String dept : depts) {
				dud = new DeptUserDb();
				v = dud.list(dept);
				if(v!=null){
					ir = v.iterator();
					while(ir.hasNext()){
						dud = (DeptUserDb)ir.next();
						buffer.append(",");
						buffer.append(StrUtil.sqlstr(dud.getUserName()));
					}
				}
			}
		}
		
		return buffer.toString();
	}
%>