/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import core.CategoriesTree;
import core.EditCookie;
import core.Util;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class Add extends Creator {

    private Util util = new Util();
    private StringBuilder message = new StringBuilder();
    private String name = "", email = "", text = "", captcha = "";
    private String UploadMessage = "", UploadName = "", UploadEmail = "", UploadEdit = null, UploadTitle = "", UploadText = "", UploadImageOn = "", UploadListContent = "";
    private CategoriesTree ct = null;

    public Add(HttpServletRequest request, HttpServletResponse response, Statement stmt, String RealPath) throws SQLException, Exception {

        EditCookie editcookie = new EditCookie(request, response);

        UploadMessage = request.getAttribute("message") != null ? request.getAttribute("message").toString() : "";
        UploadName = request.getAttribute("name") != null ? request.getAttribute("name").toString() : editcookie.getCookie("name");
        UploadEmail = request.getAttribute("email") != null ? request.getAttribute("email").toString() : editcookie.getCookie("email");
        UploadTitle = request.getAttribute("title") != null ? request.getAttribute("title").toString() : "";

        if (editcookie.getCookie("name") != null && editcookie.getCookie("email") != null) {
            UploadEdit = "true";
        }

        if (request.getAttribute("ListContent") != null) {
            UploadListContent = request.getAttribute("ListContent").toString();
        } else {
            UploadListContent = "<fieldset id=\"list-1\">"
                    + "<legend>Форма №1 &nbsp;</legend>"
                    + "<div class=\"substrate\">"
                    + "<div>"
                    + "Анекдот / Надпись над картинкой:"
                    + "<textarea name=\"text\" rows=\"9\" class=\"inp\"></textarea>"
                    + "</div>"
                    + "<div style=\"text-align: left\">"
                    + "Загрузить картинку?<br> <input type=\"file\" name=\"photo\">"
                    + "</div>"
                    + "<div>"
                    + "Описание картинки (255 сим.):"
                    + "<input name=\"alt\" style=\"width: 99%\" class=\"inp\">"
                    + "</div>"
                    + "</div>"
                    + "</fieldset>";
        }

        ct = new CategoriesTree(stmt, 0);

    }

    public StringBuilder getCategoriesSelect() {
        if (ct != null) {
            return ct.getCategoriesSelect();
        } else {
            return new StringBuilder();
        }
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEdit() {
        return UploadEdit;
    }

    public String getText() {
        return text;
    }

    public String getMessage() {
        return message.toString();
    }

    public String getUploadMessage() {
        return UploadMessage;
    }

    public String getUploadName() {
        return UploadName;
    }

    public String getUploadEmail() {
        return UploadEmail;
    }

    public String getUploadTitle() {
        return UploadTitle;
    }

    public String getUploadText() {
        return UploadText;
    }

    public String getUploadImageOn() {
        return UploadImageOn;
    }

    public String getUploadListContent() {
        return UploadListContent;
    }

    @Override
    public String getMetaTitle() {
        return "Изготовление поста.";
    }

    @Override
    public int getServerStatus() {


        return 200;

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
        return null;
    }

    @Override
    public String getMetaHead() {
        return "";
    }
}
