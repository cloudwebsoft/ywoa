<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.net.*"%>
<%
    /*
    - 功能描述：请假申请单的检查
    - 访问规则：从flow_dispose.jsp中通过include script访问
    - 过程描述：
    - 注意事项：
    - 创建者：fgf
    - 创建时间：2013-05-12
    ==================
    - 修改者：qcg
    - 修改时间：2016-12
    - 修改原因: 使流程所有节点都显示请假情况
    - 修改点:
    */
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setHeader("X-Content-Type-Options", "nosniff");

    String pageType = ParamUtil.get(request, "pageType");
    if ("moduleList".equals(pageType) || pageType.contains("show")) {
        response.setContentType("text/html;charset=utf-8");
        return;
    }

    Privilege pvg = new Privilege();
    String op = ParamUtil.get(request, "op");

    String formCode = "qjsqd";
    FormValidatorConfig fvc = new FormValidatorConfig();
    LeaveFormValidator ifv = (LeaveFormValidator) fvc.getIFormValidatorOfForm(formCode);
    String formName = ParamUtil.get(request, "cwsFormName");

    if (op.equals("getLeaveCount")) {
        response.setContentType("text/html;charset=utf-8");
        String leaveType = URLDecoder.decode(ParamUtil.get(request, "leaveType"), "utf-8");
        String userName = URLDecoder.decode(ParamUtil.get(request, "userName"), "utf-8");
        String qjkssj = ParamUtil.get(request, "qjkssj");
        JSONObject json = new JSONObject();
        if (qjkssj.equals("")) {
            json.put("ret", "0");
            json.put("msg", "请填写开始日期！");
            out.print(json);
            return;
        }
        java.util.Date d = DateUtil.parse(qjkssj, "yyyy-MM-dd");
        int year = DateUtil.getYear(d);
        double count = LeaveMgr.getLeaveCount(year, userName, leaveType);
        json.put("ret", "1");
        json.put("leaveCount", "" + NumberUtil.round(count, 1));
        //out.println("-------"+ifv.isCheckNJ);
        if (ifv.isCheckNJ() && leaveType.equals("年假")) {
            int days = 0;

            // 取得工作年限
            UserDb user = new UserDb();
            user = user.getUserDb(userName);
            String idCard = StrUtil.getNullStr(user.getIDCard());

            if (idCard.equals("")) {
                // throw new ErrMsgException("用户" + userName + "，姓名为：" + user.getRealName() + "，其用户信息中的身份证未填写！");
                json.put("ret", "0");
                json.put("msg", "用户" + userName + "，姓名为：" + user.getRealName() + "，其用户信息中的身份证未填写！");
                out.print(json);
                return;
            }

            //根据登录账户获取工作时间
            // UserInfoDb uad = new UserInfoDb();
            // uad = uad.getUserInfoDb(userName);
            java.util.Date workDate = null;
            JdbcTemplate jt = new JdbcTemplate();
            String sql = "select * from ft_personbasic where user_name = ?";
            ResultIterator ri = jt.executeQuery(sql, new Object[]{userName});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                String date = rr.getString("cjgzsj");
                workDate = DateUtil.parse(date + "-01", "yyyy-MM-dd");
                if (workDate == null) {
                    // throw new ErrMsgException("用户" + user.getRealName() + "档案信息中的参加工作时间未填写！");
                    json.put("ret", "0");
                    json.put("msg", "用户" + user.getRealName() + "人员信息中的参加工作时间未填写！");
                    out.print(json);
                    return;
                }
            }

            // java.util.Date workDate = DateUtil.parse(uad.getJoinWorkDate() + "-01", "yyyy-MM-dd");
            if (workDate == null) {
                // throw new ErrMsgException("用户" + user.getRealName() + "档案信息中的参加工作时间未填写！");
                json.put("ret", "0");
                json.put("msg", "用户" + user.getRealName() + " 人员信息中的参加工作时间未填写！");
                out.print(json);
                return;
            }

            int workDateYear = DateUtil.getYear(workDate);
            int workDateMonth = DateUtil.getMonth(workDate);

            int ksMonth = DateUtil.getMonth(d);
            int ksYear = DateUtil.getYear(d);

            // 计算工作满多少整年
            int y = year - workDateYear;
            if (ksMonth < workDateMonth) {
                y--;
            }

            if (y >= 1 && y < 10) {
                // "职工累计工作已满1年不满10年的，年休假5天！");
                days = 5;
            }
            if (y >= 10 && y < 20) {
                // "职工累计工作已满10年不满20年的，年休假10天，您当前已休" + njDayCount + "天！"
                days = 10;
            }
            if (y >= 20) {
                days = 15;
                // throw new ErrMsgException("职工累计工作已满20年的，年休假15天！"
            }
            json.put("defaultCount", "" + days);
        }

        json.put("msg", "操作成功！");
        out.print(json);
        return;
    }

    response.setContentType("text/javascript;charset=utf-8");
