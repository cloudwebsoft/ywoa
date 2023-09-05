package com.redmoon.oa.basic;

import cn.js.fan.base.ObjectDbA;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RoleMgr;
import com.redmoon.oa.pvg.UserGroupDb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class SelectKindPriv extends ObjectDbA {
    private int kindId;
    private String name = "";
    int id, see = 1, append = 1, del = 0, modify = 1;
    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;
    public static final int PRIV_SEE = 0;
    public static final int PRIV_APPEND = 1;
    public static final int PRIV_MODIFY = 2;
    public static final int PRIV_DEL = 3;
    public static final int PRIV_EXAMINE = 4;

    public SelectKindPriv(int id) {
        this.id = id;
        load();
        init();
    }

    public SelectKindPriv() {
        init();
    }

    public void init() {
        super.init();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 取得与leafCode节点相关的?
     * @return RoleDb
     */
    public Vector getRolesOfSelectKindPriv(int kindId) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from oa_select_kind_priv where kind_id=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, kindId);
            ps.setInt(2, TYPE_ROLE);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(rm.getRoleDb(rs.getString(1)));
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getRolesOfSelectKindPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    public String getTypeDesc() {
        if (type==TYPE_USER)
            return "用户";
        else if (type==TYPE_USERGROUP)
            return "用户组";
        else if (type==TYPE_ROLE)
            return "角色";
        else
            return "";
    }

    public void setQueryList() {
        QUERY_LIST =
                "select id from oa_select_kind_priv where kind_id=? order by priv_type desc, name";
    }

    public void setQueryLoad() {
        QUERY_LOAD =
                "select kind_id,name,priv_type,see,append,del,dir_modify,examine from oa_select_kind_priv where id=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from oa_select_kind_priv where id=?";
    }

    public void setQuerySave() {
        QUERY_SAVE =
                "update oa_select_kind_priv set see=?,append=?,del=?,dir_modify=?,examine=? where id=?";
    }

    public void setQueryAdd() {
        QUERY_ADD =
                "insert into oa_select_kind_priv (name,priv_type,see,append,del,dir_modify,examine,kind_id) values (?,?,?,?,?,?,?,?)";
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                kindId = rs.getInt(1);
                name = rs.getString(2);
                type = rs.getInt(3);
                see = rs.getInt(4);
                append = rs.getInt(5);
                del = rs.getInt(6);
                modify = rs.getInt(7);
                examine = rs.getInt(8);
                loaded = true;
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            /*
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public int getKindId() {
        return kindId;
    }

    public void setKindId(int kindId) {
        this.kindId = kindId;
    }

    public String getName() {
        return name;
    }

    public void setType(int t) {
        this.type = t;
    }

    public void setName(String n) {
        this.name = n;
    }

    public int getType() {
        return type;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String toString() {
        return "SelectKindPriv is " + kindId + ":" + name;
    }

    private int type = 0;

    public synchronized boolean save() {
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, see);
            ps.setInt(2, append);
            ps.setInt(3, del);
            ps.setInt(4, modify);
            ps.setInt(5, examine);
            ps.setInt(6, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("数据库错误！");
        }
        return r == 1 ? true : false;
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @return boolean
     */
    public boolean canUserDo(SelectKindDb skd, UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        SelectKindPriv leafPriv = new SelectKindPriv();
        leafPriv.setKindId(skd.getId());
        // list该节点的所有拥有权限的用户?
        Vector r = leafPriv.listPriv(privType);
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
        	SelectKindPriv lp = (SelectKindPriv) ir.next();
            // 权限项对应的是组用户?
            if (lp.getType() == TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    if (privType==PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
                            return true;
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (privType == PRIV_APPEND) {
                                    if (lp.getAppend() == 1)
                                        return true;
                                } else if (privType == PRIV_DEL) {
                                    if (lp.getDel() == 1)
                                        return true;
                                } else if (privType == PRIV_MODIFY) {
                                    if (lp.getModify() == 1)
                                        return true;
                                } else if (privType == PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                else if (privType == PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==TYPE_ROLE) {
                if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
                    if (privType==PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
                            return true;
                    }
                } 
                else {
	                if (roles!=null) {
	                        // 判断该用户所属的角色是否有权限?
	                        int len = roles.length;
	                        for (int i = 0; i < len; i++) {
	                            if (roles[i].getCode().equals(lp.getName())) {
	                                if (privType == PRIV_APPEND) {
	                                    if (lp.getAppend() == 1)
	                                        return true;
	                                } else if (privType == PRIV_DEL) {
	                                    if (lp.getDel() == 1)
	                                        return true;
	                                } else if (privType == PRIV_MODIFY) {
	                                    if (lp.getModify() == 1)
	                                        return true;
	                                } else if (privType == PRIV_SEE) {
	                                    if (lp.getSee() == 1)
	                                        return true;
	                                }
	                                else if (privType == PRIV_EXAMINE) {
	                                    if (lp.getExamine() == 1)
	                                        return true;
	                                }
	                                break;
	                            }
	                        }
	                }
                }
            }
            else if (lp.getType()==TYPE_USER) { // 个人用户
                if (lp.getName().equals(user.getName())) {
                    if (privType == PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    } else if (privType == PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    } else if (privType == PRIV_MODIFY) {
                        if (lp.getModify() == 1)
                            return true;
                    } else if (privType == PRIV_SEE) {
                        if (lp.getSee() == 1)
                            return true;
                    } else if (privType == PRIV_EXAMINE) {
                        if (lp.getExamine() == 1)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canUserDo(String username, int privType) {
        if (username==null)
            return false;
        
        UserDb user = new UserDb();
        user = user.getUserDb(username);

        UserGroupDb[] groups = user.getGroups();
        RoleDb[] roles = user.getRoles();
        SelectKindDb skd = new SelectKindDb();
        skd = skd.getSelectKindDb(kindId);

        return canUserDo(skd, user, groups, roles, privType);
    }

    /**
     * 添加权限
     * @param username String
     * @return boolean
     */
    public boolean canUserAppend(String username, int kindId) {
        if (username.equals(UserDb.ADMIN))
            return true;    	
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN))
        	return true;    	
    	this.kindId = kindId;

    	return canUserDo(username, PRIV_APPEND);
    }

    public boolean canUserDel(String username, int kindId) {
        if (username.equals(UserDb.ADMIN))
            return true;    	
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN))
        	return true;    	

        this.kindId = kindId;

        return canUserDo(username, PRIV_DEL);
    }

    public boolean canUserModify(String username, int kindId) {
        if (username.equals(UserDb.ADMIN))
            return true;    	
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN))
        	return true;    	
        
    	this.kindId = kindId;

        return canUserDo(username, PRIV_MODIFY);
    }

    /**
     * 判别用户是否具有查看的权限，如：用于flow_modify.jsp，查看流程过程
     * @param username
     * @return
     */
    public boolean canUserExamine(String username, int kindId) {
        if (username.equals(UserDb.ADMIN))
            return true;    	
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN))
        	return true;
        
    	this.kindId = kindId;
        
        return canUserDo(username, PRIV_EXAMINE);
    }

    public boolean add(String kind) throws
            ErrMsgException {
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, UserGroupDb.EVERYONE);
            ps.setInt(2, TYPE_USERGROUP);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, del);
            ps.setInt(6, modify);
            ps.setInt(7, examine);
            ps.setInt(8, kindId);
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("SelectKindPriv:数据库操作出错");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public boolean setRoles(String leafCode, String roleCodes) throws ErrMsgException {
        String[] ary = StrUtil.split(roleCodes, ",");
        int len = 0;
        if (ary!=null) {
            len = ary.length;
        }
        String sql = "select id,name from oa_select_kind_priv where kind_id=? and priv_type=?";

        Map mapExist = new HashMap();
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
            ps.setInt(2, TYPE_ROLE);
            rs = conn.executePreQuery();
            while (rs.next()) {
            	// 检查在新选择的角色中是否存在，如不存在则删除
            	boolean isFound = false;
            	String roleCode = rs.getString(2);
                for (int i=0; i<len; i++) {
                	if (roleCode.equals(ary[i])) {
                		isFound = true;
                		mapExist.put(ary[i], "");
                		break;
                	}
                }
                if (!isFound) {
                	getSelectKindPriv(rs.getInt(1)).del();
                }
            }
            for (int i=0; i<len; i++) {
            	if (!mapExist.containsKey(ary[i])) {
            		add(ary[i], TYPE_ROLE);
            	}
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("setRoles:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean add(String name, int type) throws
            ErrMsgException {
        // 检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, del);
            ps.setInt(6, modify);
            ps.setInt(7, examine);
            ps.setInt(8, kindId);
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("add:" + e.getMessage());
            throw new ErrMsgException("请检查是否有重复项存在?");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public SelectKindPriv getSelectKindPriv(int id) {
    	SelectKindPriv sPriv = null;
        try {
            sPriv = new SelectKindPriv(id);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return sPriv;
    }

    public boolean delPrivsOfKind(int kindId) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from oa_select_kind_priv where dir_code=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setInt(1, kindId);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        }
        return r;
    }

    public boolean delPrivsOfKind() {
        return delPrivsOfKind(kindId);
    }

    public boolean delPrivsOfUserOrGroup(String username) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from oa_select_kind_priv where name=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setString(1, username);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        }
        return r;
    }

    public boolean del() {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = rmconn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        }
        return r;
    }

    public int getSee() {
        return see;
    }

    public void setSee(int see) {
        this.see = see;
    }

    public int getAppend() {
        return append;
    }

    public void setAppend(int a) {
        this.append = a;
    }

    public int getDel() {
        return del;
    }

    public void setDel(int d) {
        this.del = d;
    }

    public int getModify() {
        return modify;
    }

    public int getExamine() {
        return examine;
    }

    public void setModify(int m) {
        this.modify = m;
    }

    public void setExamine(int examine) {
        this.examine = examine;
    }

    public ListResult list(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        Conn conn = new Conn(connname);
        try {
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize);

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return null;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return null;
                }
                do {
                    SelectKindPriv lp = getSelectKindPriv(rs.getInt(1));
                    result.addElement(lp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }
    
    /**
     * 取得节点上具有某权限的记录，用于优化canUserDo的效率
     * @param priv
     * @return
     */
    public Vector listPriv(int priv) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_select_kind_priv where kind_id=? and see=1";
            if (priv==PRIV_APPEND) {
                sql = "select id from oa_select_kind_priv where kind_id=? and append=1";
            }
            else if (priv==PRIV_DEL) {
                sql = "select id from oa_select_kind_priv where kind_id=? and del=1";
            }
            else if (priv==PRIV_MODIFY) {
                sql = "select id from oa_select_kind_priv where kind_id=? and dir_modify=1";
            }
            else if (priv==PRIV_EXAMINE) {
                sql = "select id from oa_select_kind_priv where kind_id=? and examine=1"; 
            }            
        	
            ps = conn.prepareStatement(sql);
            ps.setInt(1, kindId);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                	SelectKindPriv lp = getSelectKindPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    public Vector listUserPriv(String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_select_kind_priv where name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    SelectKindPriv skp = getSelectKindPriv(rs.getInt(1));
                    result.addElement(skp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("listUserPriv: " + e.getMessage());
        } finally {
            /*
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * 取出全部信息置于result中
     * @param sql String
     * @return Vector
     */
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    SelectKindPriv lp = getSelectKindPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * 取出全部信息置于result中
     * @return Vector
     */
    public Vector list() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setInt(1, kindId);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                	SelectKindPriv lp = getSelectKindPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
            /*
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    private boolean loaded = false;
    private int examine = 0;

}
