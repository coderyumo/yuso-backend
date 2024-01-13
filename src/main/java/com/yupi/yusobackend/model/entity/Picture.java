package com.yupi.yusobackend.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-13 21:58
 **/
@Data
public class Picture implements Serializable {

    private static final long serialVersionUID = -8367894577586764279L;

    private String title;

    private String url;

}
