package com.pay.application.out;

import com.pay.domain.User;

public interface UserRepository {

    public User _findByName(String name);

    public void _save(User user);
}
