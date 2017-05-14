#!/bin/bash
STDOUT=output
STDERR=errors
EXE='java -cp /home/luanb/Luan/Unicamp/11_Semestre/MO824/trabalho/workspace/Atividade6/TTTPlot/Genetic/bin/ga.jar ga.ga.GA_TTTPlot'
INSTANCE='/home/luanb/Luan/Unicamp/11_Semestre/MO824/trabalho/workspace/Atividade6/TTTPlot/Genetic/instances/qbf100'
TARGET_COST=-750
CONSTRUCTION_TYPE=padrao
POPULATION_SIZE=3
MUTATION_TAX=2
FIT_RATE=0
LOCUS_TAX=5

$EXE $INSTANCE $CONSTRUCTION_TYPE $POPULATION_SIZE $MUTATION_TAX $TARGET_COST $FIT_RATE $LOCUS_TAX 1> $STDOUT 2> $STDERR
