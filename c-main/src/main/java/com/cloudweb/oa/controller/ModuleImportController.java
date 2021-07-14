package com.cloudweb.oa.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import com.cloudweb.oa.cache.UserCache;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.visual.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

@Controller
public class ModuleImportController {
	@Autowired  
	private HttpServletRequest request;  

	@Autowired
	private UserCache userCache;

	@RequestMapping("/visual/view")
	public ModelAndView view(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/visual/module_import_list");
		return mav;
	}
	
	@RequestMapping(value = "/visual/module_import_list", method = RequestMethod.GET)
	public String getModuleImportList(@RequestParam(value = "code", required = false, defaultValue = "")
			String code, @RequestParam String formCode, Model model) {
		model.addAttribute("code", code);
		model.addAttribute("formCode", formCode);
	
		ModuleImportTemplateDb mitd = new ModuleImportTemplateDb();
		String sql = mitd.getTable().getSql("listForForm");
		Vector v = mitd.list(sql, new Object[]{formCode});
		model.addAttribute("items", v);
		return "visual/module_import_list";
	}
	
	/**
	 * 
	 * @Description: 
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/visual/module_import_del", method = RequestMethod.GET, produces={"text/html;charset=UTF-8;","application/json;"})
	public String del(@RequestParam(value = "id", required = true)
			long id) {
		// 注意在注解中加入produces是为了防止乱码
		ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
		mid = mid.getModuleImportTemplateDb(id);
		
		boolean re = false;
		try {
			re = mid.del();
		} catch (ResKeyException e) {
			e.printStackTrace();
		}
		
		JSONObject json = new JSONObject();
		try {
			if (re) {
				json.put("ret", 1);
				json.put("msg", "删除成功！");
			} else {
				json.put("ret", 0);
				json.put("msg", "删除失败");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}
	
	@ResponseBody	
	@RequestMapping(value = "/visual/module_import_edit", method = RequestMethod.GET, produces={"text/html;charset=UTF-8;","application/json;"})
	public String modifyModuleImportCols(@RequestParam(value = "code", required = false, defaultValue = "")
			String code, @RequestParam String formCode, @RequestParam(value = "id", required = true) long id, Model model) {
		JSONArray ary = new JSONArray();
		int colCount = ParamUtil.getInt(request, "colCount", -1);
		Map map = new HashMap();
		for (int i=0; i<colCount; i++) {
			String field = ParamUtil.get(request, "field" + i);
			String title = ParamUtil.get(request, "title" + i);
			int canNotRepeat = ParamUtil.getInt(request, "canNotRepeat" + i, 0);
			int canNotEmpty = ParamUtil.getInt(request, "canNotEmpty" + i, 0);
			try {
				JSONObject json = new JSONObject();
				json.put("name", field);
				json.put("title", title);
				json.put("canNotRepeat", canNotRepeat);
				json.put("canNotEmpty", canNotEmpty);
				ary.put(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// 检查指定的字段是否有重复，否则会导致在导入的时候QObject.create()发生异常
			if (!map.containsKey(field)) {
				if (!"".equals(field)) {
					map.put(field, "");
				}
			}
			else {
				FormDb fd = new FormDb();
				fd = fd.getFormDb(formCode);
				JSONObject json = new JSONObject();
				try {
					json.put("ret", 0);
					json.put("msg", "表头：" + title + "，字段：" + fd.getFieldTitle(field) + " 被重复指定！");
				} catch (JSONException e) {
					e.printStackTrace();
				}		
				
				return json.toString();				
			}
		}
		
		
		// 清洗
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);		
		JSONArray aryClean = new JSONArray();
		MacroCtlMgr mm = new MacroCtlMgr();
		SelectMgr sm = new SelectMgr();
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null && "macro_flow_select".equals(mu.getCode())) {
					if (!"1".equals(ParamUtil.get(request, "is_clean_" + ff.getName()))) {
						continue;
					}
					JSONObject jo = new JSONObject();
					try {
						jo.put("fieldName", ff.getName());
						String basicCode = ff.getDefaultValueRaw();
						if (StringUtils.isEmpty(basicCode)) {
							basicCode = ff.getDescription();
						}
						SelectDb sd = sm.getSelect(basicCode);
						Vector v = sd.getOptions(new JdbcTemplate());
						for (Object o : v) {
							SelectOptionDb sod = (SelectOptionDb) o;
							if (!sod.isOpen()) {
								continue;
							}
							String val = ParamUtil.get(request, ff.getName() + "_" + StrUtil.escape(sod.getValue()));
							jo.put(val, sod.getValue());
						}
						aryClean.put(jo);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
				
		String rules = ary.toString();
		String cleans = aryClean.toString();
		String name = ParamUtil.get(request, "name");
		
		boolean re = false;
		ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
		mid = mid.getModuleImportTemplateDb(id);
		try {
			mid.set("name", name);
			mid.set("rules", rules);
			mid.set("cleans", cleans);
			re = mid.save();
		} catch (ResKeyException e) {
			e.printStackTrace();
		}
		
		JSONObject json = new JSONObject();
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
	@RequestMapping(value = "/visual/setModuleImportCols", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
	public String setModuleImportCols(@RequestParam(value = "code", required = false, defaultValue = "")
			String code, @RequestParam String formCode, Model model) {
		model.addAttribute("code", code);
		model.addAttribute("formCode", formCode);
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);		
		Map map = new HashMap(); 
		JSONArray ary = new JSONArray();
		int colCount = ParamUtil.getInt(request, "colCount", -1);
		for (int i=0; i<colCount; i++) {
			String field = ParamUtil.get(request, "field" + i);
			String title = ParamUtil.get(request, "title" + i);
			int canNotRepeat = ParamUtil.getInt(request, "canNotRepeat" + i, 0);
			int canNotEmpty = ParamUtil.getInt(request, "canNotEmpty" + i, 0);			
			try {
				JSONObject json = new JSONObject();
				json.put("name", field);
				json.put("title", title);
				json.put("canNotRepeat", canNotRepeat);
				json.put("canNotEmpty", canNotEmpty);
				ary.put(json);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 检查指定的字段是否有重复，否则会导致在导入的时候QObject.create()发生异常
			if (!map.containsKey(field)) {
				if (!"".equals(field)) {
					map.put(field, "");
				}
			}
			else {
				JSONObject json = new JSONObject();
				try {
					json.put("ret", 0);
					json.put("msg", "表头：" + title + "，字段：" + fd.getFieldTitle(field) + " 被重复指定！");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return json.toString();				
			}			
		}
		
		// 清洗
		JSONArray aryClean = new JSONArray();
		MacroCtlMgr mm = new MacroCtlMgr();
		SelectMgr sm = new SelectMgr();
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null && "macro_flow_select".equals(mu.getCode())) {
					if (!"1".equals(ParamUtil.get(request, "is_clean_" + ff.getName()))) {
						continue;
					}
					JSONObject jo = new JSONObject();
					try {
						jo.put("fieldName", ff.getName());
						String basicCode = ff.getDefaultValueRaw();
						if (StringUtils.isEmpty(basicCode)) {
							basicCode = ff.getDescription();
						}
						SelectDb sd = sm.getSelect(basicCode);
						Vector v = sd.getOptions(new JdbcTemplate());
						for (Object o : v) {
							SelectOptionDb sod = (SelectOptionDb) o;
							if (!sod.isOpen()) {
								continue;
							}
							String val = ParamUtil.get(request, ff.getName() + "_" + StrUtil.escape(sod.getValue()));
							jo.put(val, sod.getValue());
						}
						aryClean.put(jo);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		String rules = ary.toString();
		String cleans = aryClean.toString();
		String name = ParamUtil.get(request, "name");
		
		boolean re = false;
		ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
		try {
			re = mid.create(new JdbcTemplate(), new Object[]{name,formCode,rules,cleans});
		} catch (ResKeyException e) {
			e.printStackTrace();
		}
		
		JSONObject json = new JSONObject();
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
		// return "redirect:/visual/module_import_list.do";
	}

	/***
	 * 上传Excel文件
	 * 
	 * @param file
	 * @return
	 */
	@RequestMapping("/modular/importUpload")
	public String importUpload(@RequestParam(value = "excel", required = true) MultipartFile file,
			 @RequestParam("code") String code,
			 @RequestParam("formCode") String formCode, Model model) {
		model.addAttribute("code", code);
		model.addAttribute("formCode", formCode);
		
/*		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);*/
		
		// 判断文件是否为空
		if (!file.isEmpty()) {
			InputStream in = null;
			try {
				// in = new FileInputStream(path);
				in = file.getInputStream();
				String pa = StrUtil.getFileExt(file.getOriginalFilename());
				if (pa.equals("xls")) {
					// 读取xls格式的excel文档
					HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
					// 获取sheet
					for (int i = 0; i < w.getNumberOfSheets(); i++) {
						HSSFSheet sheet = w.getSheetAt(i);
						if (sheet != null) {
							HSSFCell cell = null;
							// 获取每一行
							HSSFRow row = sheet.getRow(0);
							if (row != null) {
								int colcount = row.getLastCellNum();
								String[] cols = new String[colcount];
								// 获取每一单元格
								for (int m = 0; m < colcount; m++) {
									cell = row.getCell(m);
									if (cell == null) {
										continue;
									}

									cell.setCellType(HSSFCell.CELL_TYPE_STRING);
									String val = cell.getStringCellValue();
									cols[m] = val;
								}
								model.addAttribute("cols", cols);
							}
						}
					}
				} else if ("xlsx".equals(pa)) {
					XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
					for (int i = 0; i < w.getNumberOfSheets(); i++) {
						XSSFSheet sheet = w.getSheetAt(i);
						if (sheet != null) {
							XSSFCell cell = null;
							XSSFRow row = sheet.getRow(0);
							if (row != null) {
								int colcount = row.getLastCellNum();
								String[] cols = new String[colcount];
								
								for (int m = 0; m < colcount; m++) {
									cell = row.getCell(m);
									cell.setCellType(XSSFCell.CELL_TYPE_STRING);
									String val = cell.getStringCellValue();
									cols[m] = val;									
								}
								model.addAttribute("cols", cols);								
							}
						}
					}
				}
			} catch (Exception e) {
				// LogUtil.getLog(SignMgr.class).error(e.getMessage());
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return "visual/module_import_cols";
	}
	
	@RequestMapping(value = "/visual/doImport", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})
	public String doImport(@RequestParam String code, @RequestParam String formCode, @RequestParam String menuItem, @RequestParam String importRecords) {
		Privilege privilege = new Privilege();
		String unitCode = privilege.getUserUnitCode(request);	
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		
		int templateId = ParamUtil.getInt(request, "templateId", -1);
		long parentId = ParamUtil.getLong(request, "parentId", -1);
		String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");

		MacroCtlMgr mm = new MacroCtlMgr();

		ModuleSetupDb msd = new ModuleSetupDb();
		if (parentId==-1) {
			msd = msd.getModuleSetupDbOrInit(code);
		}
		else {
			msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
		}

		// String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String[] fields = msd.getColAry(false, "list_field");

		JSONArray arr = null;
		JSONArray aryCleans = null;		
		if (templateId != -1) {
			ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
			mid = mid.getModuleImportTemplateDb(templateId);
			String rules = mid.getString("rules");
			// DebugUtil.i(getClass(), "doImport", rules);
			try {
				arr = new JSONArray(rules);
				if (arr.length() > 0) {
					fields = new String[arr.length()];
					for (int i = 0; i < arr.length(); i++) {
						JSONObject json = (JSONObject) arr.get(i);
						fields[i] = json.getString("name");
					}
				}
				
				String strJson = StrUtil.getNullStr(mid.getString("cleans"));
				if (!"".equals(strJson)) {
					aryCleans = new JSONArray(strJson);
				}				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		String userName = privilege.getUser(request);
		JSONArray ary = null;
		try {
			ary = new JSONArray(importRecords);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (ary==null) {
			// 防止当导入成功后，再从IE中刷新页面时报错
			if (parentId != -1) {
				ModuleSetupDb parentMsd = new ModuleSetupDb();
				parentMsd = parentMsd.getModuleSetupDbOrInit(code);
				String parentFormCode = parentMsd.getString("form_code");
				String url = "/visual/module_list_relate.jsp?menuItem=" + menuItem
						+ "&code=" + code + "&formCode="
						+ parentFormCode + "&moduleCodeRelated="
						+ moduleCodeRelated + "&parentId=" + parentId + "&tmp=some";
				return url;
			} else {
				return "/visual/module_list.jsp?code=" + code + "&formCode=" + formCode;
			}			
		}
		
		FormDb fdRelate = new FormDb();
		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);

		// 记录不允许重复的字段组合
		Vector vFieldCanNotRepeat = new Vector();
		if (templateId != -1) {
			ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
			mid = mid.getModuleImportTemplateDb(templateId);
			String rules = mid.getString("rules");
			try {
				arr = new JSONArray(rules);
				if (arr.length() > 0) {
					fields = new String[arr.length()];
					for (int i = 0; i < arr.length(); i++) {
						JSONObject json = (JSONObject) arr.get(i);
						fields[i] = json.getString("name");
						int canNotRepeat = json.getInt("canNotRepeat");
						if (canNotRepeat == 1) {
							vFieldCanNotRepeat.addElement(fields[i]);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// 判断配置中是否设置了同步帐户
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");
		Vector records = new Vector();

		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		// 先创建主模块记录
		try {
			int rowCount = ary.length();
			for (int r = 0; r < rowCount; r++) {
				JSONObject jo = (JSONObject) ary.get(r);

				long mainId = -1;
				// 检查主表中是否已存在重复记录，如果已存在，则提取出记录的ID
				if (templateId!=-1) {
					StringBuffer conds = new StringBuffer();
					Iterator ir = vFieldCanNotRepeat.iterator();
					while (ir.hasNext()) {
						String fieldName = (String) ir.next();
						if (!fieldName.startsWith("nest.")) {
							StrUtil.concat(conds, " and ", FormDb.getTableName(formCode)
									+ "."
									+ fieldName
									+ "="
									+ StrUtil.sqlstr(jo.getString(fieldName)));
						}
					}
					if (conds.length()>0) {
						String sql = "select id from form_table_" + formCode + " where " + conds.toString();
						ResultIterator ri = jt.executeQuery(sql);
						if (ri.hasNext()) {
							ResultRecord rr = (ResultRecord)ri.next();
							mainId = rr.getLong(1);
						}
					}
				}
				// 如果未找到重复的主模块记录，则插入本行记录
				if (mainId==-1) {
					FormDAO fdao = new FormDAO(fd);
					for (int m = 0; m < fields.length; m++) {
						if ("cws_creator".equals(fields[m])) {
							fdao.setCreator(userName);
						} else {
							if (templateId != -1) {
								if (fields[m].startsWith("nest.")) {
									continue;
								}
							}
							String val = "";
							if (jo.has(fields[m])) {
								val = jo.getString(fields[m]);
							} else {
								LogUtil.getLog(getClass()).error("字段：" + fields[m] + " 在导入的文件中不存在");
								continue;
							}
							FormField ff = fd.getFormField(fields[m]);
							if (ff == null) {
								LogUtil.getLog(getClass()).error("字段：" + fields[m] + " 已不存在");
								continue;
							}
							if (ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null && !"macro_raty".equals(mu.getCode())) {
									// 如果是基础数据宏控件
									boolean isClean = false;
									if ("macro_flow_select".equals(mu.getCode())) {
										JSONObject json = null;
										if (aryCleans != null) {
											for (int i = 0; i < aryCleans.length(); i++) {
												json = aryCleans.getJSONObject(i);
												if (ff.getName().equals(json.get("fieldName"))) {
													isClean = true;
													break;
												}
											}
										}
										// 如果需清洗数据
										if (isClean) {
											val = json.getString(val);
										}
									}
									if (!isClean) {
										val = mu.getIFormMacroCtl().getValueByName(ff, val);
									}
								}
							}
							fdao.setFieldValue(fields[m], val);
						}
					}
					fdao.setCreator(userName);
					fdao.setUnitCode(unitCode);
					if (parentId != -1) {
						fdao.setCwsId(String.valueOf(parentId));
					}
					fdao.create();
					// 如果需要记录历史
					if (fd.isLog()) {
						FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
					}

					records.addElement(fdao);

					if (formCode.equals("personbasic") && isArchiveUserSynAccount) {
						UserDb ud = new UserDb();
						// 为新增用户自动创建帐户
						com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
						// 默认密码
						String defaultPwd = scfg.getInitPassword();
						String un = fdao.getFieldValue("user_name");
						if (un != null && !"".equals(un)) {
							ud.create(un, fdao.getFieldValue("realname"), defaultPwd, "", unitCode);
							userCache.refreshCreate();

							String deptCode = fdao.getFieldValue("dept");
							if (deptCode != null && !"".equals(deptCode)) {
								DeptUserDb dud = new DeptUserDb();
								try {
									dud.create(deptCode, fdao.getFieldValue("user_name"), "");
								} catch (ErrMsgException e) {
									e.printStackTrace();
								}
							}
						}
					}

					mainId = fdao.getId();
				}
				
				// 创建从模块记录
				ModuleRelateDb mrd = new ModuleRelateDb();
				Vector v = mrd.getModulesRelated(formCode);
				// 遍历所有从模块，并创建从模块的记录
				Iterator ir = v.iterator();
				while (ir.hasNext()) {
					mrd = (ModuleRelateDb) ir.next();
					
					String relateCode = mrd.getString("relate_code");
					fdRelate = fdRelate.getFormDb(relateCode);

					FormDAO fdao = new FormDAO(fdRelate);
					
					// 在配置中是否有从模块中的字段
					boolean isFind = false;
					
					for (int m = 0; m < fields.length; m++) {
						if ("cws_creator".equals(fields[m])) {
							fdao.setCreator(userName);
						} else {
							String fieldName = fields[m];
							if (templateId != -1) {
								if (!fieldName.startsWith("nest.")) {
									continue;
								}
								int p = fieldName.indexOf(".");
								int q = fieldName.lastIndexOf(".");
								String formCodeRelate = fieldName.substring(
										p + 1, q);
								// 如果不是对应的从模块，则跳过
								if (!formCodeRelate.equals(relateCode)) {
									continue;
								}
								
								isFind = true;
								fieldName = fieldName.substring(q + 1);
							}
							String val = jo.getString(fields[m]);
							FormField ff = fdRelate.getFormField(fieldName);
							if (ff == null) {
								LogUtil.getLog(getClass()).error(
										"字段：" + fieldName + " 已不存在");
								continue;
							}
							if (ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu != null
										&& !"macro_raty".equals(mu.getCode())) {
									// 如果是基础数据宏控件
									val = mu.getIFormMacroCtl().getValueByName(ff, val);
								}
							}
							fdao.setFieldValue(fieldName, val);
						}
					}
					
					if (isFind) {
						String relateFieldValue = fdm.getRelateFieldValue(mainId, relateCode);
						fdao.setCwsId(relateFieldValue);
	
						fdao.setUnitCode(unitCode);
						fdao.create();
						// 如果需要记录历史
						if (fdRelate.isLog()) {
							FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
						}
					}
				}				
			}

			// 导入后事件
			ModuleSetupDb vsd = new ModuleSetupDb();
			vsd = vsd.getModuleSetupDbOrInit(formCode);
			String script = vsd.getScript("import_create");
			if (script != null && !"".equals(script)) {
				Interpreter bsh = new Interpreter();
				try {
					StringBuffer sb = new StringBuffer();

					// 赋值用户
					sb.append("userName=\"" + privilege.getUser(request) + "\";");
					bsh.eval(BeanShellUtil.escape(sb.toString()));

					bsh.set("records", records);
					bsh.set("request", request);

					bsh.eval(script);
				} catch (EvalError e) {
					e.printStackTrace();
				}
			}
		}
		catch (JSONException | SQLException | ErrMsgException e) {
			e.printStackTrace();
		} finally {
			jt.close();
		}

		StringBuffer params = new StringBuffer();
		Enumeration reqParamNames = request.getParameterNames();
		while (reqParamNames.hasMoreElements()) {
			String paramName = (String) reqParamNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = ParamUtil.getParam(request, paramName);
				// 过滤掉formCode等
				if ("code".equals(paramName)
						|| "formCode".equals(paramName)
						|| "menuItem".equals(paramName)
						|| "parentId".equals(paramName)
						|| "moduleCodeRelated".equals(paramName)
						|| "importRecords".equals(paramName) // 过滤掉，否则因数据太多可能会报：HeadersTooLargeException: 尝试将更多数据写入响应标头，而不是缓冲区中有可用空间。 增加连接器上的maxHttpHeaderSize或将更少的数据写入响应头。
				) {
					;
				}
				else {
					StrUtil.concat(params, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
				}
			}
		}
		// tmp=some是因为controller在返回时会自动加.jsp
		if (parentId != -1) {
			ModuleSetupDb parentMsd = new ModuleSetupDb();
			parentMsd = parentMsd.getModuleSetupDbOrInit(code);
			String parentFormCode = parentMsd.getString("form_code");
			return "redirect:module_list_relate.jsp?parentId=" + parentId + "&menuItem=" + menuItem
					+ "&code=" + code + "&formCode="
					+ parentFormCode + "&moduleCodeRelated=" + moduleCodeRelated + "&" + params + "&tmp=some";
		} else {
			return "redirect:module_list.jsp?code=" + code + "&formCode=" + formCode + "&" + params + "&tmp=some";
		}
	}

}
