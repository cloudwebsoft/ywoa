package com.redmoon.oa.message;

import java.lang.reflect.*;

import com.cloudwebsoft.framework.aop.advice.*;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserDb;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MobileAfterAdvice extends AfterAdvice {

    /**
     * Before
     *
     * @param proxy Object
     * @param method Method
     * @param args Object[]
     * @throws Throwable
     * @todo Implement this com.cloudwebsoft.framework.aop.base.Advice method
     */
    @Override
    public void After(Object proxy, Method method, Object[] args) throws
            Throwable {
        if (method.getName().equals("AddMsg")) {
            IMessage imsg = (IMessage) proxy;
            String receiver = imsg.getFileUpload().getFieldValue("receiver");
            IMsgUtil imu = SMSFactory.getMsgUtil();
            String isToMobile = StrUtil.getNullStr(imsg.getFileUpload().getFieldValue("isToMobile"));
            if (isToMobile.equals("true") && imu != null) {
                String[] ary = receiver.split(",");
                int len = ary.length;
                UserMgr um = new UserMgr();
                UserDb ud = null;
                for (int i = 0; i < len; i++) {
                    ud = um.getUserDb(ary[i]);
                    // 去掉html标签
                    String content = StrUtil.getAbstract(null, imsg.getContent(), 100000, "");
                    imu.send(ud, content, imsg.getSender());
                }
            }
        }
        else if (method.getName().equals("sendSysMsg")) {
            IMsgUtil imu = SMSFactory.getMsgUtil();
            if (imu != null) {
                UserMgr um = new UserMgr();
                UserDb ud = um.getUserDb((String) args[0]);
                // 去掉html标签
                String content = StrUtil.getAbstract(null, (String) args[2], 100000, "");
                imu.send(ud, content, MessageDb.SENDER_SYSTEM);
            }
        }
        else if (method.getName().equals("TransmitMsg")) { // 转发
            IMessage imsg = (IMessage) proxy;
            
            String receiver = imsg.getReceiver();
            // String receiver = imsg.getFileUpload().getFieldValue("receiver");

            IMsgUtil imu = SMSFactory.getMsgUtil();
            // String isToMobile = StrUtil.getNullStr(imsg.getFileUpload().getFieldValue("isToMobile"));
            
            DebugUtil.i(getClass(), "TransmitMgr", "receiver=" + receiver + " imu=" + imu);
            
            if (imu != null) {
                String[] ary = receiver.split(",");
                int len = ary.length;
                UserMgr um = new UserMgr();
                UserDb ud = null;
                for (int i = 0; i < len; i++) {
                    ud = um.getUserDb(ary[i]);
                    // 去掉html标签
                    String content = StrUtil.getAbstract(null, imsg.getContent(), 100000, "");
                    imu.send(ud, content, imsg.getSender());
                }
            }        	
        }        
    }
}
