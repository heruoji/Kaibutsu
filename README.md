## モジュール構成
Kaibutsuフレームワークは、以下の主要なモジュールで構成されています：
- Applicationモジュール：エントリーポイントや設定の読み込み、エンジンの初期化を担当。
- Containerモジュール：依存性注入（DI）コンテナとして機能し、主要なコンポーネントを動的にインスタンス化。
- Coreモジュール：実際のクローリング処理を行うメインのモジュール。

全体のパッケージ構成は、以下になります。
```
├── application/
├── container/
└── core/
    ├── downloader/
    ├── engine/
    ├── magatamapipeline/
    ├── scheduler/
    └── tsuchigumo/
```

## Coreモジュールのアーキテクチャ
Coreモジュールは、実際にクロール処理を行う中心のモジュールで、以下のコンポーネントから構成されています。
- Engine：各コンポーネントを制御してクローリング処理を行います。
- Downloader：指定されたURLのWebページを取得します。静的ページ取得用のStaticDownloaderと、動的ページ取得用のDynamicDownloaderを用意しました。
- Scheduler：Downlaoderへ渡すリクエストの管理を行います。
- Parser：Downloaderが取得したWebページから、データと新しいリクエストをスクレイプします。
- ItemPipeline：取得したデータの後処理を行います。データのクリーニングやファイルへの出力などを行うことができます。

## クロール処理の流れ
Coreモジュールのクロール処理全体の流れは、以下になります。

1. EngineがParserで指定した最初のリクエストを、Schedulerに渡します。
2. Schedulerはリクエストを管理し、指定された間隔でEngineに渡します。Engineは、Downloaderにリクエストを渡します。
3. リクエストを受け取ったDownloaderは、Webページを取得して、Engineに返します。
4. Engineは、Downlaoderから受け取ったWebページをParserに渡します。
5. Parserは、Webページからデータ（item）と新しいクロール先のURLをスクレイプします。それらをまとめてEngineに返します。
6. EngineはParserから受け取ったスクレイプ結果のうち、新しいリクエストをSchedulerに渡し、データをItemPipelineに渡します。
7. ItemPipelineでは、DB保存やファイル出力など任意のデータ後処理を行います。
8. Schedulerからリクエストがなくなるまで、2に戻ります。

フレームワーク利用者側でParser、Item、ItemPipelineの実装クラスを用意することで、任意のWebサイトをクロールすることができます。

## フレームワーク利用方法
例として、https://quotes.toscrape.com/　に載っている著者の名言をスクレイプし、ファイルに出力する処理を行いたいと思います。
フレームワーク利用者側で以下のファイルを用意する必要があります。
1. Parser実装クラス
2. Item実装クラス（抽出したいデータのDTO）
3. ItemPipeline実装クラス（抽出したデータの後処理の実装クラス）
4. 設定ファイル

### Parser実装クラス
Parser実装クラスでは、実際のWebページに対するスクレイプ処理を実装します。使い方はScrapyにおけるSpiderとほとんど同じです。
startRequestは、クロールを開始するページのDownloaderRequestクラスを返します。
parseMainとparseAuthorはユーザー側で実装したメソッドであり、メソッド名に決まりはありません。DownloaderRequestでクロール対象のURLと、そのページに対して実行するメソッド名を指定します。例えば、startRequestでは、https://quotes.toscrape.com　というページに対して、parseMainメソッドでスクレイプを行うように指定しており、同じようにparseMainメソッドでは、各著者ページのリンクに対して、parseAuthorメソッドを実行するように指定しています。
```java
public class QuoteParser implements Parser {

    public DownloaderRequest startRequest() {
        return new DownloaderRequest("https://quotes.toscrape.com", "parseMain");
    }

    public ParserResponse parseMain(DownloaderResponse downloaderResponse) {
        List<DownloaderRequest> newDownloaderRequests = downloaderResponse.getJsoupElements(".author + a").stream().map(link -> new DownloaderRequest(link.absUrl("href"), "parseAuthor")).collect(Collectors.toList());
        newDownloaderRequests.addAll(downloaderResponse.getJsoupElements("li.next a").stream().map(link -> new DownloaderRequest(link.absUrl("href"), "parseMain")).toList());

        return ParserResponse.fromNewRequests(newDownloaderRequests);
    }

    public ParserResponse parseAuthor(DownloaderResponse downloaderResponse) {
        String name = downloaderResponse.getJsoupElements("h3.author-title").text();
        String birthday = downloaderResponse.getJsoupElements(".author-born-date").text();
        String bio = downloaderResponse.getJsoupElements(".author-description").text();
        Author author = new Author();
        author.name = name;
        author.birthday = birthday;
        author.bio = bio;
        return ParserResponse.fromItems(Collections.singletonList(author));
    }
}
```

### Item実装クラス
Item実装クラスは、取得したいデータのDTOクラスです。今回の場合は、著者の名前、誕生日、紹介文を取得します。
```java
public class Author implements Item {
    public String name;
    public String birthday;
    public String bio;

    @Override
    public String toString() {
        return String.format("{ name : %s, birthday : %s, bio : %s }", name, birthday, bio);
    }
}
```

### ItemPipeline実装クラス
ItemPipeline実装クラスでは、取得したItemに対してどのような後処理を行うかを指定します。フレームワーク側では、Printerという実装クラスを用意しており、コンソールに出力することができます。今回の要件では、csv出力を行いたいため、AuthorCsvWriterクラスを用意します。
初期化処理のopenメソッド、終了時処理のcloseメソッド、データに対する処理のprocessメソッドを実装する必要があります。
```java
public class AuthorCsvWriter implements ItemPipeline {
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
    public Item process(Item item) {
        try {
            Author author = (Author) item;
            csvPrinter.printRecord(author.name, author.birthday, author.bio);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return item;
    }
}
```

### 設定ファイル
設定ファイルでは、以下の項目を指定します。
```
parser=QuoteParser //使用するParser実装クラス
dynamic=false //静的ページを取得するかどうか
itemPipelines=Printer,AuthorCsvWriter //使用するItemPipeline実装クラス
interval=1000 //SchedulerからDownloaderにリクエストを渡す間隔。ミリ秒
parserPackage=org.example.kaibutsu.parser //Parser実装クラスのパッケージ
itemPipelinesPackage=org.example.kaibutsu.itempipeline //ItemPipeline実装クラスのパッケージ
```