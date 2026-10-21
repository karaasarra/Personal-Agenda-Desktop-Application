package projetpoo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class AgendaApp extends Application {

    private TableView<Evenement> table = new TableView<>();
    private ObservableList<Evenement> listeEvenements = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Agenda Personnel Complet");

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #f0f4f8;");
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);

        // --- 1. TITRE DE PAGE ---
        Label lbTitrePrincipal = new Label("MON AGENDA PERSONNEL");
        lbTitrePrincipal.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbTitrePrincipal.setTextFill(Color.web("#283593"));

        // --- 2. FORMULAIRE DE SAISIE (GridPane - Cours Slide 50) ---
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #cfd8dc;");

        TextField txtTitre = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField txtHeure = new TextField("10:00");
        TextArea txtDesc = new TextArea();
        txtDesc.setPrefRowCount(2);
        
        ComboBox<String> comboCat = new ComboBox<>(FXCollections.observableArrayList("Travail", "Personnel", "Loisir", "Études", "Famille"));
        comboCat.setValue("Travail");

        Button btnAjouter = new Button("➕ Enregistrer");
        btnAjouter.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-weight: bold;");

        grid.add(new Label("Titre :"), 0, 0);       grid.add(txtTitre, 1, 0);
        grid.add(new Label("Date :"), 0, 1);        grid.add(datePicker, 1, 1);
        grid.add(new Label("Heure (HH:MM):"), 0, 2); grid.add(txtHeure, 1, 2);
        grid.add(new Label("Description :"), 0, 3); grid.add(txtDesc, 1, 3);
        grid.add(new Label("Catégorie :"), 0, 4);   grid.add(comboCat, 1, 4);
        grid.add(btnAjouter, 1, 5);

        // --- 3. RECHERCHE MULTICRITÈRES (Titre, Date, Catégorie) ---
        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Chercher par titre, catégorie ou date...");
        txtSearch.setPrefWidth(400);
        Button btnSearch = new Button("🔍 Chercher ");
        Button btnReset = new Button("🔄 Afficher tout");

        HBox searchBox = new HBox(10, new Label("Recherche :"), txtSearch, btnSearch, btnReset);
        searchBox.setAlignment(Pos.CENTER);

        // --- 4. TABLEAU DE VISUALISATION (Toutes les colonnes demandées) ---
        TableColumn<Evenement, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));

        TableColumn<Evenement, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // AJOUT DE L'HEURE
        TableColumn<Evenement, String> colHeure = new TableColumn<>("Heure");
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));

        TableColumn<Evenement, String> colCat = new TableColumn<>("Catégorie");
        colCat.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        // AJOUT DE LA DESCRIPTION
        TableColumn<Evenement, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(colTitre, colDate, colHeure, colCat, colDesc);
        table.setItems(listeEvenements);
        table.setPrefHeight(300);
        // On force les colonnes à remplir la largeur (Cours Slide 50)
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // BOUTON SUPPRIMER
        Button btnSupprimer = new Button("🗑️ Supprimer la sélection");
        btnSupprimer.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- 5. ACTIONS (Cours Slide 48) ---

        btnAjouter.setOnAction(e -> {
            if(txtTitre.getText().isEmpty()) return;
            ajouterBD(txtTitre.getText(), Date.valueOf(datePicker.getValue()), 
                      Time.valueOf(txtHeure.getText()+":00"), txtDesc.getText(), comboCat.getValue());
            chargerDonnees("");
            txtTitre.clear(); txtDesc.clear();
        });

        btnSearch.setOnAction(e -> chargerDonnees(txtSearch.getText()));
        btnReset.setOnAction(e -> { txtSearch.clear(); chargerDonnees(""); });

        btnSupprimer.setOnAction(e -> {
            Evenement sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                supprimerBD(sel.getId());
                chargerDonnees("");
            }
        });

        // ASSEMBLAGE
        root.getChildren().addAll(lbTitrePrincipal, grid, searchBox, table, btnSupprimer);
        chargerDonnees(""); // Chargement initial

        Scene scene = new Scene(root, 900, 850);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- MÉTHODES JDBC (Cours 9.2 & 9.4) ---

    private void ajouterBD(String t, Date d, Time h, String desc, String c) {
        String sql = "INSERT INTO evenements (titre, date_ev, heure_ev, description, categorie) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t); ps.setDate(2, d); ps.setTime(3, h); ps.setString(4, desc); ps.setString(5, c);
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void supprimerBD(int id) {
        String sql = "DELETE FROM evenements WHERE id = ?";
        try (Connection con = ConnexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void chargerDonnees(String f) {
        listeEvenements.clear();
        String sql = "SELECT * FROM evenements WHERE titre LIKE ? OR categorie LIKE ? OR date_ev LIKE ?";
        try (Connection con = ConnexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String filtre = "%" + f + "%";
            ps.setString(1, filtre); ps.setString(2, filtre); ps.setString(3, filtre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listeEvenements.add(new Evenement(
                    rs.getInt("id"), 
                    rs.getString("titre"), 
                    rs.getDate("date_ev").toLocalDate(), 
                    rs.getTime("heure_ev").toLocalTime(),
                    rs.getString("description"), 
                    rs.getString("categorie")
                ));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) { launch(args); }
}