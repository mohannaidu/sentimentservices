package org.mimos.sentimentservices.web.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.mimos.sentimentservices.service.StockServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.jhipster.config.JHipsterProperties;

/**
 * REST controller for managing Metadata.
 */
@RestController
@RequestMapping("/api")
public class MetadataResource {

    private final Logger log = LoggerFactory.getLogger(MetadataResource.class);

    private static final String ENTITY_NAME = "metadata";

    private static final int LATEST_NEWS_SIZE = 10;
    
    private static final int ARTICLE_SIZE = 10;
    
    private static final int ANNOUNCEMENT_SIZE = 10;
    
    private static final int HOT_TOPIC_ARTICLE_SIZE = 10;
    
    private final Environment env;

    private final JHipsterProperties jHipsterProperties;
    
    private final RestHighLevelClient client;
    
    private final StockServiceClient stockServiceClient;

    public MetadataResource(Environment env, JHipsterProperties jHipsterProperties, RestHighLevelClient client, StockServiceClient stockServiceClient) {
        this.env = env;
        this.jHipsterProperties = jHipsterProperties;
        this.client = client;
        this.stockServiceClient = stockServiceClient;
    }
    
    /**
     * GET  /metadata : get getTotalArticleBySector
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getTotalArticleBySector/{sector}")
    @Timed
    @Cacheable(value = "totalArticleBySector")
    public Long getTotalArticleBySector(@PathVariable String sector) {
        log.debug("REST request to getTotalArticleBySector");
                
        try {
	        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("sector", sector);
			sourceBuilder.query(matchQueryBuilder); 
			SearchRequest searchRequest = new SearchRequest("article");
	
			searchRequest.source(sourceBuilder);
			SearchResponse response;
			
				response = client.search(searchRequest);
			
			SearchHits results = response.getHits();
	
			return results.getTotalHits();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    /**
     * GET  /metadata : get getTotalArticleByStockName
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getTotalArticleByStockName/{stockName}")
    @Timed
    @Cacheable(value = "getTotalArticleByStockName")
    public Long getTotalArticleByStockName(@PathVariable String stockName) {
        log.debug("REST request to getTotalArticleBySector");
                
        try {
	        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
	        QueryBuilder qb = QueryBuilders.nestedQuery(
			        "entity",               
			        QueryBuilders.boolQuery()           
			                .must(QueryBuilders.matchQuery("entity.name", stockName)), ScoreMode.None
			    );
	        
			sourceBuilder.query(qb); 
			SearchRequest searchRequest = new SearchRequest("article");
	
			searchRequest.source(sourceBuilder);
			SearchResponse response;
			
				response = client.search(searchRequest);
			
			SearchHits results = response.getHits();
	
			return results.getTotalHits();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    /**
     * GET  /metadata : get getTotalArticleBySector
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getLatestNewsBySector/{sector}")
    @Timed
    @Cacheable(value = "getLatestNewsBySector")
    public ResponseEntity<JsonNode> getLatestNewsBySector(@PathVariable String sector) {
        log.debug("REST request to getLatestNews");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("sector", sector);
			sourceBuilder.query(matchQueryBuilder); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.LATEST_NEWS_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));  
			SearchRequest searchRequest = new SearchRequest("article");

			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    
    /**
     * GET  /metadata : get getLatestNewsByStockname
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getLatestNewsByStockName/{stockName}")
    @Timed
    public ResponseEntity<JsonNode> getLatestNewsByStockName(@PathVariable String stockName) {
        log.debug("REST request to getLatestNews");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			QueryBuilder qb = QueryBuilders.nestedQuery(
			        "entity",               
			        QueryBuilders.boolQuery()           
			                .must(QueryBuilders.matchQuery("entity.name", stockName)), ScoreMode.None
			    );
			sourceBuilder.query(qb); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.LATEST_NEWS_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));
			
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getArticleByStockName
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getArticleByStockName/{stockName}")
    @Timed
    public ResponseEntity<JsonNode> getArticleByStockName(@PathVariable String stockName) {
        log.debug("REST request to getArticleByStockName");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			QueryBuilder qb = QueryBuilders.nestedQuery(
			        "entity",               
			        QueryBuilders.boolQuery()           
			                .must(QueryBuilders.matchQuery("entity.name", stockName)), ScoreMode.None
			    );
			sourceBuilder.query(qb); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date","sector"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.ARTICLE_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));			
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getCompanyAnnouncementBySector
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getCompanyAnnouncementBySector/{sector}")
    @Timed
    public ResponseEntity<JsonNode> getCompanyAnnouncementBySector(@PathVariable String sector) {
        log.debug("REST request to getCompanyAnnouncementBySector");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			QueryBuilder qb =  QueryBuilders.boolQuery()           
			                .must(QueryBuilders.matchQuery("sector", sector))
			                .must(QueryBuilders.matchQuery("category", "announcement"));
			sourceBuilder.query(qb); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date","sector"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.ANNOUNCEMENT_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));			
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getHotTopics
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getHotTopicsBySector/{sector}")
    @Timed
    public ResponseEntity<JsonNode> getHotTopicsBySector(@PathVariable String sector) {
        log.debug("REST request to getHotTopicsBySector");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			
			AggregationBuilder aggregation = AggregationBuilders.filter("term", QueryBuilders.termQuery("sector", sector));
			aggregation.subAggregation(AggregationBuilders.terms("term").field("hot_topic"));
			sourceBuilder.size(0);
			
			sourceBuilder.aggregation(aggregation);
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			Aggregations results = response.getAggregations();
			Map<String,Aggregation> aggs = results.getAsMap();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			 for (Map.Entry<String, Aggregation> entry : aggs.entrySet())
	            {
	            	ParsedFilter aggs2 = (ParsedFilter) entry.getValue();
	            	Aggregations apps = aggs2.getAggregations();
	            	Map<String,Aggregation> aggs3 = apps.getAsMap();
	            	 for (Map.Entry<String, Aggregation> entry1 : aggs3.entrySet())
	 	            {
	            		 ParsedStringTerms aggs4 = (ParsedStringTerms) entry1.getValue();
	            		
	 	            	for (Bucket entry2 : aggs4.getBuckets()) {
//	 	            	    System.out.println(entry1.getKey());      // Term
//	 	            	    System.out.println(entry1.getDocCount()); // Doc count
	 	            		//JsonNode jsonNode = new JsonNode();
	 	            		Map<String, String> map = new HashMap<>();
	 	            		//map.put(entry2.getKey(), entry2.getDocCount());
	 	            		
	 	            		map.put("count",String.valueOf((entry2.getDocCount()) ));
	 	            		map.put("topic",String.valueOf(entry2.getKey()));
	 	            		JsonNode 	json = mapper.readValue(mapper.writeValueAsString(map), JsonNode.class);
	 	            		arrayNode.add(json);
	 	            	}
	 	            	
	 	            }

	            }

			 
			
//			ObjectMapper mapper = new ObjectMapper();
//			ArrayNode arrayNode = mapper.createArrayNode();
//			
//			for (SearchHit hit : results) {
//				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
//				arrayNode.add(jsonNode);
//			}
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getMostDiscussedCompany
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getMostDiscussedCompany/{sectorCode}")
    @Timed
    public ResponseEntity<JsonNode> getMostDiscussedCompany(@PathVariable String sectorCode) {
        log.debug("REST request to getMostDiscussedCompany");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			ResponseEntity<List<String>> listOfCompany = stockServiceClient.getStockInfoBySector(sectorCode);
			//listOfCompany.getBody().stream().forEach(s -> System.out.println(s));
			listOfCompany.getBody().replaceAll(String::toLowerCase);
			
			String[] stockArr = new String[listOfCompany.getBody().size()];
			stockArr = listOfCompany.getBody().toArray(stockArr);
			
			AggregationBuilder aggregation = AggregationBuilders.nested("path", "entity");
			aggregation.subAggregation(AggregationBuilders.terms("company_name_count").field("entity.name").includeExclude(new IncludeExclude(stockArr, null)));

			sourceBuilder.aggregation(aggregation);
			sourceBuilder.size(0);
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			Nested agg = response.getAggregations().get("path");
			Aggregations data = agg.getAggregations();
			Map<String,Aggregation> aggs = data.getAsMap();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			 for (Map.Entry<String, Aggregation> entry : aggs.entrySet())
	            {
	            	ParsedStringTerms aggs2 = (ParsedStringTerms) entry.getValue();
	            	List apps = aggs2.getBuckets();
	            	for (Bucket entry1 : aggs2.getBuckets()) {
//	            	    System.out.println(entry1.getKey());      // Term
//	            	    System.out.println(entry1.getDocCount()); // Doc count
	            		//JsonNode jsonNode = new JsonNode();
	            		Map<String, String> map = new HashMap<>();
 	            		//map.put(entry2.getKey(), entry2.getDocCount());
 	            		
 	            		map.put("count",String.valueOf((entry1.getDocCount()) ));
 	            		map.put("topic",String.valueOf(entry1.getKey()));
 	            		
	            		JsonNode 	json = mapper.readValue(mapper.writeValueAsString(map), JsonNode.class);
 	            		arrayNode.add(json);
	            	}
	            }

			
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getHotTopicsByStockName
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getHotTopicsByStockName/{stockName}")
    @Timed
    public ResponseEntity<JsonNode> getHotTopicsByStockName(@PathVariable String stockName) {
        log.debug("REST request to getHotTopicsByStockName");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
	
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			
			AggregationBuilder aggregation = AggregationBuilders.nested("nested_entity", "entity");
			aggregation.subAggregation(
					AggregationBuilders.filter("company", QueryBuilders.termQuery("entity.name", stockName))
						.subAggregation(AggregationBuilders.reverseNested("reverse_nested_entity")
							.subAggregation(AggregationBuilders.terms("hot_topic").field("hot_topic"))));
			sourceBuilder.size(0);
			
			sourceBuilder.aggregation(aggregation);
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			Aggregations results = response.getAggregations();
			ParsedNested nestedAgg = (ParsedNested)results.get("nested_entity");
			ParsedFilter filterAgg = (ParsedFilter)nestedAgg.getAggregations().get("company");
			ParsedReverseNested reverseNestedAgg = (ParsedReverseNested)filterAgg.getAggregations().get("reverse_nested_entity");
			ParsedStringTerms termAgg = (ParsedStringTerms)reverseNestedAgg.getAggregations().get("hot_topic");
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			for (Bucket entry : termAgg.getBuckets()) {
         		
         		Map<String, String> map = new HashMap<>();
         		//map.put(entry2.getKey(), entry2.getDocCount());
         		
         		map.put("count",String.valueOf((entry.getDocCount()) ));
         		map.put("topic",String.valueOf(entry.getKey()));
         		JsonNode 	json = mapper.readValue(mapper.writeValueAsString(map), JsonNode.class);
         		arrayNode.add(json);
         	}
			
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getHotTopicArticlesBySector
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getHotTopicArticlesBySector/{sector}/{hotTopic}")
    @Timed
    public ResponseEntity<JsonNode> getHotTopicArticlesBySector(@PathVariable String sector, @PathVariable String hotTopic) {
        log.debug("REST request to getHotTopicArticlesBySector");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
    		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
    		QueryBuilder qb =  QueryBuilders.boolQuery()
    				.must(QueryBuilders.matchQuery("sector", sector))
    				.must(QueryBuilders.matchQuery("hot_topic", hotTopic));
			sourceBuilder.query(qb); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.HOT_TOPIC_ARTICLE_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));  
			SearchRequest searchRequest = new SearchRequest("article");

			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    /**
     * GET  /metadata : get getHotTopicArticlesByStockName
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getHotTopicArticlesByStockName/{stockName}/{hotTopic}")
    @Timed
    public ResponseEntity<JsonNode> getHotTopicArticlesByStockName(@PathVariable String stockName, @PathVariable String hotTopic) {
        log.debug("REST request to getHotTopicArticlesByStockName");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
    		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
    		QueryBuilder qb =  QueryBuilders.boolQuery()
    				.must(QueryBuilders.nestedQuery(
    				        "entity",               
    				        QueryBuilders.boolQuery()           
    				                .must(QueryBuilders.matchQuery("entity.name", stockName)), ScoreMode.None
    				    ))
    				.must(QueryBuilders.matchQuery("hot_topic", hotTopic));
			sourceBuilder.query(qb); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.HOT_TOPIC_ARTICLE_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));  
			SearchRequest searchRequest = new SearchRequest("article");

			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    
    
    /**
     * GET  /metadata : get getCompanyAnnouncementByStockName
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/getCompanyAnnouncementByStockName/{stockName}")
    @Timed
    public ResponseEntity<JsonNode> getCompanyAnnouncementByStockName(@PathVariable String stockName) {
        log.debug("REST request to getCompanyAnnouncementByStockName");
        ObjectMapper objectMapper = new ObjectMapper();
    	try {
    		
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
			QueryBuilder qb =  QueryBuilders.boolQuery()           
			                .must( QueryBuilders.nestedQuery(
			    			        "entity",QueryBuilders.matchQuery("entity.name", stockName), ScoreMode.None))
			                .must(QueryBuilders.matchQuery("category", "announcement"));
			sourceBuilder.query(qb); 
			sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date","sector"}, null);
			sourceBuilder.from(0); 
			sourceBuilder.size(MetadataResource.ANNOUNCEMENT_SIZE); 
			sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));			
			
			SearchRequest searchRequest = new SearchRequest("article");
			
			searchRequest.source(sourceBuilder);
			SearchResponse response = client.search(searchRequest);
			SearchHit[] results = response.getHits().getHits();
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode arrayNode = mapper.createArrayNode();
			
			for (SearchHit hit : results) {
				JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
				arrayNode.add(jsonNode);
			}
			return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    
    
	  /**
	  * SEARCH  /_search/metadata?query=:query : search for the metadata corresponding
	  * to the query.
	  *
	  * @param query the query of the metadata search
	  * @return the result of the search
	  */
	 @GetMapping("/_search/metadata")
	 @Timed
	 public ResponseEntity<JsonNode> searchMetadata(@RequestParam String query) {
		 log.debug("REST request to searchMetadata");
	        ObjectMapper objectMapper = new ObjectMapper();
	    	try {
		
				SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
				QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder(query);
				sourceBuilder.query(queryStringQueryBuilder); 
				sourceBuilder.fetchSource(new String[]{"category","description","author","url","title","content","published_date","sector"}, null);
				sourceBuilder.sort(new FieldSortBuilder("published_date").order(SortOrder.DESC));  
				SearchRequest searchRequest = new SearchRequest("article");

				searchRequest.source(sourceBuilder);
				SearchResponse response = client.search(searchRequest);
				SearchHit[] results = response.getHits().getHits();
				
				ObjectMapper mapper = new ObjectMapper();
				ArrayNode arrayNode = mapper.createArrayNode();
				
				for (SearchHit hit : results) {
					JsonNode jsonNode = objectMapper.readValue(hit.getSourceAsString(), JsonNode.class);
					arrayNode.add(jsonNode);
				}
				return new ResponseEntity<>(arrayNode, HttpStatus.CREATED);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	        return null;
	 }

}




