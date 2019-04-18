package com.redmoon.oa.android.visual;


import java.util.HashMap;
import java.util.Iterator;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;



import cn.js.fan.util.ErrMsgException;

import cn.js.fan.util.StrUtil;


import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.post.PostDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;
import com.redmoon.oa.visual.SQLBuilder;



public class ModuleListAction extends BaseAction{
	private String moduleCode = "";
	private String skey = "";
	private String formCode;
	private int pagesize = 15;
	private int pagenum = 1;
	
	


	/**
	 * @return the pagesize
	 */
	public int getPagesize() {
		return pagesize;
	}

	/**
	 * @param pagesize the pagesize to set
	 */
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}




	private String op = "";
	
	private String orderBy = "id";
	private String sort = "desc";
	
	/**
	 * @return the orderBy
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * @return the sort
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * @param sort the sort to set
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * @return the moduleCode
	 */
	public String getModuleCode() {
		return moduleCode;
	}

	/**
	 * @param moduleCode the moduleCode to set
	 */
	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	/**
	 * @return the op
	 */
	public String getOp() {
		return op;
	}

	/**
	 * @param op the op to set
	 */
	public void setOp(String op) {
		this.op = op;
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


	/**
	 * @return the pagenum
	 */
	public int getPagenum() {
		return pagenum;
	}

	/**
	 * @param pagenum the pagenum to set
	 */
	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	@Override
	public void executeAction() {
	
		// TODO Auto-generated method stub
		super.executeAction();
	    try {

	    	Privilege privilege = new Privilege();
			boolean re = privilege.Auth(getSkey());
			jReturn.put(RES,RETURNCODE_SUCCESS); //请求成功
			if(re){
				jResult.put(RETURNCODE,RESULT_TIME_OUT); //登录超时
			}else{
				HttpServletRequest request = ServletActionContext.getRequest();
				privilege.doLogin(request, skey);
			
				if(formCode == null || formCode.trim().equals("")){
					jResult.put(RETURNCODE,RESULT_FORMCODE_ERROR); //表单为空
				}else{
					FormDb fd = new FormDb();
					fd = fd.getFormDb(formCode); 
					if (!fd.isLoaded()) { //表单不存在
						jResult.put(RETURNCODE,RESULT_FORMCODE_ERROR);//表单不存在
						return;
					}else{
						MacroCtlUnit mu;
						MacroCtlMgr mm = new MacroCtlMgr();
						
						FormDAO fdao = new FormDAO();
						ModuleSetupDb msd = new ModuleSetupDb();
						msd = msd.getModuleSetupDb(moduleCode);
						int is_workLog = msd.getInt("is_workLog");
						if (msd==null) {
							jResult.put(RETURNCODE,RESULT_MODULE_ERROR);//表单不存在
							return;
						}
						if(is_workLog == 1){
							jResult.put("isWorkLog", true);
						}else{
							jResult.put("isWorkLog", false);
						}
						
						String[] ary = null;
						request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
						ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
						String sql = ary[0];
					
						
						String listField = StrUtil.getNullStr(msd.getString("list_field"));
						String[] formFieldArr = StrUtil.split(listField, ",");
						if(formFieldArr != null && formFieldArr.length>0){
							ListResult lr = fdao.listResult(formCode,sql,
									pagenum, pagesize);
							int total = lr.getTotal();
							if(total == 0){
								jResult.put(RETURNCODE,RESULT_NO_DATA); //没有数据
							}else{
								jResult.put("total", total); //总页数
								jResult.put(RETURNCODE,RESULT_SUCCESS); //请求陈宫
								Iterator ir = null;
								Vector v = lr.getResult();
								JSONArray dataArr = new JSONArray();
								if (v != null){
									ir = v.iterator();
								}
								HashMap<String,FormField> map = getFormFieldsByFromCode(formCode);
								while (ir != null && ir.hasNext()) {
									fdao = (FormDAO) ir.next();
									JSONObject dataObj = new JSONObject();
									long id  = fdao.getId();
									dataObj.put("id", id);;
									dataObj.put("creator", fdao.getCreator());
									for(String filed:formFieldArr){
										if(map.containsKey(filed)){
											FormField ff = map.get(filed);
											JSONObject formFieldObj = new JSONObject(); //Form表单对象
											formFieldObj.put("title", ff.getTitle());
											String value = fdao.getFieldValue(filed); //表单值
											ff.setValue(value);
											formFieldObj.put("value", value);
											String type = ff.getType();// 类型描述
											formFieldObj.put("type", type);
											if (type.equals("macro")) {
												mu = mm.getMacroCtlUnit(ff.getMacroType());
												String macroCode = mu.getCode();
												String controlText = StrUtil.getNullStr(mu.getIFormMacroCtl()
														.getControlText(privilege.getUserName(getSkey()),
																ff));
												
												if(macroCode.equals(MACRO_USER_SELECT_WIN) || macroCode.equals(MACRO_CURRENT_USER) || macroCode.equals(MACRO_USER_SELECT)){
													UserDb userDb = new UserDb(value);
													controlText = userDb.getRealName();
												}else if(macroCode.equals(MACRO_DEPT_SELECT)){
													DeptDb deptDb = new DeptDb(value);
													controlText = deptDb.getName();
												
												}else if(macroCode.equals(MACRO_POST) || macroCode.equals(MACRO_POST_LIST)){
													if(value != null && !value.equals("")){
														PostDb postDb = new PostDb();
														postDb = postDb.getPostDb(Integer.parseInt(value));
														controlText = postDb.getString("name");
													}
													
													
												}
												formFieldObj.put("text", controlText);
												formFieldObj.put("type", type);
											}
											dataObj.put(filed, formFieldObj);
										}
									}
									dataArr.put(dataObj);//组装json数组
								}
								jResult.put(DATAS,dataArr);
								
							}
						}
						
					}
					
				}
				
			}
			jReturn.put(RESULT, jResult);
		} catch (JSONException e) {
			Logger.getLogger(ModuleListAction.class.getName()).error(e.getMessage());
		} catch (ErrMsgException e) {
			Logger.getLogger(ModuleListAction.class.getName()).error(e.getMessage());
		}
		
	}

	
	
	
	/**
	 * 获得所有字段信息
	 * @return
	 */
	public HashMap<String,FormField> getFormFieldsByFromCode(String formCode){
		HashMap<String,FormField> map = new HashMap<String, FormField>();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode); 
		FormDAO fdao = new FormDAO(fd);//获得所有表单元素
		Iterator fdaoIr = fdao.getFields().iterator();
		while(fdaoIr!=null && fdaoIr.hasNext()){
			FormField ff = (FormField)fdaoIr.next();
			map.put(ff.getName(), ff);
		}
		return map;
		
	} 
	
	

}
