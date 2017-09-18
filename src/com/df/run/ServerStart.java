package com.df.run;

import com.df.server.ChatServer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerStart extends JFrame {

    JButton start = new JButton("START!");
    JButton stop = new JButton("STOP!");

    public static void main(String[] args) {
        new ServerStart().lanunch();
    }

    public void lanunch() {
        setLocation(200, 100);
        setSize(400, 300);
        setLayout(null);

        start.setBounds(150, 80, 100, 30);
        stop.setBounds(150, 150, 100, 30);

        add(start);
        add(stop);

        start.addMouseListener(new Mouse());
        stop.addMouseListener(new Mouse());

        setTitle("启动/停止服务");
        addWindowListener(new WindClose());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);

    }

    class WindClose extends WindowAdapter {

        public void windowClosed(WindowEvent arg0) {
            if (chatServer != null) {
                chatServer.exit();
            }
            System.out.println("正在关闭。。。。。。");
            System.out.println("成功关闭。。。");
            System.exit(0);
        }
    }

    ChatServer chatServer = new ChatServer();

    class Mouse extends MouseAdapter implements Runnable {

        int count = 0;

        public void mouseClicked(MouseEvent e) {
            count = e.getClickCount();
            if (count == 2) {
                if (e.getSource() == start) {
                    count = 0;
                    //new Thread(new Run()).start();
                    new Thread(this).start();
                } else if (e.getSource() == stop) {
                    count = 0;
                    chatServer.exit();
                }
            }
        }

        //class Run implements Runnable {
        public void run() {

            chatServer.init();
        }
        //}

    }

}
