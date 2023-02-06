%{

/*
    Jacob Sanchez
    October 31, 2022
    CS 370, LAB9

	This YACC has production rules for decaf with syntax directed semantic direction
	to create an abstract syntax tree from a given program alongside a symbol table.
	
	A symtable has also been implemented to type check variables and ensure they are declared
	and used in the right scopes.

*/

	/* begin specs */
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include "symtable.h"
#include "ast.h"
#include "emit.h"

//Line count from lex file
extern int line;
extern int mydebug;


int LEVEL = 0;	//Tracks the level of lines in program, necessary to track block scope

int OFFSET = 0; //How miuch space we are using for the environment, reset to track the offset in methods.
int MAXOFFSET; //Used to help track offset to set the size of methods.
int GOFFSET; //"Global Offset", used to track the overall offset (never reset to 0 like OFFSET).

//Global Variable for the Abstract Syntax Tree
ASTnode* Program;

int yylex(); /* prototype to eliminate warning for yylex() */

void yyerror (s)  /* Called by yyparse on error */
     char *s;
{
  fprintf (stderr, "At line %d, %s\n", line, s);
}


%}
/*  defines the start symbol, what values come back from LEX and how the operators are associated  */

//Starts Symbol has been changed to Program
%start Program

//union allows LEX to return either a value or string with a token
%union
{
    int value;
    char* string;
    struct ASTnodetype* node;
    enum AST_Decaf_Types declared_type;
    enum AST_Operators operator;
}

//Tokens gotten from DECAF
%token T_AND
%token T_ASSIGN
%token T_BOOLTYPE
%token T_BREAK
%token <value> T_CHARCONSTANT
%token T_CONTINUE
%token T_DOT
%token T_ELSE
%token T_EQ
%token T_EXTERN
%token T_FALSE
%token T_FOR
%token T_FUNC
%token T_GEQ
%token T_GT
%token T_LT
%token <string> T_ID
%token T_IF
%token <value> T_INTCONSTANT
%token T_INTTYPE
%token T_LEFTSHIFT
%token T_LEQ
%token T_NEQ
%token T_NULL
%token T_OR
%token T_PACKAGE
%token T_RETURN
%token T_RIGHTSHIFT
%token <string> T_STRINGCONSTANT
%token T_STRINGTYPE
%token T_TRUE
%token T_VAR
%token T_VOID
%token T_WHILE

%left '|'
%left '&'
%left '+' '-'
%left '*' '/' '%'
%left UMINUS

//Production rules of type Node:
%type <node> Externs ExternDefn ExternTypeList ExternTypeList1 FieldDecls FieldDecl ArrayType
%type <node> MethodDecl MethodDecls IDTypeList IDTypeList1 Block VarDecl VarDecls Constant BoolConstant
%type <node> Statement Statements Expr Simpleexpression Additiveexpression Term Factor
%type <node> MethodCall MethodArg MethodArgList MethodArgList1 Assign Lvalue

%type <declared_type> MethodType Type ExternType
%type <operator> Multop Addop Relop



%%	/* end specs, begin rules all of which were obtained from the DECAF description*/

Program	:	Externs T_PACKAGE T_ID
			{
				Insert($3, A_Decaf_INT, ID_Sub_Type_Package, LEVEL, 0, OFFSET, NULL);
			}

			'{' FieldDecls MethodDecls '}'

			{	Program = ASTCreateNode(A_PROGRAM); //Creates the root node
				Program->S1 = $1; //Links extern to S1 of A_PROGRAM node

				Program->S2 = ASTCreateNode(A_PACKAGE); //Creates A_PACKAGE linked to S2 of A_PROGRAM
				Program->S2->name = $3;
				Program->S2->S1 = $6; //Links list of FieldDecls to S1 of A_PROGRAM
				Program->S2->S2 = $7; //Links list of MethodDecls to S2 of A_PROGRAM

				//Sets package to declared_type to type int and puts it into the symbol table
				Program->S2->symbol = Search($3, LEVEL, 0);

			}
		;

