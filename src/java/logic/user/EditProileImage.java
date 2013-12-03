/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.user;

import core.Util;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import javax.imageio.ImageIO;

/**
 *
 * @author mark
 */
public class EditProileImage {

    private String message = "";
    private Util util = new Util();

    public String fileProcessing(
            String realPath,
            String filename,
            String contentType,
            InputStream is,
            HashMap avatar) throws Exception {

        String itemImage = null;
        HashSet files = new HashSet();

        File directory = null;
        String loadPath = null;
        String _nameImage = null;
        String nameImage = null;

        if (avatar != null) {
            loadPath = (String) avatar.get("path");
            _nameImage = (String) avatar.get("name");
            directory = new File(realPath + loadPath);
        } else {
            loadPath = "/img/usr";
            directory = getDirectory(realPath + loadPath);
            loadPath += "/" + directory.getName();
        }

        if (contentType != null) {
            if (contentType.equals("image/jpeg") || contentType.equals("image/gif")
                    || contentType.equals("image/png") || contentType.equals("image/jpg")) {
            } else {
                message = "Файл не является изображением.";
                return itemImage;
            }
        } else {
            message = "Файл не является изображением.";
            return itemImage;
        }

        File uploadetFile = null;

        //выбираем файлу имя пока не найдём свободное
        do {
            String mimeType = filename.substring(filename.lastIndexOf("."), filename.length()).toLowerCase();
            nameImage = Long.toString(new Date().getTime()) + mimeType;
            uploadetFile = new File(directory.toString() + "/" + nameImage);
        } while (uploadetFile.exists());

        FileOutputStream fos = new FileOutputStream(uploadetFile);
        int data = 0;
        while ((data = is.read()) != -1) {
            fos.write(data);
        }
        fos.close();

        is = Runtime.getRuntime().exec("/home/mark/sites/service/yourmood/usr_resize " + directory.toString() + " " + nameImage).getInputStream();

        String outstr = "";
        byte[] buf = new byte[512];
        int size;
        while ((size = is.read(buf)) > 0) {
            outstr += new String(buf).substring(0, size);
        }

        if (!"".equals(outstr)) {

            files.clear();
            files.add(directory.toString() + "/" + nameImage);
            files.add(directory.toString() + "/b" + nameImage);
            files.add(directory.toString() + "/m" + nameImage);
            files.add(directory.toString() + "/s" + nameImage);
            util.deleteFile(files);
            message = files + " = " + outstr.trim();
            return itemImage;
        }

        String imgXml = "";
        BufferedImage source = null;

        try {
            source = ImageIO.read(new File(directory.toString() + "/" + nameImage));
            imgXml = "<o "
                    + "w=\"" + source.getWidth() + "\" "
                    + "h=\"" + source.getHeight() + "\" "
                    + "p=\"" + loadPath + "\">"
                    + nameImage + "</o>";

            itemImage = imgXml;
        } catch (CMMException ex) {
            files.clear();
            files.add(directory.toString() + "/" + nameImage);
            files.add(directory.toString() + "/b" + nameImage);
            files.add(directory.toString() + "/m" + nameImage);
            files.add(directory.toString() + "/s" + nameImage);
            util.deleteFile(files);
            message = ex.getMessage();
            throw new CMMException(ex.getMessage());
       }

        // delete old image
        if (_nameImage != null) {
            files.add(directory.toString() + "/" + _nameImage);
            files.add(directory.toString() + "/b" + _nameImage);
            files.add(directory.toString() + "/m" + _nameImage);
            files.add(directory.toString() + "/s" + _nameImage);
            util.deleteFile(files);
        }

        return itemImage;
    }

    private File getDirectory(String path) {
        File directory = new File(path);
        int countDirectory = directory.listFiles().length;
        int countFiles = 0;
        File newDirectory = null;

        if (countDirectory > 0) {
            // count files
            newDirectory = new File(directory.getPath() + "/" + countDirectory);
            countFiles = newDirectory.listFiles().length;

            if (countFiles >= 10000) {
                newDirectory = new File(directory.getPath() + "/" + (countDirectory + 1));
                newDirectory.mkdir();

            }
        } else {
            // if empty, create
            newDirectory = new File(directory.getPath() + "/" + (countDirectory + 1));
            newDirectory.mkdir();
        }

        return newDirectory;
    }

    public String getMessage() {
        return message;
    }
}
