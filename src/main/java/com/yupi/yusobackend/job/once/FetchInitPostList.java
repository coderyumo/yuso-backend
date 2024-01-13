package com.yupi.yusobackend.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yusobackend.model.entity.Post;
import com.yupi.yusobackend.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

/**
 * @description:
 * @author: yumo
 * @create: 2024-01-13 21:19
 **/
@Component
@Slf4j
public class FetchInitPostList {

    @Resource
    private PostService postService;

//    @Scheduled(cron = "0 26 21 * * ?")
    public void insertPostList(){
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
        if (b){
            log.info("获取帖子成功,条数为:{}",postArrayList.size());
        }else {
            log.info("获取帖子失败");
        }
    }


}
