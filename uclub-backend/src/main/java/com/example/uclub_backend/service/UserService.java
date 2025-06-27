package com.example.uclub_backend.service;

import com.example.uclub_backend.model.User;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public void updateUserStatus(Integer id, String newStatus) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String statusStr = newStatus;
            User.Status status = User.Status.valueOf(statusStr); //转为枚举
            user.setStatus(status);

            userRepository.save(user);
            System.out.println("已经修改");
        } else {
            throw new RuntimeException("用户未找到");
        }
    }

    public boolean verifyUserPasswordAndRoleAndStatus(String email, String password, String requiredRole, String requiredStatus) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        //三重校验
        return user.getPassword().equals(password)
                && requiredRole.equals(user.getRole())
                && requiredStatus.equals(user.getStatus());
    }



}
