package com.mhframework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.mhframework.handler.controller.ClassMethod;
import com.mhframework.handler.controller.UrlHandler;
import com.mhframework.handler.view.ModelView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    private RequestDispatcher dispatcher;
    private HashMap<String, HashMap<String, ClassMethod>> mapUrl;
    private UrlHandler urlHandler;

    @Override
    public void init() throws ServletException {
        try {
            dispatcher = getServletContext().getNamedDispatcher("default");
            urlHandler = new UrlHandler();
            mapUrl = urlHandler.giveUrlMap();

            ServletContext context = getServletContext();
            context.setAttribute("urlMapping", mapUrl);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }

    }

    private boolean isRessource(String path) throws IOException {
        return getServletContext().getResource(path) != null;
    }

    private void shareData(HttpServletRequest req, ModelView modelView) {
        HashMap<String, Object> mapData = modelView.getData();
        mapData.forEach((key, value) -> {
            req.setAttribute(key, value);
        });
    }

    protected void service(HttpServletRequest req, HttpServletResponse res, String methode)
            throws ServletException, IOException {
        String path = req.getServletPath();
        System.out.println("METHOD : " + methode);
        if (isRessource(path)) {
            dispatcher.forward(req, res);
            return;
        } else {
            res.setContentType("text/plain;charset=UTF-8");

            PrintWriter out = res.getWriter();
            try {

                String url = req.getServletPath();

                ClassMethod classMethod = urlHandler.getByUrl(mapUrl, url, methode);

                if (classMethod != null) {
                    Method method = classMethod.getMethod();
                    Object valueOfInvoke = urlHandler.invokeMethodeUrl(req, classMethod);
                    if (method.getReturnType().equals(String.class)) {
                        out.println((String) valueOfInvoke);
                    } else if (method.getReturnType().equals(ModelView.class)) {
                        ModelView modelView = (ModelView) valueOfInvoke;
                        String viewName = modelView.getView();

                        shareData(req, modelView);
                        RequestDispatcher disp = req.getRequestDispatcher(viewName);
                        disp.forward(req, res);
                    } else {
                        throw new ServletException("Type de retour non validé");
                    }
                } else {
                    out.println("<h1>404 : Not Found</h1>");
                }

            } catch (Exception e) {
                e.printStackTrace(out);
                throw new ServletException(e.getMessage());
            }

        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "POST");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "GET");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "DELETE");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "PUT");
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "HEAD");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "OPTIONS");
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp, "TRACE");
    }
}
