	lea R1,1[R0]
	lea R2,2[R0]
	lea R3,256[R0]

	mul R5,R1,R1
	mul R6,R1,R2
	mul R7,R2,R2
	mul R8,R2,R3
	mul R9,R3,R3

	store R5,result1[R0]
	store R6,result2[R0]
	store R7,result3[R0]
	store R8,result4[R0]
	store R9,result5[R0]

	trap  R0,R0,R0

result1
	data 0
result2
	data 0
result3
	data 0
result4
	data 0
result5
	data 0
