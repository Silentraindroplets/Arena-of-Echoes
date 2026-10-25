package com.arena.model;

public class Skill {

    public enum Type { ATTACK_ONE, ATTACK_ALL, HEAL_SELF, GUARD, ULTIMATE }

    public String name;
    public String description;
    public int mpCost;
    public int cooldown;
    public Type type;
    public double multiplier = 1.0;
    public boolean pierce     = false;
    public int healAmount     = 0;
    public int poisonTurns    = 0;

    public Skill(String name, String description, int mpCost, int cooldown, Type type) {
        this.name = name;
        this.description = description;
        this.mpCost = mpCost;
        this.cooldown = cooldown;
        this.type = type;
    }
    public Skill mult(double m)      { this.multiplier = m;    return this; }
    public Skill pierce()            { this.pierce = true;      return this; }
    public Skill heal(int amount)    { this.healAmount = amount; return this; }
    public Skill poison(int turns)   { this.poisonTurns = turns; return this; }
}