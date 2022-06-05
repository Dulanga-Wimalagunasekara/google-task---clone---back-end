package lk.ijse.dep8.tasks.api;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.service.ServiceFactory;
import lk.ijse.dep8.tasks.service.SuperService;
import lk.ijse.dep8.tasks.service.custome.TaskService;
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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "TaskListServlet")
public class TaskListServlet extends HttpServlet2 {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid Content Type Or Content Type is Empty");
        }
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/?.*";
        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid end point");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        try {
            Jsonb jsonb = JsonbBuilder.create();
            TaskListDTO taskList = jsonb.fromJson(req.getReader(), TaskListDTO.class);
            taskList.setUserID(userId);
            if (taskList.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            }
            TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
            TaskListDTO list = service.saveTaskList(taskList);
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(list, resp.getWriter());
        } catch (Throwable e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }

    }

    private TaskListDTO getTaskList(HttpServletRequest req) {
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/?";
        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    String.format("Invalid end point for %s request", req.getMethod()));
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        String taskListId = matcher.group(2);

        try {
            TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
            Optional<TaskListDTO> taskList = service.getTaskList(Integer.parseInt(taskListId), userId);
            if (taskList.isPresent()) {
                return taskList.get();
            } else {
                throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND, "Invalid user id or Task list id");
            }
        } catch (Throwable e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can not get the TaskList", e);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        TaskListDTO taskList = getTaskList(req);
        try {
            TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
            service.deleteTaskList(taskList);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Throwable e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getContentType() == null || !req.getContentType().equals("application/json")) {
            throw new ResponseStatusException(405, "Invalid content type or content type is empty");
        }
        Jsonb jsonb = JsonbBuilder.create();
        TaskListDTO newTaskList;
        try {
            newTaskList = jsonb.fromJson(req.getReader(), TaskListDTO.class);
        } catch (JsonbException e) {
            throw new ResponseStatusException(400, "Invalid JSON", e);
        }
        TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
        service.updateTaskList(newTaskList);
        try {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (FailedExecutionException e) {
            throw new ResponseStatusException(400, e.getMessage(), e);
        } catch (Throwable e) {
            throw new ResponseStatusException(500, "Internal Server Error", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/?";
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        if (matcher.find()) {
            String userId = matcher.group(1);
            try {
                TaskService service = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
                Optional<List<TaskListDTO>> taskLists = service.getTaskListsByUserId(userId);
                resp.setContentType("application/json");
                Jsonb jsonb = JsonbBuilder.create();
                JsonParser parser = Json.createParser(new StringReader(jsonb.toJson(taskLists.get())));
                parser.next();
                JsonObject json = Json.createObjectBuilder().add("items", parser.getArray()).build();
                resp.getWriter().println(json);
            } catch (Throwable e) {
                throw new ResponseStatusException(500, "Internal Server Error", e);
            }
        } else {
            TaskListDTO taskList = getTaskList(req);
            Jsonb jsonb = JsonbBuilder.create();
            resp.setContentType("application/json");
            jsonb.toJson(taskList, resp.getWriter());
        }
    }
}
