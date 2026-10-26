import com.sun.net.httpserver.HttpServer; // Javaに標準で備わる、Webの要求に返事をする機能を使います。
import java.net.InetSocketAddress; // ポート（通信の出入口）の番号を指定する機能を使います。
import java.net.URLDecoder; // 今回追加：URL用に変換された文字を元に戻す機能を使います。
import java.nio.charset.StandardCharsets; // 【追加】UTF-8（日本語も扱える文字の表し方）を指定するために使います。
import java.util.ArrayList; // Todo機能を追加：項目を追加できるリスト（複数の値をまとめる入れ物）を使います。
import java.util.List; //  Todoのリスト（複数のやることをまとめる入れ物）を扱うために使います。

public class App { // Appという名前で、プログラム全体をまとめます。
    static List<Todo> todos = new ArrayList<>(); // Todoをためるリストを、mainの外に置きます。
    static int nextId = 1; // 次のTodoに付ける番号を、1から始めます。

    public static void main(String[] args) throws Exception { // プログラムの実行はここから始まります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 1 8080番のポート（通信の出入口）で待ち受ける準備をします。
        server.createContext("/", exchange -> { // 0 「/」への要求を受け取ったときの処理を書きます。
            String path = exchange.getRequestURI().getPath(); // パス（URLの「/add」などの部分）を取り出します。
            String method = exchange.getRequestMethod(); // 【追加】要求の方法（GETは表示、POSTは送信）を取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // Todo機能のため移動：返す形式は通常、text/plain（普通の文章）にします。
                                                                                            // 普通の文章をUTF-8（日本語も扱える文字の表し方）で返すと伝えます。
            if (path.equals("/add") && method.equals("POST")) { // 【追加】「/add」にPOST（送信）で届いたときに処理します。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 【追加】送られてきた中身をUTF-8の文字列として受け取ります。
                String value = body.substring(5); // 【追加】先頭の「todo=」の5文字を除きます。
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // フォームで受け取った文字を元に戻し、titleに入れます。
                if (!title.isEmpty()) { // titleが空でないときだけ追加します。
                    todos.add(new Todo(nextId, title)); // 番号とやることを渡して、未完了のTodoを作って追加します。
                    nextId++; // 次に付ける番号を1つ増やします。
                } // 【追加】空でない入力を追加する処理はここまでです。
                exchange.getResponseHeaders().set("Location", "/"); // 【追加】戻り先を「/」に指定します。
                exchange.sendResponseHeaders(303, -1); // 【追加】303（戻り先を開き直す合図）を返し、本文は送りません。
                exchange.close(); // 【追加】今回のやり取りを閉じます。
                return; // 【追加】今回の要求を処理する部分をここで終了します。
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加 「/done」をGET（リンクを開く要求）で受け取ったときに処理します。
                String query = exchange.getRequestURI().getQuery(); // ★追加 URLの「?」より後ろにある「id=2」などの文字を取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                                                                                      // 「id=」で始まり、その後ろに文字があるときだけ処理します。
                    try { // ★追加 idを数に変える処理を試します。
                        int id = Integer.parseInt(query.substring(3)); // ★追加 「id=」の後ろを切り出し、数に変えます。
                        for (Todo todo : todos) { // ★追加 Todoを1件ずつ取り出します。
                            if (todo.getId() == id) { // ★追加 リンクで指定された番号と一致するか調べます。
                                todo.setDone(true); // ★追加 一致したTodoを完了にします。
                                break; // ★追加 1件見つかったので、繰り返しを終えます。
                            } // ★追加 番号が一致したときの処理はここまでです。
                        } // ★追加 Todoを探す繰り返しはここまでです。
                    } catch (NumberFormatException e) { // ★追加 数に変換できなかったときは、ここで受け止めます。
                        // ★追加 数字でないときは、何も変更しません。
                    } // ★追加 数に変える処理はここまでです。
                } // ★追加 idが指定されているときの処理はここまでです。
                exchange.getResponseHeaders().set("Location", "/"); // ★追加 戻り先を一覧の「/」に指定します。
                exchange.sendResponseHeaders(303, -1); // ★追加 303（戻り先を開き直す合図）を返し、本文は送りません。
                exchange.close(); // ★追加 今回のやり取りを閉じます。
                return; // ★追加 今回の要求の処理を終えます。
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加 「/delete」をGET（リンクを開く要求）で受け取ったときに処理します。
                String query = exchange.getRequestURI().getQuery(); // ★追加 URLの「?」より後ろにある「id=2」などの文字を取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                                                                                      // 「id=」で始まり、その後ろに文字があるときだけ処理します。
                    try { // ★追加 idを数に変える処理を試します。
                        int id = Integer.parseInt(query.substring(3)); // ★追加 「id=」の後ろを切り出し、数に変えます。
                        todos.removeIf(todo -> todo.getId() == id); // ★追加 番号が一致するTodoをリストから取り除きます。
                    } catch (NumberFormatException e) { // ★追加 数に変換できなかったときは、ここで受け止めます。
                        // ★追加 数字でないときは、何も変更しません。
                    } // ★追加 数に変える処理はここまでです。
                } // ★追加 idが指定されているときの処理はここまでです。
                exchange.getResponseHeaders().set("Location", "/"); // ★追加 戻り先を一覧の「/」に指定します。
                exchange.sendResponseHeaders(303, -1); // ★追加 303（戻り先を開き直す合図）を返し、本文は送りません。
                exchange.close(); // ★追加 今回のやり取りを閉じます。
                return; // ★追加 今回の要求の処理を終えます。
            } else if (path.equals("/")) { // 【追加】「/」で入力フォーム（入力欄と送信ボタン）と一覧を表示します。
                String html = "<!DOCTYPE html><html lang='ja'><head><meta charset='UTF-8'>"
                        + "<title>わたしのTodo</title>"
                        + "<style>" // style（見た目の指定）で幅・余白・文字の大きさだけを整えます。
                        + "body { max-width: 640px; margin: 32px auto; padding: 0 16px; font-size: 16px; }"
                        + "h1 { font-size: 24px; margin: 0 0 16px; }"
                        + "form { margin-bottom: 16px; }"
                        + "input, button { font-size: inherit; }"
                        + "</style></head><body>"
                        + "<h1>わたしのTodo</h1>" // h1（ページの大きな見出し）を表示します。
                        + "<form method='post' action='/add'><input name='todo'><button>追加</button></form>";
                if (todos.isEmpty()) { // Todoが0件のときだけ案内を表示します。
                    html += "<p>やることは、いまゼロです</p>";
                } else { // Todoがあるときは、これまでと同じ内容で一覧を表示します。
                    html += "<ul>"; // ul（箇条書きの一覧）を始めます。
                    for (Todo todo : todos) { // Todoをリストから1件ずつ取り出します。
                        String mark = ""; // 完了の印は、最初は空にします。
                        if (todo.isDone()) { // このTodoが完了しているか読み出します。
                            mark = " ✔"; // 完了しているときだけ、空白とチェックの印を付けます。
                        } // 完了の印を決める処理はここまでです。
                        html += "<li>" + todo.getTitle() + mark + " <a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // ★追加
                                                                                                    // やることと完了の印の横に、そのTodoの番号を入れた完了・削除リンクを付けます。
                    } // 【移動】繰り返しはここまでです。
                    html += "</ul>"; // 【移動】箇条書きを閉じます。
                }
                html += "</body></html>"; // ページ全体を閉じます。
                message = html; // 【移動】組み立てたHTMLを返す内容にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 【移動】返す形式をHTMLにし、日本語をUTF-8で扱います。
            } else { // /hello・/bye・/testの分岐を削除し、それ以外のパスをここで受けます。
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

class Todo { // Todoクラス（1件のやることを表す型）を、同じファイルに作ります。
    private final int id; // 番号。privateはこのクラスの中だけ、finalは後から変更しないという意味です。
    private final String title; // やること。このクラスの中だけで扱い、後から変更しません。
    private boolean done; // 終わったか。このクラスの中だけで扱い、後から変更できます。

    Todo(int id, String title) { // コンストラクタ（newで作るときの初期設定）で番号とやることを受け取ります。
        this.id = id; // 受け取った番号を、このTodoのidに入れます。
        this.title = title; // 受け取ったやることを、このTodoのtitleに入れます。
        this.done = false; // 作ったときは、まだ終わっていない状態にします。
    } // 初期設定はここまでです。

    int getId() { // 番号を読み出すメソッド（名前を付けた処理）です。
        return id; // このTodoの番号を返します。
    } // 番号を読み出す処理はここまでです。

    String getTitle() { // やることを読み出すメソッド（名前を付けた処理）です。
        return title; // このTodoのやることを返します。
    } // やることを読み出す処理はここまでです。

    boolean isDone() { // 終わったかを読み出すメソッド（名前を付けた処理）です。
        return done; // trueなら完了、falseなら未完了を表します。
    } // 終わったかを読み出す処理はここまでです。

    void setDone(boolean done) { // 終わったかを書き換えるメソッド（名前を付けた処理）です。
        this.done = done; // 受け取った値を、このTodoのdoneに入れます。
    } // 終わったかを書き換える処理はここまでです。
} // Todoクラスはここまでです。
