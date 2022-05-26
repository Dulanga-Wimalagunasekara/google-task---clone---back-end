package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.service.custome.impl.TaskServiceImpl;
import lk.ijse.dep8.tasks.service.custome.impl.UserServiceImpl;

import java.sql.Connection;

public class ServiceFactory {
    private static ServiceFactory serviceFactory;

    private ServiceFactory(){
    }

    public static ServiceFactory getInstance(){
        return (serviceFactory==null)? (serviceFactory=new ServiceFactory()) :serviceFactory;
    }

    public <T extends SuperService> T getService(Connection connection, ServiceTypes serviceType){
        switch (serviceType){
            case USER:
                return (T) new UserServiceImpl();
            case TASK:
                return (T) new TaskServiceImpl();
            default:
                return null;
        }
    }

    public enum ServiceTypes{
        USER, TASK
    }
}

