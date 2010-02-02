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
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Player helps to play the midi data by keeping all of the javax.sound.midi
 * interaction encapsulated in one object.  This helps keep the playback
 * consistent across different areas of the application
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class Player {

    /**
     * Construct a JButton that will start the MIDI playing
     */
    public class PlayButton extends JButton implements ActionListener {

        /**
         * Construct a new button
         */
        public PlayButton() {
            super("Play");
            setMnemonic('P');
            addActionListener(this);
        }

        /**
         * Paint a 'play triangle' on the play key
         * @param g Graphics object
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(50, 200, 100));
            Polygon triangle = new Polygon();
            triangle.addPoint(5, 5);
            triangle.addPoint(15, 12);
            triangle.addPoint(5, 20);
            g.fillPolygon(triangle);
        }

        /**
         * Listen for a click to play
         * @param e Action event received
         */
        public void actionPerformed(ActionEvent e) {
            play();
        }
    }

    /**
     * Construct a JButton that will stop the MIDI playing
     */
    public class StopButton extends JButton implements ActionListener {

        /**
         * Construct a new button
         */
        public StopButton() {
            super("Stop");
            setMnemonic('S');
            addActionListener(this);
        }

        /**
         * Paint a 'stop square' on the button
         * @param g Graphics object
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(5, 7, 10, 10);
        }

        /**
         * Listen for a click to stop
         * @param e ActionEvent received
         */
        public void actionPerformed(ActionEvent e) {
            stop();
            loop = false;
        }
    }

    /**
     * Construct a JButton that will loop the MIDI
     */
    public class LoopButton extends JButton implements ActionListener {

        /**
         * Construct a new button
         */
        public LoopButton() {
            super("Loop");
            setMnemonic('L');
            addActionListener(this);
        }

        /**
         * Paint a 'loop circle' on the button
         * @param g Graphics object
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawOval(4, 8, 10, 10);
            g.drawOval(5, 9, 8, 8);
        }

        /**
         * Listen for a click to loop
         * @param e ActionEvent received
         */
        public void actionPerformed(ActionEvent e) {
            loop = true;
            play();
        }
    }

    private Sequence sequence;
    private int tempo = 120;
    private long currPos = 0;
    private boolean loop = false, playing = false;
    private Sequencer seq;
    private MidiDevice synth;

    /**
     * Construct a new MIDI play-helper
     * @param _tempo The tempo
     */
    public Player(int _tempo) {
        tempo = _tempo;
        try {
            setSynthesizer(MidiSystem.getSynthesizer().getDeviceInfo());
            setTempo(tempo);
            if (seq instanceof Sequencer) {
                seq.addMetaEventListener(new MetaEventListener() {

                    public void meta(MetaMessage mesg) {
                        // check for a MIDI stop message and loop if we need to
                        if (mesg.getType() == 47) {
                            stop();
                            if (loop) {
                                play();
                            }
                        }
                    }
                });
            }
        } catch (MidiUnavailableException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "There was a problem instantiating the midi: " + e.getMessage() + "\n" +
                    "Try another MIDI device, maybe?",
                    "MIDI Oops!",
                    JOptionPane.ERROR_MESSAGE,
                    null);
        }
    }

    /**
     * Get a button to start playing
     * @return A pre-built JButton
     */
    public PlayButton getPlayButton() {
        return new PlayButton();
    }

    /**
     * Get a button to stop playing
     * @return A pre-built JButton
     */
    public StopButton getStopButton() {
        return new StopButton();
    }

    /**
     * Get a button to loop playing
     * @return A pre-built JButton
     */
    public LoopButton getLoopButton() {
        return new LoopButton();
    }

    /**
     * Start playing
     */
    public void play() {
        try {
            seq.setMicrosecondPosition(currPos);
            seq.start();
            playing = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "There was a problem playing the midi: " + e.getMessage(),
                    "MIDI Oops!",
                    JOptionPane.ERROR_MESSAGE,
                    null);
        }
    }

    /**
     * Stop playing
     */
    public void stop() {
        if (seq instanceof Sequencer) {
            seq.stop();
            playing = false;
            seq.setTickPosition(0);
            currPos = 0;
        }
    }

    /**
     * Pause playing
     */
    public void pause() {
        if (seq instanceof Sequencer) {
            currPos = seq.getMicrosecondPosition() + 1;
            seq.stop();
            playing = false;
        }
    }

    /**
     * Set the sequence to play
     * @param _sequence The new sequence
     */
    public void setSequence(Sequence _sequence) {
        sequence = _sequence;
        try {
            // if we're running, stop, set sequence, start; otherwise just set sequence
            if (seq.isRunning()) {
                seq.stop();
                seq.setSequence(sequence);
                seq.setTempoInBPM(tempo);
                play();
            } else {
                seq.setSequence(sequence);
                seq.setTempoInBPM(tempo);
            }
        } catch (InvalidMidiDataException e) {
            if (e instanceof InvalidMidiDataException) {
                JOptionPane.showMessageDialog(
                        null,
                        "There was a problem loading the midi: " + e.getMessage(),
                        "MIDI Oops!",
                        JOptionPane.ERROR_MESSAGE,
                        null);
            }
        } catch (Exception e) {
            //
        }
    }

    /**
     * Set the speed of the sequence by modifying the tempo factor
     * @param _tempo the new tempo in BPM
     */
    public void setTempo(int _tempo) {
        if (seq instanceof Sequencer) {
            seq.setTempoFactor((float) _tempo / (float) tempo);
        }
    }

    /**
     * Set a new synthesizer to use for playback after cleaning up
     * @param _synth a MidiDevice.Info describing the new synth
     */
    public void setSynthesizer(MidiDevice.Info _synth) {
        try {
            if (synth instanceof Synthesizer) {
                synth.close();
                synth.getReceiver().close();
            }
            if (seq instanceof Sequencer) {
                seq.close();
                seq.getTransmitter().close();
            }
            synth = MidiSystem.getMidiDevice(_synth);
            seq = MidiSystem.getSequencer();
            synth.open();
            seq.open();
            seq.getTransmitter().setReceiver(synth.getReceiver());
            setSequence(sequence);
        } catch (MidiUnavailableException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "There was a problem setting up that device: " + e.getMessage(),
                    "MIDI Oops!",
                    JOptionPane.ERROR_MESSAGE,
                    null);
        }
    }

    /**
     * Tell whether or not the sequence is playing currently
     * @return True if the sequence is playing, false otherwise
     */
    public boolean isPlaying() {
        return playing;
    }
}
