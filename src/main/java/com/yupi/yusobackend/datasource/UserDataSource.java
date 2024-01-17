package com.yupi.yusobackend.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.model.dto.user.UserQueryRequest;
import com.yupi.yusobackend.model.vo.UserVO;
import com.yupi.yusobackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户服务实现
 *
 * @author yumo
 *  
 */
@Service
@Slf4j
public class UserDataSource implements DataSource<UserVO> {

    @Resource
    private UserService userService;


    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long size) {

        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserName(searchText);
        userQueryRequest.setCurrent(pageNum);
        userQueryRequest.setPageSize(size);
        Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);

        return userVOPage;
    }
}