Externs	: /* empty */ {$$ = NULL;}
		| ExternDefn Externs
		{
			$$ = $1;
			$$->next = $2; //next node of Extern list
		}
		;

ExternDefn:	T_EXTERN T_FUNC T_ID '(' ExternTypeList ')' MethodType ';'
		{
			/*Error if Extern with same ID is found in symbol table*/
			if(Search($3, LEVEL, 0) != NULL){
				yyerror("Symbol already defined");
				yyerror($3);
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_EXTERN); //creates actual node for Extern
			$$->name = $3;
			$$->declared_type = $7; //declared_type of A_EXTERN is set to MethodType
			$$->S1 = $5; //Links ExternTypeList, a list of A_EXTERN_TYPE nodes, to S1 of A_EXTERN node

			//Inserts Extern into Symbol Table if not defined, and also links the SymbTab returned from Insert to symbol of this ASTnode
			$$->symbol = Insert($3, $7, ID_Sub_Type_ExternMethod, LEVEL, 0, OFFSET, $5);
		}
		;

ExternTypeList: /* empty */ { $$ = NULL; }
		| ExternTypeList1 { $$ = $1; }
		;

ExternTypeList1: ExternType
		{
			$$ = ASTCreateNode(A_EXTERN_TYPE);
			$$->declared_type = $1; //Sets declared_type of A_EXTERN_TYPE node
		}
		| ExternType ',' ExternTypeList1
		{
			$$ = ASTCreateNode(A_EXTERN_TYPE);
			$$->declared_type = $1; //Sets declared_type of A_EXTERN_TYPE node
			$$->next = $3; //Links A_EXTERN_TYPE nodes together with next
		}
		;

ExternType: T_STRINGTYPE { $$ = A_Decaf_STRING;}
		| Type {$$ = $1;} //Passes declared_type from Type upstream
		;

FieldDecls: /* empty */ { $$ = NULL; }
		| FieldDecl FieldDecls
		{
			$$ = $1;
			$$->next = $2; //Links A_VARDEC nodes downstream together with next.
		}
		;

FieldDecl: T_VAR T_ID Type ';' /*All productions atleast create a A_VARDEC node with a name and declared_type*/
			{
			
                //Checks if var with T_ID has already been defined. Prints error message if true.
                if(Search($2, LEVEL, 0) != NULL)
                {
                    yyerror("variable already Defined");
                    yyerror($2);
                    exit(1); //Quits out when error
                }//End if
			
                $$ = ASTCreateNode(A_VARDEC);
                
                
                //Adding Var to Symbol Table then incrementing OFFSET
                $$->symbol = Insert($2, $3, ID_Sub_Type_Scalar, LEVEL, 1, OFFSET, NULL);
                OFFSET++;

				$$->name = $2;
				$$->declared_type = $3;
			}
		| T_VAR T_ID ArrayType ';'
			{
				//Checks if var with T_ID has already been defined. Prints error message if true.
                if(Search($2, LEVEL, 0) != NULL)
                {
                    yyerror("variable already Defined");
                    yyerror($2);
                    exit(1); //Quits out when error
                }//End if


				$$ = ASTCreateNode(A_VARDEC);
				$$->name = $2;
				$$->S1 = $3; //Links A_ARRAY_TYPE to S1 of A_VARDEC
				$$->declared_type = $3->declared_type; //type passed up from A_ARRAY_TYPE node.


				$$->symbol = Insert($2, $3->declared_type, ID_Sub_Type_Array, LEVEL, $3->value, OFFSET, NULL); //Inserts Array into SymbTab
                OFFSET += $3->value; //Increments OFFSET by the size of the array
			}
		| T_VAR T_ID Type T_ASSIGN Constant ';'
			{
				//Checks if var with T_ID has already been defined. Prints error message if true.
                if(Search($2, LEVEL, 0) != NULL)
                {
                    yyerror("variable already Defined");
                    yyerror($2);
                    exit(1); //Quits out when error
                }//End if

				$$ = ASTCreateNode(A_VARDEC);

                //Adding Var to Symbol Table then incrementing OFFSET
                $$->symbol = Insert($2, $3, ID_Sub_Type_Scalar, LEVEL, 1, OFFSET, NULL);
                OFFSET++;

				$$->name = $2;
				$$->declared_type = $3; //Sets A_VARDEC declared_type to declared_type passed upstream from Type
				$$->S2 = $5; //Links a Constant to S2 of A_VARDEC
			}
		;

