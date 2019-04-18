<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "org.jawin.*"%>
<%
long workplanId = ParamUtil.getLong(request, "workplanId");
WorkPlanMgr wpm = new WorkPlanMgr();
WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
WorkPlanDb wpd = wpm.getWorkPlanDb(request, (int)workplanId, "read");
String data = wpd.getGantt();
// 加入tasks
JSONObject json = null;
try {
	json = new JSONObject(data);
}
catch(Exception e) {
	out.print(SkinUtil.makeErrMsg(request, "json格式非法！"));
	return;
}

// 删除已存在的文件，防止project会弹出提示是否替换同名文件
String mppName = "c:/" + RandomSecquenceCreator.getId(30) + ".mpp";
try {
	File file = new File(mppName);
	if (file.exists())
		file.delete();
} catch (Exception e) {
	e.printStackTrace();
}

DispatchPtr app = null;
try {
	app = new DispatchPtr("MSProject.Application");
}
catch (NoClassDefFoundError e) {
	//out.print(SkinUtil.makeErrMsg(request, "请检查jawin环境配置！"));
	out.print(SkinUtil.makeErrMsg(request, "请在服务器端安装ms project！"));
	return;
}
catch (UnsatisfiedLinkError e) {
	out.print(SkinUtil.makeErrMsg(request, "请在服务器端安装ms project！"));		
	return;
} catch (Exception e) {
	out.print(SkinUtil.makeErrMsg(request, "请在服务器端安装ms project！"));		
	return;
}

DispatchPtr projects = (DispatchPtr) app.get("Projects");
DispatchPtr project = (DispatchPtr) projects.invoke("Add");

//生成一个task集合
DispatchPtr tasks = (DispatchPtr) project.get("Tasks");

DispatchPtr Resources = (DispatchPtr) project.get("Resources");

Map map = new HashMap();

UserMgr um = new UserMgr();
String[] principalAry = wpd.getPrincipals();
int len = principalAry.length;
for (int i=0; i<len; i++) {
  if (principalAry[i].equals(""))
	  continue;
  UserDb user = um.getUserDb(principalAry[i]);
  DispatchPtr resourcePtr = (DispatchPtr) Resources.invoke("Add");
  resourcePtr.put("name", user.getRealName());
  map.put(user.getRealName(), resourcePtr);
}

String[] userAry = wpd.getUsers();
len = userAry.length;
for (int i=0; i<len; i++) {
  if (userAry[i].equals(""))
	  continue;
  // 过滤掉负责人
  boolean isFound = false;
  for (int j=0; j<principalAry.length; j++) {
	  if (principalAry[j].equals(userAry[i])) {
		  isFound = true;
		  break;
	  }
  }
  if (isFound)
	  continue;
  UserDb user = um.getUserDb(userAry[i]);
  
  DispatchPtr resourcePtr = (DispatchPtr) Resources.invoke("Add");
  resourcePtr.put("name", user.getRealName());
  map.put(user.getRealName(), resourcePtr);
}

