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
public class InternalCallV2Test {

	@Autowired
	ExternalService externalService;

	@Autowired
	InternalService internalService;

	@Test
	void internalCall() {
		internalService.internal();
	}

	@Test
	void externalCall() {
		externalService.external();
	}

	@TestConfiguration
	static class InternalCallV2TestConfig{

		@Bean
		public ExternalService externalService() {
			return new ExternalService(internalService());
		}

		@Bean
		public InternalService internalService() {
			return new InternalService();
		}
	}

	@Slf4j
	static class ExternalService {

		private final InternalService internalService;

		// InternalService의 internal()에 @Transactional이 붙어있으므로 프록시 객체가 주입된다.
		ExternalService(InternalService internalService) {
			this.internalService = internalService;
		}

		public void external() {
			log.info("call external");
			printTxInfo();
			internalService.internal(); // 프록시 객체의 internal() 호출 -> 트랜잭션 시작 -> internalService 인스턴스으 internal 호출
		}

		private void printTxInfo() {
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active={}", txActive);
		}
	}

	@Slf4j
	static class InternalService {

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
