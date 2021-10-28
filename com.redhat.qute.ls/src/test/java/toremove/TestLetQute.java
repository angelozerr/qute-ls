package toremove;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

public class TestLetQute {

	public static void main(String[] args) {

		Map<String, Object> data = new HashMap<>();
		data.put("items", Arrays.asList("a", "b"));
		
		Engine engine = Engine.builder().addDefaults().build();
		
		Template template = engine.parse(convertStreamToString(TestLetQute.class.getResourceAsStream("let.qute")));
		String s = template.data(data).render();

		System.err.println(s);

	}

	private static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}
