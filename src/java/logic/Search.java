/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import core.*;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.search.Suggest;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author mark
 */
public class Search extends Creator {

    private HttpSolrServer server;
    private int page = 1, // текущая страница
            lt = 20, // лимит выводимых записей
            begin = 0;
    private String keywords = "";
    private long numFound = 0;
    private UrlOption urloption = null;
    private AdvancedAnalyzerQuest аnalyzer;
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap<>();
    private Util util = new Util();
    private Statement stmt = null;
    private ResultSet rs = null;
    private String error = "", pagnav = "";

    public Search(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {

        this.stmt = stmt;

        urloption = new UrlOption(request);
        // постраничная навигация
        page = urloption.NumberReplacementInt(request.getParameter("page"), 1);

        //page = page <= 0 ? 1 : page;
        begin = page > 0 ? (page * lt) : 0;
        begin = begin > 1000 ? 1000 : begin;



        Properties cfg = new Properties();
        cfg.setProperty("solr url", "http://46.4.48.29/solr4/humor/");
        cfg.setProperty("solr user", "solr");
        cfg.setProperty("solr pass", "qaz5Sty");
        SolrlConnection SolrlConnection = new SolrlConnection(cfg);
        try {
            server = SolrlConnection.getConnection();
        } catch (Exception ex) {
            throw new Exception(ex);
        }

        LinkedHashMap<String, String> field = new LinkedHashMap<>();

        String key = null;

        // Мне поевезет!
        if ("true".equals(request.getParameter("luck")) && "".equals(request.getParameter("key") != null ? request.getParameter("key") : "")) {
            Suggest suggest = new Suggest(server);
            key = suggest.getTermRand();
            response.sendRedirect("?" + urloption.addParam1_1("key=" + URLEncoder.encode(key, "UTF8") + "&luck=[delete]"));
        } else {
            key = request.getParameter("key");
        }


        field.put("keywords", key);
        keywords = key != null ? key.replaceAll("[\"]+", "&quot;") : "";

        // Анализ и обработка поискового запроса.
        аnalyzer = new AdvancedAnalyzerQuest(field);


        try {
            viewCatalog(querySolr(request));
        } catch (Exception ex) {
            if (ex.getMessage().equals("java.net.SocketTimeoutException: Read timed out")) {
                error = "Время соединения с сервером истекло, пожалуйста обновите страницу.";
            } else if (ex.getMessage().equals("Error executing query")) {
                error = "Ошибка при выполнении запроса. Простите.";
            } else if (ex.getMessage().equals("No search results")) {
                error = "К сожалению, мы не смогли найти то, что вы искали."
                        + "<div class=\"code\"><ul><li>Попробуйте проверить запрос на наличие ошибок или сформулировать его по-другому.</li></ul></div>";
            }
        }
    }

    /**
     * Поисковый запрос к серверу Solr.
     *
     * @param field
     */
    private HashSet querySolr(HttpServletRequest request) throws Exception {


        String q = аnalyzer.get().get("lucene").toString();
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.set("defType", "edismax");
        query.set("qf", "title^2.1 text^3.2 alt^1.5");
        query.set("wt", "xml");
        query.setStart(page == 1 ? 0 : begin - lt);
        query.setRows(lt);

        QueryResponse response = null;
        try {
            response = server.query(query);
        } catch (SolrServerException ex) {
            throw new Exception(ex.getMessage());
        }

        SolrDocumentList documents = response.getResults();
        Iterator<SolrDocument> itr = documents.iterator();

        numFound = documents.getNumFound();
        if (numFound == 0) {
            throw new Exception("No search results");
        }

        LinkedHashSet id = new LinkedHashSet();
        while (itr.hasNext()) {
            SolrDocument doc = itr.next();
            id.add(doc.getFieldValue("id"));
        }

        PagingNavigation pnav = new PagingNavigation(numFound, request.getParameter("page"), lt, urloption);
        pagnav = pnav.PagingPreviousNext();
        return id;

    }

    private void viewCatalog(HashSet id) throws SQLException {
        String sqlid = id.toString().replaceAll("[\\[\\]]", "");

        rs = stmt.executeQuery("SELECT p.*, i.text, i.image, i.img, i.alt,COUNT(i.id) as CountPosts "
                + "FROM `post_item` i, `post` p WHERE "
                + "i.post = p.id AND p.status ='on' AND p.id in(" + sqlid + ") "
                + "GROUP BY i.post ORDER BY FIELD(p.id, " + sqlid + ")");
        
        ViewMethod view = new ViewMethod(rs);
        item = view.getViewCatalog();
        //System.out.println(item);
    }

    public long getFound() {
        return numFound;
    }

    public String getKey() {
        return keywords;
    }

    public LinkedHashMap getItem() {
        return item;
    }

    public String PageNavig() {
        return pagnav;
    }

    public String getMessage() {
        return error;
    }

    @Override
    public String getMetaTitle() {
        return keywords;
    }

    @Override
    public String getMetaHead() {
        return "";
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public int getServerStatus() {
        return 200;
    }
}
