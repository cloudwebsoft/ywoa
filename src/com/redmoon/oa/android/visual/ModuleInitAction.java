package com.redmoon.oa.android.visual;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.visual.FormDAO;

public class ModuleInitAction {
	private String skey = "";
	private String formCode;
	private String result = "";
	private long id = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public String execute() {
		// 手机客户端 —— 新增 判断 需要显示的列
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		try {
			boolean re = privilege.Auth(getSkey());
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			if (formCode != null && !formCode.trim().equals("")) {
				FormDb formDb = new FormDb(formCode);
				FormDAO formDao = null;
				if (id != 0) {
					formDao = new FormDAO(id, formDb);
					// 获得附件
					Iterator itFiles = formDao.getAttachments().iterator();

					JSONArray filesArr = new JSONArray();
					while (itFiles.hasNext()) {
						JSONObject fileObj = new JSONObject();
						com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
								.next();
						String name = am.getName();
						int attId = am.getId();
						String downUrl = "/public/visual/visual_getfile.jsp";
						fileObj.put("name", name);
						fileObj.put("downloadUrl", downUrl);
						fileObj.put("id", attId);
						filesArr.put(fileObj);
					}
					json.put("files", filesArr);
				} else {
					formDao = new FormDAO(formDb);
				}
				MacroCtlMgr macroCtrlMgr = new MacroCtlMgr();
				MacroCtlUnit macroCtrlUnit = null;
				Iterator it = formDao.getFields().iterator();
				JSONArray fieldsArr = new JSONArray();
				while (it.hasNext()) {
					FormField ff = (FormField) it.next();
					JSONObject field = new JSONObject();// json field
					if (ff.isMobileDisplay() && ff.getHide()==FormField.HIDE_NONE) {
						field.put("title", ff.getTitle());// 标题
						field.put("isCanNull", ff.isCanNull());// 标题
						field.put("code", ff.getName());// 名称
						field.put("desc", ff.getDefaultValueRaw());
						// field.put("editable",true);//控件是否显示
						// field.put("isHidden",false);//是否隐藏
						String type = ff.getType();// 类型描述
						field.put("type", type);
						// 判断是否是宏控件类型
						if (type.equals("macro")) {
							String macroCode = ff.getMacroType();
							field.put("macroCode", macroCode);// 宏控件的code
							macroCtrlUnit = macroCtrlMgr
									.getMacroCtlUnit(macroCode);
							String macroType = macroCtrlUnit.getIFormMacroCtl()
									.getControlType();
							field.put("macroType", macroType);// 宏控件类型
							String controlText = StrUtil
									.getNullStr(macroCtrlUnit
											.getIFormMacroCtl()
											.getControlText(
													privilege
															.getUserName(getSkey()),
													ff));
							String controlValue = StrUtil
									.getNullStr(macroCtrlUnit
											.getIFormMacroCtl()
											.getControlValue(
													privilege
															.getUserName(getSkey()),
													ff));
							field.put("text", controlText);// 文本
							field.put("value", controlValue);// 显示的值
							if (macroType.equals("select")
									|| macroType.equals("buttonSelect")) {
								// 一般options只有在拉框中显示
								String options = StrUtil
										.getNullStr(macroCtrlUnit
												.getIFormMacroCtl()
												.getControlOptions(
														privilege
																.getUserName(getSkey()),
														ff));
								if (options != null && !options.trim().equals("")) {
									JSONArray opinionArr = new JSONArray(
											options);
									field.put("options", opinionArr);
								}
							} else {
								JSONArray opinionArr = new JSONArray();
								field.put("options", opinionArr);
							}
						} else {
							String value = StrUtil.getNullStr(ff.getValue());
							if (value.equals("")) {
								value = ff.getDefaultValue();
							}
							if (type.equals("DATE") || type.equals("DATE_TIME")) {
								if (id != 0) {
									field.put("value", ff.getValue());
								} else {
									field.put("value", ff.getDefaultValueRaw());
								}

							} else if (type.equals("select")) {// 解析普通控件中
																// select控件
								String[][] optionsArray = FormParser
										.getOptionsArrayOfSelect(formDb, ff);
								if (optionsArray != null
										&& optionsArray.length > 0) {
									JSONArray options = new JSONArray();
									for (String[] option : optionsArray) {
										JSONObject optionObj = new JSONObject();
										if (value.equals(option[0])) {
											field.put("text", option[1]);
											field.put("value", value);
										}
										optionObj.put("value", option[0]);
										optionObj.put("name", option[1]);
										options.put(optionObj);
									}
									field.put("options", options);
								}
							} else {
								field.put("value", value);
							}
						}
						
					}
					fieldsArr.put(field);
				}
				json.put("fields", fieldsArr);
				// 判断 是否允许上传附件
				json.put("hasAttach", formDb.isHasAttachment());
				json.put("res", "0");
				json.put("msg", "操作成功");
				setResult(json.toString());
				return "SUCCESS";
			} else {
				json.put("res", "-1");
				json.put("msg", "表单编码为空！");
				setResult(json.toString());
				return "SUCCESS";
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ModuleInitAction.class).error(e.getMessage());
		}
		return "SUCCESS";
	}

}
