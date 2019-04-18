package com.redmoon.forum.person;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.message.*;

public class UserFriendMgr {

    public UserFriendMgr() {
    }

    public boolean sendAddFriendRequest(HttpServletRequest request) throws ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        }
        String userName = pvg.getUser(request);
        String friend = ParamUtil.get(request, "friend");
        String addType = ParamUtil.get(request, "type");
        if (addType.equals("nick")) {
            UserDb userDd = new UserDb();
            userDd = userDd.getUserDbByNick(friend);
            if (userDd == null) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.forum.person.userservice", "user_not_exist"));
            }
            friend = userDd.getName();
        } else {
            UserDb userDd = new UserDb();
            userDd = userDd.getUser(friend);
            if (!userDd.isLoaded()) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.forum.person.userservice", "user_not_exist"));
            }
        }
        // 不能添加自己为好友
        if(userName.equals(friend)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.person.userservice", "err_add_self"));
        } else {
            UserFriendDb userFriendDb = new UserFriendDb();
            userFriendDb.setName(userName);
            userFriendDb.setFriend(friend);
            userFriendDb.setState(0); // state为0，等待身份验证
            boolean re = false;
            try {
                re = userFriendDb.create();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            if (re) {
                // 向好友发送申请信息
                sendMessage(request, userName, friend, 0);
            }
            return re;
        }
    }

    public boolean sendMessage(HttpServletRequest request, String senderName,String receiverName,int messageType) {
        String messageContent = "";
        UserDb sender = new UserDb();
        sender = sender.getUser(senderName);
        String senderNick = sender.getNick();
        switch(messageType) {
        case 0:
            messageContent = StrUtil.format(SkinUtil.LoadString(request, "res.forum.person.UserFriendDb", "ADD_FRIEND_REQUEST"), new Object[] {senderNick});
            break;
        case 1:
            messageContent = StrUtil.format(SkinUtil.LoadString(request, "res.forum.person.UserFriendDb", "ADD_FRIEND_REFUSEED"), new Object[] {senderNick});
            break;
        case 2:
            messageContent = StrUtil.format(SkinUtil.LoadString(request, "res.forum.person.UserFriendDb", "ADD_FRIEND_ACCEPTED"), new Object[] {senderNick});
            break;
        default:
            return false;
        }
        MessageDb messageDb = new MessageDb();
        messageDb.setTitle(SkinUtil.LoadString(request, "res.forum.person.UserFriendDb", "sys_msg"));
        messageDb.setContent(messageContent);
        messageDb.setSender(messageDb.USER_SYSTEM);
        messageDb.setReceiver(receiverName);
        messageDb.setType(MessageDb.TYPE_SYSTEM);
        messageDb.setIp("");
        boolean re = false;
        try {
            re = messageDb.create();
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return re;
    }

    public boolean refuse(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        UserFriendDb ufd = new UserFriendDb();
        ufd = ufd.getUserFriendDb(id);
        return sendMessage(request, ufd.getFriend(), ufd.getName(), 1);
    }

    public boolean accept(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        UserFriendDb ufd = new UserFriendDb();
        ufd = ufd.getUserFriendDb(id);
        ufd.setState(1);
        boolean re = ufd.save();
        if (re) {
            UserFriendDb ufd2 = new UserFriendDb();
            ufd2.setFriend(ufd.getName());
            ufd2.setName(ufd.getFriend());
            ufd2.setState(1);
            try {
                ufd2.create();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            sendMessage(request, ufd.getFriend(), ufd.getName(), 2);
        }
        return re;
    }

    /**
     * 把好友从好友列表中删除
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delFriend(HttpServletRequest request) throws ErrMsgException {
        int delid = ParamUtil.getInt(request, "delid");
        UserFriendDb ufd = new UserFriendDb();
        ufd = ufd.getUserFriendDb(delid);
        if (!ufd.isLoaded()) {
            return false;
        }
        if (!ufd.getName().equals(Privilege.getUser(request))) {
            if (!Privilege.isMasterLogin(request)) {
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
            }
        }
        boolean re = ufd.del();
        if (re) {
            // 从对方的好友列表中删除
            ufd = ufd.getUserFriendDb(ufd.getFriend(), ufd.getName());
            if (ufd!=null)
                ufd.del();
        }
        return re;
    }

}
