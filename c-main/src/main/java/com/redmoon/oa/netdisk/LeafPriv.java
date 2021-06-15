package com.redmoon.oa.netdisk;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.*;

public class LeafPriv extends ObjectDbA {
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

    public String getTypeDesc() {
        if (type==LeafPriv.TYPE_USER)
            return "用户";
        else if (type==LeafPriv.TYPE_USERGROUP)
            return "用户组";
        else if (type==LeafPriv.TYPE_ROLE)
            return "角色";
        else
            return "";
    }

    @Override
	public void setQueryList() {
        QUERY_LIST =
                "select id from netdisk_dir_priv where dir_code=? order by type, name";
    }

    @Override
	public void setQueryLoad() {
        QUERY_LOAD =
                "select dir_code,name,type,see,append,del,priv_modify,examine from netdisk_dir_priv where id=?";
    }

    @Override
	public void setQueryDel() {
        QUERY_DEL = "delete from netdisk_dir_priv where id=?";
    }

    @Override
	public void setQuerySave() {
        QUERY_SAVE =
                "update netdisk_dir_priv set see=?,append=?,del=?,priv_modify=?,examine=? where id=?";
    }

    @Override
	public void setQueryAdd() {
        QUERY_ADD =
                "insert into netdisk_dir_priv (name,type,see,append,del,priv_modify,examine,dir_code) values (?,?,?,?,?,?,?,?)";
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
        return "Netdisk LeafPriv is " + dirCode + ":" + name;
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

    public boolean canUserSee(String username) {
        if (username==null)
            username = "";
        // logger.info("username=" + username);
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
        return canUserDo(lf, user, groups, user.getRoles(), LeafPriv.PRIV_SEE);
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @param leaf Leaf 节点
     * @param username String
     * @return boolean
     */
    public boolean canUserDo(Leaf leaf, UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        // 如果该节点的创建者为user，则返回true
        if (leaf.getRootCode().equals(user.getName()))
            return true;
        
        // 如果拥有admin权限则返回true
        Privilege priv = new Privilege();
        if (priv.isUserPrivValid(user.getName(), PrivDb.PRIV_ADMIN)) {
        	return true;
        }

        LeafPriv leafPriv = new LeafPriv(leaf.getCode());
        // list该节点的所有拥有权限的用户
        Vector r = leafPriv.list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            LeafPriv lp = (LeafPriv) ir.next();
            //　权限项对应的是组用户
            if (lp.getType() == LeafPriv.TYPE_USERGROUP) {
                logger.info("canUserDo:name=" + lp.getName() + " privType=" + privType);
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    if (privType==LeafPriv.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
                            return true;
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            logger.info("canUserDo:group[i].desc=" + groups[i].getDesc());
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (privType == LeafPriv.PRIV_APPEND) {
                                    if (lp.getAppend() == 1)
                                        return true;
                                } else if (privType == LeafPriv.PRIV_DEL) {
                                    if (lp.getDel() == 1)
                                        return true;
                                } else if (privType == LeafPriv.PRIV_MODIFY) {
                                    if (lp.getModify() == 1)
                                        return true;
                                } else if (privType == LeafPriv.PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                else if (privType == LeafPriv.PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==LeafPriv.TYPE_ROLE) {
                if (roles!=null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                            if (roles[i].getCode().equals(lp.getName())) {
                                if (privType == LeafPriv.PRIV_APPEND) {
                                    if (lp.getAppend() == 1)
                                        return true;
                                } else if (privType == LeafPriv.PRIV_DEL) {
                                    if (lp.getDel() == 1)
                                        return true;
                                } else if (privType == LeafPriv.PRIV_MODIFY) {
                                    if (lp.getModify() == 1)
                                        return true;
                                } else if (privType == LeafPriv.PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                else if (privType == LeafPriv.PRIV_EXAMINE) {
                                    if (lp.getExamine() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                }
            }
            else { //　个人用户
                logger.info("privType=" + privType);
                logger.info("canUserDo:userName=" + lp.getName());

                if (lp.getName().equals(user.getName())) {
                    if (privType==LeafPriv.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_DEL) {
                        if (lp.getDel() == 1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_MODIFY) {
                        if (lp.getModify()==1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                    else if (privType==LeafPriv.PRIV_EXAMINE) {
                        if (lp.getExamine()==1)
                            return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    public boolean canUserDo(String username, int privType) {
        // logger.info("canUserDo:privType=" + privType);
        if (username==null)
            return false;
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(username, Privilege.ADMIN)) {
            return true;
        }

        // 检查根节点是否为用户username的根目录
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf==null)
            return false;
        if (lf.getRootCode().equals(username))
            return true;

        logger.info("canUserDo: lf.rootCode=" + lf.getRootCode());

        UserDb user = new UserDb();
        user = user.getUserDb(username);

        //　判断用户是否具有管理员的权限
        String[] privs = user.getPrivs();
        if (privs!=null) {
            int len = privs.length;
            for (int i=0; i<len; i++) {
                // 拥有管理员权限
                if (privs[i].equals(PrivDb.PRIV_ADMIN)) {
                    logger.info("canUserDo: has admin pirv");
                    return true;
                }
            }
        }

        UserGroupDb[] groups = user.getGroups();
        RoleDb[] roles = user.getRoles();

        // 如果username属于管理员组,则拥有全部权限
        // logger.info("groups[i].code=" + groups[i].getCode());
        for (int i = 0; i < groups.length; i++)
            if (groups[i].getCode().equals(UserGroupDb.ADMINISTRATORS))
                return true;

        logger.info("privType=" + privType + " dirCode=" + dirCode + " name=" + lf.getName() + " code=" + lf.getCode() + " parentCode=" + lf.getParentCode());

        if (canUserDo(lf, user, groups, roles, privType))
            return true;

        // 回溯其父节点，判别用户对其父节点是否有权限，回溯可到达根root节点
        String parentCode = lf.getParentCode();

        // while (!parentCode.equals("-1") && !parentCode.equals("root")) {
        while (!parentCode.equals(Leaf.PARENT_CODE_NONE)) {
            logger.info("canUserDo: dirCode=" + dirCode + " parentCode=" + parentCode + " privType=" + privType);
            Leaf plf = lf.getLeaf(parentCode);
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
        return canUserDo(username, LeafPriv.PRIV_APPEND);
    }

    public boolean canUserDel(String username) {
        return canUserDo(username, LeafPriv.PRIV_DEL);
    }

    public boolean canUserModify(String username) {
        return canUserDo(username, LeafPriv.PRIV_MODIFY);
    }

    public boolean canUserExamine(String username) {
        return canUserDo(username, LeafPriv.PRIV_EXAMINE);
    }

    public boolean add(String dirCode) throws
            ErrMsgException {
        return add(UserGroupDb.EVERYONE, TYPE_USERGROUP);
    }

    public boolean add(String name, int type) throws
            ErrMsgException {
        // @task:检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        // 置目录节点的共享状态为 已共享
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf!=null && lf.isLoaded()) {
            lf.setShared(true);
            lf.update();
        }
        String sql ="select id from netdisk_dir_priv where dir_code ="+StrUtil.sqlstr(dirCode)
        +" and name = "+StrUtil.sqlstr(name);
        JdbcTemplate jt = new JdbcTemplate();
        
        try {
        	ResultIterator ri = jt.executeQuery(sql);
        	if(ri.hasNext()){
        		return r;
        	}
        	
        	Vector vec = new Vector(); //将分享的子文件夹shared变为1
    		vec = lf.getAllChild(vec, lf);
    		vec.add(lf);
    		Iterator it = vec.iterator();
    		while (it.hasNext()) {
    			Leaf leaf = (Leaf) it.next();
    			dirCode = leaf.getCode();
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
    		}
            
        } catch (SQLException e) {
            logger.error("add:" + e.getMessage());
            throw new ErrMsgException("请检查节点是否已存在！");
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
            String sql = "delete from netdisk_dir_priv where dir_code=?";
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
            String sql = "delete from netdisk_dir_priv where name=?";
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
        boolean r = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select count(id) from netdisk_dir_priv where dir_code=?";
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            r = conn.executePreUpdate() == 1 ? true : false;
            if (ps!=null) {
                ps.close();
                ps = null;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, dirCode);
            rs = conn.executePreQuery();
            if (rs!=null && rs.next()) {
                int count = rs.getInt(1);
                // 当权限表中为空时，置目录的共享状态为非共享
                if (count==0) {
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(dirCode);
                    lf.setShared(false);
                    lf.update();
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {e.printStackTrace();}
                ps = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
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
                    LeafPriv lp = getLeafPriv(rs.getInt(1));
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
