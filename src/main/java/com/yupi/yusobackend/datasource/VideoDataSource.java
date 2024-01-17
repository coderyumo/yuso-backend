package com.yupi.yusobackend.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-14 14:49
 **/
public class VideoDataSource implements DataSource<Object>{


    @Override
    public Page<Object> doSearch(String searchText, int pageNum, int size) {

        return null;
    }
}
