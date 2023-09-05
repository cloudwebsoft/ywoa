package com.cloudweb.oa.utils;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class JarFileUtil {
    /**
     * 读取指定文件夹下的指定类型的文件
     *
     * @param basePath 基础目录
     * @param fileType 文件类型
     * @param lst      存放目录的容器
     */
    public List<String> loadFiles(String basePath, String fileType, List<String> lst) {
        URL resource = JarFileUtil.class.getClassLoader().getResource(basePath);
        if (resource == null) {
	        LogUtil.getLog(getClass()).warn("loadFilesInJar basePath: " + basePath + " is not exist.");
            return lst;
        }
        if ("jar".equals(resource.getProtocol())) {
            //jar包读取
            loadFilesInJar(basePath, fileType, lst);
        } else {
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                // 如果当前文件是文件夹 那么获取下面所有的文件
                String[] list = file.list();
                for (String s : list) {
                    loadFiles(basePath + "/" + s, fileType, lst);
                }
            } else if (file.isFile()) {
                if (StrUtil.isEmpty(fileType) || basePath.endsWith("." + fileType)) {
                    lst.add(basePath);
                }
            }
        }
        return lst;
    }

    // 读取jar内的文件夹信息
    public List<String> loadFilesInJar(String basePath, String fileTypes, List<String> lst) {
        try {
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + "BOOT-INF/classes/" + basePath + "/*");
            // LogUtil.getLog(getClass()).info("loadFilesInJar resources.len=" + resources.length);
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                // LogUtil.getLog(getClass()).info("loadFilesInJar fileName=" + fileName);
                if (StrUtil.isEmpty(fileTypes) || fileName.endsWith("." + fileTypes)) {
                    lst.add(basePath + "/" + fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lst;
    }

    // 本方法取不到static/images/mobileAppIcons下面的文件
    public List<String> loadFilesInJarXXX(String basePath, String fileTypes, List<String> lst) {
        try {
            ClassLoader classLoader = JarFileUtil.class.getClassLoader();
            URL url = classLoader.getResource(basePath);
            String urlStr = url.toString();
            LogUtil.getLog(getClass()).info("loadFilesInJar urlStr=" + urlStr);
            // 找到!/ 截断之前的字符串
            String jarPath = urlStr.substring(0, urlStr.indexOf("!/") + 2);
            LogUtil.getLog(getClass()).info("loadFilesInJar jarPath=" + jarPath);
            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntrys = jarFile.entries();
            LogUtil.getLog(getClass()).info("loadFilesInJar jarFile.size=" + jarFile.size());
            while (jarEntrys.hasMoreElements()) {
                JarEntry entry = jarEntrys.nextElement();
                // 简单的判断路径，如果想做到像Spring，Ant-Style格式的路径匹配需要用到正则。
                String name = entry.getName();
                LogUtil.getLog(getClass()).info("loadFilesInJar entry.getName()=" + name);
                if (name.startsWith(basePath) && !name.equals(basePath)) {
                    if (entry.isDirectory()) {
                        // 文件夹 迭代
                        loadFilesInJar(name, fileTypes, lst);
                    } else if (!lst.contains(name)) {
                        if (StrUtil.isEmpty(fileTypes) || name.endsWith("." + fileTypes)) {
                            lst.add(name);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lst;
    }

    // 读取指定文件信息
    public String loadFile(String fileName) {
        URL resource = JarFileUtil.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            return "";
        }
        // 如果文件是jar 那么需要用jar的方式读取
        if ("jar".equals(resource.getProtocol())) {
            //jar包读取
            return loadJarFile(fileName);
        } else {
            StringBuilder content = new StringBuilder();
            FileInputStream fr = null;
            try {
                fr = new FileInputStream(resource.getFile());
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = fr.read(bytes)) > 0) {
                    content.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                closeIO(fr);
            }
            return content.toString();
        }
    }

    // 读取jar包内的文件信息
    public String loadJarFile(String fileName) {
        StringBuilder content = new StringBuilder();
        InputStream fr = JarFileUtil.class.getClassLoader().getResourceAsStream(fileName);
        try {
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fr.read(bytes)) > 0) {
                content.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            closeIO(fr);
        }
        return content.toString();
    }

    public InputStream getJarFile(String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        return JarFileUtil.class.getClassLoader().getResourceAsStream(fileName);
    }

    public void closeIO(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }
}
