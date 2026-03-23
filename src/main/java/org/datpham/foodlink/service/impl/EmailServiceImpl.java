package org.datpham.foodlink.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetOtp(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail != null ? fromEmail.trim() : "");
            helper.setTo(toEmail);
            helper.setSubject("FoodLink - Mã xác nhận đặt lại mật khẩu");
            helper.setText(buildOtpEmailHtml(otpCode), true);

            mailSender.send(message);
            log.info("Password reset OTP email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildOtpEmailHtml(String otpCode) {
        return """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px; background: #faf7f5; border-radius: 16px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <h1 style="color: #C1766B; font-size: 28px; margin: 0;">🍜 FoodLink</h1>
                    </div>
                    <div style="background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
                        <h2 style="color: #333; font-size: 20px; margin-top: 0;">Đặt lại mật khẩu</h2>
                        <p style="color: #666; font-size: 15px; line-height: 1.6;">
                            Bạn đã yêu cầu đặt lại mật khẩu. Sử dụng mã xác nhận bên dưới để tiếp tục:
                        </p>
                        <div style="text-align: center; margin: 28px 0;">
                            <div style="display: inline-block; background: linear-gradient(135deg, #C1766B, #e8a598); color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: 8px; padding: 16px 36px; border-radius: 12px;">
                                %s
                            </div>
                        </div>
                        <p style="color: #999; font-size: 13px; text-align: center;">
                            Mã này có hiệu lực trong <strong>15 phút</strong>.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
                        <p style="color: #999; font-size: 12px; text-align: center;">
                            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                        </p>
                    </div>
                </div>
                """.formatted(otpCode);
    }
}
