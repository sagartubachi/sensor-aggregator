package com.example.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/*
This class is used when the login API is called to generate a JWT token. The AuthenticationManager calls the
implementation of UserDetailsService to check if the username and password sent in the login API is valid.
If yes, then the token is returned which can be used to call secured APIs like /api/query in this case.
The password is encrypted using BCrypt
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Demo users - in production, load from database
        // Password is "password" encoded with BCrypt
        if ("admin".equals(username)) {
            return new User("admin", "$2a$10$b3UEf2WaAvJdWdUQKsFGL.0u9aYCPDyFB2gFX2lkTOBPKLaBfV7b6",
                           new ArrayList<>());
        } else if ("user".equals(username)) {
            return new User("user", "$2a$10$0mKjS9q2gtG3NFkUfM5rk.3V1eRmWeiGFlcQbthq/Z6/dvXtKMSX2",
                           new ArrayList<>());
        }
        
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
