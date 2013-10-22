package logic;

import core.*;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class Anecdote extends Creator {

    private int gid = 0, imageLength = 0, next = 0, back = 0;
    private String id = "0";
    private HashMap<String, String> itemMeta = new HashMap();
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap();
    private HashMap<String, HashSet> tags = new HashMap();
    private LinkedHashMap<String, HashMap> Comment = new LinkedHashMap();
    private HashMap<String, HashMap> bn = new LinkedHashMap();
    private Util util = new Util();
    private UrlOption urloption;
    private Date LastModified = null;
    private EditCookie editcookie;
    private Connection conn;

    public Anecdote(HttpServletRequest request, HttpServletResponse response, Connection conn) throws SQLException, Exception {

        Statement stmt = conn.createStatement();

        urloption = new UrlOption(request);

        id = request.getParameter("id");
        ResultSet rs = null;
        int typeId = 0;

        rs = stmt.executeQuery("SELECT * FROM `post` WHERE status = 'on' AND id = '" + id + "'");

        while (rs.next()) {
            itemMeta.put("id", rs.getString("id"));
            itemMeta.put("name", rs.getString("name"));
            itemMeta.put("title", rs.getString("title"));
            itemMeta.put("vote", rs.getInt("vote") > 0 ? "" + rs.getString("vote") : rs.getString("vote"));

            itemMeta.put("created", util.dateFormat(rs.getTimestamp("date")));
            itemMeta.put("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+04:00").format(rs.getTimestamp("date")));
            if (LastModified == null) {
                LastModified = rs.getTimestamp("last_modified");
            }

        }
        

        rs = stmt.executeQuery("SELECT i.* FROM `type` t, `post_item` i, `post` p WHERE t.id=p.type AND i.post = p.id AND p.status ='on' AND i.post = '" + id + "' ORDER BY i.sort LIMIT 100;");

        ViewMethod view = new ViewMethod(rs, stmt, true);
        item = view.getViewCatalog();
        tags = view.getPostTags(id);

        for (Map.Entry<String, HashMap> entry : item.entrySet()) {
            if (gid == 0) {
                gid = Integer.parseInt(entry.getKey());
            } else {
                break;
            }
        }

        rs = stmt.executeQuery("SELECT p.title, p.id FROM `post`p JOIN  (SELECT min(id) as id FROM post WHERE status='on' AND type=" + itemMeta.get("typeInt") + " AND id > " + id + ") p2 ON p2.id=p.id LIMIT 1");

        if (rs.next()) {
            HashMap batton = new HashMap();
            if (rs.getString("id") != null) {
                batton.put("id", rs.getString("id"));
                if ("".equals(rs.getString("title"))) {
                    batton.put("name", "Предыдущий");
                } else {
                    batton.put("name", util.Shortening(rs.getString("title"), 35, ""));
                }
                batton.put("title", rs.getString("title"));
            }
            bn.put("next", batton);
        }

        rs = stmt.executeQuery("SELECT p.title, p.id FROM `post`p JOIN  (SELECT max(id) as id FROM post WHERE status='on' AND type=" + itemMeta.get("typeInt") + " AND id < " + id + ") p2 ON p2.id=p.id LIMIT 1");

        if (rs.next()) {
            HashMap batton = new HashMap();
            if (rs.getString("id") != null) {
                batton.put("id", rs.getString("id"));
                if ("".equals(rs.getString("title"))) {
                    batton.put("name", "Следующий");
                } else {
                    batton.put("name", util.Shortening(rs.getString("title"), 35, ""));
                }
                batton.put("title", rs.getString("title"));
            }
            bn.put("back", batton);
        }

        // отзывы 
        try {

            rs = stmt.executeQuery("SELECT * FROM `comment`c WHERE post='" + id + "' AND status='show'");

            for (int i = 0; rs.next(); i++) {
                HashMap data = new HashMap();
                data.put("name", rs.getString("name"));
                data.put("email", rs.getString("email"));
                data.put("comment", rs.getString("comment"));
                data.put("created", util.dateFormat(rs.getTimestamp("created")));
                data.put("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+03:00").format(rs.getTimestamp("created")));
                data.put("status", rs.getString("status"));
                data.put("vote", rs.getInt("vote") > 0 ? "" + rs.getString("vote") : rs.getString("vote"));

                Comment.put(rs.getString("id"), data);

            }
        } catch (Exception e) {
        }

        editcookie = new EditCookie(request, response);

    }

    public HashMap getItemMeta() {
        return itemMeta;
    }

    public HashMap getTagsItem() {
        return !tags.isEmpty() ? tags : null;
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
        return id;
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
                return itemMeta.get("title");
            }
        }

    }

    @Override
    public String getMetaTitle() {

        if (itemMeta.get("title").equals("")) {
            if (item.get(Integer.toString(gid)).get("text").toString().equals("")) {
                return "Пост № " + id;
            } else {
                return util.specialCharacters(util.Shortening(item.get(Integer.toString(gid)).get("text").toString(), 85, "")) + " # Пост №" + gid;
            }
        } else {
            if (imageLength > 1) {
                return itemMeta.get("title") + " (" + imageLength + " шт.)";
            } else {
                return itemMeta.get("title");
            }
        }

    }

    @Override
    public int getServerStatus() {

        if (item.isEmpty()) {
            return 404;
        } else {
            return 200;
        }
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
