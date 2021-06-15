package com.redmoon.oa.exam.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.exam.PaperMgr;
import com.redmoon.oa.exam.PaperQuestionDb;
import com.redmoon.oa.exam.ScoreDb;
import com.redmoon.oa.exam.ScoreMgr;
import com.redmoon.oa.exam.UserAnswerDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2018-8-31下午03:26:04
 */
@Controller
@RequestMapping("/examScore")
public class ExamScoreController {
	@Autowired
	HttpServletRequest request;
	@ResponseBody
	@RequestMapping(value="answerCheck" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String paperAdd() throws ResKeyException, JSONException, ErrMsgException, SQLException{
		JSONObject json = new JSONObject();
		int scoreId = ParamUtil.getInt(request, "scoreId");
		ScoreMgr sm = new ScoreMgr();
		int newScore = 0;
		String remarks = ParamUtil.get(request, "remarks");
		ScoreDb sd = new ScoreDb();
		sd = sd.getScoreDb(scoreId);
		String idStr = ParamUtil.get(request, "idstr");
		String [] ids = idStr.split(",");
		if(sd.isLoaded()){
			int notHasAnswerScore = sm.getScoreNotHasAnswer(scoreId);
			newScore += notHasAnswerScore;
			if(!"".equals(idStr)){
				for(int i = 0; i < ids.length; i++ ){
					newScore += ParamUtil.getInt(request, "answer_"+ids[i]);
				}
			}
			sd.setScore(newScore);
			sd.setRemarks(remarks);
			boolean re = sd.save();
			if(re){
				// 更新用户的答案表 问答题成绩is_correct为3表示已经评分，2表示问答题未评分，1表示答题正确 0表示错误
				if(!"".equals(idStr)){
					for(int i = 0; i < ids.length; i++ ){
						int score = ParamUtil.getInt(request, "answer_"+ids[i]);
						UserAnswerDb uad = new UserAnswerDb();
						uad = uad.getUserAnswerDb(Integer.parseInt(ids[i]));
						uad.setScore(score);
						uad.setIsCorrect(UserAnswerDb.ANSWER_CHECKED);
						uad.save();
					}
				}
				json.put("ret", "1");
				json.put("msg", "评分成功，得分为 " + sd.getScore() );
			}else {
				json.put("ret", "0");
				json.put("msg", "评分失败");
			}
		}else{
			json.put("ret", "0");
			json.put("msg", "数据库出错");
		}
		return json.toString();
	}
}
