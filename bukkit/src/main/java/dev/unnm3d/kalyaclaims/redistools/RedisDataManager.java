package dev.unnm3d.kalyaclaims.redistools;

import io.lettuce.core.RedisClient;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.town.Town;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class RedisDataManager extends RedisAbstract {
    public RedisDataManager(RedisClient lettuceRedisClient, int poolSize) {
        super(lettuceRedisClient, poolSize);
    }


    public void setExpiredClaims(Town town, Map<Float, Claim> claims) {
        String claimsSerialized = claims.entrySet().stream()
                .map(entry -> entry.getKey() + "§" + entry.getValue().getChunk().getX() + "§" + entry.getValue().getChunk().getZ())
                .collect(Collectors.joining("§"));
        getConnectionAsync(redisAsyncCommands ->
                redisAsyncCommands.hset(DataKeys.EXPIRED_CLAIMS + town.getName(), String.valueOf(System.currentTimeMillis()), claimsSerialized));
    }

    public CompletionStage<Map<Long, Map<Float, Claim>>> getExpiredClaims(Town town) {
        return getConnectionAsync(redisAsyncCommands ->
                redisAsyncCommands.hgetall(DataKeys.EXPIRED_CLAIMS + town.getName()))
                .thenApply(map -> {
                    Map<Long, Map<Float, Claim>> claimPricesWithTimestamp = new HashMap<>();
                    map.forEach((key, value) -> {
                        String[] split = value.split("§");
                        Map<Float, Claim> claimPrices = new HashMap<>();
                        for (int i = 0; i < split.length; i += 3) {
                            float price = Float.parseFloat(split[i]);
                            int x = Integer.parseInt(split[i + 1]);
                            int z = Integer.parseInt(split[i + 2]);
                            claimPrices.put(price, Claim.at(Chunk.at(x, z)));
                        }
                        claimPricesWithTimestamp.put(Long.parseLong(key), claimPrices);
                    });
                    return claimPricesWithTimestamp;
                });
    }
}
