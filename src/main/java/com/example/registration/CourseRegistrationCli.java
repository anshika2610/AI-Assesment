package com.example.registration;

import java.util.List;
import java.util.Scanner;

public class CourseRegistrationCli {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter course capacity: ");
        int capacity;
        try {
            capacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid capacity.");
            return;
        }

        CourseOffering course = new CourseOffering("JAVA-101", capacity);
        List<Learner> learners = List.of(
                new Learner("L1", true),
                new Learner("L2", true),
                new Learner("L3", false),
                new Learner("L4", true),
                new Learner("L5", true)
        );
        RegistrationService service = new RegistrationService(course, learners);

        System.out.println("Available learners: L1, L2, L3, L4, L5");

        while (true) {
            System.out.println("\n1. Enroll learner");
            System.out.println("2. Drop learner");
            System.out.println("3. Update learner eligibility");
            System.out.println("4. Show status");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Learner id: ");
                System.out.println("Result: " + service.enroll(scanner.nextLine().trim()));
            } else if (choice.equals("2")) {
                System.out.print("Learner id: ");
                System.out.println("Result: " + service.drop(scanner.nextLine().trim()));
            } else if (choice.equals("3")) {
                System.out.print("Learner id: ");
                String id = scanner.nextLine().trim();
                System.out.print("Prerequisite satisfied (true/false): ");
                boolean eligible = Boolean.parseBoolean(scanner.nextLine().trim());
                System.out.println("Updated: " + service.updateEligibility(id, eligible));
            } else if (choice.equals("4")) {
                System.out.println("Enrolled: " + service.getEnrolledLearners());
                System.out.println("Waitlist: " + service.getWaitlistedLearners());
                System.out.println("Available seats: " + service.getAvailableSeats());
            } else if (choice.equals("5")) {
                System.out.println("Goodbye.");
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}
