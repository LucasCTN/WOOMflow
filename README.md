# WOOMflow

Code for the WOOM Flow module for Modelio. This project is described in the article [A Tool for Modeling and Behavioral Tests Generation of Embedded Software](https://doi.org/10.20906/sbai.v1i1.2661) by Thiago Campos, Lucas Campos and Rogério Atem.

This module interacts directly with Modelio's Finite State Machine diagramming functionality, allowing the resulting diagram to be exported to the SCXML format. The model can then be used to produce a table that permits the assignment of responsibilities. This table gathers information that specifies the internal interactions of the system to be developed, which was previously specified through UML modelling of the behaviour expected by the system at a higher level.

With the generated FSM and the assignments mapped by the table, it is possible to produce the skeleton of the embedded system, as well as the methods that will test it.
