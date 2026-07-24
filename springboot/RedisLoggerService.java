/**
Redis 안에 있는 데이터 조회를 위해 사용
*/
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

	public void printAllRedisData() {

		log.info("========================================================");
		log.info("                 Redis Dump Start");
		log.info("========================================================");

		// 전체 Key 조회
		Set<String> keys = stringRedisTemplate.keys("*");

		// Key가 없는 경우
		if (keys.isEmpty()) {
			log.info("No data");
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
			log.info("-----------------------------------------------------------");
			log.info("[{}]", (i+1));
			log.info("Key        : {}", key);
			DataType type = stringRedisTemplate.type(key);
			String typeName = type.name();
			log.info("Type       : {}", typeName);
			Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
			log.info("TTL        : {}", ttl==-1L ? ttl + " (No Expiration)" : ttl + " sec");
			printByValueFormat(type, key);
			log.info("");

		}


		log.info("========================================================");
		log.info("                  Redis Dump End");
		log.info("========================================================");

	}

	public void printByValueFormat(DataType type, String key) {
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
				log.info("(Unsupported Type)");
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
