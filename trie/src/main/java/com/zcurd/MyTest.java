package com.zcurd;

import com.jfinal.kit.HttpKit;
import java.util.HashMap;

public class MyTest {
	public static void main(String[] args) {
		final HashMap<String, String> param = new HashMap<>();
		param.put("user_name", "xxx");
		param.put("password", "111111");

		for (int i = 0; i < 20; i++) {
			(new Thread(new Runnable() {
				public void run() {
					String string = HttpKit.get("http://localhost:8087/Lab0819/login/login", param);
					System.out.println(string);
				}
			})).start();
		}
	}
}
