/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import logic.SecretForm;
import core.JNDIConnection;
import core.Templating;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.*;
import logic.user.Check;

/**
 *
 * @author mark
 */
@WebServlet(name = "Main", urlPatterns = {"/index.html"})
public class Main extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        //Cache-Control	
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache,no-store,max-age=0,must-revalidate");

        PrintWriter out = response.getWriter();


        String q = request.getParameter("q") != null ? request.getParameter("q") : "home.html";
        ArrayList args = new ArrayList(Arrays.asList(q.split("/")));


        //System.out.println(q);


        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt = null;

        try {
            try {
                stmt = conn.createStatement();
            } catch (Exception e) {
                out.println(e + "Creates a Statement object for sending SQL statements to the database.");
                return;
            }

            

            //Check check = new Check(request, response, stmt);

            Creator creator = null;
            NavBlock block = new NavBlock(request, stmt);

            HashMap root = new HashMap();
            LinkedHashSet leftNav = new LinkedHashSet();
            LinkedHashSet rightNav = new LinkedHashSet();

            RandomAnecdote RandomAnecdote = null;
            try {



                if ("home.html".equals(q)) {
                    creator = new logic.Home(request, args, conn);
                    RandomAnecdote = new RandomAnecdote(stmt);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/about.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/ad-space.html");

                } else if ("search".equals(q)) {
                    creator = new logic.Search(request, response, stmt);
                    q += ".html";

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/about.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/ad-space.html");

                } else if ("anekdot".equals(q)) {
                    creator = new Anecdote(request, response, conn);
                    q += ".html";

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/ad-space.html");

                } else if ("secretform.html".equals(q)) {
                    creator = new SecretForm(request, response, stmt, getServletContext().getRealPath("/"));

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/widget.html");

                } else if ("rss.html".equals(q)) {
                    creator = new Rss(stmt);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/widget.html");

                } else if ("catalog.html".equals(q)) {
                    creator = new Catalog(stmt);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/widget.html");

                } else if ("anekdoty.html".equals(q) || "kartinki.html".equals(q) || "aforizmy.html".equals(q)
                        || "rasskazy.html".equals(q) || "mysli-velikikh.html".equals(q) || "interesno.html".equals(q)
                        || "video".equals(q)
                        || "krik-dushi.html".equals(q) || "tsitata.html".equals(q) || "nadpisi.html".equals(q)
                        || "stishki.html".equals(q)
                        || args.get(0).toString().equals("kartinki")
                        || args.get(0).toString().equals("demotivatory")) {

                    creator = new logic.Home(request, args, conn);
                    RandomAnecdote = new RandomAnecdote(stmt);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/about.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/ad-space.html");

                    q = "home.html";

                } else if ("terms_of_use.html".equals(q)) {
                    creator = new TermsOfUse(stmt);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/widget.html");

                } else if ("link.html".equals(q)) {
                    creator = new Link(stmt);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("category", block.getCategoriesLi());
                    root.put("widget", block.getWidget());
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/category.html");
                    leftNav.add("/block/widget.html");

                }




            } catch (Exception e) {
                out.println(e);
                return;
            }





            if (creator == null) {
                response.sendError(404);
                return;
            }


            if (creator.getServerStatus() != 200) {
                response.sendError(creator.getServerStatus());
                return;
            }



            // Last-Modified

            //out.println(">>"+new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US).format(creator.getLastModified()) + " GMT");

            try {
                if (creator.getLastModified() != null) {

                    if (request.getDateHeader("If-Modified-Since") >= creator.getLastModified().getTime()) {
                        response.setStatus(304);
                    }

                    response.setHeader("Last-Modified", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US).format(creator.getLastModified()) + " GMT");

                    root.put("LastModified", new SimpleDateFormat("dd MMMM yyyy г. HH:mm:ss").format(creator.getLastModified()) + " GMT");
                }
            } catch (Exception e) {
            }


            root.put("main", request.getParameter("q") == null && request.getParameter("page") == null ? "1" : "0");


            root.put("content_tpl", q);


            root.put("title", creator.getMetaTitle() + " &mdash; YourMood.Ru ");



            if (creator != null) {
                root.put("content", creator);
            }

            root.put("head", block.getTypeTitle() != null ? "" : creator.getMetaHead());

            //if (!leftNav.isEmpty()) {
            root.put("leftNav", leftNav);
            //}
            //if (!rightNav.isEmpty()) {
            root.put("rightNav", rightNav);
            //}

            if (RandomAnecdote != null) {
                root.put("block_bottom", RandomAnecdote);
            }


            root.put("footer_tpl", "footer.html");


            try {
                new Templating().getTemplating(getServletContext().getRealPath("/")).process(root, out);
            } catch (Exception e) {
                out.println("Упс! Что то сломалось(" + e);
            }




        } catch (Exception e) {
            out.println("Упс! Что то сломалось(" + e);
        } finally {
            jndi.close(stmt, null);
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}