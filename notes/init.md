# Oppstart

- Hvilke formelle ting må gjøres ?
- Når skal oppgaven leveres?
- Formelle krav til oppgaven, latex etc hvor kan dette finnes?
- Hvor ofte skal veiledning foregå, ønsker i starten ukentlig veiledning, men er veldig åpen for å se ann behovet.
- Må finne de to små emnene jeg skal ha i tillegg. TDT63 - Kvalitet av modeller og modelleringsspråk, mobile systemer.

# Initielle tanker

- Ønsker å se på tools for å forbedre programvarearkitektur/programvaredesign, slik at potensielle smells blir oppdaget på et tidligere tidspunkt. Finnes masse tools for å oppdage code-smells og potensielle feil, (lintere) men på arkitektur nivå har jeg ikke funnet så mye. Automatisk validering i henhold til en gitt arkitektur (mvvm, mvc ..) eller foreslå design i henhold til kjente design patterns. Ønsker å se på verktøy som kan analysere eksisterende kode, og som kunne blitt brukt for hjelp under utvikling og QA.

- Skal jeg se på programvare-arkitektur eller programvare-design og patterns?
- Skal det være et generelt program, eller skal analysen vite noe om programmet som skal analyseres? Vil det være en stor hjelp å spesifisere arkitektur på forhånd, eller hvilke design patterns som skal analyseres for?

- Skal man gå igjennom SOLID og andre kjente lister med prinsipper for god programvare-design og arkitektur, og finne violations på disse?

- Må finne ut hva som finnes av verktøy.

  - CodePro Analytix
  - E-Quality (kan ikke finne, men det er dette verktøyet det ene paperet har utviklet for å detektere design patterns)
  - Android Lint, andre lintere
  - Code climate

- Kan man heller detektere anti-patterns? En liste av disse ?

- Kan man bruke E-Quality lignende programvare for å markere hvor i koden hvilke design patterns er brukt? Kan dette være en plugin som kan brukes i IDE. Kan analysen avdekke design patterns som er brukt, men klasser som har feil navngivning, eventuelt utviklere som uvitende har brukt et kjent pattern uten å dokumentere det. Navnet på klassen burde gjengi hvilket pattern som er brukt.

* generere kode ut fra hva som er vanlig i prosjektet eller i andre prosjekter, ut fra spesifikt syntax..

## Possible ways of attacking the problem

There are many ways of providing tools that can help with improving the architechture and design. Below i have listed a list of ways at different abstraction levels.

### Architecture level

- Automatic validation according to a specified architecture
- Automatic generation of code according to a specified architecture

### Design pattern level

- Automatic detection of design patterns in the application
  - A graph mining approach for detecting identical design structures in object-oriented design models.pdf
  - CodeMr
- Automatic generation of design patterns
  - Builder generator is a plugin in IntelliJ - can this be generalized. If we know the structure of all the patterns, can the tool generate all of the patterns?

* Automatic detection of common architecture smells / Automatic detection of misuse of design patterns

### Design principles level

- Automatic detection of violation of common design principles like SOLID
- Other lists of design principles?

### Testable code

- Automatic detection of un-testable code, or detection of how testable the code is
- Creating objects inside methods instead of passing the object as a parameter. Could easily be detected. Only POJO objects is allowed inside method since these are testable. Maybe all dependencies that are not of simple types should be injected instead of created?. (Dependency inversion - D in Solid)
- Tool that warns the programmer, but that the programmer can "accept"
- List of warnings of untestable code http://misko.hevery.com/code-reviewers-guide/

### Idiomatic code

- Automatic detection of how idiomatic the code is, or solutions to make the code more idiomatic
  - Tools like detekt for Kotlin will do many of these simple tasks like formatting and val/var things.
  - How can one enforce the use of language features like `.apply{} .run{} .with{}` etc.
  - Functional instead of imperative where possible

### Code metrics

- Using code metrics such as number of methods per class, number of lines, code complexity to give feedback.
- Code-climate, sonarQube and similar tools already does a great job here. Is there any way they can be improved?

