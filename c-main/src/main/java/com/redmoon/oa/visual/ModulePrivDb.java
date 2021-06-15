package com.redmoon.oa.visual;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.sys.DebugUtil;

public class ModulePrivDb extends ObjectDb {
	/**
	 * 模块编码，注意已不再是表单编码 
	 */
    private String formCode = "";
    private String name = "";
    private int manage = 0;
    int id, see = 1, append = 0;
	private int modify;
	private int view = 1;
	
	private int del = 0;
	
	private String fieldWrite;
	private String fieldHide;

	/**
	 * 高级查询，默认不允许
	 */
	private int search = 0;
	
	/**
	 * 变更（重激活），默认不允许
	 */
	private int reActive = 0;
	private int importXls;

    public int getExportWord() {
        return exportWord;
    }

    public void setExportWord(int exportWord) {
        this.exportWord = exportWord;
    }

    /**
     * 生成word
     */
	private int exportWord = 0;
	
	private int log = 0;
	
	public int getImportXls() {
		return importXls;
	}

	public void setImportXls(int importXls) {
		this.importXls = importXls;
	}

	public int getExportXls() {
		return exportXls;
	}

	public void setExportXls(int exportXls) {
		this.exportXls = exportXls;
	}

	private int exportXls;

	public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_SEE = 0;
    public static final int PRIV_APPEND = 1;
    public static final int PRIV_MANAGE = 2;
    
    public static final int PRIV_MODIFY = 3;
    
    public static final int PRIV_VIEW = 4;
    
    public static final int PRIV_SEARCH = 5;
    
    public static final int PRIV_REACTIVE = 6;
    
    public static final int PRIV_IMPORT = 7;
    public static final int PRIV_EXPORT = 8;
    
    public static final int PRIV_DEL = 9;
    
    public static final int PRIV_LOG = 10;

    public static final int PRIV_EXPORT_WORD = 11;
	
    /**
     * 数据维护权限，可以不受校验规则的限制
     */
    public static final int PRIV_DATA = 12;
    
    public int getView() {
		return view;
	}

	public void setView(int view) {
		this.view = view;
	}


    public ModulePrivDb(int id) {
        this.id = id;
        init();
        load();
    }

    public ModulePrivDb(String formCode) {
        this.formCode = formCode;
        init();
    }

    public ModulePrivDb() {
        init();
    }

    @Override
    public void initDB() {
        tableName = "visual_module_priv";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new ModulePrivCache(this);
        isInitFromConfigDB = false;

        QUERY_LIST =
                "select id from visual_module_priv where form_code=? order by priv_type desc, name";
        QUERY_LOAD =
                "select form_code,name,priv_type,see,append,manage,modify,view,search,re_active,field_write,field_hide,import,export,del,log,export_word,data from visual_module_priv where id=?";
        QUERY_DEL = "delete from visual_module_priv where id=?";
        QUERY_SAVE =
                "update visual_module_priv set see=?,append=?,manage=?,modify=?,view=?,search=?,re_active=?,field_write=?,field_hide=?,import=?,export=?,del=?,log=?,export_word=?,data=? where id=?";
        QUERY_CREATE =
                "insert into visual_module_priv (name,priv_type,see,append,manage,form_code,id,modify,view,search,re_active,field_write,field_hide,import,export,del,log,export_word,data) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
     *
     * @param pk Object
     * @return Object
     */
    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ModulePrivDb(pk.getIntValue());
    }

