package com.lee.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Detail {

    @JsonIgnore
    private String content ;
    @JsonProperty("id1")
    private String roomId ;

    private String detail ;
}
