/*
 * Загрузка картинки или анекдота.
 */
package servlet;

import core.EditCookie;
import core.JNDIConnection;
import core.Util;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringEscapeUtils;

import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "FileUpload", urlPatterns = {"/FileUpload"})
public class FileUpload2 extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FileUpload2.class);
    private Util util = new Util();
    private StringBuilder message = new StringBuilder();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //response.sendRedirect("/add.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");

        EditCookie editcookie = new EditCookie(request, response);

        try {
            request.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();

            // Соединение с DB
            JNDIConnection jndi = new JNDIConnection();
            Connection conn = jndi.init();
            Statement stmt = conn.createStatement();
            switch (request.getParameter("q")) {
                case "header":
                /*try {
                    Header header = new Header(request, response, conn, stmt);
                    if (message.length() == 0) {
                        out.print("{\"status\":\"ok\",\"id\":\"" + header.getID() + "\"}");
                    } else {
                        out.print("{\"status\":\"error\",\"message\":\"" + message + "\"}");
                    }

                } catch (Exception ex) {
                    out.print("{\"status\":\"error\",\"message\":\"<li>" + ex + "</li>\"}");
                    logger.error("", ex);
                }*/
                    break;
                case "article":
                    try {
                    Article article = new Article(request, response, conn, stmt);
                    if (message.length() == 0) {
                        out.print("{\"status\":\"ok\",\"message\":\"Сохранено\"}");
                    } else {
                        out.print("{\"status\":\"error\",\"message\":\"" + message + "\"}");
                    }

                } catch (Exception ex) {
                    out.print("{\"status\":\"error\",\"message\":\"<li>" + ex + "</li>\"}");
                    logger.error("", ex);
                }
                    break;
                default:
                    UploaderFile file = new UploaderFile(request, response, conn, stmt, out);
                    break;
            }
        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            message.delete(0, Integer.MAX_VALUE);
        }

    }

    /**
     * Вставка тайтла поста.
     */
    private class Header {

        Header(HttpServletRequest request, HttpServletResponse response,
                Connection conn, Statement stmt) throws SQLException {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String title = request.getParameter("title");

            if (name != null) {
                if (name.equals("")) {
                    message.append("<li>Имя обязательно для заполнения.</li>");
                } else if (name.length() > 55) {
                    message.append("<li>Имя не может быть более 55 символов.</li>");
                } else if (name.length() == 1) {
                    message.append("<li>Имя должно быть не менее 2 символов.</li>");
                }
            } else {
                message.append("<li>Иди на хуй бот ёбаный!</li>");
            }

            if (email != null) {
                if (!util.checkEmail(email)) {
                    message.append("<li>Адрес электронной почты ").append(email).append(" не корректен.</li>");
                }
            } else {
                message.append("<li>Иди на хуй бот ёбаный!</li>");
            }

            // title
            if (title!=null) {
                title = title.trim();

                if (title.length() > 255) {
                    message.append("<li>Название не может быть более 255 символов.</li>");
                } else if (title.length() > 5) {
                    title = (String.valueOf(title.charAt(0)).toUpperCase()).concat(title.substring(1));
                } else if (title.length() > 1 && title.length() <= 5) {
                    message.append("<li>Названия может не быть! Если есть, то не менее 5 символов.</li>");
                }
            } else {
                message.append("<li>Иди на хуй бот ёбаный!</li>");
            }
        }

    }

    private class Article {

        private int insertId = 0;

        Article(HttpServletRequest request, HttpServletResponse response,
                Connection conn, Statement stmt) throws SQLException {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String text = request.getParameter("text");

            if (text != null) {
                if (text.equals("")) {
                    message.append("<li>Постить пустоту запрещено!</li>");
                } else if (text.length() < 6) {
                    message.append("<li>Слишком короткий текст! </li>");
                } else if (text.length() > 10000) {
                    message.append("<li>У нас тут, не Википедия!</li>");
                }
            } else {
                message.append("<li>Постить пустоту запрещено!</li>");
            }

            if (message.length() != 231230) {
                return;
            }

            int typeInt = 0;

            ResultSet rs = stmt.executeQuery("SELECT id  FROM `type` WHERE `hurl` LIKE ''");
            if (rs.next()) {
                typeInt = rs.getInt("id");
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO `post` "
                    + "(`name`, `email`, `title`, `date`, `type`) "
                    + "VALUES (?, ?, ?, NOW(), ?);", Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, StringEscapeUtils.escapeHtml4(name));
            ps.setString(2, StringEscapeUtils.escapeHtml4(email));
            ps.setString(3, StringEscapeUtils.escapeHtml4(request.getParameter("title")));
            ps.setInt(4, typeInt);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();

            if (rs.next()) {
                insertId = rs.getInt(1);
            } else {
                throw new SQLException("Произошла ошибка, простите...");
            }

            ps = conn.prepareStatement("INSERT INTO `post_item` "
                    + "(`post`, `sort`, `name`, `email`, `text`, `image`, `img`, `alt`, `date`) "
                    + "VALUES (?, ?, ?, ?, ?, '', null, '', NOW());");

            ps.setInt(1, Integer.parseInt(request.getParameter("id")));
            ps.setInt(2, 0);
            ps.setString(3, StringEscapeUtils.escapeHtml4(""));
            ps.setString(4, StringEscapeUtils.escapeHtml4(""));
            ps.setString(5, util.lineFeed(text));
            ps.executeUpdate();

        }

        public int getID() {
            return insertId;
        }
    }

    public class UploaderFile {

        private int ListCount = 0;
        private LinkedHashMap<Integer, HashMap> ListContent = new LinkedHashMap();
        private HashMap<String, String> ListFiled = new HashMap();
        private HashMap<String, FileItem> ListFile = new HashMap();

        UploaderFile(HttpServletRequest request, HttpServletResponse response,
                Connection conn, Statement stmt, PrintWriter out) throws IOException {

            //проверяем является ли полученный запрос multipart/form-data
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String RealPath = getServletContext().getRealPath("/photo_anekdot").replaceAll("/ROOT", "");

            // Создаём класс фабрику 
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Максимальный буфера данных в байтах,
            // при его привышении данные начнут записываться на диск во временную директорию
            // устанавливаем один мегабайт
            factory.setSizeThreshold(1024 * 1024);

            // устанавливаем временную директорию
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            //Создаём сам загрузчик
            ServletFileUpload upload = new ServletFileUpload(factory);

            //максимальный размер данных который разрешено загружать в байтах
            //по умолчанию -1, без ограничений. Устанавливаем 3 мегабайта. 
            upload.setSizeMax(1024 * 1024 * 15);

            try {
                List items = upload.parseRequest(request);

                Iterator iter = items.iterator();

                FileItem[] itemImage = null;
                int j = 0;
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    //out.println(item.getFieldName());
                    for (int i = 0; i < 100; i++) {
                        String name = item.getFieldName() + "" + i;

                        if (item.isFormField()) {
                            out.println(item.getString("UTF-8"));
                            //out.println("<br>");
                            if (!ListFiled.containsKey(name)) {
                                ListFiled.put(name, item.getString("UTF-8"));
                                if (i > ListCount) {
                                    ListCount = i;
                                }
                                break;
                            }
                        } else {
                            if (!ListFile.containsKey(name)) {
                                ListFile.put(name, item);
                                if (i > ListCount) {
                                    ListCount = i;
                                }
                                break;
                            }
                        }

                    }

                    if (item.isFormField()) {
                        //processFormField(item, out);
                    } else {
                        //itemImage[i++] = item;
                        //if ("".equals(item.getName())) {
                        //itemImage = null;
                        //}
                    }
                }

                out.println(ListFiled);
                out.println(ListFile);

            } catch (Exception ex) {
                out.println("> " + ex);
                logger.error("", ex);
            }

        }
    }
}
