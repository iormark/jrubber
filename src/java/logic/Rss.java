package logic;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 *
 * @author mark
 */
public class Rss extends Creator {

    public Rss(Statement stmt) throws SQLException {
    }

    @Override
    public String getMetaTitle() {
        return "RSS лента и экспорт в RSS";
    }

    @Override
    public int getServerStatus() {

        return 200;

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
        return null;
    }

    @Override
    public String getMetaHead() {
        return "";
    }
}
