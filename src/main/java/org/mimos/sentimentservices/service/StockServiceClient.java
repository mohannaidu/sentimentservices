package org.mimos.sentimentservices.service;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.codahale.metrics.annotation.Timed;

@FeignClient("stockservices")
public interface StockServiceClient {
	
//	@PostMapping("/api")
//    @Timed
//    public ResponseEntity<MetadataDto> createMetadata(@RequestBody MetadataDto metadata);
//
//    /**
//     * PUT  /metadata : Updates an existing metadata.
//     *
//     * @param metadata the metadata to update
//     * @return the ResponseEntity with status 200 (OK) and with body the updated metadata,
//     * or with status 400 (Bad Request) if the metadata is not valid,
//     * or with status 500 (Internal Server Error) if the metadata couldn't be updated
//     * @throws URISyntaxException if the Location URI syntax is incorrect
//     */
//    @PutMapping("/api")
//    @Timed
//    public ResponseEntity<MetadataDto> updateMetadata(@RequestBody MetadataDto metadata);

    /**
     * GET  /metadata : get getStockInfoBySector
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
    @GetMapping("/api/stocks/stock-infos-by-sector/{sectorCode}")
    @Timed
    public ResponseEntity<List<String>> getStockInfoBySector(@PathVariable("sectorCode") String sectorCode);
    
    /**
     * GET  /metadata : get getTotalArticleByStockCode
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
     */
//    @GetMapping("/metadata/getTotalArticleByStockCode/{stockCode}")
//    @Timed
//    public int getTotalArticleByStockCode(@PathVariable("stockCode") String stockCode);
//    
//    /**
//     * GET  /metadata : get getArticleByStockCode
//     *
//     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
//     */
//    @GetMapping("/metadata/getArticleByStockCode/{stockCode}")
//    @Timed
//    public List<MetadataDto> getArticleByStockCode(@PathVariable("stockCode") String stockCode);
//    
//    /**
//     * GET  /metadata : get getLatestNewsBySector
//     *
//     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
//     */
//    @GetMapping("/metadata/getLatestNewsBySector/{sector}")
//    @Timed
//    public List<MetadataDto> getLatestNewsBySector(@PathVariable("sector") String sector);
//    
//    
//    /**
//     * GET  /metadata : get all the metadata.
//     *
//     * @return the ResponseEntity with status 200 (OK) and the list of metadata in body
//     */
//    @GetMapping("/metadata")
//    @Timed
//    public List<MetadataDto> getAllMetadata();
//
//    /**
//     * GET  /metadata/:id : get the "id" metadata.
//     *
//     * @param id the id of the metadata to retrieve
//     * @return the ResponseEntity with status 200 (OK) and with body the metadata, or with status 404 (Not Found)
//     */
//    @GetMapping("/metadata/{id}")
//    @Timed
//    public ResponseEntity<MetadataDto> getMetadata(@PathVariable("id") String id);
//
//    /**
//     * DELETE  /metadata/:id : delete the "id" metadata.
//     *
//     * @param id the id of the metadata to delete
//     * @return the ResponseEntity with status 200 (OK)
//     */
//    @DeleteMapping("/metadata/{id}")
//    @Timed
//    public ResponseEntity<Void> deleteMetadata(@PathVariable("id") String id);
//
//    /**
//     * SEARCH  /_search/metadata?query=:query : search for the metadata corresponding
//     * to the query.
//     *
//     * @param query the query of the metadata search
//     * @return the result of the search
//     */
//    @GetMapping("/_search/metadata")
//    @Timed
//    public List<MetadataDto> searchMetadata(@RequestParam("query") String query);
    
}
