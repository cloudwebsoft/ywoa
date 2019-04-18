package com.redmoon.forum.security;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;

import com.cloudwebsoft.framework.base.QObjectBlockIterator;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.SequenceMgr;

public class QuizMgr {
	public QuizMgr() {
		
    }
	public boolean create(HttpServletRequest request) throws ErrMsgException {
		boolean re = false;
		QuizDb qd = new QuizDb();
		int id = (int) SequenceMgr.nextID(SequenceMgr.SQ_REGIST_QUIZ);
		String question= ParamUtil.get(request, "quizQuestion");
		String answer= ParamUtil.get(request, "quizAnswer");
		try {
			re = qd.create(new JdbcTemplate(), new Object[] {
				new Integer(id),
				question,
				answer
			});
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		return re;
	}
	public boolean save(HttpServletRequest request) throws ErrMsgException,ResKeyException{
		boolean re = false;
		QuizDb qd = new QuizDb();
		int id = ParamUtil.getInt(request, "id",0);
		if(id==0){
			return re;
		}
		String question= ParamUtil.get(request, "quizQuestion");
		String answer= ParamUtil.get(request, "quizAnswer");
		qd = qd.getQuizDb(id);
		qd.set("question", question);
		qd.set("answer", answer);
		re = qd.save();
		return re;
	}
	public boolean del(HttpServletRequest request) throws ErrMsgException,ResKeyException{
		boolean re = false;
		QuizDb qd = new QuizDb();
		int id = ParamUtil.getInt(request, "id",0);
		if(id == 0){
			return re;
		}
		qd = qd.getQuizDb(id);
		re = qd.del();
		return re;
	}
	
	public QuizDb getRandomQuizDb() {
		QuizDb qd = new QuizDb();
		String sql = qd.getTable().getQueryList();
		int count = (int)qd.getQObjectCount(sql);
		int random = NumberUtil.random(0, count);
		QObjectBlockIterator qir = qd.getQObjects(sql, 0, count);
		int c = 0;
		while (qir.hasNext()) {
			qd = (QuizDb)qir.next();
			if (c==random)
				return qd;
			c++;
		}
		return null;
	}
}
