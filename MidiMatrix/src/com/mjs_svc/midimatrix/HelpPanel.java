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
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

/**
 * Offer help to the user on using MIDIMatrix
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class HelpPanel extends JPanel {

    public class HelpItem {

        private String name;
        private String hash;

        public HelpItem(String _name, String _hash) throws MalformedURLException {
            name = _name;
            hash = _hash;
        }

        public void setName(String _name) {
            name = _name;
        }

        @Override
        public String toString() {
            return name;
        }

        public void setHash(String _hash) throws MalformedURLException {
            hash = _hash;
        }

        public String getHash() {
            return hash;
        }
    }
    private JEditorPane helpPane;
    private JTree helpTree;
    private JScrollPane treePane, scroller;
    private JSplitPane content;
    private DefaultMutableTreeNode top, category;

    /**
     * Construct a new Help panel
     */
    public HelpPanel() {
        // Try to build our help tree, keeping an eye out for malformed URLs
        try {
            // root node
            top = new DefaultMutableTreeNode(new HelpItem("MIDIMatrix", "top"));

            // matrices node
            category = new DefaultMutableTreeNode(new HelpItem("Matrices", "matrices"));
            top.add(category);
            category.add(new DefaultMutableTreeNode(new HelpItem("Note Entry", "matrices-noteentry")));
            category.add(new DefaultMutableTreeNode(new HelpItem("Pitches, volume, and instruments", "matrices-metadata")));
            category.add(new DefaultMutableTreeNode(new HelpItem("Playback", "matrices-playback")));

            // sequences node
            category = new DefaultMutableTreeNode(new HelpItem("Sequences", "sequences"));
            top.add(category);
            category.add(new DefaultMutableTreeNode(new HelpItem("Parts of a sequence", "sequences-parts")));
            category.add(new DefaultMutableTreeNode(new HelpItem("Constructing a sequence", "sequences-construction")));
            category.add(new DefaultMutableTreeNode(new HelpItem("Playback", "sequences-playback")));

            // controls node
            category = new DefaultMutableTreeNode(new HelpItem("Controls", "controls"));
            top.add(category);
            category.add(new DefaultMutableTreeNode(new HelpItem("MIDI devices", "controls-mididevices")));
            category.add(new DefaultMutableTreeNode(new HelpItem("Saving", "controls-saving")));

            // about node
            category = new DefaultMutableTreeNode(new HelpItem("About", "about"));
            top.add(category);
            category.add(new DefaultMutableTreeNode(new HelpItem("Author", "about-author")));
            category.add(new DefaultMutableTreeNode(new HelpItem("License", "about-license")));

            // javadoc node
            //category = new DefaultMutableTreeNode(new HelpItem("JavaDoc", base + "javadoc/"));
            //top.add(category);
        } catch (MalformedURLException e) {
            top = new DefaultMutableTreeNode("ERROR");
        }
        helpTree = new JTree(top);
        helpTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        helpTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) helpTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                //try {
                    helpPane.scrollToReference(((HelpItem) node.getUserObject()).getHash());
                /*} catch (IOException exc) {

                    JOptionPane.showMessageDialog(
                            null,
                            "There was a problem fetching the help page " + exc.getMessage() + "\n" +
                            "Make sure you're connected to the internet before continuing",
                            "Help Oops!",
                            JOptionPane.ERROR_MESSAGE,
                            null);
                }*/
            }
        });


        treePane = new JScrollPane(helpTree);
        treePane.setPreferredSize(new Dimension(200, 750));

        helpPane = new JEditorPane();
        scroller = new JScrollPane(helpPane);
        helpPane.setEditable(false);
        helpPane.setPreferredSize(new Dimension(550, 750));
        helpPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    JEditorPane pane = (JEditorPane) e.getSource();
                    if (e instanceof HTMLFrameHyperlinkEvent) {
                        HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                        HTMLDocument doc = (HTMLDocument) pane.getDocument();
                        doc.processHTMLFrameHyperlinkEvent(evt);
                        if (e.getDescription().indexOf("#") != -1) {
                            System.err.println("Found anchor");
                            pane.scrollToReference(e.getDescription().substring(e.getDescription().indexOf("#")));
                        }
                    } else {
                        try {
                            pane.setPage(e.getURL());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }

                }
            }
        });
        try {
            helpPane.setPage(HelpPanel.class.getResource("help.xhtml"));
        } catch (IOException e) {
            helpPane.setText("Couldn't fetch help page, sorry!  " + e.getMessage());
        }

        content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.add(treePane);
        content.add(scroller);

        add(content);
    }
}
