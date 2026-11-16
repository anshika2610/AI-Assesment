package com.example.registration;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest {
    private RegistrationService service(int capacity, Learner... learners) {
        return new RegistrationService(new CourseOffering("JAVA-101", capacity), List.of(learners));
    }

    @Test void enrollsWhenSeatIsAvailable() {
        RegistrationService s = service(2, new Learner("L1", true));
        assertEquals(RegistrationResult.ENROLLED, s.enroll("L1"));
        assertEquals(List.of("L1"), s.getEnrolledLearners());
        assertEquals(1, s.getAvailableSeats());
    }

    @Test void rejectsLearnerWithoutPrerequisite() {
        RegistrationService s = service(2, new Learner("L1", false));
        assertEquals(RegistrationResult.PREREQUISITE_FAILED, s.enroll("L1"));
        assertTrue(s.getEnrolledLearners().isEmpty());
        assertTrue(s.getWaitlistedLearners().isEmpty());
    }

    @Test void fullCourseAddsEligibleLearnerToWaitlist() {
        RegistrationService s = service(1, new Learner("L1", true), new Learner("L2", true));
        s.enroll("L1");
        assertEquals(RegistrationResult.WAITLISTED, s.enroll("L2"));
        assertEquals(List.of("L2"), s.getWaitlistedLearners());
    }

    @Test void duplicateEnrollmentIsRejected() {
        RegistrationService s = service(2, new Learner("L1", true));
        s.enroll("L1");
        assertEquals(RegistrationResult.ALREADY_ENROLLED, s.enroll("L1"));
        assertEquals(1, s.getEnrolledCount());
    }

    @Test void duplicateWaitlistEntryIsRejected() {
        RegistrationService s = service(1, new Learner("L1", true), new Learner("L2", true));
        s.enroll("L1");
        s.enroll("L2");
        assertEquals(RegistrationResult.ALREADY_WAITLISTED, s.enroll("L2"));
        assertEquals(List.of("L2"), s.getWaitlistedLearners());
    }

    @Test void promotesInFifoOrder() {
        RegistrationService s = service(1, new Learner("L1", true), new Learner("L2", true), new Learner("L3", true));
        s.enroll("L1"); s.enroll("L2"); s.enroll("L3");
        s.drop("L1");
        assertEquals(List.of("L2"), s.getEnrolledLearners());
        assertEquals(List.of("L3"), s.getWaitlistedLearners());
    }

    @Test void skipsIneligibleWaitlistedLearnerAndKeepsThem() {
        Learner l1 = new Learner("L1", true);
        Learner l2 = new Learner("L2", true);
        Learner l3 = new Learner("L3", true);
        RegistrationService s = service(1, l1, l2, l3);
        s.enroll("L1"); s.enroll("L2"); s.enroll("L3");
        s.updateEligibility("L2", false);
        s.drop("L1");
        assertEquals(List.of("L3"), s.getEnrolledLearners());
        assertEquals(List.of("L2"), s.getWaitlistedLearners());
    }

    @Test void seatStaysAvailableWhenNoWaitlistedLearnerIsEligible() {
        Learner l1 = new Learner("L1", true);
        Learner l2 = new Learner("L2", true);
        RegistrationService s = service(1, l1, l2);
        s.enroll("L1"); s.enroll("L2");
        s.updateEligibility("L2", false);
        s.drop("L1");
        assertEquals(1, s.getAvailableSeats());
        assertTrue(s.getEnrolledLearners().isEmpty());
        assertEquals(List.of("L2"), s.getWaitlistedLearners());
    }

    @Test void dropsWaitlistedLearnerWithoutPromotion() {
        RegistrationService s = service(1, new Learner("L1", true), new Learner("L2", true));
        s.enroll("L1"); s.enroll("L2");
        assertEquals(RegistrationResult.REMOVED_FROM_WAITLIST, s.drop("L2"));
        assertTrue(s.getWaitlistedLearners().isEmpty());
        assertEquals(0, s.getAvailableSeats());
    }

    @Test void repeatedDropIsSafe() {
        RegistrationService s = service(1, new Learner("L1", true));
        s.enroll("L1");
        assertEquals(RegistrationResult.DROPPED, s.drop("L1"));
        assertEquals(RegistrationResult.NOT_REGISTERED, s.drop("L1"));
        assertEquals(1, s.getAvailableSeats());
    }



    @Test void eligibilityCanChangeWhileWaitlisted() {
        Learner l1 = new Learner("L1", true);
        Learner l2 = new Learner("L2", true);
        RegistrationService s = service(1, l1, l2);
        s.enroll("L1"); s.enroll("L2");
        s.updateEligibility("L2", false);
        s.updateEligibility("L2", true);
        s.drop("L1");
        assertEquals(List.of("L2"), s.getEnrolledLearners());
        assertTrue(s.getWaitlistedLearners().isEmpty());
    }

    @Test void concurrentEnrollmentsNeverExceedCapacity() throws Exception {
        int capacity = 5;
        List<Learner> learners = new ArrayList<>();
        for (int i = 1; i <= 20; i++) learners.add(new Learner("L" + i, true));
        RegistrationService s = service(capacity, learners.toArray(new Learner[0]));
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        for (Learner learner : learners) {
            executor.submit(() -> {
                try { start.await(); s.enroll(learner.getId()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(capacity, s.getEnrolledCount());
        assertEquals(15, s.getWaitlistedLearners().size());
        assertEquals(20, s.getEnrolledCount() + s.getWaitlistedLearners().size());
    }
}
