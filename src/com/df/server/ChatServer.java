package com.df.server;

import com.df.dbo.LinkSql;
import com.df.dto.FriendMessage;
import com.df.dto.UserMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    boolean started = false;

    Socket socket = null;
    ServerSocket serverSocket = null;

    List<Client> clients = new ArrayList<Client>();

    int count = 0;

    public static void main(String[] args) {
        new ChatServer().init();
    }

    public void init() {
        try {
            serverSocket = new ServerSocket(6666);
            started = true;
            System.out.println("服务正在运行。。。。。");
            while (started) {
                socket = serverSocket.accept();
                System.out.println("当前服务器已经获取" + count + "个用户");

                Client cc = new Client(socket);

                new Thread(cc).start();
                System.out.println("当前服务器已经启动" + clients.size() + "个线程");
            }
        } catch (BindException e) {
            System.out.println("端口使用中。。。。。");
        } catch (SocketException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程开始
     */
    class Client implements Runnable {

        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        Socket socket = null;
        LinkSql link = new LinkSql();
        Statement stmt = null;
        ResultSet rs = null;
        boolean flag = false;
        String account = null;
        String password = null;
        PreparedStatement ps;
        public boolean online = false;
        List<FriendMessage> friendMessage = new ArrayList<FriendMessage>();

        public Client(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("------------------------!");
            checkInfo();
            System.out.println("------------------------!");
        }

        public void checkInfo() {
            try {
                UserMessage usermess = (UserMessage) in.readObject();
                account = usermess.getAccount();
                password = usermess.getPassword();

                checkUser(account, password);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void checkUser(String account, String password) {
            String pass = null;
            String sql = "select * from userinfo where account='" + account
                    + "'";
            try {
                stmt = link.con.createStatement();
                rs = stmt.executeQuery(sql);
                if (rs != null) {
                    if (rs.next()) {
                        pass = rs.getString("password").trim();
                    } else {
                        System.out.println("用户名不存在!");
                        try {
                            out.writeUTF("用户名不存在!");
                            out.writeBoolean(false);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("用户名不存在!");
                    try {
                        out.writeUTF("用户名不存在!");
                        out.writeBoolean(false);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                boolean b = false;
                // 验证密码是否正确
                if (password.equals(pass)) {
                    flag = true;
                    // 验证是否已经登录
                    String ss1 = "select online1 from friends where account1 ='"
                            + account + "'";
                    String ss2 = "select online2 from friends where account2 ='"
                            + account + "'";
                    rs = stmt.executeQuery(ss1);
                    if (rs != null && rs.next()) {

                        if (rs.getInt(1) != 0) {
                            flag = false;
                            b = true;
                        }

                    }
                    rs = stmt.executeQuery(ss2);
                    if (rs != null && rs.next()) {

                        if (rs.getInt(1) != 0) {
                            flag = false;
                            b = true;
                        }

                    }

                    if (b != true) {

                        System.out.println(account + "  登录成功!");
                        // 登陆成功后修改friends表中的在线状态
                        String str1 = "update friends set online1 =1 where account1='"
                                + account + "'";
                        String str2 = "update friends set online2 =1 where account2='"
                                + account + "'";
                        stmt.executeUpdate(str1);
                        stmt.executeUpdate(str2);
                        try {
                            out.writeUTF(account + "  登录成功!");
                            out.writeBoolean(true);
                            out.flush();
                            online = true;
                            System.out.println("登陆成功时已经获取----------" + ++count
                                    + "个用户");
                            clients.add(this);
                            System.out.println("登录成功时已经启动----------"
                                    + clients.size() + "个线程");
                            // 调用二个方法分别获取到该用户的信息和好友并写到客户端出去
                            UserMessage userMessage = getUserMessageFromSql();
                            friendMessage = getFriendMessage();
                            out.writeObject(userMessage);
                            out.writeObject(friendMessage);
                            out.flush();

                            // 调用一个方法查询数据库看是否有离线消息
                            offlineMessage(account);

                            // 启动一个线程接收来自客户端的信息
                            new Thread(new ReceiveFromChatFrame(out, in,
                                    online, account, stmt, this)).start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("你已经登录 " + account + " 不能重复登录!");
                        try {
                            out.writeUTF("你已经登录 " + account + " 不能重复登录!");
                            out.writeBoolean(false);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("密码错误!");
                    try {
                        out.writeUTF("密码错误!");
                        out.writeBoolean(false);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 登录失败时（已经登录、用户不存在、密码错误）
                if (!flag) {

                    System.out.println("登录失败后服务器当前已经获取*******" + count + "个用户");
                    rs.close();
                    stmt.close();
                    link.con.close();
                    try {
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("登录失败时服务器已经启动********" + clients.size()
                            + "个线程");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 查询数据库看新上线的用户是否有离线消息(friendname:message)messae(来自friendname的离线消息***)

        /********************************************/
        public void offlineMessage(String account) {
            List<String> offmessList = new ArrayList<String>();

            String str = "select * from friends where account1='" + account
                    + "'";
            try {
                rs = stmt.executeQuery(str);
                if (rs != null) {
                    while (rs.next()) {

                        String mess = rs.getString("message1");
                        if (mess != null && mess.length() > 0) {
                            String friendname = rs.getString("account2").trim();
                            mess = friendname + ":" + mess + "----离线消息";
                            String delete = "update friends set message1=null where account1='"
                                    + account + "'";
                            stmt.executeUpdate(delete);
                            offmessList.add(mess);
                        }

                    }
                }

            } catch (SQLException e) {
                // e.printStackTrace();
            }
            try {
                str = "select * from friends where account2='" + account + "'";
                rs = stmt.executeQuery(str);
                if (rs != null) {
                    while (rs.next()) {

                        String friendname = rs.getString("account1").trim();
                        String mess = rs.getString("message2");
                        if (mess != null && mess.length() > 0) {
                            mess = friendname + ":" + mess + "----离线消息";
                            String delete = "update friends set message2=null where account2='"
                                    + account + "'";
                            stmt.executeUpdate(delete);
                            offmessList.add(mess);
                        }

                    }
                }

            } catch (SQLException e) {
                // e.printStackTrace();
            }

            if (offmessList.size() > 0) {
                // 有离线消息
                for (int i = 0; i < offmessList.size(); i++) {
                    String mess = (String) offmessList.get(i);
                    send(mess);

                }

            } else {
                // 无离线消息

            }
        }

        /*****************************************************/

        /**
         * 获取用户自己信息
         */
        public UserMessage getUserMessageFromSql() {
            String str = "select * from userinfo  where account='" + account
                    + "'";
            UserMessage um = new UserMessage();
            try {
                rs = stmt.executeQuery(str);
                if (rs != null) {
                    while (rs.next()) {

                        um.setAccount(rs.getString("account"));
                        um.setPassword(rs.getString("password"));
                        um.setNickname(rs.getString("nickname"));
                        um.setName(rs.getString("name"));
                        um.setSex(rs.getString("sex"));
                        um.setAge(rs.getInt("age"));
                        um.setAddress(rs.getString("address"));
                        um.setPhone(rs.getString("phone"));
                        um.setEmail(rs.getString("email"));
                        um.setImg(rs.getString("img"));
                        um.setRemarks(rs.getString("remarks"));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
            return um;
        }

        /**
         * 获取好友信息
         */
        public List<FriendMessage> getFriendMessage() {

            String sql1 = "select * from friends where account2 ='" + account
                    + "'";
            String sql2 = "select * from friends where account1 ='" + account
                    + "'";

            ResultSet rr = null;
            try {
                ps = link.con
                        .prepareStatement("select * from userinfo where account=?");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            try {
                rs = stmt.executeQuery(sql1);
                if (rs != null) {
                    while (rs.next()) {
                        FriendMessage fm = new FriendMessage();
                        fm.setAccount(rs.getString("account1"));
                        fm.setOline(rs.getInt("online1"));
                        fm.setRemark(rs.getString("remarks2"));
                        fm.setMessage(rs.getString("message2"));
                        ps.setString(1, fm.getAccount());
                        rr = ps.executeQuery();
                        if (rr != null) {
                            while (rr.next()) {
                                fm.setNickname(rr.getString("nickname"));
                                fm.setName(rr.getString("name"));
                                fm.setSex(rr.getString("sex"));
                                fm.setAge(rr.getInt("age"));
                                fm.setAddress(rr.getString("address"));
                                fm.setPhone(rr.getString("phone"));
                                fm.setEmail(rr.getString("email"));
                                fm.setImg(rr.getString("img"));
                                fm.setRemarks(rr.getString("remarks"));
                            }
                        }
                        friendMessage.add(fm);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }

            try {
                rs = stmt.executeQuery(sql2);

                if (rs != null) {
                    while (rs.next()) {
                        FriendMessage fm = new FriendMessage();
                        fm.setAccount(rs.getString("account2"));
                        fm.setOline(rs.getInt("online2"));
                        fm.setRemark(rs.getString("remarks1"));
                        fm.setMessage(rs.getString("message1"));
                        ps.setString(1, fm.getAccount());
                        rr = ps.executeQuery();
                        if (rr != null) {
                            while (rr.next()) {
                                fm.setNickname(rr.getString("nickname"));
                                fm.setName(rr.getString("name"));
                                fm.setSex(rr.getString("sex"));
                                fm.setAge(rr.getInt("age"));
                                fm.setAddress(rr.getString("address"));
                                fm.setPhone(rr.getString("phone"));
                                fm.setEmail(rr.getString("email"));
                                fm.setImg(rr.getString("img"));
                                fm.setRemarks(rr.getString("remarks"));
                            }
                            friendMessage.add(fm);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
            return friendMessage;

        }

        public void send(String str) {
            try {
                out.writeObject(str);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 线程结束
     */

    class ReceiveFromChatFrame implements Runnable {

        ObjectOutputStream out;
        ObjectInputStream in;
        boolean online;
        String account;
        Statement stmt;
        Client client;
        String message;

        public ReceiveFromChatFrame(ObjectOutputStream out,
                                    ObjectInputStream in, boolean online, String account,
                                    Statement stmt, Client client) {
            this.out = out;
            this.in = in;
            this.online = online;
            this.account = account;
            this.stmt = stmt;
            this.client = client;
        }

        public void run() {

            while (online) {

                try {
                    /**
                     * 读取客户端发送的数据
                     */

                    // 睡一下
                    try {
                        Thread.currentThread().sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    message = (String) in.readObject();
                    int posistion = message.indexOf(":");
                    String friendname = message.substring(0, posistion);
                    message = message
                            .substring(posistion + 1, message.length());
                    System.out.println(account + "发给好友" + friendname.trim()
                            + "的信息: " + message);

                    // 调用一个方法查询数据库该好友是否在线并处理
                    checkOnlie(friendname);

                } catch (SocketException e) {
                    online = false;
                    String str = "update friends set online1=0 where account1='"
                            + account + "'";
                    try {
                        stmt.executeUpdate(str);
                        str = "update friends set online2=0 where account2='"
                                + account + "'";
                        stmt.executeUpdate(str);
                        boolean b = clients.remove(client);
                        count--;
                        System.out.println(b + "        " + account
                                + " 下线后服务器已经获取----------" + count + "个用户");
                        System.out.println(b + "        " + account
                                + " 下线后服务器已经启动----------" + clients.size()
                                + "个线程");

                        // stmt.close();

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        public void checkOnlie(String friendname) {

            ResultSet rs = null;
            boolean flag = false;
            String s1 = "select online1 from friends where account1='"
                    + friendname + "'";
            String s2 = "select online2 from friends where account2='"
                    + friendname + "'";
            try {
                rs = stmt.executeQuery(s1);
                if (rs != null && rs.next()) {
                    if (rs.getInt(1) != 0) {
                        flag = true;
                    }
                }
                rs = stmt.executeQuery(s2);
                if (rs != null && rs.next()) {
                    if (rs.getInt(1) != 0) {
                        flag = true;
                    }
                }
            } catch (SQLException e) {

                e.printStackTrace();
            }

            if (flag) {
                // 在线处理
                System.out.println(friendname + ": 在线!");
                for (int i = 0; i < clients.size(); i++) {
                    Client cc = clients.get(i);
                    if (cc.account.equals(friendname.trim())) {
                        cc.send(account + ":" + message);
                        System.out.println("已经向好友 " + friendname.trim()
                                + " 发送消息!" + message);
                    }
                }

            } else {
                // 离线处理
                System.out.println(friendname + ": 不在线!消息已存入数据库!");
                int number = 0;
                String str = null;
                String st = "select message1 from friends where account2='"
                        + account + "' and account1='" + friendname + "'";
                try {
                    rs = stmt.executeQuery(st);
                    if (rs != null && rs.next()) {
                        String mess = rs.getString("message1");
                        if (mess != null && mess.length() > 0) {
                            mess = mess + '\n' + message;
                        } else {
                            mess = message;
                        }
                        str = "update friends set message1='" + mess
                                + "' where account1='" + friendname
                                + "' and account2='" + account + "'";
                        stmt.executeUpdate(str);

                    } else {
                        st = "select message2 from friends where account1='"
                                + account + "' and account2='" + friendname
                                + "'";
                        rs = stmt.executeQuery(st);
                        if (rs != null && rs.next()) {
                            String mess = rs.getString("message2");
                            if (mess != null && mess.length() > 0) {
                                mess = mess + '\n' + message;
                            } else {
                                mess = message;
                            }
                            str = "update friends set message2='" + mess
                                    + "' where account2='" + friendname
                                    + "' and account1='" + account + "'";
                            stmt.executeUpdate(str);
                        }
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

        }

    }

    public void exit() {
        System.out.println("服务正在退出。。。。。。");
        LinkSql link = new LinkSql();
        Connection con = link.con;
        String sql = "update friends set online1=0";
        try {
            /*PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "online1");
			ps.executeUpdate();
			ps.setString(1, "online2");
			ps.executeUpdate();
			ps.close();
			con.close();*/
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            sql = "update friends set online2=0";
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            serverSocket.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        } finally {
            try {
                serverSocket.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
        }

        System.out.println("服务成功退出。。。");
        return;
        //System.exit(0);
    }

}