MethodDecls: /* empty */ { $$ = NULL; }
		| MethodDecl MethodDecls
			{
				$$ = $1;
				$$->next = $2; //Links A_METHODDEC nodes downstream together with next.
			}
		;

MethodDecl: T_FUNC T_ID

			{
				//Checks if the method name is already used
			  if(Search($2, LEVEL, 0) != NULL){
				yyerror("Method Name already in use");
				yyerror($2);
				exit(1);
			  }//end if

			  Insert($2, A_Decaf_VOID, ID_Sub_Type_Method, LEVEL, 0, 0, NULL);

			  GOFFSET = OFFSET;
			  OFFSET = 0;
			  MAXOFFSET = OFFSET;
			}

			'(' IDTypeList ')' MethodType

			{
				struct SymbTab *s;
				s = Search($2, LEVEL, 0);
				s->Type = $7;
				s->fparms = $5;
			}

			Block

			{

				$$ = ASTCreateNode(A_METHODDEC);

				$$->name = $2;
				$$->declared_type = $7;
				$$->S1 = $5; //Links list of A_METHOD_IDENTIFIER nodes to S1 of A_METHODDEC
				$$->S2 = $9; //Links A_BLOCK to S2 of A_METHODDEC

				$$->symbol = Search($2,LEVEL,0); //Inserts Method Declaration into Symbol table pointing to IDTypeList for parameters
				$$->size = MAXOFFSET; //TEMP
				$$->symbol->mysize = MAXOFFSET; //Sets the size of the method to its OFFSET.

				OFFSET = GOFFSET; //Sets the OFFSET back to the overall offset.

			}
		;

IDTypeList: /* empty */ {$$ = NULL;}
		| IDTypeList1 {$$ = $1;}

IDTypeList1: T_ID Type
			{
				//Checks if the method arg is used in the block
				if(Search($1, LEVEL+1, 0) != NULL){
					yyerror("duplicate method arg name");
					yyerror($1);
					exit(1);
				}//end if

				$$ = ASTCreateNode(A_METHOD_IDENTIFIER);
				$$->name = $1;
				$$->declared_type = $2;

				$$->symbol = Insert($1, $2, ID_Sub_Type_Scalar, LEVEL+1, 1, OFFSET, NULL); //Inserts ID Type into symbol table
				OFFSET++;
			}
		| T_ID Type ',' IDTypeList1
			{
				//Checks if the method arg is used in the block
				if(Search($1, LEVEL+1, 0) != NULL){
					yyerror("duplicate method arg name");
					yyerror($1);
					exit(1);
				}//end if


				$$ = ASTCreateNode(A_METHOD_IDENTIFIER);
				$$->name = $1;
				$$->declared_type = $2;
				$$->next = $4; //Links A_METHOD_IDENTIFIER nodes together with next.

				$$->symbol = Insert($1, $2, ID_Sub_Type_Scalar, LEVEL+1, 1, OFFSET, NULL); //Inserts ID Type into symbol table
				OFFSET++;
			}
		;

