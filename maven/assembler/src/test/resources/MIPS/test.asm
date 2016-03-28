
.text
	lbu $1, 24($0)
	lbu $2, 25($0)
	lbu $5, 26($0)
	lbu $6, 27($0)

maths:
	add $1, $0, $0
	sub $0, $5, $6

	syscall

	blt $8, $9, 23

.data
a: .byte   0x1
b: .byte   0x2, 0x3
c: .byte   0x4
