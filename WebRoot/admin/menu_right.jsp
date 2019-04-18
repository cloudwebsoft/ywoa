<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="org.jdom.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>菜单管理-bottom</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script>
        function form1_onsubmit() {
            if (form1.preCode.value == "module") {
                if (form1.formCode.value == "") {
                    jAlert("请选择模块！", "提示");
                    return false;
                }
            }
            if (form1.preCode.value == "flow") {
                if (form1.flowTypeCode.value == "" || form1.flowTypeCode.value == "not") {
                    jAlert("请选择流程！", "提示");
                    return false;
                }
            }
        }

        function selIcon(icon) {
            o("icon").value = icon;
            o("iconDiv").innerHTML = "<img src='<%=request.getContextPath()%>/<%=skinPath%>/icons/" + icon + "'>";
        }

        function selBigIcon(icon) {
            o("bigIcon").value = icon;
            o("bigIconDiv").innerHTML = "<img src='<%=request.getContextPath()%>/images/bigicons/" + icon + "'>";
        }
    </script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String action = ParamUtil.get(request, "action");
    if (action.equals("AddChild")) {
        boolean re = false;
        try {
            Directory dir = new Directory();
            re = dir.AddChild(request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        if (re) {
            String addCode = ParamUtil.get(request, "code").trim();
%>
<script>
    window.parent.leftFrame.location = "menu_tree.jsp?nodeSelected=<%=StrUtil.UrlEncode(addCode)%>";
    window.location.href = "menu_right.jsp?op=modify&code=<%=StrUtil.UrlEncode(addCode)%>";
</script>
<%
    }
    return;
} else if (action.equals("modify")) {
    boolean re = true;
    try {
        Directory dir = new Directory();
        re = dir.update(request);
    } catch (ErrMsgException e) {
        out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        return;
    }
    if (re) {
        String code = ParamUtil.get(request, "code").trim();
        String name = ParamUtil.get(request, "name").trim();
        boolean isUse = ParamUtil.getInt(request, "isUse", 0) == 1;
        out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "edit_success"), "提示", "menu_right.jsp?op=modify&code=" + StrUtil.UrlEncode(code)));
%>
<script>
    window.parent.leftFrame.setNode("<%=code%>", <%=isUse%>, "<%=name%>");
    // window.parent.leftFrame.location = "menu_tree.jsp?nodeSelected=<%=StrUtil.UrlEncode(code)%>";
</script>
<%
            return;
        }
    }

    String parent_code = ParamUtil.get(request, "parent_code");
    if (parent_code.equals(""))
        parent_code = "root";
    Leaf lfParent = new Leaf();
    lfParent = lfParent.getLeaf(parent_code);
    if (lfParent == null) {
        out.print(SkinUtil.makeErrMsg(request, "父节点不存在！"));
        return;
    } else {
        if (lfParent.getLayer() >= 4) {
            out.print(SkinUtil.makeErrMsg(request, "菜单不能超过3级！"));
            return;
        }
    }
    String parent_name = lfParent.getName();
    String code = ParamUtil.get(request, "code");
    String name = ParamUtil.get(request, "name");
    String link = ParamUtil.get(request, "link");
    int width = ParamUtil.getInt(request, "width", 60);
    boolean isWidget = false;
    int widgetWidth = 200;
    int widgetHeight = 100;
    String pvg = "", icon = "", bigIcon = "";
    boolean isHome = false;
    int type = 0;

    String op = ParamUtil.get(request, "op");
    if (op.equals("")) {
        op = "AddChild";
    }

    if (op.equals("AddChild")) {
        code = RandomSecquenceCreator.getId(10);
    }

    Leaf leaf = null;
    if (op.equals("modify")) {
        Directory dir = new Directory();
        leaf = dir.getLeaf(code);
        if (leaf == null) {
            out.print(SkinUtil.makeErrMsg(request, "节点已被删除!"));
            return;
        }
        name = leaf.getName();
        link = leaf.getLink();
        type = leaf.getType();
        isHome = leaf.getIsHome();
        width = leaf.getWidth();
        pvg = leaf.getPvg();
        icon = leaf.getIcon();
        bigIcon = leaf.getBigIcon();
        isWidget = leaf.isWidget();
        widgetWidth = leaf.getWidgetWidth();
        widgetHeight = leaf.getWidgetHeight();
    }
