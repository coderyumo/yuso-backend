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
public class DatasourceRegistry {

    @Resource
    private PictureDatasource pictureDatasource;

    @Resource
    private UserDatasource userDatasource;

    @Resource
    private PostDatasource postDatasource;

    private Map<String, DataSource<T>> typeDatasourceMap;

    @PostConstruct
    public void doInit() {
        typeDatasourceMap = new HashMap() {
            {
                put(SearchTypeEnum.POST.getValue(), postDatasource);
                put(SearchTypeEnum.PICTURE.getValue(), pictureDatasource);
                put(SearchTypeEnum.USER.getValue(), userDatasource);
            }
        };
    }


    public DataSource getDatasourceByType(String type) {
        if (typeDatasourceMap==null){
            return null;
        }
        return typeDatasourceMap.get(type);
    }

}
