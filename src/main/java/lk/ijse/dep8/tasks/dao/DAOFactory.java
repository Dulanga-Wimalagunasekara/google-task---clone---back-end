package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dao.custome.impl.QueryDAOimpl;
import lk.ijse.dep8.tasks.dao.custome.impl.TaskDAOImpl;
import lk.ijse.dep8.tasks.dao.custome.impl.TaskListDAOImpl;
import lk.ijse.dep8.tasks.dao.custome.impl.UserDAOImpl;

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
    public static DAOFactory getInstance(){
        return (daoFactory==null) ? (daoFactory=new DAOFactory()):daoFactory;
    }

    public enum DAOTypes{
        USER,TASK_LIST,TASK,QUERY
    }
}
