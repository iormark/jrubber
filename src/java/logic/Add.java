/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import core.ViewMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Check;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class Add extends Creator {

    private int id = 0;
    private int serverStatus = 200;
    private HashMap<String, String> post = new HashMap();
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap();
    private HashMap<String, HashSet> tags = new HashMap();

    public Add(HttpServletRequest request, HttpServletResponse response, Statement stmt, String RealPath, Check check) throws SQLException, Exception {

        
        if (request.getParameter("id") != null) {
            id = Integer.parseInt(request.getParameter("id"));
        } else {
            return;
        }

        ResultSet rs = stmt.executeQuery("SELECT * FROM users u, post p WHERE "
                + "u.id=p.user AND u.id = '" + check.getUserID() + "' AND p.id = '" + id + "'");
        while (rs.next()) {
            post.put("title", StringEscapeUtils.escapeHtml4(rs.getString("title")));
        }

        if (post.isEmpty()) {
            serverStatus = 404;
            return;
        }

        rs = stmt.executeQuery("SELECT id, post, sort, type, content FROM "
                + "`post_item` WHERE  post = '" + id + "' ORDER BY sort LIMIT 99;");

        Properties props = new Properties();
        props.setProperty("textSize", "0");
        props.setProperty("textLineFeed", "false");
        props.setProperty("videoIframe", "false");
        ViewMethod view = new ViewMethod(null, stmt, props);
        item = view.getItem(rs);
        tags = view.getPostTags(id);

    }

    /**
     * Id
     * @return 
     */
    public int getId() {
        return id;
    }

    /**
     * Post
     *
     * @return
     */
    public HashMap getPost() {
        return post;
    }

    /**
     * Items
     *
     * @return
     */
    public LinkedHashMap<String, HashMap> getItem() {
        return item;
    }

    /**
     * Tags
     *
     * @return
     */
    public HashMap getTagsItem() {
        return !tags.isEmpty() ? tags : null;
    }

    /**
     * Hash Code
     *
     * @return
     */
    public int getHashId() {
        return id;
    }
    
    

    public long getFileKey() {
        return System.nanoTime();
    }

    @Override
    public String getMetaTitle() {
        return "Добавить новость";
    }

    @Override
    public int getServerStatus() {
        return serverStatus;
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public String getMetaHead() {
        return "";
    }
}
