package com.yupi.yusobackend.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yusobackend.model.dto.post.PostQueryRequest;
import com.yupi.yusobackend.model.entity.Post;
import com.yupi.yusobackend.model.vo.PostVO;
import com.yupi.yusobackend.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务实现
 *
 * @author yumo
 *  
 */
@Service
@Slf4j
public class PostDatasource  implements DataSource<PostVO> {

    @Resource
    private PostService postService;



    @Override
    public Page<PostVO> doSearch(String searchText, int pageNum, int size) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(pageNum);
        postQueryRequest.setPageSize(size);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        return postService.getPostVOPage(postPage,request);
    }
}




