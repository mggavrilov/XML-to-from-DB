# 34. XML to/from relational DB applications

Consider a relational DB with tables presenting the relation Vendors n-------m; Products. Construct a Java
application with HyperSonic DB or other embedded database, which extracts data into valid XML files with proper
DTD’s, and then does the opposite process.
Students have to demonstrate the XML documents and their DTD validation, and theirs processing by the application.
More, it is required a MS Word document describing the task realization (4-7 pages).
Resources: HyperSonic Java DB link - http://hsqldb.org/
 

1	Въведение

Настоящият документ представлява документация на курсов проект за предмета „XML технологии за семантичен Уеб“ на тема „XML to/from relational DB applications“.

XML  се утвърждава като стандартен формат за данни в Интернет. Различни организации и потребители го използват за обмен на големи количества от данни. Разработването на техики за съхранение и добиване на информация от XML файлове са основни проблеми при сблъсъка между XML и релационни бази от данни. [1]
Често се налага софтуерните системи да могат да комуникират с външни системи по различни начини. Това включва приемане и изпращане на XML файлове, както и възможност за тяхното обработване и съхраняване на информацията им в база от данни. Този проект решава и двата проблема – конвертиране на записи от релационна база от данни в XML файлове и валидация, и конвертиране на XML файлове към записи в релационна база от данни.
Програмата е реализирана на програмния език Java (JRE  1.8), с помощта на библиотеката JAXB . Ще бъдат разгледани настройването на базата от данни, свързването и изпращането на заявки към нея, конвертирането на записи от базата данни към XML файлове и обратно, както и валидация чрез DTD  в Java.

 
2	Анализ на решението

2.1	Работен процес
Програмата очаква да има достъп до релационна база от данни, в която са налични таблиците „Vendors” и “Products”, за да може да прочете записите от тях. Тя запазва всеки прочетен запис в обект и след това всички обекти в XML файл. След успешна валидация на генерирания XML файл с предефинирания DTD файл, програмата извършва обратния процес: прехвърлянето на записите от XML файла в релационната база от данни.

2.2	Структура на съдържанието
 
Накратко:
Таблица vendors (id, name, address).
Таблица (id, vendor_id (FK), name, description, price, quantity)

Примерна структура на XML файловете:
vendors.xml
<vendors>
	<vendor id=”v1”>
		<name></name>
		<address></address>
	</vendor>
</vendors>

products.xml
<products>
	<product id=”p1” vendorId=”v1”>
		<name></name>
		<description></description>
		<price></price>
		<quantity></quantity>
	</product>
</products>

shop.xml
обединява XML файловете vendors.xml и products.xml
има референция към външния DTD файл shop.dtd, където са посочени правилата за валидация

shop.dtd
Поделементите на vendor елемента са name и address. Има единствен атрибут id.
Поделементите на product са name, description, price и quantity. Има 2 атрибута – id и vendorId.
product и vendor id са ID #REQUIRED, защото са primary keys в съответните таблици, а vendorId е IDREF #REQUIRED, защото е foreign key.
Тъй като стойностите на атрибутите не могат да започват с цифри, пред id-тата на vendor се добавя буквата „v” а пред тези на product – „p”, които са само за xml файловете и се премахват в програмата, преди да се вмъкнат обратно в базата от данни.

<!ENTITY vendorsfile SYSTEM "vendors.xml">
<!ENTITY productsfile SYSTEM "products.xml">

Тези entity-та са декларирани, за да може в shop.xml файла да реферираме съответните XML файлове чрез &vendorsfile; и &productsfile;


3	Дизайн

За реализацията на проекта е използвана вградената база от данни HyperSonic (HSQLDB).
Настройки на базата от данни:
Име на базата: test
Потребител: sa
Парола: без парола /празна/
Type: HSQL Database Engine Server
Необходимите заявки за инициализиране на базата от данни (създаването на таблици и попълването им с примерни данни) се намират във файла db.sql. Първо се изпълняват CREATE TABLE statement-ите, а след това INSERT statement-ите.


Алгоритъм на програмата:
-	Изчитат се всички записи от таблиците vendors и products и се записват съответно във файловете vendors.xml и products.xml
-	Предефинираният файл shop.xml, съдържащ entity референции към горепосочените файлове, се валидира с помощта на предефинирания shop.dtd файл.
-	Ако валидацията премине без грешки, означава, че успешно сме прехвърлили записите от базата данни към XML файлове. Преминаваме към обратния процес – прехвърляне на записите от XML файловете към базата от данни.
-	Изтриваме всички записи от vendors и products таблиците в базата от данни.
-	Четем записите от XML файловете и ги записваме в базата от данни. Необходимо е първо да запишем vendors, за да избегнем нарушения по foreign key constraints при вмъкване на данни в products таблицата. 

За реализацията на програмата са използвани библиотеките:
-	JAXB (Java Architecture for XML Binding – javax.xml.bind)
-	DOM validation (Document Object Model - javax.xml.parsers)
-	SAX validation (Simple API for XML - org.xml.sax)
-	JDBC (Java Database Connectivity – java.sql)

