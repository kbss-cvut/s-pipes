package cz.cvut.sforms.util;

import com.github.ledsoft.jopa.loader.BootAwareClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;

public class SPipesFormsClassPathScanner extends BootAwareClasspathScanner {
    private static final Logger LOG = LoggerFactory.getLogger(SPipesFormsClassPathScanner.class);

    protected void processElements(Enumeration<URL> urls, String scanPath) throws IOException {
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            if (visited.contains(url)) {
                continue;
            }
            visited.add(url);
            LOG.trace("Processing classpath element {}", url);
            if (isJar(url.toString())) {
                processJarFile(createJarFile(url));
            } else {
                processDirectory(new File(getUrlAsUri(url).getPath()), scanPath);
            }
        }
    }

    protected static JarFile createJarFile(URL elementUrl) throws IOException {
        final String jarPath = sanitizePath(elementUrl).replaceFirst("nested:/", "")
                .replaceFirst("file:", "").replaceFirst("[.]jar.*", JAR_FILE_SUFFIX);
        return new JarFile(jarPath);
    }
}
