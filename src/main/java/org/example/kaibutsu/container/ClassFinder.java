package org.example.kaibutsu.container;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class ClassFinder {
    public static <T> Set<Class<? extends T>> getSubClasses(String packageName, Class<T> superClass) {
        ConfigurationBuilder configuration = new ConfigurationBuilder();
        if (packageName == null || packageName.trim().isEmpty()) {
            configuration.setUrls(ClasspathHelper.forJavaClassPath());  // 全クラスパスをスキャン
        } else {
            configuration.setUrls(ClasspathHelper.forPackage(packageName));  // 指定されたパッケージをスキャン
        }
        Reflections reflections = new Reflections(configuration);
        return reflections.getSubTypesOf(superClass);
    }
}