%>
<form name="form1" method="post" action="menu_right.jsp?action=<%=op%>" onsubmit="return form1_onsubmit()">
    <table width="100%" class="tabStyle_1 percent98">
        <tr>
            <td colspan="2" align="left" class="tabStyle_1_title">
                <%if (op.equals("AddChild")) {%>
                增加
                <%} else {%>
                修改
                <%}%>
            </td>
        </tr>
        <tr>
            <td width="156" rowspan="6" align="left" valign="top">
                <div style="width:136px;"></div>
                <%
                    if ("AddChild".equals(op)) {
                %>
                <div style="margin:5px;">父节点：<%=parent_name%>
                </div>
                <%
                    }
                %>
                <div id="iconDiv" style="text-align:center">
                    <%if (!icon.equals("")) {%>
                    <br/>
                    <img title="小图标" src="<%=request.getContextPath()%>/<%=skinPath%>/icons/<%=icon%>"/>
                    <%}%>
                </div>
                <div id="bigIconDiv" style="margin-top:2px; text-align:center">
                    <%if (!bigIcon.equals("")) {%>
                    <br/>
                    <img title="大图标" src="<%=request.getContextPath()%>/images/bigicons/<%=bigIcon%>"/>
                    <%}%>
                </div>
            </td>
            <td align="left"><lt:Label res="res.label.forum.admin.menu_bottom" key="name"/>
                <input name="name" value="<%=name%>"/>
                <input name="code" type="hidden" value="<%=code%>" <%=op.equals("modify") ? "readonly" : ""%> />
                <%
                    String pChecked = "";
                    String rChecked = "checked";
                    String nChecked = "";
                    String canRepeat = "";
                    if (op.equals("modify")) {
                        if (leaf.isHasPath())
                            pChecked = "checked";
                        if (leaf.isUse())
                            rChecked = "checked";
                        else
                            rChecked = "";
                        if (leaf.isNav())
                            nChecked = "checked";
                        else
                            nChecked = "";
                        if (leaf.isCanRepeat())
                            canRepeat = "checked";
                    }
                %>
                <input name="isUse" value="1" type="checkbox" <%=rChecked%> />
                启用
                <span style="display:none">
        <input name="isNav" value="1" type="checkbox" <%=nChecked%> />
        置于导航条(仅对一级目录有效)</span>
                <!--<input name="isHasPath" value="1" type="checkbox" <%=pChecked%>>
        <lt:Label res="res.label.forum.admin.menu_bottom" key="link_replace"/>$u-->
                <input name="canRepeat" value="1" type="checkbox" <%=canRepeat%> title="允许在多个选项卡中同时打开"/>多窗口
            </td>
        </tr>
        <tr>
            <td align="left"><lt:Label res="res.label.forum.admin.menu_bottom" key="link"/>
                <input name="link" value="<%=link%>"/>
                target
                <select name="target">
                    <option value="mainFrame">右侧页面</option>
                    <option value="_blank">_blank</option>
                    <option value="_self">_self</option>
                    <option value="_parent">_parent</option>
                    <option value="_top">_top</option>
                </select>
                <input name="width" value="0" type="hidden"/>
                <input type=hidden name=parent_code value="<%=parent_code%>"/>
                <input type=hidden name=root_code value=""/>
                <%if (op.equals("modify")) {%>
                <script>
                    form1.target.value = "<%=leaf.getTarget()%>";
                </script>
                <%}%>
            </td>
        </tr>
        <tr>
            <td align="left">
                <%
                    com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
                    boolean isStyleSpecified = oaCfg.get("styleMode").equals("specified") || oaCfg.get("styleMode").equals("2");
                    // 如果指定了界面风格
                    boolean isShowIcon = true, isShowBigIcon = true, isShowFontIcon = true;
                    if (isStyleSpecified) {
                        if (oaCfg.getInt("styleSpecified") !=1 && oaCfg.getInt("styleSpecified") != 4) {
                            isShowIcon = false;
                        }
                        // 如果指定的风格不是时尚型或炫丽型，则不显示
                        if (oaCfg.getInt("styleSpecified") !=2 && oaCfg.getInt("styleSpecified") != 3) {
                            isShowBigIcon = false;
                        }
                        if (oaCfg.getInt("styleSpecified") !=5) {
                            isShowFontIcon = false;
                        }
                    }
                    if (isShowIcon) {
                %>
                图标
                <input name="icon" value="<%=icon%>"/>
                <input name="button" class="btn" type="button" onclick="openWin('menu_icon_sel.jsp', 800, 600)" value="选择"/>
                &nbsp;&nbsp;
                <%
                    }
                    if (isShowBigIcon) {
                %>
                大图标
                <input id="bigIcon" name="bigIcon" title="用于时尚型及绚丽型界面" value="<%=bigIcon%>"/>
                <input name="button" class="btn" type="button" onclick="openWin('menu_big_icon_sel.jsp', 800, 600)" value="选择"/>
                &nbsp;&nbsp;
                <%
                    }
                    if (isShowFontIcon) {
                %>
                <span title="用于轻简型界面">字体图标</span>
                <select id="fontIcon" name="fontIcon" style="width:150px" class="js-example-templating js-states form-control">
                    <%
                        ArrayList<String[]> fontAry = CSSUtil.getFontBefore();
                        int fontAryLen = fontAry.size();
                        for (int k = 0; k < fontAryLen; k++) {
                            String[] ary = fontAry.get(k);
                            String selected = "";
                            if (op.equals("modify")) {
                                if (ary[0].equals(leaf.getFontIcon())) {
                                    selected = "selected";
                                }
                            }
                    %>
                    <option value="<%=ary[0] %>" <%=selected %>>
                        <i class="fa <%=ary[0]%>"></i>
                        <%=ary[0]%>
                    </option>
                    <%
                        }
                    %>
                </select>
                <script>
                    var oMenuIcon;
                    $(function () {
                        //带图片
                        oMenuIcon = $("#fontIcon").select2({
                            width: 200,
                            templateResult: formatState,
                            templateSelection: formatState
                        });
                    });

                    function formatState(state) {
                        if (!state.id) {
                            return state.text;
                        }
                        var $state = $(
                            '<span><i class="fa ' + state.id + '"></i>&nbsp;&nbsp;' + state.text + '</span>'
                        );
                        return $state;
                    };
                </script>
                <%
                    }
                %>
            </td>
        </tr>
        <tr>
            <td align="left">
      <span id="spanPvg">
      	能看到菜单项的权限
        <input title="选择或填写权限编码(可用逗号分隔)，拥有该权限或者以该权限编码开头的权限的用户，才能看到此项菜单" id="pvg" name="pvg" value="<%=pvg%>" size="15" style="display:none"/>
        
        <select id="pvgCode" name="pvgCode" onchange="onChangePvgCode()">
		<option value="">请选择</option>
		<%
            PrivDb pd = new PrivDb();
            Iterator irpv = pd.list().iterator();
            while (irpv.hasNext()) {
                pd = (PrivDb) irpv.next();
                if (pd.getLayer() == 2) {
        %>
            <option value="<%=pd.getPriv()%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=pd.getDesc()%></option>
		<%
        } else {
        %>
            <option value="X"><%=pd.getDesc()%></option>
			<%
                    }
                }
            %>
		<option value="!admin">非管理员</option>
		</select>
		<%if (!pvg.equals("")) {%>
		<script>
		form1.pvgCode.value = "<%=pvg%>";
		</script>
		<%}%>
        （不选择表示全部人员都能看见）

