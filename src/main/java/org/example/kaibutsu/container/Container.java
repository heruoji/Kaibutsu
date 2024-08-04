package org.example.kaibutsu.container;

import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.downloader.DynamicDownloader;
import org.example.kaibutsu.core.downloader.StaticDownloader;
import org.example.kaibutsu.core.magatamapipeline.MagatamaPipeline;
import org.example.kaibutsu.core.magatamapipeline.PrintPipeline;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Container {

    public static Tsuchigumo buildTsuchigumo(String targetPackage, String tsuchigumoName) {
        Set<Class<? extends Tsuchigumo>> tsuchigumoClasses = getTsuchigumoClasses(targetPackage);
        for (Class<? extends Tsuchigumo> clazz : tsuchigumoClasses) {
            if (clazz.getSimpleName().equals(tsuchigumoName)) {
                return instantiateTsuchigumo(clazz);
            }
        }
        throw new ContainerException("指定された名前のTsuchigumoが見つかりませんでした。名前：" + tsuchigumoName);
    }

    private static Set<Class<? extends Tsuchigumo>> getTsuchigumoClasses(String targetPackage) {
        return ClassFinder.getSubClasses(targetPackage, Tsuchigumo.class);
    }

    private static Tsuchigumo instantiateTsuchigumo(Class<?> clazz) {
        try {
            return (Tsuchigumo) clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ContainerException("Tsuchigumoの初期化に失敗しました。", e);
        }
    }

    public static Downloader buildDownloader(boolean usePlaywright) {
        if (usePlaywright) {
            return new DynamicDownloader();
        } else {
            return new StaticDownloader();
        }
    }

    public static List<MagatamaPipeline> buildMagatamaPipelines(String targetPackage, List<String> names) {
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        List<MagatamaPipeline> magatamaPipelines = new ArrayList<>();
        for (String name : names) {
            MagatamaPipeline magatamaPipeline = buildMagatamaPipeline(targetPackage, name);
            magatamaPipelines.add(magatamaPipeline);
        }
        return magatamaPipelines;
    }

    private static MagatamaPipeline buildMagatamaPipeline(String targetPackage, String name) {
        if (name.equals("PrintPipeline")) {
            return new PrintPipeline();
        }
        Set<Class<? extends MagatamaPipeline>> magatamaPipelineClasses = getMagatamaPipelineClasses(targetPackage);
        for (Class<? extends MagatamaPipeline> clazz : magatamaPipelineClasses) {
            if (clazz.getSimpleName().equals(name)) {
                return instantiateMagatamaPipeline(clazz);
            }
        }
        throw new ContainerException("指定された名前のMagatamaPipelineが見つかりませんでした。名前：" + name);
    }

    private static Set<Class<? extends MagatamaPipeline>> getMagatamaPipelineClasses(String targetPackage) {
        return ClassFinder.getSubClasses(targetPackage, MagatamaPipeline.class);
    }

    private static MagatamaPipeline instantiateMagatamaPipeline(Class<?> clazz) {
        try {
            return (MagatamaPipeline) clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ContainerException("MagatamaPipelineの初期化に失敗しました", e);
        }
    }

}
