package com.cloudweb.oa.vo;

import com.cloudweb.oa.entity.OaNoticeAttach;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OaNoticeVO {

    private Long id;

    private String title;

    private String content;

    private String userName;

    private LocalDateTime createDate;

    private Boolean isDeptNotice;

    private String usersKnow;

    private Boolean isShow;

    private LocalDate beginDate;

    private LocalDate endDate;

    private String color;

    private Integer isBold;

    private String unitCode;

    private Integer noticeLevel;

    private Integer isAll;

    private Integer flowId;

    private Integer isReply;

    private Integer isForcedResponse;

    /**
     * 是否曾经读过
     */
    private boolean readed;

    /**
     * 是否为新的
     */
    private boolean fresh;

    private String realName;

    private List<OaNoticeReply> oaNoticeReplyList;

    /**
     * 是否已回复过
     */
    private boolean notReplied;

    private List<OaNoticeAttach> oaNoticeAttList;

    private User user;

    /**冗余类别
     */
    private String kind;

    /**
     * title渲染后的效果
     */
    private String caption;

}
