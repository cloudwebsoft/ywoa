<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html>
<html>
<head>
<title>工作流序列号管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=9">
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String op = ParamUtil.get(request, "op");
long begin_index = 1; 
long cur_index = 0;
String name = "";
int length = 0;
String cur_value = "";
String template = "{num}";
int seq_type = 0;
String title="添加编号";
String[] strs = new String[4];
int id = -1;

String itemSeparator = "";
int yearDigit = 4;

if(op.equals("modify")) {
	id = Integer.parseInt(ParamUtil.get(request, "id"));
	WorkflowSequenceDb wf = new WorkflowSequenceDb(id);
	begin_index = wf.getBeginIndex();
	cur_index = wf.getCurIndex();
	name = wf.getName();
	length = wf.getLength();
	cur_value = wf.getCurValue();
	template = wf.getTemplate();
	seq_type = wf.getType();
	title = "修改编号";
	
	itemSeparator = wf.getItemSeparator();
	yearDigit = wf.getYearDigit();
	
	//修改时如果是组合型，需要将“规则”状态记录。并根据此状态在“规则设置”中选定不同的按钮
	if(seq_type == 1){//表示是组合型
		String str = template;
    	int index = str.lastIndexOf("{");
    	String preString = str.substring(0, index);
    	String nextString = str.substring(index);
    	int index2 = preString.lastIndexOf("{");
    	if(index2 == -1){
    		index2 = 0;
    	}
    	String preString2 = preString.substring(0, index2);
    	strs[3] = nextString;//因为最后一个"{"肯定是{num}这一组
    	String[] strArr = preString.split("\\{");//在从前半部分截取处理
		String last = strArr[strArr.length-1];//得到数组最后一位，看符合哪一组
		String lastPreOne = "";
		String lastPreTwo = "";
		if(strArr.length == 2){
			lastPreOne = strArr[strArr.length-2];
		}else if(strArr.length > 2){
			lastPreOne = strArr[strArr.length-2];
			lastPreTwo = strArr[strArr.length-3];
		}
		if(last.equals("date:yyyy}-")){
			strs[2] = "{date:yyyy}";
			if(lastPreOne.equals("dept}-")){
				strs[1] = "{dept}";
				if(preString2.lastIndexOf("{") == -1){
					strs[0] = preString2;
				}else{
					strs[0] = preString2.substring(0, preString2.lastIndexOf("{"));
				}
			}else{
				strs[1] = "";
				if(preString.lastIndexOf("{") == -1){
					strs[0] = preString;
				}else{
					strs[0] = preString.substring(0, preString.lastIndexOf("{"));
				}
			}
		}else if(last.equals("date:yyyy-MM}-")){
			strs[2] = "{date:yyyy-MM}";
			if(lastPreOne.equals("dept}-")){
				strs[1] = "{dept}";
				if(preString2.lastIndexOf("{") == -1){
					strs[0] = preString2;
				}else{
					strs[0] = preString2.substring(0, preString2.lastIndexOf("{"));
				}
			}else{
				strs[1] = "";
				if(preString.lastIndexOf("{") == -1){
					strs[0] = preString;
				}else{
					strs[0] = preString.substring(0, preString.lastIndexOf("{"));
				}
			}
		}else if(last.equals("date:yyyy-MM-dd}-")){
			strs[2] = "{date:yyyy-MM-dd}";
			if(lastPreOne.equals("dept}-")){
				strs[1] = "{dept}";
				if(preString2.lastIndexOf("{") == -1){
					strs[0] = preString2;
				}else{
					strs[0] = preString2.substring(0, preString2.lastIndexOf("{"));
				}
			}else{
				strs[1] = "";
				if(preString.lastIndexOf("{") == -1){
					strs[0] = preString;
				}else{
					strs[0] = preString.substring(0, preString.lastIndexOf("{"));
				}
			}
		}else if(last.equals("dept}-")){
			strs[2] = "";
			strs[1] = "{dept}";
			if(preString2.lastIndexOf("{") == -1){
				strs[0] = preString2;
			}else{
				strs[0] = preString2.substring(0, preString2.lastIndexOf("{"));
			}
			
		}else{
			strs[2] = "";
			strs[1] = "";
			strs[0] = preString;
		}
	}
}
%>
<div class="spacerH"></div>
<form id="form1" action="flow_sequence_list.jsp?action=<%=op %>" method=post>
<table class="percent80" width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"><table width="556" border="0" align="center" cellspacing="0" class="tabStyle_1">
      <tr>
        <td colspan="3" align="center" class="tabStyle_1_title"><%=title %></td>
      </tr>
      <tr id="needAddTD">
        <td width="20%" align="center">编号名称</td>
        <td width="30%" align="left" id="nameWith">
        	<input name="name" value="<%=name %>">
        	<input name="beginIndex" id="beginIndex" value="<%=begin_index %>" type="hidden">
        	<input name="id" id="id" value="<%=id %>" type="hidden">
        </td>
        <%
        	String templateSetShow = "";
        	if(op.equals("modify") && seq_type == 0){
        		templateSetShow = "display:none";%>
        		<script>
        		$("#needAddTD").children("td").first().width("50%");
        		$("#needAddTD").children("td").eq(1).width("50%");
        		</script>
        	<%}
        %>
        <td width="50%" align="left" valign="top" rowspan="4" id="regulation" style="<%=templateSetShow %>">
        	<div style="width:100%;height:100%;line-height:25px;">
        		<div>规则设置：</div>
				<div>
					前缀：
					<input type="text" id="prefixValue" value='<%=strs[0]==null?"":strs[0]%>' oninput="resetTemplate()" onpropertychange="resetTemplate()">
				</div>
				<div title="每个部门单独编号">
					<input type="checkbox" id="dept" onclick="resetTemplate()">&nbsp;部门
				</div>
				<div>
					年号：
					<input type="radio" name="yearNum" value="" checked onclick="resetTemplate()">无
					<input type="radio" name="yearNum" value="{date:yyyy}" onclick="resetTemplate()">年
					<input type="radio" name="yearNum" value="{date:yyyy-MM}" onclick="resetTemplate()">年月
					<input type="radio" name="yearNum" value="{date:yyyy-MM-dd}" onclick="resetTemplate()">年月日
				</div>
				<div>
					数值重置：
					<input type="radio" name="yearNumReset" value="{num}" checked onclick="resetTemplate()">不重置
					<input type="radio" name="yearNumReset" value="{num:year}" onclick="resetTemplate()">按年
					<input type="radio" name="yearNumReset" value="{num:month}" onclick="resetTemplate()">按月
					<input type="radio" name="yearNumReset" value="{num:day}" onclick="resetTemplate()">按日
				</div>
				<%
		        	String remark = "color:red";
		        	if(op.equals("modify")){
		        		remark = "display:none;color:red";
		        	}
		        %>
				<!-- <div style="<%=remark %>">注：如果“规则”是组合型，“当前值”的设置将不起作用</div> -->
			</div>
        </td>
      </tr>
      
      <tr>
        <td align="center">当前值</td>
        <td align="left">
	        <%if(seq_type == 0){ %>
	        	<input name="curIndex" value="<%=cur_index %>">
	        <%}else if(seq_type == 1){ %>
	        	<input name="curValue" value="<%=StrUtil.HtmlEncode(cur_value)%>" readonly>
	        	<input name="curIndex" value="<%=cur_index %>" type="hidden">
	        <%} %>
        </td>
      </tr>
      <tr>
        <td align="center">规则</td>
        <td align="left">
        <input id="template" name="template" value="<%=template %>" readonly/>
        </td>
      </tr>
      <tr>
        <td align="center" title="数字长度位数，不足将在左侧补0，置为0表示不需要补位">补齐位数</td>
        <td align="left">
        <input id="length" name="length" title="数字长度位数，不足将在左侧补0，置为0表示不需要补位" value="<%=length %>" /></td>
      </tr>
      <tr>
        <td align="center" title="数字长度位数，不足将在左侧补0，置为0表示不需要补位">分隔符</td>
        <td align="left">
        <select id="itemSeparator" name="itemSeparator">
        <option value="">无</option>
        <option value="-">减号</option>
        <option value="_">下划线</option>
        <option value="|">竖线</option>
        </select>
        <script>
		o("itemSeparator").value = "<%=itemSeparator%>";
		</script>
        </td>
        <td align="left" valign="top" id="regulation2" style="<%=templateSetShow %>">
        年份位数
        <select id="yearDigit" name="yearDigit">
        <option value="4">4</option>
        <option value="2">2</option>
        </select>
        <script>
		o("yearDigit").value = "<%=yearDigit%>";
		</script>           
        </td>
      </tr>
    </table></td>
  </tr>
