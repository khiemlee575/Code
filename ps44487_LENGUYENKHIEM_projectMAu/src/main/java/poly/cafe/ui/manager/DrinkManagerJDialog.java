/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import poly.cafe.dao.CategoryDAO;
import poly.cafe.dao.DrinkDAO;
import poly.cafe.dao.impl.CategoryDAOImpl;
import poly.cafe.dao.impl.DrinkDAOImpl;
import poly.cafe.entity.Category;
import poly.cafe.entity.Drink;
import poly.cafe.util.XIcon;
import poly.cafe.util.XJdbc;



/**
 *
 * @author Lenovo
 */
public class DrinkManagerJDialog extends javax.swing.JDialog implements DrinkController {

    private DrinkDAO dao = new DrinkDAOImpl();
    private CategoryDAO cdao = new CategoryDAOImpl();
    private List<Category> categories = new ArrayList<>();
    private List<Drink> items = new ArrayList<>();
    private JFileChooser fileChooser = new JFileChooser();
    private int currentIndex = -1;

    public DrinkManagerJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);

        // Thêm ChangeListener để cập nhật lblDiscount
        SldDiscount.addChangeListener(e -> {
            lblDiscount.setText(SldDiscount.getValue() + "%");
        });
        // Thêm ListSelectionListener cho tblCategories (như đã có từ trước)
        tblCategories.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillToTable();
            }
        });
        open();
    }

    @Override
    public void open() {
        setLocationRelativeTo(null);
        fillCategories();
        fillToTable();
    }

    @Override
    public void fillCategories() {
        // Lưu lựa chọn hiện tại
        String selectedCategoryName = (String) cboCategories.getSelectedItem();
        int selectedCategoryRow = tblCategories.getSelectedRow();

        cboCategories.removeAllItems(); // Xóa tất cả phần tử cũ
        DefaultTableModel tblModel = (DefaultTableModel) tblCategories.getModel();
        tblModel.setRowCount(0);

        categories = cdao.findAll();
        if (categories.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không có danh mục nào trong cơ sở dữ liệu!");
            return;
        }

        categories.forEach(category -> {
            cboCategories.addItem(category.getName()); // Thêm tên danh mục
            tblModel.addRow(new Object[]{category.getName()});
        });

        // Khôi phục lựa chọn
        if (selectedCategoryName != null) {
            cboCategories.setSelectedItem(selectedCategoryName);
        }
        if (selectedCategoryRow != -1 && selectedCategoryRow < tblCategories.getRowCount()) {
            tblCategories.setRowSelectionInterval(selectedCategoryRow, selectedCategoryRow);
        } else if (tblCategories.getRowCount() > 0) {
            tblCategories.setRowSelectionInterval(0, 0);
        }
        syncCategorySelection();
    }

    @Override
    public void fillToTable() {
        DefaultTableModel model = (DefaultTableModel) tblDrinks.getModel();
        model.setRowCount(0);

        int selectedRow = tblCategories.getSelectedRow();
        if (selectedRow == -1 || categories.isEmpty()) {
            if (categories.isEmpty()) {
                fillCategories();
            }
            if (!categories.isEmpty()) {
                tblCategories.setRowSelectionInterval(0, 0);
                selectedRow = 0;
            } else {
                return;
            }
        }

        Category category = categories.get(selectedRow);
        items = dao.findByCategoryId(category.getId()); // Lấy dữ liệu mới từ cơ sở dữ liệu

        items.forEach(item -> {
            Object[] row = {
                item.getId(),
                item.getName(),
                item.getUnitPrice(),
                String.format("%.0f%%", item.getDiscount() * 100),
                item.isAvailable() ? "Sẵn có" : "Hết hàng",
                false
            };
            model.addRow(row);
        });

        clear();
    }

    @Override
    public void setForm(Drink drink) {
        txtId.setText(drink.getId());
        txtName.setText(drink.getName());
        txtPrice.setText(String.valueOf(drink.getUnitPrice()));
        int discountPercent = (int) (drink.getDiscount() * 100); // Chuyển discount thành phần trăm
        SldDiscount.setValue(discountPercent); // Đặt giá trị cho slider
        lblDiscount.setText(discountPercent + "%"); // Cập nhật nhãn
        String categoryName = categories.stream()
                .filter(cat -> cat.getId().equals(drink.getCategoryId()))
                .findFirst()
                .map(Category::getName)
                .orElse(null);
        cboCategories.setSelectedItem(categoryName);
        if (drink.isAvailable()) {
            rdoAvailable.setSelected(true);
        } else {
            rdoUnavailable.setSelected(true);
        }
        if (drink.getImage() != null) {
            File file = new File("images/" + drink.getImage());
            XIcon.setIcon(lblImage, file);
            lblImage.setToolTipText(file.getName());
        }
    }

    @Override
    public Drink getForm() {
        Drink drink = new Drink();
        if (!txtId.getText().isBlank()) {
            drink.setId(txtId.getText());
        }
        drink.setName(txtName.getText());
        try {
            drink.setUnitPrice(Double.parseDouble(txtPrice.getText()));
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Đơn giá phải là số hợp lệ!");
            return null;
        }
        drink.setDiscount(SldDiscount.getValue() / 100.0);
        String selectedCategoryName = (String) cboCategories.getSelectedItem();
        if (selectedCategoryName == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn mã loại hợp lệ!");
            return null;
        }
        Category selectedCategory = categories.stream()
                .filter(cat -> cat.getName().equals(selectedCategoryName))
                .findFirst()
                .orElse(null);
        if (selectedCategory == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Mã loại không hợp lệ!");
            return null;
        }
        drink.setCategoryId(selectedCategory.getId());
        drink.setAvailable(rdoAvailable.isSelected());
        drink.setImage(lblImage.getToolTipText());
        return drink;
    }

    @Override
    public void edit() {
        int selectedRow = tblDrinks.getSelectedRow();
        if (selectedRow >= 0) {
            currentIndex = selectedRow;
            Drink drink = items.get(currentIndex);
            setForm(drink);
            tabs.setSelectedIndex(1);
        }
    }

    @Override
    public void create() {
        Drink drink = getForm();
        if (drink == null) {
            return;
        }
        try {
            dao.create(drink);
            items.add(drink);
            addRowToTable(drink);
            javax.swing.JOptionPane.showMessageDialog(this, "Tạo đồ uống thành công!");
            clear();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi tạo đồ uống: " + e.getMessage());
        }
    }

    private void addRowToTable(Drink drink) {
        DefaultTableModel model = (DefaultTableModel) tblDrinks.getModel();
        Object[] row = {
            drink.getId(),
            drink.getName(),
            drink.getUnitPrice(),
            String.format("%.0f%%", drink.getDiscount() * 100),
            drink.isAvailable() ? "Sẵn có" : "Hết hàng",
            false
        };
        model.addRow(row);
    }

    @Override
    public void update() {
        if (currentIndex >= 0) {
            Drink entity = this.getForm();
            if (entity == null) {
                return;
            }
            try {
                String sql = "UPDATE Drinks SET Name=?, Image=?, UnitPrice=?, Discount=?, Available=?, CategoryId=? WHERE Id=?";
                XJdbc.executeUpdate(sql,
                        entity.getName(),
                        entity.getImage(),
                        entity.getUnitPrice(),
                        entity.getDiscount(),
                        entity.isAvailable() ? 1 : 0,
                        entity.getCategoryId(),
                        entity.getId()
                );
                items.set(currentIndex, entity);
                this.fillToTable();
                javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật đồ uống thành công!");
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật đồ uống: " + e.getMessage());
            }
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn đồ uống để cập nhật!");
        }
    }

    @Override
    public void delete() {
        if (currentIndex >= 0) {
            String drinkId = txtId.getText();
            dao.deleteById(drinkId);
            items.remove(currentIndex);
            fillToTable();
            clear();
            javax.swing.JOptionPane.showMessageDialog(this, "Xóa đồ uống thành công!");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn đồ uống để xóa!");
        }
    }

    @Override
    public void clear() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        SldDiscount.setValue(0); // Đặt lại slider về 0
        lblDiscount.setText("0%"); // Cập nhật nhãn
        if (!categories.isEmpty()) {
            cboCategories.setSelectedIndex(0);
        }
        rdoAvailable.setSelected(true);
        rdoUnavailable.setSelected(false);
        lblImage.setIcon(null);
        lblImage.setToolTipText("");
        currentIndex = -1;
    }

    @Override
    public void setEditable(boolean editable) {
        txtId.setEditable(editable);
        txtName.setEditable(editable);
        txtPrice.setEditable(editable);
        SldDiscount.setEnabled(editable);
        cboCategories.setEnabled(editable);
        rdoUnavailable.setEnabled(editable);
        rdoAvailable.setEnabled(editable);
    }

    @Override
    public void checkAll() {
        DefaultTableModel model = (DefaultTableModel) tblDrinks.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(true, i, 5);
        }
    }

    @Override
    public void uncheckAll() {
        DefaultTableModel model = (DefaultTableModel) tblDrinks.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(false, i, 5);
        }
    }

    @Override
    public void deleteCheckedItems() {
        DefaultTableModel model = (DefaultTableModel) tblDrinks.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            boolean isChecked = (boolean) model.getValueAt(i, 5);
            if (isChecked) {
                String drinkId = model.getValueAt(i, 0).toString();
                dao.deleteById(drinkId);
                model.removeRow(i);
            }
        }
        items = dao.findAll();
        fillToTable();
        clear();
    }

    @Override
    public void moveFirst() {
        if (items.size() > 0) {
            currentIndex = 0;
            setForm(items.get(currentIndex));
        }
    }

    @Override
    public void movePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            setForm(items.get(currentIndex));
        }
    }

    @Override
    public void moveNext() {
        if (currentIndex < items.size() - 1) {
            currentIndex++;
            setForm(items.get(currentIndex));
        }
    }

    @Override
    public void moveLast() {
        if (items.size() > 0) {
            currentIndex = items.size() - 1;
            setForm(items.get(currentIndex));
        }
    }

    @Override
    public void moveTo(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            currentIndex = rowIndex;
            setForm(items.get(currentIndex));
        }
    }

    @Override
    public void chooseFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            File file = XIcon.copyTo(selectedFile, "images"); // Giả sử XIcon.copyTo sao chép tệp vào thư mục images
            lblImage.setToolTipText(file.getName());
            XIcon.setIcon(lblImage, file); // Giả sử XIcon.setIcon cập nhật hình ảnh cho JLabel
        }
    }

    private void syncCategorySelection() {
        String selectedCategoryName = (String) cboCategories.getSelectedItem();
        if (selectedCategoryName == null) {
            return;
        }

        // Tìm chỉ số của danh mục trong tblCategories
        DefaultTableModel tblModel = (DefaultTableModel) tblCategories.getModel();
        for (int i = 0; i < tblModel.getRowCount(); i++) {
            String categoryName = (String) tblModel.getValueAt(i, 0);
            if (selectedCategoryName.equals(categoryName)) {
                tblCategories.setRowSelectionInterval(i, i);
                break;
            }
        }
    }
            /**
             * Creates new form DrinkManagerJDialog
             */

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tabs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDrinks = new javax.swing.JTable();
        btnCheckAll = new javax.swing.JButton();
        btnUncheckAll = new javax.swing.JButton();
        btnDeleteCheckedItems = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblCategories = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        btnCreate = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnMoveFirst = new javax.swing.JButton();
        btnMovePrevious = new javax.swing.JButton();
        btnMoveNext = new javax.swing.JButton();
        btnMoveLast = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        SldDiscount = new javax.swing.JSlider();
        txtPrice = new javax.swing.JTextField();
        cboCategories = new javax.swing.JComboBox<>();
        lblImage = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        rdoUnavailable = new javax.swing.JRadioButton();
        rdoAvailable = new javax.swing.JRadioButton();
        lblDiscount = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblDrinks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã đồ uống", "Tên đồ uống", "Đơn giá", "Giảm giá", "Trạng thái", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblDrinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDrinksMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDrinks);

        btnCheckAll.setText("Chọn tất cả");
        btnCheckAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckAllActionPerformed(evt);
            }
        });

        btnUncheckAll.setText("Bỏ chọn tất cả");
        btnUncheckAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUncheckAllActionPerformed(evt);
            }
        });

        btnDeleteCheckedItems.setText("Xóa các mục chọn");
        btnDeleteCheckedItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCheckedItemsActionPerformed(evt);
            }
        });

        tblCategories.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Loại đồ uống"
            }
        ));
        jScrollPane2.setViewportView(tblCategories);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCheckAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnUncheckAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteCheckedItems)
                .addGap(20, 20, 20))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCheckAll)
                    .addComponent(btnDeleteCheckedItems)
                    .addComponent(btnUncheckAll))
                .addContainerGap())
        );

        tabs.addTab("DANH SÁCH", jPanel1);

        jLabel1.setText("Mã đồ uống");

        jLabel2.setText("Tên đồ uống");

        btnCreate.setText("Tạo mới");
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        btnUpdate.setText("Cập nhật");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnClear.setText("Nhập mới");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnDelete.setText("Xóa");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnMoveFirst.setText("|<");
        btnMoveFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveFirstActionPerformed(evt);
            }
        });

        btnMovePrevious.setText("<<");
        btnMovePrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMovePreviousActionPerformed(evt);
            }
        });

        btnMoveNext.setText(">>");
        btnMoveNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveNextActionPerformed(evt);
            }
        });

        btnMoveLast.setText(">|");
        btnMoveLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveLastActionPerformed(evt);
            }
        });

        jLabel3.setText("Giảm giá");

        jLabel4.setText("Tên loại");

        jLabel5.setText("Đơn giá");

        cboCategories.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Trạng thái");

        buttonGroup1.add(rdoUnavailable);
        rdoUnavailable.setText("Hết hàng");

        buttonGroup1.add(rdoAvailable);
        rdoAvailable.setText("Sẵn có");
        rdoAvailable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoAvailableActionPerformed(evt);
            }
        });

        lblDiscount.setText("0%");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear)
                        .addGap(156, 156, 156)
                        .addComponent(btnMoveFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMovePrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveLast, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 269, Short.MAX_VALUE))
                    .addComponent(jSeparator1))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(cboCategories, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtId, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtPrice, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtName)
                        .addComponent(jLabel2)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(SldDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblDiscount)
                            .addGap(21, 21, 21)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(rdoAvailable, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdoUnavailable, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SldDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDiscount))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdoAvailable)
                            .addComponent(rdoUnavailable))))
                .addGap(29, 29, 29)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnClear)
                    .addComponent(btnMoveFirst)
                    .addComponent(btnMovePrevious)
                    .addComponent(btnMoveNext)
                    .addComponent(btnMoveLast))
                .addGap(35, 35, 35))
        );

        tabs.addTab("BIỂU MẪU", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 988, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblDrinksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDrinksMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            this.edit();

        }
    }//GEN-LAST:event_tblDrinksMouseClicked

    private void btnCheckAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckAllActionPerformed
        // TODO add your handling code here:
        this.checkAll();
    }//GEN-LAST:event_btnCheckAllActionPerformed

    private void btnUncheckAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUncheckAllActionPerformed
        // TODO add your handling code here:
        this.uncheckAll();
    }//GEN-LAST:event_btnUncheckAllActionPerformed

    private void btnDeleteCheckedItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCheckedItemsActionPerformed
        // TODO add your handling code here:
        this.deleteCheckedItems();
    }//GEN-LAST:event_btnDeleteCheckedItemsActionPerformed

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        // TODO add your handling code here:
        this.create();
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        this.update();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        this.clear();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        this.delete();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnMoveFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveFirstActionPerformed
        // TODO add your handling code here:
        this.moveFirst();
    }//GEN-LAST:event_btnMoveFirstActionPerformed

    private void btnMovePreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMovePreviousActionPerformed
        // TODO add your handling code here:
        this.movePrevious();
    }//GEN-LAST:event_btnMovePreviousActionPerformed

    private void btnMoveNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveNextActionPerformed
        // TODO add your handling code here:
        this.moveNext();
    }//GEN-LAST:event_btnMoveNextActionPerformed

    private void btnMoveLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveLastActionPerformed
        // TODO add your handling code here:
        this.moveLast();
    }//GEN-LAST:event_btnMoveLastActionPerformed

    private void rdoAvailableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoAvailableActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdoAvailableActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DrinkManagerJDialog dialog = new DrinkManagerJDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider SldDiscount;
    private javax.swing.JButton btnCheckAll;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteCheckedItems;
    private javax.swing.JButton btnMoveFirst;
    private javax.swing.JButton btnMoveLast;
    private javax.swing.JButton btnMoveNext;
    private javax.swing.JButton btnMovePrevious;
    private javax.swing.JButton btnUncheckAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboCategories;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblDiscount;
    private javax.swing.JLabel lblImage;
    private javax.swing.JRadioButton rdoAvailable;
    private javax.swing.JRadioButton rdoUnavailable;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTable tblCategories;
    private javax.swing.JTable tblDrinks;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    // End of variables declaration//GEN-END:variables

}
