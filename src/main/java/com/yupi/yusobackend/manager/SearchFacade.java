package com.yupi.yusobackend.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.datasource.*;
import com.yupi.yusobackend.exception.BusinessException;
import com.yupi.yusobackend.exception.ThrowUtils;
import com.yupi.yusobackend.model.dto.search.SearchRequest;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.enums.SearchTypeEnum;
import com.yupi.yusobackend.model.vo.PostVO;
import com.yupi.yusobackend.model.vo.SearchVO;
import com.yupi.yusobackend.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
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
    private PictureDatasource pictureDatasource;

    @Resource
    private UserDatasource userDatasource;

    @Resource
    private PostDatasource postDatasource;

    @Resource
    DatasourceRegistry datasourceRegistry;

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {

        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        if (StrUtil.isNotBlank(type)) {
            ThrowUtils.throwIf(searchTypeEnum == null, ErrorCode.PARAMS_ERROR);
        }
        // 如果为空，搜索全部数据
        String searchText = searchRequest.getSearchText();
        int current = searchRequest.getCurrent();
        int pageSize = searchRequest.getPageSize();
        if (StringUtils.isBlank(type)) {
            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                Page<Picture> picturePage = pictureDatasource.doSearch(searchText, current, pageSize);
                return picturePage;
            });

            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                Page<UserVO> userPage = userDatasource.doSearch(searchText, current, pageSize);
                return userPage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                Page<PostVO> postPage = postDatasource.doSearch(searchText, current, pageSize);
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
                return searchVO;
            } catch (Exception e) {
                log.error("查询数据异常:{}", e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取数据失败");
            }
        } else {
            SearchVO searchVO = new SearchVO();
            DataSource<T> dataSource =  datasourceRegistry.getDatasourceByType(type);
            Page<?> page = dataSource.doSearch(searchText, current, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }
    }


}
