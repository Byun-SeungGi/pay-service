package com.pay.adapter.out;


import com.pay.adapter.out.entity.UserEntity;
import com.pay.application.out.UserRepository;
import com.pay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public abstract class UserRepositoryImpl implements UserRepository, JpaRepository<UserEntity, Long> {

    @Override
    @Transactional(readOnly = true)
    public void _findByName(String name) {
        this.findByName(name);
    }

    @Override
    @Transactional
    public void _saveUser(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(user.getName());
        userEntity.setPassword(user.getPassword());
        save(userEntity);
    }
}
