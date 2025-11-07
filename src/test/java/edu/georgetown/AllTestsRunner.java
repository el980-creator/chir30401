package edu.georgetown;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Main test runner for all Chirpy JUnit tests.
 * Executes all test classes and provides summary reporting.
 */
public class AllTestsRunner {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("           CHIRPY 2.0 TEST SUITE");
        System.out.println("=================================================");
        System.out.println();
        
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectPackage("edu.georgetown"))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        TestExecutionSummary summary = listener.getSummary();
        
        System.out.println();
        System.out.println("=================================================");
        System.out.println("                TEST SUMMARY");
        System.out.println("=================================================");
        System.out.println("Tests found: " + summary.getTestsFoundCount());
        System.out.println("Tests started: " + summary.getTestsStartedCount());
        System.out.println("Tests successful: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        System.out.println("Tests skipped: " + summary.getTestsSkippedCount());
        System.out.println("Total time: " + summary.getTotalFailureCount() + " ms");
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println();
            System.out.println("FAILED TESTS:");
            summary.getFailures().forEach(failure -> {
                System.out.println("- " + failure.getTestIdentifier().getDisplayName());
                System.out.println("  " + failure.getException().getMessage());
            });
        }
        
        System.out.println();
        double successRate = ((double) summary.getTestsSucceededCount() / summary.getTestsFoundCount()) * 100;
        System.out.printf("SUCCESS RATE: %.1f%%\n", successRate);
        
        if (successRate >= 90.0) {
            System.out.println("✅ EXCELLENT: Test coverage meets 90%+ requirement!");
        } else if (successRate >= 80.0) {
            System.out.println("⚠️  GOOD: Test coverage above 80%");
        } else {
            System.out.println("❌ NEEDS IMPROVEMENT: Test coverage below 80%");
        }
        
        System.out.println("=================================================");
        
        // Exit with appropriate code
        System.exit(summary.getTestsFailedCount() > 0 ? 1 : 0);
    }
}