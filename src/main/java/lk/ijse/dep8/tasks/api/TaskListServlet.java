package lk.ijse.dep8.tasks.api;

import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "TaskListServlet")
public class TaskListServlet extends HttpServlet2 {
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String pattern = "/users/[A-Fa-f0-9\\-]{36}/list/?.*";
        if (pathInfo==null){
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        if (pathInfo.matches(pattern)){
            super.service(req,res);
        }else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid Content Type Or Content Type is Empty");
        }

        String pattern = "/users/[A-Fa-f0-9\\-]{36}/list/?.*";
        if (!req.getPathInfo().matches(pattern)){
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"Invalid end point");
        }

    }
}
