package emu.gui;

import emu.nes.input.StandardController;
import emu.nes.Nes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PhysicalInput extends KeyAdapter {
    private final Nes nes;

    public PhysicalInput(Nes nes) {
        this.nes = nes;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handleKey(e.getKeyCode(), false);
    }

    private void handleKey(int keyCode, boolean pressed) {
        StandardController controller = (StandardController) nes.controllerPorts.player1;

        switch(keyCode) {
            case KeyEvent.VK_TAB:
                nes.limitSpeed = !pressed;
                break;
            case KeyEvent.VK_W:
                controller.dpadUp = pressed;
                break;
            case KeyEvent.VK_A:
                controller.dpadLeft = pressed;
                break;
            case KeyEvent.VK_S:
                controller.dpadDown = pressed;
                break;
            case KeyEvent.VK_D:
                controller.dpadRight = pressed;
                break;
            case KeyEvent.VK_SPACE:
                controller.buttonA = pressed;
                break;
            case KeyEvent.VK_SHIFT:
                controller.buttonB = pressed;
                break;
            case KeyEvent.VK_MINUS:
                controller.buttonSelect = pressed;
                break;
            case KeyEvent.VK_ENTER:
                controller.buttonStart = pressed;
                break;
        }
    }

}
