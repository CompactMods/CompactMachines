package dev.compactmods.machines.forge.util;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class AnnotationScanner {

    private static final Logger SCANNER_LOG = LogManager.getLogger();

    public static <T> Stream<ModFileScanData.AnnotationData> scanModList(Class<T> type) {
        return ModList.get()
                .getAllScanData()
                .stream()
                .flatMap(scans -> scans.getAnnotations()
                        .stream()
                        .filter(ad -> ad.annotationType().equals(Type.getType(type)))
                );
    }

    public static <T, Annote extends Annotation> Stream<Field> scanFields(T instance, Class<Annote> annotationType) {
        var parent = instance.getClass();
        return Arrays.stream(parent.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotationType));
    }

    public static <T, Annote extends Annotation> Stream<Method> scanMethods(T instance, Class<Annote> annotationType) {
        var parent = instance.getClass();
        return Arrays.stream(parent.getDeclaredMethods())
                .filter(meth -> meth.isAnnotationPresent(annotationType));
    }

    public static <Target, Value> void injectFields(Target target, Value val, Set<Field> injectableFields) {
        injectableFields.stream()
                .filter(field -> field.getType().isAssignableFrom(val.getClass()))
                .forEach(field -> {
                    try {
                        field.set(target, val);
                    } catch (IllegalAccessException e) {
                        SCANNER_LOG.error("Failed to inject lookup data {} into addon {}", field.getName(), target.getClass().getName());
                        SCANNER_LOG.error(e);
                    }
                });
    }
}
