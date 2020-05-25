package com.hou.gmallsearchservice;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.PmsSearchSkuInfo;
import com.hou.gmall.bean.PmsSkuInfo;
import com.hou.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    JestClient jestClient;


    @Test
    public   void search() throws IOException {
        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","39");
        boolQueryBuilder.filter(termQueryBuilder);

        TermQueryBuilder termQueryBuilder1 = new TermQueryBuilder("skuAttrValueList.valueId", "43");
        boolQueryBuilder.filter(termQueryBuilder1);

//        TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId","["39","40","41"]");
//        boolQueryBuilder.filter(termsQueryBuilder);


        //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","华为");
        boolQueryBuilder.must(matchQueryBuilder);
         //query
        searchSourceBuilder.query(boolQueryBuilder);

        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        searchSourceBuilder.highlight(null);

        String dslStr = searchSourceBuilder.toString();
        System.out.println(dslStr);


        //用api执行复杂查询
        List<PmsSearchSkuInfo> infos = new ArrayList<>();
        /*"{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "         {\n" +
                "            \"terms\": {\n" +
                "              \"skuAttrValueList.valueId\": \n" +
                "                    [\"39\",\"40\",\"41\"]\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"term\": {\n" +
                "              \"skuAttrValueList.valueId\": \"39\"\n" +
                "            }\n" +
                "          },\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "          \"skuAttrValueList.valueId\": \"43\"\n" +
                "           }\n" +
                "      }\n" +
                "      ],\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match\": {\n" +
                "          \"skuName\": \"华为\"\n" +
                "          }\n" +
                "        }\n" +
                "        \n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}"*/
        Search build = new Search.Builder(dslStr).addIndex("gmallpms").addType("pmsSkuInfo").build();
        SearchResult execute = jestClient.execute(build);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit: hits) {
            PmsSearchSkuInfo source = hit.source;
            infos.add(source);
        }
        System.out.println(infos.size());
    }
    @Test
    public void contextLoads() throws IOException, InvocationTargetException, IllegalAccessException {

        //查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();
        pmsSkuInfoList = skuService.getAllSku("61");
        //转化为ES的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo :
                pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSearchSkuInfo, pmsSkuInfo);//工具类
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }

        //导入es   其实是http请求  和写数据库的不一样
        for (PmsSearchSkuInfo pmsSearchSkuInfo :
                pmsSearchSkuInfoList) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmallpms").type("pmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(build);

        }
    }

}
