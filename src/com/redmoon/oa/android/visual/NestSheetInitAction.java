package com.redmoon.oa.android.visual;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleSetupDb;

public class NestSheetInitAction extends BaseAction {
	private String skey = "";
	private String result = "";
	private long id = 0;
	
	String parentFormCode;	// 父表单编码
	/**
	 * @return the parentFormCode
	 */
	public String getParentFormCode() {
		return parentFormCode;
	}

	/**
	 * @param parentFormCode the parentFormCode to set
	 */
	public void setParentFormCode(String parentFormCode) { 
		this.parentFormCode = parentFormCode;
	}
 
	String formCode; 		// 嵌套表Code
	// String cwsStatus;		// 如果parentId不为-1，即流程中默认为0，将来智能模块中默认为1
	// cws_id：主表中关联字段的值
	int actionId;			// 流程节点ID
	int flowId;				// 流程ID
	
	long parentId = -1;

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public long getId() { 
		return id;
	}

	/**
	 * @return the actionId
	 */
	public int getActionId() {
		return actionId;
	}

	/**
	 * @param actionId the actionId to set
	 */
	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	/**
	 * @return the flowId
	 */
	public int getFlowId() {
		return flowId;
	}

	/**
	 * @param flowId the flowId to set
	 */
	public void setFlowId(int flowId) {
		this.flowId = flowId;
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
			
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDb(formCode);
			if (msd == null || !msd.isLoaded()) {
				json.put("res", "-1");
				json.put("msg", "模块不存在！");
				setResult(json.toString());
				return "SUCCESS";
			}
			else {
				formCode = msd.getString("form_code");
			}
			
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());
			
			FormDb formDb = new FormDb(formCode);
			if (formCode != null && !formCode.trim().equals("")) {
				FormDAO formDao = null;
				if (id != 0) {
					formDao = new FormDAO(id, formDb);
				} else {
					formDao = new FormDAO(formDb);
				}
				
				if (flowId!=-1) {
					WorkflowActionDb wfa = new WorkflowActionDb();
					wfa = wfa.getWorkflowActionDb(actionId);
					String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();
	
					String[] fds = fieldWrite.split(",");
					int len = fds.length;
	
					int nestLen = "nest.".length();
					// 将嵌套表中不可写的域筛选出
					Iterator ir = formDao.getFields().iterator();
					while (ir.hasNext()) {
						FormField ff = (FormField) ir.next();
	
						boolean finded = false;
						for (int i = 0; i < len; i++) {
				            // 如果不是嵌套表格2的可写表单域
			                if (!fds[i].startsWith("nest.")) {
			                    continue;
			                }
			                String fName = fds[i].substring(nestLen);
							if (ff.getName().equals(fName)) {
								finded = true;
								break;
							}
						}
	
						if (!finded) {
							// 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
							ff.setEditable(false);
						}
					}		
				}
				else {
					Vector v = formDb.getFields();
					
					// 置可写表单域
					String userName = privilege.getUserName(getSkey());
					ModulePrivDb mpd = new ModulePrivDb(formCode);
		           	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
		            
		           	if (!"".equals(fieldWrite)) {
		    	        String[] fds = StrUtil.split(fieldWrite, ",");
		    	        if (fds!=null) {
		    	        	int len = fds.length;
		    	            // 将不可写的域筛选出
		    	            Iterator ir = v.iterator();
		    	            while (ir.hasNext()) {
		    	                FormField ff = (FormField)ir.next();

		    	                boolean finded = false;
		    	                for (int i=0; i<len; i++) {
		    	                    if (ff.getName().equals(fds[i])) {
		    	                        finded = true;
		    	                        break;
		    	                    }
		    	                }

		    	                if (finded) {
		    	                    // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
		    	                    ff.setEditable(false);
		    	                }
		    	            }        	        	
		    	        }
		           	}	
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
						
						// 如果是计算控件，则取出精度和四舍五入属性
						if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
							FormParser fp = new FormParser();
							String isroundto5 = fp.getFieldAttribute(formDao.getFormDb(), ff, "isroundto5");
							String digit = fp.getFieldAttribute(formDao.getFormDb(), ff, "digit");
							field.put("formula", ff.getDefaultValueRaw());
							field.put("isroundto5", isroundto5);
							field.put("digit", digit);
						}
						
						String metaData = "";
						
						// 判断是否是宏控件类型
						if (type.equals("macro")) {
							String macroCode = ff.getMacroType();
							field.put("macroCode", macroCode);// 宏控件的code
							macroCtrlUnit = macroCtrlMgr
									.getMacroCtlUnit(macroCode);
							
							metaData = macroCtrlUnit.getIFormMacroCtl().getMetaData(ff);

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
						
						// 可传SQL控件条件中的字段
						field.put("metaData", metaData);
					}
					
					field.put("isEditable", ff.isEditable());
					
					fieldsArr.put(field);
				}
				json.put("fields", fieldsArr);
				json.put("formCode", formCode);
				json.put("res", "0");
				json.put("msg", "操作成功");
				
				if (flowId!=-1) {
			        FormDb flowFd = new FormDb();
			        flowFd = flowFd.getFormDb(parentFormCode);
			        com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
			        fdaoFlow = fdaoFlow.getFormDAO(flowId, flowFd);
					long parentId = fdaoFlow.getId();
					
					com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
					String relateFieldValue = fdm.getRelateFieldValue(parentId, formCode);
					
					json.put("cws_id", relateFieldValue);
				}
				else {
					json.put("cws_id", String.valueOf(parentId));
				}
				
				setResult(json.toString());
				return "SUCCESS";
			} else {
				json.put("res", "-1");
				json.put("msg", "表单编码为空！");
				setResult(json.toString());
				return "SUCCESS";
			}

		} catch (JSONException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			LogUtil.getLog(ModuleInitAction.class).error(e.getMessage());
		}
		return "SUCCESS";
	}

}

