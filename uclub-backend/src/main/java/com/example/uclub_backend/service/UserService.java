package com.example.uclub_backend.service;

import com.example.uclub_backend.model.User;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getUsersByQuery(String email, String nickname, String role, String status) {
        // 如果为空，传 null；让 JPQL @Query 自行判断是否忽略条件
        return userRepository.queryUsers(
                (email == null || email.isEmpty()) ? null : email,
                (nickname == null || nickname.isEmpty())? null :nickname,
                (role == null || role.isEmpty()) ? null : role,
                (status == null || status.isEmpty()) ? null : status
        );
    }
}
