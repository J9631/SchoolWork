/*
    Jacob Sanchez
    October 31, 2022
    CS 370, LAB9

    Header code for the AST (Abstract-Syntax-Tree) node

    Changes made:
    -added "struct SymbTab* symbol" to ASTnodetype so that tree is linked to symbol table.
 */

#include<stdio.h>
#include<malloc.h>

#ifndef AST_H
#define AST_H
int mydebug;

/* define the enumerated types for the AST.  THis is used to tell us what 
sort of production rule we came across */

enum AST_Tree_Element_Type {
   A_PACKAGE,
   A_METHODDEC,
   A_WHILESTMT,
   A_BLOCK,
   A_EXPR,
   A_VARDEC,

   //Added the following Tree Element Types:
   A_EXTERN,
   A_EXTERN_TYPE,
   A_ARRAY_TYPE,
   A_METHOD_IDENTIFIER,
   A_CONSTANT_INT,
   A_CONSTANT_BOOL,
   A_CONSTANT_STRING,
   A_BREAK,
   A_RETURN,
   A_VAR_LVALUE,
   A_VAR_RVALUE,
   A_ASSIGN,
   A_METHOD_CALL,
   A_METHOD_ARG,
   A_IF,       //root node for If-Else blocks
   A_IF_BLOCK,
   A_ELSE_BLOCK,
   A_CONTINUE,
   A_PROGRAM //root node, superior node for A_EXTERN and A_PACKAGE
};


enum AST_Operators {
   A_PLUS,
   A_MINUS,
   A_TIMES,
   A_NOT,

   //Added following operators:
   A_DIVIDE,
   A_MOD,
   A_AND,
   A_OR,
   A_RIGHT_SHIFT,
   A_LEFT_SHIFT,
   A_U_MINUS,
   A_LEQ,
   A_GEQ,
   A_GT,
   A_LT,
   A_EQ,
   A_NEQ
};

enum AST_Decaf_Types {
   A_Decaf_INT,
   A_Decaf_BOOL,
   A_Decaf_VOID,

   //Added following type
   A_Decaf_STRING
};

/* define a type AST node which will hold pointers to AST structs that will
   allow us to represent the parsed code 
*/
typedef struct ASTnodetype
{
     enum AST_Tree_Element_Type type;
     enum AST_Operators operator;
     char * name;
     int value;
     struct SymbTab* symbol; //Added to link Symbol Table to Tree
     enum AST_Decaf_Types declared_type; // Added field to track type of node

     int size; //size of elements
     char * label; //used to track lavel of strings for emit.c

     struct ASTnodetype *S1,*S2, *next ; /* used for holding IF and WHILE components -- not very descriptive */
} ASTnode;

#include "symtable.h"

/* uses malloc to create an ASTnode and passes back the heap address of the newley created node */
ASTnode *ASTCreateNode(enum AST_Tree_Element_Type mytype);

void PT(int howmany); // prints howmany spaces


ASTnode *program;  // Global Pointer for connection between YACC and backend

/*  Print out the abstract syntax tree */
void ASTprint(int level,ASTnode *p); // prints tree with level horizontal spaceing

int check_parameters(ASTnode *formal, ASTnode *actual);

#endif // of AST_H
