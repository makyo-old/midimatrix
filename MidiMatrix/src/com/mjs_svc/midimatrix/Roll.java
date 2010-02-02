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
import java.util.Vector;

/**
 * Roll represents a sequence built of (currently) 16 MMGrids.  The roll
 * consists of segments of the 16 grids, which may be turned on or off per
 * segment to determine whether they play during that segment.  This information
 * is compiled into a MIDI sequence.
 *
 * @author Matthew Scott
 * @version $Id: Roll.java 52 2009-11-19 21:59:51Z drab-makyo $
 */
public class Roll extends Sequence {

    protected Grid[] grids;
    protected int gridsEnabled, gridWidth = 16, gridHeight = 16, numGrids = 16;
    private Vector<boolean[]> roll;
    private int tempo;

    /**
     * Construct a new Roll
     * @param _tempo The tempo of the roll
     * @throws InvalidMidiDataException
     */
    public Roll(int _tempo) throws InvalidMidiDataException {
        super(Sequence.PPQ, 1, 16); // since super() has to be called first, have to hardcode the 16; should be gridWidth
        tempo = _tempo;
        gridsEnabled = -1;
        grids = new Grid[16];
        roll = new Vector<boolean[]>();
    }

    public Roll(int _tempo, int _numGrids, int _gridWidth, int _gridHeight) throws InvalidMidiDataException {
        super(Sequence.PPQ, 1, _gridWidth);
        numGrids = _numGrids;
        gridWidth = _gridWidth;
        gridHeight = _gridHeight;
        tempo = _tempo;
        gridsEnabled = -1;
        grids = new Grid[numGrids];
        roll = new Vector<boolean[]>();
    }

    /**
     * Set the roll's tempo
     * @param _tempo The new tempo
     */
    public void setTempo(int _tempo) {
        tempo = _tempo;
    }

    /**
     * Get the roll's tempo
     * @return The tempo
     */
    public int getTempo() {
        return tempo;
    }

    /**
     * Enable a new grid in the roll
     * @param _scale The scale of the new grid
     * @param _instrument The instrument of the new grid
     * @param _key The key of the new grid
     * @param _velocity The velocity (volume) of the new grid
     * @throws IndexOutOfBoundsException There may only be numGrids grids per roll
     */
    public void enableGrid(Scale _scale, int _instrument, int _key, int _velocity) throws IndexOutOfBoundsException {
        // make sure we can only enable numGrids grids
        if (gridsEnabled < numGrids - 1) {
            gridsEnabled++;
            grids[gridsEnabled] = new Grid(gridWidth, gridHeight, _scale, _instrument, _key, _velocity, this.tracks.get(gridsEnabled));
        } else {
            throw new IndexOutOfBoundsException("Roll may only have " + numGrids + " grids active at a time!");
        }
    }

    /**
     * Enable a new percussion grid in the roll
     * @param _key The starting pitch of the lowest grid index
     * @param _velocity The velocity (volume) of the new grid
     * @throws IndexOutOfBoundsException There may only be 16 grids per roll
     */
    public void enablePercussionGrid(int _key, int _velocity) throws IndexOutOfBoundsException {
        if (gridsEnabled < numGrids - 1) {
            gridsEnabled++;
            grids[gridsEnabled] = new PercussionGrid(gridWidth, gridHeight, _key, _velocity, this.tracks.get(gridsEnabled));
        } else {
            throw new IndexOutOfBoundsException("Roll may only have " + numGrids + " grids active at a time!");
        }
    }

    /**
     * Clear all data from a grid by replacing it entirely
     * @param gridIndex The grid to clear
     */
    public void clearGrid(int gridIndex) {
        Grid old = grids[gridIndex];
        grids[gridIndex] = new Grid(gridWidth, gridHeight, old.getScale(), old.getInstrument(), old.getKey(), old.getVelocity(), old.track);
    }

    /**
     * Add a new segment to the roll
     */
    public void addRollSegment() {
        // Add a segment to the roll
        roll.add(new boolean[numGrids]);
    }

