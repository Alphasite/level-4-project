
.text
	lbu $1, 24($0)
	lbu $2, 25($0)
	lbu $5, 26($0)
	lbu $6, 27($0)

	add $1, $0, $0
	sub $0, $5, $6

	syscall

.data
a: .byte   0x1
b: .byte   0x2, 0x3
c: .byte   0x4