    /**
     * 取得与leafCode节点相关的
     * @param formCode String
     * @return RoleDb
     */
    public Vector getRolesOfModule(String formCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        RoleMgr rm = new RoleMgr();
        try {
            String sql = "select name from visual_module_priv where form_code=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, formCode);
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
            logger.error("getRolesOfModule: " + e.getMessage());
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
                formCode = rs.getString(1);
                name = rs.getString(2);
                type = rs.getInt(3);
                see = rs.getInt(4);
                append = rs.getInt(5);
                manage = rs.getInt(6);
                modify = rs.getInt(7);
                view = rs.getInt(8);
                search = rs.getInt(9);
                reActive = rs.getInt(10);
                fieldWrite = StrUtil.getNullStr(rs.getString(11));
                fieldHide = StrUtil.getNullStr(rs.getString(12));
                importXls = rs.getInt(13);
                exportXls = rs.getInt(14);
                del = rs.getInt(15);
                log = rs.getInt(16);
                exportWord = rs.getInt(17);
                data = rs.getInt(18);
                loaded = true;

                primaryKey.setValue(new Integer(id));
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

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
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

    private int type = 0;

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    private int data = 0;

    @Override
    public boolean save() {
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, see);
            ps.setInt(2, append);
            ps.setInt(3, manage);
            ps.setInt(4, modify);
            ps.setInt(5, view);
            ps.setInt(6, search);
            ps.setInt(7, reActive);
            ps.setString(8, fieldWrite);
            ps.setString(9, fieldHide);
            ps.setInt(10, importXls);
            ps.setInt(11, exportXls);
            ps.setInt(12, del);
            ps.setInt(13, log);
            ps.setInt(14, exportWord);
            ps.setInt(15, data);			
            ps.setInt(16, id);
            r = conn.executePreUpdate();

            if (r==1) {
                ModulePrivCache rc = new ModulePrivCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);

                rc.refreshPrivs(formCode);
             }
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        return r == 1;
    }

    public Vector getModulePrivsOfModuleRaw(String formCode) {
        String sql = "select id from visual_module_priv where form_code=" + StrUtil.sqlstr(formCode);
        return list(sql);
    }

    public Vector getModulePrivsOfModule(String formCode) {
        ModulePrivCache mpc = new ModulePrivCache(this);
        return mpc.getModulePrivsOfModule(formCode);
    }

