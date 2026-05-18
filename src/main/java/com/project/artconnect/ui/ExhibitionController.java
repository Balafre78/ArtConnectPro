package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ExhibitionController {
    @FXML private TextField searchField;
    @FXML private TableView<Exhibition> exhibitionTable;
    @FXML private ComboBox<String> themeFilter;
    @FXML private TableColumn<Exhibition, String> titleColumn;
    @FXML private TableColumn<Exhibition, LocalDate> startDateColumn;
    @FXML private TableColumn<Exhibition, LocalDate> endDateColumn;
    @FXML private TableColumn<Exhibition, String> curatorColumn;
    @FXML private TableColumn<Exhibition, String> themeColumn;
    @FXML private TableColumn<Exhibition, String> galleryColumn;
    @FXML private TableColumn<Exhibition, String> descriptionColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        curatorColumn.setCellValueFactory(new PropertyValueFactory<>("curatorName"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        galleryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGallery() != null ? cellData.getValue().getGallery().getName() : "Unknown"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        themeFilter.setItems(FXCollections.observableArrayList(exhibitionService.getAllThemes()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        String theme = themeFilter.getValue();
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitionService.searchExhibition(query, theme)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        themeFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Exhibition> dialog = buildExhibitionDialog("Add Exhibition", null);
        Optional<Exhibition> result = dialog.showAndWait();
        result.ifPresent(exhibition -> {
            exhibitionService.createExhibition(exhibition);
            refreshTable();
        });
    }

    @FXML
    private void handleDelete() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select an exhibition to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete exhibition \"" + selected.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                exhibitionService.deleteExhibition(selected);
                refreshTable();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Construit un Dialog avec un formulaire pour créer une exposition.
     * Si exhibition != null, les champs sont pré-remplis (mode Edit).
     */
    private Dialog<Exhibition> buildExhibitionDialog(String title, Exhibition exhibition) {
        Dialog<Exhibition> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        DatePicker startDateField = new DatePicker();
        DatePicker endDateField = new DatePicker();
        TextField descriptionField = new TextField();
        TextField curatorNameField = new TextField();
        ComboBox<String> themeField = new ComboBox<>();
        ComboBox<String> galleryField = new ComboBox<>();


        titleField.setPromptText("Title");
        descriptionField.setPromptText("Description");
        curatorNameField.setPromptText("Curator name");
        themeField.setPromptText("Theme"); themeField.setItems(FXCollections.observableArrayList(exhibitionService.getAllThemes()));
        List<String> galleriesNames = galleryService.getAllGalleries().stream().map(Gallery::getName).toList();
        galleryField.setPromptText("Gallery"); galleryField.setItems(FXCollections.observableArrayList(galleriesNames));


        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1); grid.add(startDateField, 1, 1);
        grid.add(new Label("End Date:"), 0, 2); grid.add(endDateField, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descriptionField, 1, 3);
        grid.add(new Label("Curator Name:"), 0, 4); grid.add(curatorNameField, 1, 4);
        grid.add(new Label("Theme:"), 0, 5); grid.add(themeField, 1, 5);
        grid.add(new Label("Gallery"), 0, 6); grid.add(galleryField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat en objet Exhibition
        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Exhibition e = new Exhibition();
                e.setTitle(titleField.getText().trim());
                e.setStartDate(startDateField.getValue());
                e.setEndDate(endDateField.getValue());
                e.setDescription(descriptionField.getText().trim());
                e.setCuratorName(curatorNameField.getText().trim());
                e.setTheme(themeField.getValue());
                e.setGallery(galleryService.getByName(galleryField.getValue()));
                return e;
            }
            return null;
        });

        return dialog;
    }

    private void refreshTable() {
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitionService.getAllExhibitions()));
        exhibitionTable.refresh();
    }
}