Block: '{'  {LEVEL++;} VarDecls Statements '}'
			{
				$$ = ASTCreateNode(A_BLOCK);
				$$->S1 = $3; //Links list of VarDecl nodes to S1 of Block
				$$->S2 = $4; //Links list of Statement nodes to S2 of Block


				if(mydebug){
					Display();
					printf("\nExiting BLOCK with level %d\n", LEVEL); //Prints out when a Block is exited for testing purposes.
				}

				if (OFFSET > MAXOFFSET) MAXOFFSET = OFFSET; //Updates MAXOFFSET for method size upon leaving block

				OFFSET -= Delete(LEVEL); //"Exits" level from SymTab and decrements OFFSET by what was removed
				LEVEL--; //Decrements level when exiting a Block
			}
		;

VarDecls: /* empty */ {$$ = NULL;}
		| VarDecl VarDecls
			{
				$$ = $1;
				$$->next = $2; //Links VarDecl nodes together with next
			}
		;

VarDecl: T_VAR T_ID Type ';'
			{
                //Checks if var with T_ID has already been defined. Prints error message if true.
                if(Search($2, LEVEL, 0) != NULL)
                {
                    yyerror("variable already Defined");
                    yyerror($2);
                    exit(1); //Quits out when error
                }//End if
			
				$$ = ASTCreateNode(A_VARDEC);
				
				//Inserts VarDecl if not already defined, no parameters
                $$->symbol = Insert($2, $3, ID_Sub_Type_Scalar, LEVEL, 1, OFFSET, NULL);
                OFFSET++;

				$$->name = $2;
				$$->declared_type = $3;
			}
		|T_VAR T_ID ArrayType ';'
			{
			 //Checks if var with T_ID has already been defined. Prints error message if true.
                if(Search($2, LEVEL, 0) != NULL)
                {
                    yyerror("variable already Defined");
                    yyerror($2);
                    exit(1); //Quits out when error
                }//End if
			
			
				$$ = ASTCreateNode(A_VARDEC);
				
				
                $$->symbol = Insert($2, $3->declared_type, ID_Sub_Type_Array, LEVEL, $3->value, OFFSET, NULL); //Inserts Array into SymbTab
                OFFSET += $3->value; //Increases OFFSET by array size //TEMP: may need to swap to "$$->size"
                
				$$->name = $2;
				$$->S1 = $3; //Links A_ARRAY_TYPE to S1 of A_VARDEC
				$$->declared_type = $3->declared_type; //type passed up from A_ARRAY_TYPE
			}
		;

Statements: /* empty */ {$$ = NULL;}
		| Statement Statements
			{
				$$ = $1;
				$$->next = $2; //Links list of Statements together with next
			}
		;

Statement: Block
			{
				$$ = $1;
			}
		| Assign ';'
			{
				$$ = $1;
			}
		| MethodCall ';'
			{
				$$ = $1;
			}
		| T_IF '(' Expr ')' Block
			{
				$$ = ASTCreateNode(A_IF);	//Creates an A_IF node
				$$->S1 = ASTCreateNode(A_IF_BLOCK); //Links A_IF_BLOCK to S1 of A_IF
				$$->S1->S1 = $3; //Links Expr ($3) to S1 of A_IF_BLOCK
				$$->S1->S2 = $5; //Links Block ($5) to S2 of A_IF_BLOCK
				$$->S2 = NULL;	//Sets S2 of A_IF to NULL
			}
		| T_IF '(' Expr ')' Block T_ELSE Block
			{
				$$ = ASTCreateNode(A_IF);	//Creates an A_IF node
				$$->S1 = ASTCreateNode(A_IF_BLOCK); //Links A_IF_BLOCK to S1 of A_IF
				$$->S1->S1 = $3; //Links Expr ($3) to S1 of A_IF_BLOCK
				$$->S1->S2 = $5; //Links Block ($5) to S2 of A_IF_BLOCK
				$$->S2 = ASTCreateNode(A_ELSE_BLOCK); //Links A_ELSE_BLOCK to S2 of A_IF
				$$->S2->S1 = $7; //Links Block ($7) to S1 of A_ELSE_BLOCK
			}
		| T_WHILE '(' Expr ')' Block
			{
				$$ = ASTCreateNode(A_WHILESTMT);
				$$->S1 = $3; //Links Expr to S1 of A_WHILESTMT node
				$$->S2 = $5; //Links Block to S2 of A_WHILESTMT node
			}
		| T_RETURN ';'
			{
				$$ = ASTCreateNode(A_RETURN);
			}
		| T_RETURN '(' ')' ';'
			{
				$$ = ASTCreateNode(A_RETURN);
			}
		| T_RETURN '(' Expr ')' ';'
			{
				$$ = ASTCreateNode(A_RETURN);
				$$->S1 = $3; //Links A_EXPR node(s) to S1 of A_RETURN
			}
		| T_BREAK ';'
			{
				$$ = ASTCreateNode(A_BREAK);
			}
		| T_CONTINUE ';'
			{
				$$ = ASTCreateNode(A_CONTINUE);
			}
		;

