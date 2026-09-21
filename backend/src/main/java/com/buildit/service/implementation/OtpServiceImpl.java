package com.buildit.service.implementation;

import com.buildit.service.OtpService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

@Service
public class OtpServiceImpl implements OtpService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final String KEY_PREFIX = "otp:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAndStore(String phoneNumber) {
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(KEY_PREFIX + phoneNumber, code, OTP_TTL);
        return code;
    }

    @Override
    public boolean verifyAndConsume(String phoneNumber, String code) {
        String key = KEY_PREFIX + phoneNumber;
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null || !Objects.equals(stored.toString(), code)) {
            return false;
        }
        redisTemplate.delete(key);
        return true;
    }
}
