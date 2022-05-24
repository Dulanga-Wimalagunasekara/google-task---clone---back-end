package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public static void saveUser(Connection con,UserDTO user)throws SQLException{

    }

    public static void updateUser(Connection con,UserDTO user)throws SQLException{

    }

    public static void deleteUser(Connection con,String id)throws SQLException{

    }
    public static UserDTO getUser(Connection con,String id)throws SQLException{
        return null;
    }

    public static boolean existsUser(Connection con,String email) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * FROM user WHERE email=?");
        statement.setString(1, email);
        return (statement.executeQuery().next());

    }



}
