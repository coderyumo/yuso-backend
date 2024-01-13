package com.yupi.yusobackend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.exception.BusinessException;
import com.yupi.yusobackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.service.PictureService;
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
public class PictureServiceImpl implements PictureService {


    @Override
    public Page<Picture> searchPicture(PictureQueryRequest pictureQueryRequest) {

        String searchText = pictureQueryRequest.getSearchText();
        int pageNum = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();

        int current = (pageNum - 1) * pageSize;
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
            if (pictures.size() > pageSize) {
                break;
            }
        }

        Page<Picture> picturePage = new Page<>(pageNum,pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}
