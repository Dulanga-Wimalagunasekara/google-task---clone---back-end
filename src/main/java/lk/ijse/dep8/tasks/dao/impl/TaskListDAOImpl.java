package lk.ijse.dep8.tasks.dao.impl;

import lk.ijse.dep8.tasks.dao.TaskListDAO;
import lk.ijse.dep8.tasks.dao.exception.DataAccessException;
import lk.ijse.dep8.tasks.entity.TaskList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskListDAOImpl implements TaskListDAO {

    private Connection connection;

    public TaskListDAOImpl(Connection connection){
        this.connection=connection;
    }

    public TaskList saveTaskList(TaskList list){
        try {
            if (!existsTaskListById(list.getId())){
                PreparedStatement stm = connection.prepareStatement("INSERT INTO task_list (name, user_id) VALUES (?,?)");
                stm.setString(1,list.getName());
                stm.setString(2,list.getUserId());
                if (stm.executeUpdate()!=1){
                    throw new SQLException("Failed to save the user");
                }
            }else {
                PreparedStatement stm = connection.prepareStatement("UPDATE task_list SET name=?, user_id=? WHERE id=?");
                stm.setString(1,list.getName());
                stm.setString(2,list.getUserId());
                stm.setInt(3,list.getId());
                if (stm.executeUpdate()!=1){
                    throw new SQLException("Failed to update the user");
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTaskListById(TaskList list){
        try {
            if (!existsTaskListById(list.getId())){
                throw new DataAccessException("No user Found!");
            }
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task_list WHERE id=?");
            stm.setInt(1,list.getId());
            if (stm.executeUpdate()!=1){
                throw new SQLException("Failed to delete the user");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Optional<TaskList> findTaskListTById(int listId){
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task_list WHERE id=?");
            stm.setInt(1,listId);
            ResultSet rst = stm.executeQuery();
            if (rst.next()){
                return Optional.of(new TaskList(rst.getInt("id"),
                        rst.getString("name"),
                        rst.getString("user_id")));
            }else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsTaskListById(int listId){
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT id FROM task_list WHERE id=?");
            stm.setInt(1,listId);
            return stm.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TaskList> findAllTaskLists(){
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM task_list");
            List<TaskList> tasks = new ArrayList<>();
            while (rst.next()){
                tasks.add(new TaskList(rst.getInt("id"),
                        rst.getString("name"),
                        rst.getString("user_id")));
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long countTaskLists(){
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT COUNT(*) AS count FROM task_list");
            if (rst.next()){
                return rst.getLong("count");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
