package com.yupi.yusobackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yusobackend.model.entity.Picture;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-13 22:18
 **/
public interface PictureService {


    Page<Picture> searchPicture(PictureQueryRequest pictureQueryRequest);
}
