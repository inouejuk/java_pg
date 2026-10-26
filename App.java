import com.sun.net.httpserver.HttpServer; // Javaに標準で備わる、Webの要求に返事をする機能を使います。
import java.net.InetSocketAddress; // ポート（通信の出入口）の番号を指定する機能を使います。
import java.net.URLDecoder; // 今回追加：URL用に変換された文字を元に戻す機能を使います。
import java.nio.charset.StandardCharsets; // 【追加】UTF-8（日本語も扱える文字の表し方）を指定するために使います。
import java.util.ArrayList; // Todo機能を追加：項目を追加できるリスト（複数の値をまとめる入れ物）を使います。
import java.util.List; // Todo機能を追加：文字列のリストを扱うために使います。

public class App { // Appという名前で、プログラム全体をまとめます。
    public static void main(String[] args) throws Exception { // プログラムの実行はここから始まります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 1 8080番のポート（通信の出入口）で待ち受ける準備をします。
        List<String> todos = new ArrayList<>(); // 【移動】Todo（やること）をためる空のリスト（複数の値をまとめる入れ物）を一度だけ作ります。
        server.createContext("/", exchange -> { // 0 「/」への要求を受け取ったときの処理を書きます。
            String path = exchange.getRequestURI().getPath(); // 追加：パス（URLの「/hello」などの部分）を取り出します。
            String method = exchange.getRequestMethod(); // 【追加】要求の方法（GETは表示、POSTは送信）を取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // Todo機能のため移動：返す形式は通常、text/plain（普通の文章）にします。
                                                                                            // 普通の文章をUTF-8（日本語も扱える文字の表し方）で返すと伝えます。
            if (path.equals("/add") && method.equals("POST")) { // 【追加】「/add」にPOST（送信）で届いたときに処理します。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 【追加】送られてきた中身をUTF-8の文字列として受け取ります。
                String value = body.substring(5); // 【追加】先頭の「todo=」の5文字を除きます。
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8); // 【追加】URL用に変換された日本語などを元に戻します。
                if (!todo.isEmpty()) { // 【追加】入力が空でないときだけ追加します。
                    todos.add(todo); // 【追加】受け取ったTodoをリストに追加します。
                } // 【追加】空でない入力を追加する処理はここまでです。
                exchange.getResponseHeaders().set("Location", "/"); // 【追加】戻り先を「/」に指定します。
                exchange.sendResponseHeaders(303, -1); // 【追加】303（戻り先を開き直す合図）を返し、本文は送りません。
                exchange.close(); // 【追加】今回のやり取りを閉じます。
                return; // 【追加】今回の要求を処理する部分をここで終了します。
            } else if (path.equals("/")) { // 【追加】「/」で入力フォーム（入力欄と送信ボタン）と一覧を表示します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 【追加・移動】指定のフォームを置き、HTML（ページの表示を表す書き方）の箇条書きを始めます。
                for (String todo : todos) { // 【移動】リストから1件ずつ取り出して繰り返します。
                    html += "<li>" + todo + "</li>"; // 【移動】箇条書きの項目を1件ずつ足します。
                } // 【移動】繰り返しはここまでです。
                html += "</ul>"; // 【移動】箇条書きを閉じます。
                message = html; // 【移動】組み立てたHTMLを返す内容にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 【移動】返す形式をHTMLにし、日本語をUTF-8で扱います。
            } else if (path.equals("/hello")) { // 追加：パスが「/hello」と同じか比べます。
                String query = exchange.getRequestURI().getRawQuery(); // 今回追加：URLの「?」より後ろの文字列を取り出します。
                System.out.println("query = " + query);// 今回追加：「name=」の後ろを切り出します。
                String name = "ゲスト"; // 今回変更：名前の指定がないときは「ゲスト」を使います。
                if (query != null) { // 今回追加：null（値がない状態）でなければ、名前を取り出します。
                    name = query.substring(5); // 今回変更：「name=」の後ろを切り出します。
                    name = URLDecoder.decode(name, "UTF-8"); // URL用に変換された文字を元に戻します。
                } // 今回追加：名前を取り出す処理はここまでです。
                message = "こんにちは、" + name + "さん！"; // 今回変更：取り出した名前を返事につなげます。
            } else if (path.equals("/bye")) { // 追加：パスが「/bye」と同じか比べます。
                message = "さようなら！";
            } else if (path.equals("/test")) { // 追加：パスが「/bye」と同じか比べます。
                message = "OK";
            } else {
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
