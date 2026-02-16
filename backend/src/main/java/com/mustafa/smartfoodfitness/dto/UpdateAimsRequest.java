package com.mustafa.smartfoodfitness.dto;

import java.util.Set;

public class UpdateAimsRequest {
    private Set<String> aims;

    public Set<String> getAims() { return aims; }
    public void setAims(Set<String> aims) { this.aims = aims; }
}
