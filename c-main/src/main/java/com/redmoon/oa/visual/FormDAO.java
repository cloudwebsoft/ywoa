package com.redmoon.oa.visual;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.Form;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.base.*;
import com.redmoon.oa.db.SQLUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.fileark.VideoTag;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.tools.Pdf2htmlEXUtil;
import com.redmoon.oa.util.RequestUtil;

import org.apache.log4j.*;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * <p>Title: </p>
 *
 * <p>Description:  对表单中的数据进行存储
 * flowTypeCode作为保留字段，用以记录create的时候的时间值，根据该字段来得到插入记录后，该记录的id
 * 这里的id为随机插入，无法再通过SequenceMgr.nextID()来获取，因为表也是随机生成的
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

    /**
     * 存放字段的值
     */
    Map<String, FormField> fieldsMap;

    String tableName;
    long id;
    Logger logger = Logger.getLogger(FormDAO.class.getName());

    String connname = Global.getDefaultDB();

    String formCode = "";

    private FormDb formDb;

    public static final int NONEFLOWID = -1;

    Vector<Attachment> attachments;

    public static final String FormDAO_NEW_ID = "FormDAO_NEW_ID";
    
    /*
     * 用于嵌套表格2在智能模块添加时一起创建，用作临时的cws_id
     */
    public static final int TEMP_CWS_ID = -1;
    
    /**
     * 用于嵌套表格2，当插入记录时，在父表单中插入hidden input记录ID
     */
    public static final String NAME_TEMP_CWS_IDS = "tempCwsId";

    private long flowId = NONEFLOWID;
    
    /**
     * 用于嵌套表格2拉单时，将源表单的ID映射给主表单
     */
    public static final String FormDAO_ID = "FormDAO_ID";

    public FormDAO() {

    }

    public FormDAO(FormDb fd) {
        tableName = fd.getTableNameByForm();
        fields = fd.getFields();
        formCode = fd.getCode();
        attachments = new Vector<>();
        fieldsMap = new HashMap<String, FormField>();
        formDb = fd;
    }

    public FormDAO(long id, FormDb fd) {
        this.id = id;
        tableName = fd.getTableNameByForm();
        fields = fd.getFields();
        formCode = fd.getCode();
        attachments = new Vector<>();
        fieldsMap = new HashMap<String, FormField>();
        formDb = fd;
        load();
    }
    
    public String getTableName() {
    	return tableName;
    }

    @Override
    public FormDb getFormDb() {
		// 20200512 优化
        if (formDb!=null) {
            return formDb;
        }
        else {
            formDb = new FormDb();
            formDb = formDb.getFormDb(formCode);
            return formDb;
        }
    }
    
    public void setFields(Vector<FormField> fields) {
        this.fields = fields;
        fieldsMap = new HashMap<String, FormField>();
        for (FormField ff : fields) {
            fieldsMap.put(ff.getName(), ff);
        }
    }

    @Override
    public Vector<FormField> getFields() {
        return fields;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCwsId(String cwsId) {
        this.cwsId = cwsId;
    }

    public void setCwsOrder(int cwsOrder) {
        this.cwsOrder = cwsOrder;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public void setFlowId(long flowId) {
        this.flowId = flowId;
    }

    public Map<String, FormField> getFieldsMap() {
        return fieldsMap;
    }


    /**
     * 取得对应于cwsId的记录的ID，用于宏控件NestFormCtl
     * @param cwsId int
     * @return int
     */
    public int getIDByCwsId(int cwsId) {
        JdbcTemplate jt = new JdbcTemplate();
        String sql = "select id from " + tableName + " where cws_id=?";
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {cwsId});
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            return -2;
        }
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord)ri.next();
            return rr.getInt(1);
        }
        return -1;
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
        Conn conn = new Conn(connname);
        String sql = "select " + fds + ",cws_creator,cws_id,cws_order,unit_code,flowId,cws_status,cws_quote_id,cws_flag,cws_progress,cws_parent_form,cws_create_date,cws_modify_date,cws_quote_form from " + tableName + " where id=?";
        ResultSet rs;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    int k = 1;
                    for (FormField ff : fields) {
                        try {
                            String val = null;
                            if (ff.getType().equals(FormField.TYPE_DATE)) {
                                java.sql.Date dt = rs.getDate(k);
                                val = DateUtil.format(dt, "yyyy-MM-dd");
                            } else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                Timestamp ts = rs.getTimestamp(k);
                                String d = "";
                                if (ts != null) {
                                    d = DateUtil.format(new Date(ts.getTime()), "yyyy-MM-dd HH:mm:ss");
                                }
                                val = d;
                            } else {
                                val = rs.getString(k);
                            }
                            ff.setValue(val);

                            fieldsMap.put(ff.getName(), ff);
                        } catch (SQLException e) {
                            // 以免出现如下问题：load:Value '0000-00-00' can not be represented as java.sql.Timestamp
                            logger.error("load1:" + e.getMessage());
                            e.printStackTrace();
                        }
                        k++;
                    }

                    creator = StrUtil.getNullStr(rs.getString(k));
                    cwsId = StrUtil.getNullStr(rs.getString(k+1));
                    cwsOrder = rs.getInt(k+2);
                    unitCode = rs.getString(k+3);
                    flowId = rs.getLong(k+4);
                    cwsStatus = rs.getInt(k+5);
                    cwsQuoteId = rs.getLong(k+6);
                    cwsFlag = rs.getInt(k+7);
                    cwsProgress = rs.getInt(k+8);
                    cwsParentForm = StrUtil.getNullStr(rs.getString(k+9));
                    cwsCreateDate = rs.getTimestamp(k+10);
                    cwsModifyDate = rs.getTimestamp(k+11);
                    cwsQuoteForm = StrUtil.getNullStr(rs.getString(k + 12));

                    loaded = true;
                    ps.close();

                    sql = "select id from visual_attach where visualId=? and formCode=?";
                    ps = conn.prepareStatement(sql);
                    ps.setLong(1, id);
                    ps.setString(2, formCode);
                    rs = conn.executePreQuery();
                    if (rs != null) {
                        while (rs.next()) {
                            attachments.addElement(new Attachment(rs.getInt(1)));
                        }
                    }
                    ps.close();
                }
            }
        }
        catch (SQLException e) {
            logger.error("load:" + StrUtil.trace(e));
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
    }

    public String getFieldValueRaw(String fieldName) {
        for (FormField ff : fields) {
            if (ff.getName().equals(fieldName)) {
                return ff.getValue();
            }
        }
        return null;
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
                    ff = field;
                    if (ff.getName().equals(fieldName)) {
                        return ff;
                    }
                }
                return null;
            }
        }
    }

    @Override
    public String getFieldValue(String fieldName) {
        if (fieldsMap==null || fieldsMap.size()==0) {
            for (FormField ff : fields) {
                if (ff.getName().equals(fieldName)) {
                    return StrUtil.getNullStr(ff.getValue());
                }
            }
            return null;
        }
        else {
            FormField ff = fieldsMap.get(fieldName);
            if (ff == null) {
                return null;
            }

            return StrUtil.getNullStr(ff.getValue());
        }
    }
    
    /**
     * 取得field值的显示字符串，用于nest_sheet_add_relate.jsp嵌套表格2添加时
     * @param request
     * @param fieldName
     * @return
     */
    public String getFieldHtml(HttpServletRequest request, String fieldName) {
    	RequestUtil.setFormDAO(request, this);
    	MacroCtlMgr mm = new MacroCtlMgr();
        for (FormField ff : fields) {
            // System.out.println(getClass() + " " + ff.getName() + " " + ff.getValue());
            if (ff.getName().equalsIgnoreCase(fieldName)) {
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        return mu.getIFormMacroCtl().converToHtml(request, ff, ff.getValue());
                    }
                } else {
                    return StrUtil.getNullStr(ff.getValue());
                }
            }
        }
        return "";
    }

    @Override
    public void setFieldValue(String fieldName, String value) {
        for (FormField ff : fields) {
            if (ff.getName().equalsIgnoreCase(fieldName)) {
                // LogUtil.getLog(getClass()).info("setFieldValue: ff.getName()=" + ff.getName() + " fieldName=" + fieldName + " value=" + value);
                ff.setValue(value);
                break;
            }
        }
    }

    /**
     * 自动创建一条新的表单记录，用于关联模块，表单型关联方式
     * @return boolean
     */
    public boolean createEmptyForm() {
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
        String sql = "insert into " + tableName + "  (flowId, " + fds + ",flowTypeCode,cws_id,cws_status) values (?," + str + ",?,?,?)";
        boolean re = false;

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, NONEFLOWID);
            int k = 2;
            for (FormField ff : fields) {
                ff.createDAO(ps, k, fields);
                // logger.info(ff.getName() + " getDefaultValue=" + ff.getDefaultValue());
                k++;
            }
            ps.setString(k, "" + System.currentTimeMillis());
            ps.setString(k+1, cwsId);
            ps.setInt(k+2, com.redmoon.oa.flow.FormDAO.STATUS_DONE);

            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
        return re;
    }

    /**
     * 创建记录，主要考虑用于脚本设计器中调用时比较方便，不会忘记三个必填的参数
     * @param creator 创建者
     * @param unitCode 创建者所在的单位，一般为root，集团版则为用户所在的单位
     * @param cwsId 关联其它模块的ID，可以为空
     * @return
     */
    public boolean create(String creator, String unitCode, String cwsId) throws SQLException {
    	this.creator = creator;
    	this.unitCode = unitCode;
    	this.cwsId = cwsId;
    	return create();
    }

    /**
     * 创建记录，用于导入数据时或选择模块数据时
     * @return boolean
     */
    public boolean create() throws SQLException {
        String fds = "";
        StringBuilder str = new StringBuilder();
        for (FormField ff : fields) {
            if ("".equals(fds)) {
                fds = ff.getName();
                str = new StringBuilder("?");
            } else {
                fds += "," + ff.getName();
                str.append(",?");
            }
        }
        String sql = "insert into " + tableName + "  (flowId, " + fds + ",flowTypeCode,cws_id,unit_code,cws_creator,cws_status,cws_quote_id,cws_flag,cws_progress,cws_parent_form,cws_create_date,cws_quote_form) values (?," + str + ",?,?,?,?,?,?,?,?,?,?,?)";
        boolean re;

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, flowId);
            int k = 2;
            for (FormField ff : fields) {
                ff.createDAOVisual(ps, k);
                k++;
            }
            String curTime = String.valueOf(System.currentTimeMillis());
            ps.setString(k, curTime);
            ps.setString(k+1, cwsId);
            ps.setString(k+2, unitCode);
            ps.setString(k+3, creator);
            ps.setInt(k+4, com.redmoon.oa.flow.FormDAO.STATUS_DONE);
            ps.setLong(k+5, cwsQuoteId);
            ps.setInt(k+6, cwsFlag);
            ps.setInt(k+7, cwsProgress);
            ps.setString(k+8, cwsParentForm);
            ps.setTimestamp(k+9, new Timestamp(new java.util.Date().getTime()));
            ps.setString(k+10, cwsQuoteForm);

            re = conn.executePreUpdate() == 1;
            
            ps.close();
            // 取出新建记录的ID
            if (re) {
                // 取得自动增长的ID
                id = SQLFilter.getLastId(conn, tableName);
/*                ResultSet rs = conn.executeQuery("select id from " +
                        tableName + " where flowTypeCode=" + StrUtil.sqlstr(curTime));
				if (rs != null && rs.next()) {
					id = rs.getLong(1);
				} */
            }
        }
        catch (SQLException e) {
            logger.error("create:" + StrUtil.trace(e));
            // e.printStackTrace();
            throw e;
        }
        finally {
            conn.close();
        }
        return re;
    }

    public boolean create(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        StringBuilder fds = new StringBuilder();
        StringBuilder str = new StringBuilder();
        for (FormField ff : fields) {
            // 辅助字段在此不保存，跳过
            if (ff.isHelper()) {
                continue;
            }
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName());
                str = new StringBuilder("?");
            } else {
                fds.append(",").append(ff.getName());
                str.append(",?");
            }
        }

        Privilege pvg = new Privilege();
        // 保存附件
        Vector<FileInfo> v = fu.getFiles();
        String vpath = getVisualPath();
        // 置保存路径
        String filepath = Global.getRealPath() + vpath;
        if (v.size() > 0) {
            fu.setSavePath(filepath);
            // 使用随机名称写入磁盘
            fu.writeFile(true);
        }

        if (formDb==null) {
            formDb = new FormDb();
            formDb = formDb.getFormDb(formCode);
        }

        String sql = "insert into " + tableName + " (flowId, cws_creator, cws_id, cws_order, " + fds + ",flowTypeCode,unit_code,cws_status,cws_parent_form,cws_create_date) values (?,?,?,?," + str + ",?,?,?,?,?)";
        boolean re;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, flowId);
            ps.setString(2, creator);
            ps.setString(3, cwsId);
            ps.setInt(4, cwsOrder);
            int k = 5;
            Iterator ir = fields.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField)ir.next();
                // 辅助字段在此不保存，跳过
                if (ff.isHelper()) {
                    continue;
                }
                ff.createDAOVisual(ps, k, fu, formDb);
                // logger.info("create:" + ff.getName() + " getValue=" + ff.getValue());
                k++;
            }

            String curTime = String.valueOf(System.currentTimeMillis());
            ps.setString(k, curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
            ps.setString(k+1, unitCode);
            ps.setInt(k+2, cwsStatus);
            ps.setString(k+3, cwsParentForm);
            ps.setTimestamp(k+4, new Timestamp(new java.util.Date().getTime()));
            re = conn.executePreUpdate() == 1;
            ps.close();

            // 取得自动增长的ID
            long visualId = SQLFilter.getLastId(conn, tableName);

            id = visualId;
            // 置新建的ID于request的Attribute
            request.setAttribute(FormDAO_NEW_ID, "" + id);

           // 保存嵌套表单中的信息
           ir = fields.iterator();
           MacroCtlMgr mm = new MacroCtlMgr();
           while (ir.hasNext()) {
               FormField macroField = (FormField) ir.next();
               if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                   MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.getMacroType());
                   // logger.info("create: mu.getNestType()=" + mu.getNestType());
                   if (mu.getNestType()!=MacroCtlUnit.NEST_TYPE_NONE) {
                       mu.getIFormMacroCtl().createForNestCtl(request, macroField, String.valueOf(visualId), creator, fu);
                   }
               }
           }

            // logger.info("create: visualId=" + visualId);
            // 处理附件
            if (re && fu.getRet() == FileUpload.RET_SUCCESS) {
                FileInfo fi = null;
                ir = v.iterator();
                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    Attachment att = new Attachment();
                    att.setFullPath(filepath + fi.getDiskName());
                    att.setVisualId(visualId);
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setVisualPath(vpath);
                    att.setFormCode(formCode);
                    att.setFieldName(fi.getFieldName());

                    att.setCreator(pvg.getUser(request));
                    att.setFileSize(fi.getSize());

                    re = att.create();

                    String previewfile=filepath + fi.getDiskName();
                    String ext = StrUtil.getFileExt(att.getDiskName());
                    if ("doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext)) {
                        com.redmoon.oa.fileark.Document.createOfficeFilePreviewHTML(previewfile);
                    }
                    else if ("pdf".equals(ext)) {
                        Pdf2htmlEXUtil.createPreviewHTML(previewfile);
                    }
                }
            }

            if (re) {
                ir = fields.iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            // 解析ueditor中的图片、视频，将visual_attach中的相应文件的visual_id置为记录的ID
                            if ("macro_ueditor".equals(mu.getCode())) {
                                String content = fu.getFieldValue(ff.getName());
                                List<String> al = new ArrayList<>();
                                Attachment att = new Attachment();
                                // 解析content，取出其中的图片对应的ID
                                try {
                                    Parser myParser;
                                    NodeList nodeList;
                                    myParser = Parser.createParser(content, "utf-8");

                                    PrototypicalNodeFactory pnf = new PrototypicalNodeFactory();
                                    pnf.registerTag(new VideoTag());
                                    myParser.setNodeFactory(pnf);

                                    NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
                                    NodeFilter videoFilter = new NodeClassFilter(VideoTag.class);

                                    OrFilter lastFilter = new OrFilter();
                                    lastFilter.setPredicates(new NodeFilter[]{imgFilter, videoFilter});
                                    nodeList = myParser.parse(lastFilter);
                                    Node[] nodes = nodeList.toNodeArray();
                                    for (Node anode : nodes) {
                                        if (anode instanceof ImageTag) {
                                            ImageTag imagenode = (ImageTag) anode;
                                            String url = imagenode.getImageURL();
                                            String ext = StrUtil.getFileExt(url).toLowerCase();
                                            // 如果地址完整
                                            if ("gif".equals(ext) || "png".equals(ext) ||
                                                    "jpg".equals(ext) || "jpeg".equals(ext)) {
                                                int p = url.lastIndexOf("/");
                                                String diskName = url.substring(p + 1);
                                                long tmpId = att.getTmpAttId(diskName);
                                                if (tmpId != -1) {
                                                    al.add(String.valueOf(tmpId));
                                                }
                                            }
                                        } else if (anode instanceof VideoTag) {
                                            VideoTag imagenode = (VideoTag) anode;
                                            String url = imagenode.getAttribute("src");
                                            int p = url.lastIndexOf("/");
                                            String diskName = url.substring(p + 1);
                                            long tmpId = att.getTmpAttId(diskName);
                                            if (tmpId != -1) {
                                                al.add(String.valueOf(tmpId));
                                            }
                                        }
                                    }
                                } catch (ParserException e) {
                                    LogUtil.getLog(StrUtil.class.getName()).error("create:" + e.getMessage());
                                }

                                Object[] tmpAttachIds = al.toArray();
                                for (Object tmpAttachId : tmpAttachIds) {
                                    att = new Attachment(StrUtil.toLong((String) tmpAttachId));
                                    att.setVisualId(visualId);
                                    att.save();
                                }
                            }
                        }
                    }
                }
            }
            
            if (re) {
/*            	FormDAO fdao = new FormDAO();
            	fdao = fdao.getFormDAO(visualId, fd);
                ir = fields.iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField)ir.next();
                    // System.out.println(getClass() + " " + ff.getName() + " " + ff.getValue());
            		if (ff.getType().equals(FormField.TYPE_MACRO)) {
            			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
            			if (mu != null) {
            				// 对文件组控件的数据进行整理，将其存入对应的字段中
            				mu.getIFormMacroCtl().doAfterCreate(request, fu, ff, fdao);
            			}
            		}
                }*/
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        finally {
            conn.close();
        }
        return re;
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
    	// 20180725 fgf 取出日志表中的字段，以对比出丢失的字段
    	// 考虑没有意义，还是注释掉了
    	/*
    	Map<String, String> map = new HashMap<String, String>();
    	String sql = "select * from " + FormDb.getTableNameForLog(fdao.getFormCode());
    	com.cloudwebsoft.framework.db.Connection connection = new com.cloudwebsoft.framework.db.Connection(Global.getDefaultDB());
    	try {
    		connection.setMaxRows(1); //尽量减少内存的使用
    		ResultSet rs = connection.executeQuery(sql);
    		ResultSetMetaData rm = rs.getMetaData();
    		int colCount = rm.getColumnCount();
    		for (int i = 1; i <= colCount; i++) {
    			map.put(rm.getColumnName(i), "");
    		}
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    	finally {
    		connection.close();
    	}
    	*/
    	
    	StringBuilder fds = new StringBuilder();
        StringBuilder str = new StringBuilder();
        Vector<FormField> fields = fdao.getFields();
        for (FormField ff : fields) {
/*            if (!map.containsKey(ff.getName())) {
            	continue;
            }*/

            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName());
                str = new StringBuilder("?");
            } else {
                fds.append(",").append(ff.getName());
                str.append(",?");
            }
        }

        String logTable = FormDb.getTableNameForLog(fdao.getFormCode());
        String sql = "insert into " + logTable + " (flowId, cws_creator, cws_id, cws_order, " + fds + ",flowTypeCode,unit_code,cws_log_user,cws_log_type,cws_log_date,cws_log_id) values (?,?,?,?," + str + ",?,?,?,?,?,?)";
        boolean re;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, fdao.getFlowId());
            ps.setString(2, fdao.getCreator());
            ps.setString(3, fdao.getCwsId());
            ps.setInt(4, fdao.getCwsOrder());
            int k = 5;
            for (FormField ff : fields) {
                /*
                if (!map.containsKey(ff.getName())) {
                	continue;
                }*/

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
            re = conn.executePreUpdate() == 1;
            
            // 取得刚插入的记录的ID
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
	            if (logType==FormDAOLog.LOG_TYPE_DEL || logType==FormDAOLog.LOG_TYPE_CREATE) {
	            	String logFormCode = "module_log";
	            	FormDb fd = new FormDb(logFormCode);
	            	FormDAO fdaoLog = new FormDAO(fd);
	            	fdaoLog.setFieldValue("log_type", String.valueOf(logType));
	            	fdaoLog.setFieldValue("log_date", DateUtil.format(ts, "yyyy-MM-dd HH:mm:ss"));
	            	fdaoLog.setFieldValue("log_id", String.valueOf(logId));
	            	fdaoLog.setFieldValue("form_code", fdao.getFormCode());
	            	fdaoLog.setFieldValue("form_name", fdao.getFormDb().getName());
	            	fdaoLog.setFieldValue("module_id", String.valueOf(fdao.getId()));
	            	fdaoLog.setFieldValue("user_name", userName);
	            	fdaoLog.setCreator(userName);
	            	fdaoLog.create();
	            }
	            else if (logType==FormDAOLog.LOG_TYPE_EDIT) {
	            	// 与上一条记录比较差异
	            	Vector<FormField> v = fdao.getFormDb().getFields();
	            	JdbcTemplate jt = new JdbcTemplate();
	                sql = "select * from " + FormDb.getTableNameForLog(fdao.getFormCode()) + " where cws_log_id = '" + fdao.getId() + "' and id < " + logId + " order by id desc";
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
	                        	FormDAO fdaoLog = new FormDAO(fd);
	                        	fdaoLog.setFieldValue("log_type", String.valueOf(logType));
	                        	fdaoLog.setFieldValue("log_date", DateUtil.format(ts, "yyyy-MM-dd HH:mm:ss"));
	                        	fdaoLog.setFieldValue("log_id", String.valueOf(logId));
	                        	fdaoLog.setFieldValue("form_code", fdao.getFormCode());
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
            LogUtil.getLog(FormDAO.class).error("log:" + e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException(e.getMessage());
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

        com.redmoon.oa.visual.Config cfg = new com.redmoon.oa.visual.Config();
        return cfg.getAttachmentPath(formCode) + "/" + year + "/" + month + "/";
    }

    public boolean save(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        return save(request, fu, true);
    }

    /**
     * 保存
     * @param request HttpServletRequest
     * @param fu FileUpload
     * @param isSaveAttachment boolean 当嵌套表单保存时，需置为false，以免重复保存附件
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean save(HttpServletRequest request, FileUpload fu, boolean isSaveAttachment) throws ErrMsgException {
        StringBuilder fds = new StringBuilder();
        for (FormField ff : fields) {
            // 辅助字段在此不保存，跳过
            if (ff.isHelper()) {
                continue;
            }
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName() + "=?");
            } else {
                fds.append(",").append(ff.getName()).append("=?");
            }
        }

        String vpath = getVisualPath();
        String filepath = Global.getRealPath() + vpath;
        fu.setSavePath(filepath);
        Privilege pvg = new Privilege();

        // 处理附件
        LogUtil.getLog(getClass()).info("save: isSaveAttachment=" + isSaveAttachment);
        if (isSaveAttachment && fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector<FileInfo> v = fu.getFiles();
            if (v.size() > 0) {
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                FileInfo fi;
                for (FileInfo fileInfo : v) {
                    fi = fileInfo;
                    Attachment att = new Attachment();
                    att.setFullPath(filepath + fi.getDiskName());
                    att.setVisualId(id);
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setVisualPath(vpath);
                    att.setFormCode(formCode);
                    att.setFieldName(fi.getFieldName());

                    att.setCreator(pvg.getUser(request));
                    att.setFileSize(fi.getSize());

                    att.create();

                    String previewfile = filepath + fi.getDiskName();
                    String ext = StrUtil.getFileExt(att.getDiskName());
                    if ("doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext)) {
                        com.redmoon.oa.fileark.Document.createOfficeFilePreviewHTML(previewfile);
                    } else if ("pdf".equals(ext)) {
                        Pdf2htmlEXUtil.createPreviewHTML(previewfile);
                    }
                }
            }
        }

        String sql = "update " + tableName + " set " + fds + ",cws_order=?,cws_parent_form=?,cws_modify_date=? where id=?";
        // logger.info("save: sql=" + sql);
        boolean re;

        if (formDb==null) {
            formDb = new FormDb();
            formDb = formDb.getFormDb(formCode);
        }

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            int k = 1;
            for (FormField ff : fields) {
                // 辅助字段在此不保存，跳过
                if (ff.isHelper()) {
                    continue;
                }
                //logger.info(ff.getName() + "=" + ff.getValue());
                ff.saveDAOVisual(this, ps, k, id, formDb, fu);
                k++;
            }

            ps.setInt(k, cwsOrder);
            ps.setString(k+1, cwsParentForm);
            ps.setTimestamp(k+2, new Timestamp(new java.util.Date().getTime()));
            ps.setLong(k+3, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException(e.getMessage());
        }
        finally {
            conn.close();
        }
        if (re) {        	
            // 保存嵌套表单中的信息
            MacroCtlMgr mm = new MacroCtlMgr();
            for (FormField macroField : fields) {
                if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.getMacroType());
                    // logger.info("create: mu.getNestType()=" + mu.getNestType());

                    if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                        mu.getIFormMacroCtl().saveForNestCtl(request, macroField, "" + id,
                                creator, fu);
                    }
                }
            }
        }
        return re;
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

        // 保存cws_id的目的是在于有的时候要手工关联表单，如在销售工作中添加chance，则需要手工关联
        String sql = "update " + tableName + " set " + fds + ",flowTypeCode=?,cws_creator=?,cws_order=?,cws_id=?,unit_code=?,cws_status=?,cws_quote_id=?,cws_flag=?,cws_progress=?,cws_parent_form=?,cws_modify_date=?,cws_quote_form=? where id=?";
        // logger.info("save: sql=" + sql);
        boolean re = false;

/*        if (formDb==null) {
            formDb = new FormDb();
            formDb = formDb.getFormDb(formCode);
        }*/

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            int k = 1;
            for (FormField ff : fields) {
                //logger.info(ff.getName() + "=" + ff.getValue());
                ff.saveDAOVisual(ps, k);

                k++;
            }
            ps.setString(k, "" + System.currentTimeMillis()); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
            ps.setString(k+1, creator);
            ps.setInt(k+2, cwsOrder);
            ps.setString(k+3, cwsId);
            ps.setString(k+4, unitCode);
            // 20160305 fgf 因为项目任务的放弃操作，增加了“放弃”状态，故在此使cws_status可以更新
            ps.setInt(k+5, cwsStatus);
            ps.setLong(k+6, cwsQuoteId);
            ps.setInt(k+7, cwsFlag);
            ps.setInt(k+8, cwsProgress);
            ps.setString(k+9, cwsParentForm);
            ps.setTimestamp(k+10, new Timestamp(new java.util.Date().getTime()));
            ps.setString(k + 11, cwsQuoteForm);
            ps.setLong(k+12, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException(e.getMessage());
        }
        finally {
            conn.close();
        }
        return re;
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        String sql = "delete from " + tableName + " where id=?";
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                // 删除附件
                if (attachments != null) {
                    for (Attachment att : attachments) {
                        att.del();
                    }
                }

                // 因20190205加入了关联模块的删除，而嵌套表格必属于关联模块，所以注释掉，但是流程flow.FormDAO中，仍须删除嵌套表格，因为流程中没有关联模块的概念，只有嵌套表格
/*                // 删除嵌套表格
                Iterator ir = fields.iterator();
                MacroCtlMgr mm = new MacroCtlMgr();
                while (ir.hasNext()) {
                    FormField macroField = (FormField) ir.next();
                    if (macroField.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(macroField.getMacroType());
                        if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                            mu.getIFormMacroCtl().onDelNestCtlParent(macroField, String.valueOf(id));
                        }
                    }
                }*/

                // 删除关联模块
                ModuleSetupDb msd = new ModuleSetupDb();
                FormDb fd = new FormDb();
                ModuleRelateDb mrd = new ModuleRelateDb();
                for (Object o : mrd.getModulesRelated(formCode)) {
                    mrd = (ModuleRelateDb) o;
                    String relateCode = mrd.getString("relate_code");
                    msd = msd.getModuleSetupDb(relateCode);
                    String subFormCode = msd.getString("form_code");
                    fd = fd.getFormDb(subFormCode);

                    sql = "select id from " + FormDb.getTableName(subFormCode) + " where cws_id=?";
                    JdbcTemplate jt = new JdbcTemplate();
                    try {
                        ResultIterator ri = jt.executeQuery(sql, new Object[]{id});
                        while (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            long fdaoId = rr.getLong(1);
                            FormDAO fdao = getFormDAO(fdaoId, fd);
                            if (fdao.isLoaded()) {
                                fdao.del();
                            }
                        }
                    } catch (SQLException e) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                        e.printStackTrace();
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
     * 获取FormDAO
     * @param visualId int 记录的id值
     * @param fd FormDb 表单对象
     * @return FormDAO
     */
    public FormDAO getFormDAO(long visualId, FormDb fd) {
        return new FormDAO(visualId, fd);
    }

    /**
     * 根据相关联的父记录的relateFieldValue获得表单型（单个记录）关联模块的记录，该记录是唯一的
     * @param fdRelate FormDb
     * @param relateFieldValue String
     * @return FormDAO
     * @throws ErrMsgException
     */
    public FormDAO getFormDAOOfRelate(FormDb fdRelate, String relateFieldValue) throws ErrMsgException {
        String sql = "select id from " + fdRelate.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(relateFieldValue);
        ListResult lr = listResult(fdRelate.getCode(), sql, 1, 10);
        Iterator<FormDAO> ir = lr.getResult().iterator();
        if (ir.hasNext()) {
            return ir.next();
        }
        return null;
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
        
        License lic = License.getInstance();
        // if (formCode.equals("sales_customer") && lic.isSolutionVer() && !lic.canUseSolution(License.SOLUTION_CRM)) {
        // 平台版才可以用CRM模块
        if ("sales_customer".equals(formCode) && !lic.isPlatformSrc()) {
        	LogUtil.getLog(getClass()).error("listResult:平台版才能使用CRM模块！");
        	return lr;
        }
    	
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
                formDb = fm.getFormDb(formCode);
                do {
                    // logger.info("listResult: id=" + rs.getInt(1));
                    FormDAO fdao = getFormDAO(rs.getLong(1), formDb);
                    result.addElement(fdao);
                } while (rs.next());
            }
        } catch (SQLException e) {
            DebugUtil.e(getClass(), "listResult", listsql);
            logger.error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        return lr;
    }

    /**
     * 根据SQL语句列出表单编码为formCode的所有记录
     * @param formCode String
     * @param sql String
     * @return Vector
     * @throws ErrMsgException
     */
    public Vector<FormDAO> list(String formCode, String sql) throws ErrMsgException {
        Vector<FormDAO> result = new Vector<>();
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql);
            FormMgr fm = new FormMgr();
            formDb = fm.getFormDb(formCode);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                FormDAO fdao = getFormDAO(rr.getLong(1), formDb);
                result.addElement(fdao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
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
                FormMgr fm = new FormMgr();
                FormDb fd = fm.getFormDb(formCode);
                while (rs.next()) {
                    // logger.info("listResult: id=" + rs.getInt(1));
                    FormDAO fdao = getFormDAO(rs.getLong(1), fd);
                    result.add(fdao);
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


    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getFormCode() {
        return formCode;
    }

    public Vector<Attachment> getAttachments() {
        return attachments;
    }

    public String getCreator() {
        return creator;
    }

    @Override
    public String getCwsId() {
        return cwsId;
    }

    public int getCwsOrder() {
        return cwsOrder;
    }

    public String getUnitCode() {
        return unitCode;
    }

    @Override
    public int getFlowId() {
        return (int)flowId;
    }

    @Override
    public long getIdentifier() {
        return id;
    }

    private boolean loaded;
    private String creator;
    /**
     * 用以在表单嵌套时，记录父表单的ID
     */
    private String cwsId;
    private int cwsOrder = 0;
    private String unitCode = DeptDb.ROOTCODE;
    private String flowTypeCode;
    
    private int cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_DONE;
    
    /**
     * 冲抵标志位
     */
    private int cwsFlag = 0;
    
    /**
     * 父表单编码，仅会出现于关联的子表或者嵌套表中
     */
    private String cwsParentForm;
    
    public String getCwsParentForm() {
		return cwsParentForm;
	}

	public void setCwsParentForm(String cwsParentForm) {
		this.cwsParentForm = cwsParentForm;
	}

	public int getCwsProgress() {
		return cwsProgress;
	}

	public void setCwsProgress(int cwsProgress) {
		this.cwsProgress = cwsProgress;
	}

	/**
     * 拉单至嵌套表后引用源表单的记录ID
     */
    private long cwsQuoteId;
    
    /**
     * 进度
     */
    private int cwsProgress = 0;
    
    /**
     * 父表单编码，如果是嵌套表格
     */
    private String parentFormCode;

    private Date cwsCreateDate;

    public Date getCwsCreateDate() {
        return cwsCreateDate;
    }

    public void setCwsCreateDate(Date cwsCreateDate) {
        this.cwsCreateDate = cwsCreateDate;
    }

    private Date cwsModifyDate;

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

	@Override
    public String getFlowTypeCode() {
		return flowTypeCode;
	}

	@Override
    public void setFlowTypeCode(String flowTypeCode) {
		this.flowTypeCode = flowTypeCode;
	}

	public void setCwsStatus(int cwsStatus) {
		this.cwsStatus = cwsStatus;
	}

	@Override
    public int getCwsStatus() {
		return cwsStatus;
	}

	public void setCwsFlag(int cwsFlag) {
		this.cwsFlag = cwsFlag;
	}

	@Override
    public int getCwsFlag() {
		return cwsFlag;
	}

	public void setCwsQuoteId(long cwsQuoteId) {
		this.cwsQuoteId = cwsQuoteId;
	}

	@Override
    public long getCwsQuoteId() {
		return cwsQuoteId;
	}
	
	/**
	 * 使支持手工装配FormDAO，如果智能模块日志列表中需置RequestUtil.setFormDAO(request, fdao)，以支持SQL宏控件的convertToHtml
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}

    public Date getCwsModifyDate() {
        return cwsModifyDate;
    }

    public void setCwsModifyDate(Date cwsModifyDate) {
        this.cwsModifyDate = cwsModifyDate;
    }
}
