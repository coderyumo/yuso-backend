package com.yupi.yusobackend;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yusobackend.model.entity.Picture;
import com.yupi.yusobackend.model.entity.Post;
import com.yupi.yusobackend.service.PostService;
import javafx.geometry.Pos;
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
    void testData(){
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
        Map<String,Object> map = JSONUtil.toBean(result,Map.class);
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
        String url = String.format("https://www.bing.com/images/search?q=小黑子&form=HDRSC3&first=%s",current);
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        ArrayList<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址
            String m = element.select(".iusc").get(0).attr("m");
            Map<String,Object> map = JSONUtil.toBean(m,Map.class);
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

}
