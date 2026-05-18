package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class ArtworkController {
    @FXML private TextField searchField;
    @FXML private ComboBox<ArtworkTag> tagFilter;
    @FXML private TableView<Artwork> artworkTable;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> creationYearColumn;
    @FXML private TableColumn<Artwork, String> typeColumn;
    @FXML private TableColumn<Artwork, String> mediumColumn;
    @FXML private TableColumn<Artwork, Double> priceColumn;
    @FXML private TableColumn<Artwork, String> statusColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;
    @FXML private TableColumn<Artwork, String> descriptionColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        creationYearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        mediumColumn.setCellValueFactory(new PropertyValueFactory<>("medium"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        tagFilter.setItems(FXCollections.observableArrayList(artworkService.getAllTags()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        ArtworkTag tag = tagFilter.getValue();
        String tagName = (tag != null) ? tag.getName() : null;

        artworkTable.setItems(FXCollections.observableArrayList(artworkService.searchArtwork(query, tagName)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        tagFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Artwork> dialog = buildArtworkDialog("Add Artwork", null);
        Optional<Artwork> result = dialog.showAndWait();
        result.ifPresent(artwork -> {
            artworkService.createArtwork(artwork);
            refreshTable();
        });
    }

    @FXML
    private void handleEdit() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select an artist to edit.");
            return;
        }
        Dialog<Artwork> dialog = buildArtworkDialog("Edit Artwork", selected);
        Optional<Artwork> result = dialog.showAndWait();
        result.ifPresent(artwork -> {
            artworkService.updateArtwork(artwork);
            refreshTable();
        });
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select an artist to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete artist \"" + selected.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                artworkService.deleteArtwork(selected);
                refreshTable();
            }
        });
    }

    private Dialog<Artwork> buildArtworkDialog(String title, Artwork artwork) {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextField creationYearField = new TextField();
        TextField typeField = new TextField();
        TextField mediumField = new TextField();
        TextField priceField = new TextField();
        TextField descriptionField = new TextField();
        ComboBox<String> artistField = new ComboBox<>();
        ComboBox<Artwork.Status> statusField = new ComboBox<>();

        titleField.setPromptText("Title");
        creationYearField.setPromptText("Creation Year");
        typeField.setPromptText("Type");
        mediumField.setPromptText("Medium");
        priceField.setPromptText("Price");
        descriptionField.setPromptText("Description");
        List<String> artistsNames = artistService.getAllArtists().stream().map(Artist::getName).toList();
        artistField.setPromptText("Artist Name"); artistField.setItems(FXCollections.observableArrayList(artistsNames));
        statusField.setPromptText("Status"); statusField.setItems(FXCollections.observableArrayList(Artwork.Status.values()));

        // Pré-remplir si mode Edit
        if (artwork != null) {
            titleField.setText(artwork.getTitle());
            titleField.setDisable(true);
            creationYearField.setText(artwork.getCreationYear() != null ? artwork.getCreationYear().toString() : "");
            typeField.setText(artwork.getType() != null ? String.valueOf(artwork.getType()) : "");
            mediumField.setText(artwork.getType() != null ? String.valueOf(artwork.getMedium()) : "");
            priceField.setText(String.valueOf(artwork.getPrice()));
            descriptionField.setText(artwork.getDescription());
            artistField.setValue(artwork.getArtist() != null ? artwork.getArtist().getName() : "");
            artistField.setDisable(true);
            statusField.setValue(artwork.getStatus());
        }

        grid.add(new Label("Title:"),0,0); grid.add(titleField,1,0);
        grid.add(new Label("Creation Year:"),0,1); grid.add(creationYearField,1,1);
        grid.add(new Label("Type:"),0,2); grid.add(typeField,1,2);
        grid.add(new Label("Medium:"),0,3); grid.add(mediumField,1,3);
        grid.add(new Label("Price:"),0,4); grid.add(priceField,1,4);
        grid.add(new Label("Description:"),0,5); grid.add(descriptionField,1,5);
        grid.add(new Label("Artist Name:"),0,6); grid.add(artistField,1, 6);
        grid.add(new Label("Status:"),0,7); grid.add(statusField,1, 7);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat en objet Artwork
        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Artwork a = (artwork != null) ? artwork : new Artwork();
                a.setTitle(titleField.getText().trim());
                try {
                    a.setCreationYear(Integer.parseInt(creationYearField.getText().trim()));
                } catch (NumberFormatException e) {
                    a.setCreationYear(0);
                }
                a.setType(typeField.getText().trim());
                a.setMedium(mediumField.getText().trim());
                a.setPrice(Double.parseDouble(priceField.getText()));
                a.setDescription(descriptionField.getText().trim());
                a.setStatus(statusField.getValue() != null ? statusField.getValue() : Artwork.Status.FOR_SALE);
                Artist artist = artistService.getArtistByName(artistField.getValue());
                if (artist == null) return null; else a.setArtist(artist);
                return a;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
        artworkTable.refresh();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
