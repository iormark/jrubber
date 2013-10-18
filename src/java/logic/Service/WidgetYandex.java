package logic.Service;

import core.Blockage;
import core.CategoriesTree;
import core.Util;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
import logic.Creator;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
public class WidgetYandex extends Creator {

    private LinkedHashMap<String, HashMap> item = new LinkedHashMap<String, HashMap>();
    private Util util = new Util();
    private static final Logger logger = Logger.getLogger(WidgetYandex.class);

    public WidgetYandex(HttpServletRequest request, Statement stmt) throws Exception {
        String act = request.getParameter("act") != null ? request.getParameter("act") : "";
        String type = request.getParameter("type") != null ? request.getParameter("type") : "";

        int rand = 0, max = 0, min = 0;
        ResultSet rs = null;
        CategoriesTree ct = null;
        Random random = new Random();

        if (!"".equals(type) && !"0".equals(type)) {
            ct = new CategoriesTree(stmt, Integer.parseInt(type));
            type = " AND p.type IN(" + ct.getCategoriesAllId() + ")";
        }

        rs = stmt.executeQuery("SELECT MAX(id) AS max, MIN(id) AS min FROM post p WHERE p.status ='on'" + type);
        if (rs.next()) {
            max = rs.getInt("max");
            min = rs.getInt("min");
        }

        rand = min + random.nextInt((max - min) + 1);

        rs = stmt.executeQuery("SELECT t.name AS typeName, t.hurl, i.text, i.image, i.alt,COUNT(i.post) as CountPosts, p.* FROM type t, `post_item` i, `post` p WHERE t.id=p.type AND i.post = p.id AND p.status ='on' "
                + "AND (p.id <=" + max + " AND p.id>=" + min + ") AND p.id>=" + rand + type + " GROUP BY i.post LIMIT 1");
        if (rs.next()) {
            HashMap<String, String> content = new HashMap();


            if (rs.getString("image") != null) {
                content.put("alt", util.specialCharactersTags(rs.getString("alt")));
                content.put("image", rs.getString("image"));

            }

            
            content.put("typeHurl", rs.getString("hurl"));
            content.put("typeName", rs.getString("typeName"));
            
            content.put("text", util.Shortening(util.bbCode(rs.getString("text")), (rs.getString("image") != null ? 150 : 400), "<br><a href=\"/anekdot?id=" + rs.getString("id") + "\" target=\"_blank\">Читать дальше</a>"));

            content.put("date", new SimpleDateFormat("d MMM yyyy, HH:mm:ss").format(rs.getTimestamp("date")));

            item.put(rs.getString("id"), content);
        }

        
        logger.info(request.getRemoteAddr()+" - " + request.getHeader("Referer"));
    }

    public LinkedHashMap getItem() {
        return item;
    }

    @Override
    public String getMetaTitle() {
        return "";
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
