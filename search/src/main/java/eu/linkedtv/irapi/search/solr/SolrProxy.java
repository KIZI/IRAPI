package eu.linkedtv.irapi.search.solr;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import eu.linkedtv.irapi.search.util.IrapiParams;

public interface SolrProxy {

	List<SolrDocument> executeQueries(IrapiParams irapiParams) throws SolrServerException;

	List<SolrDocument> search(SolrQuery query) throws SolrServerException;

	List<SolrDocument> executeQueries(IrapiParams irapiParams, List<String> filterQueries) throws SolrServerException;

	List<String> getFilterQueries(IrapiParams irapiParams);

}
