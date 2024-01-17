package com.yupi.yusobackend.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.datasource.*;
import com.yupi.yusobackend.exception.BusinessException;
import com.yupi.yusobackend.exception.ThrowUtils;
import com.yupi.yusobackend.model.dto.post.PostQueryRequest;
import com.yupi.yusobackend.model.dto.search.SearchRequest;
import com.yupi.yusobackend.model.dto.user.UserQueryRequest;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.entity.Video;
import com.yupi.yusobackend.model.enums.SearchTypeEnum;
import com.yupi.yusobackend.model.vo.PostVO;
import com.yupi.yusobackend.model.vo.SearchVO;
import com.yupi.yusobackend.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-14 13:42
 **/
@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private VideoBiLiDataSource videoBiLiDataSource;
    @Resource
    private  VideoBingDataSource VideoBingDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    // [编程学习交流圈](https://www.code-nav.cn/) 快速入门编程不走弯路！30+ 原创学习路线和专栏、500+ 编程学习指南、1000+ 编程精华文章、20T+ 编程资源汇总

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        if (StrUtil.isNotBlank(type)) {
            ThrowUtils.throwIf(searchTypeEnum == null, ErrorCode.PARAMS_ERROR);
        }
        // 如果为空，搜索全部数据
        String searchText = searchRequest.getSearchText();
        long current = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();
        // 搜索出所有数据
        if (searchTypeEnum == null) {
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                UserQueryRequest userQueryRequest = new UserQueryRequest();
                userQueryRequest.setUserName(searchText);
                Page<UserVO> userVOPage = userDataSource.doSearch(searchText, current, pageSize);
                return userVOPage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                PostQueryRequest postQueryRequest = new PostQueryRequest();
                postQueryRequest.setSearchText(searchText);
                Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize);
                return postVOPage;
            });

            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                Page<Picture> picturePage = pictureDataSource.doSearch(searchText, 1, 10);
                return picturePage;
            });

            CompletableFuture<Page<Video>> videoBiLiTask = CompletableFuture.supplyAsync(() -> {
                Page<Video> videoBiLiPage = videoBiLiDataSource.doSearch(searchText, 1, 10);
                return videoBiLiPage;
            });

            CompletableFuture<Page<Video>> videoBingTask = CompletableFuture.supplyAsync(() -> {
                Page<Video> videoBingPage = VideoBingDataSource.doSearch(searchText, 1, 10);
                return videoBingPage;
            });

            CompletableFuture.allOf(userTask, postTask, pictureTask).join();
            try {
                Page<UserVO> userVOPage = userTask.get();
                Page<PostVO> postVOPage = postTask.get();
                Page<Picture> picturePage = pictureTask.get();
                Page<Video> videoBiLiPage = videoBiLiTask.get();
                Page<Video> videoBingPage = videoBingTask.get();
                SearchVO searchVO = new SearchVO();
                searchVO.setUserList(userVOPage.getRecords());
                searchVO.setPostList(postVOPage.getRecords());
                searchVO.setPictureList(picturePage.getRecords());
                searchVO.setVideoBiLiList(videoBiLiPage.getRecords());
                searchVO.setVideoBingList(videoBingPage.getRecords());
                return searchVO;
            } catch (Exception e) {
                log.error("查询异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        } else {
            SearchVO searchVO = new SearchVO();
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
            Page<?> page = dataSource.doSearch(searchText, current, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }
    }


}
