package com.redmoon.oa.pointsys;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.post.*;
import com.redmoon.oa.visual.FormDAO;

/**
 * @Description:
 * @author:
 * @Date: 2016-3-16上午10:40:11
 */
public class PointSystemUtil {
	private final static String IS_SCORE = "1";
	public final static int SCORE_CHANGED_REPORT = 1;
	public final static int SCORE_CHANGED_ASSESS = 2;
	public final static int SCORE_CHANGED_USED = 3;

	private PostAssessBean postAssessBean;

	public PostAssessBean getPostAssessBean() {
		return postAssessBean;
	}

	public void setPostAssessBean(PostAssessBean postAssessBean) {
		this.postAssessBean = postAssessBean;
	}

	/**
	 * 
	 * @Description:
	 * @param userName
	 * @param year
	 * @param month
	 *            从1开始
	 * @return
	 */
	public static void calcuAllUserScore() {
		// 取得配置表
		PointBean pb = getPointInit();

		java.util.Date curDate = new java.util.Date();
		int p_year = DateUtil.getYear(curDate);
		int p_mon = DateUtil.getMonth(curDate) + 1;
		// if (p_mon == 1) {
		// p_mon = 12;
		// }

		// 如果加分有延迟，下月审批完了上月的积分，则需要在事件中重新计算
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from form_table_personbasic where zzqk='1' or zzqk is null";
		FormDAO fdao = new FormDAO();
		Iterator ir = null;
		try {
			ir = fdao.list("personbasic", sql).iterator();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				String userName = StrUtil.getNullStr(fdao
						.getFieldValue("user_name"));

				if (userName.equals("")) {
					continue;
				}
				try {
					onCalcUserScore(userName, pb, p_year, p_mon, fdao);
				} catch (SQLException e) {
					e.printStackTrace();
					LogUtil.getLog("PointSystemUtil").error(
							"calcuAllUserScore:" + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog("PointSystemUtil").error(
					"calcuAllUserScore:" + e.getMessage());
		} finally {
			jt.close();
		}
	}

	public static void calcuUnInitUserScore() {
		// 取得配置表
		PointBean pb = getPointInit();

		java.util.Date curDate = new java.util.Date();
		int p_year = DateUtil.getYear(curDate);
		int p_mon = DateUtil.getMonth(curDate) + 1;
		// if (p_mon == 1) {
		// p_mon = 12;
		// }

		// 如果加分有延迟，下月审批完了上月的积分，则需要在事件中重新计算
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from form_table_personbasic where (zzqk='1' or zzqk is null) and user_name not in (select user_name from form_table_pointsys_score_mon where cws_status=1 and p_year="
				+ p_year + " and p_mon=" + p_mon + ")";
		FormDAO fdao = new FormDAO();
		Iterator ir = null;
		try {
			ir = fdao.list("personbasic", sql).iterator();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				String userName = StrUtil.getNullStr(fdao
						.getFieldValue("user_name"));

				if (userName.equals("")) {
					continue;
				}
				try {
					onCalcUserScore(userName, pb, p_year, p_mon, fdao);
				} catch (SQLException e) {
					e.printStackTrace();
					LogUtil.getLog("PointSystemUtil").error(
							"calcuAllUserScore:" + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog("PointSystemUtil").error(
					"calcuAllUserScore:" + e.getMessage());
		} finally {
			jt.close();
		}
	}

	private static void onCalcUserScore(String userName, PointBean pb,
			int p_year, int p_mon, FormDAO fdao) throws SQLException {
		UserDb ud = new UserDb(userName);
		if (ud == null || !ud.isLoaded()) {
			return;
		}

		JdbcTemplate jt = new JdbcTemplate();
		String sql = "";

		int scoreEmployed = 0;
		int scoreEdu = 0;
		int scorePost = 0;

		int scoreA = 0;

		int post = StrUtil.toInt(fdao.getFieldValue("job_level"), 0);
		String education = fdao.getFieldValue("education");

		if (post == 0) {
			PostUserMgr puMgr = new PostUserMgr();
			puMgr.setUserName(userName);
			PostUserDb puDb = puMgr.postByUserName();
			if (puDb != null && puDb.isLoaded()) {
				post = puDb.getInt("post_id");

				// 更新人员基本信息表的岗位
				sql = "update form_table_personbasic set job_level=? where user_name=?";
				jt.executeUpdate(sql, new Object[] { post, userName });
			}
		}

		// 工龄分
		java.util.Date employed = DateUtil.parse(
				fdao.getFieldValue("employed"), "yyyy-MM-dd");

		int years = DateUtil.datediff(new java.util.Date(), employed) / 365;
		if (years > 0) {
			scoreEmployed = pb.getScoreEmployedInit() + (years - 1)
					* pb.getScoreEmployed();
		}

		// 学历分
		if (pb.getEduMap().containsKey(education)) {
			scoreEdu = pb.getEduMap().get(education);
		}

		// 上月绩效考核分
		PostAssessBean pab = null;
		if (pb.isScoreAssessInit()) {
			int month = p_mon;
			int year = p_year;
			if (month == 1) {
				year--;
				month = 12;
			} else {
				month--;
			}
			pab = getAssessScore(userName, year, month);
		}

		// 岗位分
		if (pb.isPostInit()) {
			if (pb.getPostMap().containsKey(post))
				scorePost = pb.getPostMap().get(post);
		}

		// A分=工龄分+学历分+岗位分+上月绩效分
		scoreA = scoreEmployed + scoreEdu + scorePost
				+ (pab == null ? 0 : pab.getAssessScore());

		// B分值
		int scoreB = 0;
		// B分加分值
		int scoreBPlus = 0;
		// B分扣分值
		int scoreBMinus = 0;

		sql = "select sum(score) from form_table_score_reported where is_plus=1 and cws_status=1 and target="
				+ StrUtil.sqlstr(userName)
				+ " and "
				+ SQLFilter.year("cur_time")
				+ "="
				+ p_year
				+ " and "
				+ SQLFilter.month("cur_time") + "=" + p_mon;
		ResultIterator ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			scoreBPlus = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
		}

		sql = "select sum(score) from form_table_score_reported where is_plus=0 and cws_status=1 and target="
				+ StrUtil.sqlstr(userName)
				+ " and "
				+ SQLFilter.year("cur_time")
				+ "="
				+ p_year
				+ " and "
				+ SQLFilter.month("cur_time") + "=" + p_mon;
		ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			scoreBMinus = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
		}

		// b分=固定分+加分-减分
		scoreB = pb.getScoreFixed() + scoreBPlus - scoreBMinus;
		int scoreMon = scoreA + scoreB;

		// 已使用的积分
		int used = 0;
		sql = "select sum(score) from form_table_integration_exchan where cws_status=1 and user_name="
				+ StrUtil.sqlstr(userName);
		ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			used = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
		}

		sql = "insert into form_table_pointsys_score_mon (flowTypeCode,cws_status,unit_code,p_year,p_mon,user_name,point_b_plus,point_b_minus, point_a, point_b,score_mon,score_fixed,score_edu,score_job,score_employed,assess_score,used_point,sum_self_score,sum_check_score,score_grade)"
				+ "	values ('-1',1,"
				+ StrUtil.sqlstr(ud.getUnitCode())
				+ ","
				+ p_year
				+ ","
				+ p_mon
				+ " ,"
				+ StrUtil.sqlstr(userName)
				+ ","
				+ scoreBPlus
				+ ","
				+ scoreBMinus
				+ ","
				+ scoreA
				+ ","
				+ scoreB
				+ ","
				+ scoreMon
				+ ","
				+ pb.getScoreFixed()
				+ ","
				+ scoreEdu
				+ ","
				+ scorePost
				+ ","
				+ scoreEmployed
				+ ","
				+ (pab == null ? 0 : pab.getAssessScore())
				+ ","
				+ used
				+ ","
				+ (pab == null ? 0 : pab.getSumSelfScore())
				+ ","
				+ (pab == null ? 0 : pab.getSumCheckScore())
				+ ","
				+ StrUtil.sqlstr((pab == null ? "" : pab.getScoreGrade())) + ")";

		jt.executeUpdate(sql);

		int sumA = 0, sumB = 0;
		sql = "select sum(point_a),sum(point_b) from form_table_pointsys_score_mon where cws_status=1 and user_name="
				+ StrUtil.sqlstr(userName);
		ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			sumA = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
			sumB = (rr.getInt(2) == -1 ? 0 : rr.getInt(2));
		}

		int sumAll = sumA + sumB - used;
		sql = "update form_table_pointsys_score_mon set score_all=" + sumAll
				+ " where cws_status=1 and user_name="
				+ StrUtil.sqlstr(userName);
		jt.executeUpdate(sql);
	}

	/**
	 * 
	 * @Description:
	 * @param userName
	 * @param year
	 * @param month
	 *            从0开始
	 */
	public void refreshUser(String userName, int year, int month,
			int changeScore, int changeType) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from form_table_pointsys_score_mon where cws_status=1 and user_name=? and p_year=? and p_mon=?";
		ResultIterator ri = null;
		boolean isExist = false;
		try {
			ri = jt.executeQuery(sql, new Object[] { userName,
					new Integer(year), new Integer(month) });
			if (ri.hasNext()) {
				isExist = true;
			}
			sql = "select job_level,education, employed from form_table_personbasic where user_name=?";
			ri = jt.executeQuery(sql, new Object[] { userName });

			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();

				int post = StrUtil.toInt(rr.getString("job_level"), 0);
				String education = rr.getString("education");
				java.util.Date employed = DateUtil.parse(rr
						.getString("employed"), "yyyy-MM-dd");

				if (post == 0) {
					PostUserMgr puMgr = new PostUserMgr();
					puMgr.setUserName(userName);
					PostUserDb puDb = puMgr.postByUserName();
					if (puDb != null && puDb.isLoaded()) {
						post = puDb.getInt("post_id");

						// 更新人员基本信息表的岗位
						sql = "update form_table_personbasic set job_level=? where user_name=?";
						jt.executeUpdate(sql, new Object[] { post, userName });
					}
				}

				PointBean pb = getPointInit();

				// 工龄分
				java.util.Date curDate = DateUtil.getDate(year, month, 30);
				int years = DateUtil.datediff(curDate, employed) / 365;
				int scoreEmployed = 0;
				if (years > 0) {
					scoreEmployed = pb.getScoreEmployedInit() + (years - 1)
							* pb.getScoreEmployed();
				}

				// 学历分
				int scoreEdu = 0;
				if (pb.getEduMap().containsKey(education)) {
					scoreEdu = pb.getEduMap().get(education);
				}

				// 上月绩效考核分
				PostAssessBean pab = null;
				if (pb.isScoreAssessInit()) {
					int p_mon = month;
					int p_year = year;
					if (p_mon == 1) {
						p_year--;
						p_mon = 12;
					} else {
						p_mon--;
					}
					// 如果是绩效流程回写,直接引用表单中的值
					pab = (changeType == SCORE_CHANGED_ASSESS ? postAssessBean
							: getAssessScore(userName, p_year, p_mon));
				}

				// 岗位分
				int scorePost = 0;
				if (pb.isPostInit()) {
					if (pb.getPostMap().containsKey(post)) {
						scorePost = pb.getPostMap().get(post);
					}
				}

				// A分=工龄分+学历分+岗位分+上月绩效分
				int scoreA = scoreEmployed + scoreEdu + scorePost
						+ (pab == null ? 0 : pab.getAssessScore());

				// B分值
				int scoreB = 0;
				// B分加分值
				int scoreBPlus = 0;
				// B分扣分值
				int scoreBMinus = 0;

				sql = "select sum(score) from form_table_score_reported where cws_status=1 and is_plus=1 and target="
						+ StrUtil.sqlstr(userName)
						+ " and "
						+ SQLFilter.year("cur_time")
						+ "="
						+ year
						+ " and "
						+ SQLFilter.month("cur_time") + "=" + month;
				ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					rr = (ResultRecord) ri.next();
					scoreBPlus = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
				}

				// 在onfinish方法中调用时,表单的cws_status为0,无法查询到,故直接引用表单中的值,如果是减分,changeScore为负数
				if (changeType == SCORE_CHANGED_REPORT && changeScore > 0) {
					scoreBPlus += changeScore;
				}

				sql = "select sum(score) from form_table_score_reported where cws_status=1 and is_plus=0 and target="
						+ StrUtil.sqlstr(userName)
						+ " and "
						+ SQLFilter.year("cur_time")
						+ "="
						+ year
						+ " and "
						+ SQLFilter.month("cur_time") + "=" + month;
				ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					rr = (ResultRecord) ri.next();
					scoreBMinus = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
				}