### Code generation

- Automatic code completion of common code for solving a common problem (use of libraries etc. Keeping code that is similar consistent accross the application. E.g using a framework. Can improve the overall quality by being consistent.) Data mining of similar applications.
  - All applications using e.g firebase-realtime can be analyzed and a flow of how we get data can be used in auto-complete.
  - Tabnine

### Debugging tools

### Memory leak detection

### Parallell code detection

## Other thoughts

Automatic recognition of how clean the code is, could be a score based on all the above categories. (assignments only focusing on tests running)

- Help in code review. Tool can give warnings that the developer needs to think about. https://en.wikipedia.org/wiki/Automated_code_review
- https://medium.com/feedzaitech/writing-testable-code-b3201d4538eb

Different tools depending on: Which code quality metric to better; stage in development and the level of abstraction.

"Weak warnings"-tools to warn about possible things that will affect the testability of the appliaction.

Paper structure could look something like this

# State of the art for tool support for improving code

## Static analysis

### Architecture level

- Does the code comply with the specified architecture

### Design pattern level

- Detection of common design patterns

### Class level

- Linting

### Code metrics

- loc
- cohesion
- coupling

## Dynamic analysis / runtime analysis

- needs more research
-

## Machine learning

- Autocompletion based on learning from github projects
-

Tool that tries to give the programmer help with deciding names of things. Uses ai and similar approaches as tabnine etc to help the programmer. Names can be predicted based on datatype and historical data of what the e.g variable is assigned.

dele inn i:

lexikalsk (formatering )
syntax (is the syntax correct)
semantisk (analyse av semantikk - null infering, dead code)
pragmattikk (best practices)

Prøve å dele inn faktiske ulike verktøy i kategorier.

google om andre har klassifisert tools. finne en god inndeling. hvordan har de gjort det.

Målet er å kartlegge.

hecking conventions
tools avoiding using bad practices
tools finding potential bugs
tools checking architectural issues
tools measuring code coverage

granularitet - hvor mye prøver hvert tool å dekke (prosjekt, klasse , metode, variable)

til neste gang: en litt mer presis beskrivelse av hva jeg prøver å oppnå

Forklare i teksten hvordan jeg har kommet frem til alle artiklene og temaene.

fylle inn for hvert avsnitt

systematic litterature review. Kan gjenta om 5 år for å ende opp med noe nytt.

Mapping study.

CodingBuddy - kamerat som gir deg tips underveis. På en måte en en slags linter, men som ikke har nødvendigvis har noen fasit på alt, men gode innspill på f.eks testbarhet.

Til veiledning:
Lurer på om jeg burde begrense området mye mer. For eksempel bare se på tools for Kotlin/Java, JVM.

Må skrive en mye mer nøyaktig methodology for et systematisk litterature review. Inclusion og exclusion criterias. Search terms. Etter en gjennomgang av alle search terms og har eksludert masse må jeg ende opp med en liste av tools.

Formulere forskningsspørsmål:
How can tools for static analysis be improved to better code quality?

hva med kodegenerering, er det relevant?
skal jeg droppe dynamisk analyse?
er det jeg har en ok start?

semantic
spotbugs bytecode analyse

Til veiledning:
Har valgt å fokusere mer på maintainability enn performance, security.
Er dette en god ide for å innskrenke scopet på oppgaven?
Andre innfallsvinlker er å innskrenke valget av tools til et språk.
Eventuelt bare fokusere på struktur-analyse eller inspections.

Gå igjennom metode.

Teorimoduler.

fra veiledning:
savner objectives og hensikt, hva ønskes å oppnå i introduksjonen
litt mer om scope og limitations.

spisse forskningsspørssmål: skal kunne se om jeg har svart på disse. Ikke bare studere et stort tema.

ett spm på klassifisering av teknikker
ett på mangel i verktøy.

burde muligens skrive ned hvorfor jeg ikke har tatt med performance security etc..

