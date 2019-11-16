package com.redmoon.oa.exam;

import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.*;

public class PaperPriv extends ObjectDbA {
    private String name = "";
	int id, paperId, see = 1;

	public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_SEE = 0;
    
    public PaperPriv(int id) {
        this.id = id;
        load();
        init();
    }

    public PaperPriv() {
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
     * @param docId int
     * @return RoleDb
     */
    public Vector getRolesOfPaperPriv(int docId) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from oa_exam_paper_priv where paper_id=? and priv_type=? ";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, docId);
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

    public PaperPriv getPaperPriv(int id) {
        return new PaperPriv(id);
    }
    
    public PaperPriv getPaperPriv(int paperId, String name, int type) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_exam_paper_priv where paper_id=? and name=? and priv_type=? ";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, paperId);
            ps.setString(2, name);
            ps.setInt(3, type);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                if (rs.next()) {
                    return new PaperPriv(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.error("getPaperPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }    

    public String getTypeDesc() {
        if (type==PaperPriv.TYPE_USER)
            return "用户";
        else if (type==PaperPriv.TYPE_USERGROUP)
            return "用户组";
        else if (type==PaperPriv.TYPE_ROLE)
            return "角色";
        else
            return "";
    }

    public void setQueryList() {
        QUERY_LIST =
                "select id from oa_exam_paper_priv where paper_id=? order by priv_type desc, name";
    }

    public void setQueryLoad() {
        QUERY_LOAD =
                "select paper_id,name,priv_type,see from oa_exam_paper_priv where id=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from oa_exam_paper_priv where id=?";
    }

    public void setQuerySave() {
        QUERY_SAVE =
                "update oa_exam_paper_priv set see=? where id=?";
    }

    public void setQueryAdd() {
        QUERY_ADD =
                "insert into oa_exam_paper_priv (name,priv_type,see,paper_id) values (?,?,?,?)";
    }
/*
 * 20180320：sht，修改数据库新增字段is_manual 区分手工组卷和自动组卷
 */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                paperId = rs.getInt(1);
                name = rs.getString(2);
                type = rs.getInt(3);
                see = rs.getInt(4);
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
        return "PaperPriv is " + paperId + ":" + name;
    }

    private int type = 0;

    public synchronized boolean save() {
    	Conn conn = new Conn(connname);
        int r = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, see);
            ps.setInt(2, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }
    
    public boolean canUserSee(HttpServletRequest request, int paperId) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin"))
    		return true;
    	else if (pvg.isUserPrivValid(request, "admin.exam")) {
    		return true;
    	}
    	else
    		return canUserSee(pvg.getUser(request), paperId);
    }
    
    public boolean canUserSee(String username, int paperId) {
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

        return canUserDo(user, groups, user.getRoles(), paperId, PRIV_SEE);
    }

    public boolean canUserDo(UserDb user, UserGroupDb[] groups, RoleDb[] roles, int paperId, int privType) {
        PaperPriv docPriv = new PaperPriv();
		docPriv.setPaperId(paperId);
        // list该节点的所有拥有权限的用户
        Vector r = docPriv.list();
        
        // 如果没有指派权限，则默认允许
        if (r.size()==0)
        	return true;
        
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            PaperPriv lp = (PaperPriv) ir.next();
            //　权限项对应的是组用户
            if (lp.getType() == PaperPriv.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                	if (privType==PRIV_SEE) {
                        if (lp.getSee()==1) {
                            return true;
                        }
                	}             	
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                            	if (privType==PRIV_SEE) {
                                    if (lp.getSee() == 1) {
                                        return true;
                                    }
                            	}                         	
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==PaperPriv.TYPE_ROLE) {
				if (roles != null) {
					// 判断该用户所属的角色是否有权限
					int len = roles.length;
					for (int i = 0; i < len; i++) {
						if (roles[i].getCode().equals(lp.getName())) {
							if (privType == PRIV_SEE) {
								if (lp.getSee() == 1) {
									return true;
								}
							}						
							break;
						}
					}
				}
            }
            else if (lp.getType()==PaperPriv.TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                	if (privType==PRIV_SEE) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                	}              	
                }
            }
        }
        return false;
    }

    public boolean setRoles(int docid, String roleCodes) throws ErrMsgException {
        String[] ary = StrUtil.split(roleCodes, ",");
        int len = 0;
        if (ary!=null) {
            len = ary.length;
        }
        String sql = "select id,name from oa_exam_paper_priv where paper_id=? and priv_type=? ";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, docid);
            ps.setInt(2, TYPE_ROLE);
            Vector vOldRoleCodes = new Vector();
            rs = conn.executePreQuery();
            // 删除原来的role
            while (rs.next()) {
                PaperPriv lp = getDocPriv(rs.getInt(1));
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
            		add(ary[i], TYPE_ROLE,docid);
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

    public boolean add(String name, int type, int paperId) throws
            ErrMsgException {
        // 检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, see);
            ps.setInt(4, paperId);
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

    public PaperPriv getDocPriv(int id) {
        PaperPriv docPriv = null;
        try {
            docPriv = new PaperPriv(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return docPriv;
    }

    public boolean delPrivsOfPaperId() {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from oa_exam_paper_priv where paper_id=? ";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setInt(1, paperId);
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
            String sql = "delete from oa_exam_paper_priv where name=?";
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

    public int getPaperId() {
		return paperId;
	}

	public void setPaperId(int paperId) {
		this.paperId = paperId;
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
                    PaperPriv lp = getDocPriv(rs.getInt(1));
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
            String sql = "select id from oa_exam_paper_priv where name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    PaperPriv lp = getDocPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            logger.error("listUserPriv: " + e.getMessage());
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
                    PaperPriv lp = getDocPriv(rs.getInt(1));
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
     * @return Vector
     */
    public Vector list() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setInt(1, paperId);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    PaperPriv lp = getDocPriv(rs.getInt(1));
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

    /**
     * 取得拥有某权限的全部用户，用于查看未参加考试的人员
     * @param paperId int
     * @return Vector
     */
    public static Vector getUsersCanSee(int paperId) {
        PaperPriv docPriv = new PaperPriv();
		docPriv.setPaperId(paperId);
        Vector v = new Vector();
        String sql =
                "select name from oa_exam_paper_priv where paper_id=? and see=1 and priv_type=? ";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {new Integer(paperId), new Integer(TYPE_USER)});

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
                        
            sql = "select name from oa_exam_paper_priv where paper_id=? and see=1 and priv_type=? ";
            ri = jt.executeQuery(sql, new Object[] {new Integer(paperId), new Integer(TYPE_USERGROUP)});
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
            sql = "select name from oa_exam_paper_priv where paper_id=? and see=1 and priv_type=? ";
            ri = jt.executeQuery(sql, new Object[] {new Integer(paperId), new Integer(TYPE_ROLE)});
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
}
