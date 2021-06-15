package com.redmoon.oa.flow;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAOLog;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.base.*;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.tools.Pdf2htmlEXUtil;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;

import org.apache.log4j.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:  对表单中的数据进行存储
 * @task:需将getFieldValue优化为从map中存取
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormDAO implements IFormDAO, Serializable {
    Vector<FormField> fields;
    String tableName;
    int flowId;
    Logger logger = Logger.getLogger(FormDAO.class.getName());

    String connname = Global.getDefaultDB();

    FormDb fd;
    
    /**
     * 临时记录
     */
    public static final int STATUS_TEMP = -1;
    
    /**
     * 保存的草稿
     */
    public static final int STATUS_DRAFT = -5;
    
    /**
     * 流程尚未走完
     */
    public static final int STATUS_NOT = 0;
    /**
     * 流程已结束
     */
    public static final int STATUS_DONE = 1;
    
    /**
     * 流程被拒绝
     */
    public static final int STATUS_REFUSED = -2;
    
    /**
     * 流程被放弃
     */
    public static final int STATUS_DISCARD = - 3;
    
    /**
     * 流程被刪除
     */
    public static final int STATUS_DELETED = -4;
    
    /**
     * 已冲抵
     */
    public static final int FLAG_AGAINST_YES = 1;
    
    /**
     * 未冲抵
     */
    public static final int FLAG_AGAINST_NO = 0;

    public FormDAO() {
    	
    }

    public FormDAO(int flowId, FormDb fd) {
        this.flowId = flowId;
        WorkflowDb wd = new WorkflowDb();
        wd = wd.getWorkflowDb(flowId);
        flowTypeCode = wd.getTypeCode();
        tableName = fd.getTableNameByForm();
        fields = fd.getFields();
        this.fd = fd;
        load();
    }

    public FormDAO getFormDAO(int flowId, FormDb fd) {
        FormDAO fdao = new FormDAO(flowId, fd);
        fdao.load();
        return fdao;
    }
    
    @Override
    public FormDb getFormDb() {
    	return fd;
    }

    public void setFields(Vector<FormField> fields) {
        this.fields = fields;
    }

    @Override
    public Vector<FormField> getFields() {
        return fields;
    }

    @Override
    public FormField getFormField(String name) {
        for (FormField ff : fields) {
            if (ff.getName().equals(name)) {
                return ff;
            }
        }
        return null;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void setFlowTypeCode(String flowTypeCode) {
        this.flowTypeCode = flowTypeCode;
    }

    public void setCwsId(String cwsId) {
        this.cwsId = cwsId;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isRecordExist(int flowId) {
        Conn conn = new Conn(connname);
        String sql = "select flowId from " + tableName + " where flowId=?";
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, flowId);
            rs = ps.executeQuery();
            if (rs!=null) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        catch (SQLException e) {
            logger.error("isRecordExist:" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
        return false;
    }

    public void load() {
        StringBuilder fds = new StringBuilder();
        for (FormField ff : fields) {
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName());
            } else {
                fds.append(",").append(ff.getName());
            }
        }
        // logger.info("load:" + fds);
        Conn conn = new Conn(connname);
        String sql;
        // 有的表单中可能不存在字段
        if ("".equals(fds.toString())) {
            sql = "select flowTypeCode,unit_code,cws_creator,cws_status,id,cws_quote_id,cws_id,cws_progress,cws_flag,cws_create_date,cws_modify_date,cws_finish_date,cws_quote_form from " + tableName + " where flowId=?";
        }
        else {
        	sql = "select " + fds + ",flowTypeCode,unit_code,cws_creator,cws_status,id,cws_quote_id,cws_id,cws_progress,cws_flag,cws_create_date,cws_modify_date,cws_finish_date,cws_quote_form from " + tableName + " where flowId=?";
        }
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, flowId);
            rs = ps.executeQuery();
            if (rs!=null) {
                if (rs.next()) {
                    int k = 1;
                    for (FormField ff : fields) {
                        try {
                            if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
                                java.sql.Date dt = rs.getDate(k);
                                ff.setValue(DateUtil.format(dt,
                                        FormField.FORMAT_DATE));
                            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
                                Timestamp ts = rs.getTimestamp(k);
                                String d = "";
                                if (ts != null) {
                                    d = DateUtil.format(new Date(ts.
                                            getTime()), FormField.FORMAT_DATE_TIME);
                                }
                                ff.setValue(d);
                            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE) {
                                double r = rs.getDouble(k);
                                if (r == 0 && rs.wasNull()) {
                                    ff.setValue("");
                                } else {
                                    ff.setValue("" + rs.getDouble(k));
                                }

                                // LogUtil.getLog(getClass()).info(ff.getName() + " flowId=" + flowId + " rs.wasNull()=" + rs.wasNull() + " rs.getDouble(" + k + ")=" + rs.getDouble(k));
                            } else if (ff.getFieldType() == FormField.FIELD_TYPE_FLOAT) {
                                ff.setValue("" + rs.getFloat(k));
                            } else if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
                                ff.setValue(NumberUtil.round(rs.getDouble(k), 2));
                            } else {
                                ff.setValue(rs.getString(k));
                                // System.out.println(getClass() + " " + ff.getName() + " " + ff.getTitle() + " " + ff.getValue());
                            }
                        } catch (SQLException e) {
                            // 以免出现如下问题：load:Value '0000-00-00' can not be represented as java.sql.Timestamp
                            logger.error("load1:" + e.getMessage());
                            e.printStackTrace();
                        }
                        k++;
                    }
                    flowTypeCode = rs.getString(k);
                    unitCode = rs.getString(k+1);
                    creator = StrUtil.getNullStr(rs.getString(k+2));
                    status = rs.getInt(k+3);
                    // 20140406 fgf add
                    id = rs.getLong(k+4);
                    cwsQuoteId = rs.getLong(k+5);
                    cwsId = rs.getString(k+6);
                    cwsProgress = rs.getInt(k+7);
                    cwsFlag = rs.getInt(k+8);
                    cwsCreateDate = rs.getTimestamp(k+9);
                    cwsModifyDate = rs.getTimestamp(k+10);
                    cwsFinishDate = rs.getTimestamp(k+11);
                    cwsQuoteForm = StrUtil.getNullStr(rs.getString(k+12));
                    loaded = true;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("load:" + e.getMessage());
            logger.error(StrUtil.trace(e));
            DebugUtil.e(getClass(), "load", e.getMessage());
        }
        finally {
            conn.close();
        }
    }

    /**
     * 当流程创建时自动添加一条记录至相应的表单table
     * @return boolean
     */
    public boolean create() {
        StringBuilder fds = new StringBuilder();
        StringBuilder str = new StringBuilder();
        for (FormField ff : fields) {
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName());
                str = new StringBuilder("?");
            } else {
                fds.append(",").append(ff.getName());
                str.append(",?");
            }
        }
        // 考虑到当表单中一个字段也没有的情况，如@流程
        if (!"".equals(fds.toString())) {
        	fds.insert(0, ",");
        	str.insert(0, ",");
        }
        
        String sql = "insert into " + tableName + "  (flowId" + fds + ",flowTypeCode,cws_id,unit_code,cws_status,cws_create_date,cws_creator) values (?" + str + ",?,?,?,?,?,?)";
        boolean re = false;

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, flowId);
            int k = 2;
            for (FormField ff : fields) {
                ff.createDAO(flowId, ps, k, fields);
                // logger.info(ff.getName() + " getDefaultValue=" + ff.getDefaultValue());
                k++;
            }
            
            ps.setString(k, flowTypeCode);
            ps.setString(k+1, cwsId);
            ps.setString(k+2, unitCode);
            
            status = STATUS_TEMP; // 表示临时流程记录
            ps.setInt(k+3, status);
            ps.setTimestamp(k+4, new Timestamp(new java.util.Date().getTime()));
            ps.setString(k+5, creator);
            re = conn.executePreUpdate()==1;
        }
        catch (SQLException e) {
            logger.error("create:" + StrUtil.trace(e));
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
        return re;
    }

    @Override
    public String getVisualPath() {
        // 置保存路径
        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(Calendar.YEAR));
        String month = "" + (cal.get(Calendar.MONTH) + 1);
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        return cfg.get("file_flow") + "/" + year + "/" + month;
    }

    @Override
    public void setFieldValue(String fieldName, String value) {
        for (FormField ff : fields) {
            if (ff.getName().equals(fieldName)) {
                LogUtil.getLog(getClass()).info(" ff.getName()=" + ff.getName() + " fieldName=" + fieldName + " value=" + value);
                ff.setValue(value);
                break;
            }
        }
    }

    /**
     * 保存表单中的记录
     * @return boolean
     * @throws ErrMsgException
     */
    @Override
    public boolean save() throws ErrMsgException {
        StringBuilder fds = new StringBuilder();
        for (FormField ff : fields) {
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName() + "=?");
            } else {
                fds.append(",").append(ff.getName()).append("=?");
            }
        }

        String sql;
        if ("".equals(fds.toString())) {
        	sql = "update " + tableName + " set flowTypeCode=?,cws_status=?,unit_code=?,cws_id=?,cws_progress=?,cws_flag=?,cws_modify_date=?,cws_finish_date=? where flowId=?";
        }
        else {
        	sql = "update " + tableName + " set " + fds + ",flowTypeCode=?,cws_status=?,unit_code=?,cws_id=?,cws_progress=?,cws_flag=?,cws_modify_date=?,cws_finish_date=? where flowId=?";
        }

        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            Iterator<FormField> ir = fields.iterator();
            int k = 1;
            while (ir.hasNext()) {
                FormField ff = ir.next();
                ff.saveDAOVisual(ps, k);
                k++;
            }
            ps.setString(k, flowTypeCode);
            ps.setInt(k+1, status);
            ps.setString(k+2, unitCode);
            ps.setString(k+3, cwsId);
            ps.setInt(k+4, cwsProgress);
            ps.setInt(k+5, cwsFlag);
            ps.setTimestamp(k+6, new Timestamp(new java.util.Date().getTime()));
            if (cwsFinishDate==null) {
                ps.setTimestamp(k+7, null);
            } else {
                ps.setTimestamp(k+7, new Timestamp(cwsFinishDate.getTime()));
            }
            ps.setInt(k+8, flowId);

            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            logger.error("save:" + StrUtil.trace(e));
        }
        finally {
            conn.close();
        }
        return re;
    }

    public boolean save(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        StringBuilder fds = new StringBuilder();
        Iterator<FormField> ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            // 辅助字段在此不保存，跳过
            if (ff.isHelper()) {
                continue;
            }
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName() + "=?");
            }
            else {
                fds.append(",").append(ff.getName()).append("=?");
            }
        }
        
        // 考虑到当表单中一个字段也没有的情况，如@流程
        if (!"".equals(fds.toString())) {
        	fds.append(",");
        }        
        
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);

        // 20160121 fgf cws_id有可能因为在流程流转过程中，因为被其它事件调用了FormDao.save，使得null变成为''
        // 当复命时，因为任务表单已与项目表单产生关联，cws_id不为空，所以需去掉 and (cws_id is null or cws_id='')
        // String sql = "update " + tableName + " set " + fds + "flowTypeCode=?,cws_status=? where flowId=? and (cws_id is null or cws_id='')";
        String sql = "update " + tableName + " set " + fds + "flowTypeCode=?,cws_status=?,cws_modify_date=? where flowId=?";
        
        logger.info("save: sql=" + sql);

        // 验证宏控件的值，如文号控件中需检查是否有重复
        MacroCtlMgr mm = new MacroCtlMgr();
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField macroField = (FormField) ir.next();
            if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.
                                                     getMacroType());
                if (mu!=null) {
                    mu.getIFormMacroCtl().validate(request, this, macroField, fu);
                }
                else {
                    LogUtil.getLog(getClass()).error("MacroCtl is not exist, macroType=" + macroField.getMacroType());
                }
            }
        }
        
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField macroField = (FormField) ir.next();
            if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.
                                                     getMacroType());
                logger.info("save: mu.getNestType()=" + macroField.
                        getMacroType() + " mu=" + mu);

                if (mu!=null && mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                    mu.getIFormMacroCtl().saveForNestCtl(request, macroField,
                            String.valueOf(flowId),
                            userName, fu);
                }
                else {
                	if (mu==null) {
                        LogUtil.getLog(getClass()).error("MacroCtl is not exist, macroType=" + macroField.getMacroType());
                    }
                }
            }
        }

        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            int pageNum = 1;
            Vector<FileInfo> v = fu.getFiles();
            // logger.info("save: v.size=" + v.size() + " re=" + re);
            // 先处理附件，以便于宏控件getValueForSave时从FileUpload中获取diskname
            if (fu.getRet() == FileUpload.RET_SUCCESS && v.size()>0) {
                WorkflowDb wd = new WorkflowDb();
                wd = wd.getWorkflowDb(flowId);
                int docId = wd.getDocId();
                Document doc = new Document();
                doc = doc.getDocument(docId);
                DocContent dc = doc.getDocContent(1);
                
                Map<String, String> attMap = new HashMap<String, String>();
            	java.util.Vector<Attachment> attachments = doc.getAttachments(1);
                for (Object attachment : attachments) {
                    Attachment att = (Attachment) attachment;
                    attMap.put(att.getName(), att.getName());
                }

		        boolean isMobile = false;
                OperatingSystem os = null;
                try {
                    // 当从第三方接口对接时，因没有User-Agent，会导致出现异常
                    UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
                    os = ua.getOperatingSystem();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if(os==null || DeviceType.MOBILE.equals(os.getDeviceType())) {
                    isMobile = true;
                }

                String vpath = getVisualPath();
                String filepath = Global.getRealPath() + vpath + "/";
                fu.setSavePath(filepath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                FileInfo fi;
                int orders = dc.getAttachmentMaxOrders() + 1;
                Privilege privilege = new Privilege();
                Iterator<FileInfo> irFile = v.iterator();
                while (irFile.hasNext()) {
                    fi = irFile.next();
                    // 检查是否已存在同名文件，因为如果是手机端操作，则有可能是在提交后条件判断时先保存了一下，然后选择人员后再次提交，所以文件会保存两次
                    // 而在PC端，如果表单中有Office在线编辑宏控件，保存时附件表中也会出现同名文件，此时不应跳过而应保存附件，因在getValueForSave中会删除旧的附件，保存新的附件
                    if (isMobile) {
	                    if (attMap.containsKey(fi.getName())) {
	                    	continue;
	                    }
                    }
                    
                    Attachment att = new Attachment();
                    att.setFullPath(filepath + fi.getDiskName());
                    att.setDocId(docId);
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setVisualPath(vpath);
                    att.setPageNum(pageNum);
                    att.setOrders(orders);
                    att.setFieldName(fi.fieldName);
                    att.setCreator(privilege.getUser(request));
                    att.setSize(fi.getSize());

                    // System.out.println(getClass() + " " + fi.getSize());

                    re = att.create();
                    
                    String previewfile=filepath + fi.getDiskName();
                	String ext = StrUtil.getFileExt(att.getDiskName());
                	if ("doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext)) {
                		com.redmoon.oa.fileark.Document.createOfficeFilePreviewHTML(previewfile);
                	}
                	else if ("pdf".equals(ext)) {
                		Pdf2htmlEXUtil.createPreviewHTML(previewfile);
                	}
                    
                    orders ++;
                }

                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(docId, pageNum);
            }

            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            
            // 处理网络硬盘文件
            String[] netdiskFiles = fu.getFieldValues("netdiskFiles");
            // LogUtil.getLog(getClass()).info("netdiskFiles=" + netdiskFiles);
            if (netdiskFiles != null) {
                int docId = wf.getDocId();
                Document doc = new Document();
                doc = doc.getDocument(docId);
                DocContent dc = doc.getDocContent(1);
                int orders = dc.getAttachmentMaxOrders() + 1;
                String vpath = getVisualPath();
                String filepath = Global.getRealPath() + vpath + "/";

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                Privilege privilege = new Privilege();

                com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();

                LogUtil.getLog(getClass()).info("netdiskFiles.length=" +
                                                netdiskFiles.length);

                for (String netdiskFile : netdiskFiles) {
                    att = att.getAttachment(StrUtil.toInt(netdiskFile));

                    String file_netdisk = cfg.get("file_netdisk");
                    String fullPath = Global.getRealPath() + file_netdisk +
                            "/" + att.getVisualPath() + "/" +
                            att.getDiskName();

                    String newName = RandomSecquenceCreator.getId() + "." +
                            StrUtil.getFileExt(att.getDiskName());
                    String newFullPath = filepath + "/" + newName;

                    File f = new File(filepath);
                    if (!f.isDirectory()) {
                        f.mkdirs();
                    }

                    // System.out.println(getClass() + " " + fullPath);
                    // System.out.println(getClass() + " " + newFullPath);

                    FileUtil.CopyFile(fullPath, newFullPath);
                    Attachment att2 = new Attachment();
                    att2.setFullPath(filepath + att.getName());
                    att2.setDocId(docId);
                    att2.setName(att.getName());
                    att2.setDiskName(newName);
                    att2.setVisualPath(vpath);
                    att2.setPageNum(pageNum);
                    att2.setOrders(orders);
                    att2.setFieldName("");
                    att2.setCreator(privilege.getUser(request));
                    att2.setSize(att.getSize());
                    re = att2.create();
                    orders++;
                }

                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(docId, pageNum);
            }

            logger.info("save: fields=" + fields + " flowId=" + flowId);

            PreparedStatement ps = conn.prepareStatement(sql);
            ir = fields.iterator();
            int k = 1;
            while (ir.hasNext()) {
                FormField ff = (FormField)ir.next();
                // 辅助字段在此不保存，跳过
                if (ff.isHelper()) {
                    continue;
                }
                ff.saveDAO(this, ps, k, flowId, fd, fu);
                logger.info("save: field name=" + ff.getName() + " value=" + ff.getValue() + " flowId=" + flowId);
                k++;
            }
            ps.setString(k, flowTypeCode);
            
            // 如果流程状态为未开始，则置表单的status为草稿状态
/*            if (wf.getStatus()==WorkflowDb.STATUS_NOT_STARTED) {
            	status = STATUS_DRAFT;
            }*/
            Boolean isSaveDraftObj = ((Boolean)request.getAttribute("isSaveDraft"));
            boolean isSaveDraft = false;
            if (isSaveDraftObj!=null) {
            	isSaveDraft = isSaveDraftObj;
            }

            if (isSaveDraft) {
            	// 如果是保存草稿操作，且流程未开始，则将表单置为保存草稿状态，如果流程已开始，则不能置表单为草稿状态
            	if (wf.getStatus()==WorkflowDb.STATUS_NOT_STARTED) {
            		status = STATUS_DRAFT;
            	}
            }
            else {
            	// 非保存草稿操作，且流程状态为未开始，则说明流程在提交（即开始流程），应改为未完成状态
            	if (wf.getStatus()==WorkflowDb.STATUS_NOT_STARTED) {
			        status = FormDAO.STATUS_NOT;
            	}
            }

            ps.setInt(k+1, status);
            ps.setTimestamp(k+2, new Timestamp(new java.util.Date().getTime()));
            ps.setInt(k+3, flowId);
            re = conn.executePreUpdate()==1;

            if (re) {
                ir = fields.iterator();
                while (ir.hasNext()) {
                    FormField macroField = (FormField) ir.next();
                    if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.getMacroType());
                        if (mu!=null) {
                            mu.getIFormMacroCtl().onFormDAOSave(request, this, macroField, fu);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            logger.error("save:" + StrUtil.trace(e));
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
        return re;
    }

    public String getFieldValueRaw(String fieldName) {
        for (FormField ff : fields) {
            if (fieldName.equals(ff.getName())) {
                return ff.getValue();
            }
        }
        return null;
    }

    /**
     * 取得表单域的值
     * @param fieldName String
     * @return String
     */
    @Override
    public String getFieldValue(String fieldName) {
        for (FormField ff : fields) {
            if (fieldName.equals(ff.getName())) {
                // System.out.println(getClass() + " " + ff.getName() + " " + ff.getTitle() + " " + ff.getValue());
                return StrUtil.getNullStr(ff.getValue());
            }
        }
        return null;
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        String sql = "delete from " + tableName + " where flowId=?";
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, flowId);
            re = conn.executePreUpdate() == 1;

            if (re) {
                // 删除嵌套表格
                Iterator<FormField> ir = fields.iterator();
                MacroCtlMgr mm = new MacroCtlMgr();
                while (ir.hasNext()) {
                    FormField macroField = ir.next();
                    if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.getMacroType());
                        if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                        	// 20161124 fgf 将flowId改为id
                            // mu.getIFormMacroCtl().onDelNestCtlParent(macroField,
                            //         "" + flowId);
                            mu.getIFormMacroCtl().onDelNestCtlParent(macroField,
                                    String.valueOf(id));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
        return re;
    }


    /**
     * 根据SQL语句列出表单编码为formCode的分页记录
     * @param formCode String 表单编码
     * @param listsql String SQL语句
     * @param curPage int 当前页码
     * @param pageSize int 每页记录数
     * @return ListResult
     * @throws ErrMsgException
     */
    public ListResult listResult(String formCode, String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs;
        Vector<FormDAO> result = new Vector<>();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            // logger.info("countsql=" + countsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
                lr.setTotal(total);
            }
            if (rs != null) {
                rs.close();
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (!rs.absolute(absoluteLocation)) {
                    return lr;
                }
                FormMgr fm = new FormMgr();
                FormDb fd = fm.getFormDb(formCode);
                do {
                    // logger.info("listResult: id=" + rs.getInt(1));
                    FormDAO fdao = getFormDAO(rs.getInt(1), fd);
                    result.addElement(fdao);
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        return lr;
    }


    /**
     * 全部的记录列表，当记录不多时，可以使用本方法，如列出友情链接，而当记录很多时，则不宜使用
     * @param formCode String
     * @param sql String
     * @return Vector
     */
    public Vector<FormDAO> list(String formCode, String sql) {
        ResultSet rs = null;
        int total = 0;
        Vector<FormDAO> result = new Vector<>();
        Connection conn = new Connection(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            conn.prepareStatement(sql);
            if (total != 0) {
                // sets the limit of the maximum number of rows in a ResultSet object
                conn.setMaxRows(total); // 尽量减少内存的使用
            }
            rs = conn.executePreQuery();
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                rs.setFetchSize(total); // rs一次从POOL中所获取的记录数
                if (!rs.absolute(1)) {
                    return result;
                }

                FormMgr fm = new FormMgr();
                FormDb fd = fm.getFormDb(formCode);

                do {
                    FormDAO fdao = getFormDAO(rs.getInt(1), fd);
                    result.addElement(fdao);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
            }
            conn.close();
        }
        return result;
    }

    /**
     * 置嵌套表宏控件对应的源表单的记录的冲抵标志位为1
     * @param mainFormFdaoId 主表单的ID
     * @param sourceFormCode
     * @param destFormCode
     * @param flagVal
     * @return
     */
    public static int updateFlag(int mainFormFdaoId, String sourceFormCode, String destFormCode, int flagVal) {
    	// 判断formCode是否为副模块，则需取出主模块的formCode
    	ModuleSetupDb msd = new ModuleSetupDb();
    	msd = msd.getModuleSetupDb(sourceFormCode);
    	if (!sourceFormCode.equals(msd.getString("form_code"))) {
    		sourceFormCode = msd.getString("form_code");
    	}
    	
    	int ret = 0;
		String sql = "select cws_quote_id from " + FormDb.getTableName(destFormCode) + " where cws_id=" + StrUtil.sqlstr(String.valueOf(mainFormFdaoId));
		String sqlUpdate = "update " + FormDb.getTableName(sourceFormCode) + " set cws_flag=" + flagVal + " where id=?";
		
		String sqlGetCwsId = "select cws_id from " + FormDb.getTableName(sourceFormCode) + " where id=?";
		String cwsId = "";
		boolean isCwsIdEqual = true;
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				int quoteId = rr.getInt(1);
				ret = jt.executeUpdate(sqlUpdate, new Object[]{(long) quoteId});
				
				// 20171105 fgf 判断被冲抵的记录的cws_id是否都一样，如果是，则说明是冲抵了某个主表中的嵌套表记录
				if (isCwsIdEqual) {
					ResultIterator riCwsId = jt.executeQuery(sqlGetCwsId, new Object[]{new Long(quoteId)});
					if (riCwsId.hasNext()) {
						ResultRecord rrCwsId = (ResultRecord)riCwsId.next();
						// 判断 cws_id是否都一样
						String myCwsId = StrUtil.getNullStr(rrCwsId.getString("cws_id"));
						if ("".equals(cwsId)) {
							cwsId = myCwsId;
						}
						else {
							if (!myCwsId.equals(cwsId)) {
								isCwsIdEqual = false;
							}
						}
					}
				}
			}
						
			// 20171105 fgf 如果源表单是个子表，且其主表所对应的所有记录均已被冲抵，则自动冲抵源表单的主表单
			if (isCwsIdEqual) {
				// 查询源表单是否从属于某个主表
				ModuleRelateDb mrd = new ModuleRelateDb();
				Vector v = mrd.getModuleReverseRelated(sourceFormCode);
				String sqlIsAllAgainst = "select id from " + FormDb.getTableName(sourceFormCode) + " where cws_flag=" + FLAG_AGAINST_NO + " and cws_id=" + StrUtil.sqlstr(cwsId);
                for (Object o : v) {
                    mrd = (ModuleRelateDb) o;
                    String formCode = mrd.getString("code");
                    String relateField = mrd.getString("relate_field");

                    // 检查对应的主表中的所有嵌套表记录是否均已被冲抵
                    ResultIterator riIsAllAgainst = jt.executeQuery(sqlIsAllAgainst);
                    if (riIsAllAgainst.size() == 0) {
                        // 如果均已被冲抵，则自动冲抵对应的主表中的记录
                        String sqlUpdateAgainst = "update " + FormDb.getTableName(formCode) + " set cws_flag=" + FLAG_AGAINST_YES + " where " + relateField + "=" + cwsId;
                        jt.executeUpdate(sqlUpdateAgainst);
                    }
                }
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }    
    
    /**
     * 置嵌套表宏控件对应的cws_status为STATUS_DONE
     * @param formCode
     * @param cwsId
     * @return
     */
    public static int updateStatus(String formCode, long cwsId, int status) {
    	// 判断formCode是否为副模块，则需取出主模块的formCode
    	ModuleSetupDb msd = new ModuleSetupDb();
    	msd = msd.getModuleSetupDb(formCode);
    	if (!formCode.equals(msd.getString("form_code"))) {
    		formCode = msd.getString("form_code");
    	}
    	
    	int ret = 0;
		String sql = "update " + FormDb.getTableName(formCode) + " set cws_status=" + status + " where cws_id=" + StrUtil.sqlstr(String.valueOf(cwsId));
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ret = jt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String getFlowTypeCode() {
        return flowTypeCode;
    }

    @Override
    public String getCwsId() {
        return cwsId;
    }

    @Override
    public int getFlowId() {
        return flowId;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public String getCreator() {
        return creator;
    }

    private String unitCode = DeptDb.ROOTCODE;

    @Override
    public long getIdentifier() {
        return flowId;
    }

    private boolean loaded;
    private String flowTypeCode;
    private String cwsId;
    private String creator;
    
    public int getStatus() {
		return status;
	}
    
    public static String getStatusDesc(int status) {
        String r;
        switch(status) {
          case STATUS_DRAFT: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED); break;
          case STATUS_NOT: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED); break;
          case STATUS_DONE: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED); break;
          case STATUS_DISCARD: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED); break;
          case STATUS_REFUSED: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED); break;
          case STATUS_TEMP: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NONE); break;
          case STATUS_DELETED: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DELETED); break;
          default: r = WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED);
        }
        return r;
    }

    public static String getCwsFlagDesc(int cwsFlag) {
        String r;
        switch(cwsFlag) {
            case 0: r = "未冲抵"; break;
            case 1: r = "已冲抵"; break;
            default: r = "未冲抵";
        }
        return r;
    }

    /**
     * 保存历史记录
     * @param userName
     * @param logType 类型
     * @param fdao
     * @return
     * @throws ErrMsgException
     */
    public static boolean log(String userName, int logType, FormDAO fdao) throws ErrMsgException {
        StringBuilder fds = new StringBuilder();
        StringBuilder str = new StringBuilder();
        Vector<FormField> fields = fdao.getFields();
        for (FormField ff : fields) {
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName());
                str = new StringBuilder("?");
            } else {
                fds.append(",").append(ff.getName());
                str.append(",?");
            }
        }

        String logTable = FormDb.getTableNameForLog(fdao.getFormDb().getCode());
        String sql = "insert into " + logTable + " (flowId, cws_creator, cws_id, cws_order, " + fds + ",flowTypeCode,unit_code,cws_log_user,cws_log_type,cws_log_date,cws_log_id) values (?,?,?,?," + str + ",?,?,?,?,?,?)";
        boolean re = false;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, fdao.getFlowId());
            ps.setString(2, fdao.getCreator());
            ps.setString(3, fdao.getCwsId());
            ps.setInt(4, 0); // fdao.getCwsOrder());
            int k = 5;
            for (FormField ff : fields) {
                ff.createDAOVisualForLog(ps, k, ff);
                k++;
            }
            String curTime = "" + System.currentTimeMillis();
            ps.setString(k, curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
            ps.setString(k+1, fdao.getUnitCode());

            ps.setString(k+2, userName);
            ps.setInt(k+3, logType);
            Timestamp ts = new Timestamp(new java.util.Date().getTime());
            ps.setTimestamp(k+4, ts);
            ps.setLong(k+5, fdao.getId());
            re = conn.executePreUpdate()==1;

            ps.close();

            long logId = -1;
            sql = "select id from " + logTable + " where cws_creator=? and flowTypeCode=? and cws_log_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, fdao.getCreator());
            ps.setString(2, curTime);
            ps.setInt(3, logType);
            ResultSet rs = conn.executePreQuery();
            if (rs.next()) {
                logId = rs.getLong(1);
            }

            com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
            String strVersion = StrUtil.getNullStr(oaCfg.get("version"));
            double version = StrUtil.toDouble(strVersion, -1);
            if (version>=5.0) {
                // 暂不支持删除和创建日志
                if (logType==FormDAOLog.LOG_TYPE_EDIT) {
                    // 与上一条记录比较差异
                    Vector<FormField> v = fdao.getFormDb().getFields();
                    JdbcTemplate jt = new JdbcTemplate();
                    sql = "select * from " + FormDb.getTableNameForLog(fdao.getFormDb().getCode()) + " where cws_log_id = '" + fdao.getId() + "' and id < " + logId + " order by id desc";
                    ResultIterator riPrev = jt.executeQuery(sql,1,1);
                    if (riPrev.hasNext()){
                        ResultRecord rrPrev = (ResultRecord)riPrev.next();
                        //比较字段是否被修改
                        for (FormField ff : v) {
                            int fieldType = ff.getFieldType();

                            boolean isEquals = true;
                            String strValOld = "";
                            String val = StrUtil.getNullStr(fdao.getFieldValue(ff.getName()));
                            if (fieldType == FormField.FIELD_TYPE_FLOAT ||
                                    fieldType == FormField.FIELD_TYPE_DOUBLE
                                    || fieldType == FormField.FIELD_TYPE_PRICE) {
                                double valOld = rrPrev.getDouble(ff.getName());
                                double doubleVal = StrUtil.toDouble(val);
                                if (doubleVal != valOld) {
                                    isEquals = false;
                                    strValOld = String.valueOf(valOld);
                                }
                            }else {
                                strValOld = StrUtil.getNullStr(rrPrev.getString(ff.getName()));
                                isEquals = StrUtil.getNullStr(val).equals(strValOld);
                            }

                            if (!isEquals) {
                                String logFormCode = "module_log";
                                FormDb fd = new FormDb(logFormCode);
                                com.redmoon.oa.visual.FormDAO fdaoLog = new com.redmoon.oa.visual.FormDAO(fd);
                                fdaoLog.setFieldValue("log_type", String.valueOf(logType));
                                fdaoLog.setFieldValue("log_date", DateUtil.format(ts, "yyyy-MM-dd HH:mm:ss"));
                                fdaoLog.setFieldValue("log_id", String.valueOf(logId));
                                fdaoLog.setFieldValue("form_code", fdao.getFormDb().getCode());
                                fdaoLog.setFieldValue("form_name", fdao.getFormDb().getName());
                                fdaoLog.setFieldValue("module_id", String.valueOf(fdao.getId()));
                                fdaoLog.setFieldValue("user_name", userName);
                                fdaoLog.setFieldValue("field_name", ff.getName());
                                fdaoLog.setFieldValue("field_title", ff.getTitle());
                                fdaoLog.setFieldValue("new_value", val);
                                fdaoLog.setFieldValue("old_value", strValOld);
                                fdaoLog.setCreator(userName);
                                fdaoLog.create();
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            LogUtil.getLog(FormDAO.class).error("log:" + e.getMessage());
        }
        finally {
            conn.close();
        }
        return re;
    }

    public void setStatus(int status) {
		this.status = status;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
    public long getId() {
		return id;
	}

	public void setCwsQuoteId(long cwsQuoteId) {
		this.cwsQuoteId = cwsQuoteId;
	}

	@Override
    public long getCwsQuoteId() {
		return cwsQuoteId;
	}

	public void setCwsProgress(int cwsProgress) {
		this.cwsProgress = cwsProgress;
	}

	public int getCwsProgress() {
		return cwsProgress;
	}
	
	@Override
    public int getCwsFlag() {
		return cwsFlag;
	}

    @Override
    public int getCwsStatus() { return status; }

	private int status;
	
	private long id;

	/**
	 * 自动冲抵时引用的记录的ID
	 */
	private long cwsQuoteId;
	
    /**
     * 进度
     */
    private int cwsProgress = 0;

    public void setCwsFlag(int cwsFlag) {
        this.cwsFlag = cwsFlag;
    }

    /**
     * 冲抵状态
     */
    private int cwsFlag = 0;

    private Date cwsCreateDate;
    private Date cwsModifyDate;
    private Date cwsFinishDate;

    @Override
    public String getCwsQuoteForm() {
        return cwsQuoteForm;
    }

    public void setCwsQuoteForm(String cwsQuoteForm) {
        this.cwsQuoteForm = cwsQuoteForm;
    }

    /**
     * 引用记录的表单编码
     */
    private String cwsQuoteForm;

    public Date getCwsCreateDate() {
        return cwsCreateDate;
    }

    public void setCwsCreateDate(Date cwsCreateDate) {
        this.cwsCreateDate = cwsCreateDate;
    }

    public Date getCwsModifyDate() {
        return cwsModifyDate;
    }

    public void setCwsModifyDate(Date cwsModifyDate) {
        this.cwsModifyDate = cwsModifyDate;
    }

    public Date getCwsFinishDate() {
        return cwsFinishDate;
    }

    public void setCwsFinishDate(Date cwsFinishDate) {
        this.cwsFinishDate = cwsFinishDate;
    }
}
