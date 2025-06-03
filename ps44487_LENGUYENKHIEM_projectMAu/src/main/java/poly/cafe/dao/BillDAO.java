/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.util.Date;
import java.util.List;
import poly.cafe.entity.Bill;

/**
 *
 * @author Lenovo LOQ
 *
 */
public interface BillDAO extends CrudDAO<Bill, Long> {

    List<Bill> findByUsername(String username);

    List<Bill> findByCardId(Integer cardId);

    public List<Bill> findByTimeRange(Date begin, Date end);
}
