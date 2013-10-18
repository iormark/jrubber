package logic;

import core.PagingNavigation;
import core.PagingNavigationSpecular;
import core.UrlOption;
import core.Util;
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
public class Type extends Creator {

    private long found = 0; // общее колличество найденных записей.
    private int page = 1, // текущая страница
            pageSpecular = 0,
            lt = 20,
            begin = 0;
    private PagingNavigationSpecular pagnav;
    private UrlOption urloption;
    private Util util = new Util();
    private Statement stmt = null;
    private ResultSet rs = null;
    private Date LastModified = null;
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap<String, HashMap>();
    private static ArrayList num = new ArrayList();
    private HttpServletRequest request = null;

    public Type(HttpServletRequest request, Statement stmt) throws SQLException {
        this.stmt = stmt;
        this.request = request;

        urloption = new UrlOption(request);
        String type = request.getParameter("q") != null ? request.getParameter("q") : "";


        // Общее колличество соответствующих товаров.
        rs = stmt.executeQuery("SELECT count(*) as rows FROM `type`t, `post`p WHERE t.id=p.type AND t.hurl='" + type + "' AND p.status='on';");
        if (rs.next()) {
            found = rs.getInt("rows");
        }


        int pagCount = (int) ((this.found + (lt)) / lt);

        // текущая страница
        page = urloption.NumberReplacementInt(request.getParameter("page"), pagCount);

        page = page > pagCount ? pagCount : page;

        pageSpecular = pagCount - page;

        begin = pageSpecular * lt;



        //begin = (begin < 1 ? 0 : beginPage==1? begin : begin - lt);

        //System.out.println(begin+" ; "+pageSpecular+" ; "+page+" ; "+pagCount);


        //setHome(request);
        
    }

    private void setHome(HttpServletRequest request) throws SQLException {

        //rs = stmt.executeQuery("SELECT h.*,t.name as TypeName FROM `humor`h LEFT JOIN `type`t ON h.type=t.id WHERE h.status='on' ORDER BY date DESC LIMIT " + begin + "," + lt);

        rs = stmt.executeQuery("SELECT h.textj,h.image,h.alt,COUNT(h.id) as CountPosts,hm.* FROM `humor` h, `humor_meta` hm WHERE h.id = hm.id AND hm.status ='on' GROUP BY h.id ORDER BY hm.date DESC LIMIT " + begin + "," + lt);


        while (rs.next()) {
            HashMap<String, String> content = new HashMap<String, String>();


            content.put("type", rs.getString("type"));
            content.put("text", util.Shortening(util.bbCode(rs.getString("text")), 800, "<br><a href=\"/anekdot?id=" + rs.getString("id") + "\">Читать дальше</a>"));
            content.put("score", rs.getInt("score") > 0 ? "+" + rs.getString("score") : rs.getString("score"));
            content.put("CountPosts", rs.getString("CountPosts"));

            if (rs.getString("image") != null) {
                content.put("alt", util.Shortening(util.specialCharactersTags(rs.getString("alt")), 255, ""));
                content.put("image", rs.getString("image"));
            }

            if (LastModified == null) {
                LastModified = rs.getTimestamp("date");
            }



            content.put("date", new SimpleDateFormat("d MMM yy").format(rs.getTimestamp("date")));
            content.put("dateMore", new SimpleDateFormat("d MMM yy, HH:mm:ss").format(rs.getTimestamp("date")));

            String title = "";
            if (rs.getInt("CountPosts") > 1) {
                title = " (" + rs.getString("CountPosts") + " шт.)";
            }

            if (rs.getString("title").toString().equals("")) {
                content.put("title", rs.getString("type") + " № " + rs.getString("id") + title);
            } else {
                content.put("title", util.specialCharacters(util.Shortening(rs.getString("title"), 85, "")) + title);
            }


            item.put(rs.getString("id"), content);

        }





        pagnav = new PagingNavigationSpecular(found, request.getParameter("page"), lt, urloption);

    }

    public LinkedHashMap getItem() {
        return item;
    }

    public String PageNavig() {
        return pagnav != null ? pagnav.PagingNavigation() : "";
    }

    @Override
    public String getMetaTitle() {

        if (begin <= 1) {
            return "Читать смешные анекдоты, смотреть смешные картинки";
        } else {
            try {
                return "Читать смешные анекдоты. Страница " + page + "";
            } catch (IndexOutOfBoundsException ex) {
                return "Читать смешные анекдоты. Страница " + page + "";
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
        /*SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        
         Date d = null;
         try {
         d = formatter.parse("Thu, 26 Jul 2012 15:00:52 GMT");
         } catch (ParseException e) {  
         }
        
         return d;*/
        return LastModified;
    }

    @Override
    public String getMetaHead() {
        if (request.getParameter("page") == null) {
            return "<meta name=\"description\" content=\"Порем смешные анекдоты только для вас. На сайте можно читать анекдоты и смотреть смешные картинки. Добавлять собственные истории, анекдоты и афоризмы.\" />\n"
                    + "<meta name=\"keywords\" content=\"смешные анекдоты, анекдоты читать, читать смешные анекдоты\" />";
        } else {
            return "";
        }
    }
}
