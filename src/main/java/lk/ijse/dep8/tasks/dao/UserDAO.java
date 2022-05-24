package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {
    public static UserDTO saveUser(Connection con,UserDTO user)throws SQLException{
        PreparedStatement stm = con.prepareStatement("INSERT INTO user (id,email, password, full_name,profile_pic) VALUES (?,?,?,?,?)");
        String id = UUID.randomUUID().toString();
        stm.setString(1, user.getId());
        stm.setString(2, user.getEmail());
        stm.setString(3, user.getPassword());
        stm.setString(4, user.getName());
        stm.setString(5, user.getPicture());
        if (stm.executeUpdate() !=1){
            throw new SQLException("Failed to save the user");
        }
        return user;
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
