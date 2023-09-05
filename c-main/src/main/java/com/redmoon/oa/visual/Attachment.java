package com.redmoon.oa.visual;

import java.io.*;
import java.sql.*;
import java.util.Vector;

import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.*;
import com.cloudweb.oa.api.IObsService;
import com.cloudweb.oa.cache.VisualFormDaoCache;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.PreviewUtil;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.emailpop3.MailMsgDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.sys.DebugUtil;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.util.Pdf2Html;

public class Attachment implements IAttachment, java.io.Serializable {
    long id;
    long visualId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    String connname;

    public static final int TEMP_VISUAL_ID = -1; // ueditorctl上传图片文件的临时ID，置于visual_id字段

    @Override
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    String creator;

    @Override
    public String getCreatorRealName() {
        if (creator!=null && !"".equals(creator)) {
            UserDb user = new UserDb();
            user = user.getUserDb(creator);
            if (user!=null) {
                return user.getRealName();
            }
            else {
                return "";
            }
        }
        return "";
    }
    
    public long getFileSize() {
		return fileSize;
	}

    @Override
    public String getFileSizeMb() {
        return NumberUtil.round((double)fileSize / 1024000, 2) + 'M';
    }

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
    public java.util.Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}

	long fileSize = 0;
    java.util.Date createDate;

    String LOAD = "SELECT visualId, name, fullpath, diskname, visualpath, orders, formCode, field_name, creator, create_date, file_size FROM visual_attach WHERE id=?";
    String SAVE = "update visual_attach set visualId=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, formCode=?, field_name=?, creator=?, create_date=?, file_size=? WHERE id=?";

    public Attachment() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
    }

    public Attachment(long id) {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
        this.id = id;
        loadFromDb();
    }

    public Attachment(int orders, int visualId, String formCode) {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
        this.orders = orders;
        this.visualId = visualId;
        this.formCode = formCode;
        loadFromDbByOrders();
    }

    public Attachment getAttachment(long id) {
        return new Attachment(id);
    }

    public Attachment getAttachment(long id, String fieldName) {
        String sql = "select id from visual_attach where visualId=? and field_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{id, fieldName});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return new Attachment(rr.getInt(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return null;
    }

    public boolean create() {
        String sql =
            "insert into visual_attach (fullpath,visualId,name,diskname,visualpath,orders,formCode,field_name,creator, create_date, file_size) values (?,?,?,?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullPath);
            pstmt.setLong(2, visualId);
            pstmt.setString(3, name);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setString(7, formCode);
            pstmt.setString(8, fieldName);
            pstmt.setString(9, creator);
            pstmt.setTimestamp(10, new java.sql.Timestamp(new java.util.Date().getTime()));
            pstmt.setLong(11, fileSize);
            re = conn.executePreUpdate() == 1;

            id = (int)SQLFilter.getLastId(conn, "visual_attach");
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            conn.close();
        }
        return re;
    }

    @Override
    public boolean del() {
        String sql = "delete from visual_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                pstmt.close();
                // 更新其后的附件的orders
                sql = "update visual_attach set orders=orders-1 where visualId=? and formCode=? and orders>?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, visualId);
                pstmt.setString(2, formCode);
                pstmt.setInt(3, orders);
                conn.executePreUpdate();

                // 将对应字段的值置为空，否则图片宏控件将显示为叉
                try {
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(formCode);
                    if (fd.getFormField(fieldName) != null) {
                        FormDAO fdao = new FormDAO();
                        fdao = fdao.getFormDAOByCache(visualId, fd);
                        // 有可能是删除记录的同时删除附件，如果是这种情况，则fdao为null
                        if (fdao != null) {
                            fdao.setFieldValue(fieldName, "");
                            fdao.save();
                            // 如果需要记录历史
                            if (fd.isLog()) {
                                FormDAO.log(SpringUtil.getUserName(), FormDAOLog.LOG_TYPE_EDIT, fdao);
                            }
                        }
                    } else {
                        // 刷新对应dao记录的缓存
                        FormDAO fdao = new FormDAO();
                        fdao = fdao.getFormDAOByCache(visualId, fd);
                        if (fdao != null) {
                            VisualFormDaoCache visualFormDaoCache = SpringUtil.getBean(VisualFormDaoCache.class);
                            visualFormDaoCache.refreshDel(fdao);
                        }
                    }
                } catch (ErrMsgException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            return false;
        }
        finally {
            conn.close();
        }
        // 删除文件
        if (re) {
            IFileService fileService = SpringUtil.getBean(IFileService.class);
            fileService.del(visualPath, diskName);

            // 删除预览文件
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
            boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
            if (canOfficeFilePreview) {
                if (FileUtil.isOfficeFile(diskName)) {
                    PreviewUtil.jacobFileDelete(Global.getRealPath() + visualPath + "/" + diskName);
                }
            }
            if (canPdfFilePreview) {
                if ("pdf".equalsIgnoreCase(StrUtil.getFileExt(diskName))) {
                    Pdf2Html.del(Global.getRealPath() + visualPath + "/" + diskName);
                }
            }
        }
        return re;
    }

    @Override
    public void setRed(boolean red) {

    }

    @Override
    public void setSealed(boolean sealed) {

    }

    @Override
    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setLong(1, visualId);
            pstmt.setString(2, name);
            pstmt.setString(3, fullPath);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setString(7, formCode);
            pstmt.setString(8, fieldName);
            pstmt.setString(9, creator);
            if (createDate==null) {
                pstmt.setTimestamp(10, null);
            }
            else {
                pstmt.setTimestamp(10, new Timestamp(createDate.getTime()));
            }
            pstmt.setLong(11, fileSize);
            pstmt.setLong(12, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            conn.close();
        }
        return re;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public long getVisualId() {
        return this.visualId;
    }

    public void setVisualId(long visualId) {
        this.visualId = visualId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDiskName() {
        return this.diskName;
    }

    @Override
    public void setCanDocInRed(boolean isCanDocInRed) {

    }

    @Override
    public void setCanSeal(boolean isCanSeal) {

    }

    public void setDiskName(String dn) {
        this.diskName = dn;
    }

    public String getFullPath() {
        return Global.getRealPath() + visualPath + diskName;
    }

    public void setFullPath(String f) {
        this.fullPath = f;
    }

    @Override
    public String getVisualPath() {
        return this.visualPath;
    }

    @Override
    public int getOrders() {
        return orders;
    }

    @Override
    public long getSize() {
        return fileSize;
    }

    @Override
    public int getDocId() {
        return -1;
    }

    public String getFormCode() {
        return formCode;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean isSealed() {
        return false;
    }

    public void setVisualPath(String vp) {
        this.visualPath = vp;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                visualId = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                formCode = rs.getString(7);
                fieldName = rs.getString(8);
                creator = StrUtil.getNullStr(rs.getString(9));
                createDate = rs.getTimestamp(10);
                fileSize = rs.getLong(11);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDb:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    public void loadFromDbByOrders() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            String LOADBYORDERS = "SELECT id, name, fullpath, diskname, visualpath, field_name, creator, create_date, file_size FROM visual_attach WHERE orders=? and visualId=? and formCode=?";
            pstmt = conn.prepareStatement(LOADBYORDERS);
            pstmt.setInt(1, orders);
            pstmt.setLong(2, visualId);
            pstmt.setString(3, formCode);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                id = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                fieldName = rs.getString(6);
                creator = StrUtil.getNullStr(rs.getString(7));
                createDate = rs.getTimestamp(8);
                fileSize = rs.getLong(9);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDbByOrders:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }
    
    public Attachment getAttachment(String diskName) {    
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            String sql = "SELECT id FROM visual_attach WHERE diskname=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, diskName);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                int id = rs.getInt(1);
                return new Attachment(id); 
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDbByOrders:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }

    private int orders = 0;
    private String formCode;
    private boolean loaded = false;
    private String fieldName;

    @Override
    public String getPreviewUrl() {
        String url = "";
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        boolean isValid = false;
        String ext = StrUtil.getFileExt(diskName);
        if (cfg.getBooleanProperty("canPdfFilePreview")) {
            if ("pdf".equals(ext)) {
                isValid = true;
            }
        }
        if (cfg.getBooleanProperty("canOfficeFilePreview")) {
            if ("doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext)) {
                isValid = true;
            }
        }
        boolean isImg = false;
        if (StrUtil.isImage(ext)) {
            isImg = true;
        }
        if (isValid) {
            String s = Global.getRealPath() + getVisualPath() + "/" + getDiskName();
            int p = s.lastIndexOf(".");
            if (p != -1) {
                String htmlfile = s.substring(0, p) + ".html";
                java.io.File fileExist = new java.io.File(htmlfile);
                if (fileExist.exists()) {
                    SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
                    url = sysUtil.getRootPath() + "/" + visualPath + "/" + getDiskName().substring(0, getDiskName().lastIndexOf(".")) + ".html";
                }
            }
            else {
                DebugUtil.e(getClass(), "getPreviewUrl", "附件 " + id + " 文件名 " + diskName + " 非法");
            }
        }
        else if (isImg) {
            SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
            url = sysUtil.getRootPath() + "/showImg?path=" + visualPath + "/" + getDiskName();
        }
        return url;
    }

    @Override
    public boolean getCanDocInRed() {
        return false;
    }

    @Override
    public boolean getCanSeal() {
        return false;
    }

    /**
     * 根据diskName取得临时文件的ID
     * @Description:
     * @param diskName
     * @return
     */
    public long getTmpAttId(String diskName) {
        String sql = "select id from visual_attach where visualId=" + TEMP_VISUAL_ID + " and diskname=" + StrUtil.sqlstr(diskName);
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri;
        try {
            ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return rr.getLong(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return -1;
    }

    public String getVisitKey() {
        return SecurityUtil.makeVisitKey(id);
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs;
        Vector<Attachment> result = new Vector<>();
        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
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
                do {
                    Attachment attachment = getAttachment(rs.getLong(1));
                    result.addElement(attachment);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }
}
