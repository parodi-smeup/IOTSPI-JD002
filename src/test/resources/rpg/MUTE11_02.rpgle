     V*=====================================================================
     V* Date      Release Au Description
     V* dd/mm/yy  nn.mm   xx Brief description
     V*=====================================================================
     V* 19/10/18  V5R1    AS Created
     V* 04/02/19  V5R1    AS Comments translated to english
     V* 05/07/19  V5R1    BMA Renamed JD_002 in MUTE11_02
     V* 16/07/19  V5R1    AD Adjustments to work with RPG Intepreter into IOTSPI Plugin
     V*=====================================================================
     H/COPY QILEGEN,£INIZH
      *---------------------------------------------------------------
     I/COPY QILEGEN,£TABB£1DS
     I/COPY QILEGEN,£PDS
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
      * PARM LISTEN_FLD (Folder listener)
      * . Folder to monitor
     D §§FLD           S           1000
      * . Object name involved in the event
     D §§NAM           S           1000
      * . Object type involved in the event (FILE/FOLDER)
     D §§TIP           S             10
      * . Event type (see "Mode" variable)
     D §§OPE           S             10
      *---------------------------------------------------------------
      * PARM JD_NFYEVE (notify the event)
      * . Function
     D §§FUNZ          S             10
      * . Method
     D §§METO          S             10
      * . Array of variables
     D §§SVAR          S                   LIKE($$SVAR)
      *---------------------------------------------------------------
      * "Folder" variable (input): Folder name
     D $$FLD           S           1000
      * "Mode" variable (input): Events to monitor
      * . *ADD File or folder creation
      * . *CHG File change
      * . *DEL File or foldere deletion
     D $$MOD           S             15
      * "filter" variable (input): Filter to apply
      * . Now we can set up only a single filter and only in form *.extension
     D $$FLT           S           1000
      * . Recursive
     D $$REC           S              5
      *---------------------------------------------------------------
      * Work variables
     D $$EST_FLT       S             10
     D OK              S              1N
     D ADDRSK          S           4096
     D $$VAR           S           4096
     D $X              S              5  0
     D A37TAGS         S           4096
      *---------------------------------------------------------------
      * Buffer received
     D BUFFER          S          30000
      * Length buffer received
     D BUFLEN          S              5  0
      * Error indicator
     D IERROR          S              1
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
      * Initial settings
     C                   EXSR      IMP0
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
      *---------------------------------------------------------------
    RD* Initial subroutine (as *INZSR)
      *--------------------------------------------------------------*
     C     £INIZI        BEGSR
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
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Init
      *--------------------------------------------------------------*
     C     FINZ          BEGSR
      *
1    C                   SELECT
      * .Set variables
1x   C                   WHEN      U$METO='A37TAGS'
     C                   EVAL      A37TAGS=$$SVAR
      *
      * .Post Init (main program, listen to folder changes and fire event)
1x   C                   WHEN      U$METO='POSTINIT'
     C                   EVAL      ADDRSK=$$SVAR
      * .Folder(c:/myFolder/xxx)|Mode(C;M;D)|Filter(txt;pdf;jpg;doc)
      *  Recursive(true)
     C                   EXSR      CARVAR_INZ
2    C                   IF        ADDRSK<>''
3    C                   DO        *HIVAL
      * I listen to folder changes
     C                   CLEAR                   BUFFER
     C                   CLEAR                   BUFLEN
     C                   EVAL      IERROR=''
     C                   CALL      'JD_LSTFLD'
     C                   PARM                    ADDRSK
     C                   PARM                    BUFFER
     C                   PARM                    BUFLEN
     C                   PARM                    IERROR
4    C                   IF        IERROR<>''
     C                   EVAL      U$IN35='1'
     C                   LEAVE
4x   C                   ELSE
      * If buffer received
5    C                   IF        BUFLEN>0
      * Extract data (name, type, operation)
     C                   EXSR      EXTRACT_DTA
      * Check if operation returned is one of those managed
     C                   EXSR      CHKOPE
6    C                   IF        NOT(OK)
     C                   ITER
6e   C                   ENDIF
      * Check if file meets the filter
     C                   EXSR      CHKFLT
6    C                   IF        NOT(OK)
     C                   ITER
6e   C                   ENDIF
      * .Build variabled to notify the event
     C                   EXSR      COSVAR_EVE
     C                   EVAL      §§FUNZ='NFY'
     C                   EVAL      §§METO='EVE'
     C                   EVAL      §§SVAR=$$VAR
      *
      * .Notify the event (the license plate)
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
    RD* Check if operation is one of those managed
      *--------------------------------------------------------------*
     C     CHKOPE        BEGSR
      *
1    C                   IF        $$MOD=*BLANKS OR
     C                             %SCAN(%TRIM(§§OPE):$$MOD)>0
     C                   EVAL      OK=*ON
1x   C                   ELSE
     C                   EVAL      OK=*OFF
1e   C                   ENDIF
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Check if file meets the filter
      *--------------------------------------------------------------*
     C     CHKFLT        BEGSR
      *
