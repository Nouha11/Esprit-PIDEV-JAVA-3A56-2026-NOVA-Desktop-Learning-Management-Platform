package controllers.library;

import controllers.NovaDashboardController;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import models.library.Book;
import netscape.javascript.JSObject;
import services.library.GroqService;

import java.io.File;
import java.net.URL;

/**
 * PDF Viewer using PDF.js rendered inside a JavaFX WebView.
 * Supports local file paths and remote URLs.
 * Includes Grok AI assistant side panel.
 */
public class PdfViewerController {

    @FXML private WebView pdfWebView;
    @FXML private Label lblBookTitle, lblBookAuthor, lblStatus;
    @FXML private ProgressBar progressBar, readingProgress;
    @FXML private Label lblPageInfo, lblReadPercent, lblZoom;
    @FXML private VBox chatPanel, chatMessages;
    @FXML private ScrollPane chatScroll;
    @FXML private Label lblSelectedText;
    @FXML private TextArea txtUserMessage;
    @FXML private Button btnToggleChat;

    private Book book;
    private String currentSelectedText = "";
    private final GroqService grokService = new GroqService();
    private boolean chatVisible = false;
    private JavaBridge javaBridge;
    private int currentZoom = 100;

    public void initData(Book book) {
        this.book = book;
        lblBookTitle.setText(book.getTitle());
        lblBookAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "Unknown");
        System.out.println("[PdfViewer] pdf_url from DB: '" + book.getPdfUrl() + "'");
        loadPdf(book.getPdfUrl());
    }

    private void loadPdf(String pdfUrl) {
        if (pdfUrl == null || pdfUrl.isBlank()) {
            lblStatus.setText("No PDF available for this book.");
            lblStatus.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        WebEngine engine = pdfWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        progressBar.setVisible(true);
        lblStatus.setText("Loading...");

        // Resolve to a remote URL or local file path
        String resolvedUrl;
        if (pdfUrl.startsWith("http://") || pdfUrl.startsWith("https://")) {
            resolvedUrl = pdfUrl;
        } else if (pdfUrl.startsWith("uploads/") || pdfUrl.startsWith("/uploads/")) {
            // Could be a local Symfony upload OR a Render-hosted file.
            // Try local XAMPP path first, fall back to Render.
            String filename = pdfUrl.startsWith("/") ? pdfUrl.substring(1) : pdfUrl;
            // Common local Symfony public paths
            String[] localBases = {
                "C:/xampp/htdocs/Pi_web/public/",
                "C:/xampp/htdocs/projet dev/Pi_web/public/",
                System.getProperty("user.home") + "/Pi_web/public/",
            };
            String localPath = null;
            for (String base : localBases) {
                File f = new File(base + filename);
                if (f.exists()) { localPath = f.toURI().toString(); break; }
            }
            resolvedUrl = (localPath != null) ? localPath
                : "https://nova-learning-management-platform.onrender.com/" + filename;
        } else if (pdfUrl.startsWith("/")) {
            resolvedUrl = "https://nova-learning-management-platform.onrender.com" + pdfUrl;
        } else if (!pdfUrl.contains("/") && !pdfUrl.contains("\\")) {
            // Bare filename — try local first, then Render
            File localFile = new File("C:/xampp/htdocs/Pi_web/public/uploads/pdfs/" + pdfUrl);
            resolvedUrl = localFile.exists() ? localFile.toURI().toString()
                : "https://nova-learning-management-platform.onrender.com/uploads/pdfs/" + pdfUrl;
        } else {
            // Absolute local file path
            File f = new File(pdfUrl);
            if (!f.exists()) {
                lblStatus.setText("File not found: " + pdfUrl);
                lblStatus.setStyle("-fx-text-fill: #dc3545;");
                progressBar.setVisible(false);
                return;
            }
            resolvedUrl = f.toURI().toString();
        }

        final String finalResolvedUrl = resolvedUrl;

        // If it's a remote URL, download to a temp file first.
        // PDF.js inside JavaFX WebView cannot load remote HTTPS URLs due to CORS.
        if (finalResolvedUrl.startsWith("http://") || finalResolvedUrl.startsWith("https://")) {
            lblStatus.setText("Downloading PDF...");
            System.out.println("[PdfViewer] Downloading: " + finalResolvedUrl);
            Thread downloadThread = new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(finalResolvedUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15_000);
                    conn.setReadTimeout(60_000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setInstanceFollowRedirects(true);
                    int status = conn.getResponseCode();
                    System.out.println("[PdfViewer] HTTP " + status + " for " + finalResolvedUrl);
                    if (status != 200) {
                        // Try with a leading slash variant if 404
                        String altUrl = finalResolvedUrl;
                        if (status == 404 && !finalResolvedUrl.contains("/uploads/pdfs/")) {
                            altUrl = "https://nova-learning-management-platform.onrender.com/uploads/pdfs/"
                                   + finalResolvedUrl.substring(finalResolvedUrl.lastIndexOf('/') + 1);
                            System.out.println("[PdfViewer] Retrying alt URL: " + altUrl);
                        }
                        final String errMsg = "Download failed (HTTP " + status + ")\nURL: " + finalResolvedUrl;
                        javafx.application.Platform.runLater(() -> {
                            lblStatus.setText("Download failed (HTTP " + status + ")");
                            lblStatus.setStyle("-fx-text-fill: #dc3545;");
                            progressBar.setVisible(false);
                        });
                        return;
                    }
                    // Write to temp file
                    java.io.File tmp = java.io.File.createTempFile("nova_pdf_", ".pdf");
                    tmp.deleteOnExit();
                    try (java.io.InputStream in = conn.getInputStream();
                         java.io.FileOutputStream out = new java.io.FileOutputStream(tmp)) {
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                    }
                    System.out.println("[PdfViewer] Downloaded to: " + tmp.getAbsolutePath() + " (" + tmp.length() + " bytes)");
                    final String localUrl = tmp.toURI().toString();
                    javafx.application.Platform.runLater(() -> loadPdfIntoWebView(engine, localUrl));
                } catch (Exception e) {
                    System.err.println("[PdfViewer] Download error: " + e.getMessage());
                    javafx.application.Platform.runLater(() -> {
                        lblStatus.setText("Download error: " + e.getMessage());
                        lblStatus.setStyle("-fx-text-fill: #dc3545;");
                        progressBar.setVisible(false);
                    });
                }
            }, "PdfDownload");
            downloadThread.setDaemon(true);
            downloadThread.start();
        } else {
            // Already a local file:// URL
            loadPdfIntoWebView(engine, finalResolvedUrl);
        }
    }

    private void loadPdfIntoWebView(WebEngine engine, String localFileUrl) {
        final String safeUrl = localFileUrl.replace("\\", "/").replace("'", "\\'");

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                progressBar.setVisible(false);
                lblStatus.setText("Ready");

                javaBridge = new JavaBridge();
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("JavaBridge", javaBridge);

                engine.executeScript("loadPdf('" + safeUrl + "')");

                engine.executeScript(
                    "document.addEventListener('mouseup', function() {" +
                    "  var sel = window.getSelection().toString().trim();" +
                    "  if (sel.length > 0 && window.JavaBridge) window.JavaBridge.onTextSelected(sel);" +
                    "});"
                );
            }
        });

        URL viewerUrl = getClass().getResource("/pdf_viewer.html");
        if (viewerUrl == null) {
            lblStatus.setText("PDF viewer HTML not found.");
            lblStatus.setStyle("-fx-text-fill: #dc3545;");
            return;
        }
        engine.load(viewerUrl.toExternalForm());
    }

    // ── Java Bridge ───────────────────────────────────────────────────────────

    public class JavaBridge {
        public void onPdfLoaded(int totalPages) {
            Platform.runLater(() -> {
                lblPageInfo.setText("Page 1 of " + totalPages);
                lblStatus.setText("Ready");
            });
        }

        public void onPageChanged(int current, int total, int pct) {
            Platform.runLater(() -> updateProgress(current, total, pct));
        }

        public void onTextSelected(String text) {
            Platform.runLater(() -> {
                currentSelectedText = text;
                String preview = text.length() > 120 ? text.substring(0, 120) + "..." : text;
                lblSelectedText.setText(preview);
                lblSelectedText.setStyle("-fx-font-size: 12; -fx-text-fill: #e2e8f0; -fx-font-style: italic; -fx-padding: 4 0 0 0;");
                if (!chatVisible) showChatPanel(true);
            });
        }
    }

    // ── Reading Progress ──────────────────────────────────────────────────────

    private void updateProgress(int current, int total, int pct) {
        readingProgress.setProgress(pct / 100.0);
        lblReadPercent.setText(pct + "%");
        lblPageInfo.setText("Page " + current + " of " + total);
        String color = pct < 33 ? "#0d6efd" : pct < 66 ? "#f59e0b" : "#10b981";
        readingProgress.setStyle("-fx-accent: " + color + ";");
        lblReadPercent.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
    }

    // ── Chat Panel ────────────────────────────────────────────────────────────

    @FXML private void handleToggleChat() { showChatPanel(!chatVisible); }

    private void showChatPanel(boolean show) {
        chatVisible = show;
        chatPanel.setVisible(show);
        chatPanel.setManaged(show);
    }

    @FXML
    private void handleExplain() {
        if (currentSelectedText.isBlank()) { addSystemMessage("Select text from the PDF first."); return; }
        sendToGrok("Explain this passage: \"" + currentSelectedText + "\"", currentSelectedText);
    }

    @FXML
    private void handleSend() {
        String msg = txtUserMessage.getText().trim();
        if (msg.isBlank()) return;
        txtUserMessage.clear();
        sendToGrok(msg, currentSelectedText);
    }

    @FXML
    private void handleClearChat() {
        chatMessages.getChildren().clear();
        currentSelectedText = "";
        lblSelectedText.setText("Select text in the PDF...");
        lblSelectedText.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 4 0 0 0;");
    }

    private void sendToGrok(String userMessage, String selectedText) {
        addUserBubble(userMessage);
        Label aiLabel = new Label("");
        aiLabel.setWrapText(true);
        aiLabel.setMaxWidth(280);
        aiLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #e2e8f0;");
        HBox aiBubble = new HBox(aiLabel);
        aiBubble.setStyle("-fx-background-color: #252535; -fx-background-radius: 10; -fx-padding: 10 12;");
        HBox aiRow = new HBox(aiBubble);
        aiRow.setAlignment(Pos.CENTER_LEFT);
        chatMessages.getChildren().add(aiRow);
        scrollToBottom();
        txtUserMessage.setDisable(true);

        StringBuilder full = new StringBuilder();
        grokService.explainAsync(selectedText, userMessage,
            token -> Platform.runLater(() -> { full.append(token); aiLabel.setText(full.toString()); scrollToBottom(); }),
            () -> Platform.runLater(() -> txtUserMessage.setDisable(false)),
            error -> Platform.runLater(() -> { aiLabel.setText("Error: " + error); aiLabel.setStyle("-fx-text-fill: #f87171;"); txtUserMessage.setDisable(false); })
        );
    }

    private void addUserBubble(String text) {
        Label lbl = new Label(text);
        lbl.setWrapText(true); lbl.setMaxWidth(260);
        lbl.setStyle("-fx-font-size: 13; -fx-text-fill: white;");
        HBox bubble = new HBox(lbl);
        bubble.setStyle("-fx-background-color: #0d6efd; -fx-background-radius: 10; -fx-padding: 10 12;");
        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        chatMessages.getChildren().add(row);
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b; -fx-font-style: italic;");
        HBox row = new HBox(lbl);
        row.setAlignment(Pos.CENTER);
        chatMessages.getChildren().add(row);
        scrollToBottom();
    }

    private void scrollToBottom() { chatScroll.setVvalue(1.0); }

    @FXML private void handleBack() { NovaDashboardController.loadPage("/views/library/MyLibrary.fxml"); }
    @FXML private void handleReload() { if (book != null) loadPdf(book.getPdfUrl()); }

    @FXML private void handlePrev() {
        try { pdfWebView.getEngine().executeScript("prevPage()"); } catch (Exception ignored) {}
    }
    @FXML private void handleNext() {
        try { pdfWebView.getEngine().executeScript("nextPage()"); } catch (Exception ignored) {}
    }
    @FXML private void handleZoomIn() {
        currentZoom = Math.min(currentZoom + 20, 300);
        if (lblZoom != null) lblZoom.setText(currentZoom + "%");
        try { pdfWebView.getEngine().executeScript("setZoom(" + currentZoom + ")"); } catch (Exception ignored) {}
    }
    @FXML private void handleZoomOut() {
        currentZoom = Math.max(currentZoom - 20, 40);
        if (lblZoom != null) lblZoom.setText(currentZoom + "%");
        try { pdfWebView.getEngine().executeScript("setZoom(" + currentZoom + ")"); } catch (Exception ignored) {}
    }
}