    /**
     * Add a new segment to the roll at an index, shifting the rest of the
     * segments up
     * @param index The index of the new segment
     */
    public void addRollSegmentAt(int index) {
        // Add a segment to the roll at the given index
        roll.add(index, new boolean[numGrids]);
    }

    /**
     * Remove a segment from the roll
     * @param index The segment to remove
     */
    public void removeRollSegment(int index) {
        // Remove a segment from the roll at index
        roll.remove(index);
    }

    /**
     * Move a roll segment from one index to another
     * @param from The index of the original segment
     * @param to The destination index of the new segment
     */
    public void moveRollSegment(int from, int to) {
        // Move a segment from one index to another by deleting and recreating
        boolean[] segment = roll.get(from);
        roll.remove(from);
        roll.add(to, segment);
    }

    /**
     * Enable a grid in a given segment
     * @param segmentIndex The segment to work with
     * @param gridIndex The index of the grid
     */
    public void enableGridInRollSegment(int segmentIndex, int gridIndex) {
        // Turn a grid on for that particular segment of the roll
        boolean[] segment = roll.get(segmentIndex);
        segment[gridIndex] = true;
        roll.set(segmentIndex, segment);
    }

    /**
     * Disable a grid in a given segment
     * @param segmentIndex The segment to work with
     * @param gridIndex The index of the grid
     */
    public void disableGridInRollSegment(int segmentIndex, int gridIndex) {
        // Turn a grid off for that particular segment of the roll
        boolean[] segment = roll.get(segmentIndex);
        segment[gridIndex] = false;
        roll.set(segmentIndex, segment);
    }

    /**
     * Toggle the status of a grid in a given segment
     * @param segmentIndex The segment to work with
     * @param gridIndex The grid to toggle
     */
    public void toggleGridInRollSegment(int segmentIndex, int gridIndex) {
        boolean[] segment = roll.get(segmentIndex);
        segment[gridIndex] = !segment[gridIndex];
        roll.set(segmentIndex, segment);
    }

    /**
     * Get the size of the roll
     * @return The size of the roll
     */
    public int size() {
        return roll.size();
    }

    /**
     * Get a segment of the grid
     * @param segmentIndex The segment to retrieve
     * @return An array of boolean values indicating whether a grid is active or
     * inactive for that segment
     */
    public boolean[] getSegment(int segmentIndex) {
        return roll.get(segmentIndex);
    }

    // Interfaces for for each grid
    /**
     *
     * @param gridIndex
     * @see #gridSetVelocity(int, int)
     * @deprecated
     */
    @Deprecated
    public void gridVelocityUp(int gridIndex) {
        // Turn up the volume of a particular grid
        grids[gridIndex].velocityUp();
    }

    /**
     *
     * @param gridIndex
     * @see #gridSetVelocity(int, int)
     * @deprecated
     */
    @Deprecated
    public void gridVelocityDown(int gridIndex) {
        // Turn down the volume of a particular grid
        grids[gridIndex].velocityDown();
    }

    /**
     * Set the instrument of the grid
     * @param gridIndex The grid to work with
     * @param instrument The new instrument (0 is piano)
     * @see com.mjs_svc.midimatrix.Grid#setInstrument(int)
     */
    public void gridSetInstrument(int gridIndex, int instrument) {
        grids[gridIndex].setInstrument(instrument);
    }

    /**
     * Get the instrument of a grid
     * @param gridIndex The grid to work with
     * @return The instrument (0 is piano)
     * @see com.mjs_svc.midimatrix.Grid#getInstrument()
     */
    public int gridGetInstrument(int gridIndex) {
        return grids[gridIndex].getInstrument();
    }

    /**
     * Returns a vector containing the list of possible instruments
     * @param gridIndex The grid to work with
     * @return a vector of instrument strings
     */
    public Vector gridGetInstrumentList(int gridIndex) {
        return grids[gridIndex].instrumentList;
    }

