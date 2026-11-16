# Course Registration

A Java implementation of one course offering with enrollment, dropping and a FIFO waitlist.

## Assumptions

- One course offering only.
- A learner is eligible when the required prerequisite is satisfied.
- Eligibility is checked during enrollment and again during waitlist promotion.
- An ineligible learner cannot enroll or join the waitlist.
- If a waitlisted learner becomes ineligible, they stay in the queue but are skipped during promotion.
- If they become eligible again, they can be promoted on a later seat opening.
- Changing eligibility does not automatically drop an already enrolled learner.
- A learner cannot be enrolled and waitlisted at the same time.
- A learner can appear on the waitlist only once.
- Dropping a waitlisted learner removes them and does not trigger promotion.
- Dropping an enrolled learner opens a seat and promotes the first currently eligible learner in FIFO order.
- State-changing methods are synchronized so the capacity check and state update happen together.
- Data is kept in memory. There is no database or external data source.


## Run

Requires Java 17 and Maven.

```bash
mvn test
mvn package
java -cp target/classes com.example.registration.CourseRegistrationCli
```

The CLI uses a few predefined learners, so a learner does not self-declare eligibility while enrolling.

## Test coverage

Tests cover available capacity, prerequisite failure, full-course waitlisting, duplicate enrollment and waitlisting, FIFO promotion, ineligible waitlist entries, no eligible promotion, waitlist removal, repeated drops, invalid requests, eligibility changes, and concurrent enrollment.


