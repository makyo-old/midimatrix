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

import java.awt.*;
import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;
import javax.swing.event.*;

/**
 * The base tabbed pane for MidiMatrix
 *
 * @author Matthew Scott
 * @version $Id: MidiMatrixTabbedPane.java 50 2009-11-15 17:38:24Z mjs@mjs-svc.com $
 */
public class MidiMatrixTabbedPane extends JTabbedPane {

    private boolean canSave = false;
    private Roll roll;
    private Player playControl;
    private JPanel homePanel, gridPanel, rollPanel, ctrlPanel, helpPanel, progressPanel;
    private JProgressBar progressBar;
    private JFrame progressFrame;
    private JLabel progressStatus;

    /**
     * Construct a new tabbed pane
     */
    public MidiMatrixTabbedPane(boolean _canSave) {
        canSave = _canSave;

        // build a progress bar window to show the user what's going on
        progressBar = new JProgressBar(0, 9);
        progressFrame = new JFrame("Loading...");
        progressStatus = new JLabel("Initializing");
        progressPanel = new JPanel(new GridLayout(2, 1));
        progressBar.setPreferredSize(new Dimension(50, 300));
        progressBar.setStringPainted(true);
        progressFrame.add(progressPanel);
        progressPanel.add(progressBar);
        progressPanel.add(progressStatus);
        progressFrame.setPreferredSize(new Dimension(300, 75));
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        progressFrame.pack();
        progressFrame.setVisible(true);

        // Set up panels
        // Home panel
        progressStatus.setText("Setting up the Welcome tab");
        progressBar.setValue(1);
        homePanel = new HomePanel();

        progressStatus.setText("Creating play controls");
        progressBar.setValue(2);
        playControl = new Player(120);

        // set up the roll
        progressStatus.setText("Starting a new sequence");
        progressBar.setValue(3);
        try {
            roll = new Roll(120);
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressStatus.setText("Creating the Matrix panel");
        progressBar.setValue(4);
        gridPanel = new MatrixPanel(roll, playControl);

        progressStatus.setText("Creating the Sequence panel");
        progressBar.setValue(5);
        rollPanel = new SequencePanel(roll, playControl);

        progressStatus.setText("Creating the Control panel");
        progressBar.setValue(6);
        ctrlPanel = new ControlPanel(roll, playControl, canSave);

        progressStatus.setText("Creating the Help panel");
        progressBar.setValue(7);
        helpPanel = new HelpPanel();

        // Set up tabbed pane and add all panels to their own tabs
        progressStatus.setText("Building the interface");
        progressBar.setValue(8);
        addTab("MIDIMatrix", null, homePanel, "Welcome");
        addTab("Tone Matrices", null, gridPanel, "Build your tone matricies");
        addTab("Sequence", null, rollPanel, "Set up your sequence");
        addTab("Controls", null, ctrlPanel, "Set up your MIDI devices and export your work");
        addTab("Help", null, helpPanel, "Help");

        progressStatus.setText("Completed!");
        progressBar.setValue(9);
        setPreferredSize(new Dimension(800, 800));

        addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JTabbedPane source = (JTabbedPane) e.getSource();
                playControl.stop();
                if (source.getSelectedIndex() == 2) {
                    // If we change to the SequencePanel, set the playControl to use the whole sequence
                    try {
                        playControl.setSequence(roll.getSequence());
                    } catch (InvalidMidiDataException exc) {
                        //
                    }
                }
            }
        });

        progressFrame.setVisible(false);
        progressFrame.dispose();
    }
}
