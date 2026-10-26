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
