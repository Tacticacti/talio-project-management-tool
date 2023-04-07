package client.scenes;

import client.utils.CustomizationUtils;
import commons.Board;
import commons.BoardList;
import commons.Card;
import jakarta.ws.rs.WebApplicationException;
import javafx.animation.ScaleTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static client.scenes.SingleBoardCtrl.BoardID;

public class CardCtrl {
    private final SingleBoardCtrl singleBoardCtrl;
    private EventTarget target;

    public CardCtrl(SingleBoardCtrl singleBoardCtrl) {
        this.singleBoardCtrl = singleBoardCtrl;
    }

    public void placeCard(VBox parent, Card card) {
        String cardTitle = card.getTitle();
        FXMLLoader fxmlLoader = new FXMLLoader(
                singleBoardCtrl.getClass().getResource("cardGUI.fxml"));
        try {
            Node cardNode = fxmlLoader.load();
            cardNode.setId(UUID.randomUUID().toString());
            Border border = new Border(new BorderStroke(Paint.valueOf("black")
                    , BorderStrokeStyle.DASHED
                    , new CornerRadii(10), BorderWidths.DEFAULT));
            ((AnchorPane) cardNode).setBorder(border);
            if (card.getDescription() == null || card.getDescription().equals("")) {
                cardNode.lookup("#descIndicator").setVisible(false);
            }
            if (card.getSubtasks().size() > 0) {
                ((Label) cardNode.lookup("#progress"))
                        .setText(card.getCompletedSubs() + "/"
                                + card.getSubtasks().size());
            }
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), cardNode);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            cardNode.setOnMouseEntered(event -> {
//                System.out.println("Mouse Entered: " + target);
                target = cardNode;
                cardNode.requestFocus();
                scaleTransition.setNode(cardNode);
                scaleTransition.play();
            });
            cardNode.setOnMouseExited(event -> {
//                System.out.println("Mouse Exited: " + target);
                target = null;
                scaleTransition.stop();
                cardNode.setScaleX(1);
                cardNode.setScaleY(1);
            });
            cardNode.setOnKeyPressed((KeyEvent event) -> {
//                System.out.println("On Key Pressed: " + target);
                scaleTransition.setNode(cardNode);
                scaleTransition.play();
                setShortcuts(scaleTransition, event);
            });
            cardNode.setOnMouseClicked(event -> {
                cardNode.requestFocus();
                if(event.getClickCount()==2){
                    setCardDetail(scaleTransition);
                }
            });
            Button detail = (Button) cardNode.lookup("#details");
            detail.setOnMouseEntered(event -> {
                DropShadow shadow = new DropShadow();
                shadow.setRadius(30.0);
                shadow.setBlurType(BlurType.GAUSSIAN);
                shadow.setColor(Color.BLACK);
                detail.setEffect(shadow);
                detail.setStyle(
                        "-fx-cursor:hand;" +
                                " -fx-background-color: transparent");
            });
            detail.setOnMouseExited(event -> {
                detail.setEffect(null);
                detail.setStyle("-fx-cursor:pointer; " +
                        "-fx-background-color: transparent");
            });
            Label title = (Label) cardNode.lookup("#taskTitle");
            detail.setOnAction(event -> setCardDetail(scaleTransition));
            title.setText(cardTitle);
            singleBoardCtrl.nodeCardMap.put(cardNode, card);
            setDragAndDrop(parent, cardNode);
            int index = parent.getChildren().size() - 1;
            if (parent.getChildren().size() == 1) {
                index = 0;
            }

            System.out.println("updating!");
            System.out.println(CustomizationUtils.customizationData);


            CustomizationUtils.updateTextColor(cardNode, BoardID);
            CustomizationUtils.updateCardColour(cardNode, BoardID);

