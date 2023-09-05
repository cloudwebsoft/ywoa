package com.redmoon.oa.db;

import java.sql.*;
import cn.js.fan.web.Global;
import cn.js.fan.db.Conn;
import com.cloudwebsoft.framework.util.LogUtil;

public class SequenceManager implements com.cloudwebsoft.framework.base.ISequence {
    String connname;
    private static final String LOAD_ID =
        "SELECT id FROM redmoonid WHERE idType=?";
    private static final String UPDATE_ID =
        "UPDATE redmoonid SET id=? WHERE idType=? AND id=?";
  
    public static final int OA_DOCUMENT_CMS = 0; // 存档文件
    public static final int OA_WORKFLOW = 1;
    public static final int OA_WORKFLOW_ACTION = 2;
    public static final int OA_WORKFLOW_LINK = 3;
    public static final int OA_EMAIL = 4;   // 邮件草稿
    public static final int OA_DOCUMENT_FLOW = 5; // 流程文档
    public static final int OA_TASK = 6;
    public static final int OA_MESSAGE = 7; // 短消息
    // public static final int OA_DOCUMENT_NETDISK = 8;
    public static final int OA_VISUAL_DOCUMENT = 9;
    public static final int OA_LOG = 10;
    public static final int OA_ADDRESS = 11;
    public static final int OA_ADDRESS_GROUP = 12;
    public static final int OA_WORKFLOW_PREDEFINED = 13;

    public static final int OA_ORACLE_INDEX = 14; // 用于oracle索引在创建时的值

    public static final int OA_ARCHIVE_STUDY = 15;
    public static final int OA_ARCHIVE_RESUME = 16;
    public static final int OA_ARCHIVE_FAMILY = 17;
    public static final int OA_ARCHIVE_PROFESSION = 18;
    public static final int OA_ARCHIVE_ASSESS = 19;
    public static final int OA_ARCHIVE_REWARDS = 20;
    public static final int OA_ARCHIVE_DUTY = 21;
    public static final int OA_ARCHIVE_QUERY = 22;
    public static final int OA_ARCHIVE_QUERY_CONDITION = 23;
    public static final int OA_ARCHIVE_USER_HIS = 24;
    public static final int OA_ARCHIVE_STUDY_HIS = 25;
    public static final int OA_ARCHIVE_RESUME_HIS = 26;
    public static final int OA_ARCHIVE_FAMILY_HIS = 27;
    public static final int OA_ARCHIVE_PROFESSION_HIS = 28;
    public static final int OA_ARCHIVE_ASSESS_HIS = 29;
    public static final int OA_ARCHIVE_REWARDS_HIS = 30;
    public static final int OA_ARCHIVE_DUTY_HIS = 31;
    public static final int OA_ARCHIVE_PRIVILEGE = 32;

    public static final int OA_WORKFLOW_SEQUENCE = 33;

    public static final int OA_WORKFLOW_MYACTION = 34;

    public static final int OA_DOCUMENT_ATTACH_CMS = 35; // 文件柜中文章的附件

    public static final int OA_DEPT_USER = 36;

    public static final int OA_USER_DESKTOP_SETUP = 37;

    public static final int OA_SMS_SEND_RECORD = 38;

    public static final int OA_SCHEDULER = 39;

    public static final int OA_NOTICE = 40;
    public static final int OA_NOTICE_ATTACH = 41;

    public static final int OA_FLOW_ANNEX = 42;
    public static final int OA_FLOW_ANNEX_ATTACHMENT = 43;

    public static final int OA_DEPT = 44;

    public static final int OA_USER = 45;
    public static final int OA_MESSAGE_ATTACHMENT = 46;

    public static final int OA_MESSAGE_IdioAttachment = 47; // 个人文件柜中的附件
    public static final int OA_IDIOMESSAGE = 48; // 个人文件柜

    public static final int OA_WORKPLAN_ANNEX = 49;
    public static final int OA_WORKPLAN_ANNEX_ATTACHMENT = 50;
    public static final int OA_WORKPLAN = 51;

    public static final int OA_SELECT_OPTION = 52;

    public static final int QUESTIONNAIRE_FORM = 53;
    public static final int QUESTIONNAIRE_FORM_ITEM = 54;
    public static final int QUESTIONNAIRE_FORM_SUBITEM = 55;
    public static final int QUESTIONNAIRE_NUM = 56;
    public static final int QUESTIONNAIRE_ITEM = 57;
    public static final int QUESTIONNAIRE_SUBITEM = 58;

    public static final int EXAM_SUBJECT = 59;
    public static final int EXAM_PAPER = 60;
    public static final int EXAM_QUESTION = 61;
    public static final int EXAM_SCORE = 62;
    public static final int EXAM_USERANSWER = 63;

    public static final int USER_FAVORITE = 64;

    public static final int FILEARK_PRIV = 65;

    public static final int VISUAL_MODULE_PRIV = 66;

    public static final int ASSET_INFO = 67;

    public static final int OA_WATCH_TYPE = 68;
    public static final int OA_WATCH_GROUP = 69;
    public static final int OA_WATCH_PRIVILEGE_OF_USER = 70;
    public static final int OA_WATCH_ROSTER = 71;
    public static final int OA_WATCH_RECORD = 72;
    public static final int OA_WATCH_RECORD_ATTACH = 73;
    public static final int OA_WATCH_BASIC = 74;
    