1    C                   SELECT
      * If no filter, file is OK
1x   C                   WHEN      $$FLT=*BLANKS
     C                   EVAL      OK=*ON
      * If I have a filter and the event concerns a folder: NO OK
1x   C                   WHEN      $$FLT<>*BLANKS AND §§TIP<>'FILE'
     C                   EVAL      OK=*OFF
1x   C                   OTHER
     C                   EVAL      $$EST_FLT=%SUBST($$FLT:3)
2    C                   IF        %LEN(%TRIM(§§NAM))>%LEN(%TRIM($$EST_FLT))+1
     C                             AND
     C
     C                             %TRIM(%SUBST(%TRIM(§§NAM):%LEN(%TRIM(§§NAM))
     C                              -%LEN(%TRIM($$EST_FLT))))
     C                             ='.'+%TRIM($$EST_FLT)
     C                   EVAL      OK=*ON
2x   C                   ELSE
     C                   EVAL      OK=*OFF
2e   C                   ENDIF
1e   C                   ENDSL
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Build variables to notify the event
      *--------------------------------------------------------------*
     C     COSVAR_EVE    BEGSR
      *
      * Extract data (name, type, operation)
     C                   EXSR      EXTRACT_DTA
      *
     C                   EVAL      §§SVAR='Object name(' + §§NAM + ') ' +       COSTANTE
     C                                    'Object type(' + §§TIP + ') ' +       COSTANTE
     C                                 'Operation type(' + §§OPE + ') '         COSTANTE
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Load variables for INZ function
      *--------------------------------------------------------------*
     C     CARVAR_INZ    BEGSR
      *
      * Vars example:
      *
      * .Folder(c:/myFolder/xxx)|Mode(C;M;D)|Filter(txt;pdf;jpg;doc)
      *  Recursive(true)
     C                   CLEAR                   $ATTRI            5 0
     C                   CLEAR                   $BRACK            5 0
     C                   CLEAR                   $ATTLEN           5 0
      * Folder
     C                   EVAL      $ATTRI=%SCAN('Folder(':ADDRSK)               Index of Folder(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+7
     C                   EVAL      $BRACK=%SCAN(')':ADDRSK:$ATTRI)              Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      $$FLD=%SUBST(ADDRSK:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      * Mode
     C                   EVAL      $ATTRI=%SCAN('Mode(':ADDRSK)                 Index of Mode(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+5
     C                   EVAL      $BRACK=%SCAN(')':ADDRSK:$ATTRI)              Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      $$MOD=%SUBST(ADDRSK:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      * Filter
     C                   EVAL      $ATTRI=%SCAN('Filter(':ADDRSK)               Index of Filter(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+7
     C                   EVAL      $BRACK=%SCAN(')':ADDRSK:$ATTRI)              Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      $$FLT=%SUBST(ADDRSK:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      * Recursive
     C                   EVAL      $ATTRI=%SCAN('Recursive(':ADDRSK)            Index of Filter(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+10
     C                   EVAL      $BRACK=%SCAN(')':ADDRSK:$ATTRI)              Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      $$REC=%SUBST(ADDRSK:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Extract data from response
      *--------------------------------------------------------------*
     C     EXTRACT_DTA   BEGSR
      *
     C                   EVAL      $$VAR=%SUBST(BUFFER:1:BUFLEN)
      * Vars example:
      * Name(c:/myFolder/xxx)|Type(FILE)|Operation(C)
      *
     C                   CLEAR                   $ATTRI            5 0
     C                   CLEAR                   $BRACK            5 0
     C                   CLEAR                   $ATTLEN           5 0
      * Name
     C                   EVAL      $ATTRI=%SCAN('Name(':$$VAR)                  Index of Folder(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+5
     C                   EVAL      $BRACK=%SCAN(')':$$VAR:$ATTRI)               Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      §§NAM=%SUBST($$VAR:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      * Type
     C                   EVAL      $ATTRI=%SCAN('Type(':$$VAR)                  Index of Type(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+5
     C                   EVAL      $BRACK=%SCAN(')':$$VAR:$ATTRI)               Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      §§TIP=%SUBST($$VAR:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      * Operation
     C                   EVAL      $ATTRI=%SCAN('Operation(':$$VAR)             Index of Operation(
0    C                   IF        $ATTRI>0
     C                   EVAL      $ATTRI=$ATTRI+10
     C                   EVAL      $BRACK=%SCAN(')':$$VAR:$ATTRI)               Index of )
1    C                   IF        $BRACK>0
     C                   EVAL      $ATTLEN=$BRACK-$ATTRI                        Value length
     C                   EVAL      §§OPE=%SUBST($$VAR:$ATTRI:$ATTLEN)
1e   C                   ENDIF
0e   C                   ENDIF
      *
     C                   ENDSR
      *--------------------------------------------------------------*
    RD* Invoke
      *--------------------------------------------------------------*
     C     FESE          BEGSR
      *
      * This function doesn't do anything and is always successfull
     C                   EVAL      U$IN35=*BLANKS
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