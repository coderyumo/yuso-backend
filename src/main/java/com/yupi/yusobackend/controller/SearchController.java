package com.yupi.yusobackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.BaseResponse;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.common.ResultUtils;
import com.yupi.yusobackend.exception.BusinessException;
import com.yupi.yusobackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yusobackend.model.dto.post.PostQueryRequest;
import com.yupi.yusobackend.model.dto.search.SearchAllRequest;
import com.yupi.yusobackend.model.dto.user.UserQueryRequest;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.entity.Post;
import com.yupi.yusobackend.model.vo.PostVO;
import com.yupi.yusobackend.model.vo.SearchVO;
import com.yupi.yusobackend.model.vo.UserVO;
import com.yupi.yusobackend.service.PictureService;
import com.yupi.yusobackend.service.PostService;
import com.yupi.yusobackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-13 23:47
 **/
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {


    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;


    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchAllRequest searchAllRequest, HttpServletRequest request) {

        String searchText = searchAllRequest.getSearchText();
        int current = searchAllRequest.getCurrent();
        int pageSize = searchAllRequest.getPageSize();

        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
            PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
            pictureQueryRequest.setSearchText(searchText);
            pictureQueryRequest.setCurrent(current);
            pictureQueryRequest.setPageSize(pageSize);
            Page<Picture> picturePage = pictureService.searchPicture(pictureQueryRequest);
            return picturePage;
        });

        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setUserName(searchText);
            userQueryRequest.setCurrent(current);
            userQueryRequest.setPageSize(pageSize);
            Page<UserVO> userPage = userService.listUserVOByPage(userQueryRequest);
            return userPage;
        });

        CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
            PostQueryRequest postQueryRequest = new PostQueryRequest();
            postQueryRequest.setSearchText(searchText);
            postQueryRequest.setCurrent(current);
            postQueryRequest.setPageSize(pageSize);
            Page<PostVO> postPage = postService.listPostVOByPage(postQueryRequest, request);
            return postPage;
        });

        CompletableFuture.allOf(userTask, pictureTask, postTask).join();
        try {
            Page<UserVO> userVOPage = userTask.get();
            Page<Picture> picturePage = pictureTask.get();
            Page<PostVO> postVOPage = postTask.get();
            SearchVO searchVO = new SearchVO();
            searchVO.setUserList(userVOPage.getRecords());
            searchVO.setPostList(postVOPage.getRecords());
            searchVO.setPictureList(picturePage.getRecords());
            return ResultUtils.success(searchVO);
        } catch (Exception e) {
            log.error("查询数据异常:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取数据失败");
        }
    }


}
