# Rafael Santiago Moreno Velasquez

# Sebastian Castillejo Angulo


### Escuela Colombiana de Ingeniería
### Arquitecturas de Software - ARSW
## Ejercicio Introducción al paralelismo - Hilos - Caso BlackListSearch


### Dependencias:
####   Lecturas:
*  [Threads in Java](http://beginnersbook.com/2013/03/java-threads/)  (Hasta 'Ending Threads')
*  [Threads vs Processes]( http://cs-fundamentals.com/tech-interview/java/differences-between-thread-and-process-in-java.php)

### Descripción
  Este ejercicio contiene una introducción a la programación con hilos en Java, además de la aplicación a un caso concreto.
  

**Parte I - Introducción a Hilos en Java**

1. De acuerdo con lo revisado en las lecturas, complete las clases CountThread, para que las mismas definan el ciclo de vida de un hilo que imprima por pantalla los números entre A y B.
2. Complete el método __main__ de la clase CountMainThreads para que:
	1. Cree 3 hilos de tipo CountThread, asignándole al primero el intervalo [0..99], al segundo [99..199], y al tercero [200..299].

	![alt text](img/image-2.png)

	2. Inicie los tres hilos con 'start()'.
	
	![alt text](img/image-5.png)

	3. Ejecute y revise la salida por pantalla. 

	![alt text](img/image.png)
	
	### los cambios fueron realizados en la clase de Threads.java y CountThreadsMain.java 

	4. Cambie el incio con 'start()' por 'run()'. Cómo cambia la salida?, por qué?.

	![alt text](img/image-4.png)
	![alt text](img/image-3.png)

	en este caso con el run() la salida fue secuencial, el star() lo que hace es crear un hilo y corre el run(), pero si solo corremos el run() es hacer un solo llamado al metodo normal

**Parte II - Ejercicio Black List Search**


Para un software de vigilancia automática de seguridad informática se está desarrollando un componente encargado de validar las direcciones IP en varios miles de listas negras (de host maliciosos) conocidas, y reportar aquellas que existan en al menos cinco de dichas listas. 

Dicho componente está diseñado de acuerdo con el siguiente diagrama, donde:

- HostBlackListsDataSourceFacade es una clase que ofrece una 'fachada' para realizar consultas en cualquiera de las N listas negras registradas (método 'isInBlacklistServer'), y que permite también hacer un reporte a una base de datos local de cuando una dirección IP se considera peligrosa. Esta clase NO ES MODIFICABLE, pero se sabe que es 'Thread-Safe'.

- HostBlackListsValidator es una clase que ofrece el método 'checkHost', el cual, a través de la clase 'HostBlackListDataSourceFacade', valida en cada una de las listas negras un host determinado. En dicho método está considerada la política de que al encontrarse un HOST en al menos cinco listas negras, el mismo será registrado como 'no confiable', o como 'confiable' en caso contrario. Adicionalmente, retornará la lista de los números de las 'listas negras' en donde se encontró registrado el HOST.

![](img/Model.png)

Al usarse el módulo, la evidencia de que se hizo el registro como 'confiable' o 'no confiable' se dá por lo mensajes de LOGs:

INFO: HOST 205.24.34.55 Reported as trustworthy

INFO: HOST 205.24.34.55 Reported as NOT trustworthy


Al programa de prueba provisto (Main), le toma sólo algunos segundos análizar y reportar la dirección provista (200.24.34.55), ya que la misma está registrada más de cinco veces en los primeros servidores, por lo que no requiere recorrerlos todos. Sin embargo, hacer la búsqueda en casos donde NO hay reportes, o donde los mismos están dispersos en las miles de listas negras, toma bastante tiempo.

Éste, como cualquier método de búsqueda, puede verse como un problema [vergonzosamente paralelo](https://en.wikipedia.org/wiki/Embarrassingly_parallel), ya que no existen dependencias entre una partición del problema y otra.

Para 'refactorizar' este código, y hacer que explote la capacidad multi-núcleo de la CPU del equipo, realice lo siguiente:

1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que haga la búsqueda de un segmento del conjunto de servidores disponibles. Agregue a dicha clase un método que permita 'preguntarle' a las instancias del mismo (los hilos) cuantas ocurrencias de servidores maliciosos ha encontrado o encontró.

![alt text](img/image-24.png)

2. Agregue al método 'checkHost' un parámetro entero N, correspondiente al número de hilos entre los que se va a realizar la búsqueda (recuerde tener en cuenta si N es par o impar!). Modifique el código de este método para que divida el espacio de búsqueda entre las N partes indicadas, y paralelice la búsqueda a través de N hilos. Haga que dicha función espere hasta que los N hilos terminen de resolver su respectivo sub-problema, agregue las ocurrencias encontradas por cada hilo a la lista que retorna el método, y entonces calcule (sumando el total de ocurrencuas encontradas por cada hilo) si el número de ocurrencias es mayor o igual a _BLACK_LIST_ALARM_COUNT_. Si se da este caso, al final se DEBE reportar el host como confiable o no confiable, y mostrar el listado con los números de las listas negras respectivas. Para lograr este comportamiento de 'espera' revise el método [join](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html) del API de concurrencia de Java. Tenga también en cuenta:

	* Dentro del método checkHost Se debe mantener el LOG que informa, antes de retornar el resultado, el número de listas negras revisadas VS. el número de listas negras total (línea 60). Se debe garantizar que dicha información sea verídica bajo el nuevo esquema de procesamiento en paralelo planteado.

	* Se sabe que el HOST 202.24.34.55 está reportado en listas negras de una forma más dispersa, y que el host 212.24.24.55 NO está en ninguna lista negra.

	![alt text](img/image-25.png)


**Parte II.I Para discutir la próxima clase (NO para implementar aún)**

La estrategia de paralelismo antes implementada es ineficiente en ciertos casos, pues la búsqueda se sigue realizando aún cuando los N hilos (en su conjunto) ya hayan encontrado el número mínimo de ocurrencias requeridas para reportar al servidor como malicioso. Cómo se podría modificar la implementación para minimizar el número de consultas en estos casos?, qué elemento nuevo traería esto al problema?

Para no seguir buscando de más, se puede compartir un contador entre todos los hilos (por ejemplo un AtomicInteger). Cada vez que un hilo encuentra una coincidencia lo actualiza, y antes de revisar la siguiente lista mira si ya se llegó al umbral. Si sí, se sale del ciclo y no termina de recorrer su segmento.

Lo nuevo que aparece con esto es que ahora los hilos comparten un estado que todos pueden leer y escribir al mismo tiempo. Eso abre la puerta a condiciones de carrera, así que toca sincronizar el acceso, ya sea con AtomicInteger o con synchronized, para que no se pisen entre ellos.

**Parte III - Evaluación de Desempeño**

A partir de lo anterior, implemente la siguiente secuencia de experimentos para realizar las validación de direcciones IP dispersas (por ejemplo 202.24.34.55), tomando los tiempos de ejecución de los mismos (asegúrese de hacerlos en la misma máquina):

1. Un solo hilo.

![alt text](img/image-17.png)
![alt text](img/image-16.png)

2. Tantos hilos como núcleos de procesamiento (haga que el programa determine esto haciendo uso del [API Runtime](https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html)).

![alt text](img/image-15.png)
![alt text](img/image-11.png)
![alt text](img/image-18.png)

3. Tantos hilos como el doble de núcleos de procesamiento.

![alt text](img/image-10.png)
![alt text](img/image-12.png)
![alt text](img/image-19.png)

4. 50 hilos.

![alt text](img/image-13.png)
![alt text](img/image-20.png)

5. 100 hilos.

![alt text](img/image-7.png)
![alt text](img/image-21.png)



Al iniciar el programa ejecute el monitor jVisualVM, y a medida que corran las pruebas, revise y anote el consumo de CPU y de memoria en cada caso. ![](img/jvisualvm.png)

Con lo anterior, y con los tiempos de ejecución dados, haga una gráfica de tiempo de solución vs. número de hilos. Analice y plantee hipótesis con su compañero para las siguientes preguntas (puede tener en cuenta lo reportado por jVisualVM):

### Resumen de tiempos

Todas las mediciones se hicieron en el mismo equipo,
buscando la dirección dispersa 202.24.34.55, ejecutando cada experimento por separado.
El tiempo corresponde a una sola búsqueda, medido con System.currentTimeMillis() alrededor
de la llamada a checkHost

| Experimento | Hilos | Tiempo (ms) | Aceleración | Eficiencia (aceleración / hilos) |
|---|---|---|---|---|
| 1. Un solo hilo | 1 | 126.492 | 1,0x | 100 % |
| 2. Tantos hilos como núcleos | 12 | 10.701 | 11,8x | 98,5 % |
| 3. El doble de núcleos | 24 | 5.361 | 23,6x | 98,3 % |
| 4. 50 hilos | 50 | 2.609 | 48,5x | 97,0 % |
| 5. 100 hilos | 100 | 1.421 | 89,0x | 89,0 % |

el experimento con 100 hilos se ejecutó dentro de un ciclo de 5 repeticiones, porque una
sola búsqueda dura 1,4 segundos y jVisualVM no alcanza a tomar muestras. El tiempo reportado
sigue siendo el de una búsqueda; lo que cambia es la duración total del proceso.

![alt text](img/image-22.png)

Notamos que cuando se duplica el número de hilos, el tiempo se reduce
aproximadamente a la mitad.

![alt text](img/image-23.png)

La aceleración es el tiempo con un hilo dividido entre el tiempo con N hilos (el
S(n) de Amdahl). La línea punteada es el ideal teórico: aceleración = número de hilos.
Con esta gráfica lo que se busca es mirar qué tan bien se aprovechan los hilos
agregados. Vemos que sigue la línea ideal hasta 50 hilos, pero en 100 empieza a
bajar el rendimiento.

**Parte IV - Ejercicio Black List Search**

1. Según la [ley de Amdahls](https://www.pugetsystems.com/labs/articles/Estimating-CPU-Performance-using-Amdahls-Law-619/#WhatisAmdahlsLaw?):

	![](img/ahmdahls.png), donde _S(n)_ es el mejoramiento teórico del desempeño, _P_ la fracción paralelizable del algoritmo, y _n_ el número de hilos, a mayor _n_, mayor debería ser dicha mejora. Por qué el mejor desempeño no se logra con los 500 hilos?, cómo se compara este desempeño cuando se usan 200?. 

![alt text](img/image.png)

![alt text](img/image-1.png)

### Resumen de tiempos (incluyendo 200 y 500 hilos)

Mediciones tomadas en el mismo equipo (12 núcleos), buscando la dirección dispersa
202.24.34.55, ejecutando cada experimento por separado

| Experimento | Hilos | Tiempo (ms) | Aceleración | Eficiencia (aceleración / hilos) |
|---|---|---|---|---|
| 1. Un solo hilo | 1 | 126.492 | 1,0x | 100 % |
| 2. Núcleos | 12 | 10.701 | 11,8x | 98,5 % |
| 3. Doble de núcleos | 24 | 5.361 | 23,6x | 98,3 % |
| 4. 50 hilos | 50 | 2.609 | 48,5x | 97,0 % |
| 5. 100 hilos | 100 | 1.421 | 89,0x | 89,0 % |
| 6. 200 hilos | 200 | 710 | 178,2x | 89,1 % |
| 7. 500 hilos | 500 | 908 | 139,3x | 27,9 % |

el mejor desempeno no se logra con 500 hilos porque amdahl asume que agregar hilos no cuesta nada, pero en la practica si cuesta con solo 12 nucleos, cada hilo de mas genera overhead

esto se confirma con las pruebas: con 200 hilos se logro el mejor tiempo (710 ms, 178,2x de aceleracion, 89,1% de eficiencia), pero con 500 hilos el tiempo subio a 908 ms. osea que mas de 200 hilos el overhead de manejar tantos hilos supera el beneficio del paralelismo adicional, y el desempeno real se aleja cada vez mas del ideal teorico.

2. Cómo se comporta la solución usando tantos hilos de procesamiento como núcleos comparado con el resultado de usar el doble de éste?.

en base a la parte iii, se puede decir que en ambos casos (12 y 24 hilos) la solucion es eficiente, ya que la tabla muestra que se aprovecha bien el hardware disponible.

tomando el ejemplo de 12 hilos y 24 hilos, al duplicar el numero de hilos, la diferencia en aceleracion y eficiencia es minima 98,5% y 98,3%. esto indica que los hilos no permanecen ocupados el 100% del tiempo en cpu —probablemente porque hay tiempos de espera durante la consulta a cada lista negra, por lo que el doble de hilos aun encuentra trabajo util que hacer mientras otros estan en espera, sin generar contencion significativa por los nucleos disponibles

3. De acuerdo con lo anterior, si para este problema en lugar de 100 hilos en una sola CPU se pudiera usar 1 hilo en cada una de 100 máquinas hipotéticas, la ley de Amdahls se aplicaría mejor?. Si en lugar de esto se usaran c hilos en 100/c máquinas distribuidas (siendo c es el número de núcleos de dichas máquinas), se mejoraría?. Explique su respuesta.

creeria que si mejoraria, ya que no se estarian compartiendo los mismos recursos fisicos (cpu, cache, memoria), a diferencia de la parte III, donde se tenian muchos hilos compitiendo dentro de un mismo equipo.

pero al usar un hilo por maquina, se debe tener en cuenta un elemento nuevos, como la comunicacion y coordinacion entre esas maquinas, por ejemplo, para reunir y sumar los resultados parciales de cada una al final como el ejemplo de clase de join()

en la tabla de la parte III se observa que, al tener 100 hilos en una sola maquina, el desempeno empieza a desviarse de la linea ideal. al repartir un hilo por maquina, se evitaria esa desviacion causada por la contencion de recursos compartidos, acercandose mas a la linea ideal siempre que el costo de comunicacion entre maquinas no sea muy alto.

para el segundo caso  100/c maquinas, tambien mejoraria respecto a una sola maquina, ya que dentro de cada maquina el numero de hilos coincidiria con su numero de nucleos, parecido a los casos de 12 o 24 hilos de la parte III, donde la eficiencia fue buena, y evitando sobrecarga que si se presento con 100 hilos en 12 nucleos.



