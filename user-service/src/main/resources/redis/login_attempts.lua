-- KEYS[1] = loginAttemptsKey
-- KEYS[2] = lockedUserKey
-- ARGV[1] = attempts TTL (sec)
-- ARGV[2] = max attempts
-- ARGV[3] = lock TTL (sec)

-- check if user is locked
if redis.call("EXISTS", KEYS[2]) == 1 then
    return -1
end

-- user is not locked, then increment attempts
local attempts = redis.call("INCR", KEYS[1])

-- if first attempt then set TTL to specific time
if attempts == 1 then
    redis.call("EXPIRE", KEYS[1], ARGV[1])
end

-- check max attemts
if attempts >= tonumber(ARGV[2]) then
    redis.call("SET", KEYS[2], "locked", "EX", ARGV[3])
    redis.call("DEL", KEYS[1])
    return -1
end

return attempts