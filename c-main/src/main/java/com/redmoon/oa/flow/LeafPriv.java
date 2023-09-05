package com.redmoon.oa.flow;

import java.sql.*;
import java.util.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.*;

import javax.servlet.http.HttpServletRequest;

public class LeafPriv extends ObjectDb {
    private String dirCode = "", name = "";
    int id;
    /**
     * 流程发起
     */
    int see = 1;
    int append = 0; // 冗余
    int del = 0; // 冗余
    /**
     * 流程查询
     */
    int modify = 0;
    /**
     * 流程管理
     */
    private int examine = 0;

    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_SEE = 0;
    public static final int PRIV_APPEND = 1;
    public static final int PRIV_MODIFY = 2;
    public static final int PRIV_DEL = 3;
    public static final int PRIV_EXAMINE = 4;

    public LeafPriv(int id) {
        this.id = id;
        load();
        init();
    }

    public LeafPriv() {
        init();
    }

    public LeafPriv(String dirCode) {
        this.dirCode = dirCode;
        init();
    }

    @Override
    public void initDB() {
        objectCache = new LeafPrivCache(this);
        tableName = "flow_dir_priv";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        isInitFromConfigDB = false;
        QUERY_LIST =
                "select id from flow_dir_priv where dir_code=? order by priv_type desc, name";
        QUERY_LOAD =
                "select dir_code,name,priv_type,see,append,del,dir_modify,examine from flow_dir_priv where id=?";

        QUERY_DEL = "delete from flow_dir_priv where id=?";

        QUERY_SAVE =
                "update flow_dir_priv set see=?,append=?,del=?,dir_modify=?,examine=? where id=?";
        QUERY_CREATE =
                "insert into flow_dir_priv (name,priv_type,see,append,del,dir_modify,examine,dir_code) values (?,?,?,?,?,?,?,?)";
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new LeafPriv(pk.getIntValue());
    }

    @Override
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
     * 取得与leafCode节点相关的角色
     * @param leafCode String
     * @return RoleDb
     */
    public Vector getRolesOfLeafPriv(String leafCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from flow_dir_priv where dir_code=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
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

    public String getTypeDesc() {
        if (type==TYPE_USER) {
            return "用户";
        } else if (type==TYPE_USERGROUP) {
            return "用户组";
        } else if (type==TYPE_ROLE) {
            return "角色";
        } else {
            return "";
        }
    }

    @Override
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                dirCode = rs.getString(1);
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
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String getDirCode() {
        return dirCode;
    }

    public void setDirCode(String code) {
        this.dirCode = code;
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

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String toString() {
        return "Flow LeafPriv is " + dirCode + ":" + name;
    }

    private int type = 0;

    @Override
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
            if (r==1) {
                LeafPrivCache leafPrivCache = new LeafPrivCache(this);
                /*primaryKey.setValue(id);
                leafPrivCache.refreshSave(primaryKey);*/
                leafPrivCache.refreshList();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        return r == 1 ? true : false;
    }

    /**
     * 判别是否具有发起流程的权限
     * @param username String
     * @return boolean
     */
    public boolean canUserSee(String username) {
        // 根目录对所有人可见
        if (dirCode.equals(Leaf.CODE_ROOT)) {
            return true;
        }
        if (username==null) {
            return false;
        }
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN)) {
            return true;
        }

        if (pvg.isUserPrivValid(username, "admin.unit")) {
	        // 如果是单位管理员，则可以查看本单位的流程
	        Leaf lf = new Leaf();
	        lf = lf.getLeaf(dirCode);
	        
	        // 如果流程类别已不存在
	        if (lf==null) {
                return true;
            }
	        
	        UserDb user = new UserDb();
	        user = user.getUserDb(username);
	        if (lf.getUnitCode().equals(user.getUnitCode())) {
	        	return true;
	        }
        }

        return canUserDo(username, PRIV_SEE);
    }
    
