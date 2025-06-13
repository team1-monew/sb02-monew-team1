package com.team1.monew.common.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class IntegrationTestSupport {

  private static final MongoDBContainer MONGO_CONTAINER;
  private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

  // static 필드 → JVM 전체에서 단 한 번만 초기화됨
  // 이 테스트 클래스를 상속한 테스트 클래스 내의 모든 테스트 메서드가 이 컨테이너 인스턴스를 공유
  static {
    MONGO_CONTAINER = new MongoDBContainer("mongo:6.0")
        .withReuse(false);
    MONGO_CONTAINER.start();

    POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass")
        .withReuse(false);
    POSTGRES_CONTAINER.start();
  }

  @DynamicPropertySource
  static void setMongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);

    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
  }
}
