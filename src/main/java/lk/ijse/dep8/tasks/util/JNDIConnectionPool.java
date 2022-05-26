package lk.ijse.dep8.tasks.util;

import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class JNDIConnectionPool {
    private static JNDIConnectionPool pool;

    private JNDIConnectionPool(){

    }

    public static JNDIConnectionPool getInstance(){
        return (pool!=null)? pool:(pool=new JNDIConnectionPool());
    }

    public DataSource getPool(){
        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/pool");
            return dataSource;
        } catch (NamingException e) {
            throw new FailedExecutionException("Failed to initialize the pool");
        }
    }


}
