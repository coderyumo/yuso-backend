package com.yupi.yusobackend.datasource;

import com.yupi.yusobackend.model.enums.SearchTypeEnum;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-14 15:16
 **/
@Component
public class DataSourceRegistry {

    @Resource
    private PictureDataSource pictureDatasource;

    @Resource
    private UserDataSource userDatasource;

    @Resource
    private PostDataSource postDatasource;

    @Resource
    private VideoBiLiDataSource videoBiLiDataSource;

    @Resource
    private VideoBingDataSource videoBingDataSource;

    private Map<String, DataSource<T>> typeDatasourceMap;

    @PostConstruct
    public void doInit() {
        typeDatasourceMap = new HashMap() {
            {
                put(SearchTypeEnum.POST.getValue(), postDatasource);
                put(SearchTypeEnum.PICTURE.getValue(), pictureDatasource);
                put(SearchTypeEnum.VIDEO_BILI.getValue(), videoBiLiDataSource);
                put(SearchTypeEnum.VIDEO_BING.getValue(), videoBingDataSource);
                put(SearchTypeEnum.USER.getValue(), userDatasource);
            }
        };
    }


    public DataSource getDataSourceByType(String type) {
        if (typeDatasourceMap==null){
            return null;
        }
        return typeDatasourceMap.get(type);
    }

}
