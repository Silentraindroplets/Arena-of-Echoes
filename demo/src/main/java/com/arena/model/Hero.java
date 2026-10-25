package com.arena.model;

public class Hero extends Combatant {

    public static final int ULT_MAX = 100;

    private final String spriteKey;
    private final String colorHex;

    public int potions = 3;
    public int ethers  = 1;
    public int ultCharge = 0;
    public Skill ultimate;

    public Hero(String name, String spriteKey, String colorHex,
                int hp, int mp, int atk, int def, int mag, int spd) {
        this.name = name;
        this.spriteKey = spriteKey;
        this.colorHex = colorHex;
        this.maxHp = hp; this.hp = hp;
        this.maxMp = mp; this.mp = mp;
        this.atk = atk; this.def = def; this.mag = mag; this.spd = spd;
    }

    @Override public String spriteKey() { return spriteKey; }
    @Override public String colorHex() { return colorHex; }

    public void grantUltCharge(int amount) {
        if (ultimate == null) return;
        ultCharge = Math.min(ULT_MAX, ultCharge + amount);
    }
    public boolean hasUlt() { return ultimate != null && ultCharge >= ULT_MAX; }
    public double ultRatio() { return ultimate == null ? 0 : (double) ultCharge / ULT_MAX; }
    public void spendUlt() { ultCharge = 0; }

    public static Hero knight() {
        Hero h = new Hero("Knight", "knight", "#7c5cff", 130, 45, 20, 12, 6, 11);
        h.role = "Tank";
        h.skills.add(new Skill("Attack", "Basic melee strike.", 0, 0, Skill.Type.ATTACK_ONE).mult(1.0));
        h.skills.add(new Skill("Power Strike", "Heavy blow that pierces armor.", 12, 0, Skill.Type.ATTACK_ONE).mult(1.9).pierce());
        h.skills.add(new Skill("Whirlwind", "Hits all enemies with reduced damage.", 18, 2, Skill.Type.ATTACK_ALL).mult(1.1).pierce());
        h.skills.add(new Skill("Guard", "Halve incoming damage next turn. Restores 10 MP.", 0, 0, Skill.Type.GUARD));
        h.ultimate = new Skill("Oathkeeper's Vow",
                "A devastating strike that also mends the Knight's wounds.",
                0, 0, Skill.Type.ULTIMATE).mult(3.2).pierce().heal(45);
        return h;
    }

    public static Hero mage() {
        Hero h = new Hero("Mage", "mage", "#38bdf8", 90, 80, 8, 6, 24, 10);
        h.role = "Caster";
        h.skills.add(new Skill("Staff Strike", "Basic melee strike.", 0, 0, Skill.Type.ATTACK_ONE).mult(1.0));
        h.skills.add(new Skill("Fire Bolt", "A searing bolt of flame.", 10, 0, Skill.Type.ATTACK_ONE).mult(1.6).pierce());
        h.skills.add(new Skill("Ice Shard", "Frozen shards pierce all enemies.", 18, 2, Skill.Type.ATTACK_ALL).mult(1.2).pierce());
        h.skills.add(new Skill("Heal", "Restore 45 HP to yourself.", 12, 1, Skill.Type.HEAL_SELF).heal(45));
        h.ultimate = new Skill("Meteor Cascade",
                "Calls down burning meteors on every foe.",
                0, 0, Skill.Type.ULTIMATE).mult(2.2).pierce().poison(2);
        return h;
    }
}