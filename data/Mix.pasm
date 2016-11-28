; mixed Compute and I/O

	C					; start with some computation
L2	I					; and brief I/O
L1	C					; do some computing
	C
	C
	C
	B 99 L1				; normally go do it again
	B 75 L2
	X					; done