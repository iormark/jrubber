/*
 * Загрузка картинки или анекдота.
 */
package servlet;

import core.EditCookie;
import core.GifDecoder;
import core.JNDIConnection;
import core.Util;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.TermsResponse;

/**
 *
 * @author mark
 */
@WebServlet(name = "FileUpload2", urlPatterns = {"/FileUpload2"})
public class FileUpload2 extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FileUpload2.class);
    private Util util = new Util();
    private StringBuilder message = new StringBuilder();
    private Check check = new Check();
    private static HashMap censure = new HashMap();

    {
        censure.put("пизда", "пи*да");
        censure.put("пизду", "пи*ду");
        censure.put("пиздой", "пи*дой");
        censure.put("пезда", "пе*да");
        censure.put("пездой", "пе*дой");
        censure.put("пездень", "пе*день");
        censure.put("пизданутый", "пи*данутый");
        censure.put("пизданутые", "пи*данутые");
        censure.put("пизданутая", "пи*данутая");
        censure.put("хуй", "х*й");
        censure.put("хуевый", "х*евый");
        censure.put("хуевая", "х*евая");
        censure.put("хуевое", "х*евое");
        censure.put("хуйня", "х*йня");
        censure.put("членосос", "чле*осос");
        censure.put("залупа", "за*упа");
        censure.put("залупой", "за*упой");
        censure.put("член", "ч*ен");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt;
        try {
            stmt = conn.createStatement();

            try {
                Autocomplete auto = new Autocomplete(request, response, conn, stmt, out);
                out.println(auto.getJson());
            } catch (Exception ex) {
                out.println(ex);
            } finally {
                jndi.close(stmt, null);
            }
        } catch (SQLException ex) {
            out.println(ex);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            response.setContentType("text/html;charset=UTF-8");

            EditCookie editcookie = new EditCookie(request, response);
            PrintWriter out = response.getWriter();
            String q = request.getParameter("q") != null ? request.getParameter("q") : "file";

            // Соединение с DB
            JNDIConnection jndi = new JNDIConnection();
            Connection conn = jndi.init();
            Statement stmt = conn.createStatement();

            try {
                request.setCharacterEncoding("UTF-8");

                System.out.println(">>>>" + q);
                switch (q) {
                    case "header":

                        check.heckName(request.getParameter("name"));
                        check.heckEmail(request.getParameter("email"));
                        check.heckTitle(request.getParameter("title"));
                        check.heckTags(request.getParameter("tags"));

                        break;
                    case "article":
                        System.out.println("text" + request.getParameter("key"));
                        Create create = new Create(request, response, conn, stmt);

                        String text = check.heckText(request.getParameter("text"));

                        create.createPost();
                        create.createPost_item(text);

                        create.createTags(check.heckTags(request.getParameter("tags")));

                        break;
                    case "file":

                        UploaderFile file = new UploaderFile(request, response, conn, stmt, out);

                        break;
                    case "create":
                        Create create2 = new Create(request, response, conn, stmt);
                        HashSet tags = check.heckTags(request.getParameter("tags"));

                        create2.createPost();
                        System.out.println("Tags: " + tags);
                        create2.updateItem_post();
                        create2.createTags(tags);

                        break;
                    case "autocomplete":
                        //Autocomplete auto = new Autocomplete(request, response, out);


                        break;
                }

            } catch (Exception ex) {
                message.append("<li>" + ex.getMessage().trim() + "</li>");
                logger.error("", ex);
            } finally {
                jndi.close(stmt, null);

                if (message.length() == 0) {
                    out.print("{\"status\":\"ok\",\"message\":\"Сохранено\"}");
                } else {
                    out.print("{\"status\":\"error\",\"action\":\"" + q + "\",\"message\":\"" + message.toString().trim() + "\"}");
                }

                message.delete(0, Integer.MAX_VALUE);
            }

        } catch (SQLException ex) {
            logger.error("", ex);
        }

    }

    private class Autocomplete {

        private PrintWriter out;
        private String tags = "";
        private List listTags = new LinkedList();

        private Autocomplete(HttpServletRequest request, HttpServletResponse response, Connection conn, Statement stmt, PrintWriter out) {
            this.out = out;
            tags = request.getParameter("tags") != null ? request.getParameter("tags") : "";
            if ("".equals(tags)) {
                return;
            }
            String[] tagArray = tags.split(",");
            tags = (tagArray[tagArray.length - 1]).trim();
            try {
                query(stmt);
            } catch (Exception ex) {
                logger.error("", ex);
            }
        }

        private List query(Statement stmt) throws IOException, Exception {
            ResultSet rs = stmt.executeQuery("SELECT t.tags, COUNT(*) AS count FROM tags t, tags_link tl WHERE t.tags LIKE '" + tags + "%' AND t.id=tl.tags GROUP BY tl.tags LIMIT 30");
            while (rs.next()) {
                listTags.add("<span class='tag'>" + rs.getString("tags") + "</span> × <span class='count'>" + rs.getString("count") + "</span>");
            }
            return listTags;
        }

        /**
         * Json
         *
         * @return
         */
        public String getJson() {
            String json = "[]";
            if (!listTags.isEmpty()) {
                json = "[\"" + tags + "\", [";
                for (Object i : listTags) {
                    json += "\"" + i + "\",";
                }
                json = json.replaceAll("[,]$", "") + "]]";
            }
            return json;
        }
    }

    private class Create {

        private Connection conn = null;
        private Statement stmt = null;

        private String name = null;
        private String email = null;
        private String title = null;
        private long key = 0;

        private int insertPostId = 0;

        public Create(Connection conn, Statement stmt) {
            this.conn = conn;
            this.stmt = stmt;
        }

        public Create(HttpServletRequest request, HttpServletResponse response, Connection conn, Statement stmt) throws SQLException {

            this.conn = conn;
            this.stmt = stmt;

            name = request.getParameter("name");
            email = request.getParameter("email");
            title = request.getParameter("title");

            try {
                key = Long.parseLong(request.getParameter("key"));
            } catch (NumberFormatException ex) {
                message.append("<li>Ошибочка вышла, простите...</li>");
                return;
            }

            check.heckName(name);
            check.heckEmail(email);
            title = check.heckTitle(title);

        }

        public void createPost() throws SQLException {
            if (message.length() != 0) {
                return;
            }

            System.out.println("------start-------");
            System.out.println("Query post");
            PreparedStatement ps = conn.prepareStatement("INSERT INTO `post` "
                    + "(`name`, `email`, `title`, `date`, `type`) "
                    + "VALUES (?, ?, ?, NOW(), ?);", Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, StringEscapeUtils.escapeHtml4(name));
            ps.setString(2, StringEscapeUtils.escapeHtml4(email));
            ps.setString(3, StringEscapeUtils.escapeHtml4(title));
            ps.setInt(4, 0);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                insertPostId = rs.getInt(1);
            } else {
                //message.append("<li>Ошибочка вышла, простите...</li>");
            }

            System.out.println("------end-------\n");
        }

        public void createPost_item(String text) throws SQLException {
            if (message.length() != 0) {
                return;
            }

            System.out.println("------start-------");
            System.out.println("Query post_item");
            PreparedStatement ps = conn.prepareStatement("INSERT INTO `post_item` "
                    + "(`post`, `sort`, `text`, `image`, `img`, `alt`, `date`, `key`) "
                    + "VALUES (?, ?, ?, null, null, '', NOW(), null);");

            ps.setInt(1, insertPostId);
            ps.setInt(2, 0);
            ps.setString(3, util.lineFeed(text));
            ps.executeUpdate();
            System.out.println("------end-------\n");
        }

        public void createPost_items(LinkedHashMap<Integer, HashMap> ListContent) throws SQLException {
            if (message.length() != 0) {
                return;
            }

            System.out.println("------start-------");
            System.out.println("Query post_items");
            for (int i = 0; i < ListContent.size(); i++) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO `post_item` "
                        + "(`post`, `sort`, `text`, `image`, `img`, `alt`, `date`, `key`) "
                        + "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?);");

                ps.setInt(1, 0);
                ps.setInt(2, Integer.parseInt((String) ListContent.get(i).get("sort")));
                ps.setString(3, util.lineFeed((String) ListContent.get(i).get("text")));

                String img = null;
                if (ListContent.get(i).containsKey("original")) {
                    img = (String) ListContent.get(i).get("original");
                }
                ps.setString(4, img);

                img = null;
                if (ListContent.get(i).containsKey("imgXml")) {
                    img = (String) ListContent.get(i).get("imgXml");
                }
                ps.setString(5, img);

                HashSet tags = check.heckTags((String) ListContent.get(i).get("tags"));
                ps.setString(6, tags.toString().replaceAll("[\\[\\]]", ""));

                ps.setString(7, (String) ListContent.get(i).get("key"));
                System.out.println(ps);
                ps.executeUpdate();
            }
            System.out.println("------end-------\n");
        }

        public void updateItem_post() throws SQLException {
            stmt.executeUpdate("UPDATE `post_item` SET post=" + insertPostId + ", `key`=null WHERE `key`='" + key + "'");
        }

        public void createTags(HashSet tagsMap) throws SQLException {
            if (message.length() != 0) {
                return;
            }

            System.out.println("------start-------");
            System.out.println("Query tags");
            for (Object element : tagsMap) {

                String teg = (String) element;
                PreparedStatement ps = conn.prepareStatement("INSERT IGNORE `tags` "
                        + "(`tags`) VALUES (?);", Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, teg);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int tagsId = 0;

                if (rs.next()) {
                    tagsId = rs.getInt(1);
                } else {

                    rs = stmt.executeQuery("SELECT id FROM `tags` WHERE `tags` = '" + teg + "' LIMIT 1");
                    if (rs.next()) {
                        tagsId = rs.getInt("id");
                    }
                }

                stmt.executeUpdate("INSERT IGNORE `tags_link`"
                        + " (tags, post) VALUES"
                        + " (" + tagsId + ", " + insertPostId + ")");
            }
            System.out.println("------end-------");
        }
    }

    public class UploaderFile {

        private int ListCount = 0;
        private LinkedHashMap<Integer, HashMap> ListContent = new LinkedHashMap();
        private HashMap<String, String> ListFiled = new HashMap();
        private HashMap<String, FileItem> ListFile = new HashMap();
        private String realPath = getServletContext().getRealPath("/").replaceAll("/ROOT", "");
        private String realPathLoad = "";

        UploaderFile(HttpServletRequest request, HttpServletResponse response,
                Connection conn, Statement stmt, PrintWriter out) throws Exception {

            System.out.println(realPath);

            //проверяем является ли полученный запрос multipart/form-data
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

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
            upload.setSizeMax(1024 * 1024 * 20);

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
                            if (!ListFiled.containsKey(name)) {

                                if ("key0".equals(name)) {
                                    ListFiled.put(name, Long.parseLong(item.getString("UTF-8")) + "");
                                } else {
                                    ListFiled.put(name, item.getString("UTF-8"));
                                }

                                break;
                            }
                        } else {
                            if (!ListFile.containsKey(name)) {
                                ListFile.put(name, item);
                                break;
                            }
                        }
                    }
                }

                //out.println(ListFiled);
                //out.println(ListFile);
                if (message.length() == 0 && !ListFile.isEmpty()) {
                    for (int i = 0; i < ListFile.size(); i++) {

                        if (!"".equals(ListFile.get("file" + i).getName())) {

                            HashMap imageItem = (processUploadedFile(ListFile.get("file" + i), out));

                            if (imageItem != null) {

                                HashMap meta = new HashMap();
                                meta.putAll(imageItem);
                                meta.put("text", ListFiled.get("text" + i));
                                meta.put("key", ListFiled.get("key" + i));
                                meta.put("sort", ListFiled.get("sort" + i));
                                meta.put("tags", ListFiled.get("tags" + i));
                                ListContent.put(i, meta);

                                if (!imageItem.containsKey("imgXml")) {
                                    deleteFile(out);
                                }
                            } else {
                                deleteFile(out);
                            }
                        }
                    }

                }

                if (message.length() == 0) {
                    Create create = new Create(conn, stmt);
                    create.createPost_items(ListContent);

                }

            } catch (Exception ex) {
                logger.error("", ex);
                System.out.println("File - " + ex);
                message.append("<li>" + ex.getMessage() + "</li>");
                deleteFile(out);
            }

            //out.println("<br>--------<br>");
            System.out.println("File: " + ListContent);
        }

        /**
         * Сохраняет файл на сервере, в папке. Сама папка должна быть уже
         * создана.
         *
         * @param item
         * @throws Exception
         */
        private HashMap processUploadedFile(FileItem item, PrintWriter out) throws Exception {

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            calendar.setTime(new Date());

            String loadPath = "/img/" + calendar.get(Calendar.YEAR) + "/" + calendar.get(Calendar.MONTH);
            File myPath = new File(realPath + loadPath);
            if (!myPath.exists()) {
                myPath.mkdirs();
            }

            realPathLoad = myPath.toString();

            HashMap<String, String> imageItem = new HashMap();
            String fileName = new File(item.getName()).getName();

            int pintPosition = fileName.lastIndexOf(".");
            String mimeType = fileName.substring(pintPosition, fileName.length());

            if (mimeType.equals(".jpeg") || mimeType.equals(".gif")
                    || mimeType.equals(".png") || mimeType.equals(".jpg")) {
            } else {
                message.append("<li>Файл не является изображением.</li>");
                deleteFile(out);
                return null;
            }

            String nameImage = null;
            File uploadetFile = null;

            //выбираем файлу имя пока не найдём свободное
            do {
                nameImage = Long.toString(new Date().getTime()) + mimeType;
                uploadetFile = new File(realPathLoad + "/" + nameImage);
            } while (uploadetFile.exists());

            imageItem.put("original", nameImage);
            item.write(uploadetFile);

            try {
                GifDecoder d = new GifDecoder();
                d.read(realPathLoad + "/" + nameImage);
                int gif = d.getFrameCount();

                //out.println("/home/mark/sites/service/yourmood/resize " + RealPath + " " + nameImage + (gif > 1 ? " [0]" : ""));
                // если GIF анимированный передаем какой кадр взять
                InputStream is = Runtime.getRuntime().exec("/home/mark/sites/service/yourmood/resize " + realPathLoad + " " + nameImage + (gif > 1 ? " [0]" : "")).getInputStream();

                String res = "";
                byte[] buf = new byte[512];
                int size;
                while ((size = is.read(buf)) > 0) {
                    res += new String(buf).substring(0, size);
                }

                if (!"".equals(res)) {
                    message.append("<li>" + res.trim() + "</li>");
                    return imageItem;
                }

                String imgXml = "";
                BufferedImage source = null;

                source = ImageIO.read(new File(realPathLoad + "/" + nameImage));
                imgXml = "<original "
                        + "width=\"" + source.getWidth() + "\" "
                        + "height=\"" + source.getHeight() + "\" "
                        + "size=\"" + item.getSize() + "\" "
                        + "animated=\"" + gif + "\" "
                        + "path=\"" + loadPath + "\">"
                        + nameImage + "</original>";

                source = ImageIO.read(new File(realPathLoad + "/middle_" + nameImage));
                imgXml += "<middle "
                        + "width=\"" + source.getWidth() + "\" "
                        + "height=\"" + source.getHeight() + "\"/>";

                source = ImageIO.read(new File(realPathLoad + "/small_" + nameImage));
                imgXml += "<small "
                        + "width=\"" + source.getWidth() + "\" "
                        + "height=\"" + source.getHeight() + "\"/>";

                imageItem.put("imgXml", imgXml);

            } catch (Exception ex) {
                logger.error("", ex);
                message.append("<li>Ошибка при загрузке изображения!</li>");
            }
            return imageItem;
        }

        private void deleteFile(PrintWriter out) {

            for (Map.Entry<Integer, HashMap> entry : ListContent.entrySet()) {
                //out.println(RealPath + "/small_" + entry.getValue().get("original"));
                new File(realPathLoad + "/small_" + entry.getValue().get("original")).delete();
                new File(realPathLoad + "/middle_" + entry.getValue().get("original")).delete();
                new File(realPathLoad + "/" + entry.getValue().get("original")).delete();

            }

        }
    }

    /**
     * Проверка тайтла.
     */
    private class Check {

        public void heckName(String name) {
            if (name != null) {
                if (name.equals("")) {
                    message.append("<li>Имя обязательно для заполнения.</li>");
                } else if (name.length() > 55) {
                    message.append("<li>Имя не может быть более 55 символов.</li>");
                } else if (name.length() == 1) {
                    message.append("<li>Имя должно быть не менее 2 символов.</li>");
                }
            } else {
                message.append("<li>Иди на хуй бот!</li>");
            }
        }

        public void heckEmail(String email) {
            if (email != null) {
                if (!util.checkEmail(email)) {
                    message.append("<li>Адрес электронной почты ").append(email).append(" не корректен.</li>");
                }
            } else {
                message.append("<li>Иди на хуй бот!</li>");
            }
        }

        public String heckTitle(String title) {
            if (title != null) {
                title = title.trim();

                if (title.length() > 255) {
                    message.append("<li>Название не может быть более 255 символов.</li>");
                } else if (title.length() > 5) {
                    title = (String.valueOf(title.charAt(0)).toUpperCase()).concat(title.substring(1));
                } else if (title.length() > 1 && title.length() <= 5) {
                    message.append("<li>Названия может не быть! Если есть, то не менее 5 символов.</li>");
                }
            } else {
                message.append("<li>Иди на хуй бот!</li>");
            }
            
            return title;
        }

        public String heckText(String text) {
            if (text != null) {
                if (text.equals("")) {
                    message.append("<li>Постить пустоту запрещено!</li>");
                } else if (text.length() <= 5) {
                    message.append("<li>Слишком короткий текст!</li>");
                } else if (text.length() > 10000) {
                    message.append("<li>У нас тут, не Википедия!</li>");
                }
            } else {
                message.append("<li>Постить пустоту запрещено!</li>");
            }

            return text;
        }

        private HashSet heckTags(String tags) {
            if (tags == null) {
                message.append("<li>Иди на хуй бот!</li>");
                return null;
            }

            tags = tags.trim();
            HashSet tagsMap = new HashSet();

            if (tags.length() > 120) {
                message.append("<li>Общая длина тегов не может превышать 240 символов</li>");
                return null;
            }

            String[] tagArray = tags.split(",");

            if (tagArray.length < 2) {
                message.append("<li>Добавьте несколько тегов</li>");
                return null;
            }

            if (tagArray.length > 6) {
                message.append("<li>Всего разрешено 6 тегов</li>");
                return null;
            }

            for (String key : tagArray) {
                String tag = key.trim();

                if (tag.matches("((?iu)[a-zа-яё0-9-*@(=:;)\\s]+)")) {
                    String[] wordArray = tag.split("[\\s]+");

                    if (wordArray.length > 4) {
                        message.append("<li>Один тег не может содержать более 4-х слов!</li>");
                        break;
                    }

                    if (tag.length() > 30) {
                        message.append("<li>Один тег не может превышать 30 символов!</li>");
                        break;
                    }

                    if (tag.length() <= 1) {
                        message.append("<li>Тег не может состоять из 1-го символа!</li>");
                        break;
                    }

                    String words = "";
                    for (String word : wordArray) {
                        words += !censure.containsKey(word) ? word : (String) censure.get(word);
                        words += " ";
                    }

                    tagsMap.add(words.trim());
                } else {
                    message.append("<li>Недопустимые символы в тегах!</li>");
                    break;
                }

            }
            return tagsMap;
        }

    }
}