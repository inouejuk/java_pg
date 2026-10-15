public class Item { // Itemというクラス（プログラムをまとめる入れ物）を作ります。
    public static void main(String[] args) { // プログラムを実行すると、ここから処理が始まります。
        String title = "go eating"; // String型（文字列を扱う種類）の変数（値を入れる箱）titleに文字を入れます。
        String html = "<li>" + title + "</li>"; // +で文字列をつなぎ、HTML（ページの構造を表す書き方）の1行を作ります。
        int count = 3; // int型（整数を扱う種類）の変数countに、半角数字の3を入れます。
        System.out.println(html); // 作った文字列をターミナル（文字で操作や結果表示をする画面）に1行出します。
        boolean done = false;
        System.out.println(done);
        System.out.println("now:" + count);
    } // mainの処理はここまでです。
} // Itemの定義はここまでです。
