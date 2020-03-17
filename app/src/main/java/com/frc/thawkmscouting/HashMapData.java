package com.frc.thawkmscouting;

import java.util.HashMap;

/**
 * Class to return HashMaps of scoring data
 *
 * @author Aniketh Dandu - FRC Team 1100
 */
final class HashMapData {

    // **************************************************
    // Static methods
    // **************************************************

    /**
     * Store the number of shots made into the inner, outer, and bottom goals
     * Create a HashMap to store the data for a nested hierarchy
     *
     * @param data The String array of data read from the QR code
     * @return Returns a HashMap with autonomous scores
     */
    static HashMap<String, Integer> getAutoHits(String[] data) {
        final HashMap<String, Integer> AUTO_HITS = new HashMap<>();
        AUTO_HITS.put("Inner", Integer.valueOf(data[5]));
        AUTO_HITS.put("Outer", Integer.valueOf(data[6]));
        AUTO_HITS.put("Bottom", Integer.valueOf(data[7]));
        return AUTO_HITS;
    }

    /**
     * Store the number of shots attempted and missed in the high and low goals
     * Create a HashMap to store the data for a nested hierarchy
     *
     * @param data The String array of data read from the QR code
     * @return Returns a HashMap with autonomous misses
     */
    static HashMap<String, Integer> getAutoMiss(String[] data) {
        final HashMap<String, Integer> AUTO_MISS = new HashMap<>();
        AUTO_MISS.put("High", Integer.valueOf(data[8]));
        AUTO_MISS.put("Low", Integer.valueOf(data[9]));
        return AUTO_MISS;
    }

    /**
     * Store the number of shots attempted and missed in the high and low goals
     * Create a HashMap to store the data for a nested hierarchy
     *
     * @param data The String array of data read from the QR code
     * @return Returns an overarching HashMap containing 6 HashMaps with total hits and misses
     * {@code
     *  final HashMap SCORING_MAP = new HashMap<String, Integer>()
     * }
     * Each HashMap represents one of six positions on the field
     */
    @SuppressWarnings("unchecked")
    static HashMap<String, HashMap<String, Integer>> getScoring(String[] data) {
        final HashMap<String, HashMap<String, Integer>> SCORING = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            final int INDEX = i;
            final HashMap SCORING_MAP = new HashMap<String, Integer>() {
                {
                    put("Inner", Integer.valueOf(data[5*INDEX+13]));
                    put("Outer", Integer.valueOf(data[5*INDEX+14]));
                    put("Bottom", Integer.valueOf(data[5*INDEX+15]));
                    put("High", Integer.valueOf(data[5*INDEX+16]));
                    put("Low", Integer.valueOf(data[5*INDEX+17]));
                }
            };
            SCORING.put("Position: " + INDEX, SCORING_MAP);
        }
        return SCORING;
    }
}