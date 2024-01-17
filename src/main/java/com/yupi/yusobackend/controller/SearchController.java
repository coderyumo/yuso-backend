package com.yupi.yusobackend.controller;

import com.yupi.yusobackend.common.BaseResponse;
import com.yupi.yusobackend.common.ResultUtils;
import com.yupi.yusobackend.manager.SearchFacade;
import com.yupi.yusobackend.model.dto.search.SearchRequest;
import com.yupi.yusobackend.model.vo.SearchVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
    private SearchFacade searchFacade;


    /**
     * 搜索全部
     * @param searchRequest
     * @return
     */
    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        return ResultUtils.success(searchFacade.searchAll(searchRequest, request));
    }


}
