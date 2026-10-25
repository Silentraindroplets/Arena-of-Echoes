package com.arena.model;

public class Enemy extends Combatant {

    private final String spriteKey;
    private final String colorHex;

    public int xpReward;
    public Skill special;
    public double specialChance = 0;
    public boolean healer = false;
    public int healCd = 0;

    public Enemy(String name, String spriteKey, String colorHex,
                 int hp, int atk, int def, int spd, int xpReward) {
        this.name = name;
        this.spriteKey = spriteKey;
        this.colorHex = colorHex;
        this.maxHp = hp; this.hp = hp;
        this.maxMp = 0;  this.mp = 0;
        this.atk = atk; this.def = def; this.mag = atk; this.spd = spd;
        this.xpReward = xpReward;
        this.role = "Enemy";
    }

    @Override public String spriteKey() { return spriteKey; }
    @Override public String colorHex() { return colorHex; }

    public Enemy special(Skill s, double chance) { this.special = s; this.specialChance = chance; return this; }
    public Enemy healer() { this.healer = true; return this; }

    public static Enemy goblin(double s) {
        Enemy e = new Enemy("Goblin", "goblin", "#4ade80",
                (int) (55 * s), (int) (13 * s), (int) (3 * s), 13, 20);
        e.special(new Skill("Frenzy", "Two quick strikes.", 0, 3, Skill.Type.ATTACK_ONE).mult(0.62), 0.30);
        return e;
    }

    public static Enemy orc(double s) {
        Enemy e = new Enemy("Orc", "orc", "#f97316",
                (int) (100 * s), (int) (19 * s), (int) (8 * s), 7, 40);
        e.special(new Skill("Crushing Blow", "A brutal overhead smash.", 0, 3, Skill.Type.ATTACK_ONE).mult(1.7).pierce(), 0.30);
        return e;
    }

    public static Enemy darkMage(double s) {
        Enemy e = new Enemy("Dark Mage", "darkmage", "#a855f7",
                (int) (75 * s), (int) (16 * s), (int) (4 * s), 10, 45);
        e.special(new Skill("Shadow Bolt", "Damage + poison.", 0, 2, Skill.Type.ATTACK_ONE).mult(1.3).pierce().poison(2), 0.40);
        return e.healer();
    }

    public static Enemy skeleton(double s) {
        Enemy e = new Enemy("Skeleton", "skeleton", "#e2e8f0",
                (int) (62 * s), (int) (15 * s), (int) (5 * s), 12, 25);
        e.special(new Skill("Bone Shot", "A hurled shard.", 0, 2, Skill.Type.ATTACK_ONE).mult(1.3).pierce(), 0.30);
        return e;
    }

    public static Enemy demon(double s) {
        Enemy e = new Enemy("Demon Lord", "demon", "#dc2626",
                (int) (220 * s), (int) (25 * s), (int) (12 * s), 9, 220);
        e.special(new Skill("Hellfire", "Engulfs all heroes in flame.", 0, 3, Skill.Type.ATTACK_ALL).mult(1.15).pierce().poison(2), 0.35);
        return e;
    }
}