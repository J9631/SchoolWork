/*
    Jacob Sanchez
    October 31, 2022
    CS 370, LAB9

	Its assumed that the input for this program is a simple DECAF program which it will chunk into tokens that YACC will check the syntax
	on then create an abstract syntax tree with a symbol table using syntax directed semantic action.
*/

%{

int mydebug=0;
int line = 1;
#include "ast.h"
#include "y.tab.h"

//Precondition: The parameter s is a string representing a hexadecimal with the prefix "0x" or "0X".
//Postcondition: The integer value the hexadecimal represented is returned.
int HexToInt(const char* s){
    //Index starts at 2 so the "0x" prefix isn't counted towards the value
    int index = 2;
    int value = 0;

    //As long the null character isn't found (where string terminates) iterates through s and updates value
    while(s[index] != '\0'){
        /*Due to the regex that calls this method any char who's ASCII is < 'A' must be 0-9
          This conditional and the following then convert the char into an integer by subtracting ASCII values then
          adds it onto value*16 since hex is base 16.
        */
        if(s[index] < 'A')
            value = value*16 + (s[index] - '0');
        //At this point any char <= 'Z' must be an uppercase character
        else if(s[index] <= 'Z')
            value = value*16 + 10 + (s[index] - 'A');
        //Everything else must be a lowercase character
        else
            value = value*16 + 10 + (s[index] - 'a');
        index++;
    }

    //printf("Hex value on line %d has a decimal value of %d\n", line, value); //Code to test HexToInt
    return value;
}

%}


%%
\/\/.*            {/* Return Nothing */};

"while"           {return(T_WHILE);}
"var"             {return(T_VAR);}
"true"            {return(T_TRUE);}
">>"              {return(T_RIGHTSHIFT);}
"return"          {return(T_RETURN);}
"||"              {return(T_OR);}
"null"            {return(T_NULL);}
"!="              {return(T_NEQ);}
"<="              {return(T_LEQ);}
"<<"              {return(T_LEFTSHIFT);}
"if"              {return(T_IF);}
">"               {return(T_GT);}
"<"               {return(T_LT);}
">="              {return(T_GEQ);}
"false"           {return(T_FALSE);}
"for"             {return(T_FOR);}
"extern"          {return(T_EXTERN);}
"=="              {return(T_EQ);}
"else"            {return(T_ELSE);}
"\."              {return(T_DOT);}
"continue"        {return(T_CONTINUE);}
"break"           {return(T_BREAK);}
"bool"            {return(T_BOOLTYPE);}
"="               {return(T_ASSIGN);}
"&&"              {return(T_AND);}
"string"          {return(T_STRINGTYPE);}
"func"            {return(T_FUNC);}
"void"            {return(T_VOID);}
"package"         {return(T_PACKAGE);}
"int"             {return(T_INTTYPE);}

\".*\"            {yylval.string = strdup(yytext); return(T_STRINGCONSTANT);}
\'.\'             {yylval.value = yytext[1]; return(T_CHARCONSTANT);}
[a-zA-Z][a-zA-Z0-9_]*  { yylval.string = strdup(yytext); return(T_ID);}

"0"(x|X)[a-fA-F0-9]+ {yylval.value = HexToInt((const char*)yytext); return(T_INTCONSTANT);}

[0-9][0-9]*     { yylval.value = atoi((const char *)yytext); return(T_INTCONSTANT);}

[ \t]           {/*Whitespace*/ }
[;()\-+*/%{},[\]!] 	{ return (*yytext);}
\n              { line++;}

%%

int yywrap(void)
{ return 1;}


