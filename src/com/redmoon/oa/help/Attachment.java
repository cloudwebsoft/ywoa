package com.redmoon.oa.help;

import java.io.File;
import java.sql.*;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.db.SequenceManager;

public class Attachment implements java.io.Serializable {
    int id;
    int docId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    String connname;
    public static final int TEMP_DOC_ID = -1; // 上传图片文件的临时id 置于doc_id字段    
    
    /**
     * 是否嵌入于文章正文中
     */
    private boolean embedded = false;

    String LOAD = "SELECT doc_id, name, fullpath, diskname, visualpath, orders, page_num, downloadCount, upload_date, file_size, ext, is_embedded FROM help_document_attach WHERE id=?";
    String SAVE = "update help_document_attach set doc_id=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, page_num=?, downloadCount=? WHERE id=?";
    transient Logger logger = Logger.getLogger(Attachment.class.getName());

    public Attachment() {
        connname = Global.getDefaultDB();
    }

    public Attachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public Attachment(int orders, int docId, int pageNum) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
        this.orders = orders;
        this.docId = docId;
        this.pageNum = pageNum;
        loadFromDbByOrders();
    }

    public boolean create() {
        id = (int)SequenceManager.nextID(SequenceManager.OA_DOCUMENT_ATTACH_CMS);

        String sql =
            "insert into help_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num,orders,id,upload_date,file_size,ext,is_embedded) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullPath);
            pstmt.setInt(2, docId);
            pstmt.setString(3, name);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, pageNum);
            pstmt.setInt(7, orders);
            pstmt.setInt(8, id);
            pstmt.setTimestamp(9, new java.sql.Timestamp(uploadDate.getTime()));
            pstmt.setLong(10, size);
            pstmt.setString(11, ext);
            pstmt.setInt(12, embedded?1:0);
            re = conn.executePreUpdate()==1?true:false;
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
        String sql = "delete from help_document_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
            pstmt.close();
            // 更新其后的附件的orders
            sql = "update help_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            pstmt.setInt(3, orders);
            conn.executePreUpdate();
        }
        catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        // 删除文件
        File fl = new File(Global.getRealPath() + visualPath + "/" + diskName);
        fl.delete();
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
            pstmt.setInt(8, downloadCount);
            pstmt.setInt(9, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }
        finally {
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(docId);
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

    public int getDownloadCount() {
        return downloadCount;
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

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                docId = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                pageNum = rs.getInt(7);
                downloadCount = rs.getInt(8);
                uploadDate = rs.getTimestamp(9);
                size = rs.getLong(10);
                ext = StrUtil.getNullStr(rs.getString(11));
                embedded = rs.getInt(12)==1;
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
            String LOADBYORDERS = "SELECT id, name, fullpath, diskname, visualpath,downloadCount,ext FROM help_document_attach WHERE orders=? and doc_id=? and page_num=?";
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
                downloadCount = rs.getInt(6);
                ext = StrUtil.getNullStr(rs.getString("ext"));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
    
	public boolean delTmpAttach() {
        String sql = "delete from help_document_attach where doc_id="+ TEMP_DOC_ID +" and id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
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
        File fl = new File(Global.getRealPath() + visualPath + "/" + diskName);
        fl.delete();
        return re;        
	}
	
    public ListResult listResult(String sql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            // 取得总记录条数
            PreparedStatement ps = conn.prepareStatement(countsql);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
                // System.out.println("total=" + total);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (ps!=null) {
                ps.close();
                ps = null;
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages)
                curPage = totalpages;
            if (curPage <= 0)
                curPage = 1;

            ps = conn.prepareStatement(sql);

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executePreQuery();
            if (rs == null) {
                return lr;
            } else {
                Attachment att;
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    att = new Attachment(rs.getInt(1));
                    result.addElement(att);
                } while (rs.next());
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }
	

    private int orders = 0;
    private int pageNum;
    private boolean loaded = false;
    private int downloadCount = 0;
    private String ext;
    
    public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	java.util.Date uploadDate;
    long size = 0;

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public java.util.Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(java.util.Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public void setEmbedded(boolean embedded) {
		this.embedded = embedded;
	}

	public boolean isEmbedded() {
		return embedded;
	}
}
