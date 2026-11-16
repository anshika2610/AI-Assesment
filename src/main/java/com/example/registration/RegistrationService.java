package com.example.registration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class RegistrationService {
    private final CourseOffering course;
    private final Map<String, Learner> learners = new HashMap<>();
    private final Map<String, RegistrationStatus> registrations = new HashMap<>();
    private final Queue<String> waitlist = new ArrayDeque<>();

    public RegistrationService(CourseOffering course, List<Learner> learners) {
        this.course = course;
        for (Learner learner : learners) {
            if (learner != null && learner.getId() != null && !learner.getId().isBlank()) {
                this.learners.put(learner.getId(), learner);
            }
        }
    }

    public synchronized RegistrationResult enroll(String learnerId) {
        if (learnerId == null || learnerId.isBlank()) return RegistrationResult.INVALID_REQUEST;

        Learner learner = learners.get(learnerId);
        if (learner == null) return RegistrationResult.INVALID_REQUEST;

        RegistrationStatus current = registrations.get(learnerId);
        if (current == RegistrationStatus.ENROLLED) return RegistrationResult.ALREADY_ENROLLED;
        if (current == RegistrationStatus.WAITLISTED) return RegistrationResult.ALREADY_WAITLISTED;

        if (!learner.isEligible()) return RegistrationResult.PREREQUISITE_FAILED;

        if (getEnrolledCount() < course.getCapacity()) {
            registrations.put(learnerId, RegistrationStatus.ENROLLED);
            return RegistrationResult.ENROLLED;
        }

        registrations.put(learnerId, RegistrationStatus.WAITLISTED);
        waitlist.offer(learnerId);
        return RegistrationResult.WAITLISTED;
    }

    public synchronized RegistrationResult drop(String learnerId) {
        if (learnerId == null || learnerId.isBlank()) return RegistrationResult.INVALID_REQUEST;

        RegistrationStatus current = registrations.get(learnerId);
        if (current == null) return RegistrationResult.NOT_REGISTERED;

        if (current == RegistrationStatus.WAITLISTED) {
            registrations.remove(learnerId);
            waitlist.remove(learnerId);
            return RegistrationResult.REMOVED_FROM_WAITLIST;
        }

        registrations.remove(learnerId);
        promoteNextEligible();
        return RegistrationResult.DROPPED;
    }

    public synchronized boolean updateEligibility(String learnerId, boolean eligible) {
        Learner learner = learners.get(learnerId);
        if (learner == null) return false;
        learner.setPrerequisiteSatisfied(eligible);
        return true;
    }

    public synchronized List<String> getEnrolledLearners() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, RegistrationStatus> entry : registrations.entrySet()) {
            if (entry.getValue() == RegistrationStatus.ENROLLED) result.add(entry.getKey());
        }
        result.sort(String::compareTo);
        return result;
    }

    public synchronized List<String> getWaitlistedLearners() {
        return new ArrayList<>(waitlist);
    }

    public synchronized int getAvailableSeats() {
        return course.getCapacity() - getEnrolledCount();
    }

    public synchronized int getEnrolledCount() {
        int count = 0;
        for (RegistrationStatus status : registrations.values()) {
            if (status == RegistrationStatus.ENROLLED) count++;
        }
        return count;
    }

    private void promoteNextEligible() {
        if (getAvailableSeats() <= 0) return;

        Iterator<String> iterator = waitlist.iterator();
        while (iterator.hasNext()) {
            String learnerId = iterator.next();
            Learner learner = learners.get(learnerId);
            if (learner != null && learner.isEligible()) {
                iterator.remove();
                registrations.put(learnerId, RegistrationStatus.ENROLLED);
                return;
            }
        }
    }
}
