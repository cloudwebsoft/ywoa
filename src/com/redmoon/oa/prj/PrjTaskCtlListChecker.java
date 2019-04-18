package com.redmoon.oa.prj;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.chat.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-1-24下午05:48:27
 */
public class PrjTaskCtlListChecker implements IModuleChecker {
	
    public PrjTaskCtlListChecker() {
        super();
    }

    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {

        return true;
    }
    
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {

        return true;
    }

    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {

        return true;
    }

    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	Privilege pvg = new Privilege();
    	String userName = pvg.getUser(request);
    	
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb("prj_taskcontrol");
    	FormDAO fdaoTaskCtl = new FormDAO();
    	fdaoTaskCtl = fdaoTaskCtl.getFormDAO(StrUtil.toLong(fdao.getCwsId()), fd);
    	String person = fdaoTaskCtl.getFieldValue("person"); // 负责人
    	String content = fdaoTaskCtl.getFieldValue("content");
    	String pre_time = fdaoTaskCtl.getFieldValue("pre_time");
    	
    	// 发送邮件及消息
        Config cfg = new Config();
        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
        String charset = Global.getSmtpCharset();
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";
        if (flowNotifyByEmail) {
            String mailserver = Global.getSmtpServer();
            int smtp_port = Global.getSmtpPort();
            String name = Global.getSmtpUser();
            String pwd_raw = Global.getSmtpPwd();
            boolean isSsl = Global.isSmtpSSL();
            try {
                sendmail.initSession(mailserver, smtp_port, name,
                                     pwd_raw, "", isSsl);
            } catch (Exception ex) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
            }
        }

        String t = SkinUtil.LoadString(request,
                                       "res.module.prj",
                                       "msg_supervision_title");
        String c = SkinUtil.LoadString(request,
                                       "res.module.prj",
                                               "msg_supervision_content");
        t = StrUtil.format(t, new Object[]{userName});
        c = StrUtil.format(c, new Object[]{pre_time, content});

        UserDb user = new UserDb();
        user = user.getUserDb(person);
        
        IMsgUtil imu = SMSFactory.getMsgUtil();
        if (imu != null) {
            imu.send(user, StrUtil.getLeft(c, 70), MessageDb.SENDER_SYSTEM);
        }

            // 发送信息
            // String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad2.getId();
        String action = "";
        MessageDb md = new MessageDb();
        md.sendSysMsg(person, t, c, action);

        if (flowNotifyByEmail) {
            if (user.getEmail()!=null && !user.getEmail().equals("")) {
                /*
            	UserSetupDb usd = new UserSetupDb(person);
                c += "<BR />>>&nbsp;<a href='" +
                        Global.getFullRootPath(request) +
                        "/public/flow_dispose.jsp?action=" + action +
                        "' target='_blank'>" + 
                        (usd.getLocal().equals("en-US") ? "Click here to apply" : "请点击此处办理") + "</a>";
                */
                sendmail.initMsg(user.getEmail(),
                                 senderName,
                                 t, c, true);
                sendmail.send();
                sendmail.clear();
            }
        }
    	
    	return true;
    }

    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	return false;
    } 
    
    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }    
}
