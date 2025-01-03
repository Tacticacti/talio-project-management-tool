package server.api;

import commons.Board;
import commons.BoardList;
import commons.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import server.Admin;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import server.DatabaseUtils;
import server.Encryption;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BoardListControllerTest {


    private SimpMessagingTemplate messagingTemplate;

    private DatabaseUtils databaseUtils;
    private TestBoardListRepository repo;
    private TestBoardRepository boardRepo;
    private BoardListController controller;
    private BoardController boardController;
    private Board b1;
    private BoardList bl1;
    private Card c1, c2;

    DeferredResult<ResponseEntity<Card>> result = new DeferredResult<>();


    @BeforeEach
    public void setup() {
        databaseUtils = new DatabaseUtils();
        boardRepo = new TestBoardRepository();
        repo = new TestBoardListRepository();
        messagingTemplate = (SimpMessagingTemplate) new TestSimpMessagingTemplate();
        controller = new BoardListController(repo, new DatabaseUtils(), messagingTemplate);
        boardController = new BoardController(boardRepo, new DatabaseUtils(),
                messagingTemplate, new Admin(), new Encryption());

        b1 = new Board("b1");
        b1.setId(0L);
        bl1 = new BoardList("bl1");
        bl1.setId(0L);
        b1.addList(bl1);

        c1 = new Card("c1");
        c1.description = "desc";
        c1.subtasks = List.of("task1", "task2");
        c2 = new Card("c2");
        c2.description = "new description";
        c2.subtasks = List.of("st1", "st2");
        c1.id = 0L;
        c2.id = 1L;

        boardController.add(b1);
        result = controller.cardChanges();
    }

    @Test
    public void testGetAll() {
        controller.addList(bl1);
        assertEquals(List.of(bl1), controller.getAll());
    }

    @Test
    public void addNullTest() {
        var ret = controller.addList(null);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void addCardWrongId() {
        var ret = controller.addCardToId(10L, c1);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void addCardOK() {
        controller.addList(bl1);
        var ret = controller.addCardToId(0L, c1);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertTrue(boardController.getById(0L)
                .getBody().getLists().get(0).getCards().contains(c1));
    }

    @Test
    public void getById() {
        controller.addList(bl1);
        var ret = controller.getById(99L);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.getById(0L);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void changeName() {
        controller.addList(bl1);
        var ret = controller.changeListsName(99L, "new name");
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.changeListsName(0L, "new name");
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertEquals("new name", ret.getBody().getName());
    }

    @Test
    public void deleteList() {
        controller.addList(bl1);
        var ret = controller.deleteList(99L);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.deleteList(0L);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        var ret2 = controller.getById(0L);
        assertEquals(BAD_REQUEST, ret2.getStatusCode());
    }

    @Test
    public void deleteCard() {
        controller.addList(bl1);
        var ret2 = controller.addCardToId(0L, c1);
        assertNotEquals(BAD_REQUEST, ret2.getStatusCode());

        var ret = controller.deleteCardFromId(99L, Pair.of(true, c1));
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.deleteCardFromId(0L, Pair.of(false, c2));
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.deleteCardFromId(0L, Pair.of(false, c1));
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertFalse(ret.getBody().getCards().contains(c1));
    }

    @Test
    public void updateCard() {
        controller.addList(bl1);
        controller.addCardToId(0L, c1);

        var ret = controller.updateCardInId(99L, c1);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.updateCardInId(0L, c2);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.updateCardInId(0L, c1);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void insertAt() {
        controller.addList(bl1);
        controller.addCardToId(0L, c1);

        var ret = controller.insertAt(99L, Pair.of(0L, c2));
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.insertAt(0L, Pair.of(0L, c2));
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertEquals(List.of(c2, c1), ret.getBody().getCards());
    }


    @Test
    public void cardChanges(){
        controller.addList(bl1);
        var ret2 = controller.addCardToId(0L, c1);
        assertNotEquals(BAD_REQUEST, ret2.getStatusCode());

        var ret = controller.deleteCardFromId(99L, Pair.of(true, c1));
        assertEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.deleteCardFromId(0L, Pair.of(false, c2));
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        ret = controller.deleteCardFromId(0L, Pair.of(false, c1));
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertFalse(ret.getBody().getCards().contains(c1));

        assertEquals(ResponseEntity.ok(c1), result.getResult());

    }
}
