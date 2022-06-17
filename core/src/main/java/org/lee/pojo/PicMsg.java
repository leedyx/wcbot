package org.lee.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PicMsg {

    @JsonProperty("content")
    private PicMsgDetail picMsgDetail;
}
