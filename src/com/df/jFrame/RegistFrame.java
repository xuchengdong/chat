package com.df.jFrame;

import javax.swing.*;
import java.awt.*;

public class RegistFrame extends JFrame {


    JLabel account = new JLabel("QQ号");
    JLabel password = new JLabel("密码");
    JLabel nickname = new JLabel("昵称");
    JLabel name = new JLabel("姓名");
    JLabel sex = new JLabel("性别");
    JLabel age = new JLabel("年龄");
    JLabel address = new JLabel("居住地址");
    JLabel phone = new JLabel("联系方式");
    JLabel email = new JLabel("Email");
    JLabel img = new JLabel("头像");
    JLabel remarks = new JLabel("备注");
    JLabel title = new JLabel("欢迎您注册！");

    JTextField acc = new JTextField();
    JPasswordField pass = new JPasswordField();
    JTextField nick = new JTextField();
    JTextField na = new JTextField();

    JTextField add = new JTextField();
    JTextField ph = new JTextField();
    JTextField em = new JTextField();
    JTextField im = new JTextField();
    JTextArea re = new JTextArea();

    JButton bs = new JButton("提交");
    JButton br = new JButton("重置");

    Object[] ageValues = {"10~15", "16~20", "20~25", "26~30", "31~40", "41~50", "51~70", "71~85", "86~100"};


    JComboBox ag = new JComboBox(ageValues);
    JScrollPane js = new JScrollPane(re);

    JRadioButton sexman = new JRadioButton("男", true);
    JRadioButton sexwom = new JRadioButton("女");

    ButtonGroup group = new ButtonGroup();


    Object[] regist = {"注册!", "取消!"};
    Object selectValue = JOptionPane.showInputDialog(null, "请选择", "input", JOptionPane.INFORMATION_MESSAGE, null, regist, regist[0]);








/*	public static void main(String[] args) {
        RegistFrame rf = new RegistFrame();
		rf.setContentPane(new JPane());
		rf.lanunchFrame();
	}*/


    public void lanunchFrame() {

        setLocation(400, 200);
        setSize(650, 600);
        setLayout(null);
        //getContentPane().setBackground(Color.GREEN);

        title.setBounds(250, 40, 100, 25);

        account.setBounds(60, 40 + 50, 60, 20);
        acc.setBounds(125, 40 + 50, 100, 20);

        password.setBounds(340, 40 + 50, 60, 20);
        pass.setBounds(390, 40 + 50, 100, 20);

        nickname.setBounds(60, 120 + 20, 60, 20);
        nick.setBounds(125, 120 + 20, 100, 20);

        name.setBounds(340, 120 + 20, 60, 20);
        na.setBounds(390, 120 + 20, 100, 20);

        age.setBounds(60, 200, 60, 20);
        ag.setBounds(125, 200, 100, 20);


        sex.setBounds(340, 200, 60, 20);
        sexman.setBounds(390, 200, 60, 20);
        sexwom.setBounds(450, 200, 40, 20);

        phone.setBounds(60, 260, 60, 20);
        ph.setBounds(125, 260, 100, 20);

        email.setBounds(340, 260, 60, 20);
        em.setBounds(390, 260, 100, 20);

        address.setBounds(60, 320, 60, 20);
        add.setBounds(125, 320, 100, 20);

        img.setBounds(340, 320, 60, 20);
        im.setBounds(390, 320, 100, 20);

        remarks.setBounds(60, 380, 60, 20);
        js.setBounds(60, 400, 500, 100);

        bs.setBounds(200, 520, 60, 20);
        br.setBounds(350, 520, 60, 20);

        add(title);

        add(account);
        add(acc);

        add(password);
        add(pass);

        add(nickname);
        add(nick);

        add(name);
        add(na);

        add(age);
        add(ag);


        add(sex);
        add(sexman);
        add(sexwom);

        add(phone);
        add(ph);

        add(email);
        add(em);

        add(address);
        add(add);

        add(img);
        add(im);

        add(remarks);
        add(js);

        add(bs);
        add(br);

        group.add(sexman);
        group.add(sexwom);


        setTitle("欢迎注册QQ");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}


class JPane extends JPanel {

    public void paintComponent(Graphics g) {
        ImageIcon back = new ImageIcon("img/b2.jpg");
        //g = getContentPane().getGraphics();
        g.drawImage(back.getImage(), 0, 0, 650, 600, null);
    }
}
