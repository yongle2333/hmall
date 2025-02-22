package com.qiu.item;

import cn.hutool.json.JSONUtil;
import com.qiu.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author qiu
 * @version 1.0
 */

public class ElasticSearchTest {

    private RestHighLevelClient client;


    @Test
    void testMatchAll() throws IOException {
        //1.构建request对象
        SearchRequest request = new SearchRequest("items");
        //2.构建参数
        request.source()
                .query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseResponseResult(response);
    }

    @Test
    void testSearch() throws IOException {
        //1.构建request对象
        SearchRequest request = new SearchRequest("items");
        //2.组织DSL参数  复合查询 1.关键字为”脱脂牛奶“ 2.品牌为德亚 3.价格低于300
        request.source().query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("name","脱脂牛奶"))
                .filter(QueryBuilders.termQuery("brand.keyword","德亚"))
                .filter(QueryBuilders.rangeQuery("price").lt(30000))
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        parseResponseResult(response);
    }

    @Test
    void testSortAndPage() throws IOException {

        //模拟前端传递的分页  (页码-1)*每页大小 = from
        int pageNum = 2, pageSize = 5;

        //1.构建request对象
        SearchRequest request = new SearchRequest("items");
        //2.组织DSL参数
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((pageNum-1)*pageSize).size(pageSize);
        request.source().sort("sold", SortOrder.DESC)
                        .sort("price",SortOrder.ASC);
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        parseResponseResult(response);
    }


    @Test
    void testHighLight() throws IOException {

        //1.构建request对象
        SearchRequest request = new SearchRequest("items");
        //2.组织DSL参数
        //query条件
        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"));
        //高亮条件
        request.source().highlighter(
                SearchSourceBuilder.highlight()
                        .field("name")  //默认pre post就是<em> </em>
        );


        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        parseResponseResult(response);
    }



    @Test
    void testAgg() throws IOException {

        //1.构建request对象
        SearchRequest request = new SearchRequest("items");
        //2.1分页
        request.source().size(0);
        //2.2聚合条件
        String brandAggName = "brangAgg";
        request.source().aggregation(
                AggregationBuilders.terms(brandAggName).field("brand.keyword").size(10)
        );

        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        Aggregations aggregations = response.getAggregations();
        //4.1根据聚合名称获取对应的聚合
        Terms brandTerms = aggregations.get(brandAggName);  //Aggregation是父类接口，得找子类对应的桶
        //4.2获取bucket
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //4.3遍历每一个桶
        for (Terms.Bucket bucket : buckets) {
            System.out.println("brand = " + bucket.getKeyAsString());
            System.out.println("count = " + bucket.getDocCount());
        }

    }







    private static void parseResponseResult(SearchResponse response){
        SearchHits searchHits = response.getHits();
        //总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("total = " + total);
        //命中数据
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            //获取source结果
            String json = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            //处理高亮结果
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            if( hfs != null && !hfs.isEmpty()){
                //根据高亮字段名，获取高亮结果
                HighlightField hf = hfs.get("name");
                //获取高亮结果，覆盖高亮结果
                String hfName = hf.getFragments()[0].toString();  //数组字符串拼接
                doc.setName(hfName);
            }
            System.out.println("doc = " + doc);
        }
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
