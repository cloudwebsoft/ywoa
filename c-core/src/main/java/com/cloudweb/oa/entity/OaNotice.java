package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author fgf
 * @since 2019-12-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Component
public class OaNotice extends Model<OaNotice> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
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

    @TableField("flowId")
    private Integer flowId;

    private Integer isReply;

    private Integer isForcedResponse;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @TableField(exist=false)
    private List<OaNoticeAttach> oaNoticeAttList;

    @TableField(exist=false)
    private User user;
}