<style>
.checkedBox {
    width: 800px;
    height: 29px;
    border-bottom: 1px solid #47b4eb;
    background: #e7f1fe;
    padding: 0 15px 0 15px;
    font-size: 12px;
    overflow: hidden;
}

.checkedBox dt {
    color: #454444;
    line-height: 28px;
    float: left;
}

.checkedBox dd {
    float: left;
    padding: 6px 8px 0 0;
    margin-left: 10px;
}

.checkedBox dd a {
    color: #608acc;
    background: url(../images/close1.gif) right #fff no-repeat;
    overflow: hidden;
    display: block;
    float: left;
    height: 15px;
    line-height: 15px;
    border: 1px solid #b4c6dc;
    padding: 0 20px 0 5px;
}

.checkedBox dd a:hover {
    color: #47b4eb;
    background: url(../images/close2.gif) right #fff no-repeat;
    border: 1px solid #79bfe2;
}
</style>
<script language="javascript" type="text/javascript">	
function addSel(val, desc) {
    $(".checkedBox").append("<dd><a href='javascript:;' privCode='" + val + "' onclick='delSel(this)'>" + desc + "</a></dd>");
}

function delSel(obj) {
    $(obj).parent().remove();
    var privCode = $(obj).attr("privCode");
    var val = "," + o("pvg").value;
    val = val.replace(new RegExp("," + privCode, "gm"), "");
    val = val.substring(1);
    o("pvg").value = val;
}
</script>
		<dl class="checkedBox">
			<dt>您已选择权限：</dt>
			<%
                String[] ary = StrUtil.split(pvg, ",");
                if (ary != null) {
                    for (int j = 0; j < ary.length; j++) {
                        String desc = "";
                        if (ary[j].equals("!admin")) {
                            desc = "非管理员";
                        } else {
                            pd = new PrivDb(ary[j]);
                            desc = pd.getDesc();
                        }
            %>
					<dd><a href="javascript:;" privCode="<%=ary[j] %>" onclick="delSel(this)"><%=desc%></a></dd>
			<% }
            }%>
		</dl>        
        </span>
            </td>
        </tr>
        <tr>
            <td align="left">
                类型
                <%
                    String pcode = leaf == null ? "" : leaf.getPreCode();
                    String disabled = "";
                    if (op.equals("modify")) {
                        disabled = "disabled";

                        if (pcode.equals("flow") || pcode.equals("module")) {
                %>
                <script>
                    o("link").disabled = true;
                    // o("pvg").disabled = true;
                    // o("pvgCode").disabled = true;
                </script>
                <%
                        }
                    }%>
                <select name="preCode" <%=disabled%> onchange="onChangePreCode()">
                    <option value="">
                        <lt:Label res="res.label.forum.admin.menu_bottom" key="none"/>
                    </option>
                    <option value="flow" <%=pcode.equals("flow") ? "selected" : ""%>>流程</option>
                    <option value="module" <%=pcode.equals("module") ? "selected" : ""%>>模块</option>
                    <option value="basicdata" <%=pcode.equals("basicdata") ? "selected" : ""%>>基础数据</option>
                    <%
                        String opts = "";
                        com.redmoon.oa.ui.menu.Config cfg = com.redmoon.oa.ui.menu.Config.getInstance();
                        List list = cfg.root.getChild("items").getChildren();
                        if (list != null) {
                            Iterator ir = list.iterator();
                            while (ir.hasNext()) {
                                Element e = (Element) ir.next();
                                opts += "<option value='" + e.getChildText("code") + "' " + (pcode.equals(e.getChildText("code")) ? "selected" : "") + ">" + e.getChildText("desc") + "</option>";
                            }
                        }
                    %>
                    <%=opts%>
                </select>
                <%if (!disabled.equals("")) {%>
                <input name="preCode" value="<%=leaf.getPreCode()%>" type="hidden"/>
                <%}%>
                <span id="spanModule">
