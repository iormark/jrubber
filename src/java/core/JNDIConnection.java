package core;



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
public class JNDIConnection {

    Connection conn = null;
    private static final Logger logger = Logger.getLogger(JNDIConnection.class);

    public Connection init() {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/yourmood");
            conn = ds.getConnection();
            ctx.close();
        } catch (Exception e) {
            logger.error(e);
        }

        return conn;
    }
    
    /**
     * Закрытие соединения с DB
     * @param stmt
     * @param rs  
     */
    public void close(Statement stmt, ResultSet rs) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }
    }
}
