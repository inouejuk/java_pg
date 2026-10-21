public class Items { // Itemsという名前のクラス（プログラムをまとめる入れ物）を作ります。
    public static void main(String[] args) { // main（実行の開始地点）から処理を始めます。
        String[] todos = { "牛乳を買う", "卵を買う", "パンを買う" }; // 文字列（文字の並び）3件を、todosという配列（値を順番に入れる入れ物）に入れます。
        for (int i = 0; i < todos.length; i++) { // 整数を入れるiを0から始め、todos.length（配列の件数）より小さい間、1ずつ増やして繰り返します。
            System.out.println("<li>" + todos[i] + "</li>"); // todos[i]（0から数えてi番目のTodo）を<li>と</li>で囲んで1行出力します。
        } // 繰り返しの範囲を終えます。
    } // main（実行の開始地点）から始めた処理を終えます。
} // Itemsというクラス（プログラムをまとめる入れ物）の範囲を終えます。
