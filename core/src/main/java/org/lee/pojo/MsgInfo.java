package org.lee.pojo;

import lombok.Data;

/**
 * 归一化的消息
 */
@Data
public class MsgInfo {

    private String id;

    private int type;

    private String content;

    private String roomId;

    private String senderId;

    private String roomNick;
}
