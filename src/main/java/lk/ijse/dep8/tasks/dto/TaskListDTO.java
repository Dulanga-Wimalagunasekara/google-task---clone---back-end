package lk.ijse.dep8.tasks.dto;

import jakarta.json.bind.annotation.JsonbTransient;

import java.io.Serializable;

public class TaskListDTO implements Serializable{
    private Integer id;
    private String title;
    @JsonbTransient
    private String userID;

    public TaskListDTO(Integer id, String title, String userID){
        this.id = id;
        this.title = title;
        this.userID = userID;
    }

    public TaskListDTO(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public TaskListDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "TaskListDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
