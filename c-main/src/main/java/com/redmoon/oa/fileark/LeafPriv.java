package com.redmoon.oa.fileark;

import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.db.SequenceManager;

/**
 * 查看权限不能回溯，但是增加、修改、删除、审核的权限可以回溯
 * 这样可以使得，指定看到哪些目录，就只能看这些目录
 * 当增删改审时，
 * @author Administrator
 *
 */
public class LeafPriv extends ObjectDbA {
    private String dirCode = "", name = "";
    int id, see = 1, append = 0, del = 0, modify = 0;
    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;
    public static final int TYPE_DEPT = 3;

    public static final int PRIV_SEE = 0;
    public static final int PRIV_APPEND = 1;
    public static final int PRIV_MODIFY = 2;
    public static final int PRIV_DEL = 3;
    public static final int PRIV_EXAMINE = 4;
    public static final int PRIV_DOWNLOAD = 5;

    /**
     * 查看word
     */
    public static final int PRIV_EXPORT_WORD = 6;
    /**
     * 查看PDF
     */
    public static final int PRIV_EXPORT_PDF = 7;

    /**
     * 当为true时表示默认可见（目录上未赋予任何权限），否则表示默认不可见
     */
    private boolean defaultSee = false;

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
     * @param dirCode String
     * @return RoleDb
     */
    public Vector getRolesOfLeafPriv(String dirCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from dir_priv where dir_code=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, dirCode);
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

