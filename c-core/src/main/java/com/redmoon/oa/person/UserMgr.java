package com.redmoon.oa.person;

public class UserMgr {

    public UserMgr() {

    }

    public UserDb getUserDb(String name) {
        UserDb ud = new UserDb();
        return ud.getUserDb(name);
    }
}
