package com.fitlife.gym;

import com.fitlife.gym.config.CorsProperties;
import com.fitlife.gym.config.PiiProperties;
import com.fitlife.gym.config.RazorpayProperties;
import com.fitlife.gym.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    JwtProperties.class,
    CorsProperties.class,
    RazorpayProperties.class,
    PiiProperties.class
})
public class GymManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymManagementApplication.class, args);
    }
}
