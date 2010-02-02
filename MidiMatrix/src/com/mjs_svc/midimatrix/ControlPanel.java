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
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.sound.midi.*;

/**
 * ControlPanel houses all the controls for the application, such as devices
 * and file saving
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class ControlPanel extends JPanel {

    private Player playControl;
    private Roll roll;
    private JLabel deviceExplanation, deviceLabel, saveExplanation, saveLabel;
    private JComboBox deviceList;
    private JButton saveButton;
    private boolean canSave;
    private SpringLayout layout;

    /**
     * Construct a new ControlPanel
     * @param _roll The roll to modify
     * @param _playControl The play controls to modify
     * @param _canSave Whether or not the user can save
     */
    public ControlPanel(Roll _roll, Player _playControl, boolean _canSave) {
        layout = new SpringLayout();
        setLayout(layout);
        playControl = _playControl;
        roll = _roll;
        canSave = _canSave;

        // Grab all available midi devices and load them into the combo box if they have a Receiver
        MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
        deviceList = new JComboBox();
        MidiDevice deviceIter;
        for (int i = 0; i < devices.length; i++) {
            deviceIter = null;
            try {
                deviceIter = MidiSystem.getMidiDevice(devices[i]);
                if (deviceIter.getReceiver() instanceof Receiver) {
                    deviceList.addItem(devices[i]);
                }
            } catch (Exception e) {
            }

            // If the device is a Synthesizer (i.e.: default to Java Synth), select it
            if (deviceIter instanceof Synthesizer) {
                deviceList.setSelectedItem(devices[i]);
            }
        }
        // when the device is changed, change the player's synthesizer
        deviceList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox source = (JComboBox) e.getSource();
                playControl.setSynthesizer((MidiDevice.Info) source.getSelectedItem());
            }
        });

        saveButton = new JButton("Save MIDI file...");
        if (canSave) {
            try {
                // If we can save, add an action listener
                saveButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        JFileChooser saveDialog = new JFileChooser();

                        // Get the file from the user we want to save to
                        int returnVal = saveDialog.showSaveDialog(saveButton);

                        // build a sequence to write
                        Sequence s;
                        try {
                            s = roll.getSequence();
                        } catch (InvalidMidiDataException exc) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Problem compiling the midi: " + exc.getMessage(),
                                    "MIDI Oops",
                                    JOptionPane.ERROR_MESSAGE,
                                    null);
                            return;
                        }

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            // If the user clicked okay, continue using the specified file
                            File f = saveDialog.getSelectedFile();
                            if (!f.isDirectory()) {

                                // If the file is not a directory. check that we can write to it okay
                                if (f.exists() && f.canWrite()) {
                                    int existsAction = JOptionPane.showOptionDialog(
                                            saveButton,
                                            "That file already exists!  Overwrite?",
                                            "File Exists!",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.WARNING_MESSAGE,
                                            null, null, null);
                                    if (existsAction == JOptionPane.NO_OPTION) {
                                        return;
                                    }
                                }

                                // For sanity's sake, make sure we want to continue of the filename doesn't end with 'mid' - this could screw up some folk
                                if (!f.getName().substring(f.getName().length() - 3).toLowerCase().equals("mid")) {
                                    int extensionAction = JOptionPane.showOptionDialog(
                                            saveButton,
                                            "You gave a filename that does not end in 'mid'.\n" +
                                            "This file may not be associated with the proper default action on your computer\n" +
                                            "Do you wish to continue?",
                                            "File Extension Mismatch",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.WARNING_MESSAGE,
                                            null, null, null);
                                    if (extensionAction == JOptionPane.NO_OPTION) {
                                        return;
                                    }
                                }

                                // Get the MIDI filetypes from the MIDI system
                                int[] allowedTypes = MidiSystem.getMidiFileTypes(s);
                                if (allowedTypes.length == 0) {
                                    JOptionPane.showMessageDialog(
                                            null,
                                            "Problem saving midi: MidiSystem couldn't find the proper type to save the file as",
                                            "Error Saving",
                                            JOptionPane.ERROR_MESSAGE,
                                            null);
                                    return;
                                } else {

                                    // Try to write the file, warn if it doesn't work
                                    try {
                                        MidiSystem.write(s, allowedTypes[0], f);
                                    } catch (IOException exc) {
                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Problem saving midi: " + exc.getMessage(),
                                                "Error Saving",
                                                JOptionPane.ERROR_MESSAGE,
                                                null);
                                        return;
                                    }

                                    // Notify on success
                                    JOptionPane.showMessageDialog(
                                            null,
                                            "MIDI file saved!",
                                            "Saved",
                                            JOptionPane.PLAIN_MESSAGE,
                                            null);
                                }
                            }
                        }
                    }
                });
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(
                        null,
                        "Problem preparing save functionality: " + exc.getMessage() + "\n" +
                        "Saving is disabled",
                        "Error Saving",
                        JOptionPane.ERROR_MESSAGE,
                        null);

            }
        } else {
            // Otherwise, disable the button
            saveButton.setEnabled(false);
            saveButton.setText("Saving disabled");
        }

        // Add the MIDI device list with an explanation
        deviceExplanation = new JLabel("<html><font size=\"+1\">Select MIDI Device</font>" +
                "<p>If you are having trouble playing matrices or the sequence, you can try " +
                "changing the MIDI device below. <br />Keep in mind that not all devices" +
                "will work, as not all are intended for MIDI playback!</p></html>");
        deviceLabel = new JLabel("Select MIDI output device:");
        add(deviceExplanation);
        add(deviceLabel);
        add(deviceList);
        layout.putConstraint(SpringLayout.NORTH, deviceExplanation, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, deviceExplanation, 25, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, deviceLabel, 10, SpringLayout.SOUTH, deviceExplanation);
        layout.putConstraint(SpringLayout.WEST, deviceLabel, 50, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, deviceList, 10, SpringLayout.SOUTH, deviceExplanation);
        layout.putConstraint(SpringLayout.WEST, deviceList, 10, SpringLayout.EAST, deviceLabel);

        // Add the save button with an explanation
        saveExplanation = new JLabel("<html><font size=\"+1\">Save Sequence</font>" +
                "<p>If you would like to save the sequence that you have built and if " +
                "saving is enabled in the current environment <br />(stand-alone applicattion: yes," +
                "Java Applet: no), you may do so here.  " + (canSave ? "You can save." : "You <i>cannot</i> save.") + "</html>");
        saveLabel = new JLabel("Save this sequence:");
        add(saveExplanation);
        add(saveLabel);
        add(saveButton);
        layout.putConstraint(SpringLayout.NORTH, saveExplanation, 50, SpringLayout.SOUTH, deviceList);
        layout.putConstraint(SpringLayout.WEST, saveExplanation, 25, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, saveLabel, 10, SpringLayout.SOUTH, saveExplanation);
        layout.putConstraint(SpringLayout.EAST, saveLabel, 0, SpringLayout.EAST, deviceLabel);
        layout.putConstraint(SpringLayout.NORTH, saveButton, 10, SpringLayout.SOUTH, saveExplanation);
        layout.putConstraint(SpringLayout.WEST, saveButton, 10, SpringLayout.EAST, saveLabel);

        setPreferredSize(new Dimension(750, 300));
    }
}
