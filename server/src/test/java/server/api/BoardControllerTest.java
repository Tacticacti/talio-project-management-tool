package server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Board;
import commons.BoardList;
import commons.Card;
import server.DatabaseUtils;

import java.util.List;

public class BoardControllerTest {

    private DatabaseUtils databaseUtils;
    private TestBoardRepository boardRepo;
    private BoardController controller;
    private Board b1, b2;
    private BoardList bl1;
    private Card c1, c2;

    @BeforeEach
    public void setup() {
        databaseUtils = new DatabaseUtils();
        boardRepo = new TestBoardRepository();
        controller = new BoardController(boardRepo, new DatabaseUtils());
        b1 = new Board("b1");
        b2 = new Board("b2");
        bl1 = new BoardList("bl1");
        bl1.setId(0L);
        b1.addList(bl1);

        c1 = new Card("c1");
        c1.description = "desc";
        c1.subtasks = List.of("task1", "task2");
        c2 = new Card("c2");
        c2.description = "new description";
        c2.subtasks = List.of("st1", "st2");
    }

    @Test
    public void cannotAddNullBoard() {
        var ret = controller.add(null);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void addBoard() {
        var ret = controller.add(new Board("board"));
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertTrue(boardRepo.calledMethods.contains("save"));
    }

    @Test
    public void getAll() {
        controller.add(b1);
        controller.add(b2);
        var x = controller.getAll();
        assertTrue(x.contains(b1));
        assertTrue(x.contains(b2));
        String res = controller.getAllDebug();
        assertNotNull(res);
    }

    @Test
    public void getById() {
        controller.add(b1);
        var ret = controller.getById(0);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        assertEquals(b1, ret.getBody());
    }

    @Test
    public void getByIdNotPresent() {
        var ret = controller.getById(99);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void addListWrongId() {
        controller.add(b1);
        var ret = controller.addListToBoard(1000, "custom name");
        assertEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void addListOK() {
        controller.add(b1);
        String name = "custom list";
        var ret = controller.addListToBoard(0, name);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
        // assertEquals(name, ret.getBody().getName());
    }

    @Test
    public void deleteListWrongID() {
        controller.add(b1);
        var ret = controller.deleteList(99L, 99L);
        assertEquals(BAD_REQUEST, ret.getStatusCode());
    }

    @Test
    public void deleteListOK() {
        controller.add(b1);
        var ret = controller.deleteList(0L, 0L);
        assertNotEquals(BAD_REQUEST, ret.getStatusCode());
    }
}
