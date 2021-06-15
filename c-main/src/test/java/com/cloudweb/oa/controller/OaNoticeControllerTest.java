package com.cloudweb.oa.controller;

import com.cloudweb.oa.entity.OaNotice;
import com.redmoon.oa.pvg.Privilege;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
// 加载配置文件，可以指定多个配置文件，locations指定的是一个数组
@ContextConfiguration(locations = {"classpath*:applicationContext_test.xml", "file:src/main/oa/com/cloudweb/config/spring_dispatcher_servlet.xml"})
// 启动事务控制
@Rollback
@Transactional(transactionManager = "txManager")
// 配置事务管理器，同时指定自动回滚
// @TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@SpringBootTest
public class OaNoticeControllerTest {
    @Autowired
    private OaNoticeController oaNoticeController;

    // 集成Web环境方式
    @Autowired
    private WebApplicationContext context;

    @Autowired
    // @Qualifier("OaNotice")
    private OaNotice oaNotice;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(oaNoticeController).build();
        this.session = doLogin();
        try {
            //单元测试未启动web service,所以要自已加载proxool configurator
            String path = getClass().getResource("/").getPath();
            path = path + "../../webapp/WEB-INF/proxool.xml";
            JAXPConfigurator.configure(path, false);
        } catch (ProxoolException e) {
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * 获取用户登录的Session
     * @return MockHttpSession
     * @throws Exception 异常
     */
    private MockHttpSession doLogin() throws Exception {
        // 置登录session
        session = new MockHttpSession();
        session.setAttribute(Privilege.NAME, "admin");
        return session;
    }

    @Test
    public void list() throws Exception {
        // 如果用MockMvcRequestBuilders.post会返回405
        ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/notice/list")
                .param("op", "search")
                .param("person", "外卖")
                .param("type", "0")
                .session(session).accept(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); // 预期返回的状态码是200
        MvcResult result = resultActions.andReturn();

        resultActions
                // .andExpect(MockMvcResultMatchers.view().name("notice/notice_list"))
                // .andExpect(MockMvcResultMatchers.model().attributeExists("person"))
                .andExpect(handler().handlerType(OaNoticeController.class)) //验证执行的控制器类型
                .andExpect(handler().methodName("list")) //验证执行的控制器方法名
                //.andExpect(model().hasNoErrors()); //验证页面没有错误
                .andDo(print());

        // Assert.assertNotNull(result.getModelAndView().getModel().get("userName")); //自定义断言
    }


    @Test
    public void show() throws Exception {
        // 如果用MockMvcRequestBuilders.post会返回405
        ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/notice/show")
                .param("id", "1")
                .session(session).accept(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); // 预期返回的状态码是200
        MvcResult result = resultActions.andReturn();

        resultActions
                // .andExpect(MockMvcResultMatchers.view().name("notice/notice_list"))
                // .andExpect(MockMvcResultMatchers.model().attributeExists("person"))
                .andExpect(handler().handlerType(OaNoticeController.class)) //验证执行的控制器类型
                .andExpect(handler().methodName("show")) //验证执行的控制器方法名
                //.andExpect(model().hasNoErrors()); //验证页面没有错误
                .andDo(print());

        // Assert.assertNotNull(result.getModelAndView().getModel().get("userName")); //自定义断言
    }
}