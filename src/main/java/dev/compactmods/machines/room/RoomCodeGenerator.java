package dev.compactmods.machines.room;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Random;

public class RoomCodeGenerator {
    private static final String REPLACE_REGEX = "([A-Z0-9]{4})([A-Z0-9]{4})([A-Z0-9]{4})";
    private static final char[] ALPHABET = "0123456789ABDFGHJKLMNPQRSTVWXYZ".toCharArray();
    private static final Random RANDOM = new Random();

    public static String generateRoomId() {
        final var id = NanoIdUtils.randomNanoId(RANDOM, ALPHABET, 12);
        return id.replaceAll(REPLACE_REGEX, "$1-$2-$3");
    }
}
