package com.df.dbo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 连接数据库
 */
public class LinkSql {

    public Connection con = null;

    public LinkSql() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=myQQ";
            try {
                con = DriverManager.getConnection(url, "sa", "");
//System.out.println("link sqlserver2000 succeed!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

	/*public static void main(String[] args) {
        new LinkSql();
	} */
}
