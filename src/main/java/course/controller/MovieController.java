package course.controller;

import course.domain.Movie;
import course.domain.MovieSortField;
import course.lib.Page;
import course.lib.PageRequest;
import course.lib.Sort;
import course.lib.SortDirection;
import course.service.MovieService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class MovieController {
    private static final int PAGE_SIZE = 3;

    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, Long> idColumn;
    @FXML private TableColumn<Movie, String> titleColumn;
    @FXML private TableColumn<Movie, String> directorColumn;
    @FXML private TableColumn<Movie, Integer> yearColumn;
    @FXML private TableColumn<Movie, Integer> durationColumn;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private ComboBox<MovieSortField> sortFieldBox;
    @FXML private ComboBox<SortDirection> sortDirectionBox;

    private MovieService service;

    private PageRequest pageRequest = new PageRequest(1, PAGE_SIZE);
    private Page page = new Page(List.of(), 1, pageRequest);

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        directorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        var noSelection = movieTable.getSelectionModel().selectedItemProperty().isNull();
        // TODO 5: legati (bind) disableProperty() a butoanelor updateButton si deleteButton de noSelection
        //  (butoanele sunt inactive cat timp niciun film nu este selectat)
        deleteButton.disableProperty().bind(noSelection);
        updateButton.disableProperty().bind(noSelection);
        movieTable.setOnKeyPressed(x -> {
            if(x.getCode()== KeyCode.ESCAPE) {
                movieTable.getSelectionModel().clearSelection();
            }
        });

        sortFieldBox.getItems().setAll(MovieSortField.values());
        sortFieldBox.setValue(MovieSortField.ID);
        sortDirectionBox.getItems().setAll(SortDirection.values());
        sortDirectionBox.setValue(SortDirection.ASC);
    }

    public void setService(MovieService service) {
        this.service = service;
        loadPage();
    }

    @FXML
    private void onSortChanged() {
        if (service == null) return;   // fired while initializing the combo boxes
        // TODO 4: sortarea s-a schimbat: reveniti la prima pagina si reincarcati tabelul
        pageRequest = new PageRequest(1, PAGE_SIZE);
        loadPage();
    }

    @FXML
    private void onPrevious() {
        // TODO 1: treceti la pagina anterioara (actualizati pageNumber, apoi reincarcati tabelul cu loadPage())
        pageRequest = pageRequest.prev();
        loadPage();
    }

    @FXML
    private void onNext() {
        // TODO 2: treceti la pagina urmatoare (actualizati pageNumber, apoi reincarcati tabelul cu loadPage())
        pageRequest = pageRequest.next();
        loadPage();
    }

    @FXML
    private void onAdd() {
        openForm(null, "Add movie");
    }

    @FXML
    private void onUpdate() {
        // TODO 6: luati filmul selectat din movieTable si, daca exista, deschideti formularul cu el
        //  (vezi onAdd() si openForm())
        Movie movie = movieTable.getSelectionModel().getSelectedItem();
        openForm(movie,"editmovie");
    }

    @FXML
    private void onDelete() {
        Movie selected = movieTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (!Alerts.confirm(movieTable, "Delete movie",
                "Delete \"" + selected.getTitle() + "\" (id " + selected.getId() + ")?"))
            return;
        try {
            service.deleteMovie(selected.getId());
        } catch (RuntimeException e) {
            Alerts.error(movieTable, "Delete failed", "Error while deleting: " + e.getMessage());
            return;
        }
        Alerts.info(movieTable, "Movie deleted", "\"" + selected.getTitle() + "\" was deleted.");
        loadPage();
    }

    private void openForm(Movie movie, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/course/movie-form-view.fxml"));
            Stage dialog = new Stage();
            dialog.setScene(new Scene(loader.load()));
            MovieFormController controller = loader.getController();
            controller.setService(service, movie);
            Stage owner = (Stage) movieTable.getScene().getWindow();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.getIcons().setAll(owner.getIcons());
            dialog.setTitle(title);
            dialog.setResizable(false);
            dialog.showAndWait();
            if (controller.isSaved()) {
                loadPage();
                movieTable.refresh();
            }
        } catch (IOException e) {
            Alerts.error(movieTable, "Error", "Cannot open the form: " + e.getMessage());
        }
    }

    private Page<Movie> fetchPage() {
        return service.findOnPage(pageRequest, new Sort<>(sortFieldBox.getValue(), sortDirectionBox.getValue()));
    }

    private void loadPage() {
        try {
            page = fetchPage();
            if (page.number() > page.totalPages()) {   // e.g. the last movie of the last page was deleted
                pageRequest = new PageRequest(page.totalPages(), PAGE_SIZE);
                page = fetchPage();
            }
            movieTable.setItems(FXCollections.observableArrayList(page.items()));
        } catch (RuntimeException e) {
            Alerts.error(movieTable, "Database error", "Error while loading movies: " + e.getMessage());
        }
        // TODO 3: afisati "Page x / y" in pageLabel (pageNumber incepe de la 1, ca in PageRequest),
        //  dezactivati previousButton pe prima pagina si nextButton pe ultima pagina
        pageLabel.setText("Page " + page.number() + "/" + page.totalPages());

        previousButton.setDisable(page.isFirst());
        nextButton.setDisable(page.isLast());
    }
}
