package com.cloudweb.oa.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Component
@ConditionalOnProperty(name = "mq.type", matchIfMissing = false, havingValue = "RocketMQ")
@RestController
@RequestMapping("/message")
public class TestRocketMqController {

    @Autowired
    private RocketMqProducer rocketMqProducer;

    @RequestMapping("/send")
    public void sendMsg() {
        rocketMqProducer.sendMessage("我来测试一下rocketmq");

        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setTitle("好的啊");
        msgInfo.setContent("是的，这里");
        rocketMqProducer.sendMsgInfo(msgInfo);
    }
}
