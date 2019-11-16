package com.cloudweb.oa.controller;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.util.*;

import com.redmoon.mail.MailSenderInfo;
import com.redmoon.mail.sender.MailSender;
import com.redmoon.mail.sender.SSLMailSender;
import com.redmoon.mail.sender.SimpleMailSender;
import com.redmoon.oa.fileark.Directory;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.robot.Group;
import com.redmoon.oa.robot.RobotUtil;
import com.redmoon.oa.visual.FormDAO;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.*;

@Controller
@RequestMapping("/public/robot")
public class RobotController {
    @Autowired
    private HttpServletRequest request;

    /**
     * 抢红包
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/grab", method = RequestMethod.POST, produces = {"text/html;charset=utf-8", "application/json;"})
    public String grab() {
        // 判断权限
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        try {
            if (!pvg.isUserPrivValid(request, "read")) {
                json.put("ret", -2);
                json.put("msg", "请先登录！");
                return json.toString();
            }

            long resultId = ParamUtil.getLong(request, "resultId", -1);
            String groupId = ParamUtil.get(request, "groupId");
            String userName = pvg.getUser(request);

            com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();

            String formCode = "robot_red_bag_res";
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(resultId, fd);
            String status = fdao.getFieldValue("status");
            String tip = fdao.getFieldValue("tip");

            // 判断该红包是否已被抢
            if ("是".equals(status)) {
                String redBagGrabFail = cfg.getProperty("redBagGrabFail");
                json.put("ret", -1);
                json.put("msg", redBagGrabFail);
                return json.toString();
            }

            // 取紅包批次中的設置記錄的ID
            String strBatchNo = fdao.getFieldValue("batch_no");
            long batchNo = StrUtil.toLong(strBatchNo, -1);

            int curCount = 0; // 本次活动已抢的红包数
            int maxCount = 1;

            // 根据批次取得红包设置的ID
            FormDb fdBat = new FormDb();
            fdBat = fdBat.getFormDb("robot_red_bag_bat");
            String sql = "select id from form_table_robot_red_bag_bat where batch_no=" + batchNo;
            FormDAO fdaoBat = new FormDAO();
            Iterator ir = fdaoBat.list("robot_red_bag_bat", sql).iterator();
            if (ir.hasNext()) {
                fdaoBat = (FormDAO) ir.next();
                String strEndTime = fdaoBat.getFieldValue("end_time");
                maxCount = StrUtil.toInt(fdaoBat.getFieldValue("max_count"), 0);

                // 判斷是否已過期
                Date endTime = DateUtil.parse(strEndTime, "yyyy-MM-dd HH:mm:ss");
                if (DateUtil.compare(new Date(), endTime) == 1) {
                    String redBagGrabExpire = cfg.getProperty("redBagGrabExpire");
                    redBagGrabExpire = StrUtil.format(redBagGrabExpire, new Object[]{strEndTime});
                    json.put("ret", -3);
                    json.put("msg", redBagGrabExpire);
                    return json.toString();
                }

                // 判斷是否已超量
                if (maxCount > 0) {
                    sql = "select count(*) from form_table_robot_red_bag_res where batch_no=? and user_name=?";
                    JdbcTemplate jt = new JdbcTemplate();
                    try {
                        ResultIterator ri = jt.executeQuery(sql, new Object[]{batchNo, pvg.getUser(request)});
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            curCount = rr.getInt(1);
                            if (curCount >= maxCount) {
                                String msg = cfg.getProperty("redBagGrabMaxCount");
                                msg = StrUtil.format(msg, new Object[]{maxCount});
                                json.put("ret", -4);
                                json.put("msg", msg);
                                return json.toString();
                            }
                        }
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            // 根据红包设置中的有效期月份数，置有效时间
            String item = fdao.getFieldValue("item"); // 红包设置项
            long nestItemId = StrUtil.toLong(item);
            FormDb fdNest = new FormDb();
            fdNest = fdNest.getFormDb("robot_red_bag_nest");
            FormDAO fdaoNest = new FormDAO();
            fdaoNest = fdaoNest.getFormDAO(nestItemId, fdNest);
            if (!fdaoNest.isLoaded()) {
                json.put("ret", -1);
                json.put("msg", "红包设置项已不存在");
                return json.toString();
            }

            String kind = fdao.getFieldValue("kind"); // 类别
            if (!kind.equals("0")) { // 如果不是积分型的，则置有效日期
                int months = StrUtil.toInt(fdaoNest.getFieldValue("months"), 3);
                // 置米票或菜票的有效日期
                Date bd = new Date();
                Date ed = DateUtil.addDate(bd, months * 30);
                fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
                fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
            }

            fdao.setFieldValue("user_name", userName);
            fdao.setFieldValue("status", "是");
            fdao.setFieldValue("my_date", DateUtil.format(new Date(), "yyyy-MM-dd"));
            boolean re = fdao.save();

            if (re) {
                // 如果是积分类型的，则加分，其它类型的通过流程领取
                if (kind.equals("0")) {
                    FormDAO fdaoSign = RobotUtil.getLastSign(groupId, userName);
                    if (fdaoSign == null) {
                        // 一般不存在此情况
                        json.put("ret", -5);
                        json.put("msg", "抽奖无效，积分无法操作，您还没有帐户，请在群中“签到”以创建帐户");
                    } else {
                        // 根据红包设置项取得奖项的数量
                        /*
						String strItem = fdao.getFieldValue("item");
						long itemId = StrUtil.toLong(strItem, -1);
						FormDb fdNest = new FormDb();
						fdNest = fdNest.getFormDb("robot_red_bag_nest");
						FormDAO fdaoNest = new FormDAO();
						fdaoNest = fdaoNest.getFormDAO(itemId, fdNest);
						*/
                        int counts = StrUtil.toInt(fdaoNest.getFieldValue("counts"), 0);
                        // 增加积分
                        int score = StrUtil.toInt(fdaoSign.getFieldValue("score"), 0);
                        int scoreRemained = StrUtil.toInt(fdao.getFieldValue("score_remained"), 0);
                        score += counts;
                        scoreRemained += counts;
                        fdaoSign.setFieldValue("score", String.valueOf(score));
                        fdaoSign.setFieldValue("score_remained", String.valueOf(scoreRemained));
                        fdaoSign.save();

