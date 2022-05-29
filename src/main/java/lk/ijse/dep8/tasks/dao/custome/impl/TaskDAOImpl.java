package lk.ijse.dep8.tasks.dao.custome.impl;

import lk.ijse.dep8.tasks.dao.custome.TaskDAO;
import lk.ijse.dep8.tasks.dao.exception.DataAccessException;
import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.entity.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDAOImpl implements TaskDAO {
    private Connection connection;

    public TaskDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Task save(Task task) {
        try {
            if (!existsById(task.getId())) {
                PreparedStatement stm = connection.prepareStatement("INSERT INTO task (title, details, position, status, task_list_id) VALUES (?,?,?,?,?)");
                stm.setString(1, task.getTitle());
                stm.setString(2, task.getDetails());
                stm.setInt(3, task.getPosition());
                stm.setString(4, task.getStatus().toString());
                stm.setInt(5, task.getTaskListId());
                if (stm.executeUpdate() != 1) {
                    throw new SQLException("Failed to save the Task");
                }
            } else {
                PreparedStatement stm = connection.prepareStatement("UPDATE task SET title=?, details =?, position=?, status=?, task_list_id=? WHERE id=?");
                stm.setString(1, task.getTitle());
                stm.setString(2, task.getDetails());
                stm.setInt(3, task.getPosition());
                stm.setString(4, task.getStatus().toString());
                stm.setInt(5, task.getTaskListId());
                stm.setInt(6, task.getId());
                if (stm.executeUpdate() != 1) {
                    throw new SQLException("Failed to update the Task");
                }
            }
            return task;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Integer taskId) {
        try {
            if (!existsById(taskId)) {
                throw new DataAccessException("No Task Found!");
            }
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task WHERE id=?");
            stm.setInt(1, taskId);
            if (stm.executeUpdate() != 1) {
                throw new DataAccessException("Failed to delete the Task");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete the Task");
        }

    }

    @Override
    public Optional<Task> findById(Integer taskId) {
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task WHERE id=?");
            stm.setInt(1, taskId);
            ResultSet rst = stm.executeQuery();
            if (rst.next()) {
                return Optional.of(new Task(rst.getInt("id"),
                        rst.getString("title"),
                        rst.getString("details"),
                        rst.getInt("position"),
                        Task.Status.valueOf(rst.getString("status")),
                        rst.getInt("task_list_id")));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsById(Integer taskId) {
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task WHERE id=?");
            stm.setInt(1, taskId);
            return stm.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> findAll() {
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM task");
            List<Task> tasks = new ArrayList<>();
            while (rst.next()) {
                tasks.add(new Task(rst.getInt("id"),
                        rst.getString("title"),
                        rst.getString("details"),
                        rst.getInt("position"),
                        Task.Status.valueOf(rst.getString("status")),
                        rst.getInt("task_list_id")));
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count() {
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT COUNT(*) AS count FROM task");
            if (rst.next()) {
                return rst.getLong("count");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<Task>> findByTaskListId(Integer taskListId) {
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task WHERE task.task_list_id = ? ORDER BY position");
            stm.setInt(1, taskListId);
            ResultSet rst = stm.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rst.next()) {
                int id = rst.getInt("id");
                String title = rst.getString("title");
                String details = rst.getString("details");
                int position = rst.getInt("position");
                String status = rst.getString("status");
                tasks.add(new Task(id, title, details, position, Task.Status.valueOf(status), taskListId));
            }
            if (!tasks.isEmpty()){
                return Optional.of(tasks);
            }else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushUp(Connection connection, int pos, int taskListId) throws SQLException {
        PreparedStatement pstm = connection.
                prepareStatement("UPDATE task t SET position = position - 1 WHERE t.position >= ? AND t.task_list_id = ? ORDER BY t.position");
        pstm.setInt(1, pos);
        pstm.setInt(2, taskListId);
        pstm.executeUpdate();
    }

    @Override
    public void pushDown(Connection connection, int pos, int taskListId) throws SQLException {
        PreparedStatement pstm = connection.
                prepareStatement("UPDATE task t SET position = position - 1 WHERE t.position >= ? AND t.task_list_id = ? ORDER BY t.position");
        pstm.setInt(1, pos);
        pstm.setInt(2, taskListId);
        pstm.executeUpdate();
    }
}
