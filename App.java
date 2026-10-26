import com.sun.net.httpserver.HttpServer; // Javaに標準で備わる、Webの要求に返事をする機能を使います。
import java.net.InetSocketAddress; // ポート（通信の出入口）の番号を指定する機能を使います。
import java.net.URLDecoder; // 今回追加：URL用に変換された文字を元に戻す機能を使います。
import java.nio.charset.StandardCharsets; // 【追加】UTF-8（日本語も扱える文字の表し方）を指定するために使います。
import java.util.ArrayList; // Todo機能を追加：項目を追加できるリスト（複数の値をまとめる入れ物）を使います。
import java.util.List; // ★変更 Todoのリスト（複数のやることをまとめる入れ物）を扱うために使います。

public class App { // Appという名前で、プログラム全体をまとめます。
    static List<Todo> todos = new ArrayList<>(); // ★変更 Todoをためるリストを、mainの外に置きます。
    static int nextId = 1; // ★変更 次のTodoに付ける番号を、1から始めます。

    public static void main(String[] args) throws Exception { // プログラムの実行はここから始まります。
        todos.add(new Todo(nextId++, "牛乳を買う")); // ★変更 今の番号で未完了のTodoを追加し、次の番号に進めます。
        Todo egg = new Todo(nextId++, "卵を買う"); // ★変更 今の番号でTodoを作ってeggに入れ、次の番号に進めます。
        egg.setDone(true); // ★変更 卵を買うTodoを完了にします。
        todos.add(egg); // ★変更 完了にしたTodoをリストに追加します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 1 8080番のポート（通信の出入口）で待ち受ける準備をします。
        server.createContext("/", exchange -> { // 0 「/」への要求を受け取ったときの処理を書きます。
            String path = exchange.getRequestURI().getPath(); // ★変更 パス（URLの「/add」などの部分）を取り出します。
            String method = exchange.getRequestMethod(); // 【追加】要求の方法（GETは表示、POSTは送信）を取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // Todo機能のため移動：返す形式は通常、text/plain（普通の文章）にします。
                                                                                            // 普通の文章をUTF-8（日本語も扱える文字の表し方）で返すと伝えます。
            if (path.equals("/add") && method.equals("POST")) { // 【追加】「/add」にPOST（送信）で届いたときに処理します。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 【追加】送られてきた中身をUTF-8の文字列として受け取ります。
                String value = body.substring(5); // 【追加】先頭の「todo=」の5文字を除きます。
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // ★変更 フォームで受け取った文字を元に戻し、titleに入れます。
                if (!title.isEmpty()) { // ★変更 titleが空でないときだけ追加します。
                    todos.add(new Todo(nextId, title)); // ★変更 番号とやることを渡して、未完了のTodoを作って追加します。
                    nextId++; // ★変更 次に付ける番号を1つ増やします。
                } // 【追加】空でない入力を追加する処理はここまでです。
                exchange.getResponseHeaders().set("Location", "/"); // 【追加】戻り先を「/」に指定します。
                exchange.sendResponseHeaders(303, -1); // 【追加】303（戻り先を開き直す合図）を返し、本文は送りません。
                exchange.close(); // 【追加】今回のやり取りを閉じます。
                return; // 【追加】今回の要求を処理する部分をここで終了します。
            } else if (path.equals("/")) { // 【追加】「/」で入力フォーム（入力欄と送信ボタン）と一覧を表示します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 【追加・移動】指定のフォームを置き、HTML（ページの表示を表す書き方）の箇条書きを始めます。
                for (Todo todo : todos) { // ★変更 Todoをリストから1件ずつ取り出します。
                    String mark = ""; // ★変更 完了の印は、最初は空にします。
                    if (todo.isDone()) { // ★変更 このTodoが完了しているか読み出します。
                        mark = " ✔"; // ★変更 完了しているときだけ、空白とチェックの印を付けます。
                    } // ★変更 完了の印を決める処理はここまでです。
                    html += "<li>" + todo.getTitle() + mark + "</li>"; // ★変更 やることを読み出し、完了の印と一緒に一覧へ足します。
                } // 【移動】繰り返しはここまでです。
                html += "</ul>"; // 【移動】箇条書きを閉じます。
                message = html; // 【移動】組み立てたHTMLを返す内容にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 【移動】返す形式をHTMLにし、日本語をUTF-8で扱います。
            } else { // ★変更 /hello・/bye・/testの分岐を削除し、それ以外のパスをここで受けます。
                message = "ページが見つかりません";
            }
            System.out.println(message);
            byte[] body = message.getBytes("UTF-8"); // 0 文字をUTF-8（日本語も扱える文字の表し方）のバイト列（送信用の数値の並び）に変えます。
            exchange.sendResponseHeaders(200, body.length); // 0200（成功を表す番号）と、送るデータのバイト数（大きさ）を伝えます。
            exchange.getResponseBody().write(body); // 0 用意した文字のデータをブラウザに送ります。
            exchange.getResponseBody().close(); // 今回の返事を書き終えます。
        }); // 「/」への要求を受け取ったときの処理はここまでです。
        server.start(); // 1 待ち受け（アクセスを待つ状態）を始めます。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 1起動したことを、指定の日本語でターミナル（コマンドを入力する画面）に表示します。
    } // 開始時の処理はここまでです。
} // プログラム全体のまとまりはここまでです。

class Todo { // ★変更 Todoクラス（1件のやることを表す型）を、同じファイルに作ります。
    private final int id; // ★変更 番号。privateはこのクラスの中だけ、finalは後から変更しないという意味です。
    private final String title; // ★変更 やること。このクラスの中だけで扱い、後から変更しません。
    private boolean done; // ★変更 終わったか。このクラスの中だけで扱い、後から変更できます。

    Todo(int id, String title) { // ★変更 コンストラクタ（newで作るときの初期設定）で番号とやることを受け取ります。
        this.id = id; // ★変更 受け取った番号を、このTodoのidに入れます。
        this.title = title; // ★変更 受け取ったやることを、このTodoのtitleに入れます。
        this.done = false; // ★変更 作ったときは、まだ終わっていない状態にします。
    } // ★変更 初期設定はここまでです。

    int getId() { // ★変更 番号を読み出すメソッド（名前を付けた処理）です。
        return id; // ★変更 このTodoの番号を返します。
    } // ★変更 番号を読み出す処理はここまでです。

    String getTitle() { // ★変更 やることを読み出すメソッド（名前を付けた処理）です。
        return title; // ★変更 このTodoのやることを返します。
    } // ★変更 やることを読み出す処理はここまでです。

    boolean isDone() { // ★変更 終わったかを読み出すメソッド（名前を付けた処理）です。
        return done; // ★変更 trueなら完了、falseなら未完了を表します。
    } // ★変更 終わったかを読み出す処理はここまでです。

    void setDone(boolean done) { // ★変更 終わったかを書き換えるメソッド（名前を付けた処理）です。
        this.done = done; // ★変更 受け取った値を、このTodoのdoneに入れます。
    } // ★変更 終わったかを書き換える処理はここまでです。
} // ★変更 Todoクラスはここまでです。