    /**
     * 判断是否具有查询的权限
     * @param username
     * @return
     */
    public boolean canUserQuery(String username) {
        // 根目录对所有人可见
        /*if (dirCode.equals(Leaf.CODE_ROOT)) {
            return true;
        }*/
        if (username==null) {
            username = "";
        }
        // LogUtil.getLog(getClass()).info("username=" + username);
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN)) {
            return true;
        }
        
        if (pvg.isUserPrivValid(username, "admin.unit")) {
	        // 如果是单位管理员，则可以查看本单位的流程
	        Leaf lf = new Leaf();
	        lf = lf.getLeaf(dirCode);
	        
	        // 如果流程类别已不存在
	        if (lf==null) {
                return true;
            }
	        
	        UserDb user = new UserDb();
	        user = user.getUserDb(username);
	        if (lf.getUnitCode().equals(user.getUnitCode())) {
	        	return true;
	        }
        }
        
        return canUserDo(username, PRIV_MODIFY);
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @param leaf
     * @param user
     * @param groups
     * @param roles
     * @param privType
     * @return
     */
    public boolean canUserDo(Leaf leaf, UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        LeafPriv leafPriv = new LeafPriv(leaf.getCode());
        // list该节点的所有拥有权限的用户?
        Vector r = leafPriv.listPriv(privType);
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            LeafPriv lp = (LeafPriv) ir.next();
            // 权限项对应的是组用户?
            if (lp.getType() == TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    if (privType==PRIV_APPEND) {
                        if (lp.getAppend() == 1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_DEL) {
                        if (lp.getDel() == 1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_MODIFY) {
                        if (lp.getModify()==1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_SEE) {
                        if (lp.getSee()==1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_EXAMINE) {
                        if (lp.getExamine()==1) {
                            return true;
                        }
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (privType == PRIV_APPEND) {
                                    if (lp.getAppend() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_DEL) {
                                    if (lp.getDel() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_MODIFY) {
                                    if (lp.getModify() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_SEE) {
                                    if (lp.getSee() == 1) {
                                        return true;
                                    }
                                }
                                else if (privType == PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1) {
                                        return true;
                                    }
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
                        if (lp.getAppend() == 1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_DEL) {
                        if (lp.getDel() == 1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_MODIFY) {
                        if (lp.getModify()==1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_SEE) {
                        if (lp.getSee()==1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_EXAMINE) {
                        if (lp.getExamine()==1) {
                            return true;
                        }
                    }
                } 
                else {
	                if (roles!=null) {
	                        // 判断该用户所属的角色是否有权限?
	                        int len = roles.length;
	                        for (int i = 0; i < len; i++) {
	                            if (roles[i].getCode().equals(lp.getName())) {
	                                if (privType == PRIV_APPEND) {
	                                    if (lp.getAppend() == 1) {
                                            return true;
                                        }
	                                } else if (privType == PRIV_DEL) {
	                                    if (lp.getDel() == 1) {
                                            return true;
                                        }
	                                } else if (privType == PRIV_MODIFY) {
	                                    if (lp.getModify() == 1) {
                                            return true;
                                        }
	                                } else if (privType == PRIV_SEE) {
	                                    if (lp.getSee() == 1) {
                                            return true;
                                        }
	                                }
	                                else if (privType == PRIV_EXAMINE) {
	                                    if (lp.getExamine() == 1) {
                                            return true;
                                        }
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
                        if (lp.getAppend() == 1) {
                            return true;
                        }
                    } else if (privType == PRIV_DEL) {
                        if (lp.getDel() == 1) {
                            return true;
                        }
                    } else if (privType == PRIV_MODIFY) {
                        if (lp.getModify() == 1) {
                            return true;
                        }
                    } else if (privType == PRIV_SEE) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                    } else if (privType == PRIV_EXAMINE) {
                        if (lp.getExamine() == 1) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean canUserDo(String username, int privType) {
        if (username==null) {
            return false;
        }
        
/*        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, Privilege.ADMIN)) {
            return true;
        }*/

        UserDb user = new UserDb();
        user = user.getUserDb(username);

        /*
        //　判断用户是否具有管理员的权限?
        String[] privs = user.getPrivs();
        if (privs!=null) {
            int len = privs.length;
            for (int i=0; i<len; i++) {
                // 拥有管理员权限
                if (privs[i].equals(PrivDb.PRIV_ADMIN))
                    return true;
            }
        }
		*/

        UserGroupDb[] groups = user.getGroups();
        RoleDb[] roles = user.getRoles();

        // 如果属于管理员组,则拥有全部权
        // LogUtil.getLog(getClass()).info("groups[i].code=" + groups[i].getCode());
        // for (int i = 0; i < groups.length; i++)
        //    if (groups[i].getCode().equals(groups[i].ADMINISTRATORS))
        //        return true;

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        // LogUtil.getLog(getClass()).info("dirCode=" + dirCode + " name=" + lf.getName() + " code=" + lf.getCode() + " parentCode=" + lf.getParentCode());

        if (canUserDo(lf, user, groups, roles, privType)) {
            return true;
        }
        
        // 如果是查看权限，不允许回溯
        if (privType==PRIV_SEE) {
            return false;
        }

        // 回溯其父节点，判别用户对其父节点是否有权限，回溯可到达根root节点
        // 假设父节点不能覆盖子节点权限，则考虑到权限不仅有查看权限，还有管理权限
        // 那么能管理父节点，就应该能管理子节点
        // 而如能查看父节点，却不一定能管理子节点
        String parentCode = lf.getParentCode();

        // while (!parentCode.equals("-1") && !parentCode.equals("root")) {
        while (!parentCode.equals("-1")) {
            Leaf plf = lf.getLeaf(parentCode);
            if (plf==null || !plf.isLoaded()) {
                return false;
            }
            // LogUtil.getLog(getClass()).info("dirCode=" + dirCode + " parentCode=" + parentCode + " " + canUserDo(plf, user, groups, roles, privType));
            if (canUserDo(plf, user, groups, roles, privType)) {
                return true;
            }
            parentCode = plf.getParentCode();
        }

        return false;
    }

    /**
     * 如果用户对某父节点有权限，则用户对这个节点的孩子节点也拥有相应的权限
     * @param username String
     * @return boolean
     */
    public boolean canUserAppend(String username) {
        return canUserDo(username, PRIV_APPEND);
    }

    public boolean canUserDel(String username) {
        return canUserDo(username, PRIV_DEL);
    }

    public boolean canUserModify(String username) {
        return canUserDo(username, PRIV_MODIFY);
    }

    /**
     * 判别用户是否具有查看的权限，如：用于flow_modify.jsp，查看流程过程
     * @param username
     * @return
     */
    public boolean canUserExamine(String username) {
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, ConstUtil.PRIV_ADMIN)) {
            return true;
        }
        
        if (pvg.isUserPrivValid(username, "admin.unit")) {
	        // 如果是单位管理员，则可以查看本单位的流程
	        Leaf lf = new Leaf();
	        lf = lf.getLeaf(dirCode);
	        
	        // 如果流程类别已不存在
	        if (lf==null) {
                return true;
            }
	        
	        UserDb user = new UserDb();
	        user = user.getUserDb(username);
	        if (lf.getUnitCode().equals(user.getUnitCode())) {
	        	return true;
	        }
        }        
        
        return canUserDo(username, PRIV_EXAMINE);
    }

    public boolean add(String dirCode) throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, UserGroupDb.EVERYONE);
            ps.setInt(2, TYPE_USERGROUP);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, del);
            ps.setInt(6, modify);
            ps.setInt(7, examine);
            ps.setString(8, dirCode);
            r = conn.executePreUpdate() == 1 ? true : false;
            if (r) {
                LeafPrivCache leafPrivCache = new LeafPrivCache(this);
                leafPrivCache.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("LeafPriv:数据库操作出错");
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
        String sql = "select id,name from flow_dir_priv where dir_code=? and priv_type=?";

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
                    getLeafPriv(rs.getInt(1)).del();
                }
            }
            for (int i=0; i<len; i++) {
                if (!mapExist.containsKey(ary[i])) {
                    add(ary[i], TYPE_ROLE);
                }
            }
            LeafPrivCache leafPrivCache = new LeafPrivCache(this);
            leafPrivCache.refreshList();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("setRoles:" + e.getMessage());
        }
        finally {
            conn.close();
            conn = null;
        }
        return true;
    }

    public boolean add(String name, int type) throws
            ErrMsgException {
        // 检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, del);
            ps.setInt(6, modify);
            ps.setInt(7, examine);
            ps.setString(8, dirCode);
            r = conn.executePreUpdate() == 1 ? true : false;
            if (r) {
                LeafPrivCache leafPrivCache = new LeafPrivCache(this);
                leafPrivCache.refreshCreate();
            }
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

    public LeafPriv getLeafPriv(int id) {
        return (LeafPriv)getObjectDb(id);
    }

    public boolean delPrivsOfDir(String dirCode) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from flow_dir_priv where dir_code=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setString(1, dirCode);
            r = rmconn.executePreUpdate() > 0 ? true : false;
            if (r) {
                LeafPrivCache leafPrivCache = new LeafPrivCache(this);
                leafPrivCache.refreshList();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        }
        return r;
    }

    @Override
    public boolean del() {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = rmconn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            r = rmconn.executePreUpdate() == 1 ? true : false;
            if (r) {
                LeafPrivCache leafPrivCache = new LeafPrivCache(this);
                primaryKey.setValue(id);
                leafPrivCache.refreshDel(primaryKey);
            }
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

    public Vector listPriv(int priv) {
        LeafPrivCache leafPrivCache = new LeafPrivCache(this);
        return leafPrivCache.listPriv(dirCode, priv);
    }

    /**
     * 取得节点上具有某权限的记录，用于优化canUserDo的效率
     * @param priv
     * @return
     */
    public Vector listPrivByDb(int priv) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from flow_dir_priv where dir_code=? and see=1";
            if (priv==PRIV_APPEND) {
                sql = "select id from flow_dir_priv where dir_code=? and append=1";
            }
            else if (priv==PRIV_DEL) {
                sql = "select id from flow_dir_priv where dir_code=? and del=1";
            }
            else if (priv==PRIV_MODIFY) {
                sql = "select id from flow_dir_priv where dir_code=? and dir_modify=1";
            }
            else if (priv==PRIV_EXAMINE) {
                sql = "select id from flow_dir_priv where dir_code=? and examine=1"; 
            }            
        	
            ps = conn.prepareStatement(sql);
            ps.setString(1, dirCode);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    LeafPriv lp = getLeafPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("listPriv: " + e.getMessage());
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
            String sql = "select id from flow_dir_priv where name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    LeafPriv lp = getLeafPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("listUserPriv: " + e.getMessage());
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
    @Override
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
                    LeafPriv lp = getLeafPriv(rs.getInt(1));
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

    public static boolean canUserQueryAll(HttpServletRequest request) {
        Privilege privilege = new Privilege();
        LeafPriv leafPriv = new LeafPriv(Leaf.CODE_ROOT);
        return privilege.isUserPrivValid(request, "admin") || leafPriv.canUserExamine(SpringUtil.getUserName()) || leafPriv.canUserQuery(SpringUtil.getUserName());
    }

    private boolean loaded = false;

}
