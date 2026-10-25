package com.arena;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StoryCodex {

    public static class Entry {
        public final String id;
        public final String title;
        public final String subtitle;
        public final String body;
        public final String spriteKey;
        public final String colorHex;

        public Entry(String id, String title, String subtitle, String body,
                     String spriteKey, String colorHex) {
            this.id = id; this.title = title; this.subtitle = subtitle;
            this.body = body; this.spriteKey = spriteKey; this.colorHex = colorHex;
        }
    }

    private static final Map<String, Entry> LIBRARY = new LinkedHashMap<>();
    private static final List<String> UNLOCKED = new ArrayList<>();

    static {
        add(new Entry("prologue",
            "The Arena of Echoes",
            "PROLOGUE",
            "An arena sealed for one hundred years. Tonight, for the first time since the old wars, its gates have opened. Every torch in the corridor gutters as you pass — as if the flame itself is afraid.",
            "knight", "#7c5cff"));

        add(new Entry("malgoroth-first",
            "The Warning",
            "CHAPTER I",
            "\"Foolish mortal. The Demon Lord waits at the end of this arena.\" The voice has no source. It comes from everywhere and nowhere, and it does not sound triumphant. It sounds tired.",
            "demon", "#dc2626"));

        add(new Entry("warden-crest",
            "The Warden's Crest",
            "CHAPTER II",
            "On the orc warden's chest, a sigil still glows faintly. It is Malgoroth's crest — but it was drawn when he was still mortal. When he was still a king with a crown and a kingdom.",
            "demon", "#dc2626"));

        add(new Entry("fallen-knight",
            "The Fallen Knight",
            "CHAPTER III",
            "One of the skeletons wore silver armor. A blue crest. A red plume. The exact design you wear. Every knight who ever entered this Arena has worn the same. The Arena does not let its victims die — it only lets them repeat.",
            "skeleton", "#e2e8f0"));

        add(new Entry("the-ask",
            "The Ashen King's Confession",
            "CHAPTER IV",
            "\"I opened the gates because I want to die. I have been sealed here for a hundred years. Every knight — I killed. Every prayer — I ignored. Every escape — I failed. And still the Arena will not release its king. Please. Break me.\"",
            "demon", "#dc2626"));

        add(new Entry("victory",
            "The Echoes End",
            "EPILOGUE",
            "Malgoroth falls. Without his crown, his face is just a face. Tired. Relieved. Human. \"I was so tired of echoing,\" he says, and closes his eyes, and the red moon outside gutters to a cool grey dawn.",
            "knight", "#7c5cff"));

        add(new Entry("defeat",
            "The Wall",
            "EPILOGUE",
            "You fall. The last thing you see is the Ashen King's face looking down at you — and something in it that might be mercy. Years from now, a new knight will walk the same corridor. They will see new armor on the wall, hanging beside yours.",
            "demon", "#dc2626"));
    }

    private static void add(Entry e) { LIBRARY.put(e.id, e); }

    public static void unlock(String id) {
        if (id == null) return;
        if (!UNLOCKED.contains(id) && LIBRARY.containsKey(id)) UNLOCKED.add(id);
    }

    public static void unlockAll() {
        for (String id : LIBRARY.keySet()) if (!UNLOCKED.contains(id)) UNLOCKED.add(id);
    }

    public static void reset() { UNLOCKED.clear(); }

    public static List<Entry> unlocked() {
        List<Entry> out = new ArrayList<>();
        for (String id : UNLOCKED) out.add(LIBRARY.get(id));
        return out;
    }

    public static int totalCount() { return LIBRARY.size(); }
    public static int unlockedCount() { return UNLOCKED.size(); }
}