package com.redmoon.oa.flow.macroctl;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import jxl.Cell;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.read.biff.WorkbookParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.NetUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.Directory;
import com.redmoon.oa.flow.FormDAOMgr;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;

/**
 * <p>Title: 明细表，对应于nest_detaillist_view.jsp</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DetailListCtl extends AbstractMacroCtl {
    public DetailListCtl() {
        super();
    }
    
    /**
     * 通过NetUtil.gather方法取得控件的HTML代码
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    public String getDetailList(HttpServletRequest request, FormField ff) {
		// 使之隐藏
		if (ff.isHidden()) {
			return "";
		}		
		
		String op = "add";
		String cwsId = (String) request.getAttribute("cwsId");
        long workflowActionId = StrUtil.toLong((String)request.getAttribute("workflowActionId"), -1);
		
		// 数据库中为空
		if (cwsId != null) {
			String pageType = StrUtil.getNullStr((String) request
					.getAttribute("pageType"));
			if ("show".equals(pageType)) { // module_show.jsp
				op = "view&cwsId=" + cwsId;
			}
            else if ("archive".equals(pageType)) {
                op = "view&action=archive&cwsId=" + cwsId;
            }			
			else if ("flowShow".equals(pageType)) { // flow_modify.jsp
                int flowId = StrUtil.toInt(cwsId);
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(wf.getTypeCode());
                
                FormDb flowFd = new FormDb();
                flowFd = flowFd.getFormDb(lf.getFormCode());
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO(flowId, flowFd);
                long fdaoId = fdao.getId();
                op = "view&cwsId=" + fdaoId;
            }			
			else if ("add".equals(pageType)) {
				op = "add";
			}
			else if ("edit".equals(pageType)) {
                op = "edit&cwsId=" + cwsId;
            } else if ("flow".equals(pageType)) {
                int flowId = StrUtil.toInt(cwsId);
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(wf.getTypeCode());
                
                FormDb flowFd = new FormDb();
                flowFd = flowFd.getFormDb(lf.getFormCode());
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO(flowId, flowFd);
                long fdaoId = fdao.getId();

				if (ff.isEditable()) {
					// flowId用于带入nest_sheet_add_relate.jsp中作为hidden传值，以写入本嵌套表格2的flow_id字段中
					op = "edit&cwsId=" + fdaoId + "&flowId=" + flowId;
				}
				else {
                    op = "view&cwsId=" + fdaoId + "&flowId=" + flowId;
                }
			}
		}
		// 获取父页面中处理的formCode
		String parentFormCode = StrUtil.getNullStr((String) request.getAttribute("formCode"));
		// 为了向下兼容
		String nestFormCode = ff.getDescription();
		String nestFieldName = ff.getName();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(ff.getDescription());
			JSONObject json = new JSONObject(defaultVal);
			nestFormCode = json.getString("destForm");
		} catch (JSONException e) {
            LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
            LogUtil.getLog(getClass()).error(e);
		}
	
		String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));
		if ("archive".equals(pageType)) {
			String url = "/visual/nest_sheet_view.jsp?formCode="
				+ nestFormCode + "&op=" + op + "&parentFormCode="
				+ parentFormCode + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName);
			String path = Global.getFullRootPath(request) + url;
			path += "&pageType=" + pageType;
			
			String str = NetUtil.gather(request, "utf-8", path);
			str ="<div id='nestsheet_"+ff.getName()+"'>"+str+"</div>";
			return str;
		}
		
		// 借用嵌套表格2生成报表模式
		if ("flowShow".equals(pageType) || "show".equals(pageType)) { // flow_modify.jsp
			String url = "/visual/nest_sheet_view.jsp?formCode="
				+ nestFormCode + "&op=" + op + "&parentFormCode="
				+ parentFormCode + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName);
			String path = Global.getFullRootPath(request) + url;
			
			String ajaxPath = request.getContextPath() + url;
			
			path += "&pageType=" + pageType;

			String str ="<div id='nestsheet_"+ff.getName()+"'></div>";
			
	    	str += "\n<script>loadNestCtl('" + ajaxPath + "','nestsheet_" + ff.getName() + "')</script>\n";

			if (request.getAttribute("isNestSheetCtlJS") == null) {
				str = "\n<script src='" + request.getContextPath()
				+ "/flow/macro/macro_js_nestsheet.jsp?fieldName=" + ff.getName() + "&nestFormCode=" + nestFormCode + "&flowId=" + cwsId
				+ "'></script>\n" + str;			

				request.setAttribute("isNestSheetCtlJS", "y");
			}
			
			// 因为每个表单的重新载入调用的方法是不一样的，所以在这里需要分别为每个嵌套表生成方法
			str = "\n<script src='" + request.getContextPath()
				+ "/flow/macro/macro_js_nestsheet.jsp?op=forRefresh&fieldName=" + ff.getName() + "&nestFormCode=" + nestFormCode + "&path="+StrUtil.UrlEncode(ajaxPath)+"&flowId=" + cwsId
				+ "'></script>\n" + str;		
			return str;			
		}

		String str ="<table id='detaillist_"+ff.getName()+"'></table>";
		
		String params = "op=" + op + "&formCode="
			+ nestFormCode + "&parentFormCode="
			+ parentFormCode + "&nestFieldName=" + StrUtil.UrlEncode(ff.getName()) + "&workflowActionId=" + workflowActionId + "&pageType=" + pageType;
		
		params += "&isEditable=" + ff.isEditable();

		// 因为每个表单的重新载入调用的方法是不一样的，所以在这里需要分别为每个嵌套表生成方法
		str += "\n<script src='" + request.getContextPath()
			+ "/flow/macro/macro_detaillist_ctl_js.jsp?" + params
			+ "'></script>\n";		
		return str;
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        return getDetailList(request, ff);
    }

    /**
     * 当创建父记录时，同步创建嵌套表单的记录（用于visual模块，流程中用不到，因为流程中事先生成了空的表单）
     * @param macroField FormField
     * @param cwsId String
     * @param creator String
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    @Override
    public int createForNestCtl(HttpServletRequest request, FormField macroField, String cwsId,
                                String creator,
                                FileUpload fu) throws ErrMsgException {
    	String rowOrder = fu.getFieldValue("detaillist_" + macroField.getName() + "_rowOrder");
    	String[] uniqueIndexes = null;
    	if (rowOrder==null) {
    		uniqueIndexes = new String[0];
    	}
    	else {
    		uniqueIndexes = rowOrder.split(",");
    	}
    	
    	if (uniqueIndexes.length==0) {
            return 0;
        }
    	
    	int rows = uniqueIndexes.length;

        String formCode = macroField.getDescription();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
			LogUtil.getLog(getClass()).info("createForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + macroField.getDefaultValueRaw());
		}
		
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(formCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        if (fields==null) {
            LogUtil.getLog(getClass()).error("createForNestCtl:The fields is null, please set " + formCode + " module's list");
            return -1;
        }
        int cols = fields.length;

        // 找出空行
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < uniqueIndexes.length; i++) {
            boolean isBlankRow = true;
            for (int j = 1; j <= cols; j++) {
                String fieldName = fields[j - 1];
                if (fieldName.startsWith("main:")) {
                    continue;
                }
                if (fieldName.startsWith("other:")) {
                    continue;
                }
                String val = fu.getFieldValue("detaillist_" + macroField.getName() + "_" + fieldName + "_" + uniqueIndexes[i]);
                if (val!=null && !"".equals(val)) {
                    isBlankRow = false;
                    break;
                }
            }
            if (isBlankRow) {
                map.put("" + i, "" + i);
            }
        }

        // 有效性验证
        ParamChecker pck = new ParamChecker(request, fu);
        for (int i = 0; i < rows; i++) {
            // 跳过空行
            if (map.get("" + i)!=null) {
                continue;
            }
            for (String field : fields) {
                if (field.startsWith("main:")) {
                    continue;
                }
                if (field.startsWith("other:")) {
                    continue;
                }
                FormField ff = fd.getFormField(field);
                try {
                    // LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
                    FormDAOMgr.checkField(request, fu, pck, ff, "detaillist_" + macroField.getName() + "_" + ff.getName() + "_" + uniqueIndexes[i], null);
                } catch (CheckErrException e) {
                    // 如果onError=exit，则会抛出异常
                    throw new ErrMsgException(e.getMessage());
                }
            }
        }
        if (pck.getMsgs().size()!=0) {
            throw new ErrMsgException(pck.getMessage(false));
        }

        String fds = "";
        StringBuilder str = new StringBuilder();
        for (String s : fields) {
            if ("".equals(fds)) {
                fds = s;
                str = new StringBuilder("?");
            } else {
                fds += "," + s;
                str.append(",?");
            }
        }
        String sql = "insert into " + fd.getTableNameByForm() +
                     " (flowId, cws_creator, cws_id, " + fds +
                     ",flowTypeCode,cws_status,cws_parent_form) values (?,?,?," + str.toString() + ",?,?,?)";
        
        if ("".equals(fds)) {
            sql = "insert into " + fd.getTableNameByForm() +
            " (flowId, cws_creator, cws_id, flowTypeCode,cws_status,cws_parent_form) values (?,?,?,?,?,?)";
        }

        String parentFormCode = macroField.getFormCode();
        int ret = 0;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            for (int i = 0; i < rows; i++) {
                // 跳过空行
                if (map.get("" + i)!=null) {
                    continue;
                }
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, FormDAO.NONEFLOWID);
                ps.setString(2, creator);
                ps.setString(3, cwsId);

                // LogUtil.getLog(getClass()).info("cwsId=" + cwsId + " creator=" + creator);
                // 赋值
                for (int j = 0; j < cols; j++) {
                    FormField field = fd.getFormField(fields[j]);
                    // 取得表单域的值
                    String val = StrUtil.getNullStr(fu.getFieldValue("detaillist_" + macroField.getName() + "_" + field.getName() + "_" + uniqueIndexes[i]));
                    field.setValue(val);
                    // 因为在计算控件中，将根据下列语句获得表单域的字段值
                    // String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // f为计算控件中涉及的表单域的字段
                    // 所以在此需赋值给fileUpload中对应的属性
                    fu.setFieldValue(field.getName(), field.getValue());
                }

                int k = 4;
                for (String s : fields) {
                    FormField field = fd.getFormField(s);
                    field.createDAOVisual(ps, k, fu, fd);
                    k++;
                }

                String curTime = "" + System.currentTimeMillis();
                ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
                ps.setInt(k+1, com.redmoon.oa.flow.FormDAO.STATUS_DONE);
                ps.setString(k+2, parentFormCode);
                if (conn.executePreUpdate() == 1) {
                    ret++;
                }
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            throw new ErrMsgException("数据库错误！");
        } finally {
            conn.close();
        }
        return ret;
    }

    /**
     * 保存嵌套表单中的记录，智能模块与流程中共用本方法
     * @param macroField FormField
     * @param cwsId String 父记录的ID，用于与父记录关联
     * @param creator String 当运用于流程时，creator为空，因为无法记录是具体哪个人修改了嵌套表格
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    @Override
    public int saveForNestCtl(HttpServletRequest request, FormField macroField, String cwsId,
                              String creator,
                              FileUpload fu) throws ErrMsgException {
    	String skey = fu.getFieldValue("skey");
    	if(skey != null && !skey.trim().equals("")){
    		return 0;
    	}
    	String rowOrder = fu.getFieldValue("detaillist_" + macroField.getName() + "_rowOrder");
    	String[] uniqueIndexes = null;
    	if (rowOrder==null) {
    		uniqueIndexes = new String[0];
    	}
    	else {
    		uniqueIndexes = rowOrder.split(",");
    	}
    	
    	// if (uniqueIndexes.length==0)
    	// 	return 0;

        String formCode = macroField.getDescription();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).info("saveForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + macroField.getDefaultValueRaw());
            LogUtil.getLog(getClass()).error(e);
		}
		
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(formCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        int cols = fields.length;

        // 找出空行
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < uniqueIndexes.length; i++) {
            boolean isBlankRow = true;
            for (int j = 1; j <= cols; j++) {
            	String fieldName = fields[j - 1];
            	if (fieldName.startsWith("main:")) {
        			continue;
        		}
            	if (fieldName.startsWith("other:")) {
        			continue;
        		}
	            String val = fu.getFieldValue("detaillist_" + macroField.getName() + "_" + fieldName + "_" + uniqueIndexes[i]);
	            if (val!=null && !val.equals("")) {
	               isBlankRow = false;
	               break;
        		}
            }
            if (isBlankRow) {
                map.put("" + i, "" + i);
            }
        }
        
        long fdaoId = StrUtil.toLong(cwsId, -1);

        // 在flow.FormDAOMgr的update方法中，置了request的属性action
        WorkflowActionDb action = (WorkflowActionDb)request.getAttribute("action");
        // 判断是否来自于流程处理表单
        Vector fieldsWritable = new Vector();
        int flowId = FormDAO.NONEFLOWID;
        if (action!=null) {
            flowId = StrUtil.toInt(cwsId);
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(wf.getTypeCode());

            Vector flds = fd.getFields();
            
            FormDb flowFd = new FormDb();
            flowFd = flowFd.getFormDb(lf.getFormCode());
            com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
            fdao = fdao.getFormDAO(flowId, flowFd);
            fdaoId = fdao.getId();

            if (lf.getType() == Leaf.TYPE_FREE) {
                // 自由流程根据用户所属的角色，得到可写表单域
                Privilege pvg = new Privilege();
                String userName = pvg.getUser(request);
                WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
                wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

                String[] fds = wfpd.getFieldsWriteOfUser(wf, userName);
                int len = fds.length;

                Iterator ir = flds.iterator();
                // 将可写的域筛选出
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    for (int i = 0; i < len; i++) {
                    	String f = fds[i];
                    	if (f.startsWith("main:")) {
                			continue;
                		}
                    	if (f.startsWith("other:")) {
                			continue;
                		}
                    	if (f.startsWith("nest.")) {
                    		f = f.substring(5);
                    	}
                        if (ff.getName().equals(f)) {
                            fieldsWritable.addElement(ff);
                            break;
                        }
                    }
                }
            }
            else {
                // 预设流程根据动作中的设定得到可写表单域
                String fieldWrite = StrUtil.getNullString(action.getFieldWrite()).trim();
                String[] fds = fieldWrite.split(",");
                int len = fds.length;

                // 判断在流程节点上明细表宏控件字段本身是否可写
                boolean isNestMacroCtlEditable = false;
                for (int i = 0; i < len; i++) {
                    if (fds[i].equals(macroField.getName())) {
                        isNestMacroCtlEditable = true;
                        break;
                    }
                }
                // 如果宏控件本身不可写，则退出
                if (!isNestMacroCtlEditable) {
                    return 0;
                }

                Iterator ir = flds.iterator();
                // 将可写的域筛选出
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    for (int i = 0; i < len; i++) {
                    	if (fds[i].startsWith("main:")) {
                			continue;
                		}
                    	if (fds[i].startsWith("other:")) {
                			continue;
                		}
                        if (fds[i].startsWith("nest.")) {
                            String fName = fds[i].substring("nest.".length());
                            if (ff.getName().equals(fName)) {
                                fieldsWritable.addElement(ff);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 有效性验证
        ParamChecker pck = new ParamChecker(request, fu);
        for (int i = 0; i < uniqueIndexes.length; i++) {
            // 跳过空行
            if (map.get("" + i)!=null)
                continue;
            for (int j = 1; j <= cols; j++) {
            	if (fields[j-1].startsWith("main:")) {
        			continue;
        		}
            	if (fields[j-1].startsWith("other:")) {
        			continue;
        		}
                FormField ff = fd.getFormField(fields[j-1]);
                
                if (ff == null) {
                	continue;
                }

                // 判断是否在流程中，且是否为可写字段，如果是，则进行有效性验证
                boolean isCheck = false;
                if (action!=null) {
                    Iterator ir = fieldsWritable.iterator();
                    while (ir.hasNext()) {
                        FormField f = (FormField)ir.next();
                        if (f.getName().equals(ff.getName())) {
                            isCheck = true;
                            break;
                        }
                    }
                }
                else {
                    isCheck = true;
                }

                if (isCheck) {
                    try {
                        // LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
                        FormDAOMgr.checkField(request, fu, pck, ff, "detaillist_" + macroField.getName() + "_" + ff.getName() + "_" + uniqueIndexes[i], null);
                    } catch (CheckErrException e) {
                        // 如果onError=exit，则会抛出异常
                        throw new ErrMsgException(e.getMessage());
                    }
                }
            }
        }
        
        if (pck.getMsgs().size()!=0) {
            throw new ErrMsgException(pck.getMessage(false));
        }

		String fds = "";
		String str = "";
		for (int i = 0; i < cols; i++) {
			if (fields[i].startsWith("main:")) {
        		continue;
        	}
        	if (fields[i].startsWith("other:")) {
        		continue;
        	}
			if (fds.equals("")) {
				fds = fields[i];
				str = "?";
			} else {
				fds += "," + fields[i];
				str += ",?";
			}
		}

        String sql = "select id from " + fd.getTableNameByForm() +
                     " where cws_id=" + StrUtil.sqlstr(String.valueOf(fdaoId)) +
                     " order by cws_order";
        FormDAO fdao = new FormDAO();
        Vector v = fdao.list(formCode, sql);
        long[] ids = new long[v.size()]; // 数据库中已有记录的ID
        Iterator ir = v.iterator();
        int i = 0;
        while (ir.hasNext()) {
            fdao = (FormDAO) ir.next();
            ids[i] = fdao.getId();
            i++;
        }

        long[] newIds = new long[uniqueIndexes.length];
        for (i = 0; i < uniqueIndexes.length; i++) {
            String val = StrUtil.getNullStr(fu.getFieldValue("detaillist_" + macroField.getName() + "_id_" + uniqueIndexes[i]));
            // 新增加的行的id值为空字符串
            newIds[i] = StrUtil.toLong(val, -1);
        }

        int ret = 0;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            sql = "insert into " + fd.getTableNameByForm() +
                  " (flowId, cws_creator, cws_id, cws_order, " + fds +
                  ",flowTypeCode, cws_status,cws_parent_form) values (?,?,?,?," + str + ",?,?,?)";
            int cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_NOT;
            if (action==null) {
            	cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            }
            // 检查是否有新增项
            for (i = 0; i < uniqueIndexes.length; i++) {
                // 跳过空行
                if (map.get("" + i)!=null) {
                    continue;
                }
                // 如果是新增的行
                if (newIds[i]==-1) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, flowId);
                    ps.setString(2, creator);
                    ps.setString(3, String.valueOf(fdaoId));
                    ps.setInt(4, i);

                    // LogUtil.getLog(getClass()).info("cwsId=" + cwsId +
                    //        " creator=" + creator);

                    // 赋值
                    for (int j = 1; j <=cols; j++) {
                    	if (fields[j-1].startsWith("main:")) {
                    		continue;
                    	}
                    	if (fields[j-1].startsWith("other:")) {
                    		continue;
                    	}
                        FormField field = fd.getFormField(fields[j-1]);
                        if (field == null) {
                        	continue;
                        }
                        // 取得表单域的值
                        String val = StrUtil.getNullStr(fu.getFieldValue("detaillist_" + macroField.getName() + "_" + field.getName() + "_" + uniqueIndexes[i]));
                        field.setValue(val);
                        // String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // f为计算控件中涉及的表单域的字段
                        // 所以在此需赋值给fileUpload中对应的属性
                        fu.setFieldValue(field.getName(), field.getValue());

                        LogUtil.getLog(getClass()).info("fielName=" + field.getName() + " value=" + field.getValue());
                    }

                    int k = 5;
                    // 第一列为编号，所以从1而不是从0开始
                    for (int j = 1; j <= cols; j++) {
                    	if (fields[j-1].startsWith("main:")) {
                    		continue;
                    	}
                    	if (fields[j-1].startsWith("other:")) {
                    		continue;
                    	}
                        FormField field = fd.getFormField(fields[j-1]);
                        if (field == null) {
                        	continue;
                        }
                        field.createDAOVisual(ps, k, fu, fd);
                        k++;
                    }
                    String curTime = "" + System.currentTimeMillis();
                    ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
                    ps.setInt(k+1, cwsStatus);
                    ps.setString(k+2, macroField.getFormCode());
                    if (conn.executePreUpdate() == 1)
                        ret++;
                    if (ps != null) {
                        ps.close();
                        ps = null;
                    }
                }
            }

            sql = "delete from " + fd.getTableNameByForm() + " where id=?";
            // 检查是否有被删除项
            for (i = 0; i < ids.length; i++) {
                boolean isFound = false;
                for (int j = 0; j < uniqueIndexes.length; j++) {
                    if (newIds[j] == ids[i]) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setLong(1, ids[i]);
                    conn.executePreUpdate();
                }
            }

            fds = "";
            if (action==null) {
	            for (i = 0; i < cols; i++) {
	                if (fds.equals("")) {
	                    fds = fields[i] + "=?";
	                } else {
	                    fds += "," + fields[i] + "=?";
	                }
	            }
            }
            else {
	        	// 如果是处理流程，则只保存可写字段，因为不可写字段是disabled，无法提交
	        	ir = fieldsWritable.iterator();
	        	while (ir.hasNext()) {
	        		String f = ((FormField)ir.next()).getName();
		            if (fds.equals("")) {
		                fds = f + "=?";
		            } else {
		                fds += "," + f + "=?";
		            }        		
	        	}           
            }
            
            sql = "update " + fd.getTableNameByForm() + " set " + fds +
                  ",cws_creator=?,cws_order=?,flowTypeCode=? where id=?";
            if ("".equals(fds)) {
                sql = "update " + fd.getTableNameByForm() + " set cws_creator=?,cws_order=?,flowTypeCode=? where id=?";            	
            }

            // @task暂未检查记录是否被修改
            for (i = 0; i < uniqueIndexes.length; i++) {
                ir = v.iterator();
                while (ir.hasNext()) {
                    fdao = (FormDAO) ir.next();
                    if (fdao.getId() == newIds[i]) {
                        PreparedStatement ps = conn.prepareStatement(sql);

                        int k = 1;
                        if (action==null) {

                            for (int j = 0; j< cols; j++) {
                                FormField ff = fd.getFormField(fields[j]);
                                String val = fu.getFieldValue("detaillist_" + macroField.getName() + "_" + ff.getName() + "_" + uniqueIndexes[i]);

                                ff.setValue(val);
                                LogUtil.getLog(getClass()).info(ff.getName() + "=" + ff.getValue());
                                // 因为在计算控件中，将根据下列语句获得表单域的字段值
                                // String s = fDao.getFieldValue(f); // f为计算控件中涉及的表单域的字段
                                // 所以在此需赋值给fileUpload中对应的属性
                                fdao.setFieldValue(ff.getName(), ff.getValue());
	                            ff.saveDAOVisual(fdao, ps, k, fdao.getId(), fd, fu);
	                            k++;                                
                            }
                        }
                        else {
            	        	Iterator irWrite = fieldsWritable.iterator();
            	        	while (irWrite.hasNext()) {
            	        		FormField ff = (FormField)irWrite.next();
                                String val = fu.getFieldValue("detaillist_" + macroField.getName() + "_" + ff.getName() + "_" + uniqueIndexes[i]);

                                ff.setValue(val);
                                LogUtil.getLog(getClass()).info(ff.getName() + "=" + ff.getValue());
                                // 因为在计算控件中，将根据下列语句获得表单域的字段值
                                // String s = fDao.getFieldValue(f); // f为计算控件中涉及的表单域的字段
                                // 所以在此需赋值给fileUpload中对应的属性
                                fdao.setFieldValue(ff.getName(), ff.getValue());            	        		
	                            ff.saveDAOVisual(fdao, ps, k, fdao.getId(), fd, fu);
	                            k++;            	        		
            	        	}
                        }

                        ps.setString(k, creator);
                        ps.setInt(k + 1, i);
                        String curTime = "" + System.currentTimeMillis();
                        ps.setString(k + 2, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取

                        ps.setLong(k + 3, newIds[i]);
                        conn.executePreUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            throw new ErrMsgException("数据库错误！");
        } finally {
            conn.close();
        }
        return ret;
    }

    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = super.getDisableCtlScript(ff, formElementId);
        str += "\ntry {tableOperate.style.display='none';} catch(e){}";
        str += "\ncanTdEditable=false;\n";
        return str;
    }

    @Override
    public int onDelNestCtlParent(FormField macroField, String cwsId) {
        String formCode = macroField.getDescription();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).info("saveForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + macroField.getDefaultValueRaw());
		}
		
        FormDAO fdao = new FormDAO();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        int r = 0;
        String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {cwsId});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                int fdaoId = rr.getInt(1);
                fdao = fdao.getFormDAO(fdaoId, fd);
                if (!fdao.isLoaded()) {
                    continue;
                }
                if (fdao.del()) {
                    r++;
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return r;
    }

    public JSONArray uploadExcel(ServletContext application,
                                 HttpServletRequest request) throws
            ErrMsgException {
        FileUpload fileUpload = doUpload(application, request);
        String upFile = writeExcel(fileUpload);
        if (!"".equals(upFile)) {
            String excelFile = Global.getRealPath() + upFile;
            String formCode = fileUpload.getFieldValue("formCode");
            JSONArray jsonAry = read(excelFile, formCode);
            File file = new File(excelFile);
            file.delete();
            return jsonAry;
        } else {
            throw new ErrMsgException("文件不能为空！");
        }
    }

   public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"xls", "xlsx"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String writeExcel(FileUpload fu) {
        if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                vpath = "upfile/" + fi.getExt() + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + vpath;
                fu.setSavePath(filepath);
                // 临时使用随机名称写入磁盘
                fu.writeFile(true);

                //File f = new File(vpath + fi.getDiskName());
                //f.delete();
                return vpath + fi.getDiskName();
            }
        }
        return "";
    }

    public JSONArray read(String xlspath, String formCode) throws ErrMsgException,
            IndexOutOfBoundsException {
        JSONArray rows = new JSONArray();
        try {
            Workbook book = WorkbookParser.getWorkbook(new java.io.File(xlspath));
            // 获取sheet表的总行数、总列数
            jxl.Sheet rs = book.getSheet(0);
            int rsRows = rs.getRows();
            int rsColumns = rs.getColumns();

            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDbOrInit(formCode);

            // String listField = StrUtil.getNullStr(msd.getString("list_field"));
            String[] fieldCodes = msd.getColAry(false, "list_field");

            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            if (rsColumns > fieldCodes.length) {
                rsColumns = fieldCodes.length;
            }

            // 从第二行开始
            for (int i = 1; i < rsRows; i++) {
                JSONObject cell = new JSONObject();

                for (int j = 0; j < rsColumns; j++) {
                    Cell cc = rs.getCell(j, i);
                    try {
                        String c = cc.getContents();
                        if (fd.getFormField(fieldCodes[j]).getFieldType()==FormField.FIELD_TYPE_DATE) {
                            java.util.Date d = DateUtil.parse(c, "dd/MM/yyyy");
                            if (d!=null) {
                                c = DateUtil.format(DateUtil.parse(c, "dd/MM/yyyy"), "yyyy-MM-dd");
                            }
                        }
                        cell.put(fieldCodes[j], c);
                    } catch (JSONException ex1) {
                    }
                }
                rows.put(cell);
            }
        } catch (BiffException | IOException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return rows;

    }

    @Override
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) {
        // rowOrder表示上传了几行，如：1,2，注意不能单凭rowOrder来判断是否上传，因为有可能出现一行的元素全为空的情况
        String rowOrder = fu.getFieldValue("detaillist_" + ff.getName() + "_rowOrder");
        String[] uniqueIndexes = null;
        if (rowOrder==null) {
            uniqueIndexes = new String[0];
        }
        else {
            uniqueIndexes = rowOrder.split(",");
        }

        if (uniqueIndexes.length==0) {
            return;
        }

        String formCode = ff.getDescription();
        try {
            String defaultVal = StrUtil.decodeJSON(formCode);
            JSONObject json = new JSONObject(defaultVal);
            formCode = json.getString("destForm");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).info("saveForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
            LogUtil.getLog(getClass()).error(e);
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(formCode);
        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");

        int cols = fields.length;
        // 找出空行
        int c = 0;
        Map<String, String> map = new HashMap<String, String>();
        for (String uniqueIndex : uniqueIndexes) {
            for (int j = 1; j <= cols; j++) {
                String fieldName = fields[j - 1];
                if (fieldName.startsWith("main:")) {
                    continue;
                }
                if (fieldName.startsWith("other:")) {
                    continue;
                }
                String val = fu.getFieldValue("detaillist_" + ff.getName() + "_" + fieldName + "_" + uniqueIndex);
                if (val != null && !val.equals("")) {
                    c++;
                    break;
                }
            }
        }

        if (c > 0) {
            fu.setFieldValue(ff.getName(), "cws");
        }
        else {
            fu.setFieldValue(ff.getName(), "");
        }
    }

    @Override
    public String getControlType() {
        return "text";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        return "";
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        return "";
    }
}
