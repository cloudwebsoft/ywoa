<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
/*
- 功能描述：当分支线上存在条件時，匹配分支上的用户
- 访问规则：flow_dispose.jsp中的op=saveformvalueBeforeXorCondSelect时ajax调用
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2011.11.14
- 修改原因：
如果分支线上有title为空的条件，原来的处理方式是取最后一个空的分支线为默认分支线，如果没有其它条件满足，则以该默认分支线为准
现改为：如果仅有一个title为空的分支线，则以该分支线作为默认分支线
如果有两个以上的条件为空的分支线，则让用户选择走哪一条分支
- 修改点：
if (hasCond && !isCondSatisfied) 判断中原来只是提示不满足条件...，修改后，加入了title为空的分支线的处理，匹配分支线节点上的用户
当节点上仅有一个用户时，改为如果非异或发散时，才置WorkflowAction_***复选框为勾选状态，因为原来的置XorActionSelected复选框为选中状态的脚本在jquery-ui dialog中无效
发现异常情况：当条件分支仅有一个满足条件，而节点上的用户仅有一个，如果不选用户，则流程也能流转，可能是因为matchActionUser时，节点上WorkflowActionDb中只有一个username，所以检测通过
*/

String op = ParamUtil.get(request, "op");

// 列出符合条件的用户
Privilege privilege = new Privilege();
UserMgr um = new UserMgr();
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
WorkflowDb wf = new WorkflowDb();

int actionId = ParamUtil.getInt(request, "actionId");
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);

WorkflowRuler wr = new WorkflowRuler();

long myActionId = ParamUtil.getLong(request, "myActionId");
MyActionDb mad = new MyActionDb();
mad = mad.getMyActionDb(myActionId);

if (mad.getActionStatus() == WorkflowActionDb.STATE_PLUS) {
	return;
}

wf = wf.getWorkflowDb(wa.getFlowId());
WorkflowPredefineDb wfp = new WorkflowPredefineDb();
wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

Vector vto = wa.getLinkToActions();
boolean flagXorRadiate = wa.isXorRadiate();

Iterator toir = vto.iterator();

// 如果在后继节点的连接线上存在条件，则判别是否有符合条件的分支，如果有满足条件的，则自动运行，注意条件分支中应只有一个分支满足条件
// 鉴别模型 Discriminator Choice
Vector vMatched = null;
StringBuffer condBuf = new StringBuffer();
if (flagXorRadiate) {
  try {
	  request.setAttribute("myActionId", myActionId);
	  vMatched = wa.matchNextBranch(request, condBuf);
  }
  catch (ErrMsgException e) {
	  out.print("<font color=red>" + e.getMessage() + "</font><BR>");
  }
}

boolean isCondSatisfied = (vMatched!=null && vMatched.size()>0)?true:false;
// System.out.println(getClass() + " isCondSatisfied=" + isCondSatisfied + "   " + linkMatched.getFlowId());
String conds = condBuf.toString();
boolean hasCond = conds.equals("")?false:true; // 是否含有条件
int askType = ParamUtil.getInt(request, "askType", 0);
if (hasCond) {
%>
<script>
  hasCond = true;
</script>
<%
}
%>
<script>
<%
if (flagXorRadiate)
  out.println("isXorRadiate=true;");
else
  out.println("isXorRadiate=false;");
if (isCondSatisfied)
  out.println("isCondSatisfied=true;");
else
  out.println("isCondSatisfied=false;");
%>
</script>
<%
if (hasCond && !op.equals("matchNextBranch")) {
	if (!op.equals("matchAfterSelDept")) { // 选择兼职部门
		// fgf 20161009使当有条件时，不显示条件的相关信息，因为在提交后还会再次提示选择用户，以免引起混淆
		return;
	}
}
// System.out.println(getClass() + " hasCond=" + hasCond + " isCondSatisfied=" + isCondSatisfied);
	