            parent.getChildren().add(index, cardNode);
            //cardNode.getScene();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void setShortcuts(ScaleTransition scaleTransition, KeyEvent event) {
        if (target instanceof AnchorPane) {
            switch (event.getCode()) {
                case E: editTaskTitleShortcut();
                break;
                case BACK_SPACE:
                case DELETE:
                    deleteCardShortcut();
                    break;
                case ENTER:setCardDetail(scaleTransition);
                break;
                case UP: moveUpShortcut(scaleTransition, event);
                break;
                case DOWN: moveDownShortcut(scaleTransition, event);
                break;
                case LEFT: moveLeftShortcut(scaleTransition);
                break;
                case RIGHT: moveRightShortcut(scaleTransition);
                break;
            }
            MainCtrl mainCtrl = singleBoardCtrl.getMainCtrl();
            if (target != null && target instanceof AnchorPane) {
                ((AnchorPane) target).requestFocus();
                scaleTransition.setNode((AnchorPane) target);
                scaleTransition.play();
            }
        }
    }

    private void moveRightShortcut(ScaleTransition scaleTransition) {
        if (target == null) {
            return;
        }
        AnchorPane cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        ObservableList<Node> boardLists = singleBoardCtrl.getHbox_lists().getChildren();
        int listIndex = boardLists.indexOf(parent.getParent());
        if (listIndex < boardLists.size() - 2) {
            AnchorPane rightList = (AnchorPane) boardLists.get(listIndex + 1);
            setHighlightedCard(scaleTransition, cardNode, boardLists, listIndex, rightList);
        }
    }

    private void moveLeftShortcut(ScaleTransition scaleTransition) {
        if (target == null) {
            return;
        }
        AnchorPane cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        ObservableList<Node> boardLists = singleBoardCtrl.getHbox_lists().getChildren();
        int listIndex = boardLists.indexOf(parent.getParent());
        if (listIndex > 0) {
            AnchorPane leftList = (AnchorPane) boardLists.get(listIndex - 1);
            setHighlightedCard(scaleTransition, cardNode, boardLists, listIndex, leftList);
        }
    }

    private void setHighlightedCard(ScaleTransition scaleTransition, AnchorPane cardNode,
                                    ObservableList<Node> boardLists, int listIndex,
                                    AnchorPane list) {
        AnchorPane currentList = (AnchorPane) boardLists.get(listIndex);
        int size = list.getChildren().size();
        VBox listVbox = (VBox) list.getChildren().get(size - 1);
        VBox currentVbox = (VBox) currentList.getChildren().get(size - 1);
        int cardIndex = currentVbox.getChildren().indexOf(cardNode);
        AnchorPane highlightCard = (AnchorPane) target;
        if (listVbox.getChildren().size() - 2 >= cardIndex) {
            highlightCard = (AnchorPane) listVbox.getChildren().get(cardIndex);
        } else if (listVbox.getChildren().size() - 2 >= cardIndex - 1 && cardIndex > 0) {
            highlightCard = (AnchorPane) listVbox.getChildren().get(cardIndex - 1);
        } else if (listVbox.getChildren().size() > 1) {
            highlightCard = (AnchorPane) listVbox.getChildren().get(0);
        }
        scaleTransition.stop();
        cardNode.setScaleY(1);
        cardNode.setScaleX(1);
        target = highlightCard;
    }

    private void moveDownShortcut(ScaleTransition scaleTransition, KeyEvent event) {
        if (target == null) {
            return;
        }
        AnchorPane cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        ObservableList<Node> children = parent.getChildren();
        int index = children.indexOf(cardNode);
        if (event.isShiftDown()) {
            Map<Node, Card> nodeCardMap = singleBoardCtrl.getNodeCardMap();
            Card card = nodeCardMap.get(cardNode);
            BoardList boardList = (BoardList) parent.getUserData();
            if (index < boardList.getCards().size() - 1) {
                scaleTransition.stop();
                cardNode.setScaleY(1);
                cardNode.setScaleX(1);
                Node temp = children.remove(index);
                singleBoardCtrl.deleteCardFromList(singleBoardCtrl.BoardID, boardList.getId(),
                        card);
                children.add(index + 1, cardNode);
                addCardAtIndex(boardList.getId(), index + 1, card);
                target = temp;
            }
        } else if (index < children.size()-2) {
            Node node = children.get(index+1);
            scaleTransition.stop();
            cardNode.setScaleY(1);
            cardNode.setScaleX(1);
            node.requestFocus();
            target = node;
        }
    }

    private void moveUpShortcut(ScaleTransition scaleTransition, KeyEvent event) {
        if (target == null) {
            return;
        }
        AnchorPane cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        ObservableList<Node> children = parent.getChildren();
        int index = children.indexOf(cardNode);
        if (event.isShiftDown()) {
            Map<Node, Card> nodeCardMap = singleBoardCtrl.getNodeCardMap();
            Card card = nodeCardMap.get(cardNode);
            BoardList boardList = (BoardList) parent.getUserData();
            if (index > 0) {
                scaleTransition.stop();
                cardNode.setScaleY(1);
                cardNode.setScaleX(1);
                Node temp = children.remove(index);
                singleBoardCtrl.deleteCardFromList(singleBoardCtrl.BoardID, boardList.getId(),
                        card);
                children.add(index - 1, cardNode);
                addCardAtIndex(boardList.getId(), index - 1, card);
                target = temp;
            }
        } else {
            if (index > 0) {
                Node node = children.get(index-1);
                scaleTransition.stop();
                cardNode.setScaleY(1);
                cardNode.setScaleX(1);
                node.requestFocus();
                target = node;
            }
        }
    }

    private void deleteCardShortcut() {
        if (target == null) {
            return;
        }
        AnchorPane cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Delete Task");
        alert.setContentText("Are you sure you want to delete this task? (Irreversible)");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Map<Node, Card> nodeCardMap = singleBoardCtrl.getNodeCardMap();
                Card deleteCard = nodeCardMap.remove(cardNode);
                BoardList boardList = (BoardList) parent.getUserData();
                singleBoardCtrl.deleteCardFromList(singleBoardCtrl.BoardID,
                        boardList.getId(), deleteCard);
                target = null;
            }
        });
    }

    private void editTaskTitleShortcut() {
        if (target == null) {
            return;
        }
        Node cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        Optional<String> newTitle = showTitleDialog();
        Map<Node, Card> nodeCardMap = singleBoardCtrl.getNodeCardMap();
        Card card = nodeCardMap.get(cardNode);
        if (card != null && newTitle.isPresent()) {
            card.setTitle(newTitle.get());
            BoardList boardList = (BoardList) parent.getUserData();
            Board current_board = singleBoardCtrl.getCurrent_board();
            singleBoardCtrl.updateCardFromList(current_board.getId(), boardList.getId(),
                    card);
            target = cardNode;
        }
    }

    public void setCardDetail(ScaleTransition scaleTransition) {
        if (target == null) {
            return;
        }
        AnchorPane cardNode = (AnchorPane) target;
        VBox parent = (VBox) cardNode.getParent();
        if (!singleBoardCtrl.isUnlocked) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Read-only Mode");
            alert.setHeaderText(null);
            alert.setContentText("You cannot edit the content of the card in Read-only Mode.");
            alert.showAndWait();
        }
        BoardList boardList = (BoardList) parent.getUserData();
        long listId = boardList.getId();
        Card card = singleBoardCtrl.server.getCardById(
                singleBoardCtrl.nodeCardMap.get(cardNode).getId());
        FXMLLoader fxmlLoader = new FXMLLoader(
                singleBoardCtrl.getClass().getResource("AddCard.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // sets up done card button
        Button doneButton = (Button) root.lookup("#doneTaskButton");
        doneButton.setOnAction(event -> {
            if (singleBoardCtrl.checkReadOnlyMode(singleBoardCtrl.isUnlocked)) {
                return;
            }
            setDone(listId, card, event);
        });
        //sets up delete card button
        Button deleteButton = (Button) root.lookup("#deleteTaskButton");
        deleteButton.setOnAction(event -> {
            if (singleBoardCtrl.checkReadOnlyMode(singleBoardCtrl.isUnlocked)) {
                return;
            }
            setDelete(event, cardNode, card, listId);
        });
        //sets up cancel card button
        Button cancelButton = (Button) root.lookup("#cancelTaskButton");
        cancelButton.setOnAction(event -> {
            setCancel(event, cardNode);
        });
        //sets up card title
        TextField title = (TextField) root.lookup("#taskTitle");
        title.setText(card.getTitle());
        //sets up card description
        TextArea description = (TextArea) root.lookup("#taskDescription");
        description.setText(card.getDescription());
        // sets up sub-task operations
        Button addSubTask = (Button) root.lookup("#addSubtaskButton");
        AddCardCtrl addCardCtrl = fxmlLoader.getController();
        addCardCtrl.setCard(card);
        addCardCtrl.setButton(doneButton);
        addSubTask.setOnAction(event -> addCardCtrl.addSubTask(card));
        if (card.getSubtasks() != null) {
            for (String str : card.getSubtasks()) {
                if (card.getCompletedTasks().contains(str)) {
                    addCardCtrl.displayCompletedSubs(str, card);
                } else {
                    addCardCtrl.displaySubs(str, card);
                }
            }
        }
        MainCtrl mainCtrl = singleBoardCtrl.getMainCtrl();
        Stage popUpStage = new Stage();
        root.setOnKeyPressed(event -> {
            mainCtrl.showHelpPage(event);
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                popUpStage.close();
                cardNode.requestFocus();
                scaleTransition.setNode(cardNode);
                scaleTransition.play();
            }
        });
        Scene scene = new Scene(root);
        popUpStage.setTitle("Card Details");
        popUpStage.setScene(scene);
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.showAndWait();
        singleBoardCtrl.refresh();
    }

    void setDone(long listId, Card current, ActionEvent event) {
        Button source = (Button) event.getSource();
        AnchorPane ap = (AnchorPane) source.getParent();
        TextField title = (TextField) ap.lookup("#taskTitle");
        if (!title.getText().trim().isEmpty()) {
            current.setTitle(title.getText());
        }
        TextArea desc = (TextArea) ap.lookup("#taskDescription");
        current.setDescription(desc.getText());
        VBox subs = (VBox) ap.lookup("#subtaskVbox");
        for (int i = 0; i < subs.getChildren().size(); i++) {
            Node hb = subs.getChildren().get(i);
            if (hb instanceof TextField) {
                TextField subtask = (TextField) hb;
                if (!current.getSubtasks().contains(subtask.getText())
                        && !subtask.getText().equals(""))
                    current.addSubTask(subtask.getText());
            } else {
                CheckBox cb = (CheckBox) ((HBox) hb).getChildren().get(0);
                System.out.println("Text of cur subtask processed: " + cb.getText());
                if (!current.getSubtasks().contains(cb.getText()))
                    current.addSubTask(cb.getText());

                if(cb.isSelected()){
                    current.completeSubTask(cb.getText());
                }

                if (!current.getSubtasks().get(i).equals(cb.getText())
                                && current.getSubtasks().contains(cb.getText())) {
                    current.removeSubTask(cb.getText());
                    current.addSubtaskAtIndex(cb.getText(), i);
                    if (cb.isSelected()) {
                        current.completeSubTask(cb.getText());
                    }
                }


            }
        }
        singleBoardCtrl.server.addCard(current);
        singleBoardCtrl.updateCardFromList(BoardID, listId, current);
        //server.stopExec();
        Stage popup = (Stage) source.getScene().getWindow();
        popup.close();
        //singleBoardCtrl.refresh();
    }

    public void setDelete(ActionEvent event, Node hbox, Card current, long listId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Delete Task");
        alert.setContentText("Are you sure you want to delete this task? (Irreversible)");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                VBox par = (VBox) hbox.getParent();
                par.getChildren().remove(hbox);
                singleBoardCtrl.nodeCardMap.remove(hbox, current);

                singleBoardCtrl.server.deleteCardFromList(listId, current);
                singleBoardCtrl.refresh();
                Button source = (Button) event.getSource();
                Stage popup = (Stage) source.getScene().getWindow();
                System.out.println(popup);
                //popup.close();
            }
        });
        singleBoardCtrl.server.stopExec();
    }

    public void setCancel(ActionEvent event, Node hboxCard) {
        Button cancel = (Button) event.getSource();
        Stage popup = (Stage) cancel.getScene().getWindow();
        popup.close();
        singleBoardCtrl.server.stopExec();
    }

    public void addNewCard(VBox parent) {
        BoardList boardList = (BoardList) parent.getUserData();
        long listId = boardList.getId();
        Optional<String> cardTitle = showTitleDialog();
        if (cardTitle.isPresent()) {
            Card newCard = new Card(cardTitle.get());
            placeCard(parent, newCard);
            Card saved = singleBoardCtrl.server.addCard(newCard);
            newCard.setId(saved.getId());
            singleBoardCtrl.saveCardToList(BoardID, listId, newCard);
            singleBoardCtrl.refresh();
        }
    }

    public Optional<String> showTitleDialog() {
        TextInputDialog titleInput = new TextInputDialog();
        titleInput.setTitle("Task Title");
        titleInput.setHeaderText("Set task title");
        titleInput.setContentText("Enter task title:");
        Optional<String> result = titleInput.showAndWait();
        if (result.isPresent() && result.get().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Title cannot be empty!", ButtonType.OK);
            alert.showAndWait();
            return showTitleDialog();
        }
        return result;
    }

    void setDragAndDrop(VBox parent, Node cardNode) {
        Long listId = ((BoardList) parent.getUserData()).getId();
        cardNode.setOnDragDetected(event -> {
            if (singleBoardCtrl.checkReadOnlyMode(singleBoardCtrl.isUnlocked)) {
                return;
            }
            SingleBoardCtrl.dragboard = cardNode.startDragAndDrop(TransferMode.MOVE);
            singleBoardCtrl.content = new ClipboardContent();
            singleBoardCtrl.content.putString(
                    cardNode.getId() + "; " + ((BoardList) parent.getUserData()).getId());
            SingleBoardCtrl.dragboard.setContent(singleBoardCtrl.content);
            // Create a snapshot of the current card
            WritableImage snapshot = cardNode.snapshot(new SnapshotParameters(), null);
            ImageView imageView = new ImageView(snapshot);
            imageView.setFitWidth(cardNode.getBoundsInLocal().getWidth());
            imageView.setFitHeight(cardNode.getBoundsInLocal().getHeight());
            // Set the custom drag view to only show the current card being dragged
            SingleBoardCtrl.dragboard.setDragView(
                    imageView.getImage(), event.getX(), event.getY());
            event.consume();
        });
        cardNode.setOnDragOver(event -> {
            if (SingleBoardCtrl.dragboard.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        cardNode.setOnDragDropped(event -> {
            boolean success = false;
            if (SingleBoardCtrl.dragboard.hasString()) {
                String[] splitDragboard = SingleBoardCtrl.dragboard.getString().split(";");
                long sourceListId = Long.parseLong(splitDragboard[1].trim());
                long sourceListIndex =
                        singleBoardCtrl.listCtrl.getListIndex(
                                BoardID, sourceListId);
                ObservableList<Node> hboxChildren = singleBoardCtrl.hbox_lists.getChildren();
                AnchorPane sourceList = (AnchorPane) (hboxChildren.get((int) sourceListIndex));
                int sourceListSize = sourceList.getChildren().size();
                VBox sourceParent = (VBox) sourceList.getChildren().get(sourceListSize - 1);
                Node draggedCardNode = sourceParent.lookup("#" + splitDragboard[0].trim());
                Card draggedCard = singleBoardCtrl.nodeCardMap.get(draggedCardNode);
                if (draggedCardNode != null) {
                    if (sourceParent != parent) {
                        parent.getChildren().add(0, draggedCardNode);
                        singleBoardCtrl.deleteCardFromList(
                                BoardID, sourceListId, draggedCard);
                        singleBoardCtrl.saveCardToList(
                                BoardID, listId, draggedCard);
                        success = true;
                    } else {
                        ObservableList<Node> children = parent.getChildren();
                        int draggedIndex = children.indexOf((AnchorPane) event.getGestureSource());
                        int dropIndex = children.indexOf((AnchorPane) event.getGestureTarget());
                        draggedCardNode = children.remove(draggedIndex);
                        singleBoardCtrl.deleteCardFromList(
                                BoardID, sourceListId, draggedCard);
                        children.add(dropIndex, draggedCardNode);
                        addCardAtIndex(sourceListId, dropIndex, draggedCard);
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        cardNode.setOnDragDone(event -> {
            VBox sourceParent = (VBox) ((AnchorPane) event.getGestureSource()).getParent();
            VBox targetParent = null;
            if (event.getGestureTarget() != null &&
                    event.getGestureTarget() instanceof AnchorPane) {
                targetParent = (VBox) ((AnchorPane) event.getGestureTarget()).getParent();
            }
            if (SingleBoardCtrl.dragboard.hasString()
                    && event.isDropCompleted() && sourceParent != targetParent) {
                parent.getChildren().remove(cardNode);
            }
            singleBoardCtrl.refresh();
            event.consume();
        });
    }

    void addCardAtIndex(long sourceListId, int dropIndex, Card card) {
        try {
            singleBoardCtrl.server.addCardAtIndex(sourceListId, dropIndex, card);
        } catch (WebApplicationException e) {
            SingleBoardCtrl.alertError(e);
        }
    }
}
