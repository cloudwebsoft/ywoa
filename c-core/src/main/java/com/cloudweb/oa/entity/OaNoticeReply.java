package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OaNoticeReply extends Model<OaNoticeReply> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对应notice表中id
     */
    private Long noticeId;

    /**
     * 回复用户username
     */
    private String userName;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 0未读，1已读
     */
    private String isReaded;

    /**
     * 查看时间
     */
    private LocalDateTime readTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

/*    @TableField(exist=false)
    private String realName;*/

    @TableField(exist=false)
    private User user;
}
