<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int questId = ParamUtil.getInt(request, "questId", -1);
if (questId==-1) {
	return;
}
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
String sort = ParamUtil.get(request, "sort");

String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "questionnaire_priv_list.jsp?questId=<%=questId%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery.bgiframe.js"></script>
</head>
<body>
<jsp:useBean id="questPriv" scope="page" class="com.redmoon.oa.questionnaire.QuestionnairePriv"/>
<%
/*
if (!(questPriv.canUserDel(privilege.getUser(request)) || questPriv.canUserExamine(privilege.getUser(request)))) {
	out.print(StrUtil.jAlert_Back(Privilege.MSG_INVALID + " 用户需对该节点拥有删除或审核的权限！","提示"));
	return;
}
*/

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("名称不能为空！","提示"));
		return;
	}
	int type = ParamUtil.getInt(request, "type");
	String[] names = name.split("\\,");
	boolean re = false;
	for (String um : names) {
		if (type == QuestionnairePriv.TYPE_USER) {
			UserDb user = new UserDb();
			user = user.getUserDb(um);
			if (!user.isLoaded()) {
				continue;
			}
		}
		try {
			re = questPriv.add(um, type, questId);
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "questionnaire_priv_list.jsp?questId=" + questId));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败", "提示"));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		String leafCode = ParamUtil.get(request, "dirCode");
		QuestionnairePriv lp = new QuestionnairePriv();
		lp.setRoles(questId, roleCodes);
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "questionnaire_priv_list.jsp?questId=" + questId));
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	int see = 0, append=0, del=0, modify=0, examine=0, download = 0;
	String strsee = ParamUtil.get(request, "see");
	if (StrUtil.isNumeric(strsee)) {
		see = Integer.parseInt(strsee);
	}
	String strappend = ParamUtil.get(request, "append");
	if (StrUtil.isNumeric(strappend)) {
		append = Integer.parseInt(strappend);
	}
	String strmodify = ParamUtil.get(request, "modify");
	if (StrUtil.isNumeric(strmodify)) {
		modify = Integer.parseInt(strmodify);
	}
	String strdel = ParamUtil.get(request, "del");
	if (StrUtil.isNumeric(strdel)) {
		del = Integer.parseInt(strdel);
	}
	String strexamine = ParamUtil.get(request, "examine");
	if (StrUtil.isNumeric(strexamine)) {
		examine = Integer.parseInt(strexamine);
	}
	String strdownload = ParamUtil.get(request, "downLoad");
	if (StrUtil.isNumeric(strdownload)) {
		download = Integer.parseInt(strdownload);
	}
	
	int weight = ParamUtil.getInt(request, "weight", 1);
	int limitA = ParamUtil.getInt(request, "limitA", 0);
	int limitB = ParamUtil.getInt(request, "limitB", 0);
	int limitC = ParamUtil.getInt(request, "limitC", 0);
	int limitD = ParamUtil.getInt(request, "limitD", 0);
	int limitE = ParamUtil.getInt(request, "limitE", 0);
	int limitF = ParamUtil.getInt(request, "limitF", 0);
	
	String kind = ParamUtil.get(request, "kind");
	
	/*
	if (questPriv.isKindWeightExist(questId, kind, weight)) {
		out.print(StrUtil.jAlert_Back("权重相同时，种类需相同！","提示"));		
		return;
	}
	*/
	
	questPriv.setId(id);
	questPriv.setAppend(append);
	questPriv.setModify(modify);
	questPriv.setDel(del);
	questPriv.setSee(see);
	questPriv.setExamine(examine);
	questPriv.setDownLoad(download);
	
	questPriv.setWeight(weight);
	questPriv.setLimitA(limitA);
	questPriv.setLimitB(limitB);
	questPriv.setLimitC(limitC);
	questPriv.setLimitD(limitD);
	questPriv.setLimitE(limitE);
	questPriv.setLimitF(limitF);
	
	questPriv.setKind(kind);
	
	if (questPriv.save()) {
		out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "questionnaire_priv_list.jsp?questId=" + questId));
	}
	else
		out.print(StrUtil.jAlert_Back("修改失败！","提示"));
	return;
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	QuestionnairePriv lp = new QuestionnairePriv();
	lp = lp.getQuestionnairePriv(id);
	if (lp.del())
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "questionnaire_priv_list.jsp?questId=" + questId));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

