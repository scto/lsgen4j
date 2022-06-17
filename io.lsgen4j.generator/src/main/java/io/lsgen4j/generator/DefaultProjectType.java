package io.lsgen4j.generator;

public enum DefaultProjectType {

	SERVER_LSP4J_BASIC("server/lsp4j/basic");

	private final String path;

	DefaultProjectType(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
