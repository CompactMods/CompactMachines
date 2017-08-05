package org.dave.compactmachines3.utility;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.dave.compactmachines3.integration.CapabilityNullHandler;
import org.dave.compactmachines3.integration.AbstractNullHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnnotatedInstanceUtil {
	private AnnotatedInstanceUtil() {
	}

	public static List<AbstractNullHandler> getNullHandlers(ASMDataTable asmDataTable) {
		return getInstances(asmDataTable, CapabilityNullHandler.class, AbstractNullHandler.class);
	}

	private static <T> List<T> getInstances(ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
		String annotationClassName = annotationClass.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		List<T> instances = new ArrayList<T>();
		for (ASMDataTable.ASMData asmData : asmDatas) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				T instance = asmInstanceClass.newInstance();
				instances.add(instance);
			} catch (ClassNotFoundException e) {
				Logz.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (IllegalAccessException e) {
				Logz.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (InstantiationException e) {
				Logz.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (ExceptionInInitializerError e) {
				Logz.error("Failed to load: {}", asmData.getClassName(), e);
			}
		}
		return instances;
	}
}
