package lk.ijse.dep8.tasks.dto;

import java.util.List;

public class TaskListsDTO {
    private List<TaskListsDTO> items;

    public TaskListsDTO(List<TaskListsDTO> items) {
        this.items = items;
    }

    public TaskListsDTO() {
    }

    public List<TaskListsDTO> getItems() {
        return items;
    }

    public void setItems(List<TaskListsDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "TaskListsDTO{" +
                "items=" + items +
                '}';
    }
}
