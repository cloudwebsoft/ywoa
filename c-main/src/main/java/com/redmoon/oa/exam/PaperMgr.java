package com.redmoon.oa.exam;

import java.sql.SQLException;
import java.sql.Struct;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.raq.cellset.series.DictSeriesConfig.Select;

// 日程安排
public class PaperMgr {
    Logger logger = Logger.getLogger(PaperMgr.class.getName());

    public PaperMgr() {

    }

    public boolean create(HttpServletRequest request) throws ErrMsgException,
            SQLException {
        PaperDb ptd = new PaperDb();
        boolean re = true;
        String subject = "", starttime = "", endtime = "", settime = "";
        String errmsg = "", title = "", major = "";
        subject = ParamUtil.get(request, "subject");
        int testtime = ParamUtil.getInt(request, "testtime");
        int totalper = ParamUtil.getInt(request, "totalper");
        int singlecount = ParamUtil.getInt(request, "singlecount");
        int singleper = ParamUtil.getInt(request, "singleper");
        int multicount = ParamUtil.getInt(request, "multicount");
        int multiper = ParamUtil.getInt(request, "multiper");
        int judgecount = ParamUtil.getInt(request, "judgecount");
        int judgeper = ParamUtil.getInt(request, "judgeper");
        int singleTotal = ParamUtil.getInt(request, "singleTotal");
        int multiTotal = ParamUtil.getInt(request, "multiTotal");
        int judgeTotal = ParamUtil.getInt(request, "judgeTotal");
        //试卷新增问答题和多选题计分规则20180824
        int answercount = ParamUtil.getInt(request, "answercount");
        int answerper = ParamUtil.getInt(request, "answerper");
        int answerTotal = ParamUtil.getInt(request, "answerTotal");
        int multiScoreRule = ParamUtil.getInt(request, "multiScoreRule");
        int notAllRightMuntiper = 0;
        if (multiScoreRule != 0) {
            notAllRightMuntiper = ParamUtil.getInt(request, "notAllRightMuntiper");
        }
        starttime = ParamUtil.get(request, "starttime");
        endtime = ParamUtil.get(request, "endtime");
        settime = ParamUtil.get(request, "settime");
        major = ParamUtil.get(request, "major");
        boolean manual = ParamUtil.getInt(request, "isManual", 0) == 1;
        java.util.Date st = DateUtil.parse(starttime, "yyyy-MM-dd HH:mm:ss");
        java.util.Date et = DateUtil.parse(endtime, "yyyy-MM-dd HH:mm:ss");

        if (st == null)
            throw new ErrMsgException("开始时间格式非法！");

        if (et == null)
            throw new ErrMsgException("结束时间格式非法！");

        if (DateUtil.compare(st, et) == 1) {
            throw new ErrMsgException("开始时间不能大于结束时间！");
        }
        // 生成试卷判断分值分布如果在数量大于题库中的数量给出相应提示
        // 查询该科目中单选题数量
        String sql = "select count(id) num from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_SINGLE)) + " and major="
                + StrUtil.sqlstr(major);
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd = (ResultRecord) ri.next();
            int sinCount = rd.getInt("num");
            if (sinCount == 0) {
                errmsg += "题库中没有单选题，请增加\n";
            } else if (singlecount > sinCount) { // 设置的单选题数量大于题库中数量 提示单选题数量不够
                errmsg += "题库中单选题数量不足，请先增加题目或重设分值\n";
            }
        }
        // 查询所选科目题库中多选提数量sql
        sql = "select count(id) num1 from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_MULTI)) + " and major="
                + StrUtil.sqlstr(major);
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd1 = (ResultRecord) ri.next();
            int mulCount = rd1.getInt("num1");
            if (mulCount == 0) { // 题库中数量为0 提示没有多选题
                errmsg += "题库中没有多选题，请增加\n";
            } else if (multicount > mulCount) { // 设置多选题数量大于题库中多选题数量提示不足
                errmsg += "题库中多选题数量不足，请先增加题目或重设分值\n";
            }
        }
        //查询所选题库中判断题的数量sql
        sql = "select count(id) num2 from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_JUDGE)) + " and major="
                + StrUtil.sqlstr(major);
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd2 = (ResultRecord) ri.next();
            int judCount = rd2.getInt("num2");
            if (judCount == 0) {// 题库中判断题数量为0，提示没有判断题
                errmsg += "题库中没有判断题，请增加\n";
            } else if (judgecount > judCount) {//设置数量大于题库中数量提示数量不足
                errmsg += "题库中判断题数量不足，请先增加题目或重设分值\n";
            }
        }
        //查询所选题库中问答题的数量sql
        sql = "select count(id) num3 from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_ANSWER)) + " and major="
                + StrUtil.sqlstr(major);
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd2 = (ResultRecord) ri.next();
            int judCount = rd2.getInt("num3");
            if (judCount == 0) {// 题库中判断题数量为0，提示没有判断题
                errmsg += "题库中没有问答题，请增加\n";
            } else if (judgecount > judCount) {//设置数量大于题库中数量提示数量不足
                errmsg += "题库中问答题数量不足，请先增加题目或重设分值\n";
            }
        }

        title = ParamUtil.get(request, "title");
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        int limitCount = ParamUtil.getInt(request, "limitCount", 1);
        int mode = ParamUtil.getInt(request, "mode", PaperDb.MODE_SPECIFY);

        ptd.setSubject(subject);
        ptd.setTotalper(totalper);
        ptd.setSingleper(singleper);
        ptd.setMultiCount(multicount);
        ptd.setMultiper(multiper);
        ptd.setSingleCount(singlecount);
        ptd.setJudgeCount(judgecount);
        ptd.setJudgeper(judgeper);
        ptd.setStartTime(st);
        ptd.setEndtime(et);
        ptd.setSetTime(DateUtil.parse(settime, "yyyy-MM-dd"));
        ptd.setLimitCount(limitCount);
        ptd.setTitle(title);
        ptd.setTesttime(testtime);
        ptd.setMajor(major);
        ptd.setManual(manual);
        ptd.setSingleTotal(singleTotal);
        ptd.setMultiTotal(multiTotal);
        ptd.setJudgeTotal(judgeTotal);
        ptd.setAnswerCount(answercount);
        ptd.setAnswerper(answerper);
        ptd.setAnswerTotal(answerTotal);
        ptd.setMultiScoreRule(multiScoreRule);
        ptd.setNotAllRightMuntiper(notAllRightMuntiper);
        ptd.setMode(mode);
        re = ptd.create();
        return re;
    }

    public PaperDb getPaperDb(int id) {
        PaperDb ptd = new PaperDb();
        return ptd.getPaperDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        PaperDb ptd = getPaperDb(id);
        if (ptd == null || !ptd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }

        return ptd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException, SQLException {
        boolean re = true;
        String subject = "", errmsg = "", starttime = "", endtime = "", settime = "", major = "";
        int id = ParamUtil.getInt(request, "id");
        int totalper = ParamUtil.getInt(request, "totalper");
        int singlecount = ParamUtil.getInt(request, "singlecount");
        int singleper = ParamUtil.getInt(request, "singleper");
        int multicount = ParamUtil.getInt(request, "multicount");
        int multiper = ParamUtil.getInt(request, "multiper");
        int judgecount = ParamUtil.getInt(request, "judgecount");
        int judgeper = ParamUtil.getInt(request, "judgeper");
        starttime = ParamUtil.get(request, "starttime");
        int testtime = ParamUtil.getInt(request, "testtime");
        int singleTotal = ParamUtil.getInt(request, "singleTotal");
        int multiTotal = ParamUtil.getInt(request, "multiTotal");
        int judgeTotal = ParamUtil.getInt(request, "judgeTotal");
        //试卷新增问答题和多选题计分规则20180824
        int answercount = ParamUtil.getInt(request, "answercount");
        int answerper = ParamUtil.getInt(request, "answerper");
        int answerTotal = ParamUtil.getInt(request, "answerTotal");
        int multiScoreRule = ParamUtil.getInt(request, "multiScoreRule");
        int notAllRightMuntiper = ParamUtil.getInt(request, "notAllRightMuntiper");
        endtime = ParamUtil.get(request, "endtime");
        settime = ParamUtil.get(request, "settime");
        major = ParamUtil.get(request, "major");
        boolean manual = ParamUtil.getInt(request, "isManual") == 1;

        java.util.Date st = DateUtil.parse(starttime, "yyyy-MM-dd HH:mm:ss");
        java.util.Date et = DateUtil.parse(endtime, "yyyy-MM-dd HH:mm:ss");
        if (st == null)
            throw new ErrMsgException("开始时间格式非法！");

        if (et == null)
            throw new ErrMsgException("结束时间格式非法！");

        if (DateUtil.compare(st, et) == 1) {
            throw new ErrMsgException("开始时间不能大于结束时间！");
        }
        // 生成试卷判断分值分布如果在数量大于题库中的数量给出相应提示
        // 查询该科目中单选题数量
        String sql = "select count(id) num from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_SINGLE)) + " and major="
                + StrUtil.sqlstr(major);
        // System.out.println(this.getClass() + "单选题的数量sql:" + sql);
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd = (ResultRecord) ri.next();
            int sinCount = rd.getInt("num");
            // 如果题库中单选题为0，提示没有单选题请增加
            if (sinCount == 0) {
                errmsg += "题库中没有单选题，请增加\n";
            } else if (singlecount > sinCount) { // 设置的单选题数量大于题库中数量 提示单选题数量不够
                errmsg += "题库中单选题数量不足，请先增加题目或重设分值\n";
            }
        }
        // 查询所选科目题库中多选提数量sql
        sql = "select count(id) num1 from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_MULTI)) + " and major="
                + StrUtil.sqlstr(major);
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd1 = (ResultRecord) ri.next();
            int mulCount = rd1.getInt("num1");
            if (mulCount == 0) { // 题库中数量为0 提示没有多选题
                errmsg += "题库中没有多选题，请增加\n";
            } else if (multicount > mulCount) { // 设置多选题数量大于题库中多选题数量提示不足
                errmsg += "题库中多选题数量不足，请先增加题目或重设分值\n";
            }
        }
        //查询所选题库中判断题的数量sql
        sql = "select count(id) num2 from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_JUDGE)) + " and major=" + StrUtil.sqlstr(major);
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd2 = (ResultRecord) ri.next();
            int judCount = rd2.getInt("num2");
            if (judCount == 0) {// 题库中判断题数量为0，提示没有判断题
                errmsg += "题库中没有判断题，请增加\n";
            } else if (judgecount > judCount) {//设置数量大于题库中数量提示数量不足
                errmsg += "题库中判断题数量不足，请先增加题目或重设分值\n";
            }
        }
        //查询所选题库中问答题的数量sql
        sql = "select count(id) num3 from oa_exam_database where exam_type = " + StrUtil.sqlstr(String.valueOf(QuestionDb.TYPE_ANSWER)) + " and major="
                + StrUtil.sqlstr(major);
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            ResultRecord rd2 = (ResultRecord) ri.next();
            int judCount = rd2.getInt("num3");
            if (judCount == 0) {// 题库中判断题数量为0，提示没有判断题
                errmsg += "题库中没有问答题，请增加\n";
            } else if (judgecount > judCount) {//设置数量大于题库中数量提示数量不足
                errmsg += "题库中问答题数量不足，请先增加题目或重设分值\n";
            }
        }
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        String title = ParamUtil.get(request, "title");
        int limitCount = ParamUtil.getInt(request, "limitCount", 1);
        int mode = ParamUtil.getInt(request, "mode", PaperDb.MODE_SPECIFY);

        PaperDb ptd = getPaperDb(id);
        ptd.setSubject(subject);
        ptd.setTotalper(totalper);
        ptd.setSingleper(singleper);
        ptd.setMultiCount(multicount);
        ptd.setMultiper(multiper);
        ptd.setSingleCount(singlecount);
        ptd.setJudgeCount(judgecount);
        ptd.setJudgeper(judgeper);
        ptd.setStartTime(st);
        ptd.setEndtime(et);
        ptd.setSetTime(DateUtil.parse(settime, "yyyy-MM-dd"));
        ptd.setLimitCount(limitCount);
        ptd.setTitle(title);
        ptd.setTesttime(testtime);
        ptd.setMajor(major);
        ptd.setManual(manual);
        ptd.setSingleTotal(singleTotal);
        ptd.setMultiTotal(multiTotal);
        ptd.setJudgeTotal(judgeTotal);
        ptd.setAnswerCount(answercount);
        ptd.setAnswerper(answerper);
        ptd.setAnswerTotal(answerTotal);
        ptd.setMultiScoreRule((multiScoreRule));
        ptd.setNotAllRightMuntiper(notAllRightMuntiper);
        ptd.setMode(mode);
        re = ptd.save();
        return re;
    }
}
