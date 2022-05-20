package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "TaskServlet")
public class TaskServlet extends HttpServlet {

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
    private TaskDTO getTaskList(HttpServletRequest req){
        String pattern = "^/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/tasks/(\\d+)/?$";
        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    String.format("Invalid end point for %s request", req.getMethod()));
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        int taskListId = Integer.parseInt(matcher.group(2));
        int taskId = Integer.parseInt(matcher.group(3));

        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.
                    prepareStatement("SELECT * FROM task_list tl INNER JOIN task t WHERE t.id=? AND tl.id=? AND tl.user_id=?");
            stm.setInt(1, taskId);
            stm.setInt(2, taskListId);
            stm.setString(3, userId);
            ResultSet rst = stm.executeQuery();
            if (rst.next()) {
                int id = rst.getInt("id");
                String title = rst.getString("title");
                String details = rst.getString("details");
                int position = rst.getInt("position");
                String status = rst.getString("status");
                return new TaskDTO(id, title, position, details, status, taskListId);
            } else {
                throw new ResponseStatusException(404, "Invalid user id or task list id");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(500, "Failed to fetch task list details");
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid Content Type Or Content Type is Empty");
        }

        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/(\\d)+/tasks/?";
        if (!req.getPathInfo().matches(pattern)){
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"Invalid end point");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        int taskListId = Integer.parseInt(matcher.group(2));

        Connection connection=null;

        try{
            connection = pool.get().getConnection();
            PreparedStatement stm1 = connection.prepareStatement("SELECT * FROM task_list t WHERE t.id=? AND t.user_id=?");
            stm1.setInt(1,taskListId);
            stm1.setString(2,userId);
            if (!stm1.executeQuery().next()) {
                throw new ResponseStatusException(404, "Invalid user id for task list id");
            }

            Jsonb jsonb = JsonbBuilder.create();
            TaskDTO task = jsonb.fromJson(req.getReader(), TaskDTO.class);
            if (task == null || task.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            }
            task.setPosition(0);
            task.setStatusAsEnum(TaskDTO.Status.NEEDS_ACTION);
            connection.setAutoCommit(false);
            pushDown(connection,0);
            PreparedStatement stm = connection.prepareStatement("INSERT INTO task (title, details, position, status, task_list_id) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, task.getTitle());
            stm.setString(2, task.getNotes());
            stm.setInt(3, task.getPosition());
            stm.setString(4, task.getStatus().toString());
            stm.setInt(5, taskListId);

            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to save the task list");
            }

            ResultSet rst = stm.getGeneratedKeys();
            rst.next();
            task.setId(rst.getInt(1));
            connection.commit();
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(task, resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }catch (JsonbException e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }finally {
            try {
                if (connection!=null && !connection.getAutoCommit()){
                    connection.rollback();
                    connection.close();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void pushUp(Connection connection, int position) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE task t SET position = position-1 WHERE t.position=? ORDER BY t.position");
        stm.setInt(1,position);
        int i = stm.executeUpdate();

    }
    private void pushDown(Connection connection, int position) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE task t SET position = position+1 WHERE t.position=? ORDER BY t.position");
        stm.setInt(1,position);
        int i = stm.executeUpdate();

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TaskDTO task = getTaskList(req);
        Connection connection=null;
        try {
            connection=pool.get().getConnection();
            connection.setAutoCommit(false);
            pushUp(connection,task.getPosition());
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task WHERE id=?");
            stm.setInt(1,task.getId());
            if (stm.executeUpdate()!=1){
                throw new SQLException("Failed to delete the task");
            }
            connection.commit();
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ResponseStatusException(500, e.getMessage(),e);
        }finally {
            try {
                if (connection!=null){
                    connection.rollback();
                    connection.close();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(),e);
            }
        }
    }
}
