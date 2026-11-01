package com.example.aitestgen.project;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

	@Id
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected Project() {
	}

	public Project(UUID id, String name, Instant createdAt) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = requireName(name);
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
	}

	public static Project create(String name) {
		return new Project(UUID.randomUUID(), name, Instant.now());
	}

	private static String requireName(String name) {
		Objects.requireNonNull(name, "name");
		String trimmed = name.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		return trimmed;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
