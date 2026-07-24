package com.example.board.common.interceptor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLoggerService {

	private final StringRedisTemplate stringRedisTemplate;

	private static final String HEADER = "========================================================";

	private static final String LINE = "-----------------------------------------------------------";

	public void printAllRedisData() {

		log.info(HEADER);
		log.info("                 Redis Dump Start");
		log.info(HEADER);

		// 전체 Key 조회
		Set<String> keys = stringRedisTemplate.keys("*");

		// Key가 없는 경우
		if (keys == null || keys.isEmpty()) {
			log.info("No data");
			return;
		}

		List<String> sortedKeys = keys.stream().sorted().toList();

		if (!keys.isEmpty()) {
			log.info("");
			log.info("Total Keys : {}", sortedKeys.size());
			log.info("");
		}

		// Key 순회
		// 1. Key 출력
		// 2. Type 출력
		// 3. TTL 출력
		// 4. Value 출력
		for (int i = 0; i < sortedKeys.size(); i++) {
			String key = sortedKeys.get(i);
			log.info(LINE);
			log.info("[{}]", (i+1));
			log.info("Key        : {}", key);
			DataType type = stringRedisTemplate.type(key);
			log.info("Type       : {}", type);
			Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
			log.info("TTL        : {}", formatTTL(ttl));
			printValue(type, key);
			log.info("");

		}


		log.info(HEADER);
		log.info("                  Redis Dump End");
		log.info(HEADER);

	}

	private String formatTTL(Long ttl) {
		if (ttl == null)
			return "Unknown";

		if (ttl == -2L)
			return "Expired";

		if (ttl == -1L)
			return "No Expiration";

		return ttl + " sec";
	}

	public void printValue(DataType type, String key) {
		switch (type) {
			case STRING:
				log.info("Value:");
				printString(key);
				break;
			case LIST:
				log.info("Elements:");
				printList(key);
				break;
			case SET:
				log.info("Members:");
				printSet(key);
				break;
			case ZSET:
				log.info("Members (Score):");
				printZSet(key);
				break;
			case HASH:
				log.info("Fields:");
				printHash(key);
				break;
			default:
				log.info("(Unsupported Type : {})", type);
		}

	}

	public void printString(String key) {
		String value = stringRedisTemplate.opsForValue().get(key);

		log.info("{}", value);
	}

	public void printList(String key) {
		List<String> list =
			stringRedisTemplate.opsForList().range(key, 0, -1);

		if (list == null || list.isEmpty()) {
			log.info("(empty)");
			return;
		}

		for (int i = 0; i < list.size(); i++) {
			log.info("[{}] {}", i, list.get(i));
		}
	}

	public void printSet(String key) {
		Set<String> members =
			stringRedisTemplate.opsForSet().members(key);

		if (members == null || members.isEmpty()) {
			log.info("(empty)");
			return;
		}

		members.stream()
			.sorted()
			.forEach(member -> log.info("- {}", member));
	}

	public void printZSet(String key) {
		Set<ZSetOperations.TypedTuple<String>> tuples =
			stringRedisTemplate.opsForZSet()
				.rangeWithScores(key, 0, -1);

		if (tuples == null || tuples.isEmpty()) {
			log.info("(empty)");
			return;
		}

		for (ZSetOperations.TypedTuple<String> tuple : tuples) {
			log.info("{} : {}", tuple.getScore(), tuple.getValue());
		}
	}

	public void printHash(String key) {
		Map<Object, Object> map =
			stringRedisTemplate.opsForHash().entries(key);

		if (map.isEmpty()) {
			log.info("(empty)");
			return;
		}

		map.entrySet().stream()
			.sorted(Map.Entry.comparingByKey(
				Comparator.comparing(Object::toString)))
			.forEach(entry ->
				log.info("{} = {}", entry.getKey(), entry.getValue()));
	}
}
