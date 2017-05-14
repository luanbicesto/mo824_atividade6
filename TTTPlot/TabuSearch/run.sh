#!/bin/bash
STDOUT=output
STDERR=errors
EXE='java -cp /home/luanb/Luan/Unicamp/11_Semestre/MO824/trabalho/workspace/Atividade6/TTTPlot/TabuSearch/bin/ts.jar tabusearchheuristic.problems.qbf.solvers.TS_QBF_TTTPlot'
INSTANCE='/home/luanb/Luan/Unicamp/11_Semestre/MO824/trabalho/workspace/Atividade6/TTTPlot/TabuSearch/instances/qbf040'
TARGET_COST=-251
TENURE=20
ITERATION_TO_START_INTENSIFICATION=800
ITERATIONS_OF_INTENSIFICATION=400
PERCENTAGE_FIXED_ITEMS=20

$EXE $INSTANCE $TENURE $ITERATION_TO_START_INTENSIFICATION $ITERATIONS_OF_INTENSIFICATION $PERCENTAGE_FIXED_ITEMS $TARGET_COST 1> $STDOUT 2> $STDERR
