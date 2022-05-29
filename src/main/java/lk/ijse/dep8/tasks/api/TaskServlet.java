package lk.ijse.dep8.tasks.api;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import lk.ijse.dep8.tasks.dao.DAOFactory;
import lk.ijse.dep8.tasks.dao.SuperDAO;
import lk.ijse.dep8.tasks.dao.custome.QueryDAO;
import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.service.ServiceFactory;
import lk.ijse.dep8.tasks.service.SuperService;
import lk.ijse.dep8.tasks.service.custome.TaskService;
import lk.ijse.dep8.tasks.service.custome.UserService;
import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "TaskServlet")
public class TaskServlet extends HttpServlet2 {
    private AtomicReference<DataSource> pool;
    private final Logger logger = Logger.getLogger(TaskListServlet.class.getName());

    @PostConstruct
    public void init() {
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
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/(\\d)+/tasks/?";

        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid end point");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        int taskListId = Integer.parseInt(matcher.group(2));
        try {
            Jsonb jsonb = JsonbBuilder.create();
            TaskDTO task = jsonb.fromJson(req.getReader(), TaskDTO.class);
            task.setPosition(0);
            task.setStatusAsEnum(TaskDTO.Status.NEEDS_ACTION);

            TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
            TaskDTO savedTask = service.saveTask(taskListId, userId, task);
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(savedTask, resp.getWriter());
        } catch (Throwable e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        try {
            TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
            service.deleteTask(userId,taskListId,taskId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Throwable e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pattern = "^/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/tasks/?$";
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        if (matcher.find()) {
            String userId = matcher.group(1);
            int taskListId = Integer.parseInt(matcher.group(2));
            try {
                TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
                Optional<List<TaskDTO>> tasks = service.getAllTasks(taskListId, userId);
                if (tasks.isPresent()) {
                    resp.setContentType("application/json");
                    Jsonb jsonb = JsonbBuilder.create();
                    String jsonArray = jsonb.toJson(tasks.get());
                    JsonParser parser = Json.createParser(new StringReader(jsonArray));
                    parser.next();
                    JsonArray tasksArray = parser.getArray();
                    JsonObject json = Json.createObjectBuilder().
                            add("resource", Json.createObjectBuilder().add("items", tasksArray)).build();
                    resp.getWriter().println(json);
                } else {
                    resp.getWriter().println("Empty");
                }
            } catch (Throwable e) {
                throw new ResponseStatusException(500, e.getMessage(), e);
            }

        } else {
            String pattern1 = "^/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/tasks/(\\d+)/?$";
            if (!req.getPathInfo().matches(pattern1)) {
                throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        String.format("Invalid end point for %s request", req.getMethod()));
            }
            Matcher matcher1 = Pattern.compile(pattern1).matcher(req.getPathInfo());
            matcher1.find();
            String userId = matcher1.group(1);
            int taskListId = Integer.parseInt(matcher1.group(2));
            int taskId = Integer.parseInt(matcher1.group(3));
            TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
            Optional<TaskDTO> taskDTO = service.getSpecificTask(taskListId, userId, taskId);
            resp.setContentType("application/json");
            Jsonb jsonb = JsonbBuilder.create();
            if (taskDTO.isPresent()){
                jsonb.toJson(taskDTO.get(), resp.getWriter());
            }else {
                resp.getWriter().println("Empty");
            }
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(415, "Invalid content type or content type is empty");
        }

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

        TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);

        try {
            Jsonb jsonb = JsonbBuilder.create();
            TaskDTO newTask = jsonb.fromJson(req.getReader(), TaskDTO.class);
            service.updateTask(userId, taskListId, taskId, newTask);
            if (newTask.getTitle() == null || newTask.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            } else if (newTask.getPosition() == null || newTask.getPosition() < 0) {
                throw new ResponseStatusException(400, "Invalid position or position value is empty");
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (JsonbException e) {
            throw new ResponseStatusException(400, "Invalid JSON");
        } catch (Throwable e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }
    }
}
