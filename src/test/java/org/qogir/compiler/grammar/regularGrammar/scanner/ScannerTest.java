package org.qogir.compiler.grammar.regularGrammar.scanner;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;
import org.qogir.compiler.grammar.regularGrammar.TNFA;
import org.qogir.simulation.scanner.Scanner;

public class ScannerTest {
    public static void main(String[] args) {

        String[] regexes = new String[]{"regex2 := c(a|b)*"};
        //{"regex0 := c(a|b)*"};
        // {"regex2 := a|1","regex1 := ","regex3 := a)","regex4 := a**","regex5 := a|"};
        // {"regex0 := a|ε","regex1 := ab"};//{"regex0 := a|ε","regex1 := c(a|b)*"};
        // {"regex0 := abc"};
        // {"regex1 := c(a|b)*"};
        // "regex1 := c(a|b)*","regex2 := d(f|ea*(g|h))b","c(a|b)*","a|b", "ab*", "d(f|e)","d(f|ea*(g|h))b","c(a|b)*"

        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);

        System.out.println(rg);
        //test building a grammar for the grammar
        Scanner scanner = new Scanner(rg);

        //test constructing the regex tree
        System.out.println(scanner.constructRegexTrees().toString());//符号后跟数字代表这个符号的类型，详见node类

        System.out.println("Show the NFA:");
        //test constructing the NFA
        TNFA NFA = scanner.constructNFA();
        System.out.println(NFA.toString());//把原有输出换成这个方便区分下面的DFA
        //System.out.println(NFA.getAcceptingState());
        //System.out.println(scanner.constructNFA().toString());

        System.out.println("Show the DFA:");
        //test constructing the DFA

        RDFA DFA = scanner.constructDFA(NFA);
        System.out.println(DFA.StateMappingBetweenDFAAndNFAToString() + "\n" + DFA);

        System.out.println("Show the miniDFA:");
        //test minimizing the DFA
        System.out.println(scanner.minimizeDFA(DFA).toString());

    }
}