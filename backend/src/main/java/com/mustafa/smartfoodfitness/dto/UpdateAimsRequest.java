package com.mustafa.smartfoodfitness.dto;
// Define the UpdateAimsRequest DTO with a field for a set of aim strings, along with getter and setter methods for the aims field to facilitate data transfer of user aim update information between the backend and frontend of the application
import java.util.Set;

public class UpdateAimsRequest {
    private Set<String> aims;

    public Set<String> getAims() { 
        return aims; 
    }
    public void setAims(Set<String> aims) { 
        this.aims = aims; 
    }
}
