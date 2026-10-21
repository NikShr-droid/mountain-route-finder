package com.routefinder.ui;

import com.routefinder.model.Mountain;
import com.routefinder.model.User;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

/**
 * Presentation utility for displaying in-app modal overlays and Alpine alert dialogs.
 *
 * <p><b>In-App Overlay Architecture:</b>
 * Modals open directly over the active viewport with a dimmed alpine backdrop rather
 * than launching detached OS-level window popups. If an overlay host is not registered
 * (e.g. in standalone runs), it gracefully falls back to native modal dialogs.</p>
 *
 * <p><b>Defensive Programming:</b>
 * Includes safe fallback logging when invoked outside the JavaFX Application Thread
 * (e.g. during headless JUnit test execution), preventing unhandled UI toolkit exceptions.</p>
 */
public final class AlertHelper {

    /**
     * Interface implemented by the root container (e.g. MainView) to host in-app modal sheets.
     */
    public interface OverlayHost {
        void showOverlay(Node modalNode, Runnable onDismiss);
        void hideOverlay();
        boolean isOverlayActive();
    }

    private static OverlayHost overlayHost;

    private AlertHelper() {}

    public static void setOverlayHost(OverlayHost host) {
        overlayHost = host;
    }

    public static void hideCurrentOverlay() {
        if (overlayHost != null) {
            overlayHost.hideOverlay();
        }
    }

    /**
     * Prompts the user with an informative in-app warning overlay when an unseeded catalog
     * peak is clicked or selected for route planning.
     *
     * @param mountain the mountain peak that has no seeded waypoint data
     */
    public static void showUnseededMountainWarning(Mountain mountain) {
        String mountainName = mountain != null ? mountain.getName() : "this mountain";
        showWarningAlert(
                "Route Unavailable",
                null,
                "Route for " + mountainName + " is currently not available."
        );
    }

    /**
     * Displays an in-app Warning alert card.
     */
    public static void showWarningAlert(String title, String header, String content) {
        showInAppOrNative(AlertType.WARNING, title, header, content);
    }

    /**
     * Displays an in-app Error alert card.
     */
    public static void showErrorAlert(String title, String header, String content) {
        showInAppOrNative(AlertType.ERROR, title, header, content);
    }

    /**
     * Displays an in-app Information alert card.
     */
    public static void showInfoAlert(String title, String header, String content) {
        showInAppOrNative(AlertType.INFORMATION, title, header, content);
    }

    /**
     * Displays an in-app modal prompt for editing text (e.g. expedition climb notes).
     */
    public static void showTextInputOverlay(String title, String header, String initialValue, Consumer<String> onConfirm) {
        if (overlayHost == null || !Platform.isFxApplicationThread()) {
            return;
        }

        VBox card = new VBox(12);
        card.setMaxWidth(380);
        card.setPrefWidth(380);
        card.setPadding(new Insets(16));
        applyCardStyle(card);
        card.setOnMouseClicked(Event::consume);

        // Header Row
        HBox topRow = buildTopRow("📝", title, () -> overlayHost.hideOverlay());

        Label headerLbl = new Label(header);
        headerLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerLbl.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

        TextArea textArea = new TextArea(initialValue != null ? initialValue : "");
        textArea.setPromptText("Enter your climb notes and memories...");
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        textArea.setStyle(
                "-fx-control-inner-background: " + (ThemeManager.isDark() ? "#1e293b" : "#ffffff") + ";" +
                "-fx-text-fill: " + ThemeManager.getTextTitle() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + ThemeManager.getTextMuted() + ";" +
                "-fx-padding: 7 14;" +
                "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> overlayHost.hideOverlay());

        Button saveBtn = new Button("Save Notes");
        saveBtn.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            overlayHost.hideOverlay();
            if (onConfirm != null) {
                onConfirm.accept(textArea.getText());
            }
        });

