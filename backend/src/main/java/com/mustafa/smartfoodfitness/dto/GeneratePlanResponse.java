package com.mustafa.smartfoodfitness.dto;

/** Raw model output for AI plan generation; the client parses the JSON plan. */
public class GeneratePlanResponse {
    private String text;

    public GeneratePlanResponse() {}

    public GeneratePlanResponse(String text) { this.text = text; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
