import com.sun.net.httpserver.HttpServer; // Javaに標準で備わる、Webの要求に返事をする機能を使います。
import java.net.InetSocketAddress; // ポート（通信の出入口）の番号を指定する機能を使います。

public class App { // Appという名前で、プログラム全体をまとめます。
    public static void main(String[] args) throws Exception { // プログラムの実行はここから始まります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 1 8080番のポート（通信の出入口）で待ち受ける準備をします。
        String text = "ハンドラが動いた";
        System.out.println(text);
        server.createContext("/", exchange -> { // 0 「/」への要求を受け取ったときの処理を書きます。
            String message = "test";// 0
            System.out.println(message);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 0
                                                                                            // 普通の文章をUTF-8（日本語も扱える文字の表し方）で返すと伝えます。
            byte[] body = message.getBytes("UTF-8"); // 0 文字をUTF-8（日本語も扱える文字の表し方）のバイト列（送信用の数値の並び）に変えます。
            exchange.sendResponseHeaders(200, body.length); // 0200（成功を表す番号）と、送るデータのバイト数（大きさ）を伝えます。
            exchange.getResponseBody().write(body); // 0 用意した文字のデータをブラウザに送ります。
            exchange.getResponseBody().close(); // 今回の返事を書き終えます。
        }); // 「/」への要求を受け取ったときの処理はここまでです。
        server.start(); // 1 待ち受け（アクセスを待つ状態）を始めます。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 1起動したことを、指定の日本語でターミナル（コマンドを入力する画面）に表示します。
    } // 開始時の処理はここまでです。
} // プログラム全体のまとまりはここまでです。
