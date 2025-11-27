package emu.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;


public class NesPanel extends JPanel {
    public BufferedImage screen;

    private final int[] outputBuffer;

    public NesPanel(GUI gui) {
        outputBuffer = gui.nes.ppu.outputBuffer;

        // match the masks with DirectColorModel
        ColorModel cm = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF);
        DataBufferInt dataBuffer = new DataBufferInt(outputBuffer, outputBuffer.length);
        WritableRaster raster2 = Raster.createPackedRaster(dataBuffer, 256, 240, 256,
                new int[]{0xFF0000, 0x00FF00, 0x0000FF}, null);

        screen = new BufferedImage(cm, raster2, true, null);

        //screen = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
    }

    public void updateScreen() {
        //screen.getRaster().setDataElements(0, 0, 256, 240, ppuOutputBuffer);

        //Obtain the underlying buffer from the BufferedImage
        //int[] buffer = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();

        //The format of ppuOutputBuffer exactly matches the format of the BufferedImage,
        //so we can just copy the data over
        //System.arraycopy(ppuOutputBuffer, 0, buffer, 0, ppuOutputBuffer.length);

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(screen, 0, 0, getWidth(), getHeight(), null);
    }
}
