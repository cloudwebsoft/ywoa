package com.redmoon.oa.flow.macroctl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.Conn;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.NetUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.CheckErrException;
import com.redmoon.oa.flow.FormDAOMgr;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import java.io.IOException;
import com.redmoon.kit.util.FileInfo;
import java.util.Calendar;
import com.redmoon.oa.person.UserDb;
import jxl.read.biff.BiffException;
import jxl.read.biff.WorkbookParser;
import jxl.Workbook;
import jxl.Cell;

import org.json.*;
import java.io.File;
import com.redmoon.oa.flow.WorkflowActionDb;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.Directory;

/**
 * <p>Title: 嵌套表格，对应于nest_table_view.jsp，注意一个表单中不能同时出现两个嵌套表格，会致JS及<div id="dragDiv" style="display:none"></div>重复出现</p>
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
public class NestTableCtl extends AbstractMacroCtl {
    public NestTableCtl() {
        super();
    }
    
    /**
     * 通过NetUtil.gather方法取得控件的HTML代码
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    public String getNestTable(HttpServletRequest request, FormField ff) {
        String op = "add";
        String cwsId = (String) request.getAttribute("cwsId");
        long workflowActionId = StrUtil.toLong((String)request.getAttribute("workflowActionId"), -1);
        
        // 数据库中为空
        if (cwsId!=null) {
            String pageType = StrUtil.getNullStr((String)request.getAttribute("pageType"));
            if (pageType.equals("show")) { // module_show.jsp
                op = "show&cwsId=" + cwsId;
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
                op = "show&cwsId=" + fdaoId;
            }
            else if (pageType.equals("archive")) {
                op = "show&action=archive&cwsId=" + cwsId;
            }
            else if ("flow".equals(pageType)) { // flow_dispose.jsp中pageType=flow
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
                op = "edit&cwsId=" + fdaoId;
            }
            else { // module_edit.jsp中pageType=edit
                op = "edit&cwsId=" + cwsId;
            }
            if (workflowActionId!=-1) {
                WorkflowActionDb wad = new WorkflowActionDb();
                wad = wad.getWorkflowActionDb((int)workflowActionId);
                // 审阅
                if (wad.getKind()==WorkflowActionDb.KIND_READ) {
                    op = "show&cwsId=" + cwsId;
                }
            }
        }

        if (ff.isEditable()) {
            // 主表中嵌套表字段不可写，则不允许添加、删除行，只可以根据权限编辑
            op += "&isNestFormFieldEditable=true";
        }

        // 将request中其它参数也传至url中
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                // 过滤掉formCode
                if (paramName.equals("formCode"))
                    op += "&parentFormCode=" + StrUtil.UrlEncode(paramValue);
                else
                    op += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
            }
        }

        if (op.indexOf("parentFormCode=")==-1) {
            // 当在流程的flow_dispose.jsp页面处理时，传递formCode，以便于导出嵌套表中的内容至EXCEL
            if (request.getAttribute("formCode")!=null)
                op += "&parentFormCode=" + request.getAttribute("formCode");
        }

		// 为了向下兼容
		String nestFieldName = ff.getName();
		String nestFormCode = ff.getDescription();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(ff.getDescription());
			JSONObject json = new JSONObject(defaultVal);
			nestFormCode = json.getString("destForm");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
		}

		// LogUtil.getLog(getClass()).info(nestFormCode + " nestFormCode2=" + nestFormCode);

        // String path = Global.getFullRootPath(request) + "/visual/nest_table_view.jsp?formCode=" +
        //              ff.getDefaultValueRaw() + "&op=" + op;

        // 因为有的服务器需要内外网都能访问，但是从外网访问时服务器不能用外网访问自己，所以作如下修改，注意在setup的时候需用内网地址
        // setup的时候，需要将服务器设为内网IP
        String pageType = StrUtil.getNullStr((String)request.getAttribute("pageType"));

        String path = Global.getFullRootPath(request) +
                      "/visual/nest_table_view.jsp?formCode=" +
                      StrUtil.UrlEncode(nestFormCode) + "&op=" + op + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName);
        path += "&pageType=" + pageType + "&actionId=" + workflowActionId;
        // LogUtil.getLog(getClass()).info("path=" + path);        
        return NetUtil.gather(request, "utf-8", path);
       
/* 
 * 在ie11下，能够正常编辑，但是将单元格的值赋予input然后appendChild至form时，提交不了至服务器端
 * ie8下面，不能正常生成表格
 *        String ajaxPath = request.getContextPath() + "/visual/nest_table_view.jsp?formCode=" +
        		StrUtil.UrlEncode(nestFormCode) + "&op=" + op + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName);
        ajaxPath += "&pageType=" + pageType + "&actionId=" + workflowActionId;

		String str ="<div id='nesttable_"+ff.getName()+"'></div>";
        
    	str += "\n<script>loadNestCtl('" + ajaxPath + "','nesttable_" + ff.getName() + "')</script>\n";

		if (request.getAttribute("isNestTableCtlJS") == null) {
			str = "\n<script src='" + request.getContextPath()
			+ "/flow/macro/macro_js_nesttable.jsp??formCode=" +
        		StrUtil.UrlEncode(nestFormCode) + "&op=" + op + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName) + "&pageType=" + pageType + "&actionId=" + workflowActionId + "&flowId=" + cwsId
			+ "'></script>\n" + str;			

			request.setAttribute("isNestTableCtlJS", "y");
		}
		
    	return str;*/
    	
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        LogUtil.getLog(getClass()).info("ff=" + ff.getName());
        String str = getNestTable(request, ff);
        // 转换javascript脚本中的\为\\
        str = str.replaceAll("\\\\", "\\\\\\\\");
        return str;
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
    public int createForNestCtl(HttpServletRequest request, FormField macroField, String cwsId,
                                String creator,
                                FileUpload fu) throws ErrMsgException {
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
        if (fields==null) {
            LogUtil.getLog(getClass()).error("createForNestCtl:The fields is null, please set " + formCode + " module's list");
            return -1;
        }
        int cols = fields.length;

        // 有效性验证
        ParamChecker pck = new ParamChecker(request, fu);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                LogUtil.getLog(getClass()).info("fields[" + j + "]=" + fields[j]);
                FormField ff = fd.getFormField(fields[j]);
                try {
                    // LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
                    FormDAOMgr.checkField(request, fu, pck, ff, "cws_cell_" + i + "_" + j, null);
                } catch (CheckErrException e) {
                    // 如果onError=exit，则会抛出异常
                    throw new ErrMsgException(e.getMessage());
                }
            }
        }
        if (pck.getMsgs().size()!=0)
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
        String sql = "insert into " + fd.getTableNameByForm() +
                     " (flowId, cws_creator, cws_id, " + fds +
                     ",flowTypeCode,cws_status) values (?,?,?," + str + ",?,?)";

        LogUtil.getLog(getClass()).info("sql=" + sql + " cols=" + cols +
                                        " cws_cell_rows=" + cws_cell_rows);

        int ret = 0;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            conn.beginTrans();
            for (int i = 0; i < rows; i++) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, FormDAO.NONEFLOWID);
                ps.setString(2, creator);
                ps.setString(3, cwsId);

                LogUtil.getLog(getClass()).info("cwsId=" + cwsId + " creator=" +
                                                creator);
                // 赋值
                for (int j = 0; j < cols; j++) {
                    FormField field = fd.getFormField(fields[j]);
                    // 取得表单域的值
                    field.setValue(fu.getFieldValue("cws_cell_" + i + "_" + j));
                    // 因为在计算控件中，将根据下列语句获得表单域的字段值
                    // String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // f为计算控件中涉及的表单域的字段
                    // 所以在此需赋值给fileUpload中对应的属性
                    fu.setFieldValue(field.getName(), field.getValue());
                }

                int k = 4;
                for (int j = 0; j < cols; j++) {
                    FormField field = fd.getFormField(fields[j]);
                    /*
                    LogUtil.getLog(getClass()).info("cws_cell_" + i + "_" + j +
                            "=" + fu.getFieldValue("cws_cell_" + i + "_" + j));

                    field.setValue(fu.getFieldValue("cws_cell_" + i + "_" + j));
                    */

                    field.createDAOVisual(ps, k, fu, fd);
                    k++;
                }

                String curTime = "" + System.currentTimeMillis();
                ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
                ps.setInt(k+1, com.redmoon.oa.flow.FormDAO.STATUS_DONE);
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
     * @param macroField FormField
     * @param cwsId String 父记录的ID，用于与父记录关联
     * @param creator String 当运用于流程时，creator为空，因为无法记录是具体哪个人修改了嵌套表格
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    public int saveForNestCtl(HttpServletRequest request, FormField macroField, String cwsId,
                              String creator,
                              FileUpload fu) throws ErrMsgException {
    	String skey = fu.getFieldValue("skey");
    	if(skey != null && !skey.trim().equals("")){
    		return 0;
    	}
        String cws_cell_rows = fu.getFieldValue("cws_cell_rows");
        // 手机端没有post过来cws_cell_rows，在此不作处理，直接返回0，以免数据被清空
        if (cws_cell_rows==null) {
        	return 0;
        }
        
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
			LogUtil.getLog(getClass()).info("saveForNestCtl:" + formCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + macroField.getDefaultValueRaw());
		}
		
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(formCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        String listField = StrUtil.getNullStr(msd.getString("list_field"));

        String[] fields = StrUtil.split(listField, ",");
        int cols = fields.length;

        // 找出空行
        Map map = new HashMap();
        for (int i = 0; i < rows; i++) {
            boolean isBlankRow = true;
            for (int j = 1; j <= cols; j++) {
                FormField ff = fd.getFormField(fields[j - 1]);
                String val = StrUtil.getNullStr(fu.getFieldValue("cws_cell_" + i + "_" +
                                j)).trim();
                if (!val.equals("")) {
                    isBlankRow = false;
                    break;
                }
            }
            if (isBlankRow)
                map.put("" + i, "" + i);
        }
        
        long fdaoId = StrUtil.toLong(cwsId);

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
                        if (ff.getName().equals(fds[i])) {
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
                Iterator ir = flds.iterator();
                // 将可写的域筛选出
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    for (int i = 0; i < len; i++) {
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
        for (int i = 0; i < rows; i++) {
            // 跳过空行
            if (map.get("" + i)!=null)
                continue;
            for (int j = 1; j <= cols; j++) {
                FormField ff = fd.getFormField(fields[j-1]);

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
                else
                    isCheck = true;

                if (isCheck) {
                    try {
                        // LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
                        FormDAOMgr.checkField(request, fu, pck, ff, "cws_cell_" + i + "_" + j, null);
                    } catch (CheckErrException e) {
                        // 如果onError=exit，则会抛出异常
                        throw new ErrMsgException(e.getMessage());
                    }
                }
            }
        }
        
        if (pck.getMsgs().size()!=0)
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

        int[] newIds = new int[rows];
        for (i = 0; i < rows; i++) {
            newIds[i] = StrUtil.toInt(fu.getFieldValue("cws_cell_" + i + "_0"),
                                      -1);
            LogUtil.getLog(getClass()).info("fu.getFieldValue(" + i + ",0)=" + fu.getFieldValue("cws_cell_" + i + "_0"));
        }

        LogUtil.getLog(getClass()).info("sql=" + sql + " cols=" + cols +
                                       " cws_cell_rows=" + cws_cell_rows);

        int ret = 0;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            conn.beginTrans();

            sql = "insert into " + fd.getTableNameByForm() +
                  " (flowId, cws_creator, cws_id, cws_order, " + fds +
                  ",flowTypeCode, cws_status) values (?,?,?,?," + str + ",?,?)";
            int cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_NOT;
            if (action==null) {
            	cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            }
            // 检查是否有新增项
            for (i = 0; i < rows; i++) {
                // 跳过空行
                if (map.get("" + i)!=null)
                    continue;
                if (newIds[i] == -1) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, flowId);
                    ps.setString(2, creator);
                    ps.setString(3, String.valueOf(fdaoId));
                    ps.setInt(4, i);

                    // LogUtil.getLog(getClass()).info("cwsId=" + cwsId +
                    //        " creator=" + creator);

                    // 赋值
                    for (int j = 1; j <=cols; j++) {
                        FormField field = fd.getFormField(fields[j-1]);
                        // 取得表单域的值
                        field.setValue(StrUtil.getNullStr(fu.getFieldValue("cws_cell_" + i + "_" +
                                j)));
                        // String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // f为计算控件中涉及的表单域的字段
                        // 所以在此需赋值给fileUpload中对应的属性
                        fu.setFieldValue(field.getName(), field.getValue());

                        LogUtil.getLog(getClass()).info("fielName=" + field.getName() + " value=" + field.getValue());
                    }

                    int k = 5;
                    // 第一列为编号，所以从1而不是从0开始
                    for (int j = 1; j <= cols; j++) {
                        FormField field = fd.getFormField(fields[j-1]);
                        /*
                        LogUtil.getLog(getClass()).info("cws_cell_" + i + "_" +
                                j + "=" +
                                fu.getFieldValue("cws_cell_" + i + "_" + j));

                        field.setValue(StrUtil.getNullStr(fu.getFieldValue("cws_cell_" + i + "_" +
                                j)));
                        */
                        field.createDAOVisual(ps, k, fu, fd);
                        k++;
                    }
                    String curTime = "" + System.currentTimeMillis();
                    ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
                    ps.setInt(k+1, cwsStatus);
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
                for (int j = 0; j < rows; j++) {
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
            for (i = 0; i < cols; i++) {
                if (fds.equals("")) {
                    fds = fields[i] + "=?";
                } else {
                    fds += "," + fields[i] + "=?";
                }
            }
            sql = "update " + fd.getTableNameByForm() + " set " + fds +
                  ",cws_creator=?,cws_order=?,flowTypeCode=? where id=?";

            // @task暂未检查记录是否被修改
            for (i = 0; i < rows; i++) {
                ir = v.iterator();
                while (ir.hasNext()) {
                    fdao = (FormDAO) ir.next();
                    if (fdao.getId() == newIds[i]) {
                        PreparedStatement ps = conn.prepareStatement(sql);

                        for (int j = 0; j< cols; j++) {
                            FormField ff = fd.getFormField(fields[j]);
                            ff.setValue(fu.getFieldValue("cws_cell_" + i +
                                    "_" + (j+1)));
                            LogUtil.getLog(getClass()).info(ff.getName() + "=" + ff.getValue());
                            // 因为在计算控件中，将根据下列语句获得表单域的字段值
                            // String s = fDao.getFieldValue(f); // f为计算控件中涉及的表单域的字段
                            // 所以在此需赋值给fileUpload中对应的属性
                            fdao.setFieldValue(ff.getName(), ff.getValue());
                        }

                        int k = 1;
                        for (int j = 0; j< cols; j++) {
                            FormField ff = fd.getFormField(fields[j]);
                            /*
                            ff.setValue(fu.getFieldValue("cws_cell_" + i +
                                    "_" + (j+1)));
                            LogUtil.getLog(getClass()).info(ff.getName() + "=" + ff.getValue());
                            */
                            ff.saveDAOVisual(fdao, ps, k, fdao.getId(), fd, fu);
                            k++;
                        }

                        ps.setString(k, creator);
                        ps.setInt(k + 1, i);
                        String curTime = "" + System.currentTimeMillis();
                        ps.setString(k + 2, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取

                        ps.setInt(k + 3, newIds[i]);
                        conn.executePreUpdate();
                    }
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

    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = super.getDisableCtlScript(ff, formElementId);
        str += "\ntry {tableOperate.style.display='none';} catch(e){}";
        str += "\ncanTdEditable=false;\n";
        return str;
    }

    public int onDelNestCtlParent(FormField macroField, String cwsId) {
        String formCode = macroField.getDescription();
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(formCode);
			JSONObject json = new JSONObject(defaultVal);
			formCode = json.getString("destForm");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
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
                if (fdao.del())
                    r++;
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
        if (!upFile.equals("")) {
            String excelFile = Global.getRealPath() + upFile;
            String formCode = fileUpload.getFieldValue("formCode");
            JSONArray jsonAry = read(excelFile, formCode);
            File file = new File(excelFile);
            file.delete();
            return jsonAry;
        } else
            throw new ErrMsgException("文件不能为空！");
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
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String writeExcel(FileUpload fu) {
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                vpath = "upfile/" +
                        fi.getExt() + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + vpath;
                fu.setSavePath(filepath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);

                //File f = new File(vpath + fi.getDiskName());
                //f.delete();
                //System.out.println("FleUpMgr " + fi.getName() + " " + fi.getFieldName() + " " + fi.getDiskName());
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

            String listField = StrUtil.getNullStr(msd.getString("list_field"));
            String[] fieldCodes = StrUtil.split(listField, ",");

            // System.out.println(getClass() + " " + formCode + " listField=" + listField);

            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            if (rsColumns > fieldCodes.length)
                rsColumns = fieldCodes.length;

            // 从第二行开始
            for (int i = 1; i < rsRows; i++) {
                JSONObject cell = new JSONObject();

                for (int j = 0; j < rsColumns; j++) {
                    Cell cc = rs.getCell(j, i);
                    try {
                        String c = cc.getContents();
                        if (fd.getFormField(fieldCodes[j]).getFieldType()==FormField.FIELD_TYPE_DATE) {
                            java.util.Date d = DateUtil.parse(c, "dd/MM/yyyy");
                            if (d!=null)
                                c = DateUtil.format(DateUtil.parse(c, "dd/MM/yyyy"), "yyyy-MM-dd");
                        }
                        cell.put(fieldCodes[j], c);
                    } catch (JSONException ex1) {
                    }
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
}
