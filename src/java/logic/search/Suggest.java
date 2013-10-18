/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.search;

import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;

/**
 *
 * @author mark
 */
public class Suggest {

    private HttpSolrServer server = null;
    List<Term> items = null;

    public Suggest(HttpSolrServer server) {
        this.server = server;

        items = query();
        
    }

    private List<Term> query() {
        List<Term> items = null;
        SolrQuery query = new SolrQuery();
        query.addTermsField("text");
        query.setTerms(true);
        query.setTermsMaxCount((int) Math.floor(Math.random() * 300));
        query.setTermsLimit(100);
        query.setQueryType("/terms");
        query.set("wt", "json");

        try {
            System.out.println(query);
            QueryResponse qr = server.query(query);
            TermsResponse resp = qr.getTermsResponse();
            items = resp.getTerms("text");
        } catch (SolrServerException e) {
            items = null;
        }
        return items;
    }
    
    
    public String getTermRand() {
        int n = (int) Math.floor(Math.random() * items.size());
        return items.get(n).getTerm();
    }
}
