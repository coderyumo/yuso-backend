package com.yupi.yusobackend.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.exception.BusinessException;
import com.yupi.yusobackend.model.entity.Picture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @description: 图片服务实现类
 * @author: yumo
 * @create: 2024-01-13 22:18
 **/
@Service
public class PictureDatasource implements DataSource<Picture> {


    @Override
    public Page<Picture> doSearch(String searchText, int pageNum, int size) {
        int current = (pageNum - 1) * size;
        String url = String.format("https://www.bing.com/images/search?q=%s&form=HDRSC3&first=%s", searchText, current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据处理异常");
        }
        Elements elements = doc.select(".iuscp.isv");
        ArrayList<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("turl");
            // 取图片标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
            if (pictures.size() > size) {
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum,size);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}
