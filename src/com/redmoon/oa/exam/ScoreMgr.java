package com.redmoon.oa.exam;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class ScoreMgr {
    Logger logger = Logger.getLogger(ScoreMgr.class.getName());

    public ScoreMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        ScoreDb std = new ScoreDb();
        int score = ParamUtil.getInt(request, "score");
        std.setScore(score);
        std.setEndtime(new Date());
        return std.create();
    }

    public ScoreDb getScoreDb(int id) {
        ScoreDb std = new ScoreDb();
        return std.getScoreDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        ScoreDb std = getScoreDb(id);
        if (std == null || !std.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        return std.del();
    }

    /**
     * @param scoreId
     * @return
     * @Description: 根据scoreId 获取问答题是否评分
     */
    public boolean isAnswerChecked(int scoreId) {
        String sql = "select u.id from oa_exam_useranswer u, oa_exam_database d where u.is_correct = " + UserAnswerDb.ANSWER_NOT_CHECKED + " and d.id = u.question_id and d.exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_ANSWER)) + " and u.score_id = " + StrUtil.sqlstr(String.valueOf(scoreId));
        UserAnswerDb usd = new UserAnswerDb();
        Iterator it = usd.list(sql).iterator();
        boolean re = true;
        if (it.hasNext()) {
            re = false;
        }
        return re;
    }

    /**
     * @param scoreId
     * @return
     * @Description: 根据成绩id获取不包含问答题的成绩
     */
    public int getScoreNotHasAnswer(int scoreId) {
        ScoreDb sd = new ScoreDb();
        sd = sd.getScoreDb(scoreId);
        int paperId = sd.getPaperId();
        PaperDb pdb = new PaperDb();
        pdb = pdb.getPaperDb(paperId);
        int singlePer = pdb.getSingleper();
        int multiPer = 0;
        if (pdb.getMultiScoreRule() == PaperDb.MULTI_ALL_RIGHT_SCORE) {
            multiPer = pdb.getMultiper();
        } else {
            multiPer = pdb.getNotAllRightMuntiper();
        }
        int judgePer = pdb.getJudgeper();
        int result = 0;
        String selQuestionSql = "select od.id from oa_exam_score os, oa_exam_useranswer ou, oa_exam_database od where os.id = ou.score_id and ou.question_id = od.id and ou.is_correct = 1 and od.exam_type <> " + UserAnswerDb.ANSWER_NOT_CHECKED + " and os.id = " + StrUtil.sqlstr(String.valueOf(scoreId));
        QuestionDb qdb = new QuestionDb();
        Iterator it = qdb.list(selQuestionSql).iterator();
        while (it.hasNext()) {
            qdb = (QuestionDb) it.next();
            if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                result += singlePer;
            } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                result += multiPer;
            } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                result += judgePer;
            }
        }
        return result;
    }

    public JSONObject getNextScore(int paperId, int scoreId, int isPrj) {
        JSONObject json = new JSONObject();
        try {
            String sql = "select os.id from oa_exam_score os, oa_exam_useranswer ou where os.id = ou.score_id and os.paperid = " + StrUtil.sqlstr(String.valueOf(paperId)) + " and os.id > " + scoreId + " and ou.is_correct = 2 and os.is_prj =  " + StrUtil.sqlstr(String.valueOf(isPrj)) + " order by os.id limit 1";
            ScoreDb sd = new ScoreDb();
            Vector v = sd.list(sql);
            Iterator it = v.iterator();
            if (it.hasNext()) {
                sd = (ScoreDb) it.next();
                json.put("ret", 1);
                json.put("scoreId", sd.getId());
                json.put("userName", sd.getUserName());
            } else {
                json.put("ret", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject getLastScore(int paperId, int scoreId, int isPrj) {
        JSONObject json = new JSONObject();
        try {
            String sql = "select os.id from oa_exam_score os, oa_exam_useranswer ou where os.id = ou.score_id and os.paperid = " + StrUtil.sqlstr(String.valueOf(paperId)) + " and os.id < " + scoreId + " and ou.is_correct = 2 and os.is_prj = " + StrUtil.sqlstr(String.valueOf(isPrj)) + " order by os.id desc limit 1";
            ScoreDb sd = new ScoreDb();
            Vector v = sd.list(sql);
            Iterator it = v.iterator();
            if (it.hasNext()) {
                sd = (ScoreDb) it.next();
                json.put("ret", 1);
                json.put("scoreId", sd.getId());
                json.put("userName", sd.getUserName());
            } else {
                json.put("ret", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