try {
	JSONArray jsonAry = (JSONArray)json.get("tasks");
	for (int i=0; i<jsonAry.length(); i++) {
		JSONObject obj = (JSONObject)jsonAry.get(i);
		String code = obj.getString("code");
		String name = obj.getString("name");
		String strStatus = obj.getString("status");
		String start = obj.getString("start");
		String end = obj.getString("end");
		int duration = obj.getInt("duration");
		boolean startIsMilestone = obj.getBoolean("startIsMilestone");
		boolean endIsMilestone = obj.getBoolean("endIsMilestone");
		String resource = obj.getString("resource");
		int level = StrUtil.toInt(obj.getString("level"));
		
		int status = WorkPlanTaskDb.getStatusByDesc(strStatus);
		
		java.util.Date sDate = DateUtil.parse(start);
		java.util.Date eDate = DateUtil.parse(end);
		
		int progress = StrUtil.toInt(obj.getString("progress"), 0);
		String workplanRelated = obj.getString("workplanRelated");
		long longWorkplanRelated = StrUtil.toLong(workplanRelated, -1);
		int assess = StrUtil.toInt(obj.getString("assess"), 0);
			
		DispatchPtr task = (DispatchPtr) tasks.invoke("Add");
		
		// 注意UniqueID及ID均为只读字段，赋值在导出时会报错
		// task.put("UniqueID", obj.getString("id"));
		// task.put("ID", obj.getString("id"));
		// 将task的ID置于Text1用户自定义属性中
		task.put("Text1", obj.getString("id"));
		
		//当然要为任务设置属性:这要是name notes
		task.put("Name", name);
		task.put("Notes", ""); // 备注
		int l = level + 1;
		task.put("OutlineLevel", String.valueOf(l)); 
		task.put("start", DateUtil.format(sDate, "yyyy-MM-dd"));
		task.put("Finish", DateUtil.format(eDate, "yyyy-MM-dd"));

		task.put("Duration", String.valueOf(duration));
		task.put("PercentComplete", obj.getString("progress"));
		
		try {
			String dp = obj.getString("depends");
			String[] ary = StrUtil.split(dp, ":");
			if (ary!=null) {
				if (ary.length>1) {
					dp = ary[0] + "fs + " + ary[1] + "d";
				}
				else if (ary.length==1)
					dp = ary[0] + "fs";
			}
			task.put("Predecessors", dp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// task.put("resource", resourcePtr.get("id"));
		// task.Assignments.Add ResourceID:=t.Resources("test").id, Units:=8
		
		DispatchPtr ass = (DispatchPtr) task.get("Assignments");
		
		// 取得任务中的人员
		Iterator ir = wptud.getTaskUsers(StrUtil.toLong(obj.getString("id"))).iterator();
		while (ir.hasNext()) {
			wptud = (WorkPlanTaskUserDb)ir.next();
			UserDb user = um.getUserDb(wptud.getString("user_name"));

			// DispatchPtr resourcePtr = (DispatchPtr)map.get(obj.getString("resourceDesc"));
			DispatchPtr resourcePtr = (DispatchPtr)map.get(user.getRealName());
			if (resourcePtr!=null) {
				ass.invoke("Add", task.get("id"), resourcePtr.get("id"), (double)wptud.getInt("percent")/100);
			}
		}
		
		// ActiveProject.Resources(2).Assignments.Add(TaskID: =1).Work = 120
		
	}
	
	// taskName.put("end", DateUtil.format(DateUtil.addDate(new java.util.Date(), 1), "yyyy-MM-dd"));
	// 在task对象中,没有明确的父子关系可以确定,在生成的过程中,都是更具生成的id和UniqueID顺序排下去的
	// 唯一可以确定最终在msproject中的结构的就是一个OutLineLevel属性了.
	// 最后,进行保存操作,以及不要忘记进行应用程序的关闭.
	// String mppName = Global.getRealPath() + "upfile/temp/" + RandomSecquenceCreator.getId(30) + ".mpp";
	
	project.invoke("SaveAs", mppName);
	// Name, Format, Backup, ReadOnly, TaskInformation, Filtered, Table, UserID, DatabasePassWord, FormatID, Map, Password, WriteResPassword, ClearBaseline, ClearActuals, ClearResourceRates, ClearFixedCosts, XMLName, ClearConfirmed
	// project.invoke("SaveAs", new Object[]{mppPath, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null, null});
}
catch(Exception e) {
	out.print(e.getMessage());
	e.printStackTrace();
	return;
}
finally {
	app.invoke("DocClose" ); 
}

response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
/*
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/
response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(wpd.getTitle()) + ".mpp");
response.setContentType("application/msproject;charset=utf-8");

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	bis = new BufferedInputStream(new FileInputStream(mppName));
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}
} catch(final IOException e) {
	e.printStackTrace();
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

try {
	// 删除临时文件
	File file = new File(mppName);
	if (file.exists())
		; // file.delete();
} catch (Exception e) {
	e.printStackTrace();
}

out.clear();

out = pageContext.pushBody();
%>