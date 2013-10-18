/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.sexdice;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author mark
 */
@WebServlet(name = "SexDice", urlPatterns = {"/SexDice"})
public class SexDice extends HttpServlet {

    synchronized protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String q = request.getParameter("q") != null ? request.getParameter("q") : "";
        String level = request.getParameter("level"), girl = request.getParameter("girl");

        HttpSession session = request.getSession(true);



        if (q.equals("photo") && session.getAttribute("SexDiceLevel") != null) {

            response.setContentType("image/jpeg");

            level = session.getAttribute("SexDiceLevel").toString();
            girl = session.getAttribute("SexDiceGirl").toString();
            System.out.println(getServletContext().getRealPath("/") + "WEB-INF/sexdice/kartinki/" + girl + "/" + level + ".jpg");

            File file = new File(getServletContext().getRealPath("/") + "WEB-INF/sexdice/kartinki/" + girl + "/" + level + ".jpg");
            byte[] fileBArray = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileBArray);

            BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
            bos.write(fileBArray);
            bos.flush();
            bos.close();

        } else if (q.equals("setSession")) {

            session.setAttribute("SexDiceLevel", level);
            session.setAttribute("SexDiceGirl", girl);

        } else if (q.equals("setTime")) {

            PrintWriter out = response.getWriter();
            FileWriter qwe = null;


            try {

                qwe = new FileWriter(getServletContext().getRealPath("/") + "WEB-INF/sexdice/time", false);
                qwe.write(request.getParameter("besttime"));
                qwe.close();

            } catch (Exception ex) {
                out.println(ex);
            } finally {
                if (qwe != null) {
                    qwe.close();
                }
            }


            out.println(request.getParameter("besttime"));


        } else if (q.equals("getTime")) {
            PrintWriter out = response.getWriter();

            File f = new File(getServletContext().getRealPath("/") + "WEB-INF/sexdice/time");
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            StringBuffer sb = new StringBuffer();
            String eachLine = br.readLine();
            while (eachLine != null) {
                sb.append(eachLine);
                eachLine = br.readLine();
            }
            out.println(sb.toString());
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
