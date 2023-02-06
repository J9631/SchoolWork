#Jacob Sanchez
#October 31st, 2022
#CS 370
#Lab 9 -- Creating GAS code utilizing the AST tree.

all: lab9

lab9: lab9.l lab9.y ast.h ast.c symtable.c symtable.h emit.c emit.h
	lex lab9.l
	yacc -d lab9.y
	gcc ast.c symtable.c emit.c lex.yy.c y.tab.c -o lab9
	
run: lab9
	./lab9

test: lab9
	./lab9 -o postfour < lab9_test.decaf

deepTest: lab9
	./lab9 -o postfour < lab9_test.decaf
	gcc postfour.s
	./a.out

clean:
	rm -f lab9
