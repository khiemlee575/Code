/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package poly.cafe.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Lenovo LOQ

 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Bill {
private Long id;
private String username;
private Integer cardId;
@Builder.Default
private Date checkin = new Date();
private Date checkout;
private int status;
}
