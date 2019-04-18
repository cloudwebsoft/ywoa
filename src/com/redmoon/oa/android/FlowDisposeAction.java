package com.redmoon.oa.android;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import com.redmoon.oa.flow.macroctl.NestSheetCtl;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

public class FlowDisposeAction {
	transient Logger logger = Logger.getLogger(FlowDisposeAction.class
			.getName());
	private String skey = "";
	private String result = "";
	private int myActionId;
	private String op = "";

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

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

	public int getMyActionId() {
		return myActionId;
	}

	public void setMyActionId(int myActionId) {
		this.myActionId = myActionId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	private int actionId;

	public String execute() {
		JSONObject json = new JSONObject();
		JSONArray fields = new JSONArray();
		JSONObject result = new JSONObject();
		JSONArray users = new JSONArray();
		JSONArray files = new JSONArray();
		Privilege privilege = new Privilege();
		boolean hasAttach = true;
		boolean re = privilege.Auth(getSkey());
		String username = privilege.getUserName(skey);
		boolean isProgress = false;
		WorkflowAnnexMgr workflowAnnexMgr = new WorkflowAnnexMgr();
		int progress = 0;
		String res = "0";
		String msg = "操作成功";
		try {
			if (re) {
				res = "-2";
				msg = "时间过期";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			MyActionDb mad = new MyActionDb();
			mad = mad.getMyActionDb(getMyActionId());
			if (!mad.isLoaded()) {
				res = "-1";
				msg = "出现错误，请检查预设流程是否存在！";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_PASS) {
				
				res = "-1";
				msg = "流程节点已由其他人员处理，不需要再处理";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
				
			}
			else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
				res = "-1";
				msg = "待办流程已因被返回而忽略，不需要再处理！";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_TRANSFER) {
			
				res = "-1";
				msg = "	流程已指派，不需要再处理!";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			
			if(!mad.isReaded()){
				mad.setReadDate(new Date());
				mad.setReaded(true);
				mad.save();
			}
			long flowId = mad.getFlowId();
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb((int) flowId);
			// 锁定流程
			WorkflowMgr wfm = new WorkflowMgr();
			wfm.lock(wf, privilege.getUserName(skey));
			com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
			lf = lf.getLeaf(wf.getTypeCode());
			if (lf == null) {
				res = "-1";
				msg = "流程目录不存在";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
			boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
			
			FormDb fd = new FormDb();
			fd = fd.getFormDb(lf.getFormCode());
			hasAttach = fd.isHasAttachment();
			isProgress = fd.isProgress();

			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(wf.getId(), fd);

			fdao.load();
			
			progress = fdao.getCwsProgress();
			
			Vector v = fdao.getFields();
			UserMgr um = new UserMgr();

			// 这段用来验证字段是否可写
			WorkflowActionDb wa = new WorkflowActionDb();
			int actionId = (int) mad.getActionId();
			wa = wa.getWorkflowActionDb(actionId);
			if (wa == null || !wa.isLoaded()) {
				res = "-1";
				msg = "流程中的相应动作不存在";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			String userName = privilege.getUserName(getSkey());
			WorkflowPredefineDb wfp = new WorkflowPredefineDb();
			wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());
			if (wa.getStatus()==wa.STATE_DOING || wa.getStatus()==wa.STATE_RETURN)
				;
			else {
				// 有可能会是重激活的情况，或者是异或聚合的情况
				if (!wfp.isReactive() && !wa.isXorAggregate()) {
					mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
					mad.setCheckDate(new java.util.Date());
					mad.save();
					res = "-1";
					msg = "流程节点已处理";
					json.put("res", res);
					json.put("msg", msg);
					setResult(json.toString());
					return "SUCCESS";
				}
			}
			
			// 取可写表单域
			String fieldWrite = StrUtil.getNullString(wa.getFieldWrite())
					.trim();
			String[] fds = fieldWrite.split(",");
			int len = fds.length;

			// 自由流程根据用户所属的角色，得到可写表单域
			if (lf.getType() == Leaf.TYPE_FREE) {
				fds = wfp.getFieldsWriteOfUser(wf, userName);
				len = fds.length;
			}
			
			String fieldHide = StrUtil.getNullString(wa.getFieldHide()).trim();
			String[] fdsHide = fieldHide.split(",");
			int lenHide = fdsHide.length;

			MacroCtlUnit mu;
			MacroCtlMgr mm = new MacroCtlMgr();
			Iterator ir = v.iterator();
			json.put("sender", um.getUserDb(wf.getUserName()).getRealName());
			// json.put("cwsWorkflowResult",mad.getResult());
			json.put("cwsWorkflowTitle", wf.getTitle());
			json.put("status", wf.getStatusDesc());
			json.put("actionId", String.valueOf(actionId));
			json.put("myActionId", String.valueOf(getMyActionId()));
			json.put("flowId", String.valueOf(flowId));
			String flowTypeName = lf.getName();
			json.put("flowTypeName", flowTypeName);
			WorkflowPredefineDb wfd = new WorkflowPredefineDb();
			wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
						
			boolean isLight = wfd.isLight();
			json.put("isLight", isLight);// 判断时候是@liuchen
			if (isLight) {
				JSONArray jsonArr = getCwsWorkflowResultDetail(flowId,
						getMyActionId(),username);
				json.put("lightDetail", jsonArr);
			}else{
				result.put("annexs", workflowAnnexMgr.getFlowAnnex(0,username,flowId));
			}
						
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());

			com.redmoon.oa.pvg.Privilege p = new com.redmoon.oa.pvg.Privilege();
			if (p.isUserPrivValid(request, "sms")) {
				json.put("isSms", "true");
			} else {
				json.put("isSms", "false");
			}
			// 能否拒绝
			json.put("canDecline", String.valueOf(wa.canDecline()));
			
			Vector returnv = wa.getLinkReturnActions();
			boolean canReturn;
			// 如果当前为发起节点则不能退回
			if (wf.getStartActionId() == mad.getActionId()) {
				canReturn = false;
			}
			else {
				canReturn = returnv.size() > 0
					|| wfp.getReturnStyle() == WorkflowPredefineDb.RETURN_STYLE_FREE;
			}
			
			json.put("canReturn", String.valueOf(canReturn));

			json.put("url", "public/flow_dispose_do.jsp");
			
			// fgf 20170519
			json.put("isProgress", fd.isProgress());
			json.put("isReply", wfd.isReply());
			
			// fgf 20180415
			json.put("progress", progress);
			
			// fgf 20180814 显示控制
			String viewJs = WorkflowUtil.doGetViewJSMobile(request, fd, fdao, userName, false);
			json.put("viewJs", viewJs);
			
			// 判断删除按钮是否显示
			boolean isReadOnly = false;
			if (wa.getKind()==WorkflowActionDb.KIND_READ) {
				isReadOnly = true;
			}			
			boolean canDel = false;
			com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();			
			if(conf.getIsDisplay("FLOW_BUTTON_DEL")){
				String flag = wa.getFlag();				
				if (!isReadOnly && flag.length()>=4 && flag.substring(3, 4).equals("1") && mad.getActionStatus()!=WorkflowActionDb.STATE_RETURN) {
					if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
						canDel = true;
					}
				}
			}
			json.put("canDel", canDel);
			
			// 结束流程标志
			boolean canFinishAgree = false;
			if(conf.getIsDisplay("FLOW_BUTTON_FINISH")){
				String flag = wa.getFlag();				
				if (wf.isStarted() && flag.length()>=12 && flag.substring(11, 12).equals("1")) {
					if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
						canFinishAgree = true;
					}
				}
			}
			json.put("canFinishAgree", canFinishAgree);			

			while (ir.hasNext()) {
				FormField ff = (FormField) ir.next();
				String val = fdao.getFieldValue(ff.getName());
				boolean finded = false;
				for (int i = 0; i < len; ++i)
					if (ff.getName().equals(fds[i])) {
						finded = true;
						break;
					}
				if (!(finded)) {
					ff.setEditable(false);
				}

				// 如果不是自由流程
				if (lf.getType() != Leaf.TYPE_FREE) {
					// 置隐藏表单域
					finded = false;
					for (int i = 0; i < lenHide; ++i) {
						if (ff.getName().equals(fdsHide[i])) {
							finded = true;
							break;
						}
					}
					if (finded) {
						ff.setHidden(true);
					}
				}

				JSONObject field = new JSONObject();
				field.put("title", ff.getTitle());
				field.put("code", ff.getName());
				field.put("desc", StrUtil.getNullStr(ff.getDescription()));

				// 如果是计算控件，则取出精度和四舍五入属性
				if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
					FormParser fp = new FormParser();
					String isroundto5 = fp.getFieldAttribute(fd, ff,
							"isroundto5");
					String digit = fp.getFieldAttribute(fd, ff, "digit");
					field.put("formula", ff.getDefaultValueRaw());
					field.put("isroundto5", isroundto5);
					field.put("digit", digit);
				}

				String options = "";
				String macroType = "";
				String controlText = "";
				String macroCode = "";
				JSONArray js = new JSONArray();
				JSONArray opinionArr = null;
				JSONObject opinionVal = null;

				String metaData = "";
				if (ff.getType().equals("macro")) {
					mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu == null) {
						LogUtil.getLog(getClass()).error(
								"MactoCtl " + ff.getTitle() + "："
										+ ff.getMacroType() + " is not exist.");
						continue;
					}

					macroType = mu.getIFormMacroCtl().getControlType();

					mu.getIFormMacroCtl().setMyActionId(getMyActionId());

					macroCode = mu.getCode();

					IFormMacroCtl ifmc = mu.getIFormMacroCtl();
					ifmc.setFlowFormDAO(fdao);
					
					// 如果值为null，则在json中put的时候，是无效的，不会被记录至json中
					controlText = StrUtil.getNullStr(mu.getIFormMacroCtl()
							.getControlText(privilege.getUserName(getSkey()),
									ff));
					val = StrUtil.getNullStr(mu.getIFormMacroCtl()
							.getControlValue(privilege.getUserName(getSkey()),
									ff));
					// 须放在此位置，因为在当前用户宏控件的getContextValue中更改了ff的值
					metaData = mu.getIFormMacroCtl().getMetaData(ff);

					options = ifmc.getControlOptions(
							privilege.getUserName(getSkey()), ff);
					if (options != null && !options.equals("")) {
						// options = options.replaceAll("\\\"", "");
						js = new JSONArray(options);
					}
				}
				// 判断是否为意见输入框
				if (macroCode != null && !macroCode.equals("")) {
					if (macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")) {
						if (controlText != null
								&& !controlText.trim().equals("")) {
							opinionArr = new JSONArray(controlText);
						}
						if (val != null && !val.trim().equals("")) {
							opinionVal = new JSONObject(val);
						}
					}
					
					if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
						JSONObject jsonObj = NestSheetCtl.getCtlDesc(ff);
						if (jsonObj!=null) {
							field.put("desc", jsonObj);
						}
					}			
					else if (macroCode.equals("module_field_select")) {
						JSONObject jsonObj = ModuleFieldSelectCtl.getCtlDesc(ff);
						if (jsonObj!=null) {
							field.put("desc", jsonObj);
						}
					}					
				}
				field.put("type", ff.getType());
				if (ff.getType().equals("select")) {
					String[][] optionsArray = FormParser
							.getOptionsArrayOfSelect(fd, ff);
					for (int i = 0; i < optionsArray.length; i++) {
						String[] optionsItem = optionsArray[i];
						if(optionsItem!=null && optionsItem.length==2){
							JSONObject option = new JSONObject();
							option.put("value", optionsItem[0]);
							option.put("name", optionsItem[1]);
							js.put(option);
						}
					}
				} else if (ff.getType().equals("radio")) {
					FormParser fp = new FormParser();
					// options = fp.getOptionsOfSelect(fd, ff);
					String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
					for (int i = 0; i < optionsArray.length; i++) {
						JSONObject option = new JSONObject();
						option.put("value", optionsArray[i]);
						option.put("name", optionsArray[i]);
						js.put(option);
					}
				}
				String level = "";
				if (ff.getType().equals("checkbox")) {
					// level = "个人兴趣";
					level = ff.getTitle();
				}
				field.put("options", js);
				if (opinionVal != null) {
					field.put("value", opinionVal);
				} else {
					field.put("value", val);
				}
				if (opinionArr != null && opinionArr.length() > 0) {
					field.put("text", opinionArr);
				} else {
					field.put("text", controlText);
				}
				// LogUtil.getLog(getClass()).info(ff.getTitle() +
				// " controlText=" + controlText);
				field.put("level", level);
				field.put("macroType", macroType);
				field.put("editable", String.valueOf(ff.isEditable()));
				field.put("isHidden", String.valueOf(ff.isHidden()));
				field.put("isNull", String.valueOf(ff.isCanNull()));
				field.put("fieldType", ff.getFieldTypeDesc());
			
