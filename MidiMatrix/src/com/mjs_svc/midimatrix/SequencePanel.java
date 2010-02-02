package com.mjs_svc.midimatrix;

/*
 * MIDIMatrix - Matrix-based MIDI sequencer
 * Copyright (c) 2009 Matthew Scott
 *
 *>This program is free software: you can redistribute it and/or modify
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
import javax.swing.*;
import javax.swing.event.*;
import javax.sound.midi.InvalidMidiDataException;

/**
 * SequencePanel controls the roll and the tools to work with it
 *
 * @author Matthew Scott
 * @version $Id: SequencePanel.java 51 2009-11-18 17:33:20Z drab-makyo $
 */
public class SequencePanel extends JPanel {

    /**
     * Panel containing the visual representation of a Roll
     */
    public class sPanel extends JPanel implements MouseListener {
        // commonly used spaces
        // WHY DOES THIS WORK HERE AND NOT IN MATRIXPANEL?Xldlkfjnasekdnfjeownk

        protected int square = getHeight() / 17;
        protected int extraSpace = (getHeight() - square * 17) / 2;

        /**
         * Construct a new sPanel and set its mouseListener
         */
        public sPanel() {
            addMouseListener(this);
        }

        /**
         * Paint the panel with the roll
         * @param g Graphics object
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            square = getHeight() / 17;
            extraSpace = (getHeight() - square * 17) / 2;

            setBackground(Color.WHITE);

            // draw the first column and row
            g.setColor(Color.BLACK);
            for (int i = 1; i < 17; i++) {
                g.drawRect(extraSpace, extraSpace + square * i, square, square);
                g.setColor(new Color(225, 225, 225));
                g.fillRect(extraSpace + 1, extraSpace + square * i + 1, square - 1, square - 1);
                g.setColor(Color.BLACK);
                if (i < 13) {
                    g.drawString(String.valueOf(i), extraSpace + square / 4, extraSpace + (square * i) + square / 2);
                } else {
                    g.drawString("P" + String.valueOf(i - 12), extraSpace + square / 4, extraSpace + (square * i) + square / 2);
                }
            }

            // Draw each of the segments
            for (int i = 1; i <= roll.size(); i++) {
                g.setColor(Color.BLACK);

                // grab this segment
                boolean[] thisSegment = roll.getSegment(i - 1);

                // draw a header
                g.drawRect(extraSpace + square * i,
                        extraSpace,
                        square,
                        square);
                g.setColor(new Color(225, 225, 225));
                g.fillRect(extraSpace + square * i + 1,
                        extraSpace + 1,
                        square - 1,
                        square - 1);
                g.setColor(Color.BLACK);

                if (selectedSegment == i) {
                    // if this segment is selected, signify that it's selected by highlighting it
                    g.setColor(new Color(190, 190, 190));
                    g.fillRect(extraSpace + square * i + 1,
                            extraSpace + 1,
                            square - 1,
                            square - 1);
                    g.setColor(Color.BLACK);
                    g.drawRect(extraSpace + square * i + 1,
                            extraSpace + 1,
                            square - 2,
                            square * 17 - 1);
                }

                if (extraSpace + square * (i + 1) > getWidth()) {
                    // If this segment goes past the end of the pane, expand the pane
                    setPreferredSize(new Dimension(getWidth() + square * 2 + extraSpace, getHeight()));
                    setSize(new Dimension(getWidth() + square * 2 + extraSpace, getHeight()));
                }

                // label the column header
                g.drawString(String.valueOf(i),
                        extraSpace + (square * i) + square / 3 + 3,
                        extraSpace + square / 2 + 2);

                Color bar = new Color(50, 200, 100);
                Color handle = new Color(30, 235, 134);
                for (int j = 1; j < 17; j++) {
                    // draw the grid for the roll
                    g.drawRect(extraSpace + square * i,
                            extraSpace + square * j,
                            square,
                            square);
                    if (thisSegment[j - 1]) {
                        // if the cell is active, fill it
                        g.setColor(bar);
                        g.fillRect(extraSpace + square * i + 2,
                                extraSpace + square * j + 2,
                                square - 3,
                                square - 3);
                        g.setColor(handle);
                        g.fillRect(extraSpace + square * i + 1 + square / 4,
                                extraSpace + square * j + 1 + square / 4,
                                square / 2,
                                square / 2);
                        g.setColor(Color.BLACK);
                    }
                }
            }

            // Since repaint is called often enough, control enabling/disabling buttons
            if (selectedSegment == 0) {
                removeSegment.setEnabled(false);
                clearSegment.setEnabled(false);
                moveSegmentLeft.setEnabled(false);
                moveSegmentRight.setEnabled(false);
            } else {
                removeSegment.setEnabled(true);
                clearSegment.setEnabled(true);
                moveSegmentLeft.setEnabled(selectedSegment > 1);
                moveSegmentRight.setEnabled(selectedSegment < roll.size());
            }

            // Recompile if need be
            if (playControl.isPlaying()) {
                try {
                    playControl.pause();
                    playControl.setSequence(roll.getSequence());
                    playControl.play();
                } catch(InvalidMidiDataException exc) {
                    //
                }
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        /**
         * Take action when the mouse is released - either toggle the cell, or
         * if mouse was released on the header, select that segment
         * @param e The mouseEvent
         */
        public void mouseReleased(MouseEvent e) {
            Point coordinates = getCellCoordinates(e.getPoint());
            if (coordinates.y > 0) {
                selectedSegment = 0;
                roll.toggleGridInRollSegment(coordinates.x - 1, coordinates.y - 1);
            } else {
                selectedSegment = coordinates.x;
            }
            repaint();
            try {
                playControl.setSequence(roll.getSequence());
            } catch (InvalidMidiDataException exc) {
                JOptionPane.showMessageDialog(
                        null,
                        "Problem compiling MIDI!  Sorry!\n" + exc.getMessage(),
                        "MIDI Error",
                        JOptionPane.ERROR_MESSAGE,
                        null);
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        /**
         * Return a coordinate pair for the grid based on the mouse's coordinates
         * @param p The mouse's coordinates in a Point() object
         * @return A Point() object containing the grid coordinates
         */
        protected Point getCellCoordinates(Point p) {
            return new Point(
                    (int) Math.floor((p.x - extraSpace) / square),
                    (int) Math.floor((p.y - extraSpace) / square));
        }
    }
    private Roll roll;
    private Player playControl;
    private JSpinner tempo;
    private JButton addSegment, clearSegment, removeSegment, moveSegmentRight,
            moveSegmentLeft, play, loop, stop;
    private sPanel sequencePanel;
    private JPanel tools;
    private JScrollPane scroller;
    private int selectedSegment = 0;

    /**
     * Construct a new SequencePanel
     * @param _roll The roll to display, edit, and play
     * @param _playControl An object to control MIDI playback
     */
    public SequencePanel(Roll _roll, Player _playControl) {
        roll = _roll;
        roll.addRollSegment();

        // Set up play controls
        playControl = _playControl;
        try {
            playControl.setSequence(roll.getSequence());
        } catch (InvalidMidiDataException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Problem compiling MIDI!  Sorry!\n" + e.getMessage(),
                    "MIDI Error",
                    JOptionPane.ERROR_MESSAGE,
                    null);
        }

        // set up the tempo spinner
        tempo = new JSpinner(new SpinnerNumberModel(120, 40, 220, 1));
        tempo.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSpinner source = (JSpinner) e.getSource();
                roll.setTempo(((SpinnerNumberModel) source.getModel()).getNumber().intValue());
                playControl.setTempo(((SpinnerNumberModel) source.getModel()).getNumber().intValue());
            }
        });

        addSegment = new JButton("Add");
        addSegment.setToolTipText("Add a frame to the sequence");
        addSegment.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // add a new segment to the roll, recompile, repaint
                roll.addRollSegment();
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
                sequencePanel.repaint();
            }
        });

        removeSegment = new JButton("Remove");
        removeSegment.setToolTipText("Remove the selected frame from the sequence");
        removeSegment.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // remove a segment from the roll, recompile, repaint
                if (selectedSegment > 0) {
                    roll.removeRollSegment(selectedSegment - 1);
                    selectedSegment = 0;
                    try {
                        roll.compile();
                    } catch (Exception exc) {
                        //
                    }
                    sequencePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No frame selected!\nSelect a frame by clicking at its header.",
                            "No frame selected!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        clearSegment = new JButton("Clear");
        clearSegment.setToolTipText("Clear the selected frame");
        clearSegment.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // disable all grids in the roll, recompile, repaint
                if (selectedSegment > 0) {
                    for (int i = 0; i < 16; i++) {
                        roll.disableGridInRollSegment(selectedSegment - 1, i);
                    }
                    selectedSegment = 0;
                    try {
                        roll.compile();
                    } catch (Exception exc) {
                        //
                    }
                    sequencePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No frame selected!\nSelect a frame by clicking at its header.",
                            "No frame selected!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        moveSegmentLeft = new JButton("<<");
        moveSegmentLeft.setToolTipText("Move the selected frame one frame to the left");
        moveSegmentLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (selectedSegment > 1) {
                    roll.moveRollSegment(selectedSegment - 1, selectedSegment - 2);
                    selectedSegment--;
                    try {
                        roll.compile();
                    } catch (Exception exc) {
                        //
                    }
                    sequencePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No frame selected!\nSelect a frame by clicking at its header.",
                            "No frame selected!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        moveSegmentRight = new JButton(">>");
        moveSegmentRight.setToolTipText("Move the selected frame one frame to the right");
        moveSegmentRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (selectedSegment < roll.size() && selectedSegment > 0) {
                    roll.moveRollSegment(selectedSegment - 1, selectedSegment);
                    selectedSegment++;
                    try {
                        roll.compile();
                    } catch (Exception exc) {
                        //
                    }
                    sequencePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No frame selected!\nSelect a frame by clicking at its header.",
                            "No frame selected!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        play = playControl.getPlayButton();
        loop = playControl.getLoopButton();
        stop = playControl.getStopButton();

        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    playControl.setSequence(roll.getSequence());
                } catch(InvalidMidiDataException exc) {
                    //
                }
            }
        });

        loop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    playControl.setSequence(roll.getSequence());
                } catch(InvalidMidiDataException exc) {
                    //
                }
            }
        });

        tools = new JPanel();
        tools.add(new JLabel("Tempo (BPM): "));
        tools.add(tempo);
        tools.add(new JSeparator(SwingConstants.VERTICAL));
        tools.add(play);
        tools.add(loop);
        tools.add(stop);
        tools.add(new JSeparator(SwingConstants.VERTICAL));
        tools.add(addSegment);
        tools.add(removeSegment);
        tools.add(clearSegment);
        tools.add(moveSegmentLeft);
        tools.add(moveSegmentRight);
        tools.setPreferredSize(new Dimension(800, 50));

        // set up the sPanel and put it in a scroller
        sequencePanel = new sPanel();
        sequencePanel.setPreferredSize(new Dimension(800, 600));
        scroller = new JScrollPane(sequencePanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(new Dimension(800, 620));

        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tools, scroller);

        add(pane);
    }
}
