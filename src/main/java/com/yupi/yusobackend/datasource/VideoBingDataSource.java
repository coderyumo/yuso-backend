package com.yupi.yusobackend.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.common.ErrorCode;
import com.yupi.yusobackend.exception.BusinessException;
import com.yupi.yusobackend.model.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-14 14:49
 **/
@Component
public class VideoBingDataSource implements DataSource<Video> {


    @Override
    public Page<Video> doSearch(String searchText, long pageNum, long size) {
        long current = (pageNum - 1) * size;
        String url = String.format("https://cn.bing.com/videos/search?q=%s&qs=ds&form=QBVR&first=%s",searchText, current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据处理异常");
        }
        Elements elements = doc.select(".dg_u");
        ArrayList<Video> videos = new ArrayList<>();
        for (Element element : elements) {
            Video video = new Video();
            // 查找包含aria-label、alt和src属性的元素
            element = element.select("div.mc_vtvc_con_rc").first();

            // 获取ourl属性的值
            String ourlValue = element.attr("ourl");
            video.setVideoUrl(ourlValue);

            // 获取aria-label属性的值作为title
            Elements imgs = element.getElementsByTag("img");
            if (imgs.size() > 0) {
                // 获取title属性的值
                String titleValue = imgs.get(0).attr("alt");
                video.setTitle(titleValue);
                // 获取src属性的值
                String srcValue = element.select("img").attr("src");
                video.setImageUrl(srcValue);
            }else {
                String titleValue = element.select(".rms_iac").get(0).attr("data-alt");
                video.setTitle(titleValue);
                String srcValue = element.select(".rms_iac").get(0).attr("data-src");
                video.setImageUrl(srcValue);
            }
            videos.add(video);
        }
        Page<Video> videoPage = new Page<>(pageNum, size);
        videoPage.setRecords(videos);
        return videoPage;
    }


}