Assign: Lvalue T_ASSIGN Expr
		{
			//Checks that Lvalue is being assigned to a matching type
			if($1->declared_type != $3->declared_type){
				yyerror("type mismatch on assignment");
				exit(1);
			}// end if

			$$ = ASTCreateNode(A_ASSIGN);
			$$->S1 = $1;
			$$->S2 = $3;
			$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
			$$->symbol = Insert($$->name, $1->declared_type, ID_Sub_Type_Scalar, LEVEL, 1, OFFSET, NULL); //Inserts operation with temporary name into Symbol Table
			OFFSET++;
		}
		;

Lvalue: T_ID
			{

				struct SymbTab *p;
				p = Search($1, LEVEL, 1); //Looks for T_ID in this level and all levels above

				//Checks that T_ID is in the symbol table
				if(p == NULL){
					yyerror("Symbol for LVALUE not defined");
					yyerror($1);
					exit(1);
				}//end if

				//Subtype checking, this production rule is only for scalars.
				if(p->SubType != ID_Sub_Type_Scalar){
					yyerror($1);
					yyerror("Needs to be scalar, wrong subtype");
					exit(1);
				}//end if

				$$ = ASTCreateNode(A_VAR_LVALUE);
				$$->name = $1;

				$$->symbol = p; //Links ASTnode and symbol table
				$$->declared_type = p->Type;
			}
		| T_ID '[' Expr ']'
			{
				struct SymbTab *p;
				p = Search($1, LEVEL, 1); //Looks for T_ID in this level and all levels above

				//Checks that T_ID is in the symbol table
				if(p == NULL){
					yyerror("Symbol for LVALUE not defined");
					yyerror($1);
					exit(1);
				}//end if

				//Subtype checking, this production rule is only for arrays.
				if(p->SubType != ID_Sub_Type_Array){
					yyerror($1);
					yyerror("Needs to be an array, wrong subtype");
					exit(1);
				}//end if

				$$ = ASTCreateNode(A_VAR_LVALUE);
				$$->name = $1;
				$$->S1 = $3; //Links Expr to S1 of A_VAR_LVALUE for its Array Index

				$$->symbol = p; //Links ASTnode and symbol table
				$$->declared_type = p->Type;
			}

MethodCall: T_ID '(' MethodArgList ')'
		 {
			struct SymbTab *p;
			p = Search($1, LEVEL, 1); //Looks for T_ID in this level and all levels above

			//Checks that T_ID is in the symbol table
			if(p == NULL){
				yyerror("Symbol not defined");
				yyerror($1);
				exit(1);
			}//end if

			//Subtype checking, this production rule is only for methods.
			if(p->SubType != ID_Sub_Type_Method && p->SubType != ID_Sub_Type_ExternMethod){
				yyerror($1);
				yyerror("Needs to be method, wrong subtype");
				exit(1);
			}//end if

			//Checks that formal parameters are same as the actual parameters
			if(check_parameters(p->fparms, $3) == 0){
				yyerror("Formal and actual parameters don't match");
				yyerror($1);
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_METHOD_CALL);
			$$->name = $1;
			$$->S1 = $3; //Links list of MethodArgs to S1 of A_METHOD_CALL

			$$->symbol = p; //Links ASTnode and symbol table
			$$->declared_type = p->Type;
		 }
		;

