package com.redmoon.forum;

import cn.js.fan.web.Global;
import org.apache.log4j.Logger;

/**
 *
 * <p>Title:在线用户统计信息 </p>
 *
 * <p>Description: 用于保存如社区在线 16 人，当前论坛 13 人，其中注册用户 9 人，游客 4 人. 今日帖子 0</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class OnlineInfo {
    String connname = Global.getDefaultDB();
    Logger logger = Logger.getLogger(OnlineInfo.class.getName());

    public OnlineInfo() {
    }

    // 社区在线 16 人，当前论坛 13 人，其中注册用户 9 人，游客 4 人. 今日帖子 0
    public int getAllCount() {
        OnlineUserDb oud = new OnlineUserDb();
        return oud.getAllCount();
    }

    public int getAllUserCount() {
        OnlineUserDb olc = new OnlineUserDb();
        return olc.getAllUserCount();
    }

    public int getBoardCount(String boardcode) {
        OnlineUserDb olc = new OnlineUserDb();
        return olc.getBoardCount(boardcode);
    }

    public int getBoardUserCount(String boardcode) {
        OnlineUserDb olc = new OnlineUserDb();
        return olc.getBoardUserCount(boardcode);
    }

    public int getBoardTodayTopicCount(String boardcode) {
        Leaf lf = new Leaf();
        lf.getLeaf(boardcode);
        return lf.getTodayCount();
    }

}
