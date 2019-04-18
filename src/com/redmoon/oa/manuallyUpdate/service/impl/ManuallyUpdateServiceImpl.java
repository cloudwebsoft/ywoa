package com.redmoon.oa.manuallyUpdate.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.manuallyUpdate.service.ManuallyUpdateService;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.upgrade.util.IBeanshellUtil;
import com.redmoon.oa.upgrade.util.IDatabaseUpgradeUtil;
import com.redmoon.oa.upgrade.util.IFilesUpgradeUtil;
import com.redmoon.oa.upgrade.util.IMiscUtil;
import com.redmoon.oa.upgrade.util.IUnzipUtil;
/**
 * 手动更新服务实现类
 * @author Administrator
 *
 */
public class ManuallyUpdateServiceImpl implements ManuallyUpdateService {
	private static final String UPGRADE_BEANSHELL_FILE = "upgrade.sh";

	private static final String UPGRADE_SQL_FILE = "upgrade.sql";

	private static final String UPGRADE_FILES_DIRECTORY = "files";
	
	private static final String VERSION_FILE_NAME = "version.txt";
	
	private static final String UPDATE_CONTENT_NAME = "updateContent.txt";
	private IMiscUtil miscUtil;
	private IUnzipUtil unzipUtil;
	private IFilesUpgradeUtil filesUpgradeUtil;
	private IBeanshellUtil beanshellUtil;
	private IDatabaseUpgradeUtil databaseUpgradeUtil;
	
	
	public IMiscUtil getMiscUtil() {
		return miscUtil;
	}


	public void setMiscUtil(IMiscUtil miscUtil) {
		this.miscUtil = miscUtil;
	}


	public IUnzipUtil getUnzipUtil() {
		return unzipUtil;
	}


	public void setUnzipUtil(IUnzipUtil unzipUtil) {
		this.unzipUtil = unzipUtil;
	}


	public IFilesUpgradeUtil getFilesUpgradeUtil() {
		return filesUpgradeUtil;
	}


	public void setFilesUpgradeUtil(IFilesUpgradeUtil filesUpgradeUtil) {
		this.filesUpgradeUtil = filesUpgradeUtil;
	}


	public IBeanshellUtil getBeanshellUtil() {
		return beanshellUtil;
	}


	public void setBeanshellUtil(IBeanshellUtil beanshellUtil) {
		this.beanshellUtil = beanshellUtil;
	}


	public IDatabaseUpgradeUtil getDatabaseUpgradeUtil() {
		return databaseUpgradeUtil;
	}


	public void setDatabaseUpgradeUtil(IDatabaseUpgradeUtil databaseUpgradeUtil) {
		this.databaseUpgradeUtil = databaseUpgradeUtil;
	}

	/**
	 * 更新
	 */
	@Override
	public boolean manuallyUpdate(File file) throws ErrMsgException {
		boolean flag = false;
		String tempPath = this.miscUtil.getTempPath();
		String upgradeFileRoot = tempPath + "/output";
		Config cfg = new Config();
		try
		{
			
			//解压包
			this.unzipUtil.unzip(file, upgradeFileRoot);
			File versionFile = new File(upgradeFileRoot, VERSION_FILE_NAME);
			Map<String, String> map = readTxtFile(versionFile);
			
			String lastVersion = map.get("lastVersion");
			//判断更新包上一版本是否和待更新版本一致，不一致则不允许更新
			if (map.size() > 0 && cfg.get("version").equals(lastVersion))
			{
				//执行sh脚本
				this.beanshellUtil.execute(new File(upgradeFileRoot,
						UPGRADE_BEANSHELL_FILE));
				//执行sql脚本
				this.databaseUpgradeUtil.upgrade(new File(upgradeFileRoot,
						UPGRADE_SQL_FILE));
				//更新文件
				this.filesUpgradeUtil.upgrade(new File(upgradeFileRoot,
						UPGRADE_FILES_DIRECTORY).getAbsolutePath(),
						Global.getAppPath());
				//写版本号至config_oa.xml
				// cfg.put("version", map.get("thisVersion"));
				flag = true;
			}else{
				throw new ErrMsgException("当前版本号非"+ lastVersion + "版本，无法升级");
			}
			//发送消息给admin权限用户
			String title = null;
			String content = null;
			if (flag)
			{
				 title = cfg.get("upgradeRemindMsg_success_title");
				 content = cfg.get("upgradeRemindMsg_success_content");
				 String updateContent = "";
				try {
					updateContent = FileUtils.readFileToString(new File(upgradeFileRoot, UPDATE_CONTENT_NAME), "utf-8");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				content = content.replaceFirst("\\$content", updateContent.replaceAll("\\$", "&#36;"));
				sendMsg(title, content);
			}
			else
			{
				title = cfg.get("upgradeRemindMsg_error_title");
				content = cfg.get("upgradeRemindMsg_error_content");
				sendMsg(title , content);
			}
			
		}
		catch(Exception e)
		{
			LogUtil.getLog(getClass()).error("更新出错：" + StrUtil.trace(e));
		}
		finally
		{
			this.filesUpgradeUtil.clearFolder(tempPath);
		}
		
		//更新完成，重启tomcat 
		if (flag)
		{
			this.miscUtil.restartTomcat();
			
			
		}
		
		// TODO Auto-generated method stub
		return flag;
	}
	
	/**
	 * 获取version.txt文件内容
	 * @param file
	 * @return
	 */
    private Map<String,String> readTxtFile(File file){
    	Map<String,String> map = new HashMap<String,String>();
        try {

            String encoding="utf-8";

            if(file.isFile() && file.exists()){ //判断文件是否存在

                InputStreamReader read = new InputStreamReader(

                new FileInputStream(file),encoding);//考虑到编码格式

                BufferedReader bufferedReader = new BufferedReader(read);

                String lineTxt = null;

                while((lineTxt = bufferedReader.readLine()) != null){
                	String[] strs = lineTxt.split("=");
                	map.put(strs[0].trim(), strs[1].trim());

                }

                read.close();

            }

        } catch (Exception e) {

        	LogUtil.getLog(getClass()).error("获取版本号失败" + StrUtil.trace(e));

        }
        return map;
     

    }
    /**
     * 发送消息
     * @param title
     * @param content
     */
    @SuppressWarnings("static-access")
	private void sendMsg(String title, String content)
    {
		IMessage imsg = null; 
		
		//获取admin权限用户，并遍历发送消息
		Privilege privilege = new Privilege(); 
		Vector adminVector = privilege.getUsersHavePriv("admin");
		Iterator it = adminVector.iterator();
		while (it.hasNext())
		{
			UserDb ud = (UserDb)it.next();
			try {
				ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
				imsg = (IMessage) proxyFactory.getProxy();
				imsg.sendSysMsg(ud.getName(), title, content);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
    }
    

}
