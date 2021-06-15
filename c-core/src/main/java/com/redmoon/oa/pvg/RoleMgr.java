package com.redmoon.oa.pvg;

public class RoleMgr {

    public RoleMgr() {
    }

    public RoleDb getRoleDb(String code) {
        RoleDb rd = new RoleDb();
        return rd.getRoleDb(code);
    }
}
