package com.redmoon.oa.pvg;

import java.util.List;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;

import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.person.UserDb;

public class RoleDb extends ObjectDb {
    private String code;
    private String desc;

    public static long DISK_QUOTA_NOT_SET = -1;

    /**
     * 所有用户
     */
    public static String CODE_MEMBER = ConstUtil.ROLE_MEMBER;

    /**
     * 普通角色
     */
    public static final int TYPE_NORMAL = 0;

    /**
     * 特定角色
     */
    public static final int TYPE_SPECIAL = 1;

    private int type = TYPE_NORMAL;

    public RoleDb() {
        init();
    }

    public RoleDb(String code) {
        this.code = code;
        init();
        load();
    }

    public RoleDb(String code, String desc) {
        init();

        this.code = code;
        this.desc = desc;
    }

    @Override
    public void initDB() {
        tableName = "user_role";
        isInitFromConfigDB = false;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return null;
    }

    /**
     * 取得某单位的角色（含系统角色）
     * @param unitCode String
     * @return Vector
     */
    public Vector<RoleDb> getRolesOfUnit(String unitCode) {
        return getRolesOfUnit(unitCode, false);
    }
    
    public Vector<RoleDb> getRolesOfUnit(String unitCode, boolean isWithSystem) {
        IRoleService roleService = SpringUtil.getBean(IRoleService.class);
        List<Role> list = roleService.getRolesOfUnit(unitCode, isWithSystem);

        Vector<RoleDb> v = new Vector<>();
        for (Role role : list) {
            RoleDb rd = new RoleDb();
            v.addElement(getFromRole(role, rd));
        }

        return v;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        code = c;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isSystem() {
        return system;
    }

    public int getOrders() {
        return orders;
    }

    public long getDiskQuota() {
        return diskQuota;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public long getMsgSpaceQuota() {
        return msgSpaceQuota;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    private boolean system = false;

    @Override
    public void load() {
        com.cloudweb.oa.cache.RoleCache roleCache = SpringUtil.getBean(com.cloudweb.oa.cache.RoleCache.class);
        Role role = roleCache.getRole(code);
        if (role!=null) {
            getFromRole(role, this);
        }
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public boolean del() throws ErrMsgException {
        return true;
    }

    public RoleDb getRoleDb(String code) {
        // this.code = code;
        com.cloudweb.oa.cache.RoleCache roleCache = SpringUtil.getBean(com.cloudweb.oa.cache.RoleCache.class);
        Role role = roleCache.getRole(code);
        return getFromRole(role, new RoleDb());
    }

    public RoleDb getFromRole(Role role, RoleDb rd) {
        if (role==null) {
            return rd;
        }
        rd.setCode(role.getCode());
        rd.setDesc(role.getDescription());
        rd.setSystem(role.getIsSystem());
        rd.setOrders(role.getOrders());
        rd.setDiskQuota(role.getDiskQuota());
        rd.setUnitCode(role.getUnitCode());
        rd.setMsgSpaceQuota(role.getMsgSpaceQuota());
        rd.setRankCode(role.getRankCode());
        rd.setType(role.getRoleType());
        rd.setDeptManager(deptManager = "1".equals(role.getIsDeptManager()));
        rd.setStatus(role.getStatus());
        rd.setLoaded(true);
        return rd;
    }

    public Vector<UserDb> getAllUserOfRole() {
        return getAllUserOfRole(true);
    }

    /**
     * 取得该角色的所有用户
     * @param isWithGroupUser boolean 是否包含用户组属该角色的用户
     * @return Vector
     */
    public Vector<UserDb> getAllUserOfRole(boolean isWithGroupUser) {
        IRoleService roleService = SpringUtil.getBean(IRoleService.class);
        List<User> list = roleService.getAllUserOfRole(code, isWithGroupUser);
        Vector<UserDb> v = new Vector<>();
        UserDb ud = new UserDb();
        for (User user : list) {
            v.addElement(ud.getUserDb(user.getName()));
        }
        return v;
    }

    /**
     * 取得角色管理的部门
     * @return String[]
     */
    public String[] getAdminDepts() {
        com.cloudweb.oa.cache.RoleCache roleCache = SpringUtil.getBean(com.cloudweb.oa.cache.RoleCache.class);
        return roleCache.getAdminDepts(code);
    }

    /**
     * 根据用户名获取其角色所管理的本部门
     * @param userName
     * @return
     */
    public String[] getDeptsOfManager(String userName) {
    	if (!deptManager) {
    		return null;
    	}

        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
    	List<DeptUser> list = deptUserService.listByUserName(userName);
        String[] depts = new String[list.size()];
        int i = 0;
        for (DeptUser deptUser : list) {
            depts[i] = deptUser.getDeptCode();
            i++;
        }
    	return depts;
    }
    
    public String getRankCode() {
        return rankCode;
    }

    public int getType() {
        return type;
    }

    public void setRankCode(String rankCode) {
        this.rankCode = rankCode;
    }

    public void setType(int type) {
        this.type = type;
    }

	public boolean isDeptManager() {
		return deptManager;
	}

	public void setDeptManager(boolean deptManager) {
        this.deptManager = deptManager;
    }

	public void setDiskQuota(long diskQuota) {
        this.diskQuota = diskQuota;
    }

    public void setMsgSpaceQuota(long msgSpaceQuota) {
        this.msgSpaceQuota = msgSpaceQuota;
    }

    @Override
    public Vector<RoleDb> list() {
        Vector<RoleDb> v = new Vector<>();
        IRoleService roleService = SpringUtil.getBean(IRoleService.class);
        List<Role> list = roleService.list("", "", "", "");
        for (Role role : list) {
            v.addElement(getFromRole(role, new RoleDb()));
        }
        return v;
    }

	private int orders = 0;
    private long diskQuota = DISK_QUOTA_NOT_SET;
    private long msgSpaceQuota;
    private String unitCode = DeptDb.ROOTCODE;
    private String rankCode = "";
    
    private boolean deptManager = true;

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    private boolean status = true;
}
