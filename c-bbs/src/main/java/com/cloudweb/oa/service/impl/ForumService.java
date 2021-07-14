package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.api.IForumService;
import com.redmoon.forum.Privilege;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class ForumService implements IForumService {

    @Override
    public boolean JumpToCommunity(HttpServletRequest request, HttpServletResponse response, String userName) throws ErrMsgException {
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        return pvg.JumpToCommunity(request, response, userName);
    }

    @Override
    public boolean isUserLogin(HttpServletRequest request) {
        return Privilege.isUserLogin(request);
    }

    @Override
    public String getUser(HttpServletRequest request) {
        return Privilege.getUser(request);
    }
}
