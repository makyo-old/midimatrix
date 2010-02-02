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
import javax.swing.*;
import java.util.*;
import javax.swing.event.*;
import javax.sound.midi.*;

/**
 * MatrixPanel contains the matrices and the tools to work with them.
 *
 * @author Matthew Scott
 * @version $Id: MatrixPanel.java 52 2009-11-19 21:59:51Z drab-makyo $
 */
public class MatrixPanel extends JPanel {

    /**
     * Panel containing the visual representation of a Grid
     */
    protected class mPanel extends JPanel implements MouseListener {

        // POLLY SHOULDN'T BE!
        // Seriously, if I set these as attributes instead of methods, paintComponent can't see them
        /**
         * Use integer math to get 1/16 the width of the panel
         * @return 1/16 the width of the panel
         */
        private int sixteenthWidth() {
            return this.getWidth() / 16;
        }

        /**
         * Use integer math to get 1/16 the height of the panel
         * @return 1/16 the height of the panel
         */
        private int sixteenthHeight() {
            return this.getHeight() / 16;
        }

        /**
         * Get the remainder of the height after dividing it into 16ths
         * @return The remaining height
         */
        private int heightStart() {
            return (this.getHeight() - sixteenthHeight() * 16) / 2;
        }

        /**
         * Get the remainder of the width after dividing it into 16ths
         * @return The remaining width
         */
        private int widthStart() {
            return (this.getWidth() - sixteenthWidth() * 16) / 2;
        }
        private Point toggleStart = null, toggleEnd = null;
        private boolean alreadyToggled = false;

        /**
         * Paint the panel with the grid
         * @param g Graphics object
         */
        @Override
        public void paintComponent(Graphics g) {
            // Draw the panel
            super.paintComponent(g);

            // When I set these as class attributes, they come up as zero in this method, thus calling a function.
            // Don't know why.
            int h = sixteenthHeight(), w = sixteenthWidth();
            int sH = heightStart(), sW = widthStart();

            // Set the background
            setBackground(Color.WHITE);

            // colors to use
            Color bar = new Color(30, 144, 255);
            Color handle = new Color(60, 174, 255);

            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    g.setColor(Color.BLACK);
                    if (!roll.gridNoteStatus(activeGrid, x, y) || roll.gridNoteLength(activeGrid, x, y) == 1) {
                        // Draw the grid square at the coordinates given if there's no note
                        g.drawRect(sW + (w * x), // the start width + the width of a square times the x coord
                                sH + (h * y), // the start height + the height of a square times the y coord
                                w, // the width
                                h); // the height
                    } else {
                        // Otherwise, just draw the bottom line
                        g.drawLine(sW + (w * x), // the start width + the width of a square times the x coord
                                sH + (h * (y + 1)), // the start height + the height of a square times one more than the y coord (for the bottom)
                                sW + (w * (x + 1)), // the start width + the width of a square times the x coord, one cell on
                                sH + (h * (y + 1))); // as above
                    }
                    if (roll.gridNoteLength(activeGrid, x, y) > 0 && roll.gridNoteStatus(activeGrid, x, y)) {
                        // draw a bar to signify the note and its duration
                        g.setColor(bar);
                        g.fillRect(sW + (w * x) + 2,
                                sH + (h * y) + 2,
                                w * roll.gridNoteLength(activeGrid, x, y) - 3,
                                h - 3);

                        // draw a lighter rect at the first cell as that note's handle (i.e.: for aesthetics, toggling, etc)
                        g.setColor(handle);
                        g.fillRect(sW + (w * x) + 1 + w / 4,
                                sH + (h * y) + 1 + h / 4,
                                w / 2,
                                h / 2);
                        // draw another rect in case the old one got clobbered
                        g.setColor(Color.BLACK);
                        g.drawRect(sW + (w * x),
                                sH + (h * y),
                                w * roll.gridNoteLength(activeGrid, x, y),
                                h);
                    }
                }
            }
            alreadyToggled = false;