				field.put("macroCode", macroCode);
				
				// 可传SQL控件条件中的字段
				field.put("metaData", metaData);
				field.put("isReadonly", ff.isReadonly());

				fields.put(field);
			}
			
			String downPath = "";
			int doc_id = wf.getDocId();
			DocumentMgr dm = new DocumentMgr();
			Document doc = dm.getDocument(doc_id);
			if (doc != null) {
				java.util.Vector attachments = doc.getAttachments(1);
				ir = attachments.iterator();
				while (ir.hasNext()) {
					Attachment am = (Attachment) ir.next();
					
					JSONObject file = new JSONObject();
					
					boolean isPreview = false;
					boolean isHtml = false;
					String ext = StrUtil.getFileExt(am.getDiskName());
					if (canOfficeFilePreview) {
	                	if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
	                		isPreview = true;
	                		isHtml = true;
	                	}
	                }
					if (canPdfFilePreview && ext.equals("pdf")) {
                		isPreview = true;
                		isHtml = true;
                	}	
					if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "bmp".equals(ext)) {
						isPreview = true;
					}
					
                	if (isPreview) {
                		if (isHtml) {
							String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
							String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
							java.io.File fileExist = new java.io.File(htmlfile);
							if (fileExist.exists()) {
								file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());
							}
                		}
                		else {
							file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());                			
                		}
                	}
                	
                	file.put("id", am.getId());
                	
                	file.put("canDel", wa.canDelAttachment());
					
					file.put("name", am.getName());
					downPath = "public/flow_getfile.jsp?" + "flowId=" + flowId
							+ "&attachId=" + am.getId();
					file.put("url", downPath);
					file.put("size", String.valueOf(am.getSize()));
					files.put(file);
				}
			}
			
			// 取回复
            if (fd.isProgress()) {
            	/*
            	JSONObject annex = new JSONObject();
        		WorkflowAnnexDb wad = new WorkflowAnnexDb();
        		String sql = "select id from flow_annex where flow_id=" + flowId + " and (user_name=" + StrUtil.sqlstr(userName) + " or reply_name=" + StrUtil.sqlstr(userName) + " or is_secret=0) order by add_date asc";        		
        		Vector vec1 = wad.list(sql);
                Iterator<WorkflowAnnexDb> ir1 = vec1.iterator();
        		while (ir1.hasNext()) {
        			wad = ir1.next();
        			int id = (int)wad.getLong("id");
        			UserDb ud = um.getUserDb(wad.getString("user_name"));
        			annex.put("id", id);
        			annex.put("realName", ud.getRealName());
        			annex.put("progress", wad.getInt("progress"));
        			annex.put("content", wad.getString("content"));
        			annex.put("date", DateUtil.format(wad.getDate("add_date"), "yyyy-MM-dd HH:mm:ss"));
        			annexes.put(annex);
        		}
        		*/
            }

			// 置异或发散
			StringBuffer condBuf = new StringBuffer();
			boolean flagXorRadiate = wa.isXorRadiate();
			Vector vMatched = null;
			if (flagXorRadiate) {
			  vMatched = wa.matchNextBranch(wa, privilege.getUserName(getSkey()),condBuf,myActionId);
			  String conds = condBuf.toString();
			  boolean hasCond = conds.equals("")?false:true; // 是否含有条件 
			  if(hasCond){
				  flagXorRadiate = true;
			  }else{
				  flagXorRadiate = false;
			  }
			}
			json.put("flagXorRadiate", String.valueOf(flagXorRadiate));
			WorkflowRuler wr = new WorkflowRuler();

			// 取得下一步提交的用户
			
			Vector vto = wa.getLinkToActions();
			Iterator toir = vto.iterator();
			Iterator userir = null;
			while (toir.hasNext()) {
				WorkflowActionDb towa = (WorkflowActionDb) toir.next();
				if (towa.getJobCode().equals(
						WorkflowActionDb.PRE_TYPE_USER_SELECT)
						|| towa
								.getJobCode()
								.equals(
										WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
					boolean isStragegyGoDown = towa.isStrategyGoDown(); // 是否为下达
					
					JSONObject user = new JSONObject();
					user.put("actionTitle", towa.getTitle());
					user.put("roleName", towa.getJobName());
					user.put("internalname", towa.getInternalName());
					user.put("name", "WorkflowAction_" + towa.getId());
					// 手机客户端还不能区分是否在所管理的部门范围内
					user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
					user.put("realName", "自选用户");
					user.put("isSelectable", "true");

					// 如果节点上曾经选过人，则在手机客户端默认选中
					user.put("actionUserName", towa.getUserName());
					user.put("actionUserRealName", towa.getUserRealName());

					// 标志位，能否选择用户
					boolean canSelUser = wr.canUserSelUser(request, towa);
					// System.out.println(getClass() + " actionUserRealName=" +
					// towa.getUserRealName() + " canSelUser=" + canSelUser);
					user.put("canSelUser", String.valueOf(canSelUser));
					user.put("isGoDown", String.valueOf(isStragegyGoDown));

					users.put(user);
				} else {
					boolean isStrategySelectable = towa.isStrategySelectable();
					boolean isStrategySelected = towa.isStrategySelected();
					
					Vector vuser = towa.matchActionUser(request, towa, wa, false, null);
					userir = vuser.iterator();
					while (userir != null && userir.hasNext()) {
						UserDb ud = (UserDb) userir.next();
						JSONObject user = new JSONObject();
						user.put("actionTitle", towa.getTitle());
						user.put("roleName", towa.getJobName());
						user.put("internalname", towa.getInternalName());
						user.put("name", "WorkflowAction_" + towa.getId());
						user.put("value", ud.getName());
						user.put("realName", ud.getRealName());
						user.put("isSelectable", String.valueOf(isStrategySelectable));
						user.put("isSelected", String.valueOf(isStrategySelected));

						// 标志位，能否选择用户
						boolean canSelUser = wr.canUserSelUser(request, towa);
						user.put("canSelUser", String.valueOf(canSelUser));
						users.put(user);
					}
				}
			}
		} catch (JSONException e) {
			res = "-1";
			msg = "JSON解析异常";
			Logger.getLogger(FlowInitAction.class).error(e.getMessage());
		} catch (ErrMsgException e1) {
			res = "-1";
			msg = e1.getMessage();
			Logger.getLogger(FlowInitAction.class).error(e1.getMessage());
		} catch (MatchUserException e2) {
			res = "0";
			// 手机端在选择后将会post至FlowMultiDeptAction再次匹配人员
			msg = "手机端兼职处理";
			String userName = privilege.getUserName(skey);
			DeptUserDb dud = new DeptUserDb();
			Vector vu = dud.getDeptsOfUser(userName);
			Iterator irdu = vu.iterator();
			JSONArray deptArr = new JSONArray();
			try {
				while (irdu.hasNext()) {
					DeptDb dept = (DeptDb) irdu.next();
					if (dept.isHide()) {
						continue;
					}
					String code = dept.getCode();
					String name = dept.getName();

					if (!dept.getParentCode().equals(DeptDb.ROOTCODE) && !dept.getCode().equals(DeptDb.ROOTCODE)) {
						name = dept.getDeptDb(dept.getParentCode()).getName() + "->" + dept.getName();
					}
					JSONObject deptObj = new JSONObject();
					deptObj.put("name", name);
					deptObj.put("code", code);
					deptArr.put(deptObj);
				}
				result.put("multiDepts", deptArr);
			} catch (JSONException e) {
				Logger.getLogger(FlowInitAction.class).error(e2.getMessage());
			}
		} finally {
			try {
				result.put("users", users);
				result.put("fields", fields);
				result.put("files", files);
				json.put("result", result);
				json.put("res", res);
				json.put("msg", msg);
				// 是否允许上传附件
				json.put("hasAttach", hasAttach);				
			} catch (final JSONException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(FlowInitAction.class).error(e.getMessage());
			}
			setResult(json.toString());
		}
		return "SUCCESS";
	}

	public JSONArray getCwsWorkflowResultDetail(long flowId, int myActionId,String username) {
		JSONArray jsonArr = new JSONArray();
		UserMgr um = new UserMgr();
		UserDb userDb = new UserDb();
		String processListSql = "select id from flow_my_action where flow_id="
				+ flowId + " and id <>" + myActionId
				+ " order by receive_date asc";
		MyActionDb mad = new MyActionDb();
		Vector vProcess = mad.list(processListSql);
		Iterator ir = vProcess.iterator();
		WorkflowAnnexMgr workFlowAnnex = new WorkflowAnnexMgr();
		while (ir.hasNext()) {
			MyActionDb child_mad = (MyActionDb) ir.next();
			JSONObject obj = new JSONObject();
			try {
				userDb = userDb.getUserDb(child_mad.getUserName());
				String content = MyActionMgr.renderResultForMobile(child_mad);
				obj.put("result",StringEscapeUtils.unescapeHtml(content));
				obj.put("photo",StrUtil.getNullStr(userDb.getPhoto()));
				obj.put("readDate", DateUtil.format(child_mad.getReadDate(),
						"MM-dd HH:mm"));
				obj.put("userName", userDb.getRealName());
				obj.put("gender", userDb.getGender());
				obj.put("myActionId", child_mad.getId());
				obj.put("annexs", workFlowAnnex.getFlowAnnex(child_mad.getId(),username,0));
				jsonArr.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				logger.error("@详情列表" + e.getMessage());
			}

		}
		
		return jsonArr;

	}
	


}
