package com.kreview.kreview.jobs;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JobStatusService {

  private static final String KEY_PREFIX = "job:status:";
  private static final Duration TTL = Duration.ofHours(1);

  private static final String ACTIVE_KEY_PREFIX = "job:active:contract:";
  private static final Duration ACTIVE_TTL = Duration.ofMinutes(5);

  private final StringRedisTemplate redisTemplate;

  public JobStatusService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void setStatus(String jobId, String status) {
    redisTemplate.opsForValue().set(KEY_PREFIX + jobId, status, TTL);
  }

  public String getStatus(String jobId) {
    return redisTemplate.opsForValue().get(KEY_PREFIX + jobId);
  }

  public boolean tryMarkActive(Long contractId, String jobId) {
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(ACTIVE_KEY_PREFIX + contractId, jobId, ACTIVE_TTL);
    return Boolean.TRUE.equals(acquired);
  }

  public void clearActive(Long contractId) {
    redisTemplate.delete(ACTIVE_KEY_PREFIX + contractId);
  }
}