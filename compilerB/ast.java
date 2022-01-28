import java.io.*;
import java.net.IDN;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a b program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     --------            ----
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PreIncStmtNode     ExpNode
//       PreDecStmtNode     ExpNode
//       ReceiveStmtNode        ExpNode
//       PrintStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PreIncStmtNode, PreDecStmtNode, ReceiveStmtNode,   PrintStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)$
// **********************************************************************

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    public static int declOffsetCounter = 0;

    // this method can be used by the unparse methods to do indenting
    protected void addIndent(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode$$
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);

        // check if the program contains a function named main
        Symb mainSymb = symTab.lookupGlobal("main");
        if (symTab.lookupGlobal("main") == null) {
            ErrMsg.fatal(0, 0, "No main function");
        } else if (!(mainSymb instanceof FnSymb)) {
            ErrMsg.fatal(0, 0, "No main function");
        } else {
            // Ok
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck() {
        myDeclList.typeCheck();
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    /**
     * codeGeneartion
     */
    public void codeGen() {
        myDeclList.codeGen(true);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /**
     * codeGeneration
     */
    public void codeGen(boolean isGlobal) {
        for (DeclNode node : myDecls) {
            node.codeGen(isGlobal);
        }
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the
     * decls in the list.
     */
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            Symb sym = null;
            if (node instanceof VarDeclNode) {
                sym = ((VarDeclNode) node).nameAnalysis(symTab, globalTab);
            } else {
                sym = node.nameAnalysis(symTab);
            }
            if (sym != null) {
                sym.setOffset(-1 * (8 + (ASTnode.declOffsetCounter++) * 4));
            }
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck() {
        for (DeclNode node : myDecls) {
            node.typeCheck();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public int numOfDecls() {
        return this.myDecls.size();
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     * process the formal decl
     * if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        int i = myFormals.size();
        for (FormalDeclNode node : myFormals) {
            Symb sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
                sym.setOffset(4 * i);
            }
            i--;
        }
        return typeList;
    }

    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myStmtList.typeCheck(retType);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public int numOfDecls() {
        return this.myDeclList.numOfDecls();
    }

    public void codeGen() {
        myStmtList.codeGen();
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        for (StmtNode node : myStmts) {
            node.typeCheck(retType);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public void codeGen() {
        for (StmtNode stmtNode : myStmts) {
            stmtNode.codeGen();
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public int size() {
        return myExps.size();
    }

    public void codeGen() {
        for (ExpNode expNode : myExps) {
            if (expNode instanceof IdNode) {
                ((IdNode) expNode).codeGenForExp();
            } else {
                expNode.codeGen();
            }
        }
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : myExps) {
                Type actualType = node.typeCheck(); // actual type of arg

                if (!actualType.isErrorType()) { // if this is not an error
                    Type formalType = typeList.get(k); // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(),
                                "Type of actual does not match type of formal");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses$
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public Symb nameAnalysis(SymTable symTab);

    // default version of typeCheck for non-function decls
    public void typeCheck() {
    }

    /**
     * codeGen
     */
    public abstract void codeGen(boolean isGlobal);
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * Code generation
     * How exactly is the code generated depending on whether
     * this variable is a global variable or not
     */
    public void codeGen(boolean isGlobal) {
        if (isGlobal) {
            myId.sym().setIsLocal(false);
            codeGenGlobal();
        }
    }

    /**
     * Generate code if the variable is global variable
     */
    private void codeGenGlobal() {
        Codegen.generate(".data");
        Codegen.generate(".align 2");
        Codegen.generateLabeled("_" + myId.name(), ".space 4", "", "");
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type,
     * lookup type name (globally)
     * if type name doesn't exist, then error
     * if no errors so far,
     * if name has already been declared in this scope, then error
     * else add name to local symbol table
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public Symb nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }

    public Symb nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Symb sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) { // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Non-function declared void");
            badDecl = true;
        }

        else if (myType instanceof StructNode) {
            structId = ((StructNode) myType).idNode();
            sym = globalTab.lookupGlobal(structId.name());

            // if the name for the struct type is not found,
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSymb)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(),
                        "Invalid name of struct type");
                badDecl = true;
            } else {
                structId.link(sym);
            }
        }

        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Multiply declared identifier");
            badDecl = true;
        }

        if (!badDecl) { // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSymb(structId);
                } else {
                    sym = new Symb(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("[");
        p.print(myId.sym().getOffset());
        p.print("]");
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
            IdNode id,
            FormalsListNode formalList,
            FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void codeGen(boolean isGlobal) {
        if (myId.name().equals("main")) {
            codeGenMain();
        } else {
            codeGenNoneMain();
        }
    }

    public void codeGenMain() {
        genPreamble(true);
        genEntry();
        genBody();
        genExit(true);
    }

    public void codeGenNoneMain() {
        genPreamble(false);
        genEntry();
        genBody();
        genExit(false);
    }

    private void genPreamble(boolean isMain) {
        Codegen.generate(".text");
        if (isMain) {
            Codegen.generate(".globl main");
            Codegen.genLabel("main");
            Codegen.genLabel("__start");
        } else {
            Codegen.genLabel("_" + myId.name());
        }
    }

    private void genEntry() {
        // push the return address
        Codegen.genPush(Codegen.RA);
        // push the control link
        Codegen.genPush(Codegen.FP);
        // set the $fp
        Codegen.generate("addu", Codegen.FP, Codegen.SP, "8");
        // push space for local variables
        Codegen.generateWithComment("subu", "=SPACE FOR STACK=", Codegen.SP, Codegen.SP,
                String.valueOf(((FnSymb) this.myId.sym()).getTotalSizeofLocalDeclare()));
    }

    private void genBody() {
        this.myBody.codeGen();
    }

    private void genExit(boolean isMain) {
        // load return address
        Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, 0);
        // save $sp temporarily
        Codegen.generate("move", Codegen.T0, Codegen.FP);
        // restore $fp
        Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -4);
        // restore $sp
        Codegen.generate("move", Codegen.SP, Codegen.T0);
        // return
        if (isMain) {
            Codegen.generate("li", Codegen.V0, 10);
            Codegen.generate("syscall");
        } else {
            Codegen.generate("jr", Codegen.RA);
        }
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     * enter new scope
     * process the formals
     * if this function is not multiply declared,
     * update symbol table entry with types of formals
     * process the body of the function
     * exit scope
     */
    public Symb nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSymb sym = null;
        ASTnode.declOffsetCounter = 0;

        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Multiply declared identifier");
        } else { // add function name to local symbol table
            try {
                sym = new FnSymb(myType.type(), myFormalsList.length());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                        " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                        " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        symTab.addScope(); // add a new scope for locals and params

        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
            sym.setTotalSizeofLocalDeclare(this.myBody.numOfDecls() * 4);
        }

        myBody.nameAnalysis(symTab); // process the function body

        try {
            if (sym != null) {
                sym.setTotalSizeofLocalDeclare(ASTnode.declOffsetCounter * 4);
            }
            symTab.removeScope(); // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }

        return null;
    }

    /**
     * typeCheck
     */
    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent + 4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void codeGen(boolean isGlobal) {
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     * then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Symb
     */
    public Symb nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Symb sym = null;

        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Non-function declared void");
            badDecl = true;
        }

        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Multiply declared identifier");
            badDecl = true;
        }

        if (!badDecl) { // insert into symbol table
            try {
                sym = new Symb(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("[");
        p.print(myId.sym().getOffset());
        p.print("]");
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {

    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void codeGen(boolean isGlobal) {
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     * then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     * add a new entry to symbol table for this struct
     */
    public Symb nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;

        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Multiply declared identifier");
            badDecl = true;
        }

        SymTable structSymbTab = new SymTable();

        // process the fields of the struct
        myDeclList.nameAnalysis(structSymbTab, symTab);

        if (!badDecl) {
            try { // add entry to symbol table
                StructDefSymb sym = new StructDefSymb(structSymbTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                        " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                        " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses$$
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }

    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }

    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses$
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);

    abstract public void typeCheck(Type retType);

    public abstract void codeGen();
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    @Override
    public void codeGen() {
        myAssign.codeGen();
        Codegen.genPop(Codegen.T0);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myAssign.typeCheck();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;
}

class PreIncStmtNode extends StmtNode {
    public PreIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    @Override
    public void codeGen() {
        ((IdNode) myExp).codeGenForAssignment();
        Codegen.genPop(Codegen.T0);
        Codegen.generateIndexed("lw", Codegen.T1, Codegen.T0, 0, "PreInc: Get value at Id");
        Codegen.generateWithComment("add", "PreInc: Add value by 1", Codegen.T1, Codegen.T1, "1");
        Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0, "PreInc: Save incred value to addr");
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Arithmetic operator applied to non-numeric operand");
        }
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.println("++");
        myExp.unparse(p, 0);
    }

    // 1 kid
    private ExpNode myExp;
}

class PreDecStmtNode extends StmtNode {
    public PreDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    @Override
    public void codeGen() {
        ((IdNode) myExp).codeGenForAssignment();
        Codegen.genPop(Codegen.T0);
        Codegen.generateIndexed("lw", Codegen.T1, Codegen.T0, 0, "PreDec: Get value at Id");
        Codegen.generateWithComment("sub", "PreDec: Sub value by 1", Codegen.T1, Codegen.T1, "1");
        Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0, "PreDec: Save deced value to addr");
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Arithmetic operator applied to non-numeric operand");
        }
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.println("--");
        myExp.unparse(p, 0);
    }

    // 1 kid
    private ExpNode myExp;
}

class ReceiveStmtNode extends StmtNode {
    public ReceiveStmtNode(ExpNode e) {
        myExp = e;
    }

    @Override
    public void codeGen() {
        // read
        Codegen.generate("li", Codegen.V0, "5");
        Codegen.generate("syscall");

        ((IdNode) myExp).codeGenForAssignment();
        Codegen.genPop(Codegen.T0);
        Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to read a function");
        }

        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to read a struct name");
        }

        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to read a struct variable");
        }

        typeOfExp = type;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("receive >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public Type getTypeOfExp() {
        return typeOfExp;
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
    private Type typeOfExp;
}

class PrintStmtNode extends StmtNode {
    public PrintStmtNode(ExpNode exp) {
        myExp = exp;
    }

    @Override
    public void codeGen() {
        // gen (evalute) the value to be printed
        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGenForExp();
        } else {
            myExp.codeGen();
        }
        // pop the value of exp into the special register for output
        Codegen.genPop(Codegen.A0);
        // set $v0 to the correct value acoording to the type of the exp
        if (typeOfExp instanceof StringType) {
            Codegen.generate("li", Codegen.V0, "4");
        } else {
            Codegen.generate("li", Codegen.V0, "1");
        }
        Codegen.generate("syscall");
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to write a function");
        }

        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to write a struct name");
        }

        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to write a struct variable");
        }

        if (type.isVoidType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Attempt to write void");
        }
        this.typeOfExp = type;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("print << ");
        myExp.unparse(p, 0);
        p.print(": ");
        p.print(typeOfExp);
        p.println(";");
    }

    public Type getTypeOfExp() {
        return typeOfExp;
    }

    // 1 kid
    private ExpNode myExp;
    private Type typeOfExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    @Override
    public void codeGen() {
        // evaluate the condition
        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGen();
        } else {
            myExp.codeGen();
        }

        String falseLabel = Codegen.nextLabel();

        Codegen.genPop(Codegen.T0);
        Codegen.generate("li", Codegen.T1, 0);
        Codegen.generateWithComment("beq", "Jump to FalseLabel", Codegen.T0, Codegen.T1, falseLabel);
        myStmtList.codeGen();
        Codegen.genLabel(falseLabel);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Non-bool expression used as an if condition");
        }

        myStmtList.typeCheck(retType);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
            StmtListNode slist1, DeclListNode dlist2,
            StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    @Override
    public void codeGen() {
        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGenForExp();
        } else {
            myExp.codeGen();
        }
        String elseLabel = Codegen.nextLabel();
        String exitLabel = Codegen.nextLabel();

        Codegen.genPop(Codegen.T0);
        Codegen.generate("li", Codegen.T1, 0);
        Codegen.generateWithComment("beq", "Jump to else if false", Codegen.T0, Codegen.T1, elseLabel);
        myThenStmtList.codeGen();
        Codegen.generateWithComment("jr", "Jump to exit of IfThenElse", exitLabel);
        Codegen.genLabel(elseLabel);
        myElseStmtList.codeGen();
        Codegen.genLabel(exitLabel);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Non-bool expression used as an if condition");
        }

        myThenStmtList.typeCheck(retType);
        myElseStmtList.typeCheck(retType);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
        addIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    @Override
    public void codeGen() {
        String loopStartLabel = Codegen.nextLabel();
        String endLoopLabel = Codegen.nextLabel();
        Codegen.genLabel(loopStartLabel, "Begin While");

        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGenForExp();
        } else {
            myExp.codeGen();
        }
        Codegen.genPop(Codegen.T0);
        Codegen.generate("li", Codegen.T1, 0);
        Codegen.generate("beq", Codegen.T0, Codegen.T1, endLoopLabel);

        myStmtList.codeGen();

        Codegen.generateWithComment("jr", "Back to Beginning", loopStartLabel);
        Codegen.genLabel(endLoopLabel, "End of While");
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Non-bool expression used as a while condition");
        }

        myStmtList.typeCheck(retType);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    @Override
    public void codeGen() {
        // no need to do this
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                    "Non-integer expression used as a repeat clause");
        }

        myStmtList.typeCheck(retType);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    @Override
    public void codeGen() {
        myCall.codeGen();
        Codegen.genPop(Codegen.T0);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myCall.typeCheck();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    @Override
    public void codeGen() {
        // get return value, if exist
        if (myExp != null) {
            if (myExp instanceof IdNode) {
                ((IdNode) myExp).codeGenForExp();
            } else {
                myExp.codeGen();
            }
            Codegen.genPop(Codegen.V0);
        }

        // Pop AR
        // load return address
        Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, 0);
        // save $sp temporarily
        Codegen.generate("move", Codegen.T0, Codegen.FP);
        // restore $fp
        Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -4);
        // restore $sp
        Codegen.generate("move", Codegen.SP, Codegen.T0);
        // return
        Codegen.generateWithComment("jr", "RETURN", Codegen.RA);
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        if (myExp != null) { // return value given
            Type type = myExp.typeCheck();

            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                        "Return with a value in a void function");
            }

            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                        "Bad return value");
            }
        }

        else { // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(0, 0, "Missing return value");
            }
        }

    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses$$
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) {
    }

    public abstract void codeGen();

    abstract public Type typeCheck();

    abstract public int lineNum();

    abstract public int charNum();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    @Override
    public void codeGen() {
        Codegen.generate("li", Codegen.T0, String.valueOf(myIntVal));
        Codegen.genPush(Codegen.T0);
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new IntType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {

    public static HashMap<String, String> stringPool = new HashMap<>();

    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    @Override
    public void codeGen() {
        // use a pool to remove duplciate stringlit
        if (!stringPool.containsKey(myStrVal)) {
            stringPool.put(myStrVal, Codegen.nextLabel());
            Codegen.generate(".data");
            Codegen.generateLabeled(stringPool.get(myStrVal), ".asciiz", "", myStrVal);
        }

        String label = stringPool.get(myStrVal);
        Codegen.generate(".text");
        Codegen.generateWithComment("la", "STRLIT", Codegen.T0, label);
        Codegen.genPush(Codegen.T0);
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new StringType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    @Override
    public void codeGen() {
        Codegen.generate("li", Codegen.T0, "1");
        Codegen.genPush(Codegen.T0);
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("tru");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    @Override
    public void codeGen() {
        Codegen.generate("li", Codegen.T0, "0");
        Codegen.genPush(Codegen.T0);
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("fls");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void codeGen() {
    }

    public void codeGenForAssignment() {
        if (isLocal()) {
            Codegen.generateIndexed("la", Codegen.T0, Codegen.FP, getOffset(), "CodeGenForAssignment");
        } else {
            Codegen.generate("la", Codegen.T0, "_" + name());
        }
        Codegen.genPush(Codegen.T0);
    }

    public void codeGenForExp() {
        if (isLocal()) {
            Codegen.generateIndexed("lw", Codegen.T0, Codegen.FP, getOffset(), "CodeGenForExp");
        } else {
            Codegen.generate("lw", Codegen.T0, "_" + name());
        }
        Codegen.genPush(Codegen.T0);
    }

    public void codeGenForJump() {
        if (name().equals("main")) {
            Codegen.generate("jal", "main");
        } else {
            Codegen.generate("jal", "_" + name());
        }
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(Symb sym) {
        mySymb = sym;
    }

    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }

    /**
     * Return the symbol associated with this ID.
     */
    public Symb sym() {
        return mySymb;
    }

    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        Symb sym = symTab.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }

    public boolean isLocal() {
        return mySymb.isLocal();
    }

    public int getOffset() {
        return mySymb.getOffset();
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (mySymb != null) {
            return mySymb.getType();
        } else {
            System.err.println("ID with null sym field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySymb != null) {
            p.print("(" + mySymb + ")");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Symb mySymb;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
        mySymb = null;
    }

    @Override
    public void codeGen() {
        // struct related
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public Symb sym() {
        return mySymb;
    }

    /**
     * Return the line number for this dot-access node.
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }

    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     * a dot-access "higher up" in the AST can get access to the symbol
     * table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymbTab = null; // to lookup RHS of dot-access
        Symb sym = null;

        myLoc.nameAnalysis(symTab); // do name analysis on LHS

        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode) myLoc;
            sym = id.sym();

            // check ID has been declared to be of a struct type

            if (sym == null) { // ID was undeclared
                badAccess = true;
            } else if (sym instanceof StructSymb) {
                // get symbol table for struct type
                Symb tempSymb = ((StructSymb) sym).getStructType().sym();
                structSymbTab = ((StructDefSymb) tempSymb).getSymTable();
            } else { // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(),
                        "Dot-access of non-struct type");
                badAccess = true;
            }
        }

        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Symb for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode) myLoc;

            if (loc.badAccess) { // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            } else { // no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) { // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(),
                            "Dot-access of non-struct type");
                    badAccess = true;
                } else { // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSymb) {
                        structSymbTab = ((StructDefSymb) sym).getSymTable();
                    } else {
                        System.err.println("Unexpected Symb type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }

        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }

        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {

            sym = structSymbTab.lookupGlobal(myId.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Invalid struct field name");
                badAccess = true;
            }

            else {
                myId.link(sym); // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSymb) {
                    mySymb = ((StructSymb) sym).getStructType().sym();
                }
            }
        }
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return myId.typeCheck();
    }

    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;
    private IdNode myId;
    private Symb mySymb; // link to Symb for struct type
    private boolean badAccess; // to prevent multiple, cascading errors
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    /**
     * Return the line number for this assignment node.
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myLhs.lineNum();
    }

    /**
     * Return the char number for this assignment node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myLhs.charNum();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type typeLhs = myLhs.typeCheck();
        Type typeExp = myExp.typeCheck();
        Type retType = typeLhs;

        if (typeLhs.isFnType() && typeExp.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Function assignment");
            retType = new ErrorType();
        }

        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct name assignment");
            retType = new ErrorType();
        }

        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct variable assignment");
            retType = new ErrorType();
        }

        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }

        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    public void codeGen() {
        // Evaluate RHS
        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGenForExp();
        } else {
            myExp.codeGen();
        }
        // Push the address of the LHS Id onto the stack
        ((IdNode) myLhs).codeGenForAssignment();
        // Store the RHS into the address
        Codegen.genPop(Codegen.T0); // LHS addr
        Codegen.genPop(Codegen.T1); // RHS value
        Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);
        // Leave a copy of the value on the stack
        Codegen.genPush(Codegen.T1);
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    @Override
    public void codeGen() {
        // evaluate each actual parameter, push the values onto the stack
        if (myExpList != null) {
            myExpList.codeGen();
        }
        // jump and link
        myId.codeGenForJump();

        // pop parameters
        Codegen.generateWithComment("addu", "REMOVE PARAM", Codegen.SP, Codegen.SP,
                String.valueOf(((FnSymb) myId.sym()).getTotalSizeOfParams()));

        // push the returned value into $v0
        Codegen.genPush(Codegen.V0);
    }

    /**
     * Return the line number for this call node.
     * The line number is the one corresponding to the function name.
     */
    public int lineNum() {
        return myId.lineNum();
    }

    /**
     * Return the char number for this call node.
     * The char number is the one corresponding to the function name.
     */
    public int charNum() {
        return myId.charNum();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (!myId.typeCheck().isFnType()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Attempt to call a non-function");
            return new ErrorType();
        }

        FnSymb fnSymb = (FnSymb) (myId.sym());

        if (fnSymb == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }

        if (myExpList.size() != fnSymb.getNumParams()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Function call with wrong number of args");
            return fnSymb.getReturnType();
        }

        myExpList.typeCheck(fnSymb.getParamTypes());
        return fnSymb.getReturnType();
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * Return the line number for this unary expression node.
     * The line number is the one corresponding to the operand.
     */
    public int lineNum() {
        return myExp.lineNum();
    }

    /**
     * Return the char number for this unary expression node.
     * The char number is the one corresponding to the operand.
     */
    public int charNum() {
        return myExp.charNum();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void codeGen(String op) {
        if (myExp2 instanceof IdNode) {
            ((IdNode) myExp2).codeGenForExp();
        } else {
            myExp2.codeGen();
        }
        if (myExp1 instanceof IdNode) {
            ((IdNode) myExp1).codeGenForExp();
        } else {
            myExp1.codeGen();
        }

        Codegen.genPop(Codegen.T0);
        Codegen.genPop(Codegen.T1);
        Codegen.generateWithComment(op, op, Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    /**
     * Return the line number for this binary expression node.
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myExp1.lineNum();
    }

    /**
     * Return the char number for this binary expression node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myExp1.charNum();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode$
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    @Override
    public void codeGen() {
        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGenForExp();
        } else {
            myExp.codeGen();
        }
        Codegen.genPop(Codegen.T0);
        Codegen.generate("li", Codegen.T1, "0");
        Codegen.generateWithComment("sub", "unary minus", Codegen.T0, Codegen.T1, Codegen.T0);
        Codegen.genPush(Codegen.T0);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new IntType();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (type.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    @Override
    public void codeGen() {
        if (myExp instanceof IdNode) {
            ((IdNode) myExp).codeGenForExp();
        } else {
            myExp.codeGen();
        }
        Codegen.genPop(Codegen.T0);
        Codegen.generate("li", Codegen.T1, "1");
        Codegen.generateWithComment("neg", "not", Codegen.T0, Codegen.T0);
        Codegen.generateWithComment("add", "not", Codegen.T0, Codegen.T1, Codegen.T0);
        Codegen.genPush(Codegen.T0);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new BoolType();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }

        if (type.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode$$
// **********************************************************************

abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new IntType();

        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                    "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                    "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

abstract class LogicalExpNode extends BinaryExpNode {
    public LogicalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();

        if (!type1.isErrorType() && !type1.isBoolType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                    "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }

        if (!type2.isErrorType() && !type2.isBoolType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                    "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();

        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Equality operator applied to void functions");
            retType = new ErrorType();
        }

        if (type1.isFnType() && type2.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Equality operator applied to functions");
            retType = new ErrorType();
        }

        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Equality operator applied to struct names");
            retType = new ErrorType();
        }

        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Equality operator applied to struct variables");
            retType = new ErrorType();
        }

        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                    "Type mismatch");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();

        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                    "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                    "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

class PlusNode extends ArithmeticExpNode {

    @Override
    public void codeGen() {
        codeGen("add");
    }

    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("sub");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("mul");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("div");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends LogicalExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        if (myExp1 instanceof IdNode) {
            ((IdNode) myExp1).codeGenForExp();
        } else {
            myExp1.codeGen();
        }

        String alwaysFalseLabel = Codegen.nextLabel();
        String exitLabel = Codegen.nextLabel();

        Codegen.genPop(Codegen.T0);
        Codegen.generate("li", Codegen.T1, 0);
        Codegen.generate("beq", Codegen.T0, Codegen.T1, alwaysFalseLabel);

        if (myExp2 instanceof IdNode) {
            ((IdNode) myExp2).codeGenForExp();
        } else {
            myExp2.codeGen();
        }
        Codegen.genPop(Codegen.T1);

        Codegen.generate("li", Codegen.T0, 1);
        Codegen.generate("and", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);

        Codegen.generate("jr", exitLabel);

        Codegen.genLabel(alwaysFalseLabel);
        Codegen.genPush(Codegen.T0);

        Codegen.genLabel(exitLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends LogicalExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        if (myExp1 instanceof IdNode) {
            ((IdNode) myExp1).codeGenForExp();
        } else {
            myExp1.codeGen();
        }
        Codegen.genPop(Codegen.T0);

        String alwaysTrueLabel = Codegen.nextLabel();
        String exitLabel = Codegen.nextLabel();

        Codegen.generate("li", Codegen.T1, 1);
        Codegen.generate("beq", Codegen.T0, Codegen.T1, alwaysTrueLabel);

        if (myExp2 instanceof IdNode) {
            ((IdNode) myExp2).codeGenForExp();
        } else {
            myExp2.codeGen();
        }
        Codegen.genPop(Codegen.T1);

        Codegen.generate("li", Codegen.T0, 0);
        Codegen.generate("or", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);

        Codegen.generate("jr", exitLabel);

        Codegen.genLabel(alwaysTrueLabel);
        Codegen.genPush(Codegen.T0);

        Codegen.genLabel(exitLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("seq");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends EqualityExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("sne");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("slt");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("sgt");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("sle");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    @Override
    public void codeGen() {
        codeGen("sge");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}