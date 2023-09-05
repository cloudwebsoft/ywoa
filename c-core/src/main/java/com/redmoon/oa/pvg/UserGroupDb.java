package com.redmoon.oa.pvg;

import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IGroupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.oa.dept.*;
import com.redmoon.oa.person.*;

public class UserGroupDb extends ObjectDb {
    /**
     * 如果是部门型的用户组，则code与部门编码相同
     */
    private String code;

    private String desc;

    // 系统用户组
    public static final String EVERYONE = ConstUtil.GROUP_EVERYONE;
    public static final String ADMINISTRATORS = ConstUtil.GROUP_ADMINISTRATORS;

    public UserGroupDb() {
        init();
    }

    public UserGroupDb(String code) {
        this.code = code;
        load();
        init();
    }

    public UserGroupDb(String code, String desc) {
        init();

        this.code = code;
        this.desc = desc;
    }

    @Override
    public void initDB() {
        tableName = "user_group";
        isInitFromConfigDB = false;
    }

    @Override
    public Vector<UserGroupDb> list() {
        Vector<UserGroupDb> v = new Vector<>();
        IGroupService groupService = SpringUtil.getBean(IGroupService.class);
        List<Group> list = groupService.getAll();
        for (Group group : list) {
            v.addElement(getFromGroup(group, new UserGroupDb()));
        }
        return v;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return null;
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

    public boolean isDept() {
        return dept;
    }

    public boolean isIncludeSubDept() {
        return includeSubDept;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setDept(boolean dept) {
        this.dept = dept;
    }

    public void setIncludeSubDept(boolean includeSubDept) {
        this.includeSubDept = includeSubDept;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    private boolean system = false;

    public Vector getUserGroupsOfUnit(String unitCode) {
        IGroupService groupService = SpringUtil.getBean(IGroupService.class);
        List<Group> list = groupService.listByUnitCode(unitCode);
        Vector<UserGroupDb> v = new Vector<>();
        for (Group group : list) {
            v.addElement(getFromGroup(group, new UserGroupDb()));
        }
        return v;
    }

    public UserGroupDb getFromGroup(Group group, UserGroupDb ugd) {
        if (group==null) {
            return ugd;
        }
        ugd.setCode(group.getCode());
        ugd.setDesc(group.getDescription());
        ugd.setDept(group.getIsDept());
        ugd.setSystem(group.getIsSystem());
        ugd.setIncludeSubDept(group.getIsIncludeSubDept() == 1);
        ugd.setDeptCode(group.getDeptCode());
        ugd.setUnitCode(group.getUnitCode());
        ugd.setLoaded(true);
        return ugd;
    }

    @Override
    public void load() {
        com.cloudweb.oa.cache.GroupCache roleCache = SpringUtil.getBean(com.cloudweb.oa.cache.GroupCache.class);
        Group group = roleCache.getGroup(code);
        if (group != null) {
            getFromGroup(group, this);
        }
    }

    /**
     * 取得该用户组的所有用户
     *
     * @return Vector
     */
    public Vector getAllUserOfGroup() {
        IGroupService groupService = SpringUtil.getBean(IGroupService.class);
        List<User> list = groupService.getAllUserOfGroup(code);
        Vector<UserDb> v = new Vector<>();
        UserDb ud = new UserDb();
        for (User user : list) {
            v.addElement(ud.getUserDb(user.getName()));
        }

        return v;
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public boolean del() throws ErrMsgException {
        return true;
    }

    public UserGroupDb getUserGroupDb(String code) {
        // this.code = code;
        com.cloudweb.oa.cache.GroupCache roleCache = SpringUtil.getBean(com.cloudweb.oa.cache.GroupCache.class);
        Group group = roleCache.getGroup(code);
        return getFromGroup(group, new UserGroupDb());
    }

    /**
     * 取得用户组所属的角色
     *
     * @return RoleDb[]
     */
    public RoleDb[] getRoles() {
        String sql =
                "select roleCode from user_group_of_role where userGroupCode=?";
        Conn conn = new Conn(connname);
        RoleDb[] rds = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();

            if (rs != null) {
                rds = new RoleDb[conn.getRows()];
                int i = 0;
                String code;
                RoleMgr rm = new RoleMgr();
                while (rs.next()) {
                    code = rs.getString(1);
                    rds[i] = rm.getRoleDb(code);
                    i++;
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getRoles:" + e.getMessage());
        } finally {
            conn.close();
        }
        return rds;
    }

    private boolean dept = false;
    private boolean includeSubDept = true;
    private String deptCode;
    private String unitCode;
}
