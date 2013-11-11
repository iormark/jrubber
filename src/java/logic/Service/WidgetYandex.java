package logic.Service;

import core.CategoriesTree;
import core.Util;
import core.ViewMethod;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
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

        rs = stmt.executeQuery("SELECT MAX(p.id) AS max, MIN(p.id) AS min FROM `post_item` i, post p WHERE i.post = p.id AND p.status ='on' AND i.video is null");
        if (rs.next()) {
            max = rs.getInt("max");
            min = rs.getInt("min");
        }

        rand = min + random.nextInt((max - min) + 1);

        rs = stmt.executeQuery("SELECT i.text, i.image, i.img, i.alt,COUNT(i.post) as CountPosts, p.* FROM `post_item` i, `post` p WHERE i.post = p.id AND p.status ='on' "
                + "AND (p.id <=" + max + " AND p.id>=" + min + ") AND p.id>=" + rand + " AND i.video is null GROUP BY i.post LIMIT 1");
       
        Properties props = new Properties();
        props.setProperty("title", "Подробнее...");
        props.setProperty("textSize", "200");
        ViewMethod view = new ViewMethod(rs, props);
        item = view.getViewCatalog();

        
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
