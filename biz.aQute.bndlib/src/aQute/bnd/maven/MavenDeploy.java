package aQute.bnd.maven;

import static aQute.libg.slf4j.GradleLogging.LIFECYCLE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.build.Project;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.JarResource;
import aQute.bnd.osgi.Processor;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.Deploy;
import aQute.bnd.service.Plugin;
import aQute.libg.command.Command;
import aQute.service.reporter.Reporter;

public class MavenDeploy implements Deploy, Plugin {
	private final static Logger	logger	= LoggerFactory.getLogger(MavenDeploy.class);

	String		repository;
	String		url;
	String		homedir;
	String		keyname;

	String		passphrase;
	Reporter	reporter;

	public void setProperties(Map<String,String> map) {
		repository = map.get("repository");
		url = map.get("url");
		passphrase = map.get("passphrase");
		homedir = map.get("homedir");
		keyname = map.get("keyname");

		if (url == null)
			throw new IllegalArgumentException("MavenDeploy plugin must get a repository URL");
		if (repository == null)
			throw new IllegalArgumentException("MavenDeploy plugin must get a repository name");
	}

	public void setReporter(Reporter processor) {
		this.reporter = processor;
	}

	/**
	 */
	public boolean deploy(Project project, String jarName, InputStream jarStream) throws Exception {
		Parameters deploy = project.parseHeader(project.getProperty(Constants.DEPLOY));

		Map<String,String> maven = deploy.get(repository);
		if (maven == null)
			return false; // we're not playing for this bundle

		logger.info(LIFECYCLE, "deploying {} to Maven repo: {}", jarName, repository);
		File target = project.getTarget();
		File tmp = Processor.getFile(target, repository);
		if (!tmp.exists() && !tmp.mkdirs()) {
			throw new IOException("Could not create directory " + tmp);
		}

		Jar original = new Jar(jarName, jarStream);
		try {
			Manifest manifest = original.getManifest();
			if (manifest == null)
				project.error("Jar has no manifest: %s", original);
			else {
				logger.info(LIFECYCLE, "Writing pom.xml");
				PomResource pom = new PomResource(manifest);
				pom.setProperties(maven);
				File pomFile = write(tmp, pom, "pom.xml");

				Jar main = new Jar("main");
				Jar src = new Jar("src");
				try {
					split(original, main, src);
					Parameters exports = project
							.parseHeader(manifest.getMainAttributes().getValue(Constants.EXPORT_PACKAGE));
					File jdoc = new File(tmp, "jdoc");
					jdoc.mkdirs();
					logger.info(LIFECYCLE, "Generating Javadoc for: {}", exports.keySet());
					Jar javadoc = javadoc(jdoc, project, exports.keySet());
					logger.info(LIFECYCLE, "Writing javadoc jar");
					File javadocFile = write(tmp, new JarResource(javadoc), "javadoc.jar");
					logger.info(LIFECYCLE, "Writing main file");
					File mainFile = write(tmp, new JarResource(main), "main.jar");
					logger.info(LIFECYCLE, "Writing sources file");
					File srcFile = write(tmp, new JarResource(main), "src.jar");

					logger.info(LIFECYCLE, "Deploying main file");
					maven_gpg_sign_and_deploy(project, mainFile, null, pomFile);
					logger.info(LIFECYCLE, "Deploying main sources file");
					maven_gpg_sign_and_deploy(project, srcFile, "sources", null);
					logger.info(LIFECYCLE, "Deploying main javadoc file");
					maven_gpg_sign_and_deploy(project, javadocFile, "javadoc", null);

				} finally {
					main.close();
					src.close();
				}
			}
		} finally {
			original.close();
		}
		return true;
	}

	private void split(Jar original, Jar main, Jar src) {
		for (Map.Entry<String,Resource> e : original.getResources().entrySet()) {
			String path = e.getKey();
			if (path.startsWith("OSGI-OPT/src/")) {
				src.putResource(path.substring("OSGI-OPT/src/".length()), e.getValue());
			} else {
				main.putResource(path, e.getValue());
			}
		}
	}

	// gpg:sign-and-deploy-file \
	// -Durl=http://oss.sonatype.org/service/local/staging/deploy/maven2
	// \
	// -DrepositoryId=sonatype-nexus-staging \
	// -DupdateReleaseInfo=true \
	// -DpomFile=pom.xml \
	// -Dfile=/Ws/bnd/biz.aQute.bndlib/tmp/biz.aQute.bndlib.jar \
	// -Dpassphrase=a1k3v3t5x3

	private void maven_gpg_sign_and_deploy(Project b, File file, String classifier, File pomFile) throws Exception {
		Command command = new Command();
		command.setTrace();
		command.add(b.getProperty("mvn", "mvn"));
		command.add("gpg:sign-and-deploy-file", "-DreleaseInfo=true", "-DpomFile=pom.xml");
		command.add("-Dfile=" + file.getAbsolutePath());
		command.add("-DrepositoryId=" + repository);
		command.add("-Durl=" + url);
		optional(command, "passphrase", passphrase);
		optional(command, "keyname", keyname);
		optional(command, "homedir", homedir);
		optional(command, "classifier", classifier);
		optional(command, "pomFile", pomFile == null ? null : pomFile.getAbsolutePath());

		StringBuilder stdout = new StringBuilder();
		StringBuilder stderr = new StringBuilder();

		int result = command.execute(stdout, stderr);
		if (result != 0) {
			b.error("Maven deploy to %s failed to sign and transfer %s because %s", repository, file,
					"" + stdout + stderr);
		}
	}

	private void optional(Command command, @SuppressWarnings("unused") String key, String value) {
		if (value == null)
			return;

		command.add("-D=" + value);
	}

	private Jar javadoc(File tmp, Project b, Set<String> exports) throws Exception {
		Command command = new Command();

		command.add(b.getProperty("javadoc", "javadoc"));
		command.add("-d");
		command.add(tmp.getAbsolutePath());
		command.add("-sourcepath");
		command.add(Processor.join(b.getSourcePath(), File.pathSeparator));

		for (String packageName : exports) {
			command.add(packageName);
		}

		StringBuilder out = new StringBuilder();
		StringBuilder err = new StringBuilder();
		Command c = new Command();
		c.setTrace();
		int result = c.execute(out, err);
		if (result == 0) {
			Jar jar = new Jar(tmp);
			b.addClose(jar);
			return jar;
		}
		b.error("Error during execution of javadoc command: %s / %s", out, err);
		return null;
	}

	private File write(File base, Resource r, String fileName) throws Exception {
		File f = Processor.getFile(base, fileName);
		OutputStream out = new FileOutputStream(f);
		try {
			r.write(out);
		} finally {
			out.close();
		}
		return f;
	}

}
