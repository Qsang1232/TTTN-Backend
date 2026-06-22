package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Kiểm tra nếu chưa có dữ liệu thì mới tạo
        if (userRepository.count() == 0) {
            seedAllData();
        }
    }

    private void seedAllData() {
        System.out.println(">>> BẮT ĐẦU TẠO DỮ LIỆU MẪU VỚI ẢNH GITHUB...");

        List<User> users = seedUsers();
        List<Category> categories = seedCategories();
        List<Court> courts = seedCourts(categories); // Hàm này đã được sửa lại link ảnh
        seedBookings(users, courts);
        seedReviews(users, courts);

        System.out.println(">>> ĐÃ TẠO XONG TOÀN BỘ DỮ LIỆU!");
    }

    private List<User> seedUsers() {
        List<User> userList = new ArrayList<>();

        // Admin
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .email("admin@badminton.com")
                .role(Role.ADMIN)
                .phone("0900000001").address("Admin HQ").build();
        userList.add(admin);

        // User chính
        User mainUser = User.builder()
                .username("user")
                .password(passwordEncoder.encode("123456"))
                .email("user@badminton.com")
                .role(Role.USER)
                .phone("0900000002").address("User Home").build();
        userList.add(mainUser);

        // Tạo thêm 10 user phụ
        for (int i = 1; i <= 10; i++) {
            userList.add(User.builder()
                    .username("player" + i)
                    .password(passwordEncoder.encode("123456"))
                    .email("player" + i + "@mail.com")
                    .role(Role.USER)
                    .phone("09123456" + i)
                    .address("Address " + i)
                    .build());
        }
        return userRepository.saveAll(userList);
    }

    private List<Category> seedCategories() {
        List<String> districtNames = Arrays.asList(
                "Quận 1", "Quận 3", "Quận 7", "Quận 10", "Bình Thạnh",
                "Phú Nhuận", "Tân Bình", "Gò Vấp", "TP. Thủ Đức", "Hà Nội"
        );

        List<Category> categories = new ArrayList<>();
        for (String name : districtNames) {
            categories.add(new Category(null, name));
        }
        return categoryRepository.saveAll(categories);
    }

    // --- ĐÂY LÀ HÀM ĐÃ ĐƯỢC CẬP NHẬT LINK GITHUB ---
    private List<Court> seedCourts(List<Category> categories) {
        List<Court> courts = new ArrayList<>();
        Random random = new Random();

        // 1. LINK GITHUB RAW (Trỏ thẳng vào kho ảnh của bạn)
        String baseUrl = "https://raw.githubusercontent.com/Qsang1232/BackEnd_ChuyenDeThucTap/main/src/main/resources/static/images/";

        int totalImages = 25; // Số lượng ảnh bạn đã up (san1 -> san25)
        String[] images = new String[totalImages];

        // 2. VÒNG LẶP TẠO LINK ẢNH
        for (int i = 0; i < totalImages; i++) {
            // Kết quả sẽ là: .../images/san1.jpg, .../images/san2.jpg
            images[i] = baseUrl + "san" + (i + 1) + ".jpg";
        }

        int courtCount = 0; // Biến đếm để lấy ảnh từ mảng images
        for (Category cat : categories) {
            int courtsInCat = random.nextInt(3) + 3; // Mỗi quận 3-5 sân

            for (int i = 0; i < courtsInCat; i++) {
                // LOGIC QUAN TRỌNG: Dùng Modulo (%) để lấy ảnh tuần tự
                // Đảm bảo không bị lỗi IndexOutOfBounds và ảnh lặp lại đều đặn
                String imgUrl = images[courtCount % images.length];

                BigDecimal price = BigDecimal.valueOf(50000 + (random.nextInt(10) * 10000));

                Court court = Court.builder()
                        .name("Sân " + cat.getName() + " - Số " + (i + 1))
                        .description("Sân thảm tiêu chuẩn thi đấu, ánh sáng tốt. Phù hợp cho cả tập luyện và giao lưu.")
                        .address(random.nextInt(100) + " Đường Số " + (i + 1) + ", " + cat.getName())
                        .imageUrl(imgUrl) // Lưu link Online vào DB
                        .pricePerHour(price)
                        .category(cat)
                        .openingTime(LocalTime.of(5, 0))
                        .closingTime(LocalTime.of(23, 0))
                        .build();
                
                courts.add(court);
                courtCount++; // Tăng biến đếm
            }
        }
        return courtRepository.saveAll(courts);
    }

    private void seedBookings(List<User> users, List<Court> courts) {
        List<Booking> bookings = new ArrayList<>();
        Random random = new Random();
        User mainUser = users.get(1);

        // Booking QUÁ KHỨ
        for (int i = 1; i <= 5; i++) {
            Court court = courts.get(random.nextInt(courts.size()));
            bookings.add(Booking.builder()
                    .user(mainUser)
                    .court(court)
                    .startTime(LocalDateTime.now().minusDays(i).withHour(18).withMinute(0))
                    .endTime(LocalDateTime.now().minusDays(i).withHour(20).withMinute(0))
                    .status("COMPLETED")
                    .totalPrice(court.getPricePerHour().multiply(BigDecimal.valueOf(2)))
                    .build());
        }

        // Booking TƯƠNG LAI
        for (int i = 1; i <= 3; i++) {
            Court court = courts.get(random.nextInt(courts.size()));
            bookings.add(Booking.builder()
                    .user(mainUser)
                    .court(court)
                    .startTime(LocalDateTime.now().plusDays(i).withHour(9).withMinute(0))
                    .endTime(LocalDateTime.now().plusDays(i).withHour(11).withMinute(0))
                    .status("CONFIRMED")
                    .totalPrice(court.getPricePerHour().multiply(BigDecimal.valueOf(2)))
                    .build());
        }
        
        // Random Booking
        for (int i = 0; i < 20; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            Court randomCourt = courts.get(random.nextInt(courts.size()));
            bookings.add(Booking.builder()
                    .user(randomUser)
                    .court(randomCourt)
                    .startTime(LocalDateTime.now().plusDays(random.nextInt(10)).withHour(14).withMinute(0))
                    .endTime(LocalDateTime.now().plusDays(random.nextInt(10)).withHour(16).withMinute(0))
                    .status("CONFIRMED")
                    .totalPrice(randomCourt.getPricePerHour().multiply(BigDecimal.valueOf(2)))
                    .build());
        }

        bookingRepository.saveAll(bookings);
        System.out.println(">>> Đã tạo Booking mẫu");
    }

    private void seedReviews(List<User> users, List<Court> courts) {
        List<Review> reviews = new ArrayList<>();
        Random random = new Random();
        String[] comments = {
                "Sân đẹp, chủ sân nhiệt tình.",
                "Thảm hơi cũ nhưng giá rẻ, chấp nhận được.",
                "Ánh sáng hơi chói mắt ở góc sân 1.",
                "Tuyệt vời! Sẽ quay lại dài dài.",
                "Nhà vệ sinh sạch sẽ, có chỗ để xe rộng.",
                "Book lịch dễ dàng, thanh toán nhanh.",
                "Sân hơi trơn do trời mưa ẩm.",
                "10 điểm chất lượng!"
        };

        for (int i = 0; i < 40; i++) {
            reviews.add(Review.builder()
                    .user(users.get(random.nextInt(users.size())))
                    .court(courts.get(random.nextInt(courts.size())))
                    .rating(random.nextInt(3) + 3)
                    .comment(comments[random.nextInt(comments.length)])
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .build());
        }
        reviewRepository.saveAll(reviews);
        System.out.println(">>> Đã tạo Reviews mẫu");
    }
}