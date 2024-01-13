package com.yupi.yusobackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.entity.Post;
import javafx.geometry.Pos;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 聚合搜索
 *
 * @author yumo
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class SearchVO implements Serializable {

    private static final long serialVersionUID = 980379593568223655L;

    List<UserVO> userList;

    List<PostVO> postList;

    List<Picture> pictureList;
}
