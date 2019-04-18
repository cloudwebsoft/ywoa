package com.redmoon.oa.questionnaire;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.ObjectDbA;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RoleMgr;
import com.redmoon.oa.pvg.UserGroupDb;
import com.redmoon.oa.pvg.UserGroupMgr;

public class QuestionnairePriv extends ObjectDbA {
    private String name = "";
    private int questId;
    public int getModify() {
		return modify;
	}

	public void setModify(int modify) {
		this.modify = modify;
	}

	public int getExamine() {
		return examine;
	}

	public void setExamine(int examine) {
		this.examine = examine;
	}

	private int id, see = 1, append = 0, del = 0, modify = 0;
	
	private String kind = "";
	
    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_SEE = 0;
    public static final int PRIV_APPEND = 1;
    public static final int PRIV_MODIFY = 2;
    public static final int PRIV_DEL = 3;
    public static final int PRIV_EXAMINE = 4;
    public static final int PRIV_DOWNLOAD = 5;
    
    /**
     * 权重
     */
    private int weight = 1;
    
    /**
     * 最多选几个A
     */
    private int limitA = 0;
    private int limitB = 0;
    private int limitC = 0;
    private int limitD = 0;
    private int limitE = 0;
    private int limitF = 0;
    
    public QuestionnairePriv(int id) {
        this.id = id;
        load();
        init();
    }

