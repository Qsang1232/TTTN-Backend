package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final BookingService bookingService;

    // Backend & Frontend URL (có default để không crash)
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    // 1. API TẠO URL THANH TOÁN VNPay
    @GetMapping("/create-payment-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @RequestParam Long bookingId,
            jakarta.servlet.http.HttpServletRequest request
    ) throws java.io.UnsupportedEncodingException {

        // Lấy thông tin đơn hàng
        com.example.demo.model.Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking"));

        long amount = booking.getTotalPrice().longValue() * 100; // VNPay yêu cầu nhân 100
        
        String vnp_TxnRef = com.example.demo.config.VNPayConfig.getRandomNumber(8) + "_" + bookingId;
        // VNPay yêu cầu IPv4, nếu chạy localhost đôi khi ra IPv6 (0:0:0:0:0:0:0:1) sẽ bị lỗi 72
        String vnp_IpAddr = "127.0.0.1";
        
        java.util.Map<String, String> vnp_Params = new java.util.HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        // vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        // Tuyệt đối không dùng khoảng trắng để tránh lỗi encode URL
        vnp_Params.put("vnp_OrderInfo", "ThanhToanDatSan_" + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        java.util.Calendar cld = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(java.util.Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build Query URL
        java.util.List<String> fieldNames = new java.util.ArrayList<>(vnp_Params.keySet());
        java.util.Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(java.net.URLEncoder.encode(fieldName, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = com.example.demo.config.VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Tạo URL thanh toán thành công")
                        .data(paymentUrl)
                        .build()
        );
    }

    // 2. API NHẬN CALLBACK TỪ VNPay & REDIRECT VỀ FRONTEND
    @GetMapping("/vnpay-return")
    public void vnpayReturn(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String baseFrontendUrl = frontendUrl.split(",")[0]; // Cắt lấy URL đầu tiên (http://localhost:3000)

        try {
            java.util.Map<String, String> fields = new java.util.HashMap<>();
            for (java.util.Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String paramName = (String) params.nextElement();
                String paramValue = request.getParameter(paramName);
                if (paramValue != null && paramValue.length() > 0) {
                    String fieldName = java.net.URLEncoder.encode(paramName, java.nio.charset.StandardCharsets.US_ASCII.toString());
                    String fieldValue = java.net.URLEncoder.encode(paramValue, java.nio.charset.StandardCharsets.US_ASCII.toString());
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }
            
            String signValue = com.example.demo.config.VNPayConfig.hmacSHA512(vnp_HashSecret, hashAllFields(fields));
            String txnRef = request.getParameter("vnp_TxnRef");
            Long bookingId = Long.parseLong(txnRef.split("_")[1]);

            if (signValue.equals(vnp_SecureHash)) {
                if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                    // Thanh toán thành công
                    bookingService.confirmBookingPayment(bookingId);
                    response.sendRedirect(baseFrontendUrl + "/profile?payment=success");
                } else {
                    response.sendRedirect(baseFrontendUrl + "/profile?payment=failed");
                }
            } else {
                response.sendRedirect(baseFrontendUrl + "/profile?payment=invalid_signature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(baseFrontendUrl + "/profile?payment=error");
        }
    }

    private String hashAllFields(java.util.Map<String, String> fields) {
        java.util.List<String> fieldNames = new java.util.ArrayList<>(fields.keySet());
        java.util.Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    // 3. API XÁC NHẬN CHUYỂN KHOẢN (QR / Manual)
    @PostMapping("/confirm-transfer")
    public ResponseEntity<ApiResponse<String>> confirmTransfer(
            @RequestParam Long bookingId
    ) {
        bookingService.requestPaymentConfirmation(bookingId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Đã gửi yêu cầu xác nhận thanh toán")
                        .data("WAITING")
                        .build()
        );
    }
}
