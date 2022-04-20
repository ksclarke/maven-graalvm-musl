
package info.freelibrary.maven.graalvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Tests of our newly built container image.
 */
public class MgmImageFT {

    /**
     * The name of the image we want to test.
     */
    public static final DockerImageName DOCKER_IMAGE_NAME = DockerImageName.parse(getImageTag());

    /**
     * An instance of the container that we can run tests against. That container doesn't have a running process so we
     * add one to keep the container up while testing.
     */
    @ClassRule
    public static final GenericContainer<?> MGM_CONTAINER = new GenericContainer<>(DOCKER_IMAGE_NAME);

    /**
     * The expected MUSL GCC executable.
     */
    private static final String MUSL_GCC = "x86_64-linux-musl-gcc";

    /**
     * The expected location of the Maven directory.
     */
    private static final String MAVEN_HOME = "/opt/maven";

    /**
     * Tests the Maven executable.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testMavenExec() throws InterruptedException, IOException {
        final ExecResult result = which("mvn");

        assertEquals(0, result.getExitCode());
        assertEquals("/opt/maven/bin/mvn", result.getStdout().trim());
    }

    /**
     * Tests the UPX executable.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testUpxExec() throws InterruptedException, IOException {
        final ExecResult result = which("upx");

        assertEquals(0, result.getExitCode());
        assertEquals("/mgm_tools/bin/upx", result.getStdout().trim());
    }

    /**
     * Tests the musl-gcc executable.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testMuslGccExec() throws InterruptedException, IOException {
        final ExecResult result = which(MUSL_GCC);

        assertEquals(0, result.getExitCode());
        assertEquals("/mgm_tools/bin/x86_64-linux-musl-gcc", result.getStdout().trim());
    }

    /**
     * Tests the native-image executable.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testNativeImageExec() throws InterruptedException, IOException {
        final ExecResult result = which("native-image");

        assertEquals(0, result.getExitCode());
        assertEquals("/usr/bin/native-image", result.getStdout().trim());
    }

    /**
     * Tests the XZ executable.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testXzExec() throws InterruptedException, IOException {
        final ExecResult result = which("xz");

        assertEquals(0, result.getExitCode());
        assertEquals("/usr/bin/xz", result.getStdout().trim());
    }

    /**
     * Tests the presence of the cacerts.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testCacertsPresence() throws InterruptedException, IOException {
        assertTrue(fileExists("/etc/default/cacerts"));
    }

    /**
     * Tests the presence of libstdc++.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testLibstcdPresence() throws InterruptedException, IOException {
        assertTrue(fileExists("/mgm_tools/lib/libstdc++.a"));
    }

    /**
     * Tests the presence of ZLib.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testZlibPresence() throws InterruptedException, IOException {
        assertTrue(fileExists("/mgm_tools/lib/libz.a"));
    }

    /**
     * Tests the presence of the README.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testReadmePresence() throws InterruptedException, IOException {
        assertTrue(fileExists("/README.md"));
    }

    /**
     * Tests the presence of the MAVEN_HOME ENV.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testMavenHomeENV() throws InterruptedException, IOException {
        final ExecResult result = echo("MAVEN_HOME");

        assertEquals(0, result.getExitCode());
        assertEquals(MAVEN_HOME, result.getStdout().trim());
    }

    /**
     * Tests the presence of the M2_HOME ENV.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testM2HomeENV() throws InterruptedException, IOException {
        final ExecResult result = echo("M2_HOME");

        assertEquals(0, result.getExitCode());
        assertEquals(MAVEN_HOME, result.getStdout().trim());
    }

    /**
     * Tests the presence of the CC ENV.
     *
     * @throws InterruptedException If the container test is interrupted
     * @throws IOException If there is trouble reading from the container
     */
    @Test
    public final void testCcENV() throws InterruptedException, IOException {
        final ExecResult result = echo("CC");

        assertEquals(0, result.getExitCode());
        assertEquals(MUSL_GCC, result.getStdout().trim());
    }

    /**
     * Runs a which command inside the test container.
     *
     * @param aProgram A program to find using the which command
     * @return The result of the which
     * @throws InterruptedException If interactions with the container are interrupted
     * @throws IOException If there is trouble reading from the container
     */
    private ExecResult which(final String aProgram) throws InterruptedException, IOException {
        return MGM_CONTAINER.execInContainer("which", aProgram);
    }

    /**
     * Runs an echo command inside the test container to get the value of the supplied ENV property.
     *
     * @param aEnvProperty An environmental property
     * @return The result of the which
     * @throws InterruptedException If interactions with the container are interrupted
     * @throws IOException If there is trouble reading from the container
     */
    private ExecResult echo(final String aEnvProperty) throws InterruptedException, IOException {
        return MGM_CONTAINER.execInContainer("bash", "-c", "echo $" + aEnvProperty);
    }

    /**
     * Tests whether the supplied file path exists within the container.
     *
     * @param aFilePath A path to a file
     * @return True if the file exists; else, false
     * @throws InterruptedException If interactions with the container are interrupted
     * @throws IOException If there is trouble reading from the container
     */
    private boolean fileExists(final String aFilePath) throws InterruptedException, IOException {
        return MGM_CONTAINER.execInContainer("ls", aFilePath).getExitCode() == 0;
    }

    /**
     * Gets the test image's Docker tag, which varies between snapshot and tagged builds.
     *
     * @return The name of the container to spin up
     */
    private static String getImageTag() {
        String account = System.getenv("DOCKER_ACCOUNT");
        String version = System.getenv("IMAGE_VERSION");

        if (account == null || account.trim().length() == 0) {
            account = "";
        }

        if (version == null || version.contains("SNAPSHOT")) {
            version = "latest";
        }

        return account + System.getenv("IMAGE_NAME") + ":" + version;
    }
}
