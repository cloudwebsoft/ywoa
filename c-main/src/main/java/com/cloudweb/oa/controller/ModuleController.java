package com.cloudweb.oa.controller;

import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.api.INestTableCtl;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.service.ModuleService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.util.ExcelUploadUtil;
import com.redmoon.oa.util.WordUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.AttachmentLogDb;
import com.redmoon.oa.visual.Render;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Label;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	@Autowired
	private ModuleService moduleService;

	@ResponseBody
	@RequestMapping(value = "/moduleSetUnitCode", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
	public String setUnitCode() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
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
				e.printStackTrace();
			}
			return json.toString();
		}

		String strIds = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strIds, ",");
		if (ids == null) {
			try {
				json.put("ret", 0);
				json.put("msg", "请选择记录！");
			} catch (JSONException e) {
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
			for (int i = 0; i < len; i++) {
				int id = StrUtil.toInt(ids[i]);
				fdao = fdao.getFormDAO(id, fd);
				fdao.setUnitCode(toUnitCode);
				fdao.save();
			}
			json.put("ret", "1");
			json.put("msg", "操作成功！");
		} catch (ErrMsgException e) {
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/moduleBatchOp", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
	public String moduleBatchOp() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
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
		} catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
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
			e.printStackTrace();
		}

		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/moduleDel", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
	public String moduleDel() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
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
			re = fdm.del(request, false, code);
		} catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
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
			e.printStackTrace();
		}

		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/moduleDelRelate", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
	public String moduleDelRelate() {
		String code = ParamUtil.get(request, "code");
		JSONObject json = new JSONObject();
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
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
			re = fdm.delRelate(request, msd);
		} catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
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
			e.printStackTrace();
		}

		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/moduleList", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
	public String moduleList() {
		// String op = ParamUtil.get(request, "op");
		String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位

		String code = ParamUtil.get(request, "code");
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
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
		} catch (ErrMsgException e) {
			DebugUtil.e(getClass(), "moduleList", "SQL：" + e.getMessage());
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
			e1.printStackTrace();
			try {
				jobject.put("page", 1);
				jobject.put("total", 0);
			} catch (JSONException e) {
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
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();

				RequestUtil.setFormDAO(request, fdao);

				k++;
				JSONObject jo = new JSONObject();

				// prompt 图标
				if (isPrompt) {
					// 判断条件
					if (ModuleUtil.isPrompt(request, msd, fdao)) {
						jo.put("colPrompt", "<img src=\"" + request.getContextPath() + "/images/prompt/" + promptIcon + "\" style=\"width:16px;\" align=\"absmiddle\" />");
					}
				}

				long id = fdao.getId();
				jo.put("id", String.valueOf(id));

/*				if (isPrompt) {
					// 判断条件
					if (ModuleUtil.isPrompt(request, msd, fdao)) {
					}
				}*/

				for (int i = 0; i < len; i++) {
					String fieldName = fields[i];

					String val = ""; // fdao.getFieldValue(fieldName);

					if (fieldName.startsWith("main:")) {
						String[] subFields = fieldName.split(":");
						if (subFields.length == 3) {
							// 20180730 fgf 此处查询的结果可能为多个，但是这时关联的是主表单，cws_id是唯一的，应该不需要查多个
							FormDb subfd = new FormDb(subFields[1]);
							com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
							FormField subff = subfd.getFormField(subFields[2]);
							String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
							StringBuilder sb = new StringBuilder();
							try {
								JdbcTemplate jt = new JdbcTemplate();
								ResultIterator ri = jt.executeQuery(subsql);
								while (ri.hasNext()) {
									ResultRecord rr = (ResultRecord) ri.next();
									int subid = rr.getInt(1);
									subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
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
						if (logType == FormDAOLog.LOG_TYPE_DEL) {
							if (formCode.equals("module_log")) {
								if (fName.indexOf("module_id:") != -1) {
									int p = fName.indexOf(":");
									p = fName.indexOf(":", p + 1);
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
					} else if (fieldName.equals("ID")) {
						fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
						val = String.valueOf(fdao.getId());
					} else if (fieldName.equals("cws_progress")) {
						val = String.valueOf(fdao.getCwsProgress());
					} else if (fieldName.equals("cws_flag")) {
						val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
					} else if (fieldName.equals("cws_creator")) {
						String realName = "";
						if (fdao.getCreator() != null) {
							UserDb user = um.getUserDb(fdao.getCreator());
							if (user != null)
								realName = user.getRealName();
						}
						val = realName;
					} else if (fieldName.equals("flowId")) {
						val = "<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>";
					} else if (fieldName.equals("cws_status")) {
						val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
					} else if (fieldName.equals("cws_create_date")) {
						val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
					} else if (fieldName.equals("flow_begin_date")) {
						int flowId = fdao.getFlowId();
						if (flowId != -1) {
							wf = wf.getWorkflowDb(flowId);
							val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
						}
					} else if (fieldName.equals("flow_end_date")) {
						int flowId = fdao.getFlowId();
						if (flowId != -1) {
							wf = wf.getWorkflowDb(flowId);
							val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
						}
					} else if (fieldName.equals("cws_id")) {
						val = fdao.getCwsId();
					} else {
						FormField ff = fdao.getFormField(fieldName);
						if (ff == null) {
							val += "不存在！";
						} else {
							if (ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null) {
									val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
								}
							} else {
								val = FuncUtil.renderFieldValue(fdao, ff);
							}
						}
					}

					// DebugUtil.i(getClass(), "moduleList", fieldName + " link=" + fieldsLink[i]);

					if (!"#".equals(fieldsLink[i]) && !"&".equals(fieldsLink[i]) && !fieldsLink[i].startsWith("$")) {
						String link = FormUtil.parseAndSetFieldValue(fieldsLink[i], fdao);
						if (!link.startsWith("http")) {
							link = request.getContextPath() + "/" + link;
						}
						val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + link + "')\">" + val + "</a>";
					} else if (((i == 0 && "#".equals(fieldsLink[i])) || "&".equals(fieldsLink[i])) && canView) {
						// 在第一列或者fieldsLink[i]为&的列上，生成查看链接
						if (msd.getInt("btn_display_show") == 1) {
							if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
								val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_show") + "?parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + val + "</a>";
							} else {
								val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_show.jsp?parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + val + "</a>";
							}
						}
					}

					jo.put(fieldName, val);
				}

				StringBuffer sb = new StringBuffer();
				if (msd.getInt("btn_display_show") == 1 && canView) {
					if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
						sb.append("<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_show") + "?parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
					} else {
						sb.append("<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_show.jsp?parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
					}
					if (msd.getInt("btn_flow_show") == 1) {
						if (fd.isFlow() && fdao.getFlowId() != -1) {
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
				if (canLog || canManage) {
					if (msd.getInt("btn_log_show") == 1 && fd.isLog()) {
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
				if (linkNames != null) {
					if (linkRoles == null || linkRoles.length != linkNames.length) {
						linkRoles = new String[linkNames.length];
						for (int i = 0; i < linkNames.length; i++)
							linkRoles[i] = "";
					}
				}

				if (linkNames != null) {
					for (int i = 0; i < linkNames.length; i++) {
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
								if (codeAry != null) {
									UserDb user = new UserDb();
									user = user.getUserDb(privilege.getUser(request));
									RoleDb[] rdAry = user.getRoles();
									if (rdAry != null) {
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
								} else {
									canSeeLink = true;
								}
							} else {
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
							} else {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + linkName + "', '" + request.getContextPath() + "/" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "')\">" + linkName + "</a>");
							}
						}
					}
				}

				if (is_workLog == 1) {
					sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "汇报', '" + request.getContextPath() + "/queryMyWork.action?code=" + mainFormCode + "&id=" + id + "')\">汇报</a>");
				}
				if (mpd.canUserReActive(privilege.getUser(request))) {
					MyActionDb mad = new MyActionDb();
					long flowId = fdao.getFlowId();
					if (flowId != 0 && flowId != -1) {
						wf = wf.getWorkflowDb((int) flowId);
						WorkflowPredefineDb wpd = new WorkflowPredefineDb();
						wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
						boolean isReactive = false;
						if (wpd != null) {
							isReactive = wpd.isReactive();
						}

						if (isReactive) {
							mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
							if (mad != null) {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flow_dispose.jsp?myActionId=" + mad.getId() + "')\">变更</a>");
							}
						}
					}
				}

				jo.put("colOperate", sb.toString());
				rows.put(jo);
			}

			if (rows.length() > 0) {
				String propStat = msd.getString("prop_stat");
				if (StringUtils.isNotEmpty(propStat)) {
					if (propStat.equals("")) {
						propStat = "{}";
					}
					JSONObject json = new JSONObject(propStat);
					JSONObject jo = new JSONObject();
					Iterator ir3 = json.keys();
					int n = 0;
					while (ir3.hasNext()) {
						String fieldName = (String) ir3.next();
						String modeStat = json.getString(fieldName);

						FormField ff = fd.getFormField(fieldName);
						if (ff == null) {
							DebugUtil.e(getClass(), "moduleList", "field:" + fieldName + " is not exist");
						}
						int fieldType = ff.getFieldType();

						double sumVal = FormSQLBuilder.getSUMOfSQL(sql, fieldName);
						if (modeStat.equals("0")) {
							if (fieldType == FormField.FIELD_TYPE_INT
									|| fieldType == FormField.FIELD_TYPE_LONG) {
								jo.put(fieldName, "合计：" + (long) sumVal);
							} else {
								jo.put(fieldName, "合计：" + NumberUtil.round(sumVal, 2));
							}
						} else if (modeStat.equals("1")) {
							jo.put(fieldName, "平均：" + NumberUtil.round(sumVal / lr.getTotal(), 2));
						}
						n++;
					}
					if (n > 0) {
						jo.put("id", String.valueOf(ConstUtil.MODULE_ID_STAT));
						rows.put(jo);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// System.out.println(getClass() + " " + jobject.toString());

		return jobject.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/moduleListRelate", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
	public String moduleListRelate() {
		JSONObject jobject = new JSONObject();
		long parentId = ParamUtil.getLong(request, "parentId", -1);

		String formCode = ParamUtil.get(request, "formCode");
		String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
		String menuItem = ParamUtil.get(request, "menuItem");
		String moduleCode = ParamUtil.get(request, "code");

		ModuleSetupDb parentMsd = new ModuleSetupDb();
		parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);
		formCode = parentMsd.getString("form_code");

		String mode = ParamUtil.get(request, "mode");
		String tagName = ParamUtil.get(request, "tagName");

		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
		if (msd == null) {
			LogUtil.getLog(getClass()).error("模块：" + moduleCodeRelated + "不存在！");
			return "";
		}

		String formCodeRelated = msd.getString("form_code");
		boolean isEditInplace = msd.getInt("is_edit_inplace") == 1;

		// 通过选项卡标签关联
		boolean isSubTagRelated = "subTagRelated".equals(mode);

		if (isSubTagRelated) {
			String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
			try {
				JSONObject json = new JSONObject(tagUrl);
				if (json.has("viewList")) {
					int viewList = StrUtil.toInt(json.getString("viewList"), ModuleSetupDb.VIEW_DEFAULT);
					if (viewList == ModuleSetupDb.VIEW_LIST_GANTT) {
						LogUtil.getLog(getClass()).error("模块：" + moduleCodeRelated + "需用甘特图视图显示！");
						jobject.put("page", 1);
						jobject.put("total", 0);
						jobject.put("msg", "模块：" + moduleCodeRelated + "需用甘特图视图显示！");
						return jobject.toString();
					}
				}
				if (!json.isNull("formRelated")) {
					moduleCodeRelated = json.getString("formRelated");
					msd = msd.getModuleSetupDb(moduleCodeRelated);
					formCodeRelated = msd.getString("form_code");
					isEditInplace = msd.getInt("is_edit_inplace") == 1;
				} else {
					LogUtil.getLog(getClass()).error("关联模块：" + moduleCodeRelated + "选项卡关联配置不正确！");
					jobject.put("page", 1);
					jobject.put("total", 0);
					jobject.put("msg", "选项卡关联配置不正确！");
					return jobject.toString();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		String relateFieldValue = "";
		if (parentId == -1) {
			LogUtil.getLog(getClass()).error("缺少父模块记录的ID！");
			try {
				jobject.put("page", 1);
				jobject.put("total", 0);
				jobject.put("msg", "缺少父模块记录的ID！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jobject.toString();
		} else {
			if (!isSubTagRelated) {
				com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
				relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
				if (relateFieldValue == null) {
					// 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
					relateFieldValue = SQLBuilder.IS_NOT_RELATED;
					// out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
					// return;
				}
			}
		}

		// String op = ParamUtil.get(request, "op");
		String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位

		ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);

		// String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String[] fields = msd.getColAry(false, "list_field");
		String[] fieldsLink = msd.getColAry(false, "list_field_link");

		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCodeRelated);

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
		if (sort.equals("")) {
			sort = "desc";
		}

		com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		String desKey = ssoCfg.get("key");

		// 用于传过滤条件
		request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
		String[] ary = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);

		String sql = ary[0];

		DebugUtil.log(getClass(), "moduleListRelate", "sql=" + sql);

		FormDAO fdao = new FormDAO();

		// layui 的默认值为10
		int pagesize = ParamUtil.getInt(request, "limit", -1);
		if (pagesize == -1) {
			pagesize = ParamUtil.getInt(request, "rp", 20);
		}
		int curpage = ParamUtil.getInt(request, "page", 1);

		ListResult lr = null;
		try {
			lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
		} catch (ErrMsgException e1) {
			e1.printStackTrace();
			try {
				jobject.put("page", 1);
				jobject.put("total", 0);
			} catch (JSONException e) {
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
			jobject.put("errCode", 0);
			jobject.put("rows", rows);
			jobject.put("page", curpage);
			jobject.put("total", lr.getTotal());

			int len = fields.length;

			int k = 0;
			Iterator ir = lr.getResult().iterator();
			while (ir != null && ir.hasNext()) {
				fdao = (FormDAO) ir.next();

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

				for (int i = 0; i < len; i++) {
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
						if (logType == FormDAOLog.LOG_TYPE_DEL) {
							if (formCode.equals("module_log")) {
								if (fName.indexOf("module_id:") != -1) {
									int p = fName.indexOf(":");
									p = fName.indexOf(":", p + 1);
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
					} else if (fieldName.equals("ID")) {
						fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
						val = String.valueOf(fdao.getId());
					} else if (fieldName.equals("cws_progress")) {
						val = String.valueOf(fdao.getCwsProgress());
					} else if (fieldName.equals("cws_flag")) {
						val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
					} else if (fieldName.equals("cws_creator")) {
						String realName = "";
						if (fdao.getCreator() != null) {
							UserDb user = um.getUserDb(fdao.getCreator());
							if (user != null)
								realName = user.getRealName();
						}
						val = realName;
					} else if (fieldName.equals("flowId")) {
						val = "<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>";
					} else if (fieldName.equals("cws_status")) {
						val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
					} else if (fieldName.equals("cws_create_date")) {
						val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
					} else if (fieldName.equals("flow_begin_date")) {
						int flowId = fdao.getFlowId();
						if (flowId != -1) {
							wf = wf.getWorkflowDb(flowId);
							val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
						}
					} else if (fieldName.equals("flow_end_date")) {
						int flowId = fdao.getFlowId();
						if (flowId != -1) {
							wf = wf.getWorkflowDb(flowId);
							val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
						}
					} else if (fieldName.equals("cws_id")) {
						val = fdao.getCwsId();
					} else {
						FormField ff = fdao.getFormField(fieldName);
						if (ff == null) {
							val += "不存在！";
						} else {
							if (ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null) {
									val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
								}
							} else {
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
					} else if (((i == 0 && "#".equals(fieldsLink[i])) || "&".equals(fieldsLink[i])) && canView) {
						// 在第一列或者fieldsLink[i]为&的列上，生成查看链接
						if (msd.getInt("btn_display_show") == 1) {
							if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
								val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_show") + "?mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "')\">" + val + "</a>";
							} else {
								val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_show_relate.jsp?menuItem=" + menuItem + "&mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "')\">" + val + "</a>";
							}
						}
					}

					jo.put(fieldName, val);
				}

				StringBuffer sb = new StringBuffer();
				if (msd.getInt("btn_display_show") == 1 && canView) {
					if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
						sb.append("<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_show") + "?mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&parentModuleCode=" + moduleCode + "&code=" + moduleCodeRelated + "&isShowNav=0')\">查看</a>");
					} else {
						sb.append("<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_show_relate.jsp?menuItem=" + menuItem + "&mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "&menuItem=" + menuItem + "&isShowNav=0')\">查看</a>");
					}
					if (msd.getInt("btn_flow_show") == 1) {
						if (fd.isFlow() && fdao.getFlowId() != -1) {
							String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + fdao.getFlowId() + "&visitKey=" + visitKey + "')\">流程</a>");
						}
					}
				}

				if (canLog || canManage) {
					if (msd.getInt("btn_log_show") == 1 && fd.isLog()) {
						String btnLogName = "日志";
						if (isModuleLogRead) {
							btnLogName = "修改日志";
						}
						String btnHisName = "历史";

						if (isModuleHistory) {
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('历史记录', '" + request.getContextPath() + "/visual/module_his_list.jsp?op=search&code=" + moduleCodeRelated + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnHisName + "</a>");
						}
						if (isModuleLogModify) {
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('修改日志', '" + request.getContextPath() + "/visual/module_log_list.jsp?op=search&code=" + moduleCodeRelated + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnLogName + "</a>");
						}

						if (isModuleLogRead) {
							sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('浏览日志', '" + request.getContextPath() + "/visual/module_list.jsp?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + moduleCodeRelated + "&module_id=" + id + "&form_code=" + formCode + "')\">浏览日志</a>");
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
				if (linkNames != null) {
					if (linkRoles == null || linkRoles.length != linkNames.length) {
						linkRoles = new String[linkNames.length];
						for (int i = 0; i < linkNames.length; i++)
							linkRoles[i] = "";
					}
				}

				if (linkNames != null) {
					for (int i = 0; i < linkNames.length; i++) {
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
								if (codeAry != null) {
									UserDb user = new UserDb();
									user = user.getUserDb(privilege.getUser(request));
									RoleDb[] rdAry = user.getRoles();
									if (rdAry != null) {
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
								} else {
									canSeeLink = true;
								}
							} else {
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
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, moduleCodeRelated) + "\">" + linkName + "</a>");
							} else {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + linkName + "', '" + request.getContextPath() + "/" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, moduleCodeRelated) + "')\">" + linkName + "</a>");
							}
						}
					}
				}

				if (mpd.canUserReActive(privilege.getUser(request))) {
					MyActionDb mad = new MyActionDb();
					long flowId = fdao.getFlowId();
					if (flowId != 0 && flowId != -1) {
						wf = wf.getWorkflowDb((int) flowId);
						WorkflowPredefineDb wpd = new WorkflowPredefineDb();
						wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
						boolean isReactive = false;
						if (wpd != null) {
							isReactive = wpd.isReactive();
						}

						if (isReactive) {
							mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
							if (mad != null) {
								sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flow_dispose.jsp?myActionId=" + mad.getId() + "')\">变更</a>");
							}
						}
					}
				}

				jo.put("colOperate", sb.toString());
				rows.put(jo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jobject.toString();
	}

	/**
	 * 恢复记录
	 *
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/restore", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
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
			if (logType == FormDAOLog.LOG_TYPE_EDIT) {
				long privLogId = -1;
				String sql = "select id from " + FormDb.getTableNameForLog(formCode) + " where cws_log_id = '" + moduleId + "' and id < " + logId + " order by id desc";
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = jt.executeQuery(sql, 1, 1);
				if (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
					privLogId = rr.getLong(1);
				}

				if (privLogId == -1) {
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
				} else {
					json.put("ret", "0");
					json.put("msg", "记录已被删除，请先恢复被删除的记录");
					return json.toString();
				}
			} else if (logType == FormDAOLog.LOG_TYPE_DEL) {
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
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 在位编辑
	 *
	 * @param id
	 * @param code
	 * @param colName
	 * @param original_value
	 * @param update_value
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/moduleEditInPlace", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
	public String moduleEditInPlace(@RequestParam(value = "id", required = true) long id, String code, String colName, String original_value, String update_value) {
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
		if (msd == null) {
			try {
				json.put("ret", 0);
				json.put("msg", "模块：" + code + "不存在！");
			} catch (JSONException e) {
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
		} catch (ErrMsgException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());
			} catch (JSONException ex) {
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
			e.printStackTrace();
		}

		return json.toString();
	}


	/**
	 * 删除附件日志
	 *
	 * @param ids
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delLog", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
	public String delLog(String ids) {
		JSONObject json = new JSONObject();

		String[] ary = StrUtil.split(ids, ",");
		if (ary == null) {
			try {
				json.put("ret", "0");
				json.put("msg", "请选择记录！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		try {
			boolean re = false;
			for (String strId : ary) {
				long id = StrUtil.toLong(strId, -1);
				if (id != -1) {
					AttachmentLogDb ald = new AttachmentLogDb();
					ald = (AttachmentLogDb) ald.getQObjectDb(id);

					boolean isValid = false;
					com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
					if (pvg.isUserPrivValid(request, "admin")) {
						isValid = true;
					} else {
						Attachment att = new Attachment((int) ald.getLong("att_id"));
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
				} else {
					json.put("ret", "0");
					json.put("msg", "标识非法！");
					return json.toString();
				}
			}

			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ResKeyException e) {
			e.printStackTrace();
		}
		return json.toString();
	}


	/**
	 * 列出下载日志
	 *
	 * @param moduleId
	 * @param attId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listAttLog", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
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
			e.printStackTrace();
		}

		JSONArray rows = new JSONArray();
		JSONObject jobject = new JSONObject();

		try {
			jobject.put("rows", rows);
			jobject.put("page", curPage);
			jobject.put("total", lr.getTotal());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		UserDb user = new UserDb();
		Iterator ir = lr.getResult().iterator();
		while (ir.hasNext()) {
			ald = (AttachmentLogDb) ir.next();
			JSONObject jo = new JSONObject();
			try {
				jo.put("id", String.valueOf(ald.getLong("id")));
				jo.put("logTime", DateUtil.format(ald.getDate("log_time"), "yyyy-MM-dd HH:mm:ss"));

				user = user.getUserDb(ald.getString("user_name"));
				jo.put("realName", user.getRealName());

				Attachment att = new Attachment((int) ald.getLong("att_id"));
				jo.put("attName", att.getName());

				jo.put("logType", AttachmentLogDb.getTypeDesc(ald.getInt("log_type")));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			rows.put(jo);
		}

		return jobject.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
	public String create(HttpServletRequest request) throws ErrMsgException {
		return moduleService.create(request);
	}

	@ResponseBody
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
	public String update(HttpServletRequest request) throws ErrMsgException {
		return moduleService.update(request);
	}

	@ResponseBody
	@RequestMapping(value = "/delAttach", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
	public String delAttach(HttpServletRequest request) {
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		Privilege privilege = new Privilege();
		String code = ParamUtil.get(request, "code");
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			json.put("ret", "0");
			json.put("msg", "模块不存在！");
			return json.toString();
		}

		ModulePrivDb mpd = new ModulePrivDb(code);
		if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
			json.put("ret", "0");
			json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
			return json.toString();
		}

		int attachId = ParamUtil.getInt(request, "attachId", -1);
		com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
		boolean re = att.del();
		if (re) {
			json.put("ret", "1");
			json.put("msg", "操作成功！");
		} else {
			json.put("ret", "0");
			json.put("msg", "操作失败！");
		}
		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/createRelate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
	public String createRelate(HttpServletRequest request) throws ErrMsgException {
		return moduleService.createRelate(request);
	}

	@ResponseBody
	@RequestMapping(value = "/updateRelate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
	public String updateRelate(HttpServletRequest request) throws ErrMsgException {
		return moduleService.updateRelate(request);
	}

	@ResponseBody
	@RequestMapping(value = "/delAttachRelate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
	public String delAttachRelate(HttpServletRequest request) {
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		Privilege privilege = new Privilege();

		// 取从模块编码
		String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
		if ("".equals(moduleCodeRelated)) {
			json.put("ret", "0");
			json.put("msg", "缺少关联模块编码");
			return json.toString();
		}

		// 取主模块编码
		String moduleCode = ParamUtil.get(request, "code");

		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
		if (msd == null) {
			json.put("ret", "0");
			json.put("msg", "模块不存在");
			return json.toString();
		}
		String formCodeRelated = msd.getString("form_code");

		ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
		if (!mpd.canUserModify(privilege.getUser(request))) {
			json.put("ret", "0");
			json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
			return json.toString();
		}

		int id = ParamUtil.getInt(request, "id", -1);
		if (id == -1) {
			json.put("ret", "0");
			json.put("msg", SkinUtil.LoadString(request, "err_id"));
			return json.toString();
		}

		// 检查数据权限，判断用户是否可以存取此条数据
		ModuleSetupDb parentMsd = new ModuleSetupDb();
		parentMsd = parentMsd.getModuleSetupDb(moduleCode);
		if (parentMsd == null) {
			json.put("ret", "0");
			json.put("msg", "父模块不存在");
			return json.toString();
		}
		String parentFormCode = parentMsd.getString("form_code");
		String mode = ParamUtil.get(request, "mode");
		// 是否通过选项卡标签关联
		boolean isSubTagRelated = "subTagRelated".equals(mode);
		String relateFieldValue = "";
		int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
		if (parentId == -1) {
			json.put("ret", "0");
			json.put("msg", "缺少父模块记录的ID");
			return json.toString();
		} else {
			if (!isSubTagRelated) {
				com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
				relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
				if (relateFieldValue == null) {
					// 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
					relateFieldValue = SQLBuilder.IS_NOT_RELATED;
				}
			}
		}
		if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
			I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
			json.put("ret", "0");
			json.put("msg", i18nUtil.get("info_access_data_fail"));
			return json.toString();
		}

		int attachId = ParamUtil.getInt(request, "attachId", -1);
		com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
		boolean re = false;
		if (att.isLoaded()) {
			re = att.del();
		}
		if (re) {
			json.put("ret", "1");
			json.put("msg", "操作成功！");
		} else {
			json.put("ret", "0");
			json.put("msg", "操作失败！");
		}
		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/moduleListCalendar", produces = {"application/json;charset=UTF-8;"})
	public String moduleListCalendar(@RequestParam(value = "code", required = true) String code, String start, String end) {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			LogUtil.getLog(getClass()).error("模块：" + code + "不存在！");
			return "";
		}

		com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
		ModulePrivDb mpd = new ModulePrivDb(code);
		if (!mpd.canUserSee(userName)) {
			return arr.toString();
		}

		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);

		String orderBy = "id";
		String sort = "desc";

		// 用于传过滤条件
		request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
		String[] ary = null;
		try {
			String op = "";
			ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
		} catch (ErrMsgException e) {
			DebugUtil.i(getClass(), "moduleListCalendar", "SQL：" + e.getMessage());
			return "";
		}

		String sql = ary[0];
		// sql拼接入时间段
		String fieldBeginDate = msd.getString("field_begin_date");
		String fieldEndDate = msd.getString("field_end_date");
		int p = sql.lastIndexOf(" order ");
		String tmp = " ((" + fieldBeginDate + " between " + StrUtil.sqlstr(start) + " and " + StrUtil.sqlstr(end) + ") or (" + fieldEndDate + " between " + StrUtil.sqlstr(start) + " and " + StrUtil.sqlstr(end) + "))";
		if (p == -1) {
			sql += " and " + tmp;
		} else {
			String sqlPrefix = sql.substring(0, p);
			String sqlSuffix = sql.substring(p);
			sql = sqlPrefix + " and " + tmp + sqlSuffix;
		}

		MacroCtlMgr mm = new MacroCtlMgr();
		String fieldName = msd.getString("field_name");
		String fieldDesc = msd.getString("field_desc");
		String fieldLabel = msd.getString("field_label");
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		try {
			Vector v = fdao.list(formCode, sql);
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();

				String bd = fdao.getFieldValue(fieldBeginDate);
				String ed = fdao.getFieldValue(fieldEndDate);
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

				json.put("start", bd);
				json.put("end", ed);

				String name = "";
				if (!StringUtils.isEmpty(fieldName)) {
					FormField ff = fdao.getFormField(fieldName);
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							name = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
						}
					} else {
						name = FuncUtil.renderFieldValue(fdao, ff);
					}
				}

				String desc = "";
				if (!StringUtils.isEmpty(fieldDesc)) {
					FormField ff = fdao.getFormField(fieldDesc);
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							desc = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
						}
					} else {
						desc = FuncUtil.renderFieldValue(fdao, ff);
					}
				}

				String label = "";
				if (!StringUtils.isEmpty(fieldLabel)) {
					FormField ff = fdao.getFormField(fieldLabel);
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							label = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
						}
					} else {
						label = FuncUtil.renderFieldValue(fdao, ff);
					}
				}

				String t = name;
				if (!"".equals(desc)) {
					t += " " + desc;
				}
				if (!"".equals(label)) {
					t += " " + label;
				}
				json.put("title", t);
				json.put("id", fdao.getId());
				arr.add(json);
			}
		} catch (ErrMsgException e) {
			e.printStackTrace();
		}
		return arr.toString();
	}

	@RequestMapping("/exportExcel")
	public void exportExcel(HttpServletResponse response) throws IOException, ErrMsgException, JSONException {
		// 未使用模板导出，即默认导出时，将合并嵌套表的单元格
		Privilege privilege = new Privilege();
		String code = ParamUtil.get(request, "code");
		if ("".equals(code)) {
			code = ParamUtil.get(request, "formCode");
		}
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			throw new ErrMsgException("模块不存在！");
		}

		long templateId = ParamUtil.getLong(request, "templateId", -1);

		request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

		String formCode = msd.getString("form_code");

		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		if (!fd.isLoaded()) {
			throw new ErrMsgException("表单不存在！");
		}
		String op = ParamUtil.get(request, "op");
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

		if (sort.equals("")) {
			sort = "desc";
		}

		String[] ary = null;
		boolean isMine = "true".equals(ParamUtil.get(request, "isMine"));
		if (isMine) {
			ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort, privilege.getUser(request), "user_name");
		} else {
			ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
		}
		String sql = ary[0];

		if ("module_log".equals(formCode)) {
			sql = SQLBuilder.getListSqlForLogRelateModule(request, sql);
		}

		DebugUtil.i(getClass(), "sql", sql);
		// String sqlUrlStr = ary[1];

		String listFieldOrgi = StrUtil.getNullStr(msd.getString("list_field"));
		String listField;
		String cols = ParamUtil.get(request, "cols");
		if (!"".equals(cols)) {
			listField = cols;
		}
		else {
			listField = listFieldOrgi;
		}

		// 如果用户自行调整了列，则cols在split后的数组与fieldsTitle不对应，此处重新调整了对应关系
		String[] fieldsTitle = msd.getColAry(false, "list_field_title");
		String[] fields = StrUtil.split(listField, ",");
		if (!"".equals(cols) && !listFieldOrgi.equals(cols)) {
			String[] t = new String[fields.length];
			String[] fieldsOrgi = StrUtil.split(listFieldOrgi, ",");
			for (int i = 0; i < fields.length; i++) {
				for (int j = 0; j < fieldsOrgi.length; j++) {
					if (fieldsOrgi[j].equals(fields[i])) {
						t[i] = fieldsTitle[j];
					}
				}
			}
			fieldsTitle = t;
		}

		String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
		String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
		String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
		boolean isPrompt = false;
		if (!"".equals(promptField) && !"".equals(promptIcon)) {
			isPrompt = true;
		}

		// 是否导出全部字段
		boolean isAll = ParamUtil.getBoolean(request, "isAll", false);
		// 主表字段与嵌套表formCode对应关系
		HashMap<String, String> nestMapping = new HashMap<String, String>();
		// 嵌套表需显示的字段
		HashMap<String, String> nestFieldName = new HashMap<String, String>();
		// 嵌套表需显示的字段的对应名称
		HashMap<String, String[]> nestFields = new HashMap<String, String[]>();
		// 嵌套表的id数据集
		HashMap<String, Vector> nestData = new HashMap<String, Vector>();
		// 列宽
		HashMap<Integer, Integer> columnWidthMap = new HashMap<Integer, Integer>();
		// 所有嵌套表formCode
		ArrayList<String> nestList = new ArrayList<String>();
		// isAll = true;
		if (true) {
			Vector vt = fd.getFields();
			Iterator ir = vt.iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField) ir.next();
				// 当默认未用模板时，如果嵌套表不显示，则使得list为空，否则会导致表头两行合并为一行，如果嵌套表中有数据，也会出现多行合并为一行的情况
				if (templateId == -1) {
					boolean isShow = false;
					for (String field : fields) {
						if (field.endsWith(ff.getName())) {
							isShow = true;
							break;
						}
					}
					if (!isShow) {
						continue;
					}
				}
				if ("nest_table".equals(ff.getMacroType()) || "nest_sheet".equals(ff.getMacroType())) {
					String nestFormCode = ff.getDescription();
					try {
						String defaultVal = StrUtil.decodeJSON(ff.getDescription());
						JSONObject json = new JSONObject(defaultVal);
						nestFormCode = json.getString("destForm");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					nestMapping.put(ff.getName(), nestFormCode);
					nestList.add(nestFormCode);
				}
			}
		}

		MacroCtlMgr mm = new MacroCtlMgr();
		String fileName = fd.getName();

		ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
		if (templateId != -1) {
			metd = metd.getModuleExportTemplateDb(templateId);
			fileName = metd.getString("name");
		}

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(fileName) + ".xls");
		OutputStream os = response.getOutputStream();
		Config cfg = new Config();
		if (cfg.getBooleanProperty("moduleExportQuick")) {
			ModuleUtil.exportXml(request, os, fields, fieldsTitle, fd, sql, templateId);
			return;
		}

		Workbook wb = null;
		WritableWorkbook wwb = null;
		try {
			File file = new File(Global.getAppPath() + "visual/template/blank.xls");
			wb = Workbook.getWorkbook(file);
			WorkbookSettings settings = new WorkbookSettings();
			settings.setWriteAccess(null);

			UserMgr um = new UserMgr();
			Map map = new HashMap();

			// 打开一个文件的副本，并且指定数据写回到原文件
			wwb = Workbook.createWorkbook(os, wb, settings);
			WritableSheet ws = wwb.getSheet(0);

			for (String ntCode : nestList) {
				ModuleSetupDb nestmsd = new ModuleSetupDb();
				nestmsd = nestmsd.getModuleSetupDbOrInit(ntCode);

				FormDb ntfd = new FormDb();
				ntfd = ntfd.getFormDb(ntCode);

				String ntlistField = StrUtil.getNullStr(nestmsd.getString("list_field"));

				String[] ntfields = StrUtil.split(ntlistField, ",");
				String[] ntfiledsName = new String[ntfields.length];

				Vector ntvt = ntfd.getFields();

				if (ntvt.size() == 0) {
					continue;
				}

				Iterator ntir = ntvt.iterator();
				while (ntir.hasNext()) {
					FormField ff = (FormField) ntir.next();
					for (int i = 0; i < ntfields.length; i++) {
						if (ff.getName().equals(ntfields[i])) {
							ntfiledsName[i] = ff.getTitle();
							continue;
						}
					}
				}
				nestFields.put(ntCode, ntfiledsName);
				nestFieldName.put(ntCode, ntlistField);
			}

			int len = 0;
			if (fields != null) {
				len = fields.length;
			}
			int index = 0;

			/*
			 * WritableFont.createFont("宋体")：设置字体为宋体
			 * 10：设置字体大小
			 * WritableFont.NO_BOLD:设置字体非加粗（BOLD：加粗     NO_BOLD：不加粗）
			 * false：设置非斜体
			 * UnderlineStyle.NO_UNDERLINE：没有下划线
			 */

			boolean isBar = false;
			int rowHeader = 0;
			Map mapWidth = new HashMap();
			WritableFont font;
			String backColor = "", foreColor = "";
			if (templateId != -1) {
				String barName = StrUtil.getNullStr(metd.getString("bar_name"));
				if (!"".equals(barName)) {
					isBar = true;
				}

				String fontFamily = metd.getString("font_family");
				int fontSize = metd.getInt("font_size");
				backColor = metd.getString("back_color");
				foreColor = metd.getString("fore_color");
				boolean isBold = metd.getInt("is_bold") == 1;
				if (isBold) {
					font = new WritableFont(WritableFont.createFont(fontFamily),
							fontSize,
							WritableFont.BOLD);
				} else {
					font = new WritableFont(WritableFont.createFont(fontFamily),
							fontSize,
							WritableFont.NO_BOLD);
				}

				if (!"".equals(foreColor)) {
					Color color = Color.decode(foreColor); // 自定义的颜色
					wwb.setColourRGB(Colour.BLUE, color.getRed(), color.getGreen(), color.getBlue());
					font.setColour(Colour.BLUE);
				}

				String columns = metd.getString("cols");
				// 第一列的序号
				boolean isSerialNo = metd.getString("is_serial_no").equals("1");
				if (isSerialNo) {
					columns = columns.substring(1); // [{}, {},...]去掉[
					columns = "[{\"field\":\"serialNoForExp\",\"title\":\"序号\",\"link\":\"#\",\"width\":80,\"name\":\"serialNoForExp\"}," + columns;
				}

				JSONArray arr = new JSONArray(columns);
				StringBuffer colsSb = new StringBuffer();
				for (int i = 0; i < arr.length(); i++) {
					JSONObject json = arr.getJSONObject(i);

					// System.out.println(getClass() + " " + i + " " + json.getInt("width"));
					ws.setColumnView(i, (int) (json.getInt("width") * 0.09 * 0.94)); // 设置列的宽度 ，单位是自己根据实际的像素值推算出来的

					StrUtil.concat(colsSb, ",", json.getString("field"));
					mapWidth.put(json.getString("field"), json.getInt("width"));
				}

				listField = colsSb.toString();
				fields = StrUtil.split(listField, ",");
				len = fields.length;

				if (isBar) {
					WritableFont barFont;
					String barBackColor = metd.getString("bar_back_color");
					String barForeColor = metd.getString("bar_fore_color");
					String barFontFamily = metd.getString("bar_font_family");
					int barFontSize = metd.getInt("bar_font_size");
					boolean isBarbBold = metd.getInt("bar_is_bold") == 1;
					if (isBarbBold) {
						barFont = new WritableFont(WritableFont.createFont(barFontFamily),
								barFontSize,
								WritableFont.BOLD);
					} else {
						barFont = new WritableFont(WritableFont.createFont(barFontFamily),
								barFontSize,
								WritableFont.NO_BOLD);
					}

					if (!"".equals(barForeColor)) {
						Color color = Color.decode(barForeColor); // 自定义的颜色
						wwb.setColourRGB(Colour.RED, color.getRed(), color.getGreen(), color.getBlue());
						barFont.setColour(Colour.RED);
					}

					WritableCellFormat barFormat = new WritableCellFormat(barFont);
					// 水平居中对齐
					barFormat.setAlignment(Alignment.CENTRE);
					// 竖直方向居中对齐
					barFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
					barFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

					if (!"".equals(barBackColor)) {
						Color bClr = Color.decode(barBackColor); // 自定义的颜色
						wwb.setColourRGB(Colour.GREEN, bClr.getRed(), bClr.getGreen(), bClr.getBlue());
						barFormat.setBackground(Colour.GREEN);
					}

					Label a = new Label(0, 0, barName, barFormat);
					ws.addCell(a);

					ws.mergeCells(0, 0, len - 1, 0);

					ws.setRowView(0, metd.getInt("bar_line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素

					rowHeader = 1;
				}
				ws.setRowView(rowHeader, metd.getInt("line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素
			} else {
				font = new WritableFont(WritableFont.createFont("宋体"),
						12,
						WritableFont.BOLD);
			}

			WritableCellFormat wcFormat = new WritableCellFormat(font);
			//水平居中对齐
			wcFormat.setAlignment(Alignment.CENTRE);
			//竖直方向居中对齐
			wcFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
			wcFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			if (templateId != -1) {
				if (!"".equals(backColor)) {
					Color color = Color.decode(backColor); // 自定义的颜色
					wwb.setColourRGB(Colour.ORANGE, color.getRed(), color.getGreen(), color.getBlue());
					wcFormat.setBackground(Colour.ORANGE);
				}
			}

			for (int i = 0; i < len; i++) {
				String fieldName = fields[i];

				String fieldTitle = fieldsTitle[i];

				String title = "";
				if ("serialNoForExp".equals(fieldName)) {
					title = "序号";
				} else if (fieldName.startsWith("main:")) {
					String[] mainToSub = StrUtil.split(fieldName, ":");
					if (mainToSub != null && mainToSub.length == 3) {
						FormDb ntfd = new FormDb();
						ntfd = ntfd.getFormDb(mainToSub[1]);
						com.redmoon.oa.visual.FormDAO ntfdao = new com.redmoon.oa.visual.FormDAO(ntfd);
						FormField ff = ntfdao.getFormField(mainToSub[2]);
						title = ff.getTitle();
					} else {
						title = fieldName;
					}
				} else if (fieldName.startsWith("other:")) {
					String[] otherFields = StrUtil.split(fieldName, ":");
					if (otherFields.length == 5) {
						FormDb otherFormDb = new FormDb(otherFields[2]);
						String showFieldName = otherFields[4];
						if ("id".equalsIgnoreCase(showFieldName)) {
							title = otherFormDb.getName() + "ID";
						} else {
							title = otherFormDb.getFieldTitle(showFieldName);
						}
					}
				} else if (fieldName.equals("cws_creator")) {
					title = "创建者";
				} else if (fieldName.equalsIgnoreCase("ID") || fieldName.equalsIgnoreCase("CWS_MID")) {
					title = "ID";
				} else if (fieldName.equals("cws_status")) {
					title = "状态";
				} else if (fieldName.equals("cws_flag")) {
					title = "冲抵状态";
				} else if (fieldName.equalsIgnoreCase("flowId")) {
					title = "流程号";
				} else if (fieldName.equalsIgnoreCase("flow_begin_date")) {
					title = "流程开始时间";
				} else if (fieldName.equalsIgnoreCase("flow_end_date")) {
					title = "流程结束时间";
				} else if (fieldName.equals("cws_id")) {
					title = "关联ID";
				} else if ("colPrompt".equals(fieldName)) {
					title = "colPrompt"; //
				} else {
					title = fd.getFieldTitle(fieldName);
				}

				if (!"#".equals(fieldTitle)) {
					title = fieldTitle;
				}

				// 判断字段是不是嵌套表，如果是则需要在第0行显示嵌套表这个字段
				if (!nestMapping.containsKey(fieldName)) {
					Label a = new Label(i + index, rowHeader, title, wcFormat);
					ws.addCell(a);
				} else {
					Label a = new Label(i + index, 0, title, wcFormat);
					ws.addCell(a);
				}

				// 加粗+4
				columnWidthMap.put(i + index, title.getBytes().length + 4);

				if ("cws_creator".equals(fieldName)) {
					ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
				} else if ("cws_flag".equals(fieldName)) {
					ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
				} else if (fieldName.startsWith("main:")) {
					ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
				} else if (fieldName.startsWith("other:")) {
					ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
				} else if ("CWS_MID".equalsIgnoreCase(fieldName)) {
					ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
				} else {
					FormField myFf = fd.getFormField(fieldName);
					if (myFf == null) {
						fieldName = null;
					} else {
						fieldName = nestMapping.get(myFf.getName());
					}
					if (fieldName == null) { // && !nestFields.containsKey(fieldName)) {
						if (templateId == -1) {
							ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
						}
					} else {
						String[] ntFields = nestFields.get(fieldName);
						// System.out.println("fieldName=" + fieldName + " " + ntFields);
						if (templateId == -1) {
							ws.mergeCells(i + index, 0, i + index + ntFields.length - 1, 0);
						}

						for (int j = 0; j < ntFields.length; j++) {
							columnWidthMap.put(i + index, ntFields[j].getBytes().length + 4);
							if (j < ntFields.length - 1) {
								index++;
							}
							if (templateId == -1) {
								Label b = new Label(i + j, 1, ntFields[j], wcFormat);
								ws.addCell(b);
							} else {
								Label b = new Label(i + j, rowHeader, ntFields[j], wcFormat);
								ws.addCell(b);
							}
						}
					}
				}
			}

			// int j = nestList.isEmpty() ? 0 : 1;
			int j = rowHeader + 1;
			int group = 0;
			int serialNo = 0;
			WorkflowDb wf = new WorkflowDb();
			request.setAttribute(ConstUtil.IS_FOR_EXPORT, "true");
			long tDebugAll = System.currentTimeMillis();

			int totalPages = 1;
			int pageSize = 200;
			int row = 0;
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
			// 为防止内存不足，每次处理记录数为pageSize
			for (int curPage = 1; curPage <= totalPages; curPage++) {
				try {
					ListResult lr = fdao.listResult(fd.getCode(), sql, curPage, pageSize);
					if (lr.getResult().size() == 0) {
						return;
					}
					if (totalPages == 1) {
						totalPages = (int) Math.ceil((double) lr.getTotal() / pageSize);
					}

					Iterator irFdao = lr.getResult().iterator();
					while (irFdao.hasNext()) {
						fdao = (com.redmoon.oa.visual.FormDAO) irFdao.next();

						index = 0;
						int logType = StrUtil.toInt(fdao.getFieldValue("log_type"), FormDAOLog.LOG_TYPE_CREATE);

						long tDebug = System.currentTimeMillis();

						// 置SQL、表单域选择宏控件中需要用到的fdao
						RequestUtil.setFormDAO(request, fdao);

						long fid = fdao.getId();
						// 嵌套表的最大行数，如果有多个嵌套表，取行数最大的值
						int maxCount = 1;
						if (templateId != -1) {
							ws.setRowView(j, metd.getInt("line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素
						}
						for (String ntCode : nestList) {
							//String ntsql = "select " + nestFieldName.get(ntCode) + " from form_table_" + ntCode + " where cws_id=" + fid;
							String ntsql = "select id from form_table_" + ntCode + " where cws_id=" + fid;
							JdbcTemplate jt = new JdbcTemplate();
							ResultIterator ri = jt.executeQuery(ntsql);
							nestData.put(ntCode, ri.getResult());
							if (ri.getRows() > maxCount) {
								maxCount = ri.getRows();
							}
						}

						for (int i = 0; i < len; i++) {
							boolean isSingle = true; // false表示带有嵌套表
							String fieldName = fields[i];
							String fieldValue = "";
							if ("serialNoForExp".equals(fieldName)) {
								fieldValue = String.valueOf(++serialNo);
							} else if (fieldName.startsWith("main:")) {
								String[] mainToSub = StrUtil.split(fieldName, ":");
								if (mainToSub != null && mainToSub.length == 3) {
									// 此时关联的是主表单，应该只有一条记录
									FormDb subfd = new FormDb(mainToSub[1]);
									com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
									FormField subff = subfd.getFormField(mainToSub[2]);
									String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
									try {
										JdbcTemplate jt = new JdbcTemplate();
										ResultIterator ri = jt.executeQuery(subsql);
										if (ri.hasNext()) {
											ResultRecord rr = (ResultRecord) ri.next();
											int subid = rr.getInt(1);
											subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
											fieldValue = subfdao.getFieldValue(mainToSub[2]);
											if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
												MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
												if (mu != null) {
													RequestUtil.setFormDAO(request, subfdao);
													fieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, fieldValue);
													// 恢复request中原来的fdao，以免ModuleController中setFormDAO的值被修改为本方法中的fdao
													RequestUtil.setFormDAO(request, fdao);
												}
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							} else if (fieldName.startsWith("other:")) {
								// 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
								String fName = fieldName;
								if (logType == FormDAOLog.LOG_TYPE_DEL) {
									if ("module_log".equals(formCode)) {
										if (fName.contains("module_id:")) {
											int p = fName.indexOf(":");
											p = fName.indexOf(":", p + 1);
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
								fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
							} else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
								fieldValue = String.valueOf(fdao.getId());
							} else if ("cws_flag".equals(fieldName)) {
								fieldValue = String.valueOf(fdao.getCwsFlag());
							} else if ("cws_creator".equals(fieldName)) {
								fieldValue = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
							} else if ("cws_status".equals(fieldName)) {
								fieldValue = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
							} else if ("flowId".equalsIgnoreCase(fieldName)) {
								fieldValue = String.valueOf(fdao.getFlowId());
							} else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
								int flowId = fdao.getFlowId();
								if (flowId != -1) {
									wf = wf.getWorkflowDb(flowId);
									fieldValue = String.valueOf(DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss"));
								}
							} else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
								int flowId = fdao.getFlowId();
								if (flowId != -1) {
									wf = wf.getWorkflowDb(flowId);
									fieldValue = String.valueOf(DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss"));
								}
							} else if ("cws_id".equals(fieldName)) {
								fieldValue = StrUtil.getNullStr(fdao.getCwsId());
							}
							else if ("colPrompt".equals(fieldName)) {
								if (isPrompt) {
									// 判断条件
									if (ModuleUtil.isPrompt(request, msd, fdao)) {
										fieldValue = "<img src=\"" + Global.getFullRootPath(request) + "/images/prompt/" + promptIcon + "\" style=\"width:16px;\" align=\"absmiddle\" />";
									}
								}
							}
							else {
								FormField ff = fd.getFormField(fieldName);
								if (ff == null) {
									fieldValue = "不存在！";
								} else {
									if (ff.getType().equals(FormField.TYPE_MACRO)) {
										MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
										if (mu != null) {
											if ("nest_sheet".equals(mu.getCode()) || "nest_table".equals(mu.getCode())) {
												isSingle = false;
												String ntFormCode = nestMapping.get(ff.getName());
												if (ntFormCode != null) {
													String ntFieldNames = nestFieldName.get(ntFormCode);
													String[] ntFieldAry = StrUtil.split(ntFieldNames, ",");
													Vector riData = nestData.get(ntFormCode);
													if (riData != null) {
														int rowInc = 0;
														Iterator it = riData.iterator();
														while (it.hasNext()) {
															int columnInc = 0;
															Vector rrv = (Vector) it.next();
															long ntid = StrUtil.toLong(rrv.get(0).toString(), 0);
															FormDb ntfd = new FormDb(ntFormCode);
															com.redmoon.oa.visual.FormDAO ntfdao = new com.redmoon.oa.visual.FormDAO(ntid, ntfd);

															if (ntfdao != null && ntfdao.isLoaded()) {
																for (int k = 0; k < ntFieldAry.length; k++) {
																	int width = columnWidthMap.get(i + index + columnInc);
																	String content = ntfdao.getFieldValue(ntFieldAry[k]);

																	FormField ntff = ntfdao.getFormField(ntFieldAry[k]);

																	if (ntff.getType().equals(FormField.TYPE_MACRO)) {
																		MacroCtlUnit ntmu = mm.getMacroCtlUnit(ntff.getMacroType());
																		if (ntmu != null) {
																			content = StrUtil.getAbstract(request, ntmu.getIFormMacroCtl().converToHtml(request, ntff, ntfdao.getFieldValue(ntFieldAry[k])), 1000, "");
																		}
																	}

																	if (content != null && content.getBytes().length > width) {
																		columnWidthMap.put(i + index + columnInc, content.getBytes().length);
																	}

																	int fieldType = FormField.FIELD_TYPE_TEXT;
																	if (ntff != null) {
																		fieldType = ntff.getFieldType();
																	}
																	WritableCellFormat wcf = setCellFormat(fieldType, group, map);

																	// 设置列格式
																	// 如果是嵌套表，则数据从第三行开始，所以要在j+rowInc 基础上+1
																	if (templateId == -1) {
																		WritableCell wc = createWritableCell(fieldType, i + index + columnInc++, j + rowInc + 1, content, wcf);
																		ws.addCell(wc);
																	} else {
																		WritableCell wc = createWritableCell(fieldType, i + index + columnInc++, j + rowInc, content, wcf);
																		ws.addCell(wc);
																	}
																}
																rowInc++;
															}
														}
														// 将没有值的单元格补色
														for (int m = rowInc; m < maxCount; m++) {
															for (int k = 0; k < ntFieldAry.length; k++) {
																WritableCellFormat wcf = setCellFormat(FormField.FIELD_TYPE_TEXT, group, map);
																if (templateId == -1) {
																	Label a = new Label(i + index + k, j + m + 1, "", wcf);
																	ws.addCell(a);
																} else {
																	Label a = new Label(i + index + k, j + m, "", wcf);
																	ws.addCell(a);
																}
															}
														}
														index += ntFieldAry.length - 1;
													}
												}
											} else if (!mu.getCode().equals("macro_raty")) {
												fieldValue = StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)), 1000, "");
											} else {
												fieldValue = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
											}
										}
									} else {
										// fieldValue = fdao.getFieldValue(fieldName);
										fieldValue = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
									}
								}
							}

							if (isSingle) {
								int width = columnWidthMap.get(i + index);

								if (fieldValue != null && fieldValue.getBytes().length > width) {
									columnWidthMap.put(i + index, fieldValue.getBytes().length);
								}

								FormField ff = fdao.getFormField(fieldName);
								int fieldType = FormField.FIELD_TYPE_TEXT;
								if (ff != null) {
									if (!ff.getType().equals(FormField.TYPE_CHECKBOX)) {
										fieldType = ff.getFieldType();
									}
								}

								wcFormat = setCellFormat(fieldType, group, map);

								// 设置列格式

								WritableCell wc = createWritableCell(fieldType, i + index, j, fieldValue, wcFormat);
								ws.addCell(wc);

								// 设置每个单元格的值
								if (templateId != -1) {
									for (int a = j + 1; a <= j + maxCount - 1; a++) {
										WritableCell wc1 = createWritableCell(fieldType, i + index, a, fieldValue, wcFormat);
										ws.addCell(wc1);
									}
								}
								if (templateId == -1) {
									// 扩展至多行,合并单元格
									ws.mergeCells(i + index, j, i + index, j + maxCount - 1);
								}
							}
						}
						group++;
						j += maxCount;

						row++;
					}
				} catch (ErrMsgException e) {
					e.printStackTrace();
				}
			}

			if (row > 0) {
				String propStat = StrUtil.getNullStr(msd.getString("prop_stat"));
				if (propStat.equals("")) {
					propStat = "{}";
				}
				JSONObject json = new JSONObject(propStat);
				Iterator ir3 = json.keys();
				while (ir3.hasNext()) {
					String key = (String) ir3.next();
					String mode = json.getString(key);

					FormField ff = fd.getFormField(key);
					int fieldType = ff.getFieldType();

					String cellVal = "";
					double sumVal = FormSQLBuilder.getSUMOfSQL(sql, key);
					if (mode.equals("0")) {
						if (fieldType == FormField.FIELD_TYPE_INT
								|| fieldType == FormField.FIELD_TYPE_LONG) {
							cellVal = "合计：" + (long) sumVal;
						} else {
							cellVal = "合计：" + NumberUtil.round(sumVal, 2);
						}
					} else if (mode.equals("1")) {
						cellVal = "平均：" + NumberUtil.round(sumVal / row, 2);
					}

					for (int i = 0; i < len; i++) {
						String fieldName = fields[i];
						if (fieldName.equals(key)) {
							Label label = new Label(i, row + 1, cellVal);
							ws.addCell(label);
							break;
						}
					}
				}
			}

			// 如果未选择导出模板
			if (templateId == -1) {
				// 设置列宽
				for (int i = 0; i < ws.getColumns(); i++) {
					ws.setColumnView(i, columnWidthMap.get(i) > 30 ? 30 : columnWidthMap.get(i));
				}
			}

			wwb.write();

			DebugUtil.i(getClass(), "exportExcel", "all record: " + (System.currentTimeMillis() - tDebugAll) + " ms");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (wwb != null) {
					try {
						wwb.close();
					} catch (WriteException e) {
						e.printStackTrace();
					}
				}
				if (wb != null) {
					wb.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			os.close();
		}
	}

	/**
	 * 设置单元格格式
	 * 一个WritableCellFormat不能被重复引用多次，否则会报错
	 * 优化了setCellFormat，使其从map中复用取值，但是map不能置于中作为本页的全局变量，而必须作为一个参数来传
	 *
	 * @param fieldType
	 * @param row
	 * @param map
	 * @return
	 */
	private WritableCellFormat setCellFormat(int fieldType, int row, Map map) {
		WritableCellFormat wcf = null;
		boolean isFirst = false;
		try {
			// 单元格格式
			switch (fieldType) {
				case FormField.FIELD_TYPE_DOUBLE:
				case FormField.FIELD_TYPE_FLOAT:
				case FormField.FIELD_TYPE_PRICE:
					if (map.get("double") != null) {
						wcf = (WritableCellFormat) map.get("double");
					} else {
						NumberFormat nf1 = new NumberFormat("0.00");
						wcf = new WritableCellFormat(nf1);
						map.put("double", wcf);
						isFirst = true;
					}
					break;
				case FormField.FIELD_TYPE_INT:
				case FormField.FIELD_TYPE_LONG:
					if (map.get("long") != null) {
						wcf = (WritableCellFormat) map.get("long");
					} else {
						NumberFormat nf2 = new NumberFormat("#");
						wcf = new WritableCellFormat(nf2);
						map.put("long", wcf);
						isFirst = true;
					}
					break;
				case FormField.FIELD_TYPE_DATE:
					if (map.get("date") != null) {
						wcf = (WritableCellFormat) map.get("date");
					} else {
						jxl.write.DateFormat df1 = new jxl.write.DateFormat("yyyy-MM-dd");
						wcf = new jxl.write.WritableCellFormat(df1);
						map.put("date", wcf);
						isFirst = true;
					}
					break;
				case FormField.FIELD_TYPE_DATETIME:
					if (map.get("datetime") != null) {
						wcf = (WritableCellFormat) map.get("datetime");
					} else {
						jxl.write.DateFormat df2 = new jxl.write.DateFormat("yyyy-MM-dd HH:mm:ss");
						wcf = new jxl.write.WritableCellFormat(df2);
						map.put("datetime", wcf);
						isFirst = true;
					}
					break;
				default:
					if (map.get("str") != null) {
						wcf = (WritableCellFormat) map.get("str");
					} else {
						wcf = new WritableCellFormat();
						map.put("str", wcf);
						isFirst = true;
					}

					break;
			}

			if (isFirst) {
				// 不能修改已指向的format， jxl.write.biff.JxlWriteException: Attempt to modify a referenced format
				// 对齐方式
				wcf.setAlignment(Alignment.CENTRE);
				wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
				// 边框
				wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
				//自动换行
				wcf.setWrap(true);

				// 背景色
	        /*
	        if (row % 2 == 0) {
	        	wcf.setBackground(jxl.format.Colour.ICE_BLUE);
			} else {
				wcf.setBackground(jxl.format.Colour.WHITE);
			}
			*/
			}

		} catch (WriteException e) {
			e.printStackTrace();
		}
		return wcf;
	}

	// 创建单元格
	private WritableCell createWritableCell(int fieldType, int column, int row, String data, WritableCellFormat wcf) {
		WritableCell wc = null;
		if (data == null || data.equals("")) {
			wc = new Label(column, row, "", wcf);
		} else {
			switch (fieldType) {
				case FormField.FIELD_TYPE_TEXT:
				case FormField.FIELD_TYPE_VARCHAR:
					wc = new Label(column, row, data, wcf);
					break;
				case FormField.FIELD_TYPE_DOUBLE:
				case FormField.FIELD_TYPE_FLOAT:
				case FormField.FIELD_TYPE_PRICE:
					wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
					break;
				case FormField.FIELD_TYPE_INT:
				case FormField.FIELD_TYPE_LONG:
					wc = new jxl.write.Number(column, row, StrUtil.toLong(data), wcf);
					break;
				case FormField.FIELD_TYPE_DATE:
					wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd"), wcf);
					break;
				case FormField.FIELD_TYPE_DATETIME:
					wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd HH:mm:ss"), wcf);
					break;
				default:
					wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
					break;
			}
		}
		return wc;
	}

	@RequestMapping("/exportExcelRelate")
	public void exportExcelRelate(HttpServletResponse response) throws IOException, ErrMsgException, JSONException {
		String formCode = ParamUtil.get(request, "formCode");
		String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
		if ("".equals(moduleCodeRelated)) {
			// nest_sheet_view.jsp中传的是formCodeRelated
			moduleCodeRelated = ParamUtil.get(request, "formCodeRelated");
		}
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
		String formCodeRelated = msd.getString("form_code");

		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCodeRelated);
		if (!fd.isLoaded()) {
			throw new ErrMsgException("表单不存在！");
		}
		String op = ParamUtil.get(request, "op");

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
		if (sort.equals("")) {
			sort = "desc";
		}

		request.setAttribute(ConstUtil.IS_FOR_EXPORT, "true");
		String moduleCode = ParamUtil.get(request, "code");
		String mode = ParamUtil.get(request, "mode");
		String tagName = ParamUtil.get(request, "tagName");

		// 通过选项卡标签关联
		boolean isSubTagRelated = "subTagRelated".equals(mode);

		if (isSubTagRelated) {
			String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
			try {
				JSONObject json = new JSONObject(tagUrl);
				if (!json.isNull("formRelated")) {
					// formCodeRelated = json.getString("formRelated");
					moduleCodeRelated = json.getString("formRelated");
					msd = msd.getModuleSetupDb(moduleCodeRelated);
					formCodeRelated = msd.getString("form_code");
				} else {
					throw new ErrMsgException("选项卡关联配置不正确！");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		String relateFieldValue = "";
		int parentId = ParamUtil.getInt(request, "parentId", -1);
		if (parentId == -1) {
			throw new ErrMsgException("缺少父模块记录的ID！");
		} else {
			if (!isSubTagRelated) {
				com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
				relateFieldValue = fdm.getRelateFieldValue(parentId, moduleCodeRelated);
				if (relateFieldValue == null) {
					// 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
					relateFieldValue = SQLBuilder.IS_NOT_RELATED;
				}
			}
		}

		request.setAttribute(ConstUtil.IS_FOR_EXPORT, "true");
		request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
		int nestType = ParamUtil.getInt(request, "nestType", MacroCtlUnit.NEST_TYPE_NONE);
		request.setAttribute("nestType", String.valueOf(nestType));

		// 如果是嵌套表，则根据ID顺序排序
		if (nestType != MacroCtlUnit.NEST_TYPE_NONE) {
			sort = "asc";
		}

		String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
		String sql = arySQL[0];

		FormDAO fdao = new FormDAO();
		Vector v = fdao.list(formCodeRelated, sql);

		// System.out.print(sql);

		String fileName = fd.getName();
		long templateId = ParamUtil.getLong(request, "templateId", -1);
		ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
		if (templateId != -1) {
			metd = metd.getModuleExportTemplateDb(templateId);
			fileName = metd.getString("name");
		}

		String[] fields;
		String cols = ParamUtil.get(request, "cols");
		if (!"".equals(cols)) {
			fields = StrUtil.split(cols, ",");
		} else {
			if (nestType != MacroCtlUnit.NEST_TYPE_TABLE) {
				fields = msd.getColAry(false, "list_field");
			} else {
				String nestFieldName = ParamUtil.get(request, "nestFieldName");
				String parentFormCode = formCode;
				JSONObject json = null;
				int formViewId = -1;
				FormField nestField = null;
				String nestFormCode = "";
				if (!nestFieldName.equals("")) {
					FormDb parentFd = new FormDb();
					parentFd = parentFd.getFormDb(parentFormCode);
					nestField = parentFd.getFormField(nestFieldName);
					if (nestField == null) {
						throw new ErrMsgException("父表单（" + parentFormCode + "）中的嵌套表字段：" + nestFieldName + " 不存在");
					}
					try {
						String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
						json = new JSONObject(defaultVal);
						nestFormCode = json.getString("destForm");
						if (!json.isNull("formViewId")) {
							formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
						} else {
							throw new ErrMsgException("嵌套表格未视定视图");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				String viewContent = "";
				if (formViewId != -1) {
					FormViewDb formViewDb = new FormViewDb();
					formViewDb = formViewDb.getFormViewDb(formViewId);
					viewContent = formViewDb.getString("content");
				} else {
					viewContent = FormViewMgr.makeViewContent(msd);
				}

				MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
				INestTableCtl nestTableCtl = macroCtlService.getNestTableCtl();
				Vector fieldV = nestTableCtl.parseFieldsByView(fd, viewContent);
				fields = new String[fieldV.size()];
				int i = 0;
				Iterator ir = fieldV.iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField) ir.next();
					fields[i] = ff.getName();
					i++;
				}
			}
		}

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(fileName) + ".xls");

		OutputStream os = response.getOutputStream();

		try {
			File file = new File(Global.getAppPath(request) + "visual/template/blank.xls");
			Workbook wb = Workbook.getWorkbook(file);
			UserMgr um = new UserMgr();

			// 打开一个文件的副本，并且指定数据写回到原文件
			WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
			WritableSheet ws = wwb.getSheet(0);

			int len = 0;
			if (fields != null) {
				len = fields.length;
			}

			/*
			 * WritableFont.createFont("宋体")：设置字体为宋体
			 * 10：设置字体大小
			 * WritableFont.NO_BOLD:设置字体非加粗（BOLD：加粗     NO_BOLD：不加粗）
			 * false：设置非斜体
			 * UnderlineStyle.NO_UNDERLINE：没有下划线
			 */
			boolean isBar = false;
			int rowHeader = 0;
			Map mapWidth = new HashMap();
			WritableFont font;
			String backColor = "", foreColor = "";
			if (templateId != -1) {
				String barName = StrUtil.getNullStr(metd.getString("bar_name"));
				if (!"".equals(barName)) {
					isBar = true;
				}

				String fontFamily = metd.getString("font_family");
				int fontSize = metd.getInt("font_size");
				backColor = metd.getString("back_color");
				foreColor = metd.getString("fore_color");
				boolean isBold = metd.getInt("is_bold") == 1;
				if (isBold) {
					font = new WritableFont(WritableFont.createFont(fontFamily),
							fontSize,
							WritableFont.BOLD);
				} else {
					font = new WritableFont(WritableFont.createFont(fontFamily),
							fontSize,
							WritableFont.NO_BOLD);
				}

				if (!"".equals(foreColor)) {
					Color color = Color.decode(foreColor); // 自定义的颜色
					wwb.setColourRGB(Colour.BLUE, color.getRed(), color.getGreen(), color.getBlue());
					font.setColour(Colour.BLUE);
				}

				String columns = metd.getString("cols");

				boolean isSerialNo = metd.getString("is_serial_no").equals("1");
				if (isSerialNo) {
					columns = columns.substring(1); // [{}, {},...]去掉[
					columns = "[{\"field\":\"serialNoForExp\",\"title\":\"序号\",\"link\":\"#\",\"width\":80,\"name\":\"serialNoForExp\"}," + columns;
				}

				JSONArray arr = new JSONArray(columns);
				StringBuffer colsSb = new StringBuffer();
				for (int i = 0; i < arr.length(); i++) {
					JSONObject json = arr.getJSONObject(i);

					// System.out.println(getClass() + " " + i + " " + json.getInt("width"));
					ws.setColumnView(i, (int) (json.getInt("width") * 0.09 * 0.94)); // 设置列的宽度 ，单位是自己根据实际的像素值推算出来的

					StrUtil.concat(colsSb, ",", json.getString("field"));
					mapWidth.put(json.getString("field"), json.getInt("width"));
				}

				String listField = colsSb.toString();
				fields = StrUtil.split(listField, ",");
				len = fields.length;

				if (isBar) {
					WritableFont barFont;
					String barBackColor = metd.getString("bar_back_color");
					String barForeColor = metd.getString("bar_fore_color");
					String barFontFamily = metd.getString("bar_font_family");
					int barFontSize = metd.getInt("bar_font_size");
					boolean isBarbBold = metd.getInt("bar_is_bold") == 1;
					if (isBarbBold) {
						barFont = new WritableFont(WritableFont.createFont(barFontFamily),
								barFontSize,
								WritableFont.BOLD);
					} else {
						barFont = new WritableFont(WritableFont.createFont(barFontFamily),
								barFontSize,
								WritableFont.NO_BOLD);
					}

					if (!"".equals(barForeColor)) {
						Color color = Color.decode(barForeColor); // 自定义的颜色
						wwb.setColourRGB(Colour.RED, color.getRed(), color.getGreen(), color.getBlue());
						barFont.setColour(Colour.RED);
					}

					WritableCellFormat barFormat = new WritableCellFormat(barFont);
					// 水平居中对齐
					barFormat.setAlignment(Alignment.CENTRE);
					// 竖直方向居中对齐
					barFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
					barFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

					if (!"".equals(barBackColor)) {
						Color bClr = Color.decode(barBackColor); // 自定义的颜色
						wwb.setColourRGB(Colour.GREEN, bClr.getRed(), bClr.getGreen(), bClr.getBlue());
						barFormat.setBackground(Colour.GREEN);
					}

					Label a = new Label(0, 0, barName, barFormat);
					ws.addCell(a);

					ws.mergeCells(0, 0, len - 1, 0);

					ws.setRowView(0, metd.getInt("bar_line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素

					rowHeader = 1;
				}
				ws.setRowView(rowHeader, metd.getInt("line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素
			} else {
				font = new WritableFont(WritableFont.createFont("宋体"), 12, WritableFont.BOLD);
			}

			WritableCellFormat wcFormat = new WritableCellFormat(font);
			//水平居中对齐
			wcFormat.setAlignment(Alignment.CENTRE);
			//竖直方向居中对齐
			wcFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
			wcFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			if (templateId != -1) {
				if (!"".equals(backColor)) {
					Color color = Color.decode(backColor); // 自定义的颜色
					wwb.setColourRGB(Colour.ORANGE, color.getRed(), color.getGreen(), color.getBlue());
					wcFormat.setBackground(Colour.ORANGE);
				}
			}

			FormMgr fm = new FormMgr();
			for (int i = 0; i < len; i++) {
				String fieldName = fields[i];
				String title;
				if (fieldName.equals("serialNoForExp")) {
					title = "序号";
				} else if (fieldName.equals("cws_creator")) {
					title = "创建者";
				} else if (fieldName.equals("ID")) {
					title = "ID";
				} else if (fieldName.equals("cws_status")) {
					title = "状态";
				} else if (fieldName.equals("cws_flag")) {
					title = "冲抵状态";
				} else {
					if (fieldName.startsWith("main:")) {
						String[] ary = StrUtil.split(fieldName, ":");
						FormDb mainFormDb = fm.getFormDb(ary[1]);
						title = mainFormDb.getFieldTitle(ary[2]);
					} else if (fieldName.startsWith("other:")) {
						String[] ary = StrUtil.split(fieldName, ":");
						FormDb otherFormDb = fm.getFormDb(ary[2]);
						String showFieldName = ary[4];
						if ("id".equalsIgnoreCase(showFieldName)) {
							title = otherFormDb.getName() + "ID";
						} else {
							title = otherFormDb.getFieldTitle(showFieldName);
						}
					} else {
						title = fd.getFieldTitle(fieldName);
					}
					if ("".equals(title)) {
						title = fieldName + "不存在";
					}
				}

				Label a = new Label(i, rowHeader, title, wcFormat);
				ws.addCell(a);
			}

			Iterator ir = v.iterator();

			int j = rowHeader + 1;
			int k = 0;

			MacroCtlMgr mm = new MacroCtlMgr();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				// 置SQL宏控件中需要用到的fdao
				RequestUtil.setFormDAO(request, fdao);
				for (int i = 0; i < len; i++) {
					String fieldName = fields[i];
					String fieldValue = "";
					if (fieldName.equals("serialNoForExp")) {
						fieldValue = String.valueOf(++k);
					} else if (fieldName.equals("cws_creator")) {
						fieldValue = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
					} else if (fieldName.equals("cws_progress")) {
						fieldValue = String.valueOf(fdao.getCwsProgress());
					} else if (fieldName.equals("ID")) {
						fieldValue = String.valueOf(fdao.getId());
					} else if (fieldName.equals("cws_status")) {
						fieldValue = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
					} else if (fieldName.equals("cws_flag")) {
						fieldValue = String.valueOf(fdao.getCwsFlag());
					} else {
						if (fieldName.startsWith("main")) {
							String[] ary = StrUtil.split(fieldName, ":");
							FormDb mainFormDb = fm.getFormDb(ary[1]);
							com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
							com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(parentId);
							FormField ff = mainFormDb.getFormField(ary[2]);
							if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null) {
									fieldValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2]));
								}
							} else {
								fieldValue = fdmMain.getFieldValueOfMain(parentId, ary[2]);
							}
						} else if (fieldName.startsWith("other:")) {
							fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
						} else {
							FormField ff = fd.getFormField(fieldName);
							if (ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null && !mu.getCode().equals("macro_raty")) {
									fieldValue = StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)), 1000, "");
									// fieldValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
								} else {
									fieldValue = fdao.getFieldValue(fieldName);
								}
							} else {
								fieldValue = fdao.getFieldValue(fieldName);
							}
						}
					}

					Label a = new Label(i, j, fieldValue);
					ws.addCell(a);
				}

				j++;
			}
			wwb.write();
			wwb.close();
			wb.close();
		} catch (Exception e) {
			// System.out.println(e.toString());
			e.printStackTrace();
		} finally {
			os.close();
		}
	}

	// 过滤超链接、隐藏输入框
	String filterString(String content) {
		// content = "asdfasdf<input type=\"hidden\" name=\"checkItemsSel\" id=\"checkItemsSel\" value=\"\" />asdfasdf";
		String patternStr = "", replacementStr = "";
		Pattern pattern;
		Matcher matcher;
		replacementStr = "";
		patternStr = "<a .*?style=['|\"]?display:none['|\"]?>(.*?)</div>";
		pattern = Pattern.compile(patternStr,
				Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
		matcher = pattern.matcher(content);
		content = matcher.replaceAll(replacementStr);

		patternStr = "<input .*?type=['|\"]?hidden['|\"]? .*?>";
		pattern = Pattern.compile(patternStr,
				Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
		matcher = pattern.matcher(content);
		content = matcher.replaceAll(replacementStr);

		// 注意来自于嵌套表nest_table_view.jsp中的数据，如果不过滤style就会出现乱码
		// 采用以下方式导入的css文件，不会出现乱码：@import url("...");
		// 可能是因为生成word后，丢失了css，所以不会出现乱码

		// 过滤javascript
		String regExScript = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或]*?>[\\s\\S]*?<\\/script> }
		//String regExScript = "<script[^>]*>.*</script[^>]*>"; // 此行过滤不了，@task:AntiXSS.stripScriptTag中可能存在同样问题

		Pattern pat = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
		Matcher m = pat.matcher(content);
		content = m.replaceAll("");

		// 过滤style
		String regExStyle = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或]*?>[\\s\\S]*?<\\/style> }
		pat = Pattern.compile(regExStyle, Pattern.CASE_INSENSITIVE);
		m = pat.matcher(content);
		content = m.replaceAll("");

		// 过滤html
		// String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
		// System.out.println(getClass() + cont);

		return content;
	}

	@RequestMapping("/exportWord")
	public void exportWord(HttpServletResponse response) throws IOException, ErrMsgException, JSONException {
		StringBuffer cont = new StringBuffer();
		String code = ParamUtil.get(request, "code");
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		if (msd == null) {
			throw new ErrMsgException("模块不存在！");
		}

		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);

		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);

		String formContent = fd.getContent();
		int formViewId = ParamUtil.getInt(request, "formViewId", -1);
		FormViewDb formViewDb = new FormViewDb();
		if (formViewId != -1) {
			formViewDb = formViewDb.getFormViewDb(formViewId);
			formContent = formViewDb.getString("form");
		}

		FormDAO fdao = new FormDAO();

		// 用于ImageCtl中生成完整路径图片
		request.setAttribute("pageType", ConstUtil.PAGE_TYPE_WORD);

		String ids = ParamUtil.get(request, "ids");
		String[] ary = StrUtil.split(ids, ",");
		if (ary == null) {
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

			if (sort.equals("")) {
				sort = "desc";
			}

			String op = ParamUtil.get(request, "op");

			// 用于传过滤条件
			request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
			String[] arySql = null;
			arySql = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);

			Render rd = new Render(request, fd);
			Iterator ir = fdao.list(formCode, arySql[0]).iterator();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				if (formViewDb.isLoaded()) {
					formContent = Render.applyShowRule(request, formViewDb, fd, fdao, userName);
				}
				cont.append("<br/>" + rd.reportForArchive(fdao, formContent));
			}
		} else {
			Render rd = new Render(request, fd);
			for (String strId : ary) {
				fdao = fdao.getFormDAO(StrUtil.toLong(strId), fd);
				if (formViewDb.isLoaded()) {
					formContent = Render.applyShowRule(request, formViewDb, fd, fdao, userName);
				}
				cont.append("<br/>" + rd.reportForArchive(fdao, formContent));
			}
		}

		String content = filterString(cont.toString());

		response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(fd.getName()) + ".doc");

		response.setContentType("application/msword; charset=gb2312");

		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(response.getOutputStream());

			WordUtil.htmlToWord(content, bos);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
	}

	/*@RequestMapping("/moduleEdit")
	public String userResetPwd(Model model) {
		model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
		return "module_edit";
	}*/

	public static boolean isXlsxRowEmpty(XSSFRow row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			XSSFCell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != XSSFCell.CELL_TYPE_BLANK) {
				return false;
			}
		}
		return true;
	}

	public static boolean isXlsRowEmpty(HSSFRow row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			HSSFCell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
				return false;
			}
		}
		return true;
	}

	public JSONArray importData(String userName, String formCode,String unitCode,String path, boolean isAll,String cws_id, int templateId) throws ErrMsgException, IOException{
		JSONArray rowAry = new JSONArray();
		// System.out.println("userName = " + userName + "formCode = "+ formCode + "unitCode = "+unitCode + "path = "+ path + "isall = "+ isAll);
		InputStream in = null;
		try {
			// System.out.println(getClass()+"::::"+formCode);
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(formCode);

			// String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fields = msd.getColAry(false, "list_field");

			JSONArray arr = null;
			if (templateId!=-1) {
				ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
				mid = mid.getModuleImportTemplateDb(templateId);

				String rules = mid.getString("rules");
				try {
					arr = new JSONArray(rules);
					if (arr.length()>0) {
						fields = new String[arr.length()];
						for (int i = 0; i < arr.length(); i++) {
							JSONObject json = (JSONObject) arr.get(i);
							fields[i] = json.getString("name");
						}
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
					throw new ErrMsgException(e.getMessage());
				}
			}
			/*
			 for(int i = 0 ; i < fields.length; i ++){
			 System.out.println(getClass()+"::"+i+","+fields[i]); }
			 */
			// System.out.println(getClass()+"::path="+path);
			FormDb fd = new FormDb(formCode);
			// System.out.println(getClass() + " isAll2=" + isAll);
			if (isAll) {
				Vector vt = fd.getFields();
				fields = new String[vt.size()];
				Iterator ir = vt.iterator();
				int i=0;
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					fields[i] = ff.getName();
					i++;
				}
			}

			MacroCtlMgr mm = new MacroCtlMgr();

			in = new FileInputStream(path);
			String pa = StrUtil.getFileExt(path);
			if (pa.equals("xls")) {
				// 读取xls格式的excel文档
				HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
				// 获取sheet
				int rows = w.getNumberOfSheets();
				rows = 1; // 只取第1张sheet
				for (int i = 0; i < rows; i++) {
					HSSFSheet sheet = w.getSheetAt(i);
					if (sheet != null) {
						// 获取行数
						int rowcount = sheet.getLastRowNum();
						HSSFCell cell = null;

						// 取得第0行，检查表头是否相符
						HSSFRow rowHeader = sheet.getRow(0);
						if (rowHeader != null) {
							int colcount = rowHeader.getLastCellNum();
							if (templateId!=-1 && colcount != arr.length()) {
								throw new ErrMsgException("表头数量为" + colcount + "，与模板文件中的数量" + arr.length() + "不同");
							}
							// 获取每一单元格
							for (int m = 0; m < colcount; m++) {
								cell = rowHeader.getCell(m);
								if (cell==null) {
									continue;
								}
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String colTitle = cell.getStringCellValue();
								if (templateId!=-1) {
									JSONObject json = (JSONObject) arr.get(m);
									String title = json.getString("title");
									if (!title.equals(colTitle)) {
										throw new ErrMsgException("表头“" + colTitle + "”与模板文件中的“" + title + "”不相符");
									}
								}
							}
						}

						// 获取每一行
						for (int k = 1; k <= rowcount; k++) {
							HSSFRow row = sheet.getRow(k);
							if (row != null) {
								if (isXlsRowEmpty(row)) {
									continue;
								}
								int colcount = row.getLastCellNum();
								if (colcount > fields.length)
									colcount = fields.length;
								JSONObject jo = new JSONObject();

								// 获取每一单元格
								for (int m = 0; m < colcount; m++) {
									cell = row.getCell(m);

									String colName = fields[m];

									if (cell==null) {
										jo.put(colName, "");
										continue;
									}

									// 为空表示不需要导入
									if ("".equals(fields[m])) {
										jo.put(colName, "");
										continue;
									}
									// System.out.println(getClass() + " m=" + m + " fields[m]=" + fields[m]);
									if (fields[m].equals("cws_creator")) {
										jo.put(colName, userName);
									}
									else {
										if (HSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() && HSSFDateUtil.isCellDateFormatted(cell)) {
											Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
											jo.put(colName, DateUtil.format(date, "yyyy-MM-dd"));
										}
										else {
											cell.setCellType(HSSFCell.CELL_TYPE_STRING);
											String val = cell.getStringCellValue().trim();
											jo.put(colName, val);
										}
									}
								}
								rowAry.put(jo);
							}
						}
					}
				}
			} else if (pa.equals("xlsx")) {
				XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
				int rows = w.getNumberOfSheets();
				rows = 1; // 只取第1张sheet
				for (int i = 0; i < rows; i++) {
					XSSFSheet sheet = w.getSheetAt(i);
					if (sheet != null) {
						int rowcount = sheet.getLastRowNum();
						XSSFCell cell = null;
						// 取得第0行，检查表头是否相符
						XSSFRow rowHeader = sheet.getRow(0);
						if (rowHeader != null) {
							int colcount = rowHeader.getLastCellNum();
							if (templateId!=-1 && colcount != arr.length()) {
								throw new ErrMsgException("表头数量为" + colcount + "，与模板文件中的数量" + arr.length() + "不同");
							}
							// 获取每一单元格
							for (int m = 0; m < colcount; m++) {
								cell = rowHeader.getCell(m);
								if (cell==null) {
									continue;
								}
								cell.setCellType(XSSFCell.CELL_TYPE_STRING);
								String colTitle = cell.getStringCellValue();
								if (templateId!=-1) {
									JSONObject json = (JSONObject) arr.get(m);
									String title = json.getString("title");
									if (!title.equals(colTitle)) {
										throw new ErrMsgException("表头“" + colTitle + "”与模板文件中的“" + title + "”不相符");
									}
								}
							}
						}
						// FormDAO fdao = new FormDAO();
						for (int k = 1; k <= rowcount; k++) {
							XSSFRow row = sheet.getRow(k);
							if (row != null) {
								// 如果是空行则跳过
								if (isXlsxRowEmpty(row)) {
									continue;
								}
								int colcount = row.getLastCellNum();
								if (colcount > fields.length) {
									colcount = fields.length;
								}

								JSONObject jo = new JSONObject();
								for (int m = 0; m < colcount; m++) {
									cell = row.getCell(m);

									String colName = fields[m];

									if (cell==null) {
										jo.put(colName, "");
										continue;
									}
									// 为空表示不需要导入
									if ("".equals(fields[m])) {
										jo.put(colName, "");
										continue;
									}

									if (fields[m].equals("cws_creator")) {
										jo.put(colName, userName);
									}
									else {
										if (XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() && HSSFDateUtil.isCellDateFormatted(cell)) {
											Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
											jo.put(colName, DateUtil.format(date, "yyyy-MM-dd"));
										}
										else {
											cell.setCellType(XSSFCell.CELL_TYPE_STRING);
											String val = cell.getStringCellValue().trim();
											jo.put(colName, val);
										}
									}
								}
								rowAry.put(jo);
							}
						}
					}
				}
			}
		}
		catch (ErrMsgException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return rowAry;
	}

	@RequestMapping("/importExcel")
	public String importExcel(Model model) {
		String excelFile="";
		try {
			Privilege privilege = new Privilege();
			String code = ParamUtil.get(request, "code");
			String formCode = ParamUtil.get(request,"formCode");
			String cws_id = ParamUtil.get(request,"parentId");
			String userName = privilege.getUser(request);
			String menuItem = ParamUtil.get(request, "menuItem");
			String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
			boolean isAll = ParamUtil.getBoolean(request, "isAll", false);

			ExcelUploadUtil fum = new ExcelUploadUtil();
			ServletContext application = request.getServletContext();
			excelFile = fum.uploadExcel(application, request);
			DebugUtil.i(getClass(), "uploadExcel", privilege.getUser(request) + ":" + excelFile);
			if (!excelFile.equals("")) {
				int templateId = StrUtil.toInt(fum.getFileUpload().getFieldValue("templateId"), -1);
				JSONArray rowAry = importData(userName, formCode,privilege.getUserUnitCode(request), excelFile, isAll,cws_id, templateId);
				File file = new File(excelFile);
				file.delete();

				model.addAttribute("importRecords", rowAry);
				model.addAttribute("code", code);
				model.addAttribute("formCode", formCode);
				model.addAttribute("templateId", new Integer(templateId));
				model.addAttribute("parentId", cws_id);
				model.addAttribute("menuItem", menuItem);
				model.addAttribute("moduleCodeRelated", moduleCodeRelated);
				// request.getRequestDispatcher("module_import_preview.jsp").forward(request, response);
			}
			else {
				model.addAttribute("info", "文件不能为空");
				return "error";
			}
		}
		catch (ErrMsgException | IOException e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "error";
		}
		return "../../visual/module_import_preview";
	}
}