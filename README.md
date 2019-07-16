# IOTSPI-FileSystemPlugin

Versione esterna del plugin contenuto in SmeupProvider

- v1.0.0: prima versione esterna compatibile con IOTSPI 1.0.0

# Cosa sapere del plugin

La classe del plugin da inserire nello script è com.smeup.iotspi.filesystem.FileSystemConnector.  
I parametri di inizializzazione sono
- PATH: percorso della cartella da monitorare
- FILTER: eventuali filtri sui file (es: *.txt per monitorare solo file con estensione 'txt')
- RECURSIVE: true/false se controllare o meno anche le sottocartelle
- EVENT: tipo di evento da monitorare (C = creazione, M = modifica, D = cancellazione)

I valori previsti per FILTER e EVENT possono essere composti, separando i valori multipli con il carattere ';'.  
Es: 
- FILTER= *.txt;*.csv
- EVENT= C;M

Al verificarsi del tipo di evento desiderato, nella cartella monitorata, per il tipo di file impostato, viene inviato al sistema un evento composto dai seguenti valori:
- EVENT = evento riscontrato
- PATH = file al quale è relativo l'evento
- DIMENSION = dimensione del file
- DATETIME = istante in cui si è riscontrato l'evento (in formato GMT)
