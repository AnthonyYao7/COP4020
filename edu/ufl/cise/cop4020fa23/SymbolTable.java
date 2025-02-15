package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import java.util.*;

public class SymbolTable {
    private record SymbolTableEntry(int scopeNum, NameDef nameDef) {}

    int scope = -1;

    HashMap<String, LinkedList<SymbolTableEntry>> table;
    ArrayList<Integer> scopeStack;

    SymbolTable() {
        table = new HashMap<>();
        scopeStack = new ArrayList<>();
    }

    public void enterScope(){
        scopeStack.add(++scope);
    }

    public void leaveScope(){
        scopeStack.remove(scopeStack.size() - 1);
    }

    public int insert(NameDef nameDef) throws TypeCheckException {
        String ident = nameDef.getName();

        LinkedList<SymbolTableEntry> ll = table.computeIfAbsent(ident, k -> new LinkedList<>());

        int current_scope = scopeStack.get(scopeStack.size() - 1);

        for (SymbolTableEntry ste : ll) {
            if (ste.scopeNum == current_scope) {
                throw new TypeCheckException();
            }
        }

        ll.push(new SymbolTableEntry(current_scope, nameDef));
        return current_scope;
    }

    public NameDef lookup(String ident) {
        LinkedList<SymbolTableEntry> ll = table.get(ident);
        if (ll == null) {
            return null;
        }

        int highest = -1;
        NameDef highest_obj = null;

        for (SymbolTableEntry ste : ll) {
            int index = Collections.binarySearch(scopeStack, ste.scopeNum);
            if (index < 0) {
//                index = -index + 1;
//                if (index == scopeStack.size()) continue;
                continue;
            }
            if (index > highest) {
                highest = index;
                highest_obj = ste.nameDef;
            }
        }

        return highest_obj;
    }
}