    public QuestionnairePriv() {
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
     * 取得与leafCode节点相关的角色
     * @param questId int
     * @return RoleDb
     */
    public Vector getRolesOfQuestionnairePriv(int questId) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from oa_questionnaire_priv where quest_id=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, questId);
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
    
    
    public QuestionnairePriv getQuestionnairePriv(int questId, String name, int type) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_questionnaire_priv where quest_id=? and name=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, questId);
            ps.setString(2, name);
            ps.setInt(3, type);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                if (rs.next()) {
                    return new QuestionnairePriv(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.error("getQuestionnairePriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
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
                "select id from oa_questionnaire_priv where quest_id=? order by priv_type desc, name";
    }

    public void setQueryLoad() {
        QUERY_LOAD =
                "select quest_id,name,priv_type,see,append,del,quest_modify,examine,download,weight,limit_a,limit_b,limit_c,limit_d,limit_e,limit_f,kind from oa_questionnaire_priv where id=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from oa_questionnaire_priv where id=?";
    }

    public void setQuerySave() {
        QUERY_SAVE =
                "update oa_questionnaire_priv set see=?,append=?,del=?,quest_modify=?,examine=?,download=?,weight=?,limit_a=?,limit_b=?,limit_c=?,limit_d=?,limit_e=?,limit_f=?,kind=? where id=?";
    }

    public void setQueryAdd() {
        QUERY_ADD =
                "insert into oa_questionnaire_priv (name,priv_type,see,append,del,quest_modify,examine,download,quest_id) values (?,?,?,?,?,?,?,?,?)";
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
            	questId = rs.getInt(1);
                name = rs.getString(2);
                type = rs.getInt(3);
                see = rs.getInt(4);
                append = rs.getInt(5);
                del = rs.getInt(6);
                modify = rs.getInt(7);
                examine = rs.getInt(8);
                downLoad = rs.getInt(9);
                weight = rs.getInt(10);
                limitA = rs.getInt(11);
                limitB = rs.getInt(12);
                limitC = rs.getInt(13);
                limitD = rs.getInt(14);
                limitE = rs.getInt(15);
                limitF = rs.getInt(16);
                kind = StrUtil.getNullStr(rs.getString(17));
                loaded = true;
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
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
        return "Questionnaire Priv is " + questId + ":" + name;
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
            ps.setInt(6, downLoad);
            ps.setInt(7, weight);
            ps.setInt(8, limitA);
            ps.setInt(9, limitB);
            ps.setInt(10, limitC);
            ps.setInt(11, limitD);
            ps.setInt(12, limitE);
            ps.setInt(13, limitF);
            ps.setString(14, kind);
            ps.setInt(15, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }
    
    public boolean canUserSee(HttpServletRequest request, int questId) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin"))
    		return true;
    	else {
    		String username = pvg.getUser(request);
    		return canUserSee(questId, username);
    	}
    }

    public boolean canUserSee(int questId, String username) {
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
        return canUserDo(questId, user, groups, user.getRoles(), PRIV_SEE);
    }
    
    public boolean canUserAppend(HttpServletRequest request, int questId) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin"))
    		return true;
    	else {
    		String username = pvg.getUser(request);
    		return canUserAppend(questId, username);
    	}
    }

    public boolean canUserAppend(int questId, String username) {
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
        return canUserDo(questId, user, groups, user.getRoles(), PRIV_APPEND);
    }    
    
    public boolean canUserModify(HttpServletRequest request, int questId) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin"))
    		return true;
    	else {
    		String username = pvg.getUser(request);
    		return canUserModify(questId, username);
    	}
    }

    public boolean canUserModify(int questId, String username) {
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
        return canUserDo(questId, user, groups, user.getRoles(), PRIV_MODIFY);
    }        

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @param leaf Leaf 节点
     * @param username String
     * @return boolean
     */
    public boolean canUserDo(int questId, UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        // list该节点的所有拥有权限的用户
        Vector r = list(questId);
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
        	QuestionnairePriv lp = (QuestionnairePriv) ir.next();
            //　权限项对应的是组用户
            if (lp.getType() == QuestionnairePriv.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    if (privType==QuestionnairePriv.PRIV_APPEND) {
                        if (lp.getAppend() == 1)
                            return true;
                    }
                    else if (privType==QuestionnairePriv.PRIV_SEE) {
                        if (lp.getSee()==1)
                            return true;
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (privType == QuestionnairePriv.PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==QuestionnairePriv.TYPE_ROLE) {
                if (roles!=null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
                                if (privType == PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                break;                        		
                        	}
                        	else if (roles[i].getCode().equals(lp.getName())) {
                                if (privType == PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                }
            }
            else if (lp.getType()==QuestionnairePriv.TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                    if (privType == this.PRIV_SEE) {
                        if (lp.getSee() == 1)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canUserDo(int questId, String username, int privType) {
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

        return canUserDo(questId, user, groups, roles, privType);
    }

    public boolean setRoles(int questId, String roleCodes) throws ErrMsgException {
        String[] ary = StrUtil.split(roleCodes, ",");
        int len = 0;
        if (ary!=null) {
            len = ary.length;
        }
        String sql = "select id,name from oa_questionnaire_priv where quest_id=? and priv_type=?";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, questId);
            ps.setInt(2, TYPE_ROLE);
            Vector vOldRoleCodes = new Vector();
            rs = conn.executePreQuery();
            // 删除原来的role
            while (rs.next()) {
            	QuestionnairePriv lp = getQuestionnairePriv(rs.getInt(1));
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
            	if (!isFound)
            		add(ary[i], TYPE_ROLE, questId);
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

    public boolean add(String name, int type, int questId) throws
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
            ps.setInt(9, questId);
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

    public QuestionnairePriv getQuestionnairePriv(int id) {
    	QuestionnairePriv questPriv = null;
        try {
        	questPriv = new QuestionnairePriv(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return questPriv;
    }

    public boolean delPrivsOfQuest(int questId) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from oa_questionnaire_priv where quest_id=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setInt(1, questId);
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
            String sql = "delete from oa_questionnaire_priv where name=?";
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
                	QuestionnairePriv lp = getQuestionnairePriv(rs.getInt(1));
                    result.addElement(lp);
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

    public Vector listUserPriv(String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_questionnaire_priv where name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                	QuestionnairePriv lp = getQuestionnairePriv(rs.getInt(1));
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
                	QuestionnairePriv lp = getQuestionnairePriv(rs.getInt(1));
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
    
    public Vector list() {
    	return list(questId);
    }

    /**
     * 取出全部信息置于result中
     * @param questId int
     * @return Vector
     */
    public Vector list(int questId) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setInt(1, questId);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                	QuestionnairePriv lp = getQuestionnairePriv(rs.getInt(1));
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
    
    public static Vector getUsersCanSee(int questId) {
        Vector v = new Vector();
        String sql =
                "select name from oa_questionnaire_priv where quest_id=? and see=1 and priv_type=?";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {new Integer(questId), new Integer(TYPE_USER)});

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
                "select name from oa_questionnaire_priv where quest_id=? and see=1 and priv_type=?";
            ri = jt.executeQuery(sql, new Object[] {new Integer(questId), new Integer(TYPE_USERGROUP)});
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
            sql = "select name from oa_questionnaire_priv where quest_id=? and see=1 and priv_type=?";
            ri = jt.executeQuery(sql, new Object[] {new Integer(questId), new Integer(TYPE_ROLE)});
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
            e.printStackTrace();
        }
        return v;
    }      

    /**
	 * @param questId the questId to set
	 */
	public void setQuestId(int questId) {
		this.questId = questId;
	}

	/**
	 * @return the questId
	 */
	public int getQuestId() {
		return questId;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * @param limitA the limitA to set
	 */
	public void setLimitA(int limitA) {
		this.limitA = limitA;
	}

	/**
	 * @return the limitA
	 */
	public int getLimitA() {
		return limitA;
	}

	/**
	 * @param limitB the limitB to set
	 */
	public void setLimitB(int limitB) {
		this.limitB = limitB;
	}

	/**
	 * @return the limitB
	 */
	public int getLimitB() {
		return limitB;
	}

	/**
	 * @param limitC the limitC to set
	 */
	public void setLimitC(int limitC) {
		this.limitC = limitC;
	}

	/**
	 * @return the limitC
	 */
	public int getLimitC() {
		return limitC;
	}

	/**
	 * @param limitD the limitD to set
	 */
	public void setLimitD(int limitD) {
		this.limitD = limitD;
	}

	/**
	 * @return the limitD
	 */
	public int getLimitD() {
		return limitD;
	}

	/**
	 * @param limitE the limitE to set
	 */
	public void setLimitE(int limitE) {
		this.limitE = limitE;
	}

	/**
	 * @return the limitE
	 */
	public int getLimitE() {
		return limitE;
	}

	/**
	 * @param limitF the limitF to set
	 */
	public void setLimitF(int limitF) {
		this.limitF = limitF;
	}

	/**
	 * @return the limitF
	 */
	public int getLimitF() {
		return limitF;
	}

	/**
	 * @param kind the kind to set
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * @return the kind
	 */
	public String getKind() {
		return kind;
	}
	
	public boolean isKindWeightExist(int questId, String kind, int weight) {
		String sql = "select id from oa_questionnaire_priv where quest_id=" + questId + " and kind=" + StrUtil.sqlstr(kind) + " and weight=" + weight;
		return list(sql).size()>0;
	}

	private boolean loaded = false;
    private int examine = 0;
    private int downLoad = 0;

}

