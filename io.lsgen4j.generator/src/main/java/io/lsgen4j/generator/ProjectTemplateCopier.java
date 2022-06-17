package io.lsgen4j.generator;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class ProjectTemplateCopier implements FileVisitor<Path> {

	private final Path source;
	private final Path target;
	private final boolean preserve;
	private final Configuration configuration;
	private final TemplateGenerator templateGenerator;

	ProjectTemplateCopier(Path source, Path target, Configuration configuration, TemplateGenerator templateGenerator) {
		this.source = source;
		this.target = target;
		this.configuration = configuration;
		this.preserve = true;
		this.templateGenerator = templateGenerator;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		// before visiting entries in a directory we copy the directory
		// (okay if directory already exists).
		CopyOption[] options = (preserve) ? new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];

		Path resolvedDir = resolvePathExpression(dir);
		Path newdir = target.resolve(source.relativize(resolvedDir));

		try {
			Files.createDirectories(newdir);
		} catch (FileAlreadyExistsException x) {
			// ignore
		} catch (IOException x) {
			x.printStackTrace();
			return SKIP_SUBTREE;
		}
		return CONTINUE;
	}

	private Path resolvePathExpression(Path dir) {
		Path resolvedPath = Paths.get("");
		for (Path path : dir) {
			Path newPath = templateGenerator.resolvePath(path, configuration);
			resolvedPath = resolvedPath.resolve(newPath);
		}
		return resolvedPath;
	}

	@Override
	public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) {
		String fileName = sourceFile.getFileName().getName(0).toString();
		int index = fileName.indexOf(".qute.");
		if (index != -1) {
			// ex : pom.qute.xml --> pom.xml
			String newFileName = fileName.replace(".qute.", ".");
			Path renamedSourceFile = sourceFile.getParent().resolve(newFileName);
			Path targetFile = target.resolve(source.relativize(renamedSourceFile));
			targetFile = resolvePathExpression(targetFile);
			System.out.println("Generate '" + targetFile + "' from '" + sourceFile + "'");

			try {
				String content = templateGenerator.generate(sourceFile, configuration);
				Files.writeString(targetFile, content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// simple file copy
			Path targetFile = target.resolve(source.relativize(sourceFile));
			copyFile(sourceFile, targetFile, preserve);
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		// fix up modification time of directory when done
//		if (exc == null && preserve) {
//			Path newdir = target.resolve(source.relativize(dir));
//			try {
//				FileTime time = Files.getLastModifiedTime(dir);
//				Files.setLastModifiedTime(newdir, time);
//			} catch (IOException x) {
//				System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
//			}
//		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		if (exc instanceof FileSystemLoopException) {
			System.err.println("cycle detected: " + file);
		} else {
			System.err.format("Unable to copy: %s: %s%n", file, exc);
		}
		return CONTINUE;
	}

	/**
	 * Copy source file to target location. If {@code prompt} is true then prompt
	 * user to overwrite target if it exists. The {@code preserve} parameter
	 * determines if file attributes should be copied/preserved.
	 */
	static void copyFile(Path source, Path target, boolean preserve) {
		CopyOption[] options = (preserve) ? new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING }
				: new CopyOption[] { REPLACE_EXISTING };
		// if (!prompt || Files.notExists(target) || okayToOverwrite(target)) {
		try {
			Files.copy(source, target, options);
		} catch (IOException x) {
			System.err.format("Unable to copy: %s: %s%n", source, x);
		}
		// }
	}
}
