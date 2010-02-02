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

import javax.sound.midi.*;
import java.util.*;

/**
 * A PercussionGrid is an extension of Grid used for MIDI drum instruments, which use
 * channel 10 specifically.  Instruments are different for this channel.
 * @author Matthew Scott
 * @version $Id$
 */
public class PercussionGrid extends Grid {
    // Data
    public static final int lowBound = 35, highBound = 81;  // Low and high notes

    public PercussionGrid(int _width, int _height, int _key, int _velocity, Track _track) {
        super(_width, _height, Scale.CHROMATIC, 0, _key, _velocity, _track);

        // build instrument list with percussion instrument only
        instrumentList = new Vector();
        instrumentList.add("Percussion");
    }

    /**
     * Silently disallow setting the scale - may only be chromatic
     * @param _scale Ignore this argument
     */
    @Override
    public void setScale(Scale _scale) {
    }

    /**
     * Populate instrumentList vector with percussion instrument strings
     *
    private void buildInstrumentList() {
        instrumentList.add(35, "Bass Drum 2");
        instrumentList.add(36, "Bass Drum 1");
        instrumentList.add(37, "Side Stick");
        instrumentList.add(38, "Snare Drum 1");
        instrumentList.add(39, "Hand Clap");
        instrumentList.add(40, "Snare Drum 2");
        instrumentList.add(41, "Low Tom 2");
        instrumentList.add(42, "Closed Hi-hat");
        instrumentList.add(43, "Low Tom 1");
        instrumentList.add(44, "Pedal Hi-hat");
        instrumentList.add(45, "Mid Tom 2");
        instrumentList.add(46, "Open Hi-hat");
        instrumentList.add(47, "Mid Tom 1");
        instrumentList.add(48, "High Tom 2");
        instrumentList.add(49, "Crash Cymbal 1");
        instrumentList.add(50, "High Tom 1");
        instrumentList.add(51, "Ride Cymbal 1");
        instrumentList.add(52, "Chinese Cymbal");
        instrumentList.add(53, "Ride Bell");
        instrumentList.add(54, "Tambourine");
        instrumentList.add(55, "Splash Cymbal");
        instrumentList.add(56, "Cowbell");
        instrumentList.add(57, "Crash Cymbal 2");
        instrumentList.add(58, "Vibra Slap");
        instrumentList.add(59, "Ride Cymbal 2");
        instrumentList.add(60, "High Bongo");
        instrumentList.add(61, "Low Bongo");
        instrumentList.add(62, "Mute High Conga");
        instrumentList.add(63, "Open High Conga");
        instrumentList.add(64, "Low Conga");
        instrumentList.add(65, "High Timbale");
        instrumentList.add(66, "Low Timbale");
        instrumentList.add(67, "High Agogo");
        instrumentList.add(68, "Low Agogo");
        instrumentList.add(69, "Cabasa");
        instrumentList.add(70, "Maracas");
        instrumentList.add(71, "Short Whistle");
        instrumentList.add(72, "Long Whistle");
        instrumentList.add(73, "Short Guiro");
        instrumentList.add(74, "Long Guiro");
        instrumentList.add(75, "Claves");
        instrumentList.add(76, "High Wood Block");
        instrumentList.add(77, "Low Wood Block");
        instrumentList.add(78, "Mute Cuica");
        instrumentList.add(79, "Open Cuica");
        instrumentList.add(80, "Mute Triangle");
        instrumentList.add(81, "Open Triangle");
    }*/

    /**
     * Compile a grid into a MIDI track for playback
     * @return The javax.sound.midi.Track object containing the grid
     * @throws InvalidMidiDataException
     */
    @Override
    public Track compile() throws InvalidMidiDataException {
        // First, clear the track
        for (int i = 0; i < track.size(); i++) {
            track.remove(track.get(i));
        }

        int[] scaleArray = scale.getScale();    // the scale used to determine pitches
        boolean[] noteStat = new boolean[16];   // the note status for each pitch level
        ShortMessage mesg = new ShortMessage(); // The message object to use

        // loop through the grid and add apropriate noteon/offs
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (grid[x][y][0]) {
                    mesg = new ShortMessage();
                    mesg.setMessage(ShortMessage.NOTE_ON, 9, key + scale.getNoteNumberByScaleDegree((height - 1) - y), velocity);
                    track.add(new MidiEvent(mesg, x));
                    noteStat[y] = true;
                }
                if (grid[x][y][1]) {
                    mesg = new ShortMessage();
                    mesg.setMessage(ShortMessage.NOTE_OFF, 9, key + scale.getNoteNumberByScaleDegree((height - 1) - y), velocity);
                    track.add(new MidiEvent(mesg, x + 1));
                    noteStat[y] = false;
                }
            }
        }

        // Check for any lingering notes
        for (int i = 0; i < noteStat.length; i++) {
            if (noteStat[i]) {
                mesg = new ShortMessage();
                mesg.setMessage(ShortMessage.NOTE_OFF, 9, key + scale.getNoteNumberByScaleDegree((height - 1) - i), velocity);
                track.add(new MidiEvent(mesg, 16));
            }
        }

        return track;
    }
}
