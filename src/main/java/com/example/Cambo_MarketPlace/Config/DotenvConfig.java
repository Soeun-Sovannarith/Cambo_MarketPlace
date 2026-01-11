package com.example.Cambo_MarketPlace.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> dotenvMap = new HashMap<>();
            
            dotenv.entries().forEach(entry -> {
                dotenvMap.put(entry.getKey(), entry.getValue());
            });

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenvProperties", dotenvMap));
        } catch (Exception e) {
            // If .env file doesn't exist or can't be loaded, just continue
            System.out.println("No .env file found or error loading it: " + e.getMessage());
        }
    }
}
