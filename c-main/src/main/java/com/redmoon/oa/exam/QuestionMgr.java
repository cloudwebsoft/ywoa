package com.redmoon.oa.exam;
import bsh.This;
import cn.js.fan.util.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class QuestionMgr {
    Logger logger = Logger.getLogger(QuestionMgr.class.getName());

    public QuestionMgr() {

    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        QuestionDb qtd = new QuestionDb();
        // subject,type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,mark
        boolean re = true;
        String subject = "", chooseB = "", chooseA = "", chooseC = "",
               chooseD = "", question ="",chooseE="",chooseF="";
        String errmsg = "";
        subject = ParamUtil.get(request, "subject");
        System.out.println(this.getClass()+"  那到了科目"+subject);
        int type = ParamUtil.getInt(request, "type");
        question = ParamUtil.get(request, "question");
        chooseA = ParamUtil.get(request, "chooseA");
        chooseB = ParamUtil.get(request, "chooseB");
        chooseC = ParamUtil.get(request, "chooseC");
        chooseD = ParamUtil.get(request, "chooseD");
        chooseE = ParamUtil.get(request, "chooseE");
        chooseF = ParamUtil.get(request, "chooseF");
        String[] str = request.getParameterValues("answer");
        if (str == null) throw new ErrMsgException("请选择答案");
        int len = str.length;
        String answerStr = "";
            for (int i = 0; i < len; i++) {
                str[i] = StrUtil.UnicodeToUTF8(str[i]);
                if (answerStr.equals(""))
                    answerStr = str[i];
                else
                    answerStr += "," + str[i];
           }
        if (question.equals(""))
            errmsg.equals("标题不能为空");
        if (chooseA.equals(""))
            errmsg.equals("选项不能为空");
        if (answerStr.equals(""))
            errmsg.equals("答案不能为空");
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
         qtd.setSubject(subject);
         qtd.setType(type);
         qtd.setQuestion(question);
         qtd.setChooseA(chooseA);
         qtd.setChooseB(chooseB);
         qtd.setChooseC(chooseC);
         qtd.setChooseD(chooseD);
         qtd.setChooseE(chooseE);
         qtd.setChooseF(chooseF);
         qtd.setAnswer(answerStr);
         re = qtd.create();
        return re;
    }
    public QuestionDb getQuestionDb(int id) {
     QuestionDb qtd = new QuestionDb();
     return qtd.getQuestionDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        QuestionDb qtd = getQuestionDb(id);
        if (qtd == null || !qtd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }

        //BookDb bd = new BookDb();
        //if (bd.hasBookOfquestion(id)) {
        //     String info = SkinUtil.LoadString(request, "res.module.book", "warn_question_del_hasbook");
        //    throw new ErrMsgException(info);
        // }

        return qtd.del();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        boolean re = true;
        String subject = "", chooseB = "", chooseA = "", chooseC = "",
               chooseD = "", question ="",chooseE="",chooseF="",answer="";
        String errmsg = "";
        int id = ParamUtil.getInt(request,"id");
        subject = ParamUtil.get(request, "subject");
        int type = ParamUtil.getInt(request, "type");
        question = ParamUtil.get(request, "question");
        chooseA = ParamUtil.get(request, "chooseA");
        chooseB = ParamUtil.get(request, "chooseB");
        chooseC = ParamUtil.get(request, "chooseC");
        chooseD = ParamUtil.get(request, "chooseD");
        chooseE = ParamUtil.get(request, "chooseE");
        chooseF = ParamUtil.get(request, "chooseF");
        answer = ParamUtil.get(request, "answer");
        if (question.equals(""))
            errmsg += "问题不能为空！";
        if (answer.equals(""))
            errmsg += "答案不能为空！";
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        QuestionDb qtd = getQuestionDb(id);
        qtd.setSubject(subject);
        qtd.setType(type);
        qtd.setQuestion(question);
        qtd.setChooseA(chooseA);
        qtd.setChooseB(chooseB);
        qtd.setChooseC(chooseC);
        qtd.setChooseD(chooseD);
        qtd.setChooseE(chooseE);
        qtd.setChooseF(chooseF);
        qtd.setAnswer(answer);
        re = qtd.save();
        return re;

    }


}
