package com.redmoon.forum.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;

import cn.js.fan.util.*;
import com.redmoon.forum.person.UserDb;
import org.apache.log4j.Logger;

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
public class ZJMobileMsgUtil implements IMsgUtil {
    transient Logger logger = Logger.getLogger(ZJMobileMsgUtil.class.getName());

    public static String headAddr;
    public static String serialNo;
    public static String tailAddr;

    public static final int SMSLEN = 70;

    static {
        Config cfg = new Config();
        headAddr = cfg.getIsUsedProperty("headAddr");
        serialNo = cfg.getIsUsedProperty("serialNo");
        tailAddr = cfg.getIsUsedProperty("tailAddr");
    }

    public synchronized void increaseTailAddr() {
        int a = Integer.parseInt(tailAddr);
        a++;
        if (a>999)
            a = 0;
        tailAddr = StrUtil.PadString("" + a, '0', 3, true);
    }

    public String getOrgAddr() {
        return headAddr + serialNo + tailAddr;
    }

    public boolean send(UserDb user, String msgText, String sender) throws ErrMsgException {
        if (!Sender.isValidMobile("" + user.getMobile())) {
            logger.info("send:" + user.getRealName() + " mobile=" + user.getMobile() + " msgText=" + msgText);
            return false;
        }

        return send(user.getMobile(), msgText, sender);
    }

    public boolean send(String mobile, String msgText, String sender) throws ErrMsgException {
        if (!Sender.isValidMobile(mobile)) {
            // logger.info("send:mobile=" + mobile + " msgText=" + msgText);
            return false;
        }

        boolean re = false;
        int msgLen = msgText.length();

        int seg = (int)Math.round(((double) msgLen) / SMSLEN + 0.5d);
        String[] msgAry = new String[seg];
        int p = 0;
        int k = 0;
        // 如果短消息长度超过了70，则将其置入数组
        while (p < msgLen) {
            int q = p + SMSLEN;
            // System.out.println(getClass() + " seg=" + seg + " msgText=" + msgText + " p=" + p + " q=" + q);

            if (q > msgLen)
                q = msgLen;
            msgAry[k] = msgText.substring(p, q);
            k++;
            p += SMSLEN;
        }

        for (k = 0; k < seg; k++) {
            // re = Sender.send(orgAddr, mobile, strSendTime, validTime, serviceId,
            //                 feeCode, feeType, msgAry[k], msgFormat);
            Config smscfg = new Config();
            String sendSmsUrl = smscfg.getIsUsedProperty("sendSmsUrl");
            String strURL = sendSmsUrl.trim();
            
            // urlencode 处理短信参数中的空格
            String contentSms = msgAry[k];
            try {
                contentSms = java.net.URLEncoder.encode(contentSms, "GBK");
            } catch (UnsupportedEncodingException e) {
                logger.error(getClass() + "SendSms to RoyaMas: "+ e);
            }

            String response = "";
            try {
                strURL += "?MobilePhones=" + mobile
                        + "&Content=" + contentSms;
                        // + "&Priority=" + priority
                        // + "&ExNumber=" + exNumber
                        // + "&MessageFlag=" + messageFlag
                        // + "&ModuleName=" + moduleName;
                URL objURL = new URL(strURL);
                URLConnection objConn = objURL.openConnection();
                objConn.setDoInput(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        objConn.getInputStream()));
                String line = br.readLine();
                while (line != null) {
                    response += line;
                    line = br.readLine();
                    logger.info(getClass() + ": SendSMS to " + mobile + " " +
                                line);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }        	
        	
            if (re) {
                SMSSendRecordDb ssrd = new SMSSendRecordDb();
                ssrd.setUserName(sender);
                ssrd.setSendMobile(mobile);
                ssrd.setMsgText(msgText);
                // ssrd.setOrgAddr(orgAddr);
                ssrd.create();
            }
        }

        return re;
    }
}
