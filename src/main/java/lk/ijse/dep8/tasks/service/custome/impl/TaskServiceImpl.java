package lk.ijse.dep8.tasks.service.custome.impl;

import lk.ijse.dep8.tasks.dao.DAOFactory;
import lk.ijse.dep8.tasks.dao.SuperDAO;
import lk.ijse.dep8.tasks.dao.custome.QueryDAO;
import lk.ijse.dep8.tasks.dao.custome.TaskDAO;
import lk.ijse.dep8.tasks.dao.custome.TaskListDAO;
import lk.ijse.dep8.tasks.dao.custome.UserDAO;
import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.SuperEntity;
import lk.ijse.dep8.tasks.entity.Task;
import lk.ijse.dep8.tasks.entity.User;
import lk.ijse.dep8.tasks.service.custome.TaskService;
import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;
import lk.ijse.dep8.tasks.util.EntityDTOMapper;
import lk.ijse.dep8.tasks.util.ExecutionContext;
import lk.ijse.dep8.tasks.util.JNDIConnectionPool;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.servlet.http.Part;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class TaskServiceImpl implements TaskService {
    private DataSource dataSource;

    public TaskServiceImpl() {
        dataSource = JNDIConnectionPool.getInstance().getPool();
    }

    @Override
    public boolean existsTask(String emailOrId) {
        return false;
    }

    @Override
    public TaskDTO saveTask(int taskListId, String userId, TaskDTO task) {
        try {
            Connection connection = dataSource.getConnection();
            TaskListDAO taskListDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            boolean isExists = taskListDAO.existTaskListByIdAndUserId(taskListId, userId);
            if (isExists){
                throw new FailedExecutionException("Invalid user id or Task list id");
            }
            if (task == null || task.getTitle().trim().isEmpty()) {
                throw new FailedExecutionException("Invalid title or title is empty");
            }
            connection.setAutoCommit(false);
            pushDown(connection, 0, taskListId);
            TaskDAO taskDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
            Task taskEntity = EntityDTOMapper.getTask(task);
            Task save = taskDAO.save(taskEntity);
            connection.commit();
            TaskDTO taskDTO = EntityDTOMapper.getTaskDTO(save);
            return taskDTO;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<List<TaskDTO>> getTask(int taskListId, String userId) {
        try (Connection connection = dataSource.getConnection()) {
            TaskListDAO taskListDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            if (!taskListDAOImpl.existTaskListByIdAndUserId(taskListId, userId)) {
                throw new FailedExecutionException("TaskList is not exists!");
            }
            TaskDAO taskDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
            Optional<List<Task>> list = taskDAOImpl.findByTaskListId(taskListId);
            if (list.isPresent()) {
                List<TaskDTO> taskDTOs = new ArrayList<>();
                List<Task> tasks = list.get();
                for (Task task : tasks) {
                    TaskDTO taskDTO = EntityDTOMapper.getTaskDTO(task);
                    taskDTOs.add(taskDTO);
                }
                return Optional.of(taskDTOs);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new FailedExecutionException("Failed to get the Task", e);
        }
    }

    @Override
    public void deleteTask(TaskDTO task) {
        Connection connection=null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            pushUp(connection,task.getPosition(),task.getTaskListId());
            TaskDAO taskDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
            taskDAO.deleteById(task.getId());
            connection.commit();
        } catch (SQLException e) {
            throw new FailedExecutionException("Failed to delete the task",e);
        }finally {
            Connection tempConnection=connection;
            ExecutionContext.execute(()->{
                tempConnection.setAutoCommit(true);
                tempConnection.rollback();
            });
        }
    }

    @Override
    public void updateTask(String userId, int taskListId, int taskId, TaskDTO newTask) {
        Connection connection=null;
        try {
            connection = dataSource.getConnection();
            QueryDAO queryDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.QUERY);
            Task oldTask = (Task) queryDAO.getTask(taskId, taskListId, userId);

            if (newTask.getTitle() == null || newTask.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            } else if (newTask.getPosition() == null || newTask.getPosition() < 0) {
                throw new ResponseStatusException(400, "Invalid position or position value is empty");
            }

            connection.setAutoCommit(false);
            if (oldTask.getPosition() != (newTask.getPosition())) {
                pushUp(connection, oldTask.getPosition(), oldTask.getTaskListId());
                pushDown(connection, newTask.getPosition(), oldTask.getTaskListId());
            }
            TaskDAO taskDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
            Task task = EntityDTOMapper.getTask(newTask);
            taskDAO.save(task);
            connection.commit();

        } catch (SQLException e) {
            throw new FailedExecutionException("Failed to get the TaskList");
        } finally {
            Connection tempConnection=connection;
            ExecutionContext.execute(()->{
                    if (tempConnection!=null && !tempConnection.getAutoCommit()) {
                        tempConnection.rollback();
                        tempConnection.setAutoCommit(true);
                    }
                    tempConnection.close();
                });
        }
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

    @Override
    public void pushUp(Connection connection, int pos, int taskListId) throws SQLException {

    }

    @Override
    public void pushDown(Connection connection, int pos, int taskListId) throws SQLException {

    }
}

