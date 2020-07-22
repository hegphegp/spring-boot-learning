package com.condingfly;

import com.condingfly.entity.UserEntity;
import com.condingfly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Timestamp;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setPhone("phone");
        user.setDel(false);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        user.setCreateAt(now);
        user.setUpdateAt(now);
        userRepository.save(user);
    }

}