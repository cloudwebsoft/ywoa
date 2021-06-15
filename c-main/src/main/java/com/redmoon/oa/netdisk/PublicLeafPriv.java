package com.redmoon.oa.netdisk;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.*;

/**
 *
 * <p>Title: 公共共享目录权限的管理</p>
 *
 * <p>Description: 只用了canUserManage中的canUserModify判别是否具有管理的权限，其它判别权限的方法未用到</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PublicLeafPriv extends ObjectDbA {
    private String dirCode = "", name = "";
    int id, see = 1, append = 0, del = 0, modify = 0;
    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_SEE = 0;
    public static final int PRIV_APPEND = 1;
    public static final int PRIV_MODIFY = 2;
    public static final int PRIV_DEL = 3;
    public static final int PRIV_EXAMINE = 4;

    public PublicLeafPriv(int id) {
        this.id = id;
        load();
        init();
    }

    public PublicLeafPriv() {
        init();
    }

    public PublicLeafPriv(String dirCode) {
        this.dirCode = dirCode;
        init();
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
     * 取得与leafCode节点相关的
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
            String sql = "select name from netdisk_public_dir_priv where dir_code=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
            ps.setInt(2, PublicLeafPriv.TYPE_ROLE);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(rm.getRoleDb(rs.getString(1)));
                }
            }
        } catch (Exception e) {
            logger.error("getRolesOfLeafPriv: " + e.getMessage());
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

    public String getTypeDesc() {
        if (type==PublicLeafPriv.TYPE_USER)
            return "用户";
        else if (type==PublicLeafPriv.TYPE_USERGROUP)
            return "用户组";
        else if (type==PublicLeafPriv.TYPE_ROLE)
            return "角色";
        else
            return "";
    }

    @Override
	public void setQueryList() {
        QUERY_LIST =
                "select id from netdisk_public_dir_priv where dir_code=? order by priv_type desc, name";
    }

    @Override
	public void setQueryLoad() {
        QUERY_LOAD =
                "select dir_code,name,priv_type,see,append,del,edit,examine from netdisk_public_dir_priv where id=?";
    }

    @Override
	public void setQueryDel() {
        QUERY_DEL = "delete from netdisk_public_dir_priv where id=?";
    }

    @Override
	public void setQuerySave() {
        QUERY_SAVE =
                "update netdisk_public_dir_priv set see=?,append=?,del=?,edit=?,examine=? where id=?";
    }

    @Override
	public void setQueryAdd() {
        QUERY_ADD =
                "insert into netdisk_public_dir_priv (name,priv_type,see,append,del,edit,examine,dir_code) values (?,?,?,?,?,?,?,?)";
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
            logger.error("load: " + e.getMessage());
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
        return "PublicLeafPriv is " + dirCode + ":" + name;
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
        } catch (SQLException e) {
            logger.error("修改出错！");
        }
        return r == 1 ? true : false;
    }

    public boolean canUserManage(String username) {
        return canUserExamine(username);
    }
    
    public boolean canUserSeeByAncestor(String username) {
    	return canUserDo(username, PRIV_SEE);
    }     

    public boolean canUserSee(String username) {
        if (username==null)
            username = "";
        if (username.equals(UserDb.ADMIN))
            return true;
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null)
            user = user.getUserDb(username);
        if (user.isLoaded())
            groups = user.getGroups();
        PublicLeaf lf = new PublicLeaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PublicLeafPriv.PRIV_SEE);
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @param leaf PublicLeaf 节点
     * @param username String
     * @return boolean
     */
    public boolean canUserDo(PublicLeaf leaf, UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        PublicLeafPriv leafPriv = new PublicLeafPriv(leaf.getCode());
        // list该节点的所有拥有权限的用户
        Vector r = leafPriv.list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            PublicLeafPriv lp = (PublicLeafPriv) ir.next();
            //　权限项对应的是组用户
            if (lp.getType() == PublicLeafPriv.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    if (privType==PublicLeafPriv.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==PublicLeafPriv.PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==PublicLeafPriv.PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==PublicLeafPriv.PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==PublicLeafPriv.PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
                            return true;
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (privType == PublicLeafPriv.PRIV_APPEND) {
                                    if (lp.getAppend() == 1)
                                        return true;
                                } else if (privType == PublicLeafPriv.PRIV_DEL) {
                                    if (lp.getDel() == 1)
                                        return true;
                                } else if (privType == PublicLeafPriv.PRIV_MODIFY) {
                                    if (lp.getModify() == 1)
                                        return true;
                                } else if (privType == PublicLeafPriv.PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                else if (privType == PublicLeafPriv.PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==PublicLeafPriv.TYPE_ROLE) {
                if (roles!=null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                            if (roles[i].getCode().equals(lp.getName())) {
                                if (privType == PublicLeafPriv.PRIV_APPEND) {
                                    if (lp.getAppend() == 1)
                                        return true;
                                } else if (privType == PublicLeafPriv.PRIV_DEL) {
                                    if (lp.getDel() == 1)
                                        return true;
                                } else if (privType == PublicLeafPriv.PRIV_MODIFY) {
                                    if (lp.getModify() == 1)
                                        return true;
                                } else if (privType == PublicLeafPriv.PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                else if (privType == PublicLeafPriv.PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                }
            }
            else if (lp.getType()==PublicLeafPriv.TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                    if (privType == PublicLeafPriv.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    } else if (privType == PublicLeafPriv.PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    } else if (privType == PublicLeafPriv.PRIV_MODIFY) {
                        if (lp.getModify() == 1)
                            return true;
                    } else if (privType == PublicLeafPriv.PRIV_SEE) {
                        if (lp.getSee() == 1)
                            return true;
                    } else if (privType == PublicLeafPriv.PRIV_EXAMINE) {
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
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, Privilege.ADMIN)) {
            return true;
        }

        UserDb user = new UserDb();
        user = user.getUserDb(username);

        //　判断用户是否具有管理员的权限
        String[] privs = user.getPrivs();
        if (privs!=null) {
            int len = privs.length;
            for (int i=0; i<len; i++) {
                // 拥有管理员权限
                if (privs[i].equals(PrivDb.PRIV_ADMIN))
                    return true;
            }
        }

        UserGroupDb[] groups = user.getGroups();
        RoleDb[] roles = user.getRoles();

        // 如果属于管理员组,则拥有全部权
        // logger.info("groups[i].code=" + groups[i].getCode());
        // for (int i = 0; i < groups.length; i++)
        //    if (groups[i].getCode().equals(groups[i].ADMINISTRATORS))
        //        return true;

        PublicLeaf lf = new PublicLeaf();
        lf = lf.getLeaf(dirCode);
        // logger.info("dirCode=" + dirCode + " name=" + lf.getName() + " code=" + lf.getCode() + " parentCode=" + lf.getParentCode());

        if (canUserDo(lf, user, groups, roles, privType))
            return true;

        // 回溯其父节点，判别用户对其父节点是否有权限，回溯可到达根root节点
        String parentCode = lf.getParentCode();

        // while (!parentCode.equals("-1") && !parentCode.equals("root")) {
        while (!parentCode.equals("-1")) {
            // logger.info("dirCode=" + dirCode + " parentCode=" + parentCode);
            PublicLeaf plf = lf.getLeaf(parentCode);
            if (plf==null || !plf.isLoaded())
                return false;
            if (canUserDo(plf, user, groups, roles, privType))
                return true;
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

    public boolean canUserExamine(String username) {
        return canUserDo(username, PRIV_EXAMINE);
    }

    /**
     * 赋予everyone可见的权限
     * @param dirCode
     * @return
     * @throws ErrMsgException
     */
    public boolean add(String dirCode) throws
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
            ps.setString(8, dirCode);
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("LeafPriv:数据库操作出错！");
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
        String sql = "select id from netdisk_public_dir_priv where dir_code=? and priv_type=?";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
            ps.setInt(2, TYPE_ROLE);
            rs = conn.executePreQuery();
            // 删除原来的role
            while (rs.next()) {
                getPublicLeafPriv(rs.getInt(1)).del();
            }
            for (int i=0; i<len; i++) {
                add(ary[i], TYPE_ROLE);
            }
        } catch (SQLException e) {
            logger.error("setRoles:" + e.getMessage());
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
            ps.setString(8, dirCode);
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("add:" + e.getMessage());
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

    public PublicLeafPriv getPublicLeafPriv(int id) {
        PublicLeafPriv leafPriv = null;
        try {
            leafPriv = new PublicLeafPriv(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return leafPriv;
    }

    public boolean delPrivsOfDir() {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from netdisk_public_dir_priv where dir_code=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setString(1, dirCode);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
        return r;
    }

    public boolean delPrivsOfUserOrGroup(String username) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from netdisk_public_dir_priv where name=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setString(1, username);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
        } catch (SQLException e) {
            logger.error(e.getMessage());
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

    @Override
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
                    PublicLeafPriv lp = getPublicLeafPriv(rs.getInt(1));
                    result.addElement(lp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
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
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public Vector listUserPriv(String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from netdisk_public_dir_priv where name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    PublicLeafPriv lp = getPublicLeafPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            logger.error("listUserPriv: " + e.getMessage());
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
    @Override
	public Vector list() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setString(1, dirCode);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    PublicLeafPriv lp = getPublicLeafPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            logger.error("list: " + e.getMessage());
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
