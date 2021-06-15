<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String curUser = ParamUtil.get(request, "curUser");
if (curUser.equals(""))
	curUser = privilege.getUser(request);

int id = ParamUtil.getInt(request, "id");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>联系人选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script>
function sel(id,sth) {
	window.opener.setIntpuObjValue(id,sth);
	window.close();
}

function selAllCheckBox(checkboxname){
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = true;
  }
  for (i=0; i<checkboxboxs.length; i++)
  {
	  checkboxboxs[i].checked = true;
  }
  }
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function sel(type) {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择！");
		return;
	}
	if (!confirm("您确定要添加么？"))
		return;
	window.location.href = "linkman_list_sel.jsp?op=sel&id=<%=id%>&ids=" + ids + "&type=" + type;
}

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "linkman_list_sel.jsp?id=<%=id%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">联系人选择</td>
    </tr>
  </tbody>
</table>
<%
// 检查计划是否已过期
FormDb fp = new FormDb();
fp = fp.getFormDb("day_work_plan");
FormDAO fdao = new FormDAO();
fdao = fdao.getFormDAO(id, fp);
String dayStr = fdao.getFieldValue("mydate");
java.util.Date d = DateUtil.parse(dayStr, "yyyy-MM-dd");
java.util.Date curD = DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
if (DateUtil.compare(curD, d)==1) {
	out.print(StrUtil.Alert("计划已过期，不能再添加！"));
    out.print("<script>");
    out.print("window.close();");
    out.print("</script>");
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("sel")) {
	String ids = ParamUtil.get(request, "ids");
	if (ids.equals("")) {
		out.print(StrUtil.Alert_Back("请先选择！"));
		return;
	}
	String type = ParamUtil.get(request, "type");
	String[] ary = StrUtil.split(ids, ",");
	String sql = "insert into form_table_day_lxr (lxr, is_visited, cws_id, contact_type, cws_creator) values (?,?,?,?,?)";
	JdbcTemplate jt = null;
	if (ary.length>0)
		jt = new JdbcTemplate();
	for (int i=0; i<ary.length; i++) {
		String xlrId = ary[i];
		jt.executeUpdate(sql, new Object[]{new Integer(StrUtil.toInt(xlrId)), "否", "" + id, type, privilege.getUser(request)});
	}
	out.print(StrUtil.Alert("操作成功！"));
    %>
      <script>
      window.opener.location.reload();
      window.opener.focus();
      window.close();
      </script>
	<%
	return;
}

String customer = ParamUtil.get(request, "customer");
String linkmanName = ParamUtil.get(request, "linkmanName");
String kind = ParamUtil.get(request, "kind");

String code = "khlb";
%>
    <form id="form2" name="form2" action="?" method="get">
      <table width="100%" border="0" align="center" cellpadding="2" cellspacing="0">
        <tbody>
          <tr>
            <td align="center"><select name="customer2" onchange="if (this.value!='') {form2.customer.value=this.value;form2.submit()}">
                <option value="">无</option>
                <%
		FormDAO fdaoCust = new FormDAO();
		Iterator ir = fdaoCust.list("sales_customer", "select id from form_table_sales_customer where sales_person=" + StrUtil.sqlstr(privilege.getUser(request)) + " order by id desc").iterator();
		while (ir.hasNext()) {
			fdaoCust = (FormDAO)ir.next();
		%>
                <option value="<%=fdaoCust.getFieldValue("customer")%>"><%=fdaoCust.getFieldValue("customer")%></option>
                <%}%>
              </select>
              <select name="kind">
			  <option value="">客户类别</option>
                <%
			SelectMgr sm = new SelectMgr();
			SelectDb sd = sm.getSelect(code);			
            Vector v = sd.getOptions(new JdbcTemplate());
            Iterator ir2 = v.iterator();
            while (ir2.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb) ir2.next();
            %>
                <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
                <%}%>
              </select>
                <script>
		  form2.kind.value = "<%=kind%>";
		  form2.customer2.value = "<%=customer%>";
		  </script>
		  <input name="op" value="search" type="hidden" />
		  <input name="id" value="<%=id%>" type="hidden" />
		  
              客户名称：
              <input name="customer" size="10" value="<%=customer%>" />
              联系人：
              <input name="linkmanName" size="10" value="<%=linkmanName%>" />
            <input  class="btn" type="submit" value="查  询" /></td>
          </tr>
        </tbody>
      </table>
    </form>
<%
String sql = "select l.id from form_table_sales_linkman l, form_table_sales_customer c where c.id=l.customer and c.sales_person=?";		
if (op.equals("search")) {
	if (!customer.equals("")) {
		sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!linkmanName.equals("")) {
		sql += " and l.linkmanName like " + StrUtil.sqlstr("%" + linkmanName + "%");
	}
	if (!kind.equals("")) {
		sql += " and c.kind=" + StrUtil.sqlstr(kind);
	}
}

