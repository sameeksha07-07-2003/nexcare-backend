package com.nexcare.backend.security;

import com.nexcare.backend.entity.User;
import com.nexcare.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username){
        Optional<User> isUser= userRepository.findByEmail(username);
        if(isUser.isEmpty()){
            throw new UsernameNotFoundException("User not found please enter correct details.");
        }
        User user = isUser.get();
        return new CustomUserDetails(user);
    }
}