    /**
     * 判断用户对模块是否有权限
     * @param user UserDb
     * @param groups UserGroupDb[]
     * @param roles RoleDb[]
     * @param privType int 权限类型
     * @return boolean
     */
    public boolean canUserDo(UserDb user, UserGroupDb[] groups, RoleDb[] roles, int privType) {
        // list该节点的所有拥有权限的用户
        long t = System.currentTimeMillis();
        Vector v = getModulePrivsOfModule(formCode);

//        long s = System.currentTimeMillis() - t;
//        DebugUtil.i(getClass(), "canUserDo", getFormCode() + " " + getName() + " time:" + s + " ms");

        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            ModulePrivDb lp = (ModulePrivDb) ir.next();
            if (lp.getType()==TYPE_ROLE) {
                // roles中含有MEMBER
                // 判断该用户所属的角色是否有权限
                for (RoleDb role : roles) {
                    if (role.getCode().equals(lp.getName())) {
                        if (privType != PRIV_REACTIVE && lp.getManage() == 1) {
                            return true;
                        }
                        if (privType == PRIV_SEE) {
                            if (lp.getSee() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_APPEND) {
                            if (lp.getAppend() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_MODIFY) {
                            if (lp.getModify() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_VIEW) {
                            if (lp.getView() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_SEARCH) {
                            if (lp.getSearch() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_REACTIVE) {
                            if (lp.getReActive() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_IMPORT) {
                            if (lp.getImportXls() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_EXPORT) {
                            if (lp.getExportXls() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_DEL) {
                            if (lp.getDel() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_LOG) {
                            if (lp.getLog() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_EXPORT_WORD) {
                            if (lp.getExportWord() == 1) {
                                return true;
                            }
                        } else if (privType == PRIV_DATA) {
                            if (lp.getData() == 1) {
                                return true;
                            }
                        }
                        break;
                    }
                }
            }
            //　权限项对应的是组用户
            else if (lp.getType() == ModulePrivDb.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                	if (privType!=PRIV_REACTIVE && lp.getManage()==1) {
                        return true;
                    }
                    if (privType==PRIV_APPEND) {
                        if (lp.getAppend() == 1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_SEE) {
                        if (lp.getSee()==1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_MODIFY) {
                    	if (lp.getModify()==1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_VIEW) {
                    	if (lp.getView()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_SEARCH) {
                    	if (lp.getSearch()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_REACTIVE) {
                    	if (lp.getReActive()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_IMPORT) {
                    	if (lp.getImportXls()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_EXPORT) {
                    	if (lp.getExportXls()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_DEL) {
                    	if (lp.getDel()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_LOG) {
                    	if (lp.getLog()==1) {
                    	    return true;
                        }
                    }
                    else if (privType == PRIV_EXPORT_WORD) {
                        if (lp.getExportWord()==1) {
                            return true;
                        }
                    }
                    else if (privType == PRIV_DATA) {
                        if (lp.getData()==1) {
                            return true;
                        }
                    }					
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (UserGroupDb group : groups) {
                            if (group.getCode().equals(lp.getName())) {
                                if (privType != PRIV_REACTIVE && lp.getManage() == 1) {
                                    return true;
                                }
                                if (privType == PRIV_APPEND) {
                                    if (lp.getAppend() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_SEE) {
                                    if (lp.getSee() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_MODIFY) {
                                    if (lp.getModify() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_VIEW) {
                                    if (lp.getView() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_SEARCH) {
                                    if (lp.getSearch() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_REACTIVE) {
                                    if (lp.getReActive() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_IMPORT) {
                                    if (lp.getImportXls() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_EXPORT) {
                                    if (lp.getExportXls() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_DEL) {
                                    if (lp.getDel() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_LOG) {
                                    if (lp.getLog() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_EXPORT_WORD) {
                                    if (lp.getExportWord() == 1) {
                                        return true;
                                    }
                                } else if (privType == PRIV_DATA) {
                                    if (lp.getData() == 1) {
                                        return true;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                	if (privType!=PRIV_REACTIVE && lp.getManage()==1) {
                        return true;
                    }
                	
                    if (privType == PRIV_APPEND) {
                        if (lp.getAppend() == 1) {
                            return true;
                        }
                    }else if (privType == PRIV_SEE) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                    }
                    else if (privType==PRIV_MODIFY) {
                    	if (lp.getModify()==1) {
                            return true;
                        }
                    }      
                    else if (privType==PRIV_VIEW) {
                    	if (lp.getView()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_SEARCH) {
                    	if (lp.getSearch()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_REACTIVE) {
                    	if (lp.getReActive()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_IMPORT) {
                    	if (lp.getImportXls()==1) {
                    	    return true;
                        }
                    }
                    else if (privType==PRIV_EXPORT) {
                    	if (lp.getExportXls()==1) {
                    	    return true;
                        }
                    }            
                    else if (privType==PRIV_DEL) {
                    	if (lp.getDel()==1) {
                    	    return true;
                        }
                    }            
                    else if (privType==PRIV_LOG) {
                    	if (lp.getLog()==1) {
                    	    return true;
                        }
                    }
                    else if (privType == PRIV_EXPORT_WORD) {
                        if (lp.getExportWord()==1) {
                            return true;
                        }
                    }
                    else if (privType == PRIV_DATA) {
                        if (lp.getData()==1) {
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
        Privilege pvg = new Privilege();
        if (privType!=PRIV_SEARCH && privType!=PRIV_REACTIVE && privType!=PRIV_DATA) {
	        if (pvg.isUserPrivValid(username, Privilege.ADMIN)) {
	            return true;
	        }
        }

        long t = System.currentTimeMillis();

        UserDb user = new UserDb();
        user = user.getUserDb(username);

        long s = System.currentTimeMillis() - t;

        RoleDb[] roles = user.getRoles();
        // DebugUtil.i(getClass(), "canUserDo", getFormCode() + " 取用户角色 time:" + s + " ms");

        s = System.currentTimeMillis() - t;
        UserGroupDb[] groups = user.getGroups();

        // DebugUtil.i(getClass(), "canUserDo", getFormCode() + " 取用户组 time:" + s + " ms");

        // 如果属于管理员组,则拥有全部权
        // logger.info("groups[i].code=" + groups[i].getCode());
        // for (int i = 0; i < groups.length; i++)
        //    if (groups[i].getCode().equals(groups[i].ADMINISTRATORS))
        //        return true;

        // logger.info("dirCode=" + dirCode + " name=" + lf.getName() + " code=" + lf.getCode() + " parentCode=" + lf.getParentCode());

        boolean re = canUserDo(user, groups, roles, privType);

        /*s = System.currentTimeMillis() - t;
        DebugUtil.i(getClass(), "canUserDo", getFormCode() + " 判断权限 time2:" + s + " ms");*/

        return re;
    }

    public boolean canUserAppend(String username) {
        return canUserDo(username, PRIV_APPEND);
    }
    
    public boolean canUserView(String username) {
        return canUserDo(username, PRIV_VIEW);
    }

    public boolean canUserManage(String username) {
        return canUserDo(username, PRIV_MANAGE);
    }

    public boolean canUserData(String userName) {
        return canUserDo(userName, PRIV_DATA);
    }
    
    public boolean canUserModify(String username) {
    	return canUserDo(username, PRIV_MODIFY);
    }

    public boolean canUserReActive(String username) {
    	return canUserDo(username, PRIV_REACTIVE);
    }
    
    public boolean canUserImport(String userName) {
    	return canUserDo(userName, PRIV_IMPORT);    	
    }
    
    public boolean canUserExport(String userName) {
        return canUserDo(userName, PRIV_EXPORT);
    }

    public boolean canUserExportWord(String userName) {
        return canUserDo(userName, PRIV_EXPORT_WORD);
    }

    public boolean canUserSee(String username) {
        // 20200406 注释掉canUserDo(username, PRIV_MANAGE)，因为在canUserDo中已经针对PRIV_MANAGE做了判断
        return canUserDo(username, PRIV_SEE); // || canUserDo(username, PRIV_MANAGE);
    }
    
    public boolean canUserSearch(String userName) {
    	return canUserDo(userName, PRIV_SEARCH);
    }    
    
    public boolean canUserDel(String userName) {
    	return canUserDo(userName, PRIV_DEL);
    }
    
    public boolean canUserLog(String userName){
    	return canUserDo(userName, PRIV_LOG);
    }
    
    @Override
    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, see);
            ps.setInt(4, append);
            ps.setInt(5, manage);
            ps.setString(6, formCode);

            id = (int)SequenceManager.nextID(SequenceManager.VISUAL_MODULE_PRIV);
            ps.setInt(7, id);
            
            ps.setInt(8, modify);
            ps.setInt(9, view);
            ps.setInt(10, search);
            ps.setInt(11, reActive);
            
            ps.setString(12, fieldWrite);
            ps.setString(13, fieldHide);
            
            ps.setInt(14, importXls);
            ps.setInt(15, exportXls);
            ps.setInt(16, del);
            ps.setInt(17, log);
            ps.setInt(18, exportWord);
            ps.setInt(19, data);
            r = conn.executePreUpdate() == 1 ? true : false;

            if (r) {
                ModulePrivCache rc = new ModulePrivCache(this);
                rc.refreshCreate();

                rc.refreshPrivs(formCode);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("create:数据库操作出错！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public int getModify() {
		return modify;
	}

	public void setModify(int modify) {
		this.modify = modify;
	}

	public boolean setRoles(String leafCode, String roleCodes) throws ErrMsgException {
        String[] ary = StrUtil.split(roleCodes, ",");
        int len = 0;
        if (ary!=null) {
            len = ary.length;
        }
        String sql = "select id from visual_module_priv where form_code=? and priv_type=?";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, leafCode);
            ps.setInt(2, TYPE_ROLE);
            rs = conn.executePreQuery();
            // 删除原来的role 删除该处代码，解决添加新角色时原有角色权限被初始化问题  20150312 jfy
           /* while (rs.next()) {
                getModulePrivDb(rs.getInt(1)).del();
            }*/
            for (int i=0; i<len; i++) {
                create(ary[i], TYPE_ROLE);
            }

            ModulePrivCache rc = new ModulePrivCache(this);
            rc.refreshPrivs(leafCode);
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

    public boolean create(String name, int type) throws
            ErrMsgException {
        this.name = name;
        this.type = type;
        return create();
    }

    public ModulePrivDb getModulePrivDb(int id) {
        return (ModulePrivDb)getObjectDb(new Integer(id));
    }

    /**
     * 删除模块的所有权限设置
     * @param formCode String
     */
    public void delPrivsOfModule(String formCode) {
        String sql = "select id from " + tableName + " where form_code=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {formCode});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                getModulePrivDb(rr.getInt(1)).del();
            }

            ModulePrivCache rc = new ModulePrivCache(this);
            rc.refreshPrivs(formCode);
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
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
                ModulePrivCache rc = new ModulePrivCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);

                rc.refreshPrivs(formCode);
            }
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

    public int getManage() {
        return manage;
    }

    public void setAppend(int a) {
        this.append = a;
    }

    public void setManage(int manage) {
        this.manage = manage;
    }

    public Vector listUserPriv(String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            String sql = "select id from visual_module_priv where name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    ModulePrivDb lp = getModulePrivDb(rs.getInt(1));
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
     * 取得用户有权限操作的表单域，按角色、用户、用户组权限顺序优先获取
     * @param username
     * @param privType
     * @return 可写字段，以逗号分隔，null表示全部不可写，空表示全部可写
     */
    public String getUserFieldsHasPriv(String username, String privType) {
        if (username==null) {
            return null;
        }
        Privilege pvg = new Privilege();
	    if (pvg.isUserPrivValid(username, Privilege.ADMIN)) {
	        return "";
	    }

        UserDb user = new UserDb();
        user = user.getUserDb(username);

        UserGroupDb[] groups = user.getGroups();
        RoleDb[] roles = user.getRoles();
        
        // list该节点的所有拥有权限的用户
        Vector v = getModulePrivsOfModule(formCode);
        if (v.size()==0) {
        	return "";
        }
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            ModulePrivDb lp = (ModulePrivDb) ir.next();
            if (lp.getType()==TYPE_ROLE) {
                if (roles!=null) {
                    // 判断该用户所属的角色是否有权限
                    int len = roles.length;
                    for (int i = 0; i < len; i++) {
                        if (roles[i].getCode().equals(lp.getName())) {
                        	if ("write".equals(privType)) {
                        		return lp.getFieldWrite();
                        	}
                        	else {
                        		return lp.getFieldHide();
                        	}
                        }
                    }
                }
            }            
            else if (lp.getType()==TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                	if ("write".equals(privType)) {
                		return lp.getFieldWrite();
                	}
                	else {
                		return lp.getFieldHide();
                	}
                }
            }            
            //　权限项对应的是组用户
            else if (lp.getType() == ModulePrivDb.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                	if ("write".equals(privType)) {
                		return lp.getFieldWrite();
                	}
                	else {
                		return lp.getFieldHide();
                	}
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                            	if ("write".equals(privType)) {
                            		return lp.getFieldWrite();
                            	}
                            	else {
                            		return lp.getFieldHide();
                            	}
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public Vector<ModulePrivDb> list(String formCode, String orderBy, String sort) {
        String sql = "select id from visual_module_priv where form_code=" + StrUtil.sqlstr(formCode) + " order by " + orderBy + " " + sort;
        return list(sql);
    }

	public int getSearch() {
		return search;
	}

	public void setSearch(int search) {
		this.search = search;
	}

	public void setReActive(int reActive) {
		this.reActive = reActive;
	}

	public int getReActive() {
		return reActive;
	}

	public void setFieldWrite(String fieldWrite) {
		this.fieldWrite = fieldWrite;
	}

	public String getFieldWrite() {
		return fieldWrite;
	}

	public void setFieldHide(String fieldHide) {
		this.fieldHide = fieldHide;
	}

	public String getFieldHide() {
		return fieldHide;
	}

	/**
	 * @param del the del to set
	 */
	public void setDel(int del) {
		this.del = del;
	}

	/**
	 * @return the del
	 */
	public int getDel() {
		return del;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(int log) {
		this.log = log;
	}

	/**
	 * @return the log
	 */
	public int getLog() {
		return log;
	}

}
