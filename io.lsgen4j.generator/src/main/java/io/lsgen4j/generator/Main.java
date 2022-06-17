package io.lsgen4j.generator;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		configuration.setPackageName("io");
		configuration.setLanguage("Hawaii");
		configuration.setGroupId("io.hawaii");
		configuration.setArtifactId("io.hawaii");
		configuration.setProjectType(DefaultProjectType.SERVER_LSP4J_BASIC.getPath());
		configuration.setOutDir("target/generated-project");
		
		ProjectGenerator projectGenerator = new ProjectGenerator(configuration);
		try {
			projectGenerator.generate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
