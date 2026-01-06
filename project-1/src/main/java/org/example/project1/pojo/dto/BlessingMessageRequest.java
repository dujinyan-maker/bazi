package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 祝福语发送请求DTO
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Data
public class BlessingMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 祝福语内容
     */
    private String content;
}

