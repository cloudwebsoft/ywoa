package com.redmoon.oa.android;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.HtmlUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.Attachment;
import com.redmoon.oa.flow.Document;
import com.redmoon.oa.flow.DocumentMgr;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.MyActionMgr;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowAnnexDb;
import com.redmoon.oa.flow.WorkflowAnnexMgr;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.flow.WorkflowUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import com.redmoon.oa.flow.macroctl.NestSheetCtl;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

public class FlowModifyAction {
	private String skey = "";
	private String result = "";
	private String flowId = "";

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String execute() {
		JSONObject json = new JSONObject();
		JSONObject result = new JSONObject(); 
		boolean isProgress = false;
		int progress = 0;
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		String userName = privilege.getUserName(skey);
		WorkflowAnnexMgr wfam = new WorkflowAnnexMgr();
		if (re) {
			try {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    MacroCtlUnit mu;
		MacroCtlMgr mm = new MacroCtlMgr();
		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb(StrUtil.toInt(getFlowId()));
		
		//原来语句中有一个and canList=1 ，这参数不知道啥用
		String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList from form_field where formCode=? ";
		sql += " order by orders desc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
			lf = lf.getLeaf(wf.getTypeCode());
			if(lf==null){
				try {
					json.put("res", "-1");
					json.put("msg", "流程目录不存在");
					setResult(json.toString());
					return "SUCCESS";
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			HttpServletRequest request = ServletActionContext.getRequest();
			
			FormDb fd = new FormDb();
			fd = fd.getFormDb(lf.getFormCode());
			
			isProgress = fd.isProgress();

			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(wf.getId(), fd);
			
			progress = fdao.getCwsProgress();

			UserMgr um = new UserMgr();
			mm = new MacroCtlMgr();
			ri = jt.executeQuery(sql, new Object[] {lf.getFormCode()});
			json.put("res","0");
			json.put("msg","操作成功");
			json.put("sender",um.getUserDb(wf.getUserName()).getRealName());
			json.put("createDate",DateUtil.format(wf.getMydate(), "yyyy-MM-dd HH:mm:ss"));
			json.put("status",wf.getStatusDesc());
			json.put("flowTypeName", lf.getName());
			
			// fgf 20170519
			json.put("isProgress", fd.isProgress());	
			
			// fgf 20180415
			json.put("progress", progress);			
			
			// fgf 20180814 显示控制
			String viewJs = WorkflowUtil.doGetViewJSMobile(request, fd, fdao, userName, true);
			json.put("viewJs", viewJs);			
			
			WorkflowPredefineDb wfd = new WorkflowPredefineDb();
	        wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
	        
			json.put("isReply", wfd.isReply());
	        
	        boolean isLight = wfd.isLight();
	        json.put("isLight", isLight);//判断时候是@liuchen
			if(isLight){
				if(flowId!=null && !flowId.equals("")){
					JSONArray jsonArr = getCwsWorkflowResultDetail(Long.parseLong(flowId),userName);
					json.put("lightDetail",jsonArr);
				}
			}else{
				result.put("annexs", wfam.getFlowAnnex(0, userName, Long.parseLong(flowId)));
			}
			
			JSONArray fields  = new JSONArray(); 
	
			boolean isHideField = true;
			String fieldHide = "";
			String[] fdsHide = null;
			// 将不显示的字段加入fieldHide
			Iterator ir = fd.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
				if (ff.getHide()==FormField.HIDE_ALWAYS) {
					if ("".equals(fieldHide)) {
						fieldHide = ff.getName();
					}
					else {
						fieldHide += "," + ff.getName();
					}
				}
			}
			
	        MyActionDb mad = new MyActionDb();
	        mad = mad.getMyActionDbOfFlow(wf.getId(), privilege.getUserName(skey));
	        // 管理员查看时，其本人可能并未参与流程，则mad将为null
	        if (mad!=null) {
	            WorkflowActionDb wad = new WorkflowActionDb();
	            wad = wad.getWorkflowActionDb((int) mad.getActionId());
	
	            String fHide = StrUtil.getNullString(wad.getFieldHide()).trim();
				if ("".equals(fieldHide)) {
					fieldHide = fHide;
				}
				else {
					fieldHide += "," + fHide;
				}
			}

			fdsHide = StrUtil.split(fieldHide, ",");

			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				
				// FormField ff = fd.getFormField(rr.getString("name"));
				
				FormField ff = fdao.getFormField(rr.getString("name"));
				if (isHideField) {
					boolean isShow = true;
					if (fdsHide != null) {
						for (int i = 0; i < fdsHide.length; i++) {
							if (!fdsHide[i].startsWith("nest.")) {
								if (fdsHide[i].equals(ff.getName())) {
									isShow = false;
									break;
								}
							} else {
								isShow = false;
								break;
							}
						}
						
						if (!isShow)
							continue;
			        }
		        }

				// String val = ff.getValue();
				String val = ff.convertToHtml();
				// 用于当前用户宏控件判断如果为空，则不显示当前用户
				ff.setEditable(false);
				
				JSONObject field = new JSONObject(); 

				String macroCode = "";
				JSONArray jsonArr = null;
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						val = mu.getIFormMacroCtl().getControlText(privilege.getUserName(skey), ff);
						macroCode = mu.getCode();		
						if(macroCode!=null && !macroCode.equals("")){
							if(macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")){
								if(!val.equals("")){
									jsonArr = new JSONArray(val);
								}else{
									jsonArr = new JSONArray();
								}
							}
							
							if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
								JSONObject jsonObj = NestSheetCtl.getCtlDesc(ff);
								if (jsonObj!=null) {
									field.put("desc", jsonObj);
								}
								else {
									field.put("desc", ff.getDescription());									
								}
							}
							else if (macroCode.equals("module_field_select")) {
								JSONObject jsonObj = ModuleFieldSelectCtl.getCtlDesc(ff);
								if (jsonObj!=null) {
									field.put("desc", jsonObj);
								}
								else {
									field.put("desc", ff.getDescription());									
								}
							}							
						}
					}				
				}
				field.put("title",rr.getString("title"));
				field.put("code",ff.getName());
				if(jsonArr!=null){
					field.put("value",jsonArr);		
				}else{
					field.put("value",val);		
				}
				field.put("type",rr.getString("type"));
				String level="";
				if (ff.getType().equals("checkbox")) {
					// level = "个人兴趣";
					level = ff.getTitle();					
				}
				field.put("level",level);
				field.put("macroCode", macroCode);
				fields.put(field);	
			}
			
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
			boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
			
