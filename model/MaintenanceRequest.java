package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MaintenanceRequest {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    private int requestId;

    private String title;
    private String description;
    private String location;

    private Priority priority;
    private Status status;

    private int assignedTechnicianId;

    private String technicianName;

    private LocalDateTime createdAt;

    // NEW FIELDS
    private LocalDate completionDate;

    private int estimatedHours;

    /** Username of the end-user who submitted this request (USER role). Null when submitted by admin. */
    private String submittedByUsername;


    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");


    public MaintenanceRequest() {

        priority = Priority.MEDIUM;
        status = Status.PENDING;

        createdAt = LocalDateTime.now();

        estimatedHours = 1;

    }


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }



    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }



    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }



    public int getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(int assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }



    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }



    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }



    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }



    public String getCreatedAtFormatted() {

        if (createdAt == null)
            return "";

        return createdAt.format(
                DATE_TIME_FORMATTER
        );

    }



    public String getCompletionDateFormatted() {

        if (completionDate == null)
            return "";

        return completionDate.format(
                DATE_FORMATTER
        );

    }


    public String getSubmittedByUsername() { return submittedByUsername; }
    public void setSubmittedByUsername(String submittedByUsername) { this.submittedByUsername = submittedByUsername; }

    public boolean isCompleted() {

        return status == Status.COMPLETED;

    }


    public boolean isPending() {

        return status == Status.PENDING;

    }


    public boolean isInProgress() {

        return status == Status.IN_PROGRESS;

    }


    @Override
    public String toString() {

        return title +
                " [" +
                priority +
                "]";

    }

}