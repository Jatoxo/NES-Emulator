package emu.gui;

import emu.nes.Nes;

import javax.swing.*;
import java.awt.*;

public class PatternViewWindow extends JFrame {

    private Nes nes;

    private PatternPanel leftPatternPanel;
    private PatternPanel rightPatternPanel;

    public PatternViewWindow(Nes nes) {
        super("Pattern Tables");

        setSize(new Dimension(800, 400));
        this.nes = nes;

        leftPatternPanel = new PatternPanel(nes, true);
        rightPatternPanel = new PatternPanel(nes, false);

        getContentPane().setLayout(new GridLayout(1, 2));
        getContentPane().add(leftPatternPanel);
        getContentPane().add(rightPatternPanel);

        pack();
        setMinimumSize(getSize());
    }

    void update() {
        leftPatternPanel.update();
        rightPatternPanel.update();

        leftPatternPanel.repaint();
        rightPatternPanel.repaint();
    }






}
