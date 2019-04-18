package com.redmoon.oa.flow.macroctl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import jxl.Cell;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.read.biff.WorkbookParser;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
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

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
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
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;

/**
 * <p>
 * Title: 嵌套表格2，与关联模块相对应
 * </p>
 * <p>
 *  
 * Description:{"sourceForm":"sales_customer", "destForm":"access_control", "filter":"customer like {$@client}", "maps":[{"sourceField": "customer", "destField":"c"},{"sourceField": "address", "destField":"description"}]}
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class NestSheetCtl extends AbstractMacroCtl {
	public NestSheetCtl() {
		super();
	}

	/**
	 * 通过NetUtil.gather方法取得控件的HTML代码
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param ff
	 *            FormField
	 * @return String
	 */
	public String getNestSheet(HttpServletRequest request, FormField ff) {
		// 使之隐藏
		if (ff.isHidden()) {
			return "";
		}		
		
		String op = "add";
		String cwsId = StrUtil.getNullStr((String) request.getAttribute("cwsId"));
		// 获取父页面中处理的formCode
		String parentFormCode = StrUtil.getNullStr((String) request
				.getAttribute("formCode"));	
		if ("".equals(parentFormCode)) {
			// 可能是
			parentFormCode = StrUtil.getNullStr((String) request
					.getAttribute("formCodeRelated"));	
		}

		String pageType = StrUtil.getNullStr((String) request
				.getAttribute("pageType"));		
		
		long mainId = -1;
		
		// 数据库中为空
		if (cwsId != null) {
			if (pageType.equals("show")) { // module_show.jsp
            	int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;
            	long visualId = StrUtil.toLong(cwsId, -1);
            	if (visualId!=-1) {
	            	FormDb fd = new FormDb();
	            	fd = fd.getFormDb(parentFormCode);
	            	FormDAO fdao = new FormDAO();
	            	fdao = fdao.getFormDAO(visualId, fd);
	            	flowId = fdao.getFlowId();
            	}				
				op = "view&cwsId=" + cwsId + "&flowId=" + flowId;
			}
            else if (pageType.equals("archive")) {
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
			else if (pageType.equals("add")) {
				op = "add";
			}
			else if (pageType.equals("edit")) { // module_edit.jsp中pageType=edit
				op = "edit&cwsId=" + cwsId;
				if (request.getAttribute("formCodeRelated")!=null) {
					long parentId = ParamUtil.getLong(request, "parentId", -1);
					op += "&mainId=" + parentId; // 从模块所关联的父模块的ID
				}				
			}
			else if (pageType.equals("flow")) {
                int flowId = StrUtil.toInt(cwsId);
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(wf.getTypeCode());
                
                FormDb flowFd = new FormDb();
                flowFd = flowFd.getFormDb(lf.getFormCode());
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO(flowId, flowFd);
                mainId = fdao.getId();
                
				// System.out.println(getClass() + " pageType=" + pageType);
				// System.out.println(getClass() + " ff.isEditable()=" +
				// ff.isEditable() + ff.getName());
				if (ff.isEditable()) {
					// flowId用于带入nest_sheet_add_relate.jsp中作为hidden传值，以写入本嵌套表格2的flow_id字段中
					op = "edit&cwsId=" + mainId + "&flowId=" + flowId + "&mainId=" + mainId;
				}
				else
					op = "view&cwsId=" + mainId + "&flowId=" + flowId + "&mainId=" + mainId;
			}
		}
		else {
			op = "add"; 
			if (pageType.equals("add")) { // 仅module_add_relate.jsp中pageType为add，而nest_sheet_add_relate.jsp中为add_relate
				if (request.getAttribute("formCodeRelated")!=null) {
					long parentId = ParamUtil.getLong(request, "parentId", -1);
					op = "add&mainId=" + parentId; // 从模块所关联的父模块的ID
				}			
			}
		}

		// 为了向下兼容
		int isTab = 0;
		String nestFormCode = ff.getDescription();
		String nestFieldName = ff.getName();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(ff.getDescription());
			JSONObject json = new JSONObject(defaultVal);
			nestFormCode = json.getString("destForm");
			if (json.has("isTab")) {
				isTab = json.getInt("isTab");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
		}
		
		long workflowActionId = StrUtil.toLong(StrUtil.getNullStr((String)request.getAttribute("workflowActionId")), -1);

		// 因为有的服务器需要内外网都能访问，但是从外网访问时服务器不能用外网访问自己，所以作如下修改，注意在setup的时候需用内网地址
		// setup的时候，需要将服务器设为内网IP
		String url = "/visual/nest_sheet_view.jsp?formCode="
			+ nestFormCode + "&op=" + op + "&parentFormCode="
			+ parentFormCode + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName) + "&actionId=" + workflowActionId;
		String path = Global.getFullRootPath(request) + url;
		
		String ajaxPath = request.getContextPath() + url;
		
		path += "&pageType=" + pageType;

		LogUtil.getLog(getClass()).info("path=" + path);

		if (pageType.equals("archive")) {
			String str = NetUtil.gather(request, "utf-8", path);
			str ="<div id='nestsheet_"+ff.getName()+"'>"+str+"</div>";
			return str;
		}
		
		String str ="<div id='nestsheet_"+ff.getName()+"'></div>";
		
		if (isTab==1) {
			// 加入判断tabs-***是否存在，是因为在模块查看界面中并没有选项卡
			str += "\n<script>$(function() { if (o('tabs-" + nestFormCode + "')) loadNestCtl('" + ajaxPath + "','tabs-" + nestFormCode + "'); });</script>\n";			
		}
		else {
			str += "\n<script>loadNestCtl('" + ajaxPath + "','nestsheet_" + ff.getName() + "')</script>\n";
		}

		if (request.getAttribute("isNestSheetCtlJS") == null) {
			str = "\n<script src='" + request.getContextPath()
			+ "/flow/macro/macro_js_nestsheet.jsp?isTab=" + isTab + "&pageType=" + pageType + "&fieldName=" + ff.getName() + "&nestFormCode=" + nestFormCode + "&flowId=" + cwsId
			+ "'></script>\n" + str;

			request.setAttribute("isNestSheetCtlJS", "y");
		}
		
		// 因为每个表单的重新载入调用的方法是不一样的，所以在这里需要分别为每个嵌套表生成方法
		// 当自动拉单后，需调用forRefresh
		if (!"show".equals(pageType)) {
			// System.out.println(getClass() + " pageType=" + pageType);
			str = "\n<script src='" + request.getContextPath()
				+ "/flow/macro/macro_js_nestsheet.jsp?op=forRefresh&pageType=" + pageType + "&isTab=" + isTab + "&mainId=" + mainId + "&fieldName=" + ff.getName() + "&parentFormCode=" + parentFormCode + "&nestFormCode=" + nestFormCode + "&path="+StrUtil.UrlEncode(ajaxPath)+"&flowId=" + cwsId
				+ "'></script>\n" + str;
		}
		return str;
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		LogUtil.getLog(getClass()).info("ff=" + ff.getName());
		String str = getNestSheet(request, ff);
		// 转换javascript脚本中的\为\\
		str = str.replaceAll("\\\\", "\\\\\\\\");
		return str;
	}
	
    /**
     * 如果在流程中，嵌套表格2被置为必填，在验证前置表单域的值，以便于有效性检查能通过
     * @param request
     * @param fu
     * @param flowId
     * @param ff
     */
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) {
		// 为了向下兼容
		fu.setFieldValue(ff.getName(), "");    	
		String nestFormCode = ff.getDescription();
		// String nestFieldName = ff.getName();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(ff.getDescription());
			JSONObject json = new JSONObject(defaultVal);
			nestFormCode = json.getString("destForm");
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
		}
		
		String flowId = fu.getFieldValue("flowId");
		// 如果是智能模块
		if (flowId==null) {
			// 20180904 fgf 添加以取代在com.redmoon.oa.flow.FormDAOMgr.validateFields中对于数据是否为空进行验证    		
    		boolean isCreate = false;
            long visualObjId = StrUtil.toLong(fu.getFieldValue("id"), -1);
            if (visualObjId==-1) {
            	// 20170227 手机端的明细表中编辑时可能未传过来
            	visualObjId = ParamUtil.getInt(request, "id", -1);
            	if (visualObjId==-1) {
            		isCreate = true;
            	}
            }    		
            
    		if (!isCreate) {
        		// 检查是否存在数据
        		String sql = "select id from form_table_" + nestFormCode + " where cws_id=?";
        		JdbcTemplate jt = new JdbcTemplate();
        		try {
					ResultIterator ri = jt.executeQuery(sql, new Object[]{visualObjId});
					if (ri.size()>0) {
			    		fu.setFieldValue(ff.getName(), "cws");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else {
    			// 当在模块添加的时候，ifdao为null
            	// 检查从模块是否有加入表单的临时temp_cws_ids_formCode
                String fName = com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS + "_" + nestFormCode;
                String[] ary = fu.getFieldValues(fName);
                if (ary!=null && ary.length>0) {
            		fu.setFieldValue(ff.getName(), "cws");
                }
    		}
		}
		else {
			// 如果是流程
	    	FormDb flowFd = new FormDb();
	    	flowFd = flowFd.getFormDb(ff.getFormCode());
	        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
	        fdao = fdao.getFormDAO(StrUtil.toInt(flowId), flowFd);
	        long cwsId = fdao.getId();
			
			com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(ff.getFormCode());
			String relateFieldValue = fdm.getRelateFieldValue(cwsId, nestFormCode);
			
	    	FormDb fd = new FormDb();
	    	fd = fd.getFormDb(nestFormCode);
	    	String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(relateFieldValue);
	    		    	
	    	Vector fdaoV = fdao.list(nestFormCode, sql); 
	    	if (fdaoV != null && fdaoV.size()>0) {
	    		fu.setFieldValue(ff.getName(), "cws");
	    	}
	    	else {
	    		fu.setFieldValue(ff.getName(), "");
	    	}
		}

    }        	

	/**
	 * 当创建父记录时，同步创建嵌套表单的记录（用于visual模块，流程中用不到，因为流程中事先生成了空的表单
	 * 
	 * @param macroField
	 *            FormField
	 * @param cwsId
	 *            String
	 * @param creator
	 *            String
	 * @param fu
	 *            FileUpload
	 * @return int
	 * @throws ErrMsgException
	 */
	@SuppressWarnings("deprecation")
	public int createForNestCtl(HttpServletRequest request,
			FormField macroField, String cwsId, String creator, FileUpload fu)
			throws ErrMsgException {
		if (true)
			return 0;
		
		String cws_cell_rows = fu.getFieldValue("cws_cell_rows");
		int rows = StrUtil.toInt(cws_cell_rows, 0);

		String formCode = macroField.getDescription();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			LogUtil.getLog(getClass()).info("createForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + macroField.getDefaultValueRaw());
		}
		
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDbOrInit(formCode);

		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);

		String listField = StrUtil.getNullStr(msd.getString("list_field"));

		String[] fields = StrUtil.split(listField, ",");
		if (fields == null) {
			throw new ErrMsgException("模块：" + fd.getName()
					+ " 尚未配置，没有设置列表中的显示字段");
		}
		int cols = fields.length;

		// 有效性验证
		ParamChecker pck = new ParamChecker(request, fu);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				FormField ff = fd.getFormField(fields[j]);
				try {
					// LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
					FormDAOMgr.checkField(request, fu, pck, ff, "cws_cell_" + i
							+ "_" + j, null);
				} catch (CheckErrException e) {
					// 如果onError=exit，则会抛出异常
					throw new ErrMsgException(e.getMessage());
				}
			}
		}
		if (pck.getMsgs().size() != 0)
			throw new ErrMsgException(pck.getMessage(false));

		String fds = "";
		String str = "";
		for (int i = 0; i < cols; i++) {
			if (fds.equals("")) {
				fds = fields[i];
				str = "?";
			} else {
				fds += "," + fields[i];
				str += ",?";
			}
		}
		String sql = "insert into " + fd.getTableNameByForm()
				+ " (flowId, cws_creator, cws_id, " + fds
				+ ",flowTypeCode,cws_status) values (?,?,?," + str + ",?,?)";

		LogUtil.getLog(getClass()).info(
				"sql=" + sql + " cols=" + cols + " cws_cell_rows="
						+ cws_cell_rows);

		int ret = 0;
		Conn conn = new Conn(Global.getDefaultDB());
		try {
			conn.beginTrans();
			for (int i = 0; i < rows; i++) {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, FormDAO.NONEFLOWID);
				ps.setString(2, creator);
				ps.setString(3, cwsId);

				LogUtil.getLog(getClass()).info(
						"cwsId=" + cwsId + " creator=" + creator);

				int k = 4;
				for (int j = 0; j < cols; j++) {
					FormField field = fd.getFormField(fields[j]);
					LogUtil.getLog(getClass()).info(
							"cws_cell_"
									+ i
									+ "_"
									+ j
									+ "="
									+ fu.getFieldValue("cws_cell_" + i + "_"
											+ j));

					field.setValue(fu.getFieldValue("cws_cell_" + i + "_" + j));
					field.createDAOVisual(ps, k, fu, fd);
					k++;
				}
				String curTime = "" + System.currentTimeMillis();
				ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
				int cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_DONE;
				ps.setInt(k+1, cwsStatus);
				if (conn.executePreUpdate() == 1)
					ret++;
				if (ps != null) {
					ps.close();
					ps = null;
				}
			}
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			throw new ErrMsgException("数据库错误！");
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return ret;
	}

	/**
	 * 保存嵌套表单中的记录，智能模块与流程中共用本方法
	 * 
	 * @param macroField
	 *            FormField
	 * @param cwsId
	 *            String 父记录的ID，用于与父记录关联
	 * @param creator
	 *            String 当运用于流程时，creator为空，因为无法记录是具体哪个人修改了嵌套表格
	 * @param fu
	 *            FileUpload
	 * @return int
	 * @throws ErrMsgException
	 */
	/*
	 * public int saveForNestCtl(HttpServletRequest request, FormField
	 * macroField, String cwsId, String creator, FileUpload fu) throws
	 * ErrMsgException { String cws_cell_rows =
	 * fu.getFieldValue("cws_cell_rows"); int rows =
	 * StrUtil.toInt(cws_cell_rows, 0);
	 * 
	 * String formCode = macroField.getDefaultValueRaw();
	 * 
	 * ModuleSetupDb msd = new ModuleSetupDb(); msd =
	 * msd.getModuleSetupDbOrInit(formCode);
	 * 
	 * FormDb fd = new FormDb(); fd = fd.getFormDb(formCode);
	 * 
	 * String listField = StrUtil.getNullStr(msd.getString("list_field"));
	 * 
	 * String[] fields = StrUtil.split(listField, ","); int cols =
	 * fields.length;
	 * 
	 * // 有效性验证 ParamChecker pck = new ParamChecker(request, fu); for (int i =
	 * 0; i < rows; i++) { for (int j = 1; j <= cols; j++) { FormField ff =
	 * fd.getFormField(fields[j-1]); try { //
	 * LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
	 * FormDAOMgr.checkField(pck, ff, "cws_cell_" + i + "_" + j); } catch
	 * (CheckErrException e) { // 如果onError=exit，则会抛出异常 throw new
	 * ErrMsgException(e.getMessage()); } } } if (pck.getMsgs().size()!=0) throw
	 * new ErrMsgException(pck.getMessage(false));
	 * 
	 * String fds = ""; String str = ""; for (int i = 0; i < cols; i++) { if
	 * (fds.equals("")) { fds = fields[i]; str = "?"; } else { fds += "," +
	 * fields[i]; str += ",?"; } }
	 * 
	 * String sql = "select id from " + fd.getTableNameByForm() +
	 * " where cws_id=" + StrUtil.sqlstr(cwsId) + " order by cws_order"; FormDAO
	 * fdao = new FormDAO(); Vector v = fdao.list(formCode, sql); int[] ids =
	 * new int[v.size()]; // 数据库中已有记录的ID Iterator ir = v.iterator(); int i = 0;
	 * while (ir.hasNext()) { fdao = (FormDAO) ir.next(); ids[i] = fdao.getId();
	 * i++; }
	 * 
	 * int[] newIds = new int[rows]; for (i = 0; i < rows; i++) { newIds[i] =
	 * StrUtil.toInt(fu.getFieldValue("cws_cell_" + i + "_0"), -1);
	 * LogUtil.getLog(getClass()).info("fu.getFieldValue(" + i + ",0)=" +
	 * fu.getFieldValue("cws_cell_" + i + "_0")); }
	 * 
	 * LogUtil.getLog(getClass()).info("sql=" + sql + " cols=" + cols +
	 * " cws_cell_rows=" + cws_cell_rows);
	 * 
	 * int ret = 0; Conn conn = new Conn(Global.getDefaultDB()); try {
	 * conn.beginTrans();
	 * 
	 * sql = "insert into " + fd.getTableNameByForm() +
	 * " (flowId, cws_creator, cws_id, cws_order, " + fds +
	 * ",flowTypeCode) values (?,?,?,?," + str + ",?)";
	 * 
	 * // 检查是否有新增项 for (i = 0; i < rows; i++) { if (newIds[i] == -1) {
	 * PreparedStatement ps = conn.prepareStatement(sql); ps.setInt(1,
	 * FormDAO.NONEFLOWID); ps.setString(2, creator); ps.setString(3, cwsId);
	 * ps.setInt(4, i);
	 * 
	 * // LogUtil.getLog(getClass()).info("cwsId=" + cwsId + // " creator=" +
	 * creator);
	 * 
	 * int k = 5; // 第一列为编号，所以从1而不是从0开始 for (int j = 1; j <= cols; j++) {
	 * FormField field = fd.getFormField(fields[j-1]);
	 * LogUtil.getLog(getClass()).info("cws_cell_" + i + "_" + j + "=" +
	 * fu.getFieldValue("cws_cell_" + i + "_" + j));
	 * 
	 * field.setValue(StrUtil.getNullStr(fu.getFieldValue("cws_cell_" + i + "_"
	 * + j))); field.createDAOVisual(ps, k, fu, fd); k++; } String curTime = ""
	 * + System.currentTimeMillis(); ps.setString(k, "" + curTime); //
	 * 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取 if
	 * (conn.executePreUpdate() == 1) ret++; if (ps != null) { ps.close(); ps =
	 * null; } } }
	 * 
	 * sql = "delete from " + fd.getTableNameByForm() + " where id=?"; //
	 * 检查是否有被删除项 for (i = 0; i < ids.length; i++) { boolean isFound = false; for
	 * (int j = 0; j < rows; j++) { if (newIds[j] == ids[i]) { isFound = true;
	 * break; } } if (!isFound) { PreparedStatement ps =
	 * conn.prepareStatement(sql); ps.setInt(1, ids[i]);
	 * conn.executePreUpdate(); } }
	 * 
	 * fds = ""; for (i = 0; i < cols; i++) { if (fds.equals("")) { fds =
	 * fields[i] + "=?"; } else { fds += "," + fields[i] + "=?"; } } sql =
	 * "update " + fd.getTableNameByForm() + " set " + fds +
	 * ",cws_creator=?,cws_order=?,flowTypeCode=? where id=?";
	 * 
	 * // @task暂未检查记录是否被修改 for (i = 0; i < rows; i++) { ir = v.iterator(); while
	 * (ir.hasNext()) { fdao = (FormDAO) ir.next(); if (fdao.getId() ==
	 * newIds[i]) { PreparedStatement ps = conn.prepareStatement(sql); int k =
	 * 1; for (int j = 0; j< cols; j++) { FormField ff =
	 * fd.getFormField(fields[j]); ff.setValue(fu.getFieldValue("cws_cell_" + i
	 * + "_" + (j+1))); LogUtil.getLog(getClass()).info(ff.getName() + "=" +
	 * ff.getValue()); ff.saveDAOVisual(fdao, ps, k, fdao.getId(), fd, fu); k++;
	 * } ps.setString(k, creator); ps.setInt(k + 1, i); String curTime = "" +
	 * System.currentTimeMillis(); ps.setString(k + 2, "" + curTime); //
	 * 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
	 * 
	 * ps.setInt(k + 3, newIds[i]); conn.executePreUpdate(); } } }
	 * 
	 * conn.commit(); } catch (SQLException e) { conn.rollback();
	 * LogUtil.getLog(getClass()).error(StrUtil.trace(e)); throw new
	 * ErrMsgException("数据库错误！"); } finally { if (conn != null) { conn.close();
	 * conn = null; } } return ret; }
	 */

	public String getDisableCtlScript(FormField ff, String formElementId) {
		String str = super.getDisableCtlScript(ff, formElementId);
		/*
		 * str += "\ntry {tableOperate.style.display='none';} catch(e){}"; str
		 * += "\ncanTdEditable=false;";
		 */
		return str;
	}

	@SuppressWarnings("deprecation")
	public int onDelNestCtlParent(FormField macroField, String cwsId) {
		String formCode = macroField.getDescription();
		try {
			// 20140102 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			LogUtil.getLog(getClass()).info("createForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + macroField.getDefaultValueRaw());
		}
		
		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);

		int r = 0;
		String sql = "select id from " + fd.getTableNameByForm()
				+ " where cws_id=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] { cwsId });
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				long fdaoId = rr.getLong(1);
				fdao = fdao.getFormDAO(fdaoId, fd);
				if (!fdao.isLoaded()) {
					continue;
				}
				if (fdao.del())
					r++;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return r;
	}

	public int uploadExcel(ServletContext application,
			HttpServletRequest request, long parentId) throws ErrMsgException {
		FileUpload fileUpload = doUpload(application, request);
		String upFile = writeExcel(fileUpload);
		if (!upFile.equals("")) {
			String excelFile = Global.getRealPath() + upFile;
			String moduleCode = fileUpload.getFieldValue("moduleCode");
			int flowId = StrUtil.toInt(fileUpload.getFieldValue("flowId"), FormDAO.NONEFLOWID);
			int rows = 0;
			try {
				rows = read(excelFile, moduleCode, parentId, flowId, request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File file = new File(excelFile);
			file.delete();
			return rows;
		} else
			throw new ErrMsgException("文件不能为空！");
	}

	public FileUpload doUpload(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		FileUpload fileUpload = new FileUpload();
		fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
		String[] extnames = { "xls", "xlsx" };
		fileUpload.setValidExtname(extnames); // 设置可上传的文件类型
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
			if (v.size() > 0)
				fi = (FileInfo) v.get(0);
			String vpath = "";
			if (fi != null) {
				// 置保存路径
				Calendar cal = Calendar.getInstance();
				String year = "" + (cal.get(Calendar.YEAR));
				String month = "" + (cal.get(Calendar.MONTH) + 1);
				vpath = "upfile/" + fi.getExt() + "/" + year + "/" + month
						+ "/";
				String filepath = Global.getRealPath() + vpath;
				fu.setSavePath(filepath);
				// 使用随机名称写入磁盘
				fu.writeFile(true);

				// File f = new File(vpath + fi.getDiskName());
				// f.delete();
				// System.out.println("FleUpMgr " + fi.getName() + " " +
				// fi.getFieldName() + " " + fi.getDiskName());
				return vpath + fi.getDiskName();
			}
		}
		return "";
	}

	public JSONArray readXXX(String xlspath, String formCode, long parentId,
			HttpServletRequest request) throws ErrMsgException,
			IndexOutOfBoundsException {
		JSONArray rows = new JSONArray();
		try {
			Workbook book = WorkbookParser
					.getWorkbook(new java.io.File(xlspath));
			// 获取sheet表的总行数、总列数
			jxl.Sheet rs = book.getSheet(0);
			int rsRows = rs.getRows();
			int rsColumns = rs.getColumns();

			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(formCode);

			Privilege pvg = new Privilege();
			String unitCode = pvg.getUserUnitCode(request);

			String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fieldCodes = StrUtil.split(listField, ",");
			String fields = "";
			String fsize = "";
			for (int i = 0; i < fieldCodes.length; i++) {
				if (fields.equals("")) {
					fields += fieldCodes[i];
				} else {
					fields += "," + fieldCodes[i];
				}
				if (fsize.equals("")) {
					fsize += "?";
				} else {
					fsize += "," + "?";
				}
			}

			String tableName = FormDb.getTableName(formCode);
			// cws_id 父表单ID=parentId
			String sql = "insert into "
					+ tableName
					+ " (flowId, flowTypeCode, cws_creator, cws_id, unit_code, "
					+ fields + ") values (?,?,?,?,?, " + fsize + ")";
			JdbcTemplate jt = new JdbcTemplate();

			// System.out.println(getClass() + " " + formCode + " listField=" +
			// listField);

			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);

			if (rsColumns > fieldCodes.length)
				rsColumns = fieldCodes.length;

			// 从第二行开始
			for (int i = 1; i < rsRows; i++) {
				JSONObject cell = new JSONObject();

				Object[] obj = new Object[rsColumns + 5];
				obj[0] = -1;
				obj[1] = System.currentTimeMillis();
				obj[2] = pvg.getUser(request);
				obj[3] = parentId;
				obj[4] = unitCode;
				for (int j = 0; j < rsColumns; j++) {
					Cell cc = rs.getCell(j, i);
					String c = cc.getContents();
					obj[j + 5] = c;
					/**
					 * try { String c = cc.getContents(); if
					 * (fd.getFormField(fieldCodes
					 * [j]).getFieldType()==FormField.FIELD_TYPE_DATE) {
					 * java.util.Date d = DateUtil.parse(c, "dd/MM/yyyy"); if
					 * (d!=null) c = DateUtil.format(DateUtil.parse(c,
					 * "dd/MM/yyyy"), "yyyy-MM-dd"); } cell.put(fieldCodes[j],
					 * c); } catch (JSONException ex1) { }
					 */
				}
				try {
					jt.executeUpdate(sql, obj);
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(
							"NestSheetCtlImport:" + e.getMessage());
				}
				rows.put(cell);
			}
		} catch (BiffException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return rows;
	}

	/**
	 * import excel
	 * @param xlspath
	 * @param formCode
	 * @param parentId
	 * @param flowId
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public int read(String xlspath, String moduleCode, long parentId, int flowId,
			HttpServletRequest request) throws IOException {
		Privilege pvg = new Privilege();
		String unitCode = pvg.getUserUnitCode(request);
		InputStream in = null;
		int rowcount = 0;
		try {
			// System.out.println(getClass()+"::::"+formCode);
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(moduleCode);

			String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fields = StrUtil.split(listField, ",");
			/*
			 * for(int i = 0 ; i < fields.length; i ++){
			 * System.out.println(getClass()+"::"+i+","+fields[i]); }
			 */
			// System.out.println(getClass()+"::path="+path);
			FormDb fd = new FormDb(msd.getString("form_code"));
			FormDAO fdao = new FormDAO(fd);

			in = new FileInputStream(xlspath);
			String pa = StrUtil.getFileExt(xlspath);
			if (pa.equals("xls")) {
				try {
					// 读取xls格式的excel文档
					HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
					// 获取sheet
					for (int i = 0; i < w.getNumberOfSheets() && i<1; i++) {
						HSSFSheet sheet = w.getSheetAt(i);
						if (sheet != null) {
							// 获取行数
							rowcount = sheet.getLastRowNum();
							HSSFCell cell = null;

							FormField ff = null;
							Vector<FormField> vfields = null;
							// 获取每一行
							for (int k = 1; k <= rowcount; k++) {
								vfields = new Vector<FormField>();
								HSSFRow row = sheet.getRow(k);
								if (row != null) {
									int colcount = row.getLastCellNum();
									// 获取每一单元格
									for (int m = 0; m < colcount; m++) {
										ff = fd.getFormField(fields[m]);
										cell = row.getCell(m);
										// 如果单元格中的值为空，则cell可能为null
										if (cell!=null) {
											if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC && HSSFDateUtil.isCellDateFormatted(cell))
											{
												Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
												ff.setValue(DateUtil.format(date, "yyyy-MM-dd"));
											}
											else
											{
												cell.setCellType(HSSFCell.CELL_TYPE_STRING);
												ff.setValue(cell.getStringCellValue());
											}
										}
										
										vfields.add(ff);
									}
									fdao.setFields(vfields);
									fdao.setUnitCode(unitCode);
									fdao.setFlowId(flowId);
									fdao.setCwsId(String.valueOf(parentId));
									// fdao.save();
									fdao.create();
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (pa.equals("xlsx")) {
				XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
				for (int i = 0; i < w.getNumberOfSheets() && i<1; i++) {
					XSSFSheet sheet = w.getSheetAt(i);
					if (sheet != null) {
						rowcount = sheet.getLastRowNum();
						XSSFCell cell = null;

						// FormDAO fdao = new FormDAO();
						FormField ff = null;
						Vector<FormField> vfields = null;
						for (int k = 1; k <= rowcount; k++) {
							vfields = new Vector<FormField>();
							XSSFRow row = sheet.getRow(k);
							if (row != null) {
								int colcount = row.getLastCellNum();
								for (int m = 0; m < colcount; m++) {
									cell = row.getCell(m);
									ff = fd.getFormField(fields[m]);
									if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC && HSSFDateUtil.isCellDateFormatted(cell))
									{
										Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
										ff.setValue(DateUtil.format(date, "yyyy-MM-dd"));
									}
									else
									{
										cell.setCellType(XSSFCell.CELL_TYPE_STRING);
										ff.setValue(cell.getStringCellValue());
									}								
									vfields.add(ff);
								}
								fdao.setFields(vfields);
								fdao.setUnitCode(unitCode);
								fdao.setFlowId(flowId);
								fdao.setCwsId(String.valueOf(parentId));
								// fdao.save();
								fdao.create();
							}
						}
					}
				}

			}
		} catch (Exception e) {
			// LogUtil.getLog(SignMgr.class).error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return rowcount;
	}

	/**
	 * 自动拉单 20161204
	 * @Description: 
	 * @param request
	 * @param parentId
	 * @param nestField
	 * @return
	 * @throws ErrMsgException
	 */
	public static boolean autoSel(HttpServletRequest request, long parentId, FormField nestField) throws ErrMsgException {
		JSONObject json = null;
		JSONArray mapAry = new JSONArray();
		String nestFormCode = "", formCode = "";
		String filter = "";
		try {
			String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
			json = new JSONObject(defaultVal);
			nestFormCode = json.getString("destForm");
			formCode = json.getString("sourceForm");			
			filter = json.getString("filter");
			mapAry = (JSONArray)json.get("maps");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new ErrMsgException("JSON解析失败！");
		}
		
		boolean re = true;
		Privilege privilege = new Privilege();
		FormDb nestFd = new FormDb();
		nestFd = nestFd.getFormDb(nestFormCode);
		
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		
		com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
		
		String sql = "select id from " + fd.getTableNameByForm();

		// 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值，再作为参数重定向回本页面传入sql条件参数中
		if (!"".equals(filter)) {
			filter = ModuleUtil.parseFilter(request, formCode, filter)[0];
			sql += " where " + filter;
		}
		
		if (sql.toLowerCase().indexOf("cws_status")==-1) {
			if (sql.indexOf(" where ")==-1) {
				sql += " where cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
			}
			else {
				sql += " and cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
			}
		}

		// 新增记录的ID
		StringBuffer newIds = new StringBuffer();
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		Iterator ir = fdao.list(formCode, sql).iterator();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(nestFormCode);
			String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fields = StrUtil.split(listField, ",");
			int len = 0;
			if (fields!=null)
				len = fields.length;
			
			// System.out.println(getClass() + " nestType=" + nestType);
			
			// 根据映射关系赋值
			JSONObject jsonObj2 = new JSONObject();
			for (int k=0; k<mapAry.length(); k++) {
				JSONObject jsonObj = null;
				try {
					jsonObj = mapAry.getJSONObject(k);
					String sfield = (String) jsonObj.get("sourceField");
					String dfield = (String) jsonObj.get("destField");
					
					String fieldValue = fdao.getFieldValue(sfield);
					if (sfield.equals(com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID) || sfield.equals("FormDAO_ID")) {
						fieldValue = String.valueOf(fdao.getId());
					}

					jsonObj2.put(dfield, fieldValue);
					
					fdaoNest.setFieldValue(dfield, fieldValue);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
			fdaoNest.setFlowId(flowId);	
			if (parentId==-1) {
				fdaoNest.setCwsId(FormDAO.NAME_TEMP_CWS_IDS);
			}
			else {
				fdaoNest.setCwsId(String.valueOf(parentId));
			}
			fdaoNest.setCreator(privilege.getUser(request));
			fdaoNest.setUnitCode(privilege.getUserUnitCode(request));
			fdaoNest.setCwsQuoteId((int)fdao.getId());
			fdaoNest.setCwsParentForm(nestField.getFormCode());
			re = fdaoNest.create();
			
			StrUtil.concat(newIds, ",", String.valueOf(fdaoNest.getId()));
		}
		
		request.setAttribute("newIds", newIds.toString());
		
		return re;
	}
	
	public String getControlType() {
		return "text";
	}

	public String getControlValue(String userName, FormField ff) {
		return "";
	}

	public String getControlText(String userName, FormField ff) {
		return "";
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
	}

	/**
	 * 用于手机端处理流程时，得到嵌套表格、嵌套表格2、明细表宏控件的描述json，用于传值给H5界面
	 * @Description: fgf 20170412 
	 * @param ff
	 * @return
	 */
	public static JSONObject getCtlDesc(FormField ff) {
		try {
			// 20131123 fgf 添加
			JSONObject jsonObj = new JSONObject();
			String defaultVal = StrUtil.decodeJSON(ff.getDescription());
			JSONObject json = new JSONObject(defaultVal);
			String nestFormCode = json.getString("destForm");
			String filter = json.getString("filter");
			
			String formCode = json.getString("sourceForm");
			
			jsonObj.put("sourceForm", formCode);
			jsonObj.put("destForm", nestFormCode);
			
			StringBuffer parentFields = new StringBuffer();
			if (!"".equals(filter)) {
				Pattern p = Pattern.compile(
						"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(filter);
			    while (m.find()) {
			        String fieldName = m.group(1);
			        if (fieldName.equals("cwsCurUser") || fieldName.equals("curUser") 
			         	|| fieldName.equals("curUserDept") || fieldName.equals("curUserRole") || fieldName.equals("admin.dept")) {
			         	continue;
			        }
					
					StrUtil.concat(parentFields, ",", fieldName);
			    }
			}
			jsonObj.put("parentFields", parentFields.toString());	
			return jsonObj;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
    /**
     * 用于流程处理时，生成表单默认值
     * @param ff FormField
     * @return Object
     */
    public Object getValueForCreate(int flowId, FormField ff) {
        return "";
    }	
}
