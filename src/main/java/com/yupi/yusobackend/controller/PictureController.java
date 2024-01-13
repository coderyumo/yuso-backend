package com.yupi.yusobackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.BaseResponse;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.common.ResultUtils;
import com.yupi.yusobackend.exception.ThrowUtils;
import com.yupi.yusobackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yusobackend.model.dto.post.PostQueryRequest;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.entity.Post;
import com.yupi.yusobackend.model.vo.PostVO;
import com.yupi.yusobackend.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-13 22:06
 **/
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;


    /**
     * 分页获取列表（封装类）
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                           HttpServletRequest request) throws IOException {

        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Picture> picturePage = pictureService.searchPicture(pictureQueryRequest);

        return ResultUtils.success(picturePage);
    }


}
