package com.kae.article.controller;

import com.kae.article.config.JwtProvider;
import com.kae.article.exception.UnauthorizedException;
import com.kae.article.exception.UserFoundException;
import com.kae.article.model.User;
import com.kae.article.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/login")
    public String login(@RequestBody UserAuth userAuth) {
        User userFromDb = userService.findByLoginAndPassword(userAuth.getUsername(), userAuth.getPassword());
        if (userFromDb != null)
            return jwtProvider.generateToken(userFromDb.getUsername());
        else throw new UnauthorizedException();
    }

    @PostMapping("/registration")
    public User registration(
        @Valid @RequestBody User user
    ) {
        if (userService.addUser(user))
            return user;
        else throw new UserFoundException();
    }
}