package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CommunityController {
    @FXML private TextField searchField;
    @FXML private TableView<CommunityMember> memberTable;
    @FXML private TableColumn<CommunityMember, String> nameColumn;
    @FXML private TableColumn<CommunityMember, String> emailColumn;
    @FXML private TableColumn<CommunityMember, String> cityColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        memberTable.setItems(FXCollections.observableArrayList(communityService.searchMembers(query)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    private void refreshTable() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
        memberTable.refresh();
    }
}
