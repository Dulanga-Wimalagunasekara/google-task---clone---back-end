package lk.ijse.dep8.tasks.dao.custome.impl;

import lk.ijse.dep8.tasks.dao.custome.QueryDAO;

import java.sql.Connection;

public class QueryDAOimpl implements QueryDAO {

    private Connection connection;
    public QueryDAOimpl(Connection connection) {
        this.connection = connection;
    }

}
