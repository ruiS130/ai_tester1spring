package com.example.aitestgen.project;

import com.example.aitestgen.support.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(PostgresTestcontainersConfiguration.class)
class ProjectRepositoryTest {

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	void saveAndFindProject() {
		Project saved = projectRepository.save(Project.create("payments-api"));

		Project found = projectRepository.findById(saved.getId()).orElseThrow();

		assertThat(found.getName()).isEqualTo("payments-api");
		assertThat(found.getCreatedAt()).isNotNull();
	}

}
