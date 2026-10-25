    package com.arena.model;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public abstract class Combatant {

        public String name;
        public String role = "Fighter";
        public int maxHp, hp;
        public int maxMp, mp;
        public int atk, def, mag, spd;

        public int level = 1;
        public int xp = 0;
        public int xpNext = 50;

        public boolean defending = false;
        public boolean alive = true;
        public int poison = 0;

        public List<Skill> skills = new ArrayList<>();
        public Map<Skill, Integer> cooldowns = new HashMap<>();

        public abstract String spriteKey();
        public abstract String colorHex();

        public boolean isAlive() { return alive && hp > 0; }
        public double hpRatio()  { return Math.max(0, (double) hp / maxHp); }
        public double mpRatio()  { return maxMp <= 0 ? 0 : Math.max(0, (double) mp / maxMp); }
        public double xpRatio()  { return Math.min(1.0, (double) xp / xpNext); }

        public void takeDamage(int dmg) {
            hp = Math.max(0, hp - dmg);
            if (hp <= 0) alive = false;
        }
        public void heal(int amount)       { hp = Math.min(maxHp, hp + amount); }
        public void restoreMp(int amount)  { mp = Math.min(maxMp, mp + amount); }

        public int cooldownOf(Skill s) { return cooldowns.getOrDefault(s, 0); }

        public void tickCooldowns() {
            cooldowns.replaceAll((k, v) -> Math.max(0, v - 1));
        }

        public int grantXp(int amount) {
            xp += amount;
            int levelsGained = 0;
            while (xp >= xpNext) {
                xp -= xpNext;
                levelUp();
                levelsGained++;
            }
            return levelsGained;
        }

        protected void levelUp() {
            level++;
            maxHp += 15;  hp = maxHp;
            maxMp += 6;   mp = maxMp;
            atk   += 2;
            def   += 1;
            mag   += 2;
            spd   += 1;
            xpNext = (int) (xpNext * 1.45);
        }
    }