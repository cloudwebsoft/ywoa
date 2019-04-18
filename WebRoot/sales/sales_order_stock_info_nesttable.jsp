<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = ParamUtil.get(request, "formCode");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

long orderId = ParamUtil.getLong(request, "orderId");
FormDAO fdao = new FormDAO();
FormDb fdOrdProduct = new FormDb();
fdOrdProduct = fdOrdProduct.getFormDb("sales_ord_product");
Vector v = fdao.list("sales_ord_product", "select id from form_table_sales_ord_product where cws_id=" + orderId);

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
int len = 0;
if (fields!=null)
	len = fields.length;
UserMgr um = new UserMgr();	
%>
		<table width="98%" border="0" cellspacing="1" cellpadding="2" id="cwsNestTable" class="MsoTableProfessional percent98">
			  <tr align="middle" class="cwsThead">
				<%
                for (int i=0; i<len; i++) {
                    String fieldName = fields[i];
                    String title = "创建者";
                    if (!fieldName.equals("cws_creator"))
                        title = fd.getFieldTitle(fieldName);
            
                    FormField ff = fd.getFormField(fieldName);
                    String macroType = "";
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        macroType = ff.getMacroType();
                    }
                %>
                        <td width="<%=fieldsWidth[i]%>" type="<%=ff.getType()%>" macroType="<%=macroType%>">
                        <div class="resizeTd" onmousedown="MouseDownToResize(this);" onmousemove="MouseMoveToResize(this);" onmouseup="MouseUpToResize(this);"></div>
                        <%=title%>
                        </td>
                <%}%>	
			  </tr>
              <%
			  FormDAO fdaoProduct = new FormDAO();
			  FormDb fdProduct = new FormDb();
			  fdProduct = fdProduct.getFormDb("sales_product_info");
			  Iterator ir = v.iterator();
			  UserDb user;
			  while (ir.hasNext()) {
				  fdao = (FormDAO)ir.next();
			  %>
              <tr>
              <%
			  	for (int i=0; i<len; i++) {
                    String fieldName = fields[i];
					%>
                    <td <%=fieldName.equals("product")?"value="+fdao.getFieldValue(fieldName):""%>>
					<%
                    if (!fieldName.equals("cws_creator")) {
						if (fieldName.equals("product")) {
							fdaoProduct = fdaoProduct.getFormDAO(StrUtil.toLong(fdao.getFieldValue(fieldName)), fdProduct);
							%>
							<%=fdaoProduct.getFieldValue("product_name")%>
							<%
						}
						else {
					%>
							<%=fdao.getFieldValue(fieldName)%>
					<%
						}
					}
					else {
						String realName = "";
						if (fdao.getCreator()!=null) {
							user = um.getUserDb(fdao.getCreator());
							if (user!=null)
								realName = user.getRealName();
						}	
					}
					%></td><%
				}
			  %>
              </tr>
              <%}%>
		</table>
        