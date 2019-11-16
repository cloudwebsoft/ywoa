package com.redmoon.oa.android.module;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import com.redmoon.oa.flow.macroctl.NestSheetCtl;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-8-13下午03:42:15
 */
public class ModuleShowAction {
	private String skey = "";
	private String result = "";
	private long id;
	private String moduleCode = "";	

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
		String userName = privilege.getUserName(skey);
		if (userName==null || "".equals(userName)) {
			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
			userName = pvg.getUser(request);
		}		
		
	    MacroCtlUnit mu;
		MacroCtlMgr mm = new MacroCtlMgr();
		try {
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDb(moduleCode);
			String formCode = msd.getString("form_code");
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);

			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(id, fd);

			mm = new MacroCtlMgr();
			json.put("res","0");
			json.put("id", id);
			
			JSONArray fields  = new JSONArray(); 
	
			ModulePrivDb mpd = new ModulePrivDb(formCode);			
			boolean isHideField = true;
           	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
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
            String[] fdsHide = StrUtil.split(fieldHide, ","); 
	        
            Vector v = fdao.getFields();
			ir = v.iterator();
			while (ir.hasNext()) {	
				FormField ff = (FormField) ir.next();
				
				// 跳过隐藏域
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

				String val = StrUtil.getNullStr(ff.getValue());
				
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
				field.put("title", ff.getTitle());
				field.put("code",ff.getName());
				if(jsonArr!=null){
					field.put("value",jsonArr);		
				}else{
					field.put("value",val);		
				}
				field.put("type", ff.getType());
				String level="";
				if (ff.getType().equals("checkbox")) {
					level = ff.getTitle();
				}
				field.put("level",level);
				field.put("macroCode", macroCode);
				fields.put(field);	
			}
			json.put("fields",fields);
			
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
			
			// 关联模块
			FormDb fdRelated = new FormDb();
			JSONArray arrRelated = new JSONArray();
			ModuleRelateDb mrdTop = new ModuleRelateDb();
			java.util.Iterator irTop = mrdTop.getModulesRelated(formCode).iterator();
			while (irTop.hasNext()) {
				mrdTop = (ModuleRelateDb)irTop.next();
				// 有查看权限才能看到从模块选项卡
				ModulePrivDb mpdTop = new ModulePrivDb(mrdTop.getString("relate_code"));
				if (mpdTop.canUserSee(userName)) {	
					String name = fdRelated.getFormDb(mrdTop.getString("relate_code")).getName();
					JSONObject jsonRelated = new JSONObject();
					jsonRelated.put("name", name);
					jsonRelated.put("formCodeRelated", mrdTop.getString("relate_code"));					
					arrRelated.put(jsonRelated);
				}
			}
			json.put("formRelated", arrRelated);
			
			// 其它标签
			String[] subTags = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
			String[] subTagUrls = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
			int subLen = 0;
			if (subTags!=null)
				subLen = subTags.length;
			JSONArray subArr = new JSONArray();
			for (int i=0; i<subLen; i++) {
				// String uri = ModuleUtil.filterViewEditTagUrl(request, codeTop, subTagsTop[i]);
				String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, subTags[i]);
		    	if (tagUrl.startsWith("{")) {
					JSONObject jsonTag = new JSONObject(tagUrl);
					if (!jsonTag.isNull("fieldSource")) {
						JSONObject jsonRelated = new JSONObject();
						jsonRelated.put("tagName", subTags[i]);
						jsonRelated.put("subTagIndex", i); // 因为传tagName用于得到配置信息时，从RelateListAction取出来时乱码（因为是中文），所以用该键值来传递信息
						subArr.put(jsonRelated);
						continue;
					}
		    	}
			}
			json.put("subTags", subArr);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public String getModuleCode() {
		return moduleCode;
	}
}
