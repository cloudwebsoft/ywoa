package com.redmoon.oa.exam.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectMgr;
import com.redmoon.oa.exam.ExamFileUpdate;
import com.redmoon.oa.exam.PaperDb;
import com.redmoon.oa.exam.PaperQuestionDb;
import com.redmoon.oa.exam.QuestionDb;
import com.redmoon.oa.exam.QuestionSelectDb;

/**
 * @Description: 考试模块人工组卷版本题库管理controller
 * @author: sht
 * @Date: 2017-12-21下午04:19:28
 */
@Controller
@RequestMapping("/question")
public class ExamQuestionController {
	@Autowired
	private HttpServletRequest request;
	
	/*
	 * 题库管理添加题目方法
	 */
	@ResponseBody
	@RequestMapping(value="addQ" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String addQ() throws SQLException, JSONException, ErrMsgException, ResKeyException{
		int selectCouont = ParamUtil.getInt(request, "count",0);
		JSONObject selectJson = new JSONObject();
		String type  = ParamUtil.get(request, "type");
		String major  = ParamUtil.get(request, "major");
		String title  = ParamUtil.get(request, "title");
		// System.out.println(getClass() + "获取题目："+ title);
		String answer = ParamUtil.get(request, "answer");
		QuestionDb qd = new QuestionDb();
		qd.setType(Integer.parseInt(type));
		qd.setMajor(major);
		qd.setAnswer(answer);
		qd.setQuestion(title);
		int result = qd.create1();
		int j = 0;
		for(int i = (int)'A'; i < 'A' + selectCouont; i++){
			// System.out.println(getClass() + "获得选项：" + ParamUtil.get(request, "choose"+(char)i));
			selectJson.put("choose"+(char)i, ParamUtil.get(request, "choose"+(char)i));
			QuestionSelectDb qsd = new QuestionSelectDb();
			Object [] params = {result, ParamUtil.get(request, "choose"+(char)i), j};
			qsd.create(new JdbcTemplate(), params);
			j++;
		}
		// 如果题目类型是单选题或者多选题更新题目答案为选项的id，多选题用逗号分隔
		if(type.equals(String.valueOf(QuestionDb.TYPE_SINGLE)) || type.equals(String.valueOf(QuestionDb.TYPE_MULTI))){
			QuestionSelectDb selOptionDb = new QuestionSelectDb();
			String seleOptionIdSql = "";
			if(type.equals(String.valueOf(QuestionDb.TYPE_SINGLE))){
				seleOptionIdSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(result)) + " and orders = " + answer;
			}else{
				String [] order = answer.split(",");
				String orderStr = "";
				for(int i = 0; i< order.length; i++){
					if(orderStr.equals("")){
						orderStr = StrUtil.sqlstr(order[i]);
					}else{
						orderStr += "," + StrUtil.sqlstr(order[i]);
					}
				}
				seleOptionIdSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(result)) + " and orders in(" + answer + ")";
			}
			
			Iterator seleOptionIdIt = selOptionDb.list(seleOptionIdSql).iterator();
			String optionId = "";
			while(seleOptionIdIt.hasNext()){
				selOptionDb = (QuestionSelectDb)seleOptionIdIt.next();
				if("".equals(optionId)){
					optionId = selOptionDb.getString("id");
				}else{
					optionId += "," + selOptionDb.getString("id");
				}
			}
			qd.setAnswer(optionId);
			qd.save();
		}
		// System.out.println(getClass() + " 写入题目的Id：" + result);
		JSONObject json = new JSONObject();
		if(result > 0){
			List list = new ArrayList();
			list.add(title);
			Iterator iterator = selectJson.keys();
			while(iterator.hasNext()){
			     String key = (String) iterator.next();
			     // System.out.println(getClass() + " " + selectJson.getString(key));
			     list.add (selectJson.getString(key));
			}
			// 实例化html解析对象
			ExamFileUpdate efu = new ExamFileUpdate();
			for(int i=0; i < list.size(); i++){
				// 调用对比方法，传入content和题目id
				efu.updateExamFile(list.toString(), result);
			}
			json.put("ret", 1);
			json.put("msg", "操作成功,请继续添加");
		}else{
			json.put("ret", 0);
			json.put("msg", "操作失败");
		}
		return json.toString();
	}
	
	/*
	 * 题库管理删除题目方法
	 */
	@ResponseBody
	@RequestMapping(value="delQ" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String delQ() throws SQLException, JSONException, ErrMsgException{
		JSONObject json = new JSONObject();
		int id = ParamUtil.getInt(request, "questionId");
		String sql = "select id from oa_exam_paper_question where question_id = "+id;
		PaperQuestionDb pqd = new PaperQuestionDb();
		Vector v = pqd.list(sql);
		Iterator ir = v.iterator();
		String paperids = "";
		String paperTitles ="";
		boolean re =false;
		while(ir.hasNext()){
			pqd = (PaperQuestionDb)ir.next();
			if(paperids.equals("")){
				paperids = pqd.getString("paper_id");
			}else {
				paperids += ","+pqd.getString("paper_id");
			}
		}
		if(!paperids.equals("")){
			String paperId[] = paperids.split(",");
			for(int i=0;i<paperId.length;i++){
				PaperDb pd = new PaperDb();
				pd = pd.getPaperDb(Integer.parseInt(paperId[i]));
				if(paperTitles.equals("")){
					paperTitles = pd.getTitle();
				}else{
					paperTitles += "，"+pd.getTitle();
				}
			}
			json.put("ret", "0");
			json.put("msg", "此题目已在试卷：“"+paperTitles+"”中被使用，不能删除");
		}else{
			QuestionDb qd = new QuestionDb();
			qd = (QuestionDb)qd.getQuestionDb(id);
			qd.setIsValid(1);
			re = qd.save();
			if(re){
				json.put("ret", "1");
				json.put("msg", "删除成功");
			}else{
				json.put("ret", "0");
				json.put("msg", "删除失败");
			}
		}
		return json.toString();
	}
	/*
	 * 题库管理修改题目方法
	 */
	@ResponseBody
	@RequestMapping(value="editQ" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String editQ() throws SQLException, JSONException, ErrMsgException{
		
		String op = ParamUtil.get(request, "op");
		String questionId = ParamUtil.get(request, "questionId");
		int selectCouont = ParamUtil.getInt(request, "count", 0);
		String answer = ParamUtil.get(request, "answer");
		String question = ParamUtil.get(request, "question");
		int id = Integer.parseInt(questionId);
		JSONObject selectJson = new JSONObject();
		JSONObject json = new JSONObject();
		String sql = "select id from oa_exam_paper_question where question_id = " + id;
		PaperQuestionDb pqd = new PaperQuestionDb();
		Vector v = pqd.list(sql);
		if(v.size() > 0){
			json.put("ret", 0);
			json.put("msg", "改题目已被试卷使用，不能修改");
		}else{
			if("single".equals(op) || "multi".equals(op)){ // 如果是单选题或者多选题 先删除选项表中的数据然后新建
				int j = 0;
				for(int i = (int)'A'; i < 'A' + selectCouont; i++){
					selectJson.put("choose"+(char)i, ParamUtil.get(request, "choose"+(char)i));
					String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(questionId) + " and orders = " + j;
					QuestionSelectDb qsd = new QuestionSelectDb();
					Iterator optionIterator = qsd.list(selSelectOptionSql).iterator();
					if(optionIterator.hasNext()){
						qsd = (QuestionSelectDb)optionIterator.next();
						qsd.set("content", ParamUtil.get(request, "choose"+(char)i));
						try {
							qsd.save();
						} catch (ResKeyException e) {
							e.printStackTrace();
						}
					}else{
						Object [] params = {questionId,ParamUtil.get(request, "choose"+(char)i),j};
						try {
							qsd.create(new JdbcTemplate(), params);
						} catch (ResKeyException e) {
							e.printStackTrace();
						}
					}
					j++;
				}
			}
			QuestionDb qd = new QuestionDb();
			qd = qd.getQuestionDb(id);
			qd.setQuestion(question);
			String newAnswer = "";
			// 如果题目类型是单选题或者多选题更新题目答案为选项的id，多选题用逗号分隔
			if(qd.getType() == QuestionDb.TYPE_SINGLE || qd.getType() == QuestionDb.TYPE_MULTI){
				QuestionSelectDb selOptionDb = new QuestionSelectDb();
				String seleOptionIdSql = "";
				if(qd.getType() == QuestionDb.TYPE_SINGLE){
					seleOptionIdSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(questionId) + " and orders = " + answer;
				}else{
					String [] order = answer.split(",");
					String orderStr = "";
					for(int i = 0; i< order.length; i++){
						if(orderStr.equals("")){
							orderStr = StrUtil.sqlstr(order[i]);
						}else{
							orderStr += "," + StrUtil.sqlstr(order[i]);
						}
					}
					seleOptionIdSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(questionId) + " and orders in(" + answer + ")";
				}
				System.out.println(getClass() + "sql:" + seleOptionIdSql);
				Iterator seleOptionIdIt = selOptionDb.list(seleOptionIdSql).iterator();
				while(seleOptionIdIt.hasNext()){
					selOptionDb = (QuestionSelectDb)seleOptionIdIt.next();
					if("".equals(newAnswer)){
						newAnswer = selOptionDb.getString("id");
					}else{
						newAnswer += "," + selOptionDb.getString("id");
					}
				}
				answer = newAnswer;
			}
			qd.setAnswer(answer);
			boolean re = qd.save();
			if(re){
				List list = new ArrayList();
				list.add(question);
				Iterator iterator = selectJson.keys();
				while(iterator.hasNext()){
				     String key = (String) iterator.next();
				     list.add (selectJson.getString(key));
				}
				//实例化html解析对象
				ExamFileUpdate efu = new ExamFileUpdate();
				for(int i=0;i<list.size();i++){
					// 调用对比方法，传入content和题目id
					efu.updateExamFile(list.toString(), id);
				}
				json.put("ret", 1);
				json.put("msg", "操作成功");
			}else{
				json.put("ret", 0);
				json.put("msg", "操作失败");
			}
		}
		return json.toString();
	}
	/*
	 * 题目类型添加controller
	 * 
	 */
	@ResponseBody
	@RequestMapping(value="questionKindAdd" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String questionKindAdd() throws JSONException{
		
		String code = ParamUtil.get(request, "code");
		String name = ParamUtil.get(request, "name");
		String parentCode = ParamUtil.get(request, "parent_code").trim();
		String discription = ParamUtil.get(request, "description");
		JSONObject json = new JSONObject();
		TreeSelectMgr tm = new TreeSelectMgr();
		boolean re = false;
		try {
			//判断名称是否有重复
			Vector children = tm.getChildren(parentCode);
			Iterator ri = children.iterator();
			while (ri.hasNext()) {
				TreeSelectDb childlf = (TreeSelectDb) ri.next();
				String oldName = childlf.getName();
				if (oldName.equals(name)) {
					json.put("ret", "2");
					json.put("msg", "请检查名称是否有重复!");
					return json.toString();
				}
			}
			//增加节点
			TreeSelectDb tsd = new TreeSelectDb(parentCode);
			TreeSelectDb ctsd = new TreeSelectDb();
			ctsd = ctsd.getTreeSelectDb(code);
			ctsd.setName(name);
			ctsd.setCode(code);
			ctsd.setDescription(discription);
			ctsd.setParentCode(parentCode);
			re = tsd.AddChild(ctsd);
			if (re) {
				json.put("ret", "1");
				json.put("msg", "添加成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "添加失败!");
			}
		} catch (ErrMsgException e) {
			
		}
		return json.toString();
	}
	
	/*
	 * 题目类型修改controller
	 * 
	 */
	@ResponseBody
	@RequestMapping(value="questionKindEdit" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String questionKindEdit() throws JSONException, SQLException{
		
		String code = ParamUtil.get(request, "code");
		String name = ParamUtil.get(request, "name");
		String discription = ParamUtil.get(request, "description");
		JSONObject json = new JSONObject();
		if (name == null || name == "") {
            json.put("ret", "0");
            json.put("msg", "名称不能为空!");
            return json.toString();
        }
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(code);
        leaf.setName(name);
        leaf.setDescription(discription);
        boolean re = false;
        re = leaf.save();
        if(re){
        	json.put("ret", "1");
            json.put("msg", "修改成功!");
        }else{
        	json.put("ret", "0");
            json.put("msg", "修改失败!");
        }
		return json.toString();
	}
	@ResponseBody
	@RequestMapping(value="questionKindDel" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String questionKindDel() throws JSONException, ErrMsgException, SQLException{
		
		JSONObject json = new JSONObject();
		String delcode = ParamUtil.get(request, "delcode");
		boolean re;
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb(delcode);
		re = tsd.del();
		if(re){
			String delChileSql = "delete from oa_tree_select where parentCode = "+StrUtil.sqlstr(delcode);
			String delPrivSql = "delete from oa_exam_major_priv where major_code = "+StrUtil.sqlstr(delcode);
			JdbcTemplate jt = new JdbcTemplate();
			jt.executeUpdate(delChileSql);
			jt.executeUpdate(delPrivSql);
		}
		if(re){
			json.put("ret", "1");
            json.put("msg", "删除成功!");
		}else{
			json.put("ret", "0");
            json.put("msg", "删除失败!");
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="moveNode" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String moveNode(){
		JSONObject json = new JSONObject();
		try {
			String code = ParamUtil.get(request, "code");
			String parentCode = ParamUtil.get(request, "parentCode");
			int position = Integer.parseInt(ParamUtil.get(request, "position"));
			if ("exam_major".equals(code)) {
				json.put("ret", "0");
				json.put("msg", "根节点不能移动！");
				return json.toString();
			}
			if ("#".equals(parentCode)) {
				json.put("ret", "0");
				json.put("msg", "不能与根节点平级！");
				return json.toString();
			}

			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDb(code);
			int oldPosition = tsd.getOrders();//得到被移动节点原来的位置
			String oldParentCode = tsd.getParentCode();
			TreeSelectDb newParentLeaf = new TreeSelectDb();
			newParentLeaf = newParentLeaf.getTreeSelectDb(parentCode);
			int p = position + 1;
			tsd.setOrders(p);
			if (!parentCode.equals(oldParentCode)) {
				tsd.save(parentCode);
			} else {
				tsd.save();
			}
			// 重新梳理orders
			Iterator ir = newParentLeaf.getChildren().iterator();
			TreeSelectDb childLeaf = new TreeSelectDb();
			while (ir.hasNext()) {
				childLeaf = (TreeSelectDb) ir.next();
				// 跳过自己
				if (childLeaf.getCode().equals(code)) {
					continue;
				}
				if (p < oldPosition) {//上移
					if (childLeaf.getOrders() >= p) {
						childLeaf.setOrders(childLeaf.getOrders() + 1);
						childLeaf.save();
					}
				} else {//下移
					if (childLeaf.getOrders() <= p && childLeaf.getOrders() > oldPosition) {
						childLeaf.setOrders(childLeaf.getOrders() - 1);
						childLeaf.save();
					}
				}
			}
			json.put("ret", "1");
			json.put("msg", "操作成功！");
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="deleteQuestion" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String deleteQuestion() throws SQLException{
		String idStr = ParamUtil.get(request, "idstr");
		String [] ids = idStr.split(",");
		JSONObject json = new JSONObject();
		boolean re = false;
		for(int i = 0; i < ids.length; i++){
			QuestionDb qd = new QuestionDb();
			qd = qd.getQuestionDb(Integer.parseInt(ids[i]));
			try {
				re = qd.del();
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}
			if(qd.getType() == QuestionDb.TYPE_SINGLE || qd.getType() == QuestionDb.TYPE_JUDGE){
				String delSelectOptionSql = "delete from oa_exam_database_option where question_id = " + StrUtil.sqlstr(ids[i]);
				JdbcTemplate jt = new JdbcTemplate();
				jt.executeUpdate(delSelectOptionSql);
			}
		}
		try {
			if (re) {
				json.put("ret", "1");
				json.put("msg", "删除成功");
			} else {
				json.put("ret", "0");
				json.put("msg", "删除失败");
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	
	@ResponseBody
	@RequestMapping(value="recoveryQuestion" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String recoveryQuestion() throws SQLException{
		String idStr = ParamUtil.get(request, "idstr");
		String [] ids = idStr.split(",");
		JSONObject json = new JSONObject();
		try {
			boolean re = false;
			for (int i = 0; i < ids.length; i++) {
				QuestionDb qd = new QuestionDb();
				qd = qd.getQuestionDb(Integer.parseInt(ids[i]));
				qd.setIsValid(QuestionDb.IS_VALID);
				try {
					re = qd.save();
				} catch (ErrMsgException e) {
					e.printStackTrace();
				}
			}
			if (re) {
				json.put("ret", "1");
				json.put("msg", "恢复成功");
			} else {
				json.put("ret", "0");
				json.put("msg", "恢复失败");
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="isCanEdit" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String isCanEdit() throws ErrMsgException{
		JSONObject json = new JSONObject();
		try {
			int id = ParamUtil.getInt(request, "questionId");
			String sql = "select id from oa_exam_paper_question where question_id = " + id;
			PaperQuestionDb pqd = new PaperQuestionDb();
			Vector v = pqd.list(sql);
			Iterator ir = v.iterator();
			String paperids = "";
			String paperTitles = "";
			while (ir.hasNext()) {
				pqd = (PaperQuestionDb) ir.next();
				if (paperids.equals("")) {
					paperids = pqd.getString("paper_id");
				} else {
					paperids += "," + pqd.getString("paper_id");
				}
			}
			if (!paperids.equals("")) {
				String paperId[] = paperids.split(",");
				for (int i = 0; i < paperId.length; i++) {
					PaperDb pd = new PaperDb();
					pd = pd.getPaperDb(Integer.parseInt(paperId[i]));
					if (pd.isLoaded()) {
						if (paperTitles.equals("")) {
							paperTitles = pd.getTitle();
						} else {
							paperTitles += "," + pd.getTitle();
						}
					}
				}
				json.put("ret", "0");
				json.put("msg", "此题目已在试卷：“" + paperTitles + "”中被使用，不能修改");
			} else {
				json.put("ret", "1");
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
}
