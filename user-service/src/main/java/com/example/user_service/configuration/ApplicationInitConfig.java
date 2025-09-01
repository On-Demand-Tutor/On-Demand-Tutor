package com.example.user_service.configuration;

import com.example.user_service.entity.UserrRole;
import com.example.user_service.entity.User;
import com.example.user_service.enums.UserRole;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository,
                                               RoleRepository roleRepository) {
        return args -> {
            // ✅ Tạo Role ADMIN nếu chưa có
            UserrRole adminRole = roleRepository.findById(com.example.user_service.enums.UserRole.ADMIN.name())
                    .orElseGet(() -> {
                        UserrRole role = UserrRole.builder()
                                .name(com.example.user_service.enums.UserRole.ADMIN.name())
                                .description("Administrator role")
                                .build();
                        return roleRepository.save(role);
                    });

            // ✅ Tạo user admin nếu chưa có
            if (userRepository.findByEmail("adminadmin@gmail.com").isEmpty()) {
                User adminUser = User.builder()
                        .email("adminadmin@gmail.com")
                        .password(passwordEncoder.encode("Strong@123"))
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(adminUser);

                log.warn("✅ Default admin user created with username 'admin' and password 'admin'. Please change the password!");
            }
        };
    }
}
