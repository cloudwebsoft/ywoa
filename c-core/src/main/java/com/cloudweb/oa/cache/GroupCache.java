package com.cloudweb.oa.cache;

import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.service.IGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class GroupCache extends ObjCache {

    @Autowired
    IGroupService groupService;

    private static final Lock lock = new ReentrantLock();

    public Group getGroup(String code) {
        return (Group)getObj(this, code);
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        Group group = (Group)obj;
        return group.getCode();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        Group group = new Group();
        group.setCode(value);
        return group;
    }

    @Override
    public Object getObjRaw(String key) {
        return groupService.getGroup(key);
    }
}
