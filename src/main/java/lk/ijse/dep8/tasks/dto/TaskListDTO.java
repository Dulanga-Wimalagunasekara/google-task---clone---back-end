package lk.ijse.dep8.tasks.dto;

import java.io.Serializable;

public class TaskListDTO implements Serializable {
    private Integer id;
    private String title;

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
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
