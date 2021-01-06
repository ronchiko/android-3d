package com.roncho.engine;

import com.roncho.engine.android.Logger;
import com.roncho.engine.gl.objects.GLDrawable;
import com.roncho.engine.structs.ComponentBase;
import com.roncho.engine.structs.Mesh;
import com.roncho.engine.structs.Texture2D;
import com.roncho.engine.structs.primitive.d3.Int3;
import com.roncho.engine.structs.primitive.Quaternion;
import com.roncho.engine.structs.primitive.d2.Vector2;
import com.roncho.engine.structs.primitive.d3.Vector3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ObjectFactory {

    private static boolean cacheLoaded;
    private final static Class<DontSerializeField> annotationCache = DontSerializeField.class;
    private final static Class<?>[] serializableClasses = {
            Vector3.class,
            Vector2.class,
            Quaternion.class,
            Int3.class,
    };
    private final static Class<?>[] importableClasses = {
            Mesh.class,
            Texture2D.class
    };

    private final static List<String> keywords = new ArrayList<String>(){
        {
            add("in");
            add("type");
            add("mesh");
            add("texture");
            add("null");
            add("true");
            add("false");
        }
    };

    private static class ComponentData {

    }

    public static <T extends GLDrawable> String serializeObject(T object) throws Exception {
        StringBuilder sb = new StringBuilder();
        Class<?> cls = object.getClass();
        sb.append("type\"").append(cls.getName()).append("\"\n");
        Field[] fields = cls.getFields();
        sb.append("{");
        for(Field field : fields){
            sb.append(serializeField(field, object, 1));
        }
        sb.append("}");
        return sb.toString();
    }

    private static <T extends ComponentBase> String serializeComponent(T object, int indent) throws Exception{
        StringBuilder sb = new StringBuilder("{\n");
        Class<?> cls = object.getClass();
        Field[] fields = cls.getDeclaredFields();
        for(Field field : fields){
            //for(int i = 0; i < indent + 1; i++) sb.append('\t');
            sb.append(serializeField(field, object, indent + 1));
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String serializeField(Field f, Object o, int indent) throws Exception {
        if(f.getAnnotation(DontSerializeField.class) != null) return "";
        if(Modifier.isStatic(f.getModifiers())) return "";

        f.setAccessible(true);
        Class<?> cls = f.getType();
        if(cls.equals(int.class)) return multiply("\t", indent) + "." + f.getName() + ": " + f.getInt(o) + "\n";
        else if(cls.equals(float.class)) return multiply("\t", indent) + "." + f.getName() + ": " + f.getFloat(o) + "\n";
        else if(cls.equals(boolean.class)) return multiply("\t", indent) + "." + f.getName() + ": " + f.getBoolean(o) + "\n";
        else if(cls.equals(String.class)) {
            Object v = f.get(o);
            return multiply("\t", indent) + "." + f.getName() + ": " + (v == null ? "null" : "\"" + v + "\"") + "\n";
        }
        else if(ComponentBase.class.isAssignableFrom(cls)) return multiply("\t", indent) + "." + f.getName() + ": " + serializeComponent((ComponentBase) f.get(o), indent);
        else if(classArrayContains(serializableClasses, cls)) return multiply("\t", indent) + serializeAdvancedClass(f, o);
        else if(classArrayContains(importableClasses, cls))   return multiply("\t", indent) + serializeAdvancedClass(f, o);
        return "";
    }

    private static String serializeAdvancedClass(Field f, Object o) throws Exception {
        Method method = f.getType().getDeclaredMethod("toString");
        Object so = f.get(o);
        if(so == null) return "." + f.getName() + ": null\n";
        String parse = (String)method.invoke(so);
        return "." + f.getName() + ": " + parse + "\n";
    }

    private static boolean classArrayContains(Class<?>[] classes, Class<?> c){
        for (Class<?> aClass : classes) if (c.equals(aClass)) return true;
        return false;
    }

    private static String multiply(String s, int c){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < c; i++) sb.append(s);
        return sb.toString();
    }

    private enum TokenType {
        RBracket(')'),
        LBracket('('),
        Dot('.'),
        Colon(':'),
        Comma(','),
        Word,
        LCurlyBracket('{'),
        RCurlyBracket('}'),
        String,
        Keyword,
        Float,
        Int;

        private final char filler;

        TokenType(char filler) {this.filler = filler;}
        TokenType() {this(' ');}
    }

    private static class Token {
        public TokenType type;
        public String value;

        public Token(TokenType type, String value){
            this.type = type;
            this.value = value;
        }

        public Token(TokenType type){
            this(type, null);
        }

        public boolean isKeyword(String keyword){
            return type == TokenType.Keyword && value.equals(keyword);
        }
        public boolean isString(){
            return type == TokenType.String;
        }
        public boolean isWord(){
            return type == TokenType.Word;
        }
        public boolean isType(TokenType type){
            return this.type == type;
        }
    }

    private static class Lexer {
        private final Queue<Token> tokens;

        public Lexer(){
            tokens = new ArrayDeque<>();
        }

        public void getTokens(String source){
            int index = 0;
            StringBuilder sb = new StringBuilder();
            while (index < source.length()){
                switch (source.charAt(index)){
                    // case '\n': tokens.add(new Token(TokenType.NewLine)); break;
                    case ':': tokens.add(new Token(TokenType.Colon)); break;
                    case '.': tokens.add(new Token(TokenType.Dot)); break;
                    case ',': tokens.add(new Token(TokenType.Comma)); break;
                    case '(': tokens.add(new Token(TokenType.LBracket)); break;
                    case ')': tokens.add(new Token(TokenType.RBracket)); break;
                    case '{': tokens.add(new Token(TokenType.LCurlyBracket)); break;
                    case '}': tokens.add(new Token(TokenType.RCurlyBracket)); break;
                    case '"':
                        sb.delete(0, sb.length());
                        index++;
                        while (source.charAt(index) != '"'){
                            sb.append(source.charAt(index));
                            index++;
                        }
                        tokens.add(new Token(TokenType.String, sb.toString()));
                    default:
                        sb.delete(0, sb.length());
                        char c = source.charAt(index);
                        if(Character.isWhitespace(c)) break;
                        else if(Character.isAlphabetic(c)){
                            while (Character.isAlphabetic(c) || Character.isDigit(c)){
                                sb.append(c);
                                index++;
                                c = source.charAt(index);
                            }
                            tokens.add(new Token(keywords.contains(sb.toString()) ? TokenType.Keyword : TokenType.Word, sb.toString()));
                        }else if(Character.isDigit(c)){
                            int dotCount = 0;
                            while (c == '.' || Character.isDigit(c)){
                                if(c == '.'){
                                    dotCount++;
                                }
                                sb.append(c);
                                index++;
                                c = source.charAt(index);
                            }
                            tokens.add(new Token(dotCount > 0 ? TokenType.Float : TokenType.Int, sb.toString()));
                        }else break;
                        continue;
                }

                index++;
            }
        }

        public Token next() {return tokens.remove();}
        public Token peek() {return tokens.peek();}
    }

    public static class Parser {
        private final Lexer lexer;

        public Parser(Lexer lexer){
            this.lexer = lexer;
        }

        public GLDrawable makeObject() throws Exception {
            if(!lexer.peek().isKeyword("type")){
                throw new Exception(".X files should start with type\"<source class>\"");
            }
            lexer.next();
            Token token = lexer.next();
            if(!token.isWord() && !token.isString()) throw new Exception("Expected a string or word!");
            String sourceClass = token.value;
            Class<?> cls = Class.forName(sourceClass);
            Constructor<GLDrawable> constructor = (Constructor<GLDrawable>)cls.getConstructor();
            GLDrawable drawable = constructor.newInstance();
            //Logger.Log(sourceClass + ": " + drawable.getClass().getName());

            parseObject(drawable);

            return drawable;
        }

        public void parseObject(Object o) throws Exception{
            if(!lexer.peek().isType(TokenType.LCurlyBracket)) throw new Exception("Expected '{'");
            lexer.next();
            while (lexer.peek().isType(TokenType.Dot)){
                lexer.next();
                if(!lexer.peek().isWord()) throw new Exception("Expected a word");
                String field = lexer.peek().value;
                lexer.next();
                if(!lexer.peek().isType(TokenType.Colon)) throw new Exception("Expected ':'");
                lexer.next();
                parseField(o, field);
            }
            if(!lexer.peek().isType(TokenType.RCurlyBracket)) throw new Exception("Expected '}'");
            lexer.next();
        }

        public void parseField(Object o, String fieldName) throws Exception {
            Class<?> type = o.getClass();

            try {
                Field field = type.getField(fieldName);
                field.setAccessible(true);
                Class<?> cls = field.getType();
                TokenType next = lexer.peek().type;

                if(cls.equals(int.class)){
                    if(next == TokenType.Int) field.setInt(o, Integer.parseInt(lexer.next().value));
                    else {
                        field.setInt(o, 0);
                        lexer.next();
                        Logger.Warn("'" + fieldName + "'s type has changed, defaulting to 0");
                    }
                }else if(cls.equals(float.class)){
                    if(next == TokenType.Int) field.setFloat(o, (float)Integer.parseInt(lexer.next().value));
                    else if(next == TokenType.Float) field.setFloat(o, Float.parseFloat(lexer.next().value));
                    else {
                        field.setFloat(o, 0);
                        lexer.next();
                        Logger.Warn("'" + fieldName + "'s type has changed, defaulting to 0");
                    }
                }else if(cls.equals(boolean.class)){
                    if(lexer.peek().isKeyword("true")
                    || lexer.peek().isKeyword("false")) field.setBoolean(o, Boolean.parseBoolean(lexer.next().value));
                    else{
                        field.setBoolean(o, false);
                        lexer.next();
                        Logger.Warn("'" + fieldName + "'s type has changed, defaulting to false");
                    }
                }else if(cls.equals(String.class)){
                    if(next == TokenType.String) field.set(o, lexer.next().value);
                    else if(lexer.peek().isKeyword("null")) {
                        lexer.next();
                        field.set(o, null);
                    }
                    else {
                        field.set(o, "");
                        lexer.next();
                        Logger.Warn("'" + fieldName + "'s type has changed, defaulting to \"\"");
                    }
                }else if(ComponentBase.class.isAssignableFrom(cls)){
                    if(next == TokenType.LCurlyBracket) {
                        Constructor<?> c = cls.getConstructor();
                        Object obj = c.newInstance();

                        parseObject(obj);
                        field.set(o, obj);
                    }else if(lexer.peek().isKeyword("null")) {
                        lexer.next();
                        field.set(o, null);
                    }else {
                        lexer.next();
                        field.set(o, null);
                        Logger.Warn("'" + fieldName + "'s type has changed, defaulting to null");
                    }
                }else if(classArrayContains(serializableClasses, cls)){
                    if(next == TokenType.LBracket){
                        StringBuilder sb = new StringBuilder();
                        int level = 0;
                        while (!(next == TokenType.RBracket && level == 1)){
                            if(next == TokenType.LBracket) level++;
                            if(next == TokenType.RBracket) level--;
                            sb.append(lexer.peek().type.filler).append(lexer.peek().value);
                            next = lexer.next().type;
                        }
                        lexer.next();
                        sb.append(')');
                        Object obj;
                        if(field.getAnnotation(GLDrawable.AutoInitiated.class) == null) {
                            Constructor<?> c = cls.getConstructor();
                            obj = c.newInstance();
                        }else {obj = field.get(o) ;}
                        try {
                            Method parser = cls.getMethod("parse", String.class);
                            parser.invoke(obj, sb.toString());
                        }catch (NoSuchMethodException e)
                        {
                            Logger.Error("Failed to find method '" + e.getMessage() + "': in class" + cls.getName());
                        }
                    }else if(lexer.peek().isKeyword("null")) {
                        lexer.next();
                        field.set(o, null);
                    }else{
                        lexer.next();
                        field.set(o, null);
                        Logger.Warn("'" + fieldName + "'s type has changed, defaulting to null");
                    }
                }
            }catch (NoSuchFieldException e) {
                Logger.Warn("Skipping field '" + fieldName + "': it appears to be removed.");
            }
        }
    }

    public static GLDrawable loadObject(String source){
        Lexer lexer = new Lexer();
        lexer.getTokens(source);
        Parser parser = new Parser(lexer);
        try {
            return parser.makeObject();
        }catch (Exception e){
            Logger.Exception(e);
            return null;
        }
    }
}
