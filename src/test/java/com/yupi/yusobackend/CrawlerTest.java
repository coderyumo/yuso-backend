package com.yupi.yusobackend;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.entity.Post;
import com.yupi.yusobackend.model.entity.Video;
import com.yupi.yusobackend.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-13 20:56
 **/
@SpringBootTest
public class CrawlerTest {

    @Resource
    private PostService postService;

    @Test
    void testData() {
        String json = "{\n" +
                "    \"current\": 1,\n" +
                "    \"pageSize\": 10,\n" +
                "    \"sortField\": \"createTime\",\n" +
                "    \"sortOrder\": \"descend\",\n" +
                "    \"category\": \"文章\",\n" +
                "    \"reviewStatus\": 1\n" +
                "}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest
                .post(url)
                .body(json)
                .execute()
                .body();
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        System.out.println("map = " + map);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        ArrayList<Post> postArrayList = new ArrayList<>();
        for (Object record : records) {
            JSONObject jsonObject = (JSONObject) record;
            Post post = new Post();
            post.setTitle(jsonObject.getStr("title"));
            post.setContent(jsonObject.getStr("content"));
            post.setTags(JSONUtil.toJsonStr(jsonObject.getStr("tags")));
            post.setUserId(1L);
            postArrayList.add(post);
        }
        boolean b = postService.saveBatch(postArrayList);
        Assertions.assertTrue(b);
    }


    @Test
    public void testPic() throws IOException {
        int current = 1;
        String url = String.format("https://www.bing.com/images/search?q=小黑子&form=HDRSC3&first=%s", current);
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        ArrayList<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            // 取图片标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
        }
        pictures.forEach(System.out::println);
    }


    @Test
    public void testVideo() throws IOException {
        int current = 1;
        String url = String.format("https://search.bilibili.com/all?vt=92448098&keyword=小黑子&from_source=webtop_search&spm_id_from=333.1007&search_source=3");
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".video-list");

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
            if (imgTag != null) {
                String imgUrl = imgTag.attr("src");
                for (Video video : videos) {
                    video.setImageUrl(imgUrl);
                }
            }
        }
        videos.forEach(System.out::println);

    }


    @Test
    public void testVideoBilibili() throws IOException {
        int current = 1;
        String url = String.format("https://www.bilibili.com/");
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".bili-video-card");

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
            if (imgTag != null) {
                String imgUrl = imgTag.attr("src");
                for (Video video : videos) {
                    video.setImageUrl(imgUrl);
                }
            }
        }
        videos.forEach(System.out::println);

    }


    @Test
    public void testVideoBing() throws IOException {
        int current = 1;
        String url = String.format("https://cn.bing.com/videos/search?q=坤坤&qs=ds&form=QBVR&first=1");
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".dg_u");
        for (Element element : elements) {
            // 查找包含aria-label、alt和src属性的元素
            element = element.select("div.mc_vtvc_con_rc").first();

            // 获取ourl属性的值
            String ourlValue = element.attr("ourl");
            System.out.println("ourl: " + ourlValue);

            // 获取aria-label属性的值作为title
            Elements imgs = element.getElementsByTag("img");
            if (imgs.size() > 0) {
                String titleValue = imgs.get(0).attr("alt");
                System.out.println("title: " + titleValue);
                // 获取src属性的值
                String srcValue = element.select("img").attr("src");
                System.out.println("src: " + srcValue);
            }else {
                String titleValue = element.select(".rms_iac").get(0).attr("data-alt");
                System.out.println("title: " + titleValue);
                String srcValue = element.select(".rms_iac").get(0).attr("data-src");
                System.out.println("srcValue: " + srcValue);
            }
            System.out.println("------------");
        }
    }

}
