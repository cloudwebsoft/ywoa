package com.redmoon.oa.security;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.db.SequenceManager;

public class ServerIPPriv extends ObjectRaw {
    private String name = "";
    private int serverIPId = -1;

    int id, login = 0;
    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_LOGIN = 0;

    public ServerIPPriv(int id) {
        this.id = id;
        load();
        init();
    }

    public ServerIPPriv() {
        init();
    }

    public ServerIPPriv(String ip) {
        ServerIPDb sid = new ServerIPDb();
        sid = sid.getServerIPPrivByIP(ip);
        if (sid!=null)
            serverIPId = sid.getInt("id");

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
                "select id from oa_server_ip_priv where server_ip_id=? order by priv_type desc, name";
    }

    public void setQueryLoad() {
        QUERY_LOAD =
                "select server_ip_id,name,priv_type,login from oa_server_ip_priv where id=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from oa_server_ip_priv where id=?";
    }

    public void setQuerySave() {
        QUERY_SAVE =
                "update oa_server_ip_priv set login=? where id=?";
    }

    public void setQueryCreate() {
        QUERY_ADD =
                "insert into oa_server_ip_priv (name,priv_type,login,server_ip_id) values (?,?,?,?)";
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
                serverIPId = rs.getInt(1);
                name = rs.getString(2);
                type = rs.getInt(3);
                login = rs.getInt(4);
                loaded = true;
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
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

    private int type = 0;

    public synchronized boolean save() {
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, login);
            ps.setInt(2, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }

    public boolean canUserLogin(String username) {
        // 如果在后台没有设定服务器的准入权限，则判定为可以访问
        if (serverIPId == -1)
            return true;

        if (username==null)
            username = "";
        
        // if (username.equals(UserDb.ADMIN))
        //     return true;
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null)
            user = user.getUserDb(username);
        if (user.isLoaded())
            groups = user.getGroups();

        return canUserDo(user, groups, user.getRoles(), PRIV_LOGIN);
    }

    /**
     * 检查用户权限
     * @return boolean
     */
    public boolean canUserDo(UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        // list该节点的所有拥有权限的用户
        Vector r = list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            ServerIPPriv sip = (ServerIPPriv) ir.next();
            //　权限项对应的是组用户
            if (sip.getType() == ServerIPPriv.TYPE_USERGROUP) {
                // 组为everyone
                if (sip.getName().equals(UserGroupDb.EVERYONE)) {
                    if (privType==PRIV_LOGIN) {
                        if (sip.getLogin() == 1)
                            return true;
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(sip.getName())) {
                                if (privType == PRIV_LOGIN) {
                                    if (sip.getLogin() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (sip.getType()== TYPE_ROLE) {
                if (sip.getName().equals(RoleDb.CODE_MEMBER)) {
                    if (privType==PRIV_LOGIN) {
                        if (sip.getLogin() == 1)
                            return true;
                    }
                }
                else {
                    if (roles != null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                            if (roles[i].getCode().equals(sip.getName())) {
                                if (privType == PRIV_LOGIN) {
                                    if (sip.getLogin() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (sip.getType()== TYPE_USER) { //　个人用户
                if (sip.getName().equals(user.getName())) {
                    if (privType == PRIV_LOGIN) {
                        if (sip.getLogin() == 1)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean add(String name, int type, int serverIPId) throws
            ErrMsgException {
        // 检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            login = 1;
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, login);
            ps.setInt(4, serverIPId);
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("add:" + StrUtil.trace(e));
            throw new ErrMsgException("请检查是否有重复项存在！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public ServerIPPriv getServerIPPriv(int id) {
        ServerIPPriv sip = null;
        try {
            sip = new ServerIPPriv(id);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return sip;
    }

    public boolean delPrivsOfServer(int serverIPId) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from oa_server_ip_priv where server_ip_id=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setInt(1, serverIPId);
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

    public int getLogin() {
        return login;
    }

    public void setLogin(int login) {
        this.login = login;
    }

    public ListResult list(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
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
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); //尽量减少内存的使用

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
                    ServerIPPriv lp = getServerIPPriv(rs.getInt(1));
                    result.addElement(lp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
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
                    ServerIPPriv sip = getServerIPPriv(rs.getInt(1));
                    result.addElement(sip);
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
     * @param sql String
     * @return Vector
     */
    public Vector list() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setInt(1, serverIPId);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    ServerIPPriv lp = getServerIPPriv(rs.getInt(1));
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
     * 取得与leafCode节点相关的角色
     * @param leafCode String
     * @return RoleDb
     */
    public Vector getRolesOfServerIP(int serverIPId) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from oa_server_ip_priv where server_ip_id=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, serverIPId);
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
            LogUtil.getLog(getClass()).error("getRolesOfLeafPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }


    public boolean setRoles(String roleCodes, int serverIPId) throws ErrMsgException {
        String[] ary = StrUtil.split(roleCodes, ",");
        int len = 0;
        if (ary!=null) {
            len = ary.length;
        }
        String sql = "select id from oa_server_ip_priv where server_ip_id=? and priv_type=?";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, serverIPId);
            ps.setInt(2, TYPE_ROLE);
            rs = conn.executePreQuery();
            // 删除原来的role
            while (rs.next()) {
                getServerIPPriv(rs.getInt(1)).del();
            }
            for (int i=0; i<len; i++) {
                add(ary[i], TYPE_ROLE, serverIPId);
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

    public int getServerIPId() {
        return serverIPId;
    }

    private boolean loaded = false;
}
