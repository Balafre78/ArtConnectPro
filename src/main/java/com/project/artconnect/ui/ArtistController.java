package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;

public class ArtistController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist> artistTable;
    @FXML private TableColumn<Artist, String> nameColumn;
    @FXML private TableColumn<Artist, String> cityColumn;
    @FXML private TableColumn<Artist, String> emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    // ── Recherche ────────────────────────────────────────────────────────────

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        Dialog<Artist> dialog = buildArtistDialog("Add Artist", null);
        Optional<Artist> result = dialog.showAndWait();
        result.ifPresent(artist -> {
            artistService.createArtist(artist);
            refreshTable();
        });
    }

    @FXML
    private void handleEdit() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select an artist to edit.");
            return;
        }
        Dialog<Artist> dialog = buildArtistDialog("Edit Artist", selected);
        Optional<Artist> result = dialog.showAndWait();
        result.ifPresent(artist -> {
            artistService.updateArtist(artist);
            refreshTable();
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select an artist to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete artist \"" + selected.getName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                artistService.deleteArtist(selected.getName());
                refreshTable();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Construit un Dialog avec un formulaire pour créer ou modifier un artiste.
     * Si artist != null, les champs sont pré-remplis (mode Edit).
     */
    private Dialog<Artist> buildArtistDialog(String title, Artist artist) {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField      = new TextField();
        TextField bioField       = new TextField();
        TextField birthYearField = new TextField();
        TextField emailField     = new TextField();
        TextField phoneField     = new TextField();
        TextField cityField      = new TextField();

        nameField.setPromptText("Name");
        bioField.setPromptText("Bio");
        birthYearField.setPromptText("Birth Year (ex: 1990)");
        emailField.setPromptText("Email");
        phoneField.setPromptText("Phone");
        cityField.setPromptText("City");

        // Pré-remplir si mode Edit
        if (artist != null) {
            nameField.setText(artist.getName());
            nameField.setDisable(true); // le nom est la clé primaire, on ne le change pas
            bioField.setText(artist.getBio() != null ? artist.getBio() : "");
            birthYearField.setText(artist.getBirthYear() != null ? String.valueOf(artist.getBirthYear()) : "");
            emailField.setText(artist.getContactEmail() != null ? artist.getContactEmail() : "");
            phoneField.setText(artist.getPhone() != null ? artist.getPhone() : "");
            cityField.setText(artist.getCity() != null ? artist.getCity() : "");
        }

        grid.add(new Label("Name:"),       0, 0); grid.add(nameField,      1, 0);
        grid.add(new Label("Bio:"),        0, 1); grid.add(bioField,       1, 1);
        grid.add(new Label("Birth Year:"), 0, 2); grid.add(birthYearField, 1, 2);
        grid.add(new Label("Email:"),      0, 3); grid.add(emailField,     1, 3);
        grid.add(new Label("Phone:"),      0, 4); grid.add(phoneField,     1, 4);
        grid.add(new Label("City:"),       0, 5); grid.add(cityField,      1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat en objet Artist
        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Artist a = (artist != null) ? artist : new Artist();
                a.setName(nameField.getText().trim());
                a.setBio(bioField.getText().trim());
                try {
                    a.setBirthYear(Integer.parseInt(birthYearField.getText().trim()));
                } catch (NumberFormatException e) {
                    a.setBirthYear(null);
                }
                a.setContactEmail(emailField.getText().trim());
                a.setPhone(phoneField.getText().trim());
                a.setCity(cityField.getText().trim());
                a.setActive(true);
                return a;
            }
            return null;
        });

        return dialog;
    }
}