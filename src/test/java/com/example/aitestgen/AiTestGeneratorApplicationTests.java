package com.example.aitestgen;

import com.example.aitestgen.support.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestcontainersConfiguration.class)
class AiTestGeneratorApplicationTests {

	@Test
	void contextLoads() {
	}

}
