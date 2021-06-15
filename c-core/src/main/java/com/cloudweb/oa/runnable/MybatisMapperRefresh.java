package com.cloudweb.oa.runnable;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.SystemClock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class MybatisMapperRefresh implements Runnable {
    private static final Log logger = LogFactory.getLog(MybatisMapperRefresh.class);
    /**
     * 记录jar包存在的mapper
     */
    private static final Map<String, List<Resource>> jarMapper = new HashMap<>();
    private SqlSessionFactory sqlSessionFactory;
    private Resource[] mapperLocations;
    private Long beforeTime = 0L;
    private Configuration configuration;
    /**
     * 是否开启刷新mapper
     */
    private boolean enabled;
    /**
     * xml文件目录
     */
    private Set<String> fileSet;
    /**
     * 延迟加载时间
     */
    private int delaySeconds = 10;
    /**
     * 刷新间隔时间
     */
    private int sleepSeconds = 10;

    public MybatisMapperRefresh(Resource[] mapperLocations, SqlSessionFactory sqlSessionFactory, int delaySeconds,
                                int sleepSeconds, boolean enabled) {
        this.mapperLocations = mapperLocations.clone();
        this.sqlSessionFactory = sqlSessionFactory;
        this.delaySeconds = delaySeconds;
        this.enabled = enabled;
        this.sleepSeconds = sleepSeconds;
        this.configuration = sqlSessionFactory.getConfiguration();
        this.run();
    }

    public MybatisMapperRefresh(Resource[] mapperLocations, SqlSessionFactory sqlSessionFactory, boolean enabled) {
        this.mapperLocations = mapperLocations.clone();
        this.sqlSessionFactory = sqlSessionFactory;
        this.enabled = enabled;
        this.configuration = sqlSessionFactory.getConfiguration();
        this.run();
    }

    @Override
    public void run() {
        final GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
        /*
         * 启动 XML 热加载
         */
        if (enabled) {
            beforeTime = SystemClock.now();
            final MybatisMapperRefresh runnable = this;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (fileSet == null) {
                        fileSet = new HashSet<>();
                        if (mapperLocations != null) {
                            for (Resource mapperLocation : mapperLocations) {
                                try {
                                    if (ResourceUtils.isJarURL(mapperLocation.getURL())) {
                                        String key = new UrlResource(ResourceUtils.extractJarFileURL(mapperLocation.getURL()))
                                                .getFile().getPath();
                                        fileSet.add(key);
                                        if (jarMapper.get(key) != null) {
                                            jarMapper.get(key).add(mapperLocation);
                                        } else {
                                            List<Resource> resourcesList = new ArrayList<>();
                                            resourcesList.add(mapperLocation);
                                            jarMapper.put(key, resourcesList);
                                        }
                                    } else {
                                        fileSet.add(mapperLocation.getFile().getPath());
                                    }
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(delaySeconds * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    do {
                        try {
                            for (String filePath : fileSet) {
                                File file = new File(filePath);
                                if (file.isFile() && file.lastModified() > beforeTime) {
                                    List<Resource> removeList = jarMapper.get(filePath);
                                    if (removeList != null && !removeList.isEmpty()) {
                                        for (Resource resource : removeList) {
                                            runnable.refresh(resource);
                                        }
                                    } else {
                                        runnable.refresh(new FileSystemResource(file));
                                    }
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }

                        beforeTime = SystemClock.now();

                        try {
                            Thread.sleep(sleepSeconds * 1000);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    } while (true);
                }
            }, "mybatis-plus MapperRefresh").start();
        }
    }

    /**
     * 刷新mapper
     *
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void refresh(Resource resource) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        this.configuration = sqlSessionFactory.getConfiguration();
        boolean isSupper = configuration.getClass().getSuperclass() == Configuration.class;
        try {
            Field loadedResourcesField = isSupper ? configuration.getClass().getSuperclass().getDeclaredField("loadedResources")
                    : configuration.getClass().getDeclaredField("loadedResources");
            loadedResourcesField.setAccessible(true);
            Set loadedResourcesSet = ((Set) loadedResourcesField.get(configuration));
            XPathParser xPathParser = new XPathParser(resource.getInputStream(), true, configuration.getVariables(),
                    new XMLMapperEntityResolver());
            XNode context = xPathParser.evalNode("/mapper");
            String namespace = context.getStringAttribute("namespace");
            Field field = MapperRegistry.class.getDeclaredField("knownMappers");
            field.setAccessible(true);
            Map mapConfig = (Map) field.get(configuration.getMapperRegistry());
            Collection<String> mappedStatementNames = configuration.getMappedStatementNames();

            mapConfig.remove(org.apache.ibatis.io.Resources.classForName(namespace));
            loadedResourcesSet.remove(resource.toString());
            configuration.getCacheNames().remove(namespace);

            cleanParameterMap(context.evalNodes("/mapper/parameterMap"), namespace);
            cleanResultMap(context.evalNodes("/mapper/resultMap"), namespace);
            cleanKeyGenerators(context.evalNodes("insert|update|select"), namespace);
            cleanSqlElement(context.evalNodes("/mapper/sql"), namespace);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(resource.getInputStream(),
                    sqlSessionFactory.getConfiguration(),
                    resource.toString(), sqlSessionFactory.getConfiguration().getSqlFragments());
            xmlMapperBuilder.parse();
            logger.debug("refresh: '" + resource + "', success!");
        } catch (IOException e) {
            logger.error("Refresh IOException :" + e.getMessage());
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * 清理parameterMap
     *
     * @param list
     * @param namespace
     */
    private void cleanParameterMap(List<XNode> list, String namespace) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            configuration.getParameterMaps().remove(namespace + "." + id);
        }
    }

    /**
     * 清理resultMap
     *
     * @param list
     * @param namespace
     */
    private void cleanResultMap(List<XNode> list, String namespace) {
        for (XNode resultMapNode : list) {
            String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
            configuration.getResultMapNames().remove(id);
            configuration.getResultMapNames().remove(namespace + "." + id);
            clearResultMap(resultMapNode, namespace);
        }
    }

    private void clearResultMap(XNode xNode, String namespace) {
        for (XNode resultChild : xNode.getChildren()) {
            if ("association".equals(resultChild.getName()) || "collection".equals(resultChild.getName())
                    || "case".equals(resultChild.getName())) {
                if (resultChild.getStringAttribute("select") == null) {
                    configuration.getResultMapNames().remove(
                            resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    configuration.getResultMapNames().remove(
                            namespace + "." + resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    if (resultChild.getChildren() != null && !resultChild.getChildren().isEmpty()) {
                        clearResultMap(resultChild, namespace);
                    }
                }
            }
        }
    }

    /**
     * 清理selectKey
     *
     * @param list
     * @param namespace
     */
    private void cleanKeyGenerators(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getKeyGeneratorNames().remove(id + SelectKeyGenerator.SELECT_KEY_SUFFIX);
            configuration.getKeyGeneratorNames().remove(namespace + "." + id + SelectKeyGenerator.SELECT_KEY_SUFFIX);

            Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
            List<MappedStatement> objects = new ArrayList(); // Lists.newArrayList();

            Iterator<MappedStatement> it = mappedStatements.iterator();
            while (it.hasNext()){
                Object object=it.next();
                if(object instanceof org.apache.ibatis.mapping.MappedStatement) {
                    MappedStatement mappedStatement=(MappedStatement)object;
                    if (mappedStatement.getId().equals(namespace + "." + id)) {
                        objects.add(mappedStatement);
                    }
                }
            }
            /*
            for (MappedStatement mappedStatement : mappedStatements) {
                if (mappedStatement.getId().equals(namespace + "." + id)) {
                    objects.add(mappedStatement);
                }
            }*/
            mappedStatements.removeAll(objects);
        }
    }

    /**
     * 清理sql节点缓存
     *
     * @param list
     * @param namespace
     */
    private void cleanSqlElement(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getSqlFragments().remove(id);
            configuration.getSqlFragments().remove(namespace + "." + id);
        }
    }
}