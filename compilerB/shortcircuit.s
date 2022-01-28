	.text
_true:
	sw    $ra, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	sw    $fp, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	addu  $fp, $sp, 8
	subu  $sp, $sp, 0		#=SPACE FOR STACK=
	.data
.L0:	.asciiz"RETURNING TRUE\n"
	.text
	la    $t0, .L0		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	li    $t0, 1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $v0, 4($sp)	#POP
	addu  $sp, $sp, 4
	lw    $ra, 0($fp)
	move  $t0, $fp
	lw    $fp, -4($fp)
	move  $sp, $t0
	jr    $ra		#RETURN
	lw    $ra, 0($fp)
	move  $t0, $fp
	lw    $fp, -4($fp)
	move  $sp, $t0
	jr    $ra
	.text
_false:
	sw    $ra, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	sw    $fp, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	addu  $fp, $sp, 8
	subu  $sp, $sp, 0		#=SPACE FOR STACK=
	.data
.L1:	.asciiz"RETURNING FALSE\n"
	.text
	la    $t0, .L1		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	li    $t0, 0
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $v0, 4($sp)	#POP
	addu  $sp, $sp, 4
	lw    $ra, 0($fp)
	move  $t0, $fp
	lw    $fp, -4($fp)
	move  $sp, $t0
	jr    $ra		#RETURN
	lw    $ra, 0($fp)
	move  $t0, $fp
	lw    $fp, -4($fp)
	move  $sp, $t0
	jr    $ra
	.text
	.globl main
main:
__start:
	sw    $ra, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	sw    $fp, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	addu  $fp, $sp, 8
	subu  $sp, $sp, 0		#=SPACE FOR STACK=
	.data
.L2:	.asciiz"=================\n"
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L3
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 1
	and   $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L4
.L3:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L4:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L5		#Jump to else if false
	jr    .L6		#Jump to exit of IfThenElse
.L5:
	.data
.L7:	.asciiz"Bad\n"
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
.L6:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L8
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 1
	and   $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L9
.L8:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L9:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L10		#Jump to else if false
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jr    .L11		#Jump to exit of IfThenElse
.L10:
.L11:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L12
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 1
	and   $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L13
.L12:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L13:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L14		#Jump to else if false
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jr    .L15		#Jump to exit of IfThenElse
.L14:
.L15:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L16
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 1
	and   $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L17
.L16:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L17:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L18		#Jump to else if false
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jr    .L19		#Jump to exit of IfThenElse
.L18:
.L19:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 1
	beq   $t0, $t1, .L20
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 0
	or    $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L21
.L20:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L21:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L22		#Jump to else if false
	jr    .L23		#Jump to exit of IfThenElse
.L22:
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
.L23:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 1
	beq   $t0, $t1, .L24
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 0
	or    $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L25
.L24:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L25:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L26		#Jump to else if false
	jr    .L27		#Jump to exit of IfThenElse
.L26:
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
.L27:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 1
	beq   $t0, $t1, .L28
	jal   _true
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 0
	or    $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L29
.L28:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L29:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L30		#Jump to else if false
	jr    .L31		#Jump to exit of IfThenElse
.L30:
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
.L31:
	.text
	la    $t0, .L2		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 1
	beq   $t0, $t1, .L32
	jal   _false
	addu  $sp, $sp, 0		#REMOVE PARAM
	sw    $v0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $t1, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t0, 0
	or    $t0, $t0, $t1
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	jr    .L33
.L32:
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
.L33:
	lw    $t0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $t1, 0
	beq   $t0, $t1, .L34		#Jump to else if false
	.text
	la    $t0, .L7		#STRLIT
	sw    $t0, 0($sp)	#PUSH
	subu  $sp, $sp, 4
	lw    $a0, 4($sp)	#POP
	addu  $sp, $sp, 4
	li    $v0, 4
	syscall
	jr    .L35		#Jump to exit of IfThenElse
.L34:
.L35:
	lw    $ra, 0($fp)
	move  $t0, $fp
	lw    $fp, -4($fp)
	move  $sp, $t0
	li    $v0, 10
	syscall
