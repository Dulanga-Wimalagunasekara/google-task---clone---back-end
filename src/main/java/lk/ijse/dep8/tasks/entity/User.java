package lk.ijse.dep8.tasks.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements SuperEntity {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String profilePic;
}
