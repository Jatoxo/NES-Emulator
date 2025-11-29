package emu.gui;

import emu.nes.Nes;
import emu.parsing.RomParser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class EmulatorDropTarget extends DropTargetAdapter {
    private Nes nes;


    public EmulatorDropTarget(Nes nes) {
        this.nes = nes;
    }

    @Override
    public void dragOver(DropTargetDragEvent event) {
        event.acceptDrag(DnDConstants.ACTION_LINK);
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

        // Get the transfer which can provide the dropped item data
        Transferable transferable = event.getTransferable();

        try {
            // If the drop items are files
            if (event.getCurrentDataFlavors()[0].isFlavorJavaFileListType()) {
                // Get all the dropped files

                List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                File file = droppedFiles.getFirst();
                nes.insertCartridge(RomParser.parseRom(file.getPath()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inform that the drop is complete
        event.dropComplete(true);
    }
}
