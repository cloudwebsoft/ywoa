package com.cloudweb.oa.mq;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IMsgService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

public class MsgConsumer implements javax.jms.MessageListener {
    @Autowired
    IMsgService msgService;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                MsgInfo msgInfo = (MsgInfo)((ObjectMessage) message).getObject();
                if (msgInfo.getType() == ConstUtil.MQ_MSG_TYPE_MESSAGE) {
                    if (msgInfo.getUserNameArr()!=null) {
                        msgService.sendSysMsg(msgInfo.getUserNameArr(), msgInfo.getTitle(), msgInfo.getContent(), msgInfo.getActionType(), msgInfo.getActionSubType(), msgInfo.getAction());
                    }
                    else {
                        msgService.sendSysMsg(msgInfo.getUserName(), msgInfo.getTitle(), msgInfo.getContent(), msgInfo.getActionType(), msgInfo.getActionSubType(), msgInfo.getAction());
                    }
                    DebugUtil.i(getClass(), "onMessage", msgInfo.getTitle());
                }
                else if (msgInfo.getType()== ConstUtil.MQ_MSG_TYPE_MSG) {
                    msgService.sendSysMsg(msgInfo.getUserName(), msgInfo.getTitle(), msgInfo.getContent(), msgInfo.getAction());
                    DebugUtil.i(getClass(), "onMessage", msgInfo.getTitle());
                }
                else if (msgInfo.getType()==ConstUtil.MQ_MSG_TYPE_EMAIL) {
                    String charset = Global.getSmtpCharset();
                    cn.js.fan.mail.SendMail sendMail = new cn.js.fan.mail.SendMail(charset);
                    String senderName = StrUtil.GBToUnicode(Global.AppName);
                    senderName += "<" + Global.getEmail() + ">";
                    String mailserver = Global.getSmtpServer();
                    int smtp_port = Global.getSmtpPort();
                    String name = Global.getSmtpUser();
                    String pwd_raw = Global.getSmtpPwd();
                    boolean isSsl = Global.isSmtpSSL();
                    try {
                        sendMail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
                    } catch (Exception ex) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                    }

                    sendMail.initMsg(msgInfo.getEmail(), senderName, msgInfo.getTitle(), msgInfo.getContent(), true);
                    sendMail.send();
                    sendMail.clear();
                }
                else if (msgInfo.getType()==ConstUtil.MQ_MSG_TYPE_SMS) {
                    if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                        UserMgr um = new UserMgr();
                        UserDb ud = um.getUserDb(msgInfo.getUserName());
                        IMsgUtil imu = SMSFactory.getMsgUtil();
                        if (msgInfo.getUserNameArr()!=null) {
                            imu.sendBatch(msgInfo.getUserNameArr(), msgInfo.getTitle(), msgInfo.getSender());
                        }
                        else {
                            if (StringUtils.isEmpty(msgInfo.getSender())) {
                                imu.send(ud, msgInfo.getTitle(), ConstUtil.USER_SYSTEM);
                            }
                            else {
                                imu.send(ud, msgInfo.getTitle(), msgInfo.getSender());
                            }
                        }
                    }
                }
            } else if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String msg = txtMsg.getText();
                int length = msg.length();
                LogUtil.getLog(getClass()).info("[moduleLog] Received: '" + msg+ "' (length " + length + ")");
            }
        } catch (JMSException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            try {
                message.acknowledge();
            } catch (JMSException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }
}