package com.pay.application;

import com.pay.application.in.UserService;
import com.pay.application.out.UserRepository;
import com.pay.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void register(User user) {
        // 1. db 조회
        var userFromDb = userRepository._findByName(user.getName());

        // 2. db에 존재하면 skip
        if (userFromDb != null) return;

        // 3. db에 저장
        userRepository._save(user);
    }
}
