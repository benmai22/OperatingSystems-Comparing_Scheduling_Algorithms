# OperatingSystems-Comparing_Scheduling_Algorithms

Project objective:

Study the impact of different scheduling algorithms on the average
turnaround time of concurrent processes.

Description

A simulation mimics the execution of n different processes under different
scheduling algorithms. The simulation maintains a table that reflects the
current state of the system:

The table is initialized as follows:

• The field "active" indicates whether the process is currently competing
for the CPU. The value becomes 1 at the time of process arrival and 0 at
the time of process termination. Initially, the value is set to 1 for all
processes with arrival time Aᵢ = 0.

• Each Aᵢ is an integer chosen randomly from a uniform distribution
between 0 and some value k, where k is a simulation parameter.

• Each Tᵢ is an integer chosen randomly from a normal (Gaussian)
distribution with an average d and a standard deviation v, where d and v
are simulation parameters.

• Each Rᵢ is initialized to Tᵢ, since prior to execution, the remaining time is
equal to the total CPU time required.

