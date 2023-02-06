/*
    Jacob Sanchez
    October 31, 2022
    CS 370, LAB9

    The following code defines an AST (Abstract-Syntax-Tree) node using for printing the AST itself.
*/

#include<stdio.h>
#include<malloc.h>
#include "ast.h" 

//PRE: "formal" and "actual" are pointers to method parameters
//POST: Returns 1 if the formal and actual parameters have matching types, else returns 0.
int check_parameters(ASTnode *formal, ASTnode *actual){
   if(formal == NULL && actual == NULL) return 1;
   if(formal == NULL || actual == NULL) return 0;
   if(formal->declared_type != actual->S1->declared_type) return 0;
   return check_parameters(formal->next, actual->next);
}//end check_parameters



/* uses malloc to create an ASTnode and passes back the heap address of the newley created node */
//PRE: Takes a valid AST_Tree_Element_Type
//POST: Creates and allocates memory for an ASTnode struct that is the type it accepted in its precondition.
ASTnode *ASTCreateNode(enum AST_Tree_Element_Type mytype)
{
    ASTnode *p;
    if (mydebug) fprintf(stderr,"Creating AST Node \n");
    p=(ASTnode *)malloc(sizeof(ASTnode)); // get head data
    p->type=mytype; // set up the Element type
    p->S1=NULL;
    p->S2=NULL;  // det default values
    p->value=0;
    return(p);
}//end ASTCreateNode



/*  Helper function to print tabbing */
//PRE: Accepts an integer that is positive or 0
//POST: Prints an amount of spaces equal to howmany
void PT(int howmany)
{
	for(int i = 0; i < howmany; i++)
      printf(" ");
}//end PT



//PRE: An enum type.
//POST: A string representing the type
char* ASTPrint_Type(enum AST_Decaf_Types t){

  //switch determines what type to return as string
  switch(t){

    case A_Decaf_INT : return(" INT ");
                      break;
    case A_Decaf_BOOL : return(" BOOL ");
                      break;
    case A_Decaf_VOID : return(" VOID ");
                      break;
    case A_Decaf_STRING : return (" STRING ");
                      break;
    default:  fprintf(stderr," Unknown type in ASTprint_type ");
  }//end switch

} //end ASTPrint_Type


