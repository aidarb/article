package com.kae.article.service;

import com.kae.article.model.Role;
import com.kae.article.model.User;
import com.kae.article.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByLoginAndPassword(String login, String password) {
        User user = userRepository.findByUsername(login);
        if (user != null && passwordEncoder.matches(password, user.getPassword()))
            return user;
        else
            return null;
    }

    public Boolean addUser(User user)  {
        User userFormDb = userRepository.findByUsername(user.getUsername());

        if (userFormDb != null) {
            return false;
        }
        Set<Role> roles = new HashSet<Role>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);
        if (userRepository.findById(1L).orElse(null) == null)
            user.getRoles().add(Role.ROLE_ADMIN);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }
}