    public Vector<DeptDb> getDeptsOfLeafPriv(String dirCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        DeptMgr deptMgr = new DeptMgr();
        try {
            String sql = "select name from dir_priv where dir_code=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, dirCode);
            ps.setInt(2, TYPE_DEPT);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(deptMgr.getDeptDb(rs.getString(1)));
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getDeptsOfLeafPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
    
    public LeafPriv getLeafPriv(String leafCode, String name, int type) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            String sql = "select id from dir_priv where dir_code=? and name=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
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
            LogUtil.getLog(getClass()).error("getRolesOfLeafPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
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
    public void setQueryList() {
        QUERY_LIST =
                "select id from dir_priv where dir_code=? order by priv_type desc, name";
    }

    @Override
    public void setQueryLoad() {
        QUERY_LOAD =
                "select dir_code,name,priv_type,see,append,del,dir_modify,examine,download,export_word,export_pdf from dir_priv where id=?";
    }

    @Override
    public void setQueryDel() {
        QUERY_DEL = "delete from dir_priv where id=?";
    }

    @Override
    public void setQuerySave() {
        QUERY_SAVE =
                "update dir_priv set see=?,append=?,del=?,dir_modify=?,examine=?,download=?,export_word=?,export_pdf=? where id=?";
    }

    @Override
    public void setQueryAdd() {
        QUERY_ADD =
                "insert into dir_priv (name,priv_type,see,append,del,dir_modify,examine,download,dir_code,id,export_word,export_pdf) values (?,?,?,?,?,?,?,?,?,?,?,?)";
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
                downLoad = rs.getInt(9);
                exportWord = rs.getInt(10);
                exportPdf = rs.getInt(11);
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

    public int getDownLoad() {
		return downLoad;
	}

	public void setDownLoad(int downLoad) {
		this.downLoad = downLoad;
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
        return "LeafPriv is " + dirCode + ":" + name;
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
            ps.setInt(6, downLoad);
            ps.setInt(7, exportWord);
            ps.setInt(8, exportPdf);
            ps.setInt(9, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }
    
    public boolean canUserSee(HttpServletRequest request) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin"))
    		return true;
    	else {
    		String username = pvg.getUser(request);
    		return canUserSee(username);
    	}
    }

    public boolean canUserSee(String username) {
        // 根目录对所有人可见
        if (dirCode.equals(Leaf.ROOTCODE))
            return true;
        if (username==null)
            username = "";
        // LogUtil.getLog(getClass()).info("username=" + username);
        if (username.equals(UserDb.ADMIN))
            return true;
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null) {
            user = user.getUserDb(username);
        }
        if (user.isLoaded()) {
            groups = user.getGroups();
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        // 单个节点是否有查看权限，以及回溯向上是否有审核权限
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_SEE) || canUserDo(username, PRIV_EXAMINE);
    }

    public boolean canDo(LeafPriv lp, int privType) {
        if (privType==PRIV_APPEND) {
            return lp.getAppend() == 1;
        }
        else if (privType==PRIV_DEL) {
            return lp.getDel() == 1;
        }
        else if (privType==PRIV_MODIFY) {
            return lp.getModify() == 1;
        }
        else if (privType==PRIV_SEE) {
            return lp.getSee() == 1;
        }
        else if (privType==PRIV_EXAMINE) {
            return lp.getExamine() == 1;
        }
        else if(privType==PRIV_DOWNLOAD) {
            return lp.getDownLoad() == 1;
        }
        else if (privType==PRIV_EXPORT_WORD) {
            return lp.getExportWord() == 1;
        }
        else if (privType == PRIV_EXPORT_PDF) {
            return lp.getExportPdf() == 1;
        }
        return false;
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @param leaf Leaf 节点
     * @param user String
     * @return boolean
     */
    public boolean canUserDo(Leaf leaf, UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        boolean re = false;
        LeafPriv leafPriv = new LeafPriv(leaf.getCode());
        // list该节点的所有拥有权限的用户
        Vector r = leafPriv.list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            LeafPriv lp = (LeafPriv) ir.next();
            //　权限项对应的是组用户
            if (lp.getType() == TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    re = canDo(lp, privType);
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                re = canDo(lp, privType);
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==TYPE_ROLE) {
                if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
                    re = canDo(lp, privType);
                }
                else {
                    if (roles != null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                            if (roles[i].getCode().equals(lp.getName())) {
                                re = canDo(lp, privType);
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==TYPE_DEPT) {
                IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
                if (deptUserService.isUserBelongToDept(user.getName(), lp.getName())) {
                    re = canDo(lp, privType);
                }
            }
            else if (lp.getType()==TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                    re = canDo(lp, privType);
                }
            }
        }

        // 如果是判断可见权限
        if (PRIV_SEE == privType) {
            // 如果节点上没有任何权限，则默认可见
            if (r.size()==0) {
                defaultSee = true;
                return true;
            }
        }

        return re;
    }

    public boolean canUserDo(String username, int privType) {
        if (username==null) {
            return false;
        }
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
        // LogUtil.getLog(getClass()).info("groups[i].code=" + groups[i].getCode());
        // for (int i = 0; i < groups.length; i++)
        //    if (groups[i].getCode().equals(groups[i].ADMINISTRATORS))
        //        return true;

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        // LogUtil.getLog(getClass()).info("dirCode=" + dirCode + " name=" + lf.getName() + " code=" + lf.getCode() + " parentCode=" + lf.getParentCode());

        if (canUserDo(lf, user, groups, roles, privType))
            return true;

        // 回溯其父节点，判别用户对其父节点是否有权限，回溯可到达根root节点
        String parentCode = lf.getParentCode();

        // while (!parentCode.equals("-1") && !parentCode.equals("root")) {
        while (!parentCode.equals("-1")) {
            // LogUtil.getLog(getClass()).info("dirCode=" + dirCode + " parentCode=" + parentCode);
            Leaf plf = lf.getLeaf(parentCode);
            if (plf==null || !plf.isLoaded()) {
                return false;
            }
            if (canUserDo(plf, user, groups, roles, privType)) {
                return true;
            }
            parentCode = plf.getParentCode();
        }

        return false;
    }
    
    public boolean canUserAppend(String username) {
        if (username==null)
            return false;
        if (username.equals(UserDb.ADMIN))
            return true;
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null)
            user = user.getUserDb(username);
        if (user.isLoaded())
            groups = user.getGroups();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_APPEND) || canUserDo(username, PRIV_EXAMINE);
    }

    public boolean canUserDel(String username) {
        if (username==null)
            return false;
        if (username.equals(UserDb.ADMIN))
            return true;
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null)
            user = user.getUserDb(username);
        if (user.isLoaded())
            groups = user.getGroups();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_DEL) || canUserDo(username, PRIV_EXAMINE);
    }