String sql = "select id from oa_questionnaire_priv" + " where quest_id = " + questId + " order by " + orderBy + " " + sort;
Vector result = questPriv.list(sql);
Iterator ir = result.iterator();
%>
<br />
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="right">
    <input class="btn" name="button" type="button" onclick="javascript:location.href='questionnaire_priv_add.jsp?questId=<%=questId%>';" value="添加权限" width=80 height=20 />
	</td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellspacing="0" cellpadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" nowrap width="11%" style="cursor:pointer" onclick="doSort('name')">名称
        <%if (orderBy.equals("name")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>	  
	  </td>
      <td class="tabStyle_1_title" nowrap width="9%" style="cursor:pointer" onclick="doSort('priv_type')">类型<span class="right-title" style="cursor:pointer">
        <%if (orderBy.equals("priv_type")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>
      </span></td>
      <td class="tabStyle_1_title" nowrap width="7%">种类</td>
      <td class="tabStyle_1_title" nowrap width="5%">参与</td>
      <td class="tabStyle_1_title" nowrap width="7%">投票看结果</td>
      <td class="tabStyle_1_title" nowrap width="7%">查看结果</td>
      <td class="tabStyle_1_title" nowrap width="5%">权重</td>
      <td class="tabStyle_1_title" nowrap width="6%">A</td>
      <td class="tabStyle_1_title" nowrap width="6%">B</td>
      <td class="tabStyle_1_title" nowrap width="6%">C</td>
      <td class="tabStyle_1_title" nowrap width="6%">D</td>
      <td class="tabStyle_1_title" nowrap width="6%">E</td>
      <td class="tabStyle_1_title" nowrap width="6%">F</td>
      <td width="14%" nowrap class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
while (ir.hasNext()) {
 	QuestionnairePriv lp = (QuestionnairePriv)ir.next();
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="questionnaire_priv_list.jsp?op=modify" method=post>
    <tr class="highlight" id="tr<%=i%>">
      <td>
      <%
	  if (lp.getType()==QuestionnairePriv.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		out.print(ud.getRealName());
	  }else if (lp.getType()==QuestionnairePriv.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(lp.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (lp.getType()==QuestionnairePriv.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(lp.getName());
	  	out.print(ug.getDesc());
	  }
	  %>
	  <input type=hidden name="id" value="<%=lp.getId()%>" />
      <input type=hidden name="questId" value="<%=questId%>" />
	  </td>
      <td align="center">
        <%
	  if (lp.getType()==QuestionnairePriv.TYPE_USER) {
	  	%>
        用户
        <%
	  }else if (lp.getType()==QuestionnairePriv.TYPE_ROLE) {
	  	%>
        角色
        <%
	  }
	  else if (lp.getType()==QuestionnairePriv.TYPE_USERGROUP) {
	  	%>
        用户组
        <%
	  }
	  %>      
      </td>
      <td align="center">
      <input id="kind" name="kind" value="<%=lp.getKind()%>" size="5" />
      </td>
      <td align="center">
        <input id="see" name="see" type=checkbox <%=lp.getSee()==1?"checked":""%> value="1" onclick="checkPrivSee('tr<%=i%>')" />
        <span style="display:none">
        <input name="del" type=checkbox <%=lp.getDel()==1?"checked":""%> value="1" onclick="checkPrivDel('tr<%=i%>')" />
        <input name="downLoad" title="下载附件" type=checkbox <%=lp.getDownLoad()==1?"checked":""%> value="1" onclick="checkPrivModify('tr<%=i%>')" /> 
        <input name="examine" type=checkbox <%=lp.getExamine()==1?"checked":""%> value="1" onclick="checkPrivExamine('tr<%=i%>')" />
        </span>
        </td>
      <td align="center">
        <input name="append" type=checkbox <%=lp.getAppend()==1?"checked":""%> value="1" onclick="checkPrivAppend('tr<%=i%>')" /> 
      </td>
      <td align="center">
        <input name="modify" type=checkbox <%=lp.getModify()==1?"checked":""%> value="1" onclick="checkPrivModify('tr<%=i%>')" /> 
      </td>
      <td>
      <input name="weight" value="<%=lp.getWeight()%>" size="3" />
      </td>
      <td><input name="limitA" value="<%=lp.getLimitA()%>" size="6" /></td>
      <td><input name="limitB" value="<%=lp.getLimitB()%>" size="6" /></td>
      <td><input name="limitC" value="<%=lp.getLimitC()%>" size="6" /></td>
      <td><input name="limitD" value="<%=lp.getLimitD()%>" size="6" /></td>
      <td><input name="limitE" value="<%=lp.getLimitE()%>" size="6" /></td>
      <td><input name="limitF" value="<%=lp.getLimitF()%>" size="6" /></td>
      <td align="center">
	  <input class="btn" type=submit value="修改" />
	  &nbsp;<input class="btn" type=button onclick="jConfirm('您确定要删除吗?','提示',function(r){if(!r){return;}else{window.location.href='questionnaire_priv_list.jsp?op=del&questId=<%=questId%>&id=<%=lp.getId()%>' }}) " value="删除" /></td>
    </tr></form>
<%}%>     
</table>
<div style="margin-left:20px;">
注：票数=权重*得票数，A表示A项最大允许选择的个数，0表示不限，BCDEF亦相同。
<!--相同权重的票，将会被视为同一种类-->
</div>
<br />
</body>
<script>
function checkPrivSee(trId) {
	var isChecked = $("#" + trId + " input[name='see']").attr("checked");
	if (!isChecked) {
		$("#" + trId + " input[name='append']").attr("checked", false);
		$("#" + trId + " input[name='del']").attr("checked", false);
		$("#" + trId + " input[name='modify']").attr("checked", false);
		// $("#" + trId + " input[name='examine']").attr("checked", false);
	}
}

function checkPrivAppend(trId) {
	var isChecked = $("#" + trId + " input[name='append']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivDel(trId) {
	var isChecked = $("#" + trId + " input[name='del']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivModify(trId) {
	var isChecked = $("#" + trId + " input[name='modify']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivExamine(trId) {
	var isChecked = $("#" + trId + " input[name='examine']").attr("checked");
	if (isChecked) {
		// $("#" + trId + " input[name='see']").attr("checked", true);
		// $("#" + trId + " input[name='append']").attr("checked", true);
		// $("#" + trId + " input[name='del']").attr("checked", true);
		// $("#" + trId + " input[name='modify']").attr("checked", true);
	}
}
</script>
</html>