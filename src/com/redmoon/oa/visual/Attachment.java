package com.redmoon.oa.visual;

import java.io.*;
import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.person.UserDb;
import org.apache.log4j.*;

import com.redmoon.oa.flow.FormDb;

public class Attachment {
    long id;
    long visualId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    String connname;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    String creator;

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

    public String getFileSizeMb() {
        return NumberUtil.round((double)fileSize / 1024000, 2);
    }

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

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
    Logger logger = Logger.getLogger(Attachment.class.getName());

    public Attachment() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
    }

    public Attachment(long id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public Attachment(int orders, int visualId, String formCode) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
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
                ResultRecord rr = (ResultRecord)ri.next();
                return new Attachment(rr.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            re = conn.executePreUpdate()==1?true:false;

            id = (int)SQLFilter.getLastId(conn, "visual_attach");
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public boolean del() {
        String sql = "delete from visual_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate()==1?true:false;
            pstmt.close();
            // 更新其后的附件的orders
            sql = "update visual_attach set orders=orders-1 where visualId=? and formCode=? and orders>?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, visualId);
            pstmt.setString(2, formCode);
            pstmt.setInt(3, orders);
            conn.executePreUpdate();
            
            // 将对应字段的值置为空，否则图片宏控件将显示为叉
            FormDAO fdao = new FormDAO();
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            fdao = fdao.getFormDAO(visualId, fd);
            fdao.setFieldValue(fieldName, "");
            try {
				fdao.save();
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}
        }
        catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            return false;
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        // 删除文件
        if (re) {
            File fl = new File(this.fullPath);
            fl.delete();
        }
        return re;
    }

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
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public long getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getVisualId() {
        return this.visualId;
    }

    public void setVisualId(long visualId) {
        this.visualId = visualId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiskName() {
        return this.diskName;
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

    public String getVisualPath() {
        return this.visualPath;
    }

    public int getOrders() {
        return orders;
    }

    public String getFormCode() {
        return formCode;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getFieldName() {
        return fieldName;
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
            // System.out.println("attach id=" + id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                visualId = rs.getInt(1);
                name = rs.getString(2);
                // System.out.println("attach name=" + name);
                fullPath = rs.getString(3);
                // System.out.println("attach fullPath=" + fullPath);
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
            logger.error("loadFromDb:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void loadFromDbByOrders() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
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
            logger.error("loadFromDbByOrders:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }
    
    public Attachment getAttachment(String diskName) {    
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
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
            logger.error("loadFromDbByOrders:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    private int orders = 0;
    private String formCode;
    private boolean loaded = false;
    private String fieldName;

    public String getPreviewUrl() {
        String url = "";
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        if (cfg.getBooleanProperty("canPdfFilePreview") || cfg.getBooleanProperty("canOfficeFilePreview")) {
            String s = Global.getRealPath() + getVisualPath() + "/" + getDiskName();
            String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
            java.io.File fileExist = new java.io.File(htmlfile);
            if (fileExist.exists()) {
                url = visualPath + getDiskName().substring(0, getDiskName().lastIndexOf(".")) + ".html";
            }
        }
        return url;
    }
}
