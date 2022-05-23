package lk.ijse.dep8.tasks.api;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
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
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
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

        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/?.*";
        if (!req.getPathInfo().matches(pattern)){
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"Invalid end point");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);

        try (Connection connection = pool.get().getConnection()) {
            Jsonb jsonb = JsonbBuilder.create();
            TaskListDTO taskList = jsonb.fromJson(req.getReader(), TaskListDTO.class);
            if (taskList.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            }
            PreparedStatement stm = connection.prepareStatement("INSERT INTO task_list (name,user_id) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, taskList.getTitle());
            stm.setString(2, userId);
            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to save the task list");
            }
            ResultSet rst = stm.getGeneratedKeys();
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
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/?";
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
            stm.setInt(1, Integer.parseInt(taskListId));
            stm.setString(2, userId);
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
        TaskListDTO taskList = getTaskList(req);
        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task_list WHERE id=?");
            stm.setInt(1,taskList.getId());
            if (stm.executeUpdate()!=1){
                throw new SQLException("Failed to delete the task");
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ResponseStatusException(500, e.getMessage(),e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (req.getContentType()==null || !req.getContentType().equals("application/json")){
            throw new ResponseStatusException(405,"Invalid content type or content type is empty");
        }
        TaskListDTO oldTaskList = getTaskList(req);
        Jsonb jsonb = JsonbBuilder.create();
        TaskListDTO newTaskList;
        try{
            newTaskList = jsonb.fromJson(req.getReader(), TaskListDTO.class);
        }catch (JsonbException e){
            throw new ResponseStatusException(400, "Invalid JSON", e);
        }

        if (newTaskList.getTitle() == null || newTaskList.getTitle().trim().isEmpty()){
            throw new ResponseStatusException(400,"Invalid title or title is Empty");
        }
        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.prepareStatement("UPDATE task_list SET name=? WHERE id=?");
            stm.setString(1,newTaskList.getTitle());
            stm.setInt(2,oldTaskList.getId());
            if (stm.executeUpdate()!=1){
                throw new SQLException("Failed to Update the tasK list");
            }
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ResponseStatusException(500,e.getMessage(),e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/?";
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        if (matcher.find()){
            String userId = matcher.group(1);

            try (Connection connection = pool.get().getConnection()) {
                PreparedStatement stm = connection.
                        prepareStatement("SELECT * FROM task_list t WHERE t.user_id=?");
                stm.setString(1,userId);
                ResultSet rst = stm.executeQuery();

                ArrayList<TaskListDTO> taskLists = new ArrayList<>();
                while (rst.next()){
                    int id = rst.getInt("id");
                    String title = rst.getString("name");
                    taskLists.add(new TaskListDTO(id, title, userId));
                }

                resp.setContentType("application/json");
                Jsonb jsonb = JsonbBuilder.create();
                JsonParser parser = Json.createParser(new StringReader(jsonb.toJson(taskLists)));
                parser.next();
                JsonObject json = Json.createObjectBuilder().add("items", parser.getArray()).build();
                resp.getWriter().println(json);
            } catch (SQLException e) {
                throw new ResponseStatusException(500, e.getMessage(), e);
            }
        }else {
            TaskListDTO taskList = getTaskList(req);
            Jsonb jsonb = JsonbBuilder.create();
            resp.setContentType("application/json");
            jsonb.toJson(taskList,resp.getWriter());
        }
    }
}
