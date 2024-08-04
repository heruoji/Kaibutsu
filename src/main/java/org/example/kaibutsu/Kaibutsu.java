package org.example.kaibutsu;

import org.example.kaibutsu.config.Config;
import org.example.kaibutsu.config.ConfigLoader;
import org.example.kaibutsu.container.Container;
import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.engine.GodzillaEngine;
import org.example.kaibutsu.core.magatamapipeline.MagatamaPipeline;
import org.example.kaibutsu.core.scheduler.Scheduler;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;

import java.util.Arrays;
import java.util.List;

public class Kaibutsu {
    public static void main(String[] args) {
        run(args[0]);
    }

    public static void run(String config) {
        if (config.isEmpty()) {
            throw new IllegalArgumentException("設定ファイル名が指定されていません。");
        }
        try {
            GodzillaEngine godzillaEngine = initializeEngine(config);
            godzillaEngine.run();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.err.println("エラーが発生しました: " + e.getMessage());
        }
    }

    private static GodzillaEngine initializeEngine(String configName) {
        Config config = ConfigLoader.load(configName);
        Scheduler scheduler = new Scheduler(config.intervalMillSeconds);
        Downloader downloader = Container.buildDownloader(config.dynamic);
        Tsuchigumo tsuchigumo = Container.buildTsuchigumo(config.tsuchigumoPackage, config.tsuchigumo);
        List<MagatamaPipeline> magatamaPipelines = Container.buildMagatamaPipelines(config.magatamaPipelinesPackage, Arrays.asList(config.magatamaPipelines));

        return new GodzillaEngine(scheduler, downloader, tsuchigumo, magatamaPipelines);
    }
}
