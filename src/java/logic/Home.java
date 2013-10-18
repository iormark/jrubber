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
    private HttpServletRequest request = null;
    private ArrayList args;
    private CategoriesTree ct = null;
    private CategoryBreadCrumbs cbc = null;
    private HashMap SelectedType = null;
    private String CategoriesAllId = "";

    public Home(HttpServletRequest request, ArrayList args, Connection conn) throws SQLException {
        this.conn = conn;
        this.args = args;
        this.request = request;
        stmt = conn.createStatement();

        urloption = new UrlOption(request);


        String type = request.getParameter("q") != null ? request.getParameter("q").replaceAll("[/]$", "") : "home.html";
        SelectedType = new HashMap();
        int typeId = 0;

        // по hurl определяем ид категории.
        if (!type.equals("home.html") && !type.equals("")) {
            rs = stmt.executeQuery("SELECT id, title, headline, advt, body, description, keywords FROM type WHERE hurl='" + type + "'");
            if (rs.next()) {
                typeId = rs.getInt("id");
                SelectedType.put("id", typeId);
                SelectedType.put("title", rs.getString("title"));
                SelectedType.put("headline", rs.getString("headline"));
                SelectedType.put("advt", rs.getString("advt"));
                SelectedType.put("body", rs.getString("body"));
                SelectedType.put("description", rs.getString("description"));
                SelectedType.put("keywords", rs.getString("keywords"));
                ct = new CategoriesTree(stmt, typeId);
            }

            if (ct != null) {
                CategoriesAllId = " AND t.id IN(" + ct.getCategoriesAllId() + ")";
            }
            if (typeId > 0) {
                cbc = new CategoryBreadCrumbs(conn, typeId);
            }
        }

        if (CategoriesAllId.equals("")) {
            if (!type.equals("home.html")) {
                return;
            }
        }

        String by = request.getParameter("by") != null ? request.getParameter("by") : "";
        String period = "";
        if ("best7days".equals(by)) {
            by = "p.vote DESC";
            period = " AND p.date > DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY) AND p.vote > 30";
            byLink = "Лучшие за: 7 дней, <a href=\"?by=best14days\">14 дней</a>";
        } if ("best14days".equals(by)) {
            by = "p.vote DESC";
            period = " AND p.date > DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY) AND p.vote > 30";
            byLink = "Лучшие за: <a href=\"?by=best7days\">7 дней</a>, 14 дней";
        } else {
            by = "p.date DESC";
            byLink = "Лучшие за: <a href=\"?by=best7days\">7 дней</a>, <a href=\"?by=best14days\">14 дней</a>";
        }



        // Общее колличество соответствующих товаров.
        rs = stmt.executeQuery("SELECT count(*) as rows FROM `type`t, `post`p WHERE t.id=p.type AND p.status='on' " + period + " " + CategoriesAllId);
        if (rs.next()) {
            found = rs.getInt("rows");
        }

        int pagCount = Math.round((this.found + (lt - 1)) / lt);

        // текущая страница
        page = urloption.NumberReplacementInt(request.getParameter("page"), pagCount);

        page = page > pagCount ? 0 : page;

        pageSpecular = pagCount - page;

        begin = pageSpecular * lt;

        setHome(request, by, period);

    }

    private void setHome(HttpServletRequest request, String by, String period) throws SQLException {
//System.out.println("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);
        //rs = stmt.executeQuery("SELECT h.text, h.image, h.alt,COUNT(h.id) as CountPosts, hm.*, t.name AS nameType, t.name_alias AS nameAlias, t.hurl  FROM `type2` t, `humor` h, `humor_meta` hm WHERE t.id = hm.type_int AND h.id = hm.id AND hm.status ='on' " + CategoriesAllId + " GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);

        rs = stmt.executeQuery("SELECT p.*, t.name AS type_name, t.name_alias, t.hurl, "
                + "COUNT(i.id) AS CountPosts, i.text, i.alt, i.image, i.img, "
                + "(SELECT COUNT(*) FROM `comment` c WHERE p.id = c.post) AS commentCount FROM "
                + "`post` p, `post_item` i, `type` t WHERE p.id=i.post AND p.`type`=t.id "
                + "AND p.status ='on' " + period + " " + CategoriesAllId + " GROUP BY i.post "
                + "ORDER BY " + by + " LIMIT " + begin + "," + lt);
        ViewMethod view = new ViewMethod(rs);
        item = view.getViewCatalog();
        LastModified = view.getLastModified();




        pagnav = new PagingNavigationSpecular(found, request.getParameter("page"), lt, urloption);

    }

    public HashMap getSelectedType() {
        return SelectedType;
    }

    public LinkedHashMap getItem() {
        return item;
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

            return SelectedType.get("title").toString();
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
            return "<meta name=\"description\" content=\"" + (SelectedType.containsKey("description") ? SelectedType.get("description") : "") + "\" />\n"
                    + "<meta name=\"keywords\" content=\"" + (SelectedType.containsKey("keywords") ? SelectedType.get("keywords") : "") + "\" />";

        }
    }
}
