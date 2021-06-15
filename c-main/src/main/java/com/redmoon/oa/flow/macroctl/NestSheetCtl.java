package com.redmoon.oa.flow.macroctl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import bsh.EvalError;
import bsh.Interpreter;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.visual.FormDAOLog;
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
		String parentFormCode = StrUtil.getNullStr((String) request.getAttribute("formCode"));
		if ("".equals(parentFormCode)) {
			// 可能是
			parentFormCode = StrUtil.getNullStr((String) request.getAttribute("formCodeRelated"));
		}

		String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));
		
		long mainId = -1;
		
		// 数据库中为空
		if (cwsId != null) {
			if ("show".equals(pageType)) { // module_show.jsp
            	int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;
            	long visualId = StrUtil.toLong(cwsId, -1);
            	if (visualId!=-1) {
	            	FormDb fd = new FormDb();
	            	fd = fd.getFormDb(parentFormCode);
	            	FormDAO fdao = new FormDAO();
	            	fdao = fdao.getFormDAO(visualId, fd);
	            	flowId = fdao.getFlowId();
	            	mainId = fdao.getId();
            	}				
				op = "view&cwsId=" + cwsId + "&flowId=" + flowId + "&mainId=" + mainId;
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
				mainId = fdao.getId();
                op = "view&cwsId=" + mainId + "&flowId=" + flowId + "&mainId=" + mainId;
            }			
			else if ("add".equals(pageType)) {
				op = "add";
			}
			else if ("edit".equals(pageType)) { // module_edit.jsp中pageType=edit
				op = "edit&cwsId=" + cwsId;
				if (request.getAttribute("formCodeRelated")!=null) {
					long parentId = ParamUtil.getLong(request, "parentId", -1);
					op += "&mainId=" + parentId; // 从模块所关联的父模块的ID
				}
				else {
					// mainId等于cwsId
					mainId = StrUtil.toLong(cwsId, -1);
					op += "&mainId=" + cwsId;
				}
			}
			else if ("flow".equals(pageType)) {
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
				else {
					op = "view&cwsId=" + mainId + "&flowId=" + flowId + "&mainId=" + mainId;
				}
			}
		}
		else {
			op = "add"; 
			if ("add".equals(pageType)) { // 仅module_add_relate.jsp中pageType为add，而nest_sheet_add_relate.jsp中为add_relate
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
			e.printStackTrace();
			LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
		}
		
		long workflowActionId = StrUtil.toLong(StrUtil.getNullStr((String)request.getAttribute("workflowActionId")), -1);

		StringBuffer requestParamBuf = new StringBuffer();
		Enumeration reqParamNames = request.getParameterNames();
		while (reqParamNames.hasMoreElements()) {
			String paramName = (String) reqParamNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = ParamUtil.getParam(request, paramName);
				// 过滤掉formCode等
				if ("code".equals(paramName)
						|| "formCode".equals(paramName)
						|| "moduleCode".equals(paramName)
						|| "flowId".equals(paramName)
						|| "menuItem".equals(paramName)
						|| "parentId".equals(paramName)
						|| "moduleCodeRelated".equals(paramName)
						|| "formCodeRelated".equals(paramName)
						|| "mode".equals(paramName) // 去掉mode及tagName
						|| "tagName".equals(paramName)
						|| "id".equals(paramName)
				) {
					;
				}
				else {
					// 传入在定制时，可能带入的其它参数
					StrUtil.concat(requestParamBuf, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
				}
			}
		}
		String url = "/visual/nest_sheet_view.jsp?formCode="
			+ nestFormCode + "&op=" + op + "&parentFormCode="
			+ parentFormCode + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName) + "&actionId=" + workflowActionId;
		url += "&" + requestParamBuf.toString();

		String path = Global.getFullRootPath(request) + url;
		
		String ajaxPath = request.getContextPath() + url;
		
		path += "&pageType=" + pageType;

		LogUtil.getLog(getClass()).info("path=" + path);

		if ("archive".equals(pageType)) {
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
		if (!"show".equals(pageType) && !"flowShow".equals(pageType)) {
			// System.out.println(getClass() + " pageType=" + pageType);
			str = "\n<script src='" + request.getContextPath()
				+ "/flow/macro/macro_js_nestsheet.jsp?op=forRefresh&pageType=" + pageType + "&isTab=" + isTab + "&mainId=" + mainId + "&fieldName=" + ff.getName() + "&parentFormCode=" + parentFormCode + "&nestFormCode=" + nestFormCode + "&path="+StrUtil.UrlEncode(ajaxPath)+"&flowId=" + cwsId
				+ "'></script>\n" + str;
		}
		return str;
	}

	@Override
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
     * @param ff
     */
    @Override
	public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) {
		// 为了向下兼容
		fu.setFieldValue(ff.getName(), "");
		String nestFormCode = ff.getDescription();
		// String nestFieldName = ff.getName();
		try {
			String defaultVal = StrUtil.decodeJSON(ff.getDescription());
			JSONObject json = new JSONObject(defaultVal);
			nestFormCode = json.getString("destForm");
		} catch (JSONException e) {
			e.printStackTrace();
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
        		String sql = "select id from form_table_" + nestFormCode + " where cws_id=? and cws_parent_form=?";
        		JdbcTemplate jt = new JdbcTemplate();
        		try {
					ResultIterator ri = jt.executeQuery(sql, new Object[]{visualObjId, ff.getFormCode()});
					if (ri.size()>0) {
			    		fu.setFieldValue(ff.getName(), "cws");
					}
				} catch (SQLException e) {
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
	@Override
	@SuppressWarnings("deprecation")
	public int createForNestCtl(HttpServletRequest request,
			FormField macroField, String cwsId, String creator, FileUpload fu)
			throws ErrMsgException {
		if (true) {
			return 0;
		}
		return 0;
	}

	@Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
		String str = super.getDisableCtlScript(ff, formElementId);
		/*
		 * str += "\ntry {tableOperate.style.display='none';} catch(e){}"; str
		 * += "\ncanTdEditable=false;";
		 */
		return str;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int onDelNestCtlParent(FormField macroField, String cwsId) {
		String formCode = macroField.getDescription();
		try {
			// 20140102 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);

		int r = 0;
		String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=? and cws_parent_form=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] { cwsId, macroField.getFormCode() });
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				long fdaoId = rr.getLong(1);
				fdao = fdao.getFormDAO(fdaoId, fd);
				if (!fdao.isLoaded()) {
					continue;
				}
				if (fdao.del()) {
					r++;
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return r;
	}

	public int uploadExcel(ServletContext application, HttpServletRequest request, long parentId) throws ErrMsgException {
		FileUpload fileUpload = doUpload(application, request);
		String upFile = writeExcel(fileUpload);
		String excelFile = Global.getRealPath() + upFile;
		try {
			if (!upFile.equals("")) {
				String moduleCode = fileUpload.getFieldValue("moduleCode");
				int flowId = StrUtil.toInt(fileUpload.getFieldValue("flowId"), FormDAO.NONEFLOWID);
				String parentFormCode = ParamUtil.get(request, "parentFormCode");
				int rows = 0;
				try {
					rows = read(excelFile, moduleCode, parentFormCode, parentId, flowId, request);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return rows;
			} else {
				throw new ErrMsgException("文件不能为空！");
			}
		} finally {
			File file = new File(excelFile);
			file.delete();
		}
	}

	public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws ErrMsgException {
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

	/**
	 * import excel
	 * @param xlspath
	 * @param parentId
	 * @param flowId
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public int read(String xlspath, String moduleCode, String parentFormCode, long parentId, int flowId, HttpServletRequest request) throws IOException {
		Privilege pvg = new Privilege();
		String unitCode = pvg.getUserUnitCode(request);
		InputStream in = null;
		int rowcount = 0;
		try {
			// System.out.println(getClass()+"::::"+formCode);
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(moduleCode);
			String formCode = msd.getString("form_code");

			// String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fields = msd.getColAry(false, "list_field");

			/*
			 * for(int i = 0 ; i < fields.length; i ++){
			 * System.out.println(getClass()+"::"+i+","+fields[i]); }
			 */
			// System.out.println(getClass()+"::path="+path);
			FormDb fd = new FormDb(formCode);
			FormDAO fdao = new FormDAO(fd);

			Vector records = new Vector();

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
									for (int m = 0; m <= fields.length - 1; m++) {
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
									fdao.setCreator(pvg.getUser(request));
									fdao.setCwsParentForm(parentFormCode);
									fdao.create();

									// 如果需要记录历史
									if (fd.isLog()) {
										FormDAO.log(pvg.getUser(request), FormDAOLog.LOG_TYPE_CREATE, fdao);
									}
									records.addElement(fdao);
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
								for (int m = 0; m <= fields.length - 1; m++) {
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
								fdao.setCreator(pvg.getUser(request));
								fdao.setCwsParentForm(parentFormCode);
								fdao.create();

								// 如果需要记录历史
								if (fd.isLog()) {
									FormDAO.log(pvg.getUser(request), FormDAOLog.LOG_TYPE_CREATE, fdao);
								}
								records.addElement(fdao);
							}
						}
					}
				}
			}

			// 导入后事件
			if (!formCode.equals(moduleCode)) {
				msd = msd.getModuleSetupDbOrInit(formCode);
			}
			String script = msd.getScript("import_create");
			if (script != null && !script.equals("")) {
				Interpreter bsh = new Interpreter();
				try {
					StringBuffer sb = new StringBuffer();

					// 赋值用户
					sb.append("userName=\"" + pvg.getUser(request) + "\";");
					bsh.eval(BeanShellUtil.escape(sb.toString()));

					bsh.set("records", records);
					bsh.set("request", request);

					bsh.eval(script);
				} catch (EvalError e) {
					e.printStackTrace();
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
			e.printStackTrace();
			throw new ErrMsgException("JSON解析失败！");
		}
		
		boolean re = true;
		Privilege privilege = new Privilege();
		FormDb nestFd = new FormDb();
		nestFd = nestFd.getFormDb(nestFormCode);

		ModuleSetupDb msdSource = new ModuleSetupDb();
		msdSource = msdSource.getModuleSetupDb(formCode);
		formCode = msdSource.getString("form_code");

		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		
		com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
		
		String sql = "select id from " + fd.getTableNameByForm() + " t1";

		// 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值，再作为参数重定向回本页面传入sql条件参数中
		if (!"".equals(filter)) {
			filter = ModuleUtil.parseFilter(request, formCode, filter)[0];
			// 如果filter中是完整的select语句
			if (filter.startsWith("select ")) {
				sql = filter;
			}
			else {
				sql += " where " + filter;
			}
		}

		if (!sql.toLowerCase().contains("cws_status")) {
			// 取出 order by 后面的部分
			int p = sql.lastIndexOf(" order by ");
			String orderByStr = "";
			if (p != -1) {
				orderByStr = sql.substring(p);
				sql = sql.substring(0, p);
			}

			String cwsStatusName = "cws_status";
			if (sql.contains(" t1.")) {
				cwsStatusName = "t1.cws_status";
			}

			if (!sql.contains(" where ")) {
				sql += " where " + cwsStatusName + "=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
			} else {
				sql += " and " + cwsStatusName + "=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
			}
			if (p != -1) {
				sql += orderByStr;
			}
		}

		// 新增记录的ID
		ModuleSetupDb msd = new ModuleSetupDb();
		StringBuffer newIds = new StringBuffer();
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		for (FormDAO formDAO : fdao.list(formCode, sql)) {
			fdao = formDAO;
			msd = msd.getModuleSetupDbOrInit(nestFormCode);

			// 根据映射关系赋值
			JSONObject jsonObj2 = new JSONObject();
			for (int k = 0; k < mapAry.length(); k++) {
				JSONObject jsonObj = null;
				try {
					jsonObj = mapAry.getJSONObject(k);
					String sfield = (String) jsonObj.get("sourceField");
					String dfield = (String) jsonObj.get("destField");

					String fieldValue = fdao.getFieldValue(sfield);
					if (sfield.equals(FormDAO.FormDAO_NEW_ID) || "FormDAO_ID".equals(sfield)) {
						fieldValue = String.valueOf(fdao.getId());
					}

					jsonObj2.put(dfield, fieldValue);

					fdaoNest.setFieldValue(dfield, fieldValue);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			int flowId = ParamUtil.getInt(request, "flowId", FormDAO.NONEFLOWID);
			fdaoNest.setFlowId(flowId);
			if (parentId == -1) {
				fdaoNest.setCwsId(FormDAO.NAME_TEMP_CWS_IDS);
			} else {
				fdaoNest.setCwsId(String.valueOf(parentId));
			}
			fdaoNest.setCreator(privilege.getUser(request));
			fdaoNest.setUnitCode(privilege.getUserUnitCode(request));
			fdaoNest.setCwsQuoteId((int) fdao.getId());
			fdaoNest.setCwsParentForm(nestField.getFormCode());
			try {
				re = fdaoNest.create();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}

			StrUtil.concat(newIds, ",", String.valueOf(fdaoNest.getId()));
		}
		
		request.setAttribute("newIds", newIds.toString());
		
		return re;
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
    @Override
	public Object getValueForCreate(int flowId, FormField ff) {
        return "";
    }	
}
