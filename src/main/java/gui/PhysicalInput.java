package gui;

import input.StandardController;
import nes.Nes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PhysicalInput extends KeyAdapter {
    private Nes nes;

    public PhysicalInput(Nes nes) {
        this.nes = nes;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        StandardController controller = (StandardController) nes.controllerPorts.player1;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_TAB:
                nes.limitSpeed = false;
                break;
            case KeyEvent.VK_W:
                controller.dpadUp = true;
                break;
            case KeyEvent.VK_A:
                controller.dpadLeft = true;
                break;
            case KeyEvent.VK_S:
                controller.dpadDown = true;
                break;
            case KeyEvent.VK_D:
                controller.dpadRight = true;
                break;
            case KeyEvent.VK_SPACE:
                controller.buttonA = true;
                break;
            case KeyEvent.VK_SHIFT:
                controller.buttonB = true;
                break;
            case KeyEvent.VK_MINUS:
                controller.buttonSelect = true;
                break;
            case KeyEvent.VK_ENTER:
                controller.buttonStart = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        StandardController controller = (StandardController) nes.controllerPorts.player1;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_TAB:
                nes.limitSpeed = true;
                break;
            case KeyEvent.VK_W:
                controller.dpadUp = false;
                break;
            case KeyEvent.VK_A:
                controller.dpadLeft = false;
                break;
            case KeyEvent.VK_S:
                controller.dpadDown = false;
                break;
            case KeyEvent.VK_D:
                controller.dpadRight = false;
                break;
            case KeyEvent.VK_SPACE:
                controller.buttonA = false;
                break;
            case KeyEvent.VK_SHIFT:
                controller.buttonB = false;
                break;
            case KeyEvent.VK_MINUS:
                controller.buttonSelect = false;
                break;
            case KeyEvent.VK_ENTER:
                controller.buttonStart = false;
                break;
        }
    }


}
