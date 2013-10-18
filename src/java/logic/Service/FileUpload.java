/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.Service;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author mark
 */
public class FileUpload {
    
    public FileUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //проверяем является ли полученный запрос multipart/form-data

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        System.out.println(isMultipart);
        
        if (!isMultipart) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;

        }

    }
}
