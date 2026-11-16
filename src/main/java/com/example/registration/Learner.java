package com.example.registration;

public class Learner {
    private final String id;
    private boolean prerequisiteSatisfied;

    public Learner(String id, boolean prerequisiteSatisfied) {
        this.id = id;
        this.prerequisiteSatisfied = prerequisiteSatisfied;
    }

    public String getId() { return id; }
    public boolean isEligible() { return prerequisiteSatisfied; }
    public void setPrerequisiteSatisfied(boolean value) { prerequisiteSatisfied = value; }
}
