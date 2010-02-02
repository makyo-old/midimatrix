package com.mjs_svc.midimatrix;

/*
 * MIDIMatrix - Matrix-based MIDI sequencer
 * Copyright (c) 2009 Matthew Scott
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see < http://www.gnu.org/licenses/ >.
 */

import java.util.*;

/**
 * Scale is a simple representation of a scale for MMGrid
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class Scale {

    private ArrayList<Integer> scale;
    private String repr;

    /**
     * Construct a new scale for use in a grid
     * @param _repr A string representation of the scale
     * @param _scale Any number of integers representing steps above the base
     * key for the notes.  NB: 0 should be the first integer
     */
    public Scale(String _repr, int... _scale) {
        repr = _repr;
        scale = new ArrayList<Integer>();
        for (int i = 0; i < _scale.length; i++) {
            scale.add(new Integer(_scale[i]));
        }
    }

    /**
     * Override the toString method to return the name of the scale
     * @return The name of the scale
     */
    @Override
    public String toString() {
        return repr;
    }

    /**
     * Get the scale as an array of integers
     * @return The scale as an array of integers
     */
    public int[] getScale() {
        int[] scaleArray = new int[scale.size()];
        for (int i = 0; i < scale.size(); i++) {
            scaleArray[i] = scale.get(i);
        }
        return scaleArray;
    }

    /**
     * Construct a note name (i.e.: C4, D#2) given a MIDI pitch value
     * @param midiNumber The pitch as passed in a ShortMessage
     * @return A string containing the name
     */
    public static String getNoteNameByNumber(int midiNumber) {
        String[] letters = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        return letters[midiNumber % 12] + String.valueOf((int)(midiNumber / 12) - 1);
    }

    /**
     * Given a scale degree, return the interval above the base key in half-steps
     * @param scaleDegree The scale degree
     * @return The interval above the base key in half-steps
     */
    public int getNoteNumberByScaleDegree(int scaleDegree) {
        return scale.get(scaleDegree % scale.size()) + (12 * (scaleDegree / scale.size()));
    }

    // Some basic scales
    // intervals: [2, 2, 3, 2, 3, 2, 2, 3, 2, 3, 2, 2, 3, 2, 3]
    public static final Scale MAJOR_PENTATONIC = new Scale(
            "Major Pentatonic",
            0, 2, 4, 7, 9);
    // intervals: [3, 2, 2, 3, 2, 3, 2, 2, 3, 2, 3, 2, 2, 3, 2]
    public static final Scale MINOR_PENTATONIC = new Scale(
            "Minor Pentatonic",
            0, 3, 5, 7, 10);
    // intervals:  [2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 2]
    public static final Scale IONIAN = new Scale(
            "Ionian (Major)",
            0, 2, 4, 5, 7, 9, 11);
    // intervals: [2, 1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2]
    public static final Scale AEOLIAN = new Scale(
            "Aeolian (Minor)",
            0, 2, 3, 5, 7, 8, 10);
    // intervals: [1, 3, 1, 2, 1, 2, 1, 1, 1, 3, 1, 2, 1, 2, 1]
    public static final Scale ARABIC = new Scale(
            "\"Arabic\"",
            0, 1, 4, 5, 7, 8, 10, 11);
    // intervals: [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2]
    public static final Scale WHOLE_TONE = new Scale(
            "Whole Tone",
            0, 2, 4, 6, 8, 10);
    // intervals: [1, 2, 1, 2, 1, 2, 1, 2]
    public static final Scale OCTATONIC = new Scale(
            "Octatonic",
            0, 1, 3, 4, 6, 7, 9, 10);
    // intervals: [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
    public static final Scale CHROMATIC = new Scale(
            "Chromatic",
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
}
