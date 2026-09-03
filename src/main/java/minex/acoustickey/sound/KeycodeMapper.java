package minex.acoustickey.sound;

import java.util.HashMap;
import java.util.Map;

public class KeycodeMapper {

	private static final Map<Integer, Integer> GLFW_TO_JS = new HashMap<>();

	static {
		// Letters A-Z (65-90) and numbers 0-9 (48-57) are the same in GLFW and JS
		// Space (32) is also the same

		// Special keys that differ
		GLFW_TO_JS.put(256, 27);   // ESCAPE
		GLFW_TO_JS.put(257, 13);   // ENTER
		GLFW_TO_JS.put(258, 9);    // TAB
		GLFW_TO_JS.put(259, 8);    // BACKSPACE
		GLFW_TO_JS.put(260, 45);   // INSERT
		GLFW_TO_JS.put(261, 46);   // DELETE
		GLFW_TO_JS.put(262, 39);   // RIGHT
		GLFW_TO_JS.put(263, 37);   // LEFT
		GLFW_TO_JS.put(264, 40);   // DOWN
		GLFW_TO_JS.put(265, 38);   // UP
		GLFW_TO_JS.put(266, 33);   // PAGE_UP
		GLFW_TO_JS.put(267, 34);   // PAGE_DOWN
		GLFW_TO_JS.put(268, 36);   // HOME
		GLFW_TO_JS.put(269, 35);   // END
		GLFW_TO_JS.put(280, 20);   // CAPS_LOCK
		GLFW_TO_JS.put(281, 145);  // SCROLL_LOCK
		GLFW_TO_JS.put(282, 144);  // NUM_LOCK
		GLFW_TO_JS.put(283, 44);   // PRINT_SCREEN
		GLFW_TO_JS.put(284, 19);   // PAUSE
		// F1-F12
		for (int i = 0; i < 12; i++) {
			GLFW_TO_JS.put(290 + i, 112 + i);
		}
		// Numpad 0-9
		for (int i = 0; i < 10; i++) {
			GLFW_TO_JS.put(320 + i, 96 + i);
		}
		GLFW_TO_JS.put(330, 110);  // KP_DECIMAL
		GLFW_TO_JS.put(331, 111);  // KP_DIVIDE
		GLFW_TO_JS.put(332, 106);  // KP_MULTIPLY
		GLFW_TO_JS.put(333, 109);  // KP_SUBTRACT
		GLFW_TO_JS.put(334, 107);  // KP_ADD
		// Modifiers
		GLFW_TO_JS.put(340, 16);   // LEFT_SHIFT
		GLFW_TO_JS.put(341, 17);   // LEFT_CONTROL
		GLFW_TO_JS.put(342, 18);   // LEFT_ALT
	}

	public static int glfwToJs(int glfwKeyCode) {
		return GLFW_TO_JS.getOrDefault(glfwKeyCode, glfwKeyCode);
	}
}
