package com.yupi.yusobackend.datasource;

import cn.hutool.core.util.StrUtil;
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
public class VideoBiLiDataSource implements DataSource<Video> {


    @Override
    public Page<Video> doSearch(String searchText, long pageNum, long size) {
        String url = "";
        String select = "";
        if (StrUtil.isBlank(searchText)) {
            url = "https://www.bilibili.com/";
            select = ".bili-video-card";
        } else {
            if (pageNum == 1) {
                url = String.format("https://search.bilibili.com/all?vt=92448098&keyword=%s&from_source=webtop_search" +
                        "&spm_id_from=333.1007&search_source=3", searchText);
            } else {
                url = String.format("https://search.bilibili.com/all?vt=92448098&keyword=%s&from_source=webtop_search" +
                        "&spm_id_from=333.1007&search_source=3&page=%s", searchText, pageNum);
            }
            select = ".video-list";
        }
//        String url = String.format("https://search.bilibili.com/all?vt=92448098&keyword=小黑子&from_source=webtop_search&spm_id_from=333.1007&search_source=3");
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据处理异常");
        }
        Elements elements = doc.select(select);

        ArrayList<Video> videos = new ArrayList<>();
        // 读取<a>标签的href值
        Elements aTags = elements.select(".bili-video-card__info--right");
        for (Element aTag : aTags) {
            Elements children = aTag.children();
            String hrefValue = children.get(0).select("a").attr("href");
            String title = children.get(0).select("h3").attr("title");
            Video video = new Video();
            video.setVideoUrl(hrefValue);
            video.setTitle(title);
            videos.add(video);
        }

        // 读取<picture>中<img>的alt和src值
        Elements pictureTags = elements.select("picture");
        for (Element pictureTag : pictureTags) {
            Element imgTag = pictureTag.select("img").first();
            Element element = pictureTag.select("source").get(1);
            for (Video video : videos) {
                video.setImageUrl(element.attr("srcset"));
            }
        }

        Page<Video> videoPage = new Page<>(pageNum, size);
        videoPage.setRecords(videos);
        return videoPage;
    }


}