<select id="formCode" name="formCode" onchange="o('preCode').value='module';onChangePreCode()">
<option value="">选择智能模块</option>
<%
    FormDb fd = new FormDb();
    ModuleSetupDb msd = new ModuleSetupDb();
    String sql = "select code from visual_module_setup where is_use=1 order by code";
    Iterator mir = msd.list(sql).iterator();
    while (mir.hasNext()) {
        msd = (ModuleSetupDb) mir.next();
        String selected = "";
        // System.out.println(getClass() + " " + msd.getString("code") + " " + msd.getString("name"));
        if (op.equals("modify") && msd.getString("code").equals(leaf.getFormCode()))
            selected = "selected";
%>
  <option value="<%=msd.getString("code")%>" <%=selected%>><%=msd.getString("name")%></option>
<%}%>
</select>
</span>
                <script>
                    $(function () {
                        $('#formCode').select2();
                    });
                </script>
                <span id="spanFlow">
<select id="flowTypeCode" name="flowTypeCode" <%=disabled%> onchange="if (this.value=='not') {jAlert('请选择流程类型！','提示'); return;} form1.preCode.value='flow'; onChangePreCode()">
<%
    com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
    flowrootlf = flowrootlf.getLeaf(Leaf.CODE_ROOT);
    if (flowrootlf != null) {
        com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
        flowdv.ShowDirectoryAsOptions(request, out, flowrootlf, flowrootlf.getLayer());
    }
%>
</select>
<%if (op.equals("modify") && (leaf.getType() == Leaf.TYPE_FLOW || leaf.getType() == Leaf.TYPE_MODULE || leaf.getType() == Leaf.TYPE_BASICDATA)) {%>
<script>
o("spanPvg").style.display = "none";
</script>
<%}%>
</span>
                <span id="spanBasicdata">
<select id="basicdata" name="basicdata" onchange="o('preCode').value='basicdata';onChangePreCode()">
<option value="">选择基础数据类别</option>
<%
    SelectKindDb wptd = new SelectKindDb();
    Iterator ir = wptd.list().iterator();
    while (ir.hasNext()) {
        wptd = (SelectKindDb) ir.next();
%>
	  <option value="<%=wptd.getId() %>"><%=wptd.getName() %></option>
	  <%
          }
      %>
