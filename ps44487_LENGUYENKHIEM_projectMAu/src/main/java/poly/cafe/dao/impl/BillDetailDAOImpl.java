/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao.impl;

import java.util.List;
import poly.cafe.dao.BillDetailDAO;
import poly.cafe.entity.BillDetail;
import poly.cafe.util.XJdbc;
import poly.cafe.util.XQuery;

/**
 *
 * @author Lenovo LOQ
 *
 */
public class BillDetailDAOImpl implements BillDetailDAO {

    String createSql = "INSERT INTO BillDetails(Id, BillId, DrinkId, UnitPrice, Discount, Quantity) VALUES(?, ?, ?, ?, ?, ?)";
    String updateSql = "UPDATE BillDetails SET BillId=?, DrinkId=?, UnitPrice=?, Discount=?, Quantity=? WHERE Id=?";
    String deleteSql = "DELETE FROM BillDetails WHERE Id=?";
    String findAllSql = "SELECT bd.Id, bd.BillId, bd.DrinkId, bd.UnitPrice, bd.Discount, bd.Quantity, d.name AS drinkName "
            + "FROM BILLDETAILS bd JOIN DRINKS d ON d.Id = bd.DrinkId";
    String findByIdSql = "SELECT bd.Id, bd.BillId, bd.DrinkId, bd.UnitPrice, bd.Discount, bd.Quantity, d.name AS drinkName "
            + "FROM BILLDETAILS bd JOIN DRINKS d ON d.Id = bd.DrinkId WHERE bd.Id=?";
    String findByBillIdSql = "SELECT bd.Id, bd.BillId, bd.DrinkId, bd.UnitPrice, bd.Discount, bd.Quantity, d.name AS drinkName "
            + "FROM BILLDETAILS bd JOIN DRINKS d ON d.Id = bd.DrinkId WHERE bd.BillId=?";
    String findByDrinkIdSql = "SELECT bd.Id, bd.BillId, bd.DrinkId, bd.UnitPrice, bd.Discount, bd.Quantity, d.name AS drinkName "
            + "FROM BILLDETAILS bd JOIN DRINKS d ON d.Id = bd.DrinkId WHERE bd.DrinkId=?";

    @Override
    public BillDetail create(BillDetail entity) {
        XJdbc.executeUpdate(createSql,
                entity.getId(),
                entity.getBillId(),
                entity.getDrinkId(),
                entity.getUnitPrice(),
                entity.getDiscount(),
                entity.getQuantity()
        );
        return entity;
    }

    @Override
    public void update(BillDetail entity) {
        XJdbc.executeUpdate(updateSql,
                entity.getBillId(),
                entity.getDrinkId(),
                entity.getUnitPrice(),
                entity.getDiscount(),
                entity.getQuantity(),
                entity.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        XJdbc.executeUpdate(deleteSql, id);
    }

    @Override
    public List<BillDetail> findAll() {
        return XQuery.getBeanList(BillDetail.class, findAllSql);
    }

    @Override
    public BillDetail findById(Long id) {
        return XQuery.getSingleBean(BillDetail.class, findByIdSql, id);
    }

    @Override
    public List<BillDetail> findByBillId(Long billId) {
        return XQuery.getBeanList(BillDetail.class, findByBillIdSql, billId);
    }

    @Override
    public List<BillDetail> findByDrinkId(String drinkId) {
        return XQuery.getBeanList(BillDetail.class, findByDrinkIdSql, drinkId);
    }
}
