package hello.springtx.apply;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class TxBasicTest {

	@Autowired
	BasicService basicService;

	@Test
	void proxyCheck() { // 프록시가 적용되는 지 체크

		// aop class=class hello.springtx.apply.TxBasicTest$BasicService$$EnhancerBySpringCGLIB$$67f7f753
		// EnhancerBySpringCGLIB: 프록시가 적용되었다!
		log.info("aop class={}", basicService.getClass());
		assertThat(AopUtils.isAopProxy(basicService)).isTrue();
	}

	@Test
	void txTest() {
		basicService.tx();
		basicService.nonTx();
	}

	@TestConfiguration
	static class TxApplyBasicConfig { // BasicService 를 주입하기 test configuration 설정

		@Bean
		BasicService basicService() {
			return new BasicService();
		}
	}

	@Slf4j
	static class BasicService { // @Transactional 메소드가 하나라도 존재하면 프록시 객체가 주입된다.

		// 1. 클라이언트가 basicService.tx() 호출하면 프록시 객체의 tx()가 호출된다.
		// 2. 프록시가 tx() 메서드가 트랜잭션을 사용할 수 있는지 확인. 👉 @Transactional 이 붙어있으므로 사용가능!
		// 3. 트랜잭션을 시작한 다음, 프록시 객체가 아닌 실제 basicService 객체의 tx() 호출.
		// 4. 실제 basicService.tx() 의 호출이 끝나서 프록시로 제어가 돌아오면 프록시는 로직을 커밋하거나 롤백해서 트랜잭션을 종료시킨다.
		@Transactional
		public void tx() {
			log.info("call tx");
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive(); // 트랜잭션 수행 중
			log.info("tx active={}", txActive);
		}

		// 1. 클라이언트가 basicService.nonTx() 호출하면 프록시 객체의 nonTx() 가 호출됨
		// 2. 프록시가 nonTx() 메서드가 트랜잭션을 사용할 수 있는지 확인. 👉  @Transactional이 없으므로 적용대상 X
		// 3. 트랜잭션을 시작하지 않고, 프록시 객체가 아닌 실제 basicService.nonTx()를 호출하고 종료.
		public void nonTx() {
			log.info("call nonTx");
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active={}", txActive);
		}
	}
}
