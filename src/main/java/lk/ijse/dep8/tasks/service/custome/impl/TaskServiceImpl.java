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
import lk.ijse.dep8.tasks.entity.TaskList;
import lk.ijse.dep8.tasks.entity.User;
import lk.ijse.dep8.tasks.service.custome.TaskService;
import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;
import lk.ijse.dep8.tasks.util.EntityDTOMapper;
import lk.ijse.dep8.tasks.util.ExecutionContext;
import lk.ijse.dep8.tasks.util.JNDIConnectionPool;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.servlet.http.Part;
import javax.sql.DataSource;
import javax.swing.text.html.Option;
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
            if (!isExists){
                throw new FailedExecutionException("Invalid user id or Task list id");
            }
            if (task == null || task.getTitle().trim().isEmpty()) {
                throw new FailedExecutionException("Invalid title or title is empty");
            }
            connection.setAutoCommit(false);
            pushDown(connection, 0, taskListId);
            TaskDAO taskDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
            task.setTaskListId(taskListId);
            Task taskEntity = EntityDTOMapper.getTask(task);
            Task save = taskDAO.save(taskEntity);
            connection.commit();
            TaskDTO taskDTO = EntityDTOMapper.getTaskDTO(save);
            return taskDTO;
        } catch (Throwable e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<List<TaskDTO>> getAllTasks(int taskListId, String userId) {
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
    public Optional<TaskDTO> getSpecificTask(int taskListId, String userId,int taskId) {
        Connection connection=null;
        try {
            connection = dataSource.getConnection();
            QueryDAO queryDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.QUERY);
            Task task = (Task) queryDAO.getTask(taskId, taskListId, userId);
            return Optional.of(EntityDTOMapper.getTaskDTO(task));
        } catch (SQLException e) {
            throw new FailedExecutionException("Failed to delete the task",e);
        }
    }

    @Override
    public void deleteTask(String userId,int taskListId,int taskId) {
        Connection connection=null;
        try {
            connection = dataSource.getConnection();
            QueryDAO queryDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.QUERY);
            Task task = (Task) queryDAO.getTask(taskId, taskListId, userId);
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
    public TaskListDTO saveTaskList(TaskListDTO taskList) {
        try {
            Connection connection = dataSource.getConnection();
            TaskListDAO taskListDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            TaskList savedList = taskListDAO.save(EntityDTOMapper.getTaskList(taskList));
            return EntityDTOMapper.getTaskListDTO(savedList);
        } catch (SQLException e) {
            throw new FailedExecutionException("Unable to save the taskList");
        }
    }

    @Override
    public Optional<TaskListDTO> getTaskList(int taskListId, String userId) {
        try {
            Connection connection = dataSource.getConnection();
            TaskListDAO taskListDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            Optional<TaskList> taskListEntity = taskListDAO.getTaskListByIdAndUserId(taskListId, userId);
            if (taskListEntity.isPresent()){
                return Optional.of(EntityDTOMapper.getTaskListDTO(taskListEntity.get()));
            }else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<TaskListDTO>> getTaskListsByUserId(String userId){
        try {
            List<TaskListDTO> taskListDTOS = new ArrayList<>();
            Connection connection = dataSource.getConnection();
            TaskListDAO taskListDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            Optional<List<TaskList>> byUserId = taskListDAO.findByUserId(userId);
            for (TaskList entity:byUserId.get()) {
                taskListDTOS.add(EntityDTOMapper.getTaskListDTO(entity));
            }
            return Optional.of(taskListDTOS);
        } catch (SQLException e) {
            throw new FailedExecutionException("Unable to get the task lists");
        }
    }

    @Override
    public void deleteTaskList(TaskListDTO taskList) {
        try {
            Connection connection = dataSource.getConnection();
            TaskListDAO taskListDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            taskListDAO.deleteById(taskList.getId());
        } catch (SQLException e) {
            throw new FailedExecutionException("Unable to delete the tasKList");
        }
    }

    @Override
    public void updateTaskList(TaskListDTO newTaskList,TaskListDTO oldTaskList) {
        try {
            Connection connection = dataSource.getConnection();
            if (newTaskList.getTitle() == null || newTaskList.getTitle().trim().isEmpty()){
                throw new FailedExecutionException("Invalid title or title is Empty");
            }
            if (newTaskList.getId()==null || newTaskList.getUserID()==null){
                newTaskList.setUserID(oldTaskList.getUserID());
                newTaskList.setId(oldTaskList.getId());
            }
            TaskListDAO taskListDAO = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK_LIST);
            taskListDAO.save(EntityDTOMapper.getTaskList(newTaskList));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void pushUp(Connection connection, int pos, int taskListId) throws SQLException {
        TaskDAO dao = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
        dao.pushUp(connection,pos,taskListId);
    }

    @Override
    public void pushDown(Connection connection, int pos, int taskListId) throws SQLException {
        TaskDAO dao = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.TASK);
        dao.pushDown(connection,pos,taskListId);
    }
}

