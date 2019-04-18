package com.redmoon.oa.flow;

import java.io.*;
import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.tools.Pdf2htmlEXUtil;

public class Attachment implements java.io.Serializable {
    int id;
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

    String INSERT = "insert into flow_document_attach (doc_id, name, fullpath, diskname, visualpath, orders, page_num, field_name, creator, lock_user, create_date, id, file_size) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String LOAD = "SELECT doc_id, name, fullpath, diskname, visualpath, orders, page_num, field_name, lock_pwd, creator, lock_user, create_date, file_size FROM flow_document_attach WHERE id=?";
    String SAVE = "update flow_document_attach set doc_id=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, page_num=?, field_name=?, lock_pwd=?, lock_user=?, file_size=? WHERE id=?";

    public Attachment() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
    }

    public Attachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public Attachment(int orders, int docId, int pageNum) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        this.orders = orders;
        this.docId = docId;
        this.pageNum = pageNum;
        loadFromDbByOrders();
    }

    public boolean del() {
        String sql = "delete from flow_document_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
            pstmt.close();
            // 更新其后的附件的orders
            sql = "update flow_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            pstmt.setInt(3, orders);
            conn.executePreUpdate();
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            return false;
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        // 删除文件
        String fullPath = cn.js.fan.web.Global.getRealPath() + "/" + getVisualPath() + "/" + getDiskName();
        File fl = new File(fullPath);
        fl.delete();
                
        // 删除预览文件
        Document.jacobFileDelete(fullPath);
        
        if (StrUtil.getFileExt(diskName).equalsIgnoreCase("pdf")) {
        	Pdf2htmlEXUtil.del(fullPath);         
        }
        return re;
    }

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
            pstmt.setInt(12, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocId() {
        return this.docId;
    }

    public void setDocId(int di) {
        this.docId = di;
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
        return this.fullPath;
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

    public int getPageNum() {
        return pageNum;
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
             id = (int)SequenceManager.nextID(SequenceManager.FLOW_DOCUMENT_ATTACH);
             pstmt.setInt(12, id);
             pstmt.setLong(13, size);
             re = conn.executePreUpdate()==1;
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("create:" + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
         return re;
    }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, id);
            // System.out.println("attach id=" + id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                docId = rs.getInt(1);
                name = rs.getString(2);
                // System.out.println("attach name=" + name);
                fullPath = rs.getString(3);
                // System.out.println("attach fullPath=" + fullPath);
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
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
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
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
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

	public java.util.Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	private long size;
}
