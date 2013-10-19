/*
 * Загрузка картинки или анекдота.
 */
package servlet;

import core.EditCookie;
import core.GifDecoder;
import core.JNDIConnection;
import core.Util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
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
@WebServlet(name = "FileUpload", urlPatterns = {"/FileUpload_1"})
public class FileUpload_1 extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FileUpload_1.class);
    private Util util = new Util();
    private String button = "", name = "", email = "", type = "", text = "", nameImage = null, ImageOn = "", alt = "";
    private long getTime = 0;
    private StringBuilder message = new StringBuilder();
    private int GifDecoder = 0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("/secretform.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");

        EditCookie editcookie = new EditCookie(request, response);

        LinkedHashMap<Integer, HashMap> ListContent = new LinkedHashMap<Integer, HashMap>();
        int ListCount = 0;
        HashMap<String, String> ListFiled = new HashMap<String, String>();
        HashMap<String, FileItem> ListFile = new HashMap<String, FileItem>();

        try {
            request.setCharacterEncoding("UTF-8");


            PrintWriter out = response.getWriter();

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
                            //out.println(item.getString("UTF-8"));
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

                //out.println(ListCount);
                //out.println("<br>");
                //out.println(ListFiled);
                //out.println("<br>");
                //out.println(ListFile);
                //out.println("<br>");
                //out.println("<br>");




                for (int i = 0; i <= ListCount; i++) {


                    HashMap meta = new HashMap();

                    // Данные об отправителе

                    if (i == 0) {



                        if (editcookie.getCookie("name") != null) {
                            ListFiled.put("name0", editcookie.getCookie("name"));
                        }


                        if (ListFiled.containsKey("name0")) {
                            if (ListFiled.get("name0").equals("")) {
                                message.append("<li>Имя обязательно для заполнения.</li>");
                            } else if (ListFiled.get("name0").length() > 55) {
                                message.append("<li>Имя должно быть не более 55 символов.</li>");
                            } else if (ListFiled.get("name0").length() == 1) {
                                message.append("<li>Имя состоит из одной буквы? ну на хер!</li>");
                            }
                        } else {
                            //response.sendRedirect("http://46.4.48.29/");
                            message.append("<li>Иди на хуй бот ёбаный!</li>");
                            //return;
                        }


                        // e-mail

                        if (editcookie.getCookie("email") != null) {
                            ListFiled.put("email0", editcookie.getCookie("email"));
                        }

                        if (ListFiled.containsKey("email0")) {
                            if (!util.checkEmail(ListFiled.get("email0"))) {
                                message.append("<li>Адрес электронной почты ").append(ListFiled.get("email0")).append(" не корректен.</li>");
                            }
                        } else {
                            //response.sendRedirect("http://46.4.48.29/");
                            message.append("<li>Иди на хуй бот ёбаный!</li>");
                            //return;
                        }

                        // type

                        if (ListFiled.containsKey("type0")) {
                            if (ListFiled.get("type0").equals("")) {
                                message.append("<li>Выберите категорию!</li>");
                            }
                        } else {
                            //response.sendRedirect("http://46.4.48.29/");
                            message.append("<li>Иди на хуй бот ёбаный!</li>");
                            //return;
                        }


                        // title

                        if (ListFiled.containsKey("title0")) {
                            String title = ListFiled.get("title0").trim();

                            if (title.length() > 255) {
                                message.append("<li>Название не должно быть более 255 символов.</li>");
                            } else if (title.length() > 5) {
                                title = (String.valueOf(title.charAt(0)).toUpperCase()).concat(title.substring(1));
                                ListFiled.put("title0", title);
                            } else if (title.length() > 1 && title.length() <= 5) {
                                message.append("<li>Названия может не быть! Если есть, то не менее 5 символов.</li>");
                            }
                        } else {
                            message.append("<li>Иди на хуй бот ёбаный!</li>");
                        }


                    }



                    if (ListFile.containsKey("photo" + i)) {
                        if (ListFile.get("photo" + i).getName().equals("")) {


                            if (ListFiled.containsKey("text" + i)) {
                                if (ListFiled.get("text" + i).equals("")) {
                                    message.append("<li><b>Форма " + (i + 1) + ":</b> Постить пустоту запрещено!</li>");
                                } else if (ListFiled.get("text" + i).length() < 6) {
                                    message.append("<li><b>Форма " + (i + 1) + ":</b> Слишком короткий текст! Если вы считаете его достойным внимания, сообщите на <a href=\"mailto:neamno@yourmood.ru\">neamno@yourmood.ru</a></li>");
                                } else if (ListFiled.get("text" + i).length() > 10000) {
                                    message.append("<li><b>Форма " + (i + 1) + ":</b> Это чё доклад? У нас тут, не Википедия!</li>");
                                }
                            } else {
                                message.append("<li><b>Форма " + (i + 1) + ":</b> Постить пустоту запрещено!</li>");
                            }

                        } else {
                            if (ListFiled.get("alt" + i).equals("")) {
                                message.append("<li><b>Форма " + (i + 1) + ":</b> Описание картинки, важно для SEO!</li>");
                            }
                        }

                    } else {
                        message.append("<li><b>Форма " + (i + 1) + ":</b> Поле изображение отсутствует!</li>");
                    }


                    if (!ListFiled.containsKey("alt" + i)) {
                        message.append("<li><b>Форма " + (i + 1) + ":</b> Поле Описание картинки отсутствует!</li>");
                    }


                    meta.put("text", ListFiled.get("text" + i));
                    meta.put("alt", ListFiled.get("alt" + i));
                    //meta.put("photo", "");

                    ListContent.put(i, meta);

                }




                if (message.length() == 0 && !ListFile.isEmpty()) {
                    //out.println("Обработка фоток!");

                    for (int i = 0; i <= ListCount; i++) {

                        if (!"".equals(ListFile.get("photo" + i).getName())) {


                            HashMap imageItem = (processUploadedFile(ListFile.get("photo" + i), out, RealPath));

                            //out.println(imageItem);


                            if (!imageItem.isEmpty()) {
                                HashMap meta = new HashMap();
                                meta.putAll(imageItem);
                                meta.putAll(ListContent.get(i));
                                ListContent.put(i, meta);
                            } else {
                                message.append("<li>Ошибка при загрузке изображения!</li>");
                            }
                        }
                    }
                }

                //out.println(ListContent);



                if (message.length() == 0) {
                    out.println("<html><head><meta http-equiv=\"Refresh\" content=\"1; URL=http://yourmood.ru/\"></head>"
                            + "<body><b>Спасибо! Отправлено на проверку!</b></body></html>");



                    JNDIConnection jndi = null;
                    Connection conn = null;
                    Statement stmt = null;
                    try {

                        jndi = new JNDIConnection();
                        conn = jndi.init();

                        stmt = conn.createStatement();

                        int InsertId = 0;
                        int TypeInt = 0;

                        ResultSet rs = stmt.executeQuery("SELECT id  FROM `type` WHERE `hurl` LIKE '" + ListFiled.get("type0") + "'");
                        if (rs.next()) {
                            TypeInt = rs.getInt("id");
                        }

                        PreparedStatement ps = conn.prepareStatement("INSERT INTO `post` "
                                + "(`name`, `email`, `title`, `date`, `type`) "
                                + "VALUES (?, ?, ?, NOW(), ?);", Statement.RETURN_GENERATED_KEYS);

                        //ps.setInt(1, InsertId);
                        ps.setString(1, StringEscapeUtils.escapeHtml4(ListFiled.get("name0")));
                        ps.setString(2, StringEscapeUtils.escapeHtml4(ListFiled.get("email0")));
                        ps.setString(3, StringEscapeUtils.escapeHtml4(ListFiled.get("title0")));
                        ps.setInt(4, TypeInt);
                        ps.executeUpdate();

                        rs = ps.getGeneratedKeys();

                        if (rs.next()) {
                            InsertId = rs.getInt(1);
                        } else {
                            throw new SQLException("Произошла ошибка, прошу прощения!");
                        }


                        ps = conn.prepareStatement("INSERT INTO `post_item` "
                                + "(`post`, `sort`, `name`, `email`, `text`, `image`, `img`, `alt`, `date`) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW());");

                        for (int i = 0; i <= ListCount; i++) {
                            ps.setInt(1, InsertId);
                            ps.setInt(2, i);
                            ps.setString(3, StringEscapeUtils.escapeHtml4(ListFiled.get("name0")));
                            ps.setString(4, StringEscapeUtils.escapeHtml4(ListFiled.get("email0")));
                            ps.setString(5, util.lineFeed(ListContent.get(i).get("text").toString()));

                            String img = null;
                            if (ListContent.get(i).containsKey("photo")) {
                                img = ListContent.get(i).get("photo").toString();
                            }
                            ps.setString(6, img);

                            img = null;
                            if (ListContent.get(i).containsKey("img")) {
                                img = ListContent.get(i).get("img").toString();
                            }
                            ps.setString(7, img);

                            ps.setString(8, StringEscapeUtils.escapeHtml4(util.Shortening(ListContent.get(i).get("alt").toString(), 240, "\\s")));
                            ps.executeUpdate();
                        }


                        editcookie.setCookie("name", ListFiled.get("name0"), null, 3600 * 24 * 365 * 100);
                        editcookie.setCookie("email", ListFiled.get("email0"), null, 3600 * 24 * 365 * 100);

                    } catch (Exception ex) {
                        deleteFile(RealPath, ListContent);
                        out.println(">> " + ex);
                        logger.error("", ex);
                    } finally {
                        nameImage = null;
                        jndi.close(stmt, null);
                        out.close();
                    }



                }



            } catch (Exception ex) {
                deleteFile(RealPath, ListContent);

                out.println("> " + ex);
                logger.error("", ex);
            }

            //out.println("message: " + message);

        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            if (message.length() != 0) {
                try {
                    if (editcookie.getCookie("name") != null && editcookie.getCookie("email") != null) {
                        request.setAttribute("edit", "true");
                    }
                } catch (UnsupportedEncodingException ex) {
                    logger.error("", ex);
                }


                request.setAttribute("message", message);
                
                request.setAttribute("name", ListFiled.get("name0").equals("") ? null : ListFiled.get("name0"));
                request.setAttribute("email", ListFiled.get("email0").equals("") ? null : ListFiled.get("email0"));
                request.setAttribute("title", ListFiled.get("title0"));
                request.setAttribute("ListContent", ListContent);

                StringBuilder content = new StringBuilder();

                int i = 0;
                for (Map.Entry<Integer, HashMap> entry : ListContent.entrySet()) {
                    i++;

                    content.append("<fieldset id=\"list-" + i + "\">"
                            + "<legend>Форма №" + i + "" + (i > 1 ? " <a style=\"color:red;\" href=\"javascript:deleteItem(" + i + ")\">Удалить</a> &nbsp;" : "") + "</legend>"
                            + "<div class=\"substrate\">"
                            + "<div>"
                            + "Анекдот / Надпись над картинкой:"
                            + "<textarea name=\"text\" rows=\"9\" class=\"inp\">" + entry.getValue().get("text") + "</textarea>"
                            + "</div>"
                            + "<div style=\"text-align: left\">"
                            + "Загрузить картинку?<br> <input type=\"file\" name=\"photo\">"
                            + "</div>"
                            + "<div>"
                            + "Описание картинки (255 сим.):"
                            + "<input name=\"alt\" value=\"" + entry.getValue().get("alt") + "\" style=\"width: 99%\" class=\"inp\">"
                            + "</div>"
                            + "</div>"
                            + "</fieldset>");
                }

                request.setAttribute("ListContent", content);
            }

        }



        try {
            if (message.length() == 0) {
                response.sendRedirect("/");
            } else {
                RequestDispatcher rd = request.getRequestDispatcher("/secretform.html");
                rd.forward(request, response);
            }
        } catch (IOException | ServletException ex) {
            logger.error("", ex);
        } finally {
            message.delete(0, Integer.MAX_VALUE);
        }

    }

    private void deleteFile(String RealPath, LinkedHashMap<Integer, HashMap> ListContent) {

        for (Map.Entry<Integer, HashMap> entry : ListContent.entrySet()) {

            new File(RealPath + "/small_" + entry.getValue().get("photo")).delete();
            new File(RealPath + "/middle_" + entry.getValue().get("photo")).delete();
            new File(RealPath + "/" + entry.getValue().get("photo")).delete();

        }

    }

    /**
     * Сохраняет файл на сервере, в папке upload. Сама папка должна быть уже
     * создана.
     *
     * @param item
     * @throws Exception
     */
    private HashMap processUploadedFile(FileItem item, PrintWriter out, String RealPath) throws Exception {

        HashMap<String, String> imageItem = new HashMap();

        String fileName = new File(item.getName()).getName();

        int pintPosition = fileName.lastIndexOf(".");
        String mimeType = fileName.substring(pintPosition, fileName.length());

        if (mimeType.equals(".jpeg") || mimeType.equals(".gif")
                || mimeType.equals(".png") || mimeType.equals(".jpg")) {
        } else {
            //message.append("<li>Файл не является изображением.</li>");
            return null;
        }



        String nameImage = null;
        File uploadetFile = null;
        //выбираем файлу имя пока не найдём свободное
        do {
            nameImage = Long.toString(new Date().getTime()) + mimeType;

            uploadetFile = new File(RealPath + "/" + nameImage);

        } while (uploadetFile.exists());

        //создаём файл
        //uploadetFile.;

        //String getParent = uploadetFile.getParent(), getName = uploadetFile.getName();

        //File file = File.createTempFile(getName, "", new File(getParent));

        //System.out.println(getParent+"\n"+mimeType+"\n"+getName);
        item.write(uploadetFile);



        try {

            GifDecoder d = new GifDecoder();
            d.read(RealPath + "/" + nameImage);
            int gif = d.getFrameCount();


            //out.println("/home/mark/sites/service/yourmood/resize " + RealPath + " " + nameImage + (gif > 1 ? " [0]" : ""));

            // если GIF анимированный передаем какой кадр взять
            InputStream is = Runtime.getRuntime().exec("/home/mark/sites/service/yourmood/resize " + RealPath + " " + nameImage + (gif > 1 ? " [0]" : "")).getInputStream();

            String res = "";
            byte[] buf = new byte[512];
            int size;
            while ((size = is.read(buf)) > 0) {
                res += new String(buf).substring(0, size);
            }
            if (!"".equals(res)) {
                message.append("<li>" + res + "</li>");
                throw new IOException("Ошибка при обработке изображения!");
            }

            String imgXml = "";
            BufferedImage source = null;

            source = ImageIO.read(new File(RealPath + "/" + nameImage));
            imgXml = "<original "
                    + "width=\"" + source.getWidth() + "\" "
                    + "height=\"" + source.getHeight() + "\" "
                    + "size=\"" + item.getSize() + "\" "
                    + "animated=\"" + gif + "\">"
                    + "" + nameImage + "</original>";


            source = ImageIO.read(new File(RealPath + "/middle_" + nameImage));
            imgXml += "<middle "
                    + "width=\"" + source.getWidth() + "\" "
                    + "height=\"" + source.getHeight() + "\"/>";


            source = ImageIO.read(new File(RealPath + "/small_" + nameImage));
            imgXml += "<small "
                    + "width=\"" + source.getWidth() + "\" "
                    + "height=\"" + source.getHeight() + "\"/>";

            imageItem.put("photo", nameImage);
            imageItem.put("img", imgXml);

        } catch (Exception ex) {
            LinkedHashMap<Integer, HashMap> ListContent = new LinkedHashMap();
            HashMap meta = new HashMap();
            meta.put("photo", nameImage);
            ListContent.put(0, meta);
            deleteFile(RealPath, ListContent);

            out.println("> " + ex);

            logger.error("", ex);

            throw new IOException(ex);
        }






        //rescale(item.getInputStream(), getParent + "/" + getName, mimeType.replace(".", ""), 2024, 3500);
        //rescale(item.getInputStream(), getParent + "/middle_" + getName, mimeType.replace(".", ""), 600, 3500);
        //rescale(item.getInputStream(), getParent + "/small_" + getName, mimeType.replace(".", ""), 220, 220);


        return imageItem;
    }

    private void rescale(InputStream input, String pathName, String mimeType, int tw, int th) throws IOException {

        // Open the source image
        BufferedImage source = ImageIO.read(input);

        int w = source.getWidth(), h = source.getHeight();


        if (w < h) {
            tw = -1;
        }

        if (h < w) {
            th = -1;
        }

        if (h < th && th != -1) {
            th = h;
        }

        if (w < tw && tw != -1) {
            tw = w;
        }

        if (tw < th) {
            th = -1;
        }



        // Get the scaled instance
        Image scaled = source.getScaledInstance(tw, th, Image.SCALE_AREA_AVERAGING);



        // Create the image
        BufferedImage image = new BufferedImage(tw != -1 ? tw : scaled.getWidth(null), th != -1 ? th : scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
        // Create the graphics
        Graphics2D graphics = image.createGraphics();
        try {

            // Set rendering hints
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // Set the color
            graphics.setColor(Color.WHITE);
            // Paint the white rectangle
            graphics.fillRect(0, 0, tw, th);
            // Draw the image
            graphics.drawImage(scaled, 0, 0, null);

        } finally {
            // Always dispose the graphics
            graphics.dispose();
        }
        // Write the rescaled image
        ImageIO.write(image, mimeType, new File(pathName));

    }
}
