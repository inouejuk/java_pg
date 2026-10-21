public class Items { // Itemsという名前のクラス（プログラムのまとまり）を作ります。
    public static void main(String[] args) { // プログラムの実行はここから始まります。
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" }; // todosという配列（複数の値を順番に持つもの）に、Todoの文字列（文字の並び）を入れます。
        boolean[] done = { true, false, false, false }; // boolean（trueかfalseを持つ種類）の配列（複数の値を順番に持つもの）で、trueを済、falseを未完了とします。
        for (int i = 0; i <= todos.length; i++) { // iを0から始め、todos.length（Todoの件数）より小さい間、1ずつ増やして繰り返します。
            if (!todos[i].isEmpty()) { // isEmpty()で空かどうかを調べ、!で結果を反転させて、空でないときだけ次の処理を行います。
                if (done[i]) { // 同じ位置のdone[i]がtrue（済）のとき、次の処理を行います。
                    System.out.println("<li>[済]" + todos[i] + "</li>"); // 済んだTodoには[済]を付けて1行出力します。
                } else { // done[i]がfalse（未完了）のとき、次の処理を行います。
                    System.out.println("<li>" + todos[i] + "</li>"); // 未完了のTodoは印を付けずに1行出力します。
                } // 済かどうかで処理を分ける範囲を終えます。
            } // 空でないときだけ行う処理を終えます。
        } // 繰り返しの範囲を終えます。
    } // 実行する処理を終えます。
} // Itemsのまとまりを終えます。
