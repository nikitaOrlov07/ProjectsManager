package com.example.Security;


import com.example.Model.Security.UserEntity;
import com.example.Repository.Security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // configure "loadByUsername"
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {//Он загружает пользователя по имени пользователя.
        // Если пользователь найден, метод возвращает объект UserDetails, который представляет пользователя в контексте Spring Security.
        // Если пользователь не найден, метод выбрасывает исключение UsernameNotFoundException.
        UserEntity userEntity=userRepository.findFirstByUsername(username);// если нету "First" --> вернет больше одного пользователя
        if( userEntity != null)
        {
            User  authUser= new User(
                    userEntity.getUsername(),
                    userEntity.getPassword() ,
                    userEntity.getRoles().stream().map((role) -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList())
            );
            return authUser; // we only can use User entity with 3 arguments constructor - username, password,roles
        }
        else {
            throw new UsernameNotFoundException("Invalid username or password");
        }

    }


}
