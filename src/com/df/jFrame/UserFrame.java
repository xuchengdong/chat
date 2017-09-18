package com.df.jFrame;

import com.df.dto.FriendMessage;
import com.df.dto.UserMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//*********************************************//

//***********************************************//

public class UserFrame extends JFrame implements Runnable, ActionListener {

    // **************************************************//

    private static final long serialVersionUID = 5381564219860736835L;

    PopupMenu popup = new PopupMenu();
    Menu popupMessage = new Menu("弹出消息");
    MenuItem[] mItem = {new MenuItem("上线"), new MenuItem("隐身"),
            new MenuItem("×前端显示"), new MenuItem("常规消息"), new MenuItem("退出程序")};
    SystemTray tray;
    TrayIcon trayIcon;
    Image image1;
    Image image;
    boolean flag = true;

    // ***************************************************//

    // 窗口大小
    public static final int SCREEN_WIDTH = 240;
    public static final int SCREEN_HEIGHT = 600;

    UserPanel up = null;

    JScrollPane js = null;

    JTextArea showArea = new JTextArea(3, 0);
    String myAccount;

    ObjectInputStream in;
    ObjectOutputStream out;
    HashMap<String, ChatFrame> chatFrames = new HashMap<String, ChatFrame>();
    List<UserPanel.ImgPanel> imgpanels = new ArrayList<UserPanel.ImgPanel>();

    // 重载构造方法

