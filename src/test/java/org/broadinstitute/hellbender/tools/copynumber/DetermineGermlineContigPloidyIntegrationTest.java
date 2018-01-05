package org.broadinstitute.hellbender.tools.copynumber;

import org.broadinstitute.hellbender.CommandLineProgramTest;
import org.broadinstitute.hellbender.cmdline.StandardArgumentDefinitions;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.copynumber.arguments.CopyNumberStandardArgument;
import org.broadinstitute.hellbender.utils.test.ArgumentsBuilder;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Integration tests for {@link DetermineGermlineContigPloidy}.
 *
 * The test runs the CLI tool in Cohort and Case run-modes on a small simulated data.
 *
 */
public final class DetermineGermlineContigPloidyIntegrationTest extends CommandLineProgramTest {
    private static final String gCNVSimDataDir = toolsTestDir + "copynumber/gcnv-sim-data/";
    private static final File testContigPloidyPriorFile =
            new File(gCNVSimDataDir, "contig_ploidy_prior.tsv");
    private static final File[] testCountFiles = IntStream.range(0, 20)
            .mapToObj(n -> new File(gCNVSimDataDir, String.format("SAMPLE_%03d_counts.tsv", n)))
            .toArray(File[]::new);
    private final File tempOutputDir = createTempDir("test-ploidy");

    @Test(groups = {"python"})
    public void testCohort() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(testCountFiles).forEach(argsBuilder::addInput);
        argsBuilder.addFileArgument(DetermineGermlineContigPloidy.CONTIG_PLOIDY_PRIORS_FILE_LONG_NAME,
                testContigPloidyPriorFile)
                .addArgument(StandardArgumentDefinitions.OUTPUT_LONG_NAME, tempOutputDir.getAbsolutePath())
                .addArgument(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-ploidy-cohort")
                .addArgument(StandardArgumentDefinitions.VERBOSITY_NAME, "DEBUG");
        runCommandLine(argsBuilder);
    }

    @Test(groups = {"python"}, expectedExceptions = UserException.BadInput.class)
    public void testCohortWithoutContigPloidyPriors() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(testCountFiles).forEach(argsBuilder::addInput);
        argsBuilder.addArgument(StandardArgumentDefinitions.OUTPUT_LONG_NAME, tempOutputDir.getAbsolutePath())
                .addArgument(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-ploidy-cohort")
                .addArgument(StandardArgumentDefinitions.VERBOSITY_NAME, "DEBUG");
        runCommandLine(argsBuilder);
    }

    @Test(groups = {"python"}, expectedExceptions = UserException.BadInput.class)
    public void testCohortWithSingleSample() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        argsBuilder.addInput(testCountFiles[0]);
        argsBuilder.addArgument(StandardArgumentDefinitions.OUTPUT_LONG_NAME, tempOutputDir.getAbsolutePath())
                .addArgument(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-ploidy-cohort")
                .addArgument(StandardArgumentDefinitions.VERBOSITY_NAME, "DEBUG");
        runCommandLine(argsBuilder);
    }

    @Test(groups = {"python"}, expectedExceptions = IllegalArgumentException.class)
    public void testCohortDuplicateFiles() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(testCountFiles).forEach(argsBuilder::addInput);
        argsBuilder.addInput(testCountFiles[0]);  //duplicate
        argsBuilder.addFileArgument(DetermineGermlineContigPloidy.CONTIG_PLOIDY_PRIORS_FILE_LONG_NAME,
                testContigPloidyPriorFile)
                .addArgument(StandardArgumentDefinitions.OUTPUT_LONG_NAME, tempOutputDir.getAbsolutePath())
                .addArgument(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-ploidy-cohort")
                .addArgument(StandardArgumentDefinitions.VERBOSITY_NAME, "DEBUG");
        runCommandLine(argsBuilder);
    }

    /**
     * Use the first 5 samples as case and use the contig-ploidy model generated by {@link #testCohort()}
     */
    @Test(groups = {"python"}, dependsOnMethods = "testCohort")
    public void testCase() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(testCountFiles, 0, 5).forEach(argsBuilder::addInput);
        argsBuilder.addArgument(StandardArgumentDefinitions.OUTPUT_LONG_NAME, tempOutputDir.getAbsolutePath())
                .addArgument(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-ploidy-case")
                .addArgument(CopyNumberStandardArgument.MODEL_LONG_NAME,
                        new File(tempOutputDir, "test-ploidy-cohort-model").getAbsolutePath())
                .addArgument(StandardArgumentDefinitions.VERBOSITY_NAME, "DEBUG");
        runCommandLine(argsBuilder);
    }

    @Test(groups = {"python"}, dependsOnMethods = "testCohort", expectedExceptions = UserException.BadInput.class)
    public void testCaseWithContigPloidyPrior() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        Arrays.stream(testCountFiles, 0, 5).forEach(argsBuilder::addInput);
        argsBuilder.addArgument(StandardArgumentDefinitions.OUTPUT_LONG_NAME, tempOutputDir.getAbsolutePath())
                .addFileArgument(DetermineGermlineContigPloidy.CONTIG_PLOIDY_PRIORS_FILE_LONG_NAME,
                        testContigPloidyPriorFile)
                .addArgument(CopyNumberStandardArgument.OUTPUT_PREFIX_LONG_NAME, "test-ploidy-case")
                .addArgument(CopyNumberStandardArgument.MODEL_LONG_NAME,
                        new File(tempOutputDir, "test-ploidy-cohort-model").getAbsolutePath())
                .addArgument(StandardArgumentDefinitions.VERBOSITY_NAME, "DEBUG");
        runCommandLine(argsBuilder);
    }
}