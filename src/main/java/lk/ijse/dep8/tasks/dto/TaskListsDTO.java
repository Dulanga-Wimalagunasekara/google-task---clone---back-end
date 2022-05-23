package lk.ijse.dep8.tasks.dto;

import java.io.Serializable;
import java.util.List;

public class TaskListsDTO implements Serializable{
    private List<TaskListDTO> items;

    public TaskListsDTO(List<TaskListDTO> items) {
        this.items = items;
    }

    public TaskListsDTO() {
    }

    public List<TaskListDTO> getItems() {
        return items;
    }

    public void setItems(List<TaskListDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "TaskListsDTO{" +
                "items=" + items +
                '}';
    }
}
