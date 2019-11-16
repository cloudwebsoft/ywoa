<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>公文模板管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "admin.unit")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

DocTemplateMgr lm = new DocTemplateMgr();
DocTemplateDb ld = new DocTemplateDb();
String op = ParamUtil.get(request, "op");

if (op.equals("add")) {
	try {
		if (lm.add(application, request)) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "flow_doc_template_list.jsp"));
			return;
		}
	}
	catch (ErrMsgException e) {
	e.printStackTrace();
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	return;
}
else if (op.equals("edit")) {
	try {
		if (lm.modify(application, request)) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "flow_doc_template_list.jsp"));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	return;
}
else if (op.equals("del")) {
	if (lm.del(application, request)) {
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "flow_doc_template_list.jsp"));
	}
	else {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_del"), "提示"));
	}
	return;
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">公文模板管理</td>
    </tr>
  </tbody>
</table>
<%
String myUnitCode = privilege.getUserUnitCode(request);

String action = ParamUtil.get(request, "action");
String title = ParamUtil.get(request, "title");

String sql;

if (action.equals("search") && !title.equals("")) {
	sql = "select id from " + ld.getTableName() + " where title like " + StrUtil.sqlstr("%" + title + "%") + " and unit_code=" + StrUtil.sqlstr(myUnitCode) + " order by sort asc";
	if (myUnitCode.equals(DeptDb.ROOTCODE)) {
		sql = "select id from " + ld.getTableName() + " where title like " + StrUtil.sqlstr("%" + title + "%") + " order by unit_code asc, sort asc";
	}
}
else {
	sql = "select id from " + ld.getTableName() + " where unit_code=" + StrUtil.sqlstr(myUnitCode) + " order by sort asc";
	if (myUnitCode.equals(DeptDb.ROOTCODE)) {
		sql = "select id from " + ld.getTableName() + " order by unit_code asc, sort asc";
	}
}
// System.out.println(getClass() + " sql=" +sql);

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
ListResult lr = ld.listResult(sql, curpage, pagesize);
int total1 = lr.getTotal();
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

String querystr = "action=" + action + "&title=" + StrUtil.UrlEncode(title);
%>
<br>
<table width="80%" border="0" align="center">
  <tr>
    <td width="100%" align="center">
    <form action="flow_doc_template_list.jsp" method="get">
    <label>名称：</label>
    <input id="title" name="title" value="<%=title%>" size="20" />
    &nbsp;
    <input name="action" value="search" type="hidden" />
    <input name="submit" type="submit" class="btn" value="搜索" />
    </form>
    </td>
  </tr>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" class="percent98">
  <tr><td align="right">
&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>
</td></tr></table>
<table cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1">
  <thead>
    <tr>
        <td width="3%" class="tabStyle_1_title">ID</td>
      <td width="3%" class="tabStyle_1_title">编号</td>
      <td width="13%" class="tabStyle_1_title">名称</td>
      <td width="14%" class="tabStyle_1_title">文件</td>
	  <td width="27%" class="tabStyle_1_title">部门</td>
	  <td width="10%" class="tabStyle_1_title">单位</td>
	  <td class="tabStyle_1_title" width="16%"><lt:Label key="op"/></td>
    </tr>
  </thead>
  <tbody>