MethodArgList: /* empty */ {$$ = NULL;}
			| MethodArgList1 {$$ = $1;}
			;

MethodArgList1: MethodArg {$$ = $1;}
			| MethodArg ',' MethodArgList1
				{
					$$ = $1;
					$$->next = $3; //Links A_METHOD_ARG nodes downstream with next
				}
			;

MethodArg: Expr 
            {
                $$ = ASTCreateNode(A_METHOD_ARG); //Creates an A_METHOD_ARG node for the purpose of a list
                $$->S1 = $1; //Links an Expr ($1) to S1 of A_METHOD_ARG
				$$->declared_type = $1->declared_type;

				$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
				$$->symbol = Insert($$->name, $$->declared_type,ID_Sub_Type_Scalar,LEVEL,1,OFFSET,NULL); //Inserts operation with temporary name into Symbol Table
				OFFSET++;
            }
		| T_STRINGCONSTANT
			{
                $$ = ASTCreateNode(A_METHOD_ARG); //Creates a A_METHOD_ARG for the purpose of a list
                $$->S1 = ASTCreateNode(A_CONSTANT_STRING);
				$$->S1->name = $1; //Stores string in A_CONSTANT_STRING nodes name field.
				$$->S1->declared_type = A_Decaf_STRING;
				$$->declared_type = $$->S1->declared_type;

				$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
				$$->symbol = Insert($$->name, $$->declared_type,ID_Sub_Type_Scalar,LEVEL,1,OFFSET,NULL); //Inserts operation with temporary name into Symbol Table
				OFFSET++;
			}
		;

Expr: Simpleexpression {$$ = $1;} //Just passes Simpleexpression upstream, since all A_EXPR nodes are created and linked downstream
	;

Simpleexpression: Additiveexpression {$$ = $1;}
				|  Simpleexpression Relop Additiveexpression
					{
						//Checks that types on both sides match
						if($1->declared_type != $3->declared_type){
							yyerror("both sides need to be the same type");
							exit(1);
						}//end if

						$$ = ASTCreateNode(A_EXPR);
						$$->S1 = $1; //S1 linked to left Additiveexpression
						$$->operator = $2; //Sets the operator of the A_EXPR node passing it upstream from $2
						$$->S2 = $3; //S2 linked to right Simpleexpression

						//$$->declared_type = $1->declared_type;
						$$->declared_type = A_Decaf_BOOL; //Relops always result in a Boolean

						//Puts a temp into the activation record
						$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
						$$->symbol = Insert($$->name, $$->declared_type,ID_Sub_Type_Scalar,LEVEL,1,OFFSET,NULL); //Inserts operation with temporary name into Symbol Table
						OFFSET++;
					}
				;

//All Relop production rules just pass corresponding AST_Operators upstream
Relop: T_LEQ {$$ = A_LEQ;}
	| T_LT {$$ = A_LT;}
	| T_GT {$$ = A_GT;}
	| T_GEQ {$$ = A_GEQ;}
	| T_EQ {$$ = A_EQ;}
	| T_NEQ {$$ = A_NEQ;}
	;

Additiveexpression: Term {$$ = $1;}
				| Additiveexpression Addop Term
					{

						//Checks that terms have matching types and are also integers
						if ( ($1->declared_type != $3->declared_type) ||
							 ($1->declared_type != A_Decaf_INT)){
							yyerror("Addition and subtraction need INTS only");
							exit(1);
						}//end if

						$$ = ASTCreateNode(A_EXPR);
						$$->S1 = $1; //S1 linked to left Term
						$$->operator = $2; //Sets the operator of the A_EXPR node passing it upstream from $2
						$$->S2 = $3; //S2 linked to right Additiveexpression

						$$->declared_type = A_Decaf_INT;

						//Puts a temp into the activation record
						$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
						$$->symbol = Insert($$->name, $$->declared_type,ID_Sub_Type_Scalar,LEVEL,1,OFFSET,NULL); //Inserts operation with temporary name into Symbol Table
						OFFSET++;
					}
				;

