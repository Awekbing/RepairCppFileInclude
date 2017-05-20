package com.awebing.cfile;

public class Test {

	public static void main(String[] args) {
		foo();
	}
	
	public static int foo() {
		for (int i = 0; i < 10; i++) {
			if(i == 4) {
				return i;
			}
			System.out.println("time:" + i);
			
		}
		return 0;
	}
}