%>

$(document).ready(function() {
    var jqlbObj = fo("jqlb");
    // 假期类别如果被选择了
    $(jqlbObj).change(function() {
    	if (fo("qjkssj").value=="") {
            fo("desc").innerHTML = "请填写开始日期！";
            fo("desc").style.margin="5px";
        }
        else {
            getDayCount($(this).val());
        }
    });
<%
    long myActionId = ParamUtil.getLong(request, "myActionId", -1);
    if (myActionId!=-1) {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);

        int flowId = (int) mad.getFlowId();
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
        fdao = fdao.getFormDAO(flowId, fd);
        String jqlb = fdao.getFieldValue("jqlb");

        if (!"leave_type".equals(jqlb)) {
            %>
            // 使全部的流程节点都显示请假情况
            getDayCount("<%=jqlb%>");
            <%
        }
    }
	%>

    var oldValue = fo("qjkssj").value;
    var setInt = setInterval(function(){
        var formName = '<%=formName%>';
        if (o(formName) {
            if (oldValue != fo("qjkssj").value) {
                if (fo("qjkssj").value!="") {
                    if (jqlbObj.value=="") {
                        fo("desc").innerHTML = "请选择假期类别！";
                        fo("desc").style.margin="5px";
                    }
                    else {
                        getDayCount(fo("jqlb").value);
                    }
                }

                oldValue = fo("qjkssj").value;
            }
        }
    },500);
    getCurFormUtil().addInterval(setInt, '<%=formName%>');
});

function getDayCount(jqlb) {
    var ajaxData = {
        op: "getLeaveCount",
        leaveType: encodeURI(jqlb),
        userName: encodeURI(o('applier').value),
        qjkssj: o('qjkssj').value
    }
    ajaxPost('/flow/form_js/form_js_qjsqd.jsp', ajaxData).then((data) => {
        console.log('data', data);
        if (data.ret=="1") {
            // myMsg(data.msg);
            if (data.ret=="1") {
            	if (<%=ifv.isCheckNJ()%> && jqlb=="年假") {
	                o("desc").innerHTML = "年假总天数为：" + data.defaultCount + "天，本年度已请" + jqlb + "：" + data.leaveCount + "天，剩余：" + (parseFloat(data.defaultCount)-parseFloat(data.leaveCount)) + "天";
                }
                else {
                	o("desc").innerHTML = "本年度已请" + jqlb + "：" + data.leaveCount + "天";
                }
                o("desc").style.margin="5px";
            }
            else {
                myMsg(data.msg, 'error');
            }
        } else {
            myMsg(data.msg, 'error');
        }
    });
    <%--$.ajax({
        type: "post",
        url: "<%=request.getContextPath()%>/flow/form_js/form_js_qjsqd.jsp",
        data: {
            op: "getLeaveCount",
            leaveType: encodeURI(jqlb),
            userName: encodeURI(o('applier').value),
            qjkssj: o('qjkssj').value
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest) {
        },
        success: function(data, status) {
            data = $.parseJSON(data);
            if (data.ret=="1") {
            	if (<%=ifv.isCheckNJ()%> && jqlb=="年假") {
	                o("desc").innerHTML = "年假总天数为：" + data.defaultCount + "天，本年度已请" + jqlb + "：" + data.leaveCount + "天，剩余：" + (parseFloat(data.defaultCount)-parseFloat(data.leaveCount)) + "天";
                }
                else {
                	o("desc").innerHTML = "本年度已请" + jqlb + "：" + data.leaveCount + "天";
                }
                o("desc").style.margin="5px";
            }
            else {
				// jAlert(data.msg, "提示");
				$.toaster({priority : 'info', message : data.msg });
            }
        },
        complete: function(XMLHttpRequest, status){
        },
        error: function(XMLHttpRequest, textStatus){
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
    });--%>
}
