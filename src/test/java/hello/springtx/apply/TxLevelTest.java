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
public class TxLevelTest {

	/*
		💡 스프링에서 우선순위는 항상 더 구체적이고 자세한 것이 높은 우선순위를 가진다.

		- 메서드와 클래스에 애노테이션을 붙일 수 있으면 더 구체적인 메서드가 더 높은 우선순위를 갖는다.
		- 인터페이스와 구현체에 애노테이션을 붙일 수 있으면 더 구체적인 구현체가 더 높은 우선순위를 갖는다.
	 */

	@Autowired
	LevelService levelService;

	@Test
	void orderTest() {
		levelService.write();
		levelService.read();
	}

	@TestConfiguration
	static class TxLevelTestConfig {

		@Bean
		LevelService levelService() {
			return new LevelService();
		}
	}

	@Slf4j
	@Transactional(readOnly = true)
	static class LevelService {

		@Transactional(readOnly = false)
		public void write () {
			log.info("call write");
			printTxInfo();
		}

		public void read() {
			log.info("call read");
			printTxInfo();
		}

		private void printTxInfo() {
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active={}", txActive);

			boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
			log.info("tx readOnly={}", txReadOnly);
		}
	}
}
