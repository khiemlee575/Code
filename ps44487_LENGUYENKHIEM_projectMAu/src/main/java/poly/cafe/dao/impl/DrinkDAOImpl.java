/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao.impl;

import java.util.List;
import poly.cafe.dao.DrinkDAO;
import poly.cafe.entity.Drink;
import poly.cafe.util.XJdbc;
import poly.cafe.util.XQuery;

/**
 *
 * @author Lenovo LOQ
 *
 */
public class DrinkDAOImpl implements DrinkDAO {

    String createSql = "INSERT INTO Drinks (id, name, image, unitPrice, discount, available, categoryId) VALUES (?, ?, ?, ?, ?, ?, ?)";
    String updateSql = "UPDATE Drinks SET name = ?, image = ?, unitPrice = ?, discount = ?, available = ?, categoryId = ? WHERE id = ?";
    String deleteSql = "DELETE FROM Drinks WHERE id = ?";
    String findAllSql = "SELECT * FROM Drinks";
    String findByIdSql = "SELECT * FROM Drinks WHERE id = ?";
    String findByCategoryIdSql = "SELECT * FROM Drinks WHERE CategoryId=?";

    @Override
    public Drink create(Drink entity) {
        XJdbc.executeUpdate(createSql,
                entity.getId(),
                entity.getName(),
                entity.getImage(),
                entity.getUnitPrice(),
                entity.getDiscount(),
                entity.isAvailable(),
                entity.getCategoryId()
        );
        return entity;
    }

    @Override
    public void update(Drink entity) {
        XJdbc.executeUpdate(updateSql, 
            entity.getName(), 
            entity.getImage(), 
            entity.getUnitPrice(), 
            entity.getDiscount(), 
            entity.isAvailable(), 
            entity.getCategoryId(), 
            entity.getId()
        );
    }

    @Override
    public void deleteById(String id) {
        XJdbc.executeUpdate(deleteSql, id);
    }

    @Override
    public List< Drink> findAll() {
        return XQuery.getBeanList(Drink.class, findAllSql);
    }

    @Override
    public Drink findById(String id) {
        return XQuery.getSingleBean(Drink.class, findByIdSql, id);
    }

    @Override
    public List<Drink> findByCategoryId(String categoryId) {
        return XQuery.getBeanList(Drink.class, findByCategoryIdSql, categoryId);
    }
}
