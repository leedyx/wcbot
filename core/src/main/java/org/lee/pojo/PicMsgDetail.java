package org.lee.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PicMsgDetail {
    @JsonIgnore
    private String content;
    @JsonProperty("id1")
    private String roomId;

    private String detail;
}
