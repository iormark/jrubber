package logic;

import java.util.Date;

/**
 *
 * @author mark
 */
public abstract class Creator {

    public abstract String getMetaTitle();
    public abstract String getMetaHead();
    public abstract Date getLastModified();
    public abstract int getServerStatus();
}
