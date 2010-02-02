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
 * Grid represents a (currently) 16x16 grid of cells.  The cells in the grid
 * contain information that tells when a note starts or ends, allowing for notes
 * of any duration up to 16.  This information is compiled into a MIDI track.
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class Grid {

    // Data
    
    protected Scale scale;       // What notes to populate the grid with
    protected int width, height; // The width and height of the grid
    protected int instrument;    // What instrument to play the notes in
    protected int key;           // What note is the lowest y value set to
    protected int velocity;      // What volume to play the grid at
    protected boolean muted;     // Whether the grid should be muted
    protected boolean solo;      // Whether the grid should be solo
    protected Track track;	 // The actual MIDI representation of the grid
    protected boolean[][][] grid;  // The grid itself
    public Vector instrumentList;  // The list of instruments possible
    public static final int lowBound = 0, highBound = 127;  // Low and high notes

    /**
     * Construct a new Grid
     * @param _width The width of the grid (how many notes long it is)
     * @param _height The height of the grid (how many pitches for each column)
     * @param _scale The scale to use for the grid
     * @param _instrument The instrument to use for the grid
     * @param _key The key to use for the grid (middle C = 60)
     * @param _velocity The velocity (volume) of the grid
     * @param _track A javax.sound.midi.Track object for compilation
     */
    public Grid(int _width, int _height, Scale _scale, int _instrument, int _key, int _velocity, Track _track) {
        // Set all the protected data
        width = _width;
        height = _height;
        scale = _scale;
        instrument = _instrument;
        key = _key;
        velocity = _velocity;
        muted = false;
        solo = false;
        track = _track;

        // grid represents our 16x16 tone matrix.  Each of the 256 slots has space for a noteOff signal and a noteOn signal;
        //      this way, a note is held until the next noteOff signal is reached
        grid = new boolean[width][height][2];

        // Build the instrumentList vector
        instrumentList = new Vector();
        buildInstrumentList();
    }

    /**
     * Populates the instrumentList vector with strings used for display
     */
    private void buildInstrumentList() {
        // Keyboards
        instrumentList.add("Acoustic Grand Piano");
        instrumentList.add("Bright Acoustic Piano");
        instrumentList.add("Electric Grand Piano");
        instrumentList.add("Honky-tonk Piano");
        instrumentList.add("Rhodes Piano");
        instrumentList.add("Chorused Piano");
        instrumentList.add("Harpsichord");
        instrumentList.add("Clavinet");

        // Chromatic percussion
        instrumentList.add("Celesta");
        instrumentList.add("Glockenspiel");
        instrumentList.add("Music Box");
        instrumentList.add("Vibraphone");
        instrumentList.add("Marimba");
        instrumentList.add("Xylophone");
        instrumentList.add("Tubular Bells");
        instrumentList.add("Dulcimer");

        // Organs
        instrumentList.add("Hammond Organ");
        instrumentList.add("Percussive Organ");
        instrumentList.add("Rock Organ");
        instrumentList.add("Church Organ");
        instrumentList.add("Reed Organ");
        instrumentList.add("Accordion");
        instrumentList.add("Harmonica");
        instrumentList.add("Tango Accordion");

        // Guitars
        instrumentList.add("Acoustic Guitar (nylon)");
        instrumentList.add("Acoustic Guitar (steel)");
        instrumentList.add("Electric Guitar (jazz)");
        instrumentList.add("Electric Guitar (clean)");
        instrumentList.add("Electric Guitar (muted)");
        instrumentList.add("Overdriven Guitar");
        instrumentList.add("Distortion Guitar");
        instrumentList.add("Guitar Harmonics");

        // Basses
        instrumentList.add("Acoustic Bass");
        instrumentList.add("Electric Bass (finger)");
        instrumentList.add("Electric Bass (pick)");
        instrumentList.add("Fretless Bass");
        instrumentList.add("Slap Bass 1");
        instrumentList.add("Slap Bass 2");
        instrumentList.add("Synth Bass 1");
        instrumentList.add("Synth Bass 2");

        // Strings 1
        instrumentList.add("Violin");
        instrumentList.add("Viola");
        instrumentList.add("Cello");
        instrumentList.add("Contrabass");
        instrumentList.add("Tremolo Strings");
        instrumentList.add("Pizzicato Strings");
        instrumentList.add("Orchestral Harp");
        instrumentList.add("Timpani");

        // Strings 2
        instrumentList.add("String Ensemble 1");
        instrumentList.add("String Ensemble 2");
        instrumentList.add("SynthStrings 1");
        instrumentList.add("SynthStrings 2");
        instrumentList.add("Choir Aahs");
        instrumentList.add("Voice Oohs");
        instrumentList.add("Synth Voice");
        instrumentList.add("Orchestra Hit");

        // Brass
        instrumentList.add("Trumpet");
        instrumentList.add("Trombone");
        instrumentList.add("Tuba");
        instrumentList.add("Muted Trumpet");
        instrumentList.add("French Horn");
        instrumentList.add("Brass Section");
        instrumentList.add("Synth Brass 1");
        instrumentList.add("Synth Brass 2");

        // Reeds
        instrumentList.add("Soprano Sax");
        instrumentList.add("Alto Sax");
        instrumentList.add("Tenor Sax");
        instrumentList.add("Baritone Sax");
        instrumentList.add("Oboe");
        instrumentList.add("English Horn");
        instrumentList.add("Bassoon");
        instrumentList.add("Clarinet");

        // Winds
        instrumentList.add("Piccolo");
        instrumentList.add("Flute");
        instrumentList.add("Recorder");
        instrumentList.add("Pan Flute");
        instrumentList.add("Bottle Blow");
        instrumentList.add("Shakuhachi");
        instrumentList.add("Whistle");
        instrumentList.add("Ocarina");

        // Synth Leads
        instrumentList.add("Lead 1 (square)");
        instrumentList.add("Lead 2 (sawtooth)");
        instrumentList.add("Lead 3 (calliope lead)");
        instrumentList.add("Lead 4 (chiff lead)");
        instrumentList.add("Lead 5 (charang)");
        instrumentList.add("Lead 6 (voice)");
        instrumentList.add("Lead 7 (fifths)");
        instrumentList.add("Lead 8 (bass + lead)");

        // Synth Pads
        instrumentList.add("Pad 1 (new age)");
        instrumentList.add("Pad 2 (warm)");
        instrumentList.add("Pad 3 (polysynth)");
        instrumentList.add("Pad 4 (choir)");
        instrumentList.add("Pad 5 (bowed)");
        instrumentList.add("Pad 6 (metallic)");
        instrumentList.add("Pad 7 (halo)");
        instrumentList.add("Pad 8 (sweep)");

        // Synth Effects
        instrumentList.add("FX 1 (rain)");
        instrumentList.add("FX 2 (soundtrack)");
        instrumentList.add("FX 3 (crystal)");
        instrumentList.add("FX 4 (atmosphere)");
        instrumentList.add("FX 5 (brightness)");
        instrumentList.add("FX 6 (goblins)");
        instrumentList.add("FX 7 (echoes)");
        instrumentList.add("FX 8 (sci-fi)");

        // Ethnic
        instrumentList.add("Sitar");
        instrumentList.add("Banjo");
        instrumentList.add("Shamisen");
        instrumentList.add("Koto");
        instrumentList.add("Kalimba");
        instrumentList.add("Bagpipe");
        instrumentList.add("Fiddle");
        instrumentList.add("Shanai");

        // Percussion
        instrumentList.add("Tinkle Bell");
        instrumentList.add("Agogo");
        instrumentList.add("Steel Drums");
        instrumentList.add("Woodblock");
        instrumentList.add("Taiko Drum");
        instrumentList.add("Melodic Tom");
        instrumentList.add("Synth Drum");
        instrumentList.add("Reverse Cymbal");
        instrumentList.add("Guitar Fret Noise");

        // Sound Effects
        instrumentList.add("Breath Noise");
        instrumentList.add("Seashore");
        instrumentList.add("Bird Tweet");
        instrumentList.add("Telephone Ring");
        instrumentList.add("Helicopter");
        instrumentList.add("Applause");
        instrumentList.add("Gunshot");
    }

    /**
     * Set the instrument of the grid
     * @param _instrument The instrument
     */
    public void setInstrument(int _instrument) {
        instrument = _instrument;
    }

    /**
     * Get the instrument of the grid
     * @return The instrument
     */
    public int getInstrument() {
        return instrument;
    }

    /**
     * Set the scale of the grid
     * @param _scale The scale
     */
    public void setScale(Scale _scale) {
        scale = _scale;
    }

    /**
     * Get the scale of the grid
     * @return The scale
     */
    public Scale getScale() {
        return scale;
    }

    /**
     * @deprecated Use setVelocity()
     * @see #setVelocity(int)
     */
    @Deprecated
    public void velocityUp() {
        // make sure 127 is our top velocity
        velocity = (velocity + 16 > 127) ? 127 : velocity + 16;
    }

    /**
     * @deprecated Use setVelocity()
     * @see #setVelocity(int)
     */
    @Deprecated
    public void velocityDown() {
        // make sure 0 is our bottom velocity
        velocity = (velocity - 16 < 0) ? 0 : velocity - 16;
    }

    /**
     * Set the velocity (volume) of the grid
     * @param _velocity
     * @throws IndexOutOfBoundsException
     */
    public void setVelocity(int _velocity) throws IndexOutOfBoundsException {
        if (_velocity >= 0 && _velocity <= 127) {
            velocity = _velocity;
        } else {
            throw new IndexOutOfBoundsException("Velocity must be between 0 and 127");
        }
    }

    /**
     * Return the velocity (volume) of the grid
     * @return The velocity
     */
    public int getVelocity() {
        return velocity;
    }

    /**
     * Set the key of the grid (middle C = 60)
     * @param _key The new key of the grid
     * @throws IndexOutOfBoundsException Upper note of the scale can't be more
     * than 127, and the lower not less than 0
     */
    public void setKey(int _key) throws IndexOutOfBoundsException {
        // make sure that the note at the top of the scale is no greater than 127 and the bottom no lower than 0
        if (lowBound < _key && key + scale.getNoteNumberByScaleDegree(height) < highBound) {
            key = _key;
        } else {
            throw new IndexOutOfBoundsException("Key must be between " + lowBound + " and " + String.valueOf(highBound - scale.getNoteNumberByScaleDegree(height)) + " for this key");
        }
    }

    /**
     * Returns the key of the grid
     * @return The key
     */
    public int getKey() {
        return key;
    }

    /**
     * @deprecated
     * @see #setKey(int)
     */
    @Deprecated
    public void octaveUp() {
        setKey(key + 12);
    }

    /**
     * @deprecated
     * @see #setKey(int)
     */
    @Deprecated
    public void octaveDown() {
        setKey(key - 12);
    }

    /**
     * Check to see if the cell is currently "on"
     * @param x X coordinate
     * @param y Y coordiante
     * @return The status of the cell
     */
    public boolean noteStatus(int x, int y) {
        // check to see if note is currently on by looking for noteOns and adjusting for noteOffs
        boolean status = false;
        for (int i = 0; i <= x; i++) {
            if (grid[i][y][0]) {
                status = true;
            }
            if (grid[i][y][1] && i < x) {
                status = false;
            }
        }
        return status;
    }

    /**
     * Get the length of a note, given the known start
     * @param x X coordinate of start
     * @param y Y coordinate of start
     * @return The length of the note or 0
     */
    public int noteLength(int x, int y) {
        // return the length of the note
        // make sure this is a noteOn cell
        if (grid[x][y][0]) {
            // loop until we find the noteOff cell
            for (int i = x; i < width; i++) {
                if (grid[i][y][1]) {
                    return (i - x) + 1;
                }
            }
            // should never happen, but 0 is safe
            return 0;
        } else {
            // otherwise return 0
            return 0;
        }
    }

    /**
     * Toggle the noteOn message for fine control
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void toggleNoteOn(int x, int y) {
        grid[x][y][0] = !grid[x][y][0];
    }

    /**
     * Toggle the noteOff message for fine control
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void toggleNoteOff(int x, int y) {
        grid[x][y][1] = !grid[x][y][1];
    }

    /**
     * Given an empty cell, toggle it to on; given a full cell, toggle the
     * entire note off, even if it's a long one.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void toggleNote(int x, int y) {
        // toggle one grid cell, but give the option to turn a long note off

        // check if we're turning a note off
        if (grid[x][y][0]) {
            // remove the noteOn
            grid[x][y][0] = false;

            // search for the noteOff
            for (int i = x; i < width; i++) {
                if (grid[i][y][1]) {
                    grid[i][y][1] = false;
                    return;
                }
            }
        } else {
            // otherwise, turn one cell on
            toggleNote(x, y, 0);

        }
    }

    /**
     * Toggle a note of a given duration (mostly for turning a note on)
     * @param x X coordinate of start
     * @param y Y coordinate of start
     * @param duration Duration of the note
     */
    public void toggleNote(int x, int y, int duration) {
        // toggle a note of a certain duration
        if (noteStatus(x + duration, y) && x + duration < width - 1 && !grid[x + duration][y][1]) {
            // if we're ending in the middle of a note, add a noteOn
            grid[x + duration + 1][y][0] = true;
        }
        if (noteStatus(x - 1, y)) {
            // if we're starting in the middle of a note, add a noteOff
            grid[x - 1][y][1] = true;
        }
        if (!noteStatus(x, y)) {
            // otherwise, remove everything else if we're turning something on
            for (int i = x; i <= x + duration; i++) {
                grid[i][y][0] = false;
                grid[i][y][1] = false;
            }
        }

        // toggle the note
        grid[x][y][0] = !grid[x][y][0];
        grid[x + duration][y][1] = grid[x][y][0]; // ensure the same signal is being sent to noteOff as to noteOn rather than relying on existing conditions
    }

    /**
     * Compile a grid into a MIDI track for playback
     * @return The javax.sound.midi.Track object containing the grid
     * @throws InvalidMidiDataException
     */
    public Track compile() throws InvalidMidiDataException {
        // First, clear the track
        for (int i = 0; i < track.size(); i++) {
            track.remove(track.get(i));
        }

        boolean[] noteStat = new boolean[height];

        // Next, set the track up with the instrument
        ShortMessage mesg = new ShortMessage();
        mesg.setMessage(ShortMessage.PROGRAM_CHANGE, 0, instrument, 0);
        track.add(new MidiEvent(mesg, 0));

        // loop through the grid and add apropriate noteon/offs
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y][0]) {
                    mesg = new ShortMessage();
                    mesg.setMessage(ShortMessage.NOTE_ON, 0, key + scale.getNoteNumberByScaleDegree((height - 1) - y), velocity);
                    track.add(new MidiEvent(mesg, x));
                    noteStat[y] = true;
                }
                if (grid[x][y][1]) {
                    mesg = new ShortMessage();
                    mesg.setMessage(ShortMessage.NOTE_OFF, 0, key + scale.getNoteNumberByScaleDegree((height - 1) - y), velocity);
                    track.add(new MidiEvent(mesg, x + 1));
                    noteStat[y] = false;
                }
            }
        }

        // Check for any lingering notes
        for (int i = 0; i < noteStat.length; i++) {
            if (noteStat[i]) {
                mesg = new ShortMessage();
                mesg.setMessage(ShortMessage.NOTE_OFF, 0, key + scale.getNoteNumberByScaleDegree((height - 1) - i), velocity);
                track.add(new MidiEvent(mesg, width));
            }
        }

        return track;
    }
}
