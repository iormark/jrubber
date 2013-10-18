package core;

import java.io.IOException;
import java.util.Properties;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

/**
 *
 * @author mark
 */
public class SolrlConnection {

    private Properties cfg = new Properties();
    private String error = "";

    public SolrlConnection(Properties cfg) {
        this.cfg = cfg;
    }

    public synchronized HttpSolrServer getConnection() throws IOException, Exception {
        HttpSolrServer solrCore;

        try {
            solrCore = new HttpSolrServer(cfg.getProperty("solr url"));

            AbstractHttpClient client = (AbstractHttpClient) solrCore.getHttpClient();
            client.addRequestInterceptor(new SolrlConnection.PreEmptiveBasicAuthenticator(cfg.getProperty("solr user"), cfg.getProperty("solr pass")));

            solrCore.setSoTimeout(Integer.MAX_VALUE);
            solrCore.setConnectionTimeout(Integer.MAX_VALUE);
        } catch (Exception e) {
            throw new Exception(e);
        }

        return solrCore;
        //System.out.println("----------------------------------------");
    }

    public class PreEmptiveBasicAuthenticator implements HttpRequestInterceptor {

        private final UsernamePasswordCredentials credentials;

        public PreEmptiveBasicAuthenticator(String user, String pass) {
            credentials = new UsernamePasswordCredentials(user, pass);
        }

        @Override
        public void process(HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            request.addHeader(BasicScheme.authenticate(credentials, "US-ASCII", false));
        }
    }
}
