import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Todo> todos = new ArrayList<>();
        todos.add(new Todo("牛乳を買う", true));
        todos.add(new Todo("ゴミを出す", false));
        todos.add(new Todo("ジムに行く", true));
        for (Todo todo : todos) {
            System.out.println(todo.toItem());
        }
    }
}

class Todo {
    private final String title;
    private final boolean done;

    Todo(String title, boolean done) {
        this.title = title;
        this.done = done;
    }

    String toItem() {
        if (done) {
            return "<li>[_______済_______] " + title + "</li>";
        }
        return "<li>" + title + "</li>";
    }
}
