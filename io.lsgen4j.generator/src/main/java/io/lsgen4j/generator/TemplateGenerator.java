package io.lsgen4j.generator;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

public class TemplateGenerator {

	private final Engine engine;

	private final Path templatesProjectPath;

	public TemplateGenerator(Path templatesProjectPath) {
		engine = Engine.builder() //
				.addDefaults() //
				.addLocator(new ProjectTemplateLocator(templatesProjectPath))
				.addValueResolver(new ReflectionValueResolver())//
				.build();
		this.templatesProjectPath = templatesProjectPath;
	}

	public String generate(Path templateFile, Configuration configuration) {
		Template template = engine.getTemplate(getTemplateId(templateFile));
		TemplateInstance templateInstance = template.instance();
		return merge(templateInstance, configuration);
	}

	private String merge(String templateContent, Configuration configuration) {
		TemplateInstance templateInstance = engine.parse(templateContent).instance();
		return merge(templateInstance, configuration);
	}

	private String merge(TemplateInstance templateInstance, Configuration configuration) {
		return templateInstance //
				.data("language", configuration.getLanguage()) //
				.data("packageName", configuration.getPackageName()) //
				.data("groupId", configuration.getGroupId()) //
				.data("artifactId", configuration.getArtifactId()) //
				.render();
	}

	public Path resolvePath(Path path, Configuration configuration) {
		String pathName = path.getName(path.getNameCount() - 1).toString();
		if (pathName.contains("{")) {
			String newPathName = merge(pathName, configuration);
			return Paths.get(newPathName);
		}
		return path;
	}

	public String getTemplateId(Path templateFile) {
		return templatesProjectPath.relativize(templateFile).toString();
	}

}
