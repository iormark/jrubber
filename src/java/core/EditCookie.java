/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class EditCookie { 
    HttpServletRequest request;
    HttpServletResponse response;

    public EditCookie(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Проверка, принятия браузером Cookie
     * @return 
     */
    public boolean isEmpty() {
        if(request.getHeader("Cookie") == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Записываем куки.
     * @param NameCookie
     * @param Value
     * @param path
     * @param age
     */
    public void setCookie(String NameCookie, String Value, String path, int age) throws UnsupportedEncodingException {

        Cookie c = new Cookie(NameCookie, URLEncoder.encode(Value, "UTF-8"));
        c.setMaxAge(age);
        c.setPath(path != null ? path : "/");
        response.addCookie(c);

    }
    
    /**
     * Читаем куки.
     * @param NameCookie
     * @return
     */
    public String getCookie(String NameCookie) throws UnsupportedEncodingException {
        String get = null;
        if (request.getCookies() != null) {
            Cookie cookie[] = request.getCookies();
            if (cookie.length > 0) {
                for (int i = 0; i < cookie.length; i++) {
                    if (cookie[i].getName().equals(NameCookie)) {
                        get = URLDecoder.decode(cookie[i].getValue(), "UTF-8");
                        break;
                    }
                }
            }
        }
        return get;
    }
}
