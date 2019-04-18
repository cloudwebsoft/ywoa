package com.redmoon.oa.android.score;



import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.pointsys.PointBean;
import com.redmoon.oa.pointsys.PointSystemUtil;
import com.redmoon.oa.post.PostDb;
import com.redmoon.oa.post.PostUserDb;
import com.redmoon.oa.post.PostUserMgr;
import com.redmoon.oa.visual.FormDAO;




/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-15下午04:41:52
 */
public class MyScoreAction extends BaseAction{
	private final static String POINTSYS_SCORE_MON = "pointsys_score_mon";  //每月积分得分表
	private final static String PERSONBASIC= "personbasic"; //人员基本信息表
	private String skey;//用户名
	private int month = -1;
	private int year = -1;
	
	
	
	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}


	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}


	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}


	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}


	/**
	 * @return the skey
	 */
	public String getSkey() {
		return skey;
	}


	/**
	 * @param skey the skey to set
	 */
	public void setSkey(String skey) {
		this.skey = skey;
	}

	
	@Override
	public void executeAction() {
		// TODO Auto-generated method stub
		super.executeAction();
		com.redmoon.oa.android.Privilege mPriv = new com.redmoon.oa.android.Privilege();
		boolean re = mPriv.Auth(getSkey());
		
		
		try {
			jReturn.put(RES, RESULT_SUCCESS);
			if(re){
				jResult.put(RETURNCODE, RESULT_TIME_OUT);
				jReturn.put(RESULT, jResult);
				return;
			}
			Calendar cal = Calendar.getInstance();
			if(year == -1){
				year = cal.get(Calendar.YEAR);
			}
			if(month == -1){
				month = cal.get(Calendar.MONTH) + 1;
			}
			String userName = mPriv.getUserName(skey);
			String sql = "select id from form_table_pointsys_score_mon where cws_status=1 and user_name=" + StrUtil.sqlstr(userName) + " and p_year=" + year + " and p_mon=" + month;
			FormDAO fdao = new FormDAO();
			FormDAO pfdao = new FormDAO();
			try {
				Vector v = fdao.list(POINTSYS_SCORE_MON, sql);
				Iterator it = v.iterator();
				if (it.hasNext()) {
					fdao = (FormDAO) it.next();
				}
			} catch (Exception e) {
			}

			sql = "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(userName);
			try {
				Vector v = pfdao.list(PERSONBASIC, sql);
				Iterator it = v.iterator();
				if (it.hasNext()) {
					pfdao = (FormDAO) it.next();
				}
			} catch (Exception e) {
			}
			
			if (fdao == null || !fdao.isLoaded()) {
				jResult.put(RETURNCODE, RESULT_NO_DATA);
			}else{
				JSONObject data = new JSONObject();
				String point_a = fdao.getFieldValue("point_a"); //积分A
				data.put("point_a", point_a);
				Date date = DateUtil.parse(pfdao.getFieldValue("employed"), "yyyy-MM-dd");
				int employed = DateUtil.datediff(new Date(), date) / 365;//工龄
				data.put("employed", employed);
				String score_employed = fdao.getFieldValue("score_employed");//工龄积分
				data.put("score_employed", score_employed);
				int eduId = StrUtil.toInt(StrUtil.getNullStr(pfdao.getFieldValue("education")), 0);
				if (eduId > 0) {
					FormDb fd = new FormDb("pointsys_education");
					FormDAO edudao = new FormDAO(eduId, fd);
					if (edudao.isLoaded()) {
						String education = edudao.getFieldValue("education");//学历
						data.put("education", education);
					}
				}
				String score_edu = fdao.getFieldValue("score_edu") ;//学历积分
				data.put("score_edu", score_edu);
				PointBean pb = PointSystemUtil.getPointInit();
				if (pb.isPostInit()){
					PostUserMgr puMgr = new PostUserMgr();
					puMgr.setUserName(userName);
					PostUserDb pudb = puMgr.postByUserName();
					if (pudb != null && pudb.isLoaded()) {
						PostDb pdb = new PostDb();
						pdb = pdb.getPostDb(pudb.getInt("post_id"));
						if (pdb != null && pdb.isLoaded()) {
							String post_name = pdb.getString("name"); //岗位名称
							data.put("post_name", post_name);
						}
					}
					String score_job = fdao.getFieldValue("score_job");//岗位积分
					data.put("score_job", score_job);
				}
					if (pb.isScoreAssessInit()) {
						String assess_score = fdao.getFieldValue("assess_score");
						data.put("assess_score", assess_score);
						
					}
					String point_b = fdao.getFieldValue("point_b");//B分
					data.put("point_b", point_b);
					String score_fixed = fdao.getFieldValue("score_fixed");//B分固定分
					data.put("score_fixed", score_fixed);
					String point_b_plus = fdao.getFieldValue("point_b_plus");//奖分
					data.put("point_b_plus", point_b_plus);
					String point_b_minus = fdao.getFieldValue("point_b_minus");//扣分
					data.put("point_b_minus", point_b_minus);
					String used_point = fdao.getFieldValue("used_point");//已兑换积分
					data.put("used_point", used_point);
					String score_mon = fdao.getFieldValue("score_mon");//月积分
					data.put("score_mon", score_mon);
					String score_all = fdao.getFieldValue("score_all");//累计积分
					data.put("score_all", score_all);
					jResult.put(DATA, data);
					jResult.put(RETURNCODE, RESULT_SUCCESS);
					
				
				
				
				
			}
			
		
			jReturn.put(RESULT, jResult);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(MyScoreAction.class.getName()+":"+e.getMessage());
		}
		
	} 

	
}
