
package logic.user;

import core.GifDecoder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private HashMap<String, String> ListField = new HashMap();
    private HashMap<String, FileItem> ListFile = new HashMap();
    private String realPath = "";
    private String realPathLoad = "";
    private String message = "";
    private int insertItem = 0;
    private ResultSet rs = null;

    FileUploader(String realPath, String loadPath, HttpServletRequest request, HttpServletResponse response,
            Connection conn, Statement stmt, PrintWriter out, Check check) throws Exception {

        this.realPath = realPath;

        //System.out.println(realPath);

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
        upload.setSizeMax(1024 * 1024 * 5);

        try {
            List items = upload.parseRequest(request);

            Iterator iter = items.iterator();

            FileItem[] itemImage = null;
            int j = 0;
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                //out.println(item.getFieldName());
                for (int i = 0; i < 100; i++) {
                    String name = item.getFieldName();
                    //System.out.println(name+": "+item.getString("UTF-8"));
                    
                    if (item.isFormField()) {
                        if (!ListContent.containsKey(i)) {
                            HashMap meta = new HashMap();
                            ListContent.put(i, meta);
                        }
                        if (!ListContent.get(i).containsKey(name)) {

                            if (("key" + i).equals(name)) {
                                ListContent.get(i).put(name, Long.parseLong(item.getString("UTF-8")) + "");
                            } else {
                                ListContent.get(i).put(name, item.getString("UTF-8"));
                            }

                            break;
                        }
                    } else {
                        if (!ListFile.containsKey(name + "" + i)) {
                            ListFile.put(name + "" + i, item);
                            break;
                        }
                    }
                }
            }
            
            System.out.println("ListContent: "+ListContent);

            if (message.length() == 1111111110) {
                if (ListFile.isEmpty()) {
                } else {
                    for (int i = 0; i < ListFile.size(); i++) {

                        if (!"".equals(ListFile.get("file" + i).getName())) {
                            HashMap imageItem = (processUploadedFile(ListFile.get("file" + i), loadPath, out));

                            if (imageItem != null) {
                                HashMap meta = ListContent.get(i);
                                meta.putAll(imageItem);
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
            }

        } catch (Exception ex) {
            message = ("" + ex.getMessage() + "");
            deleteFile(out);
            throw new Exception(ex);
        }
    }

    /**
     * Сохраняет файл на сервере, в папке Папка должна быть уже создана.
     *
     * @param item
     * @throws Exception
     */
    private HashMap processUploadedFile(FileItem item, String loadPath, PrintWriter out) throws Exception {

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
            throw new Exception(ex);
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

    public int getInsertItem() {
        return insertItem;
    }

    public String getMessage() {
        return message;
    }
}
