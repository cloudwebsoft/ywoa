package com.cloudweb.oa.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.util.*;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.Constants;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormDAOLog;
import com.redmoon.oa.visual.FormUtil;
import com.redmoon.oa.visual.FuncUtil;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;
import com.redmoon.oa.visual.SQLBuilder;
import com.redmoon.oa.visual.AttachmentLogDb;
import com.redmoon.oa.visual.Attachment;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.Paginator;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import sun.security.util.Debug;

@Controller
@RequestMapping("/visual")
public class ModuleController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/moduleSetUnitCode", method = RequestMethod.POST, produces={"application/json;charset=UTF-8;"})
	public String setUnitCode() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd==null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}
		
		Privilege privilege = new Privilege();
		ModulePrivDb mpd = new ModulePrivDb(code);		
		if (!mpd.canUserManage(privilege.getUser(request))) {
			try {
				json.put("ret", 0);
				json.put("msg", cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();			
		}
		
		String strIds = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strIds, ",");
		if (ids==null) {
			try {
				json.put("ret", 0);
				json.put("msg", "请选择记录！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();					
		}
		
		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);			
		String toUnitCode = ParamUtil.get(request, "toUnitCode");
		FormDAO fdao = new FormDAO();
		try {
			int len = ids.length;
			for (int i=0; i<len; i++) {
				int id = StrUtil.toInt(ids[i]);
				fdao = fdao.getFormDAO(id, fd);
				fdao.setUnitCode(toUnitCode);
				fdao.save();
			}
			json.put("ret", "1");
			json.put("msg", "操作成功！");
		}
		catch (ErrMsgException e) {
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();		
	}		
	
	@ResponseBody
	@RequestMapping(value = "/moduleBatchOp", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})
	public String moduleBatchOp() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd==null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}

		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);	
		
		boolean re = false;
		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
		try {
			re = fdm.batchOperate(request);
		}
		catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}			
			return json.toString();
		}
		
		try {
			if (re) {
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", 0);
				json.put("msg", "操作失败");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return json.toString();		
	}		
	
	@ResponseBody
	@RequestMapping(value = "/moduleDel", method = RequestMethod.POST, produces={"application/json;charset=UTF-8;"})
	public String moduleDel() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd==null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}
		
		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);		
		
		boolean re = false;
		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
		try {
			//re = fdm.del(request);
			re = fdm.del(request,false,code);
		}
		catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}			
			return json.toString();			
		}
		
		try {
			if (re) {
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", 0);
				json.put("msg", "操作失败");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return json.toString();		
	}

	
	@ResponseBody
	@RequestMapping(value = "/moduleList", method = RequestMethod.POST, produces={"application/json;charset=UTF-8;"})
	public String moduleList() {		
		// String op = ParamUtil.get(request, "op");
		String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位
		
		String code = ParamUtil.get(request, "code");
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd==null) {
			LogUtil.getLog(getClass()).error("模块：" + code + "不存在！");
			return "";
		}		
		
		ModulePrivDb mpd = new ModulePrivDb(code);
		
		// String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String[] fields = msd.getColAry(false, "list_field");
		String[] fieldsLink = msd.getColAry(false, "list_field_link");

		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);		
		
		String orderBy = ParamUtil.get(request, "orderBy");
		String sort = ParamUtil.get(request, "sort");

		if (orderBy.equals("")) {
			String filter = StrUtil.getNullStr(msd.getString("filter")).trim();
			boolean isComb = filter.startsWith("<items>") || filter.equals("");
			// 如果是组合条件，则赋予后台设置的排序字段
			if (isComb) {
				orderBy = StrUtil.getNullStr(msd.getString("orderby"));
				sort = StrUtil.getNullStr(msd.getString("sort"));
			}
			if ("".equals(orderBy)) {
				orderBy = "id";
			}
		}

		if (sort.equals(""))
			sort = "desc";		
		
		com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		String desKey = ssoCfg.get("key");
		
		// 用于传过滤条件
		request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
		String[] ary = null;
		try {
			ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
		}
		catch (ErrMsgException e) {
			DebugUtil.i(getClass(), "moduleList", "SQL：" + e.getMessage());
			return "";
		}

		String sql = ary[0];
		// String sqlUrlStr = ary[1];
		// System.out.println(getClass() + " sql=" + sql);
		// 如果是日志表，则与需模块相关联，并能支持副模块
		if (formCode.equals("module_log")) {
			sql = SQLBuilder.getListSqlForLogRelateModule(request, sql);
		}
		DebugUtil.log(getClass(), "moduleList", "sql=" + sql);

		JSONObject jobject = new JSONObject();

		FormDAO fdao = new FormDAO();

		int pagesize = ParamUtil.getInt(request, "rp", 20);
		int curpage = ParamUtil.getInt(request, "page", 1);
		
		ListResult lr = null;
		try {
			lr = fdao.listResult(formCode, sql, curpage, pagesize);
		} catch (ErrMsgException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			try {
				jobject.put("page", 1);
				jobject.put("total", 0);				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return jobject.toString();
		}
		
		String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
		String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
		String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
		boolean isPrompt = false;
		if (!promptField.equals("") && !promptIcon.equals("")) {
			isPrompt = true;
		}		
		
		String mainFormCode = code;
		int is_workLog = msd.getInt("is_workLog");
		if (!msd.getString("code").equals(msd.getString("form_code"))) {
			ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
			is_workLog = msdMain.getInt("is_workLog");
			mainFormCode = msd.getString("form_code");
		}		
		
		Privilege privilege = new Privilege();
		String userName = privilege.getUser(request);
  		boolean canView = mpd.canUserView(userName);
  		boolean canLog = mpd.canUserLog(userName);
  		boolean canManage = mpd.canUserManage(userName);
  		boolean canModify = mpd.canUserModify(userName);
  		boolean canDel = mpd.canUserDel(userName);

		Config cfg = new Config();
		boolean isModuleHistory = cfg.getBooleanProperty("isModuleHistory");
		boolean isModuleLogRead = cfg.getBooleanProperty("isModuleLogRead");
		boolean isModuleLogModify = cfg.getBooleanProperty("isModuleLogModify");

		MacroCtlMgr mm = new MacroCtlMgr();
		UserMgr um = new UserMgr();
		WorkflowDb wf = new WorkflowDb();
		JSONArray rows = new JSONArray();
		try {
			jobject.put("rows", rows);
			jobject.put("page", curpage);
			jobject.put("total", lr.getTotal());	
			
			int len = fields.length;
			
			int k = 0;
			Iterator ir = lr.getResult().iterator();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				
				RequestUtil.setFormDAO(request, fdao);
				
				k++;
				JSONObject jo = new JSONObject();

				// prompt 图标
				if (isPrompt) {
					// 判断条件
					if (ModuleUtil.isPrompt(request, msd, fdao)) {
						jo.put("colPrompt", "<img src=\"" + SkinMgr.getSkinPath(request) + "/icons/prompt/" + promptIcon + "\" style=\"width:16px;\" align=\"absmiddle\" />");
					}				
				}
				
				long id = fdao.getId();
				jo.put("id", String.valueOf(id));

/*				if (isPrompt) {
					// 判断条件
					if (ModuleUtil.isPrompt(request, msd, fdao)) {
					}
				}*/

				for (int i=0; i<len; i++) {
					String fieldName = fields[i];
					
					String val = ""; // fdao.getFieldValue(fieldName);
					
					if (fieldName.startsWith("main:")) {
						String[] subFields = fieldName.split(":");
						if (subFields.length == 3) {
							// 20180730 fgf 此处查询的结果可能为多个，但是这时关联的是主表单，cws_id是唯一的，应该不需要查多个
							FormDb subfd = new FormDb(subFields[1]);
							FormDAO subfdao = new FormDAO(subfd);
							FormField subff = subfd.getFormField(subFields[2]);
							String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
							StringBuilder sb = new StringBuilder();
							try {
								JdbcTemplate jt = new JdbcTemplate();
								ResultIterator ri = jt.executeQuery(subsql);
								while (ri.hasNext()) {
									ResultRecord rr = (ResultRecord) ri.next();
									int subid = rr.getInt(1);
									subfdao = new FormDAO(subid, subfd);
									String subFieldValue = subfdao.getFieldValue(subFields[2]);
									if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
										MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
										if (mu != null) {
											subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
										}
									}
									sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							val += sb.toString();
						}
					} else if (fieldName.startsWith("other:")) {
						// 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
						String fName = fieldName;
						int logType = ParamUtil.getInt(request, "log_type", 0);
						if (logType==FormDAOLog.LOG_TYPE_DEL) {
							if (formCode.equals("module_log")) {
								if (fName.indexOf("module_id:") != -1) {
									int p = fName.indexOf(":");
									p = fName.indexOf(":", p+1);
									String prefix = fName.substring(0, p);
									fName = fName.substring(p + 1);
									p = fName.indexOf(":");
									String endStr = fName.substring(p);
									if (endStr.startsWith(":id:")) {
										// 将id替换为***_log表中的cws_log_id
										endStr = ":cws_log_id" + endStr.substring(3);
									}
									fName = fName.substring(0, p);
									fName += "_log";
									fName = prefix + ":" + fName + endStr;
								}
							}
						}
						val = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
					} 
					else if (fieldName.equals("ID")) {
						fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
						val = String.valueOf(fdao.getId());
					}
					else if (fieldName.equals("cws_progress")) {
						val = String.valueOf(fdao.getCwsProgress());
					}
					else if (fieldName.equals("cws_flag")) {
						val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
					}
					else if (fieldName.equals("cws_creator")) {
						String realName = "";
						if (fdao.getCreator()!=null) {
							UserDb user = um.getUserDb(fdao.getCreator());
							if (user!=null)
								realName = user.getRealName();
						}
						val = realName;
					}
					else if (fieldName.equals("flowId")) {
						val = "<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>";
					}
					else if (fieldName.equals("cws_status")) {
						val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
					}
					else if (fieldName.equals("cws_create_date")) {
						val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
					}
					else if (fieldName.equals("flow_begin_date")) {
						int flowId = fdao.getFlowId();
						if (flowId!=-1) {
							wf = wf.getWorkflowDb(flowId);
							val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
						}
					}
					else if (fieldName.equals("flow_end_date")) {
						int flowId = fdao.getFlowId();
						if (flowId!=-1) {
							wf = wf.getWorkflowDb(flowId);
							val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
						}
					}
					else {
						FormField ff = fdao.getFormField(fieldName);
						if (ff==null) {
							val += "不存在！";
						}
						else {
							if (ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null) {
									val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
								}
							}
							else {		
								val = FuncUtil.renderFieldValue(fdao, ff);
							}
						}
					}

					// DebugUtil.i(getClass(), "moduleList", fieldName + " link=" + fieldsLink[i]);

					if (!fieldsLink[i].equals("#") && !fieldsLink[i].equals("&") && !fieldsLink[i].startsWith("$")) {
						String link = FormUtil.parseAndSetFieldValue(fieldsLink[i], fdao);	
						if (!link.startsWith("http:")) {
							link = request.getContextPath() + "/" + link;
						}
						val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + link + "')\">" + val + "</a>";
			        }
			        else if (((i==0 && "#".equals(fieldsLink[i])) || "&".equals(fieldsLink[i])) && canView) {
			        	// 在第一列或者fieldsLink[i]为&的列上，生成查看链接
						if (msd.getInt("btn_display_show")==1) {
							if (msd.getInt("view_show")==ModuleSetupDb.VIEW_SHOW_CUSTOM) {			
								val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_show") + "?parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + val + "</a>";
							}
							else {
								val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_show.jsp?parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + val + "</a>";
							}
						}
					}
					
					jo.put(fieldName, val);
				}		
				
				StringBuffer sb = new StringBuffer();
				if (msd.getInt("btn_display_show")==1 && canView) {
					if (msd.getInt("view_show")==ModuleSetupDb.VIEW_SHOW_CUSTOM) {
				    	sb.append("<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_show") + "?parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
					}
					else { 
				    	sb.append("<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_show.jsp?parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
					}
					if (msd.getInt("btn_flow_show")==1) {
						if (fd.isFlow() && fdao.getFlowId()!=-1) { 
					        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));    
					    	sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + fdao.getFlowId() + "&visitKey=" + visitKey + "')\">流程</a>");
					    }
					}
			    }
				/*if (msd.getInt("btn_edit_show")==1 && canModify) {
					if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
			    		sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + id + "&id=" + id + "&code=" + code + "&formCode=" + formCode + "')\">修改</a>");
					}
					else {
			    		sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_edit.jsp?parentId=" + id + "&id=" + id + "&code=" + code + "&formCode=" + formCode + "')\">修改</a>");
					}
				}

				if (canDel || canManage) {
					if (msd.getInt("btn_edit_show")==1) {
						sb.append("&nbsp;&nbsp;<a onclick=\"del('" + id + "')\" href=\"javascript:;\">删除</a>");
					}
				}*/
				if (canLog || canManage){ 
			    	if (msd.getInt("btn_log_show")==1 && fd.isLog()) {
						String btnLogName = "日志";
			    		if (isModuleLogRead) {
							btnLogName = "修改日志";
						}
						String btnHisName = "历史";

						if (isModuleHistory) {
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('历史记录', '" + request.getContextPath() + "/visual/module_his_list.jsp?op=search&code=" + code + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnHisName + "</a>");
						}
						if (isModuleLogModify) {
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('修改日志', '" + request.getContextPath() + "/visual/module_log_list.jsp?op=search&code=" + code + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnLogName + "</a>");
						}

			    		if (isModuleLogRead) {
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('浏览日志', '" + request.getContextPath() + "/visual/module_list.jsp?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + code + "&module_id=" + id + "&form_code=" + formCode + "')\">浏览日志</a>");
						}
			    	}
			    }
				
				String op_link_name = StrUtil.getNullStr(msd.getString("op_link_name"));
				String[] linkNames = StrUtil.split(op_link_name, ",");
				String op_link_href = StrUtil.getNullStr(msd.getString("op_link_url"));
				String[] linkHrefs = StrUtil.split(op_link_href, ",");
				
				String op_link_field = StrUtil.getNullStr(msd.getString("op_link_field"));
				String[] linkFields = StrUtil.split(op_link_field, ",");
				String op_link_cond = StrUtil.getNullStr(msd.getString("op_link_cond"));
				String[] linkConds = StrUtil.split(op_link_cond, ",");
				String op_link_value = StrUtil.getNullStr(msd.getString("op_link_value"));
				String[] linkValues = StrUtil.split(op_link_value, ",");
				String op_link_event = StrUtil.getNullStr(msd.getString("op_link_event"));
				String[] linkEvents = StrUtil.split(op_link_event, ",");		
				String op_link_role = StrUtil.getNullStr(msd.getString("op_link_role"));
				String[] linkRoles = StrUtil.split(op_link_role, "#");		

				// 为兼容以前的版本，初始化tRole
				if (linkNames!=null) {
					if (linkRoles==null || linkRoles.length!=linkNames.length) {
						linkRoles = new String[linkNames.length];
						for (int i=0; i<linkNames.length; i++)
							linkRoles[i] = "";
					}
				}
				
				if (linkNames!=null) {
					for (int i=0; i<linkNames.length; i++) {
						String linkName = linkNames[i];
						
						String linkField = linkFields[i];
						String linkCond = linkConds[i];
						String linkValue = linkValues[i];
						String linkEvent = linkEvents[i];
						String linkRole = linkRoles[i];
						
						// 检查是否拥有权限
						if (!privilege.isUserPrivValid(request, "admin")) {
							boolean canSeeLink = false;
							if (!linkRole.equals("")) {
								String[] codeAry = StrUtil.split(linkRole, ",");
								if (codeAry!=null) {
									UserDb user = new UserDb();
									user = user.getUserDb(privilege.getUser(request));
									RoleDb[] rdAry = user.getRoles();
									if (rdAry!=null) {
										for (RoleDb rd : rdAry) {
											String roleCode = rd.getCode();
											for (String codeAllowed : codeAry) {
												if (roleCode.equals(codeAllowed)) {
													canSeeLink = true;
													break;
												}
											}
										}
									}
								}
								else {
									canSeeLink = true;
								}
							}
							else {
								canSeeLink = true;
							}
							
							if (!canSeeLink) {
								continue;
							}
						}						
						
						if (linkField.equals("#")) {
							linkField = "";
						}
						if (linkCond.equals("#")) {
							linkCond = "";
						}
						if (linkValue.equals("#")) {
							linkValue = "";
						}
						if (linkEvent.equals("#")) {
							linkEvent = "";
						}			
						if (linkField.equals("") || ModuleUtil.isLinkShow(request, msd, fdao, linkField, linkCond, linkValue)) {
							if ("click".equals(linkEvent)) {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "\">" + linkName + "</a>");
							}
							else {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + linkName + "', '" + request.getContextPath() + "/" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "')\">" + linkName + "</a>");
							}
						}
					}
				}

			    if(is_workLog==1) {
			    	sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "汇报', '" + request.getContextPath() + "/queryMyWork.action?code=" + mainFormCode + "&id=" + id + "')\">汇报</a>");
			    }
			    if (mpd.canUserReActive(privilege.getUser(request))) {
			    	MyActionDb mad = new MyActionDb();
			    	long flowId = fdao.getFlowId();
			    	if (flowId!=0 && flowId!=-1) {
						wf = wf.getWorkflowDb((int)flowId);
						WorkflowPredefineDb wpd = new WorkflowPredefineDb();
						wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
						boolean isReactive = false;
						if (wpd!=null) {
							isReactive = wpd.isReactive();
						}

						if (isReactive) {
			    			mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
							if (mad!=null) {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flow_dispose.jsp?myActionId=" + mad.getId() + "')\">变更</a>");
							}
						}
			    	}
			    }			
				
				jo.put("colOperate", sb.toString());
				rows.put(jo);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// System.out.println(getClass() + " " + jobject.toString());

		return jobject.toString();		
	}

	/**
	 * 恢复记录
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/restore", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})
	public String restore(@RequestParam(value = "id", required = true) long id) {		
		JSONObject json = new JSONObject();		
		boolean re = false;
		try {
			String moduleLog = "module_log";
			FormDb fd = new FormDb();
			fd = fd.getFormDb(moduleLog);
			
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(id, fd);
			
			String formCode = fdao.getFieldValue("form_code");
			long moduleId = StrUtil.toLong(fdao.getFieldValue("module_id"));
			long logId = StrUtil.toLong(fdao.getFieldValue("log_id"), -1);
			String fieldName = fdao.getFieldValue("field_name");
			int logType = StrUtil.toInt(fdao.getFieldValue("log_type"));

			// System.out.println("logType=" + logType + " formCode=" + formCode + " moduleId=" + moduleId + " logId=" + logId);

			// 找到之前的日志记录，恢复至上一条
			if (logType==FormDAOLog.LOG_TYPE_EDIT) {
				long privLogId = -1;
		        String sql = "select id from " + FormDb.getTableNameForLog(formCode) + " where cws_log_id = '" + moduleId + "' and id < " + logId + " order by id desc";
		        JdbcTemplate jt = new JdbcTemplate();
		        ResultIterator ri = jt.executeQuery(sql,1,1);
		    	if (ri.hasNext()) {
		    		ResultRecord rr = (ResultRecord)ri.next();
		    		privLogId = rr.getLong(1);
		    	}
		    	
		    	if (privLogId==-1) {
			    	json.put("ret", "0");
					json.put("msg", "恢复失败，之前不存在日志记录！");	    		
		    	}
		    	
		    	fd = fd.getFormDb(formCode);
				FormDAOLog fdaoLog = new FormDAOLog(fd);
				fdaoLog = fdaoLog.getFormDAOLog(privLogId);
								
				fdao = new com.redmoon.oa.visual.FormDAO();
				fdao = fdao.getFormDAO(moduleId, fd);
				if (fdao.isLoaded()) {
					fdao.setFieldValue(fieldName, fdaoLog.getFieldValue(fieldName));
					re = fdao.save();
				}
				else {
			    	json.put("ret", "0");
					json.put("msg", "记录已被删除，请先恢复被删除的记录");
					return json.toString();					
				}
			}
			else if (logType==FormDAOLog.LOG_TYPE_DEL) {
		    	fd = fd.getFormDb(formCode);
				FormDAOLog fdaoLog = new FormDAOLog(fd);
				fdaoLog = fdaoLog.getFormDAOLog(logId);
								
				fdao = new com.redmoon.oa.visual.FormDAO(fd);
				Vector v = fd.getFields();
				Iterator<FormField> ir = v.iterator();
				while (ir.hasNext()) {
					FormField ff = ir.next();
					fdao.setFieldValue(ff.getName(), fdaoLog.getFieldValue(ff.getName()));
				}
				fdao.setFlowId(fdaoLog.getFlowId());
				fdao.setCreator(fdaoLog.getCreator());
				fdao.setCwsId(fdaoLog.getCwsId());
				fdao.setCwsOrder(fdaoLog.getCwsOrder());
				fdao.setFlowTypeCode(fdaoLog.getFlowTypeCode());
				fdao.setUnitCode(fdaoLog.getUnitCode());
				re = fdao.create();			
			}
			
			if (re) {
		    	json.put("ret", "1");
				json.put("msg", "操作成功！");		
			}
			else {
		    	json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}


	@ResponseBody
	@RequestMapping(value = "/moduleEdit", method = RequestMethod.POST, produces={"application/json;charset=UTF-8;"})
	public String moduleEdit(@RequestParam(value = "id", required = true) long id, String code, String colName, String original_value, String update_value) {
		JSONObject json = new JSONObject();
		if (update_value.equals(original_value)) {
			try {
				json.put("ret", "-1");
				json.put("msg", "值未更改！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		Privilege privilege = new Privilege();
		ModulePrivDb mpd = new ModulePrivDb(code);
		if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
			try {
				json.put("ret", 0);
				json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd==null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}

		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);

		boolean re = false;
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		try {
			fdao = fdao.getFormDAO(id, fd);
			fdao.setFieldValue(colName, update_value);
			re = fdao.save();
			// 如果需要记录历史
			if (re && fd.isLog()) {
				FormDAO.log(privilege.getUser(request), FormDAOLog.LOG_TYPE_EDIT, fdao);
			}

			// 相关的脚本事件暂不考虑调用，因为涉及到FileUpload，而此时不存在此变量
		}
		catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			return json.toString();
		}

		try {
			if (re) {
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", 0);
				json.put("msg", "操作失败");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json.toString();
	}


	/**
	 * 删除附件日志
	 * @param ids
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delLog", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
	public String delLog(String ids) {
		JSONObject json = new JSONObject();

		String[] ary = StrUtil.split(ids, ",");
		if (ary==null) {
			try {
				json.put("ret", "0");
				json.put("msg", "请选择记录！");
			}catch (JSONException e){
				e.printStackTrace();
			}
			return json.toString();
		}

		try {
			boolean re = false;
			for (String strId : ary) {
				long id = StrUtil.toLong(strId, -1);
				if (id!=-1) {
					AttachmentLogDb ald = new AttachmentLogDb();
					ald = (AttachmentLogDb)ald.getQObjectDb(id);

					boolean isValid = false;
					com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
					if (pvg.isUserPrivValid(request, "admin")) {
						isValid = true;
					}
					else {
						Attachment att = new Attachment((int)ald.getLong("att_id"));
						String formCode = att.getFormCode();
						ModulePrivDb mpd = new ModulePrivDb(formCode);
						if (mpd.canUserManage(pvg.getUser(request))) {
							isValid = true;
						}
					}

					if (!isValid) {
						json.put("ret", "0");
						json.put("msg", "权限非法！");
						return json.toString();
					}

					if (isValid) {
						re = ald.del();
					}
				}
				else {
					json.put("ret", "0");
					json.put("msg", "标识非法！");
					return json.toString();
				}
			}

			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}


	/**
	 * 列出下载日志
	 * @param moduleId
	 * @param attId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listAttLog", method = RequestMethod.POST, produces={"text/html;", "application/json;charset=UTF-8;"})
	public String listAttLog(long moduleId, long attId) {
		AttachmentLogDb ald = new AttachmentLogDb();
		String sql = ald.getQuery(request, moduleId, attId);
		DebugUtil.i(getClass(), "listAttLog", sql);
		int pageSize = ParamUtil.getInt(request, "rp", 20);
		int curPage = ParamUtil.getInt(request, "page", 1);
		ListResult lr = null;
		try {
			lr = ald.listResult(sql, curPage, pageSize);
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONArray rows = new JSONArray();
		JSONObject jobject = new JSONObject();

		try {
			jobject.put("rows", rows);
			jobject.put("page", curPage);
			jobject.put("total", lr.getTotal());
		}catch (JSONException e){
			e.printStackTrace();
		}

		UserDb user = new UserDb();
		Iterator ir = lr.getResult().iterator();
		while (ir.hasNext()) {
			ald = (AttachmentLogDb)ir.next();
			JSONObject jo = new JSONObject();
			try {
				jo.put("id", String.valueOf(ald.getLong("id")));
				jo.put("logTime", DateUtil.format(ald.getDate("log_time"), "yyyy-MM-dd HH:mm:ss"));

				user = user.getUserDb(ald.getString("user_name"));
				jo.put("realName", user.getRealName());

				Attachment att = new Attachment((int) ald.getLong("att_id"));
				jo.put("attName", att.getName());

				jo.put("logType", AttachmentLogDb.getTypeDesc(ald.getInt("log_type")));
			}catch (JSONException e){
				e.printStackTrace();
			}

			rows.put(jo);
		}

		return jobject.toString();
	}


}
