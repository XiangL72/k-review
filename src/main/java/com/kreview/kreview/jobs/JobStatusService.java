package com.kreview.kreview.jobs;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JobStatusService {

  private static final String KEY_PREFIX = "job:status:";
  private static final Duration TTL = Duration.ofHours(1);

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
}