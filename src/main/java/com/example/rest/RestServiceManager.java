package com.example.rest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestServiceManager {

	private static Map<String, Class<? extends IRestService>> classes = new HashMap<String, Class<? extends IRestService>>();
	
	public static Class<? extends IRestService> getServiceClass(String service) {
		if(classes.containsKey(service))
			return classes.get(service);
		
		return null;
	}

	private static void addPackage(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		for (File directory : dirs) {
			addPackageClasses(directory, packageName);
		}
	}

	@SuppressWarnings("unchecked")
	private static void addPackageClasses(File directory, String packageName) throws ClassNotFoundException {
		if (!directory.exists()) {
			return;
		}
		
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				addPackageClasses(file, packageName + "." + file.getName());
			} else if (file.getName().endsWith(".class")) {
				Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
				if(IRestService.class.isAssignableFrom(clazz)) {
					classes.put(IRestService.getName(clazz), (Class<? extends IRestService>) clazz);
				}
			}
		}
	}

	public static void setClasses(Map<String, Class<? extends IRestService>> classMap) {
		classes = classMap;
	}

	public static Map<String, Class<? extends IRestService>> getClasses() {
		return classes;
	}

	public static void setServicesPackages(String servicesPackages) throws ClassNotFoundException, IOException {
		String[] packages = servicesPackages.replaceAll(" ", "").split(",");
		for (String packageName : packages) {
			addPackage(packageName); 
		}
	}

	@SuppressWarnings("unchecked")
	public static void setServices(String services) {
		String[] classNames = services.replaceAll(" ", "").split(",");
		for (String className : classNames) {
			Class<?> clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				System.err.println(e);
				continue;
			}
			if(IRestService.class.isAssignableFrom(clazz)) {
				classes.put(IRestService.getName(clazz), (Class<? extends IRestService>) clazz);
			}
		}
	}
}
