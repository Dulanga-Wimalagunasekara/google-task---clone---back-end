package lk.ijse.dep8.tasks.service.custome;

import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.Task;
import lk.ijse.dep8.tasks.service.SuperService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TaskService extends SuperService {
    boolean existsTask(String emailOrId);

    TaskDTO saveTask(int taskListId, String userId, TaskDTO task);

    Optional<List<TaskDTO>> getAllTasks(int taskListId, String userId);
    Optional<TaskDTO> getSpecificTask(int taskListId, String userId,int taskId);

    void deleteTask(String userId,int taskListId,int taskId);

    void updateTask(String userId, int taskListId, int taskId,TaskDTO newTask);
    /*==================================================================================*/
    UserDTO saveTaskList(Part picture, String appLocation, UserDTO user);

    Optional<TaskListDTO> getTaskList(int taskListId, String userId);

    void deleteTaskList(TaskListDTO taskList);

    void updateTaskList(TaskListDTO newTaskList);
    void pushUp(Connection connection, int pos, int taskListId) throws SQLException;

    void pushDown(Connection connection, int pos, int taskListId) throws SQLException;
}
