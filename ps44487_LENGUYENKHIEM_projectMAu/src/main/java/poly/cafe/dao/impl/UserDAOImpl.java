/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao.impl;

import java.util.List;
import poly.cafe.dao.UserDAO;
import poly.cafe.entity.User;
import poly.cafe.util.XJdbc;
import poly.cafe.util.XQuery;

/**
 *
 * @author Lenovo LOQ
 *
 */
public class UserDAOImpl implements UserDAO {

    String createSql = "INSERT INTO Users (username, password, enabled, fullname, photo, manager) VALUES (?, ?, ?, ?, ?, ?)";
    String updateSql = "UPDATE Users SET username = ?, password = ?, enabled = ?, fullname = ?, photo = ?, manager = ? WHERE username = ?";
    String deleteSql = "DELETE FROM Users WHERE username = ?";
    String findAllSql = "SELECT * FROM Users";
    String findByIdSql = "SELECT * FROM Users WHERE username = ?";

    @Override
    public User create(User entity) {
        XJdbc.executeUpdate(createSql, entity.getUsername(), entity.getPassword(), entity.isEnabled(), entity.getFullname(), entity.getPhoto(), entity.isManager());
        return entity;

    }

    @Override
    public void update(User entity) {
        XJdbc.executeUpdate(updateSql, entity.getUsername(), entity.getPassword(), entity.isEnabled(), entity.getFullname(), entity.getPhoto(), entity.isManager(), entity.getUsername());
        
    }

    @Override
    public void deleteById(String id) {
        XJdbc.executeUpdate(deleteSql, id);
    }

    @Override
    public List< User> findAll() {
        return XQuery.getBeanList(User.class, findAllSql);
    }

   @Override
    public User findById(String id) {
        return XQuery.getSingleBean(User.class, findByIdSql, id);
    }
}
