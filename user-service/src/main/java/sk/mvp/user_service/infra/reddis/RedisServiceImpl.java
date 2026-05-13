package sk.mvp.user_service.infra.reddis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RedisServiceImpl implements IRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, Object>  redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().setIfAbsent(key, value, duration);

    }

    @Override
    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void addValueToSet(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public Set<Object> getSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public void deleteValueFromSet(String key, String value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public boolean has(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Long executeLuaScript(String scriptPath, List<String> keys, Object... args) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(scriptPath));
        script.setResultType(Long.class);

        return redisTemplate.execute(script, keys, args);
    }
}
