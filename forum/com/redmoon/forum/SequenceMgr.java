package com.redmoon.forum;

import java.sql.*;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import cn.js.fan.db.Conn;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: 序列号管理</p>
 *
 * <p>Description: 序列存储于sq_id表中</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SequenceMgr implements com.cloudwebsoft.framework.base.ISequence,
        java.io.Serializable {
    String connname;
    private static final String LOAD_ID =
            "SELECT id FROM sq_id WHERE idType=?";
    private static final String UPDATE_ID =
            "UPDATE sq_id SET id=? WHERE idType=? AND id=?";
    private static final String INSERT_ID = "insert into sq_id (idType, id) values (?,?)";

    public static final int MSG = 0; // sq_message
    public static final int DOCUMENT = 1;
    public static final int AuctionOrderId = 2;
    public static final int ShopId = 3;
    public static final int SQ_CHATROOM = 4;
    public static final int SQ_FRIEND = 5;
    public static final int SQ_MASTER = 6;
    public static final int SQ_MESSAGE_ATTACH = 7;
    public static final int SQ_USER = 8;
    public static final int DOCUMENT_ATTACH = 9;
    public static final int COMMENT = 10;
    public static final int PHOTO = 11;
    public static final int SQ_FORBID_IP_RANGE = 12;
    public static final int CMS_IMAGES = 13;
    public static final int SQ_IMAGES = 14; // htmlcode中的图片
    public static final int SQ_SHORT_MESSAGE = 15;
    public static final int INFO_ATTACH = 16;
    public static final int PRICE_ID = 17;
    public static final int MARKET_ID = 18;
    public static final int SQ_CHATROOM_EMCEE = 19;
    public static final int CMS_TEMPLATE_ID = 20;
    public static final int THREAD_TYPE_ID = 21;
    public static final int SQ_AD_CONTENT_ID = 22;
    public static final int SQ_AD_BOARD_ID = 23; // 未使用
    public static final int DIR_PRIV_ID = 24;
    public static final int BLOG_ID = 25; // 2007.2.28
    public static final int CMS_ROBOT_ID = 26; // 2007.3.8
    public static final int SQ_SCORE_RECORD = 27;
    public static final int SQ_SCHEDULER = 28;
    public static final int FORUM_ROBOT_ID = 29; // 2007.3.16
    public static final int CMS_SUBJECT_LIST_ID = 30; // 2007.4.3
    public static final int CMS_IMG_STORE_FILE = 31; // 2007.4.19
    public static final int CMS_SOFTWARE_FILE = 32;
    public static final int CMS_AD = 33;
    public static final int PLUGIN_WITKEY_EVALUATION = 34;
    public static final int DESKTOP_ITEM = 35;
    public static final int TOPIC_OP = 36;
    public static final int PLUGIN_PRESENT = 37;
    public static final int SQ_TAG_ID = 38;
    public static final int MESSAGE_REPORT_ID = 39;
    public static final int BLOG_LINK = 40;
    public static final int SQ_LINK = 41;
    public static final int PLUGIN_GROUP = 42;
    public static final int PLUGIN_GROUP_THREAD = 43;
    public static final int PLUGIN_GROUP_PHOTO = 44;
    public static final int PLUGIN_GROUP_ACTIVITY = 45;
    public static final int PLUGIN_AUCTION_CATALOG_PRIV = 46;
    public static final int PLUGIN_AUCTION_WORTH = 47;
    public static final int PLUGIN_AUCTOIN_BID = 48;
    public static final int MESSAGE_RECOMMEND_ID = 49;
    public static final int SMS_SEND_RECORD = 50;
    public static final int GUESTBOOK = 51;
    public static final int DIG = 52;
    public static final int BLOG_MUSIC = 53;
    public static final int BLOG_VIDEO = 54;
    public static final int PLUGIN_FETION_ACTIVITY_MEMBER = 55;

    public static final int SQ_FORUM_MUSIC_USER = 56;

    public static final int PLUGIN_EXAM_QUESTION = 57;
    public static final int PLUGIN_EXAM_QUESTION_OPTION = 58;
    public static final int PLUGIN_EXAM_USER_ANSWER = 59;

    public static final int PLUGIN_BLOG_TEMPLATE = 60;
    public static final int CMS_SITE_AD = 61;
    public static final int CMS_FLASH_STORE_FILE = 62;
    public static final int CMS_SITE_FLASH_IMAGE = 63;
    public static final int CMS_SITE_TEMPLATE = 64;
    public static final int CMS_SITE_SCROLL_IMG = 65;

    public static final int BLOG_PHOTO_COMMENT = 66;
    public static final int BLOG_PHOTO_CATALOG = 67;

    public static final int SQ_SCORE_LOG = 68;
    public static final int SQ_BOARD_VISIT_LOG = 69;

    public static final int CMS_SOFTWARE_DOWNLOAD = 70;

    public static final int BLOG_MUSIC_COMMENT = 71;
    public static final int BLOG_VIDEO_COMMENT = 72;

    public static final int QUESTIONNAIRE_FORM = 73;
    public static final int QUESTIONNAIRE_FORM_ITEM = 74;
    public static final int QUESTIONNAIRE_FORM_SUBITEM = 75;
    public static final int QUESTIONNAIRE_ITEM = 76;
    public static final int QUESTIONNAIRE_SUBITEM = 77;
    public static final int QUESTIONNAIRE_NUM = 78;

    public static final int LOG = 79;

    public static final int CMS_DIR_VISIT_LOG = 80;

    public static final int CMS_VIDEO_STORE_FILE = 81;

    public static final int SQ_REGIST_QUIZ = 82;

    public static final int SQ_T_MSG = 83;
    public static final int SQ_T_MSG_ATT = 84;

    public static final int SNS_APP_ACTION = 85;

    public static final int SQ_T_TAG = 86;

    private static int INCREMENT = 10;

    static {
        INCREMENT = Global.isCluster() ? 1 : 10;
    }

    public SequenceMgr() {

    }

    public static String getTypeCacheKey(int idType) {
        return "bbs-Sequence-" + idType;
    }

    public static SequenceMgr getSequenceMgr(int idType) {
        // 如果启用了集群
        if (Global.isCluster()) {
            return new SequenceMgr(idType);
        } else {
            SequenceMgr sm = (SequenceMgr) RMCache.getInstance().get(
                    getTypeCacheKey(idType));
            if (sm == null) {
                sm = new SequenceMgr(idType);
                try {
                    RMCache.getInstance().put(getTypeCacheKey(idType), sm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return sm;
        }
    }

    /*
    public void refreshSequenceMgr(int idType) {
        try {
            RMCache.getInstance().remove(getTypeCacheKey(idType));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public static long nextID() {
        return getSequenceMgr(MSG).nextUniqueID();
    }

    /**
     * Returns the next ID of the specified type.
     *
     * @param type the type of unique ID.
     * @return the next unique ID of the specified type.
     */
    public static long nextID(int type) {
        return getSequenceMgr(type).nextUniqueID();
    }

    public long getNextId(int type) {
        return nextID(type);
    }

    private int type;
    private long currentID;
    private long maxID;

    /**
     * Creates a new DbSequenceManager.
     */
    public SequenceMgr(int type) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("SequenceMgr:DB is empty！");
        this.type = type;
        currentID = 0l;
        maxID = 0l;
    }

    public synchronized long nextUniqueID() {
        // System.out.println(getClass() + " type=" + type + " currentID=" + currentID + " maxID=" + maxID);
        if (!(currentID < maxID)) {
            getNextBlock(5); // 尝试5次
        }
        // System.out.println(getClass() + " type=" + type + " currentID=" + currentID + " maxID=" + maxID);

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
     *  <li> 当相关表中的id值大于sq_id中的相应id值时，如：sq_message表中的id大于sq_id中的id值时
     *          插入数据至sq_message表中会失败，每次getNextBlock将会使sq_id中的值增大
     * </ol>
     */
    private void getNextBlock(int count) {
        if (count == 0) {
            System.err.println(
                    "Failed at last attempt to obtain an ID, aborting...");
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
            long currentID = 1;
            if (!rs.next()) {
                // throw new SQLException(
                //        "Loading the current ID failed. The sq_id table may not be correctly populated.");
                // 如果ID不存在，则自动创建，插入一条记录，idType的初始值为1
                pstmt.close();
                // System.out.println(getClass() + " load fail. Now insert.");
                pstmt = conn.prepareStatement(INSERT_ID);
                pstmt.setInt(1, type);
                pstmt.setLong(2, 1);
                conn.executePreUpdate();
            }
            else
                currentID = rs.getLong(1);
            pstmt.close();
            // Increment the id to define our block.
            long newID = currentID + INCREMENT;
            pstmt = conn.prepareStatement(UPDATE_ID);
            pstmt.setLong(1, newID);
            pstmt.setInt(2, type);
            pstmt.setLong(3, currentID);

            success = pstmt.executeUpdate() == 1;
            if (success) {
                this.currentID = currentID;
                this.maxID = newID;
            }
        } catch (Exception sqle) {
            sqle.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (!success) {
            LogUtil.getLog(getClass()).warn(
                    "WARNING: failed to obtain next ID block due to " +
                    "thread contention. Trying again...");
            // Call this method again, but sleep briefly to try to avoid thread
            // contention.
            try {
                Thread.currentThread().sleep(75);
            } catch (InterruptedException ie) {
                LogUtil.getLog(getClass()).error(ie.getMessage());
            }
            getNextBlock(count - 1);
        }
    }
}
