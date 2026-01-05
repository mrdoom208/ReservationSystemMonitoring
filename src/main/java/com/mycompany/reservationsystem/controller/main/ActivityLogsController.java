package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.ActivityLogRepository;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ActivityLogsController {
    // ====================== Constructor-injected dependencies ======================
    private final AdministratorUIController adminUIController;

    @Autowired
    public ActivityLogsController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    // ====================== Other Dependencies ======================
    private User currentuser;

    @FXML
    private ScrollPane ActivityLogPane;

    @FXML
    private TableView<ActivityLog> ActivityLogsTable;

    @FXML
    private TableColumn<ActivityLog, Void> actionAL;

    @FXML
    private MFXButton applyAL;

    @FXML
    private TableColumn<ActivityLog, String> descriptionAL;

    @FXML
    private MFXDatePicker fromAL;

    @FXML
    private TableColumn<ActivityLog, String> moduleAL;

    @FXML
    private TableColumn<ActivityLog, String> positionAL;

    @FXML
    private MFXTextField searchAL;

    @FXML
    private TableColumn<ActivityLog, LocalDateTime> timestampsAL;

    @FXML
    private MFXDatePicker toAL;

    @FXML
    private TableColumn<?, ?> userAL;

    @FXML
    private ProgressIndicator progressAL;

    private final ObservableList<ActivityLog> activitylogsdata = FXCollections.observableArrayList();
    private FilteredList<ActivityLog> filteredActivityLogs = new FilteredList<>(activitylogsdata, p -> true);

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private int currentPage = 0;
    private final int pageSize = 50; // rows per page
    private boolean hasMore = true; // to check if there are more pages


    private void setupActivityLogs(){
        applyAL.setOnAction(e -> loadActivityLogsPage(true));
        ActivityLogsTable.setRowFactory(tv -> {
            TableRow<ActivityLog> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                // if row is not empty and it's the last loaded row, fetch next page
                if (!row.isEmpty() && row.getIndex() >= activitylogsdata.size() - 1) {
                    loadActivityLogsPage(false);
                }
            });
            return row;
        });
        searchAL.textProperty().addListener((obs, oldValue, newValue) -> {
            String search = (newValue == null) ? "" : newValue.toLowerCase();

            filteredActivityLogs.setPredicate(log -> {
                if (search.isEmpty()) return true;

                return (log.getUser() != null && log.getUser().toLowerCase().contains(search))
                        || (log.getPosition() != null && log.getPosition().toLowerCase().contains(search))
                        || (log.getModule() != null && log.getModule().toLowerCase().contains(search))
                        || (log.getAction() != null && log.getAction().toLowerCase().contains(search))
                        || (log.getDescription() != null && log.getDescription().toLowerCase().contains(search));
            });
        });
        timestampsAL.setCellFactory(column -> new TableCell<ActivityLog, LocalDateTime>() {

            private final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        descriptionAL.setCellFactory(column -> new TableCell<ActivityLog, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Left align the text
                setStyle("-fx-alignment: CENTER-LEFT;");

                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                    setStyle(null);
                } else {
                    setText(item);
                }
            }
        });

        ActivityLogsTable.setItems(filteredActivityLogs);
        TableColumn<?, ?>[] column = {userAL,positionAL,moduleAL,actionAL,descriptionAL,timestampsAL};
        double[] widthFactors = {0.15, 0.15, 0.1, 0.1, 0.35,0.15};
        String[] namecol = {"user", "position", "module", "action", "description","timestamp"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(ActivityLogsTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        ActivityLogsTable.setPlaceholder(new Label("No Activity Data "));
    }
    private void loadActivityLogsPage(boolean reset) {
        if (reset) {
            currentPage = 0;
            hasMore = true;
            activitylogsdata.clear();
        }

        if (!hasMore) return;

        progressAL.setVisible(true); // show indicator

        LocalDate from = fromAL.getValue();
        LocalDateTime startDateTime = (from != null) ? from.atStartOfDay() : null;

        LocalDate to = toAL.getValue();
        LocalDateTime endDateTime = (to != null) ? to.atTime(23, 59, 59) : null;

        int pageToLoad = currentPage;

        javafx.concurrent.Task<Page<ActivityLog>> task = new javafx.concurrent.Task<>() {
            @Override
            protected Page<ActivityLog> call() {
                Pageable pageable = PageRequest.of(pageToLoad, pageSize, Sort.by("timestamp").descending());
                return activityLogRepository.filterByDate(startDateTime, endDateTime, pageable);
            }
        };

        task.setOnSucceeded(event -> {
            Page<ActivityLog> dataPage = task.getValue();
            activitylogsdata.addAll(dataPage.getContent());
            hasMore = dataPage.hasNext();
            currentPage++;
            progressAL.setVisible(false); // hide after loading
        });

        task.setOnFailed(event -> {
            progressAL.setVisible(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void initialize(){
        currentuser = adminUIController.getCurrentUser();
        loadActivityLogsPage(true);
        setupActivityLogs();


    }

}
