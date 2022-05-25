package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dao.impl.*;
import lk.ijse.dep8.tasks.dto.UserDTO;

import java.sql.Connection;

public class DAOFactory {
    private static DAOFactory daoFactory;

    private DAOFactory(){

    }

    public <T extends SuperDAO> T getDAO(Connection connection, DAOTypes daoType){
        switch (daoType){
            case USER:
                return (T) new UserDAOImpl(connection);
            case TASK_LIST:
                return (T) new TaskListDAOImpl(connection);
            case TASK:
                return (T) new TaskDAOImpl(connection);
            case QUERY:
                return (T) new QueryDAOimpl(connection);
            default:
                return null;
        }
    }
//    public static DAOFactory getInstance(){
//        return (daoFactory==null) ? (daoFactory=new DAOFactory()):daoFactory;
//    }
//
//    public UserDAO getUserDAO(Connection connection){
//        return new UserDAOImpl(connection);
//    }
//
//    public TaskListDAO getTaskListDAO(Connection connection){
//        return new TaskListDAOImpl(connection);
//    }
//
//    public TaskDAO getTaskDAO(Connection connection){
//        return new TaskDAOImpl(connection);
//    }
//
//    public QueryDAO getQueryDAO(Connection connection){
//        return new QueryDAOimpl(connection);
//    }

    public enum DAOTypes{
        USER,TASK_LIST,TASK,QUERY
    }
}
