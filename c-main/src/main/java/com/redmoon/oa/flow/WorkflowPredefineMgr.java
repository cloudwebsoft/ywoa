package com.redmoon.oa.flow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.js.fan.util.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import cn.js.fan.web.SkinUtil;
import com.redmoon.oa.pvg.PrivDb;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
// 日程安排
public class WorkflowPredefineMgr {

	private int newId = -1;

	public static final String SUBSTITUTION = "#@#";

	public WorkflowPredefineMgr() {

	}

	public boolean modify(HttpServletRequest request) throws ErrMsgException, JDOMException, IOException {
		Privilege privilege = new Privilege();
		String priv = "admin.flow";
		if (!privilege.isUserPrivValid(request, priv)) {
			String flowTypeCode = ParamUtil.get(request, "typeCode");
			LeafPriv lp = new LeafPriv(flowTypeCode);
			if (!(lp.canUserSee(privilege.getUser(request)))) {
				throw new ErrMsgException(SkinUtil.LoadString(request,
						"pvg_invalid"));
			}
		}
		boolean re = true;
		String errmsg = "";

		int id = ParamUtil.getInt(request, "id");
		String flowString = ParamUtil.get(request, "flowString");
		String title = ParamUtil.get(request, "title");
		boolean returnBack = ParamUtil.getBoolean(request, "returnBack", false);
		String flowJson = ParamUtil.get(request, "flowJson");

		if ("".equals(flowString) && "".equals(flowJson)) {
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_flowstring_is_blank"); // "流程定义字符串不能为空！\n";
		}

		if (!"".equals(flowString) && flowString.indexOf("version:")==-1) {
			// version:1, 3, 0, 0;
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_download_new_client");
		}

		if (title.equals("")) {
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_title_is_blank"); // "名称不能为空！\n";
		}
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}

		String dirCode = ParamUtil.get(request, "dirCode");
		if ("not".equals(dirCode)) {
			throw new ErrMsgException("请选择目录！");
		}
		int examine = ParamUtil.getInt(request, "examine", 1);
		boolean isReactive = ParamUtil.getInt(request, "isReactive", 0) == 1;
		boolean isRecall = ParamUtil.getInt(request, "isRecall", 0) == 1;
		boolean distribute = ParamUtil.getInt(request, "isDistribute", 0)==1;

		int returnMode = ParamUtil.getInt(request, "returnMode", WorkflowPredefineDb.RETURN_MODE_NORMAL);
		int returnStyle = ParamUtil.getInt(request, "returnStyle", WorkflowPredefineDb.RETURN_STYLE_NORMAL);
		int roleRankMode = ParamUtil.getInt(request, "roleRankMode", WorkflowPredefineDb.ROLE_RANK_MODE_NONE);

		String props = ParamUtil.get(request, "props");
		String views = ParamUtil.get(request, "views");
		String linkProp = ParamUtil.get(request, "toghterCondition");
		
		String msgProp = ParamUtil.get(request, "msgProp");
		
		boolean isPlus = ParamUtil.getInt(request, "isPlus", 0) == 1;
		boolean isTransfer = ParamUtil.getInt(request, "isTransfer", 0) == 1;
		boolean isReply = ParamUtil.getInt(request, "isReply", 0) == 1;
		int downloadCount = ParamUtil.getInt(request, "downloadCount", -1);

		boolean canDelOnReturn = ParamUtil.getInt(request, "canDelOnReturn", 0)==1;
		boolean isModuleFilter = ParamUtil.getInt(request, "isModuleFilter", 0) == 1;

		WorkflowPredefineDb wpd = getWorkflowPredefineDb(request, id);
		/*
		 * LeafPriv lp = new LeafPriv(wld.getTypeCode()); if
		 * (!lp.canUserSee(privilege.getUser(request))) { throw new
		 * ErrMsgException("权限非法！"); }
		 */

