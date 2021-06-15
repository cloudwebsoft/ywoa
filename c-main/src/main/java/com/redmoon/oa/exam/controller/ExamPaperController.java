package com.redmoon.oa.exam.controller;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.exam.PaperDb;
import com.redmoon.oa.exam.PaperManulDb;
import com.redmoon.oa.exam.PaperMgr;
import com.redmoon.oa.exam.PaperQuestionDb;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

/**
 * @Description: 考试模块手工组卷版本试卷管理controler
 * @author: sht
 * @Date: 2018-1-3上午09:41:08
 */
@Controller
@RequestMapping("/exam")
public class ExamPaperController {
	@Autowired
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value="paperAdd" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String paperAdd() throws ResKeyException, JSONException, ErrMsgException, SQLException{
		String title = ParamUtil.get(request, "title");
		String questionIds = ParamUtil.get(request, "questionIds");
		JdbcTemplate jt = new JdbcTemplate();
		PaperMgr pm = new PaperMgr();
		boolean re = pm.create(request);
		JSONObject json = new JSONObject();
		if(re){
			// 获取最新一添加的一条记录，用于更新试卷题目表：oa_exam_paper_question
			String sql = "select id from oa_exam_paper where title ="+StrUtil.sqlstr(title)+" order by id desc limit 1";// 获取试卷表的新增的一条记录id
			int id =0;
			try {
				ResultIterator ri = new ResultIterator();
				ri = jt.executeQuery(sql);
				if(ri.hasNext()){
					ResultRecord rd = (ResultRecord)ri.next();
					id = rd.getInt("id");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			PaperQuestionDb pqd = new PaperQuestionDb();
			String [] str = questionIds.split(",");
			// 循环遍历题目的id,把题目id和试卷id写入试卷题目表（oa_exam_paper_question）
			for(int i=0;i<str.length;i++){
				Object[] obj1 = new Object[]{id,str[i]};
				re = pqd.create(jt,obj1);
			}
			if(re){
				json.put("ret","1");
				json.put("msg", "操作成功");
			}else{
				json.put("ret","0");
				json.put("msg", "操作失败");
			}
		}else {
			json.put("ret","0");
			json.put("msg", "操作失败");
		}
		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value="paperModify" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String paperModify() throws ResKeyException, JSONException, SQLException, ErrMsgException{
		String major = ParamUtil.get(request, "major");
		String title = ParamUtil.get(request, "title");
		int singleCount = ParamUtil.getInt(request, "single_count1");
		int singleper = ParamUtil.getInt(request, "singleper1");
		int multiCount = ParamUtil.getInt(request, "multi_count1");
		int answerCount = ParamUtil.getInt(request, "answer_count1");
		int multiper = ParamUtil.getInt(request, "multiper1");
		int singTotalScore = ParamUtil.getInt(request, "single_totle_score");
		int multiTotalScore = ParamUtil.getInt(request, "multi_totle_score");
		int answerTotalScore = ParamUtil.getInt(request, "answer_totle_score");
		int judgeCount = ParamUtil.getInt(request, "judge_count1");
		int judgeTotalScore = ParamUtil.getInt(request, "judge_totle_score");
		int judgeper = ParamUtil.getInt(request, "judgeper1");
		int answerper = ParamUtil.getInt(request, "answerper1");
		String starttime = ParamUtil.get(request, "starttime");
		String endtime = ParamUtil.get(request, "endtime");
		String testtime = ParamUtil.get(request, "testtime");
		int paperId = ParamUtil.getInt(request, "paperId");
		String questionIds = ParamUtil.get(request, "questionIds");
		int multiScoreRule = ParamUtil.getInt(request, "multiScoreRule");
		int notAllRightMuntiper = ParamUtil.getInt(request, "notAllRightMuntiper");
		int mode = ParamUtil.getInt(request, "mode", PaperDb.MODE_SPECIFY);
		PaperDb pd = new PaperDb();
		pd = pd.getPaperDb(paperId);
		pd.setSingleper(singleper);
		pd.setMultiCount(multiCount);
		pd.setMultiper(multiper);
		pd.setSingleCount(singleCount);
		pd.setJudgeCount(judgeCount);
		pd.setJudgeper(judgeper);
		pd.setStartTime(DateUtil.parse(starttime, "yyyy-MM-dd HH:mm:ss"));
		pd.setEndtime(DateUtil.parse(endtime, "yyyy-MM-dd HH:mm:ss"));
		pd.setTitle(title);
		pd.setTesttime(Integer.parseInt(testtime));
		pd.setMajor(major);
		pd.setSingleTotal(singTotalScore);
		pd.setMultiTotal(multiTotalScore);
		pd.setJudgeTotal(judgeTotalScore);
		pd.setAnswerCount(answerCount);
		pd.setAnswerper(answerper);
		pd.setAnswerTotal(answerTotalScore);
		pd.setMultiScoreRule(multiScoreRule);
		pd.setNotAllRightMuntiper(notAllRightMuntiper);
		pd.setMode(mode);
		boolean re =pd.save();
		JdbcTemplate jt = new JdbcTemplate();
		JSONObject json = new JSONObject();
		if(re){
			// 删除试卷题目表中试卷id为此paperId原有的数据
			String sql = "delete from oa_exam_paper_question where paper_id = "+StrUtil.sqlstr(String.valueOf(paperId));
			jt.executeUpdate(sql);
			PaperQuestionDb pqd = new PaperQuestionDb();
			String [] str = questionIds.split(",");
			// 新增数据到试卷题目表
			for(int i=0;i<str.length;i++){
				Object[] obj1 = new Object[]{paperId,str[i]};
				re = pqd.create(jt,obj1);
			}
			if(re){
				json.put("ret","1");
				json.put("msg", "操作成功");
			}else{
				json.put("ret","0");
				json.put("msg", "操作失败");
			}
		}else {
			json.put("ret","0");
			json.put("msg", "操作失败");
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="paperDel" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String paperDel() throws ResKeyException, JSONException, SQLException, ErrMsgException{
		String paperId = ParamUtil.get(request, "paperId");
		int id= Integer.parseInt(paperId);
		JdbcTemplate jt = new JdbcTemplate();
		PaperManulDb pmd = new PaperManulDb();
		pmd = (PaperManulDb)pmd.getQObjectDb(id);
		boolean re = pmd.del();
		JSONObject json = new JSONObject();
		if(re){
			// 删除题目
			String sql = "delete from oa_exam_paper_question where paper_id = "+StrUtil.sqlstr(paperId);
			int result = jt.executeUpdate(sql);
			if(result>0){
				String delPrivSql =" delete from oa_exam_paper_priv where paper_id =" + StrUtil.sqlstr(paperId);
				jt.executeUpdate(delPrivSql);
				json.put("ret","1");
				json.put("msg", "操作成功");
			}else{
				json.put("ret","0");
				json.put("msg", "操作失败");
			}
		}else {
			json.put("ret","0");
			json.put("msg", "操作失败");
		}
		return json.toString();
	}
}
