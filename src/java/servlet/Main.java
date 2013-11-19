/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import logic.Add;
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
import logic.user.Feed;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "Main", urlPatterns = {"/index.html"})
public class Main extends HttpServlet {

    private static final Logger logger = Logger.getLogger(Main.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        //Cache-Control	
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache,no-store,max-age=0,must-revalidate");

        PrintWriter out = response.getWriter();

        String q = request.getParameter("q") != null ? request.getParameter("q") : "home.html";
        ArrayList args = new ArrayList(Arrays.asList(q.split("/")));

        //out.println(request.getQueryString());
        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt = null;

        try {
            try {
                stmt = conn.createStatement();
            } catch (Exception e) {
                logger.error("", e);
                response.sendError(500);
                return;
            }

            Check check = new Check(request, response, stmt);
            Creator creator = null;
            NavBlock block = new NavBlock(request, stmt, check);

            HashMap root = new HashMap();
            LinkedHashSet leftNav = new LinkedHashSet();
            LinkedHashSet rightNav = new LinkedHashSet();

            try {

                if ("home.html".equals(q) || "tag".equals(args.get(0).toString())
                        || "new".equals(args.get(0).toString())) {
                    creator = new logic.Home(request, args, conn);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }

                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/about.html");
                    leftNav.add("/block/ad-space.html");
                    q = "home.html";

                } else if ("search".equals(q)) {
                    creator = new logic.Search(request, response, stmt);
                    q += ".html";

                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }

                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/about.html");
                    leftNav.add("/block/ad-space.html");

                } else if ("anekdot".equals(q)) {
                    response.setStatus(302);
                    response.sendRedirect("/post?id=" + request.getParameter("id"));

                } else if ("post".equals(q)) {

                    creator = new Post(request, response, conn);
                    q += ".html";

                    root.put("user", check);
                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }

                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/ad-space.html");

                } else if ("add".equals(q)) {
                    if (check.getCheck()) {
                        creator = new Add(request, response, stmt, getServletContext().getRealPath("/"), check);

                        root.put("time_addition", block.getTimeAddition());
                        root.put("tags", block.getTagsLi());
                        root.put("widget", block.getWidget());

                        root.put("user", check);
                        if (check.getCheck()) {
                            leftNav.add("/block/user-menu.html");
                        } else {
                            leftNav.add("/block/user-form.html");
                        }

                        leftNav.add("/block/time-addition.html");
                        leftNav.add("/block/tags.html");
                        leftNav.add("/block/widget.html");
                        q += ".html";
                    } else {
                        response.sendRedirect("/");
                        return;
                    }
                } else if ("rss.html".equals(q)) {
                    creator = new Rss(stmt);

                    root.put("user", check);
                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");

                } else if ("catalog.html".equals(q)) {
                    creator = new Catalog(stmt);

                    root.put("user", check);
                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");

                } else if ("anekdoty.html".equals(q) || "kartinki.html".equals(q) || "aforizmy.html".equals(q)
                        || "rasskazy.html".equals(q) || "mysli-velikikh.html".equals(q) || "interesno.html".equals(q)
                        || "video".equals(q)
                        || "krik-dushi.html".equals(q) || "tsitata.html".equals(q) || "nadpisi.html".equals(q)
                        || "stishki.html".equals(q)
                        || args.get(0).toString().equals("kartinki")
                        || args.get(0).toString().equals("demotivatory")) {

                    response.sendRedirect("/");

                    q = "home.html";

                } /*else if ("terms_of_use.html".equals(q)) {
                 creator = new TermsOfUse(stmt);

                 root.put("time_addition", block.getTimeAddition());
                 root.put("tags", block.getTagsLi());
                 root.put("widget", block.getWidget());
                 leftNav.add("/block/time-addition.html");
                 leftNav.add("/block/tags.html");
                 leftNav.add("/block/widget.html");

                 }*/ else if ("link.html".equals(q)) {
                    creator = new Link(stmt);

                    root.put("user", check);
                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");

                } else if ("wtf.html".equals(q)) {
                    creator = new Wtf(stmt);

                    root.put("user", check);
                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }
                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");

                } else if ("edit".equals(q)) {
                    if (check.getCheck()) {
                        creator = new logic.Edit(request, args, stmt, check);

                        root.put("user", check);
                        root.put("time_addition", block.getTimeAddition());
                        root.put("tags", block.getTagsLi());
                        root.put("widget", block.getWidget());

                        if (check.getCheck()) {
                            leftNav.add("/block/user-menu.html");
                        } else {
                            leftNav.add("/block/user-form.html");
                        }

                        leftNav.add("/block/time-addition.html");
                        leftNav.add("/block/tags.html");
                        leftNav.add("/block/widget.html");
                        leftNav.add("/block/about.html");
                        leftNav.add("/block/ad-space.html");
                        q = "edit.html";
                    } else {
                        response.sendRedirect("/");
                        return;
                    }

                } else if ("feed".equals(q)) {
                    if (check.getCheck()) {
                        creator = new Feed(request, args, stmt, check);

                        root.put("time_addition", block.getTimeAddition());
                        root.put("tags", block.getTagsLi());
                        root.put("widget", block.getWidget());

                        root.put("user", check);
                        if (check.getCheck()) {
                            leftNav.add("/block/user-menu.html");
                        } else {
                            leftNav.add("/block/user-form.html");
                        }

                        leftNav.add("/block/time-addition.html");
                        leftNav.add("/block/tags.html");
                        leftNav.add("/block/widget.html");
                        leftNav.add("/block/about.html");
                        leftNav.add("/block/ad-space.html");
                        q = "user/feed-comment.html";
                    } else {
                        response.sendRedirect("/");
                        return;
                    }
                } else {
                    creator = new logic.user.Home(request, args, conn);

                    root.put("time_addition", block.getTimeAddition());
                    root.put("tags", block.getTagsLi());
                    root.put("widget", block.getWidget());

                    root.put("user", check);
                    if (check.getCheck()) {
                        leftNav.add("/block/user-menu.html");
                    } else {
                        leftNav.add("/block/user-form.html");
                    }

                    leftNav.add("/block/time-addition.html");
                    leftNav.add("/block/tags.html");
                    leftNav.add("/block/widget.html");
                    leftNav.add("/block/about.html");
                    leftNav.add("/block/ad-space.html");
                    q = "user/index.html";
                }

            } catch (Exception e) {
                logger.error("", e);
                throw new Exception(e);
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

            root.put("title", creator.getMetaTitle());

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

            //if (RandomAnecdote != null) {
            //root.put("block_bottom", RandomAnecdote);
            //}
            root.put("footer_tpl", "footer.html");

            new Templating().getTemplating(getServletContext().getRealPath("/")).process(root, out);

        } catch (Exception e) {
            logger.error("", e);
            response.sendError(500);
            //out.println("Упс! Что то сломалось(" + e);
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
