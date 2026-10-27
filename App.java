import com.sun.net.httpserver.HttpServer; // Javaに標準で備わる、Webの要求に返事をする機能を使います。
import com.sun.net.httpserver.HttpExchange; // ★ HTTPの要求と応答を扱います。
import java.io.IOException; // ★ HTTP通信のエラーを扱います。
import java.net.InetSocketAddress; // ポート（通信の出入口）の番号を指定する機能を使います。
import java.net.URLDecoder; // 今回追加：URL用に変換された文字を元に戻す機能を使います。
import java.nio.charset.StandardCharsets; // 【追加】UTF-8（日本語も扱える文字の表し方）を指定するために使います。
import java.sql.Connection; // ★ DBへの接続を扱います。
import java.sql.DriverManager; // ★ JDBCの接続を作ります。
import java.sql.PreparedStatement; // ★ 値を安全に渡すSQLを使います。
import java.sql.ResultSet; // ★ SELECTの結果を読み出します。
import java.sql.SQLException; // ★ DB操作のエラーを扱います。

public class App { // Appという名前で、プログラム全体をまとめます。
    static final String DB_URL = "jdbc:sqlite:todos.db"; // ★ ListとnextIdを廃止し、DBを保存先にします。

    public static void main(String[] args) throws Exception { // プログラムの実行はここから始まります。
        initDatabase(); // ★ 起動時に接続し、表がなければ作ります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 1 8080番のポート（通信の出入口）で待ち受ける準備をします。
        server.createContext("/", App::handleRequest); // ★ HTTPの処理をメソッドにまとめます。
        server.createContext("/api/todos", exchange -> { // 全TodoをJSONで返す入口を追加します。
            if (!"/api/todos".equals(exchange.getRequestURI().getPath())) { // パスの完全一致を確認します。
                sendJson(exchange, 404, "{\"error\":\"Not Found\"}"); // 対象外のパスには404を返します。
                return; // 処理を終了します。
            } // パスの確認を終えます。
            if (!"GET".equals(exchange.getRequestMethod())) { // GET以外の要求を確認します。
                exchange.getResponseHeaders().set("Allow", "GET"); // 利用できるメソッドを伝えます。
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}"); // GET以外には405を返します。
                return; // 処理を終了します。
            } // メソッドの確認を終えます。
            try { // データベースから一覧を取得します。
                String json = renderTodosJson(); // 送信前にJSON全体を作ります。
                sendJson(exchange, 200, json); // 一覧をJSONで返します。
            } catch (SQLException e) { // データベースのエラーを受け止めます。
                e.printStackTrace(); // 詳細をサーバー側に出力します。
                sendJson(exchange, 500, "{\"error\":\"Database error\"}"); // エラーもJSONで返します。
            } // エラー処理を終えます。
        }); // API用の入口の登録を終えます。
        server.start(); // 1 待ち受け（アクセスを待つ状態）を始めます。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 1起動したことを、指定の日本語でターミナル（コマンドを入力する画面）に表示します。
    } // 開始時の処理はここまでです。

    static Connection connect() throws SQLException { // ★ 操作ごとに接続を作ります。
        return DriverManager.getConnection(DB_URL); // ★ 起動したフォルダーのtodos.dbへ接続します。
    } // ★ 接続処理はここまでです。

    static void initDatabase() throws SQLException { // ★ CSVのload()に代わる起動時の処理です。
        try (Connection connection = connect(); // ★ 処理後に接続を閉じます。
                PreparedStatement statement = connection.prepareStatement( // ★ 表を作るSQLを用意します。
                        "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)")) { // ★
                                                                                                                  // 指定の3列を用意します。
            statement.executeUpdate(); // ★ 既存の表とデータを保ったまま実行します。
        } // ★ SQLと接続を自動で閉じます。
    } // ★ 表の準備はここまでです。

    static void insertTodo(String title) throws SQLException { // ★ 追加をINSERTで保存します。
        try (Connection connection = connect(); // ★ 追加用の接続を作ります。
                PreparedStatement statement = connection.prepareStatement( // ★ タイトルを後から渡すSQLを用意します。
                        "INSERT INTO todos (title, done) VALUES (?, 0)")) { // ★ idはSQLiteが採番し、未完了で追加します。
            statement.setString(1, title); // ★ 最初の?へタイトルを渡します。
            statement.executeUpdate(); // ★ 1件を追加して確定します。
        } // ★ SQLと接続を自動で閉じます。
    } // ★ 追加処理はここまでです。

    static void toggleTodo(long id) throws SQLException { // ★ 完了状態をUPDATEで切り替えます。
        try (Connection connection = connect(); // ★ 更新用の接続を作ります。
                PreparedStatement statement = connection.prepareStatement( // ★ 指定したidだけ更新します。
                        "UPDATE todos SET done = CASE WHEN done = 0 THEN 1 ELSE 0 END WHERE id = ?")) { // ★ 0と1を切り替えます。
            statement.setLong(1, id); // ★ ?へ対象の番号を渡します。
            statement.executeUpdate(); // ★ 完了状態を更新して確定します。
        } // ★ SQLと接続を自動で閉じます。
    } // ★ 完了状態の更新はここまでです。

    static void deleteTodo(long id) throws SQLException { // ★ 削除をDELETEで実行します。
        try (Connection connection = connect(); // ★ 削除用の接続を作ります。
                PreparedStatement statement = connection.prepareStatement("DELETE FROM todos WHERE id = ?")) { // ★
                                                                                                               // 指定したidだけ削除します。
            statement.setLong(1, id); // ★ ?へ対象の番号を渡します。
            statement.executeUpdate(); // ★ 削除して確定します。
        } // ★ SQLと接続を自動で閉じます。
    } // ★ 削除処理はここまでです。

    static void handleRequest(HttpExchange exchange) throws IOException { // ★ HTTPの要求を処理します。
        String path = exchange.getRequestURI().getPath(); // パス（URLの「/add」などの部分）を取り出します。
        String method = exchange.getRequestMethod(); // 【追加】要求の方法（GETは表示、POSTは送信）を取り出します。
        try { // ★ DB操作の失敗をHTTPのエラー応答に変えます。
            if (path.equals("/add") && method.equals("POST")) { // ★ フォームから追加します。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 【追加】送られてきた中身をUTF-8の文字列として受け取ります。
                String value = body.startsWith("todo=") ? body.substring(5) : ""; // ★ 入力項目がある場合に値を取り出します。
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // フォームで受け取った文字を元に戻し、titleに入れます。
                if (!title.isEmpty()) { // titleが空でないときだけ追加します。
                    insertTodo(title); // ★ Listへの追加とsave()をINSERTに置き換えます。
                } // ★ 空でないTodoの追加はここまでです。
                redirect(exchange); // ★ 一覧へ戻ります。
            } else if ((path.equals("/done") || path.equals("/delete")) && method.equals("GET")) { // ★
                                                                                                   // 既存の完了・削除リンクを受け付けます。
                String query = exchange.getRequestURI().getQuery(); // ★ URLからidを取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★ 番号が指定されているか確認します。
                    try { // ★ 数字でないidを受け止めます。
                        long id = Long.parseLong(query.substring(3)); // ★ SQLiteの整数に合わせてlongで読みます。
                        if (path.equals("/done")) { // ★ 完了リンクなら更新します。
                            toggleTodo(id); // ★ Listの書き換えとsave()をUPDATEに置き換えます。
                        } else { // ★ 削除リンクなら削除します。
                            deleteTodo(id); // ★ Listからの削除とsave()をDELETEに置き換えます。
                        } // ★ 操作の振り分けはここまでです。
                    } catch (NumberFormatException e) { // ★ 数字でない場合は変更しません。
                        // ★ そのまま一覧へ戻ります。
                    } // ★ 番号の確認はここまでです。
                } // ★ idが指定された場合の処理はここまでです。
                redirect(exchange); // ★ 一覧へ戻ります。
            } else if (path.equals("/") && method.equals("GET")) { // ★ 一覧をDBから取得して表示します。
                send(exchange, 200, "text/html", renderPage()); // ★ SELECTの結果から作ったHTMLを返します。
            } else { // ★ 対応しない要求を受け止めます。
                send(exchange, 404, "text/plain", "ページが見つかりません"); // ★ 404で返します。
            } // ★ URLごとの処理はここまでです。
        } catch (SQLException e) { // ★ DB操作に失敗した場合の処理です。
            e.printStackTrace(); // ★ 詳細はサーバー側へ出力します。
            send(exchange, 500, "text/plain", "データベースの操作に失敗しました"); // ★ エラーを画面へ返します。
        } catch (IllegalArgumentException e) { // ★ 不正なURLエンコードを受け止めます。
            send(exchange, 400, "text/plain", "入力の形式が正しくありません"); // ★ 入力エラーを返します。
        } // ★ エラー処理はここまでです。
    } // ★ HTTPの処理はここまでです。

    static String renderPage() throws SQLException { // ★ 表示するたびにSELECTで一覧を取得します。
        StringBuilder html = new StringBuilder("<!DOCTYPE html><html lang='ja'><head><meta charset='UTF-8'>") // ★
                                                                                                              // HTMLを組み立てます。
                .append("<title>わたしのTodo</title><style>") // ★ 既存のタイトルと見た目を保ちます。
                .append("body { max-width: 640px; margin: 32px auto; padding: 0 16px; font-size: 16px; }") // ★ 横幅と余白です。
                .append("h1 { font-size: 24px; margin: 0 0 16px; } form { margin-bottom: 16px; }") // ★ 見出しとフォームの余白です。
                .append("input, button { font-size: inherit; }</style></head><body><h1>わたしのTodo</h1>") // ★ 入力欄と見出しです。
                .append("<form method='post' action='/add'><input name='todo'><button>追加</button></form>"); // ★
                                                                                                            // 追加フォームです。
        boolean hasTodos = false; // ★ SELECTで1件でも取得できたか記録します。
        try (Connection connection = connect(); // ★ 一覧用の接続を作ります。
                PreparedStatement statement = connection
                        .prepareStatement("SELECT id, title, done FROM todos ORDER BY id"); // ★ 番号順に読み出します。
                ResultSet result = statement.executeQuery()) { // ★ SELECTを実行します。
            while (result.next()) { // ★ 結果を1件ずつ読みます。
                if (!hasTodos) { // ★ 最初の1件で一覧を開きます。
                    html.append("<ul>"); // ★ 箇条書きを始めます。
                    hasTodos = true; // ★ Todoがあることを記録します。
                } // ★ 一覧の開始はここまでです。
                long id = result.getLong("id"); // ★ DBから番号を読みます。
                String title = result.getString("title"); // ★ DBからタイトルを読みます。
                String mark = result.getInt("done") == 1 ? " ✔" : ""; // ★ DBの完了状態を印に変えます。
                html.append("<li>").append(escapeHtml(title)).append(mark) // ★ タイトルをHTMLとして安全に表示します。
                        .append(" <a href='/done?id=").append(id).append("'>完了</a>") // ★ 完了リンクを付けます。
                        .append(" <a href='/delete?id=").append(id).append("'>削除</a></li>"); // ★ 削除リンクを付けます。
            } // ★ 結果の読み出しはここまでです。
        } // ★ 結果・SQL・接続を自動で閉じます。
        html.append(hasTodos ? "</ul>" : "<p>やることは、いまゼロです</p>"); // ★ 0件なら既存の案内を表示します。
        return html.append("</body></html>").toString(); // ★ HTMLを返します。
    } // ★ 一覧の作成はここまでです。

    static String renderTodosJson() throws SQLException { // 全TodoからJSON配列を作ります。
        StringBuilder json = new StringBuilder("["); // JSON配列を開始します。
        boolean first = true; // 最初の要素かどうかを記録します。
        try (Connection connection = connect(); // 既存の接続処理を使います。
             PreparedStatement statement = connection.prepareStatement("SELECT title, done FROM todos ORDER BY id"); // 全件を番号順で取得します。
             ResultSet result = statement.executeQuery()) { // SQLを実行します。
            while (result.next()) { // Todoを1件ずつ読み出します。
                if (!first) { // 2件目以降か確認します。
                    json.append(','); // 要素の間にだけカンマを入れます。
                } // 区切りの処理を終えます。
                first = false; // 次からは2件目以降として扱います。
                json.append("{\"title\":\"").append(escapeJson(result.getString("title"))) // タイトルをJSON用にエスケープします。
                    .append("\",\"done\":").append(result.getInt("done") == 1).append('}'); // 完了状態を真偽値として追加します。
            } // 全件の読み出しを終えます。
        } // 結果・SQL・接続を自動で閉じます。
        return json.append(']').toString(); // 空の場合も正しいJSON配列を返します。
    } // 一覧作成メソッドを終えます。

    static String escapeJson(String text) { // JSON文字列用に特殊文字を変換します。
        if (text == null) { // タイトルが未設定か確認します。
            return ""; // 未設定の場合は空文字列にします。
        } // 未設定の確認を終えます。
        StringBuilder escaped = new StringBuilder(); // 変換した文字を蓄えます。
        for (int i = 0; i < text.length(); i++) { // 各文字を順に調べます。
            char c = text.charAt(i); // 現在の文字を取り出します。
            switch (c) { // 特別な扱いが必要な文字を振り分けます。
                case '"': escaped.append("\\\""); break; // 二重引用符をエスケープします。
                case '\\': escaped.append("\\\\"); break; // バックスラッシュをエスケープします。
                case '\n': escaped.append("\\n"); break; // 改行をエスケープします。
                case '\r': escaped.append("\\r"); break; // 復帰をエスケープします。
                case '\t': escaped.append("\\t"); break; // タブをエスケープします。
                default: // その他の文字を扱います。
                    if (c < 0x20) { // 残りの制御文字か確認します。
                        escaped.append(String.format("\\u%04x", (int) c)); // 制御文字をUnicodeエスケープにします。
                    } else { // 通常の文字を扱います。
                        escaped.append(c); // 日本語などをそのまま追加します。
                    } // 文字の追加を終えます。
            } // 文字の振り分けを終えます。
        } // 全文字の変換を終えます。
        return escaped.toString(); // エスケープ済みの文字列を返します。
    } // 変換メソッドを終えます。

    static void sendJson(HttpExchange exchange, int status, String json) throws IOException { // JSON専用の応答を送ります。
        try { // 送信に失敗した場合も通信を閉じます。
            byte[] body = json.getBytes(StandardCharsets.UTF_8); // JSONをUTF-8のバイト列に変換します。
            exchange.getResponseHeaders().set("Content-Type", "application/json"); // charsetを付けずに形式を指定します。
            exchange.sendResponseHeaders(status, body.length); // 状態コードとバイト数を送ります。
            exchange.getResponseBody().write(body); // JSON本文を送ります。
        } finally { // 送信の成否にかかわらず後片付けします。
            exchange.close(); // 通信を閉じます。
        } // 後片付けを終えます。
    } // JSON送信メソッドを終えます。

    static String escapeHtml(String text) { // ★ タイトル中の記号をHTML用に変換します。
        return text == null ? ""
                : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") // ★ 本文中の特殊文字を変換します。
                        .replace("\"", "&quot;").replace("'", "&#39;"); // ★ 引用符も文字として表示します。
    } // ★ 文字の変換はここまでです。

    static void redirect(HttpExchange exchange) throws IOException { // ★ 共通の一覧への戻り処理です。
        exchange.getResponseHeaders().set("Location", "/"); // ★ 戻り先を指定します。
        exchange.sendResponseHeaders(303, -1); // ★ 本文なしの303を返します。
        exchange.close(); // ★ 通信を閉じます。
    } // ★ 一覧への戻り処理はここまでです。

    static void send(HttpExchange exchange, int status, String type, String text) throws IOException { // ★ UTF-8で応答します。
        byte[] body = text.getBytes(StandardCharsets.UTF_8); // ★ 応答をUTF-8に変換します。
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8"); // ★ 応答の形式を伝えます。
        try { // ★ 送信に失敗した場合も通信を閉じます。
            exchange.sendResponseHeaders(status, body.length); // ★ 状態と本文の長さを伝えます。
            exchange.getResponseBody().write(body); // ★ 本文を送信します。
        } finally { // ★ 送信後に必ず実行します。
            exchange.close(); // ★ 通信を閉じます。
        } // ★ 送信後の処理はここまでです。
    } // ★ 応答処理はここまでです。
} // プログラム全体のまとまりはここまでです。
