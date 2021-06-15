package com.cloudweb.oa.controller;

import com.cloudweb.oa.bean.Address;
import com.redmoon.oa.pvg.Privilege;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
// 加载配置文件，可以指定多个配置文件，locations指定的是一个数组
// @ContextConfiguration(locations = {"classpath*:applicationContext_test.xml", "file:src/main/oa/com/cloudweb/config/spring_dispatcher_servlet.xml"})
@ContextConfiguration(locations = {"classpath*:applicationContext_test.xml"})
// 启动事务控制
@Rollback
@Transactional(transactionManager = "txManager")
// 配置事务管理器，同时指定自动回滚
// @TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@SpringBootTest
public class AddressControllerTest {
    @Autowired
    private AddressController addressController;

    // 集成Web环境方式
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private javax.servlet.Filter springSecurityFilterChain;

    @Autowired
    // @Qualifier("Address")
    private Address address;

    private MockMvc mockMvc;
    private MockMvc mockMvc2;
    private MockHttpSession session;

    @Before
    public void setUp() throws Exception {
        // mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();

        // 集成Web环境方式
        // mockMvc2 = MockMvcBuilders.webAppContextSetup(this.context).build();
        // mockMvc2 = MockMvcBuilders.standaloneSetup(addressController).build();

        final MockHttpServletRequestBuilder defaultRequestBuilder = MockMvcRequestBuilders.get("/address/list");

        this.mockMvc2 = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .defaultRequest(defaultRequestBuilder)
                .alwaysDo(result -> setSessionBackOnRequestBuilder(defaultRequestBuilder, result.getRequest()))
                .apply(springSecurity(springSecurityFilterChain))
                .build();

        this.session = doLogin();

/*        try {
            //单元测试未启动web service,所以要自已加载proxool configurator
            String path = getClass().getResource("/").getPath();
            path = path + "../../webapp/WEB-INF/proxool.xml";
            JAXPConfigurator.configure(path, false);
        } catch (ProxoolException e) {
            throw e;
        }*/
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
/*        ResultActions resultActions = this.mockMvc2.perform(MockMvcRequestBuilders.post("/user/doLogin")
                .param("username", "username").param("password", "password").param("vcode", ""));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        MvcResult result = resultActions.andReturn();
        session = (MockHttpSession) result.getRequest().getSession();*/

        // 置登录session
        session = new MockHttpSession();
        session.setAttribute(Privilege.NAME, "admin");
        return session;
    }

    @Test
    public void getPositionForSection() {
    }

    private MockHttpServletRequest setSessionBackOnRequestBuilder(final MockHttpServletRequestBuilder requestBuilder,
                                                                  final MockHttpServletRequest request) {
        requestBuilder.session((MockHttpSession) request.getSession());
        return request;
    }

    @Test
    public void list() throws Exception {
        // 如果用MockMvcRequestBuilders.post会返回405
        ResultActions resultActions = this.mockMvc2.perform(MockMvcRequestBuilders.get("/address/list")
                .param("op", "search")
                .param("person", "外卖")
                .param("type", "0")
                .session(session).accept(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); // 预期返回的状态码是200
        MvcResult result = resultActions.andReturn();

        resultActions
            .andExpect(MockMvcResultMatchers.view().name("address/address"))
            .andExpect(MockMvcResultMatchers.model().attributeExists("person"))
            .andExpect(handler().handlerType(AddressController.class)) //验证执行的控制器类型
            .andExpect(handler().methodName("list")) //验证执行的控制器方法名
            .andDo(print())
            .andExpect(model().hasNoErrors()); //验证页面没有错误

        Assert.assertNotNull(result.getModelAndView().getModel().get("userName")); //自定义断言
    }

    @Test
    public void listMobile() throws Exception {
        // 如果用MockMvcRequestBuilders.post会返回405
        ResultActions resultActions = this.mockMvc2.perform(MockMvcRequestBuilders.get("/public/address/list")
                .param("what", "外卖")
                .param("type", "0")
                .session(session).accept(MediaType.APPLICATION_JSON_UTF8)
        );
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); // 预期返回的状态码是200
        MvcResult result = resultActions.andReturn();

        MockHttpServletRequest request = result.getRequest();
        MockHttpServletResponse response = result.getResponse();
        System.out.println(response.getContentAsString());

        resultActions.andExpect(content().string(containsString("res")))
                .andExpect(jsonPath("$..name").exists());
                // .andDo(print())
                // .andExpect(MockMvcResultMatchers.view().name("user/view"))
                // .andExpect(MockMvcResultMatchers.model().attributeExists("user"))
                // .andExpect(handler().handlerType(User...Controller.class)) //验证执行的控制器类型
                // .andExpect(handler().methodName("create")) //验证执行的控制器方法名
                // .andExpect(model().hasNoErrors()) //验证页面没有错误
                // .accept(MediaType.APPLICATION_JSON)) //执行请求
                // .andExpect(status().isBadRequest()) //400错误请求

        // Assert.assertNotNull(result.getModelAndView().getModel().get("user")); //自定义断

        session = (MockHttpSession) result.getRequest().getSession();
        // System.out.println(session);
    }

    @Test
    public void edit() {
    }

    @Test
    public void create() {
    }

    @Test
    public void delBatch() {
    }
}