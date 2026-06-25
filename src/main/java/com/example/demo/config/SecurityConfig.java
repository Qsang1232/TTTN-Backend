package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Chỉ bỏ qua quét Token đối với việc truy cập xem file tĩnh trực tiếp
        return (web) -> web.ignoring().requestMatchers(
                "/uploads/**", 
                "/images/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Cho phép các request kiểm tra Preflight (OPTIONS) đi qua tự do
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Mở công khai các API cho người dùng vãng lai
                .requestMatchers("/api/auth/**", "/api/categories/**", "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courts/**", "/api/courts").permitAll()
                
                // MỞ CÔNG KHAI DUY NHẤT ENDPOINT KIỂM TRA GIỜ TRỐNG CỦA BOOKING
                .requestMatchers(HttpMethod.GET, "/api/bookings/check-availability").permitAll()

                // Cấu hình xem ảnh tĩnh trực tiếp (dự phòng nếu webCustomizer bị trượt)
                // Cho phép lấy ảnh công khai bất kể lưu ở thư mục nào
                .requestMatchers(HttpMethod.GET, "/uploads/**", "/images/**", "/api/upload/**").permitAll()

                // MỞ CÔNG KHAI ENDPOINT CALLBACK CỦA VNPAY (Trình duyệt từ VNPay trả về không có Token)
                .requestMatchers(HttpMethod.GET, "/api/payment/vnpay-return").permitAll()

                // PHÂN QUYỀN QUẢN TRỊ ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/upload", "/api/upload/**").hasRole("ADMIN") // Fix lỗ hổng ai cũng upload được ảnh
                .requestMatchers(HttpMethod.GET, "/api/bookings/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/confirm").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/courts/**", "/api/courts").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/courts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courts/**").hasRole("ADMIN")

                // PHÂN QUYỀN CHO USER ĐĂNG NHẬP (Tất cả endpoint bookings còn lại như tạo đơn, hủy, my-history)
                .requestMatchers("/api/bookings/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}