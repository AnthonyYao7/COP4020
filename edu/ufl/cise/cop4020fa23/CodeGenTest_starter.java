package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.DynamicClassLoader;
import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.DynamicCompiler;
import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CodeGenTest_starter {
	

	@AfterEach
	public void separatingLine(){
		show("----------------------------------------------");
	}

	// makes it easy to turn output on and off (and less typing than System.out.println)
	static final boolean VERBOSE = true;


	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}
	
	/**This is a test of dynamic compilation and execution.  We hard code a legal Java class. **/
	@Test
	void test() throws Exception {
		String code = """
				public class Class1 {
				   public static int f(int x){
				     return x+1;
				   }
				 }
				""";
		String name = "Class1";
		byte[] byteCode = DynamicCompiler.compile(name, code);
		//Load generated classfile and execute its apply method.
		Object[] params = {3};
		Object result = (int) DynamicClassLoader.loadClassAndRunMethod(byteCode, name, "f", params);
		show(result);
		assertEquals(4, (int)result);
	}
	
	@Test
	void cg0() throws Exception {
		String input = "void f()<::>";
		Object result = PLCLangExec.runCode(packageName, input);
		show(result);
		assertNull(result);
	}
	
	@Test
	void cg1() throws Exception {
		String input = """
				int f()<: ^ 3;  :>
				""";
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(3,(int)result);
	}
	
	
	@Test
	void cg2() throws Exception {
		String input = """
				boolean f(boolean false) ##false is an identifier
				<: ^ false; 
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input,  false);
		assertEquals(false, (boolean)result);	
	}	
	
	@Test
	void cg3() throws Exception {
		String input = """
				boolean f(boolean false)
				<: ^ false; 
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input,  true);
		assertEquals(true, (boolean)result);	
	}	
	

	@Test
	void cg4() throws Exception {
		String input = """
				string f(int a, string Hello, boolean b)
				<: 
				write a;
				write Hello;
				write b;
				^ Hello;
				:>
				""";
		Object[] params = {4,"hello",true};
		Object result = PLCLangExec.runCode(packageName,input, 4, "hello", true);
		show(result);
		assertEquals("hello", result);		
	}	
	
	
	@Test
	void cg5() throws Exception {
		String input = """
				int f(int a)
				<:
				write a;
				^a+1;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,input, 4);
		assertEquals(5,(int)result);		
	}	
	
	@Test
	void cg6() throws Exception {
		String input = """
				int f(int a, int b)
				<:
				^ a ** b;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,input, 3, 2);
		show(result);
		assertEquals(9,(int)result);			
	}
	
	String packageName = "edu.ufl.cise.cop4020fa23";
	@Test
	void cg7() throws Exception {
		String input = """
				int Example(int x, int y)
				<: 
				^x+y;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input, 4,5);
		show(result);
		assertEquals(9,(int) result);	

	}	
	
	@Test 
	void cg8() throws Exception {
		String source = """
				int f(int a)
				<:
				^ -a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 10);
		show(result);
		assertEquals(-10,(int)result);
	}
	
	@Test 
	void cg9() throws Exception {
		String source = """
				int f(int a)
				<:
				^ -a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, -10);
		show(result);
		assertEquals(10,(int)result);
	}
		
	@Test 
	void cg10() throws Exception {
		String source = """
				int f(int a)
				<:
				^ --a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 10);
		show(result);
		assertEquals(10,(int)result);
	}
	
	@Test 
	void cg110() throws Exception {
		String source = """
				boolean f(boolean a)
				<:
				^ !a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, true);
		show(result);
		assertEquals(false,(boolean)result);
	}
	
	@Test 
	void cg12() throws Exception {
		String source = """
				boolean f(boolean a)
				<:
				^ !!a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, true);
		show(result);
		assertEquals(true,(boolean)result);
	}
	
	@Test
	void cg13() throws Exception {
		String source = """
				int a(int i)
				<:
				  int r = ? i>0 -> i , -i;
				  ^r;
				  :>
				  """;
		Object result = PLCLangExec.runCode(packageName, source, 42);
		show(result);
		assertEquals(42,(int)result);
	}
	
	@Test
	void cg14() throws Exception {
		String source = """
				int a(int i)
				<:
				  int r = ? i>0 -> i , -i;
				  ^r;
				  :>
				  """;
		Object result = PLCLangExec.runCode(packageName, source, -42);
		show(result);
		assertEquals(42,(int)result);
}
	
	@Test
	void cg15() throws Exception {
		String source = """
				int f(int a)
				<:
				int b;
				b = a;
				^b;
				:>
				""";
		int val = 34;
		Object result =  PLCLangExec.runCode(packageName,source, val);
		show(result);
		assertEquals(val,(int)result);
	}
	
	@Test
	void cg16() throws Exception {
		String source = """
				int f(int a)
				<:
				int b;
				b = -a;
				^b;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,source, 22);
		show(result);
		assertEquals(-22,(int)result);
	}
		
	@Test
	void cg17() throws Exception {
		String source = """
				boolean f(boolean a)
				<:
				boolean b;
				b = !a;
				^b;
				:>
				""";
		boolean val = true;
		Object result = PLCLangExec.runCode(packageName,source, val);
		show(result);
		assertEquals(!val, (boolean)result);
	}
	
	@Test
	void cg18() throws Exception {
		String source = """
				int f()
				<:
				  int a = 1;
				  int b;
				  <: 
				     int a = 2;
				     <: 
				         int a = 3;
				         b=a;
				     :>;
				  :>;
				  ^b;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(3, (int)result);
	}
	
	@Test
	void cg19() throws Exception {
		String source = """
				int f()
				<:
				  int a = 1;
				  int b;
				  <: 
				     int a = 2;
				     <: 
				         int a = 3;
				        
				     :>;
				      b=a;
				  :>;
				  ^b;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(2, (int)result);
	}
		
	@Test
	void cg20() throws Exception {
		String source = """
				int f()
				<:
				  int a = 1;
				  int b;
				  <: 
				     int a = 2;
				     <: 
				         int a = 3;
                    :>;			      
				  :>;
				  b=a;
				  ^b;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(1, (int)result);
	}
		
	@Test
	void cg21() throws Exception {
		String source = """
				string concatWithSpace(string a, string b)
				<:
				^ a + " " + b;
				:>
				""";
		String a = "Go";
	    String b = "Gators!";
		Object result = PLCLangExec.runCode(packageName,source,a,b);
		show(result);
		assertEquals(a + " " + b, result);		
	}

	@Test
	void testSetup() throws Exception {
		String javaCode = """
               package edu.ufl.cise.cop4020fa23;
               import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
               public class Three{
                  public static void apply(){
                    ConsoleIO.write(3);
                  }
                }
		""";
		String packageName = "edu.ufl.cise.cop4020fa23";
		String fullyQualifiedName = "edu.ufl.cise.cop4020fa23.Three";
		Object[] params = {};
		//Invoke Java compiler to obtain classfile
		byte[] byteCode = DynamicCompiler.compile(fullyQualifiedName, javaCode);
		//Load generated classfile and execute its "apply" method.
		Object result = DynamicClassLoader.loadClassAndRunMethod(byteCode, fullyQualifiedName, "apply", params);
		assertNull(result);
	}
}
