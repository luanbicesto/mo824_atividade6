#!/bin/bash
STDOUT=output
STDERR=errors
EXE='java -cp /home/luanb/Luan/Unicamp/11_Semestre/MO824/trabalho/workspace/Atividade6/TTTPlot/GRASP/bin/grasp.jar grasp.problems.qbf.solvers.GRASP_QBF_TTTPlot'
INSTANCE='/home/luanb/Luan/Unicamp/11_Semestre/MO824/trabalho/workspace/Atividade6/TTTPlot/GRASP/instances/qbf040'
TARGET_COST=-251
ALPHA=0.05

$EXE $INSTANCE $ALPHA $TARGET_COST 1> $STDOUT 2> $STDERR
