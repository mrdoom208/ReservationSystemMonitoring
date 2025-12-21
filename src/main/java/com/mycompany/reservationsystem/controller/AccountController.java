package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.UserRepository;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountController {
    // ====================== Constructor-injected dependencies ======================
    private final AdministratorUIController adminUIController;

    @Autowired
    public AccountController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    @FXML
    private TableView<User> AccountTable;

    @FXML
    private ScrollPane ManageStaffAndAccountsPane;

    @FXML
    private TableColumn<User, Void> actionAT;

    @FXML
    private TableColumn<User, String> firstnameAT;

    @FXML
    private TableColumn<User, String> lastnameAT;

    @FXML
    private TableColumn<User, String> positionAT;

    @FXML
    private MFXComboBox positionfilterAT;

    @FXML
    private MFXTextField searchAT;

    @FXML
    private TableColumn<?, ?> statusAT;

    @FXML
    private TableColumn<?, ?> usernameAT;

    @Autowired
    UserRepository userRepository;

    private final ObservableList<User> UserData = FXCollections.observableArrayList();



    public void setupAccountTable(){
        AccountTable.setItems(UserData);
        actionAT.setCellFactory(col -> new TableCell<User, Void>() {
            FontIcon editIcon = new FontIcon(FontAwesomeSolid.PEN_SQUARE);
            FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);

            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox hbox = new HBox(5);

            {
                editIcon.setIconSize(12);
                editIcon.setIconColor(Color.web("#000000"));
                deleteIcon.setIconSize(12);
                deleteIcon.setIconColor(Color.web("#ffffff"));

                btnEdit.setGraphic(editIcon);
                btnEdit.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                btnEdit.getStyleClass().add("edit");

                btnDelete.setGraphic(deleteIcon);
                btnDelete.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                btnDelete.getStyleClass().add("delete");


                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().addAll(btnEdit, btnDelete);
                btnEdit.setMaxWidth(Double.MAX_VALUE);
                btnDelete.setMaxWidth(Double.MAX_VALUE);
                btnEdit.setMaxHeight(Double.MAX_VALUE);
                btnDelete.setMaxHeight(Double.MAX_VALUE);

                HBox.setHgrow(btnEdit, Priority.ALWAYS);
                HBox.setHgrow(btnDelete, Priority.ALWAYS);


                btnEdit.setOnAction(event -> {
                    User data = getTableView().getItems().get(getIndex());


                });

                btnDelete.setOnAction(event -> {
                    User data = getTableView().getItems().get(getIndex());

                    if (data != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/deleteTableDialog.fxml"));
                            Parent root = loader.load();

                            // Get controller to handle callback
                            DeleteTableDialogController controller = loader.getController();
                            controller.setOnDelete(() -> {
                                if ("Active".equals(data.getStatus())) {
                                    adminUIController.getTableController().showAlert("This account cannot be deleted because it is currently in use");
                                    return;
                                }else{
                                    userRepository.deleteById(data.getId());
                                    loadAccountTable();

                                }

                            });

                            // Create & show dialog
                            Stage dialog = new Stage(StageStyle.UNDECORATED);
                            dialog.initModality(Modality.APPLICATION_MODAL);
                            dialog.initStyle(StageStyle.TRANSPARENT);
                            dialog.setResizable(false);
                            Scene scn = new Scene(root);
                            scn.setFill(Color.TRANSPARENT);
                            dialog.setScene(scn);
                            dialog.showAndWait();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);

                }else{
                    setText(null);       // clear text
                    setGraphic(hbox);
                }
            }
            private User getCurrentItem() {
                int i = getIndex();
                if (i >= 0 && i < getTableView().getItems().size()) {
                    return getTableView().getItems().get(i);
                }
                return null;
            }

        });

        TableColumn<?, ?>[] column = {usernameAT,firstnameAT,lastnameAT,positionAT,statusAT,actionAT};
        double[] widthFactors = {0.15, 0.15, 0.15, 0.15, 0.15,0.25};
        String[] namecol = {"username", "firstname", "lastname", "position", "status",""};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i + " (expected: " + namecol[i] + ")");
                continue;
            } else {
                System.out.println("✔ Column OK: index " + i + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(AccountTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        AccountTable.setPlaceholder(new Label("No Account yet "));
    }
    public void loadAccountTable(){
        List<User> data = userRepository.findAll();
        UserData.setAll(data);

    }

    public void initialize(){
        loadAccountTable();
        setupAccountTable();

    }

}