kan snevre inn på hvem som bruker verktøyet, platformer, bransje, struktur, eller inspections, fase av utvikling, kan nevnes i introduksjon.

Til veiledning:
Siden sist - jobbet en del med å prøve å finne ut hvilke tools som er relevante. Hvordan velge ut tools på en god måte? Vise sorteringen min.

Sjekk ut nye forskningsspørsmål.
Tror det kan være en fordel å kun se på tools for JVM.

Thor stålhanne

modent jvm
skrive om sortering og hvorfor det ikke gikk.

Tool som kan hjelpe til med å kategorisere tools?

mål og innsnevring

flytte rq opp. snevrer inn for fort. ikke snevre inn i introduksjon

først få oversikt over ting og analysere og kartlegge verktøy.
blitt etterpåklok.

det er et mål å finne bredden av kategorier og finne en snevrere klasse som jeg kan se nærmere på.

- se på maintenance begrepet i innledningen og se man kan bytte det ut med noe, slik at det ikke forveksles med "maintenance" i fossefallsmodellen.

- dele opp bakgrunnskapittelet i sections (kan se på programvarearkitektur boken)

* bakgrunnen kan inneholde informasjon om at det er en dimensjon som går fra abstrakt til best practices.

nevne at det kan hende jeg må snevre inn underveis for å få et fornuftig utvalg av tools.

skrive at dette er et forarbeid til master og et praktisk arbeid.

- metrics i bakgrunn. metrics kan være tegn på om man har brukt prinsipper

- kan muligens ta med related work inn i metoden. Hvordan vet Most tools for static analysis are not based on scientific research,

- Therefore a traditional “systematic literaturereview” only studying literature on the subject is not suitable for this paper. si noe om at forskinngsmetoden ikke passer helt, men at jeg har brukt forkninngsmetode selv om det ikke passer.

systemetatic tool review. adapt the method of traditional literature review. mye om fenomenene som verktøyene handler om, men ikke om verktøyene i seg selv.

bygge opp under innsnevring. kan plotte listen med 400 tools og valget.

- probable bugs / issues

  - multithreaded bugs
    - lock checker
    - race condition analysis
  - internationalization
  - Pluggable types (checker framework)
    - null analysis
    - initialization checker
    - map key checker
    - optional checker
    - interning checker (equality)
    - index checker
    - fake enum checker
    - regex checker
    - format string checker
    - signature string checker
    - gui effect checker
    - units checker
    - signedness checker
    - aliasing checker
    - purity checker
    - constant value checker
    - reflection checker
    - subtyping checker
  - incorrect api usage
  - error handling
  - serialisation
  - resource management
  - tostring issues
  - visibility
  - .... more errors checks and issues lots of them

- metrics

  - class metrics
  - method metrics
  - project metrics
  - purity? Check for purity of classes

- style

  - encapsulation
  - imports
  - regex
  - annotations
  - block checks
  - coding
  - headers
  - control flow issues
  - visibility modifiers
  - documentation
  - structure (methods order etc)
  - confusing
  - unused
  - redundant
  - idiomatic code (java-kotlin detection)

- readability

  - formatting

  - naming conventions

  - spellchecking

- structure analysis

  - design pattern detection
  - testability
  - extensibility
  - modularity

  - design smells

    - detection
    - visualization

  - architecture smells
    https://sourcemaking.com/antipatterns/software-architecture-antipatterns - detection - visualization

  - architecture pattern detection

  - architecture verification

    - Testing

    - Analysis

  - visualization

    - dependency
      - dependency-matrix
      - dependency-graph
      - tree-map
      - call-graphs
      - coupling graph
      - path graph
      - all paths graph
      - cycle graph
      - class inheritance graph
    - technical debt
    - abstractness vs instability

  - dependency cycle detection
  - dependency analysis

  - querying

    - immutability and purity
    - whatever one wants

  - duplicate code detection

- portability

til veiledning:
For eksempel probable bugs kategorien har ekstremt mange ulike ting. Hvordan skal jeg legge frem at dette området er godt
