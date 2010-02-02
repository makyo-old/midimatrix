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
import java.beans.PropertyVetoException;
import javax.swing.*;

/**
 * The base desktop for MidiMatrix (using JInternalFrames)
 *
 * @author Matthew Scott
 * @version $Id: MidiMatrixInternalFrame.java 45 2009-10-06 19:29:01Z drab-makyo $
 */
public class MidiMatrixInternalFrame extends JPanel {
    private Roll roll;
    private Player playControl;
    private JPanel homePanel, gridPanel, rollPanel, ctrlPanel, helpPanel, progressPanel;
    private final JInternalFrame homeFrame, gridFrame, rollFrame, ctrlFrame, helpFrame;
    private final JDesktopPane desktop;
    private JToolBar toolBar;
    private JButton homeButton, gridButton, rollButton, ctrlButton, helpButton;
    private JProgressBar progressBar;
    private JFrame progressFrame;
    private JLabel progressStatus;
    private boolean canSave = false;

    /**
     * Construct a new MidiMatrixInternalFrame
     * @param _canSave Whether the user can save or not
     */
    public MidiMatrixInternalFrame (boolean _canSave) {
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
        setLayout(new BorderLayout());
        desktop = new JDesktopPane();
        homeFrame = new JInternalFrame(
                "MIDIMatrix",
                false, // resizeable
                true, // closable
                false, // maximizable
                false); // iconifiable
        homeFrame.setContentPane(homePanel);
        homeFrame.setVisible(true);
        homeFrame.pack();
        homeFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        desktop.add(homeFrame);

        gridFrame = new JInternalFrame(
                "Tone Matrices",
                true, // resizable
                false, // closable
                true, // maximizable
                true); // iconifiable
        gridFrame.setContentPane(gridPanel);
        gridFrame.setVisible(true);
        gridFrame.pack();
        desktop.add(gridFrame);

        rollFrame = new JInternalFrame(
                "Sequence",
                true, // resizable
                false, // closable
                true, // maximizable
                true); // iconifiable
        rollFrame.setContentPane(rollPanel);
        rollFrame.setVisible(true);
        rollFrame.pack();
        desktop.add(rollFrame);

        ctrlFrame = new JInternalFrame(
                "Controls",
                false, // resizable
                false, // closable
                false, // maximizable
                true); // iconifiable
        ctrlFrame.setContentPane(ctrlPanel);
        ctrlFrame.setVisible(true);
        ctrlFrame.pack();
        desktop.add(ctrlFrame);

        helpFrame = new JInternalFrame(
                "Help",
                true, // resizable
                true, // closable
                true, // maximizable
                true); // iconifiable
        helpFrame.setContentPane(helpPanel);
        //helpFrame.setVisible(true);
        helpFrame.pack();
        helpFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        desktop.add(helpFrame);
        try {
            homeFrame.setSelected(true);
        } catch(PropertyVetoException e) {
            //
        }

        // set up the tool bar and set each button to switch to that frame
        toolBar = new JToolBar();
        homeButton = new JButton();
        class homeAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                try {
                    homeFrame.setVisible(true);
                    homeFrame.setSelected(true);
                } catch (Exception exc) {
                    //
                }
            }
        }
        homeButton.setAction(new homeAction());
        homeButton.setText("Welcome");
        //toolBar.add(homeButton);

        gridButton = new JButton();
        class gridAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                try {
                    gridFrame.setVisible(true);
                    gridFrame.setSelected(true);
                } catch (Exception exc) {
                    //
                }
            }
        }
        gridButton.setAction(new gridAction());
        gridButton.setText("Tone Matrices");
        toolBar.add(gridButton);

        rollButton = new JButton();
        class rollAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                try {
                    rollFrame.setVisible(true);
                    rollFrame.setSelected(true);
                } catch (Exception exc) {
                    //
                }
            }
        }
        rollButton.setAction(new rollAction());
        rollButton.setText("Sequence");
        toolBar.add(rollButton);

        ctrlButton = new JButton();
        class ctrlAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                try {
                    ctrlFrame.setVisible(true);
                    ctrlFrame.setSelected(true);
                } catch (Exception exc) {
                    //
                }
            }
        }
        ctrlButton.setAction(new ctrlAction());
        ctrlButton.setText("Controls");
        toolBar.add(ctrlButton);

        helpButton = new JButton();
        class helpAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                try {
                    helpFrame.setVisible(true);
                    helpFrame.setSelected(true);
                } catch (Exception exc) {
                    //
                }
            }
        }
        helpButton.setAction(new helpAction());
        helpButton.setText("Help");
        toolBar.add(helpButton);

        // Add the toolbar and desktop to the panel
        add(toolBar, BorderLayout.PAGE_START);
        add(desktop, BorderLayout.CENTER);

        // Finish up with the progress bar
        progressStatus.setText("Completed!");
        progressBar.setValue(9);
        setPreferredSize(new Dimension(850, 850));

        progressFrame.setVisible(false);
        progressFrame.dispose();

        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homeFrame.setVisible(false);
            }
        };
        new Timer(5000, taskPerformer).start();
    }
}
