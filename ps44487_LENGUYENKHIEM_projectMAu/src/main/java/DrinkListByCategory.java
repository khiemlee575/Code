import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DrinkListByCategory {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=PS44487_POLYCAFE;user=sa;password=123456;encrypt=true;trustServerCertificate=true;";
        String sql = "SELECT D.Id, D.Name, D.UnitPrice, D.Discount, D.Image, D.Available, C.Name AS CategoryName " +
                     "FROM Drinks D " +
                     "INNER JOIN Categories C ON D.CategoryId = C.Id " +
                     "WHERE C.Id = 'CAT001'";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(url);
            System.out.println("Ket noi thanh cong!");
            
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            
            System.out.printf("%-10s %-15s %-10s %-10s %-15s %-10s %-15s\n", 
                              "Id", "Name", "UnitPrice", "Discount", "Image", "Available", "CategoryName");
            System.out.println("-------------------------------------------------------------------------");
            
            while (rs.next()) {
                String id = rs.getString("Id");
                String name = rs.getString("Name");
                double unitPrice = rs.getDouble("UnitPrice");
                double discount = rs.getDouble("Discount");
                String image = rs.getString("Image");
                boolean available = rs.getBoolean("Available");
                String categoryName = rs.getString("CategoryName");
                
                System.out.printf("%-10s %-15s %-10.2f %-10.2f %-15s %-10b %-15s\n", 
                                  id, name, unitPrice, discount, image, available, categoryName);
            }
            
            rs.close();
            statement.close();
            connection.close();
            System.out.println("Dong ket noi thanh cong!");
            
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}