        HBox actionsRow = new HBox(10, cancelBtn, saveBtn);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(topRow, headerLbl, textArea, actionsRow);
        overlayHost.showOverlay(card, null);
    }

    /**
     * Displays an in-app modal overlay for switching or creating climber profiles.
     */
    public static void showProfilePickerOverlay(List<User> users, User currentUser,
                                                Consumer<User> onSelectUser, Consumer<String> onAddNewUser) {
        if (overlayHost == null || !Platform.isFxApplicationThread()) {
            return;
        }

        VBox card = new VBox(12);
        card.setMaxWidth(380);
        card.setPrefWidth(380);
        card.setPadding(new Insets(16));
        applyCardStyle(card);
        card.setOnMouseClicked(Event::consume);

        HBox topRow = buildTopRow("👤", "Climber Profiles", () -> overlayHost.hideOverlay());

        Label subLbl = new Label("Select an existing climber profile or create a new expedition passport.");
        subLbl.setFont(Font.font("System", 12));
        subLbl.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
        subLbl.setWrapText(true);

        // Profile ComboBox
        Label lblSelect = new Label("Active Profile:");
        lblSelect.setFont(Font.font("System", FontWeight.BOLD, 11));
        lblSelect.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");

        ComboBox<User> userCombo = new ComboBox<>();
        userCombo.getItems().setAll(users);
        if (currentUser != null) {
            userCombo.getSelectionModel().select(currentUser);
        }
        userCombo.setMaxWidth(Double.MAX_VALUE);
        userCombo.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#ffffff") + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );

        Button switchBtn = new Button("Switch Climber");
        switchBtn.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 14;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        switchBtn.setOnAction(e -> {
            User selected = userCombo.getValue();
            if (selected != null) {
                overlayHost.hideOverlay();
                if (onSelectUser != null) {
                    onSelectUser.accept(selected);
                }
            }
        });

        HBox switchRow = new HBox(8, userCombo, switchBtn);
        HBox.setHgrow(userCombo, Priority.ALWAYS);

        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: " + ThemeManager.getBorderSubtle() + ";");

        // Create New Profile
        Label lblCreate = new Label("+ Create New Climber Profile:");
        lblCreate.setFont(Font.font("System", FontWeight.BOLD, 11));
        lblCreate.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter climber name (e.g. Alex Honnold)...");
        nameField.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#ffffff") + ";" +
                "-fx-text-fill: " + ThemeManager.getTextTitle() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 6 10;"
        );

        Button createBtn = new Button("Create");
        createBtn.setStyle(
                "-fx-background-color: #16a34a;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 14;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        createBtn.setOnAction(e -> {
            String newName = nameField.getText() != null ? nameField.getText().trim() : "";
            if (!newName.isEmpty()) {
                overlayHost.hideOverlay();
                if (onAddNewUser != null) {
                    onAddNewUser.accept(newName);
                }
            }
        });

        HBox createRow = new HBox(8, nameField, createBtn);
        HBox.setHgrow(nameField, Priority.ALWAYS);

        card.getChildren().addAll(topRow, subLbl, lblSelect, switchRow, divider, lblCreate, createRow);
        overlayHost.showOverlay(card, null);
    }

    private static void showInAppOrNative(AlertType type, String title, String header, String content) {
        // Defensive check: If not on FX Application Thread, log gracefully without throwing IllegalStateException
        if (!Platform.isFxApplicationThread()) {
            System.err.printf("[%s Alert] %s - %s: %s%n", type, title, header, content);
            return;
        }

        // If in-app overlay host is registered, render in-app modal sheet directly over the app viewport
        if (overlayHost != null) {
            VBox card = buildModalCard(type, title, header, content, () -> overlayHost.hideOverlay());
            overlayHost.showOverlay(card, null);
            return;
        }

        // Fallback: native JavaFX dialog if no overlay host is attached
        try {
            Alert alert = new Alert(type);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);

            DialogPane dialogPane = alert.getDialogPane();
            URL stylesheet = AlertHelper.class.getResource(ThemeManager.getCurrentTheme().getStylesheetPath());
            if (stylesheet != null) {
                dialogPane.getStylesheets().add(stylesheet.toExternalForm());
            }

            dialogPane.setStyle(
                    "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                    "-fx-font-family: 'System';"
            );

            alert.showAndWait();
        } catch (Exception e) {
            System.err.printf("[%s Alert Error] Failed to display dialog: %s%n", type, e.getMessage());
        }
    }

    private static VBox buildModalCard(AlertType type, String title, String header, String content, Runnable onClose) {
        VBox card = new VBox(10);
        card.setMaxWidth(340);
        card.setPrefWidth(340);
        card.setPadding(new Insets(14, 16, 14, 16));
        applyCardStyle(card);
        card.setOnMouseClicked(Event::consume);

        String iconEmoji = switch (type) {
            case ERROR -> "❌";
            case INFORMATION -> "ℹ️";
            case CONFIRMATION -> "❓";
            default -> "⚠️";
        };

        HBox topRow = buildTopRow(iconEmoji, title, onClose);
        card.getChildren().add(topRow);

        // Header Label (optional)
        if (header != null && !header.isBlank()) {
            Label headerLbl = new Label(header);
            headerLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
            headerLbl.setStyle("-fx-text-fill: " + (type == AlertType.ERROR ? "#ef4444" : ThemeManager.getIceAccent()) + ";");
            headerLbl.setWrapText(true);
            card.getChildren().add(headerLbl);
        }

        // Content Body (optional)
        if (content != null && !content.isBlank()) {
            Label contentLbl = new Label(content);
            contentLbl.setFont(Font.font("System", 12));
            contentLbl.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-line-spacing: 1px;");
            contentLbl.setWrapText(true);
            card.getChildren().add(contentLbl);
        }

        // Action Button
        Button actionBtn = new Button("OK");
        actionBtn.setFont(Font.font("System", FontWeight.BOLD, 11));
        actionBtn.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 5 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        actionBtn.setOnAction(e -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        HBox actionsRow = new HBox(actionBtn);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(4, 0, 0, 0));

        card.getChildren().add(actionsRow);
        return card;
    }

    private static HBox buildTopRow(String iconEmoji, String title, Runnable onClose) {
        Label iconBadge = new Label(iconEmoji);
        iconBadge.setFont(Font.font("System", 13));
        iconBadge.setPadding(new Insets(3, 6, 3, 6));
        iconBadge.setStyle(
                (ThemeManager.isDark()
                        ? "-fx-background-color: #1e293b; -fx-background-radius: 6;"
                        : "-fx-background-color: #f1f5f9; -fx-background-radius: 6;")
        );

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLbl.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font("System", FontWeight.BOLD, 11));
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + ThemeManager.getTextMuted() + ";" +
                "-fx-cursor: hand;" +
                "-fx-padding: 2 4;"
        );
        closeBtn.setOnAction(e -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        HBox topRow = new HBox(8, iconBadge, titleLbl, spacer, closeBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);
        return topRow;
    }

    private static void applyCardStyle(VBox card) {
        card.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                (ThemeManager.isDark()
                        ? "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.65), 24, 0, 0, 8);"
                        : "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.18), 24, 0, 0, 8);")
        );
    }
}
