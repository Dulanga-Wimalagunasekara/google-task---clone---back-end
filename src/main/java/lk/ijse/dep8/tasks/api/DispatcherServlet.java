package lk.ijse.dep8.tasks.api;

import lk.ijse.dep8.tasks.util.HttpServlet2;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
@MultipartConfig(location = "/tmp",maxFileSize = 10*1023*1024)
@WebServlet(name = "DispatcherServlet", value = "/v1/users/*")
public class DispatcherServlet extends HttpServlet2 {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() == null || req.getPathInfo().equals("/")){
            System.out.println("user servlet");
            getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
        }else{
            if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/?")){
                System.out.println("user servlet");
                getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
            }else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists(/\\d+)?/?")){
                System.out.println("Task servlet");
                getServletContext().getNamedDispatcher("TaskListServlet").forward(req, resp);
            }
        }
    }
}
