package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.tasks.security.SecurityContextHolder;
import lk.ijse.dep8.tasks.util.HttpResponseErrorMsg;
import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MultipartConfig(location = "/tmp",maxFileSize = 10*1023*1024)
@WebServlet(name = "DispatcherServlet", value = "/v1/users/*")
public class DispatcherServlet extends HttpServlet2 {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() == null || req.getPathInfo().equals("/")){
            getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
        }else{
            String pattern = "/([A-Fa-f0-9\\-]{36})/?.*";
            Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
            if (matcher.find()){
                String requestUrl = matcher.group(1);
                if (!requestUrl.equals(SecurityContextHolder.getPrincipal().getId())){
                    resp.setContentType("application/json");
                    resp.setStatus(403);
                    HttpResponseErrorMsg invalid_location = new HttpResponseErrorMsg(new Date().getTime(), 404, null, "Permission Denied", req.getRequestURI());
                    Jsonb jsonb = JsonbBuilder.create();
                    jsonb.toJson(invalid_location,resp.getWriter());
                    return;
                }
            }
            if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/?")){
                getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
            }else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists(/\\d+)?/?")){
                getServletContext().getNamedDispatcher("TaskListServlet").forward(req, resp);
            }else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists/\\d/tasks(/\\d+)?/?")){
                getServletContext().getNamedDispatcher("TaskServlet").forward(req,resp);
            }else {
                resp.setContentType("application/json");
                resp.setStatus(404);
                HttpResponseErrorMsg invalid_location = new HttpResponseErrorMsg(new Date().getTime(), 404, null, "Invalid Location", req.getRequestURI());
                Jsonb jsonb = JsonbBuilder.create();
                jsonb.toJson(invalid_location,resp.getWriter());
            }
        }
    }
}
