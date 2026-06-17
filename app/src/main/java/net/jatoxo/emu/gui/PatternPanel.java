package net.jatoxo.emu.gui;

import net.jatoxo.emu.nes.Nes;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class PatternPanel extends JPanel {
    private Nes nes;
    private boolean isLeft;

    // The 128x128 Image showing the pattern table
    private BufferedImage patternsImage;

    public PatternPanel(Nes nes, boolean isLeft) {
        this.nes = nes;
        this.isLeft = isLeft;

        patternsImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

        setMinimumSize(new Dimension(128, 128));
        setPreferredSize(new Dimension(256, 256));
    }


    public void update() {
        //Pattern data for one Pattern table
        byte[] patternData = new byte[8 * 2 * 16 * 16];

        int base = isLeft ? 0x0000 : 0x1000;

        // Read one pattern table's worth of data from PPU address bus
        // Pattern table 0: 0x0000 - 0x0FFF, Pattern table 1: 0x1000 - 0x1FFF
        for(int i = base; i < base + 0x1000; i += 1) {
            patternData[i - base] = (byte) nes.ppu.ppuRead(i);
        }

        renderChrData(patternsImage, patternData);
    }

    private void renderChrData(BufferedImage img, byte[] patternData) {
        for(int i = 0; i < 0x1000; i += 16) {
            //8 bytes for bit 0 of all 8 rows of the tile
            //another 8 bytes for the bit 1 of each row
            byte[] plane0 = new byte[8];
            byte[] plane1 = new byte[8];
            System.arraycopy(patternData, i, plane0, 0, 8);
            System.arraycopy(patternData, i + 8, plane1, 0, 8);

            for(int x = 0; x < 8; x += 1) {
                for(int y = 0; y < 8; y += 1) {
                    int colorIndex =  plane0[y] >> (7 - x) & 0b1;
                    colorIndex    |= (plane1[y] >> (7 - x) & 0b1) << 1;

                    //TODO: Implement using the palette that was most recently used for the thing
                    //Use pallete 0 for now, look up which index to the output colors to use
                    int col = nes.ppu.paletteRead(colorIndex);

                    //Look up what the colors are
                    Color outCol = nes.ppu.palette.colors[col];

                    int outX = ((i / 16) % 16) * 8 + x;
                    int outY = ((i / 16) / 16) * 8 + y;
                    img.setRGB(outX, outY, outCol.getRGB());
                }
            }

/*


            for(int bit = 0; bit < 8; bit += 1) {
                int bit0 = (patternData[i] >> (7 - bit)) & 0b1;
                int bit1 = (patternData[i + 0x800] >> (7 - bit)) & 0b1;

                int colorIndex = (bit1 << 1) | bit0;

                int x = (i % 16) * 8 + bit;
                int y = (i / 16);




            }
*/


        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(patternsImage, 0, 0, getWidth(), getHeight(), null);
    }
}
