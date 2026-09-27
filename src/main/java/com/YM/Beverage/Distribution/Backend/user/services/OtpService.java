package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.user.models.Otp;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.OtpRepository;
import com.YM.Beverage.Distribution.Backend.utils.email.EmailService;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final EmailService emailService;


    @Transactional
    public void generateOtp(User user, String password, boolean isForPasswordReset) {
        int code = generateRandomOtp();

        String message = "Lemat System: Your OTP is " + code + ". Use this code to " +
                (user.isActive() ? "reset your password" : "activate your account") +
                ". This code expires in 15 minutes. Do not share this code with anyone.";

        Optional<Otp> optionalOtp = otpRepository.findByUser(user);

        if (optionalOtp.isPresent()) {
            Otp currentOtp = optionalOtp.get();
            currentOtp.setCode(BCrypt.hashpw(String.valueOf(code), BCrypt.gensalt()));
            currentOtp.setNumberOfTries(0);
            otpRepository.save(currentOtp);
        } else {
            Otp otp = Otp.builder()
                    .code(BCrypt.hashpw(String.valueOf(code), BCrypt.gensalt()))
                    .numberOfTries(0)
                    .user(user)
                    .build();
            otpRepository.save(otp);
        }

        String[] recipients = {"natiman1472@gmail.com", "natiyash24@gmail.com", user.getEmail()};

        try {
            if(isForPasswordReset){
                emailService.sendForgotPasswordEmail(recipients, user.getFirstName(), code);
            }
            else emailService.sendAccountVerificationEmail(recipients, user.getEmail(), password, code);
        }
        catch (MessagingException e){
            throw new CustomException("There was a problem sending an email, Please try again", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void verifyOtp(User user , String otpCode , boolean shouldRemoveOtp){
        Optional<Otp> optionalOtp = otpRepository.findByUser(user);

        if(optionalOtp.isPresent()){
            Otp currentOtp = optionalOtp.get();
            String code = currentOtp.getCode();
            LocalDateTime otpUpdatedAt = currentOtp.getUpdatedAt();
            Duration duration = Duration.between(otpUpdatedAt, LocalDateTime.now());
            long minutesPassed = duration.toMinutes();
            if(currentOtp.getNumberOfTries() >= 5){
                otpRepository.delete(currentOtp);
                throw new CustomException("Multiple retries detected , request a new OTP",HttpStatus.BAD_REQUEST,"");
            }
            else if(!BCrypt.checkpw(otpCode,code) || minutesPassed > 15){
                currentOtp.setNumberOfTries(currentOtp.getNumberOfTries() + 1);
                otpRepository.save(currentOtp);
                throw new CustomException("Otp has either expired or is incorrect",HttpStatus.BAD_REQUEST,"");
            }
            if(shouldRemoveOtp){
                otpRepository.delete(currentOtp);
            }
        }
        else{
            throw new CustomException("Otp not found",HttpStatus.NOT_FOUND,"");
        }
    }

    private int generateRandomOtp() {
        return (int)(Math.random() * 900000) + 100000; // Generates a random 6-digit number
    }
}
