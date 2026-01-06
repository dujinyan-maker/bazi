package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 祝福语响应DTO
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Data
public class BlessingMessageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 祝福语ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 祝福语内容
     */
    private String content;

    /**
     * 发送时间
     */
    private Date createdTime;
}