</table>
</form>
<div style="text-align:center;">
<input class="btn" type="button" value="确定" onclick="checkForm()">&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn" type="button" value="返回" onclick="history.back()">
</div>
</body>
<script language="javascript">
$(function(){
	<%if(op.equals("modify") && seq_type == 1){
		if(!"".equals(strs[1]) && strs[1]!=null){%>
			document.getElementById("dept").checked = true;
		<%}
		if(!"".equals(strs[2]) && strs[2]!=null){%>
			for(var i=0;i<document.getElementsByName("yearNum").length;i++){ 
				if(document.getElementsByName("yearNum")[i].value == "<%=strs[2]%>"){
					document.getElementsByName("yearNum")[i].checked = true;
					break;
				} 
			}
		<%}
		if(!"".equals(strs[3]) && strs[3]!=null){%>
			for(var i=0;i<document.getElementsByName("yearNumReset").length;i++){ 
				if(document.getElementsByName("yearNumReset")[i].value == "<%=strs[3]%>"){
					document.getElementsByName("yearNumReset")[i].checked = true;
					break;
				} 
			}
		<%}%>
	<%}%>
});

function checkForm(){
	//控件修改时,组合型不能改成数值型，需要验证
	<%if(op.equals("modify") && seq_type == 1){%>
		if(document.getElementById("template").value == "{num}"){
			alert("组合型不能改成数值型");
			return;
		}
	<%}%>
	$("#form1").submit();
}

function resetTemplate(){
	var prefix = "";
	var dept = "";
	var yearNum = "";
	var yearNumReset = "";
	var templateValue = "";
	
	prefix = document.getElementById("prefixValue").value;
	
	for(var i=0;i<document.getElementsByName("yearNum").length;i++){ 
		if(document.getElementsByName("yearNum")[i].checked){
			yearNum = document.getElementsByName("yearNum")[i].value;
			break;
		} 
	}
	for(var i=0;i<document.getElementsByName("yearNumReset").length;i++){ 
		if(document.getElementsByName("yearNumReset")[i].checked){
			yearNumReset = document.getElementsByName("yearNumReset")[i].value;
			break;
		} 
	}
	if(prefix != ""){
		templateValue += prefix;
	}
	if(document.getElementById("dept").checked){
		templateValue += "{dept}-";
	}
		
	if(yearNum !=""){
		templateValue += yearNum+"-";
	}
	templateValue += yearNumReset;
	
	document.getElementById("template").value = templateValue;
}
</script>
</html>