//All Addop production rules just pass corresponding AST_Operators upstream
Addop: '+' {$$ = A_PLUS;}
	| '-' {$$ = A_MINUS;}
	;

Term: Factor {$$ = $1;} //Passes A_EXPR from Factor upstream
	| Term Multop Factor
		{
			//Type checking for term and factor
			if($1->declared_type != $3->declared_type){
				yyerror("type mismatch");
				exit(1);
			}

			//Checks that BOOLs aren't being used for arithematic
			if( $1->declared_type == A_Decaf_BOOL &&
			  (($2 == A_TIMES) || ($2 == A_DIVIDE) || ($2 == A_MOD)) ){
				yyerror("cannot use Booleans in arithematic operation");
				exit(1);
			}//end if

			//Checks that INTEGERs aren't being used for boolean operations
			if( $1->declared_type == A_Decaf_INT &&
				( ($2 == A_AND) || ($2 == A_OR)  || ($2 == A_LEFT_SHIFT) || ($2 == A_RIGHT_SHIFT) ) ){
				yyerror("cannot use Integers in boolean operation");
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_EXPR);
			$$->S1 = $1; //S1 linked to left Factor
			$$->operator = $2; //Sets the operator of the A_EXPR node passing it upstream from $2
			$$->declared_type = $1->declared_type;

			$$->S2 = $3; //S2 linked to right Term

			//Puts a temp into the activation record
			$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
			$$->symbol = Insert($$->name, $$->declared_type,ID_Sub_Type_Scalar,LEVEL,1,OFFSET,NULL); //Inserts operation with temporary name into Symbol Table
			OFFSET++;
		}
	;

//All Multop production rules just pass corresponding AST_Operators upstream
Multop: '*' {$$ = A_TIMES;}
	| '/' {$$ = A_DIVIDE;}
	| T_AND {$$ = A_AND;}
	| T_OR {$$ = A_OR;}
	| T_LEFTSHIFT {$$ = A_LEFT_SHIFT;}
	| T_RIGHTSHIFT {$$ = A_RIGHT_SHIFT;}
	| '%' {$$ = A_MOD;}
	;

