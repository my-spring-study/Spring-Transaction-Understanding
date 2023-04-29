package hello.springtx.apply;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
public class InitTxTest {

	@Autowired
	Hello hello;

	@Test
	void go() {
		// 초기화 코드(@PostConstruct)는 스프링이 초기화 시점에 호출한다.
		// 스프링 빈 등록 -> @PostConstruct 실행
		// 초기화 코드와 @Transactional을 함께 사용하면 트랜잭션이 적용되지 않는다.
		// 👉 초기화 코드가 먼저 호출되고 그 다음에 트랜잭션 AOP가 적용되기 때문이다. (초기화 시점에 이미 @Transactional 이 붙은 메소드를 호출해버렸다!)

		/*
		 	(결과 로그)
			hello.springtx.apply.InitTxTest$Hello    : Hello init @PostConstruct tx active=false
			hello.springtx.apply.InitTxTest          : Started InitTxTest in 2.669 seconds (JVM running for 3.691) // 스프링 컨테이너가 다 떴고, ApplicationReadyEvent 발
			o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.InitTxTest$Hello.initV2]
			hello.springtx.apply.InitTxTest$Hello    : Hello init ApplicationReadyEvent tx active=true
			o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.apply.InitTxTest$Hello.initV2]
		 */
	}

	@TestConfiguration
	static class InitTxTestConfig {

		@Bean
		public Hello hello() {
			return new Hello();
		}
	}

	@Slf4j
	static class Hello {

		@PostConstruct
		@Transactional
		public void initV1() {
			boolean isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("Hello init @PostConstruct tx active={}", isTransactionActive);
		}

		@EventListener(ApplicationReadyEvent.class) // 스프링 컨테이너가 완전히 다 떴을 때(빈, AOP 등등 전부 준비완료)는 @Transactional 적용 가능
		@Transactional
		public void initV2() {
			boolean isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("Hello init ApplicationReadyEvent tx active={}", isTransactionActive);
		}
	}
}
