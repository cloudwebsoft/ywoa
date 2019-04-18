package com.redmoon.oa.job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.kernel.License;
/**
 * OEM定时任务
 * @author mk
 * 2015-07-02
 */
public class ClassJarFilesCheckJob implements Job {
	/**
	 * 设置系统是否启用
	 * @param flag
	 */
	private void setSystemStatus(boolean  flag){
		Config conf = new Config();
		if (flag){
			conf.put("systemIsOpen", "false");
			conf.put("systemStatus", "系统被非法修改，请使用正版授权系统");
			conf.put("oem_filesEncrypt_validate", "false");
		} else {
			conf.put("systemIsOpen", "true");
			conf.put("systemStatus", "系统正在维护中.....");
			conf.put("oem_filesEncrypt_validate", "true");
		}
	}
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		LogUtil.getLog(ClassJarFilesCheckJob.class).info("OEM工作调度开始...");
		if (!License.getInstance().isSrc()) {
			BufferedReader br = null;
			try {
				String path = Global.getAppPath();
				String contentFilePath = path+"META-INF"+File.separator+"content.bin";
				File file = new File(contentFilePath);
				if (!file.exists()){                                             //如果文件不存，直接将systemIsOpen置为false
					setSystemStatus(true);
					LogUtil.getLog(ClassJarFilesCheckJob.class).error("content.bin文件不存在");
					return;
				}
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
				String temp = null;
				boolean flag = false;                                                          // true:文件被修改     false:未被修改
				while ((temp=br.readLine())!=null) {
					String decryptStr = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb",temp);
					String[] decryptArr = decryptStr.split("\\|");
					if (decryptArr!=null&&decryptArr.length==3){
						String fileName = decryptArr[0];
						fileName = fileName.replace("\\", File.separator);
						String dPath = "";
						if (fileName.endsWith(".class")){
							dPath = path+"WEB-INF"+File.separator +"classes"+File.separator;
						}
						if (fileName.endsWith(".jar")){
							dPath = path+"WEB-INF"+File.separator +"lib"+File.separator;
						}
						String filePath = dPath + fileName;
						File srcFile = new File (filePath);
						if (!srcFile.exists()){
							flag = true;
							LogUtil.getLog(ClassJarFilesCheckJob.class).error(filePath+"文件不存在");
							break;
						} else {
							long srcTime = srcFile.lastModified();                                //文件的最后修改时间
							long decTime = StrUtil.toLong(decryptArr[2]);                        //解析content.bin文件得到的时间
							if (srcTime==decTime){                                          //如果文件最后修改时间相同，说明文件没有修改，继续读取下一行
								continue;
							} else {
								String srcDigest = DigestUtils.md5Hex(new FileInputStream(srcFile));
								if (srcDigest.equals(decryptArr[1])){                  //如果文件md5摘要信息相同，继续读取下一行
									continue;
								} else {
									flag = true;
									LogUtil.getLog(ClassJarFilesCheckJob.class).error(filePath+"文件有改动");
									break;
								}
							}
						}
					} else {
						flag = true;
						LogUtil.getLog(ClassJarFilesCheckJob.class).error("content.bin文件有改动");
						break;
					}
				}
				setSystemStatus(flag);
			}  catch (FileNotFoundException e) {
				LogUtil.getLog(ClassJarFilesCheckJob.class).error(e.getMessage());
			} catch (IOException e) {
				LogUtil.getLog(ClassJarFilesCheckJob.class).error(e.getMessage());
			} catch (NullPointerException e) {
				LogUtil.getLog(ClassJarFilesCheckJob.class).error(e.getMessage());
			}  finally {
				try {
					if (br!=null)
						br.close();
				} catch (IOException e) {
					LogUtil.getLog(ClassJarFilesCheckJob.class).error(e.getMessage());
				}
			}
		}
		LogUtil.getLog(ClassJarFilesCheckJob.class).info("OEM工作调度结束...");
		
	}

}
