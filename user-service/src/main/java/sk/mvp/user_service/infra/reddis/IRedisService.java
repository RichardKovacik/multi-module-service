package sk.mvp.user_service.infra.reddis;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRedisService {
    void set(String key,Object value, Duration duration);
    void increment(String key);
    void delete(String key);
    void addValueToSet(String key, Object value);
    Set<Object> getSet(String key);
    void deleteValueFromSet(String key, String value);

    Optional<Object> get(String key);
    boolean has(String key);
    Long executeLuaScript(String scriptPath, List<String> keys, Object... args);

}