				// 在onfinish方法中调用时,表单的cws_status为0,无法查询到,故直接引用表单中的值,如果是减分,changeScore为负数
				if (changeType == SCORE_CHANGED_REPORT && changeScore < 0) {
					scoreBMinus -= changeScore;
				}

				scoreB = pb.getScoreFixed() + scoreBPlus - scoreBMinus;

				// 已使用的积分
				int used = 0;
				sql = "select sum(score) from form_table_integration_exchan where cws_status=1 and user_name="
						+ StrUtil.sqlstr(userName);
				ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					rr = (ResultRecord) ri.next();
					used = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
				}

				// 在onfinish方法中调用时,表单的cws_status为0,无法查询到,故直接引用表单中的值
				if (changeType == SCORE_CHANGED_USED) {
					used += changeScore;
				}

				int scoreMon = scoreA + scoreB;
				UserDb ud = new UserDb(userName);

				if (isExist) {
					sql = "update form_table_pointsys_score_mon set unit_code=?, point_b_plus=?, point_b_minus=?, point_a=?, point_b=?,score_mon=?,score_fixed=?,score_edu=?,score_job=?,score_employed=?,assess_score=?,used_point=?,sum_self_score=?,sum_check_score=?,score_grade=? where user_name="
							+ StrUtil.sqlstr(userName)
							+ " and p_year="
							+ year
							+ " and p_mon=" + month;
					jt.executeUpdate(sql, new Object[] { ud.getUnitCode(),
							scoreBPlus, scoreBMinus, scoreA, scoreB, scoreMon,
							pb.getScoreFixed(), scoreEdu, scorePost,
							scoreEmployed,
							(pab == null ? 0 : pab.getAssessScore()), used,
							(pab == null ? 0 : pab.getSumSelfScore()),
							(pab == null ? 0 : pab.getSumCheckScore()),
							(pab == null ? "" : pab.getScoreGrade()) });
				} else {
					sql = "insert into form_table_pointsys_score_mon (flowTypeCode,cws_status,unit_code,p_year,p_mon,user_name,point_b_plus,point_b_minus, point_a, point_b,score_mon,score_fixed,score_edu,score_job,score_employed,assess_score,used_point,sum_self_score,sum_check_score,score_grade)"
							+ "	values ('-1',1,"
							+ StrUtil.sqlstr(ud.getUnitCode())
							+ ","
							+ year
							+ ","
							+ month
							+ " ,"
							+ StrUtil.sqlstr(ud.getUnitCode())
							+ ","
							+ scoreBPlus
							+ ","
							+ scoreBMinus
							+ ","
							+ scoreA
							+ ","
							+ scoreB
							+ ","
							+ scoreMon
							+ ","
							+ pb.getScoreFixed()
							+ ","
							+ scoreEdu
							+ ","
							+ scorePost
							+ ","
							+ scoreEmployed
							+ ","
							+ (pab == null ? 0 : pab.getAssessScore())
							+ ","
							+ used
							+ ","
							+ (pab == null ? 0 : pab.getSumSelfScore())
							+ ","
							+ (pab == null ? 0 : pab.getSumCheckScore())
							+ ","
							+ StrUtil.sqlstr((pab == null ? "" : pab.getScoreGrade())) + ")";
					jt.executeUpdate(sql);
				}

				int sumA = 0, sumB = 0;
				sql = "select sum(point_a),sum(point_b) from form_table_pointsys_score_mon where cws_status=1 and user_name="
						+ StrUtil.sqlstr(userName);
				ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					rr = (ResultRecord) ri.next();
					sumA = (rr.getInt(1) == -1 ? 0 : rr.getInt(1));
					sumB = (rr.getInt(2) == -1 ? 0 : rr.getInt(2));
				}

				int sumAll = sumA + sumB - used;
				sql = "update form_table_pointsys_score_mon set score_all="
						+ sumAll + " where cws_status=1 and user_name="
						+ StrUtil.sqlstr(userName);
				jt.executeUpdate(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error("refreshUser:" + e.getMessage());
		}
	}

	private static HashMap<String, Integer> getEducationInit() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		FormDAO fdao = new FormDAO();
		String sql = "select id from form_table_pointsys_education";
		try {
			Iterator ir = fdao.list("pointsys_education", sql).iterator();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				map.put(String.valueOf(fdao.getId()), StrUtil.toInt(fdao
						.getFieldValue("score"), 0));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog("PointSystemUtil").error(
					"getEducationInit:" + e.getMessage());
		}

		return map;
	}

	private static HashMap<Integer, Integer> getPostLevelInit() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		FormDAO fdao = new FormDAO();
		String sql = "select id from form_table_pointsys_post where cws_status=1";
		try {
			Iterator ir = fdao.list("pointsys_post", sql).iterator();
			while (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				int post = StrUtil.toInt(fdao.getFieldValue("post"), 0);
				if (post > 0) {
					map
							.put(post, StrUtil.toInt(fdao
									.getFieldValue("score"), 0));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog("PointSystemUtil").error(
					"getPostLevelInit:" + e.getMessage());
		}

		return map;
	}

	public static PostAssessBean getAssessScore(String userName, int year,
			int month) {
		PostFlowMgr pfMgr = new PostFlowMgr();
		ArrayList<String> list = pfMgr.listCanUserStartFlow(userName);
		Iterator<String> it = list.iterator();
		if (it.hasNext()) {
			String flowCode = it.next();
			Leaf leaf = new Leaf(flowCode);
			if (leaf == null || !leaf.isLoaded()) {
				return null;
			}

			String sql = "select id from form_table_" + leaf.getFormCode()
					+ " where cws_status=1 and curr_name="
					+ StrUtil.sqlstr(userName) + " and year=" + year
					+ " and month=" + month + " order by flowId desc";
			FormDAO fdao = new FormDAO();
			try {
				Iterator ir = fdao.list(leaf.getFormCode(), sql).iterator();
				if (ir.hasNext()) {
					FormDAO dao = (FormDAO) ir.next();
					if (dao != null && dao.isLoaded()) {
						PostAssessBean pab = new PostAssessBean();
						pab.setFlowId(dao.getFlowId());
						pab.setAssessScore(StrUtil.toInt(dao
								.getFieldValue("assess_score"), 0));
						pab.setSumSelfScore(StrUtil.toInt(dao
								.getFieldValue("sum_self_score"), 0));
						pab.setSumCheckScore(StrUtil.toInt(dao
								.getFieldValue("sum_check_score"), 0));
						pab.setScoreGrade(StrUtil.getNullStr(dao
								.getFieldValue("score_grade")));
						return pab;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.getLog("PointSystemUtil").error(
						"getAssessScore:" + e.getMessage());
			}
		}
		return null;
	}

	public static PointBean getPointInit() {
		PointBean pb = new PointBean();
		String sql = "select id from form_table_pointsys";
		FormDAO fdao = new FormDAO();
		try {
			Iterator ir = fdao.list("pointsys", sql).iterator();
			if (ir.hasNext()) {
				fdao = (FormDAO) ir.next();
				// 工龄每年加分
				pb.setScoreEmployed(StrUtil.toInt(fdao
						.getFieldValue("score_employed"), 0));
				// 每月固定加分
				pb.setScoreFixed(StrUtil.toInt(fdao
						.getFieldValue("score_fixed"), 0));
				// 工龄初始分
				pb.setScoreEmployedInit(StrUtil.toInt(fdao
						.getFieldValue("score_employed_init"), 0));
				// 是否关联考核成绩
				pb.setScoreAssessInit(StrUtil.getNullStr(
						fdao.getFieldValue("score_assess_init")).equals(
						IS_SCORE));
				// 是否关联岗位
				pb.setPostInit(StrUtil.getNullStr(
						fdao.getFieldValue("post_init")).equals(IS_SCORE));
			}
			pb.setEduMap(getEducationInit());
			pb.setPostMap(getPostLevelInit());
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog("PointSystemUtil").error(
					"getPointInit:" + e.getMessage());
		}
		return pb;
	}
}