                        // 记录得分明细
                        String relateId = String.valueOf(fdao.getId());
                        RobotUtil.logScoreDetail(userName, "red_bag_score", counts, relateId, "", "");
                    }
                }

                curCount++;

                json.put("ret", 1);
                json.put("msg", "操作成功！");
                json.put("maxCount", maxCount);
                json.put("curCount", curCount);

                String redBagGrabNotice = cfg.getProperty("redBagGrabNotice");
                redBagGrabNotice = StrUtil.format(redBagGrabNotice, new Object[]{"[@" + userName + "]", tip});
                RobotUtil.sendClusterMsg(request, groupId, redBagGrabNotice);
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 发布文章至QQ社群
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/shareToQQGroups", method = RequestMethod.POST, produces = {"text/html;charset=utf-8", "application/json;"})
    public String shareToQQGroups() {
        JSONObject json = new JSONObject();

        int id = ParamUtil.getInt(request, "id", -1);
        // String title = ParamUtil.get(request, "title");

        com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();

        Map groups = cfg.getGroups();
        Iterator ir = groups.keySet().iterator();
        while (ir.hasNext()) {
            String gid = (String) ir.next();
            Group group = (Group) groups.get(gid);
            if (group.isFilearkShareOpen()) {
                String docShowUrl = group.getDocShowUrl();

                Document doc = new Document();
                doc = doc.getDocument(id);

                // 如果文章中无图片，则取得默认的分享图片
                String imgUrl = doc.getFirstImagePathOfDoc();
                if ("".equals(imgUrl)) {
                    imgUrl = group.getFilearkShareDefaultImg();
                } else {
                    if (!imgUrl.startsWith("http:")) {
                        imgUrl = Global.getFullRootPath(request) + "/" + imgUrl;
                    }
                }

                docShowUrl += doc.getId() + "&groupId=" + gid;

                com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
                String desKey = ssoconfig.get("key");
                String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(id) + "|" + new Date().getTime());
                docShowUrl += "&visitKey=" + visitKey;

                String filearkShareNotice = cfg.getProperty("filearkShareNotice");
                filearkShareNotice = StrUtil.format(filearkShareNotice, new Object[]{"[image]" + imgUrl + "[/image]", doc.getTitle(), doc.getAuthor(), docShowUrl});

                RobotUtil.sendClusterMsg(request, group.getId(), filearkShareNotice);
            }
        }
        try {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    /**
     * 分享文章至朋友圈，送大礼包
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/shareTimeline", method = RequestMethod.POST, produces = {"text/html;charset=utf-8", "application/json;"})
    public String shareTimeline() {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        String qq = pvg.getUser(request);

        String formCode = "robot_red_bag_res";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(fd);
        fdao.setFieldValue("batch_no", "0");
        fdao.setFieldValue("kind", "1"); // 米票
        fdao.setFieldValue("item", "0"); // 奖项
        fdao.setFieldValue("user_name", qq);
        fdao.setFieldValue("tip", "云网OA标准版");
        // 送3张米票，分别第1个月、第2个月、第3个月内有效，另送1张菜票
        Date d = new Date();
        // 30 天内有效
        Date bd = d;
        Date ed = DateUtil.addDate(d, 30);
        fdao.setFieldValue("my_date", DateUtil.format(d, "yyyy-MM-dd"));
        fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        fdao.create();

        // 第2个月内有效
        bd = ed;
        ed = DateUtil.addDate(d, 30);
        fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        fdao.create();

        // 第3个月内有效
        bd = ed;
        ed = DateUtil.addDate(d, 30);
        fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        fdao.create();

        // 送菜票，1年内有效
        fdao.setFieldValue("kind", "2"); // 米票
        bd = d;
        ed = DateUtil.addDate(bd, 365);
        fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        fdao.create();

        // 发送email
        String email = qq + "@qq.com";
        MailSenderInfo mailInfo = new MailSenderInfo();
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();

        mailInfo.setMailServerHost(mailserver);
        mailInfo.setMailServerPort(String.valueOf(smtp_port));
        // mailInfo.setValidate(true);
        mailInfo.setUserName(name);
        mailInfo.setPassword(pwd_raw);// 邮箱密码
        mailInfo.setFromAddress(Global.getEmail());
        mailInfo.setToAddress(email);

        String docTitle = "";
        int docId = ParamUtil.getInt(request, "docId", -1);
        if (docId != -1) {
            Document doc = new Document();
            doc = doc.getDocument(docId);
            docTitle = doc.getTitle();
        }

        com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();
        String subject = cfg.getProperty("docShareTimelineTitle");
        String content = cfg.getProperty("docShareTimelineContent");
        subject = StrUtil.format(subject, new Object[]{docTitle});

/* 					com.redmoon.edm.Config edmCfg = com.redmoon.edm.Config.getInstance();
		String subject = edmCfg.getProperty("root.redbagReceiveSubject");
		String content = edmCfg.getProperty("root.redbagReceiveContent");	 */

        mailInfo.setSubject(subject);
        mailInfo.setContent(content);

        // 注意这里用的是有SSL验证的Sender
        MailSender sender;
        if (Global.isSmtpSSL()) {
            sender = new SSLMailSender();
        } else {
            sender = new SimpleMailSender();
        }
        try {// 发送两次，一次以html格式（此时附件会被发送），一次文本
            // sender.sendTextMail(mailInfo);
            sender.sendHtmlMail(mailInfo);
            // System.out.println("邮件已发送");
        } catch (AddressException e) {
            // System.err.println("发送失败");
            e.printStackTrace();
        } catch (MessagingException e) {
            // System.err.println("发送失败");
            e.printStackTrace();
        }

        // 播报，大礼包送达
        UserDb user = new UserDb();
        user = user.getUserDb(qq);
        String groupId = user.getParty();
        String docShareTimelineNote = cfg.getProperty("docShareTimelineNote");
        docShareTimelineNote = "\n" + StrUtil.format(docShareTimelineNote, new Object[]{qq, docTitle});
        RobotUtil.sendClusterMsg(request, groupId, docShareTimelineNote);

        try {
            json.put("ret", "1");
            json.put("msg", subject);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 分享文章给朋友，送小礼包
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/shareApp", method = RequestMethod.POST, produces = {"text/html;charset=utf-8", "application/json;"})
    public String shareApp() {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        String qq = pvg.getUser(request);

        String formCode = "robot_red_bag_res";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(fd);
        fdao.setFieldValue("batch_no", "0");
        fdao.setFieldValue("kind", "1"); // 米票
        fdao.setFieldValue("item", "0"); // 奖项
        fdao.setFieldValue("user_name", qq);
        fdao.setFieldValue("tip", "云网OA标准版");
        // 送1张米票，各30天、60天、90天内有效
        Date d = new Date();
        // 30 天内有效
        Date bd = d;
        Date ed = DateUtil.addDate(d, 30);
        fdao.setFieldValue("my_date", DateUtil.format(d, "yyyy-MM-dd"));
        fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        fdao.create();

        // 送1张菜票，3个月内有效
        fdao.setFieldValue("kind", "2"); // 米票
        bd = d;
        ed = DateUtil.addDate(bd, 90);
        fdao.setFieldValue("begin_date", DateUtil.format(bd, "yyyy-MM-dd"));
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        fdao.create();

        // 发送email
        String email = qq + "@qq.com";
        MailSenderInfo mailInfo = new MailSenderInfo();
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();

        mailInfo.setMailServerHost(mailserver);
        mailInfo.setMailServerPort(String.valueOf(smtp_port));
        // mailInfo.setValidate(true);
        mailInfo.setUserName(name);
        mailInfo.setPassword(pwd_raw);// 邮箱密码
        mailInfo.setFromAddress(Global.getEmail());
        mailInfo.setToAddress(email);

        String docTitle = "";
        int docId = ParamUtil.getInt(request, "docId", -1);
        if (docId != -1) {
            Document doc = new Document();
            doc = doc.getDocument(docId);
            docTitle = doc.getTitle();
        }

        com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();
        String subject = cfg.getProperty("docShareTitle");
        String content = cfg.getProperty("docShareContent");

        subject = StrUtil.format(subject, new Object[]{docTitle});

/* 					com.redmoon.edm.Config edmCfg = com.redmoon.edm.Config.getInstance();
		String subject = edmCfg.getProperty("root.redbagReceiveSubject");
		String content = edmCfg.getProperty("root.redbagReceiveContent");	 */

        mailInfo.setSubject(subject);
        mailInfo.setContent(content);

        // 注意这里用的是有SSL验证的Sender
        MailSender sender;
        if (Global.isSmtpSSL()) {
            sender = new SSLMailSender();
        } else {
            sender = new SimpleMailSender();
        }
        try {// 发送两次，一次以html格式（此时附件会被发送），一次文本
            // sender.sendTextMail(mailInfo);
            sender.sendHtmlMail(mailInfo);
            // System.out.println("邮件已发送");
        } catch (AddressException e) {
            // System.err.println("发送失败");
            e.printStackTrace();
        } catch (MessagingException e) {
            // System.err.println("发送失败");
            e.printStackTrace();
        }

        // 播报，大礼包送达
        UserDb user = new UserDb();
        user = user.getUserDb(qq);
        String groupId = user.getParty();
        String docShareNote = cfg.getProperty("docShareNote");

        docShareNote = "\n" + StrUtil.format(docShareNote, new Object[]{qq, docTitle});
        RobotUtil.sendClusterMsg(request, groupId, docShareNote);

        try {
            json.put("ret", "1");
            json.put("msg", subject);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 分享米票、菜票给朋友，延期30天
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/shareAppMessageRedbag", method = RequestMethod.GET, produces = {"text/html;", "application/json;charset=utf-8"})
    public String shareAppMessageRedbag(long id) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();
        int redBagShareDelayDays = cfg.getIntProperty("redBagShareDelayDays"); // 30天

        String formCode = "robot_red_bag_res";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(id, fd);
        String qq = fdao.getFieldValue("user_name");

        // 分享后点击的次数
        int shareHits = StrUtil.toInt(fdao.getFieldValue("share_hits"), 0);

        String beginDate = fdao.getFieldValue("begin_date");
        String endDate = fdao.getFieldValue("end_date");
        Date ed = DateUtil.parse(endDate, "yyyy-MM-dd");
        ed = DateUtil.addDate(ed, redBagShareDelayDays);
        // 延期30天
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        // 点击次数加1
        shareHits++;
        fdao.setFieldValue("share_hits", String.valueOf(shareHits));
        try {
            fdao.save();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }

        // 发送email
        String email = qq + "@qq.com";
        MailSenderInfo mailInfo = new MailSenderInfo();
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();

        mailInfo.setMailServerHost(mailserver);
        mailInfo.setMailServerPort(String.valueOf(smtp_port));
        // mailInfo.setValidate(true);
        mailInfo.setUserName(name);
        mailInfo.setPassword(pwd_raw);// 邮箱密码
        mailInfo.setFromAddress(Global.getEmail());
        mailInfo.setToAddress(email);

        String ret, msg;
        if (shareHits == 1) { // 初始值为0
            String subject = cfg.getProperty("redBagShareTitle");
            String content = cfg.getProperty("redBagShareContent");
            content = StrUtil.format(content, new Object[]{String.valueOf(id), beginDate, endDate, DateUtil.format(ed, "yyyy-MM-dd")});

            String tickName = RobotUtil.getTickName(fdao.getFieldValue("kind"));
            subject = StrUtil.format(subject, new Object[]{tickName, String.valueOf(redBagShareDelayDays)});

            mailInfo.setSubject(subject);
            mailInfo.setContent(content);

            // 注意这里用的是有SSL验证的Sender
            MailSender sender;
            if (Global.isSmtpSSL()) {
                sender = new SSLMailSender();
            } else {
                sender = new SimpleMailSender();
            }
            try {// 发送两次，一次以html格式（此时附件会被发送），一次文本
                // sender.sendTextMail(mailInfo);
                sender.sendHtmlMail(mailInfo);
                // System.out.println("邮件已发送");
            } catch (AddressException e) {
                // System.err.println("发送失败");
                e.printStackTrace();
            } catch (MessagingException e) {
                // System.err.println("发送失败");
                e.printStackTrace();
            }

            // 播报分享
	/*		UserDb user = new UserDb();
			user = user.getUserDb(qq);
			String groupId = user.getParty();*/
            String redBagShareNote = cfg.getProperty("redBagShareNote");

            Map groups = cfg.getGroups();
            Iterator ir = groups.keySet().iterator();
            while (ir.hasNext()) {
                String gid = (String) ir.next();
                Group group = (Group) groups.get(gid);
                redBagShareNote = "\n" + StrUtil.format(redBagShareNote, new Object[]{qq, tickName, String.valueOf(redBagShareDelayDays)});
                RobotUtil.sendClusterMsg(request, group.getId(), redBagShareNote);
            }
            ret = "1";
            msg = "分享成功";
        } else {
            ret = "0";
            msg = "已分享";
        }

        try {
            json.put("ret", ret);
            json.put("msg", msg);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 分享红包至朋友圈，延期120天
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/shareTimelineRedbag", method = RequestMethod.GET, produces = {"text/html;", "application/json;charset=utf-8"})
    public String shareTimelineRedbag(long id) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();
        int redBagShareTimelineDelayDays = cfg.getIntProperty("redBagShareTimelineDelayDays"); // 30天

        String formCode = "robot_red_bag_res";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(id, fd);
        String qq = fdao.getFieldValue("user_name");

        // 分享后点击的次数
        int shareHits = StrUtil.toInt(fdao.getFieldValue("share_hits"), 0);

        String beginDate = fdao.getFieldValue("begin_date");
        String endDate = fdao.getFieldValue("end_date");
        Date ed = DateUtil.parse(endDate, "yyyy-MM-dd");
        ed = DateUtil.addDate(ed, redBagShareTimelineDelayDays);
        // 延期60天
        fdao.setFieldValue("end_date", DateUtil.format(ed, "yyyy-MM-dd"));
        // 点击次数加1
        shareHits++;
        fdao.setFieldValue("share_hits", String.valueOf(shareHits));
        try {
            fdao.save();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }

        // 发送email
        String email = qq + "@qq.com";
        MailSenderInfo mailInfo = new MailSenderInfo();
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();

        mailInfo.setMailServerHost(mailserver);
        mailInfo.setMailServerPort(String.valueOf(smtp_port));
        // mailInfo.setValidate(true);
        mailInfo.setUserName(name);
        mailInfo.setPassword(pwd_raw);// 邮箱密码
        mailInfo.setFromAddress(Global.getEmail());
        mailInfo.setToAddress(email);

        String ret, msg;
        if (shareHits == 1) {
            String subject = cfg.getProperty("redBagShareTimelineTitle");
            String content = cfg.getProperty("redBagShareTimelineContent");
            content = StrUtil.format(content, new Object[]{String.valueOf(id), beginDate, endDate, DateUtil.format(ed, "yyyy-MM-dd")});

            String tickName = RobotUtil.getTickName(fdao.getFieldValue("kind"));
            subject = StrUtil.format(subject, new Object[]{tickName, String.valueOf(redBagShareTimelineDelayDays)});

            mailInfo.setSubject(subject);
            mailInfo.setContent(content);

            // 注意这里用的是有SSL验证的Sender
            MailSender sender;
            if (Global.isSmtpSSL()) {
                sender = new SSLMailSender();
            } else {
                sender = new SimpleMailSender();
            }
            try {// 发送两次，一次以html格式（此时附件会被发送），一次文本
                // sender.sendTextMail(mailInfo);
                sender.sendHtmlMail(mailInfo);
                // System.out.println("邮件已发送");
            } catch (AddressException e) {
                // System.err.println("发送失败");
                e.printStackTrace();
            } catch (MessagingException e) {
                // System.err.println("发送失败");
                e.printStackTrace();
            }

            // 播报分享
/*		UserDb user = new UserDb();
		user = user.getUserDb(qq);
		String groupId = user.getParty();*/
            String redBagShareNote = cfg.getProperty("redBagShareTimelineNote");

            Map groups = cfg.getGroups();
            Iterator ir = groups.keySet().iterator();
            while (ir.hasNext()) {
                String gid = (String) ir.next();
                Group group = (Group) groups.get(gid);

                redBagShareNote = "\n" + StrUtil.format(redBagShareNote, new Object[]{qq, tickName, String.valueOf(redBagShareTimelineDelayDays)});
                RobotUtil.sendClusterMsg(request, group.getId(), redBagShareNote);
            }
            ret = "1";
            msg = "分享成功";
        } else {
            ret = "0";
            msg = "已分享";
        }

        try {
            json.put("ret", ret);
            json.put("msg", msg);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }
}
