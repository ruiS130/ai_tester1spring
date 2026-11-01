package com.example.aitestgen.project;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

	@Test
	void createAssignsIdAndTimestamp() {
		Project project = Project.create("payments-api");

		assertThat(project.getId()).isNotNull();
		assertThat(project.getName()).isEqualTo("payments-api");
		assertThat(project.getCreatedAt()).isNotNull();
	}

	@Test
	void createRejectsBlankName() {
		assertThatThrownBy(() -> Project.create("  "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blank");
	}

}
