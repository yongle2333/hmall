package com.qiu.item;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.item.domain.po.Item;
import com.qiu.item.domain.po.ItemDoc;
import com.qiu.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.license.LicensesStatus;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.List;

/**
 * @author qiu
 * @version 1.0
 */
@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocumentTest {
    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;


    //新增文档  既能修改，又能新增（全量修改）
    @Test
    void testIndexDoc() throws IOException {
        //0.准备文档数据
        //查询数据库
        Item item = itemService.getById(100000011127L);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);

        //1.准备request
        IndexRequest request = new IndexRequest("items").id(item.getId().toString());
        //2.准备请求参数
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        //3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }


    //批量新增
    @Test
    void testBuliDoc() throws IOException {
        int pageNum = 1, pageSize = 500;
        while (true) {
            //1.准备文档数据
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNum, pageSize));
            List<Item> record = page.getRecords();
            if(record == null || record.isEmpty()){
                return;
            }
            //2.准备请求request
            BulkRequest request = new BulkRequest();
            //3.准备请求参数
            for (Item item :
                    record) {
                request.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)),XContentType.JSON));
            }
            //4.发送请求
            client.bulk(request,RequestOptions.DEFAULT);

            pageNum++;
        }

    }


    //查询文档
    @Test
    void testGetDoc() throws IOException {


        //1.准备request
        GetRequest request = new GetRequest("items", "100000011127");

        //2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        //3.解析出响应的src
        String json = response.getSourceAsString();
        ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println("doc = " + doc); //快捷键soutv直接输出
    }






    @BeforeEach
    void setUp() {
        //客户端初始化
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.45.142:9200")
        ));
    }


    //单元测试结束要销毁
    @AfterEach
    void tearDown() throws IOException {
        if (client != null){
            client.close();
        }
    }
}