    /**
     * Set the scale of a grid
     * @param gridIndex The grid to work with
     * @param scale The new scale
     * @see com.mjs_svc.midimatrix.Grid#setScale(com.mjs_svc.midimatrix.Scale)
     */
    public void gridSetScale(int gridIndex, Scale scale) {
        grids[gridIndex].setScale(scale);
    }

    /**
     * Get the scale of a grid
     * @param gridIndex The grid to work with
     * @return The grid's scale
     * @see com.mjs_svc.midimatrix.Grid#getScale()
     */
    public Scale gridGetScale(int gridIndex) {
        return grids[gridIndex].getScale();
    }

    /**
     * Set the velocity (volume) of the grid at a given index
     * @param gridIndex The grid to work with
     * @param velocity The new velocity
     * @throws IndexOutOfBoundsException
     * @see com.mjs_svc.midimatrix.Grid#setVelocity(int)
     */
    public void gridSetVelocity(int gridIndex, int velocity) throws IndexOutOfBoundsException {
        grids[gridIndex].setVelocity(velocity);
    }

    /**
     * Return the velocity (volume) of the grid at a given index
     * @param gridIndex The grid to work with
     * @return The velocity
     * @see com.mjs_svc.midimatrix.Grid#getVelocity()
     */
    public int gridGetVelocity(int gridIndex) {
        return grids[gridIndex].getVelocity();
    }

    /**
     * Set the key of the grid at a given index
     * @param gridIndex The grid to work with
     * @param key The new key
     * @see com.mjs_svc.midimatrix.Grid#setKey(int)
     */
    public void gridSetKey(int gridIndex, int key) {
        // Set the key of a particular grid
        grids[gridIndex].setKey(key);
    }

    /**
     * Return the key of grid at a given index
     * @param gridIndex
     * @return The key
     * @see com.mjs_svc.midimatrix.Grid#getKey()
     */
    public int gridGetKey(int gridIndex) {
        return grids[gridIndex].getKey();
    }

    /**
     * @deprecated
     * @see #gridSetKey(int, int)
     * @param gridIndex
     */
    @Deprecated
    public void gridOctaveUp(int gridIndex) {
        // Raise the key of a particular grid by an octave
        grids[gridIndex].octaveUp();
    }

    /**
     * @deprecated
     * @see #gridSetKey(int, int)
     * @param gridIndex
     */
    @Deprecated
    public void gridOctaveDown(int gridIndex) {
        // Lower the key of a particular grid by an octave
        grids[gridIndex].octaveDown();
    }

    /**
     * Get the status of a given cell in a given grid
     * @param gridIndex The index of the grid to check
     * @param x X coordinate on the grid
     * @param y Y coordinate on the grid
     * @return The status of the cell
     * @see com.mjs_svc.midimatrix.Grid#noteStatus(int, int)
     */
    public boolean gridNoteStatus(int gridIndex, int x, int y) {
        return grids[gridIndex].noteStatus(x, y);
    }

    /**
     * Get the length of a note on a grid given its start
     * @param gridIndex The grid to check
     * @param x X coordinate of the note start
     * @param y Y coordinate of the note start
     * @return The length of the note or 0
     * @see com.mjs_svc.midimatrix.Grid#noteLength(int, int)
     */
    public int gridNoteLength(int gridIndex, int x, int y) {
        return grids[gridIndex].noteLength(x, y);
    }

    /**
     * Toggle a noteOn on a grid for fine control
     * @param gridIndex The grid to work with
     * @param x X coordinate on the grid
     * @param y Y coordinate on the grid
     * @see com.mjs_svc.midimatrix.Grid#toggleNoteOn(int, int)
     */
    public void gridToggleNoteOn(int gridIndex, int x, int y) {
        // Start a note playing at the given coordinates for a given grid
        grids[gridIndex].toggleNoteOn(x, y);
    }

