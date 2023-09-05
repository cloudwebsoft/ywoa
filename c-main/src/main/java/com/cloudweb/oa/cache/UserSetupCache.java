package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.DateUtil;
import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class UserSetupCache extends ObjCache {

    @Autowired
    SysProperties sysProperties;

    private static final String LAST_MSG_NOTIFY_PREFIX_FROM = "lastMsgNotifyTimeFrom_";

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

    public Date getLastMsgNotifyTimeByCache(String userName) {
        // 与7.0版有所区别，因为7.0版会弹出消息，为了防止多次弹出，会记住上次的弹出时间，而8.0不弹出，故只需取3天内的消息即可
        int days = sysProperties.getMsgFetchDays();
        return DateUtil.addDate(new Date(), -days);

        /*Date dFrom = null;
        if (RMCache.getInstance().getCanCache()) {
            dFrom = (Date) RMCache.getInstance().get(LAST_MSG_NOTIFY_PREFIX_FROM + userName);
            if (dFrom == null) {
                dFrom = new Date();
                try {
                    RMCache.getInstance().put(LAST_MSG_NOTIFY_PREFIX_FROM + userName, new Date());
                } catch (CacheException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            return dFrom;
        }
        else {
            UserSetup userSetup = getUserSetup(userName);
            LocalDateTime lastNotifyTime = userSetup.getLastMsgNotifyTime();
            Date dt;
            if (lastNotifyTime != null) {
                dt = DateUtil.asDate(lastNotifyTime);
            } else {
                // 如果lastNotifyTime为null，则从当天的0点开始查询
                dt = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
            }
            return dt;
        }*/
    }

    public void setLastMsgNotifyTimeByCache(String userName) {
        /*try {
            if (RMCache.getInstance().getCanCache()) {
                Date curDate = new Date();

                Date fromDate = (Date) RMCache.getInstance().get(LAST_MSG_NOTIFY_PREFIX_FROM + userName);
                if (fromDate != null) {
                    // 如果起始时间超过12小时则写入数据库，即刷新12小时内的消息
                    if (DateUtil.datediffMinute(curDate, fromDate) >= 720) {
                        RMCache.getInstance().put(LAST_MSG_NOTIFY_PREFIX_FROM + userName, curDate);

                        UserSetup userSetup = getUserSetup(userName);
                        userSetup.setLastMsgNotifyTime(DateUtil.toLocalDateTime(curDate));
                        userSetupService.updateByUserName(userSetup);
                    }
                }
            }
            else {
                UserSetup userSetup = getUserSetup(userName);
                userSetup.setLastMsgNotifyTime(DateUtil.toLocalDateTime(new Date()));
                userSetupService.updateByUserName(userSetup);
            }
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        }*/
    }
}