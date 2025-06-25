package com.example.summer.controller;

import com.example.summer.entity.User;
import com.example.summer.service.UserService;
import com.example.summer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> loginForm) {
        try {
            String email = loginForm.get("email");
            String password = loginForm.get("password");
            
            if (email == null || password == null) {
                return Result.error(400, "邮箱和密码不能为空");
            }
            
            Optional<User> userOpt = userService.getUserByEmail(email);
            if (userOpt.isEmpty()) {
                return Result.error(401, "用户不存在");
            }
            
            User user = userOpt.get();
            if (!userService.validatePassword(password, user.getPassword())) {
                return Result.error(401, "密码错误");
            }
            
            // 登录成功，返回用户信息（不包含密码）
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> registerForm) {
        try {
            String email = registerForm.get("email");
            String password = registerForm.get("password");
            String nickname = registerForm.get("nickname");
            
            if (email == null || password == null || nickname == null) {
                return Result.error(400, "邮箱、密码和昵称不能为空");
            }
            
            // 检查邮箱是否已存在
            if (userService.getUserByEmail(email).isPresent()) {
                return Result.error(400, "邮箱已存在");
            }
            
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setNickname(nickname);
            user.setRole(User.UserRole.普通用户);
            user.setStatus(User.UserStatus.正常);
            
            User createdUser = userService.createUser(user);
            // 返回用户信息（不包含密码）
            createdUser.setPassword(null);
            return Result.success(createdUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping
    public Result<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id:\\d+}")
    public Result<User> getUserById(@PathVariable Integer id) {
        System.out.println("[DEBUG] getUserById called with id: " + id);
        try {
            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/email/{email}")
    public Result<User> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/role/{role}")
    public Result<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role);
            List<User> users = userService.getUsersByRole(userRole);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/status/{status}")
    public Result<List<User>> getUsersByStatus(@PathVariable String status) {
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status);
            List<User> users = userService.getUsersByStatus(userStatus);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public Result<List<User>> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/admins")
    public Result<List<User>> getAdmins() {
        try {
            List<User> admins = userService.getAdmins();
            return Result.success(admins);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return Result.success(createdUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> changeUserStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status);
            userService.changeUserStatus(id, userStatus);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 