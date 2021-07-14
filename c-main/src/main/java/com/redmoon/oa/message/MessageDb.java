package com.redmoon.oa.message;

/**
 * <p>Title: 内部短消息</p>
 * <p>Description: 内部短消息</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author
 * @version 1.0
 */

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.GtPushUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.dingding.service.message.MsgService;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.xinge.SendNotice;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.*;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.DesktopUnit;
import com.redmoon.oa.ui.IDesktopUnit;
import com.redmoon.oa.workplan.WorkPlanDb;
import com.redmoon.oa.xmpp.MessageUtil;
import com.redmoon.weixin.mgr.WXMessageMgr;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class MessageDb extends ObjectDb implements IMessage, IDesktopUnit {
    public static final int MESSAGE_SYSTEM_NOTICE_TYPE = 0;
    public static final int MESSAGE_SYSTEM_FLOW_TYPE = 1;

    int id;
    private FileUpload fileUpload;

    public String title, content, receiver, sender, ip, rq, receiversAll;
    public int type = 0;
    public int box = INBOX;
    public int isSent = 1;
    /**
     * 回执状态
     */
    public int receiptState = 0;
    /**
     * 消息等级
     */
    public int msgLevel = 0;
    /**
     * 发送时间
     */
    public String sendTime;
    /**
     * 是否已读
     */
    public boolean readed = false;
    /*
     * 是否保存至发件箱
     */
    public boolean isToOutBox = false;

    /**
     * 链接动作
     */
    private String action;

    private boolean dustbin = false;

    private boolean senderDustbin = false;

    public boolean isSenderDustbin() {
        return senderDustbin;
    }

    public void setSenderDustbin(boolean senderDustbin) {
        this.senderDustbin = senderDustbin;
    }


    private String actionType;

    private String actionSubType;

    public String receiverscs;

    public String receiversms;

    public String receiversjs;

    public String getReceiversjs() {
        return receiversjs;
    }

    public void setReceiversjs(String receiversjs) {
        this.receiversjs = receiversjs;
    }

    public String getReceiverscs() {
        return receiverscs;
    }

    public void setReceiverscs(String receiverscs) {
        this.receiverscs = receiverscs;
    }

    public String getReceiversms() {
        return receiversms;
    }

    public void setReceiversms(String receiversms) {
        this.receiversms = receiversms;
    }

    /**
     * 默认系统用户
     */
    public static final String SENDER_SYSTEM = "系统";
    /**
     * 系统消息
     */
    public static final int TYPE_SYSTEM = 10;
    /**
     * 收件箱
     */
    public static final int INBOX = 0;
    /**
     * 草稿箱
     */
    public static final int DRAFT = 1;
    /**
     * 发件箱
     */
    public static final int OUTBOX = 2;
    /**
     * 回执状态:不需要回执
     */
    public static final int DO_NOT_NEED_RECEIPT = 0;
    /**
     * 回执状态:需要回执
     */
    public static final int NEED_RECEIPT = 1;
    /**
     * 回执状态:已经回执
     */
    public static final int RECEIPT_RETURNED = 2;
    /**
     * 消息等级:普通
     */
    public static final int MSG_LEVEL_NORMAL = 0;

    /**
     * 消息链接动作
     */
    public static final String ACTION_FLOW_DISPOSE = "flow_dispose";
    public static final String ACTION_FLOW_SHOW = "flow_show";
    public static final String ACTION_WORKPLAN = "workplan";

    public static final String ACTION_NOTICE = "notice";

    /**
     * 收文
     */
    public static final String ACTION_PAPER_DISTRIBUTE = "paper_distribute";

    /**
     * 日程安排
     */
    public static final String ACTION_PLAN = "plan";

    public static final String ACTION_MODULE_SHOW = "module_show";
    public static final String ACTION_MODULE_EDIT = "module_edit";

    public static final String ACTION_WORKLOG = "worklog";

    public static final String ACTION_FILEARK_NEW = "fileark_new";

    public int getSendMsgId() {
        return sendMsgId;
    }

    public void setSendMsgId(int sendMsgId) {
        this.sendMsgId = sendMsgId;
    }

    /**
     * 收件箱中的消息中，指向发送者的发件箱的消息ID
     */
    private int sendMsgId = 0;

    public MessageDb() {
        init();
    }

    public MessageDb(int id) {
        this.id = id;
        init();
        load();
    }

    /**
     * 取得新消息的数量
     */
    public int getCountMsg(String receiver, String sql) {
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        int result = 0;
        try {
            ri = jt.executeQuery(sql, new Object[]{receiver});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                result = rr.getInt(1);
                return result;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getNewMsgCount(String receiver) {
        return getNewMsgsOfUser(receiver).size();
    }

    public int getNewSysMsgCount(String receiver) {
        MessageCache mc = new MessageCache(this);
        String sql =
                "select count(*) from oa_message where type=10 and isreaded=0 and box=" + MessageDb.INBOX + " and is_sent=1 and receiver=? and is_dustbin=0";
        return getCountMsg(receiver, sql);
    }

    public int getNewInnerMsgCount(String receiver) {
        MessageCache mc = new MessageCache(this);
        String sql =
                "select count(*) from oa_message where type<>10 and isreaded=0 and box=" + MessageDb.INBOX + " and is_sent=1 and receiver=? and is_dustbin=0 ";
        return getCountMsg(receiver, sql);
    }

    /**
     * 取得桌面项的列表页地址
     */
    @Override
    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    /**
     * 显示桌面项的列表
     */
    @Override
    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.
                Privilege();

        // System.out.println("PlanDb.java display sql=" + sql);
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageShow();
        String str = "";
        //int count = uds.getCount();

        String sql = "select id from oa_message where receiver=" +
                StrUtil.sqlstr(privilege.getUser(request)) +
                " and box=" + MessageDb.INBOX + " and is_sent=1 and type=0 and is_dustbin=0 order by isreaded asc,rq desc";

        String rq = "";
        boolean isreaded = true;
        ListResult wflr = null;
        try {
            wflr = listResult(sql, 1, uds.getCount());
        } catch (ErrMsgException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Iterator msgir = wflr.getResult().iterator();
        // Iterator msgir = list(sql, 1, count - 1).iterator();
        if (msgir.hasNext()) {
            str += "<table class='article_table'>";
            while (msgir.hasNext()) {
                MessageDb md = (MessageDb) msgir.next();
                id = md.getId();

                String t = StrUtil.getLeft(md.getTitle(), uds.getWordCount());
                /*
	            if(t.length()>5){
	            	t = t.substring(0, 5)+"...";
	            }
	            */
                receiver = md.getReceiver();
                rq = DateUtil.format(DateUtil.parse(md.getRq(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd");
                type = md.getType();
                isreaded = md.isReaded();
                str += "<tr><td class='article_content'><a title='" + md.getTitle() + "' href='" + url + "?id=" + md.getId() + "'>";
                if (isreaded) {
                    str += t;
                } else {
                    str += "<b>" + t + "</b>";
                }
                // str += "(" + sender + ")";
                str += "</a></td><td class='article_time'>[" + rq + "]";
                str += "</td></tr>";
            }
            str += "</table>";
        } else {
            str = "<div class='no_content'><img title='暂无内部邮件' src='images/desktop/no_content.jpg'></div>";
        }

        return str;
    }

    /**
     * 清除用户超出容量部分的message
     *
     * @param userName String
     */
    @Override
    public void clearMsgOfUser(String userName) {
        String sql = "select id from oa_message where receiver=" +
                StrUtil.sqlstr(userName) +
                " and box=" + MessageDb.INBOX + " and is_sent=1 order by isreaded asc,rq desc";
        int total = getObjectCount(sql);
        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(userName);
        if (total > usd.getMessageUserMaxCount()) {
            int count = total - usd.getMessageUserMaxCount();
            sql = "select id from oa_message where receiver=" +
                    StrUtil.sqlstr(userName) +
                    " and box=0 and is_sent=1 order by rq asc";
            Iterator ir = list(sql, 0, count - 1).iterator();
            while (ir.hasNext()) {
                MessageDb md = (MessageDb) ir.next();
                md.del();
            }
        }
    }

    /**
     * 发送消息
     */
    @Override
    public boolean AddMsg(ServletContext application, HttpServletRequest request,
                          String sender) throws
            ErrMsgException {
        MessageForm mf = new MessageForm(application, request, this);
        mf.checkCreate();
        this.fileUpload = mf.getFileUpload();
        this.sender = sender;
        String[] ary = receiver.split(",");
        // logger.info("create:toUser=" + receiver);

        int len = ary.length;
        UserMgr um = new UserMgr();
        for (int i = 0; i < len; i++) {
            // 检查用户是否存在
            UserDb user = um.getUserDb(ary[i]);
            if (!user.isLoaded()) {
                throw new ErrMsgException("用户" + ary[i] + "不存在！");
            }
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isLarkUsed = cfg.get("isLarkUsed").equals("true");

        if (this.isSent == 1) {
            int sendMsgId = 0;
            if (this.isToOutBox) {
                this.box = MessageDb.OUTBOX;
                // 存入发件箱，此时toUser可能有多个，所以记录在receives_js字段中
                create("", mf.fileUpload);
                sendMsgId = id; // 此次发送的邮件ID
            }
            this.box = MessageDb.INBOX;
            for (int i = 0; i < len; i++) {
                // System.out.println(getClass() + " ary[" + i + "]=" + ary[i] + " box=" + box);
/*                if (id == 0) { // 之前如果未create
                    create(ary[i], mf.fileUpload);
                } else {
                    create(ary[i], mf.fileUpload, sendMsgId);
                }*/
                create(ary[i], mf.fileUpload, sendMsgId);

                // System.out.println(getClass() + " isLarkUsed=" + isLarkUsed + " receiver=" + receiver);
                if (isLarkUsed) {
                    MessageUtil mu = new MessageUtil();
                    mu.send(sender, ary[i], title);
                }
            }
        }
        return true;
    }


    //通过手机端发送内部消息
    public boolean AddMsgByMobile(String sender, String receiver, String title, String content, File[] files, String[] uploadFileNames) throws ErrMsgException {

        this.sender = sender;
        this.title = title;
        this.content = content;
        this.receiversAll = receiver;
        this.receiversjs = receiver;
        String[] ary = receiver.split(",");
        // logger.info("create:toUser=" + receiver);

        int len = ary.length;
        UserMgr um = new UserMgr();
        for (int i = 0; i < len; i++) {
            // 检查用户是否存在
            UserDb user = um.getUserDb(ary[i]);
            if (!user.isLoaded()) {
                throw new ErrMsgException("用户" + ary[i] + "不存在！");
            }
        }
        if (this.isSent == 1) {
            for (int i = 0; i < len; i++) {
                // System.out.println(getClass() + " ary[" + i + "]=" + ary[i] +
                // " box=" + box);
                //create(ary[i], mf.fileUpload);
                sendMsgBysender(ary[i], files, uploadFileNames);
            }
        }
        return true;
    }

    //通过手机端发送内部消息存储进发件箱
    public boolean AddMsgByMobile(String sender, String title, String content, File[] files, String[] uploadFileNames) throws ErrMsgException {
        this.sender = sender;
        this.title = title;
        this.content = content;
        if (this.isSent == 1) {
            //手机上传时未设置是否保存发件箱，因此在此默认保存发件箱，如需改则加参数
            this.box = MessageDb.OUTBOX;
            sendMsgBysender("", files, uploadFileNames);
        }
        return true;
    }

    /**
     * 用于手机端发送消息
     */
    public boolean sendMsgBysender(String receiver, File[] files, String[] uploadFileNames)
            throws ErrMsgException {

        //指定发件人，收件人，标题，内容
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean re = false;

        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, sender);
            ps.setString(5, receiver);
            ps.setInt(6, type);
            ps.setString(7, "");
            java.util.Date curDate = new java.util.Date();
            ps.setTimestamp(8, new Timestamp(curDate.getTime()));
            ps.setInt(9, box);
            ps.setString(10, receiversAll);
            ps.setInt(11, 1);
            ps.setTimestamp(12, new Timestamp(curDate.getTime()));
            ps.setInt(13, 0);
            ps.setInt(14, 0);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendMsgId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();

            if (files != null) {
                FileOutputStream out;
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);

                String vpath = cfg.get("file_message") + "/" + year
                        + "/" + month + "/";
                String path = Global.getRealPath() + vpath;
                File file_path = new File(path);
                if (!file_path.exists()) { // 创建文件夹
                    file_path.mkdirs();
                }
                String real_path = file_path.getPath();
                Attachment att = new Attachment();

                for (int i = 0; i < files.length; i++) {
                    out = new FileOutputStream(real_path + "\\"
                            + uploadFileNames[i]);

                    FileInputStream in = new FileInputStream(files[i]);
                    int size = in.available(); // 文件大小

                    byte buffer[] = new byte[1024 * 10];
                    int length = 0;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    out.close();

                    att.setMsgId(id);
                    att.setName(uploadFileNames[i]);
                    att.setDiskName(uploadFileNames[i]);
                    att.setVisualPath(vpath);
                    att.setSize(size);
                    att.create();
                }
            }

            if (re) {
                // 发送短信
                IMsgUtil imu = SMSFactory.getMsgUtil();
                if (imu != null) {
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(receiver);
                    imu.send(ud, content, sender);
                }
            }
        } catch (Exception e) {
            logger.error("sendSysMsg: " + e.getMessage());
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 保存草稿
     *
     * @param application
     * @param request
     * @param sender
     * @return
     * @throws ErrMsgException
     */
    public boolean AddDraftMsg(ServletContext application,
                               HttpServletRequest request, String sender) throws ErrMsgException {
        MessageForm mf = new MessageForm(application, request, this);
        mf.checkCreate();
        this.fileUpload = mf.getFileUpload();
        this.sender = sender;
        this.box = MessageDb.DRAFT;

        create("", mf.fileUpload);
        return true;
    }

    /**
     * 取得发送者的姓名
     *
     * @return
     */
    public String getSenderRealName() {
        String realName = sender;
        if (!sender.equals(MessageDb.SENDER_SYSTEM)) {
            com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
            ud = ud.getUserDb(sender);
            if (ud != null && ud.isLoaded()) {
                realName = ud.getRealName();
            }
        }
        return realName;
    }

    /**
     * 删除发件箱信息 至垃圾箱
     *
     * @param ids
     * @return
     */
    public boolean delMsgBySenderDustbin(String[] ids) {

        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String sql = "select id from oa_message where id in " + str;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                MessageCache mc = new MessageCache(this);
                while (rs.next()) {
                    MessageDb md = (MessageDb) getMessageDb(rs.getInt(1));
                    md.setReaded(true);
                    md.setSenderDustbin(true);
                    md.save();
                    mc.refreshNewCountOfReceiver(md.getReceiver());
                }

                mc.refreshList();
            }
        } catch (Exception e) {
            logger.error("delMsg:" + e.getMessage());
            try {
                throw new ErrMsgException("删除消息失败！");
            } catch (ErrMsgException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
//    	boolean flag = false;
//    	JdbcTemplate jt = new JdbcTemplate();
//    	  int len = ids.length;
//          String str = "";
//          for (int i = 0; i < len; i++)
//              if (str.equals(""))
//                  str += ids[i];
//              else
//                  str += "," + ids[i];
//          str = "(" + str + ")";
//          String sql = "UPDATE oa_message SET is_sender_dustbin = 1 AND isreaded=1 WHERE id IN "+str;
//          try {
//			int result = jt.executeUpdate(sql);
//			if(result == 1){
//				flag = true;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//         return flag;
    }


    /**
     * 删除消息
     */
    @Override
    public boolean delMsg(String[] ids) throws ErrMsgException {
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String sql = "select id from oa_message where id in " + str;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    getMessageDb(rs.getInt(1)).del();
                }
            }
        } catch (Exception e) {
            logger.error("delMsg:" + e.getMessage());
            throw new ErrMsgException("删除消息失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 恢复发件箱 或者收件箱消息
     *
     * @param ids
     * @param isDel
     * @return
     * @throws ErrMsgException
     */
    public boolean doDustbin(String[] ids, boolean isDel) throws ErrMsgException {
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String sql = "select id from oa_message where id in " + str;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                MessageCache mc = new MessageCache(this);
                while (rs.next()) {
                    MessageDb md = (MessageDb) getMessageDb(rs.getInt(1));
                    md.setDustbin(isDel);
                    md.setSenderDustbin(isDel);
                    md.save();

                    mc.refreshNewCountOfReceiver(md.getReceiver());
                }

                mc.refreshList();
            }
        } catch (Exception e) {
            logger.error("delMsg:" + e.getMessage());
            throw new ErrMsgException("删除消息失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 删除会话信息
     *
     * @param ids
     * @param isDel
     * @return
     * @throws ErrMsgException
     */
    public boolean doChat(String[] ids, boolean isDel) throws ErrMsgException {
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if (str.equals("")) {
                str += ids[i];
            } else {
                str += "," + ids[i];
            }

            MessageDb messageDb = (MessageDb) getMessageDb(Integer.valueOf(str));
            String sender = messageDb.getSender();
            String receiver = messageDb.getReceiver();
            String sql = "select id from oa_message where ((sender =  '" + sender + "' and receiver = '" + receiver + "') or (sender = '" + receiver + "' and receiver = '" + sender + "')) and is_dustbin = 0 and is_sender_dustbin = 0";
            Conn conn = null;
            try {
                conn = new Conn(connname);
                ResultSet rs = conn.executeQuery(sql);
                if (rs != null) {
                    MessageCache mc = new MessageCache(this);
                    while (rs.next()) {
                        MessageDb md = (MessageDb) getMessageDb(rs.getInt(1));
                        md.setDustbin(isDel);
                        md.setSenderDustbin(isDel);
                        md.save();
                        mc.refreshNewCountOfReceiver(md.getReceiver());
                    }
                    mc.refreshList();
                }
            } catch (Exception e) {
                logger.error("delMsg:" + e.getMessage());
                throw new ErrMsgException("删除消息失败！");
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }

        }
        return true;
    }

    @Override
    public boolean isReaded() {
        return readed;
    }

    /**
     * 取得消息
     */
    @Override
    public IMessage getMessageDb(int id) {
        return (IMessage) getObjectDb(new Integer(id));
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getRq() {
        return rq;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getReceiver() {
        return receiver;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Vector getAttachments() {
        return attachments;
    }

    @Override
    public int getBox() {
        return box;
    }

    public String getReceiversAll() {
        return receiversAll;
    }

    @Override
    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public int getIsSent() {
        return isSent;
    }

    public String getSendTime() {
        return sendTime;
    }

    public int getMsgLevel() {
        return msgLevel;
    }

    public int getReCeiptState() {
        return receiptState;
    }

    public void setMsgLevel(int msgLevel) {
        this.msgLevel = msgLevel;
    }

    public void setReceiptState(int receiptState) {
        this.receiptState = receiptState;
    }

    public void setIsSent(int isSent) {
        this.isSent = isSent;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public void setReceiversAll(String receiversAll) {
        this.receiversAll = receiversAll;
    }

    /**
     * 创建消息至发件箱或草稿箱
     * <p>
     * param toUser String 始终为空
     */
    @Override
    public boolean create(String toUser, FileUpload fu) throws ErrMsgException {
        // 检查接收者邮箱容量大小是否足够
        Vector v = fu.getFiles();
        FileInfo fi = null;
        long allSize = 0;
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            fi = (FileInfo) ir.next();
            allSize += fi.getSize();
        }
        // 网络硬盘文件
        String[] netdiskFiles = fu.getFieldValues("netdiskFiles");
        if (netdiskFiles != null) {
            com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();
            for (int i = 0; i < netdiskFiles.length; i++) {
                att = att.getAttachment(StrUtil.toInt(netdiskFiles[i]));
                allSize += att.getSize();
            }
        }

        // 邮箱附件
        String[] mailFiles = fu.getFieldValues("mailFiles");
        if (mailFiles != null) {
            for (int i = 0; i < mailFiles.length; i++) {
                com.redmoon.oa.emailpop3.Attachment att = new com.redmoon.oa.emailpop3.Attachment(StrUtil.toInt(mailFiles[i]));
                allSize += att.getFileSize();
            }
        }

        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(sender);
        if (usd.getMsgSpaceUsed() + allSize > usd.getMsgSpaceAllowed())
            throw new ErrMsgException("您的空间不足，无法存入！");

        String action = StrUtil.getNullStr(fu.getFieldValue("action"));

        Conn conn = null;
        boolean re = false;
        // logger.info("create:toUser=" + toUser);
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, sender);
            ps.setString(5, toUser);
            ps.setInt(6, type);
            ps.setString(7, ip);
            java.util.Date curDate = new java.util.Date();
            ps.setTimestamp(8, new Timestamp(curDate.getTime()));
            ps.setInt(9, box);
            ps.setString(10, receiversAll);
            ps.setInt(11, isSent);
            java.util.Date sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
            ps.setTimestamp(12, new Timestamp(sendDate.getTime()));
            ps.setInt(13, receiptState);
            ps.setInt(14, msgLevel);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendMsgId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            // mc.refreshNewCountOfReceiver(receiver); // 20071124 revise
            mc.refreshNewCountOfReceiver(toUser);
            mc.refreshCreate();

            if (re) {
                // 本方法仅用于存入发件箱或草稿箱，所以无需发送手机推送或微信端消息
                //add by lichao 手机端消息推送
/*                SendNotice se = new SendNotice();
                se.PushNoticeSingleByToken(toUser, title, content, id);*/

                // WXMessageMgr wxMessageMgr = new WXMessageMgr();
                // wxMessageMgr.sendTextMessage(receiver, title + "\r\n" + content, action, WXMessageMgr.MESSAGE_TYPE_ENMU.MSG);

                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String vpath = cfg.get("file_message") + "/" + year + "/" + month + "/";
                String filepath = fu.getRealPath() + vpath;

                // 处理网络硬盘文件
                // String[] netdiskFiles = fu.getFieldValues("netdiskFiles");
                if (netdiskFiles != null) {
                    com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();
                    for (int i = 0; i < netdiskFiles.length; i++) {
                        att = att.getAttachment(StrUtil.toInt(netdiskFiles[i]));

                        String file_netdisk = cfg.get("file_netdisk");
                        String fullPath = Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName();

                        String newName = RandomSecquenceCreator.getId() + "." + StrUtil.getFileExt(att.getDiskName());
                        String newFullPath = filepath + "/" + newName;

                        File f = new File(filepath);
                        if (!f.isDirectory())
                            f.mkdirs();

                        // System.out.println(getClass() + " " + fullPath);
                        // System.out.println(getClass() + " " + newFullPath);

                        FileUtil.CopyFile(fullPath, newFullPath);
                        Attachment att2 = new Attachment();
                        //att.setFullPath(filepath + fi.getDiskName());
                        att2.setMsgId(id);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(vpath);
                        att2.setSize(att.getSize());
                        re = att2.create();
                    }
                }

                // 处理邮箱附件
                if (mailFiles != null) {
                    for (int i = 0; i < mailFiles.length; i++) {
                        com.redmoon.oa.emailpop3.Attachment att = new com.redmoon.oa.emailpop3.Attachment(StrUtil.toInt(mailFiles[i]));

                        String fullPath = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();

                        String newName = RandomSecquenceCreator.getId() + "." + StrUtil.getFileExt(att.getDiskName());
                        String newFullPath = filepath + "/" + newName;

                        File f = new File(filepath);
                        if (!f.isDirectory())
                            f.mkdirs();

                        // System.out.println(getClass() + " " + fullPath);
                        // System.out.println(getClass() + " " + newFullPath);

                        FileUtil.CopyFile(fullPath, newFullPath);
                        Attachment att2 = new Attachment();
                        att2.setMsgId(id);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(vpath);
                        att2.setSize(att.getFileSize());
                        re = att2.create();
                    }
                }

                // 处理附件
                if (fu.getRet() == FileUpload.RET_SUCCESS) {
                    fu.setSavePath(filepath);
                    // 使用随机名称写入磁盘
                    fu.writeFile(true);
                    v = fu.getFiles();
                    ir = v.iterator();
                    while (ir.hasNext()) {
                        fi = (FileInfo) ir.next();
                        Attachment att = new Attachment();
                        //att.setFullPath(filepath + fi.getDiskName());
                        att.setMsgId(id);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setVisualPath(vpath);
                        att.setSize(fi.getSize());
                        re = att.create();
                    }
                }
                // 更新用户的邮箱已用空间
                usd.setMsgSpaceUsed(usd.getMsgSpaceUsed() + allSize);
                usd.save();
            }
        } catch (Exception e) {
            logger.error("create: " + e.getMessage());
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 创建消息
     */
    public boolean create(String toUser, FileUpload fu, int sendId) throws ErrMsgException {
        // 检查接收者邮箱容量大小是否足够
        Vector v = fu.getFiles();
        FileInfo fi = null;
        long allSize = 0;
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            fi = (FileInfo) ir.next();
            allSize += fi.getSize();
        }
        // 网络硬盘文件
        String[] netdiskFiles = fu.getFieldValues("netdiskFiles");
        if (netdiskFiles != null) {
            com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();
            for (int i = 0; i < netdiskFiles.length; i++) {
                att = att.getAttachment(StrUtil.toInt(netdiskFiles[i]));
                allSize += att.getSize();
            }
        }

        // 邮箱附件
        String[] mailFiles = fu.getFieldValues("mailFiles");
        if (mailFiles != null) {
            for (int i = 0; i < mailFiles.length; i++) {
                com.redmoon.oa.emailpop3.Attachment att = new com.redmoon.oa.emailpop3.Attachment(StrUtil.toInt(mailFiles[i]));
                allSize += att.getFileSize();
            }
        }

        UserSetupDb usd = new UserSetupDb();
        if (!toUser.equals("")) {
            usd = usd.getUserSetupDb(toUser);
            if (usd.getMsgSpaceUsed() + allSize > usd.getMsgSpaceAllowed())
                throw new ErrMsgException("接收者的空间不足，无法发送！");
        } else {
            usd = usd.getUserSetupDb(sender);
            if (usd.getMsgSpaceUsed() + allSize > usd.getMsgSpaceAllowed())
                throw new ErrMsgException("您的空间不足，无法存入！");
        }

        String action = StrUtil.getNullStr(fu.getFieldValue("action"));
        if (action == null || action.equals("")) {
            action = "send_id=" + sendId;
        }

        Conn conn = null;
        boolean re = false;
        // logger.info("create:toUser=" + toUser);
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, sender);
            ps.setString(5, toUser);
            ps.setInt(6, type);
            ps.setString(7, ip);
            java.util.Date curDate = new java.util.Date();
            ps.setTimestamp(8, new Timestamp(curDate.getTime()));
            ps.setInt(9, box);
            ps.setString(10, receiversAll);
            ps.setInt(11, isSent);
            java.util.Date sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
            ps.setTimestamp(12, new Timestamp(sendDate.getTime()));
            ps.setInt(13, receiptState);
            ps.setInt(14, msgLevel);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            // mc.refreshNewCountOfReceiver(receiver); // 20071124 revise
            mc.refreshNewCountOfReceiver(toUser);
            mc.refreshCreate();

            if (re) {
                //add by lichao 手机端消息推送
                boolean isUseClient = Config.getInstance().getBooleanProperty("isUseClient");
                if (isUseClient) {
                    boolean xingeIsEnabled = Config.getInstance().getBoolean("xingeIsEnabled");
                    if (xingeIsEnabled) {
                        SendNotice se = new SendNotice();
                        se.PushNoticeSingleByToken(toUser, title, content, id);
                    }
                }
                WXMessageMgr wxMessageMgr = new WXMessageMgr();
                wxMessageMgr.sendTextMessage(receiver, title + "\r\n" + content, action, WXMessageMgr.MESSAGE_TYPE_ENMU.SYSMSG);

                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String vpath = cfg.get("file_message") + "/" + year + "/" + month + "/";
                String filepath = fu.getRealPath() + vpath;

                // 处理网络硬盘文件
                // String[] netdiskFiles = fu.getFieldValues("netdiskFiles");
                if (netdiskFiles != null) {
                    com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();
                    for (int i = 0; i < netdiskFiles.length; i++) {
                        att = att.getAttachment(StrUtil.toInt(netdiskFiles[i]));

                        String file_netdisk = cfg.get("file_netdisk");
                        String fullPath = Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName();

                        String newName = RandomSecquenceCreator.getId() + "." + StrUtil.getFileExt(att.getDiskName());
                        String newFullPath = filepath + "/" + newName;

                        File f = new File(filepath);
                        if (!f.isDirectory())
                            f.mkdirs();

                        // System.out.println(getClass() + " " + fullPath);
                        // System.out.println(getClass() + " " + newFullPath);

                        FileUtil.CopyFile(fullPath, newFullPath);
                        Attachment att2 = new Attachment();
                        //att.setFullPath(filepath + fi.getDiskName());
                        att2.setMsgId(id);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(vpath);
                        att2.setSize(att.getSize());
                        re = att2.create();
                    }
                }

                // 处理邮箱附件
                if (mailFiles != null) {
                    for (int i = 0; i < mailFiles.length; i++) {
                        com.redmoon.oa.emailpop3.Attachment att = new com.redmoon.oa.emailpop3.Attachment(StrUtil.toInt(mailFiles[i]));

                        String fullPath = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();

                        String newName = RandomSecquenceCreator.getId() + "." + StrUtil.getFileExt(att.getDiskName());
                        String newFullPath = filepath + "/" + newName;

                        File f = new File(filepath);
                        if (!f.isDirectory())
                            f.mkdirs();

                        // System.out.println(getClass() + " " + fullPath);
                        // System.out.println(getClass() + " " + newFullPath);

                        FileUtil.CopyFile(fullPath, newFullPath);
                        Attachment att2 = new Attachment();
                        att2.setMsgId(id);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(vpath);
                        att2.setSize(att.getFileSize());
                        re = att2.create();
                    }
                }

                // 处理附件
                if (fu.getRet() == FileUpload.RET_SUCCESS) {
                    fu.setSavePath(filepath);
                    // 使用随机名称写入磁盘
                    fu.writeFile(true);
                    v = fu.getFiles();
                    ir = v.iterator();
                    while (ir.hasNext()) {
                        fi = (FileInfo) ir.next();
                        Attachment att = new Attachment();
                        //att.setFullPath(filepath + fi.getDiskName());
                        att.setMsgId(id);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setVisualPath(vpath);
                        att.setSize(fi.getSize());
                        re = att.create();
                    }
                }
                // 更新用户的邮箱已用空间
                usd.setMsgSpaceUsed(usd.getMsgSpaceUsed() + allSize);
                usd.save();
            }
        } catch (Exception e) {
            logger.error("create: " + e.getMessage());
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 发送系统消息
     */
    @Override
    public boolean sendSysMsg(String receiver, String title, String content, String action) throws ErrMsgException {
        this.action = action;

        boolean isFoundActionType = false;
        String[] ary = StrUtil.split(action, "\\|");
        if (ary!=null) {
            int len = ary.length;
            Map map = null;
            if (len >= 1) {
                map = new LinkedHashMap();
                for (int i = 0; i < len; i++) {
                    String[] pair = ary[i].split("=");
                    if (pair.length == 2) {
                        map.put(pair[0], pair[1]);
                    }
                }

                String myaction = "";
                if (map != null) {
                    myaction = StrUtil.getNullStr((String) map.get("action"));
                    if (!"".equals(myaction)) {
                        isFoundActionType = true;
                        actionType = myaction;

                        List<Map.Entry> list = new ArrayList<Map.Entry>(map.entrySet());

                        Map.Entry entry = list.get(1);
                        this.action = (String)entry.getValue();
                    }
                }
            }
        }
        if (!isFoundActionType) {
            return sendSysMsg(receiver, title, content);
        }
        else {
            return sendSysMsg(receiver, title, content, actionType, "", action);
        }
    }

    @Override
    public boolean sendSysMsg(String receiver, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException {
        this.actionType = actionType;
        this.actionSubType = actionSubType;
        this.action = action;
        return sendSysMsg(receiver, title, content);
    }

    @Override
    public boolean sendSysMsg(String[] receivers, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException {
        this.actionType = actionType;
        this.actionSubType = actionSubType;
        this.action = action;
        boolean re = true;
        if (receivers!=null) {
            for (String receiver : receivers) {
                re = sendSysMsg(receiver, title, content);
            }
        }
        return re;
    }

    /**
     * 发送系统消息
     */
    @Override
    public boolean sendSysMsg(String receiver, String title, String content) throws ErrMsgException {
        boolean re = false;
        com.redmoon.oa.Config cfg = Config.getInstance();
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, SENDER_SYSTEM);
            ps.setString(5, receiver);
            ps.setInt(6, TYPE_SYSTEM);
            ps.setString(7, "");
            java.util.Date curDate = new java.util.Date();
            ps.setTimestamp(8, new Timestamp(curDate.getTime()));
            ps.setInt(9, 0);
            ps.setString(10, receiversAll);
            ps.setInt(11, 1);
            ps.setTimestamp(12, new Timestamp(curDate.getTime()));
            ps.setInt(13, 0);
            ps.setInt(14, 0);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendMsgId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();

            if (re) {
                boolean isLarkUsed = cfg.get("isLarkUsed").equals("true");
                // System.out.println(getClass() + " isLarkUsed=" + isLarkUsed + " receiver=" + receiver);
                if (isLarkUsed) {
                    MessageUtil mu = new MessageUtil();
                    mu.send(MessageUtil.USER_SYSTEM, receiver, title);
                }

                boolean isUseClient = cfg.getBooleanProperty("isUseClient");
                if (isUseClient) {
                    boolean xingeIsEnabled = cfg.getBoolean("xingeIsEnabled");
                    if (xingeIsEnabled) {
                        SendNotice se = new SendNotice();
                        se.PushNoticeSingleByToken(receiver, title, content, id);
                    }

                    boolean gtIsEnabled = cfg.getBooleanProperty("gtIsEnabled");
                    if (gtIsEnabled) {
                        GtPushUtil.getInstance().push(receiver, title, content, id);
                    }
                }

                com.redmoon.weixin.Config cfgWx = com.redmoon.weixin.Config.getInstance();
                if (cfgWx.getBooleanProperty("isUse")) {
                    WXMessageMgr wxMessageMgr = new WXMessageMgr();
                    wxMessageMgr.sendTextMessage(receiver, title, action, WXMessageMgr.MESSAGE_TYPE_ENMU.FLOW);
                }
                com.redmoon.dingding.Config cfgDd = com.redmoon.dingding.Config.getInstance();
                if (cfgDd.isUseDingDing()) {
                    MsgService msgService = new MsgService();
                    msgService.sendMsg(receiver, title, action, MsgService.MESSAGE_TYPE_ENMU.FLOW);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("sendSysMsg: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return re;
    }

    /**
     * 发送系统消息
     * lichao
     * 将noticeId写入oa_message表中action字段
     */
    public int sendSysMsgNotice(long noticeId, String[] users, String title, String content) throws ErrMsgException {
        if (users == null) {
            return 0;
        }

        int len = users.length;

        actionType = ACTION_NOTICE;
        action = String.valueOf(noticeId);
        com.redmoon.oa.Config cfg = Config.getInstance();

        JdbcTemplate jt = new JdbcTemplate(new com.cloudwebsoft.framework.db.Connection(cn.js.fan.web.Global.getDefaultDB()));
        try {
            for (int i = 0; i < len; i++) {
                String receiver = users[i];

                String sql = "insert into oa_message (id,title,content,sender,receiver,type,";
                sql += "ip,rq,box,receivers_all,is_sent,send_time,receipt_state,";
                sql += "msg_level,action,action_type,action_sub_type,receivers_cs,";
                sql += "receivers_ms,receivers_js) values (";

                id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
                sql += id + "," + StrUtil.sqlstr(title) + "," + StrUtil.sqlstr(content) + ",";
                sql += StrUtil.sqlstr(SENDER_SYSTEM) + "," + StrUtil.sqlstr(receiver) + ",";
                sql += TYPE_SYSTEM + "," + StrUtil.sqlstr("") + ",";
                java.util.Date curDate = new java.util.Date();
                sql += SQLFilter.getDateStr(DateUtil.format(curDate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + ",";
                sql += 0 + "," + StrUtil.sqlstr(receiversAll) + ",1,";
                sql += SQLFilter.getDateStr(DateUtil.format(curDate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + ",";
                sql += 0 + "," + 0 + "," + action + ",";
                sql += StrUtil.sqlstr(actionType) + "," + StrUtil.sqlstr(actionSubType) + ",";
                sql += StrUtil.sqlstr(receiverscs) + "," + StrUtil.sqlstr(receiversms) + ",";
                sql += StrUtil.sqlstr(receiversjs);
                sql += ")";

                jt.addBatch(sql);
            }
            jt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        boolean isUseClient = cfg.getBooleanProperty("isUseClient");
        WXMessageMgr wxMessageMgr = new WXMessageMgr();
        SendNotice se = new SendNotice();
        com.redmoon.weixin.Config cfgWx = com.redmoon.weixin.Config.getInstance();
        boolean isLarkUsed = cfg.get("isLarkUsed").equals("true");

        for (int i = 0; i < len; i++) {
            receiver = users[i];

            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();

            // System.out.println(getClass() + " isLarkUsed=" + isLarkUsed + " receiver=" + receiver);
            if (isLarkUsed) {
                MessageUtil mu = new MessageUtil();
                mu.send(MessageUtil.USER_SYSTEM, receiver, title);
            }

            if (isUseClient) {
                boolean xingeIsEnabled = cfg.getBoolean("xingeIsEnabled");
                if (xingeIsEnabled) {
                    se.PushNoticeSingleByToken(receiver, title, content, id);
                }
            }

            if (cfgWx.getBooleanProperty("isUse")) {
                wxMessageMgr.sendTextMessage(receiver, title + "\r\n" + content, action, WXMessageMgr.MESSAGE_TYPE_ENMU.NOTICE);
            }
            com.redmoon.dingding.Config cfgDd = com.redmoon.dingding.Config.getInstance();
            if (cfgDd.isUseDingDing()) {
                MsgService msgService = new MsgService();
                msgService.sendMsg(receiver, title + "\r\n" + content, action, MsgService.MESSAGE_TYPE_ENMU.NOTICE);
            }
        }

        return len;
    }

    /**
     * 发送用户消息
     */
    public boolean sendMsg(String sender, String receiver, String title, String content, String action) throws ErrMsgException {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean re = false;

        boolean isLarkUsed = cfg.get("isLarkUsed").equals("true");
        // System.out.println(getClass() + " isLarkUsed=" + isLarkUsed + " receiver=" + receiver);
        if (isLarkUsed) {
            MessageUtil mu = new MessageUtil();
            mu.send(MessageUtil.USER_SYSTEM, receiver, title);
        }

        com.redmoon.weixin.Config cfgWx = com.redmoon.weixin.Config.getInstance();
        if (cfgWx.getBooleanProperty("isUse")) {
            DebugUtil.log(getClass(), "sendMsg", "微信消息：" + title);
            WXMessageMgr wxMessageMgr = new WXMessageMgr();
            wxMessageMgr.sendTextMessage(receiver, title, action, WXMessageMgr.MESSAGE_TYPE_ENMU.FLOW);
        }
        com.redmoon.dingding.Config cfgDd = com.redmoon.dingding.Config.getInstance();
        if (cfgDd.isUseDingDing()) {
            DebugUtil.log(getClass(), "sendMsg", "钉钉消息：" + title);
            MsgService msgService = new MsgService();
            msgService.sendMsg(receiver, title, action, MsgService.MESSAGE_TYPE_ENMU.FLOW);
        }

        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, sender);
            ps.setString(5, receiver);
            ps.setInt(6, type);
            ps.setString(7, "");
            java.util.Date curDate = new java.util.Date();
            ps.setTimestamp(8, new Timestamp(curDate.getTime()));
            ps.setInt(9, 0);
            ps.setString(10, receiversAll);
            ps.setInt(11, 1);
            ps.setTimestamp(12, new Timestamp(curDate.getTime()));
            ps.setInt(13, 0);
            ps.setInt(14, 0);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendMsgId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();
        } catch (Exception e) {
            logger.error("sendSysMsg: " + e.getMessage());
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    @Override
    public ObjectDb getObjectDb(Object primaryKeyValue) {
        MessageCache uc = new MessageCache(this);
        primaryKey.setValue(primaryKeyValue);
        return (MessageDb) uc.getObjectDb(primaryKey);
    }

    /**
     * 根据用户、动作删除相应的记录
     *
     * @param userName   　用户名
     * @param actionType 动作类型
     * @param actionId   动作ID
     * @return
     */
    public boolean del(String userName, String actionType, String actionId) {
        // String action = "action=" + MessageDb.ACTION_PAPER_DISTRIBUTE + "|paperId=" + paperId;
        if (actionType.equals(ACTION_PAPER_DISTRIBUTE)) {
            String action = "action=" + ACTION_PAPER_DISTRIBUTE + "|paperId=" + actionId;
            String sql = "select id from oa_message where receiver=" + StrUtil.sqlstr(userName) + " and action=" + StrUtil.sqlstr(action);
            Iterator ir = list(sql).iterator();
            if (ir.hasNext()) {
                MessageDb md = (MessageDb) ir.next();
                return md.del();
            }
        }
        return false;
    }

    /**
     * 删除消息
     */
    @Override
    public synchronized boolean del() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;

            if (re) {
                MessageCache mc = new MessageCache(this);
                mc.refreshDel(primaryKey);
                mc.refreshNewCountOfReceiver(receiver);
            }
        } catch (Exception e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 删除附件
            long allSize = 0;
            if (attachments != null) {
                Iterator ir = attachments.iterator();
                while (ir.hasNext()) {
                    Attachment att = (Attachment) ir.next();
                    allSize += att.getSize();
                    att.del();
                }
            }

            if (re) {
                // 更新用户的磁盘已用空间
                UserSetupDb usd = new UserSetupDb();
                if (receiver.equals(""))
                    usd = usd.getUserSetupDb(sender);
                else
                    usd = usd.getUserSetupDb(receiver);
                usd.setMsgSpaceUsed(usd.getMsgSpaceUsed() - allSize);
                usd.save();
            }

            MessageCache uc = new MessageCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshDel(primaryKey);

        }
        return re;
    }

    @Override
    public int getObjectCount(String sql) {
        MessageCache uc = new MessageCache(this);
        return uc.getObjectCount(sql);
    }

    @Override
    public int getMessageCount(String sql) {
        return getObjectCount(sql);
    }

    @Override
    public Object[] getObjectBlock(String query, int startIndex) {
        MessageCache dcm = new MessageCache(this);
        return dcm.getObjectBlock(query, startIndex);
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new MessageDb(pk.getIntValue());
    }

    @Override
    public void setQueryCreate() {
        this.QUERY_CREATE =
                "insert into oa_message (id,title,content,sender,receiver,type,ip,rq,box,receivers_all,is_sent,send_time,receipt_state,msg_level,action,action_type,action_sub_type,receivers_cs,receivers_ms,receivers_js,send_msg_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void setQuerySave() {
        this.QUERY_SAVE =
                "update oa_message set isreaded=?,box=?,is_sent=?,title=?,content=?,receivers_all=?,send_time=?,receipt_state=?,msg_level=?,is_dustbin=?,is_sender_dustbin=?,receivers_cs=?,receivers_ms=?,receivers_js=? where id=?";
    }

    @Override
    public void setQueryDel() {
        this.QUERY_DEL = "delete from oa_message where id=?";
    }

    @Override
    public void setQueryLoad() {
        QUERY_LOAD = "select title,content,sender,receiver,rq,ip,type,isreaded,box,receivers_all,is_sent,send_time,receipt_state,msg_level,action,is_dustbin,action_type,action_sub_type,is_sender_dustbin,receivers_cs,receivers_ms,receivers_js,send_msg_id from oa_message where id=?";
    }

    @Override
    public void setQueryList() {
        QUERY_LIST = "select id from oa_message order by isreaded asc,rq desc";
    }

    /**
     * 保存消息
     */
    @Override
    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, readed ? 1 : 0);
            ps.setInt(2, box);
            ps.setInt(3, isSent);
            ps.setString(4, title);
            ps.setString(5, content);
            ps.setString(6, receiversAll);
            // LogUtil.getLog(getClass()).info("sendTime=" + sendTime);
            java.util.Date st = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
            if (st != null) {
                ps.setTimestamp(7, new Timestamp(st.getTime()));
            } else {
                ps.setTimestamp(7, null);
            }

            ps.setInt(8, receiptState);
            ps.setInt(9, msgLevel);
            ps.setInt(10, dustbin ? 1 : 0);
            ps.setInt(11, senderDustbin ? 1 : 0);
            ps.setString(12, receiverscs);
            ps.setString(13, receiversms);
            ps.setString(14, receiversjs);
            ps.setInt(15, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("save:" + StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            MessageCache uc = new MessageCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshSave(primaryKey);
            uc.refreshNewCountOfReceiver(receiver);
        }
        return re;
    }

    @Override
    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    /**
     * 载入消息
     */
    @Override
    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                title = rs.getString(1);
                content = rs.getString(2);
                sender = rs.getString(3);
                receiver = rs.getString(4);
                rq = DateUtil.format(rs.getTimestamp(5), "yyyy-MM-dd HH:mm");
                ip = rs.getString(6);
                type = rs.getInt(7);
                readed = rs.getInt(8) == 1 ? true : false;
                box = rs.getInt(9);
                receiversAll = rs.getString(10);
                isSent = rs.getInt(11);
                sendTime = DateUtil.format(rs.getTimestamp(12), "yyyy-MM-dd HH:mm");
                receiptState = rs.getInt(13);
                msgLevel = rs.getInt(14);
                action = StrUtil.getNullStr(rs.getString(15));
                dustbin = rs.getInt(16) == 1;
                actionType = StrUtil.getNullStr(rs.getString(17));
                actionSubType = StrUtil.getNullStr(rs.getString(18));
                senderDustbin = rs.getInt(19) == 1 ? true : false;
                receiverscs = rs.getString(20);
                receiversms = rs.getString(21);
                receiversjs = rs.getString(22);
                sendMsgId = rs.getInt(23);
                loaded = true;

                primaryKey.setValue(new Integer(id));

                String LOAD_DOCUMENT_ATTACHMENTS =
                        "SELECT id FROM oa_message_attach WHERE msgId=? order by orders";
                attachments = new Vector();
                pstmt = conn.prepareStatement(LOAD_DOCUMENT_ATTACHMENTS);
                pstmt.setInt(1, id);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        int aid = rs.getInt(1);
                        Attachment am = new Attachment(aid);
                        attachments.addElement(am);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    @Override
    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    @Override
    public void setAttachments(Vector attachments) {
        this.attachments = attachments;
    }

    @Override
    public void setBox(int box) {
        this.box = box;
    }

    /**
     * 取得某用户的新消息
     */
    @Override
    public Vector getNewMsgsOfUser(String userName) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            // 2013-06-08 fgf 只取上次刷新时间之后的新消息，这样可使消息只提醒一次
            UserSetupDb usd = new UserSetupDb();
            usd = usd.getUserSetupDb(userName);

            // String sql = "select id from oa_message where receiver=? and isreaded=0 and box=" + INBOX + " and is_sent=1";
            //String dateStr = DateUtil.format(usd.getLastMsgNotifyTime(), "yyyy-MM-dd HH:mm:ss");
            Date today = DateUtil.parse(DateUtil.format(new Date(),
                    "yyyy-MM-dd")
                    + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
            String dateStr = "";

            if (usd != null && usd.isLoaded()) {
                Date lastNotifyTime = usd.getLastMsgNotifyTime();

                if (lastNotifyTime != null) {
                    dateStr = DateUtil.format(
                            today.before(lastNotifyTime) ? today : lastNotifyTime,
                            "yyyy-MM-dd HH:mm:ss");
                }
            } else {
                logger.error("usd is null!");
            }

            // System.out.println("getNewMsgsOfUser usd.getLastMsgNotifyTime()=" + usd.getLastMsgNotifyTime());

            String sql = "select id from oa_message where receiver=? and isreaded=0 and box=" + INBOX + " and is_sent=1 and send_time>" + SQLFilter.getDateStr(dateStr, "yyyy-MM-dd HH:mm:ss");
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    IMessage md = getMessageDb(rs.getInt(1));
                    // System.out.println("getNewMsgsOfUser id=" + rs.getInt(1));
                    v.addElement(md);
                }
            }

            usd.setLastMsgNotifyTime(new java.util.Date());
            usd.save();

            // System.out.println("getNewMsgsOfUser save()=" + usd.getLastMsgNotifyTime());

        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    @Override
    public Attachment getAttachment(int attId) {
        Iterator ir = getAttachments().iterator();
        while (ir.hasNext()) {
            Attachment at = (Attachment) ir.next();
            if (at.getId() == attId) {
                return at;
            }
        }
        return null;
    }

    /**
     * 转发消息
     */
    @Override
    public boolean TransmitMsg(ServletContext application, HttpServletRequest request, String sender, int msgId) throws ErrMsgException {
        MessageForm mf = new MessageForm(application, request, this);
        mf.checkTransmit();
        if (this.title == null || this.title.equals("") || this.content == null
                || this.content.equals("") || this.receiver == null
                || this.receiver.equals("")) {
            mf.checkCreate();
        }
        this.sender = sender;
        String[] ary = receiver.split(",");
        int len = ary.length;
        UserMgr um = new UserMgr();
        for (int i = 0; i < len; i++) {
            // 检查用户是否存在
            UserDb user = um.getUserDb(ary[i]);
            if (!user.isLoaded()) {
                throw new ErrMsgException("用户" + ary[i] + "不存在！");
            }
        }
        int sendId = 0;
        if (this.isToOutBox) {
            this.box = MessageDb.OUTBOX;
            transmit("", msgId);
            sendId = id;
        }
        this.box = MessageDb.INBOX;
        for (int i = 0; i < len; i++) {
            if (sendId == 0) {
                transmit(ary[i], msgId);
            } else {
                transmit(ary[i], msgId, sendId);
            }
        }
        return true;
    }

    /**
     * 转发，草稿、收件、发件箱中的消息再次发送时都是调用本方法
     *
     * @param toUser String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean transmit(String toUser, int msgId) throws ErrMsgException {
        IMessage md = getMessageDb(msgId);

        // 检查接收者邮箱容量大小是否足够
        long allSize = 0;
        Vector v = md.getAttachments();
        if (v.size() > 0) {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                Attachment att = (Attachment) ir.next();
                allSize += att.getSize();
            }
        }

        UserSetupDb usd = new UserSetupDb();
        if (toUser.equals("")) {
            usd = usd.getUserSetupDb(sender);
            // 发件箱的情况,不需要记录send_id
            if (action != null && action.equals("send_id=" + msgId)) {
                action = "";
            }
        } else {
            usd = usd.getUserSetupDb(toUser);
            // 收件箱的情况,需要记录send_id
            if (action == null || action.equals("")) {
                action = "send_id=" + msgId;
            }
        }
        if (usd.getMsgSpaceUsed() + allSize > usd.getMsgSpaceAllowed())
            throw new ErrMsgException("接收者的空间不足，无法发送！");

        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);

            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);

            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, sender);
            ps.setString(5, toUser);
            ps.setInt(6, type);
            ps.setString(7, ip);
            java.util.Date curDate = new java.util.Date();
            Timestamp ts = new Timestamp(curDate.getTime());
            ps.setTimestamp(8, ts);
            ps.setInt(9, box);
            ps.setString(10, receiversAll);
            ps.setInt(11, 1);
            java.util.Date sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
            ps.setTimestamp(12, new Timestamp(sendDate.getTime()));
            ps.setInt(13, receiptState);
            ps.setInt(14, msgLevel);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendMsgId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();

            if (re) {
                v = md.getAttachments();
                if (v.size() > 0) {
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        Attachment att = (Attachment) ir.next();
                        String fullPath = Global.realPath + att.getVisualPath() + "/" + att.getDiskName();
                        String newName = RandomSecquenceCreator.getId() + "." + StrUtil.getFileExt(att.getDiskName());

                        String newFullPath = Global.realPath + att.getVisualPath() + "/" + newName;
                        FileUtil.CopyFile(fullPath, newFullPath);

                        Attachment att2 = new Attachment();
                        // att2.setFullPath(newFullPath);
                        att2.setMsgId(id);
                        // att2.setFullPath(fullPath);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(att.getVisualPath());
                        att2.setSize(att.getSize());
                        re = att2.create();
                    }

                    // 更新用户的邮箱已用空间
                    usd.setMsgSpaceUsed(usd.getMsgSpaceUsed() + allSize);
                    usd.save();
                }
            }
        } catch (Exception e) {
            logger.error("transmit: " + e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 转发，草稿、收件、发件箱中的消息再次发送时都是调用本方法
     *
     * @param toUser String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean transmit(String toUser, int msgId, int sendId) throws ErrMsgException {
        IMessage md = getMessageDb(msgId);

        // 检查接收者邮箱容量大小是否足够
        long allSize = 0;
        Vector v = md.getAttachments();
        if (v.size() > 0) {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                Attachment att = (Attachment) ir.next();
                allSize += att.getSize();
            }
        }

        UserSetupDb usd = new UserSetupDb();
        if (toUser.equals("")) {
            usd = usd.getUserSetupDb(sender);
            // 发件箱的情况,不需要记录send_id
            if (action != null && action.equals("send_id=" + sendId)) {
                action = "";
            }
        } else {
            usd = usd.getUserSetupDb(toUser);
            // 收件箱的情况,需要记录send_id
            if (action == null || action.equals("")) {
                action = "send_id=" + sendId;
            }
        }
        if (usd.getMsgSpaceUsed() + allSize > usd.getMsgSpaceAllowed())
            throw new ErrMsgException("接收者的空间不足，无法发送！");

        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);

            id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);

            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, sender);
            ps.setString(5, toUser);
            ps.setInt(6, type);
            ps.setString(7, ip);
            java.util.Date curDate = new java.util.Date();
            Timestamp ts = new Timestamp(curDate.getTime());
            ps.setTimestamp(8, ts);
            ps.setInt(9, box);
            ps.setString(10, receiversAll);
            ps.setInt(11, 1);
            java.util.Date sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
            ps.setTimestamp(12, new Timestamp(sendDate.getTime()));
            ps.setInt(13, receiptState);
            ps.setInt(14, msgLevel);
            ps.setString(15, action);
            ps.setString(16, actionType);
            ps.setString(17, actionSubType);
            ps.setString(18, receiverscs);
            ps.setString(19, receiversms);
            ps.setString(20, receiversjs);
            ps.setInt(21, sendId);
            re = conn.executePreUpdate() == 1 ? true : false;

            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();

            if (re) {
                v = md.getAttachments();
                if (v.size() > 0) {
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        Attachment att = (Attachment) ir.next();
                        String fullPath = Global.realPath + att.getVisualPath() + "/" + att.getDiskName();
                        String newName = RandomSecquenceCreator.getId() + "." + StrUtil.getFileExt(att.getDiskName());

                        String newFullPath = Global.realPath + att.getVisualPath() + "/" + newName;
                        FileUtil.CopyFile(fullPath, newFullPath);

                        Attachment att2 = new Attachment();
                        // att2.setFullPath(newFullPath);
                        att2.setMsgId(id);
                        // att2.setFullPath(fullPath);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(att.getVisualPath());
                        att2.setSize(att.getSize());
                        re = att2.create();
                    }

                    // 更新用户的邮箱已用空间
                    usd.setMsgSpaceUsed(usd.getMsgSpaceUsed() + allSize);
                    usd.save();
                }
            }
        } catch (Exception e) {
            logger.error("transmit: " + e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean canDoReturn(String userName, int sendId) {
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        String sql = "select isreaded from oa_message where sender="
                + StrUtil.sqlstr(userName) + " and action="
                + StrUtil.sqlstr("send_id=" + sendId) + " and box="
                + MessageDb.INBOX;
        boolean canDoReturn = true;
        try {
            ri = jt.executeQuery(sql);
            if (!ri.hasNext()) {
                canDoReturn = false;
            } else {
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    if (rr.getInt(1) == 1) {
                        canDoReturn = false;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("listToUser: " + e.getMessage());
        } finally {
            jt.close();
        }
        return canDoReturn;
    }

    public boolean doReturn(String userName, int sendId) {
        JdbcTemplate jt = new JdbcTemplate();
        String sql = "delete from oa_message where sender="
                + StrUtil.sqlstr(userName) + " and action="
                + StrUtil.sqlstr("send_id=" + sendId) + " and isreaded=0 and box=" + MessageDb.INBOX;
        boolean re = false;
        try {
            re = jt.executeUpdate(sql) >= 1;

        } catch (SQLException e) {
            logger.error("listToUser: " + e.getMessage());
        } finally {
            jt.close();
        }
        return re;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    /**
     * 只转换回车
     *
     * @param str
     * @return
     */
    public static String toHtml(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        java.lang.StringBuffer buf = new java.lang.StringBuffer(
                str.length() + 6);
        char ch = ' ';
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ch == '\n') {
                buf.append("<br>");
            } else {
                buf.append(ch);
            }
        }
        str = buf.toString();
        return str;
    }

    /**
     * 显示链接,action格式：action=flow_dispose|myActionId=111
     *
     * @param request
     * @return
     */
    public String renderAction(HttpServletRequest request) {
        if (action.equals("")) {
            return "";
        }

        String[] ary = StrUtil.split(action, "\\|");
        if (ary == null) {
            return "";
        }

        // 兼容6.0前旧版本
        int len = ary.length;
        Map map = null;
        if (len >= 1) {
            map = new HashMap();
            for (int i = 0; i < len; i++) {
                String[] pair = ary[i].split("=");
                if (pair.length == 2) {
                    map.put(pair[0], pair[1]);
                }
            }
        }

        String myaction = "";
        if (map != null) {
            myaction = StrUtil.getNullStr((String) map.get("action"));
            if ("".equals(myaction)) {
                myaction = actionType;
            }
        } else {
            myaction = actionType;
        }

        if (myaction.equals(ACTION_FLOW_DISPOSE)) {
            String strMyActionId;
            if (map != null) {
                strMyActionId = (String) map.get("myActionId");
            } else {
                strMyActionId = action;
            }

            long myActionId = StrUtil.toLong(strMyActionId, -1);
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);
            if (mad == null || !mad.isLoaded()) {
                return "<span style='color:red'>当前流程已删除</span>";
            }

            WorkflowMgr wfm = new WorkflowMgr();
            WorkflowDb wf = wfm.getWorkflowDb((int) mad.getFlowId());

            if (wf == null || wf.getDocId() == -1) {
                return "<span style='color:red'>当前流程已删除</span>";
            } else {
                com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
                lf = lf.getLeaf(wf.getTypeCode());

                if (lf.getType() == com.redmoon.oa.flow.Leaf.TYPE_LIST) {
                    return "<a href='javascript:;' onclick=\"addTab('" + wf.getTitle().replaceAll("\r\n", "").trim() + "', '" + request.getContextPath() + "/flow_dispose.jsp?myActionId=" + myActionId + "')\">点击处理流程</a>";
                } else {
                    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                    wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
                    if (wpd.isLight()) {
                        return "<a href='javascript:;' onclick=\"addTab('" + wf.getTitle().replaceAll("\r\n", "").trim() + "', '" + request.getContextPath() + "/flow_dispose_light.jsp?myActionId=" + myActionId + "')\">点击处理流程</a>";
                    } else {
                        return "<a href='javascript:;' onclick=\"addTab('" + wf.getTitle().replaceAll("\r\n", "").trim() + "', '" + request.getContextPath() + "/flow_dispose_free.jsp?myActionId=" + myActionId + "')\">点击处理流程</a>";
                    }
                }
            }
        } else if (myaction.equals(ACTION_FLOW_SHOW)) {
            String strFlowId;
            if (map != null) {
                strFlowId = (String) map.get("flowId");
            } else {
                strFlowId = action;
            }
            WorkflowMgr wfm = new WorkflowMgr();
            WorkflowDb wf = wfm.getWorkflowDb(StrUtil.toInt(strFlowId));

            if (wf == null || wf.getDocId() == -1) {
                return "<span style='color:red'>当前流程已删除</span>";
            } else {
                com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
                String desKey = config.get("key");
                // 以flowId作为值加密
                String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(wf.getId()));

                WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
                if (wpd.isLight()) {
                    return "<a href='javascript:;' onclick=\"addTab('" + StrUtil.toHtml(wf.getTitle()) + "', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + strFlowId + "&visitKey=" + visitKey + "')\">点击查看流程处理过程</a>";
                } else {
                    return "<a href='javascript:;' onclick=\"addTab('" + StrUtil.toHtml(wf.getTitle()) + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + strFlowId + "&visitKey=" + visitKey + "')\">点击查看流程处理过程</a>";
                }
            }
        } else if (myaction.equals(ACTION_WORKPLAN)) {
            String strId;
            if (map != null) {
                strId = (String) map.get("id");
            } else {
                strId = action;
            }
            WorkPlanDb wpd = new WorkPlanDb();
            wpd = wpd.getWorkPlanDb(StrUtil.toInt(strId));

            if (wpd == null || !wpd.isLoaded()) {
                return "<span style='color:red'>当前工作计划已删除</span>";
            } else {
                return "<a href='javascript:;' onclick=\"addTab('" + wpd.getTitle() + "', '" + request.getContextPath() + "/workplan/workplan_show.jsp?id=" + strId + "')\">点击查看计划</a>";
            }
        } else if (myaction.equals(ACTION_PLAN)) {
            String strId;
            if (map != null) {
                strId = (String) map.get("id");
            } else {
                strId = action;
            }
            PlanDb pd = new PlanDb();
            pd = pd.getPlanDb(StrUtil.toInt(strId));
            if (pd == null || !pd.isLoaded()) {
                return "<span style='color:red'>当前日程安排已删除</span>";
            } else {
                return "<a href='javascript:;' onclick=\"addTab('" + pd.getTitle() + "', '" + request.getContextPath() + "/plan/plan_show.jsp?id=" + strId + "')\">点击查看日程安排</a>";
            }
        } else if (myaction.equals(ACTION_PAPER_DISTRIBUTE)) {
            String paperId;
            if (map != null) {
                paperId = (String) map.get("paperId");
            } else {
                paperId = action;
            }
            PaperDistributeDb pdd = new PaperDistributeDb();
            pdd = pdd.getPaperDistributeDb(StrUtil.toInt(paperId));
            if (pdd == null || !pdd.isLoaded()) {
                return "<span style='color:red'>当前收文已删除</span>";
            } else {
                String pdTitle = "发文";
                try {
                    pdTitle = pdd.getString("title");
                } catch (Exception e) {
                }
                return "<a href='javascript:;' onclick=\"addTab('" + pdTitle + "', '" + request.getContextPath() + "/paper/paper_show.jsp?paperId=" + paperId + "')\">点击查看</a>";
            }
        } else if (myaction.equals(ACTION_MODULE_SHOW)) {
            //actionSubType传入formcode,action传入id
            return "<a href='javascript:;' onclick=\"addTab('查看', '" + request.getContextPath() + "/visual/module_show.jsp?id=" + action + "&code=" + actionSubType + "&parentId=" + action + "')\">点击查看</a>";
        } else if (myaction.equals(ACTION_MODULE_EDIT)) {
            return "<a href='javascript:;' onclick=\"addTab('查看', '" + request.getContextPath() + "/visual/module_edit.jsp?id=" + action + "&code=" + actionSubType + "&formCode=" + actionSubType + "&parentId=" + action + "')\">点击查看</a>";
        } else if (myaction.equals(ACTION_WORKLOG)) {
            return "<a href='javascript:;' onclick=\"addTab('查看', '" + request.getContextPath() + "/ymoa/showWorkLogById.action?workLogId=" + action + "')\">点击查看</a>";
        }
        else if (myaction.equals(ACTION_FILEARK_NEW)) {
            return "<a href='javascript:;' onclick=\"addTab('查看', '" + request.getContextPath() + "/doc_show.jsp?id=" + action + "')\">点击查看</a>";
        }
        else if (myaction.equals(ACTION_NOTICE)) {
            NoticeDb nd = new NoticeDb(StrUtil.toLong(action, -1));
            if (nd == null || !nd.isLoaded()) {
                return "<span style='color:red'>当前通知公告已删除</span>";
            } else {
                return "<a href='javascript:;' onclick=\"addTab('" + nd.getTitle() + "', '" + request.getContextPath() + "/notice/show.do?id=" + action + "&isShow=1')\">点击查看</a>";
            }
        }
        else {
            // 向下兼容
            String strId = StrUtil.getNullStr((String) map.get("noticeId"));
            if (!strId.equals("")) {
                NoticeDb nd = new NoticeDb(StrUtil.toLong(strId));
                if (nd == null || !nd.isLoaded()) {
                    return "<span style='color:red'>当前通知公告已删除</span>";
                } else {
                    return "<a href='javascript:;' onclick=\"addTab('" + nd.getTitle() + "', '" + request.getContextPath() + "/notice/show.do?id=" + strId + "&isShow=1')\">点击查看</a>";
                }
            }
        }
        return "";
    }

    // 置消息中心中当前处理人当前流程的消息为已读
    public void setUserFlowReaded(String userName, long myActionId) {
        setCommonUserReaded(userName, myActionId, MESSAGE_SYSTEM_FLOW_TYPE);
    }

    /**
     * lzm 更新oa_message 表中通知公告 ，流程 已读标志位
     *
     * @param userName
     * @param id
     * @param type
     * @Description:
     */
    public void setCommonUserReaded(String userName, long id, int type) {
        String action = StrUtil.sqlstr("noticeId=" + id);
        switch (type) {
            case MESSAGE_SYSTEM_FLOW_TYPE:
                action = StrUtil.sqlstr("action=" + ACTION_FLOW_DISPOSE + "|myActionId=" + id);
                break;
            default:
                break;
        }
        String sql = "select id from oa_message where receiver=" + StrUtil.sqlstr(userName) + " and action=" + action + " and isreaded=0 and box=" + MessageDb.INBOX;
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                int mId = rr.getInt(1);
                MessageDb md = new MessageDb(mId);
                md.setReaded(true);
                md.save();
            }
        } catch (SQLException e) {
            logger.error("setUserNoticeReaded: " + e.getMessage());
        } finally {
            if (jt != null) {
                jt.close();
            }
        }
    }

    public String getSqlOfSystem(String userName, int isRecycle, String action, String what, String kind, String orderBy, String sort, String actionType) {
        String sql = "select id from oa_message where is_sent=1 and receiver=" + StrUtil.sqlstr(userName) + " and box=" + MessageDb.INBOX + " and type=" + MessageDb.TYPE_SYSTEM;
        if (isRecycle == 0) {
            sql += " and is_dustbin=0";
        } else {
            sql += " and is_dustbin=1";
        }
        if (action.equals("search")) {
            if (!"".equals(what)) {
                if (kind.equals("title")) {
                    sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
                } else if (kind.equals("content")) {
                    sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
                } else if (kind.equals("notreaded")) {
                    sql += " and box=" + MessageDb.INBOX + " and isreaded=0 and (content like " + StrUtil.sqlstr("%" + what + "%") + "or title like " + StrUtil.sqlstr("%" + what + "%") + ")";
                }
            }

            if (!"".equals(actionType)) {
                sql += " and action_type=" + StrUtil.sqlstr(actionType);
            }
        }
        if (orderBy.equals("")) {
            // sql += " order by isreaded asc,rq desc";
            sql += " order by rq desc";
        } else {
            if (orderBy.equals("byTitle")) {
                sql += " order by title";
            } else if (orderBy.equals("bySender")) {
                sql += " order by sender";
            } else if (orderBy.equals("byDate")) {
                sql += " order by rq";
            } else {
                sql += " order by " + orderBy;
            }
            if (sort.equals("asc")) {
                sql += " asc";
            } else {
                sql += " desc";
            }
        }
        return sql;
    }

    public void setDustbin(boolean dustbin) {
        this.dustbin = dustbin;
    }

    public boolean isDustbin() {
        return dustbin;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionSubType(String actionSubType) {
        this.actionSubType = actionSubType;
    }

    public String getActionSubType() {
        return actionSubType;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the summary
     */
    @Override
    public String getSummary() {
        summary = StrUtil.getAbstract(null, content, 100);
        return summary;
    }

    private Vector attachments;

    private String summary;

    /**
     * 取得发送者的头像
     *
     * @return
     */
    @Override
    public String getSenderPortrait() {
        if (SENDER_SYSTEM.equals(sender)) {
            return "img_show.jsp?path=images/sys_user.png";
        }
        IUserService userService = SpringUtil.getBean(IUserService.class);
        User user = userService.getUser(sender);
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        return userSetupService.getPortrait(user);
    }
}