		if(!linkProp.equals("")){
			SAXBuilder parser = new SAXBuilder();
	        org.jdom.Document doc = parser.build(new InputSource(new StringReader(linkProp)));
	        Element root = doc.getRootElement();
	        List<Element> v = root.getChildren();
	        StringBuffer sb = new StringBuffer();
	        if (v.size()>0) {
	        	int i = 0;
	        	String lastLogical = "";
	        	 for (Element e : v) {
	        		 String name = e.getChildText("name");
					 String fieldName = e.getChildText("fieldName");
					 String op = e.getChildText("operator");
					 String logical = e.getChildText("logical");
					 String value = e.getChildText("value");
					 String firstBracket = e.getChildText("firstBracket");
					 String twoBracket = e.getChildText("twoBracket");
					 String fieldType = e.getChildText("fieldType");
					 
					 if(null == firstBracket || firstBracket.equals("")){
						firstBracket = "";
					}
					if(null == twoBracket || twoBracket.equals("")){
						twoBracket = "";
					}
					if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_FIELD)) {
						if (Integer.valueOf(fieldType)==FormField.FIELD_TYPE_TEXT || Integer.valueOf(fieldType)==FormField.FIELD_TYPE_VARCHAR) {
							if (op.equals("=")) {
								sb.append(firstBracket);
								sb.append("{$" + fieldName + "}");
								sb.append(".equals(\"" + value + "\")");
								sb.append(twoBracket);
							}
							else {
								sb.append(firstBracket);
								sb.append("!{$" + fieldName + "}");
								sb.append(".equals(\"" + value + "\")");	
								sb.append(twoBracket);
							}
						}
						else {
							sb.append(firstBracket);
							sb.append("{$" + fieldName + "}");	
							if(op.equals("=")){
								op = "==";
							}
							if(op.equals("<>")){
								op = "!=";
							}
							
							sb.append(op);																							
							sb.append(value);
							sb.append(twoBracket);
						}
				}else if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT) || name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT)) {
					sb.append(firstBracket);
					sb.append("部门 ");

					if(op.equals("=")){
						op = "==";
					}else{
						op = "!=";
					}

					sb.append(op);											
					sb.append("\"" + value + "\"");
					sb.append(twoBracket);
				}else if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE)) {
						sb.append(firstBracket);
						sb.append("角色 ");
	
						if(op.equals("=")){
							op = "==";
						}else{
							op = "!=";
						}
	
						sb.append(op);											
						sb.append("\"" + value + "\"");	
						sb.append(twoBracket);
					}
					
					if(logical.equals("or")){
						logical = "||";
					}else{
						logical = "&&";
					}
				
					sb.append(" " + logical + " ");
					lastLogical = logical;
	        	}
	        	 i++;
	        }
	        
	        String tempCond = sb.toString();
	        //校验括弧对称性
			boolean flag = checkComCond(tempCond);
			if(!flag){
				throw new ErrMsgException("组合条件里括号设置错误！");
			}
		}
		
		String wld_linkProp = wpd.getLinkProp();
		if(!linkProp.equals("")){
			if(!wld_linkProp.equals("")){
				SAXBuilder parser = new SAXBuilder();
		        org.jdom.Document doc = parser.build(new InputSource(new StringReader(wld_linkProp)));
		        org.jdom.Document doc1 = parser.build(new InputSource(new StringReader(linkProp)));
		        Element root = doc.getRootElement();
		        Element root1 = doc1.getRootElement();
		        List<Element> v = root.getChildren();
		        List<Element> v1 = root1.getChildren();
		        List<Element> deleList = new ArrayList<Element>();
		        List<Element> addList = new ArrayList<Element>();
		        if (v.size()>0) {
		            for (Element e1 : v1) {
		            	String from1 = e1.getChildText("from");
		            	String to1 = e1.getChildText("to");
		            	for(Element e: v){
		            		String from = e.getChildText("from");
		                	String to = e.getChildText("to");
		                	if(from1.equals(from) && to1.equals(to)){
		                		deleList.add(e);
		                	}
		            	}
		            	Element e = new Element("link");
			            e.setAttribute("id", e1.getAttributeValue("id"));
			            e.addContent(new Element("from").setText(e1.getChildText("from")));    
			            e.addContent(new Element("to").setText(e1.getChildText("to")));    
			            e.addContent(new Element("firstBracket").setText(e1.getChildText("firstBracket")));    
			            e.addContent(new Element("fieldName").setText(e1.getChildText("fieldName")));    
			            e.addContent(new Element("name").setText(e1.getChildText("name")));    
			            e.addContent(new Element("operator").setText(e1.getChildText("operator")));
			            e.addContent(new Element("value").setText(e1.getChildText("value")));
			            e.addContent(new Element("twoBracket").setText(e1.getChildText("twoBracket")));
			            e.addContent(new Element("logical").setText(e1.getChildText("logical")));			            
			            e.addContent(new Element("fieldType").setText(e1.getChildText("fieldType")));
			            addList.add(e);
		            }
		            
		            for(Element e2 : deleList){
		            	root.removeContent(e2);
		            }
		            for(Element e2 : addList){
		            	root.addContent(e2);
		            }
		            Format format = Format.getPrettyFormat();
		            ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
		            XMLOutputter xmlOut = new XMLOutputter(format);
		            xmlOut.output(doc, byteRsp);
		            wld_linkProp = byteRsp.toString("utf-8");
		    		byteRsp.close();
		        }
			}
		}
		
		wpd.setFlowString(flowString);
		wpd.setTitle(title);
		wpd.setReturnBack(returnBack);
		wpd.setDirCode(dirCode);
		wpd.setExamine(examine);
		wpd.setReactive(isReactive);
		wpd.setRecall(isRecall);
		wpd.setReturnMode(returnMode);
		wpd.setReturnStyle(returnStyle);
		wpd.setRoleRankMode(roleRankMode);
		wpd.setProps(props);
		// 不能在此处保存，因为数据来自于flow_designer_myflow.jsp，如显示规则中存在不等于<>，会被转义，致规则不能被解析
		// wpd.setViews(views);
		wpd.setDistribute(distribute);
		if("".equals(wld_linkProp)){
			wpd.setLinkProp(linkProp);
		}else{
			wpd.setLinkProp(wld_linkProp);
		}
		wpd.setMsgProp(msgProp);
		
		wpd.setPlus(isPlus);
		wpd.setTransfer(isTransfer);
		wpd.setReply(isReply);
		wpd.setDownloadCount(downloadCount);
		wpd.setCanDelOnReturn(canDelOnReturn);
		wpd.setFlowJson(flowJson);
		wpd.setModuleFilter(isModuleFilter);
		re = wpd.save();
		return re;
	}
	
	 /**
     * 校验括弧对称 false 不对称，true 对称
     * @param con
     * @return
     */
    private boolean checkComCond(String con){
    	boolean flag = false;
    	
    	//不以")"开头
    	if (con != null && !con.startsWith(")") && !con.endsWith("(")){
    		//校验对称
    		flag = StrUtil.checkBracketSymmetry(con);
    		if (flag){
    			//判断括弧位置
    			if(con.contains("|| )") || con.contains("( ||") || con.contains("&& )") || con.contains("( &&")){
    				flag = false;
    			}
    		}
    	}
    	
    	
    	return flag;
    }

	/**
	 * 格式:[roles]member:,,,,,,;11748232960781480311:,,,,,,;[/roles][starter]
	 * members:writeFields[/starter]
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean modifyFree(HttpServletRequest request)
			throws ErrMsgException {
		Privilege privilege = new Privilege();
		String priv = "admin.flow";
		if (!privilege.isUserPrivValid(request, priv)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
		}

		boolean re = true;
		String errmsg = "";

		int id = ParamUtil.getInt(request, "id");

		String flowString = "[roles]";

		String[] roleCodes = ParamUtil.getParameters(request, "roleCodes");
		int len = 0;
		if (roleCodes != null) {
			len = roleCodes.length;
		}
		for (int i = 0; i < len; i++) {
			flowString += roleCodes[i] + ":";
			String start = ParamUtil.get(request, roleCodes[i] + "_start");
			String stop = ParamUtil.get(request, roleCodes[i] + "_stop");
			String archive = ParamUtil.get(request, roleCodes[i] + "_archive");
			String discard = ParamUtil.get(request, roleCodes[i] + "_discard");
			String del = ParamUtil.get(request, roleCodes[i] + "_del");
			String editAttach = ParamUtil.get(request, roleCodes[i] + "_editAttach");
			String delAttach = ParamUtil.get(request, roleCodes[i] + "_delAttach");
			flowString += start + "," + stop + "," + archive + "," + discard + "," + del + "," + delAttach;
			// 将可写表单域的分隔符用|替换,
			String fieldWrite = ParamUtil.get(request, roleCodes[i] + "_fieldWrite");
			fieldWrite = fieldWrite.replaceAll(",", "|");
			flowString += "," + fieldWrite;
			
			flowString += "," + editAttach;

			flowString += ";";
		}
		flowString += "[/roles]";

		String starterRoleCodes = ParamUtil.get(request, "starterRoleCodes");
		String starter_fieldWrite = ParamUtil.get(request, "starter_fieldWrite");

		flowString += "[starter]" + starterRoleCodes + ":" + starter_fieldWrite + "[/starter]";

		String title = ParamUtil.get(request, "title");
		boolean returnBack = ParamUtil.getBoolean(request, "returnBack", false);

		if (flowString.equals("")) {
			errmsg += "流程定义字符串不能为空！\\n";
		}
		if (title.equals("")) {
			errmsg += "名称不能为空！\\n";
		}
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}

		String dirCode = ParamUtil.get(request, "dirCode");
		if (dirCode.equals("not")) {
			throw new ErrMsgException("请选择目录！");
		}
		int examine = ParamUtil.getInt(request, "examine", 1);

		WorkflowPredefineDb wld = getWorkflowPredefineDb(request, id);

		LeafPriv lp = new LeafPriv(wld.getTypeCode());
		if (!lp.canUserSee(privilege.getUser(request))) {
			throw new ErrMsgException("权限非法！");
		}

		boolean isReactive = ParamUtil.getInt(request, "isReactive", 0) == 1;
		boolean isRecall = ParamUtil.getInt(request, "isRecall", 0) == 1;
		boolean light = ParamUtil.getInt(request, "isLight", 0)==1;
		boolean isReply = ParamUtil.getInt(request, "isReply", 0)==1;

		wld.setFlowString(flowString);
		wld.setTitle(title);
		wld.setReturnBack(returnBack);
		wld.setDirCode(dirCode);
		wld.setExamine(examine);
		wld.setReactive(isReactive);
		wld.setRecall(isRecall);
		wld.setLight(light);
		wld.setReply(isReply);
		re = wld.save();
		return re;
	}

	public WorkflowPredefineDb getWorkflowPredefineDb(
			HttpServletRequest request, int id) throws ErrMsgException {
		WorkflowPredefineDb wld = new WorkflowPredefineDb();
		return wld.getWorkflowPredefineDb(id);
	}

	public boolean create(HttpServletRequest request) throws ErrMsgException {
		Privilege privilege = new Privilege();
		boolean re = true;

		String errmsg = "";
		String flowString = ParamUtil.get(request, "flowString");
		String typeCode = ParamUtil.get(request, "typeCode");
		String title = ParamUtil.get(request, "title");
		boolean returnBack = ParamUtil.getBoolean(request, "returnBack", false);
		String dirCode = ParamUtil.get(request, "dirCode");
		if (dirCode.equals("not")) {
			throw new ErrMsgException("请选择目录！");
		}

		int examine = ParamUtil.getInt(request, "examine", 1);
		String flowJson = ParamUtil.get(request, "flowJson");

		if ("".equals(flowString) && "".equals(flowJson)) {
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_flowstring_is_blank"); // "流程定义字符串不能为空！\n";
		}

		if (!"".equals(flowString) && flowString.indexOf("version:")==-1) {
			// version:1, 3, 0, 0;
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_download_new_client");
		}

		if (title.equals("")) {
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_title_is_blank"); // "名称不能为空！\n";
		}

		if (typeCode.equals("")) {
			errmsg += SkinUtil.LoadString(request, "res.module.flow", "msg_typecode_is_blank"); // "流程类别不能为空！\n";
		}

		boolean isReactive = ParamUtil.getInt(request, "isReactive", 0) == 1;
		boolean isRecall = ParamUtil.getInt(request, "isRecall", 0) == 1;
		int returnMode = ParamUtil.getInt(request, "returnMode",
				WorkflowPredefineDb.RETURN_MODE_NORMAL);

		LeafPriv lp = new LeafPriv(typeCode);
		if (!lp.canUserSee(privilege.getUser(request))) {
			throw new ErrMsgException("权限非法！");
		}

		int returnStyle = ParamUtil.getInt(request, "returnStyle",
				WorkflowPredefineDb.RETURN_STYLE_NORMAL);
		int roleRankMode = ParamUtil.getInt(request, "roleRankMode",
				WorkflowPredefineDb.ROLE_RANK_MODE_NONE);

		String props = ParamUtil.get(request, "props");
		String views = ParamUtil.get(request, "views");
		
		boolean light = ParamUtil.getInt(request, "isLight", 0)==1;		
		
		String msgProp = ParamUtil.get(request, "msgProp");
		boolean isPlus = ParamUtil.getInt(request, "isPlus", 0) == 1;
		boolean isTransfer = ParamUtil.getInt(request, "isTransfer", 0) == 1;
		boolean isReply = ParamUtil.getInt(request, "isReply", 0) == 1;
		int downloadCount = ParamUtil.getInt(request, "downloadCount", -1);

		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}

		WorkflowPredefineDb wld = new WorkflowPredefineDb();
		wld.setFlowString(flowString);
		wld.setTypeCode(typeCode);
		wld.setTitle(title);
		wld.setReturnBack(returnBack);
		wld.setDirCode(dirCode);
		wld.setExamine(examine);
		wld.setReactive(isReactive);
		wld.setRecall(isRecall);
		wld.setReturnMode(returnMode);
		wld.setReturnStyle(returnStyle);
		wld.setRoleRankMode(roleRankMode);
		wld.setProps(props);
		wld.setViews(views);
		wld.setLight(light);
		wld.setMsgProp(msgProp);
		wld.setPlus(isPlus);
		wld.setTransfer(isTransfer);
		wld.setReply(isReply);
		wld.setDownloadCount(downloadCount);
		wld.setFlowJson(flowJson);
		re = wld.create();

		if (re) {
			newId = wld.getId();
		}
		return re;
	}

	public boolean del(HttpServletRequest request) throws ErrMsgException {
		int id = ParamUtil.getInt(request, "id");
		WorkflowPredefineDb wld = getWorkflowPredefineDb(request, id);
		if (wld == null || !wld.isLoaded())
			throw new ErrMsgException("该项已不存在！");

		Privilege privilege = new Privilege();
		LeafPriv lp = new LeafPriv(wld.getTypeCode());
		if (!lp.canUserSee(privilege.getUser(request))) {
			throw new ErrMsgException("权限非法！");
		}

		return wld.del();
	}

	public void setNewId(int newId) {
		this.newId = newId;
	}

	public int getNewId() {
		return newId;
	}
	
	/**
	 * 取得对应于节点的检查脚本
	 * @param scripts 脚本字符串
	 * @param internalName 节点内部名称
	 * @return
	 */
	public String getValidateScript(String scripts, String internalName) {
		if (scripts == null) {
			return null;
		}
		String beginStr = "//[validate_begin_" + internalName + "]\r\n";
		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[validate_end_" + internalName + "]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}

	/**
	 * 取得节点预处理脚本
	 * @param scripts
	 * @param internalName
	 * @return
	 */
	public String getActionPreDisposeScript(String scripts, String internalName) {
		String beginStr = "//[pre_dispose_start_" + internalName + "]\r\n";

		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[pre_dispose_end_" + internalName + "]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}

	/**
	 * 取得节点激活脚本
	 * @param scripts
	 * @param internalName
	 * @return
	 */
	public String getActionActiveScript(String scripts, String internalName) {
		String beginStr = "//[action_active_start_" + internalName + "]\r\n";

		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[action_active_end_" + internalName + "]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}

	/**
	 * 取得流程预处理脚本
	 * @param scripts
	 * @return
	 */
	public String getPreInitScript(String scripts) {
		String beginStr = "//[pre_init_start]\r\n";

		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[pre_init_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}

	/**
	 * 取得节点流转事件脚本
	 * @param scripts
	 * @param internalName
	 * @return
	 */
	public String getActionFinishScript(String scripts, String internalName) {
		String beginStr = "//[action_finish_start_" + internalName + "]\r\n";
		
		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[action_finish_end_" + internalName + "]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		
		return null;
	}	
	
	/**
	 * 取得节点被返回事件脚本
	 * @param scripts
	 * @param internalName
	 * @return
	 */
	public String getActionReturnScript(String scripts, String internalName) {
		String beginStr = "//[action_return_start_" + internalName + "]\r\n";
		
		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[action_return_end_" + internalName + "]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		
		return null;
	}	
	
	/**
	 * 取得流程撤销脚本
	 * @param scripts
	 * @return
	 */
	public String getDiscardScript(String scripts) {
		String beginStr = "//[ondiscard_begin]\r\n";
		
		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[ondiscard_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		
		return null;
	}

	/**
	 * 取得撤回脚本
	 * @param scripts
	 * @return
	 */
	public String getRecallScript(String scripts) {
		String beginStr = "//[onrecall_begin]\r\n";

		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[onrecall_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}

		return null;
	}

	/**
	 * 取得流程删除事件脚本
	 * @param scripts
	 * @return
	 */
	public String getDeleteValidateScript(String scripts) {
		String beginStr = "//[ondeletevalidate_begin]\r\n";

		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[ondeletevalidate_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}

		return null;
	}

	/**
	 * 保存节点检查事件脚本
	 * @param wpd
	 * @param internalName
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveValidateScript(WorkflowPredefineDb wpd, String internalName, String script) throws ErrMsgException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}
		
		// 检查
		if (!script.equals("") && script.indexOf("ret")==-1) {
			throw new ErrMsgException("请添加返回值，ret=true或者false");
		}
		/*
		if (script.indexOf("errMsg=")==-1) {
			throw new ErrMSgException("请添加返回值，ret=...");			
		}
		*/
		
		String scripts = wpd.getScripts();		
		String beginStr = "//[validate_begin_" + internalName + "]\r\n";
		String endStr = "//[validate_end_" + internalName + "]\r\n";
		
		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);
			
			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();

	}

	/**
	 * 保存节点预处理脚本
	 * @param wpd
	 * @param internalName
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveActionPreDisposeScript(WorkflowPredefineDb wpd, String internalName, String script) throws ErrMsgException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}

		String scripts = wpd.getScripts();
		String beginStr = "//[pre_dispose_start_" + internalName + "]\r\n";
		String endStr = "//[pre_dispose_end_" + internalName + "]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();
	}

	/**
	 * 保存节点激活脚本
	 * @param wpd
	 * @param internalName
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveActionActiveScript(WorkflowPredefineDb wpd, String internalName, String script) throws ErrMsgException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}

		String scripts = wpd.getScripts();
		String beginStr = "//[action_active_start_" + internalName + "]\r\n";
		String endStr = "//[action_active_end_" + internalName + "]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();
	}

	/**
	 * 保存流程预处理脚本
	 * @param wpd
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean savePreInitScript(WorkflowPredefineDb wpd, String script) throws ErrMsgException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}

		String scripts = wpd.getScripts();
		String beginStr = "//[pre_init_start]\r\n";
		String endStr = "//[pre_init_end]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();
	}

	public boolean saveActionFinishScript(WorkflowPredefineDb wpd, String internalName, String script) throws ErrMsgException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}
		
		String scripts = wpd.getScripts();		
		String beginStr = "//[action_finish_start_" + internalName + "]\r\n";
		String endStr = "//[action_finish_end_" + internalName + "]\r\n";
		
		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);
			
			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();

	}	
	
	/**
	 * 保存节点返回脚本
	 * @param wpd
	 * @param internalName
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveActionReturnScript(WorkflowPredefineDb wpd, String internalName, String script) throws ErrMsgException {
		// 检查
		// if (script.indexOf("ret=")==-1) {
		// 	throw new ErrMsgException("请添加返回值，ret=true或者false");
		// }
		
		String scripts = wpd.getScripts();		
		String beginStr = "//[action_return_start_" + internalName + "]\r\n";
		String endStr = "//[action_return_end_" + internalName + "]\r\n";
		
		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);
			
			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();

	}		
	
	/**
	 * 保存撤销事件脚本
	 * @param wpd
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveDiscardScript(WorkflowPredefineDb wpd, String script) throws ErrMsgException {
		// 检查
		// if (script.indexOf("ret=")==-1) {
		// 	throw new ErrMsgException("请添加返回值，ret=true或者false");
		// }
		
		String scripts = wpd.getScripts();		
		String beginStr = "//[ondiscard_begin]\r\n";
		
		String endStr = "//[ondiscard_end]\r\n";
		
		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);
			
			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();
	}

	/**
	 * 保存撤回事件脚本
	 * @param wpd
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveRecallScript(WorkflowPredefineDb wpd, String script) throws ErrMsgException {
		// 检查
		// if (script.indexOf("ret=")==-1) {
		// 	throw new ErrMsgException("请添加返回值，ret=true或者false");
		// }

		String scripts = wpd.getScripts();
		String beginStr = "//[onrecall_begin]\r\n";

		String endStr = "//[onrecall_end]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();
	}

	/**
	 * 保存删除事件脚本
	 * @param wpd
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveDeleteValidateScript(WorkflowPredefineDb wpd, String script) throws ErrMsgException {
		// 检查
		// if (script.indexOf("ret=")==-1) {
		// 	throw new ErrMsgException("请添加返回值，ret=true或者false");
		// }

		String scripts = wpd.getScripts();
		String beginStr = "//[ondeletevalidate_begin]\r\n";

		String endStr = "//[ondeletevalidate_end]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();

	}

	/**
	 * 取得流程结束事件脚本
	 * @param scripts
	 * @return
	 */
	public String getOnFinishScript(String scripts) {
		String beginStr = "//[onfinish_begin]\r\n";
		
		int b = scripts.indexOf(beginStr);
		if (b!=-1) {
			String endStr = "//[onfinish_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e!=-1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		
		return null;
	}
	
	/**
	 * 保存流程结束事件脚本
	 * @param wpd
	 * @param script
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveOnFinishScript(WorkflowPredefineDb wpd, String script) throws ErrMsgException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}
		
		String scripts = wpd.getScripts();		
		String beginStr = "//[onfinish_begin]\r\n";
		String endStr = "//[onfinish_end]\r\n";
		
		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b==-1 || e==-1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		}
		else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += scripts.substring(e);
			
			scripts = str;
		}
		wpd.setScripts(scripts);
		return wpd.save();

	}	
	
	public boolean saveWriteProp(HttpServletRequest request) throws ErrMsgException {
		String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
		String internalName = ParamUtil.get(request, "internalName");
		WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
		String writeProp = wpd.getWriteProp();                   //从数据库获取回写

		Leaf lf = new Leaf();
		lf = lf.getLeaf(flowTypeCode);
		if (lf == null) {
			throw new ErrMsgException("流程类型不存在");
		}
		FormDb fd = new FormDb();
		fd = fd.getFormDb(lf.getFormCode());

		SAXBuilder parser = new SAXBuilder();
		org.jdom.Document doc = null;
		Element root = null;
		Element child = null;
		List list = null;
		Iterator ir = null;

		String writeBackFormCode  = ParamUtil.get(request,"relateCode");           //回写表单
		String writeBackTime = ParamUtil.get(request,"timestamp");                //回写时间
		int  repeatCount = ParamUtil.getInt(request,"repeatCount", 0);              //回写字段数量     <![CDATA[文本内容]]>
		String conditionStr = ParamUtil.get(request,"condition");
		
		int writeBackType = ParamUtil.getInt(request, "writeBackType", WorkflowPredefineDb.WRITE_BACK_UPDATE);
		String primaryKey = ParamUtil.get(request, "primaryKey");
		
		Element nodeToDel = null;
		// 条件
		Element cond = null;
		
		try {
			if (writeProp != null && !"".equals(writeProp)) {
				doc = parser.build(new InputSource(new StringReader(writeProp)));
				root = doc.getRootElement();
				// 流程结束
				if ("flowFinish".equals(writeBackTime)) {
					Element flowFinish = root.getChild("flowFinish");
					if (flowFinish != null) {
						String writeBackOld = flowFinish.getChildText("writeBackForm");
						if (writeBackOld.equals(writeBackFormCode)) {
							cond = flowFinish.getChild("condition");
						}
						root.removeContent(flowFinish);
					}
					child = new Element("flowFinish");
				} else {
					// 核定节点通过
					list = root.getChildren("nodeFinish");
					if (list != null) {
						ir = list.iterator();
						while (ir.hasNext()) {
							Element nodeFinish = (Element) ir.next();
							Element internalNode = nodeFinish.getChild("internalName");
							String internalNodeText = internalNode.getText();
							if (internalName.equals(internalNodeText)) { // 节点存在先删除
								String writeBackOld = nodeFinish.getChildText("writeBackForm");
								if (writeBackOld.equals(writeBackFormCode)) {
									cond = nodeFinish.getChild("condition");
								}
								nodeToDel = nodeFinish;
							}
						}
					}
					child = new Element("nodeFinish");
					Element internalNode = new Element("internalName");
					internalNode.setText(internalName);
					child.addContent(internalNode);
				}
				if (nodeToDel != null) {
					root.removeContent(nodeToDel);
				}
			} else {  // 如果writeProp为空，新建root元素
				doc = new org.jdom.Document();
				root = new Element("root");
				doc.addContent(root);
				if ("flowFinish".equals(writeBackTime)) {
					child = new Element("flowFinish");
				} else {
					child = new Element("nodeFinish");
					Element internalNode = new Element("internalName");
					internalNode.setText(internalName);
					child.addContent(internalNode);
				}
			}

			child.setAttribute("id", RandomSecquenceCreator.getId(10));
			root.addContent(child);
			Element writeBackForm = new Element("writeBackForm");
			writeBackForm.setText(writeBackFormCode);
			child.addContent(writeBackForm);
			Element writeBackTypeEl = new Element("writeBackType");
			writeBackTypeEl.setText(String.valueOf(writeBackType));
			child.addContent(writeBackTypeEl);

			Element primaryKeyEl = new Element("primaryKey");
			primaryKeyEl.setText(primaryKey);
			child.addContent(primaryKeyEl);

			Element writeBackTimeStamp = new Element("writeBackTime");
			writeBackTimeStamp.setText(writeBackTime);
			child.addContent(writeBackTimeStamp);

			FormDb fdWriteBack = new FormDb();
			fdWriteBack = fd.getFormDb(writeBackFormCode);

			// 设置字段值，可能会有多个字段
			for (int i = 0; i < repeatCount; i++) {
				String field = ParamUtil.get(request, "formfield" + i);
				String math = ParamUtil.get(request, "math" + i);
				// 判断是否为字符串型，如果是则加单引号
				FormField ff = fdWriteBack.getFormField(field);
				if (ff == null) {
					continue;
				}
				int fieldType = ff.getFieldType();
				// int、long、float、double、price类型
				if (fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_PRICE) {
					if ("".equals(math)) {
						math = "0";
					}
				} else {
					if (!math.startsWith("'")) {
						math = StrUtil.sqlstr(math);
					}
				}

				Element writeBackField = new Element("writeBackField");
				writeBackField.setAttribute("fieldName", field);
				child.addContent(writeBackField);
				Element writeBackMath = new Element("writeBackMath");
				writeBackMath.setText(math);
				writeBackField.addContent(writeBackMath);
			}
			if (!"".equals(conditionStr)) {
				org.jdom.Document cdoc = parser.build(new InputSource(new StringReader(conditionStr)));
				Element croot = cdoc.getRootElement();
				Element condition = croot.getChild("condition");
				child.addContent(condition.detach());
			} else {
				if (cond != null) {
					child.addContent(cond.detach());
				}
			}

			// 处理插入
			Element fieldsToInsert = new Element("insertFields");
			child.addContent(fieldsToInsert);
			JSONObject json = new JSONObject();
			if (writeBackType==WorkflowPredefineDb.WRITE_BACK_INSERT || writeBackType==WorkflowPredefineDb.WRITE_BACK_UPDATE_INSERT) {
				// primaryKey表示插入时如果主键字段未设置，或者主键字段不存在则插入数据，故不能以此条件判断是否需保存插入插入字段的值
				// if (!"empty".equals(primaryKey)) {
				ir = fdWriteBack.getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField) ir.next();
					String math = ParamUtil.get(request, ff.getName() + "_formula");
					int fieldType = ff.getFieldType();
					if (fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_PRICE) {
						if ("".equals(math)) {
							math = "0";
						}
					} else if (fieldType == FormField.FIELD_TYPE_DATE || fieldType == FormField.FIELD_TYPE_DATETIME) {
						// 改为在插入的时候，插入null，否则在配置界面上显示为null比较难看
					/*
					if ("".equals(math)) {
						math = "null";
					}
					*/
					} else {
						if (!math.startsWith("'")) {
							math = StrUtil.sqlstr(math);
						}
					}
					try {
						json.put(ff.getName(), math);
					} catch (JSONException e) {
						LogUtil.getLog(getClass()).error(e);
					}
				}
			}
			fieldsToInsert.setText(json.toString());

			Format format = Format.getPrettyFormat();
			ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
			XMLOutputter xmlOut = new XMLOutputter(format);
			xmlOut.output(doc, byteRsp);
			writeProp = byteRsp.toString("utf-8");
			byteRsp.close();

			wpd.setWriteProp(writeProp);
			return wpd.save();
		} catch (IOException | JDOMException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return false;
	}

	public org.json.JSONObject getFlowJson(String flowTypeCode) {
		WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
		String flowJsonStr = wpd.getFlowJson();
		if (StrUtil.isEmpty(flowJsonStr)) {
			return new org.json.JSONObject();
		} else {
			try {
				// 直接转因为其中的\字符，导致不认，故先将\转为#@#
				flowJsonStr = flowJsonStr.replaceAll("\\\\", SUBSTITUTION);
				return new JSONObject(flowJsonStr);
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
		return new org.json.JSONObject();
	}
}
