package org.lee.pojo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import org.lee.util.StringNullSerializer;

@Data
@Builder
public class SendMsgInfo {

    private String id;

    private int type;

    private String wxid;

    @JsonSerialize(nullsUsing = StringNullSerializer.class)
    private String roomid;

    private String content;

    @JsonSerialize(nullsUsing = StringNullSerializer.class)
    private String nickname;

    @JsonSerialize(nullsUsing = StringNullSerializer.class)
    private String ext;





}