    public UserFrame(List<FriendMessage> friendMessage,
                     UserMessage userMessage, ObjectInputStream in,
                     ObjectOutputStream out) {
        this.out = out;
        this.in = in;
        myAccount = userMessage.account;
        up = new UserPanel(friendMessage, userMessage);

        /**
         * 网络资料
         *
         *
         * 因为scrollPane是根据里面的子控件的preferredSize来确定滚动条的。
         * 让image尺寸变化时，scrollPane能得到新的数据。 解决方式有两种，一种是改变大小时重新设置JPanel的preferred
         * size； 一种是重载getPreferredSize()方法，按照需要返回，楼主的情况可以返回当前
         *
         *
         *
         * 根据上面的思想我一开始就获取UserPanel的preferredSize的值thsize
         */
        Dimension thesize = new Dimension(friendMessage.size() * 100,
                friendMessage.size() * 100);
        up.setPreferredSize(thesize);
        // 将UserPanel放到JSrollPane中
        js = new JScrollPane(up,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置滚动条速度
        JScrollBar bar = js.getVerticalScrollBar();
        bar.setUnitIncrement(100);

        js.getViewport().setBackground(Color.cyan);
        js.setVisible(true);
    }

    public void launchFrame() {
        // **************************************************//
        for (int i = 0; i < mItem.length; i++) {
            mItem[i].addActionListener(this);
            if (i < 4)
                popupMessage.add(mItem[i]);
        }
        popup.add(popupMessage);
        popup.add(mItem[4]);
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            image = Toolkit.getDefaultToolkit().getImage("img/tray1.gif");
            image1 = Toolkit.getDefaultToolkit().getImage("img/tray1.gif");
            trayIcon = new TrayIcon(image1, "提示信息", popup);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
            trayIcon.addActionListener(this);
            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getSource() == trayIcon
                            && e.getButton() == MouseEvent.BUTTON1) {

                        if (flag) {
                            setState(1);
                            flag = false;
                        } else {
                            setState(0);
                            flag = true;
                        }
                    }
                    setVisible(true);
                }
            });
        }
        this.addWindowListener(new WindowAdapter() {
        });

        // ****************************************************//

        setLocation(200, 100);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        // 设置背景透明
        up.setOpaque(false);

        // setResizable(false);
        js.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        showArea.setEditable(false);
        add(showArea, BorderLayout.NORTH);
        add(js, BorderLayout.CENTER);
        this.setTitle("欢迎使用QQ2010聊天系统");
        setVisible(true);
        // this.addWindowListener(new WindClose());
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // ***********************************************

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == mItem[0]) {
            trayIcon.displayMessage("上线", "上线成功", TrayIcon.MessageType.WARNING);
            trayIcon.setImage(image1);
        } else if (e.getSource() == mItem[1]) {
            trayIcon.displayMessage("隐身", "隐身成功", TrayIcon.MessageType.INFO);
            trayIcon.setImage(image);
        } else if (e.getSource() == mItem[2]) {
            if (!isAlwaysOnTop()) {
                trayIcon.displayMessage("前端显示", "前端显示",
                        TrayIcon.MessageType.NONE);
                setAlwaysOnTop(true);
                mItem[2].setLabel("√前端显示");
            } else {
                trayIcon.displayMessage("×前端显示", "取消前端显示",
                        TrayIcon.MessageType.NONE);
                setAlwaysOnTop(false);
                mItem[2].setLabel("×前端显示");
            }

        } else if (e.getSource() == mItem[3]) {
            trayIcon.displayMessage("常规消息", "常规消息", TrayIcon.MessageType.NONE);

        } else if (e.getSource() == mItem[4]) {
            System.out.println("正在退出。。。。。。");
            System.out.println("成功退出。。。");
            System.exit(0);
        }

    }

    // *********************************************

	/*
     * class WindClose extends WindowAdapter {
	 *
	 * public void windowClosed(WindowEvent arg0) {
	 * System.out.println("正在退出。。。。。。"); // 调用一个方法
	 * System.out.println("成功退出。。。"); System.exit(0); } }
	 */

    class UserPanel extends JPanel {

        private static final long serialVersionUID = 104853871399720513L;
        public List<FriendMessage> friendMessage = null;
        public UserMessage userMessage = null;

        public static final int WIDTH = 150, HEIGHT = 100, X = 10, Y = 40,
                Y1 = 10;
        public int y = Y1;

        // 构造方法
        public UserPanel(List<FriendMessage> friendMessage,
                         UserMessage userMessage) {
            this.friendMessage = friendMessage;
            this.userMessage = userMessage;

            this.setVisible(true);
            this.setLayout(null);

            addImgPanel();
        }

        public void addImgPanel() {

            showArea.setBackground(Color.GREEN);
            showArea.setText(" 账号 " + userMessage.getAccount() + '\n' + " 昵称 "
                    + userMessage.getNickname() + '\n' + " 好友人数  "
                    + friendMessage.size());
            ImgPanel jp = new ImgPanel(userMessage);
            imgpanels.add(jp);
            jp.setBounds(X, y, WIDTH, HEIGHT);
            this.add(jp);

            for (int i = 0; i < friendMessage.size(); i++) {
                FriendMessage friend = friendMessage.get(i);
                y += 100;
                ImgPanel jpp = new ImgPanel(friend);
                imgpanels.add(jpp);
                jpp.setBounds(X, y, WIDTH, HEIGHT);
                this.add(jpp);
            }
        }

        /**
         * UserPanel的好友头像图片
         */
        class ImgPanel extends JPanel {

            String myaccount = "", friendaccount = "";
            ImageIcon img;
            UserMessage userMessage;
            ImageIcon icon = new ImageIcon("img/default.jpg");
            boolean flag = false;

            public static final int WIDTH = 60, HEIGHT = 60;

            public ImgPanel(UserMessage userMessage) {

                this.userMessage = userMessage;
                this.myaccount = myAccount;
                this.friendaccount = userMessage.account;
                // 给每个好友头像添加双击事件
                this.addMouseListener(new MouseLis(this));
            }

            class MouseLis extends MouseAdapter {
                ImgPanel jp;

                public MouseLis(ImgPanel jp) {
                    this.jp = jp;

                }

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && !flag) {
                        flag = true;
                        ChatFrame chatframe = new ChatFrame(in, out, myaccount,
                                jp.friendaccount, jp, "", chatFrames);
                        chatFrames.put(jp.friendaccount.trim(), chatframe);
                        System.out.println("jp.friendaccount---------"
                                + jp.friendaccount + "***");
                        new Thread(chatframe).start();
                    }
                }
            }

            public void paint(Graphics g) {

                Color c = g.getColor();
                g.setColor(Color.BLUE);

                Font font = g.getFont();
                Font f = new Font("宋体", Font.BOLD, 15);
                g.setFont(f);

                String username = null;
                String img = null;
                boolean flag = false;

                username = userMessage.getAccount().trim();

                try {
                    img = userMessage.getImg().trim();
                } catch (NullPointerException e) {
                    img = "default.jpg";
                }
                if (img != null && img != "") {
                    icon = new ImageIcon("img/" + img);
                }
                g.drawRect(0, 0, WIDTH + 4, HEIGHT + 4);
                g.drawImage(icon.getImage(), 2, 2, WIDTH, HEIGHT, null);
                g.drawString(username, 80, 30);

                g.setColor(c);
                g.setFont(font);
            }
        }
    }

    public void run() {
        ChatFrame chatframe = null;
        while (true) {
            try {
                // 睡一下
                try {
                    Thread.currentThread().sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String message = (String) in.readObject();

                int posistion = message.indexOf(":");
                String friendname = message.substring(0, posistion);
                message = message.substring(posistion + 1, message.length());
                System.out.println("接收到" + friendname + "消息: " + message);
                if (imgpanels.size() > 0) {
                    for (int i = 0; i < imgpanels.size(); i++) {
                        UserPanel.ImgPanel imgpanel = imgpanels.get(i);
                        if (imgpanel.friendaccount.trim().equals(friendname)) {
                            System.out.println(imgpanel.myaccount + " 接收到好友 "
                                    + friendname + "消息: " + message);
                            if ((ChatFrame) chatFrames.get(friendname.trim()) == null) {
                                chatframe = new ChatFrame(in, out,
                                        imgpanel.myaccount,
                                        imgpanel.friendaccount, imgpanel,
                                        message, chatFrames);
                                chatFrames.put(friendname.trim(), chatframe);
                                new Thread(chatframe).start();

                            } else {
                                chatframe = (ChatFrame) chatFrames
                                        .get(friendname.trim());
                                chatframe.setMessage(message);
                            }
                        }
                    }
                } else {

                }
            } catch (EOFException e) {
                //System.out.println("服务器发生异常!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
