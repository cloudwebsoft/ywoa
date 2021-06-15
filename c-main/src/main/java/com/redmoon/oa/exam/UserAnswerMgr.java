package com.redmoon.oa.exam;

import cn.js.fan.util.*;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

public class UserAnswerMgr {
    Logger logger = Logger.getLogger(UserAnswerMgr.class.getName());

    public UserAnswerMgr() {

    }

    public boolean create(int scoreId, int questionId, String userAnswer, int isCorrect) throws ErrMsgException {
        UserAnswerDb utd = new UserAnswerDb();
        boolean re = true;
        utd.setScoreId(scoreId);
        utd.setUserAnswer(userAnswer);
        utd.setQuestionId(questionId);
        utd.setIsCorrect(isCorrect);
        re = utd.create();
        return re;
    }

    public UserAnswerDb getUserAnswerDb(int id) {
        UserAnswerDb utd = new UserAnswerDb();
        return utd.getUserAnswerDb(id);
    }
}
