package org.lee.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PicMsgDetail {

    @JsonProperty("id1")
    private String roomId;

    @JsonProperty("id2")
    private String senderId;

    private String detail;
}
