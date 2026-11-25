package course.controller;

import course.domain.Movie;
import course.domain.validators.ValidationException;
import course.service.MovieService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Form used both for adding (movie == null) and updating a movie.
 * The id is never editable: it is generated on add and kept on update.
 */
public class MovieFormController {
    @FXML private TextField titleField;
    @FXML private TextField directorField;
    @FXML private TextField yearField;
    @FXML private TextField durationField;

    private MovieService service;
    private Movie movie;          // null = add mode
    private boolean saved = false;

    public void setService(MovieService service, Movie movie) {
        this.service = service;
        this.movie = movie;
        if (movie != null) {
            // TODO 7: modul de editare: completati titleField, directorField, yearField si durationField
            //  cu datele filmului (indiciu: String.valueOf pentru numere)
            titleField.setText(movie.getTitle());
            directorField.setText(movie.getDirector());
            yearField.setText(String.valueOf(movie.getYear()));
            durationField.setText(String.valueOf(movie.getDurationMinutes()));

        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        int year, duration;
        try {
            year = Integer.parseInt(yearField.getText().trim());
            duration = Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException e) {
            Alerts.error(titleField, "Invalid movie", "Year and duration must be numbers.");
            return;
        }
        String title = titleField.getText().trim();
        String director = directorField.getText().trim();
        Movie result;
        try {
            result = movie == null
                    ? service.addMovie(title, director, year, duration)
                    : service.updateMovie(movie.getId(), title, director, year, duration);
        } catch (ValidationException e) {
            // MovieValidator separa erorile cu "; ": fiecare eroare pe un rand
            Alerts.error(titleField, "Invalid movie", String.join("\n", e.getMessage().split(";\\s*")));
            return;
        } catch (RuntimeException e) {
            Alerts.error(titleField, "Save failed", "Error while saving the movie: " + e.getMessage());
            return;
        }
        saved = true;
        Alerts.info(titleField, movie == null ? "Movie saved" : "Movie updated",
                "\"" + result.getTitle() + "\" was " + (movie == null ? "saved" : "updated")
                        + " (id " + result.getId() + ").");
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}
