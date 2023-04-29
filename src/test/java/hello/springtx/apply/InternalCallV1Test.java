package hello.springtx.apply;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

	@Autowired
	CallService callService;

	@Test
	void printProxy() {
		// 실제 객체가 아닌 프록시 객체가 빈으로 등록되고, 프록시 객체가 주입된다.
		log.info("callService class={}", callService.getClass()); // callService class=class hello.springtx.apply.InternalCallV1Test$CallService$$EnhancerBySpringCGLIB$$f07819c3
	}

	@Test
	void internalCall() {

		callService.internal();

		/*
			(결과)
			Getting transaction for [hello.springtx.apply.InternalCallV1Test$CallService.internal]
			call internal
			active=true
			Completing transaction for [hello.springtx.apply.InternalCallV1Test$CallService.internal]
		 */
	}

	@Test
	void externalCall() {

		// external() 호출 -> @Transactional internal() 호출 👉 트랜잭션이 수행될 것으로 기대
		callService.external();

		/*
			(⭐️ 결과: 트랜잭션이 실행되지 않는다!)
			call external
			tx active=false
			call internal
			tx active=false
		 */

		// 1. 프록시 객체가 트랜잭션을 시작하지 않은채로 callService 객체 인스턴스의 external() 호출
		// 2. external()은 internal()을 호출하는데, 이 때 this.internal()이 호출되는 것이므로 프록시 객체가 아닌 callService 인스턴스의 internal() 호출
		// 3. 트랜잭션은 프록시 객체를 통해서만 할 수 있는데, internal() 호출은 프록시 객체를 통해 호출된 것이 아니라,
		// 		callService 인스턴스를 통해 호출 된 것이므로 트랜잭션이 수행되지 않은 것!
	}

	@TestConfiguration
	static class InternalCallV1TestConfig{

		@Bean
		public CallService callService() {
			return new CallService();
		}
	}

	@Slf4j
	static class CallService {

		public void external() {
			log.info("call external");
			printTxInfo();
			internal();
		}

		@Transactional
		public void internal() {
			log.info("call internal");
			printTxInfo();
		}

		private void printTxInfo() {
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active={}", txActive);
		}
	}
}
