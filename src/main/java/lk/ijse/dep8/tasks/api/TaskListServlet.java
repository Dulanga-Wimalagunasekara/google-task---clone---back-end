package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "TaskListServlet")
public class TaskListServlet extends HttpServlet2 {
    private AtomicReference<DataSource> pool;
    private final Logger logger = Logger.getLogger(TaskListServlet.class.getName());
    @PostConstruct
    public void init(){
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp//env/jdbc/pool");
            pool = new AtomicReference<>(ds);
        } catch (NamingException e) {
            logger.severe("Failed to locate the JNDI pool");
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid Content Type Or Content Type is Empty");
        }

        String pattern = "/([A-Fa-f0-9\\-]{36})/list/?.*";
        if (!req.getPathInfo().matches(pattern)){
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"Invalid end point");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);

        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM user WHERE id = ?");
            stm.setString(1,userId);
            ResultSet rst = stm.executeQuery();
            if (!rst.next()){
                throw new ResponseStatusException(404,"Invalid User");
            }
            Jsonb jsonb = JsonbBuilder.create();
            TaskListDTO taskList = jsonb.fromJson(req.getReader(), TaskListDTO.class);
            if (taskList.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            }
            stm = connection.prepareStatement("INSERT INTO task_list (name,user_id) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, taskList.getTitle());
            stm.setString(2, userId);
            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to save the task list");
            }
            rst = stm.getGeneratedKeys();
            rst.next();
            taskList.setId(rst.getInt(1));
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(taskList, resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }catch (JsonbException e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }

    }

    private TaskListDTO getTaskList(HttpServletRequest req){
        String pattern = "/([A-Fa-f0-9\\-]{36})/list/(\\d+)/?";
        if (!req.getPathInfo().matches(pattern)){
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    String.format("Invalid end point for %s request",req.getMethod()));
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        String taskListId = matcher.group(2);

        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task_list t WHERE t.id = ? AND t.user_id=?");
            stm.setString(1, taskListId);
            stm.setString(1, userId);
            ResultSet rst = stm.executeQuery();
            if (rst.next()){
                int id = rst.getInt("id");
                String name = rst.getString("name");
                String user_id = rst.getString("user_id");
                return new TaskListDTO(id,name,user_id);
            }else {
                throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND,"Invalid user id or task list id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
