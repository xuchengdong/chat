package com.df.jFrame;

import com.df.jFrame.UserFrame.UserPanel.ImgPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class ChatFrame extends JFrame implements Serializable, Runnable {

    /**
     *
     */
    private static final long serialVersionUID = -590870733015252996L;
    public JTextArea showContent = new JTextArea();
    public JTextArea inputArea = new JTextArea(3, 0);
    JScrollPane js = new JScrollPane(showContent);
    JScrollPane jsi = new JScrollPane(inputArea);

    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    // Thread read = new Thread(new Read());
    boolean flag = false;
    ImgPanel jp;

    String message;
    String myaccount, friendaccount;

    HashMap<String, ChatFrame> chatFrames = null;

    /**
     * 构造方法
     */
    public ChatFrame() {
    }

    public ChatFrame(ObjectInputStream in, ObjectOutputStream out,
                     String myaccount, String friendaccount, ImgPanel jp,
                     String message, HashMap<String, ChatFrame> chatFrames) {
        this.myaccount = myaccount;
        this.friendaccount = friendaccount;
        this.jp = jp;
        this.setTitle("与 " + friendaccount.trim() + " 聊天");
        flag = true;
        this.out = out;
        this.in = in;
        this.message = message;
        this.chatFrames = chatFrames;

    }

    /**
     * 窗口显示
     */
    public void launchFrame() {
        /**
         * 把JTextArea放到JScrollPane里可创建滚动条
         */
        setLocation(500, 200);
        setSize(450, 350);

        this.add(js, BorderLayout.CENTER);
        this.add(jsi, BorderLayout.SOUTH);
        showContent.setEditable(false);
        showContent.setLineWrap(true);
        inputArea.setLineWrap(true);
        inputArea.addKeyListener(new InputActionEvent(this));
        this.setResizable(false);
        setVisible(true);
        this.addWindowListener(new WindClose());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    class WindClose extends WindowAdapter {

        public void windowClosed(WindowEvent e) {
            jp.flag = false;
            System.out.println("正在关闭与 " + friendaccount.trim() + " 聊天窗口。。。。。");
            chatFrames.remove(friendaccount.trim());
            System.out.println("窗口已成功关闭。。。");

            // System.exit(0);
        }
    }

    class InputActionEvent extends KeyAdapter {

        ChatFrame chatFrame;

        public InputActionEvent(ChatFrame chatFrame) {
            this.chatFrame = chatFrame;
        }

        public void keyPressed(KeyEvent e) {
            if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                String str = inputArea.getText().trim();
                if (str.length() > 0 && str != "") {
                    showContent.setText(showContent.getText()
                            + myaccount.trim() + "说:\n     " + str + '\n');
                    showContent.selectAll();

                    try {
                        out.writeObject(friendaccount + ":"
                                + chatFrame.inputArea.getText().trim());

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    inputArea.setText("");
                }
            }
        }
    }

    public void setMessage(String str) {
        this.message = str;
    }

    public void run() {
        this.launchFrame();
        while (true) {
            //睡一下
            try {
                Thread.currentThread().sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (message != null && message != "" && message.length() > 0) {
                showContent.append("好友 " + friendaccount.trim() + " 说:\n    "
                        + message + "\n");
                showContent.selectAll();
                message = null;
            }
        }

    }

}
