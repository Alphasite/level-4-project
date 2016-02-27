; ArrayMax: find the maximum element of an array
;
; The program is given
;   *  a natural number n, assume n>0
;   *  an n-element array x[0], x[1], ..., x[n-1]
;  It calculates
;   * max = the maximum element of x

; Since n>0, the array x contains at least one element,
; and a maximum element is guaranteed to exist.
;
; program ArrayMax
;   max := x[0]
;   for i := 1 to n-1 step 1
;       if x[i] > max
;         then max := x[i]

; Register usage
;   R1 = constant 1
;   R2 = n
;   R3 = i
;   R4 = max

; Initialise

       ldr   X1, 1             ; R1 = constant 1
       ldr  X2, R0, #n         ; R2 = n
       ldr   X3, 1             ; R3 = i = 1
       ldr  X4, R0, #x         ; R4 = max = x[0]

; Top of loop, determine whether to remain in loop

loop:
       cmp X3,X2               ; R5 = (i<n)
       b.lt #done              ; if i>=n then goto done

; if x[i] > max

       ldr  X5, R3, #x         ; R5 = x[i]
       cmp X5, X4              ; R6 = (x[i]>max)
       b.lt R6, X0, next       ; if x[i] <= max then goto neg

; then max := x[i]

       add   X4,X5,X0          ; max := x[i]

; Bottom of loop, increment loop index

next:  add   X3,X3,X1          ; i = i + 1
       b  loop                 ; go to top of loop

; Exit from loop

done   str X4, X0, #max        ; max = R4
       svc  0                  ; terminate

; Data area
n        .data   6
max      .data   0
x        .data  18
         .data   3
         .data  21
         .data  -2
         .data  40
         .data  25
