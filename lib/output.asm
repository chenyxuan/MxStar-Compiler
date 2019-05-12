





default rel

global main
global ans

extern printf
extern _GLOBAL_OFFSET_TABLE_


SECTION .text   

main:
        push    rbp
        mov     rbp, rsp
        sub     rsp, 16
        mov     dword [rbp-4H], 328
        mov     dword [rbp-8H], 0
L_001:  mov     eax, dword [rbp-8H]
        cmp     eax, dword [rbp-4H]
        jge     L_002
        mov     eax, dword [rbp-8H]
        cdqe
        lea     rdx, [rax*4]
        lea     rax, [rel ans]
        mov     eax, dword [rdx+rax]
        mov     esi, eax
        lea     rdi, [rel L_003]
        mov     eax, 0
        call    printf
        add     dword [rbp-8H], 1
        jmp     L_001

L_002:  mov     eax, 0
        leave
        ret



SECTION .data   align=32

ans:
        db 2AH, 0F7H, 0FFH, 0FFH, 2AH, 0F7H, 0FFH, 0FFH
        db 6CH, 04H, 00H, 00H, 0A4H, 03H, 00H, 00H
        db 0F8H, 03H, 00H, 00H, 0C3H, 0FH, 00H, 00H
        db 5BH, 0AH, 00H, 00H, 76H, 19H, 00H, 00H
        db 70H, 0FH, 00H, 00H, 37H, 05H, 00H, 00H
        db 0FFH, 05H, 00H, 00H, 2EH, 07H, 00H, 00H
        db 3CH, 12H, 00H, 00H, 2FH, 1CH, 00H, 00H
        db 2EH, 08H, 00H, 00H, 78H, 0FEH, 0FFH, 0FFH
        db 54H, 08H, 00H, 00H, 95H, 15H, 00H, 00H
        db 0D7H, 01H, 00H, 00H, 0D6H, 0F5H, 0FFH, 0FFH
        db 0C9H, 0FEH, 0FFH, 0FFH, 05H, 0F7H, 0FFH, 0FFH
        db 8BH, 03H, 00H, 00H, 0FDH, 0EFH, 0FFH, 0FFH
        db 8BH, 03H, 00H, 00H, 0AAH, 06H, 00H, 00H
        db 74H, 03H, 00H, 00H, 7DH, 08H, 00H, 00H
        db 7AH, 0F1H, 0FFH, 0FFH, 85H, 0FFH, 0FFH, 0FFH
        db 6AH, 13H, 00H, 00H, 6AH, 13H, 00H, 00H
        db 5AH, 10H, 00H, 00H, 26H, 0FBH, 0FFH, 0FFH
        db 5AH, 10H, 00H, 00H, 0DBH, 05H, 00H, 00H
        db 1BH, 0FAH, 0FFH, 0FFH, 58H, 02H, 00H, 00H
        db 03H, 0BH, 00H, 00H, 03H, 0BH, 00H, 00H
        db 1FH, 0FH, 00H, 00H, 0E4H, 0FCH, 0FFH, 0FFH
        db 0B4H, 0FEH, 0FFH, 0FFH, 0F3H, 0BH, 00H, 00H
        db 79H, 01H, 00H, 00H, 00H, 00H, 00H, 00H
        db 28H, 09H, 00H, 00H, 9AH, 0F7H, 0FFH, 0FFH
        db 28H, 09H, 00H, 00H, 28H, 09H, 00H, 00H
        db 00H, 00H, 00H, 00H, 0B2H, 0F7H, 0FFH, 0FFH
        db 0B0H, 0FCH, 0FFH, 0FFH, 90H, 00H, 00H, 00H
        db 6FH, 00H, 00H, 00H, 13H, 0AH, 00H, 00H
        db 13H, 0AH, 00H, 00H, 05H, 05H, 00H, 00H
        db 37H, 08H, 00H, 00H, 48H, 11H, 00H, 00H
        db 32H, 0F5H, 0FFH, 0FFH, 0D2H, 0F7H, 0FFH, 0FFH
        db 11H, 0EH, 00H, 00H, 5DH, 0EEH, 0FFH, 0FFH
        db 53H, 1CH, 00H, 00H, 53H, 1CH, 00H, 00H
        db 72H, 0BH, 00H, 00H, 5DH, 18H, 00H, 00H
        db 4AH, 08H, 00H, 00H, 7AH, 04H, 00H, 00H
        db 4AH, 08H, 00H, 00H, 0AEH, 0BH, 00H, 00H
        db 3FH, 0F3H, 0FFH, 0FFH, 9CH, 05H, 00H, 00H
        db 94H, 0FFH, 0FFH, 0FFH, 0D8H, 03H, 00H, 00H
        db 0AH, 00H, 00H, 00H, 0D8H, 03H, 00H, 00H
        db 0A4H, 0FEH, 0FFH, 0FFH, 0FAH, 0F7H, 0FFH, 0FFH
        db 7DH, 0E6H, 0FFH, 0FFH, 0A4H, 0FEH, 0FFH, 0FFH
        db 0A4H, 0FEH, 0FFH, 0FFH, 8FH, 0FBH, 0FFH, 0FFH
        db 9EH, 0FCH, 0FFH, 0FFH, 0DH, 08H, 00H, 00H
        db 07H, 0EEH, 0FFH, 0FFH, 3FH, 0CH, 00H, 00H
        db 96H, 0AH, 00H, 00H, 59H, 03H, 00H, 00H
        db 96H, 0AH, 00H, 00H, 00H, 00H, 00H, 00H
        db 0A0H, 08H, 00H, 00H, 0FFH, 0FAH, 0FFH, 0FFH
        db 44H, 06H, 00H, 00H, 27H, 0F9H, 0FFH, 0FFH
        db 0C2H, 04H, 00H, 00H, 66H, 07H, 00H, 00H
        db 0E5H, 0FFH, 0FFH, 0FFH, 55H, 05H, 00H, 00H
        db 0E7H, 00H, 00H, 00H, 00H, 00H, 00H, 00H
        db 26H, 03H, 00H, 00H, 91H, 0FCH, 0FFH, 0FFH
        db 26H, 03H, 00H, 00H, 3CH, 0FFH, 0FFH, 0FFH
        db 3CH, 0FFH, 0FFH, 0FFH, 0CEH, 0F8H, 0FFH, 0FFH
        db 80H, 02H, 00H, 00H, 69H, 0BH, 00H, 00H
        db 0B3H, 0BH, 00H, 00H, 7EH, 01H, 00H, 00H
        db 8CH, 04H, 00H, 00H, 0D3H, 01H, 00H, 00H
        db 48H, 07H, 00H, 00H, 0F2H, 05H, 00H, 00H
        db 0B0H, 11H, 00H, 00H, 9BH, 0F5H, 0FFH, 0FFH
        db 2EH, 00H, 00H, 00H, 0D8H, 0BH, 00H, 00H
        db 0DFH, 0FFH, 0FFH, 0FFH, 0F3H, 0F6H, 0FFH, 0FFH
        db 1AH, 05H, 00H, 00H, 5BH, 04H, 00H, 00H
        db 0FDH, 0F8H, 0FFH, 0FFH, 61H, 0FCH, 0FFH, 0FFH
        db 61H, 0F7H, 0FFH, 0FFH, 0B4H, 06H, 00H, 00H
        db 00H, 00H, 00H, 00H, 0B4H, 06H, 00H, 00H
        db 3AH, 1CH, 00H, 00H, 0BCH, 02H, 00H, 00H
        db 0ACH, 0FDH, 0FFH, 0FFH, 7FH, 09H, 00H, 00H
        db 18H, 0FEH, 0FFH, 0FFH, 0BBH, 12H, 00H, 00H
        db 0BBH, 12H, 00H, 00H, 0BBH, 12H, 00H, 00H
        db 0BBH, 12H, 00H, 00H, 2FH, 01H, 00H, 00H
        db 0BBH, 12H, 00H, 00H, 23H, 04H, 00H, 00H
        db 45H, 05H, 00H, 00H, 45H, 05H, 00H, 00H
        db 45H, 05H, 00H, 00H, 47H, 0FBH, 0FFH, 0FFH
        db 3BH, 0BH, 00H, 00H, 0AAH, 07H, 00H, 00H
        db 01H, 0FH, 00H, 00H, 83H, 07H, 00H, 00H
        db 43H, 19H, 00H, 00H, 0E6H, 09H, 00H, 00H
        db 0C1H, 0F4H, 0FFH, 0FFH, 0E6H, 09H, 00H, 00H
        db 7AH, 00H, 00H, 00H, 3DH, 01H, 00H, 00H
        db 0E4H, 0FCH, 0FFH, 0FFH, 49H, 0FAH, 0FFH, 0FFH
        db 0EAH, 04H, 00H, 00H, 0EAH, 04H, 00H, 00H
        db 0EAH, 04H, 00H, 00H, 98H, 00H, 00H, 00H
        db 20H, 12H, 00H, 00H, 3EH, 10H, 00H, 00H
        db 00H, 0AH, 00H, 00H, 0B3H, 0FFH, 0FFH, 0FFH
        db 52H, 0FBH, 0FFH, 0FFH, 0E0H, 02H, 00H, 00H
        db 0E2H, 01H, 00H, 00H, 08H, 02H, 00H, 00H
        db 24H, 0EH, 00H, 00H, 59H, 00H, 00H, 00H
        db 0DAH, 0EH, 00H, 00H, 0FEH, 10H, 00H, 00H
        db 92H, 0BH, 00H, 00H, 0FEH, 10H, 00H, 00H
        db 0B6H, 16H, 00H, 00H, 93H, 0F9H, 0FFH, 0FFH
        db 3CH, 0DH, 00H, 00H, 5FH, 0DH, 00H, 00H
        db 88H, 03H, 00H, 00H, 0D4H, 0F9H, 0FFH, 0FFH
        db 0C9H, 03H, 00H, 00H, 0C9H, 03H, 00H, 00H
        db 17H, 0FEH, 0FFH, 0FFH, 39H, 0FFH, 0FFH, 0FFH
        db 95H, 13H, 00H, 00H, 0A2H, 0AH, 00H, 00H
        db 0CFH, 04H, 00H, 00H, 0DFH, 29H, 00H, 00H
        db 0FDH, 00H, 00H, 00H, 34H, 10H, 00H, 00H
        db 89H, 0DH, 00H, 00H, 3CH, 08H, 00H, 00H
        db 0CCH, 13H, 00H, 00H, 35H, 15H, 00H, 00H
        db 62H, 16H, 00H, 00H, 86H, 08H, 00H, 00H
        db 0FH, 00H, 00H, 00H, 00H, 00H, 00H, 00H
        db 3AH, 02H, 00H, 00H, 0F9H, 11H, 00H, 00H
        db 5CH, 00H, 00H, 00H, 0C5H, 12H, 00H, 00H
        db 50H, 06H, 00H, 00H, 53H, 01H, 00H, 00H
        db 03H, 04H, 00H, 00H, 21H, 03H, 00H, 00H
        db 30H, 06H, 00H, 00H, 9CH, 12H, 00H, 00H
        db 9CH, 12H, 00H, 00H, 0E9H, 0BH, 00H, 00H
        db 48H, 03H, 00H, 00H, 0DH, 24H, 00H, 00H
        db 03H, 1BH, 00H, 00H, 19H, 0BH, 00H, 00H
        db 0C6H, 01H, 00H, 00H, 00H, 00H, 00H, 00H
        db 64H, 0EH, 00H, 00H, 0D9H, 0FDH, 0FFH, 0FFH
        db 0ADH, 04H, 00H, 00H, 0D5H, 0DH, 00H, 00H
        db 2FH, 0F9H, 0FFH, 0FFH, 0B5H, 09H, 00H, 00H
        db 0B5H, 09H, 00H, 00H, 0DAH, 0E9H, 0FFH, 0FFH
        db 03H, 0F8H, 0FFH, 0FFH, 0F0H, 0F3H, 0FFH, 0FFH
        db 6CH, 09H, 00H, 00H, 6CH, 09H, 00H, 00H
        db 99H, 05H, 00H, 00H, 9CH, 0F8H, 0FFH, 0FFH
        db 0A2H, 0E5H, 0FFH, 0FFH, 00H, 00H, 00H, 00H
        db 43H, 01H, 00H, 00H, 97H, 0EH, 00H, 00H
        db 16H, 0FEH, 0FFH, 0FFH, 85H, 0DH, 00H, 00H
        db 5FH, 0F2H, 0FFH, 0FFH, 0CEH, 0F4H, 0FFH, 0FFH
        db 00H, 00H, 00H, 00H, 1BH, 0BH, 00H, 00H
        db 02H, 0F9H, 0FFH, 0FFH, 1BH, 0BH, 00H, 00H
        db 00H, 00H, 00H, 00H, 1BH, 0BH, 00H, 00H
        db 0EBH, 0FFH, 0FFH, 0FFH, 2EH, 04H, 00H, 00H
        db 24H, 0FBH, 0FFH, 0FFH, 0C9H, 08H, 00H, 00H
        db 0AEH, 0EEH, 0FFH, 0FFH, 0BAH, 06H, 00H, 00H
        db 0BAH, 06H, 00H, 00H, 19H, 0EH, 00H, 00H
        db 45H, 0F9H, 0FFH, 0FFH, 0E3H, 09H, 00H, 00H
        db 11H, 0FDH, 0FFH, 0FFH, 0F2H, 1CH, 00H, 00H
        db 06H, 05H, 00H, 00H, 59H, 0F5H, 0FFH, 0FFH
        db 71H, 09H, 00H, 00H, 11H, 16H, 00H, 00H
        db 0A4H, 03H, 00H, 00H, 0BDH, 0FH, 00H, 00H
        db 0CEH, 0F6H, 0FFH, 0FFH, 0BCH, 0EH, 00H, 00H
        db 8AH, 02H, 00H, 00H, 8CH, 0FCH, 0FFH, 0FFH
        db 1EH, 02H, 00H, 00H, 87H, 12H, 00H, 00H
        db 7CH, 17H, 00H, 00H, 7CH, 17H, 00H, 00H
        db 0EBH, 05H, 00H, 00H, 98H, 00H, 00H, 00H
        db 5FH, 0FFH, 0FFH, 0FFH, 90H, 0BH, 00H, 00H
        db 91H, 0EH, 00H, 00H, 48H, 13H, 00H, 00H
        db 90H, 02H, 00H, 00H, 73H, 14H, 00H, 00H
        db 73H, 14H, 00H, 00H, 73H, 14H, 00H, 00H
        db 0F9H, 02H, 00H, 00H, 01H, 1CH, 00H, 00H
        db 90H, 02H, 00H, 00H, 0FAH, 0FEH, 0FFH, 0FFH
        db 62H, 03H, 00H, 00H, 5FH, 03H, 00H, 00H
        db 0D6H, 01H, 00H, 00H, 0E5H, 09H, 00H, 00H
        db 0C1H, 0FAH, 0FFH, 0FFH, 0F3H, 06H, 00H, 00H
        db 66H, 06H, 00H, 00H, 66H, 06H, 00H, 00H
        db 0E4H, 0FDH, 0FFH, 0FFH, 0A8H, 17H, 00H, 00H
        db 3FH, 0FFH, 0FFH, 0FFH, 4DH, 15H, 00H, 00H
        db 4AH, 0EH, 00H, 00H, 7DH, 0AH, 00H, 00H
        db 7DH, 0AH, 00H, 00H, 50H, 08H, 00H, 00H
        db 50H, 08H, 00H, 00H, 0D9H, 0FEH, 0FFH, 0FFH
        db 0F2H, 0BH, 00H, 00H, 8AH, 0F9H, 0FFH, 0FFH
        db 50H, 08H, 00H, 00H, 32H, 01H, 00H, 00H
        db 0B3H, 0FDH, 0FFH, 0FFH, 04H, 11H, 00H, 00H
        db 9DH, 0FDH, 0FFH, 0FFH, 96H, 0F4H, 0FFH, 0FFH
        db 16H, 01H, 00H, 00H, 0D3H, 0F8H, 0FFH, 0FFH
        db 00H, 00H, 00H, 00H, 0D0H, 07H, 00H, 00H
        db 02H, 03H, 00H, 00H, 0D0H, 07H, 00H, 00H
        db 0D0H, 07H, 00H, 00H, 19H, 0CH, 00H, 00H
        db 19H, 0CH, 00H, 00H, 19H, 0CH, 00H, 00H
        db 89H, 0AH, 00H, 00H, 7DH, 07H, 00H, 00H
        db 25H, 09H, 00H, 00H, 0BEH, 0FH, 00H, 00H
        db 0BEH, 0FH, 00H, 00H, 0C3H, 18H, 00H, 00H


SECTION .bss    


SECTION .rodata 

L_003:
        db 25H, 64H, 0AH, 00H


