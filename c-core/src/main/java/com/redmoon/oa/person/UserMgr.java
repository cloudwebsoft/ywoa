package com.redmoon.oa.person;

import org.apache.log4j.Logger;

public class UserMgr {
    public Logger logger = null;

    public UserMgr() {
        logger = Logger.getLogger(this.getClass().getName()); 
    }

    public UserDb getUserDb(String name) {
        UserDb ud = new UserDb();
        return ud.getUserDb(name);
    }
}