<%
Iterator ir = lr.getResult().iterator();
UserMgr um = new UserMgr();
int i=100;
DeptDb dd = new DeptDb();
while (ir.hasNext()) {
 	ld = (DocTemplateDb)ir.next();
	i++;
	
	String depts = ld.getDepts();
	String deptNames = "";
	String[] ary = StrUtil.split(depts, ",");
	if (ary!=null) {
		for (int k=0; k<ary.length; k++) {
			if (deptNames.equals(""))
				deptNames = dd.getDeptDb(ary[k]).getName();
			else
				deptNames += "," + dd.getDeptDb(ary[k]).getName(); 
		}
	}
	%>
  <form name="form<%=ld.getId()%>" action="?op=edit" method="post" enctype="MULTIPART/FORM-DATA">
    <tr>
        <td align="center">
            <%=ld.getId()%>
        </td>
      <td align="center">
        <input name="sort" value="<%=ld.getSort()%>" size="3" />
      </td>
      <td><input name=title value="<%=ld.getTitle()%>"></td>
      <td class="highlight">
        <input name="filename" type="file" style="width:150px" size="10">		</td>
      <td class="highlight">
        <input type="hidden" name="depts" value="<%=depts%>">
        <textarea name="deptNames" style="width:300px; height:40px" readOnly wrap="yes" id="deptNames"><%=deptNames%></textarea>
        <input class="btn" title="添加部门" onClick="openWinDepts(<%=ld.getId()%>)" type="button" value="选择">
      </td>
      <td class="highlight">
      <%if (ld.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {%>
      公共模板
      <%}else{
	  	dd = dd.getDeptDb(ld.getUnitCode());
		out.print(dd.getName());
	  }%>
      </td>
      <td>
	  <a href="javascript:form<%=ld.getId()%>.submit()"><lt:Label key="op_edit"/></a>
	  &nbsp;&nbsp;
      <a onClick="if (!confirm('<lt:Label key="confirm_del"/>')) return false" href="flow_doc_template_list.jsp?op=del&id=<%=ld.getId()%>&<%=querystr%>&CPages=<%=curpage%>"><lt:Label key="op_del"/></a>
	  &nbsp;&nbsp;
      <a href="javascript:void(0);" onclick="editTemplate(<%=ld.getId()%>)">编辑模板</a>
      &nbsp;&nbsp;
      <a href="javascript:void(0);" onclick="openWin('flow_doc_template_getfile.jsp?id=<%=ld.getId()%>')">下载</a>
	  <input name="id" value="<%=ld.getId()%>" type="hidden"></td>
    </tr>
  </form>
<%}%>
  <form id="form0" name="form0" action="flow_doc_template_list.jsp?op=add" method="post" enctype="multipart/form-data">
    <tr class="row">
        <td align="center">

        </td>
      <td align="center">
        <input name="sort" value="0" size="3" />
      </td>
      <td><input name=title value=""></td>
      <td>
        <input type="file" name="filename" style="width:150px" size="10">
      </td>
      <td><span class="highlight">
        <input type="hidden" name="depts" />
        <textarea name="deptNames" style="width:300px; height:40px" readonly wrap="yes" id="deptNames"></textarea>
        <input class="btn" title="添加部门" onclick="openWinDepts(0)" type="button" value="选择" />
      </span></td>
      <td>
      <style>
		.unit {
			background-color:#CCC;
		}
	  </style>
		<%
		if (myUnitCode.equals(DeptDb.ROOTCODE)) {%>
            <select id="unitCode" name="unitCode" onchange="if (this.value=='') alert('请选择单位！');">
            <option value="<%=Leaf.UNIT_CODE_PUBLIC%>">-公共模板-</option>
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
      <td><INPUT class="btn" type=submit height=20 width=80 value="添加"></td>
	</form>
    </tr>
    <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
      <td colspan="7" align="left" style="PADDING-LEFT: 10px">
	  注：<br />
	  1、如果编辑时上传了文件，则会替换原来的文件<br />
	  2、如果部门为空，则表示对所有人都可见</td>
    </tr>
  </tbody>
</table>
<table width="98%" class="percent98">
      <tr>
        <td align="right"><%
			   out.print(paginator.getCurPageBlock("flow_doc_template_list.jsp?"+querystr));
			 %></td>
      </tr>
</table>
</body>
<script>
function editTemplate(id) {
	<%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	if (cfg.get("isUseNTKO").equals("true")) {%>
		openWin("flow_doc_template_edit_ntko.jsp?id=" + id, 1100, 800);	
	<%}else{%>	
		openWin('flow_doc_template_weboffice.jsp?id=' + id, 1024, 768);
	<%}%>
}

var frmId;
function openWinDepts(formId) {
	frmId = formId;
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
	if (ret==null)
		return;
	var frm = o("form" + formId);
	frm.deptNames.value = "";
	frm.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (frm.deptNames.value=="") {
			frm.depts.value += ret[i][0];
			frm.deptNames.value += ret[i][1];
		}
		else {
			frm.depts.value += "," + ret[i][0];
			frm.deptNames.value += "," + ret[i][1];
		}
	}
}

function getDepts() {
	return o("form" + frmId).depts.value;
}
</script>
</html>