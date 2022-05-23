package lk.ijse.dep8.tasks.security;

import lk.ijse.dep8.tasks.dto.UserDTO;

public class SecurityContextHolder{

    private static volatile ThreadLocal<UserDTO> principal=new ThreadLocal<>();
    public static void setPrincipal(UserDTO dto){
        principal.set(dto);
    }
    public static UserDTO getPrincipal(){
        return principal.get();
    }


}
