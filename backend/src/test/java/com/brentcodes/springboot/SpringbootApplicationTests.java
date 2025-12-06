// ==================== 1. SpringbootApplicationTests.java ====================
package com.brentcodes.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class SpringbootApplicationTests {

	@Test
	void contextLoads() {
		// Verifies Spring context loads successfully
	}

	@Test
	void mainMethodRuns() {
		assertDoesNotThrow(() -> SpringbootApplication.main(new String[] {}));
	}
}