Разяснения по реализацията:
Vendor и Product класовете съдържат член-данни, съответстващи на колоните на таблиците vendors и products в базата от данни, както и get и set методи за всеки от тях.

Анотациите @XmlAttribute и @XmlElement преди set методите указват на JAXB кои член-данни ще бъдат съответно атрибути и елементи на съответния елемент родител (vendor или product).

Анотацията @XmlRootElement пред името на класа указва, че това е елементът корен (vendor или product).

Анотацията @XmlType(propOrder = { }) пред името на класа служи, за да се overwrite-не подредбата по подразбиране на елементите в XML по азбучен ред, като позволява да се укаже произволна подредба. В случая се използва същата подредба като тази в таблиците в базата от данни.

Класовете Vendors и Products са агрегиращи и съдържат съответно списъци (List) от елементи vendor и product.

Анотацията @XmlElement(name = "product") се изполва, за да се подсигурим, че в крайния xml файл поделементите на products ще се казват product.

Метод readEntriesFromDB – четем всички записи от таблиците vendors и products. За всеки прочетен запис създаваме нов обект от съответния клас (Vendor или Product) и го добавяме в списъка на отговорния за него агрегиращ клас (Vendors или Products). 

Метод convertDBEntriesToXMLFiles – Използваме JAXB Marshaller, за да конвертираме Vendors и Products агрегиращите класове съответно във vendors.xml и products.xml

Както беше споменато по-горе, добавяме “v” пред id-тата на vendor-ите и “p” пред id-тата на product-ите.

JaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
Тази опция позволява изходният файл да бъде записан с human-readable форматиране.


jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
Тази опция премахва standalone=”yes” от XML файловете, за да могат да бъдат свързани в shop.xml

Добавени са 2 метода за валидация на XML с DTD:
-	Чрез DOM (метод validateDOM)
-	Чрез SAX (метод validateSAX)

За реализацията и на двата метода е заимстван код от:
http://www.rgagnon.com/javadetails/java-0668.html

При възникнала грешка, и двата метода показват нейните детайли и stack trace за по-лесно дебъгване.

За момента се използва само SAX валидирането, но DOM валидирането е коментирано, така че лесно може да бъде превключено.

Ако валидацията премине без грешки, означава, че успешно сме пренесли записите от базата данни към XML файлове. В такъв случай, можем да преминем към обратния процес.

Метод deleteDBEntries – Изтрива всички записи от таблиците products и vendors (в този ред, за да се избегнат нарушения на foreign key constraints). След като бъдат изтрити, потребителят е уведомен за това и програмата го подканва да провери ръчно в базата от данни, за да се увери, че записите са изтрити. След като това се случи, той може да натисне ENTER, при което изпълнението ще продължи.

Метод convertXMLFilesToDBEntries – Използваме JAXB Unmarshaller, за да конвертираме vendors.xml и products.xml файловете съответно към обекти на Vendors и Products класовете.

Както беше споменато и по-горе, премахваме “v” пред id-тата на vendor-ите и “p” пред id-тата на product-ите, за да можем да ги запишем в базата от данни като цели числа, както са дефинирани в схемата ѝ. 

За всеки прочетен запис от XML файловете, запазен в списъците на съответния агрегиращ клас, изпълняваме INSERT, докато не изчерпаме всички записи.

Накрая, сравняваме дали първоначалният брой на записите във vendors и products таблиците съответства на техния брой след новото им вмъкване в базата. Ако това е вярно, изпълнението на програмата е завършило успешно.


4	Тестване

По време на изпълнението си, програмата показва уведомителни съобщения на потребителя за това какво действие се е извършило или предстои да бъде извършено. Също уведомява и за възникнали непредвидени грешки или изключения по време на изпълнението. Всичко това помага при дебъгване. XML и DTD файлът са тествани с примерни данни, въведени в базата от данни (налични в db.sql файла) под браузъра Chrome 68.


5	Заключение и възможно бъдещо развитие

При разработването на програмата бяха срещнати проблеми с разминаване между ID-тата на елементите в базата от данни и тези в XML файла, поради EBNF „Name” [6] ограничението на DTD за съдържанието на “ID” атрибут. В релационната база от данни тези ID-та са есествени числа, но ограничението в DTD не позволява съдържанието на атрибута да започва с цифра. Затова са добавени съответно буквите “v” и “p” пред ID-тата за “Vendor” и “Product”.
Въпреки срещнатите трудности, DTD валидацията на XML е интуитивна и лесна за използване. Интеграцията с Java е проста и крайният резултат от обработването на данните дава желания резултат без голям „overhead”.
Тази програма би могла да намери широко приложение при софутер, работещ с различни източници на информация, когато искаме да получим и съхраним данни в XML формат или да ги изпратим на външна система.
За бъдещо развитие на програмата може да бъде имплементирано решение, използващо reflection, което да може само да разпознава колоните от таблиците на релационна база от данни, без да е необходимо предварително да бъдат известни и описани в специален клас. 
