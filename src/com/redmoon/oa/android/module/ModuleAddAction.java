package com.redmoon.oa.android.module;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import com.redmoon.oa.flow.macroctl.NestSheetCtl;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleSetupDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-8-13上午11:04:05
 */
public class ModuleAddAction {
	private String skey = "";
	private String moduleCode;
	private String result = "";

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

	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
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
			
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());
			
			if (moduleCode != null && !moduleCode.trim().equals("")) {
				ModuleSetupDb msd = new ModuleSetupDb();
				msd = msd.getModuleSetupDb(moduleCode);
				String formCode = msd.getString("form_code");
				FormDb fd = new FormDb();
				fd = fd.getFormDb(formCode);
				
				Vector v = fd.getFields();
				Vector vWritable = new Vector(); // 可写表单域（去除了隐藏字段）
				
				// 置可写表单域
				String userName = privilege.getUserName(getSkey());
				ModulePrivDb mpd = new ModulePrivDb(formCode);
	           	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	           	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
				// 将不显示的字段加入fieldHide
				Iterator ir = v.iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					if (ff.getHide()==FormField.HIDE_EDIT || ff.getHide()==FormField.HIDE_ALWAYS) {
						if ("".equals(fieldHide)) {
							fieldHide = ff.getName();
						}
						else {
							fieldHide += "," + ff.getName();
						}
					}
				}
	            String[] fdsHide = StrUtil.split(fieldHide, ","); 
	            
	           	if (!"".equals(fieldWrite)) {
	    	        String[] fds = StrUtil.split(fieldWrite, ",");
	    	        if (fds!=null) {
	    	        	int len = fds.length;
	    	        	
	    	            // 将不可写的域筛选出
	    	            ir = v.iterator();
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
	    	                    vWritable.addElement(ff);
	    	                    // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
	    	                    ff.setEditable(false);
	    	                }
	    	            }        	        	
	    	        }
	    	        
	    	        // 从可写字段中去掉隐藏字段
	    	        if (fdsHide!=null) {
	    	            ir = vWritable.iterator();
	    	            while(ir.hasNext()) {
	    	                FormField ff = (FormField) ir.next();
	    	                for (int k=0; k<fdsHide.length; k++) {
	    		                if (ff.getName().equals(fdsHide[k])) {
	    		                	ir.remove();
	    		                }
	    	                }
	    	            }
	    	        }
	           	}
    	        else {
    	        	// 全部可写
    	        	vWritable = v;
    	        }	           	
				
	           	JSONArray fields = new JSONArray();
				MacroCtlUnit mu;
				MacroCtlMgr mm = new MacroCtlMgr();
				ir = vWritable.iterator();
				json.put("res", "0");
				// 遍历表单字段-------------------------------------------------
				while (ir.hasNext()) {
					FormField ff = (FormField) ir.next();
					
					if (!ff.isMobileDisplay()) {
						continue;
					}

					JSONObject field = new JSONObject();
					String desc = StrUtil.getNullStr(ff.getDescription());
					field.put("title", ff.getTitle());
					field.put("code", ff.getName());
					field.put("desc", desc);

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
					String controlValue = "";
					JSONArray opinionArr = null;
					JSONObject opinionVal = null;

					String macroCode = "";

					String metaData = "";

					JSONArray js = new JSONArray();
					if (ff.getType().equals("macro")) {
						mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu == null) {
							LogUtil.getLog(getClass()).error(
									"MactoCtl " + ff.getTitle() + "："
											+ ff.getMacroType() + " is not exist.");
							continue;
						}
						
						metaData = mu.getIFormMacroCtl().getMetaData(ff);

						macroCode = mu.getCode();

						macroType = mu.getIFormMacroCtl().getControlType();
						controlText = mu.getIFormMacroCtl().getControlText(
								privilege.getUserName(getSkey()), ff);
						controlValue = mu.getIFormMacroCtl().getControlValue(
								privilege.getUserName(getSkey()), ff);
						options = mu.getIFormMacroCtl().getControlOptions(
								privilege.getUserName(getSkey()), ff);
						// options = options.replaceAll("\\\"", "");
						if (options != null && !options.equals("")) {
							// options = options.replaceAll("\\\"", "");
							js = new JSONArray(options);
						}
					} else {
						String type = ff.getType();
						if (type != null && !type.equals("")) {
							if (type.equals("DATE") || type.equals("DATE_TIME")) {
								controlValue = ff.getDefaultValueRaw();
							} else {
								controlValue = ff.getDefaultValue();
							}
						} else {
							controlValue = ff.getDefaultValue();
						}
					}
					// 判断是否为意见输入框
					if (macroCode != null && !macroCode.equals("")) {
						if (macroCode.equals("macro_opinion")||macroCode.equals("macro_opinionex")) {
							if (controlText != null
									&& !controlText.trim().equals("")) {
								opinionArr = new JSONArray(controlText);
							}
							if (controlValue != null
									&& !controlValue.trim().equals("")) {
								opinionVal = new JSONObject(controlValue);
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
						// options = fp.getOptionsOfSelect(fd, ff);
						String[][] optionsArray = FormParser
								.getOptionsArrayOfSelect(fd, ff);
						for (int i = 0; i < optionsArray.length; i++) {
							String[] optionsItem = optionsArray[i];
							JSONObject option = new JSONObject();
							try {
								option.put("value", optionsItem[0]);
								option.put("name", optionsItem[1]);
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							js.put(option);
						}
					} else if (ff.getType().equals("radio")) {
						// options = fp.getOptionsOfSelect(fd, ff);
						String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
						for (int i = 0; i < optionsArray.length; i++) {
							JSONObject option = new JSONObject();
							option.put("value", optionsArray[i]);
							option.put("name", optionsArray[i]);
							js.put(option);
						}
					}
					field.put("options", js);
					field.put("text", controlText);
					String level = "";
					if (ff.getType().equals("checkbox")) {
						// level = "个人兴趣";
						level = ff.getTitle();
					}
					field.put("level", level);
					field.put("macroType", macroType);
					field.put("editable", String.valueOf(ff.isEditable()));					
					field.put("isNull", String.valueOf(ff.isCanNull()));
					field.put("fieldType", ff.getFieldTypeDesc());
					if (opinionVal != null) {
						field.put("value", opinionVal);
					} else {
						field.put("value", controlValue);
					}
					if (opinionArr != null && opinionArr.length() > 0) {
						field.put("text", opinionArr);
					} else {
						field.put("text", controlText);
					}
					field.put("macroCode", macroCode);

					// 可传SQL控件条件中的字段
					field.put("metaData", metaData);
					
					fields.put(field);
				}

				json.put("fields", fields);
				// 是否允许上传附件
				json.put("hasAttach", fd.isHasAttachment());				
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
			LogUtil.getLog(ModuleAddAction.class).error(e.getMessage());
		}
		return "SUCCESS";
	}
}
