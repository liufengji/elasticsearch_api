package com.victor.elasticsearch.ik;


import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class IK_Elasticsearch {

    private TransportClient client;

    //TODO 获取Transport Client
    @SuppressWarnings("unchecked")
    @Before
    public void getClient() throws Exception {

        // 1 设置连接的集群名称
        Settings settings = Settings.builder().put("cluster.name", "my-application").build();

        // 2 连接集群
        client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("hadoop102"), 9300));

        // 3 打印集群名称
        System.out.println(client.toString());
    }

    //TODO 创建索引(数据库)
    @Test
    public void createIndex() {
        //创建索引
        client.admin().indices().prepareCreate("blog4").get();
        //关闭资源
        client.close();
    }

    //TODO 创建使用ik分词器的mapping
    @Test
    public void createMapping() throws Exception {

        // 1设置mapping
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("article")
                .startObject("properties")
                .startObject("id1")
                .field("type", "string")
                .field("store", "yes")
                .field("analyzer","ik_smart")
                .endObject()
                .startObject("title2")
                .field("type", "string")
                .field("store", "no")
                .field("analyzer","ik_smart")
                .endObject()
                .startObject("content")
                .field("type", "string")
                .field("store", "yes")
                .field("analyzer","ik_smart")
                .endObject()
                .endObject()
                .endObject()
                .endObject();

        // 2 添加mapping
        PutMappingRequest mapping = Requests.putMappingRequest("blog4")
                .type("article").source(builder);

        client.admin().indices().putMapping(mapping).get();

        // 3 关闭资源
        client.close();
    }

    //创建文档,以map形式
    //TODO 插入数据
    @Test
    public void createDocumentByMap() {

        HashMap<String, String> map = new HashMap<>();
        map.put("id1", "2");
        map.put("title2", "Lucene");
        map.put("content", "它提供了一个分布式的web接口");

        IndexResponse response = client.prepareIndex("blog4", "article", "3")
                .setSource(map).execute().actionGet();

        //打印返回的结果
        System.out.println("结果:" + response.getResult());
        System.out.println("id:" + response.getId());
        System.out.println("index:" + response.getIndex());
        System.out.println("type:" + response.getType());
        System.out.println("版本:" + response.getVersion());

        //关闭资源
        client.close();
    }

    //TODO 词条查询
    @Test
    public void queryTerm() {

        SearchResponse response = client.prepareSearch("blog4").setTypes("article")
                .setQuery(QueryBuilders.termQuery("content","提供")).get();

        //获取查询命中结果
        SearchHits hits = response.getHits();

        System.out.println("结果条数:" + hits.getTotalHits());

        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
