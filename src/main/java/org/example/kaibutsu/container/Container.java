package org.example.kaibutsu.container;

import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.downloader.DynamicDownloader;
import org.example.kaibutsu.core.downloader.StaticDownloader;
import org.example.kaibutsu.core.itempipeline.ItemPipeline;
import org.example.kaibutsu.core.itempipeline.Printer;
import org.example.kaibutsu.core.parser.Parser;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Container {

    public static Parser buildParser(String targetPackage, String parserName) {
        Set<Class<? extends Parser>> parserClasses = getParserClasses(targetPackage);
        for (Class<? extends Parser> clazz : parserClasses) {
            if (clazz.getSimpleName().equals(parserName)) {
                return instantiateParser(clazz);
            }
        }
        throw new ContainerException("指定された名前のParserが見つかりませんでした。名前：" + parserName);
    }

    private static Set<Class<? extends Parser>> getParserClasses(String targetPackage) {
        return ClassFinder.getSubClasses(targetPackage, Parser.class);
    }

    private static Parser instantiateParser(Class<?> clazz) {
        try {
            return (Parser) clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ContainerException("Parserの初期化に失敗しました。", e);
        }
    }

    public static Downloader buildDownloader(boolean usePlaywright) {
        if (usePlaywright) {
            return new DynamicDownloader();
        } else {
            return new StaticDownloader();
        }
    }

    public static List<ItemPipeline> buildItemPipelines(String targetPackage, List<String> names) {
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemPipeline> itemPipelines = new ArrayList<>();
        for (String name : names) {
            ItemPipeline itemPipeline = buildItemPipeline(targetPackage, name);
            itemPipelines.add(itemPipeline);
        }
        return itemPipelines;
    }

    private static ItemPipeline buildItemPipeline(String targetPackage, String name) {
        if (name.equals("Printer")) {
            return new Printer();
        }
        Set<Class<? extends ItemPipeline>> itemPipelineClasses = getItemPipelineClasses(targetPackage);
        for (Class<? extends ItemPipeline> clazz : itemPipelineClasses) {
            if (clazz.getSimpleName().equals(name)) {
                return instantiateItemPipeline(clazz);
            }
        }
        throw new ContainerException("指定された名前のItemPipelineが見つかりませんでした。名前：" + name);
    }

    private static Set<Class<? extends ItemPipeline>> getItemPipelineClasses(String targetPackage) {
        return ClassFinder.getSubClasses(targetPackage, ItemPipeline.class);
    }

    private static ItemPipeline instantiateItemPipeline(Class<?> clazz) {
        try {
            return (ItemPipeline) clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ContainerException("ItemPipelineの初期化に失敗しました", e);
        }
    }

}
