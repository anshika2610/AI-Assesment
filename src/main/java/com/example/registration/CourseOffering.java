package com.example.registration;

public class CourseOffering {
    private final String courseId;
    private final int capacity;

    public CourseOffering(String courseId, int capacity) {
        if (courseId == null || courseId.isBlank()) throw new IllegalArgumentException("Course id cannot be blank");
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1");
        this.courseId = courseId;
        this.capacity = capacity;
    }

    public String getCourseId() { return courseId; }
    public int getCapacity() { return capacity; }
}
