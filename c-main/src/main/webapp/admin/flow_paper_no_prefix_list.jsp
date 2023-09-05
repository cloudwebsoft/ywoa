<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<!DOCTYPE html>
<html>
<title>公文文号管理</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="/oads_spark/skin/blue/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "admin.unit")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

PaperNoPrefixDb pnpd = new PaperNoPrefixDb();

if(op.equals("add")){
	try{
		PaperNoPrefixMgr pp = new PaperNoPrefixMgr();
		boolean re  = pp.create(request);
		if(re){
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_paper_no_prefix_list.jsp"));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
	}catch(Exception e){
		 e.printStackTrace();
	}
	return;
}
else if(op.equals("edit")){
	try{
		PaperNoPrefixMgr pp = new PaperNoPrefixMgr();
		boolean re  = pp.save(request);
		if(re){
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_paper_no_prefix_list.jsp"));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}		
	}catch(Exception e){
		 e.printStackTrace();
	}
	return;
}
else if(op.equals("del")){
	try{
		PaperNoPrefixMgr pp = new PaperNoPrefixMgr();
		boolean re  = pp.del(request);
		if(re){
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_paper_no_prefix_list.jsp"));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}		
	}catch(Exception e){
		 e.printStackTrace();
	}
	return;
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">公文文号前缀管理</td>
    </tr>
  </tbody>
</table>
<br>
<%
String myUnitCode = privilege.getUserUnitCode(request);

String action = ParamUtil.get(request, "action");
String what = ParamUtil.get(request, "what");

String sql;
if (action.equals("search") && !what.equals("")) {
	sql = "select id from " + pnpd.getTable().getName() + " where name like " + StrUtil.sqlstr("%" + what + "%") + " and unit_code=" + StrUtil.sqlstr(myUnitCode) + " order by orders desc";
	if (myUnitCode.equals(DeptDb.ROOTCODE)) {
		sql = "select id from " + pnpd.getTable().getName() + " where name like " + StrUtil.sqlstr("%" + what + "%") + " order by unit_code asc, orders desc";
	}
}
else {
	sql = "select id from " + pnpd.getTable().getName() + " where unit_code=" + StrUtil.sqlstr(myUnitCode) + " order by orders desc";
	if (myUnitCode.equals(DeptDb.ROOTCODE)) {
		sql = "select id from " + pnpd.getTable().getName() + " order by unit_code asc, orders desc";
	}
}

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
ListResult lr = pnpd.listResult(sql, curpage, pagesize);
long total1 = lr.getTotal();
Vector v = lr.getResult();
Iterator ir1 = null;
if (v!=null)
	ir1 = v.iterator();
paginator.init(total1, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

String querystr = "action=" + action + "&what=" + StrUtil.UrlEncode(what);
%>
<table width="80%" border="0" align="center">
  <tr>
    <td width="100%" align="center">
    <form action="flow_paper_no_prefix_list.jsp" method="get">
    <input id="what" name="what" value="<%=what%>" size="20" />
    &nbsp;
    <input name="action" value="search" type="hidden" />
    <input name="submit" type="submit" class="btn" value="搜索" />
    </form>
    </td>
  </tr>
</table>
<table width="98%" border="0" cellpadding="0" align="center" cellspacing="0" class="percent80">
  <tr><td align="right">
&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>
</td></tr></table>
<table id="mainTable" cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1 percent80">
  <thead>
    <tr>
      <td width="4%">编号</td>
      <td width="25%">名称</td>
	  <td width="37%">部门</td>
	  <td width="20%">单位</td>
	  <td width="14%">操作</td>
    </tr>
  </thead>
  <tbody>
<%
// Iterator ir = pnpd.list(sql).iterator();
Iterator ir = lr.getResult().iterator();
UserMgr um = new UserMgr();
int i = 100;
DeptDb dd = new DeptDb();
PaperNoPrefixDeptDb pnpdd = new PaperNoPrefixDeptDb();
while (ir.hasNext()) {
	pnpd = (PaperNoPrefixDb)ir.next();
	
	long id = pnpd.getLong("id");
	String name = pnpd.getString("name");
	int orders = pnpd.getInt("orders");
	
	i++;

	String depts = "";
	String deptNames = "";
	Iterator irdept = pnpdd.getDepts(id).iterator();
	while (irdept.hasNext()) {
		dd = (DeptDb)irdept.next();
		if (deptNames.equals("")) {
			depts = dd.getCode();
			deptNames = dd.getName();
		}
		else {
			depts += "," + dd.getCode();
			deptNames += "," + dd.getName();
		}
	}
	%>
  <form name="form<%=id%>" action="?op=edit" method="post" >
    <tr>
      <td align="center">
        <input name="sort" value="<%=orders%>" size="3" />
      </td>
      <td><input id=id_<%=id%> name="name" value="<%=name%>" /></td>
      <td>
        <input name="depts" value="<%=depts%>" type="hidden" />
        <textarea name="deptNames" cols="45" rows="5" readOnly wrap="yes" id="deptNames"><%=deptNames%></textarea>
        <input class="btn" title="添加部门" onClick="openWinDepts(<%=id%>)" type="button" value="选择部门">
      </td>
      <td>
      <%if (pnpd.getString("unit_code").equals(Leaf.UNIT_CODE_PUBLIC)) {%>
      公共
      <%}else{
	  	dd = dd.getDeptDb(pnpd.getString("unit_code"));
		out.print(dd.getName());
	  }%>      
      </td>
      <td align="center">
        <a href="javascript:;" onclick="addTab('<%=name%>', '<%=request.getContextPath()%>/admin/flow_paper_no_list.jsp?prefixId=<%=pnpd.getInt("id")%>')">文号</a>
        &nbsp;&nbsp;
        <a href="javascript:form<%=id%>.submit()">编辑</a>
        &nbsp;&nbsp;
        <a onClick="if (!confirm('您确定要删除么')) return false" href="flow_paper_no_prefix_list.jsp?op=del&id=<%=id%>">删除</a>
        &nbsp;&nbsp;
      <input name="id" value="<%=id%>" type="hidden">
      </td>
    </tr>
  </form>
<%}%>
  <form id="formAdd" name="formAdd" action="flow_paper_no_prefix_list.jsp?op=add" method="post" onsubmit="return formAdd_onsubmit()" >
    <tr class="row">
      <td align="center">
        <input name="sort" value="0" size="3" />
      </td>
      <td><input id="name" name="name" value=""></td>
      <td>
        <input type="hidden" name="depts" />
        <textarea name="deptNames" cols="45" rows="5" readonly wrap="yes" id="deptNames"></textarea>
        <input class="btn" title="添加部门" onclick="openWinDepts('Add')" type="button" value="选择部门" />
      </td>
      <td>
		<%
		if (myUnitCode.equals(DeptDb.ROOTCODE)) {%>
            <select id="unitCode" name="unitCode" onchange="if (this.value=='') alert('请选择单位！');">
            <option value="<%=Leaf.UNIT_CODE_PUBLIC%>">-公共-</option>
            <%
            DeptDb rootDept = new DeptDb();
            rootDept = rootDept.getDeptDb(DeptDb.ROOTCODE);
            %>
            <option value="<%=DeptDb.ROOTCODE%>"><%=rootDept.getName()%></option>
            <%
            // Iterator ir = privilege.getUserAdminUnits(request).iterator();
            ir = rootDept.getChildren().iterator();
            while (ir.hasNext()) {
              dd = (DeptDb)ir.next();
              String cls = "", val = "";
              if (dd.getType()==DeptDb.TYPE_UNIT) {
                  cls = " class='unit' ";
                  val = dd.getCode();
              }
              %>
              <option <%=cls%> value="<%=val%>">&nbsp;&nbsp;&nbsp;&nbsp;<%=dd.getName()%></option>
                <%
                Iterator ir2 = dd.getChildren().iterator();
                while (ir2.hasNext()) {
                    DeptDb dd2 = (DeptDb)ir2.next();
                    String cls2 = "", val2 = "";
                    if (dd2.getType()==DeptDb.TYPE_UNIT) {
                        cls2 = " class='unit' ";
                        val2 = dd2.getCode();
                    }
                    %>
                      <option <%=cls2%> value="<%=val2%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd2.getName()%></option>
                      <%
                      Iterator ir3 = dd2.getChildren().iterator();
                      while (ir3.hasNext()) {
                          DeptDb dd3 = (DeptDb)ir3.next();
                          String cls3 = "", val3 = "";
                          if (dd3.getType()==DeptDb.TYPE_UNIT) {
                              cls3 = " class='unit' ";
                              val3 = dd3.getCode();
                          }
                          %>
                          <!--
                            <option <%=cls3%> value="<%=val3%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd3.getName()%></option>
                              <%
                              Iterator ir4 = dd3.getChildren().iterator();
                              while (ir4.hasNext()) {
                                  DeptDb dd4 = (DeptDb)ir4.next();
                                  String cls4 = "", val4 = "";
                                  if (dd4.getType()==DeptDb.TYPE_UNIT) {
                                      cls4 = " class='unit' ";
                                      val4 = dd4.getCode();
                                  }
                                  %>
                                    <option <%=cls4%> value="<%=val4%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd4.getName()%></option>
                                      <%
                                      Iterator ir5 = dd4.getChildren().iterator();
                                      while (ir5.hasNext()) {
                                          DeptDb dd5 = (DeptDb)ir5.next();
                                          String cls5 = "", val5 = "";
                                          if (dd5.getType()==DeptDb.TYPE_UNIT) {
                                            cls5 = " class='unit' ";
                                            val5 = dd5.getCode();
                                          }
                                          %>
                                            <option <%=cls5%> value="<%=val5%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd5.getName()%></option>
                                      <%}                                
                              }
                              %>
                           -->
                      <%
                      }
                }
            }
            %>
            </select>
        <%}else{%>
        	<input name="unitCode" value="<%=myUnitCode%>" type="hidden" />
        <%
			dd = dd.getDeptDb(myUnitCode);
			out.print(dd.getName());
		}%>      
      </td>
      <td align="center"><INPUT class="btn" type=submit height=20 width=80 value="添加"></td>
	</form>
    </tr>
  </tbody>
</table>
<table align="center" width="98%" class="percent80">
      <tr>
        <td align="right"><%
			   out.print(paginator.getCurPageBlock("flow_paper_no_prefix_list.jsp?"+querystr));
			 %></td>
      </tr>
</table>
</body>
<script>
var frmId;
function openWinDepts(formId) {
	frmId = formId;
	openWin('../dept_multi_sel.jsp', 800, 600);
}

function setDepts(ret) {
    var frm = o("form" + frmId);
    frm.deptNames.value = "";
    frm.depts.value = "";
    for (var i = 0; i < ret.length; i++) {
        if (frm.deptNames.value == "") {
            frm.depts.value += ret[i][0];
            frm.deptNames.value += ret[i][1];
        } else {
            frm.depts.value += "," + ret[i][0];
            frm.deptNames.value += "," + ret[i][1];
        }
    }
}

function getDepts() {
	return o("form" + frmId).depts.value;
}

function checkExist(id) {
	var name = o("id_" + id).value;
	if (name=="")
		return;
	$.ajax({
		type: "post",
		url: "flow_paper_no_prefix_list_do.jsp",
		data : {
			op: "checkExist",
			name: name
		},
		success : function(data, status) { // 判断是否成功
			var returnData = data.trim();
			if(returnData=="true"){
    	 		alert("名称重复，请重新输入！", "提示");
    	 	}
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}

function formAdd_onsubmit() {
	if (o("formAdd").name.value=="") {
		alert("请输入名称！");
		return false;
	}
	
	if (o("formAdd").depts.value=="") {
		alert("请选择部门！");
		return false;
	}	
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>