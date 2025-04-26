import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParametrosSimulador {
    private int index = 0;
    private String json;

    public Object parse(String json) {
        this.json = json.trim();
        index = 0;
        return parseValue();
    }

    private Object parseValue() {
        skipWhitespace();
        if (currentChar() == '{') {
            return parseObject();
        } else if (currentChar() == '[') {
            return parseArray();
        } else if (currentChar() == '"') {
            return parseString();
        } else if (Character.isDigit(currentChar()) || currentChar() == '-') {
            return parseNumber();
        } else if (json.startsWith("true", index)) {
            index += 4;
            return true;
        } else if (json.startsWith("false", index)) {
            index += 5;
            return false;
        } else if (json.startsWith("null", index)) {
            index += 4;
            return null;
        }
        throw new RuntimeException("Unexpected character: " + currentChar());
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> obj = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (currentChar() == '}') {
            index++;
            return obj; // empty object
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            obj.put(key, value);
            skipWhitespace();
            if (currentChar() == '}') {
                index++;
                break;
            }
            expect(',');
        }
        return obj;
    }

    private List<Object> parseArray() {
        List<Object> array = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (currentChar() == ']') {
            index++;
            return array; // empty array
        }
        while (true) {
            skipWhitespace();
            Object value = parseValue();
            array.add(value);
            skipWhitespace();
            if (currentChar() == ']') {
                index++;
                break;
            }
            expect(',');
        }
        return array;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = currentChar();
            if (c == '"') {
                index++;
                break;
            }
            if (c == '\\') {
                index++;
                char next = currentChar();
                if (next == '"' || next == '\\' || next == '/') {
                    sb.append(next);
                } else if (next == 'b') {
                    sb.append('\b');
                } else if (next == 'f') {
                    sb.append('\f');
                } else if (next == 'n') {
                    sb.append('\n');
                } else if (next == 'r') {
                    sb.append('\r');
                } else if (next == 't') {
                    sb.append('\t');
                }
                // Unicode parsing skipped for simplicity
            } else {
                sb.append(c);
            }
            index++;
        }
        return sb.toString();
    }

    private Number parseNumber() {
        int start = index;
        if (currentChar() == '-')
            index++;
        while (index < json.length() && (Character.isDigit(currentChar()) || currentChar() == '.')) {
            index++;
        }
        String numberStr = json.substring(start, index);
        if (numberStr.contains(".")) {
            return Double.parseDouble(numberStr);
        } else {
            return Integer.parseInt(numberStr);
        }
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(currentChar())) {
            index++;
        }
    }

    private char currentChar() {
        return json.charAt(index);
    }

    private void expect(char expected) {
        if (currentChar() != expected) {
            throw new RuntimeException("Expected '" + expected + "' but found '" + currentChar() + "'");
        }
        index++;
    }

    public Map<String, Object> getJsonValues() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("modelo.json")));
            ParametrosSimulador parser = new ParametrosSimulador();
            Object parsed = parser.parse(content);

            // System.out.println(parsed);

            Map<String, Object> jsonMap = (Map<String, Object>) parsed;
            // System.out.println("Name: " + jsonMap.get("name"));
            // Map<String, Object> location = (Map<String, Object>) jsonMap.get("location");
            // String city = (String) location.get("city");

            // String city = (String) ((Map<String, Object>) ((Map<String, Object>)
            // parsed).get("location")).get("city");
            // System.out.println(city);

            return jsonMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}