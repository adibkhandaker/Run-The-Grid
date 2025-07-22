package org.example.nfldata;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LatestNewsController {

    private int currentPage = 0;
    private List<JSONObject> articlesList = new ArrayList<>();

    @FXML
    private Label headlineLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private ImageView mainImage;

    @FXML
    private Label dateLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    public void initialize() {
        fetchNews();
    }

    private void fetchNews() {
        try {
            JSONParser parser = new JSONParser();
            String newsURL = "https://site.api.espn.com/apis/site/v2/sports/football/nfl/news?limit=100";
            HttpURLConnection newsConnection = APIController.fetchAPIResponse(newsURL);
            String newsResponse = APIController.readAPIResponse(newsConnection);
            JSONObject root = (JSONObject) parser.parse(newsResponse);
            JSONArray articles = (JSONArray) root.get("articles");

            articlesList = (List<JSONObject>) articles.stream().filter(article -> {
                JSONObject articleObj = (JSONObject) article;
                JSONArray categories = (JSONArray) articleObj.get("categories");
                boolean isFantasyArticle = false;
                if (categories != null) {
                    for (Object category : categories) {
                        JSONObject categoryObj = (JSONObject) category;
                        Object descObj = categoryObj.get("description");
                        if (descObj != null) {
                            if (descObj.toString().toLowerCase().contains("fantasy")) {
                                isFantasyArticle = true;
                                break;
                            }
                        }
                    }
                }
                return !isFantasyArticle;
            }).collect(Collectors.toList());

            if (!articlesList.isEmpty()) {
                displayArticle(currentPage);
            } else {
                headlineLabel.setText("No non-fantasy news available.");
            }

        } catch (Exception e) {
            headlineLabel.setText("Error loading news.");
            descriptionLabel.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayArticle(int page) {
        if (page >= 0 && page < articlesList.size()) {
            JSONObject articleObj = articlesList.get(page);
            String date = (String) articleObj.get("published");
            date = date.substring(0, 10);
            dateLabel.setText(date);

            JSONArray images = (JSONArray) articleObj.get("images");
            if (images != null && !images.isEmpty()) {
                JSONObject imageObj = (JSONObject) images.get(0);
                String imageURL = (String) imageObj.get("url");
                Image image = new Image(imageURL);
                mainImage.setImage(image);
            } else {
                mainImage.setImage(null);
            }

            String headline = articleObj.get("headline") != null ? articleObj.get("headline").toString() : "No headline available.";
            String description = articleObj.get("description") != null ? articleObj.get("description").toString() : "No description available.";

            headlineLabel.setText(headline);
            headlineLabel.setWrapText(true);
            headlineLabel.setStyle("-fx-font-weight: bold");

            descriptionLabel.setText(description);
            descriptionLabel.setWrapText(true);

            updateButtonStates();
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        if (currentPage < articlesList.size() - 1) {
            currentPage++;
            displayArticle(currentPage);
        }
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayArticle(currentPage);
        }
    }

    private void updateButtonStates() {
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= articlesList.size() - 1);
    }

    @FXML
    private void goBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
