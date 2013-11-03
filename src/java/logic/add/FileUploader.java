/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.add;

import core.GifDecoder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Check;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author mark
 */
public class FileUploader {

    private LinkedHashMap<Integer, HashMap> ListContent = new LinkedHashMap();
    private HashMap<String, String> ListFiled = new HashMap();
    private HashMap<String, FileItem> ListFile = new HashMap();
    private String realPath = "";
    private String realPathLoad = "";
    private String message = "";

    FileUploader(String realPath, HttpServletRequest request, HttpServletResponse response,
            Connection conn, Statement stmt, PrintWriter out, Check check) throws Exception {

        this.realPath = realPath;

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

            System.out.println(ListFiled);
            //System.out.println(ListFile);
            if (message.length() == 0 && !ListFile.isEmpty()) {
                for (int i = 0; i < ListFile.size(); i++) {

                    if (!"".equals(ListFile.get("file" + i).getName())) {

                        HashMap imageItem = (processUploadedFile(ListFile.get("file" + i), out));

                        if (imageItem != null) {

                            HashMap meta = new HashMap();
                            meta.putAll(imageItem);
                            meta.put("text", ListFiled.get("text" + i));
                            meta.put("video", ListFiled.get("video" + i));
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
                FieldCheck fc = new FieldCheck((String) ListContent.get(0).get("text"), (String) ListContent.get(0).get("video"));
                HashSet tags = fc.checkTags((String) ListContent.get(0).get("tags"));
                message = fc.getMessage();

                if (!fc.getMessage().equals("")) {
                    deleteFile(out);
                } else {
                    PostCreate pc = new PostCreate(conn, stmt, check);
                    pc.createPost_items(ListContent, tags);
                }
            }

        } catch (Exception ex) {
            //logger.error("", ex);
            System.out.println("File - " + ex);
            message = ("" + ex.getMessage() + "");
            deleteFile(out);
        }

        //out.println("<br>--------<br>");
        System.out.println("File: " + ListContent);
    }

    /**
     * Сохраняет файл на сервере, в папке. Сама папка должна быть уже создана.
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
            message = ("Файл не является изображением.");
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
                message = ("" + res.trim() + "");
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
            //logger.error("", ex);
            message = ("Ошибка при загрузке изображения!");
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

    public String getMessage() {
        return message;
    }
}