    /**
     * Toggle a noteOff on a grid for fine control
     * @param gridIndex The grid to work with
     * @param x X coordinate on the grid
     * @param y Y coordinate on the grid
     * @see com.mjs_svc.midimatrix.Grid#toggleNoteOff(int, int)
     */
    public void gridToggleNoteOff(int gridIndex, int x, int y) {
        // Stop a note playing at the given coordinates for the given grid
        grids[gridIndex].toggleNoteOff(x, y);
    }

    /**
     * On a grid, given an empty cell, toggle it; given a full cell, toggle the
     * entire note off, even if it's a long one
     * @param gridIndex The grid to work with
     * @param x X coordinate on the grid
     * @param y Y coordinate on the grid
     * @see com.mjs_svc.midimatrix.Grid#toggleNote(int, int)
     */
    public void gridToggleNote(int gridIndex, int x, int y) {
        grids[gridIndex].toggleNote(x, y);
    }

    /**
     * On a grid, toggle a note of a given duration (mostly for turning on)
     * @param gridIndex The grid to work with
     * @param x X coordinate of the grid
     * @param y Y coordinate of the grid
     * @param duration Duration of the note to toggle
     * @see com.mjs_svc.midimatrix.Grid#toggleNote(int, int, int)
     */
    public void gridToggleNote(int gridIndex, int x, int y, int duration) {
        grids[gridIndex].toggleNote(x, y, duration);
    }

    /**
     * Compile all the grids in the roll, then build the roll's sequence out of
     * those compiled tracks
     * @throws InvalidMidiDataException from Grid.compile()
     * @see com.mjs_svc.midimatrix.Grid#compile()
     */
    public void compile() throws InvalidMidiDataException {
        Track[] newTracks = new Sequence(Sequence.PPQ, 1, gridWidth).getTracks();
        MidiEvent evt;

        // loop through and compile each grid to make sure it's up to date
        for (int i = 0; i < this.tracks.size(); i++) {
            this.tracks.setElementAt(grids[i].compile(), i);
        }

        // loop through each segment in the roll
        for (int i = 0; i < roll.size(); i++) {
            long ticksPassed = i * gridWidth; // 160 ticks per segment
            // loop through each grid in the segment
            for (int j = 0; j < numGrids; j++) {
                // if the grid is enabled for that segment
                if (roll.get(i)[j]) {
                    // loop through each message in the grid's track and
                    // add it to the sequence with a new time stamp
                    for (int k = 0; k < this.tracks.get(j).size(); k++) {
                        evt = this.tracks.get(j).get(k);
                        if(evt.getMessage() instanceof ShortMessage) {
                            newTracks[j].add(new MidiEvent(
                                    evt.getMessage(),
                                    evt.getTick() + ticksPassed));
                        }
                    }
                }
            }
        }

        // Replace the roll's tracks with the new compiled tracks
        for (int i = 0; i < this.tracks.size(); i++) {
            this.tracks.setElementAt(newTracks[i], i);
        }
    }

    /**
     * Compile the entire roll into a sequence
     * @return A javax.sound.midi.Sequence to play
     * @throws InvalidMidiDataException from Grid.compile() via Roll.compile()
     * @see #compile()
     */
    public Sequence getSequence() throws InvalidMidiDataException {
        compile();
        return (Sequence) this;
    }

    /**
     * Compile just one grid into a sequence for testing
     * @param gridIndex The grid to compile
     * @return A javax.sound.midi.Sequence to play
     * @throws InvalidMidiDataException from Grid.compile()
     * @see com.mjs_svc.midimatrix.Grid#compile()
     */
    public Sequence compileGrid(int gridIndex) throws InvalidMidiDataException {
        // create a new sequence to return
        Sequence singleSequence = new Sequence(Sequence.PPQ, 1, 1);

        // create a new track that will hold only the specified grid
        Track t = singleSequence.createTrack();

        // compile to update the grid
        Track gt = grids[gridIndex].compile();
        for (int i = 0; i < gt.size(); i++) {
            t.add(gt.get(i));
        }

        return singleSequence;
    }
}
