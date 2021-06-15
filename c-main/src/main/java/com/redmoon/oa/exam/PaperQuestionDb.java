package com.redmoon.oa.exam;

import com.cloudwebsoft.framework.base.QObjectDb;

import java.util.Vector;

/**
 * @Description: 
 * @author: 
 * @Date: 2018-2-2下午03:23:13
 */
public class PaperQuestionDb extends QObjectDb {
	public PaperQuestionDb(){
		super();
	}

	public Vector listOfPaper(int paperId) {
		String sql = "select id from oa_exam_paper_question where paper_id = " + paperId;
		return list(sql);
	}
}
