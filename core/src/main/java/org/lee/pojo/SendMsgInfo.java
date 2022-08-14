package org.lee.pojo;

import lombok.Data;

@Data
public class SendMsgInfo {

    private String id;

    private String content;

    private String wxid;

    private int type;
}
