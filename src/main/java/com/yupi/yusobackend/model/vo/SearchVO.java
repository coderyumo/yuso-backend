package com.yupi.yusobackend.model.vo;

import com.yupi.yusobackend.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合搜索
 *
 * @author yumo
 *  
 */
@Data
public class SearchVO implements Serializable {

    private static final long serialVersionUID = 980379593568223655L;

    private List<UserVO> userList;

    private List<PostVO> postList;

    private  List<Picture> pictureList;

    private List<?> dataList;
}
