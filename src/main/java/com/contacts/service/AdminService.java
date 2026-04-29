package com.contacts.service;

import com.contacts.dto.UserSummary;
import com.contacts.entity.User;
import com.contacts.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserSummary> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserSummary> result = new ArrayList<>();
        for (User user : users) {
            result.add(new UserSummary(user.getUsername(), user.getEmail()));
        }
           
        return result;
    }   
}
