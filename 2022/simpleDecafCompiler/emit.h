/*
    Jacob Sanchez
    October 31, 2022
    CS 370, LAB9

	The header file for emit.c provides access to only the EMIT() function as its the only
	method needed outside of emit.c itself for lab9.y to pass the generated AST and a file into
	where the GAS code will be generated.
*/

#include "ast.h"

#ifndef EMIT_H
#define EMIT_H

//Only function that needs to be available
void EMIT(ASTnode *p, FILE *fp);

#endif