/*  Print out the abstract syntax tree */
//PRE: Takes a nonnegative integer and ASTnode pointer that isn't NULL.
//POST: Prints contents of the pointer with level many leading spaces and the rest of its tree recursively.
void ASTprint(int level,ASTnode *p)
{
   int i;

   //Returns nothing if pointer is null
   if (p == NULL ) return;
   else
     { 
       PT(level); /*indents by level amount of spaces*/

       //Switches to approptiate print routine depending on ASTnode type.
       switch (p->type) {

        //modified printf
        case A_VARDEC :
                     printf("Variable %s", p->name);

                     //Checks if p is an array
                     if(p->S1 != NULL)
                       printf("[%d]", p->S1->value);

                     printf(" with type%s", ASTPrint_Type(p->declared_type));

                     //Checks if p has an assigned value
                     if (p->S2 != NULL)
                        //prints "true" or "false" if variable is boolean
                        if(p->declared_type == A_Decaf_BOOL)
                           printf("= %s", (p->S2->value)?"TRUE":"FALSE");
                        //print routine for an integer assignment
                        else
                           printf("= %d",p->S2->value);

                     printf("\n");
                     break;
        //End case A_VARDEC

        case A_METHODDEC :  
                     printf("METHOD FUNCTION %s%s",p->name, ASTPrint_Type(p->declared_type));

                     /*Checks if there is a paramter list and prints it, otherwise prints (VOID)*/
                     if (p->S1 == NULL ) {
                       printf ("(VOID) ");
                      }
                    //end if
                    else{
                       printf("\n");
                       PT(level+2);
                       printf( "( \n");
                       ASTprint(level+2, p->S1);
                       PT(level+2);
                       printf( ") ");
                      }
                     //end else
                     printf("\n");
                     ASTprint(level+2, p->S2); // print out the block
                     break;
        //End case A_METHODDEC

        case A_EXPR :  printf("EXPR ");

                     //Switch determines what operator to print
                     switch(p->operator) {
                        case A_PLUS : printf(" + ");
                           break;
                        case A_MINUS : printf(" - ");
                           break;
                        case A_TIMES : printf(" * ");
                           break;
                        case A_NOT : printf(" ! ");
                           break;
                        case A_DIVIDE : printf(" / ");
                           break;
                        case A_MOD : printf(" %% ");
                           break;
                        case A_AND : printf(" && ");
                           break;
                        case A_OR : printf(" || ");
                           break;
                        case A_RIGHT_SHIFT : printf(" >> ");
                           break;
                        case A_LEFT_SHIFT : printf(" << ");
                           break;
                        case A_U_MINUS : printf(" - ");
                           break;
                        case A_LEQ : printf(" <= ");
                           break;
                        case A_GEQ : printf(" >= ");
                           break;
                        case A_GT : printf(" > ");
                           break;
                        case A_LT : printf(" < ");
                           break;
                        case A_EQ : printf(" == ");
                           break;
                        case A_NEQ : printf(" != ");
                           break;
                        default: printf(" unknown EXPR operator ");
                       }//end switch

                     printf("\n");

                     ASTprint(level+1, p->S1); //Expr always prints the S1 node since it should never be NULL

                     //ASTprints S2 link if not A_NOT or A_U_MINUS since S2 is NULL in these cases
                     if (p->operator != A_NOT || p->operator != A_U_MINUS)
                         ASTprint(level+1, p->S2);
                     break;
        //End case A_EXPR

        case A_BLOCK :  printf("BLOCK STATEMENT  \n",p->name);
                     ASTprint(level+1, p->S1);
                     ASTprint(level+1, p->S2);
                     break;
        //End case A_BLOCK

        case A_WHILESTMT :  printf("WHILE STATEMENT \n");
                     ASTprint(level+1, p->S1);
                     ASTprint(level+2, p->S2);
                     break;
        //End case A_WHILESTMT

      //ALL of following cases added until the default case:
        case A_PACKAGE :  printf("PACKAGE name %s\n", p->name);
                     ASTprint(level+1, p->S1);
                     printf("\n"); //added WHITESPACE divider
                     ASTprint(level+1, p->S2);
                     break;
        //End case A_PACKAGE

        case A_EXTERN : printf("EXTERN FUNC %s\n", p->name);
                     ASTprint(level+1, p->S1); //prints ExternTypeList
                     printf("END EXTERN with Type: %s\n\n", ASTPrint_Type(p->declared_type)); //Prints the externs declared_type after ')'
                     break;
        //End case A_EXTERN

        case A_EXTERN_TYPE : printf("Extern Type %s\n", ASTPrint_Type(p->declared_type));
                     break;
        //End case A_EXTERN_TYPE

        case A_PROGRAM : ASTprint(level, p->S1);
                     ASTprint(level, p->S2);
                     break;
        //End case A_PROGRAM

        case A_METHOD_IDENTIFIER : printf("PARAMETER%s%s\n", ASTPrint_Type(p->declared_type), p->name);
                     break;
        //End case A_METHOD_IDENTIFIER

        case A_BREAK : printf("BREAK STATEMENT \n");
                     break;
        //End csae A_BREAK

        case A_RETURN : printf("RETURN STATEMENT \n");
                     ASTprint(level+1, p->S1); //RETURN expr if there is one.
                     break;
        //End case A_RETURN

       case A_CONSTANT_INT : printf("INT CONSTANT with value %d\n", p->value);
                     break;
       //End case A_CONSTANT_INT

       case A_CONSTANT_BOOL : printf("BOOL CONSTANT with value ");
                     //Simple if-else statement checks if p->value is true or false
                     if(p->value == 1) printf("TRUE\n");
                     else printf("FALSE\n");
                     break;
       //End case A_CONSTANT_BOOL

       case A_METHOD_CALL : printf("METHOD CALL name: %s\n", p->name);
                     PT(level+1);
                     printf("(\n");
                     ASTprint(level+2, p->S1); //prints called Methods arguements
                     PT(level+1);
                     printf(")\n");
                     break;
       //End case A_METHOD_CALL

       case A_CONSTANT_STRING : printf("STRING CONSTANT %s\n", p->name);
                     break;
       //End case A_CONSTANT_STRING

       case A_ASSIGN : printf("ASSIGNMENT STATEMENT\n");
                     ASTprint(level, p->S1);
                     ASTprint(level+2, p->S2);
                     break;
       //End case A_ASSIGN

       case A_IF : printf("IF STATEMENT\n");
                        //Directly prints child nodes from A_IF_BLOCK
                        ASTprint(level+1, p->S1->S1);
                        ASTprint(level+3, p->S1->S2);
                        
                        //Checks if A_IF has an ELSE statement
                        //and prints A_ELSE_BLOCK children directly if it does
                        if(p->S2 != NULL){
                            PT(level+2);
                            printf("ELSE STATEMENT\n");
                            ASTprint(level+3, p->S2->S1);
                        }
                        break;
       //End case A_IF

       case A_VAR_LVALUE : printf("Variable %s\n", p->name);
                           //Checks if variable is also an array with corresponding print routine
                           if(p->S1 != NULL){
                              PT(level);
                              printf("[\n");
                              ASTprint(level+1,p->S1);
                              PT(level);
                              printf("]\n");
                           }
                           break;
       //End case A_VAR_LVALUE

       case A_VAR_RVALUE : printf("Variable %s\n", p->name);
                           //Checks if variable is also an array with corresponding print routine
                           if(p->S1 != NULL){
                              PT(level);
                              printf("[\n");
                              ASTprint(level+1,p->S1);
                              PT(level);
                              printf("]\n");
                           }
                           break;
       //End case A_VAR_RVALUE
        
       case A_METHOD_ARG : printf("METHOD ARG\n");
                           ASTprint(level, p->S1);
                           break;
       //End case A_METHOD_ARG

       case A_CONTINUE : printf("CONTINUE STATEMENT\n");
                           break;
       //End case A_CONTINUE

        default: printf("unknown type in ASTprint\n");


       }//end of switch

       ASTprint(level, p->next); //Automatically prints the "next" node of each p.

     }//end of else

}//end of ASTprint



/* dummy main program so I can compile for syntax error independently
void main()
{
}
*/
