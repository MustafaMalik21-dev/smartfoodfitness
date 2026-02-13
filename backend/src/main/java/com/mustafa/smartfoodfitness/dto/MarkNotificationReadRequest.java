package com.mustafa.smartfoodfitness.dto;
// Define the MarkNotificationReadRequest DTO with a field for read status, along with getter and setter methods for the field to facilitate data transfer of notification read status update information between the backend and frontend of the application
import jakarta.validation.constraints.NotNull;

public class MarkNotificationReadRequest {

    @NotNull
    private Boolean isRead;

    public Boolean getIsRead() { 
        return isRead; 
    }
    public void setIsRead(Boolean isRead) { 
        this.isRead = isRead; 
    }
}
