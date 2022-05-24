package lk.ijse.dep8.tasks.entity;

import java.io.Serializable;

public class TaskList implements Serializable {
    private int id;
    private String name;
    private String userId;

    public TaskList(int id, String name, String userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    public TaskList() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "TaskList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
