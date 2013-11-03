package logic;

import core.CategoriesTree;
import core.PagingNavigation;
import core.UrlOption;
import core.Util;
import core.ViewMethod;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mark
 */
public class Home extends Creator {

    private int serverStatus = 200;
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
    private String status = "on";

    public Home(HttpServletRequest request, ArrayList args, Connection conn) throws SQLException {
        this.conn = conn;
        this.args = args;
        this.request = request;
        stmt = conn.createStatement();

        System.out.println(request.getParameter("q"));

        urloption = new UrlOption(request);
        page = urloption.NumberReplacementInt(request.getParameter("page"), 1);

        //page = page <= 0 ? 1 : page;
        begin = page > 0 ? (page * lt) : 0;
        begin = begin > 1000 ? 1000 : begin;

        String tags = request.getParameter("q") != null ? request.getParameter("q").replaceAll("[/]$", "") : "home.html";
        selectedTags = new HashMap();
        String tagsID = "";
        int tagsId = 0;
        status = (String) args.get(args.size() - 1);

        if (!tags.equals("home.html") && (args.size() == 1 || args.size() == 3)) {
            if (!"new".equals(status)) {
                serverStatus = 404;
                return;
            }
        } else {
            status = "on";
        }

        // по hurl определяем ид категории.
        if (!tags.equals("home.html") && !tags.equals("") && "tag".equals(args.get(0))) {

            rs = stmt.executeQuery("SELECT id, tags FROM tags WHERE tags='" + args.get(1) + "'");
            if (rs.next()) {
                tagsId = rs.getInt("id");
                selectedTags.put("id", tagsId);
                selectedTags.put("tags", rs.getString("tags"));
            }
        }

        if (tagsId > 0) {
            tagsID = " AND tl.tags=" + tagsId;
        } else if (!tags.equals("home.html") && !tags.equals("new")) {
            serverStatus = 404;
            return;
        } else {
            //return;
        }

        //if (args.size() > 2) {
        //status = args.get(args.size()-1).toString();
        //if(!"new".equals(status)) return;
        //}
        setHome(request, tagsID, status);

    }

    private void setHome(HttpServletRequest request, String tagsID, String status) throws SQLException {

        //rs = stmt.executeQuery("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias, t.hurl  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);

        rs = stmt.executeQuery("SELECT SQL_CALC_FOUND_ROWS u.login, p.*, i.itemCount, pi.text, pi.image, pi.img, pi.alt, pi.video, (SELECT COUNT(*) FROM `comment` c WHERE p.id = c.post) AS commentCount FROM users u, post p, tags_link tl, post_item pi JOIN (SELECT id, post, COUNT(*) AS itemCount, MIN(sort) AS sortMin FROM post_item GROUP BY post) AS i ON pi.post=i.post AND pi.sort=i.sortMin WHERE u.id=p.user AND tl.post=p.id AND p.id=pi.post AND p.status='" + status + "' " + tagsID + " GROUP BY p.id ORDER BY p.svc_date DESC LIMIT " + (page == 1 ? 0 : begin - lt) + "," + lt);
        

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
        return !item.isEmpty() ? item : null;
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

    public String getMenu() {
        String tag = selectedTags.containsKey("tags") ? "<a href=\"/" + ("new".equals(status) ? "new" : "") + "\">×</a> <h1>" + selectedTags.get("tags") + "</h1>" : "";
        String tagUrl = selectedTags.containsKey("tags") ? "/tag/" + (String) selectedTags.get("tags") +"": "/";
        String tagUrlNew = selectedTags.containsKey("tags") ? "/tag/" + (String) selectedTags.get("tags") +"/new": "/new";

        String menu = "<ul class=\"menu\">";

        menu += "<li class=\"tag\" title=\"Удалить\">" + tag + "</li>";

        if ("on".equals(status)) {
            menu += "<li class=\"active\"><a href=\"" + tagUrl + "\">Лучшее</a></li>";
            menu += "<li class=\"noactive\"><a href=\"" + tagUrlNew + "\">Новое</a></li>";
        } else if ("new".equals(status)) {
            menu += "<li class=\"noactive\"><a href=\"" + tagUrl + "\">Лучшее</a></li>";
            menu += "<li class=\"active\"><a href=\"" + tagUrlNew + "\">Новое</a></li>";
        }

        menu += "</ul>";
        return menu;
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
            //return 404;
        } else {
        }
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
        if (selectedTags.isEmpty()) {
            return "<meta name=\"description\" content=\"Начальник достал, он урод. Денег нет в кармане, не беда. В жизни самое смешное, ерунда. Анекдоты почитай и картинки посмотри, на х*й брось эту работу, а начальнику въеби!\" />\n"
                    + "<meta name=\"keywords\" content=\"самое смешное, самое прикольное, самое остроумное\" />";

        } else {
            return "<meta name=\"description\" content=\"" + (selectedTags.containsKey("description") ? selectedTags.get("description") : "") + "\" />\n"
                    + "<meta name=\"keywords\" content=\"" + (selectedTags.containsKey("keywords") ? selectedTags.get("keywords") : "") + "\" />";

        }
    }
}