            // Recompile the grid and reset the sequence used in the play controller
            if (playControl.isPlaying()) {
                try {
                    playControl.pause();
                    playControl.setSequence(roll.compileGrid(activeGrid));
                    playControl.play();
                } catch (InvalidMidiDataException exc) {
                    //
                }
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        /**
         * Start toggling cells on the grid by setting toggleStart
         * @param e MouseEvent received
         */
        public void mousePressed(MouseEvent e) {
            toggleStart = getCellCoordinates(e.getPoint());
        }

        /**
         * Stop toggling cells on the grid by setting toggleEnd, making sure to
         * handle toggling a single cell
         * @param e MouseEvent received
         */
        public void mouseReleased(MouseEvent e) {
            toggleEnd = getCellCoordinates(e.getPoint());
            if (toggleEnd.equals(toggleStart)) {
                mouseClicked(e);
                return;
            } else {
                // support backwards dragging
                int startx = (toggleStart.x > toggleEnd.x ? toggleEnd.x : toggleStart.x);
                int endx = (toggleStart.x > toggleEnd.x ? toggleStart.x : toggleEnd.x);
                int starty = (toggleStart.y > toggleEnd.y ? toggleEnd.y : toggleStart.y);
                int endy = (toggleStart.y > toggleEnd.y ? toggleStart.y : toggleEnd.y);
                for (int i = starty; i <= endy; i++) {
                    roll.gridToggleNote(activeGrid, startx, i, endx - startx);
                }
                alreadyToggled = true;
            }

            // repaint to toggle the square
            repaint();
        }

        /**
         * Use this to toggle a single cell
         * @param e MouseEvent received
         */
        public void mouseClicked(MouseEvent e) {
            if (!alreadyToggled) {
                Point cell = getCellCoordinates(e.getPoint());
                roll.gridToggleNote(activeGrid, cell.x, cell.y);
                alreadyToggled = true;

                // repaint to toggle the square
                repaint();
            }
        }

        /**
         * Get the cell's coordinates based on the mouse-pointer's coordinates
         * @param p The mouse's coordinates on the panel
         * @return a Point object containing the coordinates
         */
        public Point getCellCoordinates(Point p) {
            return new Point((int) Math.floor((p.x - widthStart()) / sixteenthWidth()),
                    (int) Math.floor((p.y - heightStart()) / sixteenthHeight()));
        }
    }
    private Player playControl;
    private int activeGrid;
    private Roll roll;
    private JPanel matrixSelector, tools;
    private mPanel matrix;
    private SpringLayout toolsLayout;
    private JComboBox instrument, scale;
    private JSlider velocity, key;
    private JButton octaveUp, octaveDown, clearMatrix, play, loop, stop;
    private final JButton[] matrices;
    private JLabel currentKey, instLabel, scaleLabel, keyLabel, octaveLabel, velLabel;

