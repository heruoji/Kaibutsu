package org.example.kaibutsu.application;

import org.example.kaibutsu.application.config.Config;
import org.example.kaibutsu.application.config.ConfigLoader;
import org.example.kaibutsu.container.Container;
import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.engine.Engine;
import org.example.kaibutsu.core.itempipeline.ItemPipeline;
import org.example.kaibutsu.core.scheduler.Scheduler;
import org.example.kaibutsu.core.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Kaibutsu {
    private static final Logger logger = LoggerFactory.getLogger(Kaibutsu.class);

    public static void main(String[] args) {
        run(args[0]);
    }

    public static void run(String config) {
        if (config.isEmpty()) {
            logger.error("設定ファイル名が指定されていません。プログラムを実行するには有効な設定ファイル名を引数に指定してください。");
            throw new IllegalArgumentException("設定ファイル名が指定されていません。");
        }
        try {
            Engine engine = initializeEngine(config);
            engine.run();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            logger.error("エンジンの初期化または実行中にエラーが発生しました: ", e);
        }
    }

    private static Engine initializeEngine(String configName) {
        Config config = ConfigLoader.load(configName);
        Scheduler scheduler = new Scheduler(config.intervalMillSeconds);
        Downloader downloader = Container.buildDownloader(config.dynamic);
        Parser parser = Container.buildParser(config.parserPackage, config.parser);
        List<ItemPipeline> itemPipelines = Container.buildItemPipelines(config.itemPipelinesPackage, Arrays.asList(config.itemPipelines));

        return new Engine(scheduler, downloader, parser, itemPipelines);
    }
}
