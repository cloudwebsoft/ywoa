package com.redmoon.oa.pointsys;

import java.util.HashMap;

/**
 * @Description:
 * @author:
 * @Date: 2016-3-28下午06:00:44
 */
public class PointBean {
	private HashMap<String, Integer> eduMap;
	private HashMap<Integer, Integer> postMap;
	private int scoreEmployed;
	private int scoreFixed;
	private int scoreEmployedInit;
	private boolean scoreAssessInit = false;
	private boolean postInit = false;

	public HashMap<String, Integer> getEduMap() {
		return eduMap;
	}

	public void setEduMap(HashMap<String, Integer> eduMap) {
		this.eduMap = eduMap;
	}

	public HashMap<Integer, Integer> getPostMap() {
		return postMap;
	}

	public void setPostMap(HashMap<Integer, Integer> postMap) {
		this.postMap = postMap;
	}

	public int getScoreEmployed() {
		return scoreEmployed;
	}

	public void setScoreEmployed(int scoreEmployed) {
		this.scoreEmployed = scoreEmployed;
	}

	public int getScoreFixed() {
		return scoreFixed;
	}

	public void setScoreFixed(int scoreFixed) {
		this.scoreFixed = scoreFixed;
	}

	public int getScoreEmployedInit() {
		return scoreEmployedInit;
	}

	public void setScoreEmployedInit(int scoreEmployedInit) {
		this.scoreEmployedInit = scoreEmployedInit;
	}

	public boolean isScoreAssessInit() {
		return scoreAssessInit;
	}

	public void setScoreAssessInit(boolean scoreAssessInit) {
		this.scoreAssessInit = scoreAssessInit;
	}

	public boolean isPostInit() {
		return postInit;
	}

	public void setPostInit(boolean postInit) {
		this.postInit = postInit;
	}

}
