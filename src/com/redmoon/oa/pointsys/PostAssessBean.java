package com.redmoon.oa.pointsys;

/**
 * @Description:
 * @author:
 * @Date: 2016-4-26下午04:49:44
 */
public class PostAssessBean {
	private long flowId;
	private int sumSelfScore;
	private int sumCheckScore;
	private int assessScore;
	private String scoreGrade;

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public int getSumSelfScore() {
		return sumSelfScore;
	}

	public void setSumSelfScore(int sumSelfScore) {
		this.sumSelfScore = sumSelfScore;
	}

	public int getSumCheckScore() {
		return sumCheckScore;
	}

	public void setSumCheckScore(int sumCheckScore) {
		this.sumCheckScore = sumCheckScore;
	}

	public int getAssessScore() {
		return assessScore;
	}

	public void setAssessScore(int assessScore) {
		this.assessScore = assessScore;
	}

	public String getScoreGrade() {
		return scoreGrade;
	}

	public void setScoreGrade(String scoreGrade) {
		this.scoreGrade = scoreGrade;
	}

}