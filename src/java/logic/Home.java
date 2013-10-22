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
    private String pagnav = "";
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
    private HashMap selectedTags = null;
    private String tagsID = "";

    public Home(HttpServletRequest request, ArrayList args, Connection conn) throws SQLException {
        this.conn = conn;
        this.args = args;
        this.request = request;
        stmt = conn.createStatement();

        urloption = new UrlOption(request);
        page = urloption.NumberReplacementInt(request.getParameter("page"), 1);

        //page = page <= 0 ? 1 : page;
        begin = page > 0 ? (page * lt) : 0;
        begin = begin > 1000 ? 1000 : begin;

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
            tagsID = " AND tl.tags=" + tagsId;
        } else if (!tags.equals("home.html")) {
            return;
        }

        setHome(request, tagsID);

    }

    private void setHome(HttpServletRequest request, String tagsID) throws SQLException {
//System.out.println("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);
        //rs = stmt.executeQuery("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias, t.hurl  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);

        rs = stmt.executeQuery("SELECT SQL_CALC_FOUND_ROWS p.*, i.itemCount, i.text, i.image, i.img, i.alt, "
                + "(SELECT COUNT(*) FROM `comment` c WHERE p.id = c.post) "
                + "AS commentCount FROM tags_link tl, post p JOIN "
                + "(SELECT post, text, image, img, alt, COUNT(*) AS itemCount FROM post_item GROUP BY post) AS i "
                + "ON p.id=i.post WHERE p.status='on' AND p.id=tl.post " + tagsID + " ORDER BY p.date DESC LIMIT " + (page == 1 ? 0 : begin - lt) + "," + lt);

        
        ViewMethod view = new ViewMethod(rs, stmt);
        item = view.getViewCatalog();
        rs = stmt.executeQuery("SELECT FOUND_ROWS() as rows;");
        if (rs.next()) {
            found = rs.getInt("rows");
        }
        
        tags = view.getPostTags();
        LastModified = view.getLastModified();
        
        //pagnav = new PagingNavigationSpecular(found, request.getParameter("page"), lt, urloption);
        PagingNavigation pnav = new PagingNavigation(found, request.getParameter("page"), lt, urloption);
        pagnav = pnav.PagingPreviousNext();

    }

    public HashMap getSelectedTags() {
        return selectedTags;
    }

    public LinkedHashMap getItem() {
        return item;
    }

    public HashMap getTagsItem() {
        return tags;
    }

    public String PageNavig() {
        return pagnav;
    }

    public String getByLink() {
        return byLink;
    }

    @Override
    public String getMetaTitle() {
        if (!selectedTags.isEmpty()) {

            return selectedTags.get("tags").toString();
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
        if (selectedTags.isEmpty()) {
            return "<meta name=\"description\" content=\"Начальник достал, он урод. Денег нет в кармане, не беда. В жизни самое смешное, ерунда. Анекдоты почитай и картинки посмотри, нафик брось эту работу, а начальнику въеби!\" />\n"
                    + "<meta name=\"keywords\" content=\"самое смешное, самое прикольное, самое остроумное\" />";

        } else {
            return "<meta name=\"description\" content=\"" + (selectedTags.containsKey("description") ? selectedTags.get("description") : "") + "\" />\n"
                    + "<meta name=\"keywords\" content=\"" + (selectedTags.containsKey("keywords") ? selectedTags.get("keywords") : "") + "\" />";

        }
    }
}
