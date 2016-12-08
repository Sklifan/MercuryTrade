package com.home.clicker;

import com.home.clicker.events.EventHandler;
import com.home.clicker.events.EventRouter;
import com.home.clicker.events.custom.SendMessageEvent;
import com.home.clicker.misc.WhisperNotifier;
import com.home.clicker.utils.FileMonitor;
import com.home.clicker.utils.LoggedMessagesUtils;
import com.home.clicker.utils.User32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.Timer;

/**
 * Exslims
 * 07.12.2016
 */
public class PrivateMessageManager {
    private GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook();
    private GlobalKeyAdapter adapter;
    private JFrame frame;
    private User32 user32 = User32.INSTANCE;
    private boolean isEndedDelay = true;
    private WinDef.HWND cachedWindow;

    public PrivateMessageManager(JFrame jFrame) {
        this.frame = jFrame;
        GlobalKeyAdapter adapter = getAdapter();
        this.adapter = adapter;

        EventRouter.registerHandler(SendMessageEvent.class,new EventHandler<SendMessageEvent>(){
            public void handle(SendMessageEvent event) {
                execute(event.getMessage());
            }
        });
        keyboardHook.addKeyListener(adapter);
        new WhisperNotifier();
        new LoggedMessagesUtils();
        new FileMonitor();

    }

    private void execute(String message){
        try {
            keyboardHook.removeKeyListener(adapter);
            frame.setVisible(false);
            isEndedDelay = false;
            StringSelection selection = new StringSelection("@" + message + " TY");
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);

            setForegroundWindow("Path of Exile");

            Robot robot = new Robot();
            robot.keyRelease(KeyEvent.VK_F2);

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isEndedDelay = true;
                }
            }, 1500);

            keyboardHook.addKeyListener(adapter);

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private GlobalKeyAdapter getAdapter(){
        return new GlobalKeyAdapter() {
            @Override
            public void keyPressed(GlobalKeyEvent event) {
                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_F2 && isEndedDelay) {
                    System.out.println("ALT PRESSED");
                    frame.setVisible(true);
                }
            }

            @Override
            public void keyReleased(GlobalKeyEvent event) {
                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_F2) {
                    System.out.println("ALT RELEASED");
                    frame.setVisible(false);
                }
            }
        };
    }

    private void setForegroundWindow(final String titleName){
        if(cachedWindow == null) {
            user32.EnumWindows(new WinUser.WNDENUMPROC() {
                int count = 0;

                public boolean callback(WinDef.HWND hWnd, Pointer arg1) {
                    byte[] windowText = new byte[512];
                    user32.GetWindowTextA(hWnd, windowText, 512);
                    String wText = Native.toString(windowText);

                    if (wText.isEmpty()) {
                        return true;
                    }
//
                System.out.println("Found window with text " + hWnd
                        + ", total " + ++count + " Text: " + wText);
                    if (wText.equals(titleName)) {
                        cachedWindow = hWnd;
                        user32.SetForegroundWindow(hWnd);
                        return false;
                    }
                    return true;
                }
            }, null);
        }else {
            user32.SetForegroundWindow(cachedWindow);
        }
    }

}