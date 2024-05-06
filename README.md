# Kaibutsu - A Java Web Crawler Framework

## 概要
Kaibutsuは、Javaで開発された高性能Webクローラーフレームワークです。Scrapyからインスピレーションを得て、柔軟なスケジューリング、エラーハンドリング、プラグイン式のコンポーネントを特徴としています。

## 主な特徴
- **非同期・並列処理:** Reactorを使用した非同期処理をサポートし、高いスループットを実現します。
- **プラグインアーキテクチャ:** ダウンローダー、スケジューラー、パーサー（土蜘蛛）、抽出データ（勾玉）、抽出データパイプライン（勾玉パイプライン）など、多様なコンポーネントをカスタマイズ可能です。

## アーキテクチャの詳細
### コンポーネント概要
Kaibutsuは複数のコアコンポーネントによって構成されています:
- **アプリケーションクラス (`Kaibutsu`)**: クローリングプロセスの起点として機能し、設定ファイルを読み込んで`GodzillaEngine`を初期化、実行を開始します。
- **エンジンクラス (`GodzillaEngine`)**: クローリング操作の中核を担い、ダウンローダー、スケジューラー、パーサー、データパイプラインの調整を行います。
- **ダウンローダークラス (`Downloader`)**: HTTPリクエストを非同期に実行し、Webページのレスポンスを取得します。
- **スケジューラークラス (`Scheduler`)**: HTTPリクエストの管理とスケジューリングを担当し、ダウンローダーにHTTPリクエストを送信します。
- **パーサークラス（`Tsuchigumo`)**: ダウンローダーで取得したHTTPレスポンスからデータを解析・抽出し、必要に応じて新たなHTTPリクエストを生成します。
- **データクラス（`Magatama`）**: パーサークラスで抽出するデータクラスを表します。
- **データパイプラインクラス (`MagatamaPipeline`)**: パーサークラスで抽出したデータを受け取り、必要に応じてさらなる処理を行います。

### データフロー
1. **初期化**: `GodzillaEngine`とそのコンポーネントが初期化されます。
2. **リクエストの生成とスケジューリング**: 土蜘蛛が最初のリクエストを生成し、スケジューラーに送ります。
3. **ダウンロード**: スケジューラーがダウンローダーにリクエストを送信し、レスポンスを取得します。
4. **解析**: 土蜘蛛が上記レスポンスを解析してデータを抽出し、新たなリクエストを生成します。
5. **データ処理**: 上記で抽出されたデータはデータパイプラインで処理されます。
6. **終了**: 全リクエストの処理が完了すると、エンジンはシャットダウンします。

## 実装例
以下は、[`quotes.toscrape.com`](https://quotes.toscrape.com/)というサイトから著者情報を収集するためのカスタムクラスの例です

### カスタム土蜘蛛クラス
```java
package org.example.kaibutsu.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;
import org.example.kaibutsu.core.tsuchigumo.TsuchigumoResponse;
import org.example.kaibutsu.magatama.Author;

import java.util.List;
import java.util.stream.Collectors;

public class QuoteTsuchigumo implements Tsuchigumo {
    public Request startRequest() {
        return new Request("https://quotes.toscrape.com", "parseMain");
    }

    public TsuchigumoResponse parseMain(Response response, TsuchigumoResponse.TsuchigumoResponseBuilder builder) {
        List<Request> authorRequests = response.select(".author + a").stream().map(link -> new Request(link.absUrl("href"), "parseAuthor")).collect(Collectors.toList());
        List<Request> paginationRequests = response.select("li.next a").stream().map(link -> new Request(link.absUrl("href"), "parseMain")).toList();
        authorRequests.addAll(paginationRequests);

        return builder.requests(authorRequests).build();
    }

    public TsuchigumoResponse parseAuthor(Response response, TsuchigumoResponse.TsuchigumoResponseBuilder builder) {
        String name = response.select("h3.author-title").text();
        String birthday = response.select(".author-born-date").text();
        String bio = response.select(".author-description").text();
        Author author = new Author();
        author.name = name;
        author.birthday = birthday;
        author.bio = bio;
        return builder.addMagatama(author).build();
    }
}
```

### カスタム勾玉クラス
```java
package org.example.kaibutsu.magatama;

import org.example.kaibutsu.core.tsuchigumo.Magatama;

public class Author implements Magatama {
    public String name;
    public String birthday;
    public String bio;

    @Override
    public String toString() {
        return String.format("{ name : %s, birthday : %s, bio : %s }", name, birthday, bio);
    }
}
```

### カスタムデータパイプラインクラス
```java
package org.example.kaibutsu.magatamapipeline;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.kaibutsu.core.magatamapipeline.MagatamaPipeline;
import org.example.kaibutsu.core.tsuchigumo.Magatama;
import org.example.kaibutsu.magatama.Author;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class WriteCsvAuthor implements MagatamaPipeline {
    private CSVPrinter csvPrinter;

    @Override
    public void open() {
        try {
            Writer writer = new FileWriter("authors.csv");
            this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("name", "birthday", "bio"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            csvPrinter.flush();
            this.csvPrinter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Magatama processMagatama(Magatama magatama) {
        try {
            Author author = (Author) magatama;
            csvPrinter.printRecord(author.name, author.birthday, author.bio);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return magatama;
    }
}
```

### 実行方法
設定ファイル`quote.properties`をresourcesディレクトリに用意します。
```
tsuchigumo=org.example.kaibutsu.tsuchigumo.QuoteTsuchigumo
dynamic=false
magatamaPipelines=PrintPipeline,WriteCsvAuthor
interval=1000
retryCount=3
tsuchigumoPackage=org.example.kaibutsu.tsuchigumo
magatamaPipelinesPackage=org.example.kaibutsu.magatamapipeline
```
この設定により、`QuoteTsuchigumo`クラスが最初のリクエストを生成し、`https://quotes.toscrape.com`からデータを取得し始めます。取得したデータは`Author`クラスのインスタンスとして処理され、最終的にCSVファイルに保存されます。

Kaibutsuを使用してクローリングを開始するには、上記のカスタムクラスと設定ファイルを準備し、`Kaibutsu`のmainメソッドを実行します。
引数では、設定ファイルの名前（上記例の場合、`quote`）を指定してください。
