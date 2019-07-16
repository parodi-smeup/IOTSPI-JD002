     V*=====================================================================
     V* MODIFICHE Ril.  T Au Descrizione
     V* gg/mm/aa  nn.mm i xx Breve descrizione
     V*=====================================================================
     V* 19/10/18  V5R1   BMA Created
     V* 05/02/19  V5R1   BMA Comments translated to english
     V* 09/05/19  V5R1   BMA Corrected eval $$SVAR
     V* 19/06/19  V5R1   BMA Get Targa attribute without p_rxval and p_rxlate
     V* 25/06/19  V5R1   AD  Move entry from £INIZI to main
     V* 25/06/19  V5R1   AD  IERROR from Boolean to String
     V* 25/06/19  V5R1   AD  Remove boolean indicator (50) on call
     V* 26/06/19  V5R1   AD  Add fun/met and change entry to handle LAO37_xx parameters
     V* 26/06/19  V5R1   AD  Remove unused XML parse
     V*=====================================================================
     H/COPY QILEGEN,£INIZH
      *---------------------------------------------------------------
     I/COPY QILEGEN,£TABB£1DS
     I/COPY QILEGEN,£PDS
      * Buffer received from socket
     D BUFFER          S          30000
      * Lenght buffer received
     D BUFLEN          S              5  0
      * Error indicator
     D IERROR          S              1
      * XML
     D $XML            S          30000    VARYING
      *---------------------------------------------------------------
     D $$SVAR          S           4096
      *---------------------------------------------------------------
      * ENTRY
      * . Function
     D U$FUNZ          S             10
      * . Method
     D U$METO          S             10
      * . Array of Variables
     D U$SVARSK        S                   LIKE($$SVAR)
      * . Return Code ('1'=ERROR / blank=OK)
     D U$IN35          S              1
      *---------------------------------------------------------------
      * PARM JD_NFYEVE (notify the event)
      * . Function
     D §§FUNZ          S             10
      * . Method
     D §§METO          S             10
      * . Array of variables
     D §§SVAR          S                   LIKE($$SVAR)
      *---------------------------------------------------------------
     D ADDRSK          S             15
     D $X              S              5  0
     D $R              S              5  0
     D A37TAGS         S           4096
     D $$VAL           S             50
      *---------------------------------------------------------------
     D* M A I N
      *---------------------------------------------------------------
      *
     C     *ENTRY        PLIST
     C                   PARM                    U$FUNZ
     C                   PARM                    U$METO
     C                   PARM                    U$SVARSK
     C                   PARM                    U$IN35
      *
      * Log
     C                   EVAL      $$VAL='Called JD_003 ' + U$SVARSK            COSTANTE
     C                   DSPLY                   $$VAL
      * Initial settings
     C                   EXSR      IMP0
      *
      * Function / Method
1    C                   SELECT
      * Init
1x   C                   WHEN      U$FUNZ='INZ'
     C                   EXSR      FINZ
      * Invoke (empty subroutine in this case)
1x   C                   WHEN      U$FUNZ='ESE'
     C                   EXSR      FESE
      * Detach (empty subroutine in this case)
1x   C                   WHEN      U$FUNZ='CLO'
     C                   EXSR      FCLO
1e   C                   ENDSL
      * Final settings
     C                   EXSR      FIN0
      * End
     C                   SETON                                        RT
      *---------------------------------------------------------------
     C/COPY QILEGEN,£INZSR
      *--------------------------------------------------------------*
    RD* Init
      *--------------------------------------------------------------*
     C     FINZ          BEGSR
      *
1    C                   SELECT
1x   C                   WHEN      U$METO='A37TAGS'
     C                   EVAL      A37TAGS=$$SVAR
      * Log
     C                   EVAL      $$VAL='A37TAGS ' + A37TAGS
     C                   DSPLY                   $$VAL
      *
1x   C                   WHEN      U$METO='POSTINIT'
      * Get port to listen to the socket
     C                   EVAL      ADDRSK=$$SVAR
      * Log
     C                   EVAL      $$VAL='POSTINT ' + ADDRSK
     C                   DSPLY                   $$VAL
      *
2    C                   IF        ADDRSK<>''
3    C                   DO        *HIVAL
      * I listen to the socket
     C                   CLEAR                   BUFFER
     C                   CLEAR                   BUFLEN
     C                   EVAL      IERROR=''
      *
     C                   CALL      'JD_RCVSCK'
     C                   PARM                    ADDRSK
     C                   PARM                    BUFFER
     C                   PARM                    BUFLEN
     C                   PARM                    IERROR
      * Log
     C                   EVAL      $$VAL='BUFFER ' + BUFFER
     C                   DSPLY                   $$VAL
      * Log
     C                   EVAL      $$VAL='BUFLEN ' + %CHAR(BUFLEN)
     C                   DSPLY                   $$VAL
      * Log
     C                   EVAL      $$VAL='IERROR ' + IERROR
     C                   DSPLY                   $$VAL
      *
4    C                   IF        IERROR<>''
      * Socket error
     C                   EVAL      U$IN35='1'
     C                   LEAVE
4x   C                   ELSE
      * If buffer received
5    C                   IF        BUFLEN>0
     C                   EVAL      $XML=%SUBST(BUFFER:1:BUFLEN)
     C                   EVAL      §§FUNZ='NFY'
     C                   EVAL      §§METO='EVE'
     C                   EVAL      §§SVAR=$XML
      * Log
     C                   EVAL      $$VAL='NFYEVE ' + $XML
     C                   DSPLY                   $$VAL
      * Log
     C                   EVAL      $$VAL='NFYEVE ' + A37TAGS
     C                   DSPLY                   $$VAL
      * Notify the event (the license plate)
     C                   CALL      'JD_NFYEVE'
     C                   PARM                    §§FUNZ
     C                   PARM                    §§METO
     C                   PARM                    §§SVAR
     C                   PARM                    A37TAGS
5e   C                   ENDIF
4e   C                   ENDIF
      *
3e   C                   ENDDO
2x   C                   ELSE
      * Empty address: Error
     C                   EVAL      U$IN35='1'
2e   C                   ENDIF
      *
1e   C                   ENDSL
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Invoke
      *--------------------------------------------------------------*
     C     FESE          BEGSR
      *
      * This function doesn't do anything and is always successfull
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Detach
      *--------------------------------------------------------------*
     C     FCLO          BEGSR
      *
      * This function doesn't do anything and is always successfull
      *
     C                   ENDSR
      *---------------------------------------------------------------
    RD* Initial subroutine (as *INZSR)
      *--------------------------------------------------------------*
     C     £INIZI        BEGSR
      * Every Sme.UP program encapsulates *INZSR in a /COPY.
      * So we provide £INIZI subroutine to do the same job
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Initial settings
      *--------------------------------------------------------------*
     C     IMP0          BEGSR
      *
      * Clear error field
     C                   EVAL      U$IN35=*BLANKS
      *
     C                   EVAL      $$SVAR=U$SVARSK
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Final settings
      *--------------------------------------------------------------*
     C     FIN0          BEGSR
      *
      *
     C                   ENDSR
      *--------------------------------------------------------------*