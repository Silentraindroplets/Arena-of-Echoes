package com.arena;

public class StoryLine {
    public final String speaker;
    public final String text;
    public final String spriteKey;
    public final String colorHex;
    public final boolean dramatic;

    public StoryLine(String speaker, String text, String spriteKey, String colorHex) {
        this(speaker, text, spriteKey, colorHex, false);
    }

    public StoryLine(String speaker, String text, String spriteKey, String colorHex, boolean dramatic) {
        this.speaker   = speaker;
        this.text      = text;
        this.spriteKey = spriteKey;
        this.colorHex  = colorHex;
        this.dramatic  = dramatic;
    }
}