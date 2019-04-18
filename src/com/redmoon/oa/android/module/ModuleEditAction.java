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
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
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
 * @Date: 2017-8-13下午12:04:05
 */
public class ModuleEditAction {
	private String skey = "";
	private String result = "";
	private String moduleCode = "";
	private long id = 0;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String execute() {
		
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
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
		
		HttpServletRequest request = ServletActionContext.getRequest();
		privilege.doLogin(request, getSkey());		

		if (moduleCode != null && !moduleCode.trim().equals("")) {
           	JSONArray fields = new JSONArray();
			MacroCtlUnit mu;
			MacroCtlMgr mm = new MacroCtlMgr();
			try {
				json.put("res", "0");
				
				ModuleSetupDb msd = new ModuleSetupDb();
				msd = msd.getModuleSetupDb(moduleCode);
				String formCode = msd.getString("form_code");
				FormDb fd = new FormDb();
				fd = fd.getFormDb(formCode);
				Vector v = fd.getFields();
				
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
	    	        
	    	        // 去掉隐藏字段
	               	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
	                String[] fdsHide = StrUtil.split(fieldHide, ",");    	        
	    	        if (fdsHide!=null) {
	    	            Iterator ir = v.iterator();
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
				
	           	FormDAO fdao = new FormDAO();
	           	fdao = fdao.getFormDAO(id, fd);
	           	Iterator ir = fdao.getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField) ir.next();

					if (!ff.isMobileDisplay()) {
						continue;
					}
					
					String val = fdao.getFieldValue(ff.getName());

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

						IFormMacroCtl ifmc = mu.getIFormMacroCtl();
						ifmc.setVisualFormDAO(fdao);
						
						macroType = mu.getIFormMacroCtl().getControlType();
						metaData = mu.getIFormMacroCtl().getMetaData(ff);
						macroCode = mu.getCode();

						// 如果值为null，则在json中put的时候，是无效的，不会被记录至json中
						controlText = StrUtil.getNullStr(mu.getIFormMacroCtl()
								.getControlText(privilege.getUserName(getSkey()),
										ff));
						val = StrUtil.getNullStr(mu.getIFormMacroCtl()
								.getControlValue(privilege.getUserName(getSkey()),
										ff));
						options = mu.getIFormMacroCtl().getControlOptions(
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
					// field.put("isHidden", String.valueOf(ff.isHidden())); // 之前已被去除
					field.put("isNull", String.valueOf(ff.isCanNull()));
					field.put("fieldType", ff.getFieldTypeDesc());
				
					field.put("macroCode", macroCode);
					
					// 传SQL控件条件中的字段
					field.put("metaData", metaData);

					fields.put(field);
				}
				
				json.put("fields", fields);
				
				Iterator itFiles = fdao.getAttachments().iterator();
				JSONArray filesArr = new JSONArray();
				while (itFiles.hasNext()) {
					com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
							.next();
					JSONObject fileObj = new JSONObject();
					String name = am.getName();
					fileObj.put("name", name);
					String url = "/public/visual/visual_getfile.jsp?attachId=" + am.getId();
					fileObj.put("url", url);
					fileObj.put("id", am.getId());
					fileObj.put("size", String.valueOf(am.getFileSize()));					
					filesArr.put(fileObj);
				}
				json.put("files", filesArr);	
				
				// 是否允许上传附件
				json.put("hasAttach", fd.isHasAttachment());				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		setResult(json.toString());
		return "SUCCESS";
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public String getModuleCode() {
		return moduleCode;
	}

}
