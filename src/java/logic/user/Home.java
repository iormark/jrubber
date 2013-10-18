/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.user;

import java.util.Date;
import logic.Creator;

/**
 *
 * @author mark
 */
public class Home extends Creator {
    
    public Home() {
        
    }

    @Override
    public String getMetaTitle() {
        return "Мой аккаунт";
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
