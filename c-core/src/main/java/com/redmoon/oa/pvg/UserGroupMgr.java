package com.redmoon.oa.pvg;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import java.util.List;

import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.service.IGroupService;
import com.cloudweb.oa.utils.SpringUtil;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;

public class UserGroupMgr {

    public UserGroupMgr() {
    }

    public UserGroupDb getUserGroupDb(String code) {
        UserGroupDb ugd = new UserGroupDb();
        ugd = ugd.getUserGroupDb(code);
        return ugd;
    }

    /**
     * 取出所有的用户组
     * @param isDept boolean 是否为部门型用户组
     * @return UserGroupDb[]
     */
    public UserGroupDb[] getAllUserGroup(boolean isDept) {
        IGroupService groupService = SpringUtil.getBean(IGroupService.class);
        List<Group> list = groupService.listByIsDept(isDept);
        UserGroupDb[] ugArr = new UserGroupDb[list.size()];
        int i = 0;
        UserGroupDb userGroupDb = new UserGroupDb();
        for (Group group : list) {
            ugArr[i] = userGroupDb.getUserGroupDb(group.getCode());
            i++;
        }
        return ugArr;
    }
}
