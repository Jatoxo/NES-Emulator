package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;


public class NesPanel extends JPanel {
    public BufferedImage screen;

    public NesPanel() {
        //BufferedImage doesn't have a byte RGB format, so we need to create one manually
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel model = new ComponentColorModel(colorSpace, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 256, 240, 3, null);

        screen = new BufferedImage(model, raster, true, null);
    }

    public void updateScreen(byte[] ppuOutputBuffer) {
        //screen.getRaster().setDataElements(0, 0, 256, 240, ppuOutputBuffer);

        //Obtain the underlying buffer from the BufferedImage
        byte[] buffer = ((DataBufferByte) screen.getRaster().getDataBuffer()).getData();

        //The format of ppuOutputBuffer exactly matches the format of the BufferedImage,
        //so we can just copy the data over
        System.arraycopy(ppuOutputBuffer, 0, buffer, 0, ppuOutputBuffer.length);

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(screen, 0, 0, getWidth(), getHeight(), null);
    }
}
