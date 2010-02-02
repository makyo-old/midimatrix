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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;

/**
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class HomePanel extends JPanel {
    private JLabel intro, splash, attrib;

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        intro = new JLabel("<html><font size=\"+1\" color=\"blue\"><strong>Welcome to MIDIMatrix.</strong></font><br /><br />" +
                "MidiMatrix is a tone-matrix based MIDI sequencer.  " +
                "This allows the user to write small, reusable chunks of music at a time,<br />" +
                "then string them together into a complete song.</html>");
        splash = new JLabel(new ImageIcon(getClass().getResource("MIDIMatrix-splash.gif")));
        attrib = new JLabel("Written for CS150 by Matthew Scott, 2009");

        add(intro, BorderLayout.NORTH);
        add(splash, BorderLayout.CENTER);
        add(attrib, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(790, 500));
    }
}
