package projetpoo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evenement {
    private int id;
    private String titre;
    private LocalDate date;
    private LocalTime heure;
    private String description;
    private String categorie;

    public Evenement(int id, String titre, LocalDate date, LocalTime heure, String description, String categorie) {
        this.id = id;
        this.titre = titre;
        this.date = date;
        this.heure = heure;
        this.description = description;
        this.categorie = categorie;
    }

    // Getters pour le TableView (Indispensables)
    public int getId() { return id; }
    public String getTitre() { return titre; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public String getDescription() { return description; }
    public String getCategorie() { return categorie; }
}