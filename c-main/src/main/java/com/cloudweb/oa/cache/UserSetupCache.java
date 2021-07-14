package com.cloudweb.oa.cache;

import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class UserSetupCache extends ObjCache {

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    IRoleService roleService;

    private static final Lock lock = new ReentrantLock();

    public UserSetup getUserSetup(String userName) {
        return (UserSetup) getObj(this, userName);
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        UserSetup userSetup = (UserSetup) obj;
        return userSetup.getUserName();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        UserSetup userSetup = new UserSetup();
        userSetup.setUserName(value);
        return userSetup;
    }

    @Override
    public Object getObjRaw(String key) {
        return userSetupService.getUserSetup(key);
    }
}