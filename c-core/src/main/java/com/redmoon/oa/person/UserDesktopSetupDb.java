package com.redmoon.oa.person;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.*;
import com.redmoon.oa.ui.*;
 
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserDesktopSetupDb extends ObjectDb {
    private int id,rows,isSystem,td,orderInTd;
    private String userName,title,moreUrl,moduleCode,moduleItem;
    
    private long portalId;
    
	public static final int TD_LEFT = 0;
    public static final int TD_RIGHT = 1;
    
    public static final int TD_SIDEBAR = 2;
    
    /**
     * 系统门户中的桌面项，其system_id字段为0
     */
    public static final int SYSTEM_ID_NONE = 0;
    
    private int systemId;
    
    private boolean canDelete = true;
    
    private String metaData;
    
    private String icon;

    public final static String MODULE_FILEARK = "fileark";

    public long getPortalId() {
		return portalId;
	}

	public void setPortalId(long portalId) {
		this.portalId = portalId;
	}

    public UserDesktopSetupDb() {
        init();
    }

    public UserDesktopSetupDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        tableName = "user_desktop_setup";
        primaryKey = new PrimaryKey("ID", PrimaryKey.TYPE_INT);
        objectCache = new UserDesktopSetupCache(this);
        this.isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName + " (id,user_name,title,more_url,module_rows,module_code,module_item,is_system,td,order_in_td,word_count,portal_id,system_id,can_delete,meta_data,icon) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set title=?,more_url=?,module_rows=?,module_code=?,module_item=?,is_system=?,td=?,order_in_td=?,word_count=?,can_delete=?,meta_data=?,icon=? where id=?";
        QUERY_LOAD = "select user_name,title,more_url,module_rows,module_code,module_item,is_system,td,order_in_td,word_count,portal_id,system_id,can_delete,meta_data,icon from " + tableName + " where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LIST = "select id from " + tableName;
    }

    public String getSqlByPortalId(long portalId) {
        return "select id from " + getTableName() + " where portal_id=" + portalId + " order by td asc, order_in_td asc";
    }

    public int getCount() {
        return rows;
    }

    public void setCount(int count) {
        this.rows = count;
    }

    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (int)SequenceManager.nextID(SequenceManager.OA_USER_DESKTOP_SETUP);
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setInt(1, id);
            pstmt.setString(2, userName);
            pstmt.setString(3, title);
            pstmt.setString(4, moreUrl);
            pstmt.setInt(5, rows);
            pstmt.setString(6, moduleCode);
            pstmt.setString(7, moduleItem);
            pstmt.setInt(8, isSystem);
            pstmt.setInt(9, td);
            pstmt.setInt(10, orderInTd);
            pstmt.setInt(11, wordCount);
            pstmt.setLong(12, portalId);
            pstmt.setLong(13, systemId);
            pstmt.setInt(14, canDelete?1:0);
            pstmt.setString(15, metaData);
            pstmt.setString(16, icon);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                UserDesktopSetupCache userDesktopItemCache = new UserDesktopSetupCache(this);
                userDesktopItemCache.refreshCreate();
            }
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

    public UserDesktopSetupDb getUserDesktopSetupDb(int id) {
        return (UserDesktopSetupDb)getObjectDb(new Integer(id));
    }

    public boolean del() throws ErrMsgException, ResKeyException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                re = conn.executePreUpdate() >= 0 ? true : false;
                UserDesktopSetupCache userDesktopItemCache = new UserDesktopSetupCache(this);
                userDesktopItemCache.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey primaryKey) {
        return new UserDesktopSetupDb(primaryKey.getIntValue());
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    userName = StrUtil.getNullStr(rs.getString(1));
                    title = StrUtil.getNullStr(rs.getString(2));
                    moreUrl = StrUtil.getNullStr(rs.getString(3));
                    rows = rs.getInt(4);
                    moduleCode = StrUtil.getNullStr(rs.getString(5));
                    moduleItem = StrUtil.getNullStr(rs.getString(6));
                    isSystem = rs.getInt(7);
                    td = rs.getInt(8);
                    orderInTd = rs.getInt(9);
                    wordCount = rs.getInt(10);
                    portalId = rs.getLong(11);
                    systemId = rs.getInt(12);
                    canDelete = rs.getInt(13)==1;
                    metaData = StrUtil.getNullStr(rs.getString(14));
                    icon = StrUtil.getNullStr(rs.getString(15));
                    loaded = true;
                    primaryKey.setValue(new Integer(id));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean save() throws ErrMsgException, ResKeyException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, title);
            pstmt.setString(2, moreUrl);
            pstmt.setInt(3, rows);
            pstmt.setString(4, moduleCode);
            pstmt.setString(5, moduleItem);
            pstmt.setInt(6, isSystem);
            pstmt.setInt(7, td);
            pstmt.setInt(8, orderInTd);
            pstmt.setInt(9, wordCount);
            pstmt.setInt(10, canDelete?1:0);
            pstmt.setString(11, metaData);
            pstmt.setString(12, icon);
            pstmt.setInt(13, id);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                UserDesktopSetupCache userDesktopItemCache = new UserDesktopSetupCache(this);
                primaryKey.setValue(new Integer(id));
                userDesktopItemCache.refreshSave(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public int getId() {
        return id;
    }

    public int getIsSystem() {
        return isSystem;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleItem() {
        return moduleItem;
    }

    public int getOrderInTd() {
        return orderInTd;
    }

    public String getMoreUrl() {
        return moreUrl;
    }

    public int getRows() {
        return rows;
    }

    public int getTd() {
        return td;
    }

    public String getTitle() {
        return title;
    }

    public String getUserName() {
        return userName;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTd(int td) {
        this.td = td;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setOrderInTd(int orderInTd) {
        this.orderInTd = orderInTd;
    }

    public void setMoreUrl(String moreUrl) {
        this.moreUrl = moreUrl;
    }

    public void setModuleItem(String moduleItem) {
        this.moduleItem = moduleItem;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public void setIsSystem(int isSystem) {
        this.isSystem = isSystem;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public void delDesktopOfPortal(long portalId) {
        JdbcTemplate jt = new JdbcTemplate(new com.cloudwebsoft.framework.db.Connection(cn.js.fan.web.Global.getDefaultDB()));
        String sql = "delete from user_desktop_setup where portal_id=?";
                
        try {
            jt.executeUpdate(sql, new Object[] {new Long(portalId)});
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("deleteDesktopOfUser:" + e.getMessage());
        }
    }

    /**
     * 初始化用户桌面项，用于PortalDb.init
     * @param pd PortalDb 用于复制的门户实例
     * @param portalId long 门户ID
     * @param userName String 用户名
     */
    public void initDesktopOfUser(PortalDb pd, long portalId, String userName) {        
        String listsql = "select id from user_desktop_setup where portal_id=" + pd.getLong("id");
        
        LogUtil.getLog(getClass()).info("initDesktopOfUdser listsql=" + listsql);
    	
        JdbcTemplate jt = new JdbcTemplate(new com.cloudwebsoft.framework.db.
                                           Connection(cn.js.fan.web.Global.
                                                      getDefaultDB()));
        try {
        	Vector v = list(listsql);
            Iterator ir = v.iterator();
            while (ir.hasNext()) { 
				UserDesktopSetupDb udsd = (UserDesktopSetupDb) ir.next();
				
				long systemId = udsd.getId();
				
				int id = (int) SequenceManager
						.nextID(SequenceManager.OA_USER_DESKTOP_SETUP);
				String sql = "INSERT INTO user_desktop_setup(ID, USER_NAME, TITLE, module_rows, MODULE_CODE,";
				sql += "MODULE_ITEM, IS_SYSTEM, TD,";
				sql += "order_in_td, word_count, portal_id, system_id, can_delete, meta_data, icon) VALUES ";
				sql += "(" + id + ", " + StrUtil.sqlstr(userName) + ","
						+ StrUtil.sqlstr(udsd.getTitle()) + "," + udsd.getRows() + ", "
						+ StrUtil.sqlstr(udsd.getModuleCode()) + "," + StrUtil.sqlstr(udsd.getModuleItem()) + ",0,"
						+ udsd.getTd() + "," + udsd.getOrderInTd() + ","
						+ udsd.getWordCount() + "," + portalId + "," + systemId + "," + (udsd.isCanDelete()?1:0) + "," + StrUtil.sqlstr(metaData) + "," + StrUtil.sqlstr(icon) + ")";
				jt.addBatch(sql);
            }

            if (v.size()>0)
            	jt.executeBatch();
            else
            	jt.close();

            UserDesktopSetupCache udsc = new UserDesktopSetupCache(this);
            udsc.refreshList();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("initDesktopOfUser:" + e.getMessage());
        }
    }

    /**
     * 将管理员桌面上的某项复制至所有其它用户的桌面
     * 在运用门户后,已弃用
     * @deprecated
     */
    public void copyToAllOtherUsersDesktop(String userName) {
        UserDb ud = new UserDb();
        Iterator ir = ud.list().iterator();
        while (ir.hasNext()) {
            ud = (UserDb)ir.next();
            if (!ud.getName().equals(userName)) {
                UserDesktopSetupDb uds = new UserDesktopSetupDb();
                uds.setUserName(ud.getName());
                uds.setTitle(title);
                uds.setMoreUrl(moreUrl);
                uds.setRows(rows);
                uds.setModuleCode(moduleCode);
                uds.setModuleItem(moduleItem);
                uds.setIsSystem(isSystem);
                uds.setTd(td);
                uds.setOrderInTd(orderInTd);

                uds.setModuleCode(moduleCode);
                uds.setModuleItem(moduleItem);
                uds.setWordCount(wordCount);
                
                uds.setSystemId(systemId);
                uds.setCanDelete(canDelete);
                uds.setMetaData(metaData);
                
    			uds.setIcon(icon);

                uds.create();
            }
        }
    }
    
    /**
     * 將系統門戶的桌面項覆製到其他用戶的門戶桌面上
     */
    public void copyToUsersDesktop() {
        UserDb ud = new UserDb();
        Iterator ir = ud.list().iterator();
        while (ir.hasNext()) {
            ud = (UserDb)ir.next();
            if (!ud.isValid())
            	continue;

            PortalDb pd = new PortalDb();
            pd = pd.getPortal(ud.getName(), portalId);
            if (pd==null)
            	continue;
            
			UserDesktopSetupDb uds = new UserDesktopSetupDb();
			uds.setUserName(ud.getName());
			uds.setTitle(title);
			uds.setMoreUrl(moreUrl);
			uds.setRows(rows);
			uds.setModuleCode(moduleCode);
			uds.setModuleItem(moduleItem);
			uds.setIsSystem(isSystem);
			uds.setTd(td);
			uds.setOrderInTd(orderInTd);

			uds.setModuleCode(moduleCode);
			uds.setModuleItem(moduleItem);
			uds.setWordCount(wordCount);

			uds.setSystemId(systemId);
			uds.setCanDelete(canDelete);
			uds.setPortalId(pd.getLong("id"));
			uds.setMetaData(metaData);
			
			uds.setIcon(icon);

			uds.create();
        }
    }    

    public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}

	public int getSystemId() {
		return systemId;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public String getMetaData() {
		return metaData;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	private int wordCount = 60;
}
