package com.redmoon.oa.exam;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RoleMgr;
import com.redmoon.oa.pvg.UserGroupDb;

import cn.js.fan.base.ObjectDbA;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

/**
 * @Description:
 * @author:
 * @Date: 2018-4-24上午10:23:45
 */
public class MajorPriv extends ObjectDbA {
    private String majorCode, name;
    int id, canManage = 1, invigilate = 0;

    private int priveType;
    public static final int TYPE_USER = 0; // 表示用户
    public static final int TYPE_ROLE = 1; // 表示角色

    public MajorPriv(int id) {
        this.id = id;
        load();
        init();
    }

    public MajorPriv() {
        init();
    }

    public MajorPriv(String majorCode) {
        this.majorCode = majorCode;
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
     * 取得与节点相关的角色
     *
     * @param majorCode String
     * @return RoleDb
     */
    public Vector getRolesOfLeafPriv(String majorCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from oa_exam_major_priv where major_code=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, majorCode);
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
            logger.error("getRolesOfLeafPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * @param majorCode
     * @param name
     * @param type
     * @return
     * @Description: 取得节点的权限
     */
    public LeafPriv getLeafPriv(String majorCode, String name, int type) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_exam_major_priv where major_code=? and name=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, majorCode);
            ps.setString(2, name);
            ps.setInt(3, type);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                if (rs.next()) {
                    return new LeafPriv(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.error("getRolesOfLeafPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    public String getTypeDesc() {
        if (priveType == this.TYPE_USER)
            return "用户";
        else if (priveType == this.TYPE_ROLE)
            return "角色";
        else
            return "";
    }

    /**
     * @return
     * @Description:
     */
    @Override
    public boolean del() {
        // TODO Auto-generated method stub
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

    /**
     * @return
     * @throws SQLException
     * @Description:
     */
    @Override
    public Vector list() throws SQLException {
        // TODO Auto-generated method stub
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setString(1, majorCode);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    MajorPriv mp = getMajorPriv(rs.getInt(1));
                    result.addElement(mp);
                }
            }
        } catch (Exception e) {
            logger.error("list: " + e.getMessage());
        } finally {
            /*
             * if (ps != null) { try { ps.close(); } catch (Exception e) {} ps =
             * null; }
             */
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    public MajorPriv getMajorPriv(int id) {
        MajorPriv majorPriv = null;
        try {
            majorPriv = new MajorPriv(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return majorPriv;
    }

    /**
     * @param listsql
     * @param curPage
     * @param pageSize
     * @return
     * @throws ErrMsgException
     * @Description:
     */
    @Override
    public ListResult list(String listsql, int curPage, int pageSize)
            throws ErrMsgException {
        // TODO Auto-generated method stub
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
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

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
                    MajorPriv mp = getMajorPriv(rs.getInt(1));
                    result.addElement(mp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
                    MajorPriv mp = getMajorPriv(rs.getInt(1));
                    result.addElement(mp);
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
     * @Description:
     */
    @Override
    public void load() {
        // TODO Auto-generated method stub
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                majorCode = rs.getString(1);
                name = rs.getString(2);
                priveType = rs.getInt(3);
                canManage = rs.getInt(4);
                invigilate = rs.getInt(5);
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
        } finally {
            /*
             * if (ps != null) { try { ps.close(); } catch (Exception e) {} ps =
             * null; }
             */
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * @return
     * @Description:
     */
    @Override
    public boolean save() {
        // TODO Auto-generated method stub
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, canManage);
            ps.setInt(2, invigilate);
            ps.setInt(3, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }

    public boolean canUserSee(HttpServletRequest request) throws SQLException {
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(request, "admin"))
            return true;
        else {
            String username = pvg.getUser(request);
            return canUserSee(username);
        }
    }

    /**
     * @param name
     * @param type
     * @return
     * @throws ErrMsgException
     * @Description: 权限添加
     */
    public boolean add(String name, int type) throws ErrMsgException {
        // 检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, canManage);
            ps.setString(4, majorCode);
            ps.setInt(5, invigilate);
            // id = (int) SequenceManager.nextID(SequenceManager.FILEARK_PRIV);
            // ps.setInt(5, id);
            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("add:" + e.getMessage());
            throw new ErrMsgException("请检查是否有重复项存在！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     *
     * @return boolean
     * @throws SQLException
     */
    public boolean canUserDo(TreeSelectDb tsd, UserDb user, RoleDb[] roles,
                             int privType) throws SQLException {
        MajorPriv majorPriv = new MajorPriv(tsd.getCode());
        // list该节点的所有拥有权限的用户
        Vector r = majorPriv.list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            MajorPriv mp = (MajorPriv) ir.next();
            if (mp.getPriveType() == mp.TYPE_ROLE) {
                if (roles != null) {
                    // 判断该用户所属的角色是否有权限
                    int len = roles.length;
                    for (int i = 0; i < len; i++) {
                        if (roles[i].getCode().equals(mp.getName())) {
                            if (mp.canManage == 0)
                                return true;
                            break;
                        }
                    }
                }
            } else if (mp.getPriveType() == mp.TYPE_USER) { // 　个人用户
                if (mp.getName().equals(user.getName())) {
                    if (mp.canManage == 0)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean setRoles(String majorCode, String roleCodes)
            throws ErrMsgException {
        String[] ary = StrUtil.split(roleCodes, ",");
        int len = 0;
        if (ary != null) {
            len = ary.length;
        }
        String sql = "select id,name from oa_exam_major_priv where major_code=? and priv_type=?";
        for (int i = 0; i < ary.length; i++) {
            System.out.println(this.getClass() + "来了： " + ary[i]);
        }
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, majorCode);
            ps.setInt(2, TYPE_ROLE);
            Vector vOldRoleCodes = new Vector();
            rs = conn.executePreQuery();
            // 删除原来的role
            while (rs.next()) {
                MajorPriv mp = getMajorPriv(rs.getInt(1));
                String rCode = mp.getName();
                vOldRoleCodes.addElement(rCode);
                // 检查原有记录是否出现在了被选角色中，如果是，则不删除
                boolean isFound = false;
                for (int i = 0; i < len; i++) {
                    if (ary[i].equals(rCode)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound)
                    mp.del();
            }
            for (int i = 0; i < len; i++) {
                // 检查是否原来该角色是否已被赋权，如果是则跳过，不再添加
                Iterator ir = vOldRoleCodes.iterator();
                boolean isFound = false;
                while (ir.hasNext()) {
                    String rc = (String) ir.next();
                    if (rc.equals(ary[i])) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound)
                    add(ary[i], TYPE_ROLE);
            }
        } catch (SQLException e) {
            logger.error("setRoles:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean canUserSeeWithAncestorNode(String username) throws SQLException {
        if (canUserSee(username)) {
            return true;
        }
        return false;
    }

    public boolean canUserSee(String username) throws SQLException {
        // 根目录对所有人可见
        if (majorCode.equals("exam_major"))
            return true;
        if (username == null)
            username = "";
        // logger.info("username=" + username);
        if (username.equals(UserDb.ADMIN))
            return true;
        UserDb user = new UserDb();
        if (username != null)
            user = user.getUserDb(username);
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(majorCode);
        return canUserDo(tsd, user, user.getRoles(), priveType);
    }

    /**
     * @Description:
     */
    @Override
    public void setQueryAdd() {
        // TODO Auto-generated method stub
        QUERY_ADD = "insert into oa_exam_major_priv (name,priv_type,can_manage,major_code,invigilate) values (?,?,?,?,?)";
    }

    /**
     * @Description:
     */
    @Override
    public void setQueryDel() {
        // TODO Auto-generated method stub
        QUERY_DEL = "delete from oa_exam_major_priv where id=?";
    }

    /**
     * @Description:
     */
    @Override
    public void setQueryList() {
        // TODO Auto-generated method stub
        QUERY_LIST = "select id from oa_exam_major_priv where major_code=? order by priv_type desc, name";
    }

    /**
     * @Description:
     */
    @Override
    public void setQueryLoad() {
        // TODO Auto-generated method stub
        QUERY_LOAD = "select major_code,name,priv_type,can_manage,invigilate from oa_exam_major_priv where id=?";
    }

    /**
     * @Description:
     */
    @Override
    public void setQuerySave() {
        // TODO Auto-generated method stub
        QUERY_SAVE = "update oa_exam_major_priv set can_manage=?,invigilate=? where id=?";
    }

    /**
     * @return the majorCode
     */
    public String getMajorCode() {
        return majorCode;
    }

    /**
     * @param majorCode the majorCode to set
     */
    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the priveType
     */
    public int getPriveType() {
        return priveType;
    }

    /**
     * @param priveType the priveType to set
     */
    public void setPriveType(int priveType) {
        this.priveType = priveType;
    }

    /**
     * @return the canManage
     */
    public int getCanManage() {
        return canManage;
    }

    /**
     * @param canManage the canManage to set
     */
    public void setCanManage(int canManage) {
        this.canManage = canManage;
    }

    /**
     * @return the invigilate
     */
    public int getInvigilate() {
        return invigilate;
    }

    /**
     * @param invigilate the invigilate to set
     */
    public void setInvigilate(int invigilate) {
        this.invigilate = invigilate;
    }

    public static String getMajorsOfUser(String userName) {
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        RoleDb[] roles = user.getRoles();
        int len = roles.length;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            StrUtil.concat(sb, ",", StrUtil.sqlstr(roles[i].getCode()));
        }
        String sqlExamMajor = "";
        String sql = "select major_code from oa_exam_major_priv where (name in (" + sb.toString() + ") and priv_type=" + TYPE_ROLE + ") or (name=" + StrUtil.sqlstr(userName) + " and priv_type=" + TYPE_USER + ")";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rd = (ResultRecord) ri.next();
                if (sqlExamMajor.equals("")) {
                    sqlExamMajor = StrUtil.sqlstr(rd.getString("major_code"));
                } else {
                    sqlExamMajor += "," + StrUtil.sqlstr(rd.getString("major_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sqlExamMajor;
    }
}
