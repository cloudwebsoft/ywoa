package com.redmoon.oa.flow;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudweb.oa.api.IObsService;
import com.cloudweb.oa.cache.FlowFormDaoCache;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.PreviewUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.Pdf2Html;
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
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;

import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;

/**
 * <p>Title: </p>
 *
 * <p>Description:  对表单中的数据进行存储
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

    String connname = Global.getDefaultDB();

    FormDb fd;

    /**
     * 存放字段的值
     */
    Map<String, FormField> fieldsMap;

    /**
     * 临时记录
     */
    public static final int STATUS_TEMP = -1;
    
    /**
     * 保存的草稿
     */
    public static final int STATUS_DRAFT = -5;
    
    /**
     * 流程处理中，流程尚未走完
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
        setFields(fd.getFields());
        this.fd = fd;
        load();
    }

    public FormDAO getFormDAO(int flowId, FormDb fd) {
        return new FormDAO(flowId, fd);
    }

    public FormDAO getFormDAOByCache(int flowId, FormDb fd) {
        FlowFormDaoCache flowFormDaoCache = SpringUtil.getBean(FlowFormDaoCache.class);
        return (FormDAO)flowFormDaoCache.getFormDao(flowId, fd.getCode());
    }
    
    @Override
    public FormDb getFormDb() {
    	return fd;
    }

    public void setFields(Vector<FormField> fields) {
        this.fields = fields;
        fieldsMap = new HashMap<>();
        for (FormField ff : fields) {
            fieldsMap.put(ff.getName(), ff);
        }
    }

    @Override
    public Vector<FormField> getFields() {
        return fields;
    }

    @Override
    public FormField getFormField(String fieldName) {
        if (fieldsMap == null) {
            for (FormField ff : fields) {
                if (ff.getName().equals(fieldName)) {
                    return ff;
                }
            }
            return null;
        } else {
            FormField ff = fieldsMap.get(fieldName);
            if (ff != null) {
                return ff;
            } else {
                for (FormField field : fields) {
                    if (field.getName().equals(fieldName)) {
                        return field;
                    }
                }
                return null;
            }
        }

       /* for (FormField ff : fields) {
            if (ff.getName().equals(name)) {
                return ff;
            }
        }
        return null;*/
    }

    public Map<String, FormField> getFieldsMap() {
        return fieldsMap;
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
            LogUtil.getLog(getClass()).error(e);
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
        // LogUtil.getLog(getClass()).info("load:" + fds);
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
                            } else if (ff.getFieldType()== FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT
                                    || ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
                                // 防止显示为科学计数法
                                ff.setValue(NumberUtil.toString(rs.getDouble(k)));
                                // 使用wasNull()方法来判断最新一次get数据是否为空
                                if (rs.wasNull()) {
                                    ff.setValue(null);
                                }
                            }
                            else {
                                ff.setValue(rs.getString(k));
                            }

                            fieldsMap.put(ff.getName(), ff);
                        } catch (SQLException e) {
                            // 以免出现如下问题：load:Value '0000-00-00' can not be represented as java.sql.Timestamp
                            LogUtil.getLog(getClass()).error("load1:" + e.getMessage());
                            LogUtil.getLog(getClass()).error(e);
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
                    cwsId = StrUtil.getNullStr(rs.getString(k+6));
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
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
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
                // LogUtil.getLog(getClass()).info(ff.getName() + " getDefaultValue=" + ff.getDefaultValue());
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
            LogUtil.getLog(getClass()).error("create:" + StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
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
        FormField ff = getFormField(fieldName);
        if (ff == null) {
            LogUtil.getLog(getClass()).error(fieldName + " 不存在");
            throw new IllegalArgumentException(fieldName + " 不存在");
        }
        ff.setValue(value);
        /*
        boolean isFound = false;
        for (FormField ff : fields) {
            if (ff.getName().equals(fieldName)) {
                // LogUtil.getLog(getClass()).info(" ff.getName()=" + ff.getName() + " fieldName=" + fieldName + " value=" + value);
                ff.setValue(value);
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            LogUtil.getLog(getClass()).error("字段: " + fieldName + " 不存在");
            throw new IllegalArgumentException("字段: " + fieldName + " 不存在");
        }*/
    }

    public void setFieldValue(String fieldName, Object value) {
        FormField ff = getFormField(fieldName);
        if (ff == null) {
            LogUtil.getLog(getClass()).error(fieldName + " 字段不存在");
            throw new IllegalArgumentException(fieldName + " 字段不存在");
        }
        if (value instanceof String) {
            ff.setValue((String)value);
        }
        else if (value instanceof Date) {
            if (ff.getType().equals(FormField.TYPE_DATE)) {
                ff.setValue(DateUtil.format((Date)value, "yyyy-MM-dd"));
            } else {
                ff.setValue(DateUtil.format((Date)value, "yyyy-MM-dd HH:mm:ss"));
            }
        }
        else {
            ff.setValue(String.valueOf(value));
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
            if (re) {
                // 刷新缓存
                FlowFormDaoCache flowFormDaoCache = SpringUtil.getBean(FlowFormDaoCache.class);
                flowFormDaoCache.refreshSave(this);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            conn.close();
        }
        return re;
    }

    public static boolean generateImage(String imgStr, String path) {
        if (imgStr == null) {
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // 解密
            byte[] b = decoder.decodeBuffer(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            LogUtil.getLog(FormDAO.class).error(e);
            return false;
        }
    }

    public String convertBase64ToImgFile(String content, String filePath, int docId) {
        String contentTmp = content;
        Parser parser;
        try {
            parser = new Parser(content);
            parser.setEncoding("utf-8");//
            TagNameFilter filter = new TagNameFilter("img");
            NodeList nodes = parser.parse(filter);
            if (nodes == null || nodes.size() == 0) {
                return content;
            }
            else {
                StringBuilder sb = new StringBuilder();
                int lastNodeEnd = 0;
                for (int k=0; k<nodes.size(); k++) {
                    ImageTag node = (ImageTag) nodes.elementAt(k);

                    // image/png;base64,
                    String imgUrl = node.getImageURL();
                    int p = imgUrl.indexOf(",");
                    if (p != -1) {
                        String base64img = imgUrl.substring(p + 1);
                        int a = imgUrl.indexOf("/");
                        int b = imgUrl.indexOf(";");
                        String ext = imgUrl.substring(a + 1, b);

                        String diskName = FileUpload.getRandName() + "." + ext;
                        boolean re = generateImage(base64img, Global.getRealPath() + filePath + "/" + diskName);
                        if (re) {
                            node.setImageURL(Global.getFullRootPath(SpringUtil.getRequest()) + filePath + "/" + diskName);
                            int s = node.getStartPosition();
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, s);
                            c += node.toHtml();
                            sb.append(c);
                            lastNodeEnd = e;

                            IFileService fileService = SpringUtil.getBean(IFileService.class);
                            fileService.upload(Global.getRealPath() + filePath + "/" + diskName, filePath, diskName);

                            Document doc = new Document();
                            doc = doc.getDocument(docId);

                            File f = new File(Global.getRealPath() + filePath + "/" + diskName);
                            long size = f.length();
                            Attachment att = new Attachment();
                            att.setDocId(docId);
                            att.setName(diskName);
                            att.setDiskName(diskName);
                            att.setVisualPath(filePath);
                            att.setSize(size);
                            att.setPageNum(1);
                            att.setOrders(0);
                            att.setCreateDate(new java.util.Date());
                            att.setFlowId((int)doc.getFlowId());
                            att.create();
                        }
                    }
                    else {
                        int e = node.getEndPosition();
                        String c = contentTmp.substring(lastNodeEnd, e);
                        sb.append(c);
                        lastNodeEnd = e;
                    }
                }
                sb.append(StringUtils.substring(contentTmp, lastNodeEnd));
                content = sb.toString();
            }

        } catch (ParserException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return content;
    }

    public boolean save(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        WorkflowDb wd = new WorkflowDb();
        wd = wd.getWorkflowDb(flowId);
        StringBuilder fds = new StringBuilder();
        Iterator<FormField> ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = ir.next();
            // 辅助字段在此不保存，跳过
            if (ff.isHelper()) {
                continue;
            }

            // 用不到了，因为ueditor支持拷贝或截图后自动上传
            /*if (ff.getType().equals(FormField.TYPE_MACRO)) {
                if ("macro_ueditor".equals(ff.getMacroType())) {
                    // 如果是富文本宏控件，则转换
                    String content = StrUtil.getNullStr(ff.getValue());
                    content = convertBase64ToImgFile(content, getVisualPath(), wd.getDocId());
                }
            }*/

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
        
        // LogUtil.getLog(getClass()).info("save: sql=" + sql);

        MacroCtlMgr mm = new MacroCtlMgr();
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField macroField = ir.next();
            if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.
                                                     getMacroType());
                LogUtil.getLog(getClass()).info("save: mu.getNestType()=" + macroField.
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
            LogUtil.getLog(getClass()).info("save: v.size=" + v.size() + " re=" + re + " flowId=" + flowId);
            // 先处理附件，以便于宏控件getValueForSave时从FileUpload中获取diskname
            int docId = wd.getDocId();
            if (fu.getRet() == FileUpload.RET_SUCCESS && v.size()>0) {
                Document doc = new Document();
                doc = doc.getDocument(docId);
                DocContent dc = doc.getDocContent(1);
                
                Map<String, String> attMap = new HashMap<String, String>();
            	java.util.Vector<IAttachment> attachments = doc.getAttachments(1);
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
                    LogUtil.getLog(getClass()).error(e);
                }
                if(os==null || DeviceType.MOBILE.equals(os.getDeviceType())) {
                    isMobile = true;
                }

                String vpath = getVisualPath();
                com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
                boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
                boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");

                SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
                IFileService fileService = SpringUtil.getBean(IFileService.class);
                FileInfo fi;
                Pattern pat1 = Pattern.compile("^att([0-9]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                int orders = dc.getAttachmentMaxOrders() + 1;
                Privilege privilege = new Privilege();
                for (FileInfo fileInfo : v) {
                    fi = fileInfo;

                    File f = fileService.write(fi, vpath, true, false);
                    // 检查是否已存在同名文件，因为如果是手机端操作，则有可能是在提交后条件判断时先保存了一下，然后选择人员后再次提交，所以文件会保存两次
                    // 而在PC端，如果表单中有Office在线编辑宏控件，保存时附件表中也会出现同名文件，此时不应跳过而应保存附件，因在getValueForSave中会删除旧的附件，保存新的附件
                    if (isMobile) {
                        if (attMap.containsKey(fi.getName())) {
                            continue;
                        }
                    }

                    Attachment att = new Attachment();
                    att.setDocId(docId);
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setVisualPath(vpath);
                    att.setPageNum(pageNum);
                    att.setOrders(orders);

                    // 过滤掉默认生成的name值，如：att1、att2、...
                    Matcher m1 = pat1.matcher(fi.fieldName);
                    if (!m1.find()) {
                        att.setFieldName(fi.fieldName);
                    }

                    att.setCreator(privilege.getUser(request));
                    att.setSize(fi.getSize());
                    att.setFlowId(flowId);
                    re = att.create();

                    String previewfile = Global.getRealPath() + vpath + "/" + fi.getDiskName();
                    String ext = StrUtil.getFileExt(att.getDiskName());
                    if (canOfficeFilePreview) {
                        if (FileUtil.isOfficeFile(att.getDiskName())) {
                            PreviewUtil.createOfficeFilePreviewHTML(previewfile);
                        }
                    }
                    if (canPdfFilePreview) {
                        if ("pdf".equals(ext)) {
                            Pdf2Html.createPreviewHTML(previewfile);
                        }
                    }

                    if (sysProperties.isObjStoreEnabled()) {
                        // 删除本地文件
                        if (!sysProperties.isObjStoreReserveLocalFile()) {
                            f.delete();
                        }
                    }

                    orders++;
                }

                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(docId, pageNum);
            }

            // 对可视化宏控件的图片排序
            String imgOrders = fu.getFieldValue("uploaderImgOrders");
            String[] orderArr = StrUtil.split(imgOrders, ",");
            if (orderArr!=null) {
                for (int i = 0; i< orderArr.length; i++) {
                    String imgOrder = orderArr[i];
                    if (NumberUtil.isNumeric(imgOrder)) {
                        int imgId = StrUtil.toInt(imgOrder);
                        Attachment att = new Attachment(imgId);
                        att.setOrders(i);
                        att.save();
                    }
                    else {
                        Attachment att = new Attachment();
                        att = att.getAttachmentByName(docId, imgOrder);
                        if (att!=null) {
                            att.setOrders(i);
                            att.save();
                        }
                    }
                }
            }

            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);

            // LogUtil.getLog(getClass()).info("save: fields=" + fields + " flowId=" + flowId);

            PreparedStatement ps = conn.prepareStatement(sql);
            ir = fields.iterator();
            int k = 1;
            while (ir.hasNext()) {
                FormField ff = ir.next();
                // 辅助字段在此不保存，跳过
                if (ff.isHelper()) {
                    continue;
                }
                ff.saveDAO(this, ps, k, flowId, fd, fu);
                // LogUtil.getLog(getClass()).info("save: field name=" + ff.getName() + " value=" + ff.getValue() + " flowId=" + flowId);
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
                // 刷新缓存
                FlowFormDaoCache flowFormDaoCache = SpringUtil.getBean(FlowFormDaoCache.class);
                flowFormDaoCache.refreshSave(this);

                ir = fields.iterator();
                while (ir.hasNext()) {
                    FormField macroField = ir.next();
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
            LogUtil.getLog(getClass()).error("save:" + StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
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
        FormField ff = getFormField(fieldName);
        if (ff!=null) {
            return StrUtil.getNullStr(ff.getValue());
        } else {
            return null;
        }

        /*for (FormField ff : fields) {
            if (fieldName.equals(ff.getName())) {
                return StrUtil.getNullStr(ff.getValue());
            }
        }
        return null;*/
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
                // 刷新缓存
                FlowFormDaoCache flowFormDaoCache = SpringUtil.getBean(FlowFormDaoCache.class);
                flowFormDaoCache.refreshDel(this);

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
                            mu.getIFormMacroCtl().onDelNestCtlParent(macroField, String.valueOf(id));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
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
            // LogUtil.getLog(getClass()).info("countsql=" + countsql);
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
                    // LogUtil.getLog(getClass()).info("listResult: id=" + rs.getInt(1));
                    FormDAO fdao = getFormDAOByCache(rs.getInt(1), fd);
                    result.addElement(fdao);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        return lr;
    }

    /**
     * 根据SQL语句列出表单编码为formCode的分页记录
     * @param formCode String 表单编码
     * @param listSql String SQL语句
     * @param objectParams Object[] 参数
     * @param curPage int 当前页码
     * @param pageSize int 每页记录数
     * @return ListResult
     * @throws ErrMsgException
     */
    public ListResult listResult(String formCode, String listSql, Object[] objectParams, int curPage, int pageSize) throws
            ErrMsgException {
        Vector<FormDAO> result = new Vector<>();
        ListResult lr = new ListResult();
        lr.setResult(result);

        License lic = License.getInstance();
        // if (formCode.equals("sales_customer") && lic.isSolutionVer() && !lic.canUseSolution(License.SOLUTION_CRM)) {
        // 平台版才可以用CRM模块
        if ("sales_customer".equals(formCode) && !lic.isPlatformSrc()) {
            LogUtil.getLog(getClass()).error("listResult:平台版才能使用CRM模块！");
            return lr;
        }
        FormMgr fm = new FormMgr();
        FormDb formDb = fm.getFormDb(formCode);

        try {
            // 取得总记录条数
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(listSql, objectParams, curPage, pageSize);
            lr.setTotal(ri.getTotal());
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                result.addElement(getFormDAOByCache(rr.getInt(1), formDb));
            }
        } catch (SQLException e) {
            DebugUtil.e(getClass(), "listResult", listSql);
            LogUtil.getLog(getClass()).error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
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

                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);

                do {
                    FormDAO fdao = getFormDAOByCache(rs.getInt(1), fd);
                    result.addElement(fdao);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
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
     * 更新表单选择宏控件需冲抵的记录
     * @param jsonDesc
     * @param ff
     * @return
     * @throws ErrMsgException
     */
    public static int updateFlagForModuleFieldSelectCtl(com.alibaba.fastjson.JSONObject jsonDesc, FormField ff) throws ErrMsgException {
        try {
            String sourceFormCode = jsonDesc.getString("sourceFormCode");
            String idField = jsonDesc.getString("idField");
            String sql = "select id,cws_flag from ft_" + sourceFormCode + " where " + idField + "=?";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{ff.getValue()});
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                if (rr.getInt(2) == 1) {
                    throw new ErrMsgException("表单：" + sourceFormCode + "中的记录" + idField + "=" + ff.getValue() + "已被冲抵");
                }
            }
            sql = "update ft_" + sourceFormCode + " set cws_flag=1 where " + idField + "=?";
            return jt.executeUpdate(sql, new Object[]{ff.getValue()});
        } catch (SQLException e) {
            LogUtil.getLog(FormDAO.class).error(e);
        }
        return 0;
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
		String sql = "select cws_quote_id from " + FormDb.getTableName(destFormCode) + " where cws_id='" + mainFormFdaoId + "'";
		String sqlUpdate = "update " + FormDb.getTableName(sourceFormCode) + " set cws_flag=" + flagVal + " where id=?";
		
		String sqlGetCwsId = "select cws_id,cws_parent_form from " + FormDb.getTableName(sourceFormCode) + " where id=?";
		String parentForm = "";
		boolean isCwsParentFormEqual = true;
		List<String> listCwsId = new ArrayList<>();
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = ri.next();
				int quoteId = rr.getInt(1);
				ret = jt.executeUpdate(sqlUpdate, new Object[]{(long) quoteId});
				
				// 20171105 fgf 判断被冲抵的记录的cws_id是否都一样，如果是，则说明是冲抵了某个主表中的嵌套表记录
                // 20220107 有可能从多个主表记录中去拉单，但这些记录来源是同一个表单，所以需加上对于cws_parent_form的判断，而不是针对cws_id去判断
				if (isCwsParentFormEqual) {
					ResultIterator riCwsId = jt.executeQuery(sqlGetCwsId, new Object[]{(long) quoteId});
					if (riCwsId.hasNext()) {
						ResultRecord rrCwsId = riCwsId.next();
						String myCwsId = StrUtil.getNullStr(rrCwsId.getString(1));
                        listCwsId.add(myCwsId);
						String myParentForm = StrUtil.getNullStr(rrCwsId.getString(2));
						if ("".equals(parentForm)) {
                            parentForm = myParentForm;
						}
						else {
							if (!parentForm.equals(myParentForm)) {
                                isCwsParentFormEqual = false;
							}
						}
					}
				}
			}
						
			// 20171105 fgf 如果源表单是个子表，且其主表所对应的所有记录均已被冲抵，则自动冲抵源表单的主表单
			if (isCwsParentFormEqual) {
				// 查询源表单是否从属于某个主表
				ModuleRelateDb mrd = new ModuleRelateDb();
				Vector v = mrd.getModuleReverseRelated(sourceFormCode);
				for (String myCwsId : listCwsId) {
                    String sqlIsAllAgainst = "select id from " + FormDb.getTableName(sourceFormCode) + " where cws_flag=" + FLAG_AGAINST_NO + " and cws_id='" + myCwsId + "'";
                    for (Object o : v) {
                        mrd = (ModuleRelateDb) o;
                        String formCode = mrd.getString("code");
                        String relateField = mrd.getString("relate_field");

                        // 检查对应的主表中的所有嵌套表记录是否均已被冲抵
                        ResultIterator riIsAllAgainst = jt.executeQuery(sqlIsAllAgainst);
                        if (riIsAllAgainst.size() == 0) {
                            // 如果均已被冲抵，则自动冲抵对应的主表中的记录
                            String sqlUpdateAgainst = "update " + FormDb.getTableName(formCode) + " set cws_flag=" + FLAG_AGAINST_YES + " where " + relateField + "=" + myCwsId;
                            jt.executeUpdate(sqlUpdateAgainst);
                        }
                    }
                }
			}
		} catch (SQLException e) {
            LogUtil.getLog(FormDAO.class).error(e);
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
		String sql = "update " + FormDb.getTableName(formCode) + " set cws_status=" + status + " where cws_id='" + cwsId + "'";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ret = jt.executeUpdate(sql);
		} catch (SQLException e) {
            LogUtil.getLog(FormDAO.class).error(e);
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
    private String cwsId = com.redmoon.oa.visual.FormDAO.CWS_ID_NONE;
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

            // 取得刚插入的记录的ID
            long logId = SQLFilter.getLastId(conn, logTable);

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
                                FormDb fd = new FormDb();
                                fd = fd.getFormDb(logFormCode);
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
            LogUtil.getLog(FormDAO.class).error(e);
        }
        finally {
            conn.close();
        }
        return re;
    }

    /**
     * 取出结果存于List，与list方法一样，只是结果由Vector改为了List
     * @param formCode
     * @param sql
     * @return
     * @throws ErrMsgException
     */
    public List<FormDAO> selectList(String formCode, String sql) throws ErrMsgException {
        ResultSet rs;
        List<FormDAO> result = new ArrayList<>();
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return result;
            } else {
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                while (rs.next()) {
                    result.add(getFormDAOByCache(rs.getInt(1), fd));
                }
            }
        } catch (SQLException e) {
            DebugUtil.e(getClass(), "listResult", e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }

        return result;
    }

    public List<FormDAO> selectList(String formCode, String sql, Object[] arr) throws ErrMsgException {
        List<FormDAO> result = new ArrayList<>();
        try {
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, arr);
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                result.add(getFormDAOByCache(rr.getInt(1), fd));
            }
        } catch (SQLException e) {
            DebugUtil.e(getClass(), "listResult", e.getMessage());
            throw new ErrMsgException("数据库出错！");
        }
        return result;
    }

    public List<com.redmoon.oa.visual.FormDAO> listNest(String nestFormCode) throws ErrMsgException {
        com.redmoon.oa.visual.FormDAO dao = new com.redmoon.oa.visual.FormDAO();
        return dao.selectList(nestFormCode, "select id from ft_" + nestFormCode + " where cws_id='" + id + "' and cws_parent_form=" + StrUtil.sqlstr(fd.getCode()));
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

    @Override
    public String getFormCode() {
        if (fd != null) {
            return fd.getCode();
        }
        else {
            return null;
        }
    }

    @Override
    public Vector<IAttachment> getAttachments() {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Document doc = new Document();
        doc = doc.getDocument(wf.getDocId());
        return doc.getAttachments(1);
    }

    public double getDoubleValue(String fieldName) {
        return Double.parseDouble(getFieldValue(fieldName));
    }

    public double getDoubleValue(String fieldName, double defaultVal) {
        return StrUtil.toDouble(getFieldValue(fieldName), defaultVal);
    }

    public int getIntValue(String fieldName) {
        return Integer.parseInt(getFieldValue(fieldName));
    }

    public int getIntValue(String fieldName, int defaultVal) {
        return StrUtil.toInt(getFieldValue(fieldName), defaultVal);
    }

    public long getLongValue(String fieldName) {
        return Long.parseLong(getFieldValue(fieldName));
    }

    public long getLongValue(String fieldName, long defaultVal) {
        return StrUtil.toLong(getFieldValue(fieldName), defaultVal);
    }

    public float getFloatValue(String fieldName) {
        return Float.parseFloat(getFieldValue(fieldName));
    }

    public float getFloatValue(String fieldName, int defaultVal) {
        return StrUtil.toFloat(getFieldValue(fieldName), defaultVal);
    }
}
