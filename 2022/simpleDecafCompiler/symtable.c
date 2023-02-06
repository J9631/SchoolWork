/*
    Jacob Sanchez
    October 31, 2022
    CS 370, LAB9

    The following code is a Symbol Table implemented with a linked list structure.
    The table has levels indicating the scope of each function starting at 0.

 */

    
#include <string.h>
#include "ast.h"
#include "symtable.h"

//Global symTab pointer that points to first node of the SymTab linked list
struct SymbTab * first=NULL;

int GTEMP = 0; // One up number for creating temps

//PRE: None
//POST: A temporary name string in the format_Tn where n is an integer is returned.
char* Create_Temp(){
  char s[100];
  sprintf(s, "_T%d", GTEMP);
  GTEMP++;
  return(strdup(s));
}


/* Simple Insert into the symbol table with the size, type level that the name is being inserted into */
//PRE: Called with a non-null name and valid type and subtype in symtable.h alongside numbers for a level, size, and offset with a pointer to its parameters if it has any.
//POST: A symbol with a type, subtype, level, size, offset, and a pointer to its parameters (if it has any) is inserted into the symbol table.
struct SymbTab * Insert(char *name, enum AST_Decaf_Types Type, enum ID_Sub_Type subtype , int  level, int mysize, int offset , ASTnode * fparms)

{
  struct SymbTab * n;
    n=Search(name,level, 0);
    if(n!=NULL)
      {
      printf("\n\tThe name %s exists at level %d already in the symbol table\n\tDuplicate can.t be inserted",name, level);
      return (NULL);
      }
    else
    {
      struct SymbTab *p;
      p=malloc(sizeof(struct SymbTab));
      p->name=name;
      p->offset=offset;  /* assign the offset */
      p->level=level;  /* assign the level */
      p->mysize=mysize;  /* assign the size */
      p->Type=Type;  /* assign the Type */
      p->SubType=subtype;  /* assign the Function  */
      p->fparms=fparms;  /* assign the Method parameter list  */
      p->next=NULL;

   /* Check on how many elements we have in the symbol table */
      if(first==NULL)
      {
        first=p;
      }
      else
      {
        p->next=first;
        first=p;
      }
      return (p);
 
    }
     
  printf("\n\tLabel inserted\n");
}

/* print out a single symbol table entry -- for debugging */
//PRE: s points to a SymbTab that isn't null.
//POST: The name, offset, display type, and subtype of s are printed.
void PrintSym(struct SymbTab *s)
{
	char *string,*display_type; 

     //determines the subtype to be printed
	 switch(s->SubType) {
		 case ID_Sub_Type_Scalar: string = "Scalar " ; break;

         //ADDED:
         case ID_Sub_Type_ExternMethod: string = "Extern Method "; break;
         case ID_Sub_Type_Method: string = "Method " ; break;
         case ID_Sub_Type_Array: string = "Array " ; break;
         case ID_Sub_Type_Package: string = "Package "; break;

		 default:  string ="Unknown subtype in print symbol";
	 }//end switch

	 //determines the display_type to be printed
	 switch(s->Type) {
		 case A_Decaf_INT: display_type = "INT " ; break;
         //ADDED:
         case A_Decaf_BOOL: display_type = "BOOL "; break;
         case A_Decaf_VOID: display_type = "VOID "; break;
         case A_Decaf_STRING: display_type = "STRING "; break;

		 default:  string ="Unknown Declared TYPE in print symbol";
	 }//end switch

     //printf("\t%s\t\t%d\t%d\t%s\t%s\n",s->name,s->offset, s->level,display_type,string);
     //TEMP: Altered to also print mySize
     printf("\t%s\t\t%d\t%d\t%d\t%s\t%s\n",s->name,s->offset, s->mysize, s->level,display_type,string);

}//end PrintSym


/*  General display to see what is our symbol table */
//PRE: first is a pointer to SymbTab.
//POST: The linked list "first" is the head of is printed with each elements information arranged in columns using PrintSym.
void Display()
{
   int i;
   struct SymbTab *p;
   p=first;

   //printf("\n\tLABEL\t\tOffset\tLEVEL\ttype\tsubtype\n");
   printf("\n\tLABEL\t\tOffset\tSIZE\tLEVEL\ttype\tsubtype\n"); //TEMP: Altered to also print mySize

      //Goes through linked list printing each element.
      while (p!=NULL)
      {
         PrintSym(p);
         p=p->next;
      }//end while
}//end Display()



/*  Search for a symbol name at level or below.  We have to do multiple passes into the symbol table because we have to find
   the name closest to us 


  If recur is non-zero, then we look through all of the levels, otherwise, only our level 
   We return a pointer to a SymbolTab structure so that we can use other functions/methods to get the attributes */

//PRE: None.
//POST: Returns a pointer to a SymTab contianing the symbol at the level, or NULL if not found.
struct SymbTab * Search(char name[], int level, int recur)
{
   int i,flag=0;
   struct SymbTab *p;

  /* for each level, try to find our symbol returning SymbTab if found*/
   while (level >= 0)
    {
       p=first;
       while (p!=NULL)
        {
         if((strcmp(p->name,name)==0) && (p->level == level))
           return p;
         p=p->next;
        }
       if (recur == 0) return (NULL);   /* we did not find it at our level */
       level--; /* check the next level up */
    }//End while


   return  NULL;  /* did not find it, return 0 */
}//end Search



/* Remove all enteries that have the indicated level
   We need to take care about updating first pointer into the linked list when we are deleting edge elements */

//PRE: level is a vaiable address inside the symbol table
//POST: the symbol at the address level is removed from the linked list
int Delete(int level)
{
    struct SymbTab *p,*f=NULL;  /* we follow with pointer f */
    int SIZE=0;
    p=first;

    
    
    /* cruise through the list */
    while (p != NULL)
      {
        /* do we match? */
        if (p->level >= level ){ 
           /* if it is the first in the list we have to update first, we know this by f being NULL */
           SIZE += p->mysize;

           if ( f == NULL) first = p->next;
           else /* not the first element */
           {
             f->next = p->next;
           }//end else

           p = p->next;
           
        }//End if
        else{
               /* update follow pointer, move the p pointer */
                f = p;
                p = p->next;
        }//End else

      }//End while
      
    return(SIZE);
}//End Delete

