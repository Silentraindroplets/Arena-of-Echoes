package com.arena;

import java.util.ArrayList;
import java.util.List;

public class StoryScript {

    public static final String NARRATOR  = "#94a3b8";
    public static final String KNIGHT    = "#7c5cff";
    public static final String MAGE      = "#38bdf8";
    public static final String MALGOROTH = "#dc2626";

    public static List<StoryLine> interludeAfterWave(int waveJustCleared, int numPlayers) {
        switch (waveJustCleared) {
            case 1: return afterFirstWave(numPlayers);
            case 2: return afterSecondWave(numPlayers);
            case 3: return afterThirdWave(numPlayers);
            case 4: return beforeMalgoroth(numPlayers);
            default: return new ArrayList<>();
        }
    }

    private static List<StoryLine> afterFirstWave(int numPlayers) {
        List<StoryLine> s = new ArrayList<>();
        s.add(new StoryLine("Narrator",
            "The goblins are only the outer sentries. Their blood steams on the black stone and drips between the cracked tiles.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "Beyond them, a winding stair descends into the lower tiers of the Arena. The air grows warmer. It smells of iron and old ash.",
            "knight", NARRATOR));
        s.add(new StoryLine("???",
            "So the little hero has a spine after all. How charming. Come down. Malgoroth so rarely has visitors.",
            "demon", MALGOROTH, true));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "I'm not here to visit.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Knight",
                "We're not here to visit.",
                "knight", KNIGHT));
            s.add(new StoryLine("Mage",
                "Speak for yourself. I'm here to redecorate.",
                "mage", MAGE));
        }
        s.add(new StoryLine("???",
            "Aren't you? They all say that. They all come down the stair. They all end up on the walls.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Narrator",
            "You descend. Behind you, the first wave's bodies fade into smoke and join the ceiling like they were never there at all.",
            "knight", NARRATOR));
        return s;
    }

    private static List<StoryLine> afterSecondWave(int numPlayers) {
        List<StoryLine> s = new ArrayList<>();
        s.add(new StoryLine("Narrator",
            "The warden crumples. As it falls, something strange catches your eye — a sigil tattooed on its chest, still glowing faintly through its hide.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "The sigil is ancient. You've seen it in the old histories. It's the crest of Malgoroth — but drawn when he was still mortal. When he was still a king.",
            "knight", NARRATOR));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "He was a man once. A king with a crest and a crown and a kingdom.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Mage",
                "That's a mortal crest. Malgoroth was human before he was this.",
                "mage", MAGE));
            s.add(new StoryLine("Knight",
                "A man doesn't become a Demon Lord for no reason.",
                "knight", KNIGHT));
        }
        s.add(new StoryLine("???",
            "No. He doesn't.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("???",
            "He broke an oath. A very old, very binding one. And for that, they sealed him here with a thousand promises and a thousand dead knights.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("???",
            "Now he is neither man nor god. He is only hunger. He is only echo.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Narrator",
            "The voice sounds almost... sad. You descend another tier.",
            "knight", NARRATOR));
        return s;
    }

    private static List<StoryLine> afterThirdWave(int numPlayers) {
        List<StoryLine> s = new ArrayList<>();
        s.add(new StoryLine("Narrator",
            "The bones clatter to the floor. The dark mage's robes collapse like a punctured lung.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "One of the skeletons catches your eye as it falls. Its armor is rusted silver, its crest is blue, its helm has a red plume. It's the same design you are wearing.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "It's the same design the knight in the corridor was wearing. It's the same design every knight who ever entered this Arena has worn.",
            "knight", NARRATOR));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "He wore my crest.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Knight",
                "He wore my crest.",
                "knight", KNIGHT));
            s.add(new StoryLine("Mage",
                "You aren't the first. You were never going to be the first.",
                "mage", MAGE));
        }
        s.add(new StoryLine("???",
            "Now you see it. The Arena does not let its victims die. It only lets them repeat.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("???",
            "Every knight who came before you is still here. Every one of them is still trying. Every one of them has failed, a thousand times, in this exact hall, on this exact tile.",
            "demon", MALGOROTH, true));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "Then I'll be the first one who doesn't.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Knight",
                "Then we'll be the first who don't.",
                "knight", KNIGHT));
            s.add(new StoryLine("Mage",
                "Loops break when someone stops playing by the rules. We don't play by the rules.",
                "mage", MAGE));
        }
        s.add(new StoryLine("Narrator",
            "You descend the final stair. The temperature rises. The embers thicken. The laughter — if it was ever laughter — stops.",
            "knight", NARRATOR));
        return s;
    }

    private static List<StoryLine> beforeMalgoroth(int numPlayers) {
        List<StoryLine> s = new ArrayList<>();
        s.add(new StoryLine("Narrator",
            "The elite guard fall one by one, and the hall goes silent. Only the crackle of embers and the slow drip of cooling metal remains.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "Then the far doors open. And Malgoroth the Ashen King walks through.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Narrator",
            "He is not what you imagined. He is not a beast. He is a gaunt, crowned figure — his skin like cracked porcelain, his eyes two dying coals, his crown heavy and worn like it has been worn for a very long time.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Malgoroth",
            "You made it. All the way down. The warden, the legion, the guard. All of them.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Malgoroth",
            "Do you know why I opened the gates tonight, little hero?",
            "demon", MALGOROTH, true));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "To add another knight to your collection.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Mage",
                "The usual? More bones for your walls?",
                "mage", MAGE));
        }
        s.add(new StoryLine("Malgoroth",
            "No. I opened them because I want to die.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Malgoroth",
            "I have been sealed here for a hundred years. Every knight who entered — I killed. Every prayer — I ignored. Every escape — I failed.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Malgoroth",
            "And still the Arena will not release its king. It will only release a broken one.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Malgoroth",
            "So you see — I need you to be better than me. I need you to end what I could not.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Malgoroth",
            "Please. Break me.",
            "demon", MALGOROTH, true));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "Gladly.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Knight",
                "We will.",
                "knight", KNIGHT));
            s.add(new StoryLine("Mage",
                "Not for you. For the thousand knights in the walls. Including us, if we fail.",
                "mage", MAGE));
        }
        s.add(new StoryLine("Narrator",
            "He raises a hand. The hall ignites. The throne room is yours now — for however long it lasts.",
            "demon", MALGOROTH, true));
        return s;
    }

    public static List<StoryLine> victoryEpilogue(int numPlayers) {
        List<StoryLine> s = new ArrayList<>();
        s.add(new StoryLine("Narrator",
            "Malgoroth the Ashen King falls.",
            "demon", MALGOROTH, true));
        s.add(new StoryLine("Narrator",
            "His crown rolls away from his head and stops in a crack in the tile. Without it, his face is just a face. Tired. Relieved. Human.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Malgoroth",
            "Thank you. Truly.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Malgoroth",
            "I was... so tired of echoing.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Narrator",
            "He closes his eyes. And for the first time in a hundred years, the Ashen King smiles.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Narrator",
            "The ceiling of the great hall begins to crack. The red moon outside gutters, flickers, and fades to a cool grey dawn. Embers rise instead of fall.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "You walk back up through the tiers. The bodies are gone. The armor on the walls is gone. Only the stone remains — plain, quiet stone.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "You step out through the iron doors you entered by. The gates of the Arena of Echoes close behind you, and this time, they do not creak. They close the way a book closes — softly, at the end.",
            "knight", NARRATOR));
        if (numPlayers == 1) {
            s.add(new StoryLine("Knight",
                "The echoes stop here.",
                "knight", KNIGHT));
        } else {
            s.add(new StoryLine("Knight",
                "The echoes stop here.",
                "knight", KNIGHT));
            s.add(new StoryLine("Mage",
                "Good. I could use the sleep.",
                "mage", MAGE));
        }
        s.add(new StoryLine("Narrator",
            "Somewhere far away, a new moon rises over a new kingdom, and no one in it knows your name. That's fine. You know it. That's enough.",
            "knight", NARRATOR));
        return s;
    }

    public static List<StoryLine> defeatEpilogue(int numPlayers) {
        List<StoryLine> s = new ArrayList<>();
        s.add(new StoryLine("Narrator",
            "You fall.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "The last thing you see is the Ashen King's face looking down at you — and something in it that might be mercy.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Malgoroth",
            "Rest now, little hero. You earned it more than most.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Malgoroth",
            "I am sorry it could not be you.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Narrator",
            "The ceiling crack closes. The red moon rights itself. Somewhere in the dark, the Arena of Echoes resumes its patient, eternal count.",
            "demon", MALGOROTH));
        s.add(new StoryLine("Narrator",
            "Years from now, a new knight will walk the same corridor. They will see new armor on the wall, hanging beside yours.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "They will wonder, like you did, what lies at the end of it.",
            "knight", NARRATOR));
        s.add(new StoryLine("Narrator",
            "The Arena of Echoes does not close. It never does.",
            "knight", NARRATOR));
        return s;
    }
}