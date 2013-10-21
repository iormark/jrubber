package logic;

import core.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mark
 */
public class Home extends Creator {

    private long found = 0; // общее колличество найденных записей.
    private int page = 1, // текущая страница
            pageSpecular = 0,
            lt = 25,
            begin = 0;
    private String typeQ = "", typeId;
    private String byLink = "";
    private PagingNavigationSpecular pagnav;
    private UrlOption urloption;
    private Util util = new Util();
    private Connection conn;
    private Statement stmt = null;
    private ResultSet rs = null;
    private Date LastModified = null;
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap<>();
    private HashMap<String, HashSet> tags = new HashMap();
    private HttpServletRequest request = null;
    private ArrayList args;
    private CategoriesTree ct = null;
    private CategoryBreadCrumbs cbc = null;
    private HashMap selectedTags = null;
    private String tagsID = "";

    public Home(HttpServletRequest request, ArrayList args, Connection conn) throws SQLException {
        this.conn = conn;
        this.args = args;
        this.request = request;
        stmt = conn.createStatement();

        urloption = new UrlOption(request);

        String tags = request.getParameter("q") != null ? request.getParameter("q").replaceAll("[/]$", "") : "home.html";
        selectedTags = new HashMap();
        int tagsId = 0;

        // по hurl определяем ид категории.
        if (!tags.equals("home.html") && !tags.equals("") && args.size() > 1) {
            rs = stmt.executeQuery("SELECT id, tags FROM tags WHERE tags='" + args.get(1) + "'");
            if (rs.next()) {
                tagsId = rs.getInt("id");
                selectedTags.put("id", tagsId);
                selectedTags.put("tags", rs.getString("tags"));
            }
        }

        if (tagsId > 0) {
            tagsID = " AND t.id=" + tagsId;
        } else if(!tags.equals("home.html")) {
            return;
        }

        setHome(request, tagsID);

    }

    private void setHome(HttpServletRequest request, String tagsID) throws SQLException {
//System.out.println("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);
        //rs = stmt.executeQuery("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias, t.hurl  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);

        rs = stmt.executeQuery("SELECT p.*, COUNT(i.id) AS CountPosts, i.text, i.alt, i.image, i.img, "
                + "(SELECT COUNT(*) FROM `comment` c WHERE p.id = c.post) AS commentCount "
                + "FROM post p, post_item i, tags t, tags_link tl "
                + "WHERE p.id=i.post AND p.status='on' AND p.id=tl.post AND t.id=tl.tags "
                + tagsID + " GROUP BY i.post ORDER BY date desc LIMIT 0, 10");
        ViewMethod view = new ViewMethod(rs, stmt);
        item = view.getViewCatalog();
        tags = view.getPostTags();
        LastModified = view.getLastModified();

        pagnav = new PagingNavigationSpecular(found, request.getParameter("page"), lt, urloption);

    }

    public HashMap getSelectedType() {
        return selectedTags;
    }

    public LinkedHashMap getItem() {
        return item;
    }
    
    public HashMap getTags() {
        return tags;
    }

    public String PageNavig() {
        return pagnav != null ? pagnav.PagingNavigation() : "";
    }

    public String getCategoryBreadCrumbs() {
        if (cbc != null) {
            return cbc.getCategoryBreadCrumbs(false);
        } else {
            return "";
        }
    }

    public String getByLink() {
        return byLink;
    }

    @Override
    public String getMetaTitle() {
        if (cbc != null) {

            return selectedTags.get("title").toString();
        } else {
            return "Самое смешное";
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
        if (cbc == null) {
            return "<meta name=\"description\" content=\"Начальник достал, он урод. Денег нет в кармане, не беда. В жизни самое смешное, ерунда. Анекдоты почитай и картинки посмотри, нафик брось эту работу, а начальнику въеби!\" />\n"
                    + "<meta name=\"keywords\" content=\"самое смешное, самое прикольное, самое остроумное\" />";

        } else {
            return "<meta name=\"description\" content=\"" + (selectedTags.containsKey("description") ? selectedTags.get("description") : "") + "\" />\n"
                    + "<meta name=\"keywords\" content=\"" + (selectedTags.containsKey("keywords") ? selectedTags.get("keywords") : "") + "\" />";

        }
    }
}
