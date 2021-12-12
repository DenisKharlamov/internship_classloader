package com.deniskharlamov.internship_classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MyClassLoader extends ClassLoader{

	// кэш
	private Map<String, Class<?>> classesCache = new HashMap<>();
	
	// набор путей поиска
	public String[] classPath;
	
	
	public MyClassLoader(String[] classPath) throws ClassNotFoundException {
		this.classPath = classPath;
	}
	
	/*
	 * Реализация метода loadClass() действующего наоборот: вначале
	 * пытается загрузить .class файл самостоятельно далее 
	 * обращается к системному загрузчику
	 */
	@Override
	public synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Class<?> result = findClass(name);
		if (resolve) {
			resolveClass(result);
		}
		return result;
	}
	
	/*
	 * Метод findClass загружает байт-код указанного класса(чтение файла)
	 * после чего для полученного массива байтов выполняется вызов
	 * метода defineClass()
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// смотрим в кэше
		Class<?> result = classesCache.get(name);
		if (result != null) {
			System.out.println("Class " + name + " found in cache.");
			return result;
		}
		// преобразуем полное квалифицированное имя класса в путь
		// к файлу и пробуем найти файл
		File file = findFile(name.replace(".", "/"), ".class");
		System.out.println("Class " + name + (
				file == null?"":" found in " + file));
		if (file == null) {
			/*
			 * В случае неудачи с поиском файла обращаемся к системному 
			 * загрузчику.
			 * findSystemClass() метод абстрактного класса ClassLoader
			 * с объявлением protected Class findSystemClass(String name)
			 * предназначенный для использования в наследниках и не
			 * подлежащий переопределению. Он выполняет поиск и 
			 * загрузку класса по алгоритму системного загрузчика.
			 */
			return findSystemClass(name);
		}
		
		try {
			byte[] classBytes = loadFileAsBytes(file);
			/*
			 * Метод defineClass реализован в native-коде. Именно он 
			 * помещает байт-код класса в недра виртуальной машины, 
			 * где он приобретает вид, пригодный для непосредственного 
			 * исполнения на конкретной аппаратной платформе, в частности, 
			 * компилируется в машинный код процессора для более быстрого 
			 * исполнения (так называемая технология Just-In-Time, 
			 * сокращенно JIT-компиляция).
			 */
			result = defineClass(
					 name, classBytes, 0, classBytes.length);
		} catch (IOException ex) {
			throw new ClassNotFoundException(
					"Cannot load class " + name + ": " + ex);
		} catch (ClassFormatError ex) {
			throw new ClassNotFoundException(
					"Format of class file incorrect for class " + name + ": " + ex);
		}
		// добавляем в кэш
		classesCache.put(name, result);
		return result;
	}
	
	/*
	 * Метод findResource должен просто найти файл, соответствующий 
	 * данному ресурсу – по тем же правилам, по которым отыскивается 
	 * файл класса в методе findClass() – и вернуть ссылку на него 
	 * в виде URL.
	 * Метод необходимо переопределить т.к. возможно придется 
	 * использовать загрузчик с каталогами, неизвестными системному
	 * загрузчику(отличными от каталога CLASSPATH)
	 */
	@Override
	protected URL findResource(String name) {
		File file = findFile(name, "");
		if (file == null) {
			return null;
		}
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException ex) {
			return null;
		}
	}
	
	/*
	 * Поиск файла с именем name и расширением extension
	 * в каталогах поиска classPath
	 */
	private File findFile(String name, String extension) {
		File file;
		for (int i = 0; i < classPath.length; i++) {
			file = new File(classPath[i] + File.separatorChar + name + extension);
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}
	
	public byte[] loadFileAsBytes(File file) 
			throws IOException {
		byte[] result = new byte[(int)file.length()];
		try (FileInputStream fis = 
				new FileInputStream(file)) {
			fis.read(result, 0, result.length);
		}
		return result;
	}
}







