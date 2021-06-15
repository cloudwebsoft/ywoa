<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%
    String ids = request.getParameter("questionId");
    String selQuestionSql = "select id from oa_exam_database where id in (" + ids + ")";
    QuestionDb qd = new QuestionDb();
    Vector v = qd.list(selQuestionSql);
    Iterator ir = null;
    if (v != null)
        ir = v.iterator();
    int i = 0;
    while (ir != null && ir.hasNext()) {
        i++;
        qd = (QuestionDb) ir.next();
        if (qd.getType() == QuestionDb.TYPE_SINGLE || qd.getType() == QuestionDb.TYPE_MULTI) {
            QuestionSelectDb qsdb = new QuestionSelectDb();
%>
<table class="tabStyle_1 percent98" id="<%=qd.getId() %>">
    <tr>
        <td align="left" colspan="2">
            <img id="plus_<%=qd.getId() %>" src="../forum/images/plus.gif" style="cursor: pointer;width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
            <img id="minus_<%=qd.getId() %>" class="question" src="../forum/images/minus.gif" style="cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=qd.getId()%>
        </td>
        <%
            if (qd.getType() == QuestionDb.TYPE_SINGLE) {%>
        <td align="right"><input type="button" value="删除" onclick="delQ('<%=qd.getId() %>','tab_<%=qd.getId() %>>',0)"/></td>
        <%
        } else {%>
        <td align="right"><input type="button" value="删除" onclick="delQ('<%=qd.getId() %>','tab_<%=qd.getId() %>>',1)"/></td>
        <%
            }
        %>
    </tr>
    <tr>
        <td colspan="3"><%=qd.getQuestion().replaceAll("</?[^/?(img)][^><]*>", "") %>
        </td>
    </tr>
    <%
        QuestionSelectDb qsd = new QuestionSelectDb();
        String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(qd.getId()));
        Iterator optionIt = qsd.list(selSelectOptionSql).iterator();
        int k = 0;

        while (optionIt.hasNext()) {
            int o = (int) 'A';
            o = o + k;
            qsd = (QuestionSelectDb) optionIt.next();
    %>
    <tr id="q<%=k+1 %>_<%=qd.getId() %>" class="question">
        <td>&nbsp;&nbsp;<%=(char) o %>、</td>
        <td colspan="2"><%=qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", "") %>
        </td>
    </tr>
    <%
            k++;
        }
    %>
    <tr id="q<%=k+1 %>_<%=qd.getId() %>" class="question">
        <td width="60">&nbsp;&nbsp;答案</td>
        <td colspan="3">
            <%
                if (qd.getType() == QuestionDb.TYPE_SINGLE) {
                    QuestionSelectDb qsdSingle = (QuestionSelectDb) qsdb.getQObjectDb(qd.getAnswer());
                    if (qsdSingle != null) {
            %>
            <%=(char) ((int) 'A' + qsdSingle.getInt("orders"))%>
            <%
                }
            } else if (qd.getType() == QuestionDb.TYPE_MULTI) {
                String[] answerStr = qd.getAnswer().split(",");
                String answerShow = "";
                for (int j = 0; j < answerStr.length; j++) {
                    QuestionSelectDb qsdSingle = (QuestionSelectDb) qsdb.getQObjectDb(answerStr[j]);
                    if (qsdSingle != null) {
                        if ("".equals(answerShow)) {
                            answerShow = String.valueOf((char) ((int) 'A' + qsdSingle.getInt("orders")));
                        } else {
                            answerShow += "," + String.valueOf((char) ((int) 'A' + qsdSingle.getInt("orders")));
                        }
                    }
                }
            %>
            <%=answerShow%>
            <%}%>
        </td>
    </tr>
</table>
<%} else if (qd.getType() == QuestionDb.TYPE_JUDGE || qd.getType() == QuestionDb.TYPE_ANSWER) {%>
<table class="tabStyle_1 percent98" id="<%=qd.getId() %>">
    <tr>
        <td align="left" colspan="2">
            <img id="plus_<%=qd.getId() %>" src="../forum/images/plus.gif" style="cursor: pointer;width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
            <img id="minus_<%=qd.getId() %>" class="question" src="../forum/images/minus.gif" style="cursor: pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=qd.getId()%>
        </td>
        <td align="right"><input type="button" value="删除" onclick="delQ('<%=qd.getId() %>','tab_<%=qd.getId() %>>',<%=qd.getType()%>)"/></td>
    </tr>
    <tr>
        <td colspan="4"><%=StrUtil.getAbstract(request, qd.getQuestion(), 1000).replaceAll("</?[^/?(img)][^><]*>", "") %>
        </td>
    </tr>
    <tr id="q_<%=qd.getId() %>" class="question">
        <td width="60">&nbsp;&nbsp;答案</td>
        <td colspan="3">
            <%
                if (qd.getType() == QuestionDb.TYPE_ANSWER) {
                    out.print(qd.getAnswer().replaceAll("</?[^/?(img)][^><]*>", ""));
                }
                else {
                    if ("n".equals(qd.getAnswer())) {
                        out.print("错误");
                    }
                    else {
                        out.print("正确");
                    }
                }
            %>
        </td>
    </tr>
</table>
<%
        }
    }
%>