// 如果没有满足条件的，则显示默认分支
if (hasCond && !isCondSatisfied) {
  // 2011-11-14 找出条件为空的分支，以选择用户
  int q = 0;
  WorkflowLinkDb wld = new WorkflowLinkDb();
  int isMultiDeptCount = 0;
  while (askType == 1 && toir.hasNext()) {
	  WorkflowActionDb towa = (WorkflowActionDb)toir.next();

	  boolean canSelUser = wr.canUserSelUser(request, towa);

	  wld = wld.getWorkflowLinkDbForward(wa, towa);
      // @task:是否该改为condType为-1（不需要，因为title中存储的是条件，cond_desc中才是描述
	  // 过滤掉非默认分支
	  if (!wld.getTitle().trim().equals(""))
	  	continue;
	  q ++;
	  String color = "";
  %>
  	  <div style="clear:both; margin-top:5px">
	  <span style="display:none">
	  <input name="XorActionSelected" type="checkbox" id="XOR<%=towa.getId()%>" value="<%=towa.getInternalName()%>" />
	  </span>
  <%
  	  String actionJobName = "&nbsp;"; // "&nbsp;-&nbsp;" + towa.getJobName(); 角色

	  out.print("<image src='" + request.getContextPath() + "/images/flow_action.gif' align='absmiddle' />&nbsp;&nbsp;<font color='" + color + "'><b>" + towa.getTitle() + actionJobName + "</b></font>：<BR>");
	  // 如果是预设的是用户，而不是角色
	  if (towa.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT) || towa.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
	  %>
		  <span id=spanUsers_<%=towa.getId()%>>
		  <span id="checker">
		  <%
		  String uName = towa.getUserName();
		  if (!uName.equals("")) {
			  String[] nameAry = StrUtil.split(uName, ",");
			  int aryLen = nameAry.length;
			  for (int k=0; k<aryLen; k++) {%>
			  	<span name="WorkflowAction_span_<%=towa.getId()%>">
				  <input name="WorkflowAction_<%=towa.getId()%>" value="<%=nameAry[k]%>" type="checkbox" checked><%=um.getUserDb(nameAry[k]).getRealName()%>&nbsp;&nbsp;&nbsp;&nbsp;
			  	</span>
			  <%}
		  }
		  %>
		  </span>
		  </span>
		  <input class="btn" type="button" value="选择用户" onClick="OpenModifyWin('<%=towa.getInternalName()%>', '<%=towa.getId()%>', 'true')">
	  <%}
	  else {
		  Iterator userir = null;
		  int userCount = 0;
		  try {
			  // Vector vuser = towa.matchActionUser(towa, wa);
			  String deptOfUserWithMultiDept = ParamUtil.get(request, "deptOfUserWithMultiDept");
			  Vector vuser = towa.matchActionUser(request, towa, wa, false, deptOfUserWithMultiDept);			  
			  userir = vuser.iterator();
			  userCount = vuser.size();
		  }
		  catch (ErrMsgException e) {
			  out.print("<font color=red>" + e.getMessage() + "</font>");
			  return;
		  }
		  catch (MatchUserException e) {
		 	if (isMultiDeptCount==0) {
			  DeptUserDb du = new DeptUserDb();
			  Vector vu = du.getDeptsOfUser(mad.getUserName());
			  Iterator irdu = vu.iterator();
			  %>
			  请选择您所在的部门&nbsp;
			  <%
			  DeptMgr dm = new DeptMgr();
			  while (irdu.hasNext()) {
				  DeptDb mdd = (DeptDb)irdu.next();
				  String deptName = "";
				  if (!mdd.getParentCode().equals(DeptDb.ROOTCODE) && !mdd.getCode().equals(DeptDb.ROOTCODE)) {					
					  deptName = dm.getDeptDb(mdd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + mdd.getName();
				  }
				  else {
					  deptName = mdd.getName();		
				  }		  
			  %>
			  <input type="radio" id="deptOfUserWithMultiDept" name="deptOfUserWithMultiDept" value="<%=mdd.getCode()%>" onclick="onSelDept(this.value)" /><%=deptName%>&nbsp;&nbsp;
			  <%}
			  isMultiDeptCount++;
			 }
			 %>
			 <script>spanNextUser.style.display = "";</script>
			 <%
		  }
		  String[] userSelected = StrUtil.split(towa.getUserName(), ",");
		  if (userSelected == null)
			  userSelected = new String[0];
		  int userSelectedLen = userSelected.length;
		  int nodeMode = WorkflowActionDb.NODE_MODE_ROLE_SELECTED;
		  if (towa.getNodeMode()==towa.NODE_MODE_USER_SELECTED || towa.getNodeMode()==towa.NODE_MODE_USER)
			  nodeMode = towa.NODE_MODE_USER_SELECTED;
		  %>
		  <span id=spanUsers_<%=towa.getId()%>>          
		  <span id="checker">
		  <%
		  while (userir!=null && userir.hasNext()) {
			  UserDb ud = (UserDb)userir.next();
			  String checked = (!flagXorRadiate && userCount==1)?"checked":"";
	  %>
			  <span name="WorkflowAction_span_<%=towa.getId()%>" style="display:block; width:90px; float:left">
              <input name="WorkflowAction_<%=towa.getId()%>" onclick="checkXOR(this,'<%=towa.getId()%>')" type="checkbox" <%=checked%> value="<%=ud.getName()%>">
			  <%=ud.getRealName()%>
              </span>
			  <%
			  /* 当ajax时，脚本可能不一定会生效
			  if (userCount==1) {%>
				  <script>
				  // 如果只有一个人，则使异或隐藏选择框被选中
				  if (o("XOR<%=towa.getId()%>")) {
					  o("XOR<%=towa.getId()%>").checked = true;
				  }
				  </script>
			  <%}
			  */%>
			  <%
		  }
		  %>
		  </span>
          </span>
          <%		  
		  if (canSelUser) {%>
	          <input class="btn" type="button" value="选择用户" onClick="OpenModifyWin('<%=towa.getInternalName()%>', '<%=towa.getId()%>', '<%=flagXorRadiate?"true":"false"%>')">
		  <%}
	  }
	  WorkflowLinkDb wld2 = wld.getWorkflowLinkDbForward(wa, towa);
	  if (wld2.getExpireHour()!=0) {
	  %>
		  完成时间：<%=wld2.getExpireHour()%>&nbsp;
		  <%
		  String flowExpireUnit = cfg.get("flowExpireUnit");
		  if (flowExpireUnit.equals("day"))
			  out.print("天");
		  else
			  out.print("小时");			
		  %>
	  <%}%>
  <%}
  %>
  </div>
  <%
  if (q==0) {
	  if (askType == 1) {
  		out.print("条件 " + StrUtil.toHtml(conds) + " 不匹配，请注意填写是否正确，重新填写后请点击提交按钮！");
	  } else {
		  out.print("当前节点为条件分支，尚未匹配，请正确填写表单，提交后选择下一节点的处理人！");
	  }
  }
}
else {
  // 如果满足条件，或不存在條件
  int q = 0;
  boolean isMatchUserException = false;
  String deptOfUserWithMultiDept = ParamUtil.get(request, "deptOfUserWithMultiDept");
  %>
  <input type="hidden" name="deptOfUserWithMultiDept" value="<%=deptOfUserWithMultiDept%>" />
  <table style="clear:both; margin-top:5px">  
  <%
  WorkflowLinkDb wld = new WorkflowLinkDb();	
  while (toir.hasNext()) {
	  WorkflowActionDb towa = (WorkflowActionDb)toir.next();
	  // 在出现节点所设用户的同时，能否选择用户
	  boolean canSelUser = wr.canUserSelUser(request, towa);
	  
	  q ++;
	  String color = "";
	  %>
      <tr id='actionTr<%=q%>'>
      <td width="120px" valign="top" align="center">  
	  <%
	  // 如果本节点是异或发散节点
	  if (flagXorRadiate) {
		  // 如果有节点被忽略，则说明有后继节点被选中，但要注意全部被忽略的情况
		  if (towa.getStatus()==WorkflowActionDb.STATE_IGNORED) {
			  color = "#99CCCC";
		  }

		  // 如果存在条件，且匹配结果不为空
		  if (hasCond) {
			  if (isCondSatisfied) {
				  // 找到分支上满足条件的节点
				  Iterator irMatched = vMatched.iterator();
				  boolean isTowaMatched = false;
				  while (irMatched.hasNext()) {
				  	WorkflowLinkDb linkMatched = (WorkflowLinkDb)irMatched.next();
					if (towa.getInternalName().equals(linkMatched.getTo())) {
						isTowaMatched = true;
						break;
					}
				  }
				  if (isTowaMatched) {
					  // 匹配到的分支线
  %>
					<input name="XorActionSelected" type="checkbox" checked style="display:none" value="<%=towa.getInternalName()%>">
  <%
				  }
				  else {
					  // 条件不满足，则继续寻找，不显示待选节点
					  %>
					  <script>
					  o("actionTr<%=q%>").style.display = "none"; 
					  </script>
					  <%
					  out.print("</td></tr>");
					  continue;
				  }
			  }
		  }
		  else {
			  // 异或发散处理
  %>
			<input name="XorActionSelected" style="display:none" type="checkbox" id="XOR<%=towa.getId()%>" value="<%=towa.getInternalName()%>" />
  <%	  }
	  }
	  
	  String inputType = "checkbox";
	  // 如果是子流程，则只允许选择一个用户，因为流程发起人只能是一个
	  if (towa.getKind()==WorkflowActionDb.KIND_SUB_FLOW) {
		  inputType = "radio";
	  }
	  
  	  String actionJobName = "&nbsp;"; // "&nbsp;-&nbsp;" + towa.getJobName(); 角色	  
	  if (towa.getJobName().equals("自选用户")) {
	  	actionJobName = "";
	  }
	  // out.print("<image src='" + request.getContextPath() + "/images/flow_action.gif' align='absmiddle' />&nbsp;&nbsp;<font color='" + color + "'>" + towa.getTitle() + actionJobName + "</font>：");
	  out.print("<font color='" + color + "'>" + towa.getTitle() + actionJobName + "</font>：");
	  %>
      </td>
      <td valign="top">
	  <%
	  // 自选用户
	  if (towa.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT) || towa.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
	  %>
		  <span id=spanUsers_<%=towa.getId()%>>
		  <%
		  String uName = towa.getUserName();
		  if (!uName.equals("")) {
			  String[] nameAry = StrUtil.split(uName, ",");
			  int aryLen = nameAry.length;
			  for (int k=0; k<aryLen; k++) {%>
              	<span name="WorkflowAction_span_<%=towa.getId()%>" class="checkerUser">
              	<%if (flagXorRadiate) {%>
				  <input name="WorkflowAction_<%=towa.getId()%>" value="<%=nameAry[k]%>" type="checkbox" onclick="checkXOR(this,'<%=towa.getId()%>')"><%=um.getUserDb(nameAry[k]).getRealName()%>&nbsp;&nbsp;&nbsp;&nbsp;                
                <%}
                else {
					// 如果是下达模式，则其他人已选的用户不能被变动
					if (towa.isStrategyGoDown()) {
						%>
                        <input type="checkbox" checked disabled />
				  		<input style="display:none" name="WorkflowAction_<%=towa.getId()%>" value="<%=nameAry[k]%>" type="checkbox" checked>
						<%
					}
					else {
                %>
				  		<input name="WorkflowAction_<%=towa.getId()%>" value="<%=nameAry[k]%>" type="checkbox" checked>
			  	<%	}
					%>
						<%=um.getUserDb(nameAry[k]).getRealName()%>&nbsp;&nbsp;&nbsp;&nbsp;
					<%
				}%>
			  	</span>
			  <%}
		  }
		  %>
		  </span>
		  <input class="btn" type="button" value="选择用户" onClick="OpenModifyWin('<%=towa.getInternalName()%>', '<%=towa.getId()%>', '<%=flagXorRadiate?"true":"false"%>')">
	  <%}
	  else if ((wa.getStatus() == WorkflowActionDb.STATE_RETURN || wfp.isReactive()) && StrUtil.getNullStr(towa.getJobCode()).startsWith(WorkflowActionDb.PRE_TYPE_FIELD_USER)) {
		  continue;
	  } else {
		  Iterator userir = null;
		  int userCount = 0;
		  try {
			  // 当异或发散不带有条件时，如果存在兼职且已选择了所在部门时
			  Vector vuser = towa.matchActionUser(request, towa, wa, false, deptOfUserWithMultiDept);
			  userir = vuser.iterator();
			  
			  // 2014-02-09 fgf 于集群修改为允许重复修改，比如：政府领导循环走回时，再送至节点上其它领导
			  /*
			  // 如果towa中原来已有用户，则只保留原来的用户
			  String uName = towa.getUserName();
			  if (!uName.equals("")) {
				  String[] nameAry = StrUtil.split(uName, ",");
				  int aryLen = nameAry.length;
				  while (userir.hasNext()) {
					  UserDb user = (UserDb)userir.next();
					  boolean isFound = false;
					  for (int k=0; k<aryLen; k++) {
						  if (nameAry[k].equals(user.getName())) {
						  	isFound = true;
							break;
						  }
					  }
					  if (!isFound) {
					  	userir.remove();
					  }
				  }
			  }
			  */
			  
			  userir = vuser.iterator();
			  userCount = vuser.size();
		  }
		  catch (ErrMsgException e) {
			  // 需补齐标签，否则页面显示会异常
			  out.print("<font color=red>" + e.getMessage() + "</font></td></tr></table>");
			  return;
		  }
		  catch (MatchUserException e) {
			  isMatchUserException = true;
		  }
		  /*
		  String[] userSelected = StrUtil.split(towa.getUserName(), ",");
		  if (userSelected == null)
			  userSelected = new String[0];
		  int userSelectedLen = userSelected.length;
		  */
		  int nodeMode = WorkflowActionDb.NODE_MODE_ROLE_SELECTED;
		  if (towa.getNodeMode()==towa.NODE_MODE_USER_SELECTED || towa.getNodeMode()==towa.NODE_MODE_USER)
			  nodeMode = towa.NODE_MODE_USER_SELECTED;
		  %>
		  <span id=spanUsers_<%=towa.getId()%>>          
		  <span id="checker">
		  <%
		  while (userir!=null && userir.hasNext()) {
			  UserDb ud = (UserDb)userir.next();
			  if (ud == null || !ud.isLoaded()) {
				  userCount--;
				  continue;
			  }
			  %>
			  <span name="WorkflowAction_span_<%=towa.getId()%>" class="checkerUser">
			  <%
			  // 如果含条件，且分支节点上仅一个用户满足条件，则置为checked
			  // 否则仅异或发散时，需要依靠click事件去选分支
			  if (hasCond && userCount==1) {
				  %>
                  <input name="WorkflowAction_<%=towa.getId()%>" style="display:none" onclick="checkXOR(this,'<%=towa.getId()%>')" type="checkbox" checked value="<%=ud.getName()%>">
				  <input type="checkbox" checked disabled />
				  <%
			  }
			  else {
				  // 如果不是异或发散，且节点上用户唯一，则选中
				  // String checked = (!flagXorRadiate && userCount==1)?"checked":"";
				  if (!flagXorRadiate) {
				  	if ( !towa.isStrategySelectable() || userCount==1) {
					  %>
                      <input name="WorkflowAction_<%=towa.getId()%>" style="display:none" onclick="checkXOR(this,'<%=towa.getId()%>')" type="checkbox" checked value="<%=ud.getName()%>">
                      <input type="checkbox" checked disabled />
					  <%
					}
					else {
						String checked = towa.isStrategySelected() ? "checked" : "";
					%>
                      <input name="WorkflowAction_<%=towa.getId()%>" <%=checked %> onclick="checkXOR(this,'<%=towa.getId()%>')" type="<%=inputType%>" value="<%=ud.getName()%>">					
					<%
					}
				  }
				  else {
				  	if (!towa.isStrategySelectable()) {%>
                      <input name="WorkflowAction_<%=towa.getId()%>" style="display:none" onclick="checkXOR(this,'<%=towa.getId()%>')" type="<%=inputType%>" checked value="<%=ud.getName()%>">
                      <input type="checkbox" checked disabled />                      
				  	<%}
				  	else {%>
                      <input name="WorkflowAction_<%=towa.getId()%>" onclick="checkXOR(this,'<%=towa.getId()%>')" type="<%=inputType%>" value="<%=ud.getName()%>">
				  <%
				  	}
				  }
			  }
	  		  %>
			  <%=ud.getRealName()%>
              </span>
			  <%
		  }
		  %>
		  </span>
          </span>
          <%if (canSelUser) {%>
          		<%if (hasCond) {%>
          		<input name="XorActionSelected" type="checkbox" id="XOR<%=towa.getId()%>" value="<%=towa.getInternalName()%>" />
          		<%} %>
	          <input class="btn" type="button" value="选择用户" onClick="OpenModifyWin('<%=towa.getInternalName()%>', '<%=towa.getId()%>', '<%=flagXorRadiate?"true":"false"%>')">
		  <%
		  }
	  }
	  WorkflowLinkDb wld2 = wld.getWorkflowLinkDbForward(wa, towa);
	  if (wld2.getExpireHour()!=0) {
	  %>
		  完成时间：<%=wld2.getExpireHour()%>&nbsp;
		  <%
		  String flowExpireUnit = cfg.get("flowExpireUnit");
		  if (flowExpireUnit.equals("day"))
			  out.print("天");
		  else
			  out.print("小时");			
		  %>
	  <%}%>
      </td>
      </tr>
  <%}%>
  </table>
