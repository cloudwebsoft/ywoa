<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<%
Privilege privilege = new Privilege();

String op = ParamUtil.get(request, "op");
if (op.equals("delAttach")) {
	int doc_id = ParamUtil.getInt(request, "doc_id");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	int page_num = ParamUtil.getInt(request, "page_num", 1);
	Document doc = new Document();
	doc = doc.getDocument(doc_id);
	DocContent dc = doc.getDocContent(page_num);
	Attachment att = new Attachment(attach_id);
	String fieldName = att.getFieldName();
	boolean re = dc.delAttachment(attach_id);
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	if (re) {
		// 清空对应的field中的值
		if (fieldName!=null && !"".equals(fieldName)) {
			int flowId = ParamUtil.getInt(request, "flowId", -1);
			if (flowId != -1) {
				WorkflowDb wf = new WorkflowDb();
				wf = wf.getWorkflowDb(flowId);
				Leaf lf = new Leaf();
				lf = lf.getLeaf(wf.getTypeCode());
				FormDAO fdao = new FormDAO();
				fdao = fdao.getFormDAO(flowId, new FormDb(lf.getFormCode()));
				fdao.setFieldValue(fieldName, "");
				fdao.save();
			}
		}
		out.print("{\"re\":\"true\", \"msg\":\""+str+"\"}");
	}
	else
		out.print("{\"re\":\"false\", \"msg\":\""+str+"\"}");
	return;
}