    public static final int PLAN_PERIODICITY = 75;
    
    public static final int OA_DOCUMENT_ROBOT = 76;
    public static final int CMS_IMAGES = 77;
    
    public static final int EMAIL_POP3 = 78;
    
    public static final int OA_STAMP = 79;
    public static final int OA_STAMP_LOG = 80;
    
    public static final int FLOW_DOCUMENT_ATTACH = 81;
    
    public static final int ASS_FORM = 82;

    public static final int OA_MENU_MOST_RECENTLY_USED = 83;
    
    /**
     * 短信发送批次
     */
    public static final int OA_SMS_BATCH = 84;
    
    public static final int OA_FORM_QUERY = 85;
    
    public static final int OA_FORM_QUERY_CONDITION = 86;
    
    public static final int OA_STAMP_PRIV = 87;
    
    public static final int FLOW_PAPER_NO_PREFIX = 88;
    
    public static final int OA_FLOW_PAPER_DISTRIBUTE = 89;

    /**
     * 职位
     */
    public static final int OA_POST = 90;
    
    public static final int OA_REPORT_MANAGE = 91;         //报表管理

    public static final int OA_ROLE = 92; //	角色模板id
    
    public static final int OA_WORK_LOG = 93;//工作日报
    
    public static final int OA_USER_GROUP = 94;//添加用户组
    
    public static final int OA_WORK_LOG_EXPAND = 95;//工作汇报扩展表ID
    
    public static final int OA_WORK_DATE = 96;//工作日表ID

    public static final int OA_FLASH_IMAGE = 97; // SiteFlashImageDb

    public static final int OA_FILEARK_ROBOT = 98;

    /**
     * 增量
     */
    private static int INCREMENT = 10;
    static {
        INCREMENT = Global.isCluster() ? 1 : 10;
    }

    // Statically startup a sequence manager for each of the five sequence
    // counters.
    private static SequenceManager[] managers;
    static {
        init();
    }
    
    public static void init() {
        managers = new SequenceManager[100];
        for (int i=0; i<managers.length; i++) {
            managers[i] = new SequenceManager(i);
        }    	
    }

    /**
     * Returns the next ID of the specified type.
     *
     * @param type the type of unique ID.
     * @return the next unique ID of the specified type.
     */
    public static long nextID(int type) {
        return managers[type].nextUniqueID();
    }

    public long getNextId(int type) {
        return nextID(type);
    }

    public static long nextID() {
        return managers[0].nextUniqueID();
    }

    private int type;
    private long currentID;
    private long maxID;

    public SequenceManager() {

    }

    /**
     * Creates a new DbSequenceManager.
     */
    public SequenceManager(int type) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("SequenceManager:默认数据库名为空！");
        this.type = type;
        currentID = 0l;
        maxID = 0l;
    }

    /**
     * Returns the next available unique ID. Essentially this provides for the
     * functionality of an auto-increment database field.
     */
    public synchronized long nextUniqueID() {
        if (! (currentID < maxID)) {
            // Get next block -- make 5 attempts at maximum.
            getNextBlock(5);
        }
        long id = currentID;
        currentID++;
        return id;
    }

    /**
     * Performs a lookup to get the next availabe ID block. The algorithm is as
     * follows:<ol>
     *  <li> Select currentID from appropriate db row.
     *  <li> Increment id returned from db.
     *  <li> Update db row with new id where id=old_id.
     *  <li> If update fails another process checked out the block first; go
     *          back to step 1. Otherwise, done.
     * </ol>
     */
    private void getNextBlock(int count) {
        if (count == 0) {
            System.err.println("Failed at last attempt to obtain an ID, aborting...");
            return;
        }
        boolean success = false;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            // Get the current ID from the database.
            pstmt = conn.prepareStatement(LOAD_ID);
            pstmt.setInt(1, type);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Loading the current ID failed. The " +
                    "redmoonid table may not be correctly populated. type=" + type);
            }
            long currentID = rs.getLong(1);
            pstmt.close();
            // Increment the id to define our block.
            long newID = currentID + INCREMENT;
            // The WHERE clause includes the last value of the id. This ensures
            // that an update will occur only if nobody else has performed an
            // update first.
            pstmt = conn.prepareStatement(UPDATE_ID);
            pstmt.setLong(1, newID);
            pstmt.setInt(2, type);
            pstmt.setLong(3, currentID);
            // Check to see if the row was affected. If not, some other process
            // already changed the original id that we read. Therefore, this
            // round failed and we'll have to try again.
            success = pstmt.executeUpdate() == 1;
            if (success) {
                this.currentID = currentID;
                this.maxID = newID;
            }
        }
        catch( Exception e ) {
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            try {  pstmt.close();   }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        if (!success) {
            System.err.println("WARNING: failed to obtain next ID block due to " +
                "thread contention. Trying again...");
            // Call this method again, but sleep briefly to try to avoid thread
            // contention.
            try {
                Thread.currentThread().sleep(75);
            } catch (InterruptedException ie) { }
            getNextBlock(count-1);
        }
    }
}
