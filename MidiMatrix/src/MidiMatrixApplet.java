
import javax.swing.*;
import java.awt.*;
import com.mjs_svc.midimatrix.*;

/**
 * <p>MIDIMatrix applet - a web applet interface to the matrix-based MIDI sequencer
 * written for CS150, FA09 - Colorado State University<br />
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
public class MidiMatrixApplet extends JApplet {
    private MidiMatrixTabbedPane pane;

    /**
     * Build the Applet, hope sizes are set.
     */
    @Override
    public void init() {
        pane = new MidiMatrixTabbedPane(false);
        add(pane);
        setSize(new Dimension(800, 800));
    }
}
