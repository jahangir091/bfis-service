//package eu.flora.faobis;
//
//import java.io.InputStream;
//import java.util.List;
//
//import junit.framework.TestCase;
//
//public class TestQueryByClassFullPrototype extends TestCase {
//
//    public void test() {
//	InputStream legend = FullPrototypeQuery.class.getClassLoader().getResourceAsStream("legend.xml");
//	InputStream clazz = FullPrototypeQuery.class.getClassLoader().getResourceAsStream("class.xml");
//	LCMLClass lcmlClazz = new LCMLClass(clazz);
//	lcmlClazz.print();
//	InputStream clazzB = FullPrototypeQuery.class.getClassLoader().getResourceAsStream("class.xml");
//	LCMLClass lcmlClazzB = new LCMLClass(clazzB);
//	InputStream clazz2 = FullPrototypeQuery.class.getClassLoader().getResourceAsStream("class2.xml");
//	LCMLClass lcmlClazz2 = new LCMLClass(clazz2);
//	InputStream clazz3 = FullPrototypeQuery.class.getClassLoader().getResourceAsStream("class3.xml");
//	LCMLClass lcmlClazz3 = new LCMLClass(clazz3);
//
//	assertTrue(lcmlClazz.fullMatch(lcmlClazzB, false));
//	assertTrue(lcmlClazz.fullMatch(lcmlClazzB, true));
//	assertTrue(lcmlClazzB.fullMatch(lcmlClazz, false));
//	assertTrue(lcmlClazzB.fullMatch(lcmlClazz, true));
//
//	assertTrue(lcmlClazz.fullMatch(lcmlClazz2, false));
//	assertTrue(lcmlClazz.fullMatch(lcmlClazz2, true));
//	assertTrue(lcmlClazz2.fullMatch(lcmlClazz, false));
//	assertFalse(lcmlClazz2.fullMatch(lcmlClazz, true));
//
//	assertTrue(lcmlClazz.fullMatch(lcmlClazz3, false));
//	assertTrue(lcmlClazz.fullMatch(lcmlClazz3, true));
//	System.out.println();
//	System.out.println();
//	assertFalse(lcmlClazz3.fullMatch(lcmlClazz, false));
//	assertFalse(lcmlClazz3.fullMatch(lcmlClazz, true));
//
//	FullPrototypeQuery ftq = new FullPrototypeQuery(legend);
//	List<LCMLClass> legendClasses = ftq.getClasses();
//	for (LCMLClass legendClass : legendClasses) {
//	    System.out.println("Legend class id " + legendClass.getId() + " map code: " + legendClass.getMapCode());
//	    boolean match = lcmlClazz.fullMatch(legendClass, true);
//	    System.out.println("Result: " + match);
//	}
//
//    }
//
//}
