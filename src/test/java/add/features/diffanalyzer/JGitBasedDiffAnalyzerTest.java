package add.features.diffanalyzer;

import add.main.Config;
import add.main.Constants;
import add.utils.TestUtils;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fermadeiral
 */
public class JGitBasedDiffAnalyzerTest {

    @Test
    public void testFixedVersionCreation() {
        Config config = TestUtils.setupConfig("time_12");
        String fixedSourceDirectoryPath = config.getBuggySourceDirectoryPath().replace("buggy", "fixed");
        JGitBasedDiffAnalyzer jgitDiffAnalyzer = new JGitBasedDiffAnalyzer(config.getDiffPath());
        Map<String, List<String>> fixedFiles = jgitDiffAnalyzer.getOriginalFiles(fixedSourceDirectoryPath);
        Map<String, List<String>> patchedFiles = jgitDiffAnalyzer.getPatchedFiles(config.getBuggySourceDirectoryPath());
        for (String path: fixedFiles.keySet()) {
            String buggyPath = path.replace(fixedSourceDirectoryPath, config.getBuggySourceDirectoryPath());
            assertEquals("Patched source version not valid for " + config.getBugId(), String.join(Constants.LINE_BREAK, fixedFiles.get(path)),
                    String.join(Constants.LINE_BREAK, patchedFiles.get(buggyPath)));
        }
    }

    @Test
    public void testMethodAnalyze() {
        Config config = TestUtils.setupConfig("closure_24");

        JGitBasedDiffAnalyzer jgitDiffAnalyzer = new JGitBasedDiffAnalyzer(config.getDiffPath());
        Changes changes = jgitDiffAnalyzer.analyze();

        assertEquals(1, jgitDiffAnalyzer.getNbFiles());
        assertEquals(4, changes.getCombinedChanges().size());
    }

    @Test
    public void testMethodGetOriginalFilesToReturnOneFile() {
        Config config = TestUtils.setupConfig("chart_1");

        String buggySourcePath = config.getBuggySourceDirectoryPath();
        List<String> expectedBuggyFilePaths = new ArrayList<>(
                Arrays.asList(buggySourcePath+"/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java"));

        JGitBasedDiffAnalyzer jgitDiffAnalyzer = new JGitBasedDiffAnalyzer(config.getDiffPath());
        Map<String, List<String>> buggyFiles = jgitDiffAnalyzer.getOriginalFiles(buggySourcePath);

        List<String> actualBuggyFilePaths = new ArrayList<>();
        Iterator<String> ite = buggyFiles.keySet().iterator();
        while (ite.hasNext()) {
            actualBuggyFilePaths.add(ite.next());
        }

        assertEquals(expectedBuggyFilePaths.size(), actualBuggyFilePaths.size());
        assertTrue(actualBuggyFilePaths.containsAll(expectedBuggyFilePaths));
    }

    @Test
    public void testMethodGetOriginalFilesToReturnMoreThanOneFile() {
        Config config = TestUtils.setupConfig("chart_18");

        String buggySourcePath = config.getBuggySourceDirectoryPath();
        List<String> expectedBuggyFilePaths = new ArrayList<>(
                Arrays.asList(buggySourcePath+"/org/jfree/data/DefaultKeyedValues.java",
                        buggySourcePath+"/org/jfree/data/DefaultKeyedValues2D.java"));

        JGitBasedDiffAnalyzer jgitDiffAnalyzer = new JGitBasedDiffAnalyzer(config.getDiffPath());
        Map<String, List<String>> buggyFiles = jgitDiffAnalyzer.getOriginalFiles(buggySourcePath);

        List<String> actualBuggyFilePaths = new ArrayList<>();
        Iterator<String> ite = buggyFiles.keySet().iterator();
        while (ite.hasNext()) {
            actualBuggyFilePaths.add(ite.next());
        }

        assertEquals(expectedBuggyFilePaths.size(), actualBuggyFilePaths.size());
        assertTrue(actualBuggyFilePaths.containsAll(expectedBuggyFilePaths));
    }

}