			// 文件附件
			JSONArray files = new JSONArray();
			String downPath = "";
			int doc_id = wf.getDocId();
			DocumentMgr dm = new DocumentMgr();
			Document doc = dm.getDocument(doc_id);
			if (doc!=null) {					
				java.util.Vector attachments = doc.getAttachments(1);
			    ir = attachments.iterator();
				while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next();
					JSONObject file = new JSONObject();
					
					boolean isPreview = false;
					boolean isHtml = false;
					String ext = StrUtil.getFileExt(am.getDiskName());
					// System.out.println(getClass() + " ext=" + ext + " " + ext.equals("pdf") + " canOfficeFilePreview=" + canOfficeFilePreview + " canPdfFilePreview=" + canPdfFilePreview);
					LogUtil.getLog(getClass()).info(" ext=" + ext + " canOfficeFilePreview=" + canOfficeFilePreview + " isHtml=" + isHtml);
					if (canOfficeFilePreview) {
	                	if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
	                		isPreview = true;
	                		isHtml = true;
	                	}
	                }
					if (canPdfFilePreview && ext.equalsIgnoreCase("pdf")) {
                		isPreview = true;
                		isHtml = true;
                	}	
					if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "bmp".equals(ext)) {
						isPreview = true;
					}
					
					// System.out.println(getClass() + " ext=" + ext + " isHtml=" + isHtml);

                	if (isPreview) {
                		if (isHtml) {
							String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
							String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
							java.io.File fileExist = new java.io.File(htmlfile);
							
							// System.out.println(getClass() + " " + htmlfile + " fileExist.exists()=" + fileExist.exists());
							LogUtil.getLog(getClass()).info("fileExist.exists()=" + fileExist.exists());
							if (fileExist.exists()) {
								file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());
							}
                		}
                		else {
							file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());                			
                		}
                	}
                	
					file.put("name",am.getName());
					downPath = "public/flow_getfile.jsp?"+"flowId="+flowId+"&attachId="+am.getId();
					file.put("url",downPath);
					file.put("size",String.valueOf(am.getSize()));
					files.put(file);
				}
			}					
			result.put("fields",fields);
			result.put("files",files);
			json.put("result",result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
	
	public JSONArray getCwsWorkflowResultDetail(long flowId,String userName){
		WorkflowAnnexMgr wfam = new WorkflowAnnexMgr(); 
		JSONArray jsonArr = new JSONArray();
		UserMgr um = new UserMgr();
		UserDb userDb = new UserDb();
		String processListSql = "select id from flow_my_action where flow_id="
			+ flowId+" order by receive_date asc";
		MyActionDb mad = new MyActionDb();
		Vector vProcess = mad.list(processListSql);
		Iterator ir = vProcess.iterator();
		while (ir.hasNext()) {
			MyActionDb pmad = (MyActionDb) ir.next();
			JSONObject obj = new JSONObject();
			try {
				userDb = userDb.getUserDb(pmad.getUserName());
				
				String content = MyActionMgr.renderResultForMobile(pmad);
				obj.put("result",StringEscapeUtils.unescapeHtml(content) );
				obj.put("readDate",DateUtil.format(pmad.getReadDate(), "MM-dd HH:mm"));
				obj.put("userName", userDb.getRealName());
				obj.put("photo",StrUtil.getNullStr(userDb.getPhoto()));
				obj.put("gender", userDb.getGender());
				obj.put("myActionId", pmad.getId());
				obj.put("annexs", wfam.getFlowAnnex(pmad.getId(), userName,0));
				
				jsonArr.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(FlowModifyAction.class).error("@详情列表"+e.getMessage());
			}
			
		
		}
		return jsonArr;
		
	}
	
	
	
}
