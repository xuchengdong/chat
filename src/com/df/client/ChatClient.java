package com.df.client;

import com.df.dto.FriendMessage;
import com.df.dto.UserMessage;
import com.df.jFrame.RegistFrame;
import com.df.jFrame.UserFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient implements Serializable {

    static ChatClient client = null;

    LoginFrame loginF = new LoginFrame();
    UserMessage usermess = new UserMessage();

    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    Socket socket = null;
    boolean bConnected = false;
    UserMessage userMessage = null;
    List<FriendMessage> friendMessage = null;

    public static void main(String[] args) {
        client = new ChatClient();
    }

    public ChatClient() {
        loginF.setContentPane(new JPane());
        loginF.launchFrame();

    }

    class LoginFrame extends JFrame {

        JLabel juser = new JLabel("用户名");
        JLabel jpass = new JLabel("密码");

        JTextField tuser = new JTextField(20);
        JPasswordField tpass = new JPasswordField(20);

        JButton bs = new JButton("登录");
        JButton br = new JButton("注册");

        public String user = null;
        public String pass = null;

        public boolean flag = false;

        public void launchFrame() {
            juser.setBounds(60, 70, 50, 30);
            tuser.setBounds(130, 70, 150, 30);
            jpass.setBounds(60, 120, 50, 30);
            tpass.setBounds(130, 120, 150, 30);
            bs.setBounds(100, 200, 70, 30);
            br.setBounds(200, 200, 70, 30);

            setLayout(null);
            setLocation(300, 100);
            setSize(350, 300);

            add(juser);
            add(tuser);
            add(jpass);
            add(tpass);
            add(bs);
            add(br);
            // pack();
            setVisible(true);
            bs.addMouseListener(new Mouse());
            br.addMouseListener(new Mouse());
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }

        class Mouse extends MouseAdapter {

            public void mousePressed(MouseEvent e) {
                if (e.getSource() == bs) {
                    user = tuser.getText().trim();
                    pass = tpass.getText().trim();
                    flag = submit();
                    if (flag) {
                        usermess.account = user;
                        usermess.password = pass;
                        connected();
                    }
                } else if (e.getSource() == br) {
                    RegistFrame rf = new RegistFrame();
                    rf.setContentPane(new JPane());
                    rf.lanunchFrame();
                    // new RegistFrame().lanunchFrame();
                    loginF.setVisible(false);
                    loginF.dispose();
                }
            }
        }

        public boolean submit() {
            if (user.length() <= 0 || user == " " || user == null) {
                JOptionPane.showMessageDialog(this, "账号不能为空!", "Waring",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            } else if (pass.length() <= 0 || pass == "" || pass == null) {
                JOptionPane.showMessageDialog(this, "密码不能为空!", "Waring",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            } else {
                return true;
            }
        }

    }

    class JPane extends JPanel {

        public void paintComponent(Graphics g) {
            ImageIcon back = new ImageIcon("img/b2.jpg");
            // g = getContentPane().getGraphics();
            g.drawImage(back.getImage(), 0, 0, 650, 600, null);
        }
    }

    public void connected() {
        try {
            socket = new Socket("127.0.0.1", 6666);
            System.out.println("connected succeed!");
            bConnected = true;
            System.out.println("**********************!");
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            sendToServer();
            System.out.println("**********************!");
        } catch (ConnectException e) {
            System.out.println("请先启动服务器。。。。。");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer() {
        try {
            System.out.println("account:" + usermess.getAccount() + '\n'
                    + "userpass:" + usermess.getPassword());
            out.writeObject(usermess);
            out.flush();
            getBackInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getBackInfo() {
        try {
            System.out.println("*******************");
            /**
             * 从服务器端接受返回信息
             */
            String str = in.readUTF();
            boolean flag = in.readBoolean();
            if (flag) {
                try {
                    userMessage = (UserMessage) in.readObject();
                    friendMessage = (ArrayList) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (EOFException e) {
                    System.out.println("/*********************/");
                }
                // 登陆成功后调用方法启动客户端界面
                UserFrame uf = new UserFrame(friendMessage, userMessage, in,
                        out);
                // 把用户发送给服务器端

                uf.launchFrame();
                loginF.setVisible(false);
                new Thread(uf).start();
            } else {
                JOptionPane.showMessageDialog(loginF, str, "Waring",
                        JOptionPane.WARNING_MESSAGE);
            }
            System.out.println(str);
            System.out.println("*******************");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
