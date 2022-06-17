package com.lee.pojo;

import lombok.Data;

@Data
public class Message<T> {

    private T content;


}
