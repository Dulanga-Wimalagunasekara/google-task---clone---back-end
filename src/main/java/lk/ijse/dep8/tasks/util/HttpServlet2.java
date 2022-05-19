package lk.ijse.dep8.tasks.util;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.apache.commons.httpclient.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "HttpServlet2", value = "/HttpServlet2")
public class HttpServlet2 extends HttpServlet {
    private Logger logger= Logger.getLogger(HttpServlet2.class.getName());
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            super.service(req, resp);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            e.printStackTrace(pw);
            resp.setContentType("application/json");
            HttpResponseErrorMsg errorMessage = null;
            if (e instanceof ResponseStatusException){
                ResponseStatusException err = (ResponseStatusException) e;
                errorMessage=new HttpResponseErrorMsg(new Date().getTime(), err.getStatus(), sw.toString(), e.getMessage(), req.getRequestURI());
                resp.setStatus(err.getStatus());
            }else {
                errorMessage=new HttpResponseErrorMsg(new Date().getTime(), 500, sw.toString(), e.getMessage(), req.getRequestURI());
            }
            Jsonb jsonb = JsonbBuilder.create();
            jsonb.toJson(errorMessage,resp.getWriter());
        }
    }
}
