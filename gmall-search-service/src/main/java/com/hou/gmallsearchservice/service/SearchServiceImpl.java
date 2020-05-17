package com.hou.gmallsearchservice.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.hou.gmall.bean.PmsSearchParam;
import com.hou.gmall.bean.PmsSearchSkuInfo;
import com.hou.gmall.bean.PmsSkuAttrValue;
import com.hou.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;

    private String getSearchDsl(PmsSearchParam pmsSearchParam) {
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String keyWord = pmsSearchParam.getKeyWord();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        System.out.println("keyWord--->" + keyWord);
        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuAttrValueList != null) {
            for (String valueId : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }

        //must
        if (StringUtils.isNotBlank(keyWord)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyWord);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);

        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:purple'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

        //sort   SortOrder是个枚举类
//        searchSourceBuilder.sort("id",SortOrder.DESC); 按ID查询不出，可能因为id 设置的string类型导致的
        searchSourceBuilder.sort("price", SortOrder.DESC);
        String dslStr = searchSourceBuilder.toString();
        System.out.println(dslStr);
        return dslStr;
    }

   /*
      前端list页面使用 （catalog3Id  销售属性集合  关键字）  查找 sku的集合
    */
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        String searchDsl = getSearchDsl(pmsSearchParam);
        //用api执行复杂查询
        List<PmsSearchSkuInfo> infos = new ArrayList<>();
        Search build = new Search.Builder(searchDsl).addIndex("gmallpms").addType("pmsSkuInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);

            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;
                Map<String, List<String>> highlight = hit.highlight;
                if (highlight != null && highlight.size() != 0) {
                    String skuName = highlight.get("skuName").get(0); //List里只有一条，skuName每条数据 永远只有一个
                    System.out.println("高亮内容-》》  " + skuName);
                    source.setSkuName(skuName);
                }
                infos.add(source);
            }
            System.out.println(infos.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return infos;
    }
}

