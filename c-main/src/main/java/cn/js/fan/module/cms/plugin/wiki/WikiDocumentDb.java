package cn.js.fan.module.cms.plugin.wiki;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.plugin.base.IPluginDocument;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * images: path|desc#@#path|desc#@#path|desc
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WikiDocumentDb extends ObjectDb implements IPluginDocument {
	
	public static final int BEST_ID_NONE = 0;
	
	public static final int STATUS_LOCKED = 1;
	public static final int STATUS_UNLOCKED = 0;
	
	public static final int LEVEL_TOP = 100;
	public static final int LEVEL_NONE = 0;
	
    public WikiDocumentDb() {
    }

    public WikiDocumentDb(int docId) {
        this.docId = docId;
        init();
        load();
    }
    
    public String getStatusDesc() {
    	if (status==STATUS_LOCKED)
    		return "锁定";
    	else
    		return "未锁定";
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setInt(1, docId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        boolean re = rowcount>0? true:false;
        if (re) {
        	// 删除所有的更新记录
        	WikiDocUpdateDb wdud = new WikiDocUpdateDb();
        	wdud.delOfDoc(docId);
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WikiDocumentDb(pk.getIntValue());
    }

    public WikiDocumentDb getWikiDocumentDb(int docId) {
        return (WikiDocumentDb)getObjectDb(new Integer(docId));
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setInt(1, docId);
            ps.setLong(2, bestId);
            ps.setInt(3, editCount);
            ps.setString(4, bestUserName);
            ps.setTimestamp(5, new Timestamp(lastEditDate.getTime()));
            ps.setString(6, lastEditUser);
            ps.setInt(7, status);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }


    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, docId);
            primaryKey.setValue(new Integer(docId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                bestId = rs.getLong(1);
                editCount = rs.getInt(2);
                bestUserName = StrUtil.getNullStr(rs.getString(3));
                lastEditDate = rs.getTimestamp(4);
                lastEditUser = StrUtil.getNullStr(rs.getString(5));
                status = rs.getInt(6);
                level = rs.getInt(7);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() {
            int rowcount = 0;
            Conn conn = null;
            try {
                conn = new Conn(connname);
                PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
                ps.setLong(1, bestId);
                ps.setInt(2, editCount);
                ps.setString(3, bestUserName);
                ps.setTimestamp(4, new Timestamp(lastEditDate.getTime()));
                ps.setString(5, lastEditUser);
                ps.setInt(6, status);
                ps.setInt(7, level);
                ps.setInt(8, docId);
                rowcount = conn.executePreUpdate();

                primaryKey.setValue(new Integer(docId));
                objectCache.refreshSave(primaryKey);
            } catch (SQLException e) {
                logger.error(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        return rowcount>0? true:false;
    }

    public void initDB() {
        this.tableName = "cms_wiki_doc";

        primaryKey = new PrimaryKey("doc_id", PrimaryKey.TYPE_INT);
        objectCache = new WikiDocumentCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE doc_id=?";
        this.QUERY_CREATE =
                "INSERT " + tableName + " (doc_id,best_id,edit_count,best_user_name,last_edit_date,last_edit_user,status) VALUES (?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT best_id,edit_count,best_user_name,last_edit_date,last_edit_user,status,doc_level FROM " + tableName + " WHERE doc_id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET best_id=?,edit_count=?,best_user_name=?,last_edit_date=?,last_edit_user=?,status=?,doc_level=? WHERE doc_id=?";
        isInitFromConfigDB = false;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getDocId() {
        return docId;
    }

    public int getPageCount() {
    	return IPluginDocument.PAGE_COUNT_NONE;
    }

    private int docId;
    private int editCount = 0;
    private long bestId;
    private String bestUserName;
    private java.util.Date lastEditDate;
    private String lastEditUser;
    private int status = STATUS_UNLOCKED;
    private int level = LEVEL_NONE;
    
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public java.util.Date getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(java.util.Date lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	public String getLastEditUser() {
		return lastEditUser;
	}

	public void setLastEditUser(String lastEditUser) {
		this.lastEditUser = lastEditUser;
	}

	public int getEditCount() {
		return editCount;
	}

	public void setEditCount(int editCount) {
		this.editCount = editCount;
	}

	public long getBestId() {
		return bestId;
	}

	public void setBestId(long bestId) {
		this.bestId = bestId;
	}

	public String getBestUserName() {
		return bestUserName;
	}

	public void setBestUserName(String bestUserName) {
		this.bestUserName = bestUserName;
	}
	
	/**
	 * 初始化最好页码
	 */
	public void initBestPageNum() {
		WikiDocUpdateDb wdud2 = new WikiDocUpdateDb();
    	if (bestId!=BEST_ID_NONE) {
			wdud2 = wdud2.getWikiDocUpdateDb(new Long(bestId));
			if (wdud2!=null && wdud2.getInt("check_status") == WikiDocUpdateDb.CHECK_STATUS_PASSED)
				return; // wdud2.getInt("page_num");
			else
				wdud2 = new WikiDocUpdateDb();
    	}
		
    	Document doc = new Document();
    	doc = doc.getDocument(docId);
    	if (doc.getPageCount()==1)
    		;
    	else {
			WikiDocUpdateDb wdud;
			for (int p = doc.getPageCount(); p >= 1; p--) {
				wdud = wdud2.getWikiDocUpdateDb(docId, p);
				if (wdud!=null && wdud.getInt("check_status") == WikiDocUpdateDb.CHECK_STATUS_PASSED) {
					wdud.set("page_num", new Integer(p));
					try {
						wdud.save();
					} catch (ResKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
    	}
	}
    
    public int getBestPageNum() {
		// 检查该ID对应的记录是否为通过状态
		WikiDocUpdateDb wdud2 = new WikiDocUpdateDb();
    	if (bestId!=BEST_ID_NONE) {
			wdud2 = wdud2.getWikiDocUpdateDb(new Long(bestId));
			if (wdud2!=null && wdud2.getInt("check_status") == WikiDocUpdateDb.CHECK_STATUS_PASSED)
				return wdud2.getInt("page_num");
			else
				wdud2 = new WikiDocUpdateDb();
    	}
		
    	Document doc = new Document();
    	int pageNum = 1;
    	doc = doc.getDocument(docId);
    	if (doc.getPageCount()==1)
    		pageNum = 1;
    	else {
			WikiDocUpdateDb wdud;
			for (int p = doc.getPageCount(); p >= 1; p--) {
				wdud = wdud2.getWikiDocUpdateDb(docId, p);
				if (wdud!=null && wdud.getInt("check_status") == WikiDocUpdateDb.CHECK_STATUS_PASSED) {
					pageNum = p;
					break;
				}
			}
    	}
		return pageNum;
    }	
}