</select>
<%if ("modify".equals(op)) { %>
<script>
$('#basicdata').val('<%=leaf.getFormCode()%>');
</script>
<%} %>
</span>
                <%if (!disabled.equals("")) {%>
                <input name="formCode" value="<%=leaf.getFormCode()%>" type="hidden"/>
                <%if (op.equals("modify") && leaf.getPreCode().equals("flow")) {%>
                <script>
                    o("flowTypeCode").value = "<%=leaf.getFormCode()%>";
                    o("flowTypeCode").disabled = false;
                </script>
                <%}%>
                <%
                    }
                %>
            </td>
        </tr>
        <tr>
            <td align="left">
                <%
                    if (op.equals("modify")) {
                        if (leaf.getCode().equals(Leaf.CODE_ROOT)) {
                %>
                <input type="hidden" name="parentCode" value="-1"/>
                <%
                } else {
                %>
                <span style="display:none">
        <lt:Label res="res.label.forum.admin.menu_bottom" key="dir_parent"/>
        <select name="parentCode">
		<%
            Leaf rootlf = leaf.getLeaf("root");
            DirectoryView dv = new DirectoryView(request, rootlf);
            dv.ShowDirectoryAsOptionsWithCode(out, rootlf, rootlf.getLayer());
        %>
        </select>
		<script>
        	o('parentCode').value = "<%=leaf.getParentCode()%>";
        </script>
        </span>
                <%
                        }
                    }
                %>
                <input type="hidden" name="isHome" value="true"/>
                <input name="templateId" type="hidden" value="-1"/>
                <%
                    boolean isShowWidget = true;
                    // 如果指定了界面风格
                    if (isStyleSpecified) {
                        // 如果指定的风格不是炫丽型，则不显示
                        if (oaCfg.getInt("styleSpecified") != 3) {
                            isShowWidget = false;
                        }
                    }
                    if (isShowWidget && (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform())) {
                %>
                <input title="是否为绚丽型界面窗口组件" id="isWidget" name="isWidget" type="checkbox" onclick="onclickIsWidget()" <%=isWidget ? "checked" : ""%> value="1"/>窗口组件
                <span id="spanWidgetProp">
			宽度
			<input id="widgetWidth" name="widgetWidth" size="3" value="<%=widgetWidth%>"/>
			高度
			<input id="widgetHeight" name="widgetHeight" size="3" value="<%=widgetHeight%>"/>
			</span>
                <%
                    }
                    if (op.equals("modify")) {
                %>
                &nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=leaf.getName(request)%>', '<%=request.getContextPath()%>/<%=leaf.getLink(request)%>')">打开菜单</a>
                <%
                    }
                %>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center"><input type="submit" class="btn" value="<lt:Label key="ok"/>"/>
                &nbsp;&nbsp;&nbsp;
                <input name="Submit" type="reset" class="btn" value="<lt:Label key="reset"/>"/>
                &nbsp;&nbsp;&nbsp;
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    function onChangePvgCode() {
        if (o("pvgCode").value == "X") {
            jAlert("您选择的是权限类别，请选择权限项", "提示");
            return;
        }

        if (o("pvgCode").value == "") {
            // o("pvg").value = "";
            return;
        }

        if (o("pvg").value == "") {
            o("pvg").value = o("pvgCode").value;
            addSel(o("pvgCode").value, $("#pvgCode").find("option:selected").text());
        } else {
            o("pvg").value += "," + o("pvgCode").value;
            addSel(o("pvgCode").value, $("#pvgCode").find("option:selected").text());
        }
    }

    function onChangePreCode() {
        if (o("preCode").value == "") {
            o("link").disabled = false;

            o("pvgCode").disabled = false;
            o("pvg").disabled = false;
            $('#spanPvg').show();
            $('#spanFlow').hide();
            $('#spanModule').hide();
            $('#spanBasicdata').hide();
        } else if (o("preCode").value == "flow") {
            o("link").value = "";
            o("link").disabled = true;
            $('#spanPvg').hide();
            $('#spanFlow').show();
            $('#spanModule').hide();
            $('#spanBasicdata').hide();
        } else if (o("preCode").value == "module") {
            o("link").value = "";
            o("link").disabled = true;
            $('#spanPvg').hide();
            $('#spanFlow').hide();
            $('#spanModule').show();
            $('#spanBasicdata').hide();
        } else if (o("preCode").value == "basicdata") {
            o("link").value = "";
            o("link").disabled = true;
            $('#spanPvg').hide();
            $('#spanFlow').hide();
            $('#spanModule').hide();
            $('#spanBasicdata').show();
        }
    }

    $(function () {
        onChangePreCode();
    });
</script>
</html>
