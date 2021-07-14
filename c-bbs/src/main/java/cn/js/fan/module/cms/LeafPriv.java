package cn.js.fan.module.cms;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.module.pvg.*;
import cn.js.fan.util.*;
import com.redmoon.forum.SequenceMgr;

public class LeafPriv extends ObjectDbA {
    private String dirCode = "", name = "";
    int id, see = 1, append = 0, del = 0, modify = 0;
    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;

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

    public void init() {
        super.init();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQueryList() {
        QUERY_LIST =
                "select id from dir_priv where dir_code=? order by priv_type, name";
    }

    public void setQueryLoad() {
        QUERY_LOAD =
                "select dir_code,name,priv_type,see,append,del,dir_modify,examine from dir_priv where id=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from dir_priv where id=?";
    }

    public void setQuerySave() {
        QUERY_SAVE =
                "update dir_priv set see=?,append=?,del=?,dir_modify=?,examine=? where id=?";
    }

    public void setQueryAdd() {
        QUERY_ADD =
                "insert into dir_priv (name,priv_type,see,append,del,dir_modify,examine,dir_code,id) values (?,?,?,?,?,?,?,?,?)";
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

    public boolean isLoaded() {
        return loaded;
    }

    public String toString() {
        return "LeafPriv is " + dirCode + ":" + name;
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
            logger.error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }

    /**
     * 是否有察看的权限（不回溯父节点）
     * @param username String
     * @return boolean
     */
    public boolean canUserSee(String username) {
        if (username==null)
            username = "";
        // logger.info("username=" + username);
        if (username.equals(User.ADMIN))
            return true;
        User user = new User();
        UserGroup[] groups = null;
        if (username != null)
            user = user.getUser(username);
        if (user.isLoaded())
            groups = user.getGroup();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, this.PRIV_SEE);
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @param leaf Leaf 节点
     * @return boolean
     */
    public boolean canUserDo(Leaf leaf, User user, UserGroup[] groups, int privType) {
        if (leaf==null)
            return false;
        LeafPriv leafPriv = new LeafPriv(leaf.getCode());
        // list该节点的所有拥有权限的用户
        Vector r = leafPriv.list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            LeafPriv lp = (LeafPriv) ir.next();
            //　权限项对应的是组用户
            if (lp.getType() == lp.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroup.EVERYONE)) {
                    if (privType==this.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==this.PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==this.PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==this.PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==this.PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
                            return true;
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (privType == this.PRIV_APPEND) {
                                    if (lp.getAppend() == 1)
                                        return true;
                                } else if (privType == this.PRIV_DEL) {
                                    if (lp.getDel() == 1)
                                        return true;
                                } else if (privType == this.PRIV_MODIFY) {
                                    if (lp.getModify() == 1)
                                        return true;
                                } else if (privType == this.PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                else if (privType == this.PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            } else { //　个人用户
                if (lp.getName().equals(user.getName())) {
                    if (privType==this.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==this.PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==this.PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==this.PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==this.PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
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
        if (username.equals(User.ADMIN))
            return true;

        User user = new User();
        user = user.getUser(username);

        //　判断用户是否具有管理员的权限
        String[] privs = user.getPrivs();
        if (privs!=null) {
            int len = privs.length;
            for (int i=0; i<len; i++) {
                // 拥有管理员权限
                if (privs[i].equals(Priv.PRIV_ADMIN))
                    return true;
            }
        }

        UserGroup[] groups = user.getGroup();

        // 如果属于管理员组,则拥有全部权限
        // logger.info("groups[i].code=" + groups[i].getCode());
        for (int i = 0; i < groups.length; i++)
            if (groups[i].getCode().equals(groups[i].Administrators))
                return true;

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf==null)
            return false;
        // logger.info("dirCode=" + dirCode + " name=" + lf.getName() + " code=" + lf.getCode() + " parentCode=" + lf.getParentCode());

        if (canUserDo(lf, user, groups, privType))
            return true;

        // 回溯其父节点，判别用户对其父节点是否有权限，回溯可到达根root节点
        String parentCode = lf.getParentCode();

        // while (!parentCode.equals("-1") && !parentCode.equals("root")) {
        while (!parentCode.equals("-1")) {
            // logger.info("dirCode=" + dirCode + " parentCode=" + parentCode);
            Leaf plf = lf.getLeaf(parentCode);
            if (canUserDo(plf, user, groups, privType))
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
        return canUserDo(username, this.PRIV_APPEND);
    }

    public boolean canUserDel(String username) {
        return canUserDo(username, this.PRIV_DEL);
    }

    public boolean canUserModify(String username) {
        return canUserDo(username, this.PRIV_MODIFY);
    }

    public boolean canUserExamine(String username) {
        return canUserDo(username, this.PRIV_EXAMINE);
    }

    /**
     * 判别用户是否具有查看的权限（回溯祖先节点），如果用户被设定不能看此节点，则判断用户是否对其祖先节点有管理权，否则不允许看到
     * @param username String
     * @return boolean
     */
    public boolean canUserSeeWithAncestorNode(String username) {
        if (canUserSee(username)) {
            return true;
        }
        else {
            // return canUserDo(username, PRIV_APPEND) || canUserDo(username, PRIV_MODIFY) || canUserDo(username, PRIV_DEL) || canUserDo(username, PRIV_DEL);
            return canUserDo(username, PRIV_APPEND);
        }
    }

    public boolean add(String dirCode) throws
            ErrMsgException {
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, UserGroup.EVERYONE);
            ps.setInt(2, this.TYPE_USERGROUP);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, del);
            ps.setInt(6, modify);
            ps.setInt(7, examine);
            ps.setString(8, dirCode);
            ps.setInt(9, (int)SequenceMgr.nextID(SequenceMgr.DIR_PRIV_ID));
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("add:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public boolean add(String name, int type) throws
            ErrMsgException {
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
            ps.setInt(9, (int)SequenceMgr.nextID(SequenceMgr.DIR_PRIV_ID));
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("LeafPriv:" + e.getMessage());
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
        LeafPriv leafPriv = null;
        try {
            leafPriv = new LeafPriv(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return leafPriv;
    }

    public boolean delPrivsOfDir() {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from dir_priv where dir_code=?";
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
            String sql = "delete from dir_priv where name=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setString(1, username);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
                    LeafPriv lp = getLeafPriv(rs.getInt(1));
                    result.addElement(lp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("list:" + e.getMessage());
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
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
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
            logger.error("list: " + e.getMessage());
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
            logger.error("list: " + e.getMessage());
        } finally {
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