    /**
     * Construct a new MatrixPanel with a Roll associated with it
     * @param _roll A Roll to control
     */
    public MatrixPanel(Roll _roll, Player _playControl) {
        roll = _roll;
        playControl = _playControl;
        activeGrid = 0;

        // set up the layout
        //setLayout(new GridBagLayout());
        //GridBagConstraints c = new GridBagConstraints();
        //c.fill = GridBagConstraints.BOTH;


        // set up the matrix selector panel
        matrixSelector = new JPanel(new GridLayout(2, 8));
        matrixSelector.setPreferredSize(new Dimension(800, 100));
        matrixSelector.setMinimumSize(new Dimension(800, 75)); // try this...

        // set up the tools panel
        tools = new JPanel();
        toolsLayout = new SpringLayout();
        tools.setLayout(toolsLayout);
        tools.setPreferredSize(new Dimension(225, 575));
        tools.setMinimumSize(new Dimension(210, 500));

        // set up the matrix panel
        matrix = new mPanel();
        matrix.setPreferredSize(new Dimension(500, 575));
        matrix.addMouseListener(matrix);

        // populate the matrix selector
        matrices = new JButton[16];
        for (int i = 0; i < 16; i++) {
            // make sure we have as many active grids as selectors
            if (i < 12) {
                matrices[i] = new JButton(String.valueOf(i + 1));
                roll.enableGrid(Scale.MAJOR_PENTATONIC, 0, 60, 64);
            } else {
                matrices[i] = new JButton(
                        String.valueOf(i - 11),
                        new ImageIcon(getClass().getResource("drumico.gif")));
                roll.enablePercussionGrid(PercussionGrid.lowBound, 64);
            }
            matrixSelector.add(matrices[i]);

            // Add a listener to each button to change the matrices
            matrices[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (playControl.isPlaying()) {
                        // if we're playing, stop
                        playControl.stop();
                    }
                    for (int n = 0; n < matrices.length; n++) {
                        if (matrices[n].equals(e.getSource())) {
                            // Set up the matrixSelector for the new matrix
                            matrices[activeGrid].setEnabled(true);
                            activeGrid = n;
                            matrices[n].setEnabled(false);

                            // repaint the matrix
                            matrix.repaint();

                            // set the metadata to correspond to the new matrix
                            instrument.setSelectedIndex(roll.gridGetInstrument(activeGrid));
                            scale.setSelectedItem(roll.gridGetScale(activeGrid));
                            if (activeGrid < 12) {
                                scale.setEnabled(true);
                                instrument.setEnabled(true);
                            } else {
                                scale.setEnabled(false);
                                instrument.setEnabled(false);
                            }
                            velocity.setValue(roll.gridGetVelocity(activeGrid));
                            key.setValue(roll.gridGetKey(activeGrid) % 12);
                            currentKey.setText("Key: " + Scale.getNoteNameByNumber(roll.gridGetKey(activeGrid)));

                            // Tell playControl to play only this grid
                            try {
                                playControl.setSequence(roll.compileGrid(activeGrid));
                            } catch (Exception exc) {
                                //
                            }

                            tools.repaint();
                            break;
                        }
                    }
                }
            });
        }

        // First matrix selected by default
        matrices[0].setEnabled(false);

        // populate the tools panel and spring everything together

        // start with instrument dropdown
        instrument = new JComboBox(roll.gridGetInstrumentList(0));

        // listen for a change and set the instrument accordingly, then recompile
        instrument.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox _instrument = (JComboBox) e.getSource();
                roll.gridSetInstrument(activeGrid, (int) _instrument.getSelectedIndex());
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
            }
        });

        // Scale dropdown
        scale = new JComboBox();
        scale.addItem(Scale.MAJOR_PENTATONIC);
        scale.addItem(Scale.MINOR_PENTATONIC);
        scale.addItem(Scale.IONIAN);
        scale.addItem(Scale.AEOLIAN);
        scale.addItem(Scale.ARABIC);
        scale.addItem(Scale.WHOLE_TONE);
        scale.addItem(Scale.OCTATONIC);
        scale.addItem(Scale.CHROMATIC);

        // listen for a change and set the scale accordingly, then recompile
        scale.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox _scale = (JComboBox) e.getSource();
                roll.gridSetScale(activeGrid, (Scale) _scale.getSelectedItem());
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
            }
        });

        // Volume slider
        velocity = new JSlider(JSlider.VERTICAL, 0, 127, 64);
        velocity.setSnapToTicks(true);

        // listen for a change and set the volume accordingly, then recompile
        velocity.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    roll.gridSetVelocity(activeGrid, source.getValue());
                }
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
            }
        });

        // Add play-control buttons
        play = playControl.getPlayButton();
        loop = playControl.getLoopButton();
        stop = playControl.getStopButton();

        // Add an additional action listener to the play button so that it uses only the grid
        play.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    playControl.setSequence(roll.compileGrid(activeGrid));
                } catch (InvalidMidiDataException exc) {
                    //
                }
            }
        });

        // Add an additional action listener to the loop button so that it uses only the grid
        loop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    playControl.setSequence(roll.compileGrid(activeGrid));
                } catch (InvalidMidiDataException exc) {
                    //
                }
            }
        });

        // Key slider
        key = new JSlider(JSlider.HORIZONTAL, 0, 11, 0);
        key.setMajorTickSpacing(1);
        key.setPaintTicks(true);
        key.setSnapToTicks(true);
        Hashtable<Integer, JLabel> keys = new Hashtable<Integer, JLabel>(7);
        keys.put(new Integer(0), new JLabel("C"));
        keys.put(new Integer(2), new JLabel("D"));
        keys.put(new Integer(4), new JLabel("E"));
        keys.put(new Integer(5), new JLabel("F"));
        keys.put(new Integer(7), new JLabel("G"));
        keys.put(new Integer(9), new JLabel("A"));
        keys.put(new Integer(11), new JLabel("B"));
        key.setLabelTable(keys);
        key.setPaintLabels(true);

        // Listen for a change and set the key accordingly, then recompile
        key.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    roll.gridSetKey(activeGrid, (int) (roll.gridGetKey(activeGrid) / 12) * 12 + source.getValue());
                    currentKey.setText("Key: " + Scale.getNoteNameByNumber(roll.gridGetKey(activeGrid)));
                }
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
            }
        });

        // Listen for a change on the octave buttons, then set the octave accordinlgy and recompile
        ActionListener octaveListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(octaveUp)) {
                    roll.gridSetKey(activeGrid, roll.gridGetKey(activeGrid) + 12);
                } else {
                    roll.gridSetKey(activeGrid, roll.gridGetKey(activeGrid) - 12);
                }
                currentKey.setText("Key: " + Scale.getNoteNameByNumber(roll.gridGetKey(activeGrid)));
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
            }
        };

        // Octave buttons
        octaveUp = new JButton("Up");
        octaveUp.addActionListener(octaveListener);
        octaveDown = new JButton("Down");
        octaveDown.addActionListener(octaveListener);
        currentKey = new JLabel("Key: " + Scale.getNoteNameByNumber(roll.gridGetKey(activeGrid)));

        // Clear the selected matrix button
        clearMatrix = new JButton("Clear Matrix");

        // Listen for a change, warn, and clear
        clearMatrix.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showOptionDialog(null, "You're about to clear this matrix - this cannot be undone!\n" + "Are you sure you want to continue?", "Clear Matrix", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, null, null);
                if (confirm == JOptionPane.YES_OPTION) {
                    roll.clearGrid(activeGrid);
                    matrix.repaint();
                }
                try {
                    roll.compile();
                } catch (Exception exc) {
                    //
                }
            }
        });

        // Add things to the tool panel and position apropriately
        instLabel = new JLabel("Matrix Instrument");
        instLabel.setLabelFor(instrument);
        tools.add(instLabel);
        toolsLayout.putConstraint(SpringLayout.WEST, instLabel, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, instLabel, 5, SpringLayout.NORTH, tools);

        tools.add(instrument);
        toolsLayout.putConstraint(SpringLayout.WEST, instrument, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, instrument, 5, SpringLayout.SOUTH, instLabel);

        scaleLabel = new JLabel("Matrix Scale:");
        scaleLabel.setLabelFor(scale);
        tools.add(scaleLabel);
        toolsLayout.putConstraint(SpringLayout.WEST, scaleLabel, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, scaleLabel, 15, SpringLayout.SOUTH, instrument);

        tools.add(scale);
        toolsLayout.putConstraint(SpringLayout.WEST, scale, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, scale, 5, SpringLayout.SOUTH, scaleLabel);

        keyLabel = new JLabel("Matrix Key:");
        keyLabel.setLabelFor(key);
        tools.add(keyLabel);
        toolsLayout.putConstraint(SpringLayout.WEST, keyLabel, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, keyLabel, 15, SpringLayout.SOUTH, scale);

        tools.add(key);
        toolsLayout.putConstraint(SpringLayout.WEST, key, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, key, 5, SpringLayout.SOUTH, keyLabel);

        octaveLabel = new JLabel("Matrix Octave:");
        octaveLabel.setLabelFor(octaveUp);
        tools.add(octaveLabel);
        toolsLayout.putConstraint(SpringLayout.WEST, octaveLabel, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, octaveLabel, 15, SpringLayout.SOUTH, key);

        tools.add(octaveDown);
        tools.add(octaveUp);
        tools.add(currentKey);
        toolsLayout.putConstraint(SpringLayout.WEST, octaveDown, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.WEST, octaveUp, 0, SpringLayout.EAST, octaveDown);
        toolsLayout.putConstraint(SpringLayout.WEST, currentKey, 5, SpringLayout.EAST, octaveUp);
        toolsLayout.putConstraint(SpringLayout.NORTH, octaveDown, 5, SpringLayout.SOUTH, octaveLabel);
        toolsLayout.putConstraint(SpringLayout.NORTH, octaveUp, 5, SpringLayout.SOUTH, octaveLabel);
        toolsLayout.putConstraint(SpringLayout.NORTH, currentKey, 10, SpringLayout.SOUTH, octaveLabel);

        velLabel = new JLabel("Matrix Volume:");
        velLabel.setLabelFor(velocity);
        tools.add(velLabel);
        toolsLayout.putConstraint(SpringLayout.WEST, velLabel, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, velLabel, 15, SpringLayout.SOUTH, octaveDown);

        tools.add(velocity);
        toolsLayout.putConstraint(SpringLayout.WEST, velocity, 5, SpringLayout.WEST, tools);
        toolsLayout.putConstraint(SpringLayout.NORTH, velocity, 5, SpringLayout.SOUTH, velLabel);

        tools.add(play);
        tools.add(loop);
        tools.add(stop);
        toolsLayout.putConstraint(SpringLayout.WEST, play, 20, SpringLayout.EAST, velocity);
        toolsLayout.putConstraint(SpringLayout.NORTH, play, 20, SpringLayout.SOUTH, velLabel);
        toolsLayout.putConstraint(SpringLayout.WEST, loop, 20, SpringLayout.EAST, velocity);
        toolsLayout.putConstraint(SpringLayout.NORTH, loop, 10, SpringLayout.SOUTH, play);
        toolsLayout.putConstraint(SpringLayout.WEST, stop, 20, SpringLayout.EAST, velocity);
        toolsLayout.putConstraint(SpringLayout.NORTH, stop, 10, SpringLayout.SOUTH, loop);

        tools.add(clearMatrix);
        toolsLayout.putConstraint(SpringLayout.EAST, clearMatrix, -10, SpringLayout.EAST, tools);
        toolsLayout.putConstraint(SpringLayout.SOUTH, clearMatrix, -10, SpringLayout.SOUTH, tools);

        // finally, add everything to the panel
        JSplitPane horizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tools, matrix);
        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontal, matrixSelector);
        add(vertical);
    }
}
