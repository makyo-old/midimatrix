package com.mjs_svc.midimatrix;

import javax.swing.*;
import java.awt.event.*;

/**
 * MIDIMatrix standalone application
 * <p>A matrix-based MIDI sequencer - construct matrices of tones and string
 * those matrices together into a sequence to compose your own music.</p>
 *
 * <p>MIDIMatrix - Matrix-based MIDI sequencer
 * Copyright (c) 2009 Matthew Scott</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU General Public License
 * along with this program.  If not, see < http://www.gnu.org/licenses/ >.</p>
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class MidiMatrix extends JFrame {

    private MidiMatrixInternalFrame pane;

    /**
     * Construct a new window with a MidiMatrixPane in it
     */
    public MidiMatrix() {
        super("MIDIMatrix");
        pane = new MidiMatrixInternalFrame(true);
        setContentPane(pane);
        pack();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                MidiMatrix.this.setVisible(false);
                MidiMatrix.this.dispose();
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        new MidiMatrix().setVisible(true);
    }
}
