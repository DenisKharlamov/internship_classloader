package com.deniskharlamov.internship_classloader;

public class TestLoader {

	public static void main(String[] args) throws Exception {
		
		// создаем загрузчик и указываем classpath
		MyClassLoader loader = new MyClassLoader( 
				new String[] {"/home/deniskharlamov/eclipse-workspace"
						+ "/internship_classloader/src/main/java"});
		
		// загружаем класс(он лежит уже скомпилированный
		// в своем пакете)
		Class<?> clazz = loader.loadClass(
				"com.deniskharlamov.internship_classloader.SimpleInterfaceImpl");
		/*
		 * Интерфейс должен загружаться системным загрузчиком, классы
		 * загружаемые разными загрузчиками для системы разные. Если
		 * интерфейс будет загружаться с промощью созданного загрузчика,
		 * то его нельзя будет использовать в классах, загружаемых
		 * системным загрузчиком --> ClassCastException
		 */
		SimpleInterface testClass = (SimpleInterface) clazz.newInstance();
		System.out.println(testClass);
		/*
		 * Если класс загружается сторонним загрузчиком, то и все классы
		 * на которые он ссылается будут загружаться этим же загрузчиком.
		 * Также имеется возможность указать загрузчик как базовый для
		 * всего приложения через параметр командной строки:
		 * 		-Djava.system.class.loader
		 * и загрузчик станет загрузчиком по умолчанию.
		 */
	}

}
