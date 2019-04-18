package com.redmoon.forum.security;

import com.cloudwebsoft.framework.base.QObjectDb;

public class QuizDb extends QObjectDb{
	 public QuizDb getQuizDb(int id) {
		return (QuizDb) getQObjectDb(new Integer(id));
	}
}
