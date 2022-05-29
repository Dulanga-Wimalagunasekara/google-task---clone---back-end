package lk.ijse.dep8.tasks.service.custome.impl;

import lk.ijse.dep8.tasks.dao.DAOFactory;
import lk.ijse.dep8.tasks.dao.SuperDAO;
import lk.ijse.dep8.tasks.dao.custome.TaskDAO;
import lk.ijse.dep8.tasks.dao.custome.TaskListDAO;
import lk.ijse.dep8.tasks.dao.custome.UserDAO;
import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.Task;
import lk.ijse.dep8.tasks.service.custome.TaskService;
import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;
import lk.ijse.dep8.tasks.util.EntityDTOMapper;
import lk.ijse.dep8.tasks.util.JNDIConnectionPool;

import javax.servlet.http.Part;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskServiceImpl implements TaskService {
    private DataSource dataSource;

    public TaskServiceImpl() {
        dataSource= JNDIConnectionPool.getInstance().getPool();
    }

    @Override
    public boolean existsTask(String emailOrId) {
        return false;
    }

    @Override
    public UserDTO saveTask(Part picture, String appLocation, UserDTO user) {
        return null;
    }

    @Override
    public Optional<List<TaskDTO>> getTask(int taskListId, String userId) {
        try (Connection connection = dataSource.getConnection()) {
            TaskListDAO taskListDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            if (!taskListDAOImpl.existTaskListByIdAndUserId(taskListId,userId)){
                throw new FailedExecutionException("TaskList is not exists!");
            }
            TaskDAO taskDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
            Optional<List<Task>> list = taskDAOImpl.findByTaskListId(taskListId);
            if (list.isPresent()){
                List<TaskDTO> taskDTOs = new ArrayList<>();
                List<Task> tasks = list.get();
                for (Task task:tasks) {
                    TaskDTO taskDTO = EntityDTOMapper.getTaskDTO(task);
                    taskDTOs.add(taskDTO);
                }
                return Optional.of(taskDTOs);
            }else {
                return Optional.empty();
            }
        }catch (SQLException e){
            throw new FailedExecutionException("Failed to get the Task", e);
        }
    }

    @Override
    public void deleteTask(String id, String appLocation) {

    }

    @Override
    public void updateTask(UserDTO user, Part picture, String appLocation) {

    }


    /*================================================================================*/

    @Override
    public UserDTO saveTaskList(Part picture, String appLocation, UserDTO user) {
        return null;
    }

    @Override
    public UserDTO getTaskList(int taskListId, String userId) {
        return null;
    }

    @Override
    public void deleteTaskList(String id, String appLocation) {

    }

    @Override
    public void updateTaskList(UserDTO user, Part picture, String appLocation) {

    }
}

