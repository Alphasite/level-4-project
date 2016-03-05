Your task (should you choose to accept it) is to: 

1. First do the tutorial, and glance through the manual to get a grasp of it. This skills you learn here will help you 
with the remaining tasks. Feel free to refer back to the files produced from it if you get stuck.

RX instructions are a fixed 16 bits.
RR instructions are a fixed 32 bits, 16 for the instruction, 16bits for the label offset. 

Sigma16 has instructions in 2 forms, RX instructions, which have the form of:
 - 4 bits for sigill
 - 4 bits for destination register
 - 4 bits for regA
 - 4 bits for regB

And RR instructions:
 - 4 bits for the value: 0xF (indicates that its an RR instruction)
 - 4 bits destination register
 - 4 bits for the offset
 - 4 bits to identify which RR instruction this is.
 - 16 bits for a label address

2. Your first task is to add a new instruction definition to the Sigma16 assembler. Your goal is to add a new instruction called 'loadxi', all 
it does is combine a load instruction and an increment instruction into a single instruction. 

 'loadxi', like load, is a RR instruction, with the identity value of 0x7.

3. Your second task is to add a second 'add' instruction, except this one has a 4 bit literal as its 3rd argument, 
instead of a register. It should share the name "add" but it should select the specific instruction based on that 
argument (hint, use meta to alias the 2 instructions onto the same mnemonic). The sigill for this is 0x3.

4. Your third task is to add a new literal, the binary type. These are written as |1001| and are 4 bits long (hint: the 
correct type is BINARY). It isn't fair to ask you to write the Regular expression without a reference, hence I have
provided you with them: "|\\d+|" and "|(\\d+)|" 

