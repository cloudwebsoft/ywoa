package com.redmoon.oa.flow;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IObsService;
import com.cloudweb.oa.api.IObsServiceFactory;
import com.cloudweb.oa.cache.FlowFormDaoCache;
import com.cloudweb.oa.utils.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.util.Pdf2Html;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Attachment implements IAttachment, java.io.Serializable {

    long id;
    int docId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    String connname;

    /**
     * Word文档锁定密码,暂无用处
     */
    String lockPwd;

    /**
     * 锁定文档的用户
     */
    String lockUser;

    /**
     * 创建者
     */
    String creator;

    java.util.Date createDate;

    String INSERT = "insert into flow_document_attach (doc_id, name, fullpath, diskname, visualpath, orders, page_num, field_name, creator, lock_user, create_date, id, file_size, flow_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String LOAD = "SELECT doc_id, name, fullpath, diskname, visualpath, orders, page_num, field_name, lock_pwd, creator, lock_user, create_date, file_size,is_red,is_sealed FROM flow_document_attach WHERE id=?";
    String SAVE = "update flow_document_attach set doc_id=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, page_num=?, field_name=?, lock_pwd=?, lock_user=?, file_size=?,is_red=?,is_sealed=? WHERE id=?";

    public Attachment() {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
    }

    public Attachment(long id) {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
        this.id = id;
        loadFromDb();
    }

    public Attachment(int orders, int docId, int pageNum) {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
        this.orders = orders;
        this.docId = docId;
        this.pageNum = pageNum;
        loadFromDbByOrders();
    }

    @Override
    public boolean del() {
        String sql = "delete from flow_document_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() == 1;
            pstmt.close();
            // 更新其后的附件的orders
            sql = "update flow_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            pstmt.setInt(3, orders);
            conn.executePreUpdate();

            // 刷新对应dao记录的缓存
            Document doc = new Document();
            doc = doc.getDocument(docId);
            Leaf lf = new Leaf();
            lf = lf.getLeaf(doc.getDirCode());
            FormDb fd = new FormDb();
            fd = fd.getFormDb(lf.getFormCode());
            com.redmoon.oa.flow.FormDAO dao = new com.redmoon.oa.flow.FormDAO();
            dao = dao.getFormDAOByCache((int)doc.getFlowId(), fd);
            FlowFormDaoCache flowFormDaoCache = SpringUtil.getBean(FlowFormDaoCache.class);
            flowFormDaoCache.refreshDel(dao);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            conn.close();
        }

        String fullPath = cn.js.fan.web.Global.getRealPath() + "/" + getVisualPath() + "/" + getDiskName();

        // 删除文件
        boolean isCosReserveLocalFile = com.redmoon.oa.Config.getInstance().getBooleanProperty("isCosReserveLocalFile");

        SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
        if (sysProperties.isObjStoreEnabled()) {
            IObsServiceFactory obsServiceFactory = SpringUtil.getBean(IObsServiceFactory.class);
            IObsService obsService = obsServiceFactory.getInstance();
            obsService.delete(getVisualPath() + "/" + getDiskName());
        }
        if (isCosReserveLocalFile) {
            File file = new File(fullPath);
            if (file.exists()) {
                file.delete();
            }
        }

        // 删除预览文件
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
        boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
        if (canOfficeFilePreview) {
            if (FileUtil.isOfficeFile(fullPath)) {
                PreviewUtil.jacobFileDelete(fullPath);
            }
        }
        if (canPdfFilePreview) {
            if ("pdf".equalsIgnoreCase(StrUtil.getFileExt(diskName))) {
                Pdf2Html.del(fullPath);
            }
        }
        return re;
    }

    @Override
    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setInt(1, docId);
            pstmt.setString(2, name);
            pstmt.setString(3, fullPath);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setInt(7, pageNum);
            pstmt.setString(8, fieldName);
            pstmt.setString(9, lockPwd);
            pstmt.setString(10, lockUser);
            pstmt.setLong(11, size);
            pstmt.setInt(12, red ? 1 : 0);
            pstmt.setInt(13, sealed ? 1 : 0);
            pstmt.setLong(14, id);
            re = conn.executePreUpdate() == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            conn.close();
        }
        return re;
    }

    public Attachment getAttachment(int docId, String fieldName) {
        String sql = "select id from flow_document_attach where doc_id=? and field_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{docId, fieldName});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return new Attachment(rr.getInt(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return null;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getDocId() {
        return this.docId;
    }

    @Override
    public long getVisualId() {
        return -1;
    }

    public void setDocId(int di) {
        this.docId = di;
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

    public void setDiskName(String dn) {
        this.diskName = dn;
    }

    public String getFullPath() {
        return this.fullPath;
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

    public int getPageNum() {
        return pageNum;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    public void setVisualPath(String vp) {
        this.visualPath = vp;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean create() {
        // INSERT = "insert into flow_document_attach (doc_id, name, fullpath, diskname, visualpath, orders, page_num) values (?,?,?,?,?,?,?)";
        boolean re = false;
        PreparedStatement pstmt = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(INSERT);
            pstmt.setInt(1, docId);
            pstmt.setString(2, name);
            pstmt.setString(3, fullPath);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setInt(7, pageNum);
            pstmt.setString(8, fieldName);
            pstmt.setString(9, creator);
            pstmt.setString(10, lockUser);
            pstmt.setTimestamp(11, new java.sql.Timestamp(new java.util.Date().getTime()));
            id = SequenceManager.nextID(SequenceManager.FLOW_DOCUMENT_ATTACH);
            pstmt.setLong(12, id);
            pstmt.setLong(13, size);
            pstmt.setInt(14, flowId);
            re = conn.executePreUpdate() == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setLong(1, id);
            // LogUtil.getLog(getClass()).info("attach id=" + id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                docId = rs.getInt(1);
                name = rs.getString(2);
                // LogUtil.getLog(getClass()).info("attach name=" + name);
                fullPath = rs.getString(3);
                // LogUtil.getLog(getClass()).info("attach fullPath=" + fullPath);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                pageNum = rs.getInt(7);
                fieldName = rs.getString(8);
                lockPwd = StrUtil.getNullStr(rs.getString(9));
                creator = StrUtil.getNullStr(rs.getString(10));
                lockUser = StrUtil.getNullStr(rs.getString(11));
                createDate = rs.getTimestamp(12);
                size = rs.getLong(13);
                red = rs.getInt(14) == 1;
                sealed = rs.getInt(15) == 1;
                loaded = true;

                // poi-dl只支持docx
                canDocInRed = !isRed() && ("docx".equals(StrUtil.getFileExt(getDiskName())));
                canSeal = "docx".equals(StrUtil.getFileExt(getDiskName()));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            conn.close();
        }
    }

    public void loadFromDbByOrders() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String LOADBYORDERS = "SELECT id, name, fullpath, diskname, visualpath, field_name, lock_pwd, file_size FROM flow_document_attach WHERE orders=? and doc_id=? and page_num=?";
            pstmt = conn.prepareStatement(LOADBYORDERS);
            pstmt.setInt(1, orders);
            pstmt.setInt(2, docId);
            pstmt.setInt(3, pageNum);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                id = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                fieldName = rs.getString(6);
                lockPwd = StrUtil.getNullStr(rs.getString(7));
                size = rs.getLong(8);
                loaded = true;

                // poi-dl只支持docx
                canDocInRed = !isRed() && ("docx".equals(StrUtil.getFileExt(getDiskName())));
                canSeal = "docx".equals(StrUtil.getFileExt(getDiskName()));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            conn.close();
        }
    }

    public Attachment getAttachmentByName(int docId, String name) {
        String sql = "select id from flow_document_attach where doc_id=? and name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{docId, name});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return new Attachment(rr.getInt(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return null;
    }

    private int orders = 0;
    private int pageNum;
    private boolean loaded = false;

    /**
     * 表单中的
     */
    private String fieldName;

    public String getLockPwd() {
        return lockPwd;
    }

    public void setLockPwd(String lockPwd) {
        this.lockPwd = lockPwd;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLockUser() {
        return lockUser;
    }

    public void setLockUser(String lockUser) {
        this.lockUser = lockUser;
    }

    @Override
    public java.util.Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private long size;

    public boolean isRed() {
        return red;
    }

    @Override
    public void setRed(boolean red) {
        this.red = red;
    }

    @Override
    public boolean isSealed() {
        return sealed;
    }

    @Override
    public void setSealed(boolean sealed) {
        this.sealed = sealed;
    }

    private boolean red = false;

    private boolean sealed = false;

    @Override
    public String getFileSizeMb() {
        return NumberUtil.round((double) size / 1024000, 2) + 'M';
    }

    @Override
    public String getCreatorRealName() {
        if (creator != null && !"".equals(creator)) {
            UserDb user = new UserDb();
            user = user.getUserDb(creator);
            if (user != null) {
                return user.getRealName();
            } else {
                return "";
            }
        }
        return "";
    }

    @Override
    public String getPreviewUrl() {
        Config cfg = Config.getInstance();
        boolean isPreview = false;
        if (cfg.getBooleanProperty("canPdfFilePreview") || cfg.getBooleanProperty("canOfficeFilePreview")) {
            String s = Global.getRealPath() + getVisualPath() + "/" + getDiskName();
            String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
            java.io.File fileExist = new java.io.File(htmlfile);
            if (fileExist.exists()) {
                isPreview = true;
            }
        }
        String url;
        SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
        boolean isImg = false;
        if (StrUtil.isImage(StrUtil.getFileExt(getDiskName()))) {
            isImg = true;
        }
        if (isPreview) {
            url = sysUtil.getRootPath() + "/" + getVisualPath() + "/" + getDiskName().substring(0, getDiskName().lastIndexOf(".")) + ".html";
        }
        else if (isImg) {
            url = sysUtil.getRootPath() + "/showImg?path=" + getVisualPath() + "/" + getDiskName();
        }
        else {
            url = "";
        }
        return url;
    }

    public String getVisitKey() {
        return SecurityUtil.makeVisitKey(id);
    }

    private boolean canDocInRed = false;
    private boolean canSeal = false;

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    private int flowId;

    @Override
    public boolean getCanDocInRed() {
        return canDocInRed;
    }

    @Override
    public boolean getCanSeal() {
        return canSeal;
    }

    @Override
    public void setCanDocInRed(boolean canDocInRed) {
        canDocInRed = canDocInRed;
    }

    @Override
    public void setCanSeal(boolean canSeal) {
        canSeal = canSeal;
    }
}
