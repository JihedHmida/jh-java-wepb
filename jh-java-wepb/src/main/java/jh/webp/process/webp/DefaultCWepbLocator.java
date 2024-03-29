package jh.webp.process.webp;

import jh.webp.process.ProcessLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DefaultCWepbLocator implements ProcessLocator {
    private final static String LIB_NAME = "cwebp-";
    private final static String LIB_PATH = "jh/webp/nativebin/";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCWepbLocator.class);
    private final String path;


    public DefaultCWepbLocator() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("windows");
        boolean isMac = os.contains("mac");

        LOG.debug("Os name is <{}> isWindows: {} isMac: {}", os, isWindows, isMac);
        File dirFolder = new File(System.getProperty("java.io.tmpdir"), "jave/");

        if (!dirFolder.exists()) {
            LOG.debug("Creating jave temp folder to place executables in <{}>", dirFolder.getAbsolutePath());
            dirFolder.mkdirs();
        } else {
            LOG.debug("Jave temp folder exists in <{}>", dirFolder.getAbsolutePath());
        }

        String suffix = isWindows ? ".exe" : (isMac ? "-osx" : "");
        String arch = System.getProperty("os.arch");
        File cwebpFile = new File(dirFolder, LIB_NAME + arch + suffix);

        LOG.debug("Executable path: {}", cwebpFile.getAbsolutePath());

        synchronized (DefaultCWepbLocator.class) {
            if (cwebpFile.exists()) {
                LOG.debug("Executable exists in <{}>", cwebpFile.getAbsolutePath());
            } else {
                LOG.debug("Need to copy executable to <{}>", cwebpFile.getAbsolutePath());
                copyFile(LIB_NAME + arch + suffix, cwebpFile);
            }

            if (!isWindows) {
                try {
                    Runtime.getRuntime().exec(new String[]{"/bin/chmod", "755", cwebpFile.getAbsolutePath()});
                } catch (IOException e) {
                    LOG.error("Error setting executable via chmod", e);
                }
            }

        }


        this.path = cwebpFile.getAbsolutePath();

        if (cwebpFile.exists()) {
            LOG.debug("cwebp executable found: {}", path);
        } else {
            LOG.error("cwebp executable NOT found: {}", path);
        }


    }

    private void copyFile(String path, File dest) {

        String resourceName = "nativebin/" + path;

        try {

            LOG.debug("Copy from resource <{}> to target <{}>", resourceName, dest.getAbsolutePath());

            InputStream is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                // Use this for Java 9+ only if required
                resourceName = LIB_PATH + path;
                LOG.debug(
                        "Alternative copy from SystemResourceAsStream <{}> to target <{}>",
                        resourceName,
                        dest.getAbsolutePath());
                is = ClassLoader.getSystemResourceAsStream(resourceName);


            }

            if (is == null) {
                // Use this for spring boot with different class loaders
                resourceName = LIB_PATH + path;
                LOG.debug(
                        "Alternative copy from Thread.currentThread().getContextClassLoader() <{}> to target <{}>",
                        resourceName,
                        dest.getAbsolutePath());
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                is = classloader.getResourceAsStream(resourceName);
            }

            if (is != null) {
                if (copy(is, dest.getAbsolutePath())) {
                    if (dest.exists()) {
                        LOG.debug("Target <{}> exists", dest.getAbsolutePath());
                    } else {
                        LOG.error("Target <{}> does not exist", dest.getAbsolutePath());
                    }
                } else {
                    LOG.error("Copy resource to target <{}> failed", dest.getAbsolutePath());
                }
                try {
                    is.close();
                } catch (IOException ioex) {
                    LOG.warn("Error in closing input stream", ioex);
                }
            } else {
                LOG.error("Could not find cwebp platform executable in resources for <{}>", resourceName);
            }
        } catch (NullPointerException ex) {
            LOG.error(
                    "Could not find cwebp executable for {} is the correct platform jar included?",
                    resourceName);
            throw ex;

        }

    }

    private boolean copy(InputStream source, String destination) {
        boolean success = true;

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOG.error("Cannot write file " + destination, ex);
            success = false;
        }

        return success;
    }

    public String getExecutablePath() {
        return path;
    }
}
