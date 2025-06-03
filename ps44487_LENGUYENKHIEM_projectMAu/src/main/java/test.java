
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Lenovo LOQ
 *
 */
public class test {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=PS44487_POLYCAFE;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String password = "123456";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Ket noi thanh cong!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Ket noi that bai!");
            e.printStackTrace();
        }
    }
}
