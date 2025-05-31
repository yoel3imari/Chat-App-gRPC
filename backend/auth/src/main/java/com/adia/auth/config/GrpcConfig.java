package com.adia.auth.config;

import com.adia.auth.security.JwtServerInterceptor;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class GrpcConfig {

   private JwtServerInterceptor jwtInterceptor;

   @Bean
   public GrpcServerConfigurer serverConfigurer() {
       return serverBuilder -> serverBuilder.intercept(jwtInterceptor);
   }
}
