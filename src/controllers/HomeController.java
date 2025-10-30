package controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;           // đúng lớp Parent của JavaFX
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class HomeController {

    // ====== FXML refs (đúng với home.fxml) ======
    @FXML private VBox sidebar;        // <VBox fx:id="sidebar" ...>
    @FXML private ImageView imgToggle; // <ImageView fx:id="imgToggle" ... onMouseClicked="#toggleSidebar">
    @FXML private ScrollPane scroll;   // <ScrollPane fx:id="scroll" ...>

    // ====== State ======
    private boolean collapsed = false;
    private final double expandedWidth  = 260;
    private final double collapsedWidth = 72;

    private final List<Button> menuButtons = new ArrayList<>();
    private final Map<String, Parent> viewCache = new HashMap<>();

    // ====== INIT ======
    @FXML
    private void initialize() {
        System.out.println("[HomeController] initialize()");
        if (imgToggle != null) {
            imgToggle.setStyle("-fx-cursor: hand;");
        }

        if (sidebar != null) {
            collectMenuButtons(sidebar);
        }

        // Bind content fillWidth nếu content là VBox
        if (scroll != null && scroll.getContent() instanceof VBox) {
            VBox content = (VBox) scroll.getContent();
            content.setFillWidth(true);
            content.prefWidthProperty().bind(scroll.widthProperty().subtract(24)); // padding 2 bên
        }

        
    }

    private void collectMenuButtons(VBox box) {
        for (Node n : box.getChildren()) {
            if (n instanceof Button b && b.getStyleClass().contains("nav-button")) {
                menuButtons.add(b);
            } else if (n instanceof VBox inner) {
                collectMenuButtons(inner);
            }
        }
    }


    // ====== ĐIỀU HƯỚNG ======
    @FXML
    private void handleNav(ActionEvent e) {
        if (!(e.getSource() instanceof Button b)) return;
        Object ud = b.getUserData();
        if (ud == null) return;

        String fxmlPath = ud.toString();
        if (loadViewIntoCenter(fxmlPath)) {
            highlightActiveButton(b);
        }
    }

    private boolean loadViewIntoCenter(String fxmlPath) {
        if (scroll == null) return false;
        try {
            Parent view = viewCache.get(fxmlPath);
            if (view == null) {
                URL url = getClass().getResource(fxmlPath);
                if (url == null) {
                    System.err.println("Không tìm thấy FXML: " + fxmlPath + " (đặt file vào src" + fxmlPath + ")");
                    return false;
                }
                view = FXMLLoader.load(url);
                viewCache.put(fxmlPath, view);
            }
            scroll.setContent(view);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void highlightActiveButton(Button active) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("active");
        }
        if (!active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    private void highlightActiveByRoute(String fxmlPath) {
        for (Button btn : menuButtons) {
            Object ud = btn.getUserData();
            if (ud != null && fxmlPath.equals(ud.toString())) {
                highlightActiveButton(btn);
                break;
            }
        }
    }
}
