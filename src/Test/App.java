package Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import Analyser.Analyser;
import error.CompileError;
import instruction.Instruction;
import Tokenizer.StringIter;
import Tokenizer.Token;
import Tokenizer.TokenType;
import Tokenizer.Tokenizer;


public class App {
    public static void main(String[] args) throws CompileError, FileNotFoundException {
       
        Scanner scanner;
        InputStream input = new FileInputStream("C:\\Users\\DELL\\eclipse-workspace\\compiler\\src\\in.txt");
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer =new Tokenizer(iter);
        var analyser = new Analyser(tokenizer);
        analyser.analyseProgram();
    }
}