Factor: T_ID
		{

			struct SymbTab *p;
			p = Search($1, LEVEL, 1);

			//Checks that T_ID is in symbol table
			if(p == NULL){
				yyerror("Symbol for RVALUE not defined");
				yyerror($1);
				exit(1);
			}

			//Subtype checking, this production rule is only for scalars.
			if(p->SubType != ID_Sub_Type_Scalar){
				yyerror($1);
				yyerror("Needs to be scalar, wrong subtype");
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_VAR_RVALUE);
			$$->name = $1;

			$$->symbol = p; //Links ASTnode and symbol table
			$$->declared_type = p->Type;
		}
	| MethodCall
		{
			$$ = $1;
		}
	| T_ID '[' Expr ']'
		{
			struct SymbTab *p;
			p = Search($1, LEVEL, 1); //Looks for T_ID in this level and all levels above

			//Checks that T_ID is in the symbol table
			if(p == NULL){
				yyerror("Symbol for RVALUE not defined");
				yyerror($1);
				exit(1);
			}//end if

			//Subtype checking, this production rule is only for arrays.
			if(p->SubType != ID_Sub_Type_Array){
				yyerror($1);
				yyerror("Needs to be an array, wrong subtype");
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_VAR_RVALUE);
			$$->name = $1;
			$$->S1 = $3;

			$$->symbol = p; //Links ASTnode and symbol table
			$$->declared_type = p->Type;
		}
	| Constant {$$ = $1;}
	| '(' Expr ')' {$$ = $2;}
	| '!' Factor
		{
			//Checks that Factor is BOOL
			if($2->declared_type != A_Decaf_BOOL){
				yyerror("type mismatch, expecting boolean");
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_EXPR); //Creates a new node to hold A_NOT operator
			$$->operator = A_NOT;
			$$->S1 = $2; //S1 of A_EXPR holding A_NOT is linked to Factor at $2

			$$->declared_type = A_Decaf_BOOL; //Passes up type, since unary minus only applies to BOOL

			//Puts a temp into the activation record
			$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
			$$->symbol = Insert($$->name, A_Decaf_BOOL, ID_Sub_Type_Scalar, LEVEL, 1, OFFSET, NULL); //Inserts operation with temporary name into Symbol Table
			OFFSET++;
		}
	| '-' Factor
		{
			//Checks that Factor is INT
			if($2->declared_type != A_Decaf_INT){
				yyerror("type mismatch, expecting integer");
				exit(1);
			}//end if

			$$ = ASTCreateNode(A_EXPR); //Creates a new node to hold A_U_MINUS operator
			$$->operator = A_U_MINUS;
			$$->S1 = $2; //S1 of A_EXPR holding A_U_MINUS is linked to Factor at $2

			$$->declared_type = A_Decaf_INT;

			//Puts a temp into the activation record
			$$->name = Create_Temp(); //Creates a temporary name to store the operation in the Symbol Table
			$$->symbol = Insert($$->name, A_Decaf_INT, ID_Sub_Type_Scalar, LEVEL, 1, OFFSET, NULL); //Inserts operation with temporary name into Symbol Table
			OFFSET++;
		}
	;

//Passes a declared_type upstream
Type: T_INTTYPE { $$ = A_Decaf_INT; }
	| T_BOOLTYPE { $$ = A_Decaf_BOOL; }
	;

MethodType: T_VOID { $$ = A_Decaf_VOID;}
 		| Type { $$ = $1;}
		;

ArrayType: '[' T_INTCONSTANT ']' Type
			{
				$$ = ASTCreateNode(A_ARRAY_TYPE); //Creates new node for holding array type info.
				$$->value = $2;	//Value of this node is assinged to declared size of array
				$$->declared_type = $4;
			}
		;

Constant: T_INTCONSTANT
			{
				$$ = ASTCreateNode(A_CONSTANT_INT);
				$$->value = $1;
				$$->declared_type = A_Decaf_INT;
			}
		| BoolConstant { $$ = $1; }
		;

BoolConstant: T_TRUE
				{
					$$ = ASTCreateNode(A_CONSTANT_BOOL);
					$$->value = 1;
					$$->declared_type = A_Decaf_BOOL;
				}
			| T_FALSE
				{
					$$ = ASTCreateNode(A_CONSTANT_BOOL);
					$$->value = 0;
					$$->declared_type = A_Decaf_BOOL;
				}
			;
%%	/* end of rules, start of program */

int main(int argc, char *argv[])
{

  int i = 1;
  FILE *fp;
  char s[100];

  //Processes the input line
  while(i < argc){
	//Debug symbol
	if(strcmp(argv[i], "-d") == 0){
		mydebug = 1;
	}

	if(strcmp(argv[i], "-o") == 0){
		//Given that we have an input file
		//copies argv into temprho
		strcpy(s, argv[i+1]);
		//adds suffix ".s"
		strcat(s, ".s");

		//attempts to open file, exiting if it can't
		if( ( fp=fopen(s, "w") ) == NULL ){
			printf("cannot open %s\n", argv[i+1]);
			exit(1);
		}
	}//end while

	i++;
  }

  yyparse();
  if(mydebug){
	Display();//Prints out at 0 level
	printf("Parsing Completed\n");
	ASTprint(0, Program);
  }

  //Writes assembly to a ".s" file
  EMIT(Program, fp);
}
