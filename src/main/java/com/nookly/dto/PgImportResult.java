package com.nookly.dto;

import java.util.List;

public class PgImportResult {
	private int totalProcessed;
	private int successCount;
	private int failedCount;
	private List<String> errors;

	public PgImportResult(int totalProcessed, int successCount, int failedCount, List<String> errors) {
		this.totalProcessed = totalProcessed;
		this.successCount = successCount;
		this.failedCount = failedCount;
		this.errors = errors;
	}

	public int getTotalProcessed() {
		return totalProcessed;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public List<String> getErrors() {
		return errors;
	}
}