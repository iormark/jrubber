package logic;

import core.EditCookie;
import core.UrlOption;
import core.Util;
import core.ViewMethod;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Comment;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
public class Post extends Creator {

    private int serverStatus = 200;
    private int gid = 0, imageLength = 0, next = 0, back = 0;
    private int id = 0;
    private HashMap itemMeta = new HashMap();
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap();
    private HashMap<String, HashSet> tags = new HashMap();
    private LinkedHashMap<String, HashMap> Comment = null;
    private HashMap<String, HashMap> bn = new LinkedHashMap();
    private static Util util = new Util();
    private UrlOption urloption;
    private Date LastModified = null;
    private EditCookie editcookie;
    private Connection conn;
    private ViewMethod view = null;
    private static final Logger logger = Logger.getLogger(Post.class);

    public Post(HttpServletRequest request, HttpServletResponse response, Connection conn) throws Exception {

        Statement stmt = conn.createStatement();

        urloption = new UrlOption(request);

        id = Integer.parseInt(request.getParameter("id"));
        ResultSet rs = null;
        int typeId = 0;

        rs = stmt.executeQuery("SELECT * FROM users u, post p WHERE u.id=p.user AND p.status IN('new','on') AND p.id = '" + id + "'");

        while (rs.next()) {
            itemMeta.put("id", rs.getString("id"));
            itemMeta.put("user", rs.getString("user"));
            itemMeta.put("login", rs.getString("login"));
            itemMeta.put("title", rs.getString("title"));
            itemMeta.put("vote", rs.getInt("vote") > 0 ? "" + rs.getString("vote") : rs.getString("vote"));

            itemMeta.put("created", util.dateFormat(rs.getTimestamp("date")));
            itemMeta.put("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+04:00").format(rs.getTimestamp("date")));
            if (LastModified == null) {
                LastModified = rs.getTimestamp("last_modified");
            }
        }

        if (itemMeta.isEmpty()) {
            serverStatus = 404;
            return;
        }

        rs = stmt.executeQuery("SELECT id, post, sort, type, content FROM "
                + "`post_item` WHERE  post = '" + id + "' ORDER BY sort LIMIT 99;");

        Properties props = new Properties();
        props.setProperty("textSize", "0");
        view = new ViewMethod(null, stmt, props);
        item = view.getItem(rs);
        tags = view.getPostTags(id);

        for (Map.Entry<String, HashMap> entry : item.entrySet()) {
            if (gid == 0) {
                gid = Integer.parseInt(entry.getKey());
            } else {
                break;
            }
        }

        rs = stmt.executeQuery("SELECT p.title, p.id FROM `post`p JOIN  (SELECT min(id) as id FROM post WHERE (status = 'new' OR status = 'on')  AND id > " + id + ") p2 ON p2.id=p.id LIMIT 1");

        if (rs.next()) {
            HashMap batton = new HashMap();
            if (rs.getString("id") != null) {
                batton.put("id", rs.getString("id"));
                if ("".equals(rs.getString("title"))) {
                    batton.put("name", "Предыдущий");
                } else {
                    batton.put("name", util.Shortening(rs.getString("title"), 35, ""));
                }
                batton.put("title", StringEscapeUtils.escapeHtml4(rs.getString("title")));
            }
            bn.put("next", batton);
        }

        rs = stmt.executeQuery("SELECT p.title, p.id FROM `post`p JOIN  (SELECT max(id) as id FROM post WHERE (status = 'new' OR status = 'on')  AND id < " + id + ") p2 ON p2.id=p.id LIMIT 1");

        if (rs.next()) {
            HashMap batton = new HashMap();
            if (rs.getString("id") != null) {
                batton.put("id", rs.getString("id"));
                if ("".equals(rs.getString("title"))) {
                    batton.put("name", "Следующий");
                } else {
                    batton.put("name", util.Shortening(rs.getString("title"), 35, ""));
                }
                batton.put("title", StringEscapeUtils.escapeHtml4(rs.getString("title")));
            }
            bn.put("back", batton);
        }

        // отзывы 
        try {
            Comment comment = new Comment(stmt, id);
            Comment = comment.getComment();
        } catch (Exception e) {
            logger.error(e);
        }

        editcookie = new EditCookie(request, response);

    }

    public String varMenu() {
        return "Комментарии";
    }

    public HashMap getItemMeta() {
        return itemMeta;
    }

    public HashMap<String, HashSet> getTagsItem() {
        return !tags.isEmpty() ? tags : null;
    }

    public String getAlt() {
        return view.getPostTags(Integer.toString(id));
    }

    public LinkedHashMap getItem() {
        return item;
    }

    public LinkedHashMap getComment() {
        return Comment;
    }

    /**
     * Навигация Back - Next
     *
     * @return
     */
    public HashMap getBN() {
        return bn;
    }

    public String getId() {
        return Integer.toString(id);
    }

    public int getNext() {
        return next;
    }

    public int getBack() {
        return back;
    }

    public String getCName() throws UnsupportedEncodingException {
        return editcookie.getCookie("name");
    }

    public String getCEmail() throws UnsupportedEncodingException {
        return editcookie.getCookie("email");
    }

    public String getTitle() {

        if (itemMeta.get("title").equals("")) {
            return "№ " + id;
        } else {
            if (imageLength > 1) {
                return itemMeta.get("title") + " (" + imageLength + " шт.)";
            } else {
                return (String) itemMeta.get("title");
            }
        }

    }

    @Override
    public String getMetaTitle() {

        if (itemMeta.get("title").equals("")) {
            if (!item.get(Integer.toString(gid)).get("type").toString().equals("text")) {
                return "Новость № " + id;
            } else {
                return util.specialCharacters(util.Shortening(item.get(Integer.toString(gid)).get("content").toString(), 85, "")) + " # Новость №" + gid;
            }
        } else {
            if (imageLength > 1) {
                return itemMeta.get("title") + " (" + imageLength + " шт.)";
            } else {
                return (String) itemMeta.get("title");
            }
        }

    }

    @Override
    public int getServerStatus() {

        return serverStatus;

    }

    @Override
    public Date getLastModified() {
        /*
         * SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy
         * HH:mm:ss", Locale.US);
         *
         * Date d = null; try { d = formatter.parse("Thu, 26 Jul 2012 15:00:52
         * GMT"); } catch (ParseException e) { }
         *
         * return d;
         */
        return LastModified;
    }

    @Override
    public String getMetaHead() {
        return "";
    }
}