    public boolean canUserModify(String username) {
        if (username==null) {
            return false;
        }
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null) {
            user = user.getUserDb(username);
        }
        if (user.isLoaded()) {
            groups = user.getGroups();
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_MODIFY) || canUserDo(username, PRIV_EXAMINE);
    }

    public boolean canUserExportWord(String username) {
        if (username==null) {
            return false;
        }
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null) {
            user = user.getUserDb(username);
        }
        if (user.isLoaded()) {
            groups = user.getGroups();
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_EXPORT_WORD) || canUserDo(username, PRIV_EXAMINE);
    }
    
    public boolean canUserExportPdf(String username) {
        if (username==null) {
            return false;
        }
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null) {
            user = user.getUserDb(username);
        }
        if (user.isLoaded()) {
            groups = user.getGroups();
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_EXPORT_PDF) || canUserDo(username, PRIV_EXAMINE);
    }

    public boolean canUserExamine(String username) {
        return canUserDo(username, PRIV_EXAMINE);
    }
    
    public boolean canUserDownLoad(String username){
    	if (username==null) {
            return false;
        }
        if (username.equals(UserDb.ADMIN)) {
            return true;
        }
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null) {
            user = user.getUserDb(username);
        }
        if (user.isLoaded()) {
            groups = user.getGroups();
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        return canUserDo(lf, user, groups, user.getRoles(), PRIV_DOWNLOAD) || canUserDo(username, PRIV_EXAMINE);
    }

    /**
     * 为目录初始化权限，允许每个人浏览
     * @param dirCode
     * @return
     * @throws ErrMsgException
     */
    public boolean add(String dirCode) throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, RoleDb.CODE_MEMBER);
            ps.setInt(2, TYPE_ROLE);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, del);
            ps.setInt(6, modify);
            ps.setInt(7, examine);
            ps.setInt(8, downLoad);
            ps.setString(9, dirCode);

            id = (int)SequenceManager.nextID(SequenceManager.FILEARK_PRIV);
            ps.setInt(10, id);

            ps.setInt(11, exportWord);
            ps.setInt(12, exportPdf);

            r = conn.executePreUpdate() == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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

    /**
     * 复制权限
     * @param dirCode
     * @return
     * @throws ErrMsgException
     */
    public boolean copy(String dirCode) throws ErrMsgException {
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
            ps.setInt(8, downLoad);
            ps.setString(9, dirCode);

            id = (int)SequenceManager.nextID(SequenceManager.FILEARK_PRIV);
            ps.setInt(10, id);

            ps.setInt(11, exportWord);
            ps.setInt(12, exportPdf);

            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
        String sql = "select id,name from dir_priv where dir_code=? and priv_type=?";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
            ps.setInt(2, TYPE_ROLE);
            Vector vOldRoleCodes = new Vector();
            rs = conn.executePreQuery();
            // 删除原来的role
            while (rs.next()) {
                LeafPriv lp = getLeafPriv(rs.getInt(1));
                String rCode = lp.getName();
                vOldRoleCodes.addElement(rCode);
                // 检查原有记录是否出现在了被选角色中，如果是，则不删除
                boolean isFound = false;
                for (int i=0; i<len; i++) {
                   if (ary[i].equals(rCode)) {
                	   isFound = true;
                	   break;
                   }
                }
                if (!isFound)
                	lp.del();
            }
            for (int i=0; i<len; i++) {
            	// 检查是否原来该角色是否已被赋权，如果是则跳过，不再添加
            	Iterator ir = vOldRoleCodes.iterator();
            	boolean isFound = false;
            	while (ir.hasNext()) {
            		String rc = (String)ir.next();
            		if (rc.equals(ary[i])) {
            			isFound = true;
            			break;
            		}
            	}
            	if (!isFound) {
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

    public boolean setDepts(String dirCode, String deptCodes) throws ErrMsgException {
        String[] ary = StrUtil.split(deptCodes, ",");
        int len = 0;
        if (ary!=null) {
            len = ary.length;
        }
        String sql = "select id,name from dir_priv where dir_code=? and priv_type=?";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, dirCode);
            ps.setInt(2, TYPE_DEPT);
            Vector vOldDeptCodes = new Vector();
            rs = conn.executePreQuery();
            // 删除原来的
            while (rs.next()) {
                LeafPriv lp = getLeafPriv(rs.getInt(1));
                String rCode = lp.getName();
                vOldDeptCodes.addElement(rCode);
                // 检查原有记录是否出现在了被选部门中，如果是，则不删除
                boolean isFound = false;
                for (int i=0; i<len; i++) {
                    if (ary[i].equals(rCode)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound)
                    lp.del();
            }
            for (int i=0; i<len; i++) {
                // 检查是否原来该部门是否已被赋权，如果是则跳过，不再添加
                Iterator ir = vOldDeptCodes.iterator();
                boolean isFound = false;
                while (ir.hasNext()) {
                    String rc = (String)ir.next();
                    if (rc.equals(ary[i])) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    add(ary[i], TYPE_DEPT);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("setDepts:" + e.getMessage());
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
            ps.setInt(8, downLoad);
            ps.setString(9, dirCode);

            id = (int)SequenceManager.nextID(SequenceManager.FILEARK_PRIV);
            ps.setInt(10, id);

            ps.setInt(11, exportWord);
            ps.setInt(12, exportPdf);

            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("add:" + e.getMessage());
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

    public LeafPriv getLeafPriv(int id) {
        LeafPriv leafPriv = null;
        try {
            leafPriv = new LeafPriv(id);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
            LogUtil.getLog(getClass()).error(e.getMessage());
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
                    LeafPriv lp = getLeafPriv(rs.getInt(1));
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

    public String getListSqlForAll(String orderBy, String sort) {
        return "select id from dir_priv" + " order by " + orderBy + " " + sort;
    }

    public String getListSqlForDir(String dirCode, String orderBy, String sort) {
        return "select id from dir_priv" + " where dir_code = " + StrUtil.sqlstr(dirCode) + " order by " + orderBy + " " + sort;
    }

    public Vector listUserPriv(String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from dir_priv where name=?";
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
     * 判别用户是否具有查看的权限（回溯祖先节点），如果用户被设定不能看此节点，则判断用户是否对其祖先节点有添加文章的权限 ，否则不允许看到
     * 祖先节点能添加文章，那么在子节点中应该也能添加文章
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
    
    public static Vector getUsersCanSee(String dirCode) {
        Vector v = new Vector();
        String sql =
                "select name from dir_priv where dir_code=? and see=1 and priv_type=?";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {dirCode, new Integer(TYPE_USER)});

            ResultRecord rr = null;
            UserDb user;
            UserMgr um = new UserMgr();
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String userName = rr.getString(1);
                user = um.getUserDb(userName);
                v.addElement(user);
            }

            UserGroupDb ug;
            UserGroupMgr ugm = new UserGroupMgr();
                        
            sql =
                "select name from dir_priv where dir_code=? and see=1 and priv_type=?";
            ri = jt.executeQuery(sql, new Object[] {dirCode, new Integer(TYPE_USERGROUP)});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String groupCode = rr.getString(1);
                ug = ugm.getUserGroupDb(groupCode);
                // 检查是否有重复，不重复则加入v
                Vector v2 = ug.getAllUserOfGroup();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }

            // 2010-8-25修复此bug，原来是将所有的role加入了返回值
            RoleMgr rm = new RoleMgr();
            RoleDb role;
            sql = "select name from dir_priv where dir_code=? and see=1 and priv_type=?";
            ri = jt.executeQuery(sql, new Object[] {dirCode, new Integer(TYPE_ROLE)});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String roleCode = rr.getString(1);
                role = rm.getRoleDb(roleCode);

                // 检查是否有重复，不重复则加入v
                Vector v2 = role.getAllUserOfRole();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }
        }
        catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(Privilege.class).error(StrUtil.trace(e));
            LogUtil.getLog(LeafPriv.class).error(e);
        }
        return v;
    }      

    private boolean loaded = false;
    /**
     * 管理权限
     */
    private int examine = 0;
    /**
     * 下载附件
     */
    private int downLoad = 0;

    public int getExportWord() {
        return exportWord;
    }

    public void setExportWord(int exportWord) {
        this.exportWord = exportWord;
    }

    public int getExportPdf() {
        return exportPdf;
    }

    public void setExportPdf(int exportPdf) {
        this.exportPdf = exportPdf;
    }

    /**
     * 将文档导出为word
     */
    private int exportWord = 1;
    /**
     * 将文档导出为PDF
     */
    private int exportPdf = 1;

    public boolean isDefaultSee() {
        return defaultSee;
    }

    public void setDefaultSee(boolean defaultSee) {
        this.defaultSee = defaultSee;
    }
}
