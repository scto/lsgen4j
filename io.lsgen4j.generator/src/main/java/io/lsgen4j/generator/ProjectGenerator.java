package io.lsgen4j.generator;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

public class ProjectGenerator {

	private final Configuration configuration;

	public ProjectGenerator(Configuration configuration) {
		this.configuration = configuration;
	}

	public void generate() throws IOException {
		Path templatesProjectPath = Paths
				.get("src/main/resources/templates/" + configuration.getTemplateProjectBaseDir());
		if (!Files.exists(templatesProjectPath)) {
			throw new ProjectGeneratorException("Cannot find templates " + templatesProjectPath);
		}

		Path output = Paths.get(configuration.getOutDir());

		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		ProjectTemplateCopier tc = new ProjectTemplateCopier(templatesProjectPath, output, configuration,
				new TemplateGenerator(templatesProjectPath));
		Files.walkFileTree(templatesProjectPath, opts, Integer.MAX_VALUE, tc);
	}

}
