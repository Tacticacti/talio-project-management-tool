package server;

import org.springframework.stereotype.Service;

import commons.Board;
import commons.BoardList;
import commons.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DatabaseUtils {
    public Board mockSimpleBoard() {
        Board board = new Board("test board");
        BoardList l1 = new BoardList("test list 1");
        l1.addCard(new Card("aa"));
        l1.addCard(new Card("bb"));
        l1.addCard(new Card("ca"));

        board.addList(l1);

        BoardList l2 = new BoardList("test list 2");
        l2.addCard(new Card("az"));
        l2.addCard(new Card("bz"));
        l2.addCard(new Card("cz"));

        board.addList(l2);
        return board;
    }

    public void updateCard(Card card, String title, String description,
                           List<String> subtasks, List<String> tags) {
        card.setTitle(title);
        card.setDescription(description);
        card.subtasks = new ArrayList<>();
        for(String s : subtasks){
            card.addSubTask(s);
        }
    }

    public Optional<BoardList> getListById(Board board, Long listId) {
        return board.getLists().stream()
                .filter(x -> Objects.equals(x.getId(), listId))
                .findFirst();
    }
}