String myname = privilege.getUser( request );
UserMgr um = new UserMgr();
UserDb myUser = um.getUserDb(myname);
int flowId;
int myActionId = ParamUtil.getInt(request, "myActionId", -1);
MyActionDb mad = new MyActionDb();
if (myActionId!=-1) {
	mad = mad.getMyActionDb((long)myActionId);
	if (!mad.isLoaded()) {
		String str = LocalUtil.LoadString(request,"res.flow.Flow","toDoProcess");
		out.print(SkinUtil.makeErrMsg(request, str));
		return;
	}
	if (!mad.getUserName().equalsIgnoreCase(myname) && !mad.getProxyUserName().equalsIgnoreCase(myname)) {
		// 权限检查
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	flowId = (int)mad.getFlowId();
}
else {
	// 当查看@流程时
	flowId = ParamUtil.getInt(request, "flowId", -1);
}
	
WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb(flowId);

WorkflowActionDb wa = new WorkflowActionDb();
if (myActionId!=-1) {
	int actionId = (int)mad.getActionId();
	wa = wa.getWorkflowActionDb(actionId);
	if ( wa==null || !wa.isLoaded()) {
		String str = LocalUtil.LoadString(request,"res.flow.Flow","actionNotExist");
		out.print(SkinUtil.makeErrMsg(request, str));
		return;
	}
	String flag = wa.getFlag();
}

int doc_id = wf.getDocId();
Document doc = new Document();
doc = doc.getDocument(doc_id);
if (doc!=null) {
	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
	wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
	
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
	
    boolean isLight = wpd.isLight();
	java.util.Vector attachments = doc.getAttachments(1);
    WorkflowAnnexAttachment wfaa = new WorkflowAnnexAttachment();
    java.util.Vector annexAtt = wfaa.getAllAttachments(flowId);
	if ((attachments!=null && attachments.size()>0) || (annexAtt != null && annexAtt.size() > 0)) {
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();	
		
		// 判断是否有word或Excel文件
		boolean isShowPDF = false;
		if ("true".equals(cfg.get("canConvertToPDF"))) {
			java.util.Iterator ir = attachments.iterator();
			while (ir.hasNext()) {
				Attachment am = (Attachment) ir.next();
				String ext = StrUtil.getFileExt(am.getDiskName());
				if (ext.equals("doc") || ext.equals("docx")) {
					isShowPDF = true;
					break;
				}
			}
		}
%>
        <table class="tabStyle_1 percent98" width="80%"  border="0" align="center" cellpadding="0" cellspacing="0" style="margin-top:6px">
        <%
		  java.util.Iterator ir = attachments.iterator();
		  String creatorRealName = "";
		  String lockUser = "";
		  while (ir.hasNext()) {
			Attachment am = (Attachment) ir.next();
			
			if (am.getFieldName()!=null && !"".equals(am.getFieldName())) {
				if (!am.getFieldName().startsWith("att") && !am.getFieldName().startsWith("upload")) {
					// continue;
				}
			}			
			
			UserDb creator = um.getUserDb(am.getCreator());
			if (creator.isLoaded()) {
				creatorRealName = creator.getRealName();
			}
			
			lockUser = StrUtil.getNullString(am.getLockUser());
			%>
          <tr id="trAtt<%=am.getId()%>">
            <td height="31" align="left" title="<lt:Label res="res.flow.Flow" key="creator"/>：<%=creatorRealName%> <%=DateUtil.format(am.getCreateDate(), "MM-dd HH:mm")%>">
            <img src="images/attach.gif" />
			&nbsp;<span id="spanAttLink<%=am.getId()%>"><a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flowId%>&download=1" target="_blank"><span id="spanAttName<%=am.getId()%>"><%=am.getName()%></span></a></span>
			<span id="spanAttNameInput<%=am.getId()%>" style="display:none"><input id="attName<%=am.getId()%>" value="<%=StrUtil.HtmlEncode(am.getName())%>" /><input class="btn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="renameAtt('<%=am.getId()%>')"></span>
			&nbsp;&nbsp;
			<%
			if(myActionId!=-1 && wa.canEditAttachment()){ 
			%>
			<span id="spanRename<%=am.getId()%>" style="display:none;color:#aaaaaa;cursor:pointer" onclick="changeName('<%=am.getId()%>')">改名</span>
			<%}else{ %>
			<span id="spanRename<%=am.getId()%>" style="display:none;color:#aaaaaa;cursor:pointer"></span>
			<%} %>
            <%=NumberUtil.round((double)am.getSize()/1024000, 2)%>M&nbsp;&nbsp;
            <%
			boolean isPdf = false;
			if ("true".equals(cfg.get("canConvertToPDF")) && (StrUtil.getFileExt(am.getName()).equals("doc") || StrUtil.getFileExt(am.getName()).equals("docx"))) {
				isPdf = true;
			}

			if (myActionId!=-1) {
				String ext = StrUtil.getFileExt(am.getDiskName());
				if (ext.equals("doc") || ext.equals("xls") || ext.equals("docx") || ext.equals("xlsx") || ext.equals("wps")) {%>
					<%if (wa.isStart==1) {%>
						<a href="javascript:;" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)"><lt:Label res="res.flow.Flow" key="edit"/></a>
					<%}else{
						if (lf.getType()==Leaf.TYPE_FREE) {
							if (wpd.canUserDo(myUser, wf.getTypeCode(), "editAttach")) {
								%>
								<a href="javascript:;" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)"><lt:Label res="res.flow.Flow" key="approval"/></a>								
								<%
							}
						} else {					
							if (wa.canEditAttachment()) {
						%>
								<a href="javascript:;" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)"><lt:Label res="res.flow.Flow" key="approval"/></a>
						<%	}
						}
					}%>
				<%}else{%>
					<a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flowId%>" target="_blank"><lt:Label res="res.flow.Flow" key="show"/></a>
				<%}%>
				<%
				if (lf.getType()==Leaf.TYPE_FREE) {
					if (wpd.canUserDo(myUser, wf.getTypeCode(), "delAttach")) {
						%>
					  	&nbsp;&nbsp;<a href="javascript:;" onClick="delAtt('<%=doc_id%>', '<%=am.getId()%>', '<%=am.getFieldName()%>')"><lt:Label res="res.flow.Flow" key="delete"/></a>
						<%
					}
				}
				else {
					if (myActionId!=-1 && wa.canDelAttachment()) {%>
					  &nbsp;&nbsp;<a href="javascript:;" onClick="delAtt('<%=doc_id%>', '<%=am.getId()%>')"><lt:Label res="res.flow.Flow" key="delete"/></a>
					<%}
				}%>
				<%if (!isLight && lf.getType()!=Leaf.TYPE_FREE && wa.canReceiveRevise() && (ext.equals("doc") || ext.equals("docx"))) {%>
					&nbsp;&nbsp;<a href="javascript:;" onclick="selTemplate('<%=doc_id%>', '<%=am.getId()%>')"><lt:Label res="res.flow.Flow" key="pred"/></a>
				<%}%>
			<%
			}
			
			if (isPdf) {
				%>
				&nbsp;&nbsp;<a href="flow_getfile.jsp?op=toPDF&flowId=<%=flowId%>&attachId=<%=am.getId()%>" title="下载PDF文件" target="_blank">PDF</a>
				<%
			}
			
			if (cfg.getBooleanProperty("canPdfFilePreview")) {
				String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
				String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
				java.io.File fileExist = new java.io.File(htmlfile);
				if (fileExist.exists()) {
					%>
					&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=am.getName()%>', '<%=request.getContextPath()%>/<%=am.getVisualPath()%>/<%=am.getDiskName().substring(0, am.getDiskName().lastIndexOf(".")) + ".html"%>')">预览</a>
					<%
				}
			}
			%>
			</td>
          </tr>
        <%}%>
        <%
        java.util.Iterator annexIr = annexAtt.iterator();
        while (annexIr.hasNext()) {
        	WorkflowAnnexAttachment wfaatt = (WorkflowAnnexAttachment) annexIr.next();
        	long annexId = wfaatt.getAnnexId();
        	WorkflowAnnexDb wfadb = new WorkflowAnnexDb();
        	wfadb = (WorkflowAnnexDb) wfadb.getQObjectDb(annexId);
        	String annexUser = wfadb.getString("user_name");
        	UserDb annexUserDb = new UserDb(annexUser);
        	%>
            <tr>
              <td height="31" align="left" title="<lt:Label res="res.flow.Flow" key="creator"/>：<%=annexUserDb.getRealName()%> (附言) <%=DateUtil.format(wfadb.getDate("add_date"), "MM-dd HH:mm")%>"><img src="images/attach.gif" />
  			&nbsp;<a href="<%=wfaatt.getAttachmentUrl(request)%>" target="_blank"><%=wfaatt.getName()%></a>&nbsp;&nbsp;&nbsp;&nbsp;
  			<%=NumberUtil.round((double)wfaatt.getSize()/1024000, 2)%>M
              &nbsp;&nbsp;&nbsp;&nbsp;<a href="<%=wfaatt.getAttachmentUrl(request)%>" target=_blank><lt:Label res="res.flow.Flow" key="download"/></a>
              </td>
              </tr>
        <%
        }
        %>
</table>
<%}else{%>
	<script>
		$("#flowwhitebg").hide();
	</script>
<%}}%>