<%
  if (isMatchUserException) {
	  DeptUserDb du = new DeptUserDb();
	  Vector vu = du.getDeptsOfUser(mad.getUserName());
	  Iterator irdu = vu.iterator();
	  %>
	  请选择您所在的部门&nbsp;
	  <%
	  DeptMgr dm = new DeptMgr();
	  while (irdu.hasNext()) {
		  DeptDb mdd = (DeptDb)irdu.next();
		  String deptName = "";
		  if (!mdd.getParentCode().equals(DeptDb.ROOTCODE) && !mdd.getCode().equals(DeptDb.ROOTCODE)) {					
			  deptName = dm.getDeptDb(mdd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + mdd.getName();
		  }
		  else {
			  deptName = mdd.getName();		
		  }			  
	  %>
		  <input type="radio" id="deptOfUserWithMultiDept" name="deptOfUserWithMultiDept" value="<%=mdd.getCode()%>" onclick="onSelDept(this.value)" /><%=deptName%>&nbsp;&nbsp;
	  <%}%>
	  <script>spanNextUser.style.display = "";</script>
	  <%
  }
}

if (vto.size()==1) {
	// 检查是否延迟，且可以更改时间
	WorkflowActionDb nwa = (WorkflowActionDb)vto.elementAt(0);
	if (nwa.isDelayed()) {
		%>
        <div style="margin-top:10px">
        <img src="images/job.png" align="absmiddle" />&nbsp;延时动作：<%=nwa.getTitle()%>&nbsp;-&nbsp;<%=nwa.getJobName()%>
		<%
		if (nwa.isCanPrivUserModifyDelayDate()) {
			%>
            &nbsp;&nbsp;
            <input title="是否延迟" type="checkbox" id="isDelayed" name="isDelayed" value="1" checked />
            延迟
            <input id="timeDelayedValue" name="timeDelayedValue" size="3" value="<%=nwa.getTimeDelayedValue()%>" />
            <select id="timeDelayedUnit" name="timeDelayedUnit">
            <option value="<%=WorkflowActionDb.TIME_UNIT_DAY%>">天</option>
            <option value="<%=WorkflowActionDb.TIME_UNIT_HOUR%>">小时</option>
            <option value="<%=WorkflowActionDb.TIME_UNIT_WORKDAY%>">工作日</option>
            <option value="<%=WorkflowActionDb.TIME_UNIT_WORKHOUR%>">工作小时</option>
            </select>
            <script>
			o("timeDelayedUnit").value = "<%=nwa.getTimeDelayedUnit()%>";
			</script>
			<%
		}
		else {
			%>
			延迟<%=nwa.getTimeDelayedValue()%>&nbsp;<%=WorkflowActionDb.getTimeUnitDesc(nwa.getTimeDelayedValue())%>
			<%
		}
		%>
        </div>
		<%
	}
}

// 如果匹配成功了，则flowString会被改写，因此这儿要重新获取一下以刷新Designer中的视图
WorkflowMgr wfm = new WorkflowMgr();
wf = wfm.getWorkflowDb(wa.getFlowId());
%>
<textarea id="textareaFlowString" style="display:none"><%=wf.getFlowString()%></textarea>