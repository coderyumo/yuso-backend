package com.yupi.yusobackend.job.once;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.yupi.yusobackend.esdao.PostEsDao;
import com.yupi.yusobackend.model.dto.post.PostEsDTO;
import com.yupi.yusobackend.model.entity.Post;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步帖子到 es
 *
 * @author yumo
 *  
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class IncrementSyncPostToEs  {

    @Resource
    private PostEsDao postEsDao;


    @Scheduled(initialDelay = 0,fixedRate =Long.MAX_VALUE)
    public void listenMysql() {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmptyCount = Integer.MAX_VALUE;
            while (emptyCount < totalEmptyCount) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    emptyCount = 0;
                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            System.out.println("empty too many times, exit");
        } finally {
            connector.disconnect();
        }
    }


    private  void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------&gt; before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------&gt; after");
                    List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                    ArrayList<Post> postList = new ArrayList<>();
                    if (CollUtil.isNotEmpty(afterColumnsList)) {
                        Post post = new Post();

                        for (CanalEntry.Column column : afterColumnsList) {
                            String columnName = column.getName();
                            String columnValue = column.getValue();

                            // 根据列名设置对应的属性值
                            switch (columnName) {
                                case "id":
                                    post.setId(Long.parseLong(columnValue));
                                    break;
                                case "title":
                                    post.setTitle(columnValue);
                                    break;
                                case "thumbNum":
                                    post.setThumbNum(Integer.valueOf(columnValue));
                                    break;
                                case "favourNum":
                                    post.setFavourNum(Integer.valueOf(columnValue));
                                    break;
                                case "content":
                                    post.setContent(columnValue);
                                    break;
                                case "userId":
                                    post.setUserId(Long.parseLong(columnValue));
                                    break;
                                case "createTime":
                                    // 同样，假设 updateTime 也是字符串表示的时间
                                    post.setCreateTime(DateUtil.parseDateTime(columnValue));
                                    break;
                                case "updateTime":
                                    // 同样，假设 updateTime 也是字符串表示的时间
                                    post.setUpdateTime(DateUtil.parseDateTime(columnValue));
                                    break;
                                case "isDelete":
                                    post.setIsDelete(Integer.parseInt(columnValue));
                                    break;
                                // 继续设置其他属性...
                            }
                        }

                        // 处理 tags 字段
                        String tagsStr = afterColumnsList.stream()
                                .filter(column -> "tags".equals(column.getName()))
                                .map(CanalEntry.Column::getValue)
                                .findFirst()
                                .orElse(null);

                        if (StringUtils.isNotBlank(tagsStr)) {
                            post.setTags(tagsStr);
                        }
                        postList.add(post);
                        System.out.println("post = " + post);
                    }
                    List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
                    postEsDao.saveAll(postEsDTOList);
                }
            }
        }
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
