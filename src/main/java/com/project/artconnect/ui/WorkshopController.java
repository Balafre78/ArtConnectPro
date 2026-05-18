package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkshopController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private TableView<Workshop> workshopTable;
    @FXML private TableColumn<Workshop, String> titleColumn;
    @FXML private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop, Integer> durationColumn;
    @FXML private TableColumn<Workshop, Integer> maxParticipantsColumn;
    @FXML private TableColumn<Workshop, String> instructorColumn;
    @FXML private TableColumn<Workshop, Double> priceColumn;
    @FXML private TableColumn<Workshop, String> levelColumn;
    @FXML private TableColumn<Workshop, String> locationColumn;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null ? cellData.getValue().getInstructor().getName()
                        : "Unknown"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        maxParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("maxParticipants"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        difficultyFilter.setItems(FXCollections.observableArrayList(List.of("Débutant", "Intermédiaire", "Avancé")));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        String difficulty = difficultyFilter.getValue();

        workshopTable.setItems(FXCollections.observableArrayList(workshopService.searchWorkshop(query, difficulty)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        difficultyFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Workshop> dialog = buildWorkshopDialog("Add Workhop", null);
        Optional<Workshop> result = dialog.showAndWait();
        result.ifPresent(workshop -> {
            workshopService.createWorkshop(workshop);
            refreshTable();
        });
    }

    @FXML
    private void handleEdit() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select an artist to edit.");
            return;
        }
        Dialog<Workshop> dialog = buildWorkshopDialog("Edit Workshop", selected);
        Optional<Workshop> result = dialog.showAndWait();
        result.ifPresent(workshop -> {
            workshopService.updateWorkshop(workshop);
            refreshTable();
        });
    }

    @FXML
    private void handleDelete() {

    }

    private Dialog<Workshop> buildWorkshopDialog(String title, Workshop workshop) {
        Dialog<Workshop> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        DatePicker dateField = new DatePicker();
        ComboBox<String> levelField = new ComboBox<>();
        TextField durationMinutesField = new TextField();
        TextField maxParticipantsField = new TextField();
        TextField descriptionField = new TextField();
        TextField priceField = new TextField();
        TextField locationField = new TextField();
        ComboBox<String> curatorNameField = new ComboBox<>();

        titleField.setPromptText("Title");
        dateField.setPromptText("Date");
        levelField.setPromptText("Level"); levelField.setItems(FXCollections.observableArrayList(List.of("Débutant", "Intermédiaire", "Avancé")));
        durationMinutesField.setPromptText("DurationTime");
        maxParticipantsField.setPromptText("Max participants");
        descriptionField.setPromptText("Description");
        priceField.setPromptText("Price");
        locationField.setPromptText("Location");
        List<String> artistsNames = artistService.getAllArtists().stream().map(Artist::getName).toList();
        curatorNameField.setPromptText("Curator Name") ; curatorNameField.setItems(FXCollections.observableArrayList(artistsNames));

        // Pré-remplir si mode Edit
        if (workshop != null) {
            titleField.setText(workshop.getTitle());
            titleField.setDisable(true);
            dateField.setValue(workshop.getDate().toLocalDate());
            levelField.setValue(workshop.getLevel());
            durationMinutesField.setText(String.valueOf(workshop.getDurationMinutes()));
            maxParticipantsField.setText(String.valueOf(workshop.getMaxParticipants()));
            descriptionField.setText(workshop.getDescription());
            priceField.setText(String.valueOf(workshop.getPrice()));
            locationField.setText(workshop.getLocation());
            curatorNameField.setValue(workshop.getInstructor().getName());
        }

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Date"), 0, 1); grid.add(dateField, 1, 1);
        grid.add(new Label("Level"), 0, 2); grid.add(levelField, 1, 2);
        grid.add(new Label("Duration Minutes"), 0, 3); grid.add(durationMinutesField, 1, 3);
        grid.add(new Label("Max Participants"), 0, 4); grid.add(maxParticipantsField, 1, 4);
        grid.add(new Label("Description"), 0, 5); grid.add(descriptionField, 1, 5);
        grid.add(new Label("Price"), 0, 6); grid.add(priceField, 1, 6);
        grid.add(new Label("Location"), 0, 7); grid.add(locationField, 1, 7);
        grid.add(new Label("Instructor"), 0, 8); grid.add(curatorNameField, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Workshop w = (workshop != null) ? workshop : new Workshop();
                w.setTitle(titleField.getText().trim());
                w.setDate(dateField.getValue().atStartOfDay());
                w.setLevel(levelField.getValue());
                w.setDurationMinutes(Integer.parseInt(durationMinutesField.getText().trim()));
                w.setMaxParticipants(Integer.parseInt(maxParticipantsField.getText().trim()));
                w.setDescription(descriptionField.getText().trim());
                w.setPrice(Double.parseDouble(priceField.getText()));
                w.setLocation(locationField.getText().trim());
                w.setInstructor(artistService.getArtistByName(curatorNameField.getValue()));
                return w;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
        workshopTable.refresh();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
