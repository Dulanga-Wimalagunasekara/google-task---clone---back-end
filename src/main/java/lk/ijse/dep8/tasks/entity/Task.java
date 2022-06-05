package lk.ijse.dep8.tasks.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Task implements SuperEntity{
    private int id;
    private String title;
    private String details;
    private int position;
    private Status status;
    private int taskListId;

    public enum Status{
        completed,needsAction
    }
}