sql += " order by " + orderBy + " " + sort;

// out.print(sql);

String formCode = "sales_linkman";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql, new Object[]{curUser}, curpage, pagesize);

int total = (int)jt.getTotal();

paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

String privurl = StrUtil.getUrl(request);
%>
      <table width=98% border="0" align=center cellpadding="0" cellspacing="0">
      <tr>
        <td align=right backgroun="images/title1-back.gif">
		找到符合条件的记录
      <b><%=paginator.getTotal()%></b> 条　每页显示 <b>
      <%=paginator.getPageSize()%>
      </b> 条　页次 <b>
      <%=curpage%>/<%=totalpages%></b>
      </td>
      </tr>
</table>
            <table width="98%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent98">
              <tr>
                <td width="5%" align="center" class="tabStyle_1_title"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
                <td width="17%" class="tabStyle_1_title">联系人</td>
                <td width="26%" class="tabStyle_1_title">客户</td>
                <td width="9%" class="tabStyle_1_title">是否联系</td>
                <td width="22%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('kind')">客户类别
				<%if (orderBy.equals("kind")) {
					if (sort.equals("asc")) 
						out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
					else
						out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
				}%>	
				</td>
                <td width="21%" class="tabStyle_1_title">最后联系</td>
              </tr>

<%
	  	int i = 0;
		FormDb customerfd = new FormDb();
		customerfd = customerfd.getFormDb("sales_customer");		
		FormDb linkmanfd = new FormDb();
		linkmanfd = linkmanfd.getFormDb("sales_linkman");		
	  	FormDAO fdaoCustomer = new FormDAO();
	  	FormDAO fdaoLinkman = new FormDAO();
		
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			i++;
			int lxrId = rr.getInt(1);
			// 检查是否曾经联系过
			boolean isContacted = false;
			sql = "select c.id,c.visit_date from form_table_day_work_plan w, form_table_day_lxr c where w.id=c.cws_id and w.cur_user=" + StrUtil.sqlstr(curUser) + " and c.lxr=" + lxrId + " and c.is_visited='是' order by mydate";
			ResultIterator ri2 = jt.executeQuery(sql, 1, 1);
			boolean isContact = false;
			String dt = "";
			if (ri2.hasNext()) {
				ResultRecord rr2 = (ResultRecord)ri2.next();	
				isContact = true;
				dt = DateUtil.format(rr2.getDate(2), "yyyy-MM-dd");
			}
			fdaoLinkman = fdaoLinkman.getFormDAO(lxrId, linkmanfd);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(fdaoLinkman.getFieldValue("customer")), customerfd);			
%>
              <tr>
                <td align="center">
                <input type="checkbox" name="ids" value="<%=lxrId%>" />	          	</td>
                <td><a href="../sales/linkman_show.jsp?id=<%=lxrId%>&formCode=sales_linkman" target="_blank"><%=fdaoLinkman.getFieldValue("linkmanName")%></a></td>
                <td><a href="customer_show.jsp?id=<%=fdaoLinkman.getFieldValue("customer")%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
                <td align="center">
				  <%=isContact?"是":"否"%>
				</td>
                <td align="center"><%
			String optName = "";
			sd = sm.getSelect(code);
			if (sd.getType() == SelectDb.TYPE_LIST) {
				SelectOptionDb sod = new SelectOptionDb();
				optName = sod.getOptionName(code, fdaoCustomer.getFieldValue("kind"));
			} else {
				TreeSelectDb tsd = new TreeSelectDb();
				tsd = tsd.getTreeSelectDb(fdaoCustomer.getFieldValue("kind"));
				optName = tsd.getName();
			}
			out.print(optName);		  
		  %></td>
                <td>
				  <%=dt%>				</td>
              </tr>
		<%}%>
	</table>
      <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td width="53%"><input class="btn" name="button2" type="button" onclick="sel('<%=StrUtil.UrlEncode("面访")%>')" value="面访" />
&nbsp;
      <input class="btn" name="button22" type="button" onclick="sel('<%=StrUtil.UrlEncode("电话")%>')" value="电话" /></td>
        <td width="47%" align="right">
<%
String querystr = "id=" + id + "&customer=" + StrUtil.UrlEncode(customer) + "&linkmanName=" + StrUtil.UrlEncode(linkmanName) + "&kind=" + kind;
out.print(paginator.getCurPageBlock("?" + querystr));
%>
      </td>
      </tr>
      </table>
</body>
</html>
