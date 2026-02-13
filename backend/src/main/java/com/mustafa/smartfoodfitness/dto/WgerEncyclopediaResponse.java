package com.mustafa.smartfoodfitness.dto;

import java.util.List;

public class WgerEncyclopediaResponse {
    private List<WgerExerciseTile> chest;
    private List<WgerExerciseTile> back;
    private List<WgerExerciseTile> shoulders;
    private List<WgerExerciseTile> arms;
    private List<WgerExerciseTile> core;
    private List<WgerExerciseTile> legs;
    private List<WgerExerciseTile> cardio;

    public List<WgerExerciseTile> getChest() { return chest; }
    public void setChest(List<WgerExerciseTile> chest) { this.chest = chest; }

    public List<WgerExerciseTile> getBack() { return back; }
    public void setBack(List<WgerExerciseTile> back) { this.back = back; }

    public List<WgerExerciseTile> getShoulders() { return shoulders; }
    public void setShoulders(List<WgerExerciseTile> shoulders) { this.shoulders = shoulders; }

    public List<WgerExerciseTile> getArms() { return arms; }
    public void setArms(List<WgerExerciseTile> arms) { this.arms = arms; }

    public List<WgerExerciseTile> getCore() { return core; }
    public void setCore(List<WgerExerciseTile> core) { this.core = core; }

    public List<WgerExerciseTile> getLegs() { return legs; }
    public void setLegs(List<WgerExerciseTile> legs) { this.legs = legs; }

    public List<WgerExerciseTile> getCardio() { return cardio; }
    public void setCardio(List<WgerExerciseTile> cardio) { this.cardio = cardio; }
}
