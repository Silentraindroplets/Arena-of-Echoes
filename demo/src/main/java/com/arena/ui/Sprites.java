package com.arena.ui;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/** Pixel-art patterns. Each pattern is a String[] grid; each char maps to a palette color. */
public class Sprites {

    // --- Patterns (14 wide x 16 tall) ---
    // Palette letters:
    //   .  transparent   D  dark slate   S  silver      K  near-black (eyes/visor)
    //   B  blue          R  red          G  green       W  cream/white
    //   P  purple        Y  gold         O  orange
    //   V  violet        C  cyan         A  amber/gold

    // ----------------------------------------------------------------
    //  PLAYER 1 — KNIGHT
    //  Bulky, silver plate armour, wide pauldrons, blue tabard with gold emblem,
    //  red plume on the helm, dark visor slit, gold boots.
    // ----------------------------------------------------------------
    public static final String[] KNIGHT = {
        "....RRRR......",   // red plume
        "....DSSD......",   // helm crown
        "...DSSSSSD....",   // helm
        "..DSSSSSSSD...",   // helm widest
        "..DSKKKKKSD...",   // dark visor slit
        "..DSSSSSSSD...",   // helm
        "...DSSSSSD....",   // helm base
        ".VVVVVVVVVVVV.",   // wide pauldrons
        ".VVVVSSSSVVVV.",   // shoulder + torso
        ".VVVSSSSSSVVV.",   // torso
        ".VVSSYYYYSSVV.",   // gold chest emblem
        ".VVVSSSSSSVVV.",   // torso
        "..VVSSSSSSVV..",   // lower torso
        "...DD....DD...",   // legs
        "...DD....DD...",
        "..AAA....AAA.."    // gold boots
    };

    // ----------------------------------------------------------------
    //  PLAYER 2 — MAGE
    //  Slender, tall pointed wizard hat with a golden gem, cream face,
    //  purple robe with cyan magic trim, boots peeking under the hem.
    // ----------------------------------------------------------------
    public static final String[] MAGE = {
        "......YY......",   // gold gem
        "......PP......",   // hat tip
        ".....PPPP.....",   // hat
        "....PPPPPP....",   // hat
        "...PPPPPPPP...",   // hat
        "..PPPPPPPPPP..",   // wide brim
        "..WWWWWWWWWW..",   // face
        "..WWKWWWWKWW..",   // eyes
        "..WWWWWWWWWW..",   // face
        "...WWWWWWWW...",   // chin
        "..CCPPPPPPCC..",   // robe with cyan shoulder trim
        ".CCCPPPPPPCCC.",   // robe
        ".CCCPPPPPPCCC.",   // robe
        ".CCPPPPPPPPCC.",   // lower robe
        "..PPPPPPPPPP..",   // robe hem
        "..DD......DD.."    // boots
    };

    // ----------------------------------------------------------------
    //  ENEMIES (unchanged)
    // ----------------------------------------------------------------

    public static final String[] GOBLIN = {
        "..............",
        "....GGGGGG....",
        "...GGGGGGGG...",
        "..GGKGGGGKGG..",
        "..GGGGGGGGGG..",
        "..GGDDDDDDGG..",
        "...GGGGGGGG...",
        "....GGGGGG....",
        "...GGGGGGGG...",
        "..GGBBBBBBGG..",
        "..GGBBBBBBGG..",
        "..GGGGGGGGGG..",
        "...GG....GG...",
        "...GG....GG...",
        "...DD....DD...",
        "...DD....DD..."
    };

    public static final String[] ORC = {
        "..............",
        "...GGGGGGGG...",
        "..GGGGGGGGGG..",
        "..GGKGGGGKGG..",
        "..GGGGGGGGGG..",
        "..GWDDDDDDWG..",
        "...GGGGGGGG...",
        "..RRRRRRRRRR..",
        ".RRRRRRRRRRRR.",
        ".RRRRRRRRRRRR.",
        ".RRRRRRRRRRRR.",
        ".RRRRRRRRRRRR.",
        "..RRRRRRRRRR..",
        "...GG....GG...",
        "...GG....GG...",
        "...DD....DD..."
    };

    public static final String[] DARKMAGE = {
        "..............",
        "....PPPPPP....",
        "...PDDDDDDP...",
        "..PDDDDDDDDP..",
        "..PDKDDDDKDP..",
        "..PDDDDDDDDP..",
        "..PDDDDDDDDP..",
        "...PDDDDDDP...",
        "....PPPPPP....",
        "...PPPPPPPP...",
        "..PPKKKKKKPP..",
        "..PPKKKKKKPP..",
        "..PPPPPPPPPP..",
        "...PP....PP...",
        "...PP....PP...",
        "...DD....DD..."
    };

    public static final String[] SKELETON = {
        "..............",
        "....WWWWWW....",
        "...WWWWWWWW...",
        "..WWKWWWWKWW..",
        "..WWWWWWWWWW..",
        "..WWDDDDDDWW..",
        "...WWWWWWWW...",
        "....WWWWWW....",
        "...WWWWWWWW...",
        "..WWWWWWWWWW..",
        ".WWWWWWWWWWWW.",
        ".WWWWWWWWWWWW.",
        "..WWWWWWWWWW..",
        "...WW....WW...",
        "...WW....WW...",
        "...WW....WW..."
    };

    public static final String[] DEMON = {
        "..............",
        ".D..........D.",
        ".DD........DD.",
        "..DDDDDDDDDD..",
        ".DRDDDDDDDDRD.",
        ".DRDDDDDDDDRD.",
        "..DDDDDDDDDD..",
        "..DDRRRRRRDD..",
        "...DDDDDDDD...",
        "..RRRRRRRRRR..",
        ".RRRRRRRRRRRR.",
        ".RRRRRRRRRRRR.",
        ".RRRRRRRRRRRR.",
        "..RRRRRRRRRR..",
        "...RR....RR...",
        "...DD....DD..."
    };

    public static String[] get(String key) {
        switch (key) {
            case "knight":   return KNIGHT;
            case "mage":     return MAGE;
            case "goblin":   return GOBLIN;
            case "orc":      return ORC;
            case "darkmage": return DARKMAGE;
            case "skeleton": return SKELETON;
            case "demon":    return DEMON;
            default:         return KNIGHT;
        }
    }

    public static Map<Character, Color> palette() {
        Map<Character, Color> p = new HashMap<>();
        p.put('.', Color.TRANSPARENT);
        p.put('D', Color.web("#1e293b"));   // dark slate
        p.put('S', Color.web("#cbd5e1"));   // silver
        p.put('K', Color.web("#0f172a"));   // near-black
        p.put('B', Color.web("#3b82f6"));   // blue
        p.put('R', Color.web("#dc2626"));   // red
        p.put('G', Color.web("#22c55e"));   // green
        p.put('W', Color.web("#fef3c7"));   // cream
        p.put('P', Color.web("#a855f7"));   // purple
        p.put('Y', Color.web("#facc15"));   // gold
        p.put('O', Color.web("#f97316"));   // orange
        // --- new accents (used by the redesigned heroes) ---
        p.put('V', Color.web("#7c5cff"));   // violet (Knight armour)
        p.put('C', Color.web("#22d3ee"));   // cyan   (Mage magic trim)
        p.put('A', Color.web("#fbbf24"));   // amber  (Knight boots)
        return p;
    }
}