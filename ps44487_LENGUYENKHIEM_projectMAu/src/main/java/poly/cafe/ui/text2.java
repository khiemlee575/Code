package poly.cafe.ui;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import poly.cafe.entity.Category;
import poly.cafe.util.XJdbc;
import poly.cafe.util.XQuery;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Lenovo LOQ

 */
public class text2 {
     public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=PS44487_POLYCAFE;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String password = "123456";
        try {
            
            Connection conn = XJdbc.openConnection();
            System.out.println("Ket noi thanh cong!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Ket noi that bai!");
            e.printStackTrace();
        }
        // câu 1
        String insertSql = "INSERT INTO Categories (Id, Name) VALUES (?, ?)";
        XJdbc.executeUpdate(insertSql, "C27", "Loai 27");
        XJdbc.executeUpdate(insertSql, "C28", "Loai 28");

//        // câu 2
        String selectSql = "SELECT * FROM Categories WHERE Name LIKE ?";
        try (ResultSet rs = XJdbc.executeQuery(selectSql, "%Loai%")) {
            System.out.println("\n--- Truy van ResultSet ---");
            while (rs.next()) {
                System.out.println(rs.getString("Id") + " - " + rs.getString("Name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        //câu 3
        List<Category> list = XQuery.getBeanList(Category.class, selectSql, "%Loai%");
        System.out.println("\n--- Danh sach Category Bean ---");
        for (Category c : list) {
            System.out.println(c.getId() + " - " + c.getName());
        }
//        //câu 4
        String oneSql = "SELECT * FROM Categories WHERE Id = ?";
        Category cat = XQuery.getSingleBean(Category.class, oneSql, "C02");
        System.out.println("\n--- Mot Category ---");
        if (cat != null) {
            System.out.println(cat.getId() + " - " + cat.getName());
        }
//        //câu 5
        String maxIdSql = "SELECT MAX(Id) FROM Categories WHERE Name LIKE ?";
        String maxId = (String) XJdbc.getValue(maxIdSql, "%Loai%");
        System.out.println("\nMax ID: " + maxId);
   }

}
    