package org.lee.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TxtMsg {

    private String id;

    private String content;

    @JsonProperty("id1")
    private String senderId;

    @JsonProperty("wxid")
    private String roomId;

}
