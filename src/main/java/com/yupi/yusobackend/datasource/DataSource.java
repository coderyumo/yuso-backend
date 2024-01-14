package com.yupi.yusobackend.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @description: 数据源接口，定义规范（新接入的数据源必须实现）
 * @author: yumo
 * @create: 2024-01-14 14:22
 **/
public interface DataSource<T> {


    /**
     * 搜索
     * @param searchText
     * @param pageNum
     * @param size
     * @return
     */
    Page<T> doSearch(String searchText, int pageNum,int size);
}
