# Web-Anwendung

Angular-Projekt. Wird von Rolle D ab Woche 2 aufgesetzt.

## Anlegen

    npm install -g @angular/cli
    ng new web-app --routing --style=css
    ng add @angular/localize

## Vier Dialoge

- Essen (CRUD, nur Admin darf aendern)
- Essensplan (CRUD, Filtern nach Woche)
- Essensbewertung (Sterne, Pflichttext, Foto)
- Login

## Hinweise

Die Kamera-Anforderung betrifft die iOS-App. Im Browser laesst sich das
ueber `getUserMedia` loesen, ist aber laut Aufgabenstellung nur fuer das
Mobile-Device zwingend.

Internationalisierung deutsch und englisch ist Pflicht. Angular bringt
das mit `@angular/localize` mit.
